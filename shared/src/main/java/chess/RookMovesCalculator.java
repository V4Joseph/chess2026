package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMovesCalculator implements PieceMovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] moves = {
                {-1,0},{0,-1},{0,1},{1,0}
        };
        UnlimitedMoves unlimitedMoves = new UnlimitedMoves();
        return unlimitedMoves.movePiece(board, myPosition, moves);
    }
}
