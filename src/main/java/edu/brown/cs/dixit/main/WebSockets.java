package edu.brown.cs.dixit.main;

import java.io.IOException;
import java.net.HttpCookie;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.brown.cs.dixit.gameManagement.DixitGame;
import edu.brown.cs.dixit.gameManagement.GameTracker;
import edu.brown.cs.dixit.setting.Card;
import edu.brown.cs.dixit.setting.GamePlayer;
import edu.brown.cs.dixit.setting.Referee;

@WebSocket
public class WebSockets {
  private static final Gson GSON = new Gson();
  private static final GameTracker gt = new GameTracker();
  private static final Queue<Session> allSessions = new ConcurrentLinkedQueue<>();
  private Referee currRef;
  private static Connection conn = null;

  private static enum MESSAGE_TYPE {
    CONNECT, CREATE, JOIN, NEW_GAME, ALL_JOINED, ST_SUBMIT, GS_SUBMIT, ALL_GUESSES, VOTE, STATUS, MULTI_TAB, RESULTS, CHAT_UPDATE, CHAT_MSG, END_OF_ROUND, LOAD, RESTART, UPDATE_LOBBY, EXIT
  }

  /**
   * Connect to the DB that contains messages.
   * 
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void connectDB() throws ClassNotFoundException, SQLException {
    Class.forName("org.sqlite.JDBC");
    String urlToDB = "jdbc:sqlite:" + "chatroom.sqlite3";
    conn = DriverManager.getConnection(urlToDB);
    Statement stat = conn.createStatement();
    stat.executeUpdate("PRAGMA foreign_keys = ON;");

    PreparedStatement prep;
    prep = conn.prepareStatement("CREATE TABLE IF NOT EXISTS messages("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, game TEXT,"
            + "username TEXT, body TEXT, time INTEGER)");
    prep.executeUpdate();
    prep.close();

  }

  /**
   * Clear DB.
   * 
   * @param id
   *          id of the game room
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void clearDB(int id) throws ClassNotFoundException,
          SQLException {
	  if(conn!=null){
		  PreparedStatement prep;
		  prep = conn.prepareStatement("DELETE FROM messages where game = ?");
		  prep.setInt(1, id);
		  prep.executeUpdate();
		  prep.close();
		}
    }

  /**
   * save chatroom messages to the DB.
   * 
   * @param game
   *          gameId
   * @param username
   *          user name
   * @param body
   *          text
   * @param time
   *          time that message was sent
   * @throws SQLException
   */
  private void saveMessage(String game, String username, String body,
          Integer time) throws SQLException {
    PreparedStatement prep;
    prep = conn.prepareStatement(
            "INSERT INTO messages (game, username, body, time) VALUES (?, ?, ?, ?);");
    prep.setString(1, game);
    prep.setString(2, username);
    prep.setString(3, body);
    prep.setInt(4, time);
    prep.executeUpdate();
    prep.close();
  }

  /**
   * retrieve messages.
   * 
   * @param game
   *          gameId
   * @throws SQLException
   */
  private void getMessage(DixitGame game) throws SQLException {
    System.out.println("getmessage in java called with gameid " + game);
    ChatMessage message = new ChatMessage();
    PreparedStatement prep;
    prep = conn.prepareStatement(
            "SELECT game, username, body, time FROM messages WHERE game = ? ORDER BY time;");
    prep.setString(1, Integer.toString(game.getId()));
    ResultSet rs = prep.executeQuery();
    while (rs.next()) {
      message.game.add(rs.getString(1));
      message.username.add(rs.getString(2));
      message.body.add(rs.getString(3));
      message.time.add(rs.getInt(4));
    }
    prep.close();

    JsonObject chatMessage = new JsonObject();
    JsonObject chatPayload = new JsonObject();
    System.out.println("message is" + message);
    chatMessage.addProperty("type", MESSAGE_TYPE.CHAT_UPDATE.ordinal());
    chatPayload.addProperty("messages", GSON.toJson(message));
    chatMessage.add("payload", chatPayload);
    sendMsgToGame(chatMessage.toString(), game);
  }

