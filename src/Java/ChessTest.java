/**
* This is just a test file to test if the logic works after every change/addition.
* It's commented out most of the times to avoid duplicate class names error.
*/

// import java.util.ArrayList;
// import java.util.List;

// public class ChessTest {

//     public static void main(String[] args) {
//         System.out.println("Initializing Board and Pre-calculating Attack Tables...");
//         Board board = new Board();
//         MoveGen generator = new MoveGen();

//         // --- TEST WHITE KNIGHTS ---
//         System.out.println("\n--- Initial White Knights Bitboard ---");
//         printBitboard(board.whiteKnights);

//         List<Move> whiteKnightMoves = generator.generateWhiteKnightMoves(board);
//         System.out.println("Total White Knight moves found: " + whiteKnightMoves.size());
//         for (Move m : whiteKnightMoves) {
//             System.out.println("White Knight moves from index " + m.startSquare + " to " + m.targetSquare);
//         }

//         // --- TEST BLACK KNIGHTS ---
//         System.out.println("\n--- Initial Black Knights Bitboard ---");
//         printBitboard(board.blackKnights);

//         List<Move> blackKnightMoves = generator.generateBlackKnightMoves(board);
//         System.out.println("Total Black Knight moves found: " + blackKnightMoves.size());
//         for (Move m : blackKnightMoves) {
//             System.out.println("Black Knight moves from index " + m.startSquare + " to " + m.targetSquare);
//         }
//     }

//     public static void printBitboard(long bitboard) {
//         for (int rank = 7; rank >= 0; rank--) {
//             for (int file = 0; file < 8; file++) {
//                 int squareIndex = rank * 8 + file;
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

// // --- The Board Class ---
// class Board {
//     public long whitePawns, whiteKnights;
//     public long blackPawns, blackKnights;
//     public long whitePieces, blackPieces, allPieces;

//     public Board() {
//         // Standard starting positions
//         whitePawns = 0x000000000000FF00L; 
//         whiteKnights = 0x0000000000000042L; // B1 (bit 1) and G1 (bit 6)
        
//         blackPawns = 0x00FF000000000000L; 
//         blackKnights = 0x4200000000000000L; // B8 (bit 57) and G8 (bit 62)
        
//         // Group the pieces for collision detection
//         whitePieces = whitePawns | whiteKnights;
//         blackPieces = blackPawns | blackKnights;
//         allPieces = whitePieces | blackPieces; 
//     }
// }

// // --- The MoveGen Class ---
// class MoveGen {
//     private static final long NOT_A_FILE = ~0x0101010101010101L;
//     private static final long NOT_H_FILE = ~0x8080808080808080L;
//     private static final long NOT_AB_FILE = ~0x0303030303030303L;
//     private static final long NOT_GH_FILE = ~0xC0C0C0C0C0C0C0C0L;

//     // The Pre-calculated Look-Up Table
//     private final long[] knightAttacks = new long[64];

//     public MoveGen() {
//         initKnightAttacks();
//     }

//     private void initKnightAttacks() {
//         for (int square = 0; square < 64; square++) {
//             long knight = 1L << square;
//             long attacks = 0L;

//             // Generate the 8 possible L-shapes using shifts and masks
//             attacks |= (knight << 17) & NOT_A_FILE;
//             attacks |= (knight << 15) & NOT_H_FILE;
//             attacks |= (knight << 10) & NOT_AB_FILE;
//             attacks |= (knight << 6)  & NOT_GH_FILE;
            
//             attacks |= (knight >>> 17) & NOT_H_FILE;
//             attacks |= (knight >>> 15) & NOT_A_FILE;
//             attacks |= (knight >>> 10) & NOT_GH_FILE;
//             attacks |= (knight >>> 6)  & NOT_AB_FILE;

//             knightAttacks[square] = attacks;
//         }
//     }

//     public List<Move> generateWhiteKnightMoves(Board board) {
//         List<Move> moves = new ArrayList<>();
//         long knights = board.whiteKnights; 
        
//         // White Knights can land on empty squares OR Black pieces, but NOT White pieces
//         long validSquares = ~board.whitePieces; 

//         while (knights != 0) {
//             int startSquare = Long.numberOfTrailingZeros(knights);
//             long attacks = knightAttacks[startSquare] & validSquares;
            
//             extractMoves(attacks, startSquare, moves);
//             knights &= (knights - 1); // Clear the processed knight
//         }
//         return moves;
//     }

//     public List<Move> generateBlackKnightMoves(Board board) {
//         List<Move> moves = new ArrayList<>();
//         long knights = board.blackKnights; 
        
//         // Black Knights can land on empty squares OR White pieces, but NOT Black pieces
//         long validSquares = ~board.blackPieces; 

//         while (knights != 0) {
//             int startSquare = Long.numberOfTrailingZeros(knights);
//             long attacks = knightAttacks[startSquare] & validSquares;
            
//             extractMoves(attacks, startSquare, moves);
//             knights &= (knights - 1); // Clear the processed knight
//         }
//         return moves;
//     }

//     // --- OVERLOADED EXTRACT MOVES ---
    
//     // For pieces like Knights where we already know the exact start square
//     private void extractMoves(long targetBitboard, int startSquare, List<Move> moves) {
//         while (targetBitboard != 0) {
//             int targetSquare = Long.numberOfTrailingZeros(targetBitboard);
//             moves.add(new Move(startSquare, targetSquare));
//             targetBitboard &= (targetBitboard - 1);
//         }
//     }

//     // For pieces like Pawns where we calculate the start square using a mathematical offset
//     private void extractMovesWithOffset(long targetBitboard, int offset, List<Move> moves) {
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