package client;
import model.requestsandresults.*;

public class ServerFacade {
    public RegisterResult register(RegisterRequest registerRequest) {...}
    public LoginResult login(LoginRequest loginRequest) {...}
    public void joinGame(JoinGameRequest joinGameRequest) {...}
    public ListGamesResult listGames() {...}
}
