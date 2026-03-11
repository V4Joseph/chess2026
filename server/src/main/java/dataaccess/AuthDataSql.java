package dataaccess;

import com.google.gson.Gson;
import model.AuthData;

import java.sql.*;

public class AuthDataSql implements AuthDataAccess{
    public AuthDataSql() throws DataAccessException {

    }
    public AuthData createAuth(AuthData authData){
        var statement = "INSERT INTO authData (authToken, authData) VALUES (?, ?)";
        String json = new Gson().toJson(authData);

    }
    public AuthData getAuth(String authToken) throws DataAccessException {

    }
    public void deleteAuth(String authToken) {

    }
    public void clearAuths(){

    }









    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) {
                        ps.setString
                    }
                }
            }
        }
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS authData (
        'authToken' varchar(256),
        'json' TEXT DEFAULT NULL,
        PRIMARY KEY ('authToken'))
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
