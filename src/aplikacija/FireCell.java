package aplikacija;

import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Fire cell with animated flame effects.
 * Placed randomly on the right edge of the grid.
 * When water reaches it, the fire is extinguished (game won).
 */
public class FireCell extends Kvadrat {

    private int animFrame = 0;
    private boolean extinguished = false;
    private double[] flameSeeds;

    public FireCell() {
        super();
        Random rand = new Random();
        flameSeeds = new double[8];
        for (int i = 0; i < flameSeeds.length; i++) {
            flameSeeds[i] = rand.nextDouble();
        }
    }

    public void tick() {
        animFrame++;
        repaint();
    }

    public boolean isExtinguished() {
        return extinguished;
    }

    public void extinguish() {
        extinguished = true;
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

        if (extinguished) {
            drawExtinguished(g2, w, h);
        } else {
            drawFire(g2, w, h);
        }

        // Entry pipe on LEFT edge
        int pipeThick = (int) (Math.min(w, h) * 0.38);
        int halfPipe = pipeThick / 2;
        int pipeEndX = w / 3;

        if (extinguished) {
            GradientPaint pipePaint = new GradientPaint(
                    0, h / 2 - halfPipe, new Color(31, 111, 235),
                    0, h / 2 + halfPipe, new Color(5, 80, 174));
            g2.setPaint(pipePaint);
            g2.fillRect(0, h / 2 - halfPipe, pipeEndX, pipeThick);
            g2.setColor(new Color(60, 160, 255, 220));
            g2.fillRect(0, h / 2 - halfPipe + 4, pipeEndX, pipeThick - 8);
        } else {
            GradientPaint pipePaint = new GradientPaint(
                    0, h / 2 - halfPipe, new Color(200, 80, 50),
                    0, h / 2 + halfPipe, new Color(140, 45, 25));
            g2.setPaint(pipePaint);
            g2.fillRect(0, h / 2 - halfPipe, pipeEndX, pipeThick);
            g2.setColor(new Color(255, 120, 60, 180));
            g2.fillRect(0, h / 2 - halfPipe + 4, pipeEndX, pipeThick - 8);
        }

        // Pipe edge lines
        Color edgeC = extinguished ? new Color(13, 65, 157) : new Color(100, 30, 15);
        g2.setColor(edgeC);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, h / 2 - halfPipe, pipeEndX, h / 2 - halfPipe);
        g2.drawLine(0, h / 2 + halfPipe, pipeEndX, h / 2 + halfPipe);

        // Border
        Color borderC = extinguished ? new Color(63, 185, 80, 150) : new Color(248, 81, 73, 120);
        g2.setColor(borderC);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(1, 1, w - 3, h - 3);

        // Label
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fm = g2.getFontMetrics();
        if (extinguished) {
            g2.setColor(new Color(63, 185, 80));
            String label = "OUT!";
            g2.drawString(label, (w - fm.stringWidth(label)) / 2, h - 5);
        } else {
            g2.setColor(new Color(255, 210, 170));
            String label = "FIRE";
            g2.drawString(label, (w - fm.stringWidth(label)) / 2, h - 5);
        }

