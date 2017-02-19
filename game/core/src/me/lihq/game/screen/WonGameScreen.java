package me.lihq.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.lihq.game.GameMain;
import me.lihq.game.screen.elements.WonGame;

/**
 * Created by vishal on 18/12/2016.
 */
public class WonGameScreen extends AbstractScreen
{
    /**
     * This is the menu element for the pause menu
     */
    private WonGame wonGame;

    /**
     * The camera for the pause menu to use
     */
    private OrthographicCamera camera = new OrthographicCamera();
    private Viewport viewport;

    /**
     * This constructor sets the relevant properties of the class.
     *
     * @param game this provides access to the gameMain class so that screens can set the states of the game.
     */
    public WonGameScreen(GameMain game) {
        super(game);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        //Sets up the camera
        camera.setToOrtho(false, w, h);
        camera.update();

        //Creates the Pause menu
        this.wonGame = new WonGame(game);
    }

    /**
     * This is ran when the navigation screen becomes the visible screen in GameMain
     */
    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this.wonGame.stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * This method is called once a game tick
     */
    @Override
    public void update() {

    }

    /**
     * Called when the screen should render itself.
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        //Renders the pause menu
        this.wonGame.render();
    }

    /**
     * This is called when the window is resized
     *
     * @param width  - The new width
     * @param height - The new height
     */
    @Override
    public void resize(int width, int height) {
        this.wonGame.resize(width, height);
    }

    /**
     * This is called when the focus is lost on the window
     */
    @Override
    public void pause() {

    }

    /**
     * This method is called when the window is brought back into focus
     */
    @Override
    public void resume() {

    }

    /**
     * This method is called when the user hides the window
     */
    @Override
    public void hide() {

    }

    /**
     * This is to be called when you want to dispose of all data
     */
    @Override
    public void dispose() {
        //Disposes of the Pause menu
        this.wonGame.dispose();
    }
}
