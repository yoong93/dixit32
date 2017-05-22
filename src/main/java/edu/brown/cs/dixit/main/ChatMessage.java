package edu.brown.cs.dixit.main;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for one chat message transfer, to model the json object.
 * @author jongjekim
 *
 */
public class ChatMessage{
    public List<String> game;
    public List<String> username;
    public List<String> body;
    public List<Integer> time;

    /**
     * At initializatino of a chat mesage body, create a list for the gameId, usernames,
     * body and time to be transferred to clients as a json object.
     */
    public ChatMessage() {
    	game = new ArrayList<>();
    	username = new ArrayList<>();
    	body = new ArrayList<>();
    	time = new ArrayList<>();
    }
}