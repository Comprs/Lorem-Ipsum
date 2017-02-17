package me.lihq.game;

public class ScoreTracker {
    /**
     * This will be the interface to a score calculator object.
     */
    @FunctionalInterface
    public static interface ScoreCalculator {
        /**
         * Calculate the score with the given values.
         *
         * @param timeTake The time taken so far in the game in milliseconds.
         * @param incorrectAccusations The number of incorrect accusations made so far.
         * @param askedQuestions The number of questions asked so far.
         * @param cluesFound The number of clues found so far.
         * @return The score.
         */
        public int calculateScore(
            long timeTaken, int incorrectAccusations, int askedQuestions, int cluesFound
        );
    }

    // The time is dumbly calculated by taking the difference between the UNIX time at the start of
    // the game and at the end of the game. This does not take into account the game pausing and
    // uses a simplistic approach to time.
    private long startTime;

    private int incorrectAccusations;

    private int askedQuestions;

    private int cluesFound;

    /**
     * Construct a score tracker where the start time is given.
     *
     * @param startTime The time stamp of the time the game started at.
     */
    public ScoreTracker(long startTime) {
        this.startTime = startTime;
        this.incorrectAccusations = 0;
        this.askedQuestions = 0;
        this.cluesFound = 0;
    }

    /**
     * Construct a score tracker with all values set to an initial state.
     */
    public ScoreTracker() {
        this(System.currentTimeMillis());
    }

    /**
     * Indicate to the score tracker that an incorrect accusation has happened.
     */
    public void addIncorrectAccusation() {
        ++this.incorrectAccusations;
    }

    /**
     * Indicate to the score tracker that a question has been asked.
     */
    public void addQuestion() {
        ++this.askedQuestions;
    }

    /**
     * Indicate to the score tracker that a clue has been found.
     */
    public void addClue() {
        ++this.cluesFound;
    }

    /**
     * Combine all of the parameters into a single score.
     *
     * @param currentTime The end time to use to calculate the total time.
     * @param scoreCalculator The score calculator to use.
     * @return The resulting score.
     */
    public int collectScore(long currentTime, ScoreCalculator scoreCalculator) {
        return scoreCalculator.calculateScore(
            currentTime - this.startTime,
            this.incorrectAccusations,
            this.askedQuestions,
            this.cluesFound
        );
    }

    /**
     * Combine all of the parameters into a single score.
     * <p>
     * This method assumes that the game has ended the instant the method was called.
     * </p>
     *
     * @param scoreCalculator The score calculator to use.
     * @return The resulting score.
     */
    public int collectScore(ScoreCalculator scoreCalculator) {
        return this.collectScore(System.currentTimeMillis(), scoreCalculator);
    }
}
