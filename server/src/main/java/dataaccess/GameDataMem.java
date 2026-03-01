package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class GameDataMem implements GameDataAccess {
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public GameData createGame(GameData gameData) throws DataAccessException {
        gameData = new GameData(gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                gameData.game());

        games.put(gameData.gameID(),gameData);
        return gameData;
    }
    public GameData getGame(int GameID) throws DataAccessException {
        return games.get(GameID);
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    public void updateGame(int gameID) throws DataAccessException {
        // Unsure about this part


    }

    public void clearGames() throws DataAccessException {
        games.clear();
    }
}
