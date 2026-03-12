package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;
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
        var statement = "INSERT INTO gameData (gameID, whiteUsername, blackUsername, gameName, json) VALUES (?, ?, ?, ?, ?)";
        executeUpdate(statement,null,null,gameName,new ChessGame());
        GameData gameData = new GameData()
    }
    public GameData getGame(int gameID) throws DataAccessException {

    }
    public Collection<GameData> listGames() throws DataAccessException {

    }
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {

    }
    public void clearGames() {

    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var json = rs.getString("json");
        ChessGame game = new Gson().fromJson(json,ChessGame.class);
        return new GameData(gameID,whiteUsername,blackUsername,gameName,game);
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

//                ResultSet rs = ps.getGeneratedKeys();
//                if (rs.next()) {
//                    return rs.getInt(1);
//                }
//
//                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private final String[] createStatements = {
            """
        CREATE TABLE IF NOT EXISTS gameData (
        `gameID` int NOT NULL AUTO_INCREMENT,
        'whiteUsername' varchar(256),
        'blackUsername' varchar(256),
        'gameName' varchar(256),
        'json' TEXT DEFAULT NULL,
        PRIMARY KEY ('gameID'),
        INDEX(whiteUsername),
        INDEX(blackUsername),
        INDEX(gameName) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
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
