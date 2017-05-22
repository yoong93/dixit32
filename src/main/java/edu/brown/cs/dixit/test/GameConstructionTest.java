package edu.brown.cs.dixit.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import edu.brown.cs.dixit.gameManagement.DixitGame;

public class GameConstructionTest {

  @Test
  public void DixitGameConstruction() {
    DixitGame a = new DixitGame(1, 4, 10, "hello");
    assertNotNull(a);
    assertEquals(a.getCapacity(), 4);
    assertEquals(a.getId(), 1);
    assertEquals(a.getName(), "hello");
  }

  @Test
  public void AddPlayers() {
    DixitGame a = new DixitGame(1, 4, 10, "hello");
    assertNotNull(a);
    a.addPlayer("id", "name1");
    assertEquals(a.getNumPlayers(), 1);
    a.addPlayer("id2", "name2");
    assertEquals(a.getNumPlayers(), 2);
    a.addPlayer("id3", "name3");
    a.addPlayer("id4", "name4");
    assertEquals(a.getNumPlayers(), 4);
  }

  @Test
  public void RemovePlayers() {
    DixitGame a = new DixitGame(1, 4, 10, "hello");
    assertNotNull(a);
    a.addPlayer("id", "name1");
    assertEquals(a.getNumPlayers(), 1);
    a.removePlayer(a.getPlayer("id"));
    assertEquals(a.getNumPlayers(), 0);
    a.addPlayer("id3", "name3");
    a.addPlayer("id4", "name4");
    assertEquals(a.getNumPlayers(), 2);
  }
}
