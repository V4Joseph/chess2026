package dataaccess;
import model.UserData;
import java.sql.*;

public class UserDataSql implements UserDataAccess{
    public UserDataSql(){
        try {
            DatabaseManager.configureDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed database configuration");
        }
    }

    public void createUser(UserData userData) throws DataAccessException {
        var statement = "INSERT INTO userdata (username, password, email) VALUES (?, ?, ?)";
        DatabaseManager.executeUpdate(statement,userData.username(),userData.password(),userData.email());
    }

    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM userdata WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }

    public void clearUsers() throws DataAccessException{
        var statement = "TRUNCATE userdata";
        DatabaseManager.executeUpdate(statement);
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username,password,email);
    }
}
