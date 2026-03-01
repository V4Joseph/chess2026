package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[][] moves = {
                {2,1},{2,-1},{-2,1},{-2,-1},{1,2},{-1,2},{1,-2},{-1,-2}
        };
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
