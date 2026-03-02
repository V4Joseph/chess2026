package service;

import com.google.gson.Gson;
import dataaccess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.RR_Classes.LoginRequest;
import service.RR_Classes.LoginResult;
import service.RR_Classes.RegisterRequest;
import service.RR_Classes.RegisterResult;
import service.UserService;

import java.util.Map;


public class UserServiceTests {
    UserDataAccess userDataAccess = new UserDataMem();
    AuthDataAccess authDataAccess = new AuthDataMem();
    UserService userService = new UserService(userDataAccess,authDataAccess);
    private static RegisterRequest registerRequest;
    private static RegisterResult registerResult;
    private static LoginRequest loginRequest;
    private static LoginResult loginResult;


    @Test
    @DisplayName("Register Success")
    public void registerSuccess () throws DataAccessException, ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        Assertions.assertEquals(registerResult.username(), registerRequest.username(), "Result did not have correct username");
        Assertions.assertNotNull(registerResult.authToken(), "Result missing authToken");
    }
    @Test
    @DisplayName("Register Fail")
    public void registerFail () throws DataAccessException,ServiceException{
        registerRequest = new RegisterRequest("yomom","socool",null);
        try {
            registerResult = userService.register(registerRequest);
        } catch (ServiceException e){
            int status = e.getStatus();
            Assertions.assertEquals(400,status,"Error status should be 400");
        }
    }

    @Test
    @DisplayName("Login Success")
    public void loginSuccess () throws DataAccessException, ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        loginRequest = new LoginRequest("yomom","socool");
        loginResult = userService.login(loginRequest);
        Assertions.assertEquals(loginResult.username(), loginRequest.username(), "Result did not have correct username");
        Assertions.assertNotNull(loginResult.authToken(), "Result missing authToken");
    }
    @Test
    @DisplayName("Login Fail")
    public void loginFail () throws DataAccessException,ServiceException{
        loginRequest = new LoginRequest("yomom",null);
        try {
            loginResult = userService.login(loginRequest);
        } catch (ServiceException e){
            int status = e.getStatus();
            Assertions.assertEquals(400,status,"Error status should be 400");
        }
    }

    @Test
    @DisplayName("Logout Success")
    public void logoutSuccess () throws DataAccessException, ServiceException {
        registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
        registerResult = userService.register(registerRequest);
        userService.logout(registerResult.authToken());
        Assertions.assertNull(authDataAccess.getAuth(registerResult.authToken()));

    }
    @Test
    @DisplayName("Logout Fail")
    public void logoutFail () throws DataAccessException,ServiceException{
        try {
            registerRequest = new RegisterRequest("yomom","socool","bmo@gmail.com");
            registerResult = userService.register(registerRequest);
            userService.logout("FakeAuthTokenLOL");
        } catch (ServiceException e){
            int status = e.getStatus();
            Assertions.assertEquals(401,status,"Error status should be 401");
        }
    }
}
