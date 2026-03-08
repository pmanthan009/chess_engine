# Java Bitboard Chess Engine

**Author:** Manthan Patel

## Overview
A high-performance chess engine built from scratch in Java. This project bypasses traditional array-based board representations in favor of **Bitboards** (64-bit integer mapping) to achieve lightning-fast move generation and collision detection using raw bitwise operations.

## Current Progress

### 1. Board Representation
* **Bitboard Architecture:** The board is represented using 64-bit `long` primitives, where each bit maps to a specific square on the 8x8 chessboard.
* **Hexadecimal Initialization:** The standard starting position is initialized using compressed Hex values for efficient memory allocation.
* **Occupancy Tracking:** Maintained combined bitboards for White pieces, Black pieces, and all pieces to perform O(1) collision detection.

### 2. Move Generation
The engine currently supports pseudo-legal and strictly legal move generation for all piece types:

* **Pawns (White & Black):**
    * Calculates single pushes, double pushes, and diagonal captures simultaneously using bitwise shifts.
    * Implements File A and File H masks to prevent the "Wrap-Around Bug."
    * Utilizes Java's unsigned right shift (`>>>`) for Black pawns to prevent sign extension errors.
    * **En Passant:** Accurately detects and captures "Ghost Pawns" left behind by double-pushes, reaching behind the target square to clear the enemy piece.
    * **Promotions:** Utilizes a specialized extraction loop to seamlessly morph pawns into Knights, Bishops, Rooks, or Queens upon reaching the final rank.
* **Knights & Kings (Leaping Pieces):**
    * Utilizes **Pre-calculated Attack Look-Up Tables**.
    * Calculates all possible attack squares for all 64 board positions exactly once upon initialization.
    * In-game move generation is reduced to an O(1) array lookup masked against friendly occupancies.
* **Rooks, Bishops, & Queens (Sliding Pieces):**
    * Implemented **Magic Bitboards** for O(1) sliding piece move generation, completely eliminating in-game `while` loops.
    * Generates blocker masks that strategically exclude outer edges to compress the hash tables (e.g., 4096 combinations for Rooks, 512 for Bishops).
    * **Dynamic Magic Generator:** Bypasses fragile hardcoded arrays by implementing a brute-force algorithm that discovers perfect, collision-free 64-bit Magic Numbers dynamically during engine startup (mirroring the architecture of modern engines like Stockfish).
    * Queens efficiently reuse and combine (bitwise OR) the Rook and Bishop lookup tables.

### 3. Move Extraction & Bit-Twiddling
* Implemented a highly optimized move extraction loop using `Long.numberOfTrailingZeros()` to identify target squares.
* Utilizes the `targetBitboard &= (targetBitboard - 1)` trick to instantly clear the least significant bit, avoiding unnecessary loop iterations.

### 4. Board State Management & Spatial Awareness
* Implemented `makeMove()` and `undoMove()` functions to physically transition the board between states using extremely fast bitwise XOR (`^`) operations.
* **Special Moves Execution:** Handles complex state changes like Castling (simultaneous King/Rook teleportation via XOR) and piece morphing without array corruption.
* Engineered a memory-aware `Move` object that acts as a historical snapshot. It records captured piece IDs, castling rights, and En Passant targets, allowing the engine to perfectly reconstruct previous game states during deep search tree reversions.
* **Reverse Attack Detection:** Implemented an O(1) `isSquareAttacked()` function utilizing the pre-calculated attack tables to give the engine spatial awareness for Castling path validation and Check detection.

### 5. Static Evaluation (The AI's "Taste")
* **Hardware-Accelerated Material Counting:** Utilizes Java's `Long.bitCount()` (which maps to the CPU's native `popcnt` instruction) to count pieces across 64 squares in a single CPU cycle.
* **Centipawn Scoring:** Evaluates board states using standard centipawn metrics (Pawn = 100, Knight/Bishop = 300, Rook = 500, Queen = 900) to allow for granular positional bonuses.
* **Piece-Square Tables (PSTs):** Implemented an array of positional bonuses to grant the engine strategic intuition (e.g., centralizing knights, tucking the King away, pushing pawns).
* **Bitwise Mirroring:** Reuses White's Piece-Square Tables for Black by dynamically flipping the target square indices using a vertical mirror XOR calculation (`square ^ 56`).

### 6. Debugging Utilities
* **Console Visualizer:** Includes a utility to print any 64-bit integer as an 8x8 grid to the console, making it easy to visually verify bitwise operations and attack masks.

## Test Outputs

### 1. Knights:
<img width="733" height="845" alt="Screenshot 2026-03-04 172742" src="https://github.com/user-attachments/assets/124c9622-db8f-420d-a362-96481980fa5d" />

### 2. Sliding Pieces (Bishops, Rooks, Queens):
<img width="618" height="869" alt="Screenshot 2026-03-04 172759" src="https://github.com/user-attachments/assets/ab344b77-9c10-4024-aff5-7b25f0e00ae6" />

### 3. Captures (with a demo of undoMove() as well):
<img width="537" height="548" alt="Screenshot 2026-03-04 172826" src="https://github.com/user-attachments/assets/24b2a8c7-9aa2-404d-a256-4c7f7372d298" />
<img width="681" height="548" alt="Screenshot 2026-03-04 172847" src="https://github.com/user-attachments/assets/09765381-4cad-407b-bae3-819b391b52eb" />
<img width="659" height="550" alt="Screenshot 2026-03-04 172858" src="https://github.com/user-attachments/assets/296c151c-3dfa-480a-8bdc-6f6ad90801f5" />

### 4. Castling:
<img width="695" height="788" alt="Screenshot 2026-03-04 172934" src="https://github.com/user-attachments/assets/69132b18-284c-4026-a7c6-c0205dff1fdb" />
<img width="707" height="869" alt="Screenshot 2026-03-04 172955" src="https://github.com/user-attachments/assets/298d0135-3fd8-45fc-9547-37d7ab937057" />

### 5. Pawn Promotions:
<img width="669" height="694" alt="Screenshot 2026-03-06 235203" src="https://github.com/user-attachments/assets/2cba195b-04a8-4ba7-aa81-1b0f32b26988" />
<img width="632" height="531" alt="Screenshot 2026-03-06 235214" src="https://github.com/user-attachments/assets/114cf213-e894-499b-9626-a75aa2ce3f80" />

## Next Steps
* [x] Implement pre-calculated attack tables for the King.
* [x] Implement sliding piece move generation (Rooks, Bishops, Queens) using Magic Bitboards.
* [x] Build the `makeMove()` and `undoMove()` functions to transition board states.
* [x] Implement `isSquareAttacked()` for spatial awareness.
* [x] Implement Special Moves (Castling, En Passant, Pawn Promotions) and State History.
* [x] Develop a static board evaluation function.
* [ ] Implement the Minimax algorithm with Alpha-Beta Pruning.
