package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String message;
    String errorMessage;
    ChessGame game;
    ChessGame.TeamColor color;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String message, ChessGame game, ChessGame.TeamColor color) {
        this.serverMessageType = type;
        this.message = message;
        this.game = game;
        this.color = color;
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
        this.message = null;
    }
    public static String getMessage(){return message;}
    public String getErrorMessage(){return this.errorMessage;}
    public ChessGame getGame(){return this.game;}
    public ChessGame.TeamColor getColor(){return this.color;}

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
