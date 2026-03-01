package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board;
    public ChessBoard() {
        this.board = new ChessPiece[8][8];
    }
    public ChessBoard(ChessBoard copy) {
        board = new ChessPiece[8][8];
        for(int x = 1;x<9;x++) {
            for (int y=1;y<9;y++) {
                ChessPosition targetPos = new ChessPosition(x,y);
                ChessPiece target = copy.getPiece(targetPos);
                if (target != null) {
                    addPiece(targetPos,new ChessPiece(target.getTeamColor(),target.getPieceType()));
                }
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow();
        int col = position.getColumn();
        board[row-1][col-1] = piece;


    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[8][8];

        // Place pawns
        for (int col = 1; col < 9; col++) {
            ChessPiece whitePawn = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            ChessPiece blackPawn = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);

            addPiece(new ChessPosition(2, col), whitePawn);
            addPiece(new ChessPosition(7, col), blackPawn);
        }

        ChessPiece.PieceType[] backRow = {
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        };

        for (int col = 1; col < 9; col++) {
            ChessPiece whitePiece = new ChessPiece(ChessGame.TeamColor.WHITE, backRow[col-1]);
            ChessPiece blackPiece = new ChessPiece(ChessGame.TeamColor.BLACK, backRow[col-1]);

            addPiece(new ChessPosition(1, col), whitePiece);
            addPiece(new ChessPosition(8, col), blackPiece);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
