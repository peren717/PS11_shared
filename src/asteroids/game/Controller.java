package asteroids.game;

import static asteroids.game.Constants.*;
import java.awt.MouseInfo;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import asteroids.participants.*;

/**
 * Controls a game of Asteroids.
 */
public class Controller implements KeyListener, ActionListener, MouseListener
{
    /** The state of all the Participants */
    private ParticipantState pstate;

    /** The ship (if one is active) or null (otherwise) */
    private Ship ship;

    /** The alien ship */
    private AlienShip AlienShip;

    /** When this timer goes off, it is time to refresh the animation */
    private Timer refreshTimer;

    /**
     * The time at which a transition to a new stage of the game should be made. A transition is scheduled a few seconds
     * in the future to give the user time to see what has happened before doing something like going to a new level or
     * resetting the current level.
     */
    private long transitionTime;

    /** Number of lives left */
    private int lives;

    /** The game display */
    private Display display;

    /** Level currently being played **/
    private int level;

    /** Current Score */
    private int score;

    /** Boolean that indicates ship movement */
    private boolean forward;
    /** Boolean that indicates ship movement */
    private boolean right;
    /** Boolean that indicates ship movement */
    private boolean left;
    /** Boolean that indicates ship movement */
    private boolean fire;

    /** Mouse coordinates X */
    private double mouseX;
    /** Mouse coordinates Y */
    private double mouseY;

    /** console command */
    private String consoleCommand;

    /** True if mouse control is enabled */
    private boolean mouseControl;

    /** The version of the game */
    private int version;

    /**
     * Constructs a controller to coordinate the game and screen
     */
    public Controller (int version)
    {
        // Initialize the ParticipantState
        pstate = new ParticipantState();

        // Set up the refresh timer.
        refreshTimer = new Timer(FRAME_INTERVAL, this);

        // Clear the transitionTime
        transitionTime = Long.MAX_VALUE;

        // Record the display object
        display = new Display(this);

        // Initialize level
        level = 1;

        // initialize control
        mouseControl = false;

        // Bring up the splash screen and start the refresh timer
        splashScreen();
        display.setVisible(true);
        refreshTimer.start();

        // Sets the version
        this.version = version;
    }

    /**
     * Returns the ship, or null if there isn't one
     */
    public Ship getShip ()
    {
        return ship;
    }

    /**
     * Configures the game screen to display the splash screen
     */
    private void splashScreen ()
    {
        // Clear the screen, reset the level, and display the legend
        clear();
        display.setLegend("Asteroids");

        // Place four asteroids near the corners of the screen.
        placeAsteroids(4);
        if (version == 1)
        {
            // place stars as background
            placeStars();
        }
    }

    /**
     * The game is over. Displays a message to that effect.
     */
    private void finalScreen ()
    {
        display.setLegend(GAME_OVER);
        display.removeKeyListener(this);
    }

    /**
     * Place a new ship in the center of the screen. Remove any existing ship first.
     */
    private void placeShip ()
    {
        // Place a new ship
        Participant.expire(ship);
        ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
        addParticipant(ship);
        display.setLegend("");
    }

    /**
     * Places an asteroid near the corners of the screen based on level. Gives it a random velocity, variety, and
     * rotation.
     */
    private void placeAsteroids (int num)
    {
        while (num > 0)
        {
            if (num % 4 == 0)
            {
                addParticipant(new Asteroid(new Random().nextInt(3), 2, 150, 150, 3, this));
                num--;
            }
            else if (num % 4 == 1)
            {
                addParticipant(new Asteroid(new Random().nextInt(3), 2, 600, 150, 3, this));
                num--;
            }
            else if (num % 4 == 2)
            {
                addParticipant(new Asteroid(new Random().nextInt(3), 2, 150, 600, 3, this));
                num--;
            }
            else
            {
                addParticipant(new Asteroid(new Random().nextInt(3), 2, 600, 600, 3, this));
                num--;
            }
        }
    }

    /**
     * Place some stars
     */
    private void placeStars ()
    {
        Random rng = new Random();
        for (int i = 0; i < rng.nextInt(10) + 5; i++)
        {
            this.addParticipant(new Star());
        }
    }

    /**
     * Clears the screen so that nothing is displayed
     */
    private void clear ()
    {
        pstate.clear();
        display.setLegend("");
        ship = null;
        AlienShip = null;
    }

    /**
     * Sets things up and begins a new game.
     */
    private void initialScreen ()
    {
        // Clear the screen
        clear();

        // Place asteroids
        placeAsteroids(4);

        if (version == 1)
        {
            // place stars
            placeStars();
        }

        // Place the ship
        placeShip();

        // reset movement
        right = false;
        left = false;
        forward = false;
        fire = false;

        // Reset statistics
        level = 1;
        display.setLevel(level);
        lives = 3;
        display.setLives(lives);

        // Start listening to events (but don't listen twice)
        display.removeKeyListener(this);
        display.addKeyListener(this);
        display.removeMouseListener(this);
        display.addMouseListener(this);

        // Give focus to the game screen
        display.requestFocusInWindow();

        // Set the level control
        this.levelControl(level);
    }

