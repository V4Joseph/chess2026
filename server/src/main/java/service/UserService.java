package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccess;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.requestsandresults.*;

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

    public RegisterResult register(RegisterRequest registerRequest) throws ServiceException, DataAccessException{
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new ServiceException("Error: bad request", 400);
        }
        UserData userData = userDataAccess.getUser(registerRequest.username());
        if (userData != null) {
            throw new ServiceException("Error: already taken", 403);
        } else {
            String hashedPassword = BCrypt.hashpw(registerRequest.password(),BCrypt.gensalt());
            userData = new UserData(registerRequest.username(),hashedPassword,registerRequest.email());
            userDataAccess.createUser(userData);
            String authToken = generateToken();
            AuthData authData = new AuthData(authToken,registerRequest.username());
            authDataAccess.createAuth(authData);
            return new RegisterResult(authData.username(),authToken);
        }
    }
    public LoginResult login(LoginRequest loginRequest) throws DataAccessException, ServiceException{
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new ServiceException("Error: bad request", 400);
        }
        UserData userData = userDataAccess.getUser(loginRequest.username());
        String hashedPassword = BCrypt.hashpw(loginRequest.password(),BCrypt.gensalt());
        if (userData == null || BCrypt.checkpw(hashedPassword,userData.password())) {
            throw new ServiceException("Error: unauthorized", 401);
        }  else {
            String authToken = generateToken();
            authDataAccess.createAuth(new AuthData(authToken,loginRequest.username()));
            return new LoginResult(loginRequest.username(),authToken);
        }
    }

    public void logout(String authToken) throws DataAccessException, ServiceException{
        if (authDataAccess.getAuth(authToken) == null) {
            throw new ServiceException("Error: unauthorized",401);
        } else {
            authDataAccess.deleteAuth(authToken);
        }

    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }


}
