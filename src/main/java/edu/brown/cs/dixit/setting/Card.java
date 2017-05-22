package edu.brown.cs.dixit.setting;

/**
 * Models the card coponent of the game, which consists the visual card.
 * Contains reference to the id, url to image and a toString method to be usd
 * in sending information. 
 * @author jongjekim
 *
 */
public class Card {

  private final int id;
  private final String imgLink;
  
  /**
   * Constructor takes in card id and url, and sets variables.
   * @param id id of card
   * @param imgLink url link of picture
   */
  public Card(int id, String imgLink) {
    this.id = id;
    this.imgLink = imgLink; 
    }


  
  /*
   * Getter for id.
   * @return id of card
   */
  public int getId() {
    return id;
  }
  
  /*
   * Getter for the image url.
   * @return imgLink url of image
   */
  public String getImgLink() {
    return imgLink;
  }
  
  /**
   * Overriden toString method to transfer data as json.
   * @return string including id and url 
   */
  @Override
  public String toString() {
    return "id:" + id + ":url:" + imgLink;    
  }

}
