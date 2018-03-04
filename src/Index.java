import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

public class Index {
    //Map<Term, HashMap<DocId, ArrayList<Positions>>>
    private HashMap<String, HashMap<String, ArrayList<Integer>>> dataStorage;
    private HashMap<String, ArrayList<Integer>> All_Scenes;
    private HashMap<String, String[]> sceneText;

    private Index() {
        dataStorage = new HashMap<>();
        sceneText = new HashMap<>();
        All_Scenes = new HashMap<>();
    }

    public static void main(String[] args) throws IOException, ParseException {
        Index index = new Index();
        HashMap dataStorage = index.readJSON("shakespeare-scenes.json");
        //index.writeDataHashMap("results.txt", dataStorage);

        String[] runTimes = new String[7];

        //terms 0
        String moreThanWord = "you";
        String[] terms0List = new String[2];
        terms0List[0] = "thee";
        terms0List[1] = "thou";
        long startTime = System.nanoTime();
        HashMap<String, ArrayList<String>> terms0 = index.usedMoreThan(terms0List, moreThanWord);
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1000000;
        runTimes[0] = "Terms 0: " + Double.toString(duration);
        index.writeTermFiles("terms0.txt", terms0);

        //terms 1
        String[] terms1List = new String[3];
        terms1List[0] = "Verona";
        terms1List[1] = "Rome";
        terms1List[2] = "Italy";
        startTime = System.nanoTime();
        HashMap terms1 = index.isMentioned(terms1List, "s");
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        runTimes[1] = "Terms 1: " + Double.toString(duration);
        index.writeTermFiles("terms1.txt", terms1);

        //terms 2
        String[] terms2List = new String[1];
        terms2List[0] = "falstaff";
        startTime = System.nanoTime();
        HashMap terms2 = index.isMentioned(terms2List, "p");
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        runTimes[2] = "Terms 2: " + Double.toString(duration);
        index.writeTermFiles("terms2.txt", terms2);

        //terms 3
        String[] terms3List = new String[1];
        terms3List[0] = "Soldier";
        startTime = System.nanoTime();
        HashMap terms3 = index.isMentioned(terms3List, "p");
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        runTimes[3] = "Terms 3: " + Double.toString(duration);
        index.writeTermFiles("terms3.txt", terms3);

        //phrase 0
        String phraseWord0 = "lady macbeth";
        startTime = System.nanoTime();
        HashMap phrase0 = index.phraseMentioned(phraseWord0);
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        runTimes[4] = "Phrase 0: " + Double.toString(duration);
        index.writeTermFiles("phrase0.txt", phrase0);

        //phrase 1
        String phraseWord1 = "a rose by any other name";
        startTime = System.nanoTime();
        HashMap phrase1 = index.phraseMentioned(phraseWord1);
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        runTimes[5] = "Phrase 1: " + Double.toString(duration);
        index.writeTermFiles("phrase1.txt", phrase1);

        //phrase 2
        String phraseWord2 = "cry havoc";
        startTime = System.nanoTime();
        HashMap phrase2 = index.phraseMentioned(phraseWord2);
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        runTimes[6] = "Phrase 2: " + Double.toString(duration);
        index.writeTermFiles("phrase2.txt", phrase2);

        /*
        System.out.println("Runtime - Milliseconds");
        for(int x = 0 ; x < runTimes.length; x++) {
            System.out.println(runTimes[x]);
        }
        */

        //average length of scene
        double total = 0.0;
        double numberScenes = 0.0;
        for (String scene : index.All_Scenes.keySet()) {
            numberScenes++;
            total += index.All_Scenes.get(scene).size();
        }
        double averageLengthScene = total / numberScenes;
        System.out.println("Average Length of Scene: " + averageLengthScene);
        System.out.println();

        //shortest scene
        String sceneNameShortest = "";
        int sceneSize = 999999999; //lazy magic numbers

        for (String scene : index.All_Scenes.keySet()) {
            int newSceneSize = index.All_Scenes.get(scene).size();
            if (newSceneSize < sceneSize) {
                sceneNameShortest = scene;
                sceneSize = newSceneSize;
            }
        }
        System.out.println("Shortest Scene: " + sceneNameShortest);
        //System.out.println("Size: " + sceneSize);
        System.out.println();

        //create play hashMap
        HashMap<String, ArrayList<Integer>> plays = new HashMap<>();
        for(String scene : index.All_Scenes.keySet()) {
            String sceneToPlay = scene.split(":")[0];
            if(plays.containsKey(sceneToPlay)) {
                plays.get(sceneToPlay).addAll(index.All_Scenes.get(scene));
            } else {
                plays.put(sceneToPlay, index.All_Scenes.get(scene));
            }
        }

        //longest play
        String longestPlayName = "";
        int longestPlayLength = 0;

        for(String play : plays.keySet()) {
           int newPlayLength =  plays.get(play).size();
           if(newPlayLength > longestPlayLength) {
               longestPlayName = play;
               longestPlayLength = newPlayLength;
           }
        }
        System.out.println("Longest Play: " + longestPlayName);
        //System.out.println("Size: " + longestPlayLength);
        System.out.println();

        //shortest play

        String shortestPlayName = "";
        int shortestPlayLength = 999999999;

        for(String play : plays.keySet()) {
            int newPlayLength =  plays.get(play).size();
            if(newPlayLength < shortestPlayLength) {
                longestPlayName = play;
                longestPlayLength = newPlayLength;
            }
        }
        System.out.println("Shortest Play: " + longestPlayName);
        //System.out.println("Size: " + longestPlayLength);


    }

