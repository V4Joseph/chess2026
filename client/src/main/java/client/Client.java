package client;

import java.io.IOException;
import java.util.*;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.PlayerColor;
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
        private PlayerColor playerColor = PlayerColor.White;
        private final ConsoleBoard board;
        private  int maxGameNum;
        private int currentGameID;
        private String serverURL;
        private  Map<Integer, Integer> gameNum = new HashMap<>();
        private ChessGame currentGame;

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
            currentGame = game;
            String colorName = (color != null) ? color.name() : "WHITE";
            drawBoard(System.out, game.getBoard(),colorName);
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
        if (color.equalsIgnoreCase("Black")) {
            playerColor = PlayerColor.Black;
        } else {
            playerColor = PlayerColor.White;
        }
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
        state = State.INGAME;
        playerColor = PlayerColor.White;
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

        public String redraw() throws ResponseException {
            assertInGame();
            String colorName = (playerColor == PlayerColor.Black) ? "BLACK" : "WHITE";
            drawBoard(System.out, currentGame.getBoard(), colorName);
            return "";
        }
        public String leave() throws IOException, ResponseException {
            assertInGame();
            webSocketFacade.leave(authToken, currentGameID);
            state = State.SIGNEDIN;
            return "Succesfully left the game";
        }
        public String move() throws ResponseException, IOException {
            assertInGame();
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter the start and end position for your move (e2 e3)");
            String[] inputs = scanner.nextLine().split(" ");
            if (inputs.length <2) {
                return "Error: Invalid input, missing a position";
            }
            ChessPosition startPosition = findPosition(inputs[0]);
            ChessPosition endPosition = findPosition(inputs[1]);
            ChessPiece.PieceType promotion = null;
            if ((endPosition.getRow() == 1 || endPosition.getRow() == 8) && currentGame.getBoard().getPiece(startPosition).getPieceType() == ChessPiece.PieceType.PAWN) {
                System.out.println("Please enter the promotion piece");
                String input = scanner.nextLine();
                if ((ChessPiece.PieceType.valueOf(input.toUpperCase()) != ChessPiece.PieceType.QUEEN) &&
                        (ChessPiece.PieceType.valueOf(input.toUpperCase()) != ChessPiece.PieceType.BISHOP) &&
                        (ChessPiece.PieceType.valueOf(input.toUpperCase()) != ChessPiece.PieceType.KNIGHT) &&
                        (ChessPiece.PieceType.valueOf(input.toUpperCase()) != ChessPiece.PieceType.ROOK)
                ) {
                    return "Error: Invalid Promotion";
                }
                promotion = (ChessPiece.PieceType.valueOf(input.toUpperCase()));
            }
            ChessMove move = new ChessMove(startPosition, endPosition, promotion);
            webSocketFacade.makeMove(authToken,currentGameID,move);
            return "";
        }

        private ChessPosition findPosition(String position) {
            int col = position.charAt(0) - 'a' +1;
            int row = position.charAt(1) - '0';
            return new ChessPosition(row,col);
        }

        public String resign() throws IOException, ResponseException {
            assertInGame();
            webSocketFacade.resign(authToken,currentGameID);
            return "Resigned from game";
        }
        public String highlight() throws ResponseException {
            assertInGame();

            return "";
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
        private void assertInGame() throws ResponseException {
            if (!(state == State.INGAME)) {
                throw new ResponseException(ResponseException.Code.ClientError, "Must be in a game");
            }
        }
    }


