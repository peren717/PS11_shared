package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.Random;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Controller;
import asteroids.game.Participant;

/**
 * Represents asteroids
 */
public class Asteroid extends Participant implements ShipDestroyer
{
    /** The size of the asteroid (0 = small, 1 = medium, 2 = large) */
    private int size;

    /** The outline of the asteroid */
    private Shape outline;

    /** The game controller */
    private Controller controller;

    /**
     * Throws an IllegalArgumentException if size or variety is out of range.
     * 
     * Creates an asteroid of the specified variety (0 through 3) and size (0 = small, 1 = medium, 2 = large) and
     * positions it at the provided coordinates with a random rotation. Its velocity has the given speed but is in a
     * random direction.
     */
    public Asteroid (int variety, int size, double x, double y, int speed, Controller controller)
    {
        // Make sure size and variety are valid
        if (size < 0 || size > 2)
        {
            throw new IllegalArgumentException("Invalid asteroid size: " + size);
        }
        else if (variety < 0 || variety > 3)
        {
            throw new IllegalArgumentException();
        }

        // Create the asteroid
        this.controller = controller;
        this.size = size;
        setPosition(x, y);
        setVelocity(speed, RANDOM.nextDouble() * 2 * Math.PI);
        setRotation(2 * Math.PI * RANDOM.nextDouble());
        createAsteroidOutline(variety, size);
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    /**
     * Creates the outline of the asteroid based on its variety and size.
     */
    private void createAsteroidOutline (int variety, int size)
    {
        // This will contain the outline
        Path2D.Double poly = new Path2D.Double();

        // Fill out according to variety
        if (variety == 0)
        {
            poly.moveTo(0, -30);
            poly.lineTo(28, -15);
            poly.lineTo(20, 20);
            poly.lineTo(4, 8);
            poly.lineTo(-1, 30);
            poly.lineTo(-12, 15);
            poly.lineTo(-5, 2);
            poly.lineTo(-25, 7);
            poly.lineTo(-10, -25);
            poly.closePath();
        }
        else if (variety == 1)
        {
            poly.moveTo(10, -28);
            poly.lineTo(7, -16);
            poly.lineTo(30, -9);
            poly.lineTo(30, 9);
            poly.lineTo(10, 13);
            poly.lineTo(5, 30);
            poly.lineTo(-8, 28);
            poly.lineTo(-6, 6);
            poly.lineTo(-27, 12);
            poly.lineTo(-30, -11);
            poly.lineTo(-6, -15);
            poly.lineTo(-6, -28);
            poly.closePath();
        }
        else if (variety == 2)
        {
            poly.moveTo(10, -30);
            poly.lineTo(30, 0);
            poly.lineTo(15, 30);
            poly.lineTo(0, 15);
            poly.lineTo(-15, 30);
            poly.lineTo(-30, 0);
            poly.lineTo(-10, -30);
            poly.closePath();
        }
        else
        {
            poly.moveTo(30, -18);
            poly.lineTo(5, 5);
            poly.lineTo(30, 15);
            poly.lineTo(15, 30);
            poly.lineTo(0, 25);
            poly.lineTo(-15, 30);
            poly.lineTo(-25, 8);
            poly.lineTo(-10, -25);
            poly.lineTo(0, -30);
            poly.lineTo(10, -30);
            poly.closePath();
        }

        // Scale to the desired size
        double scale = ASTEROID_SCALE[size];
        poly.transform(AffineTransform.getScaleInstance(scale, scale));

        // Save the outline
        outline = poly;
    }

    /**
     * Returns the size of the asteroid
     */
    public int getSize ()
    {
        return size;
    }

    /**
     * When an Asteroid collides with an AsteroidDestroyer, it expires or splits, based on size. Will also create
     * temporary debris where the destroyed asteroid used to be. Additionally, explosion sounds will play and scores
     * will update.
     */
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof AsteroidDestroyer)
        {
            if (this.getSize() == 2)
            {
                controller.addParticipant(
                        new Asteroid(0, 1, this.getX(), this.getY(), RANDOM.nextInt(3 + 1) + 3, controller));
                controller.addParticipant(
                        new Asteroid(0, 1, this.getX(), this.getY(), RANDOM.nextInt(3 + 1) + 3, controller));
                controller.addParticipant(
                        new AsteroidDebris(this.getX(), this.getY(), 2 * Math.PI * RANDOM.nextDouble()));
                controller.addParticipant(
                        new AsteroidDebris(this.getX(), this.getY(), 2 * Math.PI * RANDOM.nextDouble()));
                controller.scoreControl(20);
                controller.playSound("/sounds/bangLarge.wav");
            }
            else if (this.getSize() == 1)
            {
                controller.addParticipant(
                        new Asteroid(0, 0, this.getX(), this.getY(), RANDOM.nextInt(5 + 1) + 3, controller));
                controller.addParticipant(
                        new Asteroid(0, 0, this.getX(), this.getY(), RANDOM.nextInt(5 + 1) + 3, controller));
                controller.addParticipant(
                        new AsteroidDebris(this.getX(), this.getY(), 2 * Math.PI * RANDOM.nextDouble()));
                controller.addParticipant(
                        new AsteroidDebris(this.getX(), this.getY(), 2 * Math.PI * RANDOM.nextDouble()));
                controller.scoreControl(50);
                controller.playSound("/sounds/bangMedium.wav");
            }
            else
            {
                controller.playSound("/sounds/bangSmall.wav");
                controller.scoreControl(100);
            }

            controller.addParticipant(new AsteroidDebris(this.getX(), this.getY(), 2 * Math.PI * RANDOM.nextDouble()));
            controller.addParticipant(new AsteroidDebris(this.getX(), this.getY(), 2 * Math.PI * RANDOM.nextDouble()));

            // Expire the asteroid
            Participant.expire(this);
            if (!p.isInvulnerability())
            {
                Participant.expire(p);
            }

            // Inform the controller
            controller.asteroidDestroyed();
            if (controller.getVersion() == 1)
            {
                this.dropLoot();
            }
        }
    }

    public void dropLoot ()
    {
        Random rng = new Random();
        if (rng.nextInt(100) < 20)
        {
            controller.addParticipant(new supply(rng.nextInt(3), this.getX(), this.getY(), controller));
        }
    }
}
