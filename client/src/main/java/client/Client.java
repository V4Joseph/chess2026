package client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;

import exception.ResponseException;
import model.GameData;
import model.requestsandresults.*;
import ui.ConsoleBoard;

public class Client {
        private String authToken = null;
        private final ServerFacade facade;
        private State state = State.SIGNEDOUT;
        private final ConsoleBoard board;

        public Client(String serverUrl) throws ResponseException {
            facade = new ServerFacade(serverUrl);
            board = new ConsoleBoard();
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


//        public void notify(Notification notification) {
//            System.out.println(RED + notification.message());
//            printPrompt();
//        }

        private void printPrompt() {
            System.out.print("\n" + "Please enter a menu option here: ");
        }


        public String eval(String input) {
            try {
                String[] tokens = input.toLowerCase().split(" ");
                String cmd = (tokens.length > 0) ? tokens[0] : "help";
                String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
                return switch (cmd) {
                    case "login" -> login(params);
                    case "register" -> register(params);
                    case "list" -> listGames();
                    case "join" -> joinGame();
                    case "create" -> createGame();
                    case "logout" -> logout();
                    case "quit" -> "quit";
                    default -> help();
                };
            } catch (ResponseException ex) {
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
            return "Unauthorized";
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Incomplete");
    }

    public String listGames() throws ResponseException {
            assertSignedIn();
        ListGamesResult listGamesResult = facade.listGames(authToken);
        Collection<GameData> games = listGamesResult.games();
        if (games.isEmpty()) {
            return "No available games";
        }
        StringBuilder result = new StringBuilder();
        for (GameData game : games) {
            result.append("ID: ")
                    .append(game.gameID())
                    .append(" / Name: ")
                    .append(game.gameName())
                    .append("\n");
        }
        return result.toString();
    }

    public String joinGame() throws ResponseException{
        assertSignedIn();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the ID of the game you want to join");
        int gameID = Integer.parseInt(scanner.nextLine());
        System.out.println("Please enter the color you want to play as");
        String color = scanner.nextLine();
        JoinGameRequest joinGameRequest = new JoinGameRequest(color, gameID);
        facade.joinGame(joinGameRequest, authToken);
        return String.format("Joined Game: %d",gameID);
    }

    public String createGame() throws ResponseException{
        assertSignedIn();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the name of the game you want to create");
        String gameName = scanner.nextLine();
        System.out.println("Please enter the color you want to play as");
        String color = scanner.nextLine();
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName);
        facade.createGame(createGameRequest,authToken);

        return String.format("Created Game: %s",gameName);
    }

        public String logout() throws ResponseException {
            assertSignedIn();
            facade.logout(authToken);
            state = State.SIGNEDOUT;
            authToken = null;
            return "Goodbye";
        }

        public String help() {
            if (state == State.SIGNEDOUT) {
                return """
                    - login
                    - register
                    - quit
                    """;
            }
            return """
                - join
                - create
                - list
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


