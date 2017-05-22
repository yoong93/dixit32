package edu.brown.cs.dixit.setting;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a turn in the game, with the number of players, current turn, players
 * @author jongjekim
 *
 */
public class Turn {
  
  private final int numPlayer;
  private int currTurn;
  private List<GamePlayer> players;
  private String prompt;
  private int answer;
  
  /**
   * Constructor initializes each variable, and sets number of players to the paramteer.
   * @param number number of players
   */
  public Turn(int number) {
    numPlayer = number;
    currTurn = 0;
    players = new ArrayList<>();
    prompt = "";
  }
  
  /**
   * Method to add a player to the game (lsit of players).
   * @param user player to be added.
   */
  public void addPlayers(GamePlayer user) {
    players.add(user);
  }
  
  /**
   * Method to remove player from the list of players.
   * @param player player to be removed.
   */
  public void removePlayers(GamePlayer player){
	  players.remove(player);
  }
  
  /**
   * Getter method for the current teller/
   * @return current teller's id.
   */
  public String getCurrTeller() {
    return players.get(currTurn).playerId();
  }
 
  /**
   * Increment method for the turn, as turn moves on.
   */
  public void incrementTurn() {
    if (currTurn == numPlayer - 1) {
      currTurn = 0;
    } else {
      currTurn += 1;
    }
  }
  
  /**
   * Getter for the prompt.
   * @return prompt.
   */
  public String getPrompt() {
    return prompt;
  }
  
  /**
   * Setter for the prompt.
   * @param newPrompt new prompt to be changed to.
   */
  public void setPrompt(String newPrompt) {
    prompt = newPrompt;
  }
  
  /**
   * Getter for the answer.
   * @return answer gotten.
   */
  public int getAnswer() {
    return answer;
  }
  
  /**
   * Setter for the answer.
   * @param newAnswer new answer to be set.
   */
  public void setAnswer(int newAnswer) {
    answer = newAnswer;
  }
  
  /**
   * Setter for the turn.
   * @param zero integer to set current turn to.
   */
  public void setTurn(int zero) {
    currTurn = zero;
  }
}
