import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.sql.Timestamp;

public class gameInfo {
	int maxIncorrectGuesses = 6;
	int guessesCount = 0;
	int incorrectGuessesCount = 0;
	boolean guessedWord = false;
	boolean gameOver = false;
	int userScore = 0;
	String userName = "";
	
	int hangmanState = 0;
	String[] hangmanPack = { 
			"  +---+\r\n"
			+ "  |   |\r\n"
			+ "      |\r\n"
			+ "      |\r\n"
			+ "      |\r\n"
			+ "      |\r\n"
			+ "=========",
			
			"  +---+\r\n"
			+ "  |   |\r\n"
			+ "  O   |\r\n"
			+ "      |\r\n"
			+ "      |\r\n"
			+ "      |\r\n"
			+ "=========",
			
			"  +---+\r\n"
			+ "  |   |\r\n"
			+ "  O   |\r\n"
			+ "  |   |\r\n"
			+ "      |\r\n"
			+ "      |\r\n"
			+ "=========",
			
			"  +---+\r\n"
			+ "  |   |\r\n"
			+ "  O   |\r\n"
			+ " /|   |\r\n"
			+ "      |\r\n"
			+ "      |\r\n"
			+ "=========",
			
			"  +---+\r\n"
			+ "  |   |\r\n"
			+ "  O   |\r\n"
			+ " /|\\  |\r\n"
			+ "      |\r\n"
			+ "      |\r\n"
			+ "=========",
			
			"  +---+\r\n"
			+ "  |   |\r\n"
			+ "  O   |\r\n"
			+ " /|\\  |\r\n"
			+ " /    |\r\n"
			+ "      |\r\n"
			+ "=========",
			
			"  +---+\r\n"
			+ "  |   |\r\n"
			+ "  O   |\r\n"
			+ " /|\\  |\r\n"
			+ " / \\  |\r\n"
			+ "      |\r\n"
			+ "========="};
	
