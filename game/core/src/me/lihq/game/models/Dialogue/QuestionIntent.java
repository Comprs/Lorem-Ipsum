package me.lihq.game.models.Dialogue;

/**
 * Created by LJ on 16/02/2017.
 */
import java.util.ArrayList;

public class QuestionIntent {
    private ArrayList<Question> questions;
    private ResponseIntent response;
    //string used when displaying the question intents to choose from
    private String description;

    QuestionIntent(ArrayList<Question> questions, ResponseIntent resp, String description) {
        this.questions = questions;
        this.response = resp;
        this.description = description;
    }

    QuestionIntent(ArrayList<Question> questions, String description) {
        this.questions = questions;
        this.description = description;
    }

    void attachResponse(ResponseIntent resp) { this.response = resp; }

    String getDescription(){
        return this.description;
    }

    ArrayList<Question> getStyleChoices(){
        return this.questions;
    }

    ResponseIntent getResponseIntent(){
        return this.response;
    }



}