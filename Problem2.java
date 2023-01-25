/*
    This program computes letter and word frequencies
    on a subset of documents in the Gutenberg corpus
    Author: Evan Brown
    Date Created: 02 Jan 2021
    Date Last Modified: 09 Jan 2021
*/

// represents files and directory pathnames 
// in an abstract manner
import java.io.File;

import java.io.FileNotFoundException;
// reads data from files as streams of characters
import java.io.FileReader;

// reads text efficiently from character-input
// stream buffers 
import java.io.BufferedReader;

import java.io.PrintWriter;

// signals that an input/output (I/O) exception 
// of some kind has occurred
import java.io.IOException;

// compiled representation of a regular expressions
import java.util.regex.Pattern;

// matches a compiled regular expression with an input string
import java.util.regex.Matcher;

// scans for user input
import java.util.Scanner;


import java.util.*;


public class Problem2 {

    // no more than this many input files needs to be processed
    final static int MAX_NUMBER_OF_INPUT_FILES = 1000;

    // an array to hold Gutenberg corpus file names
    static String[] inputFileNames = new String[MAX_NUMBER_OF_INPUT_FILES];

    static int fileCount = 0;


    // loads all files names in the directory subtree into an array
    // violates good programming practice by accessing a global variable (inputFileNames)
    public static void listFilesInPath(final File path) {
        for (final File fileEntry : path.listFiles()) {
        	if (fileEntry.isDirectory()) {
                listFilesInPath(fileEntry);
            } 
            else if (fileEntry.getName().endsWith((".txt")))  {
                inputFileNames[fileCount++] = fileEntry.getPath();
                // fileNameListWriter.println(fileEntry.getPath());
                // System.out.println(fileEntry.getName());
                // System.out.println(fileEntry.getAbsolutePath());
                // System.out.println(fileEntry.getCanonicalPath());
            }
        }
    }

    // returns index of a character in the alphabet 
    // uses zero-based indexing
    public static int getLetterValue(char letter) {
        return (int) Character.toUpperCase(letter) - 65;
    }
	
    // returns a list of the intersects within a given buffer value using skip pointers
    public static LinkedList<Integer> intersectWithSkips(LinkedList<Integer> word1, LinkedList<Integer> word2, int bufferValue) {
    	// initialize new linked lists to not destroy the passed values
    	LinkedList<Integer> p1 = word1;
    	LinkedList<Integer> p2 = word2;
    	LinkedList<Integer> answer = new LinkedList<Integer>();
    	
    	int skipP1, skipP2;
    	// check if the positional indexes are empty for the given term and file
    	if (p1 == null || p2 == null) {
    		return p1;
    	}
    	else { // if the positional indexes are not null then we create the skip pointer value
    		skipP1 = (int) Math.sqrt(p1.size());
    		skipP2 = (int) Math.sqrt(p2.size());
    	} 
    	
    	// while the indexes are not empty
    	while (p1.size()-1 != 0 && p2.size()-1 != 0 && p1 != null && p2 != null) {
    		if (p1.peek() == p2.peek()) { // check the latest index for value
        		answer.add(p1.pop());
        		//System.out.print("Answer added: " + answer.getLast() + "  ");
        		p2.remove(0);
        	}
    		else if (p1.peek() < p2.peek()) { // if the first index is less than the second then...
    			// check the buffer values for matches
    			if (p1.get(0) > p2.peek() - Math.min(p1.size(), bufferValue)) {
               		answer.add(p1.pop());
                	p2.pop();
                	continue;
               	}
    			if (skipP1 < p1.size()-1 && p1.get(skipP1) <= p2.peek()) { // check the skip pointer
    				while (skipP1 < p1.size()-1 && p1.get(skipP1) <= p2.peek()) {
    					//System.out.println("Check p1 skip: " + p1.get(skipP1) + " against p2: " + p2.peek());
    					for (int j = 0; j < skipP1; j++) {
    						//System.out.println("Removing: " + p1.peek());
    						p1.remove(0);
    					}
    				}
    			}
    			else {
    				p1.pop();
    			}
    		}
    		else { // if first index is greater than second then...
    			// check the buffer values for matches
    			if (p2.get(0) > p1.peek() - Math.min(p2.size(), bufferValue)) {
               		answer.add(p2.pop());
                	p1.pop();
               		continue;
               	}
    			if (skipP2 < p2.size()-1 && p2.get(skipP2) <= p1.peek()) { // check the skip pointer
    				while (skipP2 < p2.size()-1 && p2.get(skipP2) <= p1.peek()) {
    					//System.out.println("Check p2 skip: " + p2.get(skipP2) + " against p1: " + p1.peek());
    					for (int j = 0; j < skipP2; j++) {
    						//System.out.println("Removing: " + p2.peek());
    						p2.remove(0);
    					}
    				}
    			}
    			else {
    				p2.pop();
    			}
    		}
    	} // end while
    	return answer;
    }
    
