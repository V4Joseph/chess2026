package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator implements PieceMovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[][] moves = {
                {-1,-1},{-1,1},{1,-1},{1,1}
        };
        for (int[] mov : moves) {
            int step = 1;
            while (true) {
                int newRow = row + (mov[0]*step);
                int newCol = col + (mov[1]*step);
                if (newRow >= 1 && newRow < 9 && newCol >= 1 && newCol < 9) {
                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
                    ChessPiece checkPosition = board.getPiece(newPosition);
                    if (checkPosition == null) {
                        possibleMoves.add(new ChessMove(myPosition,newPosition,null));
                    } else if (checkPosition.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                        possibleMoves.add(new ChessMove(myPosition,newPosition,null));
                        break;
                    } else {
                        break;
                    }
                }
                else {
                    break;
                }
                step++;
            }
        }
        return possibleMoves;
    }
}
