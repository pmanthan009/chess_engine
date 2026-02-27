import java.util.ArrayList;
import java.util.List;

public class MoveGen {

    // --- BITMASKS ---
    // Masks to prevent the "Wrap-Around Bug"
    private static final long NOT_A_FILE = ~0x0101010101010101L;
    private static final long NOT_H_FILE = ~0x8080808080808080L;
    
    // Masks for pieces that move two squares horizontally (Knights)
    private static final long NOT_AB_FILE = ~0x0303030303030303L;
    private static final long NOT_GH_FILE = ~0xC0C0C0C0C0C0C0C0L;
    
    // Masks for double pawn pushes
    private static final long RANK_4 = 0x00000000FF000000L;
    private static final long RANK_5 = 0x000000FF00000000L;

    // --- PRE-CALCULATED TABLES ---
    private final long[] knightAttacks = new long[64];

    public MoveGen() {
        initKnightAttacks();
    }

    private void initKnightAttacks() {
        for (int square = 0; square < 64; square++) {
            long knight = 1L << square;
            long attacks = 0L;

            // Generate the 8 possible L-shapes
            attacks |= (knight << 17) & NOT_A_FILE;
            attacks |= (knight << 15) & NOT_H_FILE;
            attacks |= (knight << 10) & NOT_AB_FILE;
            attacks |= (knight << 6)  & NOT_GH_FILE;
            
            attacks |= (knight >>> 17) & NOT_H_FILE;
            attacks |= (knight >>> 15) & NOT_A_FILE;
            attacks |= (knight >>> 10) & NOT_GH_FILE;
            attacks |= (knight >>> 6)  & NOT_AB_FILE;

            knightAttacks[square] = attacks;
        }
    }

    // --- PAWN MOVE GENERATION ---

    public List<Move> generateWhitePawnMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long emptySquares = ~board.allPieces;

        long singlePushes = (board.whitePawns << 8) & emptySquares;
        extractMovesWithOffset(singlePushes, -8, moves); 

        long doublePushes = (singlePushes << 8) & emptySquares & RANK_4;
        extractMovesWithOffset(doublePushes, -16, moves); 

        long capturesRight = ((board.whitePawns & NOT_H_FILE) << 9) & board.blackPieces;
        extractMovesWithOffset(capturesRight, -9, moves);

        long capturesLeft = ((board.whitePawns & NOT_A_FILE) << 7) & board.blackPieces;
        extractMovesWithOffset(capturesLeft, -7, moves);

        return moves;
    }

    public List<Move> generateBlackPawnMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long emptySquares = ~board.allPieces;

        long singlePushes = (board.blackPawns >>> 8) & emptySquares;
        extractMovesWithOffset(singlePushes, 8, moves);

        long doublePushes = (singlePushes >>> 8) & emptySquares & RANK_5;
        extractMovesWithOffset(doublePushes, 16, moves);

        long capturesAFile = ((board.blackPawns & NOT_A_FILE) >>> 9) & board.whitePieces;
        extractMovesWithOffset(capturesAFile, 9, moves);

        long capturesHFile = ((board.blackPawns & NOT_H_FILE) >>> 7) & board.whitePieces;
        extractMovesWithOffset(capturesHFile, 7, moves);

        return moves;
    }

    // --- KNIGHT MOVE GENERATION ---

    public List<Move> generateWhiteKnightMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long knights = board.whiteKnights; 
        
        // White Knights can land on empty squares OR Black pieces, but NOT White pieces
        long validSquares = ~board.whitePieces; 

        while (knights != 0) {
            int startSquare = Long.numberOfTrailingZeros(knights);
            long attacks = knightAttacks[startSquare] & validSquares;
            
            extractMoves(attacks, startSquare, moves);
            knights &= (knights - 1); 
        }
        return moves;
    }

    public List<Move> generateBlackKnightMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long knights = board.blackKnights; 
        
        // Black Knights can land on empty squares OR White pieces, but NOT Black pieces
        long validSquares = ~board.blackPieces; 

        while (knights != 0) {
            int startSquare = Long.numberOfTrailingZeros(knights);
            long attacks = knightAttacks[startSquare] & validSquares;
            
            extractMoves(attacks, startSquare, moves);
            knights &= (knights - 1); 
        }
        return moves;
    }

    // --- HELPER METHODS FOR EXTRACTING MOVES ---

    /**
     * For pieces like Knights where we already know the exact start square.
     */
    private void extractMoves(long targetBitboard, int startSquare, List<Move> moves) {
        while (targetBitboard != 0) {
            int targetSquare = Long.numberOfTrailingZeros(targetBitboard);
            moves.add(new Move(startSquare, targetSquare));
            targetBitboard &= (targetBitboard - 1);
        }
    }

    /**
     * For pieces like Pawns where we calculate the start square using a mathematical offset.
     */
    private void extractMovesWithOffset(long targetBitboard, int offset, List<Move> moves) {
        while (targetBitboard != 0) {
            int targetSquare = Long.numberOfTrailingZeros(targetBitboard);
            moves.add(new Move(targetSquare + offset, targetSquare));
            targetBitboard &= (targetBitboard - 1);
        }
    }
}

/**
 * A simple Move structure to hold the start and end coordinates.
 * In a full engine, you'd expand this to track things like promotions or
 * captured pieces.
 */
class Move {
    public int startSquare;
    public int targetSquare;

    public Move(int startSquare, int targetSquare) {
        this.startSquare = startSquare;
        this.targetSquare = targetSquare;
    }

    @Override
    public String toString() {
        return "Move from " + startSquare + " to " + targetSquare;
    }
}