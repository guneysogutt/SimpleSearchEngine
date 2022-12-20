package SimpleSearchEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Scanner;

public class SimpleSearchEngine {

    long totalIndexingTime = 0;
    long totalSearchTime = 0;
    long averageSearchTime = 0;
    long minSearchTime,maxSearchTime;

    int[] fileWordLengths;

    Object[] word1Files,word2Files,word3Files;

    public SimpleSearchEngine() throws FileNotFoundException {
        test();
    }

    String DELIMITERS = "[-+=" +
            " " +        //space
            "\r\n " +    //carriage return line fit
            "1234567890" + //numbers
            "’'\"" +       // apostrophe
            "(){}<>\\[\\]" + // brackets
            ":" +        // colon
            "," +        // comma
            "‒–—―" +     // dashes
            "…" +        // ellipsis
            "!" +        // exclamation mark
            "." +        // full stop/period
            "«»" +       // guillemets
            "-‐" +       // hyphen
            "?" +        // question mark
            "‘’“”" +     // quotation marks
            ";" +        // semicolon
            "/" +        // slash/stroke
            "⁄" +        // solidus
            "␠" +        // space?
            "·" +        // interpunct
            "&" +        // ampersand
            "@" +        // at sign
            "*" +        // asterisk
            "\\" +       // backslash
            "•" +        // bullet
            "^" +        // caret
            "¤¢$€£¥₩₪" + // currency
            "†‡" +       // dagger
            "°" +        // degree
            "¡" +        // inverted exclamation point
            "¿" +        // inverted question mark
            "¬" +        // negation
            "#" +        // number sign (hashtag)
            "№" +        // numero sign ()
            "%‰‱" +      // percent and related signs
            "¶" +        // pilcrow
            "′" +        // prime
            "§" +        // section sign
            "~" +        // tilde/swung dash
            "¨" +        // umlaut/diaeresis
            "_" +        // underscore/understrike
            "|¦" +       // vertical/pipe/broken bar
            "⁂" +        // asterism
            "☞" +        // index/fist
            "∴" +        // therefore sign
            "‽" +        // interrobang
            "※" +          // reference mark
            "]";

    AList<String> stopWords = new AList<String>();
    public Object[] fileNames;
    HashedDictionary<String,String> hashedDictionary = new HashedDictionary<String,String>();

    public void test() throws FileNotFoundException {
        readStopWords(); // read the stop words and assign them into the arraylist
        readArticles();
        // searching and timing operations
        /*
        readSearchWords();
        System.out.println("total collision : " + hashedDictionary.collisionCount);
        System.out.println("total indexing time : " + totalIndexingTime);
        System.out.println("total search time : " + totalSearchTime);
        System.out.println("average search time : " + averageSearchTime);
        System.out.println("min search time : " + minSearchTime);
        System.out.println("max search time : " + maxSearchTime);
         */
        inputProcess(); // call input process
        findMostRelevant(); // find most relevant document
    }

    public void readArticles(){
        try {
            File fileName = new File("files\\sport"); // assign file
            File[] filesList = fileName.listFiles(); // file array for the .txt files

            assert filesList != null;
            fileWordLengths = new int[filesList.length]; // word counts for each file
            int fileWordLengthsIndex = 0;

            int count = 0;
            // create a new array in the following format
            // 001.txt 0,002.txt 0,003.txt 0,........................................
            for (File file : filesList) {
                count++;
                if (count == 101)
                    break;
                Object object = file.getName() + " " + 0;
                this.fileNames = addItemToArray(fileNames,object);
            }
            count = 0; // reassign 0 for count the control how many files we'll work with

            for (File file : filesList) {
                int wordCounter = 0; // counts the words for each file
                // control the amount of files (total 511 files)
                count++;
                if (count == 101)
                    break;

                Scanner scanner = new Scanner(file); // scanner for reading
                String input;

                while (scanner.hasNextLine()) {
                    input = scanner.nextLine();
                    input = input.toLowerCase(Locale.ENGLISH);

                    String[] splitted = input.split(DELIMITERS); // split the input by delimiters

                    for (String item : splitted) {
                        wordCounter++; // counts the words for each document
                        if (!stopWords.contains(item.toLowerCase(Locale.ENGLISH))) { // if not a stop word
                            long startTime = System.nanoTime(); //start timer
                            hashedDictionary.add(item, file.getName(), fileNames.clone(),false); // add to the hashed dictionary
                            long endTime = System.nanoTime(); // end timer
                            totalIndexingTime += (endTime-startTime); // assign total indexing time
                        }
                    }
                }
                fileWordLengths[fileWordLengthsIndex++] = wordCounter; // assign whole word counts
            }
        }
        catch (FileNotFoundException s){
            System.out.println("File not found!");
        }
    }

