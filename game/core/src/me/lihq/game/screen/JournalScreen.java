package me.lihq.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

import me.lihq.game.GameMain;
import me.lihq.game.screen.elements.journal.Journal;

/**
 * Used to control the initialisation and drawing of the Journal Screen.
 * @author jacobwunwin
 *
 */
public class JournalScreen extends AbstractScreen {

    private Journal journal;

    /**
     * This is the camera for the screen
     */
    private OrthographicCamera camera = new OrthographicCamera();


    public JournalScreen(GameMain game) {
        super(game);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        //Setting up the camera
        this.camera.setToOrtho(false, w, h);
        this.camera.update();

        this.journal = new Journal(game);
    }

    @Override
    public void show() {
        this.journal.cluesView.updateMain(); //update the clues table
    }

    @Override
    public void update() {
        this.journal.updateMain();
    }

    @Override
    public void render(float delta) {
        this.journal.renderMain();
    }

    @Override
    public void resize(int width, int height) {
        this.journal.resize(width, height);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        this.journal.dispose();
    }
}
