package me.lihq.game;

import me.lihq.game.models.Clue;
import me.lihq.game.people.AbstractPerson.Personality;
import me.lihq.game.people.NPC;
import me.lihq.game.people.Player;
import me.lihq.game.screen.elements.SpeechBox;
import me.lihq.game.screen.elements.SpeechBoxButton;

import java.util.ArrayList;

/**
 * This class controls conversation flow between Player and NPCs
 */
public class ConversationManagement
{
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
     * This method starts a conversation with the specified NPC
     *
     * @param npc - The NPC to have a conversation with
     */
    public void startConversation(NPC npc)
    {
        this.tempCluePos = -1;
        this.tempQuestionStyle = null;
        this.tempNPC = npc;

        npc.setDirection(player.getDirection().getOpposite());
        npc.canMove = false;
        player.canMove = false;
        player.inConversation = true;

        //Introduction
        speechboxMngr.addSpeechBox(new SpeechBox(this.player.getName(), this.player.getSpeech("Introduction"), 5));

        // If the NPC has been accused, then we can no longer interact with them. Abort the
        // conversation early and with the appropriate text.
        if (this.tempNPC.isAccused()) {
            speechboxMngr.addSpeechBox(new SpeechBox(
                "This person is no longer willing to cooperate", 5
            ));
            finishConverstation();
            return;
        }

        speechboxMngr.addSpeechBox(new SpeechBox(this.tempNPC.getName(), this.tempNPC.getSpeech("Introduction"), 5));

        queryQuestionType();
    }

    /**
     * This constructs the speech box that finds out what question the player wishes to ask the NPC
     */
    private void queryQuestionType()
    {

        ArrayList<SpeechBoxButton> buttons = new ArrayList<>();
        SpeechBoxButton.EventHandler eventHandler = (result) -> handleResponse(QuestionStage.TYPE, result);

        if (!player.collectedClues.isEmpty()) {
            // We can only ask questions if we have clues to ask about.
            buttons.add(new SpeechBoxButton("Question?", 0, eventHandler));
        } else {
            // Tell the player they need clues if they try to question without any clues.
            speechboxMngr.addSpeechBox(new SpeechBox(
                "You need to find some clues before you question a suspect", 5
            ));
        }

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
    private void queryQuestionStyle()
    {
        ArrayList<SpeechBoxButton> buttons = new ArrayList<>();
        SpeechBoxButton.EventHandler eventHandler = (result) -> handleResponse(QuestionStage.STYLE, result);

        buttons.add(new SpeechBoxButton("Nicely", 0, eventHandler));
        buttons.add(new SpeechBoxButton("Neutrally", 1, eventHandler));
        buttons.add(new SpeechBoxButton("Aggressively", 2, eventHandler));
        speechboxMngr.addSpeechBox(new SpeechBox("How do you want to ask the question?", buttons, -1));
    }

    /**
     * This constructs the speechbox that asks the player what clue they wish to ask about
     */
    private void queryWhichClue()
    {
        ArrayList<SpeechBoxButton> buttons = new ArrayList<>();
        SpeechBoxButton.EventHandler eventHandler = (result) -> {
            handleResponse(QuestionStage.CLUE, result);
        };


        int i = 0;
        for (Clue c : this.player.collectedClues) {
            buttons.add(new SpeechBoxButton(c.getName(), i, eventHandler));
            i++;
        }

        speechboxMngr.addSpeechBox(new SpeechBox("What clue do you want to ask about?", buttons, -1));
    }

    /**
     * This method initialises a questioning user interface
     */
    private void questionNPC()
    {
        speechboxMngr.addSpeechBox(new SpeechBox(player.getName(), player.getSpeech(player.collectedClues.get(tempCluePos), tempQuestionStyle), 3));
        speechboxMngr.addSpeechBox(new SpeechBox(tempNPC.getName(), tempNPC.getSpeech(player.collectedClues.get(tempCluePos), tempQuestionStyle), 3));
        this.scoreTracker.addQuestion();
        finishConverstation();
    }

    /**
     * This method initialises an accusing user interface
     */
    private void accuseNPC()
    {
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
    private void finishConverstation()
    {
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
    private void handleResponse(QuestionStage stage, int option)
    {
        speechboxMngr.removeCurrentSpeechBox();

        switch (stage) {
            case TYPE:
                switch (option) {
                    case 0:
                        queryQuestionStyle();
                        break;
                    case 1:
                        accuseNPC();
                        break;
                    case 2:
                        finishConverstation();
                        break;
                }
                break;

            case STYLE:
                this.tempQuestionStyle = convertToQuestionStyle(option);
                queryWhichClue();
                break;

            case CLUE:
                this.tempCluePos = option;
                questionNPC();
                break;
        }

    }

    /**
     * Takes an int and returns a personality style
     *
     * @param style 0 = Nice
     *              1 = Neutral
     *              2 = AGGRESSIVE
     *              default is Neutral
     * @return
     */
    private Personality convertToQuestionStyle(int style)
    {
        switch (style) {
            case 0:
                return Personality.NICE;

            case 1:
                return Personality.NEUTRAL;

            case 2:
                return Personality.AGGRESSIVE;

        }
        //defaults to Neutral
        return Personality.NEUTRAL;
    }

    /**
     * This is the enumeration for the different stages of questioning the NPC
     */
    public enum QuestionStage
    {

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
         * This stage indicates that the player has been asked what clue they want to ask about.
         */
        CLUE
    }

}
