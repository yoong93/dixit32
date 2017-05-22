/* create_game.js
 * This file outlines the actions taken by the front-end when a game is being created/joined.
 */

let prevSelected;
let currSelected;
let currPlayer;
let capacity;
let gameCount = 1;
let newGameId = 1;
let username;

$(document).ready(function(){
    
    // user enters a username to enter the lobby
    $("#username-form").on("submit", function(event) {
      event.preventDefault();
      if($(".username").val() != "") {
        username = $(".username").val();
        $("#username-form").toggleClass("hidden");
        $(".start-game").removeClass("hidden");
      }
      sendLoad();
    });
    
    // on creation of a game, a message is sent to the back end to initialize the relevant objects
    $("#create-form").on("submit", function(event) {
      event.preventDefault();
      $(".create-error-message").empty();
      
      // check that user has provided a lobby name
      if($(".lobby-name").val() == "") {
        $(".create-error-message").append("<p style=\"color:red;margin-top:30px;margin-left:30px;\">Please provide a lobby name.</p>")
      } else {
        // initialize the JSON object containing all the game details
        let gameInit = {
          type: MESSAGE_TYPE.CREATE,
          payload: {
            game_id: newGameId,
            user_name: username,
            lobby_name: $(".lobby-name").val(),
            num_players: Number($(".num-players").val()),
            victory_pts: $(".victory-points").val(),
            cards: $(".configure-cards.active").text().trim(),
            story_types: {
              text: $("#story-text").attr("class").includes("active"),
              audio: $("#story-audio").attr("class").includes("active"),
              video: $("#story-video").attr("class").includes("active")  
            }
          }
        }
        
        // send new game information to backend
        conn.send(JSON.stringify(gameInit));
        newGameId++;
      }
      
    });
    
    // on selection of a game from the displayed list of available games
    $('#lobbyt tbody').on('click', function() {
      currPlayer = $(event.target).parent().find("#num_players").html().split("/")[0];
      capacity = $(event.target).parent().find("#num_players").html().split("/")[1];
      currSelected = $(event.target);
      currSelected.parent().toggleClass('selected-row');
      if (prevSelected != undefined) {
        prevSelected.parent().toggleClass('selected-row');
      }
      prevSelected = currSelected;

    });
    
    // when an intention to join the selected game is committed
    $("#join-form").on("submit", function(event) {
      event.preventDefault();
      $(".join-error-message").empty();
      
      // check that the user has selected a lobby before he/she pressed Join
      if(currSelected == undefined ) {
        $(".join-error-message").append("<p style=\"color:red;margin-top:30px;margin-left:30px;\">Please select an available lobby.</p>");
      } else {
        // check if the players has attempted to join a full lobby
        if (currPlayer >= capacity){
              $(".join-error-message").append("<p style=\"color:red;margin-top:30px;margin-left:30px;\">Lobby is full! Please select another lobby.</p>")
            } else {
              window.location = window.location.href + "play";
              const joinMessage = {
                type: MESSAGE_TYPE.JOIN,
                payload: {
                  user_name: $(".username").val(),
                  game_id: currSelected.attr('class')  
                }
              }
            conn.send(JSON.stringify(joinMessage));
          }
        }
      })
  });
      

function sendLoad(){
  let gameLoad = {
          type: MESSAGE_TYPE.LOAD,
          payload: {
          }
      }
  conn.send(JSON.stringify(gameLoad));
}

