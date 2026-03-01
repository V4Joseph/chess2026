package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.RR_Classes.RegisterResult;
import service.UserService;
import service.RR_Classes.RegisterRequest;


public class Server {

    private final Javalin javalin;
    UserDataAccess userDataAccess = new UserDataMem();
    AuthDataAccess authDataAccess = new AuthDataMem();
    UserService userService = new UserService(userDataAccess, authDataAccess);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user",this::register)



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

    private void register(Context context) throws DataAccessException {
        RegisterRequest registerRequest = getBodyObject(context,RegisterRequest.class);
        RegisterResult registerResult = userService.register(registerRequest);
        String json = new Gson().toJson(registerResult);
        context.json(json);

    }

    private static <T> T getBodyObject(Context context, Class<T> givenClass) {
        var bodyObject = new Gson().fromJson(context.body(),givenClass);
        if (bodyObject == null) {
            throw new RuntimeException("Empty body");
        }
        return bodyObject;
    }
}
