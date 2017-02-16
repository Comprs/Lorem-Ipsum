package me.lihq.game.screen.elements.journal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

import me.lihq.game.GameMain;

/**
 * Creates the base Journal components including the background image, and left hand menu. Also allows the selection of
 * Clues, Speech or Notepad for display and renders these objects.
 * @author jacobwunwin
 *
 */
public class Journal {

    enum State {
        clues, notepad
    }

    public Stage stage;
    private Skin uiSkin;
    private State state;
    private Sprite backgroundImage;
    public Clues cluesView;
    private Notepad notepadView;
    private SpriteBatch batch;

    public Journal(final GameMain game) {
        this.uiSkin = initSkin();
        this.stage = this.initJournal(this.uiSkin, game);
        this.state = State.clues; //start on the clues view
        this.batch = new SpriteBatch();

        this.cluesView = new Clues(game, this.uiSkin);
        this.notepadView = new Notepad(game, uiSkin);
    }

    public Skin initSkin() {
        Skin skin = new Skin(Gdx.files.internal("skins/skin_pretty/skin.json")); //load ui skin from assets
        return skin;
    }

    public Stage initJournal(Skin uiSkin, GameMain game) {

        Stage stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        //create a sprite for the journal background
        Texture journalBackground = new Texture(Gdx.files.internal("Open_journal.png"));
        this.backgroundImage = new Sprite(journalBackground);
        this.backgroundImage.setPosition(50, 90);

        //Create buttons and labels
        final TextButton cluesButton = new TextButton("Clues", uiSkin);
        final TextButton notepadButton = new TextButton("Notepad", uiSkin);
        final TextButton closeButton = new TextButton("Close", uiSkin);
        Label journalLabel = new Label ("Journal", uiSkin);

        //textButton.setPosition(200, 200);
        journalLabel.setPosition(240, 600);
        journalLabel.setColor(Color.BLACK);
        journalLabel.setFontScale(1.5f);

        cluesButton.setPosition(260, 400);
        notepadButton.setPosition(250, 350);
        closeButton.setPosition(260, 300);

        // Add a listener to the clues button
        cluesButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                System.out.println("Clues button was pressed");
                state = State.clues; //change the journal state to that of the clues table
            }
        });

        //add a listener for the show interview log button
        notepadButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                System.out.println("Notepad button was pressed");
                state = State.notepad;
            }
        });

       //add a listener for the show interview log button
        closeButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                System.out.println("Close Journa button was pressed");
                game.setScreen(game.navigationScreen);
            }
        });

        stage.addActor(cluesButton);
        stage.addActor(journalLabel);
        stage.addActor(notepadButton);
        stage.addActor(closeButton);

        return stage;
    }

    public void updateMain() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this.stage);
        switch (this.state){
            case clues:
                multiplexer.addProcessor(this.cluesView.getStage());
                break;
            case notepad:
                multiplexer.addProcessor(this.notepadView.getStage());
                break;
            default:
                break;
        }
        Gdx.input.setInputProcessor(multiplexer);
    }

    public void renderMain() {
        this.batch.begin();
        this.backgroundImage.draw(this.batch); //draw the journal background
        this.batch.end();
        this.stage.act();
        this.stage.draw();

        switch (this.state){
            case clues:
                this.cluesView.renderMain();
                break;
            case notepad:
                this.notepadView.renderMain();
                break;
            default:
                break;
        }
    }

    /**
     * This method disposes of all elements
     */
    public void dispose() {
        //Called when disposing the main menu
        stage.dispose();
        this.cluesView.dispose();
        this.notepadView.dispose();
    }

    /**
     * This method is called when the window is resized.
     *
     * @param width  - The new width
     * @param height - The new height
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        //update the secondary GUI components
        this.cluesView.resize(width, height);
        this.notepadView.resize(width, height);
    }
}
