package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccess;
import model.AuthData;
import model.UserData;
import service.RR_Classes.*;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    private final UserDataAccess userDataAccess;
    private final AuthDataAccess authDataAccess;

    public UserService(UserDataAccess userDataAccess,
                       AuthDataAccess authDataAccess) {
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException{
        UserData userData = userDataAccess.getUser(registerRequest.username());
        if (userData != null) {
            throw new DataAccessException("Error: username already taken");
        } else {
            userData = new UserData(registerRequest.username(),registerRequest.password(),registerRequest.email());
            userDataAccess.createUser(userData);
            String authToken = generateToken();
            AuthData authData = new AuthData(authToken,registerRequest.username());
            authDataAccess.createAuth(authData);
            return new RegisterResult(authData.username(),authToken);
        }
    }
    public LoginResult login(LoginRequest loginRequest) throws DataAccessException{
        UserData userData = userDataAccess.getUser(loginRequest.username());
        if (userData == null) {
            throw new DataAccessException("Username not found");
        } else if (!Objects.equals(userData.password(), loginRequest.password())){
            throw new DataAccessException("Password does not match");
        } else {
            String authToken = generateToken();
            authDataAccess.createAuth(new AuthData(authToken,loginRequest.username()));
            return new LoginResult(loginRequest.username(),authToken);
        }
    }

    public void logout(LogoutRequest logoutRequest){
        authDataAccess.deleteAuth(logoutRequest.authToken());
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }


}
