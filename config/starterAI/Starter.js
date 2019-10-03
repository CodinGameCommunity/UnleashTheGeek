/**
 * Deliver more ore to hq (left side of the map) than your opponent. Use radars to find ore but beware of traps!
 **/
let inputs = readline().split(' ');
const MAP_WIDTH = parseInt(inputs[0]);
const MAP_HEIGHT = parseInt(inputs[1]); // size of the map

const NONE = -1;
const ROBOT_ALLY = 0;
const ROBOT_ENEMY = 1;
const HOLE = 1;
const RADAR = 2;
const TRAP = 3;
const ORE = 4;

class Pos {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    distance(pos) {
        return Math.abs(this.x - pos.x) + Math.abs(this.y - pos.y);
    }

}

class Entity extends Pos {
    constructor(x, y, type, id) {
        super(x, y);
        this.id = id;
        this.type = type;
    }
}

class Robot extends Entity {
    constructor(x, y, type, id, item) {
        super(x, y, type, id);
        this.item = item;
    }

    isDead() {
        return this.x === -1 && this.y === -1;
    }

    move(x, y, message = "") {
        console.log(`MOVE ${x} ${y} ${message}`);
    }

    wait(message = "") {
        console.log(`WAIT ${message}`);
    }

    dig(x, y, message = "") {
        console.log(`DIG ${x} ${y} ${message}`);
    }

    request(item, message = "") {
        if(item === RADAR){
            console.log(`REQUEST RADAR ${message}`);
        }
        else if(item === TRAP){
            console.log(`REQUEST TRAP ${message}`);
        }
        else{
            throw Error(`unrecognized item: ${item}`);
        }

    }

}

class Cell extends Pos {
    constructor(ore, hole, x, y) {
        super(x, y);
        this.update(ore, hole);
    }

    hasHole() {
        return this.hole === HOLE;
    }

    update(ore, hole) {
        this.ore = ore;
        this.hole = hole;
    }
}

class Grid {
    constructor() {
        this.cells = [];
    }

    init() {
        for (let y = 0; y < MAP_HEIGHT; y++) {
            for (let x = 0; x < MAP_WIDTH; x++) {
                let index = x + MAP_WIDTH * y;
                this.cells[index] = new Cell(0, 0, x, y);
            }
        }
    }

    getCell(x, y) {
        if (x < MAP_WIDTH && y < MAP_HEIGHT && x >= 0 && y >= 0) {
            return this.cells[x + MAP_WIDTH * y];
        }
        return null;
    }

}

class Game {
    constructor() {
        this.grid = new Grid();
        this.grid.init();
        this.myScore = 0;
        this.enemyScore = 0;
        this.radarCooldown = 0;
        this.trapCooldown = 0;

        this.reset();
    }

    reset() {
        this.radars = [];
        this.traps = [];
        this.myRobots = [];
        this.enemyRobots = [];
    }

}

let game = new Game();

// game loop
while (true) {
    let inputsScore = readline().split(' ');
    game.myScore = parseInt(inputsScore[0]); // Players score
    game.enemyScore = parseInt(inputsScore[1]);
    for (let i = 0; i < MAP_HEIGHT; i++) {
        let inputs = readline().split(' ');
        for (let j = 0; j < MAP_WIDTH; j++) {
            const ore = inputs[2 * j];// amount of ore or "?" if unknown
            const hole = parseInt(inputs[2 * j + 1]);// 1 if cell has a hole
            game.grid.getCell(j, i).update(ore, hole);
        }
    }

    let inputsStatus = readline().split(' ');
    const entityCount = parseInt(inputsStatus[0]); // number of visible entities
    game.radarCooldown = parseInt(inputsStatus[1]); // turns left until a new radar can be requested
    game.trapCooldown = parseInt(inputsStatus[2]); // turns left until a new trap can be requested

    game.reset();

    for (let i = 0; i < entityCount; i++) {
        let inputsEntities = readline().split(' ');
        const id = parseInt(inputsEntities[0]); // unique id of the entity
        const type = parseInt(inputsEntities[1]); // 0 for your robot, 1 for other robot, 2 for radar, 3 for trap
        const x = parseInt(inputsEntities[2]);
        const y = parseInt(inputsEntities[3]); // position of the entity
        const item = parseInt(inputsEntities[4]); // if this entity is a robot, the item it is carrying (-1 for NONE, 2 for RADAR, 3 for TRAP, 4 for ORE)
        if (type === ROBOT_ALLY) {
            game.myRobots.push(new Robot(x, y, type, id, item));
        } else if (type === ROBOT_ENEMY) {
            game.enemyRobots.push(new Robot(x, y, type, id, item));
        } else if (type === RADAR) {
            game.radars.push(new Entity(x, y, type, id));
        } else if (type === TRAP) {
            game.traps.push(new Entity(x, y, type, id));
        }
    }

    for (let i = 0; i < game.myRobots.length; i++) {
        game.myRobots[i].wait(`Starter AI ${i}`);
    }
}
