package dataaccess;
import model.AuthData;

import java.util.HashMap;

public class AuthDataMem implements AuthDataAccess {
    final private HashMap<String, AuthData> auths = new HashMap<>();

    public AuthData createAuth(AuthData authData) {
        authData = new AuthData(authData.authToken(),authData.username());
        auths.put(authData.authToken(),authData);
        return authData;
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return auths.get(authToken);

    }

    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public void clearAuths() {
        auths.clear();
    }
}
