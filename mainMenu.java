import java.util.Scanner;  // Import the Scanner class to allow for user input

import org.json.simple.JSONArray; // Allow me to do use JSON arrays

import java.util.InputMismatchException; // Allow me catch input mismatch errors
import java.util.Arrays; // Allow me to do use arrays
import java.util.List; // Allow me to do use lists

public class mainMenu {

	public static void main(String[] args) {
		// Call start game function, done in separate function to make restarting/playing again easier
		startScreen();
	}
	
	static void startScreen() {
		// Start the game
		Boolean validInput = false; // Create variable to control input loops if invalid response is given
		Scanner input = new Scanner(System.in);  // Create a Scanner object
		int menuChoice = 0; // Initialised to 0 rather than empty as it wouldn't let me do comparison without it
		
		System.out.println("Welcome to Hangman [B].");
		
		while (!validInput) {
			// Show main menu, let user pick option
			System.out.println("\n1) Play");
			System.out.println("2) Instructions");
			System.out.println("3) Exit");
			
			try {
				menuChoice = input.nextInt();  // Read user input
				if (menuChoice > 0 && menuChoice <= 3) {
					validInput = true;
				} else {
					System.out.println("Invalid response, please try again.");
				}
			} catch (InputMismatchException e) {
				System.out.println("Invalid response, please try again.");
			}
			input.nextLine(); // Clears input buffer to avoid infinite loop using previous user-entered value
	
		}

		if (menuChoice == 3 ) {
			// Exit program
			System.out.println("\nSee you later!");
			
		} else if (menuChoice == 2) {
			// Print Instructions text
			System.out.println("\n--------------------------------------------------------------------------------------");
			System.out.println("This is hangman - an interactive word guessing game.\n\nYour job is to guess the word displayed on the screen in least amount of turns.\nYou have a limited amount of guesses before the hangman is fully created and you lose.\nYou can guess both single letters and full words, depending on how confident you are.\n\nGuessing 1 wrong letter = 1 bad guess\nGuessing wrong word = 2 bad guesses\n\nAt the end your score will be recorded to see how you match up against your friends.\n\nGood luck!!!");
			System.out.println("--------------------------------------------------------------------------------------\n");
			startScreen(); // return them to the start of main menu where they have option to choose to play or exit
			
		} else {
			// Could've use == 1 but wouldn't matter has results are filtered beforehand to only be on of 3 options.
			startGame(0,0);
			
		}
		
		input.close();
	}
	
