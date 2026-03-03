package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class KingMovesCalculator implements PieceMovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] moves = {
                {-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}
        };
        LimitedMoves limitedMoves = new LimitedMoves();
        return limitedMoves.movePiece(board, myPosition, moves);
    }
}
