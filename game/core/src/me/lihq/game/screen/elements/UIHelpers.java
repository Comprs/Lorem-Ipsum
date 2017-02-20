package me.lihq.game.screen.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * This is used to create a Sprite of one colour at the specified size.
 * <p>
 * Used when creating a UI.
 * </p>
 */
public class UIHelpers {
    /**
     * Returns drawable with single colour fill.
     *
     * @param colour Colour to fill drawable with.
     * @param width The width of the sprite.
     * @param height The height of the sprite.
     * @return Drawable to use with LibGdx Scene2d controls.
     */
    public static Drawable getBackgroundDrawable(Color colour, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(colour);
        pixmap.fill();
        return new Image(new Texture(pixmap)).getDrawable();
    }
}
