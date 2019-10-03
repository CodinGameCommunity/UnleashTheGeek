import sys
import math

TYPE_NONE = -1
TYPE_FRIEND = 0
TYPE_FOE = 1
TYPE_RADAR = 2
TYPE_TRAP = 3
TYPE_GOLD = 4

class Agent:
    def __init__(self, x, y, item):
        self.x = x
        self.y = y
        self.action = 'WAIT'
        self.tx = -1
        self.ty = -1
        self.item = item
        self.gogo = False


    def thinkHard(self, r, cell, main):
        if self.item == TYPE_GOLD:
            self.tx = 0
            self.ty = self.y
        elif cell != None:
            self.tx = cell[0]
            self.ty = cell[1]
        elif self.item == TYPE_RADAR:
            self.tx = (self.y * 57649 * r + 5535853) % width
            self.ty = (self.y + r) % height
        elif main:
            self.gogo = True
            self.tx = 0
            self.ty = self.y
        else:
            self.tx = (self.y * 5649 * r + 5535853) % width
            self.ty = (self.x * 8485849 * r + 8466231) % height


    def think(self, r, cell = None, main = False):
        if self.tx != -1:
            if self.x == self.tx and self.y == self.ty:
                if self.gogo:
                    self.action = 'REQUEST RADAR'
                else:
                    self.action = f'DIG {self.tx} {self.ty}'    
                self.tx = -1
                self.gogo = False
            else:
                self.action = f'MOVE {self.tx} {self.ty}'
        else:
            self.thinkHard(r, cell, main)
            self.action = f'MOVE {self.tx} {self.ty}'


width, height = [int(i) for i in input().split()]

my_gold = 0


def findGoldCell(golds):
    for y in range(height):
        for x in range(width):
            if golds[y][x] > 0:
                return (x, y)


agents = [ Agent(0, 0, 0) for i in range(5) ]
r = 0

while True:
    my_gold, enemy_gold = [int(i) for i in input().split()]

    golds = [ [ None ] * width for i in range(height) ]
    for i in range(height):
        inputs = input().split()
        for j in range(width):
            gold = inputs[2*j]
            hole = int(inputs[2*j+1])

            golds[i][j] = int(gold) if gold != '?' else 0

    robot = None
    my_other_robots = []

    k = 0

    entity_count, radar_cooldown, trap_cooldown = [int(i) for i in input().split()]
    for i in range(entity_count):
        id, type, x, y, item = [int(j) for j in input().split()]
        if type == TYPE_FRIEND:
            agents[k].x = x
            agents[k].y = y
            agents[k].item = item
            if my_gold == 0 and robot == None and x != -1:
                robot = agents[k]
            else:
                my_other_robots.append(agents[k])
            k += 1

    if robot:
        robot.think(r, findGoldCell(golds), True)
        print(robot.action)

    r += 1

    for robot in my_other_robots:
        if my_gold == 0:
            robot.think(r)
            print(robot.action)
        else:
            print(f'MOVE {(robot.y * 15233 + 545647) % width} {(robot.x * 689241 - 75132) % height}')
