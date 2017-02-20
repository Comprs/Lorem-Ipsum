import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import me.lihq.game.ScoreTracker;

public class ScoreTrackerTests {
    public static final ScoreTracker.ScoreCalculator simpleCal = (i, j, k, l) -> {
        return (int)i + j + k + l;
    };

    // These test cases test the general functionality of the score tracker.
    @Test
    public void testCase0() {
        final ScoreTracker sc = new ScoreTracker(1000);
        final int calScore = sc.collectScore(1000, this.simpleCal);
        assertEquals(calScore, 0);
    }

    @Test
    public void testCase1() {
        final ScoreTracker sc = new ScoreTracker(1000);

        sc.addIncorrectAccusation();
        sc.addIncorrectAccusation();
        sc.addQuestion();
        sc.addQuestion();
        sc.addQuestion();
        sc.addQuestion();
        sc.addQuestion();
        sc.addClue();

        final int calScore = sc.collectScore(2000, this.simpleCal);
        assertEquals(calScore, 1008);
    }

    // These test cases test some of the time functionality. This only includes checking that the
    // clock won't go backwards. The case of an overflow is not checked.
    @Test
    public void testTimePlus0() {
        final ScoreTracker sc = new ScoreTracker();
        int lastNumb = 0;

        for (int i = 0; i < 1000; ++i) {
            final int calScore = sc.collectScore(this.simpleCal);
            assertTrue(calScore - lastNumb >= 0);
            lastNumb = calScore;
        }
    }

    @Test
    public void testTimePlus1() {
        for (int i = 0; i < 1000; ++i) {
            final ScoreTracker sc = new ScoreTracker();
            final int calScore = sc.collectScore(this.simpleCal);
            assertTrue(calScore >= 0);
        }
    }
}
