__ADJACENCY__

Sets whether to allow diagonals in pathfinding and interactions.

**Values:** One of two strings: `EIGHT_ADJACENCY` or `FOUR_ADJENCENCY`

*Default:  FOUR_ADJACENCY*



__AGENTS_MOVE_DISTANCE__ 

Sets the max distance in cells a robot can move in a turn.

*Default: 4*



__AGENTS_PER_PLAYER__ 

Sets the amount of agents each player controls.

*Default: 5*



__AGENT_INTERACT_RADIUS__ 

Sets how far in cells a robot can reach.

*Default: 1*



__AGENT_RESPAWN_TIME__ 

Sets amount of turns until a destroyed robot respawns.

*Default: 999*



__MAP_CLUSTER_SIZE__ 

Sets the size of the area around a gold vein in which to generate additional veins.

*Default: 5*



__MAP_GOLD_COEFF_X__ 

Probability modifier for the X coordinate of gold veins.

1 = normal probability.

0 = always maximum.

*Default: 0.55*



__MAP_HEIGHT__ 

Sets the height in cells of the map.

*Default: 15*



__MAP_WIDTH__ 

Sets the width in cells of the map.

*Default: 30*



__MAP_CLUSTER_DISTRIBUTION_MAX__ 

Maximum percentage of cells that should be the center of a cluster of veins.

*Default: 0.064*



__MAP_CLUSTER_DISTRIBUTION_MIN__ 

Minimum percentage of cells that should be the center of a cluster of veins.

*Default: 0.032*



__MAP_GOLD_IN_CELL_MAX__ 

Maximum amount of gold per vein.

*Default: 3*



__MAP_GOLD_IN_CELL_MIN__ 

Minimum amount of gold per vein.

*Default: 1*



__RADAR_COOLDOWN__ 

Amount of turns until a radar can be requested again.

*Default: 5*



__RADAR_RANGE__ 

The range in which gold is visible around a radar.

*Default: 4*


__EUCLIDEAN_RADAR__ 

Whether the radar should calculate range using the euclidean distance.

*Default: false*

__AGENTS_START_PACKED__ 

Whether pairs of agents should start on the same cell. Otherwise, they spawn next to one another.

*Default: true*


__ROBOTS_CAN_OCCUPY_SAME_CELL__ 

Sets whether robots may occupy the same cell after a `MOVE`.

**Values:** `true` or `false`

*Default: true*



__TRAP_CHAIN_REACTION__ 

Sets whether traps trigger neighbouring traps.

**Values:** `true` or `false`

*Default: true*



__TRAP_COOLDOWN__ 

Amount of turns until a trap can be requested again.

*Default: 5*


__TRAP_RANGE__ 

How far in cells a triggered trap reaches.

*Default: 1*

