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
            configureDatabase();
        } catch (Exception e) {

        }
    }

    public GameData createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO gameData (whiteUsername, blackUsername, gameName, json) VALUES (?,?,?,?)";
        ChessGame game = new ChessGame();
        String json = new Gson().toJson(game);
        int gameID = executeUpdate(statement,null,null,gameName,json);
        return new GameData(gameID,null,null,gameName,game);
    }
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM gameData WHERE gameID=?";
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
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM gameData";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
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
        var statement = "DELETE FROM gameData WHERE gameID=?";
        executeUpdate(statement,gameID);
        statement = "INSERT INTO gameData (whiteUsername, blackUsername, gameName, json) VALUES (?,?,?,?)";
        String json = new Gson().toJson(gameData.game());
        executeUpdate(statement,gameData.whiteUsername(),gameData.blackUsername(),gameData.gameName(),json);
    }
    public void clearGames() throws DataAccessException {
        var statement = "TRUNCATE gameData";
        executeUpdate(statement);
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

    private int executeUpdate(String statement, Object... params) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private final String[] createStatements = {
            """
        CREATE TABLE IF NOT EXISTS gameData (
        'gameID' int NOT NULL AUTO_INCREMENT,
        'whiteUsername' varchar(256),
        'blackUsername' varchar(256),
        'gameName' varchar(256) NOT NULL,
        'json' TEXT DEFAULT NULL,
        PRIMARY KEY ('gameID'),
        INDEX(gameName)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
