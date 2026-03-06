package aplikacija;

import java.awt.*;

/**
 * Empty cell on the grid. Shows a subtle pattern to indicate it's placeable.
 */
public class Zid extends Kvadrat {

    public Zid() {
        super();
    }

    @Override
    public boolean mozeOznaci() {
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // If no pipe, draw subtle diagonal hatch to show it's an empty/placeable cell
        if (cev == null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(48, 54, 61, 30));
            g2.setStroke(new BasicStroke(1f));
            int step = 14;
            for (int i = -getHeight(); i < getWidth() + getHeight(); i += step) {
                g2.drawLine(i, getHeight(), i + getHeight(), 0);
            }
            // Center dot to mark placeable
            g2.setColor(new Color(60, 68, 78, 60));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.fillOval(cx - 2, cy - 2, 4, 4);
            g2.dispose();
        }
    }
}
