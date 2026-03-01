package service.RR_Classes;

public record JoinGameRequest(String authToken, String color, int gameID) {
}
