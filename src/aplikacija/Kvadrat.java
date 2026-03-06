package aplikacija;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Abstract base cell for the game grid.
 * Professional dark-themed rendering with hover and selection effects.
 */
public abstract class Kvadrat extends JPanel {

    protected boolean oznacen;
    protected Kanalizacija kanalizacija;
    protected Cev cev;
    protected boolean hovered = false;

    public static final int CELL_SIZE = 85;

    private static final Color BG_COLOR = new Color(22, 27, 34);
    private static final Color BG_HOVER = new Color(30, 38, 55);
    private static final Color SELECTED_COLOR = new Color(88, 166, 255);

    public Kvadrat() {
        setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
        setOpaque(true);
        setBackground(BG_COLOR);
        this.oznacen = false;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (kanalizacija != null) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        kanalizacija.removeCev(Kvadrat.this);
                    } else {
                        kanalizacija.cellClicked(Kvadrat.this);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public void postaviCev(Cev cev) {
        this.cev = cev;
        repaint();
    }

    public Cev getCev() {
        return cev;
    }

    public void setKanalizacija(Kanalizacija k) {
        this.kanalizacija = k;
    }

    public abstract boolean mozeOznaci();

    public void postaviOznaku(boolean bool) throws GOznaka {
        if (!mozeOznaci()) throw new GOznaka();
        this.oznacen = bool;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // Background
        if (hovered && mozeOznaci()) {
            g2.setColor(BG_HOVER);
        } else {
            g2.setColor(BG_COLOR);
        }
        g2.fillRect(0, 0, w, h);

        // Subtle inner shadow (top-left edges)
        g2.setColor(new Color(0, 0, 0, 25));
        g2.fillRect(0, 0, w, 2);
        g2.fillRect(0, 0, 2, h);

        // Water glow underneath pipe
        if (cev != null && cev.hasWater()) {
            g2.setColor(new Color(40, 120, 220, 30));
            g2.fillRect(0, 0, w, h);
        }

        // Draw pipe if present
        if (cev != null) {
            cev.draw(g2, w, h);
        }

        // Selection glow
        if (oznacen) {
            g2.setColor(new Color(88, 166, 255, 35));
            g2.fillRect(2, 2, w - 4, h - 4);
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(SELECTED_COLOR);
            g2.drawRect(1, 1, w - 3, h - 3);
            // Outer glow
            g2.setColor(new Color(88, 166, 255, 15));
            g2.setStroke(new BasicStroke(5f));
            g2.drawRect(0, 0, w - 1, h - 1);
        }

        g2.dispose();
    }
}
