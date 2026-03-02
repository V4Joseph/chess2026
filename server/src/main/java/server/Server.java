package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.RR_Classes.*;
import service.ServiceException;
import service.UserService;

import java.util.Map;


public class Server {

    private final Javalin javalin;
    UserDataAccess userDataAccess = new UserDataMem();
    AuthDataAccess authDataAccess = new AuthDataMem();
    GameDataAccess gameDataAccess = new GameDataMem();
    UserService userService = new UserService(userDataAccess, authDataAccess);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user",this::register)
                .delete("/db", this::delete)
                .post("/session", this::login)
                .delete("/session", this::logout)
//                .error(404, this::notFound)



        ;

        // Register your endpoints and exception handlers here.

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
            String Msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", Msg));
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
            String Msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", Msg));
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
            String Msg = e.getMessage();
            var body = new Gson().toJson(Map.of("message", Msg));
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
