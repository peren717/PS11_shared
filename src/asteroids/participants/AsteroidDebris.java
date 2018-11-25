package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class AsteroidDebris extends Participant
{
    private Shape outline;

    /*
     * Creates new piece of debris at x and y moving in direction that disappears at the end of the timer.
     */
    public AsteroidDebris (double x, double y, double direction)
    {
            this.setPosition(x, y);
            this.setVelocity(1.0, direction);
            this.outline = new Ellipse2D.Double(0.0, 0.0, 1.0, 1.0);
            new ParticipantCountdownTimer(this, this, 1000);

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
