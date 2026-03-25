package service;

import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import model.requestsandresults.*;

public class GameServiceTests {
    UserDataAccess userDataAccess = new UserDataMem();
    AuthDataAccess authDataAccess = new AuthDataMem();
    GameDataAccess gameDataAccess = new GameDataMem();
    UserService userService = new UserService(userDataAccess,authDataAccess);
    GameService gameService = new GameService(gameDataAccess,authDataAccess);
    private static RegisterRequest registerRequest;
    private static RegisterResult registerResult;
    private static LoginRequest loginRequest;
    private static LoginResult loginResult;
    private static ListGamesResult listGamesResult;
    private static CreateGameResult createGameResult;
    private static CreateGameRequest createGameRequest;
    private static JoinGameRequest joinGameRequest;

    @Test
    @DisplayName("Create Game Success")
    public void createGameSuccess() throws DataAccessException,ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        createGameRequest = new CreateGameRequest("NewGame21");
        createGameResult = gameService.createGame(createGameRequest,registerResult.authToken());
        Assertions.assertNotEquals(0,createGameResult.gameID());
    }

    @Test
    @DisplayName("Create Game Fail")
    public void createGameFail() throws DataAccessException,ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        createGameRequest = new CreateGameRequest(null);
        try {
        createGameResult = gameService.createGame(createGameRequest,registerResult.authToken());
        } catch (ServiceException e){
            int status = e.getStatus();
            Assertions.assertEquals(400,status,"Error status should be 400");
        }
    }

    @Test
    @DisplayName("Join Game Success")
    public void joinGameSuccess() throws DataAccessException,ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        createGameRequest = new CreateGameRequest("NewGame21");
        createGameResult = gameService.createGame(createGameRequest,registerResult.authToken());
        joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResult.gameID());
        gameService.joinGame(joinGameRequest,registerResult.authToken());
        GameData gameData = gameDataAccess.getGame(joinGameRequest.gameID());
        Assertions.assertEquals(registerRequest.username(),gameData.whiteUsername());
    }

    @Test
    @DisplayName("Join Game Fail")
    public void joinGameFail() throws DataAccessException,ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        createGameRequest = new CreateGameRequest("NewGame21");
        createGameResult = gameService.createGame(createGameRequest,registerResult.authToken());
        joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, null);
        try {
            gameService.joinGame(joinGameRequest,registerResult.authToken());
        } catch (ServiceException e){
            int status = e.getStatus();
            Assertions.assertEquals(400,status,"Error status should be 400");
        }
    }

    @Test
    @DisplayName("List Games Success")
    public void listGamesSuccess() throws DataAccessException,ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        createGameRequest = new CreateGameRequest("NewGame21");
        createGameResult = gameService.createGame(createGameRequest,registerResult.authToken());
        listGamesResult = new ListGamesResult(gameService.listGames(registerResult.authToken()));
        Assertions.assertNotNull(listGamesResult);
    }

    @Test
    @DisplayName("List Games Fail")
    public void listGamesFail() throws DataAccessException,ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        createGameRequest = new CreateGameRequest("NewGame21");
        createGameResult = gameService.createGame(createGameRequest,registerResult.authToken());

        try {
            listGamesResult = new ListGamesResult(gameService.listGames(null));
        } catch (ServiceException e){
            int status = e.getStatus();
            Assertions.assertEquals(401,status,"Error status should be 400");
        }
    }

}
