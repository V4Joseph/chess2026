package client;

import chess.*;
import exception.ResponseException;

public class ClientMain {
    public static void main(String[] args) throws ResponseException{
        String port = "http://localhost:8080";
        Client client = new Client(port);
        client.run();
    }
}
