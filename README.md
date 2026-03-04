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
    * Utilizes perfect hashing functions to map current board blocker occupancies to pre-calculated ray-casted attack tables.
    * Queens efficiently reuse and combine (bitwise OR) the Rook and Bishop lookup tables.

### 3. Move Extraction & Bit-Twiddling
* Implemented a highly optimized move extraction loop using `Long.numberOfTrailingZeros()` to identify target squares.
* Utilizes the `targetBitboard &= (targetBitboard - 1)` trick to instantly clear the least significant bit, avoiding unnecessary loop iterations.

### 4. Board State Management
* Implemented `makeMove()` and `undoMove()` functions to physically transition the board between states using extremely fast bitwise XOR (`^`) operations.
* Engineered a memory-aware `Move` object that records captured piece IDs, allowing the engine to perfectly reconstruct and resurrect captured pieces during state reversions.
* Established the foundational state-space traversal mechanics required for deep search tree algorithms without causing board state corruption.

### 5. Debugging Utilities
* **Console Visualizer:** Includes a utility to print any 64-bit integer as an 8x8 grid to the console, making it easy to visually verify bitwise operations and attack masks.

## Next Steps
* [ ] Add special moves i.e Castling, En Passant & Pawn-promotions.
* [ ] Develop a static board evaluation function (e.g., piece values, piece-square tables).
* [ ] Implement the Minimax algorithm with Alpha-Beta Pruning for the core search tree.