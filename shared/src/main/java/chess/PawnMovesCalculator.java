package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator implements PieceMovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[] pawnMoves = {-1,0,1};
        ChessPiece myPiece = board.getPiece(myPosition);
        int step = 1;
        if (myPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {

            step = -1;
        }
        ChessPiece.PieceType[] promotionList = {ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT};
        for (int mov : pawnMoves) {
            int newRow = row + step;
            int newCol = col + mov;
            if (newCol >=1 && newCol < 9) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);
                if (mov == 0) {
                    if (targetPiece == null) {
                        if (newRow == 8 || newRow == 1) {
                            for (ChessPiece.PieceType p : promotionList) {
                                possibleMoves.add(new ChessMove(myPosition,newPosition,p));
                            }
                        }
                        else {
                            possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        if (myPiece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2 || myPiece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7) {
                            newRow = row + (step*2);
                            newPosition = new ChessPosition(newRow,col);
                            targetPiece = board.getPiece(newPosition);
                            if (targetPiece == null) {
                                possibleMoves.add(new ChessMove(myPosition,newPosition,null));
                            }

                        }

                    }
                } else {
                    if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) {
                        if (newRow == 8 || newRow == 1) {
                            for (ChessPiece.PieceType p : promotionList) {
                                possibleMoves.add(new ChessMove(myPosition,newPosition,p));
                            }
                        }
                        else {
                            possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }
        }
        return possibleMoves;
    }
}
