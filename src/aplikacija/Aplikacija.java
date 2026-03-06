package aplikacija;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * PIPE RESCUE - Main game window.
 *
 * Professional dark-themed puzzle game where you connect pipes
 * from a water source to a fire to extinguish it.
 *
 * Features:
 *  - 7x10 grid with random water source (left) and fire (right)
 *  - 6 pipe types: Horizontal, Vertical, 4 L-bends
 *  - Animated water flow via BFS
 *  - Animated fire & water source
 *  - Timer and move counter
 *  - Right-click to remove pipes
 */
public class Aplikacija extends JFrame {

    private static final int GRID_ROWS = 7;
    private static final int GRID_COLS = 10;

    private Kanalizacija kanalizacija;
    private String selectedPipeType = null;
    private JLabel statusLabel;
    private JLabel movesLabel;
    private JLabel timerLabel;
    private int moves = 0;
    private int elapsedSeconds = 0;
    private javax.swing.Timer gameTimer;
    private boolean gameActive = true;

    private JToggleButton[] pipeButtons;
    private ButtonGroup pipeGroup;

    // ==== Color Palette (GitHub Dark) ====
    private static final Color BG_DARK   = new Color(13, 17, 23);
    private static final Color BG_SIDE   = new Color(22, 27, 34);
    private static final Color BORDER    = new Color(48, 54, 61);
    private static final Color TEXT_PRI  = new Color(201, 209, 217);
    private static final Color TEXT_SEC  = new Color(139, 148, 158);
    private static final Color ACCENT    = new Color(88, 166, 255);
    private static final Color BTN_BG    = new Color(33, 38, 45);
    private static final Color BTN_HOVER = new Color(48, 54, 61);
    private static final Color SUCCESS   = new Color(63, 185, 80);
    private static final Color DANGER    = new Color(248, 81, 73);

    public Aplikacija() {
        setTitle("PIPE RESCUE - Extinguish the Fire!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_DARK);

        buildUI();

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        startGameTimer();
    }

    // ================ UI CONSTRUCTION ================

    private void buildUI() {
        add(createHeader(), BorderLayout.NORTH);

        kanalizacija = new Kanalizacija(GRID_ROWS, GRID_COLS);
        kanalizacija.setApp(this);

        JPanel boardWrapper = new JPanel(new BorderLayout());
        boardWrapper.setBackground(BG_DARK);
        boardWrapper.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 4));
        boardWrapper.add(kanalizacija, BorderLayout.CENTER);
        add(boardWrapper, BorderLayout.CENTER);

