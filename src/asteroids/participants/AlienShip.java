package asteroids.participants;

import static asteroids.game.Constants.RANDOM;
import static asteroids.game.Constants.SHIP_ACCELERATION;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Random;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class AlienShip extends Participant implements ShipDestroyer, AsteroidDestroyer
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
    public AlienShip (int x, int y, double direction, Controller controller, int level)
    {
        this.size = level - 1;
        this.controller = controller;
        setPosition(x, y);
        setRotation(direction);

        Path2D.Double poly = new Path2D.Double();
        if (size == 1)
        {
            poly.moveTo(20.0, 0.0);
            poly.lineTo(9.0, 9.0);
            poly.lineTo(-9.0, 9.0);
            poly.lineTo(-20.0, 0.0);
            poly.lineTo(20.0, 0.0);
            poly.lineTo(-20.0, 0.0);
            poly.lineTo(-9.0, -9.0);
            poly.lineTo(9.0, -9.0);
            poly.lineTo(-9.0, -9.0);
            poly.lineTo(-5.0, -17.0);
            poly.lineTo(5.0, -17.0);
            poly.lineTo(9.0, -9.0);
        }
        else
        {
            poly.moveTo(20.0 / 2, 0.0 / 2);
            poly.lineTo(9.0 / 2, 9.0 / 2);
            poly.lineTo(-9.0 / 2, 9.0 / 2);
            poly.lineTo(-20.0 / 2, 0.0 / 2);
            poly.lineTo(20.0 / 2, 0.0 / 2);
            poly.lineTo(-20.0 / 2, 0.0 / 2);
            poly.lineTo(-9.0 / 2, -9.0 / 2);
            poly.lineTo(9.0 / 2, -9.0 / 2);
            poly.lineTo(-9.0 / 2, -9.0 / 2);
            poly.lineTo(-5.0 / 2, -17.0 / 2);
            poly.lineTo(5.0 / 2, -17.0 / 2);
            poly.lineTo(9.0 / 2, -9.0 / 2);
        }
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
        int num;
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
                num = RANDOM.nextInt(1);
                if (num == 0)
                {
                    this.setDirection(180/Math.PI);
                    new ParticipantCountdownTimer(this, "changeDirection", 3000);
                }
                else if (num == 1)
                {
                    this.setDirection(-(180/Math.PI));
                    new ParticipantCountdownTimer(this, "changeDirection", 3000);
                }
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
        controller.addParticipant(new AlienBullets(this.getX(), this.getY(), getPlayerDirection()));
        controller.playSound("/sounds/fire.wav");
    }

    public double getPlayerDirection ()
    {
        Random rng = new Random();
        double direction = rng.nextInt(6);
        if (size == 1)
        {
            return direction;
        }
        else
        {
            Ship ship = this.controller.getShip();
            if (ship != null)
            {
                double dX = ship.getX() - this.getX();
                double dY = ship.getY() - this.getY();
                direction = Math.acos(dX / Math.sqrt(dX * dX + dY * dY) + (rng.nextInt(2) - 2) / 75);
            }
            return direction;
        }
    }

    /**
     * When a Ship collides with a ShipDestroyer, it expires
     */
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof Bullets || p instanceof Asteroid || p instanceof Ship)
        {
            // Expire the asteroid
            Participant.expire(this);

            this.controller.AlienShipDestroyed();
            controller.addParticipant(new AsteroidDebris(this.getX(), this.getY(), 2 * Math.PI * RANDOM.nextDouble()));
            controller.addParticipant(new AsteroidDebris(this.getX(), this.getY(), 2 * Math.PI * RANDOM.nextDouble()));
            if (size == 1)
            {
                this.controller.scoreControl(200);
            }
            else
            {
                this.controller.scoreControl(1000);
            }
        }

    }

    /**
     * This method is invoked when a ParticipantCountdownTimer completes its countdown.
     */
    @Override
    public void countdownComplete (Object payload)
    {
        if (payload.equals("fire"))
        {
            fire();
            new ParticipantCountdownTimer(this, "fire", 2000);
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
}
