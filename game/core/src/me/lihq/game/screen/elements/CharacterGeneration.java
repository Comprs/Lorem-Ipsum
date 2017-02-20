package me.lihq.game.screen.elements;

import java.awt.List;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;

import me.lihq.game.GameMain;

public class CharacterGeneration {
    public Stage stage;
    private Skin uiSkin;
    private Sprite backgroundImage;
    private SpriteBatch batch;
    private ArrayList<String> traits;
    private static int maxTraits = 3;
    private GameMain game;
    private ArrayList<Sprite> costumes;
    private String[] costumeList;
    private int currentCostumePointer;
    private OrthographicCamera camera = new OrthographicCamera();

    public CharacterGeneration(GameMain game) {
        this.uiSkin = this.initSkin();
        this.traits = new ArrayList<String>();
        this.game = game;

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        //Setting up the camera
        this.camera.setToOrtho(false, w, h);
        this.camera.update();

        this.batch = new SpriteBatch();
        this.costumes = new ArrayList<Sprite>();

        this.stage = this.initGeneration();
    }

    /**
     * Initialises the costume generation.
     *
     * @param stage The stage to place UI elements onto.
     */
    private void initCostumeSelection(Stage stage) {
        JsonValue jsonData = new JsonReader().parse(Gdx.files.internal("people/player/sprite_sets/sprites.JSON"));

        this.costumeList = jsonData.get("sprites").asStringArray();

        for (int i = 0; i < this.costumeList.length; i++) {
            String spriteFile = this.costumeList[i].substring(1, this.costumeList[i].length() - 1);
            System.out.println(spriteFile);

            Texture texture = new Texture(Gdx.files.internal("people/player/sprite_sets/" + spriteFile));
            Sprite sprite = new Sprite(texture, 0, 0, 30, 48);
            sprite.scale(4);
            System.out.println(sprite == null);
            sprite.setPosition(690, 350);

            this.costumes.add(sprite);
        }

        this.currentCostumePointer = 0;

        final TextButton nextButton = new TextButton("Next", uiSkin);
        nextButton.setPosition(840, 400);

        // Add a listener to the finish button
        nextButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                if (currentCostumePointer < costumeList.length - 1){
                    currentCostumePointer += 1;
                }
            }
        });

        final TextButton previousButton = new TextButton("Previous", uiSkin);
        previousButton.setPosition(550, 400);

        // Add a listener to the finish button
        previousButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                if (currentCostumePointer > 0){
                    currentCostumePointer -= 1;
                }
            }
        });

        stage.addActor(nextButton);
        stage.addActor(previousButton);
    }

    /**
     * Initialises and places the generation screen GUI.
     *
     * @return The resulting UI.
     */
    private Stage initGeneration() {

        Stage stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        //create a sprite for the journal background
        Texture journalBackground = new Texture(Gdx.files.internal("Open_journal.png"));
        this.backgroundImage = new Sprite(journalBackground);
        this.backgroundImage.setPosition(50, 90);

        //Create buttons and labels
        Label titleLable = new Label ("Make your Character", this.uiSkin);
        titleLable.setPosition(200, 600);
        titleLable.setColor(Color.BLACK);
        titleLable.setFontScale(1.5f);

        //Create buttons and labels
        Label instructionLabel = new Label ("Choose 3 character traits:", this.uiSkin);
        instructionLabel.setPosition(220, 520);
        instructionLabel.setColor(Color.BLACK);
        instructionLabel.setFontScale(1f);

        final TextButton finishButton = new TextButton("Build My Character", uiSkin);
        finishButton.setPosition(650, 150);

        ArrayList<CheckBox> tempCheckBoxes = new ArrayList<CheckBox>();

        final CheckBox checkBoxAggressive = new CheckBox("Aggressive", this.uiSkin);
        checkBoxAggressive.setPosition(250, 450);
        tempCheckBoxes.add(checkBoxAggressive);

        final CheckBox checkBoxPlacating = new CheckBox("Placating", this.uiSkin);
        checkBoxPlacating.setPosition(250, 400);
        tempCheckBoxes.add(checkBoxPlacating);

        final CheckBox checkBoxConversational = new CheckBox("Conversational", this.uiSkin);
        checkBoxConversational.setPosition(250, 350);
        tempCheckBoxes.add(checkBoxConversational);

        final CheckBox checkBoxDirect = new CheckBox("Direct", this.uiSkin);
        checkBoxDirect.setPosition(250, 300);
        tempCheckBoxes.add(checkBoxDirect);

        final CheckBox checkBoxGnP = new CheckBox("Grunts and Points", this.uiSkin);
        checkBoxGnP.setPosition(250, 250);
        tempCheckBoxes.add(checkBoxGnP);

        for (CheckBox checkBox : tempCheckBoxes){
            checkBox.addListener(new ChangeListener() {
                public void changed (ChangeEvent event, Actor actor) {
                    if (!traits.contains(checkBox.getText().toString())) {
                        if (traits.size() < maxTraits){
                            traits.add(checkBox.getText().toString());
                        } else {
                            checkBox.setChecked(false);
                        }
                    } else {
                        traits.remove(checkBox.getText().toString());
                    }
                    System.out.println(traits);
                }
            });

            stage.addActor(checkBox);
        }

        // Add a listener to the finish button
        finishButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                if (traits.size() == maxTraits){
                    String playerFile = costumeList[currentCostumePointer].substring(1, costumeList[currentCostumePointer].length() - 1);
                    game.generateGame(traits, playerFile);
                    game.setScreen(game.navigationScreen);
                }
            }
        });

        //add actors to the stage
        stage.addActor(titleLable);
        stage.addActor(instructionLabel);
        stage.addActor(finishButton);

        this.initCostumeSelection(stage);

        return stage;
    }

    /**
     * Initialises the skin used across the journal.
     *
     * @return The new skin.
     */
    public Skin initSkin() {
        Skin skin = new Skin(Gdx.files.internal("skins/skin_pretty/skin.json")); //load ui skin from assets
        return skin;
    }

    /**
     * Renders the character generation screen.
     */
    public void renderMain() {
        this.batch.begin();
        this.backgroundImage.draw(this.batch); //draw the journal background
        this.costumes.get(this.currentCostumePointer).draw(this.batch);
        this.batch.end();

        //draw the GUI elements
        this.stage.act();
        this.stage.draw();
    }
}