  /**
   * connect to socket.
   * 
   * @param session
   *          session of the socket
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  @OnWebSocketConnect
  public void connected(Session session) throws IOException,
          ClassNotFoundException, SQLException {
    allSessions.add(session);
    System.out.print(session.hashCode());
    System.out.println("");
    List<HttpCookie> cookies = session.getUpgradeRequest().getCookies();
    if (cookies == null) {
      System.out.println(
              "cookie is null, should be redirected to beginning page");
    } else {
      this.updateCookies(session, cookies);
    }
    JsonObject connectMessage = new JsonObject();
    connectMessage.addProperty("type", MESSAGE_TYPE.CONNECT.ordinal());
    try {
      session.getRemote().sendString(connectMessage.toString());
    } catch (WebSocketException e) {
      System.out.println("remote can't be found");
    }
  }

  /**
   * Close websocket session.
   * 
   * @param session
   * @param statusCode
   * @param reason
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason)
          throws ClassNotFoundException, SQLException {
    System.out.println("session closed");
    boolean removed = false;
    List<HttpCookie> cookies = session.getUpgradeRequest().getCookies();
    if (cookies != null) {
      for (HttpCookie cookie : cookies) {
        if (cookie.getName().equals("userid")) {
          removed = checkGame(cookie.getValue());
        }
      }
    }
    if (removed) {
      loadLobbies(allSessions);
    }
    System.out.print(session.hashCode());
    System.out.println("");

    allSessions.remove(session);
  }

  /**
   * Set of actions according to the message type.
   * 
   * @param session
   *          websocket session
   * @param message
   *          type of action
   * @throws IOException
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  @OnWebSocketMessage
  public void message(Session session, String message) throws IOException,
          SQLException, ClassNotFoundException {
    JsonObject received = GSON.fromJson(message, JsonObject.class);
    JsonObject payload = received.getAsJsonObject("payload");
    MESSAGE_TYPE messageType = MESSAGE_TYPE.values()[received.get("type")
            .getAsInt()];
    DixitGame currGame = gt.getGame(this.getRoomId(session));
    GamePlayer teller;
    switch (messageType) {
    default:
      System.out.println(messageType.toString() + ": Unknown message type!");
      break;
    case CONNECT:
      String url = payload.get("url").getAsString();
      if (url.equals("http://localhost:4567/")) {
        boolean removed = false;
        List<HttpCookie> cookies = session.getUpgradeRequest().getCookies();
        if (cookies != null) {
          for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals("userid")) {
              removed = checkGame(cookie.getValue());
            }
          }
        }

        if (removed) {
          loadLobbies(allSessions);
        }
      }
      break;
    case LOAD:
      Queue<Session> q = new ConcurrentLinkedDeque<Session>();
      q.add(session);
      loadLobbies(q);
      break;
    case CREATE:
      int newGameId = gt.createGameID();
      DixitGame newGame = new DixitGame(newGameId, payload.get("num_players")
              .getAsInt(), payload.get("victory_pts").getAsInt(), payload.get(
                      "lobby_name").getAsString());
      gt.addGame(newGame);
      newGame.getDeck().initializeDeck("../img/img");
      // set Storyteller
      teller = createNewUser(session, newGame, payload.get("user_name")
              .getAsString());
      newGame.addStatus(teller.playerId(), "Storytelling");
      // send message
      JsonObject newGameMessage = new JsonObject();
      newGameMessage.addProperty("type", MESSAGE_TYPE.NEW_GAME.ordinal());
      JsonObject newGamePayload = new JsonObject();
      newGamePayload.addProperty("game_id", newGameId);
      newGamePayload.addProperty("lobby_name", payload.get("lobby_name")
              .getAsString());
      newGamePayload.addProperty("num_players", newGame.getNumPlayers());
      newGamePayload.addProperty("capacity", newGame.getCapacity());

      newGameMessage.add("payload", newGamePayload);

      for (Session indivSession : allSessions) {
        try {
          indivSession.getRemote().sendString(newGameMessage.toString());
        } catch (WebSocketException e) {
          System.out.println("skip to next session plz");
        }
      }

      creatorJoin(session);
      break;

    case JOIN:
      int gameId = payload.get("game_id").getAsInt();
      String user = payload.get("user_name").getAsString();
      DixitGame join = gt.getGame(gameId);
      GamePlayer joiner = createNewUser(session, join, user);
      join.addStatus(joiner.playerId(), "Waiting");
      updateLobby(gameId, join.getNumPlayers());
      break;

    case ST_SUBMIT:
      String prompt = payload.get("prompt").getAsString();

      int cardId = payload.get("card_id").getAsInt();
      String cardUrl = payload.get("card_url").getAsString();
      JsonObject stMessage = new JsonObject();
      stMessage.addProperty("type", MESSAGE_TYPE.ST_SUBMIT.ordinal());

      JsonObject stSubmitPayload = new JsonObject();
      stSubmitPayload.addProperty("prompt", prompt);
      stSubmitPayload.addProperty("card_id", cardId);
      stSubmitPayload.addProperty("card_url", cardUrl);
      stMessage.add("payload", stSubmitPayload);
      if (currGame != null) {
        System.out.println("curr:" + currGame);
        currRef = currGame.getRefree();
        currRef.receiveStory(prompt, currGame.getST(), cardId);
        currRef.setChosen(currGame.getST(), cardId);

        teller = currGame.getPlayer(currGame.getST());
        teller.removeCard(cardId);

        for (GamePlayer player : currGame.getPlayers()) {
          currGame.addStatus(player.playerId(), "Guessing");
        }
        currGame.addStatus(currGame.getST(), "Waiting");
        sendMsgToGame(stMessage.toString(), currGame);
        updateStatus(currGame);
      }
      break;

    case GS_SUBMIT:
      if (currGame != null) {
        System.out.println("Guess received");
        int guessedCard = payload.get("card_id").getAsInt();
        String userId = payload.get("user_id").getAsString();
        GamePlayer guesser = currGame.getPlayer(userId);
        guesser.removeCard(guessedCard);
        currRef = currGame.getRefree();
        currRef.setChosen(userId, guessedCard);

        System.out.println("num guesses: " + currRef.getChosenSize());
        System.out.println("capacity: " + currGame.getCapacity());

        if (currRef.getChosenSize() == currGame.getCapacity()) {
          System.out.println("all guesses received");
          JsonObject allGuessesMessage = new JsonObject();
          allGuessesMessage.addProperty("type", MESSAGE_TYPE.ALL_GUESSES
                  .ordinal());
          JsonObject guessesPayload = new JsonObject();
          JsonObject guessed = new JsonObject();
          guessesPayload.addProperty("answer", currRef.getAnswer());

          int temp = 0;
          for (String whoGuessed : currGame.getPlayerNames()) {
            if (!whoGuessed.equals(currGame.getST())) {
              guessed.addProperty(String.valueOf(temp), currRef.getChosen(
                      whoGuessed));
              temp += 1;
            }
          }

          guessesPayload.add("guessed", guessed);
          allGuessesMessage.add("payload", guessesPayload);
          sendMsgToGame(allGuessesMessage.toString(), currGame);

          for (GamePlayer player : currGame.getPlayers()) {
            currGame.addStatus(player.playerId(), "Voting");
          }
          currGame.addStatus(currGame.getST(), "Waiting");
          updateStatus(currGame);
        }
      }

      break;

    case VOTE:
      if (currGame != null) {
        System.out.println("Vote received");
        int vote = payload.get("card_id").getAsInt();
        String voterId = payload.get("user_id").getAsString();
        System.out.println("voter Id: " + voterId);
        System.out.println("voted cardId: " + vote);
        System.out.println("curr Game id: " + currGame.getId());
        GamePlayer voter = currGame.getPlayer(voterId);
        currGame.addStatus(voterId, "Voted");
        updateStatus(currGame);

        currRef = currGame.getRefree();
        currRef.receiveVotes(voterId, vote);

        JsonObject voteUpdate = new JsonObject();
        voteUpdate.addProperty("type", MESSAGE_TYPE.VOTE.ordinal());
        JsonObject voteInfo = new JsonObject();
        voteInfo.addProperty("card_id", vote);
        voteInfo.addProperty("user_name", voter.playerName());
        voteUpdate.add("payload", voteInfo);
        sendMsgToGame(voteUpdate.toString(), currGame);

        if (currRef.getPickedSize() == currGame.getCapacity() - 1) {
          System.out.println("all voting done!");
          Map<String, Integer> result = currRef.tallyScores();
          JsonObject voteResult = new JsonObject();
          voteResult.addProperty("type", MESSAGE_TYPE.RESULTS.ordinal());
          JsonObject resultInfo = new JsonObject();
          // new Storyteller
          currGame.nextTurn();
          JsonObject stInfo = getSTdetails(currGame);

          JsonObject points = new JsonObject();
          for (String key : result.keySet()) {
            points.addProperty(key, result.get(key));
          }
          resultInfo.add("points", points);
          JsonObject winner = new JsonObject();
          String winnerId = currRef.getWinner();
          winner.addProperty("winner_id", winnerId);
          currRef.setWinner("");
          if (winnerId.equals("")) {
            winner.addProperty("winner_name", "");
          } else {
            winner.addProperty("winner_name", currGame.getPlayer(winnerId)
                    .playerName());
          }
          resultInfo.add("winner", winner);
          System.out.println("number of chosen cards: " + currRef
                  .getChosenSize());
          System.out.println("pick record: " + currRef.getPickedSize());
          System.out.println("result size: " + result.size());
          for (String key : result.keySet()) {
            resultInfo.add("storyteller", stInfo);
            GamePlayer currPlayer = currGame.getPlayer(key);

            List<Card> personalDeck = currPlayer.refillCard();
            JsonObject hand = new JsonObject();
            for (int i = 0; i < personalDeck.size(); i++) {
              hand.addProperty(String.valueOf(i), personalDeck.get(i)
                      .toString());
            }
            resultInfo.add("hand", hand);
            voteResult.add("payload", resultInfo);
            try {
              gt.getSession(key).getRemote().sendString(voteResult.toString());
            } catch (WebSocketException e) {
              System.out.println("skip to next session plz");
            }
            System.out.println("userid from result: " + key);

            // update player statuses
            String currPlayerId = currPlayer.playerId();
            if (currPlayerId == currGame.getST()) {
              System.out.println(String.valueOf(currPlayer)
                      + " now storytelling");
              currGame.addStatus(currPlayerId, "Storytelling");
            } else {
              System.out.println(String.valueOf(currPlayer) + " now waiting");
              currGame.addStatus(currPlayerId, "Waiting");
            }
          }
          updateStatus(currGame);
        }
      }
      break;

    case CHAT_MSG:
      System.out.print("this far?");
      if (currGame != null) {
        System.out.println("chat_msg");
        String body = payload.get("body").getAsString();
        Integer time = payload.get("time").getAsInt();
        String username = this.getUsername(session);
        System.out.printf("%s, %d, %s \n", body, time, username);
        this.saveMessage(Integer.toString(currGame.getId()), username, body,
                time);
        this.getMessage(currGame);

      }
      break;

    case RESTART:
      if (currGame != null) {
        currGame.incrementRestart();
        System.out.println(currGame.getRestart());
        if (currGame.getRestart() == currGame.getCapacity()) {
          currGame.resetGame();
          allJoined(currGame.getPlayers(), currGame);
          updateStatus(currGame);
        }
      }
      break;

    case EXIT:
      JsonObject leaveIntent = new JsonObject();
      leaveIntent.addProperty("type", MESSAGE_TYPE.EXIT.ordinal());
      sendMsgToGame(leaveIntent.toString(), currGame);
      break;

    }
  }

  /**
   * Retrieve user id.
   * 
   * @param s
   *          websocket session
   * @return userid
   */
  private String getUserId(Session s) {
    List<HttpCookie> cookies = s.getUpgradeRequest().getCookies();
    for (HttpCookie crumb : cookies) {
      if (crumb.getName().equals("userid")) {
        return crumb.getValue();
      }
    }
    return "";
  }

