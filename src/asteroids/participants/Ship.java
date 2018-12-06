package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Shape;
import java.awt.geom.*;
import asteroids.destroyers.*;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

/**
 * Represents ships
 */
public class Ship extends Participant implements AsteroidDestroyer
{
    /** The outline of the ship */
    private Shape outline;
    /** The outline of the ship with fire */
    private Shape forwardOutline;

    /** Game controller */
    private Controller controller;
    /** True if the ship is accelerating */
    private boolean forward;

    /**
     * Constructs a ship at the specified coordinates that is pointed in the given direction.
     */
    public Ship (int x, int y, double direction, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setRotation(direction);

        Path2D.Double poly = new Path2D.Double();
        poly.moveTo(21, 0);
        poly.lineTo(-21, 12);
        poly.lineTo(-14, 10);
        poly.lineTo(-14, -10);
        poly.lineTo(-21, -12);
        poly.closePath();
        outline = poly;

        poly = new Path2D.Double();
        poly.moveTo(21, 0);
        poly.lineTo(-21, 12);
        poly.lineTo(-14, 10);
        poly.lineTo(-14, -5);
        poly.lineTo(-25, 0.);
        poly.lineTo(-14, 5);
        poly.lineTo(-14, -10);
        poly.lineTo(-21, -12);
        poly.closePath();
        forwardOutline = poly;

        // play the sound in loop every 1 sec
        new ParticipantCountdownTimer(this, "beat", 1000);

    }

    /**
     * Returns the x-coordinate of the point on the screen where the ship's nose is located.
     */
    public double getXNose ()
    {
        Point2D.Double point = new Point2D.Double(20, 0);
        transformPoint(point);
        return point.getX();
    }

    /**
     * Returns the x-coordinate of the point on the screen where the ship's nose is located.
     */
    public double getYNose ()
    {
        Point2D.Double point = new Point2D.Double(20, 0);
        transformPoint(point);
        return point.getY();
    }

    @Override
    protected Shape getOutline ()
    {
        if (forward)
        {
            return forwardOutline;
        }
        return outline;
    }

    /**
     * Customizes the base move method by imposing friction
     */
    @Override
    public void move ()
    {
        applyFriction(SHIP_FRICTION);
        super.move();
    }

    /**
     * Turns right by Pi/16 radians
     */
    public void turnRight ()
    {
        rotate(Math.PI / 16);
    }

    /**
     * Turns left by Pi/16 radians
     */
    public void turnLeft ()
    {
        rotate(-Math.PI / 16);
    }

    /**
     * Accelerates by SHIP_ACCELERATION
     */
    public void accelerate ()
    {
        accelerate(SHIP_ACCELERATION);
        controller.playSound("/sounds/thrust.wav");
        forward = true;
    }

    /**
     * Decelerate by SHIP_ACCELERATION
     */
    public void stop ()
    {
        forward = false;
    }

    /**
     * fire bullets
     */
    public void fire ()
    {
        if (!controller.hasMaxBullets())
        {
            controller.addParticipant(new Bullets(this.getXNose(), this.getYNose(), this.getRotation()));
            controller.playSound("/sounds/fire.wav");
        }
    }

    /**
     * When a Ship collides with a ShipDestroyer, it expires
     */
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof ShipDestroyer && !this.isInvulnerable)
        {
            // Expire the ship from the game
            Participant.expire(this);

            // plays explosions sound
            controller.playSound("/sounds/bangShip.wav");

            // Tell the controller the ship was destroyed
            controller.shipDestroyed();
        }
    }

    /**
     * Gives the ship benefits based on the supply loot
     */
    public void getSupply (int variety)
    {
        if (variety == 1)
        {
            this.setInvulnerability();
            new ParticipantCountdownTimer(this, "reset", 3500);
            controller.playSound("/sounds/alarm_beep.wav");
        }
        else if (variety == 2)
        {
            controller.addLives(1);
            controller.playSound("/sounds/coin_flip.wav");
        }

    }

    /**
     * This method is invoked when a ParticipantCountdownTimer completes its countdown.
     */
    @Override
    public void countdownComplete (Object payload)
    {
        // Give a burst of acceleration, then schedule another
        // burst for 200 msecs from now.
        if (payload.equals("move"))
        {
            accelerate();
            new ParticipantCountdownTimer(this, "move", 200);
        }
        else if (payload.equals("beat"))
        {
            // play the sound in loop every 1 sec
            controller.playSound("/sounds/beat1.wav");
            new ParticipantCountdownTimer(this, "beat", 1000);
        }
        else if (payload.equals("reset"))
        {
            this.setInvulnerability();
        }
    }
}
