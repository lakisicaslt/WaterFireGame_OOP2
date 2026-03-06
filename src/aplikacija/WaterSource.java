package aplikacija;

import java.awt.*;
import java.awt.geom.*;

/**
 * Water source cell with animated ripple effects and water drop icon.
 * Placed randomly on the left edge of the grid.
 */
public class WaterSource extends Kvadrat {

    private int animFrame = 0;

    public WaterSource() {
        super();
    }

    public void tick() {
        animFrame++;
        repaint();
    }

    @Override
    public boolean mozeOznaci() {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // Deep water background gradient
        GradientPaint bg = new GradientPaint(0, 0, new Color(8, 28, 58), w, h, new Color(4, 16, 42));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // Animated water ripples
        double phase = animFrame * 0.12;
        for (int ring = 3; ring >= 0; ring--) {
            double offset = (phase + ring * 0.7) % 3.0;
            int radius = (int) (8 + offset * 11);
            int alpha = (int) (130 - offset * 42);
            if (alpha < 0) alpha = 0;
            g2.setColor(new Color(88, 166, 255, alpha));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(w / 2 - radius, h / 2 - radius, radius * 2, radius * 2);
        }

        // Water drop icon
        int dx = w / 2;
        int dy = h / 2 - 2;
        int dropW = 18;
        int dropH = 24;
        Path2D drop = new Path2D.Double();
        drop.moveTo(dx, dy - dropH / 2);
        drop.curveTo(dx + dropW / 2.0, dy - dropH / 6.0,
                     dx + dropW / 2.0, dy + dropH / 3.0,
                     dx, dy + dropH / 2);
        drop.curveTo(dx - dropW / 2.0, dy + dropH / 3.0,
                     dx - dropW / 2.0, dy - dropH / 6.0,
                     dx, dy - dropH / 2);
        drop.closePath();

        GradientPaint dropGrad = new GradientPaint(
                dx - dropW / 2, dy, new Color(31, 111, 235),
                dx + dropW / 2, dy, new Color(100, 180, 255));
        g2.setPaint(dropGrad);
        g2.fill(drop);

        // Drop highlight
        g2.setColor(new Color(200, 235, 255, 180));
        g2.fillOval(dx - 4, dy - 5, 5, 7);

        // Exit pipe on RIGHT edge
        int pipeThick = (int) (Math.min(w, h) * 0.38);
        int halfPipe = pipeThick / 2;
        int pipeStartX = w - w / 3;

        GradientPaint pipePaint = new GradientPaint(
                0, h / 2 - halfPipe, new Color(31, 111, 235),
                0, h / 2 + halfPipe, new Color(5, 80, 174));
        g2.setPaint(pipePaint);
        g2.fillRect(pipeStartX, h / 2 - halfPipe, w - pipeStartX, pipeThick);

        // Inner pipe channel
        g2.setColor(new Color(60, 160, 255, 220));
        g2.fillRect(pipeStartX, h / 2 - halfPipe + 4, w - pipeStartX, pipeThick - 8);

        // Pipe edge lines
        g2.setColor(new Color(13, 65, 157));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(pipeStartX, h / 2 - halfPipe, w, h / 2 - halfPipe);
        g2.drawLine(pipeStartX, h / 2 + halfPipe, w, h / 2 + halfPipe);

        // Highlight
        g2.setColor(new Color(121, 192, 255, 80));
        g2.fillRect(pipeStartX, h / 2 - halfPipe + 1, w - pipeStartX, 2);

        // Border glow
        g2.setColor(new Color(31, 111, 235, 120));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(1, 1, w - 3, h - 3);

        // Label
        g2.setColor(new Color(180, 220, 255, 200));
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fm = g2.getFontMetrics();
        String label = "WATER";
        g2.drawString(label, (w - fm.stringWidth(label)) / 2, h - 5);

        g2.dispose();
    }
}