  /**
   * Retrieve game room id.
   * 
   * @param s
   *          websocket session
   * @return roomid
   */
  private int getRoomId(Session s) {
    List<HttpCookie> cookies = s.getUpgradeRequest().getCookies();
    if (cookies != null) {
      for (HttpCookie crumb : cookies) {
        if (crumb.getName().equals("gameid")) {
          return Integer.parseInt(crumb.getValue());
        }
      }
    }
    return -1;
  }

  /**
   * Retrieve user name
   * 
   * @param s
   *          websocket session
   * @return username
   */
  private String getUsername(Session s) {
    String userId = getUserId(s);
    int gameId = getRoomId(s);
    DixitGame currGame = gt.getGame(gameId);
    System.out.println("this eneteered");
    String username = "no player found";
    System.out.println("cookie is");
    if (currGame != null) {
      System.out.print("game is not null!");
      username = currGame.getPlayer(userId).playerName();
    }
    return username;
  }

  /**
   * Generated random UUID.
   * 
   * @return id
   */
  private String randomId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Create new user for particular game.
   * 
   * @param s
   *          websocket session
   * @param game
   *          DixitGame object
   * @param user_name
   *          username
   * @return
   */
  private GamePlayer createNewUser(Session s, DixitGame game,
          String user_name) {
    if (game.getCapacity() > game.getNumPlayers()) {
      List<HttpCookie> cookies = new ArrayList<HttpCookie>();
      String id = randomId();
      cookies.add(new HttpCookie(Network.USER_IDENTIFER, id));
      cookies.add(new HttpCookie(Network.GAME_IDENTIFIER, Integer.toString(game
              .getId())));
      gt.addSession(id, s);
      GamePlayer newPlayer = game.addPlayer(id, user_name);
      sendCookie(s, cookies);
      return newPlayer;
    }
    return null;
  }

