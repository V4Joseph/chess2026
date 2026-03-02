package service.RR_Classes;

import chess.ChessGame;

public record JoinGameRequest(String playerColor, Integer gameID) {

    public JoinGameRequest(ChessGame.TeamColor playerColor, Integer gameID) {
        this(playerColor.name(), gameID);
    }

    public JoinGameRequest(String playerColor, Integer gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }
}
