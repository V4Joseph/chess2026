package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DataAccessTests {
    AuthDataAccess authDataAccess = new AuthDataSql();

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
        var statement = "SELECT authToken, username FROM authdata WHERE authToken=?";

    }

    @Test
    @DisplayName("Create Auth F")
    public void createAuthF() {

    }
}
