package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Types.NULL;

public class GameDataSql implements GameDataAccess{
    public GameDataSql() {
        try {
            DatabaseManager.configureDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed database configuration");
        }
    }

    public GameData createGame(String gameName) throws DataAccessException {
        if (gameName.isEmpty()) {
            throw new DataAccessException("Error: Invalid Game Name");
        }
        var statement = "INSERT INTO gamedata (whiteUsername, blackUsername, gameName, json) VALUES (?,?,?,?)";
        ChessGame game = new ChessGame();
        String json = new Gson().toJson(game);
        try {
            int gameID = DatabaseManager.executeUpdate(statement, null, null, gameName, json);
            return new GameData(gameID, null, null, gameName, game);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, json FROM gamedata WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1,gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> gameList = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, json FROM gamedata";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        gameList.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return gameList;
    }
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        var statement = "UPDATE gamedata SET whiteUsername=?, blackUsername=?, gameName=?, json=? WHERE gameID=?";
        String json = new Gson().toJson(gameData.game());
        DatabaseManager.executeUpdate(statement,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                json,
                gameID);
    }
    public void clearGames() throws DataAccessException {
        var statement = "TRUNCATE gamedata";
        DatabaseManager.executeUpdate(statement);
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        ChessGame game = new Gson().fromJson(rs.getString("json"), ChessGame.class);
        GameData gameData = new GameData(gameID,whiteUsername,blackUsername,gameName,game);
        return gameData;
    }
}
