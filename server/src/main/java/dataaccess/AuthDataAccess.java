package dataaccess;

import model.AuthData;

public interface AuthDataAccess {

    AuthData createAuth(AuthData authData);
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken);
    void clearAuths();

}
