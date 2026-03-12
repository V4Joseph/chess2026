package dataaccess;

import com.google.gson.Gson;
import model.AuthData;

import java.sql.*;

import static java.sql.Types.NULL;

public class AuthDataSql implements AuthDataAccess{
    public AuthDataSql(){
        try {
            DatabaseManager.configureDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed database configuration");
        }

    }

    public AuthData createAuth(AuthData authData) throws DataAccessException{
        var statement = "INSERT INTO authdata (authToken, username) VALUES (?, ?)";
        DatabaseManager.executeUpdate(statement,authData.authToken(),authData.username());
        return new AuthData(authData.authToken(),authData.username());
    }
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM authdata WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }
    public void deleteAuth(String authToken) throws DataAccessException{
        var statement = "DELETE FROM authdata WHERE authToken=?";
        DatabaseManager.executeUpdate(statement,authToken);
    }
    public void clearAuths() throws DataAccessException{
        var statement = "TRUNCATE authdata";
        DatabaseManager.executeUpdate(statement);
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var username = rs.getString("username");
        AuthData authData = new AuthData(authToken,username);
        return authData;
    }
}
