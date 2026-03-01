package service;

import chess.ChessGame;
import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccess;
import dataaccess.UserDataAccess;
import model.AuthData;
import model.GameData;
import service.RR_Classes.CreateGameRequest;
import service.RR_Classes.JoinGameRequest;

import java.util.Collection;
import java.util.PriorityQueue;

public class GameService {
    private final UserDataAccess userDataAccess;
    private final GameDataAccess gameDataAccess;
    private final AuthDataAccess authDataAccess;

    private PriorityQueue<Integer> availableGameID = new PriorityQueue<>();
    private int nextID = 0;

    public GameService (UserDataAccess userDataAccess,
                             GameDataAccess gameDataAccess,
                             AuthDataAccess authDataAccess) {
        this.userDataAccess = userDataAccess;
        this.gameDataAccess = gameDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        AuthData authData = authorize(authToken);
        return gameDataAccess.listGames();
    }

    public int createGame(CreateGameRequest createGameRequest)throws DataAccessException {
        AuthData authData = authorize(createGameRequest.authToken());
            int ID;
            // Need to implement removal of gameIDs
            if (!availableGameID.isEmpty()){
                ID = availableGameID.poll();
            } else {
                ID = nextID++;
            }
            GameData gameData = new GameData(ID,
                    null,
                    null,
                    createGameRequest.gameName(),
                    new ChessGame());
            gameDataAccess.createGame(gameData);
            return ID;

    }

    public void joinGame(JoinGameRequest joinGameRequest) throws DataAccessException {
        AuthData authData = authorize(joinGameRequest.authToken());
        GameData gameData = gameDataAccess.getGame(joinGameRequest.gameID());
        if (gameData == null) {
            throw new DataAccessException("Game not found");
        } else {
            if (joinGameRequest.color().equalsIgnoreCase("WHITE") && gameData.whiteUsername() == null) {

                gameDataAccess.updateGame(joinGameRequest.gameID());
            } else if (joinGameRequest.color().equalsIgnoreCase("BLACK") && gameData.blackUsername() == null) {

                gameDataAccess.updateGame(joinGameRequest.gameID());
            } else {
                throw new DataAccessException("Color already taken");
            }


        }
    }

    public AuthData authorize(String authToken) throws DataAccessException{
        AuthData authData = authDataAccess.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Unauthorized Request");
        }
        else return authData;
    }





}
