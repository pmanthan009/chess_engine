# Java Bitboard Chess Engine

**Author:** Manthan Patel

## Overview
A high-performance chess engine built from scratch in Java. This project bypasses traditional array-based board representations in favor of **Bitboards** (64-bit integer mapping) to achieve lightning-fast move generation and collision detection using raw bitwise operations.

## Current Progress

### 1. Board Representation
* **Bitboard Architecture:** The board is represented using 64-bit `long` primitives, where each bit maps to a specific square on the 8x8 chessboard.
* **Hexadecimal Initialization:** The standard starting position is initialized using compressed Hex values for efficient memory allocation.
* **Occupancy Tracking:** Maintained combined bitboards for White pieces, Black pieces, and all pieces to perform $O(1)$ collision detection.

### 2. Move Generation
The engine currently supports pseudo-legal move generation for all piece types:

* **Pawns (White & Black):**
    * Calculates single pushes, double pushes, and diagonal captures simultaneously using bitwise shifts.
    * Implements File A and File H masks to prevent the "Wrap-Around Bug."
    * Utilizes Java's unsigned right shift (`>>>`) for Black pawns to prevent sign extension errors.
* **Knights & Kings (Leaping Pieces):**
    * Utilizes **Pre-calculated Attack Look-Up Tables**.
    * Calculates all possible attack squares for all 64 board positions exactly once upon initialization.
    * In-game move generation is reduced to an $O(1)$ array lookup masked against friendly occupancies.
* **Rooks, Bishops, & Queens (Sliding Pieces):**
    * Implemented **Magic Bitboards** for $O(1)$ sliding piece move generation, completely eliminating in-game `while` loops.
    * Generates blocker masks that strategically exclude outer edges to compress the hash tables (e.g., 4096 combinations for Rooks, 512 for Bishops).
    * **Dynamic Magic Generator:** Bypasses fragile hardcoded arrays by implementing a brute-force algorithm that discovers perfect, collision-free 64-bit Magic Numbers dynamically during engine startup (mirroring the architecture of modern engines like Stockfish).
    * Queens efficiently reuse and combine (bitwise OR) the Rook and Bishop lookup tables.

### 3. Move Extraction & Bit-Twiddling
* Implemented a highly optimized move extraction loop using `Long.numberOfTrailingZeros()` to identify target squares.
* Utilizes the `targetBitboard &= (targetBitboard - 1)` trick to instantly clear the least significant bit, avoiding unnecessary loop iterations.

### 4. Board State Management & Spatial Awareness
* Implemented `makeMove()` and `undoMove()` functions to physically transition the board between states using extremely fast bitwise XOR (`^`) operations.
* Engineered a memory-aware `Move` object that records captured piece IDs, allowing the engine to perfectly reconstruct and resurrect captured pieces during state reversions.
* **Reverse Attack Detection:** Implemented an $O(1)$ `isSquareAttacked()` function utilizing the pre-calculated attack tables to give the engine spatial awareness for Castling and Check detection.

### 5. Debugging Utilities
* **Console Visualizer:** Includes a utility to print any 64-bit integer as an 8x8 grid to the console, making it easy to visually verify bitwise operations and attack masks.

## Next Steps
* [x] Implement pre-calculated attack tables for the King.
* [x] Implement sliding piece move generation (Rooks, Bishops, Queens) using Magic Bitboards.
* [x] Build the `makeMove()` and `undoMove()` functions to transition board states.
* [x] Implement `isSquareAttacked()` for spatial awareness.
* [ ] Implement Special Moves (Castling, En Passant, Pawn Promotions) and State History.
* [ ] Develop a static board evaluation function.
* [ ] Implement the Minimax algorithm with Alpha-Beta Pruning.