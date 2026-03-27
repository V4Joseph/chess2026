package model.requestsandresults;

import chess.ChessGame;

public record JoinGameRequest(String playerColor, Integer gameID) {

    public JoinGameRequest(ChessGame.TeamColor playerColor, Integer gameID) {
        this(playerColor.name(), gameID);
    }
}
