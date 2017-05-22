<#assign content>
<script>
	$(document).ready(function() {
	  setup_update();
	});
</script>

<div class = "wrapper">
	<div class = "row screen">
		<div class = "col-xs-9 board">
			<div id="board-error-message" style="float:left;">
      </div>
			<div id="board">
				<div class="board-item promptField-container">
				</div>
				<div class="board-item prompt">
					<i id ="promptValue"></i>
				</div>
				<div class="board-item picked-cards-container">
					<div class="picked-cards">
						<div class="card">
							<div class="image bigimg" ondrop ="drop(event)" ondragover="allowDrop(event)" style ="background-image: url(../img/blank.jpg)"></div>
						</div>
					</div>
				</div>
				<div class="board-item player-submit-container">
		      		<button type="button" class="btn" id="player-submit">Submit</button>
	      		</div>
			</div>
			
			<div class="hand-container" style="text-align: center">
				<div class="hand">
				</div>
			</div>
		</div>
		<div class = "col-xs-3 text-center statusbar">
      <ul class="nav nav-tabs">
        <li class="active"><a data-toggle="tab" href="#player-dashboard">Player</a></li>
        <li><a data-toggle="tab" href="#chatroom">Chat</a></li>
      </ul>
      <div class="tab-content">
        <div class="tab-pane fade in active" id="player-dashboard">
        	<div id="player-feed">
	            <div id="user-name">
	            </div>
	            <div id="stopwatch">
	              <span id="stopwatchvalue"></span>
                <span id="countdown-message" style="font-size:1vw;color:red;"></span>
	            </div>
	            <div id="status-indicator">
	              <span id="status-indicator-text"></span>
	            </div>
	            <div id="st-identity-container">
	              <p class="statuselement-header">Storyteller</p>
	              <p id="st-identity">Player</p>
	            </div>
	            <div id="player-information">
	              <span class="statuselement-header">Player Feed</span>
	              <br>
	              <table class="table" id="scoreboard">
	                <thead>	
	                  <tr>
	                    <th>Player</th>
	                    <th>Status</th>
	                    <th>Points</th>
	                  </tr>
	                </thead>
	                <tbody id="scoreboard-body">
	                </tbody>
	              </table>
	            </div>
	            <div id="leave-button-container">
	            	<button type="button" class="btn btn-danger" id="leave-button">Leave game</button>
	            </div>
            </div>
        </div>

        <div id="chatroom" class="tab-pane fade">
          <div class = "chatbox">
		    <ul class = "chatList">
		    </ul>
          </div>
          <div id="chat-input">
            <form method="POST" action="/whatever" id="messageForm">
                <input type="text" name="message"   id="messageField">
                <input type="hidden" name="nickname" id="nicknameField" value="">
                <input type="hidden" name="time" id="timeField" value="">
                <input class="btn btn-secondary" id="chat-submit" type="submit" value="Send">
            </form>
          </div>
        </div>
        
      </div>
		    </div>
		</div>
	</div>
	<div class="results-overlay">
		<span id="results-message">Please wait for the other players to arrive.</span>
		<button type="button" class="btn btn-primary hidden" id="play-again-button">Play again</button>
	</div>
</div>

<div id="picture-zoom" class="modal fade" role="dialog">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">&times;</button>
        <div class="modal-title">Detailed Card View</div>
      </div>
      <div class="modal-body">
      </div>
      <div class="modal-footer">
      </div>
    </div>
  </div>
</div>

<div id="wait-leave" class="modal fade" role="dialog">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <div class="modal-title"></div>
      </div>
      <div class="modal-body">
      </div>
      <div class="modal-footer">
      </div>
    </div>
  </div>
</div>

<div id="exit-message-self" class="modal fade" role="dialog">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <div class="modal-title">Are you sure?</div>
      </div>
      <div class="modal-body">
      	<p>You're about to leave the game. Click Leave to return to the lobby, or Stay to cancel.</p>
      	<br>
         <button type="button" class="btn btn-danger" id="leave-button-actual">Leave</button>
         <button type="button" class="btn btn-secondary" data-dismiss="modal">Stay</button>
      </div>
      <div class="modal-footer">
      </div>
    </div>
  </div>
</div>

<div id="exit-message" class="modal fade" role="dialog">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <div class="modal-title">A player has left!</div>
      </div>
      <div class="modal-body">
      	<p>A player has exited the game. Click below to return to lobby.</p>
      	<br>
         <button type="button" class="btn btn-danger" onclick="location.href = '/'">Leave</button>
      </div>
      <div class="modal-footer">
      </div>
    </div>
  </div>
</div>

</#assign>
<#include "main.ftl">