	public static void main(String[] args){

        // did the user provide correct number of command line arguments?
        // if not, print message and exit
        if (args.length != 2){
            System.err.println("Number of command line arguments must be 2");
            System.err.println("You have given " + args.length + " command line arguments");
            System.err.println("Incorrect usage. Program terminated");
            System.err.println("Correct usage: java Ngrams <path-to-input-files> <outfile-for-words>");
            System.exit(1);
        }
        
        // extract input file name from command line arguments
        // this is the name of the file from the Gutenberg corpus
        String inputFileDirName = args[0];
        System.out.println("Input files directory path name is: " + inputFileDirName);
        
        // collects file names and write them to 
        listFilesInPath(new File (inputFileDirName));
        
        // br for efficiently reading characters from an input stream
        BufferedReader br = null;
        
        // wordPattern specifies pattern for words using a regular expression
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");
        
        // wordMatcher finds words by spotting word word patterns with input
        Matcher wordMatcher;
        
        
        // a line read from file
        String line;
        
        // an extracted word from a line
        String word;
        
        
        // initialization
        // lists to hold the scraped text
        // map holds term that links to a map of files and the key's locations in the file
        TreeMap<String, TreeMap<Integer, LinkedList<Integer>>> positionalIndex = new TreeMap<>();
        
        // create new file index list in case the word exists in the positional index but is in a new file
    	TreeMap<Integer, LinkedList<Integer>> fileIndex;
        
    	int fileNum = 1, filePosition = 1;
        
    	System.out.print("Start parsing words.\n");
    	
        // process one file at a time
        for (int index = 0; index < fileCount; index++){
        	
        	filePosition = 1;
        	
        	
            // open the input file, read one line at a time, extract words
            // in the line, extract characters in a word, write words into
        	// positional indez
            try {
                // get a BufferedReader object, which encapsulates
                // access to a (disk) file
                br = new BufferedReader(new FileReader(inputFileNames[index]));
                
                // as long as we have more lines to process, read a line
                // the following line is doing two things: makes an assignment
                // and serves as a boolean expression for while test
                while ((line = br.readLine()) != null) {
                    // process the line by extracting words using the wordPattern
                    wordMatcher = wordPattern.matcher(line);

                    // process one word at a time
                    while ( wordMatcher.find() ) {
                        // extract the word
                        word = line.substring(wordMatcher.start(), wordMatcher.end());
                        // System.out.println(word);

                        // convert the word to lowercase, and write to word file
                        word = word.toLowerCase();
                        
                        // positional index creation from word scraping 
                        if (positionalIndex.get(word) != null) { // if the word is already in the list
                        	if (positionalIndex.get(word).get(fileNum) != null) { // if the file is already in the list
                        		positionalIndex.get(word).get(fileNum).add(filePosition);
                        		// System.out.print("1");
                        	}
                        	else { // if the word exists but has no file index started
                        		positionalIndex.get(word).put(fileNum, new LinkedList<Integer>());
                        		positionalIndex.get(word).get(fileNum).add(filePosition);
                        	}
                        }
                        if (positionalIndex.get(word) == null) { // the word is not in the list already
                        	fileIndex = new TreeMap<>();
                        	fileIndex.put(fileNum, new LinkedList<Integer>()); // create a new file index for the file value in use
                        	positionalIndex.put(word, fileIndex);
                    		positionalIndex.get(word).get(fileNum).add(filePosition); // add the file position to the newly created index
                        	// System.out.print("0");
                        }
                        
                        filePosition++;
                    } // while - wordMatcher
                } // while - line
            } // try
            catch (IOException ex) {
                System.err.println("File " + inputFileNames[index] + " not found. Program terminated.\n");
                System.exit(1);
            }
            fileNum++;
        } // for -- process one file at a time
        
    	System.out.print("Stop parsing words.\n");
        
        // try writing for testing cases and error checking
        /* Testing input values for the positional index, below in the user interface there are comments for testing
        PrintWriter indexWriter = null;
        PrintWriter outWriter = null;
        
        try { // open file and catch error
			indexWriter = new PrintWriter("positionalIndexes.txt");
			outWriter = new PrintWriter("codeOuput.txt");
		} catch (FileNotFoundException e) {
			System.err.println("File: positionalIndexes.txt not found");
			System.exit(1);
		}
        
        for(Map.Entry<String, TreeMap<Integer, LinkedList<Integer>>> entry : positionalIndex.entrySet()) {
        	for (int i = 0; i < fileNum; i++) {
        		indexWriter.printf("File Num: %-5d, Word: %-10s, Index List: ", i, entry.getKey());
        		indexWriter.print(entry.getValue().get(i) + "\n");
        	}
        }
        */
        
        
        // start of user input and proximity query parsing 
        Scanner userIn = new Scanner(System.in);
        boolean end = false;
        String user, userWord1, userWord2, userExit;
        int userSpace;
        String[] words;
        
        while (end != true) {
        	System.out.println("Please enter strings separated by an integer of the maximum distance between words:");
            System.out.println("For example: united 0 states 2 engaged");
            
            user = userIn.nextLine();
            
            words = user.split(" ");
            
            // start looping through the files for the position checks
            for (int i = 0; i < words.length - 1; i = i + 2) {
            	userWord1 = words[i].toLowerCase();
            	userSpace = Integer.parseInt(words[i + 1]);
            	userWord2 = words[i + 2].toLowerCase();
            	
            	System.out.printf("Started searching for words: %s, %s. With maximum distance of: %-2d\n", userWord1, userWord2, userSpace);
            	for (int j = 1; j <= fileNum; j++) {
            		LinkedList<Integer> answer = intersectWithSkips(positionalIndex.get(userWord1).get(j), positionalIndex.get(userWord2).get(j), userSpace);
            		if (answer != null && answer.size() > 0) {
            			System.out.printf("For file number: %-3d The indexes for words %s and %s, with %d max spaces in-between are: ", j, userWord1, userWord2, userSpace);
                		System.out.print(answer + "\n");
            			//outWriter.printf("For file number: %-3d The indexes for words %s and %s, with %d max spaces in-between are: ", j, userWord1, userWord2, userSpace);
                		//outWriter.print(answer + "\n\n");
            		}
            	}
            	System.out.print("\n");
            }
            System.out.print("Would you like to test another proximity query? Y/N ");
            userExit = userIn.nextLine();
            if (!userExit.equals("Y") || !userExit.equals("y")) {
            	end = true;
            	System.out.println("\nExiting system.");
            }
        }
        
    	
        // close input reader
        userIn.close();
        // close the writer for testing cases
        //indexWriter.close();
        //outWriter.close();
        
	} // main()
} // class