        add(createSidebar(), BorderLayout.EAST);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SIDE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(14, 22, 14, 22)));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);

        JLabel fireIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Small flame icon
                int[] fx = {2, 10, 18};
                int[] fy = {20, 2, 20};
                g2.setColor(new Color(255, 120, 30));
                g2.fillPolygon(fx, fy, 3);
                g2.setColor(new Color(255, 220, 60));
                int[] fx2 = {6, 10, 14};
                int[] fy2 = {20, 8, 20};
                g2.fillPolygon(fx2, fy2, 3);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 22);
            }
        };
        titlePanel.add(fireIcon);

        JLabel title = new JLabel("PIPE RESCUE");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRI);
        titlePanel.add(title);

        JLabel subtitle = new JLabel("  Extinguish the fire!");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 13));
        subtitle.setForeground(TEXT_SEC);
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        // Stats
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        stats.setOpaque(false);

        statusLabel = new JLabel("Connect water to fire!");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusLabel.setForeground(ACCENT);

        timerLabel = new JLabel("Time: 0:00");
        timerLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        timerLabel.setForeground(TEXT_SEC);

        movesLabel = new JLabel("Moves: 0");
        movesLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        movesLabel.setForeground(TEXT_SEC);

        stats.add(statusLabel);
        stats.add(createSeparatorLabel());
        stats.add(timerLabel);
        stats.add(createSeparatorLabel());
        stats.add(movesLabel);

        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JLabel createSeparatorLabel() {
        JLabel sep = new JLabel("|");
        sep.setForeground(new Color(48, 54, 61));
        sep.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return sep;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDE);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(16, 14, 16, 14)));
        sidebar.setPreferredSize(new Dimension(190, 0));

        // Pipes section
        addSectionTitle(sidebar, "PIPES");
        sidebar.add(Box.createVerticalStrut(8));

        String[] pipeNames = {"Horizontal", "Vertical", "L-Bend LB", "L-Bend LT", "L-Bend TR", "L-Bend BR"};
        String[] pipeTypes = {"LevoDesno", "GoreDole", "LevoDole", "LevoGore", "GoreDesno", "DoleDesno"};

        pipeGroup = new ButtonGroup();
        pipeButtons = new JToggleButton[pipeTypes.length];

        for (int i = 0; i < pipeTypes.length; i++) {
            final String type = pipeTypes[i];
            final String name = pipeNames[i];
            JToggleButton btn = createPipeButton(name, type);
            pipeButtons[i] = btn;
            pipeGroup.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalStrut(14));
        addSeparator(sidebar);
        sidebar.add(Box.createVerticalStrut(14));

        // Actions section
        addSectionTitle(sidebar, "ACTIONS");
        sidebar.add(Box.createVerticalStrut(8));

        JButton flowBtn = createActionButton("Flow Water", new Color(31, 111, 235));
        flowBtn.addActionListener(e -> flowWater());
        sidebar.add(flowBtn);
        sidebar.add(Box.createVerticalStrut(6));

        JButton newGameBtn = createActionButton("New Game", TEXT_SEC);
        newGameBtn.addActionListener(e -> newGame());
        sidebar.add(newGameBtn);

        sidebar.add(Box.createVerticalStrut(16));
        addSeparator(sidebar);
        sidebar.add(Box.createVerticalStrut(12));

        // Instructions
        JTextArea instr = new JTextArea(
                "HOW TO PLAY:\n\n" +
                "1. Select a pipe type\n" +
                "2. Click a cell to place\n" +
                "3. Right-click to remove\n" +
                "4. Click 'Flow Water'\n" +
                "5. Reach the fire to win!");
        instr.setFont(new Font("SansSerif", Font.PLAIN, 11));
        instr.setForeground(new Color(90, 100, 115));
        instr.setBackground(BG_SIDE);
        instr.setEditable(false);
        instr.setFocusable(false);
        instr.setLineWrap(true);
        instr.setWrapStyleWord(true);
        instr.setAlignmentX(Component.LEFT_ALIGNMENT);
        instr.setMaximumSize(new Dimension(170, 200));
        sidebar.add(instr);

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private void addSectionTitle(JPanel parent, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(TEXT_SEC);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(label);
    }

    private void addSeparator(JPanel parent) {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BG_SIDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        parent.add(sep);
    }

    // ================ CUSTOM BUTTONS ================

    private JToggleButton createPipeButton(String name, String type) {
        JToggleButton btn = new JToggleButton(name) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background
                if (isSelected()) {
                    g2.setColor(new Color(88, 166, 255, 25));
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                    g2.setColor(ACCENT);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(BTN_HOVER);
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                } else {
                    g2.setColor(BTN_BG);
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                }

                // Pipe icon
                int iconSize = 22;
                int ix = 10;
                int iy = (h - iconSize) / 2;
                drawPipeIcon(g2, type, ix, iy, iconSize);

                // Label text
                g2.setColor(isSelected() ? ACCENT : TEXT_PRI);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString(name, ix + iconSize + 10, h / 2 + 4);

                g2.dispose();
            }
        };

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(162, 34));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> selectedPipeType = type);

        return btn;
    }

    /**
     * Draws a small pipe icon preview for sidebar buttons.
     */
    private void drawPipeIcon(Graphics2D g2, String type, int x, int y, int size) {
        int cx = x + size / 2;
        int cy = y + size / 2;
        int pw = size / 3;
        int hp = pw / 2;

        // Outer pipe
        g2.setColor(new Color(100, 108, 120));
        drawPipeShape(g2, type, x, y, cx, cy, size, pw, hp, false);

        // Inner channel
        g2.setColor(new Color(55, 62, 72));
        drawPipeShape(g2, type, x, y, cx, cy, size, pw - 3, hp - 1, true);

        // Icon border
        g2.setColor(new Color(60, 66, 76));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x - 1, y - 1, size + 1, size + 1, 3, 3);
    }

    private void drawPipeShape(Graphics2D g2, String type, int x, int y,
                                int cx, int cy, int size, int pw, int hp, boolean inner) {
        int off = inner ? 1 : 0;
        switch (type) {
            case "LevoDesno":
                g2.fillRect(x, cy - hp, size, pw);
                break;
            case "GoreDole":
                g2.fillRect(cx - hp, y, pw, size);
                break;
            case "LevoDole":
                g2.fillRect(x, cy - hp, size / 2 + hp + off, pw);
                g2.fillRect(cx - hp, cy - hp, pw, size / 2 + hp + off);
                break;
            case "LevoGore":
                g2.fillRect(x, cy - hp, size / 2 + hp + off, pw);
                g2.fillRect(cx - hp, y, pw, size / 2 + hp + off);
                break;
            case "GoreDesno":
                g2.fillRect(cx - hp, y, pw, size / 2 + hp + off);
                g2.fillRect(cx - hp, cy - hp, size / 2 + hp + off, pw);
                break;
            case "DoleDesno":
                g2.fillRect(cx - hp, cy - hp, pw, size / 2 + hp + off);
                g2.fillRect(cx - hp, cy - hp, size / 2 + hp + off, pw);
                break;
        }
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background states
                if (getModel().isPressed()) {
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                } else {
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 15));
                }
                g2.fillRoundRect(0, 0, w, h, 8, 8);

                // Border
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);

                // Text
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (w - fm.stringWidth(text)) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, textX, textY);

                g2.dispose();
            }
        };

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(162, 38));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // ================ GAME LOGIC ================

    /**
     * Returns a new Cev instance based on the currently selected pipe type.
     */
    public Cev getSelectedPipe() {
        if (selectedPipeType == null || !gameActive) return null;
        switch (selectedPipeType) {
            case "LevoDesno": return new LevoDesno();
            case "GoreDole":  return new GoreDole();
            case "LevoDole":  return new LevoDole();
            case "LevoGore":  return new LevoGore();
            case "GoreDesno": return new GoreDesno();
            case "DoleDesno": return new DoleDesno();
            default: return null;
        }
    }

    public void incrementMoves() {
        moves++;
        movesLabel.setText("Moves: " + moves);
    }

    private void flowWater() {
        if (!gameActive) return;
        statusLabel.setText("Flowing water...");
        statusLabel.setForeground(ACCENT);

        kanalizacija.animateFlow(() -> {
            // Called if water does NOT reach fire
            statusLabel.setText("Water didn't reach fire! Try again.");
            statusLabel.setForeground(DANGER);
        });
    }

    /**
     * Called by Kanalizacija when water reaches fire.
     */
    public void gameWon() {
        gameActive = false;
        gameTimer.stop();

        statusLabel.setText("FIRE EXTINGUISHED! YOU WIN!");
        statusLabel.setForeground(SUCCESS);

        // Show victory dialog after a short delay (let animations finish)
        javax.swing.Timer winDelay = new javax.swing.Timer(1800, e -> {
            String msg = "Congratulations! You extinguished the fire!\n\n" +
                         "Time: " + formatTime(elapsedSeconds) + "\n" +
                         "Moves: " + moves + "\n\n" +
                         "Play again?";
            int result = JOptionPane.showConfirmDialog(this, msg, "Victory!",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                newGame();
            }
        });
        winDelay.setRepeats(false);
        winDelay.start();
    }

    private void newGame() {
        gameActive = true;
        moves = 0;
        elapsedSeconds = 0;
        selectedPipeType = null;
        pipeGroup.clearSelection();
        movesLabel.setText("Moves: 0");
        timerLabel.setText("Time: 0:00");
        statusLabel.setText("Connect water to fire!");
        statusLabel.setForeground(ACCENT);
        kanalizacija.newGame();
        if (!gameTimer.isRunning()) gameTimer.start();
    }

    private void startGameTimer() {
        gameTimer = new javax.swing.Timer(1000, e -> {
            if (gameActive) {
                elapsedSeconds++;
                timerLabel.setText("Time: " + formatTime(elapsedSeconds));
            }
        });
        gameTimer.start();
    }

    private String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    // ================ ENTRY POINT ================

    public static void main(String[] args) {
        // Set system look and feel hints for font rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> new Aplikacija());
    }
}
