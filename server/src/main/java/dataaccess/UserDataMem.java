package dataaccess;

import model.UserData;

import java.util.HashMap;

public class UserDataMem implements UserDataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();


    public void createUser(UserData userData) {
        userData = new UserData(userData.username(),userData.password(),userData.email());
        users.put(userData.username(), userData);
    }

    public UserData getUser(String username) {

        return users.get(username);
    }

    public void clearUsers() {
        users.clear();
    }
}
