
package asteroids.participants;

import asteroids.destroyers.AsteroidDestroyer;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Bullets extends Participant implements AsteroidDestroyer
{
    
    private Shape outline;
    
    /*
    /  Creates new bullet at x and y moving in direction that disappears at the end of the timer.
    */
    public Bullets(double x, double y, double direction) 
    {
        this.setPosition(x, y);
        this.setVelocity(14.0, direction);
        this.outline = new Ellipse2D.Double(0.0, 0.0, 1.0, 1.0);
        new ParticipantCountdownTimer(this, this, 1000);
    }

    @Override
    protected Shape getOutline() 
    {
        return this.outline;
    }

    /*
    / At the end of the timer, remove the bullet.
    */
    @Override
    public void countdownComplete(Object bullet) 
    {
        Participant.expire(this);
    }

    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof Asteroid)
        {
            Participant.expire(this);
        }        
    }
}

