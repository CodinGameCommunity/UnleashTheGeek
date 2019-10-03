<!-- LEAGUES level1 level2 -->
<div id="statement_back" class="statement_back" style="display: none"></div>
<div class="statement-body">
  <!-- LEAGUE ALERT -->
  <div style="color: #7cc576; 
    background-color: rgba(124, 197, 118,.1);
    padding: 20px;
    margin-right: 15px;
    margin-left: 15px;
    margin-bottom: 10px;
    text-align: left;">
    <div style="text-align: center; margin-bottom: 6px">
      <img src="//cdn.codingame.com/smash-the-code/statement/league_wood_04.png" />
    </div>
    <p style="text-align: center; font-weight: 700; margin-bottom: 6px;">
      This is a <b>league based</b> challenge.
    </p>
    <span class="statement-league-alert-content">
      For this challenge, multiple leagues for the same game are available. Once you have proven your skills against the
      first Boss, you will access a higher league and extra opponents will be available.
    </span>
  </div>
  <!-- GOAL -->
  <div class="statement-section statement-goal">
    <h2>
      <span class="icon icon-goal">&nbsp;</span>
      <span>Goal</span>
    </h2>
    <div class="statement-goal-content">
      <p><strong>Amadeusium</strong> is a rare and valuable crystal ore which is only found on inhospitable planets.
        As one of two competing mining companies, you must control the robots on-site to unearth as much ore as you can.
      </p>
      <span>Deliver more Amadeusium than your opponent!</span>
    </div>
  </div>
  <!-- RULES -->
  <div class="statement-section statement-rules">
    <h2>
      <span class="icon icon-rules">&nbsp;</span>
      <span>Rules</span>
    </h2>

    <div class="statement-rules-content">
      <p>Both players control a team of several <strong>robots</strong>. The teams start out at the same points on the
        map, at the <strong>headquarters</strong>. The robots can use <strong>radars</strong> from the headquarters to
        detect and mine Amadeusium veins. They may also trap certain areas of the map with <strong>EMP traps</strong>.
        These can be triggered by robots which are then rendered inoperable.
      </p>

      <br>
      <p><b>The map</b></p>
      <br>

      <p>The game is played on a grid <const>30</const> cells wide by <const>15</const> cells high. The coordinates
        <const>x=0, y=0</const>
        corresponds to the top left cell. </p>

      <p>The first column of cells is considered to be part of the <strong>headquarters</strong>. This is where
        <strong>Amadeusium ore</strong> must be returned to once mined and where objects are requested.</p>
      <p>The cells that contain Amadeusium ore are called <strong>vein cells</strong>. Veins are not
        visible to the players unless they are within the range of the player's radar. There are no vein cells in the
        headquarters.</p>

      <p>Robots can drill a <strong>hole</strong> on any cell (except the headquarters'). Holes are visible to both
        players and do not impede movement.</p>



      <br>
      <p><b>Robots</b></p>
      <br>

      <p>Each robot can hold <const>1</const> <strong>item</strong> in its inventory.</p>

      <p>A robot may:</p>

      <ul>
        <li>
          <action>REQUEST</action> an item from the headquarters.
        </li>

        <li>
          <action>MOVE</action> towards a given cell.
        </li>
        <li>
          <action>DIG</action> on a cell. This will, in order:

          <ol style="padding-top: 0; padding-bottom: 0;">
            <li>Create a <strong>hole</strong> on this cell if there isn't one already.</li>

            <li>Bury any item the robot is holding into the hole.</li>

            <li>If digging on a <strong>vein cell</strong> and ore was not buried on step 2, place one unit of ore into
              the robot's inventory.</li>
          </ol>

        </li>
        <li>
          <action>WAIT</action> to do nothing.
        </li>
      </ul>

      <p>Details:</p>
      <ul>
        <li>Robots may only dig on the cell they occupy or neighbouring cells. Cells have <const>4</const> neighbours:
          up,
          left, right, and down.</li>

        <li>Robots on any cell part of the headquarters will automatically deliver any ore it is
          holding.</li>

        <li>Robots can occupy the same cell.</li>

        <li>Robots cannot leave the grid.</li>

        <li>Robots' inventories are not visible to the opponent.</li>

      </ul>

      <p><b>Items</b></p>
      <br>

      <p><strong>Amadeusium Ore</strong> is considered an item and should be delivered to the headquarters to
        score <const>1</const> point.</p>

      <p>At the <strong>headquarters</strong>, robots may request one of two possible <strong>items</strong>: a <action>
          RADAR</action> or a
        <action>TRAP</action>.</p>

      <p>If an item is taken from the headquarters, that item will no longer be
        available for the robots of the same team for <const>5</const> turns.</p>

      <p>A <strong>trap</strong> buried inside a <strong>hole</strong> will go off if any robot uses the <action>DIG
        </action> command on
        the cell it is buried in. The EMP pulse destroys any robots on the cell or on the <const>4</const> neighbouring
        cells. Any other
        trap caught in the pulse will also go off, causing a chain reaction.</p>
      <!-- TODO: Add an illustration? -->

      <p>A <strong>radar</strong> buried inside a <strong>hole</strong> will grant the ability to see the amount of
        buried ore
        in <strong>veins</strong> within a range of <const>4</const> cells, for the team which buried it.
        <!-- TODO: Add an illustration? -->
        If an opponent robot uses the <action>DIG</action> on the cell the radar is buried in, the radar is destroyed.
      </p>

      <br>
      <p><b id="actionsorderforoneturn">Action order for one turn</b></p>

      <ol>
        <li>If <action>DIG</action> commands would trigger <strong>Traps</strong>, they go off.</li>

        <li>
          The other <action>DIG</action> commands are resolved.
        </li>

        <li>
          <action>REQUEST</action> commands are resolved.
        </li>

        <li>Request timers are decremented.</li>

        <li>
          <action>MOVE</action> and <strong>WAIT</strong> commands are resolved.
        </li>

        <li><strong>Ore</strong> is delivered to the <strong>headquarters</strong>.</li>

      </ol>
    </div>
    <!-- Victory conditions -->
    <div class="statement-victory-conditions">
      <div class="icon victory"></div>
      <div class="blk">
        <div class="title">Victory Conditions</div>
        <div class="text">
          <ul style="padding-top:0; padding-bottom: 0;">
            <li>After <code>200</code> rounds, your team has delivered the most Amadeusium ore.</li>
            <li>You have delivered more ore than your opponent and they have no more active robots.</li>
          </ul>
        </div>
      </div>
    </div>
    <!-- Lose conditions -->
    <div class="statement-lose-conditions">
      <div class="icon lose"></div>
      <div class="blk">
        <div class="title">Defeat Conditions</div>
        <div class="text">
          <ul style="padding-top:0; padding-bottom: 0;">
            <li>Your program does not provide one valid command per robot in time, including destroyed robots.</li>
          </ul>
        </div>
      </div>
    </div>
  </div>


  <!-- EXPERT RULES -->
  <div class="statement-section statement-expertrules">
    <h2>
      <span class="icon icon-expertrules">&nbsp;</span>
      <span>Technical Details</span>
    </h2>
    <div class="statement-expert-rules-content">
      <ul style="padding-left: 20px;padding-bottom: 0">
        <li>Robots can insert ore into a cell, the cell becomes a <strong>vein</strong>.</li>

        <li>Each robot, radar and trap has a unique id.</li>

        <li>Receiving an item from the <strong>headquarters</strong> will destroy any item a robot may already be
          holding.</li>

        <li>When several robots of the same team request an item, robots with no item will be given priority for the
          request.</li>

        <li>Traps have no effect on buried radars and ore.</li>

        <li>If a robot holding an item is destroyed, the item is lost.</li>
      </ul>
      <p>You can check out the source code of this game <a rel="nofollow" target="_blank"
          href="https://github.com/CodinGameCommunity/UnleashTheGeek">on this GitHub repo</a>.</p>
    </div>
  </div>

  <!-- PROTOCOL -->
  <div class="statement-section statement-protocol">
    <h2>
      <span class="icon icon-protocol">&nbsp;</span>
      <span>Game Input</span>
    </h2>
    <!-- Protocol block -->
    <div class="blk">
      <div class="title">Initialization Input</div>
      <div class="text">
        <span class="statement-lineno">Line 1:</span> two integers
        <var>width</var> and
        <var>height</var> for the size of the map.
        The leftmost row are cells with access to the headquarters.
        <br>
      </div>
    </div>
    <!-- Protocol block -->
    <div class="blk">
      <div class="title">Input for One Game Turn</div>
      <div class="text">
        <span class="statement-lineno">First line:</span> Two integers: <br>
        <ul style="padding-left: 20px;padding-top:0;padding-bottom:0px">
          <li>
            <var>myScore</var> for the amount of ore you delivered to the elevator.
          </li>
          <li>
            <var>enemyScore</var> for the amount ore your opponent delivered to the elevator.
          </li>
        </ul>
        <span class="statement-lineno">Next <var>height</var> lines:</span> each line has <var>width</var> * <const>2
        </const> variables: <var>ore</var> and <var>hole</var>.<br>
        <var>ore</var> is: <ul style="padding-bottom: 0; padding-top:0">
          <li>
            <const>?</const> character if this cell is not within range of a radar you control.
          </li>
          <li>A positive integer otherwise, for the amount of ore this cell contains.</li>
        </ul>
        <var>hole</var> is: <ul style="padding-bottom: 0; padding-top:0">
          <li>
            <const>1</const> if this cell has a hole on it.
          </li>
          <li>
            <const>0</const> otherwise.
          </li>
        </ul>
        <br><br>
        <span class="statement-lineno">Next line:</span> Four integers
        <ul style="padding-left: 20px;padding-top:0;padding-bottom:0px">
          <li>
            <var>entityCount</var> for the amount of robots, radars and traps currently visible to you.
          </li>
          <li>
            <var>radarCooldown</var> for the number of turns until a new <action>RADAR</action> can be requested.
          </li>
          <li>
            <var>trapCooldown</var> for the number of turns until a new <action>TRAP</action> can be requested.
          </li>
        </ul>
        <span class="statement-lineno">Next
          <var>entityCount</var> lines:</span> 5 integers to describe each entity
        <ul style="padding-left: 20px;padding-top:0;padding-bottom:0px">
          <li>
            <var>id</var>: entity's unique id.
          </li>
          <li>
            <var>type</var>:
            <ul style="padding-left: 20px;padding-top:0;padding-bottom:0px">
              <li>
                <const>0</const>: one of your robots
              </li>
              <li>
                <const>1</const>: one of your opponent's robots
              </li>
              <li>
                <const>2</const>: one of your buried radars
              </li>
              <li>
                <const>3</const>: one of your buried traps
              </li>
            </ul>
          </li>
          <li>
            <var>x</var> &
            <var>y</var>: the entity's position.<br> If this entity is a destroyed robot, <var>x y</var> will equal
            <const>-1 -1</const>
          </li>
          <li>
            <var>item</var>: if this entity is a robot, the item this robot is carrying:
            <ul>
              <li>
                <const>-1</const>for nothing
              </li>
              <li>
                <const>2</const>for a radar
              </li>
              <li>
                <const>3</const>for a trap
              </li>
              <li>
                <const>4</const>for a unit of Amadeusium ore
              </li>
            </ul>
          </li>
        </ul>

      </div>
    </div>
    <!-- Protocol block -->
    <div class="blk">
      <div class="title">Output</div>
      <div class="text">
        <span class="statement-lineno">
          <const>5</const> lines,
        </span> one for each robot, in the same order in which they were given, containing one of the
        following actions:
        <ul style="padding-left: 20px;padding-top: 0">
          <li>
            <action>WAIT</action>: the robot does nothing.
          </li>
          <li>
            <action>MOVE x y</action>: the robot moves
            <const>4</const> cells towards the given cell.
          </li>
          <li>
            <action>DIG x y</action>: the robot attempts to bury the item it is carrying in
            the target cell, retrieve ore from the cell, or both. <b>If the cell is not adjacent, the robot will execute
              a <action>MOVE</action> command towards the target instead.</b>
          </li>
          <li>
            <action>REQUEST</action> followed by <action>RADAR</action> or <action>TRAP</action>: the robot attempts to
            take an item from the <strong>headquarters</strong>. If the robot is not on a headquarters cell, the robot
            will execute the <action>MOVE 0 y</action> command instead, where y is the ordinate of the robot.
          </li>
        </ul>
        You may append text to a command to have it displayed in the viewer above your robot.
        <br><br> Examples: <ul style="padding-top:0; padding-bottom: 0;">
          <li>
            <action>MOVE 8 4</action>
          </li>
          <li>
            <action>WAIT nothing to do...</action>
          </li>
        </ul>

        You must provide a command to all robots each turn, even if they are destroyed. Destroyed robots will ignore the
        command.</div>
    </div>
    <div class="blk">
      <div class="title">Constraints</div>
      <div class="text">
        Response time per turn ≤
        <const>50</const>ms
        <br>Response time for the first turn ≤
        <const>1000</const>ms
      </div>
    </div>
  </div>
  <!-- STORY -->
  <div class="statement-story-background">
    <div class="statement-story-cover"
      style="background-size: cover; background-image: url(https://static.codingame.com/servlet/fileservlet?id=31275947693156)">
      <div class="statement-story" style="min-height: 300px; position: relative">
        <h2><span style="color: #b3b9ad">Getting Started</span></h2>
        <div class="story-text">
          Why not jump straight into battle with one these <b>Starter AIs</b>,
          provided by the Unleash The Geek Team:
          <ul>
            <li>
              C++
              <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                href="https://gist.github.com/CGjupoulton/a4053faf94bc3fe2b4db3e71d6253ac5">https://gist.github.com/CGjupoulton/a4053faf94bc3fe2b4db3e71d6253ac5</a>
            </li>
            <li>
              C#
              <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                href="https://gist.github.com/CGjupoulton/557d990b31b79538f6e44709efd66968">https://gist.github.com/CGjupoulton/557d990b31b79538f6e44709efd66968</a>
            </li>
            <li>
              Java
              <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                href="https://gist.github.com/CGjupoulton/f273031a12f552c145602582115f2b25">https://gist.github.com/CGjupoulton/f273031a12f552c145602582115f2b25</a>
            </li>
            <li>
              JavaScript
              <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                href="https://gist.github.com/CGjupoulton/d258d155c0dd443d16035c372bb58525">https://gist.github.com/CGjupoulton/d258d155c0dd443d16035c372bb58525</a>
            </li>
            <li>
              Python
              <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                href="https://gist.github.com/CGjupoulton/e93512f74336aeef97a2c2a52b381e20">https://gist.github.com/CGjupoulton/e93512f74336aeef97a2c2a52b381e20</a>
            </li>
          </ul>

          <p>
            You can modify them to suit your own coding style or start completely
            from scratch.
          </p>
          Other starters may become available during the event <a target="_blank" rel="nofollow"
            href="https://gist.github.com/CGjupoulton/">here</a>.


        </div>
      </div>
    </div>
  </div>
</div>