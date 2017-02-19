package me.lihq.game.models.generation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.lihq.game.models.Clue;
import me.lihq.game.models.Dialogue.Question;
import me.lihq.game.models.Dialogue.QuestionIntent;
import me.lihq.game.models.Dialogue.ResponseIntent;
import org.sqlite.JDBC;




/**
 * Created by LJ on 17/02/2017.
 * A single class that loads data from an sqlite3 database, along with metadata required for the generation
 * of the scenario but not the game itself.
 *The load functions are concerned with loading in the correct data from tables
 * while the build functions are used to manage the randomness
 */
public class ScenarioDatabase {
    public enum Classes {
        GOODSTUDENT,
        BADSTUDENT,
        PROGOOSE,
        ANTIGOOSE,
        PICKYEATER,
        CRAPPYBARISTA,
        ANGRYLIBRARIAN,
        TBA
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
        Clue clue;
        List<String> responses = new ArrayList<>();
    }

    public class DataClue {
        int id;
        String name;
        String description;
        List<Classes> classes = new ArrayList<>();
        int storyNode;
        boolean isAbstract;
    }


    public HashMap<Classes, List<Classes>> murderVictimRelations;
    public HashMap<Integer, List<Integer>> questionToResponses;
    public HashMap<Integer, DataCharacter> characters;
    public HashMap<Integer, DataQuestion> questions;
    public HashMap<Integer, DataQuestionIntent> questionIntents;
    public HashMap<Integer, DataResponseIntent> responseIntents;
    public HashMap<Integer, DataClue> clues;

    public ScenarioDatabase() {
        murderVictimRelations = new HashMap<>();
        questionToResponses = new HashMap<>();
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

            this.chooseMurdererVictim(ranGen, murderClass, victimClass);
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

    private void chooseMurdererVictim(Random ranGen, Classes mClass, Classes vClass) {
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
        tempM.get(ranGen.nextInt(tempM.size() - 1)).isMurderer = true;
        tempV.get(ranGen.nextInt(tempV.size() - 1)).isVictim = true;
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

    private void loadQuestionToResponse(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from QuestionToResponse");
            while(resSet.next()) {
                List<Integer> tmp = new ArrayList<>();
                for (int counter = 0; counter < 9; counter++) {
                    tmp.add(resSet.getInt("responseIntent" + Integer.toString(counter)));
                    //handle arraylist exceptions
                }
                this.questionToResponses.put(resSet.getInt("id"), tmp);
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
                for (int counter = 0; counter < 5; counter++) {
                    dQs.add(this.questions.get(resSet.getInt("question" + Integer.toString(counter))));
                }
                for (DataQuestion dQ: dQs) {
                    questionIntent.questions.add(new Question(dQ.style, dQ.questionText));
                }
                this.questionIntents.put(questionIntent.id, questionIntent);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //loads in only the necessary data: i.e. the questionIntent is used to determine tree structure and is not loaded
    private void loadResponseIntent(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from ResponseIntent");
            while (resSet.next()) {
                DataResponseIntent responseIntent = new DataResponseIntent();
                responseIntent.id = resSet.getInt("id");
                try {
                    DataClue tmpDataClue = this.clues.get(resSet.getInt("clue"));
                    responseIntent.clue = new Clue(tmpDataClue.name, tmpDataClue.description);
                } catch (SQLException e) {
                    //no clue present for this responseintent in the database
                }
                for (int counter = 0; counter < 3; counter++) {
                    responseIntent.responses.add(resSet.getString("response" + Integer.toString(counter)));
                }

                this.responseIntents.put(responseIntent.id, responseIntent);
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
                    clue.description = resSet.getString("description");
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
