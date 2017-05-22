package edu.brown.cs.dixit.gameManagement;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

/**
 * Tracks all DixitGame for concurrency.
 * 
 * @author sangha
 *
 */
public class GameTracker {

  private static final Map<Integer, DixitGame> gameInfo = new ConcurrentHashMap<>();
  private static final Map<String, Session> playerSession = new ConcurrentHashMap<>();
  private Set<Integer> idset = new HashSet<Integer>();

  /**
   * Create game id.
   * 
   * @return game id
   */
  public int createGameID() {
    for (int i = 1; i < 100; i++) {
      if (!idset.contains(i)) {
        idset.add(i);
        return i;
      }
    }
    return -1;
  }

  /**
   * Add Game to the hashmap.
   * 
   * @param game
   *          DixitGame object
   */
  public void addGame(DixitGame game) {
    gameInfo.put(game.getId(), game);
  }

  /**
   * Get game from the hashmap.
   * 
   * @param gameId
   *          game id
   * @return DixitGmae
   */
  public DixitGame getGame(int gameId) {
    return gameInfo.get(gameId);
  }

  /**
   * Get capacity of given DixitGame.
   * 
   * @param gameId
   *          game id
   * @return capacity of the game
   */
  public int getCapacity(int gameId) {
    return gameInfo.get(gameId).getNumPlayers();
  }

  /**
   * Add session-player id pair to the hashmap.
   * 
   * @param playerId
   *          player id
   * @param session
   *          websocket session
   */
  public void addSession(String playerId, Session session) {
    playerSession.put(playerId, session);
    System.out.printf("add session %s, %d \n", playerId, session.hashCode());
    for (Session value : playerSession.values()) {
      System.out.println(value.hashCode());
    }
  }

  /**
   * Check if the session of the player is open or closed.
   * 
   * @param playerId
   *          player id
   * @return boolean value
   */
  public boolean checkOpenSession(String playerId) {
    Session sess = playerSession.get(playerId);
    if (sess == null || !sess.isOpen()) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Get session of a player.
   * 
   * @param playerId
   *          player id
   * @return websocket session
   */
  public Session getSession(String playerId) {
    return playerSession.get(playerId);
  }

  /**
   * Get entire session hashmap.
   * 
   * @return hashmap of player to session
   */
  public Map<String, Session> getSession() {
    return playerSession;
  }

  /**
   * Get all DixitGames
   * 
   * @return hashmap of id to DixitGmame
   */
  public Map<Integer, DixitGame> getAllGame() {
    return gameInfo;
  }
}
