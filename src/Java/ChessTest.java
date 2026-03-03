/**
 * This is just a test file to test if the logic works after every change/addition.
 */

import java.util.List;

public class ChessTest {

    public static void main(String[] args) {
        System.out.println("Initializing Board and Pre-calculating Attack Tables...");
        Board board = new Board();
        MoveGen generator = new MoveGen();

        // --- TEST WHITE KNIGHTS ---
        System.out.println("\n--- Initial White Knights Bitboard ---");
        printBitboard(board.whiteKnights);

        List<Move> whiteKnightMoves = generator.generateWhiteKnightMoves(board);
        System.out.println("Total White Knight moves found: " + whiteKnightMoves.size());
        for (Move m : whiteKnightMoves) {
            System.out.println("White Knight moves from index " + m.startSquare + " to " + m.targetSquare);
        }

        // --- TEST BLACK KNIGHTS ---
        System.out.println("\n--- Initial Black Knights Bitboard ---");
        printBitboard(board.blackKnights);

        List<Move> blackKnightMoves = generator.generateBlackKnightMoves(board);
        System.out.println("Total Black Knight moves found: " + blackKnightMoves.size());
        for (Move m : blackKnightMoves) {
            System.out.println("Black Knight moves from index " + m.startSquare + " to " + m.targetSquare);
        }

        // --- TEST ROOK MASKS ---
        System.out.println("Initializing Engine and Pre-calculating Masks...");

        System.out.println("\n--- Testing Rook Mask for D4 (Index 27) ---");
        long rookMask = generator.getRookMask(27);
        printBitboard(rookMask);

        System.out.println("\n--- Testing Bishop Mask for D4 (Index 27) ---");
        long bishopMask = generator.getBishopMask(27);
        printBitboard(bishopMask);

        // --- TEST WHITE QUEEN MAGIC BITBOARDS ---
        System.out.println("\n--- Testing White Queen on D4 ---");
        
        // Create a blank custom board for testing
        Board queenTestBoard = new Board();
        // Clear all default pieces
        queenTestBoard.whitePawns = 0L; queenTestBoard.whiteKnights = 0L; queenTestBoard.whiteBishops = 0L; queenTestBoard.whiteRooks = 0L; queenTestBoard.whiteKing = 0L;
        queenTestBoard.blackPawns = 0L; queenTestBoard.blackKnights = 0L; queenTestBoard.blackBishops = 0L; queenTestBoard.blackRooks = 0L; queenTestBoard.blackQueens = 0L; queenTestBoard.blackKing = 0L;
        
        // 1. Place a White Queen on D4 (Index 27)
        queenTestBoard.whiteQueens = 1L << 27;
        
        // 2. Place some Black Pawns as target blockers
        // D7 (Index 51) - straight up, H8 (Index 63) - up-right diagonal
        queenTestBoard.blackPawns = (1L << 51) | (1L << 63);
        
        // 3. Update the occupancies manually
        queenTestBoard.whitePieces = queenTestBoard.whiteQueens;
        queenTestBoard.blackPieces = queenTestBoard.blackPawns;
        queenTestBoard.allPieces = queenTestBoard.whitePieces | queenTestBoard.blackPieces;

        // Generate the moves
        List<Move> queenMoves = generator.generateWhiteQueenMoves(queenTestBoard);
        
        // Visualize the resulting attacks on a bitboard!
        long queenAttacksBitboard = 0L;
        for (Move m : queenMoves) {
            queenAttacksBitboard |= (1L << m.targetSquare);
        }
        
        System.out.println("Total Queen moves found: " + queenMoves.size());
        printBitboard(queenAttacksBitboard);

        // --- TEST MAKEMOVE AND UNDOMOVE ---
        System.out.println("\n--- Testing makeMove() and undoMove() ---");
        
        // 1. Setup a fresh board
        Board stateTestBoard = new Board();
        stateTestBoard.whitePawns = 0L; stateTestBoard.whiteKnights = 0L; stateTestBoard.whiteBishops = 0L; stateTestBoard.whiteRooks = 0L; stateTestBoard.whiteQueens = 0L; stateTestBoard.whiteKing = 0L;
        stateTestBoard.blackPawns = 0L; stateTestBoard.blackKnights = 0L; stateTestBoard.blackBishops = 0L; stateTestBoard.blackRooks = 0L; stateTestBoard.blackQueens = 0L; stateTestBoard.blackKing = 0L;

        // Place White Queen on D4 (27) and Black Pawn on D7 (51)
        stateTestBoard.whiteQueens = 1L << 27;
        stateTestBoard.blackPawns = 1L << 51;
        stateTestBoard.updateOccupancies();

        System.out.println("1. BEFORE MOVE:");
        System.out.println("White Queens Bitboard:");
        printBitboard(stateTestBoard.whiteQueens);
        System.out.println("Black Pawns Bitboard:");
        printBitboard(stateTestBoard.blackPawns);

        // 2. Execute the Capture
        // Move from D4 (27) to D7 (51)
        Move captureMove = new Move(27, 51); 
        
        // Note: Make sure your makeMove method is updated with the capture memory logic!
        stateTestBoard.makeMove(captureMove, true); // true = White's turn

        System.out.println("2. AFTER MOVE (Queen captures Pawn):");
        System.out.println("Captured Piece ID recorded: " + captureMove.capturedPiece + " (Should be 1 for Pawn)");
        System.out.println("White Queens Bitboard (Should be on D7):");
        printBitboard(stateTestBoard.whiteQueens);
        System.out.println("Black Pawns Bitboard (Should be completely empty):");
        printBitboard(stateTestBoard.blackPawns);

        // 3. Undo the Capture
        stateTestBoard.undoMove(captureMove, true);

        System.out.println("3. AFTER UNDO (Queen retreats, Pawn resurrected):");
        System.out.println("White Queens Bitboard (Should be back on D4):");
        printBitboard(stateTestBoard.whiteQueens);
        System.out.println("Black Pawns Bitboard (Pawn should be back on D7!):");
        printBitboard(stateTestBoard.blackPawns);
    }

    public static void printBitboard(long bitboard) {
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int squareIndex = rank * 8 + file;
                long mask = 1L << squareIndex;
                if ((bitboard & mask) != 0) {
                    System.out.print("1 ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println("  // Rank " + (rank + 1));
        }
        System.out.println("A B C D E F G H\n");
    }
}