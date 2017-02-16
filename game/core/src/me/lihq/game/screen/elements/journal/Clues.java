package me.lihq.game.screen.elements.journal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

import me.lihq.game.GameMain;
import me.lihq.game.models.Clue;

/**
 * Creates components for the clues viewer for the Journal screen
 * and allows them to be rendered.
 * @author jacobwunwin
 *
 */
public class Clues {
    private GameMain game;
    private Table cluesTable;
    private Stage stage;
    private Skin uiSkin;

    public Clues(GameMain game, Skin uiSkin) {
        this.game = game;
        this.stage = this.initClues(game, uiSkin);
        this.uiSkin = uiSkin;
    }

    public Stage initClues(GameMain game, Skin uiSkin) {
        Stage stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        Label clueLabel = new Label("Clues", uiSkin);

        clueLabel.setColor(Color.BLACK);
        clueLabel.setFontScale(1.5f);

        clueLabel.setPosition(650, 600);

        //create a new table to store clues
        this.cluesTable = new Table(uiSkin);

        //place the clues table in a scroll pane
        Table container = new Table(uiSkin);
        ScrollPane scroll = new ScrollPane(cluesTable, uiSkin);


        scroll.layout();
        //add the scroll pane to an external container
        container.add(scroll).width(300f).height(400f);
        container.row();
        container.setPosition(700, 360); //set the position of the extenal container

        //add actors to the clues stage
        stage.addActor(clueLabel);
        stage.addActor(container);

        return stage;
    }

    public void updateMain() {
        //reset the cluesTable and update its contents
        this.cluesTable.reset(); //reset the table

        for (Clue clue : this.game.player.collectedClues) {
            Label label = new Label (clue.getName() + " : " + clue.getDescription(), this.uiSkin);
            cluesTable.add(label).width(280f); //set a maximum width on the row of 300 pixels
            cluesTable.row(); //end the row
        }
    }

    public void renderMain() {
        this.stage.act();
        this.stage.draw();
    }

    /**
     * This method disposes of all elements
     */
    public void dispose() {
        this.stage.dispose();
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

    public Stage getStage() {
        return this.stage;
    }
}
