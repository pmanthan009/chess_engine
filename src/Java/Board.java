public class Board {

    long whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing;
    long blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing;

    long whitePieces, blackPieces, allPieces;

    // --- SPECIAL MOVE STATE ---
    // Tracks if the King and Rooks have moved yet
    public boolean whiteCanCastleKingside;
    public boolean whiteCanCastleQueenside;
    public boolean blackCanCastleKingside;
    public boolean blackCanCastleQueenside;

    // Tracks the target square index for an En Passant capture (-1 if none is
    // available)
    public int enPassantTarget;

    public Board() {
        setupStandardPosition();
        // At the start of a standard game, everyone can castle!
        whiteCanCastleKingside = true;
        whiteCanCastleQueenside = true;
        blackCanCastleKingside = true;
        blackCanCastleQueenside = true;

        // No En Passant target on turn 1
        enPassantTarget = -1;
    }

    private void setupStandardPosition() {
        // WHITE PIECES (Ranks 1 and 2)
        whitePawns = 0x000000000000FF00L; // Rank 2
        whiteRooks = 0x0000000000000081L; // A1 (bit 0) and H1 (bit 7)
        whiteKnights = 0x0000000000000042L; // B1 (bit 1) and G1 (bit 6)
        whiteBishops = 0x0000000000000024L; // C1 (bit 2) and F1 (bit 5)
        whiteQueens = 0x0000000000000008L; // D1 (bit 3)
        whiteKing = 0x0000000000000010L; // E1 (bit 4)

        // BLACK PIECES (Ranks 7 and 8)
        blackPawns = 0x00FF000000000000L; // Rank 7
        blackRooks = 0x8100000000000000L; // A8 (bit 56) and H8 (bit 63)
        blackKnights = 0x4200000000000000L; // B8 and G8
        blackBishops = 0x2400000000000000L; // C8 and F8
        blackQueens = 0x0800000000000000L; // D8
        blackKing = 0x1000000000000000L; // E8

        updateOccupancies();
    }

    /**
     * Executes a move on the board using bitwise operations.
     * 
     * @param move    The move to execute
     * @param isWhite True if it is White's turn, false if Black's
     */
    public void makeMove(Move move, boolean isWhite) {
        // --- 0. SAVE STATE MEMORY BEFORE DOING ANYTHING ---
        move.prevWhiteCanCastleKingside = this.whiteCanCastleKingside;
        move.prevWhiteCanCastleQueenside = this.whiteCanCastleQueenside;
        move.prevBlackCanCastleKingside = this.blackCanCastleKingside;
        move.prevBlackCanCastleQueenside = this.blackCanCastleQueenside;
        move.prevEnPassantTarget = this.enPassantTarget;

        long startMask = 1L << move.startSquare;
        long targetMask = 1L << move.targetSquare;
        long moveMask = startMask | targetMask;

        // Reset En Passant for the next turn (we will override this later for double
        // pawn pushes)
        this.enPassantTarget = -1;

        if (isWhite) {
            // 1. Find which White piece is moving and toggle it (Remove from start, add to
            // target)
            if ((whitePawns & startMask) != 0)
                whitePawns ^= moveMask;
            else if ((whiteKnights & startMask) != 0)
                whiteKnights ^= moveMask;
            else if ((whiteBishops & startMask) != 0)
                whiteBishops ^= moveMask;
            else if ((whiteQueens & startMask) != 0)
                whiteQueens ^= moveMask;
            // Update Castling Rights
            else if ((whiteRooks & startMask) != 0) {
                whiteRooks ^= moveMask;
                if (move.startSquare == 0)
                    whiteCanCastleQueenside = false; // A1 Rook moved
                if (move.startSquare == 7)
                    whiteCanCastleKingside = false; // H1 Rook moved
            } else if ((whiteKing & startMask) != 0) {
                whiteKing ^= moveMask;
                whiteCanCastleKingside = false;
                whiteCanCastleQueenside = false; // King moved, lost all rights
            }

            // 2. Handle Captures: If Black has a piece on the target square, remove it
            if ((blackPieces & targetMask) != 0) {
                long captureMask = ~targetMask;
                if ((blackPawns & targetMask) != 0) {
                    move.capturedPiece = 1;
                    blackPawns &= captureMask;
                } else if ((blackKnights & targetMask) != 0) {
                    move.capturedPiece = 2;
                    blackKnights &= captureMask;
                } else if ((blackBishops & targetMask) != 0) {
                    move.capturedPiece = 3;
                    blackBishops &= captureMask;
                } else if ((blackQueens & targetMask) != 0) {
                    move.capturedPiece = 5;
                    blackQueens &= captureMask;
                } else if ((blackRooks & targetMask) != 0) {
                    move.capturedPiece = 4;
                    blackRooks &= captureMask;
                } else if ((blackRooks & targetMask) != 0) {
                    move.capturedPiece = 4;
                    blackRooks &= captureMask;
                    // If we captured their Rook on its starting square, they lose castling rights!
                    if (move.targetSquare == 56)
                        blackCanCastleQueenside = false; // A8
                    if (move.targetSquare == 63)
                        blackCanCastleKingside = false; // H8
                }
                // Don't need to check the King, as Kings cannot be captured in chess
            }
        } else {
            // 1. Find which Black piece is moving and toggle it
            if ((blackPawns & startMask) != 0)
                blackPawns ^= moveMask;
            else if ((blackKnights & startMask) != 0)
                blackKnights ^= moveMask;
            else if ((blackBishops & startMask) != 0)
                blackBishops ^= moveMask;
            else if ((blackQueens & startMask) != 0)
                blackQueens ^= moveMask;
            // Update Castling Rights
            else if ((blackRooks & startMask) != 0) {
                blackRooks ^= moveMask;
                if (move.startSquare == 56)
                    blackCanCastleQueenside = false; // A8 Rook moved
                if (move.startSquare == 63)
                    blackCanCastleKingside = false; // H8 Rook moved
            } else if ((blackKing & startMask) != 0) {
                blackKing ^= moveMask;
                blackCanCastleKingside = false;
                blackCanCastleQueenside = false; // King moved, lost all rights
            }

            // 2. Handle Captures: If White has a piece on the target square, remove it
            if ((whitePieces & targetMask) != 0) {
                long captureMask = ~targetMask;
                if ((whitePawns & targetMask) != 0) {
                    move.capturedPiece = 1;
                    whitePawns &= captureMask;
                } else if ((whiteKnights & targetMask) != 0) {
                    move.capturedPiece = 2;
                    whiteKnights &= captureMask;
                } else if ((whiteBishops & targetMask) != 0) {
                    move.capturedPiece = 3;
                    whiteBishops &= captureMask;
                } else if ((whiteQueens & targetMask) != 0) {
                    move.capturedPiece = 5;
                    whiteQueens &= captureMask;
                } else if ((whiteRooks & targetMask) != 0) {
                    move.capturedPiece = 4;
                    whiteRooks &= captureMask;
                    // If we captured their Rook on its starting square, they lose castling rights!
                    if (move.targetSquare == 0)
                        whiteCanCastleQueenside = false; // A1
                    if (move.targetSquare == 7)
                        whiteCanCastleKingside = false; // H1
                }
            }
        }

        // 3. Update the occupancy summary boards
        updateOccupancies();
    }

    /**
     * Reverts a move on the board using bitwise operations.
     * 
     * @param move    The move to undo (must contain captured piece data)
     * @param isWhite True if it was White's turn when the move was MADE
     */
    public void undoMove(Move move, boolean isWhite) {
        long startMask = 1L << move.startSquare;
        long targetMask = 1L << move.targetSquare;
        long moveMask = startMask | targetMask;

        if (isWhite) {
            // 1. Move the White piece back
            if ((whitePawns & targetMask) != 0)
                whitePawns ^= moveMask;
            else if ((whiteKnights & targetMask) != 0)
                whiteKnights ^= moveMask;
            else if ((whiteBishops & targetMask) != 0)
                whiteBishops ^= moveMask;
            else if ((whiteRooks & targetMask) != 0)
                whiteRooks ^= moveMask;
            else if ((whiteQueens & targetMask) != 0)
                whiteQueens ^= moveMask;
            else if ((whiteKing & targetMask) != 0)
                whiteKing ^= moveMask;

            // 2. Resurrect the captured Black piece (if any)
            if (move.capturedPiece != 0) {
                if (move.capturedPiece == 1)
                    blackPawns |= targetMask;
                else if (move.capturedPiece == 2)
                    blackKnights |= targetMask;
                else if (move.capturedPiece == 3)
                    blackBishops |= targetMask;
                else if (move.capturedPiece == 4)
                    blackRooks |= targetMask;
                else if (move.capturedPiece == 5)
                    blackQueens |= targetMask;
            }
        } else {
            // 1. Move the Black piece back
            if ((blackPawns & targetMask) != 0)
                blackPawns ^= moveMask;
            else if ((blackKnights & targetMask) != 0)
                blackKnights ^= moveMask;
            else if ((blackBishops & targetMask) != 0)
                blackBishops ^= moveMask;
            else if ((blackRooks & targetMask) != 0)
                blackRooks ^= moveMask;
            else if ((blackQueens & targetMask) != 0)
                blackQueens ^= moveMask;
            else if ((blackKing & targetMask) != 0)
                blackKing ^= moveMask;

            // 2. Resurrect the captured White piece (if any)
            if (move.capturedPiece != 0) {
                if (move.capturedPiece == 1)
                    whitePawns |= targetMask;
                else if (move.capturedPiece == 2)
                    whiteKnights |= targetMask;
                else if (move.capturedPiece == 3)
                    whiteBishops |= targetMask;
                else if (move.capturedPiece == 4)
                    whiteRooks |= targetMask;
                else if (move.capturedPiece == 5)
                    whiteQueens |= targetMask;
            }
        }

        // --- RESTORE STATE MEMORY ---
        this.whiteCanCastleKingside = move.prevWhiteCanCastleKingside;
        this.whiteCanCastleQueenside = move.prevWhiteCanCastleQueenside;
        this.blackCanCastleKingside = move.prevBlackCanCastleKingside;
        this.blackCanCastleQueenside = move.prevBlackCanCastleQueenside;
        this.enPassantTarget = move.prevEnPassantTarget;
        
        // 3. Update the occupancy summary boards
        updateOccupancies();
    }

    /**
     * Refreshes the summary bitboards after pieces have moved.
     */

    public void updateOccupancies() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        allPieces = whitePieces | blackPieces;
    }
}