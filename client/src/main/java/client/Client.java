package client;

import java.io.IOException;
import java.util.*;

import chess.ChessGame;
import client.websocket.ServerMessageObserver;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import model.GameData;
import model.requestsandresults.*;
import ui.ConsoleBoard;
import websocket.messages.ServerMessage;

import static ui.ConsoleBoard.drawBoard;

// Make a map of gameID


public class Client implements ServerMessageObserver {
        private String authToken = null;
        private final ServerFacade facade;
        private WebSocketFacade webSocketFacade;
        private State state = State.SIGNEDOUT;
        private final ConsoleBoard board;
        private  int maxGameNum;
        private int currentGameID;
        private String serverURL;
        private  Map<Integer, Integer> gameNum = new HashMap<>();

        public Client(String serverUrl) throws ResponseException {
            facade = new ServerFacade(serverUrl);
            board = new ConsoleBoard();
            serverURL = serverUrl;
        }

        @Override
        public void notify(ServerMessage serverMessage) {
            switch (serverMessage.getServerMessageType()) {
                case NOTIFICATION -> displayNotification(serverMessage.getMessage());
                case ERROR -> displayError(serverMessage);
                case LOAD_GAME -> loadGame(serverMessage.getGame(), serverMessage.getColor());
            }
        }

        public void displayNotification(String message) {
            System.out.println(message);
        }
        public void displayError(ServerMessage serverMessage) {
            serverMessage.setErrorMessage(serverMessage.getMessage());
            System.out.println("Error: " + serverMessage.getErrorMessage());
        }
        public void loadGame(ChessGame game, ChessGame.TeamColor color) {
            drawBoard(System.out, game.getBoard(),color.name());
        }

        public void run() {
            System.out.println("Welcome to Chess");
            System.out.print(help());

            Scanner scanner = new Scanner(System.in);
            var result = "";
            while (!"quit".equals(result)) {
                printPrompt();
                String line = scanner.nextLine();

                try {
                    result = eval(line);
                    System.out.print(result);
                } catch (Throwable e) {
                    var msg = e.toString();
                    System.out.print(msg);
                }
            }
            System.out.println();
        }

        private void printPrompt() {
            System.out.print("\n" + "Please enter a menu option here: ");
        }


        public String eval(String input) {
            try {
                String[] tokens = input.toLowerCase().split(" ");
                String cmd = (tokens.length > 0) ? tokens[0] : "help";
                String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
                if (params.length > 1) {
                    throw new RuntimeException("Invalid Input");
                }
                return switch (cmd) {
                    case "login" -> login(params);
                    case "register" -> register(params);
                    case "list" -> listGames();
                    case "join" -> joinGame();
                    case "create" -> createGame();
                    case "observe" -> observeGame();
                    case "logout" -> logout();
                    case "quit" -> "quit";
                    case "redraw" -> redraw();
                    case "leave" -> leave();
                    case "move" -> move();
                    case "resign" -> resign();
                    case "highlight" -> highlight();
                    default -> help();
                };
            } catch (ResponseException | IOException ex) {
                return ex.getMessage();
            }
        }

        public String login(String... params) throws ResponseException {
            System.out.println("Please enter your username");
            Scanner scanner = new Scanner(System.in);
            String username = scanner.nextLine();
            System.out.println("Please enter your password");
            String password = scanner.nextLine();
            if ((password != null) && (username != null)) {
                LoginRequest loginRequest = new LoginRequest(username, password);
                LoginResult loginResult = facade.login(loginRequest);
                if (loginResult.authToken() != null) {
                    state = State.SIGNEDIN;
                    authToken = loginResult.authToken();
                    listGames();
                    return String.format("Succesfully logged in as %s", loginResult.username());
                }
            }
            throw new ResponseException(ResponseException.Code.ClientError, "Missing username or password");
        }

