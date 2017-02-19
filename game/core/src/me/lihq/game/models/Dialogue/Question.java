package me.lihq.game.models.Dialogue;

/**
 * Created by LJ on 16/02/2017.
 */
public class Question {
    public enum Style {
        AGGRESSIVE, PLACATING, CONVERSATIONAL, DIRECT, GRUNTSANDPOINTS
    }

    private Style style;
    private String questionText;

    public Question(Style style, String text) {
        this.style = style;
        this.questionText = text;
    }

    String getQuestionText() {
        return this.questionText;
    }

    Style getStyle() {
        return this.style;
    }
}