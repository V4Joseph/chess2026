package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDataAccess {
    GameData createGame(String gameName) throws DataAccessException;
    GameData getGame(int GameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(int GameID, GameData gameData) throws DataAccessException;
    void clearGames();
}
