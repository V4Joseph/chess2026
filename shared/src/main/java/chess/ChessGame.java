package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor TeamTurn;
    private ChessBoard GameBoard;

    public ChessGame() {
        this.TeamTurn = TeamColor.WHITE;
        this.GameBoard = new ChessBoard();
        GameBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return TeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        TeamTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return TeamTurn == chessGame.TeamTurn && Objects.equals(GameBoard, chessGame.GameBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(TeamTurn, GameBoard);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessBoard board = getBoard();
        ChessBoard backUpBoard = new ChessBoard(board);
        ChessPiece target = board.getPiece(startPosition);
        Collection<ChessMove> MoveList;
        Collection<ChessMove> validMoveList = new ArrayList<>();
        if (target == null) {
            return null;
        }
        else {
            TeamColor targetColor = target.getTeamColor();
            MoveList = target.pieceMoves(board,startPosition);
            for (ChessMove move: MoveList) {
                makeTestMove(move);
                if (!isInCheck(targetColor)) {
                    validMoveList.add(move);
                }
                GameBoard = new ChessBoard(backUpBoard);
            }
        }

        return validMoveList;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        if (GameBoard.getPiece(startPosition) == null || GameBoard.getPiece(startPosition).getTeamColor() != TeamTurn) throw new InvalidMoveException("This move is invalid");
        Collection<ChessMove> validMoveList = validMoves(startPosition);
        if (validMoveList.contains(move)) {
            makeTestMove(move);
            if (TeamTurn == TeamColor.WHITE) {
                TeamTurn = TeamColor.BLACK;
            } else TeamTurn = TeamColor.WHITE;

        }
        else throw new InvalidMoveException("This move is invalid");
    }
    public void makeTestMove(ChessMove move) {
        ChessBoard board = getBoard();
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        ChessPiece target = board.getPiece(startPosition);
        if (target != null) {
            if (promotionPiece != null) {
                board.addPiece(endPosition, new ChessPiece(target.getTeamColor(),promotionPiece));
            }
            else board.addPiece(endPosition,target);
            board.addPiece(startPosition,null);
        }
    }
//    public void unMakeMove(ChessMove move) {
//        ChessBoard board = getBoard();
//        ChessPosition startPosition = move.getEndPosition();
//        ChessPosition endPosition = move.getStartPosition();
//        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
//        ChessPiece target = board.getPiece(startPosition);
//        if (target != null) {
//            if (promotionPiece != null) {
//                board.addPiece(endPosition, new ChessPiece(target.getTeamColor(), ChessPiece.PieceType.PAWN));
//            }
//            else board.addPiece(endPosition,target);
//            board.addPiece(startPosition,null);
//        }
//    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        ChessPosition checkPosition = null;
        kingSearch:
        for (int x = 1; x<9; x++) {
            for (int y=1;y<9;y++) {
                kingPosition = new ChessPosition(x,y);
                if (Objects.equals(getBoard().getPiece(kingPosition), new ChessPiece(teamColor, ChessPiece.PieceType.KING))) {
                    break kingSearch;
                }
            }
        }
        for (int x = 1; x<9; x++) {
            for (int y = 1; y < 9; y++) {
                checkPosition = new ChessPosition(x,y);
                ChessPiece target = getBoard().getPiece(checkPosition);
                if (target != null && target.getTeamColor() != teamColor) {
                    Collection<ChessMove> moveList = new ArrayList<>();
                    moveList = target.pieceMoves(getBoard(),checkPosition);
                    for (ChessMove move: moveList) {
                        if (Objects.equals(move.getEndPosition(), kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {

        if (isInCheck(teamColor) && validMovesChecker(teamColor)) {
            return true;
        } else return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) return false;
        else return validMovesChecker(teamColor);
    }
    public boolean validMovesChecker (TeamColor teamColor) {
        int row;
        int col;
        for (row = 1;row<9;row++) {
            for (col = 1;col<9;col++) {
                ChessPosition checkPosition = new ChessPosition(row,col);
                ChessPiece target = GameBoard.getPiece(checkPosition);
                if (target != null && target.getTeamColor() == teamColor) {
                    if (!validMoves(checkPosition).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        GameBoard = board;

    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return GameBoard;
    }
}