	static void startGame(int w, int l) {
		List<Object> readLeaderboardData; // Initialise object to use when loading in leaderboard
		Scanner input = new Scanner(System.in); 
		gameInfo gameObject = new gameInfo(w,l); // Initialise game object - start with default settings
		gameObject.selectRandomWord(); // Select random word
	
		// Game starting
		while (!gameObject.gameOver) {
			boolean validGuess = false;
			String userGuess = "";
			List<Object> guessInfo; // Opted for list object to allow for different types of variables
			
			// Import current game data from the gameInfo object
			System.out.println("\n\n"+gameObject.hangmanPack[gameObject.hangmanState]+"\n"); // Display hangman ASCII image
			System.out.println("Secret Word: "+ gameObject.prettyPrintMaskedWord()); // Display hidden word + any correct guesses
			System.out.println("Guesses made: "+ gameObject.guessesCount); // Display number of guesses they made, regardless of right or wrong
			
			while (!validGuess) { // Loops guess-prompt until user provides a valid answer
				System.out.println("\nPlease Enter your guess below:");
				userGuess = input.nextLine();
				guessInfo = gameObject.validateGuess(userGuess); // Return object formatted as is this valid guess? (bool), error message (str)
				validGuess = (Boolean) guessInfo.get(0); // Puts value of index 0 and specifies type
				if (!validGuess) {
					System.out.println((String) guessInfo.get(1)); // Prints out a customized message depending on type of error
				}
				
			}
			
			// Check if guess was correct
			gameObject.guessesCount += 1;
			
			if (userGuess.length() == 1) {
				// If user guessed a letter
				
				// add letter to list of letters that have been guessed
				gameObject.guessedLetters.add(userGuess.toLowerCase());
				
				if ((gameObject.currentWord.toLowerCase()).indexOf(userGuess.toLowerCase()) != -1) { // -1 = not found
					gameObject.revealLetter(userGuess); // Update masked word variable
					System.out.println("\n-------------------------------");
					System.out.println("You correctly guessed a letter!");
					System.out.println("-------------------------------\n");
					
					// Check if word is complete
					if (gameObject.currentWord.toLowerCase().equals(gameObject.currentWordMasked.toLowerCase())) {
						gameObject.guessedWord = true;
						gameObject.gameOver = true;
					}
					
				} else {
					// Wrong guess on letter
					gameObject.hangmanState += 1;
					gameObject.incorrectGuessesCount += 1;
					System.out.println("\n-----------------------------");
					System.out.println("Sorry, that wasn't correct :(");
					System.out.println("-----------------------------\n");
					
					// Check if all guesses used
					if (gameObject.incorrectGuessesCount == gameObject.maxIncorrectGuesses) {
						gameObject.gameOver = true;
					}
				}
				
			} else {
				// If user guessed a word
				
				// add word to list of words that have been guessed
				gameObject.guessedWords.add(userGuess.toLowerCase());
				
				if (userGuess.toLowerCase().equals(gameObject.currentWord.toLowerCase())) { // can't use == on strings
					// They guessed word!
					gameObject.guessedWord = true;
					gameObject.gameOver = true;
					
				} else {
					// Add extra guess since they attempted to do a word
					gameObject.guessesCount += 1;
					gameObject.incorrectGuessesCount += 2;
					gameObject.hangmanState += 2;
					System.out.println("\n-----------------------------");
					System.out.println("Sorry, that wasn't correct :(");
					System.out.println("-----------------------------\n");
					
					// Check if all guesses used
					if (gameObject.incorrectGuessesCount >= gameObject.maxIncorrectGuesses) { // did greater/equal rather than just equal as increments are not just 1
						gameObject.gameOver = true;
					}
				}
			}
	
		}
		
		// Calculate score
		gameObject.calculateScore();
		
		// Check if they guessed word
		if (gameObject.guessedWord) {
			System.out.println("Congrats, you guessed the word which was: "+gameObject.currentWord);
			System.out.println("You scored: "+gameObject.userScore);
			gameObject.sessionWins += 1;
			
			// Ask if they would like to save their score
			String saveScoreInput = "";
			System.out.println("\nWould you like to save your score? (y/n)");
			saveScoreInput = input.nextLine();
			
			
			if ((saveScoreInput.toLowerCase()).equals("y")) {
				// Do the save score system
				readLeaderboardData = gameObject.readLeaderboardFile();
	
				if ( !(boolean) readLeaderboardData.get(0) ) { // Checks if there was an error reading the leaderboard file
					System.out.println("Error getting leaderboard: "+ (String) readLeaderboardData.get(2));
				} else {
					// Ask user for their information
					System.out.println("\nPlease enter your nickname below:");
					gameObject.userName = input.nextLine();
					
					if (gameObject.userName.equals("")) { // Quick check to see if value is empty to replace with placeholder
						gameObject.userName = "PLAYER";
					}
					
					// Save user score
					gameObject.saveLeaderboardFile((JSONArray) readLeaderboardData.get(1));
					
				}
				
			} 
			
		} else {
			System.out.println("Sorry, but you did not guess the word which was: "+gameObject.currentWord);
			gameObject.sessionLosses += 1;
		}
		
		// Display leaderboard
		gameObject.displayLeaderboard();
		
		// Display score vs computer
		System.out.println("\n      Your Session Stats:");
		System.out.println("-------------------------------");
		System.out.println("    YOU       vs      COM");
		System.out.println("     "+gameObject.sessionWins+"\t\t       "+gameObject.sessionLosses);
		System.out.println("-------------------------------\n");
		
		// Ask if they want to play again
		String playAgainInput = "";
		System.out.println("\nWould you like to play again? (y/n)");
		playAgainInput = input.nextLine();
		if ((playAgainInput.toLowerCase()).equals("y")) {
			// Check if they won
			if (gameObject.guessedWord) {
				// Passes through updated stats for when new game object is created.
				startGame(gameObject.sessionWins, gameObject.sessionLosses);
			} else {
				startGame(gameObject.sessionWins, gameObject.sessionLosses);
			}
		} else {
			// They don't want to play again
			System.out.println("\nSorry to see you go :( - Hope you had fun!");
		}
		
		
	}
	
}
