package dataaccess;

import model.UserData;

public interface UserDataAccess {
    void createUser(UserData userData) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void clearUsers() throws DataAccessException;


}
