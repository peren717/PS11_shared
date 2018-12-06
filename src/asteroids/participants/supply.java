package asteroids.participants;

import static asteroids.game.Constants.RANDOM;
import java.awt.Shape;
import java.awt.geom.Path2D;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class supply extends Participant
{
    /** The game controller */
    private Controller controller;

    /** The outline of the asteroid */
    private Shape outline;

    private int variety;

    public supply (int variety, double x, double y, Controller controller)
    {
        // Create the supply loot
        this.controller = controller;
        setPosition(x, y);
        setVelocity(5, RANDOM.nextDouble() * 2 * Math.PI);
        setRotation(2 * Math.PI * RANDOM.nextDouble());
        createSupplyLootOutline(variety);
        this.variety = variety;
        new ParticipantCountdownTimer(this, "expire", 8000);
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {

        if (p instanceof Ship)
        {
            ((Ship) p).getSupply(variety);
            Participant.expire(this);
        }

    }

    private void createSupplyLootOutline (int variety)
    {
        // This will contain the outline
        Path2D.Double poly = new Path2D.Double();

        if (variety == 1)
        {
            poly.moveTo(20 / 2, 10 / 2);
            poly.lineTo(20 / 2, -10 / 2);
            poly.lineTo(10 / 2, -20 / 2);
            poly.lineTo(-10 / 2, -20 / 2);
            poly.lineTo(-20 / 2, -10 / 2);
            poly.lineTo(-20 / 2, 10 / 2);
            poly.lineTo(-10 / 2, 20 / 2);
            poly.lineTo(10 / 2, 20 / 2);
            poly.closePath();
        }
        else if (variety == 2)
        {
            poly.moveTo(0, -10);
            poly.lineTo(10, 0);
            poly.lineTo(10, 10);
            poly.lineTo(5, 10);
            poly.lineTo(0, 5);
            poly.lineTo(-5, 10);
            poly.lineTo(-10, 10);
            poly.lineTo(-10, 0);
            poly.closePath();
        }

        // Save the outline
        outline = poly;
    }
    
    @Override
    public void countdownComplete (Object payload)
    {
        if (payload.equals("expire"))
        {
            Participant.expire(this);
        }
    }

}
