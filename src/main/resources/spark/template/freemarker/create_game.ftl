<#assign content>

<script>
	$(document).ready(function() {
	  setup_update();
	  deleteAllCookies();
	});
</script>

<div id="login-wrapper">
	<h1 class="title">Dixit Online</h1>
	<div class="container-fluid text-center enter-username">
		<form id="username-form">
			<input type="text" class="form-control username" placeholder="Enter a new username"></input>
			<input type="submit" class="btn submit" id="username-submit"></input>
		</form>
	</div>
	
	<div class="container-fluid start-game hidden">
	        <div class="row">
	          <div class="col-xs-1"></div>
	          <div class="col-xs-10">
	            <form id="create-form">
	              <span class="glyphicon glyphicon-pencil form-title"></span><span class="form-title-text">Create game</span>
	            <div class="row">
	              <div class="col-xs-6 form-element">
	                <p>Lobby name</p>
	                <input type="text" class="form-control lobby-name"></input>
	              </div>
	              <div class="col-xs-3 form-element">
	                <p>No. of players</p>
	                <select class="form-control num-players">
	                  <option value="2">2 players</option>
	                  <option value="3">3 players</option>
	                  <option value="4">4 players</option>
	                  <option value="5">5 players</option>
	                  <option value="6">6 players</option>
	                  <option value="7">7 players</option>
	                  <option value="8">8 players</option>
	                </select>
	              </div>
	              <div class="col-xs-3 form-element">
	                <p>Victory points</p>
	                <input type="number" class="form-control victory-points" min="2" max="80" value="20"></input>
	              </div>
	            </div>
	          
	            <div class="row">
	                <div class="col-xs-6 btn-group form-element" data-toggle="buttons">
	                    <p>Configure cards</p>
	                    <label class="btn btn-default configure-cards active">
	                      <input type="radio" checked autocomplete="off"> Default
	                    </label>
	                    <label class="btn btn-default configure-cards">
	                      <input type="radio" autocomplete="off"> Upload images
	                    </label>
	                  </div>
	                <div class="col-xs-6 btn-group form-element" data-toggle="buttons">
	                  <p class="configure-story-title">Configure story</p>
	                  <label id="story-text" class="btn btn-default active">
	                    <input  type="checkbox" checked autocomplete="off"> Text
	                  </label>
	                  <label id="story-audio" class="btn btn-default">
	                    <input  type="checkbox" autocomplete="off"> Audio
	                  </label>
	                  <label id="story-video" class="btn btn-default">
	                    <input type="checkbox" autocomplete="off"> Video
	                  </label>
	                </div>
	            </div>
	          
	            <div class="row">
	              <div class="col-xs-10 create-error-message">
	                
	              </div>
	              <div class="col-xs-1 create-button">
	                <input type="submit" class="btn submit" value="Create"></button>
	              </div>
	            </div>
	          
	          </form>
	        </div>
	      </div>
	      <div class="row">
	        <div class="col-xs-1"></div>
	          <div class="col-xs-10">
	            <form id="join-form">
	              <span class="glyphicon glyphicon-arrow-right form-title"></span><span class="form-title-text">Join game</span>
	              <table class="table" id ="lobbyt">
	                <thead>
	                  <tr>
	                    <th>Lobby name</th>
	                    <th>Players</th>
	                  </tr>
	                </thead>
	                <tbody>
	                </tbody>
	              </table>
	              <div class="row">
	                <div class="col-xs-10 join-error-message"></div>
	                <div class="col-xs-1">
	                  <input type="submit" class="btn submit" value="Join"></input>
	                <div>
	              </div>  
	            </form>
	          </div>
	        </div>  
	      </div>
	      <div class="col-xs-1"></div>
</div>

</#assign>
<#include "main.ftl">
