package edu.brown.cs.dixit.setting;

import java.util.HashMap;
import java.util.Map;

/**
 * Models a referee that keeps track of the game and scores, for the game logic to work.
 * Keeps track of many variables of theh game.
 * @author jongjekim
 *
 */
public class Referee {
  
  private int numPlayers;
  private int victoryPoint;
  private String gameWon;
  private Map<String, Integer> chosen; 
  private Map<String, Integer> pickRecord; 
  private Map<String, Integer> result;
  private Map<String, Integer> scoreBoard;
  private Turn gameTurn;
  
  /**
   * Constructor initializes the private variables.
   */
  public Referee() {
    numPlayers = Setting.NUM_DEFAULT_PLAYERS;
    victoryPoint = Setting.NUM_DEFAULT_VICTORY_POINT;
    gameWon = "";
    chosen = new HashMap<String, Integer>();
    pickRecord = new HashMap<String, Integer>();
    scoreBoard = new HashMap<String, Integer>();
  }
  
  /** 
   * Constructor taking in the victorypoint, size of game and turn. 
   * sets variables accordingly.
   * @param cap the cap of the game
   * @param victPoint victory point limit
   * @param turn turn
   */
  public Referee(int cap, int victPoint, Turn turn) {
    numPlayers = cap;
    victoryPoint = victPoint;
    gameWon = "";
    chosen = new HashMap<String, Integer>();
    pickRecord = new HashMap<String, Integer>();
    scoreBoard = new HashMap<String, Integer>();
    gameTurn = turn;
  }
  
  /**
   * Receves storyprompt from storyteller to keep track of.
   * @param prompt prompt given
   * @param playerId id of storyteller
   * @param cardId id of card
   */
  public void receiveStory(String prompt, String playerId, int cardId) {
    setPrompt(prompt);
    setAnswer(cardId);
  }
  
  /**
   * Receives vote from the guessers.
   * @param id id of the guesser
   * @param pick picked card
   */
  public void receiveVotes(String id, int pick) {
     pickRecord.put(id, pick);
  }
  
  /**
   * Calculates the scores of the game, for each player with game logic.
   * @return result mapping player to score
   */
  public Map<String, Integer> tallyScores() {
    //reset result
    result = new HashMap<String, Integer>();
    System.out.println("result size: " + result.size());
    int count_answer = 0;
    int pickedCard = 0;
    //Points for other players
    System.out.println("pick record size: " + pickRecord.size());
    for (String key: pickRecord.keySet()) {    
      System.out.println("user id: " + key);
      pickedCard = pickRecord.get(key);
      if (pickedCard == getAnswer()) {
        if (result.containsKey(key)) {
          result.put(key, result.get(key) + 3);
        } else {
          result.put(key, 3);
        }
        count_answer += 1;  
      } else {
        result.put(key, 0);
      }
    }
    
    for (String key: pickRecord.keySet()) {
        for (String keyTwo: chosen.keySet()) {
          if (chosen.get(keyTwo) == pickedCard && !keyTwo.equals(key)) {
            if (result.containsKey(keyTwo)) {
              result.put(keyTwo, result.get(keyTwo) + 1);
            } else {
              result.put(keyTwo, 1);
            }
          }
        }
    }
    
    System.out.println("result size before st: " + result.size());
    //Point for Story-teller
    
    if (count_answer == pickRecord.size()) {
      result.put(gameTurn.getCurrTeller(), 0);
      for (String key: pickRecord.keySet()) {
        result.put(key, 2);
      }
    } else if (count_answer == 0) {
      result.put(gameTurn.getCurrTeller(), 0);
      for (String key: pickRecord.keySet()) {
        if (result.containsKey(key)) {
          result.put(key,  result.get(key) + 2);
        } else {
          result.put(key, 2);
        }
      }
    } else {
      result.put(gameTurn.getCurrTeller(), 3);
    }
    
    System.out.println("result size after st: " + result.size());
    
    //reset 
    pickRecord = new HashMap<String, Integer>();
    chosen = new HashMap<String, Integer>();
    
    //need to check if the game ended
    for (String key: result.keySet()) {
      int newScore = scoreBoard.get(key) + result.get(key);
      scoreBoard.put(key, newScore);
      if (newScore >= victoryPoint) {
        gameWon = key;
        for (String id: scoreBoard.keySet()) {
          scoreBoard.put(id, 0);
        }
      break;
      }
    }
    return result;
  }
 
  /**
   * Getter for the answer.
   * @return answer of the game
   */
  public int getAnswer() {
    return gameTurn.getAnswer();
  }
  
/**
 * Setter for the answer.
 * @param cardId id of the answer.
 */
  public void setAnswer(int cardId) {
    gameTurn.setAnswer(cardId);
  }
  
  /**
   * Getter for the storyteller of the game.
   * @return storyteller of game.
   */
  public String getStoryTeller() {
    return gameTurn.getCurrTeller();
  }
  
  /**
   * Increments the turn by adding 1.
   */
  public void incrementTurn() {
    gameTurn.incrementTurn();
  }
  
  /**
   * Getter for the victorypoint of the game.
   * @return victoryPoint victorypoint of game.
   */
  public int getVictoryPoint() {
    return victoryPoint;
  }
  
  /**
   * Getter for the prompt of game.
   * @return prompt prompt of game.
   */
  public String getPrompt() {
    return gameTurn.getPrompt();
  }
  
  /**
   * Setter for the pormpt.
   * @param prompt prompt received.
   */
  public void setPrompt(String prompt) {
    gameTurn.setPrompt(prompt);
  }

  /**
   * getter for the result, the scoremap.
   * @return pickRecord the scoremap.
   */
  public Map<String, Integer> getPickRecord() {
    return pickRecord;
  }

  /*
   * setter for the pickRecord, inserting pair.
   * @param id id to insert
   * @param selectedCard selected card
   */
  public void setPickRecord(String id, int selectedCard) {
    pickRecord.put(id, selectedCard);
  }

  /**
   * Getter for ie of the pickRecord.
   * @return pickedRecord size of pickrecord.
   */
  public int getPickedSize() {
	return pickRecord.size();
  }
  
  /**
   * Setter for chosen card.
   * @param id id to change.
   * @param chosenCard chosen card.
   */	
  public void setChosen(String id, int chosenCard) {
    chosen.put(id, chosenCard);
  }
  
  /**
   * getter for size of chosen.
   * @return size of chosen.
   */
  public int getChosenSize() {
    return chosen.size();
  }
  
  /**
   * getter for the chosen card given id.
   * @param id id to query/
   * @return chosen card.
   */
  public Integer getChosen(String id) {
    return chosen.get(id);
  }
  
  /**
   * Getter for the current turn.
   * @return gameTurn turn of game
   */
  public Turn getTurn() {
    return gameTurn;
  }
  
  /** 
   * method to add board to scoreboard.
   * @param id id to insert
   * @param defPoint defpoint of game.
   */
  public void addBoard(String id, int defPoint) {
    scoreBoard.put(id, defPoint);
  }
  
  /**
   * Removal method for board given id.
   * @param id id to be removed.
   */
  public void removeBoard(String id){
	  scoreBoard.remove(id);
  }
  
  /**
   * Getter for the winner of game.
   * @return gameWon winner of game
   */
  public String getWinner() {
    return gameWon;
  }
  
  /**
   * Setter for winner of game given id.
   * @param id id of winner.
   */
  public void setWinner(String id) {
    gameWon = id;
  }
}