    /**
     * Adds a new Participant
     */
    public void addParticipant (Participant p)
    {
        pstate.addParticipant(p);
    }

    /**
     * The ship has been destroyed
     */
    public void shipDestroyed ()
    {
        // Null out the ship
        ship = null;
        fire = false;
        forward = false;
        left = false;
        right = false;
        // Display a legend
        display.setLegend("Ouch!");

        // Decrement lives
        lives--;
        display.setLives(lives);

        // Since the ship was destroyed, schedule a transition
        scheduleTransition(END_DELAY);
    }

    /**
     * An asteroid has been destroyed
     */
    public void asteroidDestroyed ()
    {
        // If all the asteroids are gone, schedule a transition
        if (pstate.countAsteroids() == 0)
        {
            scheduleTransition(END_DELAY);
        }

        display.setScore(score);
    }

    public void AlienShipDestroyed ()
    {
        AlienShip = null;
        Random rng = new Random();
        this.scheduleTransition(5000 + rng.nextInt(5000));
    }

    /**
     * Schedules a transition m msecs in the future
     */
    private void scheduleTransition (int m)
    {
        transitionTime = System.currentTimeMillis() + m;
    }

    /**
     * Return true if there are at least 8 bullets
     */
    public boolean hasMaxBullets ()
    {
        if (!ship.isInvulnerable)
        {
            return this.pstate.countBullets() >= 8;
        }
        else
        {
            return false;
        }
    }

    /**
     * This method will be invoked because of button presses and timer events.
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
        // The start button has been pressed. Stop whatever we're doing
        // and bring up the initial screen
        if (e.getSource() instanceof JButton)
        {
            initialScreen();
        }

        // Time to refresh the screen and deal with keyboard input
        else if (e.getSource() == refreshTimer)
        {
            // It may be time to make a game transition
            performTransition();

            // Move the ship according to which boolean is currently true
            if (ship != null)
            {
                if (right)
                {
                    ship.turnRight();
                }
                else if (left)
                {
                    ship.turnLeft();
                }
                if (forward)
                {
                    ship.accelerate();
                }
                if (fire)
                {
                    ship.fire();
                }
            }

            // Control the alien ship
            if (AlienShip != null)
            {
                AlienShip.setSpeed(MAXIMUM_LARGE_ASTEROID_SPEED);
            }

            // Update mouse coordinates
            mouseY = MouseInfo.getPointerInfo().getLocation().y;
            mouseX = MouseInfo.getPointerInfo().getLocation().x;
            // Update ship direction
            if (ship != null && mouseControl)
            {
                double dx = mouseX - ship.getX();
                double dy = mouseY - ship.getY();
                ship.setRotation(Participant.normalize(Math.atan2(dy, dx)));
            }

            // Move the participants to their new locations
            pstate.moveParticipants();

            // Refresh screen
            display.refresh();
        }
    }

    /**
     * Returns an iterator over the active participants
     */
    public Iterator<Participant> getParticipants ()
    {
        return pstate.getParticipants();
    }

    /**
     * If the transition time has been reached, transition to a new state
     */
    private void performTransition ()
    {
        // Do something only if the time has been reached
        if (transitionTime <= System.currentTimeMillis())
        {
            // Clear the transition time
            transitionTime = Long.MAX_VALUE;

            // If there are no lives left, the game is over. Show the final
            // screen.
            if (lives <= 0)
            {
                finalScreen();
            }
            else if (pstate.countAsteroids() == 0 && pstate.countAlienShip() == 0)
            {
                placeAsteroids(level + 4);
                level++;
                display.setLevel(level);
                levelControl(level);
            }
            else if (ship == null)
            {
                this.placeShip();
            }
            else if (AlienShip == null)
            {
                levelControl(level);
            }
        }
    }

    /**
     * Spawns the alien ship according to the current level
     */
    public void levelControl (int level)
    {
        if (level == 2)
        {
            Random rng = new Random();
            int posY = rng.nextInt(SIZE);
            AlienShip = new AlienShip(0, posY, 0, this, level);
            this.addParticipant(AlienShip);
        }
        else if (level > 2)
        {
            Random rng = new Random();
            int posY = rng.nextInt(SIZE);
            AlienShip = new AlienShip(0, posY, 0, this, level);
            this.addParticipant(AlienShip);
        }
    }

    public void scoreControl (int score)
    {
        this.score = this.score + score;
    }

