package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import server.Server;
import model.*;
import model.requestsandresults.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    RegisterRequest registerRequest = new RegisterRequest("player1", "password", "yomom@yahoo.com");

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = String.valueOf(server.run(0));
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearAll() throws ResponseException {
        facade.delete();
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    void register() throws Exception {
        var authData = facade.register(registerRequest);
        assertTrue(authData.authToken().length() > 10);
    }
}
