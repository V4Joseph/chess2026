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
        ChessPiece.PieceType[] promotionList =
                {ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT};
        for (int mov : pawnMoves) {
            int newRow = row + step;
            int newCol = col + mov;
            if (newCol >=1 && newCol < 9) {
                movePawn(board, myPosition, mov, newRow, newCol, promotionList, possibleMoves, myPiece, row, step, col);
            }
        }
        return possibleMoves;
    }

    private static void movePawn(ChessBoard board,
                                 ChessPosition myPosition,
                                 int mov, int newRow,
                                 int newCol,
                                 ChessPiece.PieceType[] promotionList,
                                 List<ChessMove> possibleMoves,
                                 ChessPiece myPiece,
                                 int row,
                                 int step,
                                 int col) {
        ChessPosition newPosition = new ChessPosition(newRow, newCol);
        ChessPiece targetPiece = board.getPiece(newPosition);
        if (mov == 0 && targetPiece == null) {
            forwardMove(board, myPosition, newRow, promotionList, possibleMoves, newPosition, myPiece, row, step, col);
        } else if (mov != 0){
            diagonalMoves(myPosition, targetPiece, myPiece, newRow, promotionList, possibleMoves, newPosition);
        }
    }

    private static void forwardMove(ChessBoard board,
                                    ChessPosition myPosition,
                                    int newRow,
                                    ChessPiece.PieceType[] promotionList,
                                    List<ChessMove> possibleMoves,
                                    ChessPosition newPosition,
                                    ChessPiece myPiece,
                                    int row,
                                    int step,
                                    int col) {
        ChessPiece targetPiece;
        if (newRow == 8 || newRow == 1) {
            for (ChessPiece.PieceType p : promotionList) {
                possibleMoves.add(new ChessMove(myPosition, newPosition,p));
            }
        }
        else {
            possibleMoves.add(new ChessMove(myPosition, newPosition, null));
        }
        if (myPiece.getTeamColor() == ChessGame.TeamColor.WHITE &&
            row == 2 ||
            myPiece.getTeamColor() == ChessGame.TeamColor.BLACK &&
            row == 7) {
            newRow = row + (step *2);
            newPosition = new ChessPosition(newRow, col);
            targetPiece = board.getPiece(newPosition);
            if (targetPiece == null) {
                possibleMoves.add(new ChessMove(myPosition, newPosition,null));
            }
        }
    }

    private static void diagonalMoves(ChessPosition myPosition,
                                      ChessPiece targetPiece,
                                      ChessPiece myPiece,
                                      int newRow,
                                      ChessPiece.PieceType[] promotionList,
                                      List<ChessMove> possibleMoves,
                                      ChessPosition newPosition) {
        if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) {
            if (newRow == 8 || newRow == 1) {
                for (ChessPiece.PieceType p : promotionList) {
                    possibleMoves.add(new ChessMove(myPosition, newPosition,p));
                }
            }
            else {
                possibleMoves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
    }
}
