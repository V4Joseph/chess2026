package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import server.Server;
import model.*;
import model.requestsandresults.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = String.valueOf(server.run(8080));
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:8080");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearAll() throws ResponseException {
        facade.delete();
    }
    // Register

    // Register Tests

    @Test
    void registerSuccess() throws Exception {
        var result = facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        assertNotNull(result.authToken());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    void registerDuplicateUserThrows() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        // Registering the same username a second time must fail
        assertThrows(ResponseException.class, () ->
                facade.register(new RegisterRequest("player1", "password", "p1@mail.com")));
    }

    // Login Tests

    @Test
    void loginSuccess() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        var result = facade.login(new LoginRequest("player1", "password"));
        assertNotNull(result.authToken());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    void loginWrongPasswordThrows() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        assertThrows(ResponseException.class, () ->
                facade.login(new LoginRequest("player1", "wrongPassword")));
    }

    // Logout Tests

    @Test
    void logoutSuccess() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        // Should complete without throwing
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutInvalidTokenThrows() {
        assertThrows(ResponseException.class, () ->
                facade.logout("this-is-not-a-real-token"));
    }

    // Create Tests

    @Test
    void createGameSuccess() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        var result = facade.createGame(new CreateGameRequest("My Game"), auth.authToken());
        assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameUnauthorizedThrows() {
        assertThrows(ResponseException.class, () ->
                facade.createGame(new CreateGameRequest("My Game"), "bad-token"));
    }

    // List Tests

    @Test
    void listGamesSuccess() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        facade.createGame(new CreateGameRequest("Game One"), auth.authToken());
        facade.createGame(new CreateGameRequest("Game Two"), auth.authToken());
        var result = facade.listGames(auth.authToken());
        assertNotNull(result.games());
        assertEquals(2, result.games().size());
    }

    @Test
    void listGamesUnauthorizedThrows() {
        assertThrows(ResponseException.class, () ->
                facade.listGames("bad-token"));
    }

    // Join Tests

    @Test
    void joinGameSuccess() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        var game = facade.createGame(new CreateGameRequest("My Game"), auth.authToken());
        assertDoesNotThrow(() ->
                facade.joinGame(new JoinGameRequest("WHITE", game.gameID()), auth.authToken()));
    }

    @Test
    void joinGameAlreadyTakenThrows() throws Exception {
        var auth1 = facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        var auth2 = facade.register(new RegisterRequest("player2", "password", "p2@mail.com"));
        var game = facade.createGame(new CreateGameRequest("My Game"), auth1.authToken());
        facade.joinGame(new JoinGameRequest("WHITE", game.gameID()), auth1.authToken());
        // Second player tries to claim the same color
        assertThrows(ResponseException.class, () ->
                facade.joinGame(new JoinGameRequest("WHITE", game.gameID()), auth2.authToken()));
    }

    // Delete Tests

    @Test
    void deleteSuccess() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@mail.com"));
        facade.createGame(new CreateGameRequest("My Game"), auth.authToken());
        // After delete, the game list should be empty for a freshly registered user
        assertDoesNotThrow(() -> facade.delete());
        var auth2 = facade.register(new RegisterRequest("player2", "password", "p2@mail.com"));
        var result = facade.listGames(auth2.authToken());
        assertTrue(result.games().isEmpty());
    }

    @Test
    void deleteCalledTwiceStillSucceeds() throws Exception {
        // Deleting an already-empty database should not throw
        facade.delete(); // first call (BeforeEach already called it once)
        assertDoesNotThrow(() -> facade.delete());
    }
}
