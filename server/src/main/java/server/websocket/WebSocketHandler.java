package server.websocket;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.*;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import service.ServiceException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    GameDataAccess gameDataAccess = new GameDataSql();
    AuthDataAccess authDataAccess = new AuthDataSql();
    GameService gameService = new GameService(gameDataAccess,authDataAccess);
    private final String[] colLetters = {"t","a","b","c","d","e","f","g","h"};

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) throws InvalidMoveException {
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT ->connect(command.getAuthToken(), command.getGameID(), ctx.session);
                case MAKE_MOVE ->makeMove(command.getMove(), command.getAuthToken(), command.getGameID(), ctx.session, command.getColor());
                case LEAVE -> leave(command.getAuthToken(), command.getGameID(), ctx.session);
                case RESIGN -> resign(command.getAuthToken(), command.getGameID(), ctx.session);
            }
        } catch (IOException | DataAccessException | InvalidMoveException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String authToken, int gameID, Session session) throws IOException, DataAccessException {

        authorize(session, authToken);
        String username = authDataAccess.getAuth(authToken).username();
        model.GameData gameData = gameDataAccess.getGame(gameID);
        if (gameData == null) {
            ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR,null,null,null);
            errorMsg.setErrorMessage("Error: Invalid Game ID");
            connections.singleSend(session,errorMsg);
            return;
        }
        ChessGame game = gameData.game();
        connections.add(gameID,session);
        ServerMessage loadMessage;
        if (username.equals(gameDataAccess.getGame(gameID).blackUsername())) {
            loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, game, ChessGame.TeamColor.BLACK);
        } else {
            loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, game, ChessGame.TeamColor.WHITE);
        }
        connections.singleSend(session,loadMessage);
        var msg = String.format("%s has connected to the game", username);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null, null);
        connections.broadcast(gameID,session, serverMessage);
    }

    public String getName(int row, int col) {
        return String.format(colLetters[col] + row);
    }

    private void makeMove(ChessMove move,
                          String authToken,
                          int gameID,
                          Session session, ChessGame.TeamColor color) throws DataAccessException, InvalidMoveException, IOException {
        authorize(session, authToken);
        GameData gameData = gameDataAccess.getGame(gameID);
        ChessGame game = gameData.game();
        String username = authDataAccess.getAuth(authToken).username();
        if (game.isGameOver()) {
            ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR,null,null,null);
            errorMsg.setErrorMessage("Error: Game Over");
            connections.singleSend(session,errorMsg);
        } else if (checkObserver(username, gameData)) {
            ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR,null,null,null);
            errorMsg.setErrorMessage("Error: Observers are not able to move pieces");
            connections.singleSend(session,errorMsg);
        } else if (checkTurn(username, gameData)) {
            ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR,null,null,null);
            errorMsg.setErrorMessage("Error: Not your turn yet");
            connections.singleSend(session,errorMsg);
        }
        else {
            Collection<ChessMove> validMoveList = game.validMoves(move.getStartPosition());
            if (validMoveList == null) {
                ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR,null,null,null);
                errorMsg.setErrorMessage("Error: Invalid Move");
                connections.singleSend(session,errorMsg);
            } else {
                ChessBoard board = game.getBoard();
                String pieceName = board.getPiece(move.getStartPosition()).getPieceType().toString();
                int row = move.getEndPosition().getRow();
                int col = move.getEndPosition().getColumn();
                String movePosition = getName(row, col);
                for (ChessMove m : game.validMoves(move.getStartPosition())) {
                    if (m.equals(move)) {
                        game.makeMove(move);
                        GameData newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
                        gameDataAccess.updateGame(gameID, newGameData);
                        var msg = String.format("%s has moved %s to %s",username, pieceName, movePosition);
                        var notifyMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null,null);
                        var loadOtherMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, game, null);
                        var loadPlayerMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, game, null);
                        connections.singleSend(session, loadPlayerMessage);
                        connections.broadcast(gameID,session,loadOtherMessage);
                        connections.broadcast(gameID,session,notifyMessage);
                        specialBroadcast(gameID, username, game, gameData);
                    }
                }
            }
        }
    }

    private void specialBroadcast(int gameID, String username, ChessGame game, GameData gameData) throws DataAccessException, IOException {
        String msg;
        if (username.equals(gameData.blackUsername())) {
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                gameOver(gameID, game, gameData);
                msg = String.format("%s is in checkmate.", gameData.whiteUsername());
                var specialMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null,null);
                connections.broadcast(gameID,null,specialMessage);
            } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
                msg = String.format("%s is in check.", gameData.whiteUsername());
                var specialMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null,null);
                connections.broadcast(gameID,null,specialMessage);
            } else if (game.isInStalemate(ChessGame.TeamColor.WHITE)) {
                gameOver(gameID, game, gameData);
                msg = String.format("%s is in stalemate.", gameData.whiteUsername());
                var specialMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null,null);
                connections.broadcast(gameID,null,specialMessage);
            }
        } else {
            if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                gameOver(gameID, game, gameData);
                msg = String.format("%s is in checkmate.", gameData.blackUsername());
                var specialMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null,null);
                connections.broadcast(gameID,null,specialMessage);
            } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
                msg = String.format("%s is in check.", gameData.blackUsername());
                var specialMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null,null);
                connections.broadcast(gameID,null,specialMessage);
            } else if (game.isInStalemate(ChessGame.TeamColor.BLACK)) {
                gameOver(gameID, game, gameData);
                msg = String.format("%s is in stalemate.", gameData.blackUsername());
                var specialMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null,null);
                connections.broadcast(gameID,null,specialMessage);
            }
        }
    }

    private void gameOver(int gameID, ChessGame game, GameData gameData) throws DataAccessException {
        game.setGameOver(true);
        GameData newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        gameDataAccess.updateGame(gameID, newGameData);
    }

    private boolean checkObserver (String username, GameData gameData) {
        return !username.equals(gameData.blackUsername()) && !username.equals(gameData.whiteUsername());
    }

    private boolean checkTurn (String username, GameData gameData) {

        if (username.equals(gameData.whiteUsername())) {
            return (gameData.game().getTeamTurn().equals(ChessGame.TeamColor.BLACK));
        } else if (username.equals(gameData.blackUsername())) {
            return (gameData.game().getTeamTurn().equals(ChessGame.TeamColor.WHITE));
        }
        return true;
    }

    private void leave(String authToken, int gameID, Session session) throws DataAccessException, IOException {
        authorize(session, authToken);
        String username = authDataAccess.getAuth(authToken).username();
        var msg = String.format("%s left the game", username);
        var notifyMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null, null);
        connections.broadcast(gameID,session, notifyMessage);
        connections.remove(gameID, session);
        GameData gameData = gameDataAccess.getGame(gameID);
        GameData newGameData;
        if (username.equals(gameData.blackUsername())) {
            newGameData = new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        } else if (username.equals(gameData.whiteUsername())) {
            newGameData = new GameData(gameID, null, gameData.blackUsername(),gameData.gameName(), gameData.game());
        } else {
            newGameData = gameData;
        }
        gameDataAccess.updateGame(gameID, newGameData);
    }

    private void resign(String authToken, int gameID, Session session) throws DataAccessException, IOException {
        authorize(session, authToken);
        String username = authDataAccess.getAuth(authToken).username();
        GameData gameData = gameDataAccess.getGame(gameID);
        if (checkObserver(username, gameData)) {
            ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR,null,null,null);
            errorMsg.setErrorMessage("Error: Observers are not able to resign");
            connections.singleSend(session,errorMsg);
            return;
        }
        ChessGame game = gameData.game();
        if (game.isGameOver()) {
            ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR,null,null,null);
            errorMsg.setErrorMessage("Error: Game is already over");
            connections.singleSend(session,errorMsg);
            return;
        }
        gameOver(gameID, game, gameData);
        var msg = String.format("%s resigned from the game", username);
        var notifyMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null, null);
        connections.broadcast(gameID, null, notifyMessage);
    }

    private void authorize(Session session, String authToken) throws IOException {
        try {
            gameService.authorize(authToken);
        } catch (ServiceException | DataAccessException e) {
            ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR,null,null,null);
            errorMsg.setErrorMessage("Error: Unauthorized");
            connections.singleSend(session,errorMsg);
            throw new RuntimeException(e);
        }
    }
}