package service;

import chess.ChessGame;
import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccess;
import dataaccess.UserDataAccess;
import model.AuthData;
import model.GameData;
import model.requestsandresults.CreateGameRequest;
import model.requestsandresults.CreateGameResult;
import model.requestsandresults.JoinGameRequest;

import java.util.Collection;

public class GameService {
    private final GameDataAccess gameDataAccess;
    private final AuthDataAccess authDataAccess;

    public GameService (GameDataAccess gameDataAccess,
                             AuthDataAccess authDataAccess) {
        this.gameDataAccess = gameDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        authorize(authToken);
        return gameDataAccess.listGames();
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest, String authToken)throws DataAccessException {
        if (authToken == null || createGameRequest.gameName() == null || createGameRequest.gameName().isEmpty()) {
            throw new ServiceException("Error: bad create request", 400);
        }
        authorize(authToken);
        GameData gameData = gameDataAccess.createGame(createGameRequest.gameName());
        return new CreateGameResult(gameData.gameID());

    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws DataAccessException {
        // Null parameters
        if (authToken == null ||
                joinGameRequest.playerColor() == null ||
                joinGameRequest.playerColor().isEmpty() ||
                joinGameRequest.gameID() == null ||
                joinGameRequest.gameID() == 0) {
            throw new ServiceException("Error: bad param request", 400);
        }
        boolean equalsWhite = joinGameRequest.playerColor().equalsIgnoreCase(ChessGame.TeamColor.WHITE.name());
        boolean equalsBlack = joinGameRequest.playerColor().equalsIgnoreCase(ChessGame.TeamColor.BLACK.name());
        // Not WHITE/BLACK for playerColor
        if (!(equalsWhite || equalsBlack)) {
            throw new ServiceException("Error: bad color request",400);
        }
        AuthData authData = authorize(authToken);
        GameData gameData = gameDataAccess.getGame(joinGameRequest.gameID());
        // Game not found
        if (gameData == null) {
            throw new ServiceException("Error: unauthorized",401);
        } else {
            if (equalsWhite && gameData.whiteUsername() == null) {
                GameData newGameData = new GameData(gameData.gameID(),
                        authData.username(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        gameData.game());
                gameDataAccess.updateGame(joinGameRequest.gameID(),newGameData);
            } else if (equalsBlack && gameData.blackUsername() == null) {
                GameData newGameData = new GameData(gameData.gameID(),
                        gameData.whiteUsername(),
                        authData.username(),
                        gameData.gameName(),
                        gameData.game());
                gameDataAccess.updateGame(joinGameRequest.gameID(),newGameData);
            } else {
                // Color already taken
                throw new ServiceException("Error: already taken", 403);
            }


        }
    }

    public AuthData authorize(String authToken) throws DataAccessException{
        AuthData authData = authDataAccess.getAuth(authToken);
        if (authData == null) {
            throw new ServiceException("Error: unauthorized", 401);
        }
        else {
            return authData;
        }
    }





}
