import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Index {
    //Map<Term, HashMap<DocId, ArrayList<Positions>>>
    private HashMap<String, HashMap<String, ArrayList<Integer>>> dataStorage;

    public Index() {
        dataStorage = new HashMap<>();
    }

    public static void main(String[] args) throws IOException, ParseException {
        Index index = new Index();
        HashMap<String, HashMap<String, ArrayList<Integer>>> dataStorage = index.readJSON("shakespeare-scenes.json");
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
        HashMap<String, ArrayList<String>> terms1 = index.isMentioned(terms1List, "s");
        index.writeTermFiles("terms1.txt", terms1);

        //terms 2
        String[] terms2List = new String[1];
        terms2List[0] = "falstaff";
        HashMap<String, ArrayList<String>> terms2 = index.isMentioned(terms2List, "p");
        index.writeTermFiles("terms2.txt", terms2);

        //terms 3
        String[] terms3List = new String[1];
        terms3List[0] = "Soldier";
        HashMap<String, ArrayList<String>> terms3 = index.isMentioned(terms3List, "p");
        index.writeTermFiles("terms3.txt", terms3);

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
        } catch(IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
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
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for(Object entry : data.entrySet()) {
            writer.write(entry.toString());
            writer.newLine();
        }
        writer.close();
    }

    private HashMap<String, ArrayList<String>> usedMoreThan(String[] words, String moreThanWord) { //scenes
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        for(String word : words) {
            ArrayList<String> scenes = new ArrayList<>();
            //scene, index
            HashMap<String, ArrayList<Integer>> wordStorage = dataStorage.get(word);
            for(String scene: wordStorage.keySet()) {
                int wordCount = wordStorage.get(scene).size();
                try {
                    int moreThanWordCount = dataStorage.get(moreThanWord).get(scene).size();
                    if(wordCount > moreThanWordCount) {
                        scenes.add(scene);
                    }
                } catch (NullPointerException e) { //in the case that 'you' does not appear in the scene for the word
                    int moreThanWordCount = 0;
                    if(wordCount > moreThanWordCount) {
                        scenes.add(scene);
                    }
                }
            }
            result.put(word, scenes);
        }
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
                    String play = scene.split("\\:")[0];
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
}
