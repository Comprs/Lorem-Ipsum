package me.lihq.game.models.Dialogue;

/**
 * Created by LJ on 16/02/2017.
 */
import java.util.ArrayList;

import me.lihq.game.GameMain;
import me.lihq.game.models.Dialogue.Question.Style;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Holds questionintents for a character and has convenience methods to navigate the resulting tree.
 *
 * @author Jacob Wunwin
 */

public class DialogueTree {
    private HashMap<Style, Mood> mapStyleMood = new HashMap<>();
    private Mood mood;
    private boolean hasBeenWrong = false;
    List<QuestionIntent> questions;

    public enum Mood {
        NERVOUS,
        RELAXED,
        AGGRESSIVE,
    }

    /**
     * Initializer function.
     */
    public DialogueTree(List<QuestionIntent> questions, List<Style> style){
        this.questions = questions;
        this.mood = Mood.RELAXED;
        for (int counter = 0; counter < 3; counter++) {
            this.mapStyleMood.put(style.get(counter), Mood.values()[counter]);
        }

    }

    /**
     * Returns a new Dialogue from the given intention and style.
     *
     * @param intention The id of the intention of the question
     * @param style The id of the style of the question
     * @return The response dialogue
     */

    /**
     * gives a preview of the question intents you can use (player selects one, then selects a style)
     * @return
     */
    public ArrayList<String> getAvailableIntentsAsString(){
        ArrayList<String> intents = new ArrayList<>();
        for (QuestionIntent questionIntent : this.questions){
            intents.add(questionIntent.getDescription());
        }
        return intents;
    }

    List<QuestionIntent> getAvailableIntents(){
        return this.questions;
    }

    /**
     *
     * given a QuestionIntent, return the available styled questions as an arraylist of strings
     * @return
     */
    public ArrayList<String> getAvailableStyles(int intentSelection){
        ArrayList<String> styledQuestions = new ArrayList<>();
        for (Question styledQuestion : this.questions.get(intentSelection).getStyleChoices()){
            styledQuestions.add(styledQuestion.getStyle().name());
        }
        return styledQuestions;
    }

    /**
     *
     * @param intentSelection - selects intent
     * @param styleSelection - selects questionstyle
     * if the correct choice is made, an optimal response is given, a new intent is added to the tree,
     * and a clue is yielded, otherwise a bad response and no progressing is given.
     * @return
     */
    public String selectStyledQuestion(int intentSelection, int styleSelection, GameMain gameMain){
        ResponseIntent respInt = this.questions.get(intentSelection).getResponseIntent();
        Style style = this.questions.get(intentSelection).getStyleChoices().get(styleSelection).getStyle();
        this.questions.remove(intentSelection);

        if (this.mapStyleMood.get(style) == this.mood) {
            if(!respInt.isDead()){
                this.questions.add(respInt.getQuestionIntent());
            }
            gameMain.player.collectedClues.add(respInt.getClue());
            //add the clues to the player.collected clues.
            return respInt.getCorrectResponse();
        }
        else {
            Random rGen = new Random();
            if (this.hasBeenWrong) {
                this.mood = Mood.values()[rGen.nextInt(3)];
                this.hasBeenWrong = false;
            } else {
                this.hasBeenWrong = true;
            }
            return respInt.responses.get(this.mood.ordinal());
        }
    }
}