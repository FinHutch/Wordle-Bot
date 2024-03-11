package org.example.firstjavafx;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordHandler { // current best average = 3.4790496760259177
    private List<String> answerList;
    private List<String> guessList;
    private FileOutputStream output;
    public WordHandler(String answersFile,String guessesFile){
        this.answerList = loadWords(String.valueOf(answersFile));
        this.guessList = loadWords(String.valueOf(guessesFile));
    }
    private void printWordList(List<String> wordList) {
        for (String word : wordList) {
            System.out.println(word);
        }
    }
    public static void filechecker(String filename) {
        String filePath = filename; // Replace this with your file path
        File file = new File(filePath);

        if (file.exists()) {
            System.out.println("File exists!");
        } else {
            System.out.println("File does not exist.");
        }
    }
    public static void saveMapToFile(Map<String,Map<List<Integer>,String >> map,String filename) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
            outputStream.writeObject(map);
            System.out.println("Map saved to file: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to deserialize the map of maps from a file
    public static Map<String,Map<List<Integer>,String> > loadMapFromFile(String filename) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename))) {
            Map<String,Map<List<Integer>,String> > map = (Map<String,Map<List<Integer>,String>>) inputStream.readObject();
            System.out.println("Map loaded from file: " + filename);
            return map;
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<String,Map<List<Integer>,String> >();
        }
    }
    public List<String> getAnswerList() {
        return answerList;
    }

    // Setter for answerList
    public void setAnswerList(List<String> answerList) {
        this.answerList = answerList;
    }

    // Getter for guessList
    public List<String> getGuessList() {
        return guessList;
    }

    // Setter for guessList
    public void setGuessList(List<String> guessList) {
        this.guessList = guessList;
    }
    private List<String> loadWords(String filename) {
        List<String> wordList = new ArrayList<>();
        try (
                InputStream inputStream = GameLogic.class.getResourceAsStream( filename);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                wordList.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
        return wordList;
    }
}
