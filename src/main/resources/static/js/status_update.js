/* 
 * status_update.js
 * This file contains all the functions needed for the progress of the game.
 */

/* 
 * This function initializes the storyteller's interface, to include an input text field
 * to allow him/her to enter a story.
 */
function initStorytellerBoard() {
  if($("#board").find("#promptField").attr("id") == undefined) {
    $(".promptField-container").prepend("<input type=\"text\" name=\"prompt\" id=\"promptField\" placeholder=\"Enter a story\">");
  }
  $("#board") .find("#player-submit").val("Submit"); 
  $("#promptField").removeClass("hidden")

}

/* 
 * This function initializes the guesser's interface, to remove and replace any interactive
 * UI items which might have been retained during his/her transition from a storyteller role.
 */
function initGuesserBoard() {
  $("#promptField").remove();
  $("#board").find(".formSubmit").val("Guess");
  $("#playerInput").removeClass("hidden");
}

/* 
 * This function initializes the hand of the player, to display 6 initial cards.
 * @params JSON object containing information about all 6 cards in the hand, hand
 */
function initHand(hand){
  for (card of Object.keys(hand)) {
    let cardInfo = hand[card].split(":");
    let url = cardInfo[3];
    let cardId = cardInfo[1];
    
    $(".hand").append("<div class=\"card hand-card\" draggable=\"true\" ondragstart=\"drag(event)\" data-toggle=\"modal\" data-target=\"#myModal\"><div class=\"image\" id=\"" + cardId + "\" style=\"background-image:url(../img/img" + cardId + ".jpg);\"></div></div>");
  }
}

/* 
 * This function shuffles and displays the selected cards for the round.
 * @params JSON object containing information the guessed and answer cards, cards
 */
function displaySelectedCards(cards) {
  
  let shuffledCards = [cards.answer];
  for (card in Object.keys(cards.guessed)) {
    shuffledCards.push(cards.guessed[card]);
  }
  
  // shuffle cards
  for (let i = shuffledCards.length; i; i--) {
    let j = Math.floor(Math.random() * i);
    [shuffledCards[i - 1], shuffledCards[j]] = [shuffledCards[j], shuffledCards[i - 1]];
  }
  
  // append shuffled cards to DOM
  $(".picked-cards").empty();
  for (randomCardId of shuffledCards) {
    $(".picked-cards").append("<div class=\"card\"><div class = \"image bigimg\" id=\"" + randomCardId + "\" style = \"background-image: url(" + "../img/img" + randomCardId + ".jpg" + "); background-size: cover; background-repeat: no-repeat;\"></div><div class=\"voters\"></div></div>").hide().show('slow', 'swing');
  }

}

let timer = 0;

/*
 * This function starts the timer for the round.
 * @params the number of seconds to count down, seconds 
 */
function startTimer(seconds) {
	let time = seconds;
	timer = setInterval(function() {
    if (time >= 0) {
			$("#stopwatchvalue").html(time >= 10 ? "00:" + time : "00:0" + time);
      if (time <= 5) {
        $("#countdown-message").html("A decision is going to be made for you in " + time + "...");
      } else {
        $("#countdown-message").empty();
      }
      time -= 1;
		} else {
      $("#countdown-message").empty();
			$("#stopwatchvalue").html("<span style = \'font-size: 4vw; color: red;\'>Time's Up!</span>");

      // a random card is guessed or voted for if no decision is made by the player when by the allotted time
      if (currState == "Guessing") {
        //send random guess
        hand = [];
        $(".hand-card").each(function() {
          hand.push(getCardInfo($(this).find(".image")));
        })
        const randomCard = hand[Math.floor(Math.random()*hand.length)];
        sendGuess(randomCard.id);
      }
      
      if (currState == "Voting") {
    	  hand = [];
          $(".hand-card").each(function() {
            hand.push(getCardInfo($(this).find(".image")));
          })
          const randomCard = hand[Math.floor(Math.random()*hand.length)];
          sendVote(randomCard.id);
      }
		}
	}, 1000);
}

/*
 * Helper function for stopping the timer.
 */
function stopTimer() {
  clearInterval(timer);
}

/*
 * Sets the current storyteller, and displays his/her identity in the sidebar.
 * @params JSON object containing id and username of storyteller, st
 */
function setStoryTeller (st) {
  storyteller = st.user_id;
	$("#st-identity").html(st.user_name);
}

/*
 * Sets the current state of the game in the sidebar.
 * @params string detailing the current state of the game, status
 */
