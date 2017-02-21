package me.lihq.game;

import me.lihq.game.models.Clue;
import me.lihq.game.people.AbstractPerson.Personality;
import me.lihq.game.people.NPC;
import me.lihq.game.people.Player;
import me.lihq.game.screen.elements.SpeechBox;
import me.lihq.game.screen.elements.SpeechBoxButton;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.List;

/**
 * This class controls conversation flow between Player and NPCs
 */
public class ConversationManagement {
    /**
     * The player that will be starting the conversation
     */
    private Player player;

    /**
     * The manager for speechboxes, controls the flow of speechboxes
     */
    private SpeechboxManager speechboxMngr;

    /**
     * Stores the current NPC that is being questioned
     */
    private NPC tempNPC;

    /**
     * This stores the position of the clue in the players list for use in the questioning
     */
    private int tempCluePos;

    private ArrayList<String> tempIntents;

    private String tempCurrentIntent;

    /**
     * This stores the style of questioning for how the player wants to ask the question
     */
    private Personality tempQuestionStyle;

    // Keep a reference to the score tracker so that we may update the score.
    private ScoreTracker scoreTracker;

    /**
     * This constructs a conversation manager.
     *
     * @param player The player that will initiate the conversation.
     * @param speechboxManager The speechbox manager that displays the conversation.
     * @param scoreTracker The score tracker to update where appropriate.
     */
    public ConversationManagement(
        Player player, SpeechboxManager speechboxManager, ScoreTracker scoreTracker
    ) {
        this.player = player;
        this.speechboxMngr = speechboxManager;
        this.scoreTracker = scoreTracker;
    }


    /**
     * This is the enumeration for the different stages of questioning the NPC
     */
    public enum QuestionStage {

        /**
         * This stage indicates that the player has been asked what type of question they have asked
         * e.g. question or accuse
         */
        TYPE,

        /**
         * Thus stage means that the player has been asked the how they want to ask the question
         * e.g. nice, neutral or harsh
         */
        STYLE,

        /**
         * This stage indicates that the player has been asked what intent they want to ask.
         */
        INTENT
    }


    /**
     * This method starts a conversation with the specified NPC
     *
     * @param npc - The NPC to have a conversation with
     */
    public void startConversation(NPC npc) {
        this.tempCluePos = -1;
        this.tempQuestionStyle = null;
        this.tempNPC = npc;

        npc.setDirection(player.getDirection().getOpposite());
        npc.canMove = false;
        player.canMove = false;
        player.inConversation = true;

        //Introduction
        speechboxMngr.addSpeechBox(new SpeechBox("What can I do for you?", 3));

        // If the NPC has been accused, then we can no longer interact with them. Abort the
        // conversation early and with the appropriate text.
        if (this.tempNPC.isAccused()) {
            speechboxMngr.addSpeechBox(new SpeechBox(
                "This person is no longer willing to cooperate", 2
            ));
            finishConverstation();
            return;
        }

        queryQuestionType();
    }

    /**
     * This constructs the speech box that finds out what question the player wishes to ask the NPC
     */
    private void queryQuestionType() {

        ArrayList<SpeechBoxButton> buttons = new ArrayList<>();
        SpeechBoxButton.EventHandler eventHandler = (result) -> handleResponse(QuestionStage.TYPE, result);

        // We can only ask questions if we have clues to ask about.
        buttons.add(new SpeechBoxButton("Question?", 0, eventHandler));

        if (player.collectedClues.size() >= 4) {
            // If we have enough clues, we can accuse.
            buttons.add(new SpeechBoxButton("Accuse?", 1, eventHandler));
        }

        // We are always able to ignore the NPC.
        buttons.add(new SpeechBoxButton("Ignore", 2, eventHandler));

        // Give the player a hint.
        speechboxMngr.addSpeechBox(new SpeechBox("What do you want to do?", buttons, -1));
    }

