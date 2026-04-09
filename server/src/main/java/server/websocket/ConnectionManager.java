package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Use map with game ID key attached to set of sessions for each game
public class ConnectionManager {
    public final Map<Integer,Set<Session>> gameSessions = new HashMap<>();

    public void add(int gameID, Session session) {
        Set<Session> sessionSet = gameSessions.get(gameID);
        sessionSet.add(session);
        gameSessions.put(gameID,sessionSet);
    }

    public void remove(int gameID) {
        gameSessions.remove(gameID);
    }

    public void broadcast(Session excludeSession, ServerMessage message) throws IOException {
        String msg = message.toString();
        for (Set<Session> s : gameSessions.values()) {
            for (Session c : s) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
                }
            }
        }
    }
}
