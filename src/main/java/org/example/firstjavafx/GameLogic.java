package org.example.firstjavafx;

import java.util.*;
import java.util.stream.Collectors;


public class GameLogic {
    private final List<LetterInfo> letterInfoList;
    private final int COLUMNS;
    public Map<String, Map<List<Integer>,String>> bestSecondGuessesMap;
    private final int ROWS;
    WordHandler wordHandler;

    private String secondGuessesFileName;
    public GameLogic(int r, int c) {
        letterInfoList = new ArrayList<>();
        ROWS = r;
        COLUMNS = c;
        wordHandler = new WordHandler("possibleAnswers.txt","possibleGuesses.txt");
        secondGuessesFileName = "src/main/resources/org/example/firstjavafx/bestSecondGuesses.dat";
        WordHandler.filechecker(secondGuessesFileName);
        bestSecondGuessesMap = WordHandler.loadMapFromFile(secondGuessesFileName);
    }


    public String calculate(char[][] characters, int[][] clickCounts, int lastActivatedRow) {
        List<LetterInfo> allLetterInfo = addRowInformation(characters, clickCounts, lastActivatedRow);
        if (lastActivatedRow == -1){
            return "next guess : crate";

        }else if (lastActivatedRow == 0){
            String firstGuess = new String(characters[0]);
            if(bestSecondGuessesMap.containsKey(firstGuess)){
                List<Integer> colours = getModulo3List(clickCounts[0]);
                String nextGuess = bestSecondGuessesMap.get(firstGuess).get(colours);
                return ("next guess : " + nextGuess);
            }
        }
        if(allLetterInfo.isEmpty()){
            return ("Invalid colours");
        }
        System.out.println("\n");

        List<String> wordList = wordHandler.getAnswerList();
        List<String> guessList = wordHandler.getGuessList();
        wordList = reduceList(allLetterInfo,wordList);
        String nextGuess = findBestGuess(wordList,guessList);
        if (Objects.equals(nextGuess, " ")){
            return ("No available words!");
        }
        return ("next guess : " + findBestGuess(wordList,guessList));

    }
    public void runAlgorithm(String firstGuess){   // best first guess reast - 3.44
        List<Integer> scores = new ArrayList<>();
        List<String> wordList = wordHandler.getAnswerList();
        List<String> guessList = wordHandler.getGuessList();
        Map<List<Integer>, String> bestSecondGuess = new HashMap<>();
        String bestGuess = " ";
        if(!bestSecondGuessesMap.containsKey(firstGuess)) {
            for (String tempAnswer : wordList) {
                List<Integer> coloursList = getColours(firstGuess, tempAnswer);

                // Convert the int[] colours array to a List<Integer> for better handling

                // Update the colorOccurrences map
                bestSecondGuess.put(coloursList, null);
            }

            for (Map.Entry<List<Integer>, String> entry : bestSecondGuess.entrySet()) {
                List<Integer> coloursList = entry.getKey();
                wordList = reduceList(getInfo(firstGuess.toCharArray(), coloursList), wordHandler.getAnswerList());

                List<LetterInfo> tempInfo = getInfo(firstGuess.toCharArray(), coloursList);
                bestGuess = findBestGuess(wordList, guessList);
                entry.setValue(bestGuess);
                System.out.println("Colours List: " + coloursList);
                System.out.println("Associated String: " + bestGuess);

            }
            bestSecondGuessesMap.put(firstGuess,bestSecondGuess);
            WordHandler.saveMapToFile(bestSecondGuessesMap,secondGuessesFileName);
        }else{
            bestSecondGuess = bestSecondGuessesMap.get(firstGuess);
        }

        //wordHandler.save;MapToFile(bestSecondGuess,"bestSecondGuesses.dat");
        //System.out.println("map saved");
        for (String answer : wordHandler.getAnswerList()){
            int score = 1;
            wordList = wordHandler.getAnswerList();
            List<Integer> colours = getColours(firstGuess,answer);

            wordList = reduceList(getInfo(firstGuess.toCharArray(),colours),wordList);
            bestGuess = firstGuess;
            System.out.print(firstGuess);
            while (score<=6) {
                if (bestGuess.equals(answer)){
                    scores.add(score);
                    System.out.println();
                    //System.out.println(score + " ");
                    break;
                }
                System.out.print(",");
                if (score ==1){
                    bestGuess = bestSecondGuess.get(colours);
                } else {
                    bestGuess = findBestGuess(wordList, guessList);
                }
                score++;
                System.out.print(bestGuess);
                colours = getColours(bestGuess, answer);
                wordList = reduceList(getInfo(bestGuess.toCharArray(),colours),wordList);

            }
            if (score ==7){
                System.out.println("fail \n");
            }

        }
        int sum = 0;
        for (int num : scores) {
            sum += num;
        }
        System.out.println(scores);
        System.out.println((double)sum / scores.size());

    }
    private String findBestGuess(List<String> wordList,List<String> guessList){
        float averageWordsLeft;
        float topScore = 3000;
        String topGuess = " ";

        for (String s : guessList) {
            averageWordsLeft = findAverageWordsLeft(s, wordList);
            //System.out.println(s + " " + averageWordsLeft);
            if(averageWordsLeft < topScore){
                topGuess = s;
                topScore = averageWordsLeft;
            }
        }

        return topGuess;

    }
    private float findAverageWordsLeft(String guess, List<String> wordList){
        int score = 0;
        int size = wordList.size();
        Map<List<Integer>, Integer> colourOccurrences = new HashMap<>();

        for (String tempAnswer : wordList) {
            List<Integer> coloursList = getColours(guess, tempAnswer);

            // Convert the int[] colours array to a List<Integer> for better handling

            // Update the colorOccurrences map
            colourOccurrences.put(coloursList, colourOccurrences.getOrDefault(coloursList, 0) + 1);
        }
        List<String> tempList = wordList;
        for (Map.Entry<List<Integer>, Integer> entry : colourOccurrences.entrySet()) {
            tempList = wordList;
            List<Integer> coloursList = entry.getKey();
            int occurrenceCount = entry.getValue();
            List<LetterInfo> tempInfo = getInfo(guess.toCharArray(),coloursList);
            //combineLetterInfos(tempInfo, letterInfo);
            tempList = reduceList(tempInfo, tempList);
            boolean containsOnlyGuess = tempList.size() == 1 && tempList.getFirst().equals(guess);
            if (!containsOnlyGuess){
                score+= tempList.size() * occurrenceCount;
            }
        }
//
        return score/(float)size;
    }
    private List<Integer> getModulo3List(int[] array) {
        List<Integer> result = new ArrayList<>();
        for (int value : array) {
            result.add(value % 3);
        }
        return result;
    }
    public void printColorOccurrences(Map<List<Integer>, Integer> colorOccurrences) {
        int score = 0;
        for (Map.Entry<List<Integer>, Integer> entry : colorOccurrences.entrySet()) {
            List<Integer> colorsListPrint = entry.getKey();
            int occurrenceCount = entry.getValue();
            score += occurrenceCount;
            // Print the colors and their occurrence count
            System.out.println("Colors: " + colorsListPrint + ", Occurrence Count: " + occurrenceCount);
        }
        System.out.println("Total Words is: " + score);
    }
    public List<Integer> getColours(String guess, String answer) {
        List<Integer> coloursGuess = new ArrayList<>(Collections.nCopies(guess.length(), 0));
        List<Integer> coloursAnswer = new ArrayList<>(Collections.nCopies(guess.length(), 0));
        for (int i = 0; i<coloursAnswer.size(); i++){
            if (guess.charAt(i)==answer.charAt(i)){
                coloursGuess.set(i,2);
                coloursAnswer.set(i,2);
            }
        }
        for (int i = 0; i<coloursAnswer.size(); i++){
            if (coloursAnswer.get(i) != 0){continue;}
            for (int j = 0; j<coloursGuess.size(); j++) {
                if (coloursGuess.get(j) !=0){continue;}
                if (guess.charAt(j) == answer.charAt(i)) {
                    coloursGuess.set(j,1);
                    coloursAnswer.set(i,1);
                    break;
                }
            }
        }
        return coloursGuess;
    }

