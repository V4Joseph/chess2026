package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DataAccessTests {
    AuthDataAccess authDataAccess = new AuthDataSql();
    AuthData authData1 = new AuthData("sigma", "boi");
    AuthData authData2 = new AuthData("labubu", "tulip");
    UserDataAccess userDataAccess = new UserDataSql();
    UserData userData1 = new UserData("sigma", "boi","sk8ter@gmail.com");
    UserData userData2 = new UserData("labubu", "tulip","yamom@yahoo.com");

    @BeforeEach
    public void setUp() throws DataAccessException{
        authDataAccess.clearAuths();
    }

    // Auth Tests
    @Test
    @DisplayName("Create Auth S")
    public void createAuthS() {
        try {
            authDataAccess.createAuth(authData1);
        } catch (Exception e) {
            throw new RuntimeException("Failure to create auth");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM authdata WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,authData1.authToken());
                try (ResultSet rs = ps.executeQuery()) {
                    Assertions.assertTrue(rs.next(),"Not Found");
                    AuthData getAuth = new AuthData(rs.getString("authToken"),rs.getString("username"));
                    Assertions.assertEquals(authData1.authToken(),getAuth.authToken());
                    Assertions.assertEquals(authData1.username(),getAuth.username());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("Create Auth F")
    public void createAuthF() {
        try {
            authDataAccess.createAuth(authData1);
        } catch (Exception e) {
            throw new RuntimeException("Failure to create first authData");
        }
        Assertions.assertThrows(DataAccessException.class,()-> {
            authDataAccess.createAuth(authData1);
        }, "Cannot create duplicate authToken");
    }

    @Test
    @DisplayName("Get Auth S")
    public void getAuthS() {
        try {
            authDataAccess.createAuth(authData1);
            authDataAccess.createAuth(authData2);
        } catch (Exception e) {
            throw new RuntimeException("Failure to create auth");
        }

        try {
            AuthData getAuth1 = authDataAccess.getAuth(authData1.authToken());
            Assertions.assertNotNull(getAuth1, "First Auth not found");
            Assertions.assertEquals(authData1.authToken(), getAuth1.authToken());
            Assertions.assertEquals(authData1.username(), getAuth1.username());

            AuthData getAuth2 = authDataAccess.getAuth(authData2.authToken());
            Assertions.assertNotNull(getAuth2, "First Auth not found");
            Assertions.assertEquals(authData2.authToken(), getAuth2.authToken());
            Assertions.assertEquals(authData2.username(), getAuth2.username());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("Get Auth F")
    public void getAuthF() {
        try {
            authDataAccess.createAuth(authData2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            AuthData getAuth = authDataAccess.getAuth("sigma");
            Assertions.assertNull(getAuth, "Should be null for invalid authtoken");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Delete Auth S")
    public void deleteAuthS() {
        try {
            authDataAccess.createAuth(authData1);
            AuthData checkAuth = authDataAccess.getAuth(authData1.authToken());
            Assertions.assertEquals(authData1,checkAuth);
            authDataAccess.deleteAuth(authData1.authToken());
            AuthData nullAuth = authDataAccess.getAuth(authData1.authToken());
            Assertions.assertNull(nullAuth);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Delete Auth F")
    public void deleteAuthF() {
        Assertions.assertThrows(DataAccessException.class, ()-> {
            authDataAccess.deleteAuth("this_is_not_a_real_authToken");
        }, "Should throw Exception deleting non-existent authToken");
    }

    @Test
    @DisplayName("Clear Auth S")
    public void clearAuth() {
        try {
            authDataAccess.createAuth(authData1);
            authDataAccess.createAuth(authData2);
            authDataAccess.clearAuths();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            AuthData getAuth1 = authDataAccess.getAuth(authData1.authToken());
            AuthData getAuth2 = authDataAccess.getAuth(authData2.authToken());
            Assertions.assertNull(getAuth1, "Should return null after clearing auth1");
            Assertions.assertNull(getAuth2, "Should return null after clearing auth2");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

        // User Tests
        @Test
        @DisplayName("Create User S")
        public void createUserS () {
            try {
                userDataAccess.createUser(userData1);
            } catch (Exception e) {
                throw new RuntimeException("Failure to create user");
            }
            try (Connection conn = DatabaseManager.getConnection()) {
                var statement = "SELECT username, password, email FROM userdata WHERE username=?";
                try (PreparedStatement ps = conn.prepareStatement(statement)) {
                    ps.setString(1, userData1.username());
                    try (ResultSet rs = ps.executeQuery()) {
                        Assertions.assertTrue(rs.next(), "Not Found");
                        UserData getUser = new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                        Assertions.assertEquals(userData1.password(), getUser.password());
                        Assertions.assertEquals(userData1.username(), getUser.username());
                        Assertions.assertEquals(userData1.email(), getUser.email());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        @Test
        @DisplayName("Create User F")
        public void createUserF () {
            try {
                userDataAccess.createUser(userData1);
            } catch (Exception e) {
                throw new RuntimeException("Failure to create first userData");
            }
            Assertions.assertThrows(DataAccessException.class, () -> {
                userDataAccess.createUser(userData1);
            }, "Cannot create duplicate userToken");
        }

        @Test
        @DisplayName("Get User S")
        public void getUserS () {
            try {
                userDataAccess.createUser(userData1);
                userDataAccess.createUser(userData2);
            } catch (Exception e) {
                throw new RuntimeException("Failure to create user");
            }

            try {
                UserData getUser1 = userDataAccess.getUser(userData1.username());
                Assertions.assertNotNull(getUser1, "First User not found");
                Assertions.assertEquals(userData1.password(), getUser1.password());
                Assertions.assertEquals(userData1.username(), getUser1.username());
                Assertions.assertEquals(userData1.email(), getUser1.email());

                UserData getUser2 = userDataAccess.getUser(userData2.username());
                Assertions.assertNotNull(getUser2, "First User not found");
                Assertions.assertEquals(userData2.password(), getUser2.password());
                Assertions.assertEquals(userData2.username(), getUser2.username());
                Assertions.assertEquals(userData1.email(), getUser1.email());
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }

        }

        @Test
        @DisplayName("Get User F")
        public void getUserF () {
            try {
                userDataAccess.createUser(userData2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                UserData getUser = userDataAccess.getUser("sigma");
                Assertions.assertNull(getUser, "Should be null for invalid username");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        @Test
        @DisplayName("Clear User S")
        public void clearUser() {
            try {
                userDataAccess.createUser(userData1);
                userDataAccess.createUser(userData2);
                userDataAccess.clearUsers();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                UserData getUser1 = userDataAccess.getUser(userData1.username());
                UserData getUser2 = userDataAccess.getUser(userData2.username());
                Assertions.assertNull(getUser1, "Should return null after clearing user1");
                Assertions.assertNull(getUser2, "Should return null after clearing user2");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }

    }

