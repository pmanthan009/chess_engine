# Java Bitboard Chess Engine

## Overview
A chess engine built from scratch in Java. This project bypasses traditional array-based board representations in favor of **Bitboards** (64-bit integer mapping) to achieve lightning-fast move generation and collision detection using raw bitwise operations.

## Current Progress

### 1. Board Representation
* **Bitboard Architecture:** The board is represented using 64-bit `long` primitives, where each bit maps to a specific square on the 8x8 chessboard.
* **Hexadecimal Initialization:** The standard starting position is initialized using compressed Hex values for efficient memory allocation.
* **Occupancy Tracking:** Maintained combined bitboards for White pieces, Black pieces, and all pieces to perform O(1) collision detection.

### 2. Move Generation
The engine currently supports pseudo-legal move generation for the following pieces:

* **Pawns (White & Black):**
    * Calculates single pushes, double pushes, and diagonal captures simultaneously using bitwise shifts.
    * Implements File A and File H masks to prevent the "Wrap-Around Bug."
    * Utilizes Java's unsigned right shift (`>>>`) for Black pawns to prevent sign extension errors.
* **Knights (White & Black):**
    * Utilizes **Pre-calculated Attack Look-Up Tables**.
    * Calculates all 8 possible L-shapes for all 64 squares exactly once upon initialization.
    * In-game move generation is reduced to an O(1) array lookup masked against friendly occupancies.

### 3. Move Extraction & Bit-Twiddling
* Implemented a highly optimized move extraction loop using `Long.numberOfTrailingZeros()` to identify target squares.
* Utilizes the `targetBitboard &= (targetBitboard - 1)` trick to instantly clear the least significant bit, avoiding unnecessary loop iterations.

### 4. Debugging Utilities
* **Console Visualizer:** Includes a utility to print any 64-bit integer as an 8x8 grid to the console, making it easy to visually verify bitwise operations and attack masks.

## Next Steps
* [ ] Implement pre-calculated attack tables for the King.
* [ ] Implement sliding piece move generation (Rooks, Bishops, Queens) using Magic Bitboards or Ray Casting.
* [ ] Build the `makeMove()` and `undoMove()` functions to transition board states.
* [ ] Implement the Minimax algorithm with Alpha-Beta Pruning for the core search tree.
