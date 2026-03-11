import java.util.List;

public class Search {

    private MoveGen generator = new MoveGen();
    private Evaluator evaluator = new Evaluator();

    /**
     * The root of the search tree. Evaluates all legal moves and returns the best
     * one.
     */
    public Move getBestMove(Board board, int depth, boolean isWhite) {
        List<Move> legalMoves = generator.getLegalMoves(board, isWhite);

        Move bestMove = null;
        // White wants positive infinity, Black wants negative infinity
        int bestScore = isWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // Initial absolute worst-case scenarios
        int alpha = -1000000; 
        int beta = 1000000;

        for (Move move : legalMoves) {
            // 1. Play the move
            board.makeMove(move, isWhite);

            // 2. Look into the future (Switch turns!)
            int score = minimax(board, depth - 1, alpha, beta, !isWhite);

            // 3. Undo the move to restore the board
            board.undoMove(move, isWhite);

            // 4. Did we find a better score?
            if (isWhite) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore); // Update root Alpha
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                beta = Math.min(beta, bestScore); // Update root Beta
            }
        }

        // --- FORMAT SCORE OUTPUT ---
        String displayScore;
        
        // If the score is astronomical, it's a forced Checkmate!
        if (Math.abs(bestScore) > 90000) {
            // Calculate roughly how many moves until mate based on the depth modifier
            int mateDistance = (100000 - Math.abs(bestScore) + 1) / 2; 
            
            // If positive, White is delivering mate. If negative, Black is delivering mate.
            displayScore = (bestScore > 0 ? "+M" : "-M") + Math.max(1, mateDistance);
        } else {
            // Standard evaluation: Convert centipawns to decimal (e.g., 320 -> 3.20)
            displayScore = String.format("%.2f", bestScore / 100.0);
            
            // Add a plus sign for White's advantage to match standard conventions
            if (bestScore > 0) displayScore = "+" + displayScore;
        }
        
        System.out.println("Evaluation: " + displayScore);

        return bestMove;
    }

    /**
     * The recursive time machine.
     */
    private int minimax(Board board, int depth, int alpha, int beta, boolean isWhite) {
        // 1. Generate STRICTLY LEGAL moves
        List<Move> legalMoves = generator.getLegalMoves(board, isWhite);

        // 2. CHECKMATE / STALEMATE DETECTION
        if (legalMoves.isEmpty()) {
            // Find the king
                
            long kingBoard = isWhite ? board.whiteKing : board.blackKing;
            int kingSquare = Long.numberOfTrailingZeros(kingBoard);
            
            // Are we in check?
            boolean inCheck = generator.isSquareAttacked(kingSquare, !isWhite, board);
            
            if (inCheck) {
                // Checkmate! Returning a massive score.
                // We add/subtract 'depth' so the engine prefers faster checkmates!
                return isWhite ? -99999 - depth : 99999 + depth; 
            } else {
                // Stalemate! It's a draw.
                return 0; 
            }
        }

        // Base Case: We hit the depth limit! Evaluate the board.
        if (depth == 0) {
            return evaluator.evaluate(board);
        }

        if (isWhite) {
            int maxScore = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                board.makeMove(move, true);
                int score = minimax(board, depth - 1, alpha, beta, false);
                board.undoMove(move, true);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score); // Update Alpha (White's guaranteed minimum)

                // --- ALPHA-BETA PRUNING ---
                if (beta <= alpha) {
                    break; // Black had a better option earlier in the tree. Prune this branch!
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                board.makeMove(move, false);
                int score = minimax(board, depth - 1, alpha, beta, true);
                board.undoMove(move, false);
                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score); // Update Beta (Black's guaranteed maximum)

                // --- ALPHA-BETA PRUNING ---
                if (beta <= alpha) {
                    break; // White had a better option earlier in the tree. Prune this branch!
                }
            }
            return minScore;
        }
    }
}