    /**
     * If a key of interest is pressed, record that it is down.
     */
    @Override
    public void keyPressed (KeyEvent e)
    {
        if ((e.getKeyCode() == KeyEvent.VK_D | e.getKeyCode() == KeyEvent.VK_RIGHT) && ship != null)
        {
            right = true;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_A | e.getKeyCode() == KeyEvent.VK_LEFT) && ship != null)
        {
            left = true;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_W | e.getKeyCode() == KeyEvent.VK_UP) && ship != null)
        {
            forward = true;
        }
        if ((e.getKeyCode() == KeyEvent.VK_SPACE) && ship != null)
        {
            fire = true;
        }

        if (version == 1 && e.getKeyCode() == KeyEvent.VK_M && ship != null && version == 1)
        {
            if (mouseControl)
            {
                mouseControl = false;
            }
            if (!mouseControl)
            {
                mouseControl = true;
            }
        }
    }

    /**
     * Return the version of the game
     */
    public int getVersion ()
    {
        return version;
    }

    /**
     * These events are ignored.
     */
    @Override
    public void keyTyped (KeyEvent e)
    {
    }

    /**
     * If a key of interest is released, record that it is up.
     */
    @Override
    public void keyReleased (KeyEvent e)
    {
        if ((e.getKeyCode() == KeyEvent.VK_D | e.getKeyCode() == KeyEvent.VK_RIGHT) && ship != null)
        {
            right = false;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_A | e.getKeyCode() == KeyEvent.VK_LEFT) && ship != null)
        {
            left = false;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_W | e.getKeyCode() == KeyEvent.VK_UP) && ship != null)
        {
            forward = false;
            ship.stop();
        }
        if ((e.getKeyCode() == KeyEvent.VK_SPACE) && ship != null)
        {
            fire = false;
        }

        // debugging console commands
        if (e.getKeyCode() == KeyEvent.VK_DECIMAL)
        {
            consoleCommand = JOptionPane.showInputDialog(null, "", "Console", JOptionPane.DEFAULT_OPTION);
            // Execute console command
            if (consoleCommand != null)
            {
                if (consoleCommand.equals("whosyourdaddy") && ship != null)
                {
                    this.playSound("/sounds/John_Cena.wav");
                    ship.setInvulnerability();
                }
                else if (consoleCommand.equals("add_as") && ship != null)
                {
                    addParticipant(new Asteroid(new Random().nextInt(3), 2, 150, 150, 3, this));
                }
                else if (consoleCommand.equals("mousecontrol") && ship != null)
                {
                    if (mouseControl)
                    {
                        mouseControl = false;
                    }
                    if (!mouseControl)
                    {
                        mouseControl = true;
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "Command invalid: ship is expired");
                }
            }
        }
    }

    /**
     * Creates an audio clip from a sound file.
     */
    public Clip createClip (String soundFile)
    {
        // Opening the sound file this way will work no matter how the
        // project is exported. The only restriction is that the
        // sound files must be stored in a package.
        try (BufferedInputStream sound = new BufferedInputStream(getClass().getResourceAsStream(soundFile)))
        {
            // Create and return a Clip that will play a sound file. There are
            // various reasons that the creation attempt could fail. If it
            // fails, return null.
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(sound));
            return clip;
        }
        catch (LineUnavailableException e)
        {
            return null;
        }
        catch (IOException e)
        {
            return null;
        }
        catch (UnsupportedAudioFileException e)
        {
            return null;
        }
    }

    /**
     * Play designated sound
     */
    public void playSound (String filePath)
    {
        Clip sound = createClip(filePath);
        if (sound.isRunning())
        {
            sound.stop();
        }
        sound.setFramePosition(0);
        sound.start();
    }

    @Override
    public void mouseClicked (MouseEvent e)
    {
    }

    /**
     * Left pressed to accelerate, right pressed to fire
     */
    @Override
    public void mousePressed (MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1 && mouseControl)
        {
            forward = true;
        }
        else
        {
            fire = true;
        }

    }

    /**
     * stop accelerating and firing once button released
     */
    @Override
    public void mouseReleased (MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1 && mouseControl)
        {
            forward = false;
            if (ship != null)
            {
                ship.stop();
            }
        }
        else
        {
            fire = false;
        }
    }

    @Override
    public void mouseEntered (MouseEvent e)
    {
    }

    @Override
    public void mouseExited (MouseEvent e)
    {
    }

    /**
     * Add extra lives if having less than 10 lives, otherwise add 1000 score.
     */
    public void addLives (int num)
    {
        if (lives < 10)
        {
            this.lives = this.lives + num;
            display.setLives(lives);
        }
        else
        {
            score = score + 1000;
            display.setScore(score);
        }
    }
}