    public String register(String... params) throws ResponseException {
        Scanner regScan = new Scanner(System.in);
        System.out.println("Please enter a username");
        String username = regScan.nextLine();
        System.out.println("Please enter a password");
        String password = regScan.nextLine();
        System.out.println("Please enter an email");
        String email = regScan.nextLine();

        if ((!Objects.equals(password, "")) && (!Objects.equals(username, "")) && (!Objects.equals(email, ""))) {
            RegisterRequest registerRequest = new RegisterRequest(username, password, email);
            RegisterResult registerResult = facade.register(registerRequest);
            if (registerResult.authToken() != null) {
                authToken = registerResult.authToken();
                state = State.SIGNEDIN;
                return String.format("Succesfully registered as %s", registerResult.username());
            }
            return "Error: Unauthorized";
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Error: Incomplete inputs");
    }

    public String listGames() throws ResponseException {
            int i;
            maxGameNum = 0;
            assertSignedIn();
        ListGamesResult listGamesResult = facade.listGames(authToken);
        Collection<GameData> games = listGamesResult.games();
        if (games.isEmpty()) {
            return "No available games";
        }
        StringBuilder result = new StringBuilder();
        i = 1;
        for (GameData game : games) {
            result.append("Game #: ")
                    .append(i)
                    .append(" / Name: ")
                    .append(game.gameName())
                    .append(" / White: ")
                    .append(game.whiteUsername())
                    .append(" / Black: ")
                    .append(game.blackUsername())
                    .append("\n");
            gameNum.put(i,game.gameID());
            if (i>maxGameNum) {
                maxGameNum = i;
            }
            i++;
        }
        return result.toString();
    }

    public String joinGame() throws ResponseException, IOException {
        int gameID;
        assertSignedIn();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the number of the game you want to join");
        state = State.INGAME;
        try {
            gameID = gameNum.get(Integer.parseInt(scanner.nextLine()));
            currentGameID = gameID;
        } catch (Exception e) {
            throw new ResponseException(ResponseException.Code.ServerError, "Invalid Input");
        }
        System.out.println("Please enter the color you want to play as");
        String color = scanner.nextLine();
        JoinGameRequest joinGameRequest = new JoinGameRequest(color, gameID);
        facade.joinGame(joinGameRequest, authToken);
        String[] perspective = {color};
//        ConsoleBoard.main(perspective);
        webSocketFacade = new WebSocketFacade(serverURL, this);
        webSocketFacade.connect(authToken, gameID);
        return String.format("Joined Game: %d",gameID);
    }

    public String createGame() throws ResponseException{
        assertSignedIn();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the name of the game you want to create");
        String gameName = scanner.nextLine();
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName);
        CreateGameResult createGameResult = facade.createGame(createGameRequest,authToken);
        if (maxGameNum < createGameResult.gameID()) {
            maxGameNum = createGameResult.gameID();
        }
        return String.format("Created Game: %s ",gameName);
    }

    public String observeGame() throws ResponseException{
            int gameID;
        assertSignedIn();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the number of the game you want to observe");
        try {
            gameID = gameNum.get(Integer.parseInt(scanner.nextLine()));
            currentGameID = gameID;
        } catch (Exception e) {
            throw new ResponseException(ResponseException.Code.ServerError, "Invalid Input");
        }
            String[] perspective = {"white"};
            ConsoleBoard.main(perspective);
            return String.format("Now observing game #%d", gameID);

    }

        public String logout() throws ResponseException {
            assertSignedIn();
            facade.logout(authToken);
            state = State.SIGNEDOUT;
            authToken = null;
            return "Goodbye";
        }

        public String redraw() {

        }
        public String leave() throws IOException {
            webSocketFacade.leave(authToken, currentGameID);
            state = State.SIGNEDIN;
            return "Succesfully left the game";
        }
        public String move() {

        }
        public String resign() throws IOException {
            webSocketFacade.resign(authToken,currentGameID);
            return "Resigned from game";
        }
        public String highlight() {

        }

        public String help() {
            if (state == State.SIGNEDOUT) {
                return """
                    - login
                    - register
                    - quit
                    """;
            } else if (state == State.INGAME) {
                return """
                        - help
                        - redraw
                        - leave
                        - move
                        - resign
                        - highlight
                        """;
            }
            return """
                - join
                - create
                - list
                - observe
                - logout
                - quit
                """;
        }

        private void assertSignedIn() throws ResponseException {
            if (state == State.SIGNEDOUT) {
                throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
            }
        }
    }