	String currentWord = "";
	String currentWordMasked = "";
	List<String> guessedLetters = new ArrayList<>();
	List<String> guessedWords = new ArrayList<>();
	String[] wordList = {"angry", "broken", "complex", "development", "error", "fixed"};
	String[] alphabet = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};

	
	// For optional requirement 2
	int sessionWins = 0;
	int sessionLosses = 0;
	
	public gameInfo(int w, int l) {
		sessionWins = w;
		sessionLosses = l;
	}
	
	public void selectRandomWord() {
		Random r = new Random();
		int randomIndex = r.nextInt(wordList.length);
		currentWord = wordList[randomIndex];
		
		// In same function will create the masked word too which is displayed to user
		for (int i = 0; i < currentWord.length(); i++) {
			currentWordMasked += "_";
		  }
		
	}
	
	public List<Object> validateGuess(String guess) {	
		String[] guessArray = guess.split("");
		
		// Check through each char of guess string and check if its a letter
		for (int i = 0; i < guessArray.length; i++) {
			if (!Arrays.asList(alphabet).contains(guessArray[i].toLowerCase())) {
				return Arrays.asList(false, "Invalid attempt, please only use letters in your guess.");
			}
		}
		
		// Check if guess was empty
		if (guess.length() == 0) {
			return Arrays.asList(false, "Invalid attempt, you cant guess nothing.");
		}
		
		// Check if guess was already used
		if (guess.length() == 1) {
			if (guessedLetters.contains(guess.toLowerCase())) {
				return Arrays.asList(false, "Invalid attempt, you have already guessed that letter.");
			}
		} else {
			if (guessedWords.contains(guess.toLowerCase())) {
				return Arrays.asList(false, "Invalid attempt, you have already guessed that word.");
			}
		}
		
		return Arrays.asList(true, "");

	}
	
	public void revealLetter(String letter) {
		String[] currentWordMaskedArray = currentWordMasked.split("");
		String[] currentWordArray = currentWord.split("");	
		
		// Loops through unmasked word to check which letters match the guessed letter
		for (int i = 0; i < currentWord.length(); i++) {
			if (currentWordArray[i].toLowerCase().equals(letter.toLowerCase())) {
				// replaces index of found letter(s) with guessed letter
				currentWordMaskedArray[i] = letter.toLowerCase();
			}
	
		  }
		
		// Join up array of new masked array and replace value
		currentWordMasked = String.join("", currentWordMaskedArray);
	}
	
	public String prettyPrintMaskedWord() {
		// Wanted to display masked word with spaces for easier readability
		String[] currentWordMaskedArray = currentWordMasked.split("");
		String prettyMaskedWord = "";
		
		for (int i = 0; i < currentWordMasked.length(); i++) {
			prettyMaskedWord += currentWordMaskedArray[i].toUpperCase()+" ";
			}
		prettyMaskedWord = prettyMaskedWord.substring(0, prettyMaskedWord.length() - 1); // Remove the last " " char from the word that was left from the loop.
		
		return prettyMaskedWord;
	}
	
	public void calculateScore() {
		// Formula is (lives remaining) * (no of unique letters in word) = score
		int livesRemaining = 0;
		List<String> uniqueLetters = new ArrayList<>();
		
		// Get lives remaining
		livesRemaining = maxIncorrectGuesses - incorrectGuessesCount;
		
		// Get unique letters
		String[] currentWordArray = currentWord.split("");
		
		// First go through each letter
		for (int i = 0; i < currentWord.length(); i++) {
			if (!uniqueLetters.contains(currentWordArray[i].toLowerCase())) {
				uniqueLetters.add(currentWordArray[i].toLowerCase());
			}	
		}
		
		// Do score calculation
		userScore = livesRemaining * uniqueLetters.size();
		
	}
	
	// Read leaderboard file
	public List<Object> readLeaderboardFile() {
		
		// Return object in format:
		// gotleaderboard? (bool) , jsonObject (jsonobject) , error msg (string)
		
		//JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader("leaderboard.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            JSONArray leaderboardList = (JSONArray) obj;
            
            return Arrays.asList(true, leaderboardList, "");

        } catch (FileNotFoundException e) {
        	return Arrays.asList(false, null, "Failed to locate leaderboard.json file.");
        } catch (IOException e) {
        	return Arrays.asList(false, null, "Failed to read leaderboard.json file.");
        } catch (ParseException e) {
        	return Arrays.asList(false, null, "Failed to parse leaderboard.json file.");
        }
		
	}
	
	
	// Prettyprint leaderboard
	public void displayLeaderboard() {
		List<Object> readLeaderboardData;
		JSONArray leaderboardData;
		
		// Read leaderboard data from file
		readLeaderboardData = readLeaderboardFile();
		
		// Check if it was able to read it
		if ( !(boolean) readLeaderboardData.get(0) ) {
			System.out.println("Error getting leaderboard: "+ (String) readLeaderboardData.get(2));
		
		} else {
			leaderboardData = (JSONArray) readLeaderboardData.get(1);
				
			System.out.println("\n      Leaderboard Stats:");
			System.out.println("===============================");
			
			// Sort leaderboard
			leaderboardData = sortLeaderboard(leaderboardData);
			
			// Loop through each leaderboard entry
			leaderboardData.forEach(player -> {
			    JSONObject playerObj = (JSONObject) player;
			    System.out.println("\t  "+playerObj.get("score")+"  -  "+playerObj.get("nickname"));
			});
			
			System.out.println("===============================");
		}
	}
	
	// Sort leaderboard data
	public JSONArray sortLeaderboard(JSONArray leaderboardData) {
		JSONArray sortedData = new JSONArray();
		List<Long> sortedScores = new ArrayList<Long>();
		List<Long> lookedAtPlayers = new ArrayList<Long>();
		
		// Add all user scores to list to be later sorted
		leaderboardData.forEach(player -> {
		    JSONObject playerObj = (JSONObject) player;
		    sortedScores.add( (long) playerObj.get("score"));
		});
		
		// Sorts the scores from greatest to lowest
		Collections.sort(sortedScores, Collections.reverseOrder());
		
		// Go through all the scores, from the top person
		for(int i = 0; i < sortedScores.size();i++ ) {

			long temp = sortedScores.get(i); // Store indexed score in variable so it can be accessible by forEach below
			leaderboardData.forEach(player -> {
			    JSONObject playerObj = (JSONObject) player;
				if ((temp == (long) playerObj.get("score")) && !lookedAtPlayers.contains((long) playerObj.get("timestamp"))) {
			    	// Add them to new array
			    	sortedData.add(playerObj);
			    	lookedAtPlayers.add((long) playerObj.get("timestamp")); // Could create a new "primary key" in the leaderboard such as ID but no two users can have same timestamp so works just as fine. 
			    }
			});
        }
			
		return sortedData;
				
	}
	
	// Save to leaderboard file
	public void saveLeaderboardFile(JSONArray leaderboardData) {
		// Get player timestamp
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	
		// Assign user data to object
		JSONObject playerToAdd = new JSONObject();
		playerToAdd.put("nickname", userName);
		playerToAdd.put("score", (long) userScore);
		playerToAdd.put("timestamp", timestamp.getTime()); // Gets timestamp as a long variable 
		
		// Add to existing data
		leaderboardData.add(playerToAdd);
		
		// Attempt to save to database file
		try (FileWriter file = new FileWriter("leaderboard.json")) {
            //We can write any JSONArray or JSONObject instance to the file
            file.write(leaderboardData.toJSONString()); 
            file.flush();
            
        } catch (IOException e) {  // Only check for IOException this time since any other errors other than failure to write wouldve waterfalled into error handling earlier on
        	System.out.println("Error saving your score: Failed to write to leaderboard.json");	
        }
				
	}
	
	
}