  /**
   * Send messages to all players in the game.
   * 
   * @param message
   *          JsonObject of message in string
   * 
   * @param game
   *          DixitGame object
   */
  private void sendMsgToGame(String message, DixitGame game) {
    for (GamePlayer player : game.getPlayers()) {
      try {
        gt.getSession(player.playerId()).getRemote().sendString(message);
      } catch (WebSocketException | IOException e) {
        System.out.println("skip to next session plz");
      }
    }
  }

  /**
   * Send cookies back to the front-end for each player.
   * 
   * @param s
   *          websocket session
   * @param cookies
   *          list of HttpCookie
   */
  private void sendCookie(Session s, List<HttpCookie> cookies) {
    JsonObject json = new JsonObject();
    JsonObject jsonCookie = new JsonObject();
    json.addProperty("type", "set_uid");
    jsonCookie.add("cookies", GSON.toJsonTree(cookies));
    json.add("payload", jsonCookie);
    try {
      s.getRemote().sendString(json.toString());
    } catch (IOException | WebSocketException e) {
      System.out.println("Found IOException while sending cookie");
    }

  }

  /**
   * Update statuses of players and sends the message to front end.
   * 
   * @param game
   *          DixitGame object
   */
  private void updateStatus(DixitGame game) {
    if (game != null) {
      JsonObject statusMessage = new JsonObject();
      JsonObject statusPayload = new JsonObject();
      statusMessage.addProperty("type", MESSAGE_TYPE.STATUS.ordinal());
      List<String> playerIds = new ArrayList<>();
      List<String> statuses = new ArrayList<>();

      for (GamePlayer user : game.getPlayers()) {
        playerIds.add(user.playerId());
        statuses.add(game.getStatus(user.playerId()));
      }

      statusPayload.addProperty("player_ids", GSON.toJson(playerIds));
      statusPayload.addProperty("statuses", GSON.toJson(statuses));

      statusMessage.add("payload", statusPayload);
      sendMsgToGame(statusMessage.toString(), game);

    }
  }