        g2.dispose();
    }

    private void drawFire(Graphics2D g2, int w, int h) {
        // Dark hot background
        GradientPaint bg = new GradientPaint(0, 0, new Color(55, 14, 5), w, h, new Color(35, 8, 2));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // Radial glow from base
        RadialGradientPaint glow = new RadialGradientPaint(
                w / 2f, h * 0.65f, w * 0.55f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(255, 140, 0, 70), new Color(255, 50, 0, 35), new Color(0, 0, 0, 0)});
        g2.setPaint(glow);
        g2.fillRect(0, 0, w, h);

        double t = animFrame * 0.15;
        int numFlames = 6;

        for (int i = 0; i < numFlames; i++) {
            double fx = w * 0.15 + (w * 0.7) * i / (numFlames - 1.0);
            double baseY = h * 0.68;
            double seed = flameSeeds[i % flameSeeds.length];

            // Flame height with flickering
            double flameH = h * 0.30 + h * 0.18 * seed + h * 0.06 * Math.sin(t * 1.8 + i * 1.3);
            double flameW = w * 0.10 + w * 0.05 * Math.sin(t * 2.3 + i);

            // Outer flame (orange-red)
            int green = (int) (70 + 70 * Math.sin(t * 1.5 + i * 0.9));
            g2.setColor(new Color(255, green, 15, 175));
            int[] xp = {(int) (fx - flameW), (int) fx, (int) (fx + flameW)};
            int[] yp = {(int) baseY, (int) (baseY - flameH), (int) baseY};
            g2.fillPolygon(xp, yp, 3);
            // Round base
            g2.fillOval((int) (fx - flameW * 0.8), (int) (baseY - flameW), (int) (flameW * 1.6), (int) (flameW * 1.2));

            // Inner flame (bright yellow)
            double innerH = flameH * 0.55;
            double innerW = flameW * 0.5;
            g2.setColor(new Color(255, 240, 60, 170));
            int[] xp2 = {(int) (fx - innerW), (int) fx, (int) (fx + innerW)};
            int[] yp2 = {(int) baseY, (int) (baseY - innerH), (int) baseY};
            g2.fillPolygon(xp2, yp2, 3);
        }

        // Ember base
        GradientPaint embers = new GradientPaint(
                0, (int) (h * 0.63), new Color(210, 60, 0, 200),
                0, (int) (h * 0.78), new Color(90, 20, 0, 150));
        g2.setPaint(embers);
        g2.fillRoundRect(w / 8, (int) (h * 0.63), w * 3 / 4, (int) (h * 0.15), 6, 6);

        // Hot spots on embers
        g2.setColor(new Color(255, 160, 30, 120));
        g2.fillOval(w / 3, (int) (h * 0.64), w / 5, (int) (h * 0.08));

        // Floating sparks
        g2.setColor(new Color(255, 200, 60, 140));
        for (int i = 0; i < 5; i++) {
            double sparkPhase = (t * 0.4 + i * 0.2 + flameSeeds[i % flameSeeds.length]) % 1.0;
            double sparkX = w * 0.15 + w * 0.7 * flameSeeds[(i + 2) % flameSeeds.length];
            double sparkY = h * 0.6 - h * 0.55 * sparkPhase;
            int sparkAlpha = (int) (140 * (1.0 - sparkPhase));
            if (sparkAlpha < 0) sparkAlpha = 0;
            g2.setColor(new Color(255, 200, 60, sparkAlpha));
            g2.fillOval((int) sparkX, (int) sparkY, 3, 3);
        }
    }

    private void drawExtinguished(Graphics2D g2, int w, int h) {
        // Cool gray background
        GradientPaint bg = new GradientPaint(0, 0, new Color(38, 42, 48), w, h, new Color(28, 32, 38));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        double t = animFrame * 0.06;

        // Rising smoke wisps
        for (int i = 0; i < 4; i++) {
            double smokeX = w * 0.25 + w * 0.15 * i;
            double smokeOff = Math.sin(t + i * 1.8) * 6;
            double smokeY = h * 0.15 + h * 0.15 * Math.sin(t * 0.5 + i);
            int alpha = (int) (45 + 15 * Math.sin(t + i));
            g2.setColor(new Color(130, 130, 130, alpha));
            g2.fillOval((int) (smokeX + smokeOff) - 10, (int) smokeY - 10, 20 + i * 3, 20 + i * 3);
        }

        // Charred logs base
        g2.setColor(new Color(45, 38, 32));
        g2.fillRoundRect(w / 7, (int) (h * 0.60), w * 5 / 7, (int) (h * 0.14), 5, 5);
        g2.setColor(new Color(60, 50, 42));
        g2.fillRoundRect(w / 5, (int) (h * 0.62), w * 3 / 5, (int) (h * 0.06), 3, 3);

        // Victory checkmark
        g2.setColor(new Color(63, 185, 80));
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(w / 2 - 12, h / 2 + 2, w / 2 - 3, h / 2 + 10);
        g2.drawLine(w / 2 - 3, h / 2 + 10, w / 2 + 12, h / 2 - 8);

        // Subtle success glow
        RadialGradientPaint successGlow = new RadialGradientPaint(
                w / 2f, h / 2f, w * 0.4f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(63, 185, 80, 30), new Color(63, 185, 80, 10), new Color(0, 0, 0, 0)});
        g2.setPaint(successGlow);
        g2.fillRect(0, 0, w, h);
    }
}
