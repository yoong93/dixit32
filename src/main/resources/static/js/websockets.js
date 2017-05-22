/* websockets.js
 * This file outlines the actions taken by the front-end when a message is received via the websocket.
 */
const MESSAGE_TYPE = {

  CONNECT: 0,
  CREATE: 1,
  JOIN: 2,
  NEW_GAME: 3,
  ALL_JOINED: 4,
  ST_SUBMIT: 5,
  GS_SUBMIT: 6,
  ALL_GUESSES: 7,
  VOTE: 8,
  STATUS: 9,
  MULTI_TAB: 10,
  RESULTS: 11,
  CHAT_UPDATE: 12,
  CHAT_MSG: 13,
  END_OF_ROUND: 14,
  LOAD:15,
  RESTART: 16,
  UPDATE_LOBBY: 17,
  EXIT: 18
};

//declares 
let conn;
let storyteller = -1;
let myId = -1;

//set up socket connection and define types
const setup_update = () => {
  conn = new WebSocket("ws://localhost:4567/connect");
  //conn = new WebSocket("ws://104.196.191.156/connect");  
	conn.onerror = err => {
    	console.log('Connection error:', err);
  };

  // defines the actions taken by the front-end upon receipt of a specific message from the back end
  conn.onmessage = msg => {
    const data = JSON.parse(msg.data);
    const payload = data.payload;
    const table = $("#lobbyt tbody");
    switch (data.type) {
      default:
        console.log('Unknown message type!', data.type);
        break;
      
      case MESSAGE_TYPE.CONNECT:
        let currurl = window.location.toString();
        const urlMessage = {
          type: MESSAGE_TYPE.CONNECT,
          payload: {
            url: currurl
          }
        }
        conn.send(JSON.stringify(urlMessage));
        break;
        
      // update the lobby's occupancy when a player joins 
      case MESSAGE_TYPE.UPDATE_LOBBY:
        let updateid = payload.id;
        let updatenum = payload.players;
        let cols = document.getElementById('lobbyt').getElementsByTagName('td'), colslen = cols.length, i = 1;
        while (i < colslen) {
          cols[i].innerHTML = updatenum + "/" + cols[i].innerHTML.split("/")[1];
          i+=2;
        }
        break;
        
      case MESSAGE_TYPE.MULTI_TAB:
        alert('multi tab opened! Only one tab is allowed');
      
      // store the user ID and game ID in the browser's cookies
      case "set_uid":
        updateCookie(payload.cookies[0].name, payload.cookies[0].value)
        updateCookie(payload.cookies[1].name, payload.cookies[1].value)
        break;
      
      // retrieve created games from memory
      case MESSAGE_TYPE.LOAD:
        table.html("");
        if(payload.gamearray != "none"){
          for(let game in payload.gamearray){
            table.append("<tr><td class=\"" + payload.gamearray[game].id + "\">" + payload.gamearray[game].name + "</td><td id=\"num_players\" class=\"" + payload.gamearray[game].id + "\">" + payload.gamearray[game].player + "/" + payload.gamearray[game].capacity + "</td></tr>");
          }
        }
        break;
        
      // appends a new game to the lobby at the login page
      case MESSAGE_TYPE.NEW_GAME:
        table.append("<tr><td class=\"" + payload.game_id + "\">" + payload.lobby_name + "</td><td id=\"num_players\" class=\"" + payload.game_id + "\">" + payload.num_players + "/" + payload.capacity + "</td></tr>");
        break;
      
      // redirects the player's screen to the game page once the player creates/joins a game
      case MESSAGE_TYPE.JOIN:
        window.location = window.location.href + "play";
        break;
      
      // initializes the UI interface for both the storyteller and the guessers, once all players have joined
      case MESSAGE_TYPE.ALL_JOINED:
        
        // initialize the board and personal hands
        prepareBoard();
        initHand(payload.hand);
        
        // initiailize the information in the sidebar
        const players = payload.players;
        $("#scoreboard-body").empty();
        for (player of Object.keys(players)) {
          let player_name = players[player].user_name;
          let player_id = players[player].user_id;
          $("#scoreboard-body").append("<tr><td>" + player_name + "</td><td id=\"" + player_id + "status\"></td><td id=\"" + player_id + "points\">0</td></tr>");
          myId = getElementFromCookies("userid");
          if (myId == player_id) {
            $("#user-name").html(player_name);
          }
        }
        
        setStoryTeller(payload.storyteller);
        setStatus("Storytelling");
        
        myId = getElementFromCookies("userid");
        if (myId == storyteller) {
          initStorytellerBoard($("#board"));
        } else {
          initGuesserBoard($("#board"));
        }
        
        break;
        
      // displays the storyteller's prompt
      case MESSAGE_TYPE.ST_SUBMIT:
        let prompt = payload.prompt;
        let cardId = payload.card_id;
        let cardUrl = payload.card_url;
        $("#promptValue").html("\"" + prompt + "\"" );
        setStatus("Guessing");
        myId = getElementFromCookies("userid");
        if (myId != storyteller) {
          startTimer(15);  
        }
        break;
      
      // updates the statuses of the players displayed on the sidebar according to those outlined by the
      // status message
      case MESSAGE_TYPE.STATUS:
    	  let statusMap = {};
    	  let statuses = JSON.parse(data.payload.statuses);
    	  let playerIds = JSON.parse(data.payload.player_ids);

    	  for (let i = 0; i < statuses.length; i ++) {
    		  statusMap[playerIds[i]] = statuses[i];
    	  }
        updateStatus(statusMap);
        break;
    	
      // displays all the guessed cards' once all of them have been received
      case MESSAGE_TYPE.ALL_GUESSES:
        setStatus("Voting");
        displaySelectedCards(payload);
    	  

        myId = getElementFromCookies("userid");
        if (myId != storyteller) {
          startTimer(30);  
        }
        
        break;
      
      // appends the voters name to the voted card
      case MESSAGE_TYPE.VOTE:
        let imgId = payload.card_id;
        let votedCardDiv = $("#" + imgId).parent().find(".voters");
        votedCardDiv.append("<span class=\"voter\">" + payload.user_name + "</span>");
        break;
      
      // updates all scores for the game, and concludes the game if someone wins
      case MESSAGE_TYPE.RESULTS:
        updatePoints(payload.points);
        if (payload.winner.winner_id != "") {
          displayWinner(payload.winner);
        } else {
          displayPoints(payload.points);
          setTimeout(function() { newRound(payload); }, 5000);
        }
        
    	  break;
      
      // updates the chat room with the most recently sent chat
      case MESSAGE_TYPE.CHAT_UPDATE:

        let messages = JSON.parse(payload.messages);
        let length = messages.username.length;
        $(".chatList").empty();
        for (let i = 0; i < length ; i ++ ) {
            $(".chatList").append("<li> <span style=\"color: grey\">" + messages.username[i] + "</span> : " + messages.body[i]  + "</li>");
        } 
        $(".chatList").scrollTop($(".chatList")[0].scrollHeight);
        break;
        
    	// notifies the player that someone in their lobby has left the game

      case MESSAGE_TYPE.EXIT:
          $("#exit-message").modal({
            backdrop: 'static', 
            keyboard: false
          });
          break;
    }

  };
}

