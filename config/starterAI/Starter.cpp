#include <array>
#include <cassert>
#include <iostream>
#include <vector>

using namespace std;

//*********************************  UTILS  **********************************************

//----------------------------------Point----------------------------------------------------------
struct Point {
    int x{-1}, y{-1};

    int distance(const Point& oth) { return abs(x - oth.x) + abs(y - oth.y); }
    ostream& dump(ostream& ioOut) const {
        ioOut << x << " " << y;
        return ioOut;
    }
};
ostream& operator<<(ostream& ioOut, const Point& obj) { return obj.dump(ioOut); }

//*********************************  GAME STATE  **********************************************

//----------------------------------Constants----------------------------------------------------------
enum class Type : int { NONE = 0, ROBOT, RADAR, TRAP, ORE, HOLE };
enum class ActionType : int { WAIT = 0, MOVE, DIG, REQUEST };

static constexpr int MAX_PLAYERS = 2;
static constexpr int MAX_WIDTH = 30;
static constexpr int MAX_HEIGHT = 15;
static constexpr int MAX_ROBOTS = 5;

//----------------------------------Cell----------------------------------------------------------
struct Cell : Point {
    bool hole{false};
    bool oreVisible{false};
    int ore{0};

    void update(Point p, int _ore, bool _oreVisible, int _hole) {
        hole = _hole;
        ore = _ore;
        oreVisible = _oreVisible;
        x = p.x;
        y = p.y;
    }
};

//----------------------------------Entity----------------------------------------------------------
struct Entity : Point {
    int id{0};
    Type type{Type::NONE};
    Type item{Type::NONE};
    int owner{0};

    Entity() = default;
    Entity(int _id, Type _type, Point p, Type _item, int _owner) : Point{p}, id{_id}, type{_type}, item{_item}, owner{_owner} {}
    void update(int _id, Type _type, Point p, Type _item, int _owner) {
        x = p.x;
        y = p.y;
        id = _id;
        type = _type;
        owner = _owner;
        item = _item;
    }
};

//----------------------------------Robot----------------------------------------------------------
struct Robot : Entity {
    bool isDead() const { return x == -1 && y == -1; }
};

//----------------------------------Player----------------------------------------------------------
struct Player {
    array<Robot, MAX_ROBOTS> robots;
    int ore{0};
    int cooldownRadar{0}, cooldownTrap{0};
    int owner{0};

    void updateRobot(int id, Point p, Type item, int owner) {
        int idxOffset{0};
        if (id >= MAX_ROBOTS) { idxOffset = MAX_ROBOTS; }
        robots.at(id - idxOffset).update(id, Type::ROBOT, p, item, owner);
        owner = owner;
    }
    void updateOre(int _owner, int _ore) {
        ore = _ore;
        owner = _owner;
    }
    void updateCooldown(int radar, int trap) {
        cooldownRadar = radar;
        cooldownTrap = trap;
    }
};

//----------------------------------Game----------------------------------------------------------
struct Game {
    array<array<Cell, MAX_HEIGHT>, MAX_WIDTH> grid;
    array<Player, MAX_PLAYERS> players;
    vector<Entity> radars;
    vector<Entity> traps;

    Game() { reset(); }
    Cell& get(int x, int y) { return grid.at(x).at(y); }
    Cell& get(Point p) { return grid.at(p.x).at(p.y); }
    void reset() {
        radars.reserve(20);
        radars.clear();
        traps.reserve(30);
        traps.clear();
    }
    void updateOre(int owner, int ore) { players.at(owner).updateOre(owner, ore); }
    void updateCooldown(int owner, int radar, int trap) { players.at(owner).updateCooldown(radar, trap); }
    void updateCell(int x, int y, const string& ore, int hole) {
        int oreAmount{0};
        bool oreVisible{false};
        Point p{x, y};
        if (ore != "?") {
            oreAmount = stoi(ore);
            oreVisible = true;
        }
        get(p).update(p, oreAmount, oreVisible, hole);
    }
    void updateEntity(int id, int type, int x, int y, int _item) {
        // item
        Type item{Type::NONE};
        switch (_item) {  //-1 for NONE, 2 for RADAR, 3 for TRAP, 4 ORE
        case -1: item = Type::NONE; break;
        case 2:  item = Type::RADAR; break;
        case 3:  item = Type::TRAP; break;
        case 4:  item = Type::ORE; break;
        default: assert(false);
        }
        Point p{x, y};
        switch (type) {  // 0 for your robot, 1 for other robot, 2 for radar, 3 for trap
        case 0:
        case 1: players.at(type).updateRobot(id, p, item, type); break;
        case 2: radars.emplace_back(id, Type::RADAR, p, item, 0); break;
        case 3: traps.emplace_back(id, Type::TRAP, p, item, 0); break;
        default: assert(false);
        }
    }
};

