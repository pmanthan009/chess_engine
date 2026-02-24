/**
 * This is just a test file to test if the logic works.
 * It's commented out most of the times to avoid duplicate class names error.
 */

// import java.util.ArrayList;
// import java.util.List;

// public class ChessTest {

//     public static void main(String[] args) {
//         System.out.println("Initializing Board...");
//         Board board = new Board();
//         MoveGen generator = new MoveGen();

//         System.out.println("\n--- Initial White Pawns Bitboard ---");
//         printBitboard(board.whitePawns);

//         System.out.println("\n--- Generating White Pawn Moves ---");
//         List<Move> moves = generator.generateWhitePawnMoves(board);

//         System.out.println("Total moves found: " + moves.size());
//         for (Move m : moves) {
//             // Adding 1 to the index just to make it 1-64 for easier human reading
//             System.out.println("Pawn moves from index " + m.startSquare + " to " + m.targetSquare);
//         }
//     }

//     /**
//      * A crucial debugging tool. Prints any 64-bit long as an 8x8 chessboard.
//      * Rank 8 is at the top, Rank 1 is at the bottom.
//      */
//     public static void printBitboard(long bitboard) {
//         for (int rank = 7; rank >= 0; rank--) {
//             for (int file = 0; file < 8; file++) {
//                 int squareIndex = rank * 8 + file;
//                 // Check if the bit at squareIndex is a 1
//                 long mask = 1L << squareIndex;
//                 if ((bitboard & mask) != 0) {
//                     System.out.print("1 ");
//                 } else {
//                     System.out.print(". ");
//                 }
//             }
//             System.out.println("  // Rank " + (rank + 1));
//         }
//         System.out.println("A B C D E F G H\n");
//     }
// }

// // --- Simplified Board Class for Testing ---
// class Board {
//     public long whitePawns, blackPieces, allPieces;

//     public Board() {
//         // Rank 2
//         whitePawns = 0x000000000000FF00L; 
        
//         // Black pieces taking up Ranks 7 and 8
//         blackPieces = 0xFFFF000000000000L; 
        
//         // White pieces on Ranks 1 and 2, Black on 7 and 8
//         allPieces = 0xFFFF00000000FFFFL; 
//     }
// }

// // --- The MoveGen Class ---
// class MoveGen {
//     private static final long NOT_A_FILE = ~0x0101010101010101L;
//     private static final long NOT_H_FILE = ~0x8080808080808080L;
//     private static final long RANK_4 = 0x00000000FF000000L;

//     public List<Move> generateWhitePawnMoves(Board board) {
//         List<Move> moves = new ArrayList<>();
//         long emptySquares = ~board.allPieces;

//         long singlePushes = (board.whitePawns << 8) & emptySquares;
//         extractMoves(singlePushes, -8, moves); 

//         long doublePushes = (singlePushes << 8) & emptySquares & RANK_4;
//         extractMoves(doublePushes, -16, moves); 

//         long capturesRight = ((board.whitePawns & NOT_H_FILE) << 9) & board.blackPieces;
//         extractMoves(capturesRight, -9, moves);

//         long capturesLeft = ((board.whitePawns & NOT_A_FILE) << 7) & board.blackPieces;
//         extractMoves(capturesLeft, -7, moves);

//         return moves;
//     }

//     private void extractMoves(long targetBitboard, int offset, List<Move> moves) {
//         while (targetBitboard != 0) {
//             int targetSquare = Long.numberOfTrailingZeros(targetBitboard);
//             moves.add(new Move(targetSquare + offset, targetSquare));
//             targetBitboard &= (targetBitboard - 1);
//         }
//     }
// }

// // --- The Move Class ---
// class Move {
//     public int startSquare;
//     public int targetSquare;

//     public Move(int startSquare, int targetSquare) {
//         this.startSquare = startSquare;
//         this.targetSquare = targetSquare;
//     }
// }