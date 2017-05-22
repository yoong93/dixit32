package edu.brown.cs.dixit.gameManagement;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.brown.cs.dixit.setting.Deck;
import edu.brown.cs.dixit.setting.GamePlayer;
import edu.brown.cs.dixit.setting.Referee;
import edu.brown.cs.dixit.setting.Turn;

public class DixitGame {

  private final int id;
  private final int capacity;
  private int restartVote;
  private Deck deck;
  private final String name;
  private Map<String, GamePlayer> players;
  private Referee referee;
  private Map<String, String> playerStatus;

  /**
   * Wrapper class that contains all the information of a DixitGame.
   * 
   * @param gameID
   *          game id
   * @param cap
   *          number of players that can join
   * @param victPoint
   *          victory point of the game
   * @param gameName
   *          name of the game room
   */
  public DixitGame(int gameID, int cap, int victPoint, String gameName) {
    id = gameID;
    name = gameName;
    capacity = cap;
    deck = new Deck();
    players = new ConcurrentHashMap<>();
    referee = new Referee(cap, victPoint, new Turn(cap));
    playerStatus = new ConcurrentHashMap<>();
  }

  /**
   * Get game room id.
   * 
   * @return game room id
   */
  public int getId() {
    return id;
  }

  /**
   * Get game capacity.
   * 
   * @return capacity
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Get current size of players.
   * 
   * @return number of players
   */
  public int getNumPlayers() {
    return players.size();
  }

  /**
   * Get card deck of the game.
   * 
   * @return deck
   */
  public Deck getDeck() {
    return deck;
  }

  /**
   * Add player to the DixitGame.
   * 
   * @param id
   *          player id
   * @param name
   *          player name
   * @return GamePlayer object
   */
  public GamePlayer addPlayer(String id, String name) {
    GamePlayer new_player = new GamePlayer(id, name, deck);
    players.put(id, new_player);
    referee.getTurn().addPlayers(new_player);
    referee.addBoard(id, 0);
    return new_player;
  }

  /**
   * Remove player in the DixitGame.
   * 
   * @param player
   *          GamePlayer object
   */
  public void removePlayer(GamePlayer player) {
    players.remove(player.playerId());
    referee.getTurn().removePlayers(player);
    referee.removeBoard(player.playerId());
    playerStatus.remove(player.playerId());
  }

  /**
   * Get player with id.
   * 
   * @param id
   *          player id
   * @return GamePlayer object
   */
  public GamePlayer getPlayer(String id) {
    return players.get(id);
  }

  /**
   * Get all players.
   * 
   * @return Collection of players
   */
  public Collection<GamePlayer> getPlayers() {
    return players.values();
  }

  /**
   * Get player names.
   * 
   * @return Set of names
   */
  public Set<String> getPlayerNames() {
    return players.keySet();
  }

  /**
   * Get referee of the Dixit Game.
   * 
   * @return
   */
  public Referee getRefree() {
    return referee;
  }

  /**
   * Get storyteller id.
   * 
   * @return id
   */
  public String getST() {
    return referee.getStoryTeller();
  }

  /**
   * Increment turn.
   */
  public void nextTurn() {
    referee.incrementTurn();
  }

  /**
   * Add status of the player.
   * 
   * @param id
   *          player id
   * @param status
   *          current status of the player
   */
  public void addStatus(String id, String status) {
    playerStatus.put(id, status);
  }

  /**
   * Retrieve status of the player.
   * 
   * @param id
   *          player id
   * @return status
   */
  public String getStatus(String id) {
    return playerStatus.get(id);
  }

  /**
   * Get game's name.
   * 
   * @return name of the game
   */
  public String getName() {
    return name;
  }

  /**
   * Reset game to re-start the game.
   */
  public void resetGame() {
    deck = new Deck();
    deck.initializeDeck("../img/img");
    for (GamePlayer user : getPlayers()) {
      user.resetHand(deck);
    }
    getRefree().getTurn().setTurn(0);
    resetStart();
  }

  /**
   * Get restart votes.
   * 
   * @return votes
   */
  public int getRestart() {
    return restartVote;
  }

  /**
   * Increment re-start votes.
   */
  public void incrementRestart() {
    restartVote += 1;
  }

  /**
   * Reset re-start votes
   */
  public void resetStart() {
    restartVote = 0;
  }
}