//*********************************  GAME SIMULATION  **************************************************

//----------------------------------Action----------------------------------------------------------
struct Action {
    static const array<string, 4> LABELS_ACTIONS;

    Point dest;
    ActionType type{ActionType::WAIT};
    Type item{Type::NONE};
    string message;

    void wait(string _message = "") {
        dest = Point{0, 0};
        type = ActionType::WAIT;
        item = Type::NONE;
        message = _message;
    }
    void move(Point _dest, string _message = "") {
        dest = _dest;
        type = ActionType::MOVE;
        item = Type::NONE;
        message = _message;
    }
    void dig(Point _dest, string _message = "") {
        dest = _dest;
        type = ActionType::DIG;
        item = Type::NONE;
        message = _message;
    }
    void request(Type _item, string _message = "") {
        dest = Point{0, 0};
        type = ActionType::REQUEST;
        item = _item;
        message = _message;
    }
    ostream& dump(ostream& ioOut) const {
        ioOut << LABELS_ACTIONS.at((int)(type));
        if (type == ActionType::MOVE || type == ActionType::DIG) { ioOut << " " << dest; }
        if (type == ActionType::REQUEST && item == Type::RADAR) { ioOut << " RADAR"; }
        if (type == ActionType::REQUEST && item == Type::TRAP) { ioOut << " TRAP"; }
        if (message != "") { ioOut << " " << message; }
        return ioOut;
    }
};
const array<string, 4> Action::LABELS_ACTIONS{"WAIT", "MOVE", "DIG", "REQUEST"};
ostream& operator<<(ostream& ioOut, const Action& obj) { return obj.dump(ioOut); }

//*********************************  AI  *****************************************************************

array<Action, MAX_ROBOTS> getActions(Game& game) {
    array<Action, MAX_ROBOTS> actions;

    // smart code here
    if (game.players.at(0).ore <= 0) {
        cerr << "time to collect stuf!\n";
    }
    actions.at(0).wait();
    actions.at(1).wait("I'm Robot1");
    Player& me{game.players.at(0)};
    Robot& robot1{me.robots.at(1)};
    Robot& robot2{me.robots.at(2)};
    if (robot2.distance(robot1) > 3) { actions.at(2).wait("Where is Robot1?"); }
    else { actions.at(2).wait("I'm robot2"); }
    actions.at(3).wait("No beer here!");
    actions.at(4).wait();
    // end smart code

    return actions;
}

//*********************************  MAIN  *****************************************************************

int main() {
    Game game;

    // global inputs
    int width;
    int height;  // size of the map
    cin >> width >> height;
    cin.ignore();

    // game loop
    while (1) {
        game.reset();
        // first loop local inputs
        int myOre;
        int enemyOre;
        cin >> myOre >> enemyOre;
        cin.ignore();
        game.updateOre(0, myOre);
        game.updateOre(1, enemyOre);

        // other loop local inputs
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                string ore;  // amount of ore or "?" if unknown
                int hole;           // 1 if cell has a hole
                cin >> ore >> hole;
                cin.ignore();
                game.updateCell(j, i, ore, hole);
            }
        }
        int entityCount;    // number of visible entities
        int radarCooldown;  // turns left until a new radar can be requested
        int trapCooldown;   // turns left until a new trap can be requested
        cin >> entityCount >> radarCooldown >> trapCooldown;
        cin.ignore();
        game.updateCooldown(0, radarCooldown, trapCooldown);
        for (int i = 0; i < entityCount; i++) {
            int id;    // unique id of the entity
            int type;  // 0 for your robot, 1 for other robot, 2 for radar, 3 for trap
            int x;
            int y;     // position of the entity
            int item;  // if this entity is a robot, the item it is carrying (-1 for NONE, 2 for RADAR, 3 for TRAP, 4 for ORE)
            cin >> id >> type >> x >> y >> item;
            cin.ignore();
            game.updateEntity(id, type, x, y, item);
        }

        // AI ------------------------------------------------------------------
        auto actions{getActions(game)};
        // AI ------------------------------------------------------------------

        for (const Action& action : actions) {
            // Write an action using cout. DON'T FORGET THE "<< endl"
            // To debug: cerr << "Debug messages..." << endl;

            cout << action << "\n";  // WAIT|MOVE x y|REQUEST item
        }
    }
}
