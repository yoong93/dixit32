/* play.js
 * This file outlines the responses executed by the front-end when the player is playing a game.
 */

let currState = "Storytelling";

$(document).ready(function(){

  // on click of a card from the hand for zooming
  $(".hand").on("click", "div.image", function(event) {
    
    // a modal dialog box is displayed for an enlarged image of the card
    const cardInfo = getCardInfo($(this));
    let myId = getElementFromCookies("userid");
      $("#picture-zoom").find(".modal-body").html("<div class = \"image bigimg mag\" id=\"" + cardInfo.id + "\" style = \"background-image: url(" + cardInfo.url + "); background-size: cover; background-repeat: no-repeat;\"></div>");
      $("#picture-zoom").modal("show");
  });

  // a player's submission during a round
	$('#player-submit').on('click', function(e) {
    e.preventDefault();
    myId = getElementFromCookies("userid");
    
    // sends the prompt and the associated card to all players
    if (currState == "Storytelling" && myId == storyteller) {
      const cardInfo = getCardInfo($(".picked-cards").find(".bigimg"));
      const pickedId = cardInfo.id;
      const prompt = $("#promptField").val();
      
      // error check that the storyteller provided both a card and a prompt
      if(pickedId == undefined) {
        $("#board-error-message").text("Please pick a card.");
      } else if (prompt == "") {
        $("#board-error-message").text("Please submit a prompt.");
      } else {
        const url = cardInfo.url;
        submitPrompt(prompt, pickedId, url); 
        // remove the selected card
        $(".hand").find("#" + pickedId).parent().remove();
        $("#board-error-message").text("");
        $("#promptField").toggleClass("hidden");
      } 
      
    // sends the guessed card to all players
    } else if (currState == "Guessing") {
      const pickedId = $(".picked-cards").find(".bigimg").attr("id");
      if (pickedId != undefined) {
        sendGuess(pickedId);
      }
    
    // sends the vote to all players
    } else if (currState == "Voting") {
      const votedId = $(".vote-selected").attr("id");
      if (votedId != undefined) {
        sendVote(votedId);
        $("#guesser-button").toggleClass("hidden");
      }
    } 
	});
  
  // highlights the card when a user selects it for voting
  $(".picked-cards").click(function(event) {
    console.log("clicking");
    let myId = getElementFromCookies("userid");
    if (currState == "Voting" && myId != storyteller ) {
      if($(event.target).attr("class") == undefined){
        
        // deselect all other cards when a particular card is clicked
        $(".picked").each(function() {
          $(this).removeClass("vote-selected");
        });
        $(event.target).parent().toggleClass("vote-selected");
      } else if ($(event.target).attr("class") == "image bigimg") {
        $(".image").each(function() {
          $(this).removeClass("vote-selected");
        });
        $(event.target).toggleClass("vote-selected");
      }
    } 
  });
  
  // sends the chat from one player to all players
  $("#messageForm").on('submit', function(e) {
	  e.preventDefault();
	  const body = $("#messageField").val();
	  const time = new Date().getTime();
	  sendChat(body, time);	  
	  $("#messageForm")[0].reset();

  });
  
  // shows warning message to confirm user's intention to leave the room
  $("#leave-button").on("click", function(event) {
	    $("#exit-message-self").modal({
	      backdrop: 'static', 
	      keyboard: false
	    });
	  });
  
  // redirects the user back to the home page once user confirms his/her intention to leave
  $("#leave-button-actual").on("click", function(event) {
    console.log("leaving");
    sendLeaveIntent();
    window.location.href = "/";
  });

  // sends a restart intent to all users
  $("#play-again-button").click(function(event) {
    $("#wait-leave").find(".modal-title").html("Play again");
    $("#wait-leave").find(".modal-body").html("Please wait for the rest of the room to be ready to start again.");
    $("#wait-leave").modal("show");
    sendRestartIntent();
  });
    
});

/*
 * The following three functions provide functionality for dragging and dropping.
 */
function allowDrop(event) {
	event.preventDefault();
}

function drag(event) {
	console.log("dragging");
	console.log($(event.target));
	let cardInfo = null;
	if ($(event.target).attr('class') == "image") {
		cardInfo = getCardInfo($(event.target));
	} else {
		cardInfo = getCardInfo($(event.target).find("div"));
	}
    event.dataTransfer.setData("text", cardInfo.id);

}

function drop(event) {
    event.preventDefault();
    const id = event.dataTransfer.getData("text");
    const url = "../img/img" +id + ".jpg";
    
    let myId = getElementFromCookies("userid");
    console.log("drop event id + url " + id + url);
    if ((currState == "Storytelling" && myId == storyteller) || (currState == "Guessing" && myId != storyteller)) {
      $(".picked-cards").html("<div class=\"card\"><div class = \"image bigimg\" id=\"" + id + "\" style = \"background-image: url(" + url + "); background-size: cover; background-repeat: no-repeat;\" ondrop =\"drop(event)\" ondragover=\"allowDrop(event)\"></div></div>") ;
    }
}

/*
 * Helper function for the storyteller to submit a prompt.
 * @params  prompt string, inputPrompt
            id of the selected card, card_id
            url of the selected card's image, card_url
 */
function submitPrompt(inputPrompt, card_id, card_url) {
	const promptMessage = {
		type: MESSAGE_TYPE.ST_SUBMIT,
		payload: {
			prompt: inputPrompt,
			card_id: card_id,
      card_url: card_url
		}
	}
	conn.send(JSON.stringify(promptMessage));
}

/*
 * Helped function to allow guesser to submit a card.
 * @params id of the selected card, card_id
 */
function sendGuess(card_id) {
  const guess = {
    type: MESSAGE_TYPE.GS_SUBMIT,
    payload: {
      user_id: getElementFromCookies("userid"),
      card_id: card_id
    }
  }
  conn.send(JSON.stringify(guess));
  $(".hand").find("#" + card_id).parent().remove();
  $(".formSubmit").val("Vote");
  
  // stop the timer once a guess has been submitted
  stopTimer();
  $("#stopwatchvalue").html("Guessed!");
}

/*
 * Helper function to allow voter to submit a vote.
 * @params id of the selected card, card_id
 */
function sendVote(card_id) {
  const vote = {
    type: MESSAGE_TYPE.VOTE,
    payload: {
      user_id: getElementFromCookies("userid"),
      card_id: card_id
    }
  }
  conn.send(JSON.stringify(vote));
  
  // stop the timer once a vote has been made
  stopTimer();
  $(".formSubmit").val("Guess");
  $("#stopwatchvalue").html("Voted!");
}

/*
 * Helper function to get the card's ID and URL from its interface element.
 * @params jQuery selector of card div element in HTML DOM
 * @return JSON object containing card information
 */
function getCardInfo(card) {
  const id = card.attr("id");
  console.log("card id");
  const img = card.attr("style");

  const url = img.replace(/.*\s?url\([\'\"]?/, '').replace(/[\'\"]?\).*/, '');
  return {id: id, url: url};
}

/*
 * Helper function to allow sending a chat.
 * @params  message text, message
            the time at which the chat was sent, inputTime
 */
function sendChat(message, inputTime) {
  const chat = {
    type: MESSAGE_TYPE.CHAT_MSG,
    payload: {
      body: message,
      time: inputTime
    }
  }
  conn.send(JSON.stringify(chat));
}

