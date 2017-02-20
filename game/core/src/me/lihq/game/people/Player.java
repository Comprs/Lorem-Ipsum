package me.lihq.game.people;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import me.lihq.game.GameMain;
import me.lihq.game.Settings;
import me.lihq.game.ScoreTracker;
import me.lihq.game.models.Clue;
import me.lihq.game.models.Room;
import me.lihq.game.screen.elements.SpeechBox;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the player that the person playing the game will be represented by.
 */
public class Player extends AbstractPerson {
    /**
     * This object stores the clues that the player has picked up
     */
    public List<Clue> collectedClues = new ArrayList<>();

    /**
     * This stores whether the player is in the middle of a conversation or not
     */
    public boolean inConversation = false;

    // The personality will be a percent score (0-100) 0 being angry, 50 being neutral, and 100
    // being happy/nice.
    private int personalityLevel = 50;

    /**
     * This is the constructor for player, it creates a new playable person.
     *
     * @param name The name for the new player.
     * @param imgSrc The image used to represent it.
     * @param tileX The spritesheet x location.
     * @param tileY The spritesheet y location.
     */
    public Player(String name, String imgSrc, int tileX, int tileY) {
        super(name, "people/player/sprite_sets/" + imgSrc, tileX, tileY);
        animTime = Settings.TPS / 7f;
    }

    /**
     * This method will change the players personality by the given amount.
     * <p>
     * It will cap the personality between 0 and 100. If the change takes it out of these bounds,
     * it will change it to the min or max.
     * </p>
     *
     * @param change The amount to change by, can be positive or negative.
     */
    public void addToPersonality(int change) {
        personalityLevel = personalityLevel + change;

        if (personalityLevel < 0) {
            personalityLevel = 0;
        } else if (personalityLevel > 100) {
            personalityLevel = 100;
        }
    }


    /**
     * This Moves the player to a new tile.
     *
     * @param dir The direction that the player should move in.
     */
    public void move(Direction dir) {
        if (this.state != PersonState.STANDING) {
            return;
        }

        if (!canMove) {
            return;
        }

        if (this.isOnTriggerTile() && dir.toString().equals(getRoom().getMatRotation(this.tileCoordinates.x, this.tileCoordinates.y))) {
            GameMain.me.getNavigationScreen().initialiseRoomChange();
            return;
        }

        if (!getRoom().isWalkableTile(this.tileCoordinates.x + dir.getDx(), this.tileCoordinates.y + dir.getDy())) {
            setDirection(dir);
            return;
        }

        initialiseMove(dir);
    }

    /**
     * This method is called when the player interacts with the map.
     *
     * @param scoreTracker The score tracker to manipulate.
     */
    public void interact(ScoreTracker scoreTracker) {
        if (inConversation) return;

        NPC npc = getFacingNPC();
        if (npc != null) {
            GameMain.me.getNavigationScreen().convMngt.startConversation(npc);
        } else {
            checkForClue(scoreTracker);
        }
    }

    /**
     * This method tries to get an NPC if the player is facing one.
     *
     * @return The npc the player is facing. It is null if such does not exist.
     */
    private NPC getFacingNPC() {
        for (NPC npc : GameMain.me.getNPCS(getRoom())) {
            if ((npc.getTileCoordinates().x == getTileCoordinates().x + getDirection().getDx()) && (npc.getTileCoordinates().y == getTileCoordinates().y + getDirection().getDy())) {
                if (npc.getState() != PersonState.STANDING) return null;

                return npc;
            }
        }

        return null;
    }

    // This method checks to see if the tile the player is facing has a clue hidden in it or not.
    private void checkForClue(ScoreTracker scoreTracker) {
        int x = getTileCoordinates().x + getDirection().getDx();
        int y = getTileCoordinates().y + getDirection().getDy();


        if (!this.getRoom().isHidingPlace(x, y)) {
            return;
        }

        Clue clueFound = getRoom().getClue(x, y);
        if (clueFound != null) {
            GameMain.me.getNavigationScreen().speechboxMngr.addSpeechBox(new SpeechBox("You found: " + clueFound.getDescription(), 6));
            scoreTracker.addClue();
            this.collectedClues.add(clueFound);
        } else {
            GameMain.me.getNavigationScreen().speechboxMngr.addSpeechBox(new SpeechBox("Sorry no clue here", 1));
        }
    }

    /**
     * This method returns whether or not the player is standing on a tile that initiates a
     * Transition to another room.
     *
     * @return Whether the player is on a trigger tile or not.
     */
    public boolean isOnTriggerTile() {
        return this.getRoom().isTriggerTile(this.tileCoordinates.x, this.tileCoordinates.y);
    }

    /**
     * Getter for personality, it uses the personalityLevel of the player and thus returns either
     * AGGRESSIVE, NEUTRAL or NICE.
     *
     * @return Returns the personality of this player.
     */
    @Override
    public Personality getPersonality() {
        if (personalityLevel < 33) {
            return Personality.AGGRESSIVE;

        } else if (personalityLevel < 66) {
            return Personality.NEUTRAL;

        } else if (personalityLevel <= 100) {
            return Personality.NICE;
        }
        return Personality.NEUTRAL;
    }

    /**
     * This gets the players personality level; this similar to Personality but a integer
     * representation.
     *
     * @return Value between 0-100.
     */
    public int getPersonalityLevel() {
        return this.personalityLevel;
    }


    /**
     * This takes the player at its current position, and automatically gets the transition data
     * for the next room and applies it to the player and game.
     */
    public void moveRoom() {
        if (isOnTriggerTile()) {
            Room.Transition newRoomData = this.getRoom().getTransitionData(this.getTileCoordinates().x, this.getTileCoordinates().y);

            this.setRoom(newRoomData.getNewRoom());


            if (newRoomData.newDirection != null) {
                this.setDirection(newRoomData.newDirection);
                this.updateTextureRegion();
            }

            this.setTileCoordinates(newRoomData.newTileCoordinates.x, newRoomData.newTileCoordinates.y);

            //TODO: Look into making a getter for the players Game this way we can do this.getGame() here instead of GameMain.

            GameMain.me.navigationScreen.updateTiledMapRenderer();
        }
    }
}