  /**
   * Update cookies for redirect and rejoin.
   * 
   * @param session
   *          websocket session
   * @param cookies
   *          list of HttpCookie
   */
  private void updateCookies(Session session, List<HttpCookie> cookies) {
    DixitGame currGame = null;
    try {
      for (HttpCookie crumb : cookies) {
        if (crumb.getName().equals("gameid")) {
          currGame = gt.getGame(Integer.parseInt(crumb.getValue()));
        }
        if (crumb.getName().equals("userid")) {
          String userId = crumb.getValue();
          // System.out.printf("session added to %s %d \n", userId,
          // session.hashCode());
          gt.addSession(userId, session);
        }
      }
      if (currGame != null) {
        System.out.println("num players currently: " + currGame.getPlayers()
                .size());
        if (currGame.getPlayers().size() == currGame.getCapacity()) {
          System.out.println("all joined");
          Collection<GamePlayer> users = currGame.getPlayers();
          allJoined(users, currGame);
          updateStatus(currGame);
        }
      }

    } catch (NullPointerException e) {
      // TODO Auto-generated catch block
      System.out.println("Find how to refresh Browser");

    }
  }

  /**
   * Retrieve relevant info about storyteller.
   * 
   * @param currGame
   *          DixitGame object
   * @return JsonObject of storyteller's id and name
   */
  public JsonObject getSTdetails(DixitGame currGame) {
    JsonObject stInfo = new JsonObject();
    GamePlayer st = currGame.getPlayer(currGame.getST());
    stInfo.addProperty("user_name", st.playerName());
    stInfo.addProperty("user_id", st.playerId());
    return stInfo;
  }

