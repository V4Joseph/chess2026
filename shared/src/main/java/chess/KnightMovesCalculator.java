package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] moves = {
                {2,1},{2,-1},{-2,1},{-2,-1},{1,2},{-1,2},{1,-2},{-1,-2}
        };
        LimitedMoves limitedMoves = new LimitedMoves();
        return limitedMoves.movePiece(board,myPosition,moves);
    }
}
