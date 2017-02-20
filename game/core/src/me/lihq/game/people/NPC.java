package me.lihq.game.people;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import me.lihq.game.models.Clue;
import me.lihq.game.models.Room;
import me.lihq.game.models.Dialogue.DialogueTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The class which is responsible for the non-playable characters within the game that the player
 * will meet.
 */
public class NPC extends AbstractPerson
{
    /**
     * Associated clues.
     */
    public List<Clue> associatedClues = new ArrayList<>();

    private Random random;

    // The motive string details why the NPC committed the murder.
    private String motive = "";

    // These two booleans decide whether an NPC has the potential to be a killer and if, in this
    // particular game, they are the killer.
    private boolean canBeKiller = false;
    private boolean isKiller = false;
    private boolean isVictim = false;

    public DialogueTree dialogueTree;

    // This indicates whether the NPC has been accused.
    private boolean hasBeenAccused = false;

    // This stores the players personality {@link me.lihq.game.people.AbstractPerson.Personality}
    private Personality personality;

    /**
     * Define an NPC with location coordinates , room, spritesheet and whether or not they can be the killer
     *
     * @param name The name of the NPC
     * @param tileX x coordinate of tile that the NPC will be initially rendered on.
     * @param tileY y coordinate of tile that the NPC will be initially rendered on.
     * @param room ID of room they are in.
     * @param dialogueTree The dialogue tree.
     */
    public NPC(
        String name,
        int tileX,
        int tileY,
        Room room,
        DialogueTree dialogueTree
    ) {
        super(name, "people/NPCs/" + name, tileX, tileY);
        this.setRoom(room);
        this.random = new Random();
        this.dialogueTree = dialogueTree;
    }

    /**
     * This method is called once a game tick to randomise movement.
     */
    @Override
    public void update() {
        super.update();
        this.randomMove();
    }


    /**
     * Allow the NPC to move around their room.
     *
     * @param dir the direction person should move in.
     */
    public void move(Direction dir) {
        if (this.state != PersonState.STANDING) {
            return;
        }

        if (!canMove) return;

        if (!getRoom().isWalkableTile(this.tileCoordinates.x + dir.getDx(), this.tileCoordinates.y + dir.getDy())) {
            setDirection(dir);
            return;
        }

        initialiseMove(dir);
    }

    // This method attempts to move the NPC in a random direction.
    private void randomMove() {
        if (getState() == PersonState.WALKING) return;

        if (random.nextDouble() > 0.01) {
            return;
        }

        Direction dir;

        Double dirRand = random.nextDouble();
        if (dirRand < 0.5) {
            dir = this.direction;
        } else if (dirRand < 0.62) {
            dir = Direction.NORTH;
        } else if (dirRand < 0.74) {
            dir = Direction.SOUTH;
        } else if (dirRand < 0.86) {
            dir = Direction.EAST;
        } else {
            dir = Direction.WEST;
        }

        move(dir);
    }


    /**
     * Getter for canBeKiller
     *
     * @return Returns value of canBeKiller for this object. {@link #canBeKiller}
     */
    public boolean canBeKiller() {
        return canBeKiller;
    }

    /**
     * Getter for isKiller.
     *
     * @return Return a value of isKiller for this object. {@link #isKiller}
     */
    public boolean isKiller() {
        return isKiller;
    }

    /**
     * Getter for isVictim
     *
     * @return Returns the value of isVictim for this object {@link #isVictim}
     */
    public boolean isVictim() {
        return isVictim;
    }

    /**
     * Getter for motive.
     *
     * @return Returns the motive string for this object. {@link #motive}
     */
    public String getMotive() {
        return motive;
    }

    /**
     * Setter for the NPC's motive string.
     *
     * @param motive The motive this particular NPC has for committing the murder.
     * @return Returns the NPC object.
     */
    public NPC setMotive(String motive) {
        this.motive = motive;
        return this;
    }

    /**
     * This method sets the NPC as the killer for this game.
     * <p>
     * It first checks they aren't the victim and if they can be the killer.
     * </p>
     *
     * @return Returns whether it successfully set the NPC to the killer or not.
     */
    public boolean setKiller() {
        if (isVictim() || !canBeKiller) return false;

        isKiller = true;
        System.out.println(getName() + " is the killer");
        return true;
    }

    /**
     * This method sets the NPC to be the victim for the game.
     * <p>
     * It first checks if the NPC isn't also the killer.
     * </p>
     *
     * @return Returns whether it successfully set the NPC to the victim or not.
     */
    public boolean setVictim() {
        if (isKiller()) return false;

        isVictim = true;
        System.out.println(getName() + " is the victim");
        return true;
    }


    /**
     * This handles speech for a clue that has a question style.
     *
     * @param clue The clue to be questioned about.
     * @param style The style of questioning.
     * @return The speech.
     */
    public String getSpeech(Clue clue, Personality style) {
        return null;
    }

    /**
     * This method returns the NPCs personality.
     *
     * @return The NPCs personality {@link me.lihq.game.people.AbstractPerson.Personality}
     */
    @Override
    public Personality getPersonality() {
        return this.personality;
    }

    /**
     * Sets the accuse state of the NPC.
     */
    public void accuse() {
        this.hasBeenAccused = true;
    }

    /**
     * Get whether the NPC has been accused.
     *
     * @return Whether the NPC has been accused.
     */
    public boolean isAccused() {
        return this.hasBeenAccused;
    }
}
