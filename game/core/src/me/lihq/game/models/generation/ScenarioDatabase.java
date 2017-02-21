package me.lihq.game.models.generation;

import java.sql.*;
import java.util.*;

import me.lihq.game.Assets;
import me.lihq.game.models.Clue;
import me.lihq.game.models.Dialogue.*;
import me.lihq.game.models.Room;
import me.lihq.game.models.Vector2Int;
import me.lihq.game.people.NPC;
import me.lihq.game.screen.elements.journal.Clues;


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
        HOMOCIDALLECTURER
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
    public List<Question.Style> styles;

    //lists of instantiated classes
    List<NPC> instCharacters;
    List<DialogueTree> instDialogueTree;
    List<Clue> instClues;

    public ScenarioDatabase() {
        this.murderVictimRelations = new HashMap<>();
        this.questionToResponses = new HashMap<>();
        this.characters = new HashMap<>();
        this.questions = new HashMap<>();
        this.questionIntents = new HashMap<>();
        this.responseIntents = new HashMap<>();
        this.clues = new HashMap<>();
        this.DataDialogueTrees = new ArrayList<>();
        this.styles = new ArrayList<>();

        this.instCharacters = new ArrayList<>();
        this.instDialogueTree = new ArrayList<>();
        this.instClues = new ArrayList<>();
    }

    public ScenarioDatabase(String dbName, List<String> traits) {
        this();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        Random ranGen = new Random();
        try (Connection sqlConn = DriverManager.getConnection("jdbc:sqlite:" + dbName)) {
            this.loadCharacters(sqlConn);
            this.loadRelations(sqlConn);
            this.loadStyles(traits);

            //choosing our murderer class from the set of classes, then
            //a random victim class from the classes present in the relation
            //must take care to ensure things are truly random.
            int randomNum = ranGen.nextInt(Classes.values().length);
            Classes murderClass = Classes.values()[randomNum];
            List<Classes> victimSet = this.murderVictimRelations.get(murderClass);
            Classes victimClass = victimSet.get(ranGen.nextInt(victimSet.size()));

            this.chooseMurdererVictim(ranGen, murderClass, victimClass);
            this.loadClue(sqlConn, victimClass);
            this.loadWeapon(sqlConn, murderClass);

            this.loadQuestion(sqlConn);
            this.trimQuestions();
            this.loadQuestionIntent(sqlConn);
            this.loadQuestionToResponse(sqlConn);
            this.loadResponseIntent(sqlConn);
            //currentcrasher
            this.generateTrees();
            this.trimClues();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Classes> parseClassString(String classString) {
        List<Classes> classes = new ArrayList<>();
        String[] classStringArr = classString.split(",");
        System.out.println(classString);
        for (String w: classStringArr) {
            int value = Integer.valueOf(w);
            classes.add(Classes.values()[value]);
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

    private void loadStyles(List<String> traits) {
        for(String trait: traits) {
            trait = trait.replaceAll("\\s", "");
            for (Question.Style style: Question.Style.values()) {
                if (trait.equalsIgnoreCase(style.name())) {
                    this.styles.add(style);
                }
            }
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
        this.murderer = tempM.get(ranGen.nextInt(tempM.size()));
        this.murderer.isMurderer = true;
        this.victim = tempV.get(ranGen.nextInt(tempV.size()));
        this.victim.isVictim = true;
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
                for (int counter = 0; counter < 8; counter++) {
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
                    if(tmpDataClue != null) {
                        responseIntent.clue = new Clue(tmpDataClue.name, tmpDataClue.description);
                        responseIntent.correctResponse = responseIntent.correctResponse + tmpDataClue.inDialogue;
                    }
                }
                this.responseIntents.put(responseIntent.id, responseIntent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadClue(Connection conn, Classes vClass) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet resSet = stmt.executeQuery("SELECT * from Clues");
            while (resSet.next()) {
                DataClue clue = new DataClue();
                clue.classes = this.parseClassString(resSet.getString("classes"));
                for (Classes mClass: this.murderer.classes) {
                    if (clue.classes.contains(mClass)) {
                        clue.id = resSet.getInt("id");
                        clue.name = resSet.getString("name");
                        clue.description = resSet.getString("description");
                        clue.storyNode = resSet.getInt("story");
                        clue.isAbstract = resSet.getBoolean("isAbstract");
                        clue.inDialogue = resSet.getString("inDialogue");

                        this.clues.put(clue.id, clue);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadWeapon(Connection conn, Classes mClass) {
        try (Statement stmt = conn.createStatement()) {
            int mClassID = mClass.ordinal();
            ResultSet resSet = stmt.executeQuery("SELECT * from Weapons WHERE classID = " +
                    Integer.toString(mClassID));
            Clue mClue = new Clue(resSet.getString("name"), resSet.getString("description"), Assets.getArrowDirection("NORTH"));
            this.murderWeaponClue = mClue;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //removes questions that don't have the style chosen by the player from the set of questions
    private void trimQuestions() {
        List<Integer> idsToBeRemoved = new ArrayList<>();
        for (DataQuestion q: this.questions.values()) {

            if (!this.styles.contains(q.style)) {
                idsToBeRemoved.add(q.id);
            }

        }

        for (int id: idsToBeRemoved) {
            this.questions.remove(id);
        }
    }

    private void trimClues() {
        List<Integer> cluesPerStoryNode = new ArrayList<>();
        for(int nodes = 0; nodes < 4; nodes++) {
            cluesPerStoryNode.add(0);
        }

        //for (DataClue c: this.clues.values()) {
        for (int counter = 0; counter < this.clues.values().size(); counter++) {
            List<DataClue> tempClues = new ArrayList<DataClue>(this.clues.values());
            DataClue c = tempClues.get(counter);

            int numCluesAtNode = cluesPerStoryNode.get(c.storyNode);
            if (numCluesAtNode < 3 && !c.isAbstract) {
                this.clues.remove(c.id);
            } else if (!c.isAbstract){
                cluesPerStoryNode.add(c.storyNode, numCluesAtNode + 1);
            }
        }
    }

    private void generateTrees() {
        Random ran = new Random();
        //creating the initial intents for all trees
        for(int treeIndex = 0; treeIndex < this.characters.size(); treeIndex++) {
            List<DataQuestionIntent> tmpQuestionIntents = new ArrayList<>();
            for (int intentIndex = 1; intentIndex < 4; intentIndex++) {
                tmpQuestionIntents.add(this.questionIntents.get(intentIndex));
            }
            this.DataDialogueTrees.add(new DataDialogueTree());
            this.DataDialogueTrees.get(treeIndex).questionLayers.add(tmpQuestionIntents);
            this.DataDialogueTrees.get(treeIndex).classes = this.characters.get(treeIndex+1).classes;
        }

        //remove the introductory questions so they do not repeat.
        /**
         * this.questionIntents.remove(0);
            this.questionIntents.remove(1);
            this.questionIntents.remove(2);
            this.questionIntents.remove(3);
         */


        //for each intent in each layer in each tree add a responseIntent
        //to the matching responseIntent layer
        //for each subsequent response that isn't dead, add a questionintent
        //ensure that a questionIntent within a single tree is not a duplicate
        for(DataDialogueTree tree: this.DataDialogueTrees) {
            HashMap<Integer, DataQuestionIntent> unusedQuestionIntents = (HashMap) this.questionIntents.clone();
            for(int counter = 0; counter < tree.questionLayers.size(); counter++) {
            //for(List<DataQuestionIntent> dQISet: tree.questionLayers) {
                List<DataQuestionIntent> dQISet = tree.questionLayers.get(counter);
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
                if(tmpRILayer.size()>0){
                    tree.responseLayers.add(tmpRILayer);
                }
                //if there exists any living responseIntent in the latest responseintent layer
                //add a new QuestionIntent layer and add randomly selected questionIntents.
                List<DataQuestionIntent> tmpQILayer = new ArrayList<>();
                for(DataResponseIntent dRI: tree.responseLayers.get(tree.responseLayers.size()-1)) {
                    if(!dRI.isDead) {
                        int ranQuestionIndex = ran.nextInt(unusedQuestionIntents.size());
                        tmpQILayer.add(unusedQuestionIntents.get(ranQuestionIndex));
                        unusedQuestionIntents.remove(ranQuestionIndex);
                    }
                    if(tmpQILayer.size()>0) {
                        tree.questionLayers.add(tmpQILayer);
                    }
                }
            }
        }
    }

    public void initialiseGame(List<Room> rooms, List<NPC> npcs) {
        //initialise dialogueTrees from DataDialogueTrees
        //initialise sets of clues from DataClues (ensure the isabstract field is false)
        //distribute (randomly?) across the rooms
        Random ran = new Random();

        for(DataDialogueTree dTree: this.DataDialogueTrees) {
            //has a list of quesitonintents that chain
            List<List<QuestionIntent>> tmp = new ArrayList<>();
            //get the end setlayers
            for(int layerIndex = dTree.questionLayers.size() - 1; layerIndex >= 0; layerIndex--) {
                List<DataQuestionIntent> latestQLayer = dTree.questionLayers.get(layerIndex);
                List<DataResponseIntent> latestRLayer = dTree.responseLayers.get(layerIndex);
                List<QuestionIntent> tmpQuestionIntents = new ArrayList<>();

                //for each dQuestion and dResponse in the layers, link them together
                for (int Index = 0; Index < latestQLayer.size(); Index++) {
                    QuestionIntent tmpQI = new QuestionIntent(latestQLayer.get(Index).questions,
                            latestQLayer.get(Index).description);
                    ResponseIntent tmpRI = new ResponseIntent(latestRLayer.get(Index).responses,
                            latestRLayer.get(Index).correctResponse,
                            latestRLayer.get(Index).clue);
                    tmpQI.attachResponse(tmpRI);
                    //add the QI to the list representing the current "layer"
                    tmpQuestionIntents.add(tmpQI);
                }
                //add that list to the tmp so that they can continue to be processed in the later
                tmp.add(tmpQuestionIntents);
                System.out.println(tmp.size());
            }
            //begin attaching the question/response pairs together
            for(int Index = tmp.size() - 1; Index > 0; Index--) {
                List<QuestionIntent> pairs = tmp.get(Index);
                List<QuestionIntent> successorPairs = tmp.get(Index + 1);
                int deadResponses = 0;

                for(int counter = 0; counter < pairs.size(); counter++) {
                    ResponseIntent tmpRI = pairs.get(counter).getResponseIntent();
                    if(!tmpRI.isDead()) {
                        deadResponses += 1;
                    }
                    else {
                        tmpRI.attachQuestionIntent(successorPairs.get(counter - deadResponses));
                    }
                }
            }
            DialogueTree tree = new DialogueTree(tmp.get(tmp.size()-1), this.styles);
            this.instDialogueTree.add(tree);
            //i have never before felt such elation as i had when writing this single line of code
        }

        for(int counter = 0; counter < this.instDialogueTree.size(); counter++) {
            DialogueTree dTree = this.instDialogueTree.get(counter);
            DataCharacter character = this.characters.get(counter+1);
            System.out.println("Adding NPC");
            Room thisRoom = rooms.get(ran.nextInt(rooms.size()));

            boolean positionFound = false;
            int x = 0;
            int y = 0;
            while (!positionFound){
                positionFound = thisRoom.isWalkableTile(x, y);
                x++;
                y++;
            }
            npcs.add(new NPC(character.name + ".png",x,y, thisRoom, dTree));
        }

        this.instClues.add(this.murderWeaponClue);

        for(DataClue clue: this.clues.values()) {
            this.instClues.add(new Clue(clue.name, clue.description,  Assets.getArrowDirection("NORTH")));
        }

        //ensure each room has one clue
        for(int index = 0; index < rooms.size()-1; index++) {
            Room room = rooms.get(index);
            Vector2Int randHidingSpot = room.getRandHidingSpot();
            try {
                if ((randHidingSpot.x != 0) && (randHidingSpot.y != 0)){
                    room.addClue(this.instClues.get(index).setTileCoordinates(randHidingSpot));
                }
            } catch(IndexOutOfBoundsException e) {
                System.out.println(e.toString());
            }
        }

        for(int index = rooms.size()-1;index < this.instClues.size(); index++) {
            int random = ran.nextInt(rooms.size());
            rooms.get(random).addClue(this.instClues.get(index));
        }
    }

}