/**
 * 
 * Testing input of more than one query below

Input files directory path name is: C:\Users\Evan\Desktop\CSCI4130\EclipseStuff\AssignmentTwo\src\Good
Start parsing words.
Stop parsing words.
Please enter strings separated by an integer of the maximum distance between words:
For example: united 0 states 2 engaged
united 1 states 5 then
Started searching for words: united, states. With maximum distace of: 1 
For file number: 17  The indexes for words united and states, with 1 max spaces inbetween are: [4781]
For file number: 31  The indexes for words united and states, with 1 max spaces inbetween are: [4432]
For file number: 37  The indexes for words united and states, with 1 max spaces inbetween are: [20]
For file number: 42  The indexes for words united and states, with 1 max spaces inbetween are: [8299]
For file number: 51  The indexes for words united and states, with 1 max spaces inbetween are: [34, 46, 698]
For file number: 84  The indexes for words united and states, with 1 max spaces inbetween are: [18]
For file number: 100 The indexes for words united and states, with 1 max spaces inbetween are: [7878]
For file number: 109 The indexes for words united and states, with 1 max spaces inbetween are: [3298]
For file number: 116 The indexes for words united and states, with 1 max spaces inbetween are: [50]
For file number: 209 The indexes for words united and states, with 1 max spaces inbetween are: [5566]
For file number: 240 The indexes for words united and states, with 1 max spaces inbetween are: [703, 9993]
For file number: 244 The indexes for words united and states, with 1 max spaces inbetween are: [24, 42, 62, 80, 96]
For file number: 253 The indexes for words united and states, with 1 max spaces inbetween are: [842]
For file number: 256 The indexes for words united and states, with 1 max spaces inbetween are: [445]
For file number: 257 The indexes for words united and states, with 1 max spaces inbetween are: [425]
For file number: 259 The indexes for words united and states, with 1 max spaces inbetween are: [341]
For file number: 260 The indexes for words united and states, with 1 max spaces inbetween are: [40, 797]
For file number: 262 The indexes for words united and states, with 1 max spaces inbetween are: [789, 1528]
For file number: 263 The indexes for words united and states, with 1 max spaces inbetween are: [823]
For file number: 264 The indexes for words united and states, with 1 max spaces inbetween are: [865]
For file number: 269 The indexes for words united and states, with 1 max spaces inbetween are: [38, 860]
For file number: 273 The indexes for words united and states, with 1 max spaces inbetween are: [91, 702]
For file number: 277 The indexes for words united and states, with 1 max spaces inbetween are: [521]
For file number: 278 The indexes for words united and states, with 1 max spaces inbetween are: [843]
For file number: 280 The indexes for words united and states, with 1 max spaces inbetween are: [340]
For file number: 281 The indexes for words united and states, with 1 max spaces inbetween are: [668]
For file number: 282 The indexes for words united and states, with 1 max spaces inbetween are: [696]
For file number: 283 The indexes for words united and states, with 1 max spaces inbetween are: [718]
For file number: 284 The indexes for words united and states, with 1 max spaces inbetween are: [866]
For file number: 285 The indexes for words united and states, with 1 max spaces inbetween are: [818]
For file number: 286 The indexes for words united and states, with 1 max spaces inbetween are: [880]
For file number: 287 The indexes for words united and states, with 1 max spaces inbetween are: [883]
For file number: 288 The indexes for words united and states, with 1 max spaces inbetween are: [740]
For file number: 289 The indexes for words united and states, with 1 max spaces inbetween are: [865]
For file number: 290 The indexes for words united and states, with 1 max spaces inbetween are: [832]
For file number: 292 The indexes for words united and states, with 1 max spaces inbetween are: [707, 4307, 8247]
For file number: 293 The indexes for words united and states, with 1 max spaces inbetween are: [473]
For file number: 297 The indexes for words united and states, with 1 max spaces inbetween are: [381]
For file number: 298 The indexes for words united and states, with 1 max spaces inbetween are: [782]
For file number: 299 The indexes for words united and states, with 1 max spaces inbetween are: [817]
For file number: 300 The indexes for words united and states, with 1 max spaces inbetween are: [429]
For file number: 301 The indexes for words united and states, with 1 max spaces inbetween are: [436]
For file number: 302 The indexes for words united and states, with 1 max spaces inbetween are: [780]
For file number: 303 The indexes for words united and states, with 1 max spaces inbetween are: [63, 702]
For file number: 304 The indexes for words united and states, with 1 max spaces inbetween are: [877]
For file number: 305 The indexes for words united and states, with 1 max spaces inbetween are: [832]
For file number: 306 The indexes for words united and states, with 1 max spaces inbetween are: [911]
For file number: 307 The indexes for words united and states, with 1 max spaces inbetween are: [742]
For file number: 309 The indexes for words united and states, with 1 max spaces inbetween are: [415]
For file number: 310 The indexes for words united and states, with 1 max spaces inbetween are: [803]
For file number: 311 The indexes for words united and states, with 1 max spaces inbetween are: [383]
For file number: 312 The indexes for words united and states, with 1 max spaces inbetween are: [802]
For file number: 313 The indexes for words united and states, with 1 max spaces inbetween are: [737]
For file number: 314 The indexes for words united and states, with 1 max spaces inbetween are: [392]
For file number: 315 The indexes for words united and states, with 1 max spaces inbetween are: [350]
For file number: 316 The indexes for words united and states, with 1 max spaces inbetween are: [857]
For file number: 317 The indexes for words united and states, with 1 max spaces inbetween are: [820]
For file number: 321 The indexes for words united and states, with 1 max spaces inbetween are: [855]
For file number: 322 The indexes for words united and states, with 1 max spaces inbetween are: [958]
For file number: 323 The indexes for words united and states, with 1 max spaces inbetween are: [430]
For file number: 325 The indexes for words united and states, with 1 max spaces inbetween are: [378]
For file number: 327 The indexes for words united and states, with 1 max spaces inbetween are: [849]
For file number: 328 The indexes for words united and states, with 1 max spaces inbetween are: [855]
For file number: 330 The indexes for words united and states, with 1 max spaces inbetween are: [935]
For file number: 332 The indexes for words united and states, with 1 max spaces inbetween are: [373]
For file number: 333 The indexes for words united and states, with 1 max spaces inbetween are: [816]
For file number: 334 The indexes for words united and states, with 1 max spaces inbetween are: [412]
For file number: 335 The indexes for words united and states, with 1 max spaces inbetween are: [778]
For file number: 336 The indexes for words united and states, with 1 max spaces inbetween are: [725]
For file number: 338 The indexes for words united and states, with 1 max spaces inbetween are: [861]
For file number: 340 The indexes for words united and states, with 1 max spaces inbetween are: [808]
For file number: 341 The indexes for words united and states, with 1 max spaces inbetween are: [873]
For file number: 342 The indexes for words united and states, with 1 max spaces inbetween are: [396]
For file number: 343 The indexes for words united and states, with 1 max spaces inbetween are: [823]
For file number: 345 The indexes for words united and states, with 1 max spaces inbetween are: [775]
For file number: 387 The indexes for words united and states, with 1 max spaces inbetween are: [5402]
For file number: 392 The indexes for words united and states, with 1 max spaces inbetween are: [48]
For file number: 413 The indexes for words united and states, with 1 max spaces inbetween are: [32, 35, 76]
For file number: 429 The indexes for words united and states, with 1 max spaces inbetween are: [7971]
For file number: 450 The indexes for words united and states, with 1 max spaces inbetween are: [25]
For file number: 458 The indexes for words united and states, with 1 max spaces inbetween are: [1578]

Started searching for words: states, then. With maximum distace of: 5 
For file number: 33  The indexes for words states and then, with 5 max spaces inbetween are: [154]
For file number: 36  The indexes for words states and then, with 5 max spaces inbetween are: [207]
For file number: 63  The indexes for words states and then, with 5 max spaces inbetween are: [286, 927]
For file number: 73  The indexes for words states and then, with 5 max spaces inbetween are: [1079]
For file number: 113 The indexes for words states and then, with 5 max spaces inbetween are: [1906]
For file number: 120 The indexes for words states and then, with 5 max spaces inbetween are: [327]
For file number: 131 The indexes for words states and then, with 5 max spaces inbetween are: [4233]
For file number: 142 The indexes for words states and then, with 5 max spaces inbetween are: [3011]
For file number: 157 The indexes for words states and then, with 5 max spaces inbetween are: [441, 3213]
For file number: 164 The indexes for words states and then, with 5 max spaces inbetween are: [1150, 1162, 1472, 2507, 2527, 2885, 2970, 3011]
For file number: 168 The indexes for words states and then, with 5 max spaces inbetween are: [307]
For file number: 261 The indexes for words states and then, with 5 max spaces inbetween are: [5894]
For file number: 271 The indexes for words states and then, with 5 max spaces inbetween are: [155, 374, 386, 400, 408, 671, 695, 709, 735, 752, 763, 797, 813, 830, 965, 968, 1112, 1176, 1325, 1354, 1365, 1373, 1389, 1404, 1421, 1528, 1600, 1664, 1814, 1843, 1854, 1862, 1878, 1893, 1910, 2017, 2089, 2153]
For file number: 276 The indexes for words states and then, with 5 max spaces inbetween are: [310]
For file number: 339 The indexes for words states and then, with 5 max spaces inbetween are: [3225]
For file number: 354 The indexes for words states and then, with 5 max spaces inbetween are: [1435]
For file number: 358 The indexes for words states and then, with 5 max spaces inbetween are: [1234]
For file number: 361 The indexes for words states and then, with 5 max spaces inbetween are: [158, 459, 527]
For file number: 380 The indexes for words states and then, with 5 max spaces inbetween are: [1246, 1704, 1992]
For file number: 386 The indexes for words states and then, with 5 max spaces inbetween are: [459, 922, 1308]
For file number: 446 The indexes for words states and then, with 5 max spaces inbetween are: [1409]

Would you like to test another proximity query? Y/N N

Exiting system.


 * 
 */