    private HashMap readJSON(String fileName) throws ParseException, IOException {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONObject jsonObject = (JSONObject) obj;

            //loop
            JSONArray corpus = (JSONArray) jsonObject.get("corpus");

            Iterator<JSONObject> iterator = corpus.iterator();
            while (iterator.hasNext()) {
                JSONObject object = iterator.next();
                String sceneID = (String) object.get("sceneId");
                String text = (String) object.get("text");
                String[] text_array = text.split("\\s+");
                sceneText.put(sceneID, text_array); //helper collection for phrases
                All_Scenes.put(sceneID, new ArrayList<>());

                for (int x = 0; x < text_array.length; x++) {
                    if (dataStorage.containsKey(text_array[x])) {
                        HashMap<String, ArrayList<Integer>> valueMap = dataStorage.get(text_array[x]);
                        if (valueMap.containsKey(sceneID)) {
                            valueMap.get(sceneID).add(x);
                            dataStorage.replace(text_array[x], valueMap);
                            All_Scenes.get(sceneID).add(x);
                        } else {
                            ArrayList<Integer> newEntryList = new ArrayList<>();
                            newEntryList.add(x);
                            valueMap.put(sceneID, newEntryList);
                            All_Scenes.get(sceneID).add(x);
                        }
                    } else {
                        //create the value hash map for the new term
                        HashMap<String, ArrayList<Integer>> newEntryMap = new HashMap<>();
                        ArrayList<Integer> newEntryList = new ArrayList<>();
                        newEntryList.add(x);
                        newEntryMap.put(sceneID, newEntryList);

                        //add key and hash map to top level hash map
                        dataStorage.put(text_array[x], newEntryMap);
                        All_Scenes.get(sceneID).add(x);
                    }

                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dataStorage;
    }

    private void writeTermFiles(String fileName, HashMap data)
            throws IOException {
        HashMap<String, ArrayList<String>> copyData = data;
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        ArrayList<String> alphabeticallySorted = new ArrayList<>();
        for (String entry : copyData.keySet()) {
            ArrayList<String> dataEntries = copyData.get(entry);
            for (String dataentry : dataEntries) {
                if (!alphabeticallySorted.contains(dataentry)) {
                    alphabeticallySorted.add(dataentry);
                }
            }
        }
        Collections.sort(alphabeticallySorted);
        for (String entry : alphabeticallySorted) {
            writer.write(entry);
            writer.newLine();
        }
        writer.close();
    }

    private HashMap<String, ArrayList<String>> usedMoreThan(String[] words, String moreThanWord) { //scenes
        HashMap<String, ArrayList<String>> result = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> wordCounts = new HashMap<>(); //words
        HashMap<String, Integer> morethanWordCounts = new HashMap<>();

        //word count for each scene moreThanWord appears in
        HashMap<String, ArrayList<Integer>> moreThanWordScenes = dataStorage.get(moreThanWord);
        for (String scene : moreThanWordScenes.keySet()) {
            morethanWordCounts.put(scene, moreThanWordScenes.get(scene).size());
        }

        //word count for each scene each word appears in
        for (String word : words) {
            HashMap<String, ArrayList<Integer>> wordScenes = dataStorage.get(word);
            HashMap<String, Integer> sceneWordCount = new HashMap<>();
            for (String scene : wordScenes.keySet()) {
                int wordCountScene = wordScenes.get(scene).size();
                sceneWordCount.put(scene, wordCountScene);
            }
            wordCounts.put(word, sceneWordCount);
        }

        //get union of words for each scene
        HashMap<String, Integer> unionScenes = new HashMap<>();
        for (String word : wordCounts.keySet()) {
            HashMap<String, Integer> scenes = wordCounts.get(word);
            for (String scene : scenes.keySet()) {
                if (unionScenes.containsKey(scene)) {
                    int current = unionScenes.get(scene);
                    int add = scenes.get(scene);
                    unionScenes.replace(scene, current + add);
                } else {
                    unionScenes.put(scene, scenes.get(scene));
                }
            }
        }

        //comparison for all scenes of other word
        ArrayList<String> finalScenes = new ArrayList<>();
        for (String scene : morethanWordCounts.keySet()) {
            try {
                int wordCountWords = unionScenes.get(scene);
                int wordCountMoreThanWord = morethanWordCounts.get(scene);
                if (wordCountWords > wordCountMoreThanWord) {
                    finalScenes.add(scene);
                }
            } catch (NullPointerException e) { //if the moreThanWord does not appear at all
                int wordCountWords = 0;
                int wordCountMoreThanWord = morethanWordCounts.get(scene);
                if (wordCountWords > wordCountMoreThanWord) {
                    finalScenes.add(scene);
                }
            }
        }

        //comparison for all scenes of words
        for (String scene : unionScenes.keySet()) {
            try {
                int wordCountWords = unionScenes.get(scene);
                int wordCountMoreThanWord = morethanWordCounts.get(scene);
                if (wordCountWords > wordCountMoreThanWord) {
                    finalScenes.add(scene);
                }
            } catch (NullPointerException e) { //if the moreThanWord does not appear at all
                int wordCountWords = unionScenes.get(scene);
                int wordCountMoreThanWord = 0;
                if (wordCountWords > wordCountMoreThanWord) {
                    finalScenes.add(scene);
                }
            }
        }

        result.put("Result", finalScenes);
        return result;
    }

    private HashMap isMentioned(String[] mentionedWord, String scene_play) { //scenes or play
        HashMap<String, ArrayList<String>> result = new HashMap<>();

        for (String word : mentionedWord) {
            ArrayList<String> scenes = new ArrayList<>();
            ArrayList<String> plays = new ArrayList<>();
            if (dataStorage.containsKey(word.toLowerCase())) {
                HashMap<String, ArrayList<Integer>> termValues = dataStorage.get(word.toLowerCase());
                for (String scene : termValues.keySet()) {
                    String play = scene.split(":")[0];
                    if (!plays.contains(play)) {
                        plays.add(play);
                    }
                    if (!scenes.contains(scene)) {
                        scenes.add(scene);
                    }
                }
                if (scene_play.equals("s")) {
                    result.put(word, scenes);
                } else if (scene_play.equals("p")) {
                    result.put(word, plays);
                }
            }
        }
        return result;
    }

    private HashMap phraseMentioned(String phrase) { //scenes
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> scenes = new ArrayList<>();
        String[] splitPhrase = phrase.split(" ");
        int wordsInPhrase = splitPhrase.length;
        boolean phraseInScene;

        //scenes that contain the first word
        HashMap<String, ArrayList<Integer>> possibleScenes = dataStorage.get(splitPhrase[0]); //all scenes with first word
        for (String scene : possibleScenes.keySet()) { //for all scenes that have the first word
            ArrayList<Integer> wordIndex = possibleScenes.get(scene); //all indexes of first word in scene
            for (Integer index : wordIndex) { //for each index of first word
                phraseInScene = true; //by default start with true
                String[] text = sceneText.get(scene); //get the text for comparison
                for (int x = 0; x < wordsInPhrase; x++) { //iterate every word in phrase
                    if (!splitPhrase[x].equals(text[index + x])) { //check if next text word matches next phrase word
                        phraseInScene = false;
                        break;
                    }
                }
                if (phraseInScene) {
                    scenes.add(scene);
                    break;
                }
            }
        }
        result.put(phrase, scenes);
        return result;
    }
}