    private List<LetterInfo> addRowInformation(char[][] characters, int[][] clickCounts, int lastActivatedRow) {
        List<LetterInfo> mainInfo = new ArrayList<>();
        for (int row = 0; row <= lastActivatedRow; row++) {
            int[] clickCountsForRow = clickCounts[row];
            List<Integer> clickCountsForRowList = Arrays.stream(clickCountsForRow).boxed().toList();
            List<LetterInfo> rowInfo = getInfo(characters[row],clickCountsForRowList );
            mainInfo = combineLetterInfos(mainInfo, rowInfo);
             // Return the updated mainInfo list
        }
        return mainInfo;

    }
    private List<LetterInfo> combineLetterInfos(List<LetterInfo> letterInfoList1, List<LetterInfo> letterInfoList2){

        for (LetterInfo letter2 : letterInfoList2) {
            boolean sharedInfo = false;
            for (int i = 0; i < letterInfoList1.size(); i++) {
                LetterInfo letter1 = letterInfoList1.get(i);
                if (letter2.getLetter() == letter1.getLetter()) {
                    sharedInfo = true;
                    LetterInfo combinedInfo = compareLetterInfo(letter1, letter2);
                    if (!combinedInfo.getValidity()) {
                        letterInfoList1.clear();
                        return letterInfoList1;
                    } else {
                        // Update mainInfo with combinedInfo
                        letterInfoList1.set(i, combinedInfo);
                    }
                }
            }
            // If no shared info, add rowLetter to mainInfo
            if (!sharedInfo) {
                letterInfoList1.add(letter2);
            }
        }
        return letterInfoList1;
    }
    private List<LetterInfo> getInfo(char[] word, List<Integer> wordColours) {
        List<LetterInfo> tempList = new ArrayList<>();
        char[] letters = new char[COLUMNS];
        int lettersFound = 0;
        for (int i = 2; i>=0; i--){
            for (int col = 0; col < COLUMNS; col++) {
                char currLetter = word[col];
                int occurrences;
                int currColour = wordColours.get(col) % 3;
                int foundIndex = isFound(currLetter, letters);
                int[] position = new int[COLUMNS]; // 0 maybe, 1 definitely, 2 definitely not
                if (foundIndex==-1 && currColour == i) {
                    letters[lettersFound] = currLetter;
                    lettersFound++;
                    switch (currColour) {
                        case 0:
                            //grey

                            Arrays.fill(position, 2);
                            tempList.add(new LetterInfo(currLetter, position,0, true));
                            break;

                        case 1:
                            //yellow
                            position[col] = 2;
                            tempList.add(new LetterInfo(currLetter, position,1, false));
                            break;

                        case 2:
                            //green
                            position[col] = 1;
                            tempList.add(new LetterInfo(currLetter, position,1, false));
                            break;
                    }

                } else if (currColour == i){
                    occurrences = tempList.get(foundIndex).getOccurrences();
                    boolean numberKnown = tempList.get(foundIndex).isNumberKnown();
                    position = tempList.get(foundIndex).getPosition();
                    switch (currColour) {
                        case 0:
                            numberKnown = true;
                            position[col] = 2;
                            break;

                        case 1:
                            //yellow
                            occurrences++;
                            position[col] = 2;
                            break;

                        case 2:
                            //green
                            position[col] = 1;
                            occurrences++;
                            break;
                    }
                    tempList.set(foundIndex, new LetterInfo(currLetter, position, occurrences, numberKnown));
                }
            }
        }
        return tempList;

    }

