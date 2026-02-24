import java.util.ArrayList;
import java.util.List;

public class MoveGen {

    // Bitmasks to prevent the "Wrap-Around Bug"
    private static final long NOT_A_FILE = ~0x0101010101010101L;
    private static final long NOT_H_FILE = ~0x8080808080808080L;

    // Bitmask for Rank 4 (used to validate double pushes)
    private static final long RANK_4 = 0x00000000FF000000L;
    // Bitmask for Rank 5 (used to validate Black double pushes)
    private static final long RANK_5 = 0x000000FF00000000L;

    /**
     * Generates all legal pawn moves for White and returns them as a list of Move
     * objects.
     */
    public List<Move> generateWhitePawnMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long emptySquares = ~board.allPieces;

        // 1. Single Pushes (Shift up 8)
        long singlePushes = (board.whitePawns << 8) & emptySquares;
        // The origin square is exactly 8 squares behind the target
        extractMoves(singlePushes, -8, moves);

        // 2. Double Pushes (Shift single pushes up 8 again, must land on Rank 4)
        long doublePushes = (singlePushes << 8) & emptySquares & RANK_4;
        // The origin square is exactly 16 squares behind the target
        extractMoves(doublePushes, -16, moves);

        // 3. Captures Right (Shift up and right 9)
        long capturesRight = ((board.whitePawns & NOT_H_FILE) << 9) & board.blackPieces;
        extractMoves(capturesRight, -9, moves);

        // 4. Captures Left (Shift up and left 7)
        long capturesLeft = ((board.whitePawns & NOT_A_FILE) << 7) & board.blackPieces;
        extractMoves(capturesLeft, -7, moves);

        return moves;
    }

    /**
     * Generates all legal pawn moves for Black and returns them as a list of Move
     * objects.
     */
    public List<Move> generateBlackPawnMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long emptySquares = ~board.allPieces;

        // 1. Single Pushes (Shift down 8 using UNSIGNED right shift)
        long singlePushes = (board.blackPawns >>> 8) & emptySquares;
        // The origin square is exactly +8 squares ahead of the target index
        extractMoves(singlePushes, 8, moves);

        // 2. Double Pushes (Shift single pushes down 8 again, must land on Rank 5)
        long doublePushes = (singlePushes >>> 8) & emptySquares & RANK_5;
        // The origin square is exactly +16 squares ahead of the target index
        extractMoves(doublePushes, 16, moves);

        // 3. Captures towards the A-File (Shift down and left 9)
        // A pawn on B7 (index 50) captures to A6 (index 41). 50 - 41 = 9.
        long capturesAFile = ((board.blackPawns & NOT_A_FILE) >>> 9) & board.whitePieces;
        extractMoves(capturesAFile, 9, moves);

        // 4. Captures towards the H-File (Shift down and right 7)
        // A pawn on G7 (index 54) captures to H6 (index 47). 54 - 47 = 7.
        long capturesHFile = ((board.blackPawns & NOT_H_FILE) >>> 7) & board.whitePieces;
        extractMoves(capturesHFile, 7, moves);

        return moves;
    }

    /**
     * Helper method to scan a bitboard, find the 1s, and convert them into Move
     * objects.
     * * @param targetBitboard The bitboard containing the valid destination squares
     * 
     * @param offset The mathematical difference between the target square and the
     *               start square
     * @param moves  The list to append the generated moves to
     */
    private void extractMoves(long targetBitboard, int offset, List<Move> moves) {

        // Loop runs as long as there is at least one '1' left in the bitboard
        while (targetBitboard != 0) {

            // Long.numberOfTrailingZeros finds the index of the first '1' bit starting from
            // the right (0 to 63)
            int targetSquare = Long.numberOfTrailingZeros(targetBitboard);

            // Calculate where the piece originally came from
            int startSquare = targetSquare + offset;

            // Create the move and add it to our list
            moves.add(new Move(startSquare, targetSquare));

            // CRITICAL STEP: Clear the least significant '1' bit so we don't process it
            // again.
            // (targetBitboard - 1) flips the lowest '1' and everything right of it.
            // ANDing it with the original bitboard zeroes out exactly that lowest '1'.
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