  /**
   * Redirect for room creating player.
   * 
   * @param s
   *          websocket session
   */
  private void creatorJoin(Session s) {
    JsonObject joinGameMessage = new JsonObject();
    joinGameMessage.addProperty("type", MESSAGE_TYPE.JOIN.ordinal());
    try {
      s.getRemote().sendString(joinGameMessage.toString());
    } catch (WebSocketException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Remove game that's already joined by everybody.
   * 
   * @param userid
   *          userId of the player
   * @return boolean value
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public boolean checkGame(String userid) throws ClassNotFoundException,
          SQLException {
    boolean removed = false;
    for (int gameKey : gt.getAllGame().keySet()) {
      DixitGame game = gt.getGame(gameKey);
      for (GamePlayer player : game.getPlayers()) {
        if (player.playerId().equals(userid)) {
          game.removePlayer(player);
          if (game.getNumPlayers() == 0) {
            removed = true;
            clearDB(game.getId());
            System.out.println("gt remove game!!!");
            gt.getAllGame().remove(game.getId());
          }
        }
      }
    }
    return removed;

  }

  /**
   * Send info about game lobbies to the front end.
   * 
   * @param sessions
   *          websocket session
   */
  private void loadLobbies(Queue<Session> sessions) {
    JsonObject loadGameMessage = new JsonObject();
    loadGameMessage.addProperty("type", MESSAGE_TYPE.LOAD.ordinal());
    JsonObject loadGamePayload = new JsonObject();
    if (!gt.getAllGame().keySet().isEmpty()) {
      JsonArray gameArray = new JsonArray();
      JsonObject gameJson = new JsonObject();
      for (int gameKey : gt.getAllGame().keySet()) {
        DixitGame loadedGame = gt.getAllGame().get(gameKey);
        gameJson.addProperty("id", loadedGame.getId());
        gameJson.addProperty("name", loadedGame.getName());
        gameJson.addProperty("capacity", loadedGame.getCapacity());
        gameJson.addProperty("player", loadedGame.getNumPlayers());
        System.out.printf("id: %d, numplayers : %d, name : %s \n", loadedGame
                .getId(), loadedGame.getNumPlayers(), loadedGame.getName());
      }
      gameArray.add(gameJson);
      loadGamePayload.add("gamearray", gameArray);
      loadGameMessage.add("payload", loadGamePayload);

    } else {
      loadGamePayload.addProperty("gamearray", "none");
      loadGameMessage.add("payload", loadGamePayload);
    }
    for (Session indivSession : sessions) {
      try {
        indivSession.getRemote().sendString(loadGameMessage.toString());
      } catch (WebSocketException | IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Update lobby if any players join or leave.
   * 
   * @param gameId
   *          id of the game
   * @param numPlayers
   *          number of players currently joined
   */
  private void updateLobby(int gameId, int numPlayers) {
    JsonObject updateLMessage = new JsonObject();
    updateLMessage.addProperty("type", MESSAGE_TYPE.UPDATE_LOBBY.ordinal());
    JsonObject updateLPayload = new JsonObject();
    updateLPayload.addProperty("id", gameId);
    updateLPayload.addProperty("players", numPlayers);
    updateLMessage.add("payload", updateLPayload);
    for (Session indivSession : allSessions) {
      try {
        indivSession.getRemote().sendString(updateLMessage.toString());
      } catch (WebSocketException | IOException e) {
        // TODO Auto-generated catch block
        allSessions.remove(indivSession);
      }
    }
  }

  /**
   * Distribute cards and start the game if all players are joined.
   * 
   * @param users
   *          players in the game
   * @param currGame
   *          DixitGame object
   */
  private void allJoined(Collection<GamePlayer> users, DixitGame currGame) {
    JsonObject players = new JsonObject();
    int playerCount = 0;
    for (GamePlayer user_temp : users) {
      currGame.addStatus(user_temp.playerId(), "Waiting");
      playerCount++;
      JsonObject player = new JsonObject();
      player.addProperty("user_name", user_temp.playerName());
      player.addProperty("user_id", user_temp.playerId());
      players.add(String.valueOf(playerCount), player);
    }
    currGame.addStatus(currGame.getST(), "Storytelling");

    for (GamePlayer user : users) {

      // define new ALL_JOINED message object
      JsonObject allJoinedMessage = new JsonObject();
      JsonObject playerInfo = new JsonObject();
      allJoinedMessage.addProperty("type", MESSAGE_TYPE.ALL_JOINED.ordinal());

      // add information about all players
      playerInfo.add("players", players);

      // add hand information
      List<Card> personalDeck = user.getFirstHand();
      JsonObject hand = new JsonObject();
      for (int i = 0; i < personalDeck.size(); i++) {
        hand.addProperty(String.valueOf(i), personalDeck.get(i).toString());
      }
      playerInfo.add("hand", hand);

      // add information about storyteller
      JsonObject stInfo = getSTdetails(currGame);
      playerInfo.add("storyteller", stInfo);

      // send message to all players
      try {
        allJoinedMessage.add("payload", playerInfo);
        System.out.println("all messages sent");
        gt.getSession(user.playerId()).getRemote().sendString(allJoinedMessage
                .toString());
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}
