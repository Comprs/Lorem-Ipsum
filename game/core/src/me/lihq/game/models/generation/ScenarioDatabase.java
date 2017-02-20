package me.lihq.game.models.generation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.lihq.game.models.Clue;
import me.lihq.game.models.Dialogue.*;


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
        String description;
        List<Question> questions = new ArrayList<>();
    }

    public class DataResponseIntent {
        int id;
        Clue clue;
        boolean isDead;
        List<String> responses = new ArrayList<>();
        String correctResponse;
    }

    public class DataClue {
        int id;
        String name;
        String description;
        String inDialogue;
        List<Classes> classes = new ArrayList<>();
        int storyNode;
        boolean isAbstract;
    }

    public class DataDialogueTree {
        List<Classes> classes;
        //sets of questionIntents and ResponseIntents at each "layer" of the tree
        List<List<DataQuestionIntent>> questionLayers = new ArrayList<>();
        List<List<DataResponseIntent>> responseLayers = new ArrayList<>();
    }

    public DataCharacter murderer;
    public DataCharacter victim;
    public HashMap<Classes, List<Classes>> murderVictimRelations;
    public HashMap<Integer, List<Integer>> questionToResponses;
    public HashMap<Integer, DataCharacter> characters;
    public HashMap<Integer, DataQuestion> questions;
    public HashMap<Integer, DataQuestionIntent> questionIntents;
    public HashMap<Integer, DataResponseIntent> responseIntents;
    public HashMap<Integer, DataClue> clues;
    public Clue murderWeaponClue;
    public List<DataDialogueTree> DataDialogueTrees;

    public ScenarioDatabase() {
        this.murderVictimRelations = new HashMap<>();
        this.questionToResponses = new HashMap<>();
        this.characters = new HashMap<>();
        this.questions = new HashMap<>();
        this.questionIntents = new HashMap<>();
        this.responseIntents = new HashMap<>();
        this.clues = new HashMap<>();
        this.DataDialogueTrees = new ArrayList<>();


    }

    public ScenarioDatabase(String dbName, List<String> traits) {
        this();
        Random ranGen = new Random();
        try (Connection sqlConn = DriverManager.getConnection("jdbc:sqlite" + dbName)) {
            this.loadCharacters(sqlConn);
            this.loadRelations(sqlConn);

            //choosing our murderer class from the set of classes, then
            //a random victim class from the classes present in the relation
            //must take care to ensure things are truly random.
<<<<<<< HEAD
            int randomNum = ranGen.nextInt(Classes.values().length);
            Classes murderClass = Classes.values()[randomNum];
            List<Classes> victimSet = this.murderVictimRelations.get(murderClass);
            Classes victimClass = victimSet.get(ranGen.nextInt(victimSet.size()));

            this.chooseMurdererVictim(ranGen, murderClass, victimClass);
            this.loadClue(sqlConn, murderClass, victimClass);
            this.loadWeapon(sqlConn, murderClass);

            this.loadQuestion(sqlConn);
            this.trimQuestions(traits);
            this.loadQuestionIntent(sqlConn);
            this.loadQuestionToResponse(sqlConn);
            this.loadResponseIntent(sqlConn);
=======

            int randomNum = ranGen.nextInt(Classes.values().length - 1);
            Classes murderClass = Classes.values()[randomNum];
            List<Classes> victimSet = this.murderVictimRelations.get(murderClass);
            Classes victimClass = victimSet.get(ranGen.nextInt(victimSet.size() - 1));
            List<DataCharacter> murdererAndVictim = this.chooseMurdererVictim(ranGen, murderClass, victimClass);
            murdererAndVictim.get(0).isMurderer = true;
            murdererAndVictim.get(1).isVictim = true;

            this.loadClue(sqlConn, murderClass, victimClass);
>>>>>>> 30f1022... most of the generation loading from database complete.


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

<<<<<<< HEAD
    private void chooseMurdererVictim(Random ranGen, Classes mClass, Classes vClass) {
=======
    /**
     *
     * @param ranGen
     * @param mClass
     * @param vClass
     * @return arraylist whose first value is the chosen murderer, and second value is the chosen victim. we are playin' god.
     */
    private List<DataCharacter> chooseMurdererVictim(Random ranGen, Classes mClass, Classes vClass) {
        List<DataCharacter> murdererAndVictim = new ArrayList<>();
>>>>>>> 30f1022... most of the generation loading from database complete.
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
<<<<<<< HEAD
        this.murderer = tempM.get(ranGen.nextInt(tempM.size()));
        this.murderer.isMurderer = true;
        this.victim = tempV.get(ranGen.nextInt(tempV.size()));
        this.victim.isVictim = true;
=======
        murdererAndVictim.add(tempM.get(ranGen.nextInt(tempM.size() - 1)));
        murdererAndVictim.add(tempV.get(ranGen.nextInt(tempV.size() - 1)));
        return murdererAndVictim;
>>>>>>> 30f1022... most of the generation loading from database complete.
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
                    int tmpVal = resSet.getInt("responseIntent" + Integer.toString(counter));
                    if(!resSet.wasNull())
                    tmp.add(tmpVal);
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
                questionIntent.description = resSet.getString("description");
                List<DataQuestion> dQs = new ArrayList<>();
                for (int counter = 0; counter < 5; counter++) {
                    DataQuestion tmpDQ = this.questions.get(resSet.getInt("question" + Integer.toString(counter)));
                    if (tmpDQ != null) {
                        dQs.add(tmpDQ);
                    }
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
                responseIntent.isDead = resSet.getBoolean("isDead");
                responseIntent.correctResponse = resSet.getString("response0");
                for (int counter = 1; counter < 3; counter++) {
                    responseIntent.responses.add(resSet.getString("response" + Integer.toString(counter)));
                }


                int tmpClueID = resSet.getInt("clue");
                if (!resSet.wasNull()) {
                    DataClue tmpDataClue = this.clues.get(tmpClueID);
                    for (Classes c: tmpDataClue.classes) {
                        for (Classes mClass: this.murderer.classes) {
                            if(c == mClass) {
                                responseIntent.clue = new Clue(tmpDataClue.name, tmpDataClue.description);
                            }
                        }
                    }

                }
                this.responseIntents.put(responseIntent.id, responseIntent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
=======

>>>>>>> 30f1022... most of the generation loading from database complete.
    private void loadClue(Connection conn, Classes mClass, Classes vClass) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from Clues");
            while (resSet.next()) {
                DataClue clue = new DataClue();
                clue.classes = this.parseClassString(resSet.getString("classes"));
                if (clue.classes.contains(mClass) || clue.classes.contains(vClass)) {
                    clue.id = resSet.getInt("id");
                    clue.name = resSet.getString("name");
<<<<<<< HEAD
                    clue.description = resSet.getString("description");
                    clue.storyNode = resSet.getInt("story");
                    clue.isAbstract = resSet.getBoolean("isAbstract");
                    clue.inDialogue = resSet.getString("inDialogue");
=======
                    clue.storyNode = resSet.getInt("story");
                    clue.isAbstract = resSet.getBoolean("isAbstract");
>>>>>>> 30f1022... most of the generation loading from database complete.

                    this.clues.put(clue.id, clue);
                }
            }
<<<<<<< HEAD
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadWeapon(Connection conn, Classes mClass) {
        try (Statement stmt = conn.createStatement()) {
            int mClassID = mClass.ordinal();
            ResultSet resSet = stmt.executeQuery("SELECT * from Weapons WHERE classID = " +
                    Integer.toString(mClassID));
            Clue mClue = new Clue(resSet.getString("name"), resSet.getString("description"));
            this.murderWeaponClue = mClue;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //removes questions that don't have the style chosen by the player from the set of questions
    private void trimQuestions(List<String> traits) {
        for (DataQuestion q: this.questions.values()) {
            for (String trait: traits) {
                if (!trait.equalsIgnoreCase(q.style.name())) {
                    this.questions.remove(q.id);
                }
            }
        }
    }

    private void generateTrees() {
        Random ran = new Random();
        //creating the initial intents for all trees
        for(int treeIndex = 0; treeIndex < 10; treeIndex++) {
            List<DataQuestionIntent> tmpQuestionIntents = new ArrayList<>();
            for (int intentIndex = 0; intentIndex < 4; intentIndex++) {
                tmpQuestionIntents.add(this.questionIntents.get(intentIndex));
            }
            this.DataDialogueTrees.add(new DataDialogueTree());
            this.DataDialogueTrees.get(treeIndex).questionLayers.add(tmpQuestionIntents);
            this.DataDialogueTrees.get(treeIndex).classes = this.characters.get(treeIndex).classes;
        }

        //for each intent in each layer in each tree add a responseIntent
        //to the matching responseIntent layer
        //for each subsequent response that isn't dead, add a questionintent
        //ensure that a questionIntent within a single tree is not a duplicate
        for(DataDialogueTree tree: this.DataDialogueTrees) {
            HashMap<Integer, DataQuestionIntent> unusedQuestionIntents = (HashMap) this.questionIntents.clone();
            for(List<DataQuestionIntent> dQISet: tree.questionLayers) {
                List<DataResponseIntent> tmpRILayer = new ArrayList<>();
                //for every dqi in the current layer of the current tree generate a RI
                for (DataQuestionIntent dQI: dQISet) {
                    //select a random class from the dialoguetree
                    Classes ranClass = tree.classes.get(ran.nextInt(tree.classes.size()));
                    //get the set of possible responseIntents for the current questionIntent
                    List<Integer> respSetID = this.questionToResponses.get(dQI.id);
                    //select a responseIntent based on a randomly selected class of our DT/character
                    tmpRILayer.add(this.responseIntents.get(respSetID.get(ranClass.ordinal())));
                }
                tree.responseLayers.add(tmpRILayer);
                //if there exists any living responseIntent in the latest responseintent layer
                //add a new QuestionIntent layer and add randomly selected questionIntents.
                List<DataQuestionIntent> tmpQILayer = new ArrayList<>();
                for(DataResponseIntent dRI: tree.responseLayers.get(tree.responseLayers.size()-1)) {
                    if(!dRI.isDead) {
                        int ranQuestionIndex = ran.nextInt(unusedQuestionIntents.size());
                        tmpQILayer.add(unusedQuestionIntents.get(ranQuestionIndex));
                        unusedQuestionIntents.remove(ranQuestionIndex);
                    }
                    tree.questionLayers.add(tmpQILayer);
                }
            }
        }
=======
        } catch (SQLException e) {
            e.printStackTrace();
        }
>>>>>>> 30f1022... most of the generation loading from database complete.
    }
}
