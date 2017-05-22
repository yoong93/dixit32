package edu.brown.cs.dixit.setting;

import java.util.ArrayList;
import java.util.List;

/**
 * Models the deck of cards, which is a list of cards that would be used
 * throughout the game. Contains methods to initialize and draw deck, and 
 * to keep size of the deck.
 * @author jongjekim
 *
 */
public class Deck {
  // default deck of 40 cards
  private int SIZE = 84;
  private List<Card> deck;
  
  /**
   * Constructor initializes an empty array.
   */
  public Deck () {
    deck = new ArrayList<>();
  }
  
  /**
   * Initializes the initial deck of cards, of size. Instantiates cards with
   * images in the path and pushes them into the deck (list).
   * @param imgLink path to the image
   */
  public void initializeDeck(String imgLink) {
    // need to call the imgLink for every card
    for (int i = 1; i < SIZE + 1; i++) {
      String eachLink = imgLink + String.valueOf(i) + ".jpg";
      Card temp = new Card(i, eachLink);
      deck.add(temp);
    }
  }
 
  /**
   * Method to draw the card at index.
   * @param number index
   * @return card removed
   */
  public Card drawCard(int number) {
    return deck.remove(number);
  }

  /**
   * Getter for deck size.
   * @return size of deck
   */
  public int getDeckSize() {
    return deck.size();
  }
}