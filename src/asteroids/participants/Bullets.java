package asteroids.participants;

import java.awt.Shape;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.game.Controller;
import asteroids.game.Participant;

public class Bullets extends Participant implements AsteroidDestroyer
{

    public Bullets (double x, double y, double direction, Controller controller)
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Shape getOutline ()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void collidedWith (Participant p)
    {
        // TODO Auto-generated method stub

    }

}
