/*
* This is the link to the executable jar file created from this project
*
* http://www.lihq.me/Downloads/Assessment2/Game.jar
*
* or visit http://www.lihq.me
* and click "Download Game"
 */

package me.lihq.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import me.lihq.game.models.Clue;
import me.lihq.game.models.Map;
import me.lihq.game.models.Room;
import me.lihq.game.models.Vector2Int;
import me.lihq.game.models.generation.ScenarioDatabase;
import me.lihq.game.people.NPC;
import me.lihq.game.people.Player;
import me.lihq.game.screen.AbstractScreen;
import me.lihq.game.screen.CharacterCreationScreen;
import me.lihq.game.screen.JournalScreen;
import me.lihq.game.screen.MainMenuScreen;
import me.lihq.game.screen.NavigationScreen;
import me.lihq.game.screen.WonGameScreen;

import java.util.*;

/**
 * This is the class responsible for the game as a whole. It manages the current states and entry points of the game
 */
public class GameMain extends Game
{
    /**
     * This is a static reference to itself. Comes in REALLY handy when in other classes that don't have a reference to the main game
     */
    public static GameMain me = null;

    /**
     * A list holding NPC objects
     */
    private List<NPC> NPCs = new ArrayList<>();

    /**
     * The game map
     */
    private Map gameMap;
    /**
     * A player object for the player of the game
     */
    public Player player;

    /**
     * This controls the game ticks and calculating how many ticks per second there are
     */
    private int ticks = 0;
    private int lastSecond = -1;

    /**
     * A screen to be used to display standard gameplay within the game , including the status bar.
     */
    public NavigationScreen navigationScreen;

    /**
     * An FPSLogger, FPSLogger allows us to check the game FPS is good enough
     */
    private FPSLogger FPS;

    /**
     * The main menu screen that shows up when the game is first started
     */
    private MainMenuScreen menuScreen;

    /**
     * The Creation Screen that is used to select character traits
     */
    public CharacterCreationScreen creationScreen;

    /**
     * The Screen that is displayed when the player wins the game
     */
    public WonGameScreen wonGameScreen;

    /**
     * The Journal Screen that is used to display the Journal
     */
    public JournalScreen journalScreen;

    /**
     * The score tracker.
     */
    public ScoreTracker scoreTracker;

    /**
     * The generate game screen generates the game. this is triggered by the completion of the CharacterCreationScreen.
     * An array list of traits is passed in for the generation class to make use of.
     */
    public void generateGame(ArrayList<String> traits, String playerCostume) {
        this.gameMap = new Map(); //instantiate game map

        //Add ALL NPCs to the list
        //This is how you initialise an NPC
        this.player = new Player("Player", playerCostume, 3, 6);
        this.player.setRoom(gameMap.getRoom(0));

        //set up the screen and display the first room
        this.navigationScreen = new NavigationScreen(this);
        this.navigationScreen.updateTiledMapRenderer();

        ScenarioDatabase db = new ScenarioDatabase("scenario_gen.db", traits);

    }

    /**
     * This is called at start up. It initialises the game.
     */
    @Override
    public void create()
    {
        this.me = this;

        Assets.load();// Load in the assets the game needs

        //Set up the Menu
        this.menuScreen = new MainMenuScreen(this);
        this.journalScreen = new JournalScreen(this);
        this.creationScreen = new CharacterCreationScreen(this);
        this.wonGameScreen = new WonGameScreen(this);

        this.setScreen(this.menuScreen);

        //Instantiate the FPSLogger to show FPS
        FPS = new FPSLogger();

        this.scoreTracker = new ScoreTracker();

        gameLoop();
    }

    /**
     * This defines what's rendered on the screen for each frame.
     */
    @Override
    public void render()
    {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //FPS.log();//this is where fps is displayed

        super.render(); // This calls the render method of the screen that is currently set

    }

    /**
     * This is to be called when you want to dispose of all data
     */
    @Override
    public void dispose()
    {

    }

    /**
     * Overrides the getScreen method to return our AbstractScreen type.
     * This means that we can access the additional methods like update.
     *
     * @return The current screen of the game.
     */
    @Override
    public AbstractScreen getScreen()
    {
        return (AbstractScreen) super.getScreen();
    }

    /**
     * This is the main gameLoop that only needs to be called once, it then creates a logic thread to be executed once a game tick
     */
    public void gameLoop()
    {
        Timer gameTimer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                ticks++;

                Calendar cal = Calendar.getInstance();

                if (cal.get(Calendar.SECOND) != lastSecond) {
                    lastSecond = cal.get(Calendar.SECOND);
                    System.out.println("TPSLogger: tps:      " + ticks);
                    ticks = 0;
                }

                me.getScreen().update();
            }
        };

        gameTimer.schedule(task, 0, 1000 / Settings.TPS);
    }

    /**
     * This method returns the Navigation Screen that the game runs on.
     *
     * @return navigationScreen - The gameplay screen.
     */
    public NavigationScreen getNavigationScreen()
    {
        return navigationScreen;
    }

    /**
     * This method returns a list of the NPCs that are in the specified room
     *
     * @param room The room to check.
     * @return The NPCs that are in the specified room.
     */
    public List<NPC> getNPCS(Room room)
    {
        List<NPC> npcsInRoom = new ArrayList<>();
        for (NPC n : this.NPCs) {
            if (n.getRoom() == room) {
                npcsInRoom.add(n);
            }
        }

        return npcsInRoom;
    }
}
