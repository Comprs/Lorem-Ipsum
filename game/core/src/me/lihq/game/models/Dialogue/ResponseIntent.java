package me.lihq.game.models.Dialogue;

/**
 * Created by LJ on 16/02/2017.
 */
import java.util.ArrayList;
import java.util.List;

import me.lihq.game.models.Clue;

public class ResponseIntent {
    List<String> responses;
    private String correctResponse;
    private Clue clue;
    private QuestionIntent newQuestion;
    private boolean isDead;

    public ResponseIntent(List<String> responses, String correctResponse, Clue clue, QuestionIntent newQ){
        this.responses = responses;
        this.correctResponse = correctResponse;
        this.clue = clue;
        this.newQuestion = newQ;
        this.isDead = false;
    }

    public ResponseIntent(List<String> responses, String correctResponse, Clue clue){
        this.responses = responses;
        this.correctResponse = correctResponse;
        this.clue = clue;
        this.newQuestion = null;
        this.isDead = true;
    }

    public void attachQuestionIntent(QuestionIntent questionIntent) {
        this.newQuestion = questionIntent;
    }


    QuestionIntent getQuestionIntent(){
        return this.newQuestion;
    }

    Clue getClue(){
        return this.clue;
    }

    String getCorrectResponse() {
        return this.correctResponse;
    }

    public boolean isDead(){
        return this.isDead;
    }
}