package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Random;
import asteroids.game.Constants;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class Star extends Participant
{
    private Shape outline;

    /*
     * Creates new piece of debris at x and y moving in direction that disappears at the end of the timer.
     */
    public Star ()
    {
        Random rng = new Random();
        int x = rng.nextInt(Constants.SIZE);
        int y = rng.nextInt(Constants.SIZE);
        this.setPosition(x, y);
        this.setVelocity(3.0, 0.5 * Math.PI);
        this.outline = new Ellipse2D.Double(0.0, 0.0, 1.0, 1.0);
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    /*
     * At the end of the timer, remove the debris.
     */
    @Override
    public void countdownComplete (Object debris)
    {
        Participant.expire(this);
    }

    @Override
    public void collidedWith (Participant p)
    {
        return;
    }

}