    /**
     * This constructs the speechbox that asks the player how they wish to ask the question
     */
    private void chooseStyle() {
        ArrayList<SpeechBoxButton> buttons = new ArrayList<>();
        SpeechBoxButton.EventHandler eventHandler = (result) -> handleResponse(QuestionStage.STYLE, result);

        //generates the ordered array list of all available intents
        ArrayList<String> intents = this.tempNPC.dialogueTree.getAvailableIntentsAsString();
        //generates the ordered array list of all available styles for the current tempIntent
        ArrayList<String> styles = this.tempNPC.dialogueTree.getAvailableStyles(intents.indexOf(this.tempCurrentIntent));

        for (int i = 0; i < styles.size(); i++){
            buttons.add(new SpeechBoxButton(styles.get(i), i, eventHandler));
        }

        speechboxMngr.addSpeechBox(new SpeechBox("How do you want to ask the question?", buttons, -1));
    }

    private void initiateIntentSelection() {
        this.tempIntents = this.tempNPC.dialogueTree.getAvailableIntentsAsString();
        this.chooseQuestion();
    }

    private void chooseQuestion() {
        this.tempCurrentIntent = this.tempIntents.get(0); //get the current intent to query with the user
        this.tempIntents.remove(0); //remove the current intent from the front of the array
        this.tempIntents.add(this.tempCurrentIntent); //add the current intent to the back of the array

        ArrayList<SpeechBoxButton> buttons = new ArrayList<>();
        SpeechBoxButton.EventHandler eventHandler = (result) -> handleResponse(QuestionStage.INTENT, result);

        buttons.add(new SpeechBoxButton("Ask Question", 1, eventHandler));
        buttons.add(new SpeechBoxButton("Next Question", 0, eventHandler));

        speechboxMngr.addSpeechBox(new SpeechBox(this.tempCurrentIntent, buttons, -1));
    }

    private void getResponse(int questionStyle) {
      //generates the ordered array list of all available intents
        ArrayList<String> intents = this.tempNPC.dialogueTree.getAvailableIntentsAsString();
        int intent = intents.indexOf(this.tempCurrentIntent);
        String response = this.tempNPC.dialogueTree.selectStyledQuestion(intent, questionStyle, GameMain.me);
        speechboxMngr.addSpeechBox(new SpeechBox(response, 3));
        this.finishConverstation();
    }




    /**
     * This method initialises an accusing user interface
     */
    private void accuseNPC() {
        if (this.tempNPC.isKiller()) {
            speechboxMngr.addSpeechBox(new SpeechBox("You found the killer - well done!", -1));
            finishConverstation();
            GameMain.me.setScreen(GameMain.me.wonGameScreen);
        } else {
            this.tempNPC.accuse();
            this.scoreTracker.addIncorrectAccusation();
            speechboxMngr.addSpeechBox(new SpeechBox("They are clearly not the killer, just look at them.", 5));
            finishConverstation();
        }
    }

    /**
     * This method is called when a conversation is over to change some variables back for normal gameplay to resume
     */
    private void finishConverstation() {
        this.tempNPC.canMove = true;
        this.player.canMove = true;
        this.player.inConversation = false;
    }

    /**
     * This method is called to handle a users input
     *
     * @param stage  - The stage of the questioning process that they are currently at
     * @param option - The option chosen by the user
     */
    private void handleResponse(QuestionStage stage, int option) {
        speechboxMngr.removeCurrentSpeechBox();

        switch (stage) {
        case TYPE:
            switch (option) {
            case 0:
                initiateIntentSelection();
                break;
            case 1:
                accuseNPC();
                break;
            case 2:
                finishConverstation();
                break;
            }
            break;

        case INTENT:
            switch (option) {
            case 0:
                chooseQuestion();
                break;
            case 1:
                chooseStyle();
                break;
            }
            break;

        case STYLE:
            this.getResponse(option);
            break;
        }
    }
}

