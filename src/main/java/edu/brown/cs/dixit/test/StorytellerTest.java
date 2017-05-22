package edu.brown.cs.dixit.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.brown.cs.dixit.gameManagement.DixitGame;

public class StorytellerTest {
  @Test
  public void StoryTeller() {
    DixitGame a = new DixitGame(1, 4, 10, "hello");
    a.addPlayer("id", "name1");
    a.addPlayer("id2", "name2");
    a.addPlayer("id3", "name3");
    a.addPlayer("id4", "name4");
    assertEquals(a.getST(), "id");
  }

  @Test
  public void StoryTellerTurn() {
    DixitGame a = new DixitGame(1, 4, 10, "hello");
    a.addPlayer("id", "name1");
    a.addPlayer("id2", "name2");
    a.addPlayer("id3", "name3");
    a.addPlayer("id4", "name4");
    a.nextTurn();
    assertEquals(a.getST(), "id2");
    a.nextTurn();
    assertEquals(a.getST(), "id3");
  }
}
