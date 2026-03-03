package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LimitedMoves {
    private ChessBoard board;
    private ChessPosition myPosition;
    private int [][] moves;

    public LimitedMoves() {
        this.board = board;
        this.myPosition = myPosition;
        this.moves = moves;
    }

    public Collection<ChessMove> movePiece (ChessBoard board, ChessPosition myPosition, int [][] moves) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        for (int[] mov : moves) {
            int newRow = row + mov[0];
            int newCol = col + mov[1];
            if (newRow >= 1 && newRow < 9 && newCol >= 1 && newCol < 9) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece checkPosition = board.getPiece(newPosition);
                if (checkPosition == null || checkPosition.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    possibleMoves.add(new ChessMove(myPosition,newPosition,null));
                }
            }
        }
        return possibleMoves;
    }
}
