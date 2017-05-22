package edu.brown.cs.dixit.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Models a player in the game, who has refernece to his
 * id, name, hands as well as dek.
 * @author jongjekim
 *
 */
public class GamePlayer implements Player {
  
  private final String playerId;
  private final String playerName;
  private List<Card> playerDeck;
  private Deck globalDeck;
 
  /**
   * Constructor initializes player with the id, name and initial deck.
   * @param id id of player
   * @param name name of player
   * @param deck deck of player.
   */
  public GamePlayer(String id, String name, Deck deck) {
    playerId = id;
    playerName = name;
    playerDeck = new ArrayList<>();
    globalDeck = deck;
  }
  
  
  /**
   * Getter for the id of player.
   * @return playerId id of player
   */
  @Override
  public String playerId() {
    return playerId;
  }

  /**
   * Getter for the name of player.
   * @return playrName name of plyer
   */
  @Override
  public String playerName() {
    return playerName;
  }
  
/**
 * Method to get the first hand of the player.
 * @return playerDeck deck of the player
 */
  public List<Card> getFirstHand() {
    Random rand = new Random();
    while (playerDeck.size() < 6) {
      int drawNumber = rand.nextInt(globalDeck.getDeckSize());
      playerDeck.add(globalDeck.drawCard(drawNumber));
    }
    return playerDeck;
  }
  
  /**
   * method to remove card of particular Id from the deck.
   * @param cardId id of card to be removed.
   */
  public void removeCard(int cardId) {
    Card removed = null;
    for (Card c: playerDeck) {
      if (c.getId() == cardId) {
        removed = c;
      }
    }
    playerDeck.remove(removed);
  }
  
  /**
   * method to refill card, by netting a new card and adding it to deck.
   * @return playerDeck deck with added card
   */
  public List<Card> refillCard() {
    Random rand = new Random();
    Card newCard;
    if (globalDeck.getDeckSize() > 0) {
      int drawNumber = rand.nextInt(globalDeck.getDeckSize());
      newCard = globalDeck.drawCard(drawNumber);
      playerDeck.add(newCard);
    }
    
    return playerDeck;
  }
  
  /**
   * Method to reset the hand with the given deck, by setting global deck to the parameter.
   * @param deck
   */
  public void resetHand(Deck deck) {
    globalDeck = deck;
  }
}
