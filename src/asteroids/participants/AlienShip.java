package asteroids.participants;

import static asteroids.game.Constants.SHIP_ACCELERATION;
import static asteroids.game.Constants.SHIP_FRICTION;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Random;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class AlienShip extends Participant implements ShipDestroyer
{
    /** The outline of the ship */
    private Shape outline;

    /** Game controller */
    private Controller controller;

    private boolean changeDirection = false;

    private int size;

    /**
     * Constructs a ship at the specified coordinates that is pointed in the given direction.
     */
    public AlienShip (int x, int y, double direction, Controller controller, int size)
    {
        this.size = size;
        this.controller = controller;
        setPosition(x, y);
        setRotation(direction);

        Path2D.Double poly = new Path2D.Double();
        poly.moveTo(20, 10);
        poly.lineTo(20, -10);
        poly.lineTo(10, -20);
        poly.lineTo(-10, -20);
        poly.lineTo(-20, -10);
        poly.lineTo(-20, 10);
        poly.lineTo(-10, 20);
        poly.lineTo(10, 20);
        poly.closePath();
        outline = poly;

        // Schedule an acceleration in two seconds, commented out
        new ParticipantCountdownTimer(this, "fire", 2000);
        new ParticipantCountdownTimer(this, "changeDirection", 3000);
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
        return outline;
    }

    /**
     * Customizes the base move method by imposing friction
     */
    @Override
    public void move ()
    {
        Random rng = new Random();
        super.move();
        if (changeDirection)
        {
            if (this.getDirection() != 0)
            {
                this.changeDirection = false;
                this.setDirection(0);
                new ParticipantCountdownTimer(this, "changeDirection", 3000);
            }
            else
            {
                this.changeDirection = false;
                this.setDirection(rng.nextInt(3)+1);
                new ParticipantCountdownTimer(this, "changeDirection", 3000);
            }
        }
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
        if (p instanceof AsteroidDestroyer)
        {
            // Expire the asteroid
            Participant.expire(this);
        }
    }

    /**
     * This method is invoked when a ParticipantCountdownTimer completes its countdown.
     */
    @Override
    public void countdownComplete (Object payload)
    {
        Ship AlienShip = this.controller.getShip();
        if (payload.equals("fire"))
        {

            if (AlienShip != null)
            {
                fire();
                new ParticipantCountdownTimer(this, "fire", 2000);
            }
        }
        else if (payload.equals("changeDirection"))
        {
            changeDirection = true;
        }
    }

    public int getSize ()
    {
        return size;
    }

    public void expire ()
    {
        Participant.expire(this);
    }
}
