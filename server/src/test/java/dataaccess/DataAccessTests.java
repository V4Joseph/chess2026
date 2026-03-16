package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DataAccessTests {
    AuthDataAccess authDataAccess = new AuthDataSql();


    @BeforeAll
    public static void setUp() {

    }

    // Auth Tests
    @Test
    @DisplayName("Create Auth S")
    public void createAuthS() {
        AuthData authData = new AuthData("sigma","boi");
        try {
            authDataAccess.createAuth(authData);
        } catch (Exception e) {
            throw new RuntimeException("Failure to create auth");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM authdata WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,authData.authToken());
                try (ResultSet rs = ps.executeQuery()) {
                    Assertions.assertTrue(rs.next(),"Not Found");
                    AuthData authData2 = new AuthData(rs.getString("authToken"),rs.getString("username"));
                    Assertions.assertEquals(authData.authToken(),authData2.authToken());
                    Assertions.assertEquals(authData.username(),authData2.username());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("Create Auth F")
    public void createAuthF() {

    }
}
