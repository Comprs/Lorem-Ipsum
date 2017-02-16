package me.lihq.game.screen.elements.journal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.utils.viewport.FitViewport;

import me.lihq.game.GameMain;

/**
 * Creates a Notepad for the Journal scren and allows it to be
 * rendered.
 * @author jacobwunwin
 *
 */
public class Notepad {

    private Skin uiSkin;
    private Stage stage;
    private GameMain game;

    public Notepad(final GameMain game, Skin uiSkin) {
        this.game = game;
        this.uiSkin = uiSkin;
        this.stage = this.initNotepad();
    }

    public Stage initNotepad() {
        Stage stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        //Create labels
        Label notepadLabel = new Label("Notepad", this.uiSkin);

        notepadLabel.setColor(Color.BLACK);
        notepadLabel.setFontScale(1.5f);

        notepadLabel.setPosition(620, 600);
        TextArea notepad = new TextArea("Here are my notes about a particularly develish crime...", this.uiSkin);
        notepad.setX(550);
        notepad.setY(70);
        notepad.setWidth(290);
        notepad.setHeight(400);

        stage.addActor(notepadLabel);
        stage.addActor(notepad);

        return stage;
    }

    public void renderMain() {
        this.stage.act();
        this.stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * This method disposes of all elements
     */
    public void dispose() {
        //stage.dispose();
    }

    /**
     * This method is called when the window is resized.
     *
     * @param width  - The new width
     * @param height - The new height
     */
    public void resize(int width, int height) {
        //stage.getViewport().update(width, height, true);
    }
}
