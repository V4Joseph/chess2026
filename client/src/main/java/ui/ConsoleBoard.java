package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static ui.EscapeSequences.*;

public class ConsoleBoard {
    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 1;
    private static int highlight = 0;

    // Padded characters.
    private static final String EMPTY = "\u3000\u3000\u2009";

    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.resetBoard();
        out.print(ERASE_SCREEN);
        drawBoard(out, chessBoard, args[0], null);
        out.print(RESET_BG_COLOR);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    public static void drawBoard(PrintStream out, ChessBoard chessBoard, String color, Collection<ChessMove> validMoveList) {
        System.out.println("\n");
        drawHeaderLine(out, color);
        drawChessBoard(out, chessBoard,color, validMoveList);
        drawHeaderLine(out, color);
        out.print(RESET_BG_COLOR);
        out.print(SET_TEXT_COLOR_WHITE);
    }


    private static void drawHeaderLine(PrintStream out, String color) {

        setGray(out);
        out.print("  \u2009");
        String[] headers = {"a","b","c","d","e","f","g","h"};
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            if (color.equalsIgnoreCase("White")) {
                drawSingleHeader(out, headers[boardCol]);
            } else {
                drawSingleHeader(out, headers[BOARD_SIZE_IN_SQUARES - (boardCol+1)]);
            }
        }
        out.print(" \u3000");
        setBlack(out);
        out.println();
    }

    private static void drawSingleHeader(PrintStream out, String headerText) {
        int totalWidth = EMPTY.length() * SQUARE_SIZE_IN_PADDED_CHARS;
        int padding = totalWidth-1;
        int leftPad = padding/2;
        int rightPad = padding - leftPad;

        out.print(" \u2009".repeat(leftPad));
        printHeaderText(out, headerText);
        out.print(" \u2009".repeat(rightPad));
    }

    private static void printHeaderText(PrintStream out, String player) {
        setText(out);
        out.print(player);
        setGray(out);
    }

    private static void drawChessBoard(PrintStream out, ChessBoard chessBoard, String color, Collection<ChessMove> validMoveList) {
        for (int boardRow = 0; boardRow < BOARD_SIZE_IN_SQUARES; ++boardRow) {
            int label;
            if (color.equalsIgnoreCase("White")) {
                label = BOARD_SIZE_IN_SQUARES - boardRow;
            } else {
                label = boardRow+1;
            }
            drawRowOfSquares(out, boardRow, label, chessBoard, color,validMoveList);
        }
    }

    private static void drawRowOfSquares(PrintStream out, int boardRow, int label, ChessBoard chessBoard, String color, Collection<ChessMove> validMoveList) {
        for (int squareRow = 0; squareRow < SQUARE_SIZE_IN_PADDED_CHARS; squareRow++) {
            int shift = 0;
            ChessPosition position;
            if (color.equalsIgnoreCase("Black")) {
                shift = 1;
            }
            setText(out);
            if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS/2) {
                out.print(" " + label + " ");
            } else {
                out.print(EMPTY);
            }

            for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
                highlight = 0;
                if (validMoveList != null) {
                    for (ChessMove m : validMoveList) {
                        int row = m.getEndPosition().getRow()-1;
                        int col = m.getEndPosition().getColumn() - 1;
                        if (color.equalsIgnoreCase("black")) {
                            col = 7- col;
                        } else {
                            row = 7-row;
                        }
                        if (boardCol == col && boardRow == row) {
                            out.print(SET_BG_COLOR_YELLOW);
                            highlight = 1;
                        }
                    }
                }
                if ((boardCol + boardRow) % 2 == 0 && highlight == 0) {
                    out.print(SET_BG_COLOR_MAGENTA);

                } else if (highlight == 0){
                    out.print(SET_BG_COLOR_DARK_GREEN);

                }


                if (shift == 0) {
                    position = new ChessPosition(7-boardRow+1,boardCol+1);
                } else {
                    position = new ChessPosition(boardRow+1,7-boardCol+1);
                }
                if (chessBoard.getPiece(position) != null) {
                    ChessPiece piece = chessBoard.getPiece(position);
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        out.print(SET_TEXT_COLOR_WHITE);
                    } else {
                        out.print(SET_TEXT_COLOR_BLACK);
                    }
                    switch (piece.getPieceType()) {
                        case PAWN:
                            out.print(BLACK_PAWN);
                            break;
                        case ROOK:
                            out.print(BLACK_ROOK);
                            break;
                        case QUEEN:
                            out.print(BLACK_QUEEN);
                            break;
                        case BISHOP:
                            out.print(BLACK_BISHOP);
                            break;
                        case KING:
                            out.print(BLACK_KING);
                            break;
                        case KNIGHT:
                            out.print(BLACK_KNIGHT);
                            break;
                        case null, default:
                            out.print(EMPTY.repeat(SQUARE_SIZE_IN_PADDED_CHARS));
                            break;
                    }
                } else {
                    out.print(EMPTY.repeat(SQUARE_SIZE_IN_PADDED_CHARS));
                }
                setBlack(out);
            }
            setText(out);
            if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS/2) {
                out.print(" " + label + " ");
            } else {
                out.print(EMPTY);
            }
            setBlack(out);
            out.println();
        }
    }

    private static void setGray(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_LIGHT_GREY);
    }

    private static void setBlack(PrintStream out) {
        out.print(RESET_BG_COLOR);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setText(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_BLACK);
    }

}
