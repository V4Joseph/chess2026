package dataaccess;

import model.AuthData;

public interface AuthDataAccess {

    AuthData createAuth(AuthData authData) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clearAuths() throws DataAccessException;

}
