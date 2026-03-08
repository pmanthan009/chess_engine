/**
 * Evaluates a given board state and returns an integer score.
 * Positive scores favor White, negative scores favor Black.
 */
public class Evaluator {

    // --- STANDARD PIECE VALUES (in centipawns) ---
    // 100 = 1 Pawn. This gives us room to add smaller positional bonuses later.
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 300;
    private static final int BISHOP_VALUE = 300;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    // --- PIECE-SQUARE TABLES (Tuned from White's perspective) ---
    
    // Pawns want to push forward and control the center.
    private static final int[] PAWN_PST = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10,-20,-20, 10, 10,  5,
         5, -5,-10,  0,  0,-10, -5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5,  5, 10, 25, 25, 10,  5,  5,
        10, 10, 20, 30, 30, 20, 10, 10,
        50, 50, 50, 50, 50, 50, 50, 50,
         0,  0,  0,  0,  0,  0,  0,  0
    };

    // Knights are terrible on the edges ("A Knight on the rim is dim").
    private static final int[] KNIGHT_PST = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    // Bishops like long diagonals and hate being trapped on the edges.
    private static final int[] BISHOP_PST = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };

    // --- KING SAFETY (Middlegame) ---
    // The King should stay on the back rank, tucked away in the corners.
    // Notice the massive bonuses for C1 (Index 2) and G1 (Index 6) to encourage Castling!
    private static final int[] KING_PST = {
         20,  30,  10,   0,   0,  10,  30,  20,
         20,  20,   0,   0,   0,   0,  20,  20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30
    };

    /**
     * Calculates the static evaluation of the board based purely on material difference.
     */
    public int evaluate(Board board) {
        int whiteScore = 0;
        int blackScore = 0;

        // --- WHITE EVALUATION (Material + Position) ---
        whiteScore += Long.bitCount(board.whitePawns) * PAWN_VALUE;
        whiteScore += evaluatePositional(board.whitePawns, PAWN_PST, true);

        whiteScore += Long.bitCount(board.whiteKnights) * KNIGHT_VALUE;
        whiteScore += evaluatePositional(board.whiteKnights, KNIGHT_PST, true);

        whiteScore += Long.bitCount(board.whiteBishops) * BISHOP_VALUE;
        whiteScore += evaluatePositional(board.whiteBishops, BISHOP_PST, true);

        whiteScore += Long.bitCount(board.whiteRooks) * ROOK_VALUE; // Rooks just use raw material for now
        whiteScore += Long.bitCount(board.whiteQueens) * QUEEN_VALUE; 

        whiteScore += evaluatePositional(board.whiteKing, KING_PST, true);

        // --- BLACK EVALUATION (Material + Position) ---
        blackScore += Long.bitCount(board.blackPawns) * PAWN_VALUE;
        blackScore += evaluatePositional(board.blackPawns, PAWN_PST, false);

        blackScore += Long.bitCount(board.blackKnights) * KNIGHT_VALUE;
        blackScore += evaluatePositional(board.blackKnights, KNIGHT_PST, false);

        blackScore += Long.bitCount(board.blackBishops) * BISHOP_VALUE;
        blackScore += evaluatePositional(board.blackBishops, BISHOP_PST, false);

        blackScore += Long.bitCount(board.blackRooks) * ROOK_VALUE;
        blackScore += Long.bitCount(board.blackQueens) * QUEEN_VALUE;

        blackScore += evaluatePositional(board.blackKing, KING_PST, false);
        // Positive means White is winning, Negative means Black is winning
        return whiteScore - blackScore;
    }

    // --- HELPER METHODS ---

    /**
     * Extracts pieces from a bitboard and sums their positional bonuses from the PST.
     */
    private int evaluatePositional(long bitboard, int[] pst, boolean isWhite) {
        int positionalScore = 0;
        while (bitboard != 0) {
            int square = Long.numberOfTrailingZeros(bitboard);
            
            // If Black, flip the square vertically so it reads the White-oriented table correctly
            int tableIndex = isWhite ? square : (square ^ 56);
            
            positionalScore += pst[tableIndex];
            
            // Clear the LSB
            bitboard &= (bitboard - 1);
        }
        return positionalScore;
    }
}