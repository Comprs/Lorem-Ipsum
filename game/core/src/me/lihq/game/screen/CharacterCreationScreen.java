package me.lihq.game.screen;

import com.badlogic.gdx.Gdx;

import me.lihq.game.GameMain;
import me.lihq.game.screen.elements.CharacterGeneration;

/**
 * @author jacobwunwin
 *
 */
public class CharacterCreationScreen extends AbstractScreen {

    private CharacterGeneration characterGeneration;
    private GameMain game;

    public CharacterCreationScreen(GameMain game) {
        super(game);
        this.game = game;
    }

    @Override
    public void show() {
        this.characterGeneration = new CharacterGeneration(this.game); //initialise the character generation class
        Gdx.input.setInputProcessor(this.characterGeneration.stage);
    }

    @Override
    public void update() {
        // TODO Auto-generated method stub

    }

    @Override
    public void render(float delta) {
        this.characterGeneration.renderMain();
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

    }

}
