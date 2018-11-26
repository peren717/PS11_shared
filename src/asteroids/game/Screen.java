package asteroids.game;

import static asteroids.game.Constants.*;
import java.awt.*;
import java.util.Iterator;
import javax.swing.*;
import asteroids.participants.Ship;

/**
 * The area of the display in which the game takes place.
 */
@SuppressWarnings("serial")
public class Screen extends JPanel
{
    /** Legend that is displayed across the screen */
    private String legend;

    /** Game controller */
    private Controller controller;

    /** Level currently being played **/
    private int level;

    /** lives currently being played **/
    private int lives;

    /** A shape used to draw lives */
    private Ship liveShape;

    /**
     * Creates an empty screen
     */
    public Screen (Controller controller)
    {
        this.controller = controller;
        legend = "";
        setPreferredSize(new Dimension(SIZE, SIZE));
        setMinimumSize(new Dimension(SIZE, SIZE));
        setBackground(Color.black);
        setForeground(Color.white);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 120));
        setFocusable(true);
        liveShape = new Ship(0, 0, 1.5 * Math.PI, controller);
    }

    /**
     * Set the legend
     */
    public void setLegend (String legend)
    {
        this.legend = legend;
    }

    /**
     * Set current level
     */
    public void setLevel (int level)
    {
        this.level = level;
    }

    /** Set current lives */
    public void setLives (int lives)
    {
        this.lives = lives;
    }

    /**
     * Paint the participants onto this panel
     */
    @Override
    public void paintComponent (Graphics graphics)
    {
        // Use better resolution
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Do the default painting
        super.paintComponent(g);

        // Draw each participant in its proper place
        Iterator<Participant> iter = controller.getParticipants();
        while (iter.hasNext())
        {
            iter.next().draw(g);
        }

        // Draw the legend across the middle of the panel
        int size = g.getFontMetrics().stringWidth(legend);
        g.drawString(legend, (SIZE - size) / 2, SIZE / 2);

        // Draw the level
        if (level > 0)
        {
            g.setFont(new Font("SansSerif", 0, 50));
            g.drawString("Level:" + level, SIZE - 180, 50);
        }

        // Draw the score
        if (level > 0)
        {
            g.drawString("Lives:", 1, 50);
            for (int i = 0; i < lives; i++)
            {
                this.liveShape.setPosition(150 + i * 26, 35);
                this.liveShape.move();
                this.liveShape.draw(g);
            }
        }

    }
}