/*
 * This function sets the unique user-id of the user and stores it in his/her browser's cookies
 * so that it is retained on redirect of the apge.
 * @params object containing game and user ID info, data
 */
function setuserid(data){
  for(let i=0;i<data.cookies.length; i++){
    if(data.cookies[i].name == "userid"){
      const cook = data.cookies[i];
      setCookie(cook.name, cook.value);
      myId = cook.value;
    }
    if(data.cookies[i].name == "gameid"){
      const cook = data.cookies[i];
      setCookie(cook.name, cook.value);
    }
  }
}

/*
 * This function helps update users' exiting cookies, to contain information about the user ID and game ID.
 * @params name of the cookie, cookiename and value of the cookie, cookievalue
 */
function updateCookie(cookiename, cookievalue){
    let cookies = document.cookie.split(";");

    for (let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i];
        let eqPos = cookie.indexOf("=");
        let name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        if(name==cookiename){
          document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
        }
    }
    setCookie(cookiename, cookievalue);
}

/*
 * This function removes all cookies from storage.
 */
function deleteAllCookies() {
    let cookies = document.cookie.split(";");
    for (let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i];
        let eqPos = cookie.indexOf("=");
        let name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        if(name!="userid"){
          document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
        }
    }
}

function getStoryteller() {
	const storyMessage = {
			type: MESSAGE_TYPE.STORY,
		}
	conn.send(JSON.stringify(storyMessage));
}

function sendQuery(){
  let uid = getElementFromCookies("userid");
  let gid = getElementFromCookies("gameid"); 
  const queryMessage = {
    type: MESSAGE_TYPE.QUERRY,
    payload: {
      userid: uid,
      gameid: gid
    }
  }
  conn.send(JSON.stringify(queryMessage));
}

/*
 * This is a helper function for getting an element from the browser's cookies.
 * @params string detailing the name of the desired cookie, element
 */
function getElementFromCookies(element) {
  let cookies = document.cookie.split(";");
  for (let i = 0; i < cookies.length; i++) {
    let eqPos = cookies[i].indexOf("=");
    let name = eqPos > -1 ? cookies[i].substr(0, eqPos) : cookies[i];
    if (name == element) {
      let value = eqPos > -1 ? cookies[i].substr(eqPos+1) : "";
      return value;
    }
  }
}

/*sets a Cookie of with a (name, value) pair and saves it into the document
@params : cookiename and cookie value
@return : none
*/
function setCookie(cookiename, cookievalue){
  const newcookie = cookiename + "="+cookievalue;
  document.cookie = newcookie;
}

/*
returns true if the caller of the function is a storyteller, by checking the corresponding cookie
@params : none
@return : boolean indicating whether user is storyteller
*/
function isStoryteller() {
  return getElementFromCookies("userid") == storyteller;
}


