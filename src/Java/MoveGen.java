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
    private final long[] kingAttacks = new long[64];

    // --- MAGIC BITBOARD VARIABLES ---
    
    // 1. The Masks (Which squares can block a sliding piece on a given square?)
    private final long[] rookMasks = new long[64];
    private final long[] bishopMasks = new long[64];
    
    // 2. The Attack Tables (The pre-calculated moves)
    // First index is the square (0-63). 
    // Second index is the hashed blocker arrangement (up to 4096 possible combinations for a Rook).
    private final long[][] rookAttacks = new long[64][4096];
    private final long[][] bishopAttacks = new long[64][512]; // Bishops have fewer blockers

    // 3. The Magic Numbers
    private static final long[] ROOK_MAGICS = {
        0x8a80104000800020L, 0x140002000100040L, 0x2801880a0017001L, 0x100081001000420L,
        0x200020010080420L, 0x3001c0002010008L, 0x8480008002000100L, 0x2080088004402900L,
        0x800098204000L, 0x2024401000200040L, 0x100802000801000L, 0x120800800801000L,
        0x208808088000400L, 0x2802200800400L, 0x2200800100020080L, 0x801000060821100L,
        0x80044006422000L, 0x100808020004000L, 0x12108a0010204200L, 0x140848010000802L,
        0x481828014002800L, 0x8094004002004100L, 0x4010040010010802L, 0x200040006043300L,
        0x804040008008080L, 0x800002800400800L, 0x200028104220200L, 0x11001401040202L,
        0x200040E08040050L, 0x1001A0011002020L, 0x400202100800800L, 0x800202001008100L,
        0x400200100208100L, 0x20480240060A00L, 0x810100420800010L, 0x1002000A08012010L,
        0x100408100240000L, 0x100000100210004L, 0x120102040020200L, 0x100810208010L,
        0x100401004010L, 0x402002004000801L, 0x100100121004200L, 0x20002010400080L,
        0x200008204000400L, 0x801210000204000L, 0x201020800040100L, 0x204010020004L,
        0x40100200004L, 0x8000104004000L, 0x1008008002000L, 0x2020208004000L,
        0x102002008200L, 0x2021004010400L, 0x10400120100L, 0x10800201000L,
        0x8000010400L, 0x4200002000L, 0x100800200L, 0x204010000L,
        0x4080020L, 0x1020010L, 0x80080L, 0x20040L
    };

    private static final long[] BISHOP_MAGICS = {
        0x40040844404084L, 0x2004208A004208L, 0x10190041080202L, 0x108060845042010L,
        0x581104180800210L, 0x2112080446200010L, 0x1080820820060210L, 0x20041420100804L,
        0x410810049020004L, 0x10021104080202L, 0x80010405020102L, 0x14010404010202L,
        0x10004020810100L, 0x1121020804210L, 0x20002011080400L, 0x10800804104104L,
        0x40080208041042L, 0x811080801041010L, 0x40001021104014L, 0x80000410410400L,
        0x4040008041000L, 0x4002082004400L, 0x8020140201100L, 0x11100404104104L,
        0x10400200104242L, 0x2014010040101L, 0x4004010020402L, 0x8000104004004L,
        0x1002082001010L, 0x4020004010404L, 0x4080104010444L, 0x20808010104202L,
        0x104040804002L, 0x2040410104101L, 0x8020800040104L, 0x10010400100101L,
        0x204004011040L, 0x10210100402L, 0x208040080100L, 0x2004010080041L,
        0x4104004041L, 0x820101002081L, 0x204101020800L, 0x140410040100L,
        0x420410000100L, 0x1082100400L, 0x1010100400L, 0x1001041000L,
        0x20204100L, 0x20804080L, 0x40210100L, 0x80100040L,
        0x8002010L, 0x4001008L, 0x401000L, 0x82004L,
        0x10020L, 0x20010L, 0x1001L, 0x10002L,
        0x4200L, 0x80L, 0x100L, 0x2L
    };

    public MoveGen() {
        initKnightAttacks();
        initKingAttacks();
        initSlidingMasks();
        initMagicBitboards();
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

    private void initKingAttacks() {
        for (int square = 0; square < 64; square++) {
            long king = 1L << square;
            long attacks = 0L;

            // 1. Horizontal and Vertical
            attacks |= (king << 8); // Up
            attacks |= (king >>> 8); // Down
            attacks |= (king & NOT_H_FILE) << 1; // Right (+1 index)
            attacks |= (king & NOT_A_FILE) >>> 1; // Left (-1 index)

            // 2. Diagonals
            attacks |= (king & NOT_H_FILE) << 9; // Up-Right
            attacks |= (king & NOT_A_FILE) << 7; // Up-Left
            attacks |= (king & NOT_H_FILE) >>> 7; // Down-Right
            attacks |= (king & NOT_A_FILE) >>> 9; // Down-Left

            // Store the fully calculated attack bitboard in the array
            kingAttacks[square] = attacks;
        }
    }

    private void initSlidingMasks() {
        for (int square = 0; square < 64; square++) {
            rookMasks[square] = maskRookAttacks(square);
            bishopMasks[square] = maskBishopAttacks(square);
        }
    }

    private void initMagicBitboards() {
        for (int square = 0; square < 64; square++) {
            // --- ROOKS ---
            long rookMask = rookMasks[square];
            int rookBits = Long.bitCount(rookMask);
            int rookCombinations = 1 << rookBits; // 2^rookBits (max 4096)

            for (int i = 0; i < rookCombinations; i++) {
                long blockers = setOccupancy(i, rookBits, rookMask);
                int magicIndex = magicHash(blockers, ROOK_MAGICS[square], rookBits);
                rookAttacks[square][magicIndex] = rookAttacksOnTheFly(square, blockers);
            }

            // --- BISHOPS ---
            long bishopMask = bishopMasks[square];
            int bishopBits = Long.bitCount(bishopMask);
            int bishopCombinations = 1 << bishopBits; // 2^bishopBits (max 512)

            for (int i = 0; i < bishopCombinations; i++) {
                long blockers = setOccupancy(i, bishopBits, bishopMask);
                int magicIndex = magicHash(blockers, BISHOP_MAGICS[square], bishopBits);
                bishopAttacks[square][magicIndex] = bishopAttacksOnTheFly(square, blockers);
            }
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

    // --- KING MOVE GENERATION ---

    public List<Move> generateWhiteKingMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long kingBoard = board.whiteKing; 
        
        // The King can move to any square that DOES NOT contain a White piece
        long validSquares = ~board.whitePieces; 

        if (kingBoard != 0) {
            int startSquare = Long.numberOfTrailingZeros(kingBoard);
            long attacks = kingAttacks[startSquare] & validSquares;
            
            extractMoves(attacks, startSquare, moves);
        }
        return moves;
    }

    public List<Move> generateBlackKingMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long kingBoard = board.blackKing; 
        
        // The King can move to any square that DOES NOT contain a Black piece
        long validSquares = ~board.blackPieces; 

        if (kingBoard != 0) {
            int startSquare = Long.numberOfTrailingZeros(kingBoard);
            long attacks = kingAttacks[startSquare] & validSquares;
            
            extractMoves(attacks, startSquare, moves);
        }
        return moves;
    }

    public List<Move> generateWhiteRookMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long rooks = board.whiteRooks; // Make sure to add this to your Board class!
        long validSquares = ~board.whitePieces; 

        while (rooks != 0) {
            int startSquare = Long.numberOfTrailingZeros(rooks);
            
            // 1. Get ONLY the blockers on the board that matter for this square
            long blockers = board.allPieces & rookMasks[startSquare];
            
            // 2. Hash it to find the index
            int magicIndex = magicHash(blockers, ROOK_MAGICS[startSquare], Long.bitCount(rookMasks[startSquare]));
            
            // 3. O(1) Lookup and mask out friendly pieces
            long attacks = rookAttacks[startSquare][magicIndex] & validSquares;
            
            extractMoves(attacks, startSquare, moves);
            rooks &= (rooks - 1); 
        }
        return moves;
    }

    public List<Move> generateBlackRookMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long rooks = board.blackRooks; // Make sure to add this to your Board class!
        long validSquares = ~board.blackPieces; 

        while (rooks != 0) {
            int startSquare = Long.numberOfTrailingZeros(rooks);
            
            // 1. Get ONLY the blockers on the board that matter for this square
            long blockers = board.allPieces & rookMasks[startSquare];
            
            // 2. Hash it to find the index
            int magicIndex = magicHash(blockers, ROOK_MAGICS[startSquare], Long.bitCount(rookMasks[startSquare]));
            
            // 3. O(1) Lookup and mask out friendly pieces
            long attacks = rookAttacks[startSquare][magicIndex] & validSquares;
            
            extractMoves(attacks, startSquare, moves);
            rooks &= (rooks - 1); 
        }
        return moves;
    }

    public List<Move> generateWhiteBishopMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long bishops = board.whiteBishops; // Make sure to add this to your Board class!
        long validSquares = ~board.whitePieces; 

        while (bishops != 0) {
            int startSquare = Long.numberOfTrailingZeros(bishops);
            
            // 1. Get ONLY the blockers on the board that matter for this square
            long blockers = board.allPieces & bishopMasks[startSquare];
            
            // 2. Hash it to find the index
            int magicIndex = magicHash(blockers, BISHOP_MAGICS[startSquare], Long.bitCount(bishopMasks[startSquare]));
            
            // 3. O(1) Lookup and mask out friendly pieces
            long attacks = bishopAttacks[startSquare][magicIndex] & validSquares;
            
            extractMoves(attacks, startSquare, moves);
            bishops &= (bishops - 1); 
        }
        return moves;
    }

    public List<Move> generateBlackBishopMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long bishops = board.blackBishops; // Make sure to add this to your Board class!
        long validSquares = ~board.blackPieces; 

        while (bishops != 0) {
            int startSquare = Long.numberOfTrailingZeros(bishops);
            
            // 1. Get ONLY the blockers on the board that matter for this square
            long blockers = board.allPieces & bishopMasks[startSquare];
            
            // 2. Hash it to find the index
            int magicIndex = magicHash(blockers, BISHOP_MAGICS[startSquare], Long.bitCount(bishopMasks[startSquare]));
            
            // 3. O(1) Lookup and mask out friendly pieces
            long attacks = bishopAttacks[startSquare][magicIndex] & validSquares;
            
            extractMoves(attacks, startSquare, moves);
            bishops &= (bishops - 1); 
        }
        return moves;
    }

    // --- QUEEN MOVE GENERATION ---

    public List<Move> generateWhiteQueenMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long queens = board.whiteQueens; // Ensure this is in your Board class!
        long validSquares = ~board.whitePieces; 

        while (queens != 0) {
            int startSquare = Long.numberOfTrailingZeros(queens);
            
            // 1. Get Rook-style attacks
            long rookBlockers = board.allPieces & rookMasks[startSquare];
            int rookIndex = magicHash(rookBlockers, ROOK_MAGICS[startSquare], Long.bitCount(rookMasks[startSquare]));
            long rookAttacksBoard = rookAttacks[startSquare][rookIndex];

            // 2. Get Bishop-style attacks
            long bishopBlockers = board.allPieces & bishopMasks[startSquare];
            int bishopIndex = magicHash(bishopBlockers, BISHOP_MAGICS[startSquare], Long.bitCount(bishopMasks[startSquare]));
            long bishopAttacksBoard = bishopAttacks[startSquare][bishopIndex];

            // 3. Combine them and mask out friendly pieces
            long queenAttacksBoard = (rookAttacksBoard | bishopAttacksBoard) & validSquares;
            
            extractMoves(queenAttacksBoard, startSquare, moves);
            queens &= (queens - 1); 
        }
        return moves;
    }

    public List<Move> generateBlackQueenMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        long queens = board.blackQueens; 
        long validSquares = ~board.blackPieces; 

        while (queens != 0) {
            int startSquare = Long.numberOfTrailingZeros(queens);
            
            // 1. Get Rook-style attacks
            long rookBlockers = board.allPieces & rookMasks[startSquare];
            int rookIndex = magicHash(rookBlockers, ROOK_MAGICS[startSquare], Long.bitCount(rookMasks[startSquare]));
            long rookAttacksBoard = rookAttacks[startSquare][rookIndex];

            // 2. Get Bishop-style attacks
            long bishopBlockers = board.allPieces & bishopMasks[startSquare];
            int bishopIndex = magicHash(bishopBlockers, BISHOP_MAGICS[startSquare], Long.bitCount(bishopMasks[startSquare]));
            long bishopAttacksBoard = bishopAttacks[startSquare][bishopIndex];

            // 3. Combine them and mask out friendly pieces
            long queenAttacksBoard = (rookAttacksBoard | bishopAttacksBoard) & validSquares;
            
            extractMoves(queenAttacksBoard, startSquare, moves);
            queens &= (queens - 1); 
        }
        return moves;
    }
    // --- MAGIC BITBOARD MASK GENERATION ---

    /**
     * Generates a mask for a Rook on a specific square, EXCLUDING the outer edges.
     */
    private long maskRookAttacks(int square) {
        long mask = 0L;
        int targetRank = square / 8;
        int targetFile = square % 8;

        // Up (Stop at rank 6, leaving rank 7/edge alone)
        for (int r = targetRank + 1; r <= 6; r++) {
            mask |= (1L << (r * 8 + targetFile));
        }
        // Down (Stop at rank 1, leaving rank 0/edge alone)
        for (int r = targetRank - 1; r >= 1; r--) {
            mask |= (1L << (r * 8 + targetFile));
        }
        // Right (Stop at file 6, leaving file 7/edge alone)
        for (int f = targetFile + 1; f <= 6; f++) {
            mask |= (1L << (targetRank * 8 + f));
        }
        // Left (Stop at file 1, leaving file 0/edge alone)
        for (int f = targetFile - 1; f >= 1; f--) {
            mask |= (1L << (targetRank * 8 + f));
        }
        return mask;
    }

    /**
     * Generates a mask for a Bishop on a specific square, EXCLUDING the outer edges.
     */
    private long maskBishopAttacks(int square) {
        long mask = 0L;
        int targetRank = square / 8;
        int targetFile = square % 8;

        // Up-Right
        for (int r = targetRank + 1, f = targetFile + 1; r <= 6 && f <= 6; r++, f++) {
            mask |= (1L << (r * 8 + f));
        }
        // Up-Left
        for (int r = targetRank + 1, f = targetFile - 1; r <= 6 && f >= 1; r++, f--) {
            mask |= (1L << (r * 8 + f));
        }
        // Down-Right
        for (int r = targetRank - 1, f = targetFile + 1; r >= 1 && f <= 6; r--, f++) {
            mask |= (1L << (r * 8 + f));
        }
        // Down-Left
        for (int r = targetRank - 1, f = targetFile - 1; r >= 1 && f >= 1; r--, f--) {
            mask |= (1L << (r * 8 + f));
        }
        return mask;
    }

    /**
     * Maps an integer index to a specific combination of blockers on a mask.
     * * @param index The arrangement number (e.g., 0 to 4095)
     * @param bitsInMask The number of '1's in the attack mask
     * @param attackMask The chopped-edge mask we generated earlier
     * @return A bitboard representing one specific combination of blocking pieces
     */
    private long setOccupancy(int index, int bitsInMask, long attackMask) {
        long occupancy = 0L;

        // Loop through the total number of bits in our mask
        for (int count = 0; count < bitsInMask; count++) {
            // Find the square index of the lowest '1' in the attack mask
            int square = Long.numberOfTrailingZeros(attackMask);
            
            // Instantly clear that lowest '1' so we can find the next one on the next loop iteration
            attackMask &= (attackMask - 1);
            
            // Check if the 'count'-th bit is set to '1' in our index integer
            // If it is, we place a blocker on the corresponding square in our occupancy board
            if ((index & (1 << count)) != 0) {
                occupancy |= (1L << square);
            }
        }
        return occupancy;
    }

    /**
     * Calculates true Rook attacks using slow Ray Casting. 
     * ONLY USED DURING INITIALIZATION to populate the Magic arrays.
     */
    private long rookAttacksOnTheFly(int square, long block) {
        long attacks = 0L;
        int targetRank = square / 8;
        int targetFile = square % 8;

        // Up
        for (int r = targetRank + 1; r <= 7; r++) {
            attacks |= (1L << (r * 8 + targetFile));
            if ((block & (1L << (r * 8 + targetFile))) != 0) break; // Hit a piece, stop
        }
        // Down
        for (int r = targetRank - 1; r >= 0; r--) {
            attacks |= (1L << (r * 8 + targetFile));
            if ((block & (1L << (r * 8 + targetFile))) != 0) break;
        }
        // Right
        for (int f = targetFile + 1; f <= 7; f++) {
            attacks |= (1L << (targetRank * 8 + f));
            if ((block & (1L << (targetRank * 8 + f))) != 0) break;
        }
        // Left
        for (int f = targetFile - 1; f >= 0; f--) {
            attacks |= (1L << (targetRank * 8 + f));
            if ((block & (1L << (targetRank * 8 + f))) != 0) break;
        }
        return attacks;
    }

    /**
     * Calculates true Bishop attacks using slow Ray Casting. 
     * ONLY USED DURING INITIALIZATION to populate the Magic arrays.
     */
    private long bishopAttacksOnTheFly(int square, long block) {
        long attacks = 0L;
        int targetRank = square / 8;
        int targetFile = square % 8;

        // Up-Right
        for (int r = targetRank + 1, f = targetFile + 1; r <= 7 && f <= 7; r++, f++) {
            attacks |= (1L << (r * 8 + f));
            if ((block & (1L << (r * 8 + f))) != 0) break;
        }
        // Up-Left
        for (int r = targetRank + 1, f = targetFile - 1; r <= 7 && f >= 0; r++, f--) {
            attacks |= (1L << (r * 8 + f));
            if ((block & (1L << (r * 8 + f))) != 0) break;
        }
        // Down-Right
        for (int r = targetRank - 1, f = targetFile + 1; r >= 0 && f <= 7; r--, f++) {
            attacks |= (1L << (r * 8 + f));
            if ((block & (1L << (r * 8 + f))) != 0) break;
        }
        // Down-Left
        for (int r = targetRank - 1, f = targetFile - 1; r >= 0 && f >= 0; r--, f--) {
            attacks |= (1L << (r * 8 + f));
            if ((block & (1L << (r * 8 + f))) != 0) break;
        }
        return attacks;
    }

    // --- HELPER METHODS ---

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

    /**
     * The Magic Hash: Compresses a 64-bit blocker board into a small array index.
     * @param blockers The current pieces blocking the sliding piece
     * @param magic The Magic Number for this specific square
     * @param bits The number of bits in the mask (e.g., 12 for a Rook on D4)
     */
    private int magicHash(long blockers, long magic, int bits) {
        return (int) ((blockers * magic) >>> (64 - bits));
    }

    // --- GETTERS & SETTERS ---
    public long getRookMask(int square) {
        return rookMasks[square];
    }

    public long getBishopMask(int square) {
        return bishopMasks[square];
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