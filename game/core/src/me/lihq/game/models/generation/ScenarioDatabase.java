package me.lihq.game.models.generation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
        boolean isMurderer;
        boolean isVictim;
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
        Random ranGen = new Random();
        try (Connection sqlConn = DriverManager.getConnection("jdbc:sqlite" + dbName)) {
            this.loadCharacters(sqlConn);
            this.loadRelations(sqlConn);

            //choosing our murderer class from the set of classes, then
            //a random victim class from the classes present in the relation
            //must take care to ensure things are truly random.

            int randomNum = ranGen.nextInt(Classes.values().length - 1);
            Classes murderClass = Classes.values()[randomNum];
            List<Classes> victimSet = this.murderVictimRelations.get(murderClass);
            Classes victimClass = victimSet.get(ranGen.nextInt(victimSet.size() - 1));
            List<DataCharacter> murdererAndVictim = this.chooseMurdererVictim(ranGen, murderClass, victimClass);
            murdererAndVictim.get(0).isMurderer = true;
            murdererAndVictim.get(1).isVictim = true;

            this.loadClue(sqlConn, murderClass, victimClass);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Classes> parseClassString(String classString) {
        List<Classes> classes = new ArrayList<>();
        String[] classStringArr = classString.split(",");
        for (String w: classStringArr) {
            classes.add(Classes.values()[Integer.getInteger(w)]);
        }
        return classes;
    }

    private void loadCharacters(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from Characters");
            while (resSet.next()) {
                DataCharacter character = new DataCharacter();
                character.id = resSet.getInt("id");
                character.name = resSet.getString("name");
                character.isMurderer = false;
                character.isVictim = false;

                String classesString = resSet.getString("classes");
                character.classes = this.parseClassString(classesString);

                this.characters.put(character.id, character);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param ranGen
     * @param mClass
     * @param vClass
     * @return arraylist whose first value is the chosen murderer, and second value is the chosen victim. we are playin' god.
     */
    private List<DataCharacter> chooseMurdererVictim(Random ranGen, Classes mClass, Classes vClass) {
        List<DataCharacter> murdererAndVictim = new ArrayList<>();
        List<DataCharacter> tempM = new ArrayList<>();
        List<DataCharacter> tempV = new ArrayList<>();
        for (DataCharacter c : this.characters.values()) {
            for (Classes cls : c.classes) {
                if (cls == mClass) {
                    tempM.add(c);
                }
                else if (cls == vClass){
                    tempV.add(c);
                }
            }
        }
        murdererAndVictim.add(tempM.get(ranGen.nextInt(tempM.size() - 1)));
        murdererAndVictim.add(tempV.get(ranGen.nextInt(tempV.size() - 1)));
        return murdererAndVictim;
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


    private void loadClue(Connection conn, Classes mClass, Classes vClass) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from Clues");
            while (resSet.next()) {
                DataClue clue = new DataClue();
                clue.classes = this.parseClassString(resSet.getString("classes"));
                if (clue.classes.contains(mClass) || clue.classes.contains(vClass)) {
                    clue.id = resSet.getInt("id");
                    clue.name = resSet.getString("name");
                    clue.storyNode = resSet.getInt("story");
                    clue.isAbstract = resSet.getBoolean("isAbstract");

                    this.clues.put(clue.id, clue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