    private LetterInfo compareLetterInfo(LetterInfo letter1, LetterInfo letter2){
        if(letter2.isNumberKnown() && !letter1.isNumberKnown()){
            return compareLetterInfo(letter2,letter1);
        }
        int[] position = new int[COLUMNS];
        boolean numberKnown = false;
        boolean validity = true;
        int occurrences = 0;
        char letter = letter1.getLetter();
        // check number knowns line up
        if (letter1.isNumberKnown()){
            if (letter2.isNumberKnown()){
                if (letter1.getOccurrences() != letter2.getOccurrences()){
                    validity = false;
                }
                numberKnown = true;
            }
            if (letter2.getOccurrences()>letter1.getOccurrences()) {
                validity = false;
            }
        }
        occurrences = Math.max(letter2.getOccurrences(),letter1.getOccurrences());
        for (int i = 0; i<COLUMNS; i++){
            int higherVal = Math.max(letter1.getPosition()[i],letter2.getPosition()[i]);
            int lowerVal = Math.min(letter1.getPosition()[i],letter2.getPosition()[i]);
            if (higherVal ==2 && lowerVal ==1) {
                validity = false;
            }else if (higherVal == 2){
                position[i] = 2;
            }else if (higherVal == 1){
                position[i] = 1;
            }
        }
        return new LetterInfo(letter,position,occurrences,numberKnown,validity);
    }
    private List<String> reduceList(List<LetterInfo> conditions, List<String> words) {
        List<String> reducedWords = new ArrayList<>();
        for (String word : words) {
            if (fitsConditions(word, conditions)) {
                reducedWords.add(word);
            }
        }
        return reducedWords;
    }

