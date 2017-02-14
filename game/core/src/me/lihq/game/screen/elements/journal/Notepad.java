package me.lihq.game.screen.elements.journal;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Creates a Notepad for the Journal scren and allows it to be
 * rendered.
 * @author jacobwunwin
 *
 */
public class Notepad {

    private Skin uiSkin;
    private Stage stage;

    public Notepad(Skin uiSkin) {
        this.uiSkin = uiSkin;
    }

    public void renderMain() {

    }

    public Stage getStage() {
        return stage;
    }
}
