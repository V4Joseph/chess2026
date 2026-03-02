package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class GameDataMem implements GameDataAccess {
    private int nextID = 1;
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public GameData createGame(String gameName) throws DataAccessException {
        GameData gameData = new GameData(nextID++,null,null,gameName,new ChessGame());
        games.put(gameData.gameID(),gameData);
        return gameData;
    }
    public GameData getGame(int GameID) throws DataAccessException {
        return games.get(GameID);
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        // Unsure about this part
        games.remove(gameID);
        games.put(gameData.gameID(),gameData);


    }

    public void clearGames(){
        games.clear();
    }
}