    private boolean fitsConditions(String word, List<LetterInfo> conditions){
        for (LetterInfo letterInfo : conditions){
            int letterCount = 0;
            char letter = letterInfo.getLetter();
            int[] position = letterInfo.getPosition();
            int occurrences = letterInfo.getOccurrences();
            for (int i = 0; i< COLUMNS; i++){
                if (word.charAt(i) == letter) {
                    letterCount++;
                    if (position[i] ==2){return false;}

                }else if (position[i] == 1) {return false;} // if not that letter there discard

            }
            if (letterCount < occurrences){return false;}
            if (letterInfo.isNumberKnown() && letterCount != occurrences){return false;}

        }
        return true;
    }

    private int isFound(char letterToFind, char[] array){
        int counter = 0;
        for (char letter : array) {
            if (letter ==letterToFind) {
                return counter;
            }
            counter++;
        }
        return -1;
    }


}

class LetterInfo {
    private char letter;
    private final int occurrences;
    private int[] position;

    private boolean validity;
    private boolean numberKnown;

    public LetterInfo(char letter, int[] position,int number, boolean numberKnown) {
        this.letter = letter;
        this.position = position;
        this.occurrences = number;
        this.numberKnown = numberKnown;
        this.validity= true;

    }
    public LetterInfo(char letter, int[] position,int number, boolean numberKnown,boolean validity) {
        this.letter = letter;
        this.position = position;
        this.occurrences = number;
        this.numberKnown = numberKnown;
        this.validity= validity;

    }

    // Getters and Setters
    public char getLetter() {
        return letter;
    }

    public boolean getValidity() {return validity;}
    public void setValidity(boolean bool) {
        this.validity = bool;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public int getOccurrences() {
        return occurrences;
    }



    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public boolean isNumberKnown() {
        return numberKnown;
    }

    public void setNumberKnown(boolean numberKnown) {
        this.numberKnown = numberKnown;
    }

    public void printInfo() {
        System.out.println("Letter: " + letter);
        System.out.println("Occurrences: " + occurrences);
        System.out.println("Position: " + Arrays.toString(position));
        System.out.println("Validity: " + validity);
        System.out.println("Number Known: " + numberKnown);
    }

}
