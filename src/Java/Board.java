public class Board {
    
    long whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing;
    long blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing;
    
    long whitePieces, blackPieces, allPieces;

    public Board() {
        setupStandardPosition();
    }

    private void setupStandardPosition() {
        // WHITE PIECES (Ranks 1 and 2)
        whitePawns   = 0x000000000000FF00L; // Rank 2
        whiteRooks   = 0x0000000000000081L; // A1 (bit 0) and H1 (bit 7)
        whiteKnights = 0x0000000000000042L; // B1 (bit 1) and G1 (bit 6)
        whiteBishops = 0x0000000000000024L; // C1 (bit 2) and F1 (bit 5)
        whiteQueens  = 0x0000000000000008L; // D1 (bit 3)
        whiteKing    = 0x0000000000000010L; // E1 (bit 4)

        // BLACK PIECES (Ranks 7 and 8)
        blackPawns   = 0x00FF000000000000L; // Rank 7
        blackRooks   = 0x8100000000000000L; // A8 (bit 56) and H8 (bit 63)
        blackKnights = 0x4200000000000000L; // B8 and G8
        blackBishops = 0x2400000000000000L; // C8 and F8
        blackQueens  = 0x0800000000000000L; // D8
        blackKing    = 0x1000000000000000L; // E8

        updateOccupancies();
    }

    private void updateOccupancies() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        allPieces = whitePieces | blackPieces;
    }
}