    public void inputProcess() {

        Scanner inp = new Scanner(System.in);
        String input = null;
        boolean check = false; // check for incorrect input

        while(!check){
            System.out.println("Please enter 3 words to search (Ex: Barcelona Football Club)");
            input = inp.nextLine();
            if (input.split(" ").length == 3)
                check = true;
        }

        String[] splittedInputs = input.toLowerCase(Locale.ENGLISH).split(" ");
        String firstWord = splittedInputs[0];
        String secondWord = splittedInputs[1]; // split the input words by space and assign them
        String thirdWord = splittedInputs[2];
        Iterator<String> iterator = hashedDictionary.getKeyIterator();
        Iterator<Object> iterator1 = hashedDictionary.getObjectIterator(); // use iterators to visit each key and object arrays
        int counter = 0;

            while (counter!=3 && iterator.hasNext()){
                String key = (String) iterator.next();
                Object [] array = (Object[]) iterator1.next();
                if (firstWord.equalsIgnoreCase(key)){
                    counter++;
                    word1Files = array;
                }
                else if (secondWord.equalsIgnoreCase(key)){
                    counter++;
                    word2Files = array;
                }
                else if (thirdWord.equalsIgnoreCase(key)){
                    counter++;
                    word3Files = array;
                }
            }

        if (word1Files == null) {
            word1Files = fileNames.clone();
            System.out.println(firstWord + " is not found");
        }
        if (word2Files == null) {
            word2Files = fileNames.clone();
            System.out.println(secondWord + " is not found");
        }
        if (word3Files == null) {
            word3Files = fileNames.clone();
            System.out.println(thirdWord + " is not found");
        }

        System.out.println(Arrays.toString(word1Files));
        System.out.println(Arrays.toString(word2Files)); // print file names and word counts for each input word
        System.out.println(Arrays.toString(word3Files));
    }

    public void findMostRelevant(){
        String mostRelevantFileName = "none"; // initial assignment for most relevant file name
        //initial assignments of the elements of the process
        double relevantFileStat = 0.0;
        double word1Stat = 0.0;
        double word2Stat = 0.0;
        double word3Stat = 0.0;

        for (int i = 0; i < fileNames.length; i++) {
            double fileLength = ((double)fileWordLengths[i]); // store file length
            // word counts for the current file
            word1Stat = Double.parseDouble(word1Files[i].toString().substring(8))/fileLength;
            word2Stat = Double.parseDouble(word2Files[i].toString().substring(8))/fileLength;
            word3Stat = Double.parseDouble(word3Files[i].toString().substring(8))/fileLength;

            double tempRelevantStat = word1Stat + word2Stat + word3Stat; // relevancy score of the current file

            if (tempRelevantStat > relevantFileStat) { // check for the most relevant
                relevantFileStat = tempRelevantStat;
                mostRelevantFileName = word1Files[i].toString().substring(0,7);
            }
        }
        System.out.println("Most Relevant File is : " + mostRelevantFileName);
    }

    public void readStopWords() {
        try {
            File fileName = new File("files\\stop_words_en.txt");
            Scanner scanner = new Scanner(fileName);
            String input;
            String[] inputArr;

            while (scanner.hasNextLine()) {
                input = scanner.nextLine();
                inputArr = input.split(DELIMITERS);
                for (String item : inputArr) // split by delimiters then add to the arraylist
                    stopWords.add(item);
            }
        }
        catch (FileNotFoundException s){
            System.out.println("File not found!");
        }
    }


    public void readSearchWords() {
        minSearchTime = 999999999;
        maxSearchTime = 0;
        long fileLength = 0;
        try {
            File fileName = new File("files\\search.txt");
            Scanner scanner = new Scanner(fileName);
            String input;
            while (scanner.hasNextLine()) {
                input = scanner.nextLine();
                long startTime = System.nanoTime(); // start time
                hashedDictionary.contains(input); // call the searching function
                long endTime = System.nanoTime(); // end time
                long timeDiff = endTime - startTime;
                totalSearchTime += timeDiff;
                fileLength++; // increase file word length
                // check max and min index time
                if (timeDiff < maxSearchTime)
                    minSearchTime = timeDiff;
                if (timeDiff > maxSearchTime)
                    maxSearchTime = timeDiff;
            }
            averageSearchTime = totalSearchTime/fileLength; // calculate average search time
        }
        catch (FileNotFoundException s){
            System.out.println("File not found!");
        }
    }

    public Object[] addItemToArray(Object[] srcArray, Object elementToAdd) {
        if (srcArray == null) {
            Object[] resultArray = {elementToAdd};
            return resultArray;
        }

        Object[] resultArray = new Object[srcArray.length + 1];

        for (int i = 0; i < srcArray.length; i++) {
            resultArray[i] = srcArray[i];
        }

        resultArray[resultArray.length - 1] = elementToAdd;
        return resultArray;
    }
}