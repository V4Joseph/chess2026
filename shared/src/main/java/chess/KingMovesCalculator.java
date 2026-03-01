package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class KingMovesCalculator implements PieceMovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[][] moves = {
                {-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}
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
