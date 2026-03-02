package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import model.GameData;
import service.GameService;
import service.requestsandresults.*;
import service.ServiceException;
import service.UserService;

import java.util.Collection;
import java.util.Map;


public class Server {

    private final Javalin javalin;
    UserDataAccess userDataAccess = new UserDataMem();
    AuthDataAccess authDataAccess = new AuthDataMem();
    GameDataAccess gameDataAccess = new GameDataMem();
    UserService userService = new UserService(userDataAccess, authDataAccess);
    GameService gameService = new GameService(userDataAccess,gameDataAccess,authDataAccess);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user",this::register)
                .delete("/db", this::delete)
                .post("/session", this::login)
                .delete("/session", this::logout)
                .get("/game", this::listGames)
                .post("/game", this::createGame)
                .put("/game", this::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void register(Context context) throws DataAccessException, ServiceException {
        RegisterRequest registerRequest = getBodyObject(context,RegisterRequest.class);
        try {
            RegisterResult registerResult = userService.register(registerRequest);
            String json = new Gson().toJson(registerResult);
            context.json(json);
        }
        catch (ServiceException e){
            int status = e.getStatus();
            context.status(status);
            String msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", msg));
            context.json(body);
        }
    }

    private void delete(Context context) {
        userDataAccess.clearUsers();
        authDataAccess.clearAuths();
        gameDataAccess.clearGames();
        context.status(200);
    }

    private void login(Context context) throws DataAccessException, ServiceException {
        LoginRequest loginRequest = getBodyObject(context, LoginRequest.class);
        try {
            LoginResult loginResult = userService.login(loginRequest);
            String json = new Gson().toJson(loginResult);
            context.json(json);
        }
        catch (ServiceException e){
            int status = e.getStatus();
            context.status(status);
            String msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", msg));
            context.json(body);
        }

    }

    private void logout(Context context) throws DataAccessException, ServiceException{
        String authToken = context.header("authorization");
        try {
            userService.logout(authToken);
        }catch (ServiceException e){
            int status = e.getStatus();
            context.status(status);
            String msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", msg));
            context.json(body);
        }
    }

    private void listGames(Context context) throws DataAccessException, ServiceException {
        String authToken = context.header("authorization");
        try{
            Collection<GameData> games = gameService.listGames(authToken);
            ListGamesResult listGamesResult = new ListGamesResult(games);
            String json = new Gson().toJson(listGamesResult);
            context.json(json);
        } catch (ServiceException e){
            int status = e.getStatus();
            context.status(status);
            String msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", msg));
            context.json(body);
        }
    }

    private void createGame(Context context) throws DataAccessException, ServiceException {
        String authToken = context.header("authorization");
        CreateGameRequest createGameRequest = getBodyObject(context, CreateGameRequest.class);
        try {
            CreateGameResult createGameResult = gameService.createGame(createGameRequest,authToken);
            String json = new Gson().toJson(createGameResult);
            context.json(json);
        } catch (ServiceException e){
            int status = e.getStatus();
            context.status(status);
            String msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", msg));
            context.json(body);
        }
    }

    private void joinGame(Context context) throws ServiceException, DataAccessException {
        String authToken = context.header("authorization");
        JoinGameRequest joinGameRequest = getBodyObject(context, JoinGameRequest.class);
        try {
            gameService.joinGame(joinGameRequest,authToken);
        } catch (ServiceException e){
            int status = e.getStatus();
            context.status(status);
            String msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", msg));
            context.json(body);
        }
    }

    private static <T> T getBodyObject(Context context, Class<T> givenClass) {
        var bodyObject = new Gson().fromJson(context.body(),givenClass);
        if (bodyObject == null) {
            throw new RuntimeException("Empty body");
        }
        return bodyObject;
    }


}
