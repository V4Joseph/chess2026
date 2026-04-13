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
        gameSessions.computeIfAbsent(gameID, k -> new HashSet<>()).add(session);
    }

    public void remove(int gameID, Session session) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                gameSessions.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, Session excludeSession, ServerMessage message) throws IOException {
        String msg = message.toString();
        if (message.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            excludeSession.getRemote().sendString(msg);
        } else {
            Set<Session> sessions = gameSessions.get(gameID);
            if (sessions == null) return;
            for (Session c : sessions) {
                if (c.isOpen()) {
                    if (!c.equals(excludeSession)) {
                        c.getRemote().sendString(msg);
                    }
                }
            }
        }
    }

    public void singleSend(Session session, ServerMessage message) throws IOException {
        if (session.isOpen()) {
            if (message.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
                session.getRemote().sendString(message.getErrorMessage());
            } else {
                session.getRemote().sendString(message.toString());
            }
        }
    }
}
