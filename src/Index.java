import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

public class Index {
    //Map<Term, HashMap<DocId, ArrayList<Positions>>>
    private HashMap<String, HashMap<String, ArrayList<Integer>>> dataStorage;
    private HashMap<String, String[]> sceneText;

    private Index() {
        dataStorage = new HashMap<>();
        sceneText = new HashMap<>();
    }

    public static void main(String[] args) throws IOException, ParseException {
        Index index = new Index();
        HashMap dataStorage = index.readJSON("shakespeare-scenes.json");
        //index.writeDataHashMap("results.txt", dataStorage);

        //terms 0
        String moreThanWord = "you";
        String[] terms0List = new String[2];
        terms0List[0] = "thee";
        terms0List[1] = "thou";
        HashMap<String, ArrayList<String>> terms0 = index.usedMoreThan(terms0List, moreThanWord);
        index.writeTermFiles("terms0.txt", terms0);

        //terms 1
        String[] terms1List = new String[3];
        terms1List[0] = "Verona";
        terms1List[1] = "Rome";
        terms1List[2] = "Italy";
        HashMap terms1 = index.isMentioned(terms1List, "s");
        index.writeTermFiles("terms1.txt", terms1);

        //terms 2
        String[] terms2List = new String[1];
        terms2List[0] = "falstaff";
        HashMap terms2 = index.isMentioned(terms2List, "p");
        index.writeTermFiles("terms2.txt", terms2);

        //terms 3
        String[] terms3List = new String[1];
        terms3List[0] = "Soldier";
        HashMap terms3 = index.isMentioned(terms3List, "p");
        index.writeTermFiles("terms3.txt", terms3);

        //phrase 0
        String phraseWord0 = "lady macbeth";
        HashMap phrase0 = index.phraseMentioned(phraseWord0);
        index.writeTermFiles("phrase0.txt", phrase0);

        //phrase 1
        String phraseWord1 = "a rose by any other name";
        HashMap phrase1 = index.phraseMentioned(phraseWord1);
        index.writeTermFiles("phrase1.txt", phrase1);

        //phrase 2
        String phraseWord2 = "cry havoc";
        HashMap phrase2 = index.phraseMentioned(phraseWord2);
        index.writeTermFiles("phrase2.txt", phrase2);

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
               JSONObject object =  iterator.next();
               String sceneID = (String) object.get("sceneId");
               String text = (String) object.get("text");
               String[] text_array = text.split("\\s+");

               sceneText.put(sceneID, text_array); //helper collection for phrases

               for(int x = 0; x < text_array.length; x++) {
                   if (dataStorage.containsKey(text_array[x])) {
                       HashMap<String, ArrayList<Integer>> valueMap = dataStorage.get(text_array[x]);
                       if(valueMap.containsKey(sceneID)) {
                           valueMap.get(sceneID).add(x);
                           dataStorage.replace(text_array[x], valueMap);
                       } else {
                           ArrayList<Integer> newEntryList = new ArrayList<>();
                           newEntryList.add(x);
                           valueMap.put(sceneID, newEntryList);
                       }
                   } else {
                       //create the value hash map for the new term
                       HashMap<String, ArrayList<Integer>> newEntryMap = new HashMap<>();
                       ArrayList<Integer> newEntryList = new ArrayList<>();
                       newEntryList.add(x);
                       newEntryMap.put(sceneID, newEntryList);

                       //add key and hash map to top level hash map
                       dataStorage.put(text_array[x], newEntryMap);
                   }

               }

            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return dataStorage;
    }

    private void writeDataHashMap(String fileName, HashMap dataStorage)
    throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for(Object entry : dataStorage.entrySet()) {
            writer.write(entry.toString());
            writer.newLine();
        }
    }

    private void writeTermFiles(String fileName, HashMap data)
            throws IOException {
        HashMap<String, ArrayList<String>> copyData = data;
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        ArrayList<String> alphabeticallySorted = new ArrayList<>();
        for(String entry: copyData.keySet()){
            ArrayList<String> dataEntries = copyData.get(entry);
            for(String dataentry : dataEntries) {
                if(!alphabeticallySorted.contains(dataentry)) {
                    alphabeticallySorted.add(dataentry);
                }
            }
        }
        Collections.sort(alphabeticallySorted);
        for(String entry : alphabeticallySorted) {
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
        for(String scene : moreThanWordScenes.keySet()) {
            morethanWordCounts.put(scene, moreThanWordScenes.get(scene).size());
        }

        //word count for each scene each word appears in
        for(String word : words) {
            HashMap<String, ArrayList<Integer>> wordScenes = dataStorage.get(word);
            HashMap<String, Integer> sceneWordCount = new HashMap<>();
            for(String scene : wordScenes.keySet()) {
                int wordCountScene = wordScenes.get(scene).size();
                sceneWordCount.put(scene, wordCountScene);
            }
            wordCounts.put(word, sceneWordCount);
        }

        //get union of words for each scene
        HashMap<String, Integer> unionScenes = new HashMap<>();
        for(String word : wordCounts.keySet()) {
            HashMap<String, Integer> scenes = wordCounts.get(word);
            for(String scene : scenes.keySet()) {
                if(unionScenes.containsKey(scene)) {
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
        for(String scene : morethanWordCounts.keySet()) {
            try {
                int wordCountWords = unionScenes.get(scene);
                int wordCountMoreThanWord = morethanWordCounts.get(scene);
                if(wordCountWords > wordCountMoreThanWord) {
                    finalScenes.add(scene);
                }
            } catch (NullPointerException e) { //if the moreThanWord does not appear at all
                int wordCountWords = 0;
                int wordCountMoreThanWord = morethanWordCounts.get(scene);
                if(wordCountWords > wordCountMoreThanWord) {
                    finalScenes.add(scene);
                }
            }
        }

        //comparison for all scenes of words
        for(String scene : unionScenes.keySet()) {
            try {
                int wordCountWords = unionScenes.get(scene);
                int wordCountMoreThanWord = morethanWordCounts.get(scene);
                if(wordCountWords > wordCountMoreThanWord) {
                    finalScenes.add(scene);
                }
            } catch (NullPointerException e) { //if the moreThanWord does not appear at all
                int wordCountWords = unionScenes.get(scene);
                int wordCountMoreThanWord = 0;
                if(wordCountWords > wordCountMoreThanWord) {
                    finalScenes.add(scene);
                }
            }
        }

        result.put("Result", finalScenes);
        return result;
    }

    private HashMap isMentioned(String[] mentionedWord, String scene_play) { //scenes or play
        HashMap<String, ArrayList<String>> result = new HashMap<>();

        for(String word: mentionedWord) {
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
        for(String scene: possibleScenes.keySet()) { //for all scenes that have the first word
            ArrayList<Integer> wordIndex = possibleScenes.get(scene); //all indexes of first word in scene
            for(Integer index: wordIndex) { //for each index of first word
                phraseInScene = true; //by default start with true
                String[] text = sceneText.get(scene); //get the text for comparison
                for(int x = 0; x < wordsInPhrase; x++) { //iterate every word in phrase
                    if(!splitPhrase[x].equals(text[index + x])) { //check if next text word matches next phrase word
                        phraseInScene = false;
                        break;
                    }
                }
                if(phraseInScene){
                    scenes.add(scene);
                    break;
                }
            }
        }
        result.put(phrase, scenes);
        return result;
    }
}
