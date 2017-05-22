package edu.brown.cs.dixit.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

import edu.brown.cs.dixit.gameManagement.DixitGame;
import edu.brown.cs.dixit.gameManagement.GameTracker;
import edu.brown.cs.dixit.main.WebSockets;

public class LobbyTest {

  @Test
  public void testNew() {
    // TODO: Test not null:
    GameTracker gt = new GameTracker();
    int gameid = gt.createGameID();
    DixitGame newGame = new DixitGame(gameid, 4, 10, "lobby");
    gt.addGame(newGame);
    newGame.addPlayer("testid", "testname");
    newGame.addPlayer("test2id", "name2name");
    DixitGame popped = gt.getGame(gameid);
    assertTrue(popped.getCapacity() == 4);
    assertTrue(popped.getName() == "lobby");
    assertTrue(popped.getNumPlayers() == 2);
  }

  @Test
  public void testRemove() throws ClassNotFoundException, SQLException {
    // TODO: Test not null:
    GameTracker gt = new GameTracker();
    WebSockets ws = new WebSockets();
    int gameid = gt.createGameID();
    DixitGame newGame = new DixitGame(gameid, 4, 10, "lobby");
    gt.addGame(newGame);
    newGame.addPlayer("testid", "testname");
    newGame.addPlayer("test2id", "test2name");
    DixitGame popped = gt.getGame(gameid);
    assertFalse(ws.checkGame("testid"));
    assertTrue(popped.getNumPlayers() == 1);
    assertTrue(ws.checkGame("test2id"));
    assertEquals(gt.getGame(gameid), null);
  }

  @Test
  public void testSTDetail() {
    GameTracker gt = new GameTracker();
    WebSockets ws = new WebSockets();
    int gameid = gt.createGameID();
    DixitGame newGame = new DixitGame(gameid, 4, 10, "lobby");
    gt.addGame(newGame);
    newGame.addPlayer("testid", "testname");
    newGame.addPlayer("test2id", "name2name");
    DixitGame popped = gt.getGame(gameid);
    String stid = ws.getSTdetails(popped).get("user_name").toString();
    assertEquals(stid.substring(1, stid.length() - 1), "testname");
  }

}
