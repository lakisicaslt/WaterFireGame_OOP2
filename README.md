# 🔥 Pipe Rescue

A Java Swing puzzle game where you connect pipes to guide water from the source to extinguish a fire.

## About

**Pipe Rescue** is a grid-based puzzle game built with Java Swing. Players must strategically place pipe segments on a 7×10 board to create a connected path from a randomly placed water source (left edge) to a fire (right edge). The goal: route the water to extinguish the flames before time runs out.

## Features

- **6 pipe types** — Horizontal, Vertical, and 4 L-bend variations
- **Animated water flow** — BFS-based pathfinding with water shimmer effects
- **Animated fire & water source** — Dynamic flame particles and water ripple graphics
- **Dark-themed UI** — Professional GitHub-dark color palette
- **Move counter & timer** — Track your efficiency
- **Right-click to remove** — Undo pipe placements easily
- **Randomized layouts** — Water source and fire positions change each game

## How to Play

1. Select a pipe type from the sidebar
2. Click an empty cell on the grid to place it
3. Build a connected path from 💧 to 🔥
4. Water flows automatically when a valid path is detected
5. Extinguish the fire to win!

## Tech Stack

- **Language:** Java
- **GUI:** Swing / AWT
- **Rendering:** Custom `Graphics2D` with gradients, anti-aliasing, and animation timers
- **Architecture:** OOP with abstract base classes (`Kvadrat`, `Cev`) and polymorphic pipe types

## Running

```bash
javac -d bin src/aplikacija/*.java
java -cp bin aplikacija.Aplikacija
```
