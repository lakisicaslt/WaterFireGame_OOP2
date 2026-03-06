package aplikacija;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Game board panel that manages the grid of cells.
 * Handles water source/fire placement, pipe placement, and water flow logic.
 */
public class Kanalizacija extends JPanel {

    Kvadrat[][] mreza;
    Kvadrat oznacen = null;
    int rows, cols;
    WaterSource waterSource;
    FireCell fireCell;
    int waterRow, fireRow;
    private Aplikacija app;

    private javax.swing.Timer animTimer;

    public Kanalizacija(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        setLayout(new GridLayout(rows, cols, 1, 1));
        setBackground(new Color(13, 17, 23)); // Dark gap color = grid lines
        setBorder(BorderFactory.createLineBorder(new Color(48, 54, 61), 2));

        initGrid();

        // Animation timer for fire and water source
        animTimer = new javax.swing.Timer(80, e -> {
            if (waterSource != null) waterSource.tick();
            if (fireCell != null) fireCell.tick();
        });
        animTimer.start();
    }

    public void setApp(Aplikacija app) {
        this.app = app;
    }

    private void initGrid() {
        removeAll();
        mreza = new Kvadrat[rows][cols];

        Random rand = new Random();
        waterRow = rand.nextInt(rows);
        fireRow = rand.nextInt(rows);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == waterRow && j == 0) {
                    waterSource = new WaterSource();
                    mreza[i][j] = waterSource;
                } else if (i == fireRow && j == cols - 1) {
                    fireCell = new FireCell();
                    mreza[i][j] = fireCell;
                } else {
                    mreza[i][j] = new Zid();
                }
                mreza[i][j].setKanalizacija(this);
                add(mreza[i][j]);
            }
        }

        revalidate();
        repaint();
    }

    public void newGame() {
        oznacen = null;
        initGrid();
    }

    /**
     * Called when a cell is left-clicked.
     * Selects the cell and places the currently selected pipe type.
     */
    public void cellClicked(Kvadrat cell) {
        // Deselect previous
        if (oznacen != null) {
            try {
                oznacen.postaviOznaku(false);
                oznacen.repaint();
            } catch (GOznaka e) { /* ignore */ }
        }

        // Select new
        try {
            cell.postaviOznaku(true);
            oznacen = cell;
            cell.repaint();
        } catch (GOznaka e) {
            oznacen = null;
            return;
        }

        // Place pipe if a type is selected and cell is a placeable Zid
        if (app != null && cell instanceof Zid) {
            Cev newPipe = app.getSelectedPipe();
            if (newPipe != null) {
                cell.postaviCev(newPipe);
                app.incrementMoves();
            }
        }
    }

    /**
     * Called on right-click: removes a pipe from a cell.
     */
    public void removeCev(Kvadrat cell) {
        if (cell instanceof Zid && cell.getCev() != null) {
            cell.postaviCev(null);
            cell.repaint();
        }
    }

    /**
     * Legacy method kept for compatibility.
     */
    public void promeniStatusOznake(Kvadrat kvadrat) {
        cellClicked(kvadrat);
    }

    /**
     * Legacy method kept for compatibility.
     */
    public void dodajCev(Kvadrat kvadrat, Cev cev) {
        if (oznacen == null) return;
        if (kvadrat == oznacen) {
            oznacen.postaviCev(cev);
        }
        repaint();
    }

    // ===== WATER FLOW BFS LOGIC =====

    /**
     * Animate water flowing from source through connected pipes.
     * Uses level-by-level BFS with a timer for progressive visual effect.
     *
     * @param onNoPath called if water doesn't reach fire
     */
    public void animateFlow(Runnable onNoPath) {
        // Reset all water states
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cev c = mreza[i][j].getCev();
                if (c != null) c.setHasWater(false);
            }
        }
        repaint();

        // Prepare BFS
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new LinkedList<>();
        visited[waterRow][0] = true;

        // Water exits RIGHT from water source -> check cell at (waterRow, 1)
        if (cols > 1) {
            int ni = waterRow;
            int nj = 1;
            Cev neighborPipe = mreza[ni][nj].getCev();
            if (neighborPipe != null && neighborPipe.getConnections().contains(Direction.LEFT)) {
                queue.add(new int[]{ni, nj});
                visited[ni][nj] = true;
            }
        }

        // Animated BFS: one "wave" per timer tick
        javax.swing.Timer flowTimer = new javax.swing.Timer(180, null);
        flowTimer.addActionListener(e -> {
            if (queue.isEmpty()) {
                flowTimer.stop();
                if (onNoPath != null) onNoPath.run();
                return;
            }

            int levelSize = queue.size();
            boolean reachedFire = false;

            for (int l = 0; l < levelSize; l++) {
                int[] current = queue.poll();
                int ci = current[0];
                int cj = current[1];
                Cev currentPipe = mreza[ci][cj].getCev();
                if (currentPipe == null) continue;

                currentPipe.setHasWater(true);
                mreza[ci][cj].repaint();

                Set<Direction> connections = currentPipe.getConnections();
                for (Direction dir : connections) {
                    int ni = ci, nj = cj;
                    switch (dir) {
                        case UP:    ni--; break;
                        case DOWN:  ni++; break;
                        case LEFT:  nj--; break;
                        case RIGHT: nj++; break;
                    }

                    if (ni < 0 || ni >= rows || nj < 0 || nj >= cols) continue;
                    if (visited[ni][nj]) continue;

                    Direction needed = dir.opposite();

                    // Special: water reached fire cell from LEFT
                    if (mreza[ni][nj] instanceof FireCell && needed == Direction.LEFT) {
                        visited[ni][nj] = true;
                        reachedFire = true;
                        continue;
                    }

                    // Skip water source
                    if (mreza[ni][nj] instanceof WaterSource) continue;

                    Cev neighborPipe = mreza[ni][nj].getCev();
                    if (neighborPipe != null && neighborPipe.getConnections().contains(needed)) {
                        visited[ni][nj] = true;
                        queue.add(new int[]{ni, nj});
                    }
                }
            }

            if (reachedFire) {
                flowTimer.stop();
                fireCell.extinguish();
                if (app != null) app.gameWon();
                return;
            }

            if (queue.isEmpty()) {
                flowTimer.stop();
                if (onNoPath != null) onNoPath.run();
            }
        });

        flowTimer.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                cols * (Kvadrat.CELL_SIZE + 1) + 5,
                rows * (Kvadrat.CELL_SIZE + 1) + 5);
    }
}
