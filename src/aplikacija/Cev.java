package aplikacija;

import java.awt.*;
import java.util.Set;

public abstract class Cev {

    protected boolean hasWater = false;

    public abstract Set<Direction> getConnections();

    public void setHasWater(boolean water) {
        this.hasWater = water;
    }

    public boolean hasWater() {
        return hasWater;
    }

    /**
     * Draw this pipe inside a cell of the given width/height.
     * Uses professional metallic gradients with 3D effects.
     */
    public void draw(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Set<Direction> conn = getConnections();
        int cx = w / 2;
        int cy = h / 2;
        int pipeThick = (int) (Math.min(w, h) * 0.40);
        int halfPipe = pipeThick / 2;

        // Draw shadow layer first
        g2.setColor(new Color(0, 0, 0, 35));
        for (Direction d : conn) {
            switch (d) {
                case LEFT:
                    g2.fillRect(2, cy - halfPipe + 2, cx + halfPipe, pipeThick);
                    break;
                case RIGHT:
                    g2.fillRect(cx - halfPipe + 2, cy - halfPipe + 2, w - cx + halfPipe, pipeThick);
                    break;
                case UP:
                    g2.fillRect(cx - halfPipe + 2, 2, pipeThick, cy + halfPipe);
                    break;
                case DOWN:
                    g2.fillRect(cx - halfPipe + 2, cy - halfPipe + 2, pipeThick, h - cy + halfPipe);
                    break;
            }
        }

        // Draw each connection segment
        for (Direction d : conn) {
            switch (d) {
                case LEFT:
                    drawSegment(g2, 0, cy - halfPipe, cx + halfPipe, pipeThick, true);
                    break;
                case RIGHT:
                    drawSegment(g2, cx - halfPipe, cy - halfPipe, w - cx + halfPipe, pipeThick, true);
                    break;
                case UP:
                    drawSegment(g2, cx - halfPipe, 0, pipeThick, cy + halfPipe, false);
                    break;
                case DOWN:
                    drawSegment(g2, cx - halfPipe, cy - halfPipe, pipeThick, h - cy + halfPipe, false);
                    break;
            }
        }

        // Center junction
        drawCenter(g2, cx - halfPipe, cy - halfPipe, pipeThick, pipeThick);

        // Decorative bolts at the junction
        drawBolts(g2, cx, cy, halfPipe);

        // Water flow indicator (animated shimmer)
        if (hasWater) {
            drawWaterShimmer(g2, conn, cx, cy, halfPipe, w, h, pipeThick);
        }
    }

    private void drawSegment(Graphics2D g2, int sx, int sy, int sw, int sh, boolean horizontal) {
        int border = 4;

        // Main body gradient (metallic or water)
        GradientPaint gp;
        if (horizontal) {
            gp = new GradientPaint(sx, sy, getColor1(), sx, sy + sh, getColor2());
        } else {
            gp = new GradientPaint(sx, sy, getColor1(), sx + sw, sy, getColor2());
        }
        g2.setPaint(gp);
        g2.fillRect(sx, sy, sw, sh);

        // Inner channel
        g2.setColor(getInnerColor());
        if (horizontal) {
            g2.fillRect(sx, sy + border, sw, sh - 2 * border);
        } else {
            g2.fillRect(sx + border, sy, sw - 2 * border, sh);
        }

        // Edge lines
        g2.setColor(getEdgeColor());
        g2.setStroke(new BasicStroke(1f));
        if (horizontal) {
            g2.drawLine(sx, sy, sx + sw - 1, sy);
            g2.drawLine(sx, sy + sh - 1, sx + sw - 1, sy + sh - 1);
        } else {
            g2.drawLine(sx, sy, sx, sy + sh - 1);
            g2.drawLine(sx + sw - 1, sy, sx + sw - 1, sy + sh - 1);
        }

        // Top/left highlight (3D effect)
        g2.setColor(getHighlight());
        if (horizontal) {
            g2.fillRect(sx, sy + 1, sw, 2);
        } else {
            g2.fillRect(sx + 1, sy, 2, sh);
        }
    }

    private void drawCenter(Graphics2D g2, int x, int y, int w, int h) {
        int border = 4;
        GradientPaint gp = new GradientPaint(x, y, getColor1(), x + w, y + h, getColor2());
        g2.setPaint(gp);
        g2.fillRect(x, y, w, h);
        g2.setColor(getInnerColor());
        g2.fillRect(x + border, y + border, w - 2 * border, h - 2 * border);
    }

    private void drawBolts(Graphics2D g2, int cx, int cy, int halfPipe) {
        int boltSize = 5;
        Color boltColor = hasWater ? new Color(40, 100, 160) : new Color(90, 100, 115);
        Color boltHighlight = hasWater ? new Color(100, 180, 255, 120) : new Color(140, 150, 165, 100);

        int[][] positions = {
            {cx - halfPipe + 3, cy - halfPipe + 3},
            {cx + halfPipe - boltSize - 3, cy - halfPipe + 3},
            {cx - halfPipe + 3, cy + halfPipe - boltSize - 3},
            {cx + halfPipe - boltSize - 3, cy + halfPipe - boltSize - 3}
        };

        for (int[] pos : positions) {
            g2.setColor(boltColor);
            g2.fillOval(pos[0], pos[1], boltSize, boltSize);
            g2.setColor(boltHighlight);
            g2.fillOval(pos[0], pos[1], boltSize / 2 + 1, boltSize / 2 + 1);
        }
    }

    private void drawWaterShimmer(Graphics2D g2, Set<Direction> conn,
                                   int cx, int cy, int halfPipe, int w, int h, int pipeThick) {
        // Subtle animated-looking highlights on the inner channel
        int border = 6;
        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.setColor(new Color(150, 220, 255));

        for (Direction d : conn) {
            switch (d) {
                case LEFT:
                    g2.fillRect(4, cy - halfPipe + border, cx + halfPipe - 8, 3);
                    break;
                case RIGHT:
                    g2.fillRect(cx - halfPipe + 4, cy - halfPipe + border, w - cx + halfPipe - 8, 3);
                    break;
                case UP:
                    g2.fillRect(cx - halfPipe + border, 4, 3, cy + halfPipe - 8);
                    break;
                case DOWN:
                    g2.fillRect(cx - halfPipe + border, cy - halfPipe + 4, 3, h - cy + halfPipe - 8);
                    break;
            }
        }
        g2.setComposite(oldComp);
    }

    // ---- Color scheme methods ----

    protected Color getColor1() {
        return hasWater ? new Color(31, 111, 235) : new Color(110, 118, 129);
    }

    protected Color getColor2() {
        return hasWater ? new Color(5, 80, 174) : new Color(72, 79, 88);
    }

    protected Color getInnerColor() {
        return hasWater ? new Color(60, 160, 255, 230) : new Color(45, 51, 59);
    }

    protected Color getEdgeColor() {
        return hasWater ? new Color(13, 65, 157) : new Color(28, 33, 40);
    }

    protected Color getHighlight() {
        return hasWater ? new Color(121, 192, 255, 100) : new Color(139, 148, 158, 60);
    }
}
