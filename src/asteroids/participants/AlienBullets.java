package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class AlienBullets extends Participant implements ShipDestroyer, AsteroidDestroyer
{

    private Shape outline;
    
    public AlienBullets (double x, double y, double direction)
    {
        this.setPosition(x, y);
        this.setVelocity(14.0, direction);
        this.outline = new Ellipse2D.Double(0.0, 0.0, 1.0, 1.0);
        new ParticipantCountdownTimer(this, this, 2000);
    }
    
    @Override
    protected Shape getOutline ()
    {
        return this.outline;
    }
    
    /*
     * At the end of the timer, remove the bullet.
     */
    @Override
    public void countdownComplete (Object bullet)
    {
        Participant.expire(this);
    }

    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof Ship)
        {
            Participant.expire(this);
        }
        
        if (p instanceof Asteroid)
        {
            Participant.expire(this);
        }
    }

}
