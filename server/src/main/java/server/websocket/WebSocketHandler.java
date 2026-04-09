package server.websocket;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import exception.ResponseException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.*;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    GameDataAccess gameDataAccess = new GameDataSql();
    UserDataAccess userDataAccess = new UserDataSql();
    AuthDataAccess authDataAccess = new AuthDataSql();
    UserService userService = new UserService(userDataAccess, authDataAccess);
    GameService gameService = new GameService(gameDataAccess,authDataAccess);

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT ->connect(command.getAuthToken(), command.getGameID(), ctx.session);
                case MAKE_MOVE ->makeMove(command.getMove(), command.getAuthToken(), command.getGameID(), ctx.session);
                case LEAVE -> leave(command.getAuthToken(), command.getGameID(), ctx.session);
                case RESIGN -> resign(command.getAuthToken(), command.getGameID(), ctx.session);
            }
        } catch (IOException | DataAccessException ex) {
            ex.printStackTrace();
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String authToken, int gameID, Session session) throws IOException, DataAccessException {
        gameService.authorize(authToken);
        String username = authDataAccess.getAuth(authToken).username();
        connections.add(gameID,session);
        var msg = String.format("%s has connected to the game", username);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null, null);
        connections.broadcast(session, serverMessage);
    }

    private void makeMove(ChessMove move, String authToken, int gameID, Session session) throws DataAccessException, InvalidMoveException, IOException {
        gameService.authorize(authToken);
        String username = authDataAccess.getAuth(authToken).username();
        GameData gameData = gameDataAccess.getGame(gameID);
        ChessGame game = gameData.game();
        game.makeMove(move);
        GameData newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        gameDataAccess.updateGame(gameID, newGameData);

        ChessBoard board = game.getBoard();
        String pieceName = board.getPiece(move.getStartPosition()).getPieceType().toString();
        String movePosition = move.getEndPosition().getName();
        var msg = String.format("%s has moved %s to %s",username, pieceName, movePosition);
        var notifyMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null,null);
        var loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, game, null);
        connections.broadcast(session,notifyMessage);
        connections.broadcast(null,loadMessage);
    }

    private void leave(String authToken, int gameID, Session session) throws DataAccessException, IOException {
        gameService.authorize(authToken);
        String username = authDataAccess.getAuth(authToken).username();
        var msg = String.format("%s left the game", username);
        var notifyMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null, null);
        connections.broadcast(session, notifyMessage);
        connections.remove(gameID);
    }

    private void resign(String authToken, int gameID, Session session) throws DataAccessException, IOException {
        gameService.authorize(authToken);
        String username = authDataAccess.getAuth(authToken).username();
        var msg = String.format("%s resigned from the game", username);
        var notifyMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg, null, null);
        connections.broadcast(session, notifyMessage);
        connections.remove(gameID);
    }

//
//    public void makeNoise(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var notification = new Notification(Notification.Type.NOISE, message);
//            connections.broadcast(null, notification);
//        } catch (Exception ex) {
//            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
//        }
//    }
}