function setStatus (status) {
  currState = status;
	$("#status-indicator-text").html(status);
  if (status == "Storytelling") {
    $("#status-indicator").css("background-color", "#FFDF3C");
  } else if (status == "Guessing") {
    $("#status-indicator").css("background-color", "#16C69E");
  } else if (status == "Voting") {
    $("#status-indicator").css("background-color", "#FF9494");
  }
  
}

/*
 * Updates the players individual states in the sidebar.
 * @params the locally stored map of all players' updated statuses
 */
function updateStatus(statusMap) {
	let players = Object.keys(statusMap);
	for (let i = 0; i < players.length; i ++) {
    $("#scoreboard").find("#" + players[i] + "status").html(statusMap[players[i]]);
	}
} 

/*
 * Updates the scoreboard with the newly-received points after a rounds
 * @params the number of points to update the scoreboard with, points
 */
function updatePoints(points) {
  for (player of Object.keys(points)) {
    let currPoints = parseInt($("#" + player + "points").html());
    $("#" + player + "points").html(points[player] + currPoints); 
  }
  
}

/*
 * Displays the points each individual player received to the player.
 * @params the number of points to display, points
 */
function displayPoints(points) {
  for (id of Object.keys(points)) {
    if (id == myId) {
      console.log(id)
      $("#results-message").html("You received " + points[id] + " points!");
    }
  }
  $(".results-overlay").removeClass("hidden");
}

/*
 * Displays the identity of the winner to all players.
 * @params JSON object containing information about the winner, winner
 */
function displayWinner(winner){
  $("#results-message").html(winner.winner_name + " won, with " + $("#" + winner.winner_id + "points").html() + " points!");
  $(".results-overlay").removeClass("hidden");
  $("#play-again-button").removeClass("hidden");
}

/*
 * Sends an intention to leave from one player to all players, to start closing the room.
 */
function sendLeaveIntent() {
  const leaveIntent = {
    type: MESSAGE_TYPE.EXIT,
    payload: {
      user_id: getElementFromCookies("userid") 
    }
  }
  conn.send(JSON.stringify(leaveIntent));
}

/* 
 * Sends an intention to restart the current game to all players, to start reinitializing the game.
 */
function sendRestartIntent() {

  $(".results-overlay").toggleClass("hidden");
  $("#play-again-button").toggleClass("hidden");
  
  const restartIntent = {
    type: MESSAGE_TYPE.RESTART,
    payload: {
      game_id: getElementFromCookies("gameid")
    }
  }
  conn.send(JSON.stringify(restartIntent));
}

/*
 * Helper function which performs all the necessary steps to transition to a new round
 * @params JSON object containing information about the new round (new cards, new storyteller etc), details
 */
function newRound(details) {
  $(".results-overlay").addClass("hidden");
  let newHand = details.hand;
  let oldHand = $(".hand").html();
  
  // if the player was the old storyteller, initialize the guesser's board
  if (myId == storyteller) {
    initGuesserBoard();
    
  // if the player is now the new storyteller, initialize the storyteller's board
  } else if (myId == details.storyteller.user_id) {
    initStorytellerBoard();
  } 

  // clear stopwatch, prompt and picked cards for all players, regardless of role
  $(".picked-cards").html("<div class=\"card\"><div class=\"image bigimg\" ondrop =\"drop(event)\" ondragover=\"allowDrop(event)\" style=\"background-image: url(../img/blank.jpg)\"></div></div></div>");
  $("#promptValue").empty();
  $("#stopwatchvalue").empty();
  
  // set the new storyteller
  setStoryTeller(details.storyteller);
  setStatus("Storytelling");
  
  // add the new card
  for (card in Object.keys(newHand)) {
    let cardDetails = newHand[card].split(":");
    let cardId = cardDetails[1];
    let cardUrl = cardDetails[3]
    if ($(".hand").find("#" + cardId).attr("id") == undefined) {
      let newCard = ["<div class=\"card hand-card\" draggable = \"true\" ondragstart=\"drag(event)\" data-toggle=\"modal\" data-target=\"#myModal\"><div class=\"image\" id=\"", cardId, "\" style=\"background-image:url(", cardUrl, ");\"></div></div>"]
      $(".hand").append(newCard.join(""));
    }
  }
  
}

/*
 * Prepares the interface for reinitialization in a new round.
 */
function prepareBoard() {
  $(".hand").empty();
  $(".picked-cards").html("<div class=\"card\"><div class=\"image bigimg\" ondrop =\"drop(event)\" ondragover=\"allowDrop(event)\" style=\"background-image: url(../img/blank.jpg)\"></div></div>")
  $("#stopwatchvalue").empty();
  $("#promptValue").empty();
  $("#wait-leave").modal("hide");
  $(".results-overlay").addClass("hidden");
  
}