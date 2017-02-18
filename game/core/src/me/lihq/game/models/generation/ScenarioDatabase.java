package me.lihq.game.models.generation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.lihq.game.models.Dialogue.Question;
import me.lihq.game.models.Dialogue.ResponseIntent;
import org.sqlite.JDBC;




/**
 * Created by LJ on 17/02/2017.
 * A single class that loads data from an sqlite3 database, along with metadata required for the generation
 * of the scenario but not the game itself.
 */
public class ScenarioDatabase {
    public enum Classes {
        GOODSTUDENT,
        BADSTUDENT,
        PROGOOSE,
        ANTIGOOSE,
        PICKYEATER,
        CRAPPYBARISTA,
        ANGRYLIBRARIAN
    }

    public class DataCharacter{
        int id;
        String name;
        List<Classes> classes = new ArrayList<>();
    }

    public class DataQuestion {
        int id;
        Question.Style style;
        String questionText;
    }

    public class DataQuestionIntent {
        int id;
        List<Question> questions = new ArrayList<>();
        ResponseIntent resp;

    }

    public class DataResponseIntent {
        int id;
        List<String> responses = new ArrayList<>();
    }

    public class DataClue {
        int id;
        String name;
        List<Classes> classes = new ArrayList<>();
        int storyNode;
        boolean isAbstract;
    }

    public HashMap<Classes, List<Classes>> murderVictimRelations;
    public HashMap<Integer, DataCharacter> characters;
    public HashMap<Integer, DataQuestion> questions;
    public HashMap<Integer, DataQuestionIntent> questionIntents;
    public HashMap<Integer, DataResponseIntent> responseIntents;
    public HashMap<Integer, DataClue> clues;

    public ScenarioDatabase() {

        murderVictimRelations = new HashMap<>();
        characters = new HashMap<>();
        questions = new HashMap<>();
        questionIntents = new HashMap<>();
        responseIntents = new HashMap<>();
        clues = new HashMap<>();
    }

    public ScenarioDatabase(String dbName) {
        this();
        try (Connection sqlConn = DriverManager.getConnection("jdbc:sqlite" + dbName)) {
            this.loadCharacters(sqlConn);
            this.loadRelations(sqlConn);



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCharacters(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from Characters");
            while (resSet.next()) {
                DataCharacter character = new DataCharacter();
                character.id = resSet.getInt("id");
                character.name = resSet.getString("name");

                String classesString = resSet.getString("classes");
                String[] classesStringArr = classesString.split(",");
                for (String w: classesStringArr) {
                    character.classes.add(Classes.values()[Integer.getInteger(w)]);
                }

                this.characters.put(character.id, character);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRelations(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            int x = Classes.values().length;
            for (int counter = 0; counter < x; counter++) {
                ResultSet resSet = stmt.executeQuery("SELECT * from Relations WHERE murderClassID =" + counter);
                List<Classes> classesTemp = new ArrayList<>();
                while (resSet.next()) {
                    classesTemp.add(Classes.values()[resSet.getInt("victimClassID")]);
                }
                this.murderVictimRelations.put(Classes.values()[counter], classesTemp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadQuestion(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from Question");
            while (resSet.next()) {
                DataQuestion question = new DataQuestion();
                question.id = resSet.getInt("id");
                question.style = Question.Style.values()[resSet.getInt("style")];
                question.questionText = resSet.getString("text");

                this.questions.put(question.id, question);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadQuestionIntent(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from QuestionIntent");
            while (resSet.next()) {
                DataQuestionIntent questionIntent = new DataQuestionIntent();
                questionIntent.id = resSet.getInt("id");
                List<DataQuestion> dQs = new ArrayList<>();
                dQs.add(this.questions.get(resSet.getInt("question0")));
                dQs.add(this.questions.get(resSet.getInt("question1")));
                dQs.add(this.questions.get(resSet.getInt("question2")));
                dQs.add(this.questions.get(resSet.getInt("question3")));
                dQs.add(this.questions.get(resSet.getInt("question4")));

                for (DataQuestion dQ: dQs) {
                    questionIntent.questions.add(new Question(dQ.style, dQ.questionText));
                }
                this.questionIntents.put(questionIntent.id, questionIntent);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadResponseIntent(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from questions");
            while (resSet.next()) {

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    private void loadClue(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from questions");
            while (resSet.next()) {

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
