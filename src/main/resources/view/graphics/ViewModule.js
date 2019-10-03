import { WIDTH, HEIGHT } from '../core/constants.js'
import * as game from './gameConstants.js'
import * as utils from '../core/utils.js'
import { messageBox, initMessages } from './MessageBoxes.js'
import { parseData, parseGlobalData } from './Deserializer.js'
import { bell } from '../core/transitions.js'
import { getRenderer, flagForDestructionOnReinit } from '../core/rendering.js'

function zeroPad (x, n) {
  return x <= 9999 ? `000${x}`.slice(-n) : x
}

/* global PIXI */

function is (id) {
  return a => a.id === id
}

const EFFECT_MOVE_ITEM = 0
const EFFECT_BOOM = 1
const EFFECT_REQUEST_ITEM = 2

const ITEM_FRAMES = {
  [game.ITEM_RADAR]: 'Radar',
  [game.ITEM_TRAP]: 'Piege',
  [game.ITEM_ORE]: 'Cristal'
}

const BURIED_ITEM_FRAMES = {
  [game.ITEM_RADAR]: [
    'Radar0001',
    'Radar0002',
    'Radar0003',
    'Radar0004',
    'Radar0005',
    'Radar0006',
    'Radar0007',
    'Radar0008',
    'Radar0009',
    'Radar0010',
    'Radar0011',
    'Radar0012',
    'Radar0013',
    'Radar0014',
    'Radar0015',
    'Radar0016',
    'Radar0017',
    'Radar0018',
    'Radar0019',
    'Radar0020',
    'Radar0021',
    'Radar0022',
    'Radar0023',
    'Radar0024',
    'Radar0025',
    'Radar0026',
    'Radar0027',
    'Radar0028',
    'Radar0029',
    'Radar0030',
    'Radar_2'
  ],
  [game.ITEM_TRAP]: [
    'Piege0001',
    'Piege0002',
    'Piege0003',
    'Piege0004',
    'Piege0005',
    'Piege0006',
    'Piege0007',
    'Piege0008',
    'Piege0009',
    'Piege0010',
    'Piege0011',
    'Piege0012',
    'Piege0013',
    'Piege0014',
    'Piege0015',
    'Piege0016',
    'Piege0017',
    'Piege0018',
    'Piege0019',
    'Piege0020',
    'Piege0021',
    'Piege_2'
  ]
}

const EXPLOSION_FRAMES = []
for (let i = 1; i <= 24; ++i) {
  EXPLOSION_FRAMES.push(`Piege_Explosion00${zeroPad(i, 2)}`)
}

const ROBOT_ANIMATIONS = {
  Creuse: {
    frames: 39,
    anchor: {
      x: 50 / 100,
      y: 128 / 175
    }
  },
  Deterre: {
    frames: 39,
    anchor: {
      x: 50 / 100,
      y: 126 / 175
    }
  },
  Enterre: {
    frames: 33,
    anchor: {
      x: 50.5 / 100,
      y: 128 / 175
    }
  }
}

const api = {
  options: {
    debugMode: false,
    radarRange: -1,
    destinations: false,

    // Messages
    showOthersMessages: false,
    meInGame: false,
    showMyMessages: true

  },
  setDebug: () => {},
  updateAllRadarOverlay: () => {},
  atLeastOnePixel: (width) => {
    if (width > 0 && width < api.toPixel) {
      return api.toPixel
    }
    return width
  }
}
export { api }

export class ViewModule {
  constructor (assets) {
    this.canvasMode = getRenderer().type === PIXI.RENDERER_TYPE.CANVAS
    this.states = []
    this.globalData = {}
    this.pool = {}
    this.renderables = []
    window.module = this

    this.reinitAspectRatio()

    // Graphic constants
    api.toPixel = 1
    api.unconvertPosition = (pos) => this.unconvertPosition(pos)
    api.updateAllRadarOverlay = this.updateAllRadarOverlay.bind(this)
    api.playerIndexFromAgentId = (id) => this.playerIndexFromAgentId(id)
    this.api = api
  }

  static get name () {
    return 'graphics'
  }

  getFromPool (type) {
    if (!this.pool[type]) {
      this.pool[type] = []
    }

    for (let e of this.pool[type]) {
      if (!e.busy) {
        e.busy = true
        if (type === EFFECT_MOVE_ITEM) {
          for (let itemId in e.sprites) {
            e.sprites[itemId].visible = false
          }
        } else if (type === EFFECT_REQUEST_ITEM) {
          for (let itemId in e.item.sprites) {
            e.item.sprites[itemId].visible = false
          }
        }
        return e
      }
    }

    let e = this.createEffect(type)
    this.pool[type].push(e)
    this.fxLayer.addChild(e)
    e.busy = true
    return e
  };

  createItem () {
    const item = this.newLayer()

    const itemSize = 0.8 * this.worldUnitSize

    item.sprites = {}

    for (let itemId in ITEM_FRAMES) {
      let frame = ITEM_FRAMES[itemId]
      const sprite = PIXI.Sprite.fromFrame(frame)
      const scale = utils.fitAspectRatio(sprite.texture.width, sprite.texture.height, itemSize, itemSize)
      sprite.anchor.set(0.5)
      sprite.scale.set(scale)
      sprite.baseScale = scale
      item.addChild(sprite)
      sprite.visible = false
      item.sprites[itemId] = sprite
    }
    return item
  }

  createEffect (type) {
    if (type === EFFECT_MOVE_ITEM) {
      const fx = this.createItem()
      return fx
    } else if (type === EFFECT_BOOM) {
      const fx = PIXI.extras.AnimatedSprite.fromFrames(EXPLOSION_FRAMES)
      fx.anchor.set(0.5)
      fx.width = this.convertDistance(3)
      fx.height = this.convertDistance(3)
      return fx
    } else if (type === EFFECT_REQUEST_ITEM) {
      const fx = new PIXI.Container()
      const item = this.createItem()
      const arm = PIXI.Sprite.fromFrame('Bras_Robot')
      arm.scale.set(this.worldUnitSize / 70)
      fx.arm = arm
      fx.item = item
      arm.anchor.set(119 / 124, 0.5)
      fx.addChild(arm)
      fx.addChild(item)
      return fx
    }
    const graphics = new PIXI.Graphics()
    return graphics
  }

  asLayer (func) {
    const layer = new PIXI.Container()
    func.bind(this)(layer)
    return layer
  }

  updateEvents (previousData, currentData, progress) {
    for (let event of currentData.events) {
      // if (event.type === game.EV_RADAR_DESTRUCTION) {
      //   const fx = this.getFromPool(EFFECT_BOOM)
      //   fx.width = this.convertDistance(0.5)
      //   fx.height = this.convertDistance(0.5)
      //   fx.tint = 0x999999
      //   fx.visible = true
      //   this.setAnimationProgress(fx, progress)
      //   fx.position.copy(this.convertPosition(event))
      // } else
      if (event.type === game.EV_EXPLOSION) {
        const fx = this.getFromPool(EFFECT_BOOM)
        fx.alpha = 0.9
        fx.visible = true
        fx.position.copy(this.convertPosition(event))

        if (progress === 1 && this.playerSpeed === 0 && this.frameDelta !== 0) {
          fx.gotoAndStop(11)
        } else {
          this.setAnimationProgress(fx, progress)
        }
      }
    }
  }

  radarInRange (data, playerIdx, x, y) {
    const key = `${playerIdx} ${x} ${y}`
    if (data.radarCache[key] != null) {
      return data.radarCache[key]
    }
    let result = false
    for (let key in data.items[playerIdx][game.ITEM_RADAR]) {
      if (data.items[playerIdx][game.ITEM_RADAR][key]) {
        const coords = key.split(' ').map(v => +v)
        const itemX = coords[0]
        const itemY = coords[1]
        if (distance(x, y, itemX, itemY) <= game.RADAR_RANGE) {
          result = true
          break
        }
      }
    }
    data.radarCache[key] = result
    return result
  }

  updateAllRadarOverlay () {
    const width = this.globalData.width
    const height = this.globalData.height

    for (let y = 0; y < height; ++y) {
      for (let x = 1; x < width; ++x) {
        const cell = this.cells[y][x]
        this.updateCellRadarOverlay(cell)
      }
    }
  }

  updateCellRadarOverlay (cell) {
    if (api.options.radarRange === 2) {
      let overlayColor
      if (cell.inRadarRange[0] && cell.inRadarRange[1]) {
        overlayColor = utils.lerpColor(this.globalData.players[0].color, this.globalData.players[1].color, 0.5)
      } else if (cell.inRadarRange[0]) {
        overlayColor = this.globalData.players[0].color
      } else {
        overlayColor = this.globalData.players[1].color
      }
      cell.radarOverlay.tint = overlayColor
    } else if (api.options.radarRange !== -1) {
      cell.radarOverlay.tint = this.globalData.players[api.options.radarRange].color
    }
  }

  updateGrid (previous, current, progress) {
    const width = this.globalData.width
    const height = this.globalData.height
    for (let y = 0; y < height; ++y) {
      for (let x = 1; x < width; ++x) {
        const cell = this.cells[y][x]
        let map = current.map
        let prevOre = previous.map[y][x].ore
        let curOre = current.map[y][x].ore
        if (prevOre === 0 && curOre > 0) {
          map = progress < 1 ? previous.map : current.map
        }

        cell.alpha = 1
        if (cell.ore) {
          cell.ore.visible = curOre > 0 || prevOre > 0

          let scale
          if (curOre && !prevOre) {
            scale = utils.unlerp(0, 0.5, progress) || 0.0001
          } else if (prevOre && !curOre) {
            scale = (1 - utils.unlerp(0, 0.9, progress)) || 0.0001
          } else {
            scale = 1
          }
          cell.ore.parent.scale.set(scale)
        }
        let inRadarRange = []
        for (let i = 0; i < this.globalData.playerCount; ++i) {
          inRadarRange.push(this.radarInRange(progress < 1 ? previous : current, i, x, y))
        }
        cell.inRadarRange = inRadarRange

        this.updateCellRadarOverlay(cell)

        for (let item of game.REQUESTABLES) {
          let prevs = this.getMapItems(previous.items, item, x, y)
          let curs = this.getMapItems(current.items, item, x, y)
          let visible = false
          // let alpha = 1
          for (let i = 0; i < this.globalData.playerCount; ++i) {
            let cur = curs[i]
            let prev = prevs[i]
            if (cur === prev) {
              if (cur && prev) {
                visible = true
              }
            } else if (cur !== prev) {
              if (cur && !prev) {
                if (progress >= 0.2584164) {
                  visible = true
                }
              } else if (cur && prev) {
                if (item === game.ITEM_RADAR) {
                  visible = true
                }
              } else if (!cur && prev) {
                // visible = true
                if (progress <= 0.2584164) {
                  visible = true
                }
              }
            }
          }
          // cell.buried[item].alpha = alpha
          if (cell.buried[item]) {
            cell.buried[item].visible = visible
          }
        }

        if (cell.hole) {
          const curHole = current.map[y][x].hole
          const prevHole = previous.map[y][x].hole
          cell.hole.visible = curHole || prevHole
          const p = utils.unlerp(0, 0.5, progress)
          if (curHole && !prevHole) {
            const coeff = p || 0.0001
            cell.hole.scale.x = cell.hole.baseScale.x * coeff
            cell.hole.scale.y = cell.hole.baseScale.y * coeff
            if (cell.ore) {
              cell.ore.texture = progress >= 0.5 ? PIXI.Texture.fromFrame('Cristal_3_shadow') : PIXI.Texture.fromFrame('Cristal_2_shadow')
            }
          } else if (prevHole && curHole) {
            cell.hole.scale.x = cell.hole.baseScale.x
            cell.hole.scale.y = cell.hole.baseScale.y
            if (cell.ore) {
              cell.ore.texture = PIXI.Texture.fromFrame('Cristal_3_shadow')
            }
          } else {
            cell.hole.scale.x = cell.hole.baseScale.x * 1
            cell.hole.scale.y = cell.hole.baseScale.y * 1
            if (cell.ore) {
              cell.ore.texture = PIXI.Texture.fromFrame('Cristal_2_shadow')
            }
          }
        }
      }
    }
  }

  getMapItems (items, itemId, x, y) {
    return items.map(itemMap => itemMap[itemId][x + ' ' + y])
  }

  updateScene (previousData, currentData, progress, playerSpeed) {
    this.playerSpeed = playerSpeed
    this.frameDelta = api.currentData ? (api.currentData.number - currentData.number) : 0

    this.resetEffects()
    this.updateEffects(previousData, currentData, progress)
    this.updateRobots(previousData, currentData, progress)
    this.updateEvents(previousData, currentData, progress)
    this.updateGrid(previousData, currentData, progress)
    this.updateHud(previousData, currentData, progress)

    for (let updatable of this.updatables) {
      updatable.update(previousData, currentData, progress)
    }

    api.previousData = previousData
    api.currentData = currentData
    api.progress = progress
  }
  resetEffects () {
    for (let type in this.pool) {
      for (let effect of this.pool[type]) {
        effect.visible = false
        effect.alpha = 1
        effect.busy = false
      }
    }
  }

  updateEffects (previousData, currentData, progress) {

  }

  updateHud (previous, current, progress) {
    for (let i = 0; i < this.globalData.playerCount; ++i) {
      this.hud[i].score.text = `${(progress === 1 ? current : previous).scores[i]}`
      for (let itemId of game.REQUESTABLES) {
        const countdown = current.countdowns[i][itemId]
        const prevCountdown = previous.countdowns[i][itemId]
        const curP = utils.unlerp(game.ITEM_COOLDOWN, 0, countdown)
        const prevP = utils.unlerp(game.ITEM_COOLDOWN, 0, prevCountdown)
        let p
        if (prevP > curP) {
          p = 1
        } else {
          p = utils.lerp(prevP, curP, progress)
        }

        const percent = Math.round(100 * p)
        this.hud[i].percentage[itemId].text = `${percent}%`
        const itemMask = this.hud[i].itemMask[itemId]
        itemMask.y = utils.lerp(itemMask.baseY + 40, itemMask.baseY, p)
      }
    }
  }

  unconvertPosition (pos) {
    const padding = this.padding
    return {
      x: ((pos.x - padding.left - this.worldUnitSize / 2) / (WIDTH - padding.left - padding.right)) * this.globalData.width,
      y: (pos.y - padding.top - this.worldUnitSize / 2) / this.worldUnitSize
    }
  }

  convertPosition (pos) {
    const padding = this.padding
    return {
      x: pos.x / this.globalData.width * (WIDTH - padding.left - padding.right) + padding.left + this.worldUnitSize / 2,
      y: padding.top + this.worldUnitSize * 0.5 + this.worldUnitSize * pos.y
    }
  }
  convertDistance (x) {
    return x / this.globalData.width * (WIDTH - this.padding.left - this.padding.right)
  }

  playerIndexFromAgentId (agentId) {
    return agentId < this.globalData.agentsPerPlayer ? 0 : 1
  }

  updateMapOre (map, x, y, amount) {
    map[y] = [...map[y]]
    map[y][x] = { ...map[y][x] }
    map[y][x].ore += amount
    if (map[y][x].ore < 0) {
      map[y][x].ore = 0
    }
  }

  addHole (map, x, y) {
    map[y] = [...map[y]]
    map[y][x] = { ...map[y][x] }
    map[y][x].hole = true
  }

  handleFrameData (frameInfo, raw) {
    const data = parseData(raw, this.globalData)
    return this._handleFrameData(frameInfo, data)
  }

  _handleFrameData (frameInfo, data) {
    const previous = this.states[this.states.length - 1]

    const items = []
    const countdowns = []
    for (let i = 0; i < this.globalData.playerCount; ++i) {
      const itemMap = {}
      const countdownMap = {}

      for (let itemType of game.REQUESTABLES) {
        itemMap[itemType] = previous ? {
          ...previous.items[i][itemType]
        } : {}
        countdownMap[itemType] = previous ? previous.countdowns[i][itemType] : 0
        if (countdownMap[itemType]) {
          countdownMap[itemType]--
        }
      }
      items.push(itemMap)
      countdowns.push(countdownMap)
    }

    data.agents = data.agents.sort((a, b) => a.id - b.id)

    data.agents.forEach(a => {
      a.events = []
      if (previous) {
        a.item = previous.agents[a.id].item
      } else {
        a.item = {
          type: game.ITEM_NONE
        }
      }
    })

    const map = previous ? [...previous.map] : [...this.map]

    // Events must stay sorted by computation order
    for (let event of data.events) {
      if (event.agent != null) {
        data.agents[event.agent].events.push(event)
      }

      if (event.type === game.EV_BURY) {
        data.agents[event.agent].item = {
          type: game.ITEM_NONE
        }
        if (event.item === game.ITEM_ORE) {
          this.updateMapOre(map, event.x, event.y, 1)
        } else if (event.item !== game.ITEM_NONE) {
          const playerIdx = this.playerIndexFromAgentId(event.agent)
          this.updateMapItem(items[playerIdx], event.item, event, true)
          this.globalItemMap[event.item][event.x + ' ' + event.y] = true
        }
        this.addHole(map, event.x, event.y)
        this.globalHoleMap[event.x + ' ' + event.y] = true
      } else if (event.type === game.EV_RADAR_DESTRUCTION) {
        const destroyer = this.playerIndexFromAgentId(event.agent)
        for (let i = 0; i < this.globalData.playerCount; ++i) {
          if (i !== destroyer) {
            this.updateMapItem(items[i], game.ITEM_RADAR, event, false)
          }
        }
      } else if (event.type === game.EV_EXPLOSION) {
        for (let i = 0; i < this.globalData.playerCount; ++i) {
          this.updateMapItem(items[i], game.ITEM_TRAP, event, false)
        }
      } else if (event.type === game.EV_GET_ORE) {
        this.updateMapOre(map, event.x, event.y, -1)
        data.agents[event.agent].item = {
          type: game.ITEM_ORE
        }
      } else if (event.type === game.EV_REQUEST) {
        const requester = this.playerIndexFromAgentId(event.agent)
        countdowns[requester][event.item] = game.ITEM_COOLDOWN
        data.agents[event.agent].item = {
          type: event.item
        }
      } else if (event.type === game.EV_GIVE_ORE) {
        const agent = data.agents[event.agent]
        agent.item = {
          type: game.ITEM_NONE,
          delay: true
        }
      } else if (event.type === game.EV_AGENT_DEATH) {
        const agent = data.agents[event.agent]
        agent.item = {
          type: game.ITEM_NONE,
          delay: true
        }
      }
    }

    if (previous) {
      for (let event of previous.events) {
        if (event.type === game.EV_GIVE_ORE) {
          const agent = data.agents[event.agent]
          agent.givingOre = true
        }
      }
    }

    const state = {
      ...data,
      number: frameInfo.number,
      items,
      countdowns,
      map,
      radarCache: {}
    }
    state.previous = previous || state

    this.states.push(state)
    return state
  }

  updateMapItem (itemMap, itemId, itemPos, itemPresence) {
    itemMap[itemId] = {
      ...itemMap[itemId],
      [itemPos.x + ' ' + itemPos.y]: itemPresence ? ++this.itemCount : null
    }
  }

  newLayer () {
    return new PIXI.Container()
  }

  reinitAspectRatio () {
    this.padding = {
      bottom: 25,
      top: 148,
      left: 72,
      right: 20
    }
  }

  reinitScene (container, canvasData) {
    this.oversampling = canvasData.oversampling

    api.toPixel = (WIDTH / canvasData.width) * canvasData.oversampling
    this.container = container
    this.pool = {}
    this.renderables = []
    this.updatables = []

    const messageLayer = this.asLayer(initMessages)
    const background = PIXI.Sprite.fromFrame('Background.jpg')
    // background.alpha = 0.6
    const gridLayer = this.asLayer(this.initGrid)
    const hudLayer = this.asLayer(this.initHud)
    const robotLayer = this.asLayer(this.initRobots)
    const debugLayer = this.asLayer(this.initDebugLines)
    // const itemLayer = this.asLayer(this.initItems)
    this.fxLayer = this.newLayer()

    this.container.addChild(background)
    this.container.addChild(gridLayer)
    this.container.addChild(robotLayer)
    this.container.addChild(this.fxLayer)
    this.container.addChild(hudLayer)
    // this.container.addChild(itemLayer)
    this.container.addChild(debugLayer)

    this.container.addChild(messageLayer)

    gridLayer.interactiveChildren = false
    // itemLayer.interactiveChildren = false
    this.fxLayer.interactiveChildren = false
    hudLayer.interactiveChildren = false
    messageLayer.interactiveChildren = false

    // this.globalItemMap = null
    // this.globalHoleMap = null
  }

  generateTexture (graphics) {
    var tex = getRenderer().generateTexture(graphics)
    flagForDestructionOnReinit(tex)
    return tex
  }

  createRobot (playerIndex, robotIndex) {
    const robotSize = 1.25 * this.worldUnitSize
    return (layer) => {
      const colorName = playerIndex ? 'Rouge' : 'Bleu'
      // let image = `Robot_${colorName}.png`
      let image = `${colorName}_Roule0001`
      const texture = PIXI.Texture.fromFrame(image)
      const scale = utils.fitAspectRatio(texture.width, texture.height, robotSize, robotSize)
      const body = PIXI.Sprite.fromFrame(image)
      body.rotation = Math.PI / 2
      body.anchor.set(48 / 98, 49 / 98)
      body.scale.set(scale)

      for (const animKey in ROBOT_ANIMATIONS) {
        const frames = []
        const anim = ROBOT_ANIMATIONS[animKey]

        for (let i = 0; i < anim.frames; ++i) {
          frames.push(`${colorName}_${animKey}${zeroPad(i + 1, 4)}`)
        }

        const view = PIXI.extras.AnimatedSprite.fromFrames(frames)
        view.rotation = Math.PI / 2
        view.scale.set(scale)
        view.anchor.copy(anim.anchor)
        // view.loop = true
        // view.play()
        view.visible = false
        layer.addChild(view)
        layer[animKey] = view
      }
      layer.Roule = body
      layer.addChild(body)

      layer.interactive = true
      layer.mousemove = (event) => {
        if (body.containsPoint(event.data.global)) {
          api.getMouseOverFunc(robotIndex, api.tooltip)()
          layer.mouseIsOver = true
        } else {
          api.getMouseOutFunc(robotIndex, api.tooltip)()
          layer.mouseIsOver = false
        }
      }

      layer.k = robotIndex
    }
  }

  // initItems (layer) {
  //   for (let robot of this.robots) {
  //     const item = this.createItem()

  //     robot.item = item

  //     layer.addChild(item)
  //   }
  // }

  initDebugLines (layer) {
    this.debugLines = []

    const getShouldShowLine = (robot) => {
      return () => {
        if (!api.options.destinations) {
          return robot.mouseIsOver
        }
        return true
      }
    }

    for (let robot of this.robots) {
      let line = new PIXI.Graphics()
      this.debugLines.push(line)
      layer.addChild(line)
      Object.defineProperty(line, 'visible', {
        get: getShouldShowLine(robot)
      })
    }
  }

  initRobots (layer) {
    const robots = []
    let robotIndex = 0
    for (let i = 0; i < this.globalData.playerCount; i++) {
      for (let k = 0; k < this.globalData.agentsPerPlayer; ++k) {
        const robot = this.asLayer(this.createRobot(i, robotIndex++))
        robot.owner = i
        robot.targetPosition = null
        robot.targetRotation = 0
        robots.push(robot)
        const item = this.createItem()
        robot.item = item
        layer.addChild(robot)
        layer.addChild(item)
        if (this.messages) {
          robot.message = this.messages[i][k]
        }
        robot.render = (step) => {
          if (!robot.targetPosition) {
            return true
          }
          if (this.playerSpeed === 0 && Math.abs(this.frameDelta) <= 6) {
            const stepFactor = Math.pow(0.983, step)
            robot.x = robot.x * stepFactor + robot.targetPosition.x * (1 - stepFactor)
            robot.y = robot.y * stepFactor + robot.targetPosition.y * (1 - stepFactor)
          } else {
            robot.position.copy(robot.targetPosition)
          }

          if (robot.rotation !== robot.targetRotation) {
            const eps = 0.02
            let r = utils.lerpAngle(robot.rotation, robot.targetRotation, 0.133)
            if (angleDiff(r, robot.targetRotation) < eps) {
              r = robot.targetRotation
            }
            robot.rotation = r
          }

          robot.item.position.copy(robot.position)
          robot.item.rotation = robot.rotation
        }
        this.renderables.push(robot)
      }
    }
    this.robots = robots
    api.robots = robots
  }

  updateRobots (previous, current, progress) {
    for (let i = 0; i < this.robots.length; ++i) {
      let robot = this.robots[i]
      let now = current.agents.find(is(i)) // TODO: use map?
      let prev = previous.agents.find(is(i))
      robot.Roule.visible = true
      robot.Creuse.visible = false
      robot.Enterre.visible = false
      robot.Deterre.visible = false

      if (now.item.delay && prev.item.type !== game.ITEM_NONE) {
        robot.item.visible = true
        for (let itemId in robot.item.sprites) {
          robot.item.sprites[itemId].visible = false
        }
        robot.item.sprites[prev.item.type].visible = true
      } else {
        if (now.item.type !== game.ITEM_NONE) {
          let visible = false
          if (prev.item.type === game.ITEM_NONE) {
            visible = progress === 1
          } else if (now.item === prev.item) {
            visible = true
          } else if (now.item !== prev.item) {
            visible = progress === 1
          }
          robot.item.visible = visible
          for (let itemId in robot.item.sprites) {
            robot.item.sprites[itemId].visible = false
          }

          let itemId = (progress < 1 && prev.item.type !== game.ITEM_NONE) ? prev.item.type : now.item.type
          robot.item.sprites[itemId].visible = true
        } else {
          robot.item.visible = false
        }
      }

      // Destination debug line
      const line = this.debugLines[i]
      line.clear()
      robot.item.alpha = 1
      if (prev.dead && !now.dead) {
        // Spawn animation
        robot.alpha = progress
        robot.targetRotation = 0
        robot.rotation = 0
        robot.visible = true
        if (robot.targetPosition == null) {
          robot.targetPosition = new PIXI.Point()
        }
        robot.targetPosition.copy(this.convertPosition(now))
        robot.position.copy(robot.targetPosition)
      } else if (!prev.dead && now.dead) {
        // Destruction animation
        robot.alpha = 1 - progress
        robot.item.alpha = 1 - progress
        robot.visible = progress < 1
        if (robot.targetPosition == null) {
          robot.targetPosition = new PIXI.Point()
        }
        robot.targetPosition.copy(this.convertPosition(now))
        robot.position.copy(robot.targetPosition)
        if (now.item !== game.ITEM_NONE) {
          robot.item.visible &= robot.visible
        }
      } else if (prev.dead && now.dead) {
        robot.visible = false
        robot.item.visible = false
        robot.targetPosition = new PIXI.Point()
      } else {
        // On screen
        robot.visible = true
        robot.alpha = 1

        let from = this.convertPosition(prev)
        let to = this.convertPosition(now)
        let position = utils.lerpPosition(from, to, progress)

        if (from.x !== to.x || from.y !== to.y) {
          let angle = Math.atan2(to.y - from.y, to.x - from.x)
          if (robot.targetRotation == null) {
            robot.rotation = angle
          }
          robot.targetRotation = angle
        }

        if (robot.targetPosition == null) {
          robot.position.copy(position)
          robot.targetPosition = new PIXI.Point()
        }
        robot.targetPosition.copy(position)

        // Destination debug line
        if (now.tx != null) {
          const target = this.convertPosition({
            x: now.tx,
            y: now.ty
          })
          line.lineStyle(api.atLeastOnePixel(1), this.globalData.players[robot.owner].color, 1)
          line.moveTo(robot.targetPosition.x, robot.targetPosition.y)
          line.lineTo(target.x, target.y)
        }
      }
      robot.item.pivot.set(0)
      robot.item.sprites[game.ITEM_ORE].scale.set(robot.item.sprites[game.ITEM_ORE].baseScale)
      for (let event of now.events) {
        if (event.type === game.EV_REQUEST) {
          const fx = this.getFromPool(EFFECT_REQUEST_ITEM)
          fx.visible = progress < 1
          fx.item.sprites[event.item].visible = true
          fx.position.copy(robot.position)
          const offset = this.worldUnitSize * 1.5
          if (progress < 0.5) {
            fx.arm.x = utils.lerp(-offset, 0, utils.unlerp(0, 0.5, progress))
            fx.item.x = fx.arm.x
          } else {
            fx.arm.x = utils.lerp(0, -offset, utils.unlerp(0.5, 1, progress))
            fx.item.x = 0
          }
        } else if (event.type === game.EV_BURY) {
          let animation = event.item === game.ITEM_NONE ? robot.Creuse : robot.Enterre
          if (progress <= 0.5) {
            this.playAnimation(robot, animation, progress * 2)
            if (event.item !== game.ITEM_NONE) {
              robot.item.sprites[event.item].visible = true
              robot.item.visible = true
              if (animation.currentFrame >= 0 && animation.currentFrame < 11) {
                robot.item.pivot.x = (0.1 * this.worldUnitSize) * utils.unlerp(0, 11, animation.currentFrame)
              } else if (animation.currentFrame >= 11 && animation.currentFrame < 16) {
                robot.item.pivot.x = -this.worldUnitSize * utils.unlerp(11, 16, animation.currentFrame)
              } else if (animation.currentFrame >= 16 && animation.currentFrame <= 17) {
                robot.item.pivot.x = -this.worldUnitSize
              } else {
                robot.item.visible = false
              }
            }
          }

          if (now.x !== event.x || now.y !== event.y) {
            let angle = Math.atan2(event.y - now.y, event.x - now.x)
            if (robot.targetRotation == null) {
              robot.rotation = angle
            }
            robot.targetRotation = angle
          }
        } else if (event.type === game.EV_GET_ORE) {
          if (progress > 0.5) {
            this.playAnimation(robot, robot.Deterre, utils.unlerp(0.5, 1, progress))
            robot.item.visible = true
            robot.item.sprites[game.ITEM_ORE].visible = true
            robot.item.sprites[game.ITEM_TRAP].visible = false
            robot.item.sprites[game.ITEM_RADAR].visible = false
            if (robot.Deterre.currentFrame < 19) {
              robot.item.pivot.x = -this.worldUnitSize
              robot.item.sprites[game.ITEM_ORE].scale.set(utils.unlerp(0, 19, robot.Deterre.currentFrame) * robot.item.sprites[game.ITEM_ORE].baseScale)
            } else if (robot.Deterre.currentFrame >= 19 && robot.Deterre.currentFrame < 29) {
              robot.item.pivot.x = utils.lerp(-this.worldUnitSize, -0.01 * this.worldUnitSize, utils.unlerp(19, 29, robot.Deterre.currentFrame))
            } else {
              robot.item.pivot.x = utils.lerp(-0.09 * this.worldUnitSize, 0, utils.unlerp(29, 39, robot.Deterre.currentFrame))
            }
          }
        }
      }
      if (now.givingOre) {
        const p = utils.unlerp(0, 0.6, progress)
        const fx = this.getFromPool(EFFECT_MOVE_ITEM)
        fx.visible = p < 1
        fx.sprites[game.ITEM_ORE].visible = true
        fx.position.copy(this.convertPosition(prev))
        fx.position.x -= p * this.convertDistance(2)
        fx.scale.set(utils.lerp(1, 4, bell(p)))
        fx.alpha = 1 - p
      }

      // Update Message
      this.updateMessage(robot, i, now.message)
    }
  }

  playAnimation (robot, animation, progress) {
    animation.visible = true
    robot.Roule.visible = false
    this.setAnimationProgress(animation, progress)
    /*
    let idx = Math.floor(utils.lerp(0, animation.totalFrames, progress))
    if (idx === animation.totalFrames) {
      idx--
    }
    animation.gotoAndStop(idx)
    */
  }

  updateMessage (robot, id, messageText) {
    let message = robot.message
    message.position.copy(robot.targetPosition)
    message.x += messageBox.offset.x
    message.y += messageBox.offset.y
    var minPad = 10
    var scale = {
      x: (message.messageText.width + minPad) / messageBox.width,
      y: (message.messageText.height + minPad) / 80
    }

    message.messageBackground.scale.x = Math.max(1, scale.x)
    message.messageBackground.scale.y = Math.max(1, scale.y)

    if (messageText) {
      message.messageText.text = messageText

      // Shorten message if it doesn't fit in two lines
      let shortened = false
      while (message.messageText.height > 100) {
        message.messageText.text = message.messageText.text.substring(0, message.messageText.text.length - 1)
        shortened = true
      }
      if (shortened) {
        message.messageText.text = message.messageText.text.substring(0, message.messageText.text.length - 2) + '...'
      }

      message.visible = true
    } else {
      message.visible = false
    }
  }

  setAnimationProgress (fx, progress) {
    let idx = Math.floor(progress * fx.totalFrames)
    idx = Math.min(fx.totalFrames - 1, idx)
    fx.gotoAndStop(idx)
  }

  initHud (layer) {
    this.hud = []
    layer.addChild(PIXI.Sprite.fromFrame('Hud_left.png'))
    layer.addChild(PIXI.Sprite.fromFrame('Hud_top.png'))
    for (let i = 0; i < this.globalData.playerCount; ++i) {
      this.hud.push({})
      let playerHud = this.asLayer(this.initPlayerHud(i))
      playerHud.position.set(i * WIDTH, 0)

      layer.addChild(playerHud)
    }
    const left = PIXI.Sprite.fromFrame('Hud_dessus_left')
    const right = PIXI.Sprite.fromFrame('Hud_dessus_right')
    left.position.set(66, 13)
    right.position.set(1800, 13)
    layer.addChild(right)
    layer.addChild(left)
  }

  initPlayerHud (index) {
    const player = this.globalData.players[index]
    return (layer) => {
      let avatar = PIXI.Sprite.fromFrame('$' + index)
      avatar.position.set(index ? -22 : 75, 22)
      avatar.width = 93
      avatar.height = 93
      avatar.anchor.x = index

      const bannerWidth = 340

      let name = new PIXI.Text()
      name.text = player.name
      name.style.fill = 0xFFFFFF
      name.style.fontWeight = 'bold'
      name.style.fontSize = 34
      name.style.fontFamily = 'Lato'
      name.anchor.x = 0.5
      name.anchor.y = 0.5
      name.x = index ? -275 : 329
      name.y = 48

      let coeff = utils.fitAspectRatio(name.width, name.height, bannerWidth, 40)
      name.scale.set(Math.min(1, coeff))

      let score = new PIXI.Text()
      score.text = '0'
      score.style.fontWeight = 'bold'
      score.style.fill = 'white'
      score.style.fontSize = 57
      score.style.fontFamily = 'Arial'
      score.anchor.set(0.5, 1)

      if (index === 0) {
        score.position.set(583, 94)
      } else {
        score.position.set(-531, 94)
      }

      const indicators = new PIXI.Container()

      this.hud[index].percentage = {}
      this.hud[index].itemMask = {}

      for (let i = 0; i < game.REQUESTABLES.length; ++i) {
        const itemId = game.REQUESTABLES[i]
        const itemOff = PIXI.Sprite.fromFrame(`HUD_${i ? 'Radar' : 'Piege'}_Off`)
        const itemOn = PIXI.Sprite.fromFrame(`HUD_${itemId === game.ITEM_RADAR ? 'Radar' : 'Piege'}_Ok`)
        const itemMask = new PIXI.Graphics()

        itemOff.anchor.set(0.5)
        itemOn.anchor.set(0.5)
        itemOn.position.set(100 + 100 * i, 10)
        itemOff.position.copy(itemOn)
        itemMask.position.copy(itemOn)
        itemMask.beginFill()
        itemMask.drawRect(-20, -20, 40, 40)
        itemMask.endFill()

        indicators.addChild(itemOff)
        indicators.addChild(itemOn)
        indicators.addChild(itemMask)

        itemMask.baseY = itemMask.y
        itemOn.mask = itemMask

        const percentage = new PIXI.Text('100')
        percentage.style.fontFamily = 'Arial'
        percentage.style.fontSize = 20
        percentage.style.fill = 'white'
        percentage.style.fontWeight = 'bold'
        percentage.anchor.set(0.5, 0.5)
        percentage.position.set(145 + 100 * i, 10)
        indicators.addChild(percentage)

        this.hud[index].percentage[itemId] = percentage
        this.hud[index].itemMask[itemId] = itemMask
      }
      if (index === 0) {
        indicators.position.set(150, 104)
      } else {
        indicators.position.set(-450, 104)
      }

      this.hud[index].score = score

      layer.addChild(avatar)
      layer.addChild(name)
      layer.addChild(score)
      layer.addChild(indicators)
    }
  }

  initGrid (layer) {
    this.cells = []

    const width = this.globalData.width
    const height = this.globalData.height

    const floorLayer = this.newLayer()
    const itemLayer = this.newLayer()
    const holeLayer = this.newLayer()
    const oreLayer = this.newLayer()

    this.worldUnitSize = this.convertDistance(1)

    const cellSize = this.worldUnitSize

    let cellTexture = null
    let overlayTexture = null
    if (!this.canvasMode) {
      cellTexture = this.generateTexture(this.drawSquare(cellSize, 0))
      overlayTexture = this.generateTexture(this.drawSquare(cellSize, 1))
    }

    for (let y = 0; y < height; ++y) {
      let row = []

      for (let x = 0; x < width; ++x) {
        let cell
        let radarOverlay
        if (this.canvasMode) {
          cell = this.drawSquare(cellSize, 0)
          radarOverlay = this.drawSquare(cellSize, 1)
        } else {
          cell = new PIXI.Sprite(cellTexture)
          radarOverlay = new PIXI.Sprite(overlayTexture)
        }

        cell.radarOverlay = radarOverlay
        cell.inRadarRange = []
        cell.x = (cell.x + cellSize) * x
        cell.y = (cell.y + cellSize) * y
        row.push(cell)
        floorLayer.addChild(cell)

        radarOverlay.alpha = 0.35
        radarOverlay.position.copy(cell.position)
        floorLayer.addChild(radarOverlay)
        Object.defineProperty(radarOverlay, 'visible', {
          get: () => {
            if (api.options.radarRange === -1) {
              return false
            }
            if (api.options.radarRange === 2) {
              return cell.inRadarRange.some(v => v)
            }
            return cell.inRadarRange[api.options.radarRange]
          }
        })

        if (x > 0) {
          cell.buried = {}

          let buriedItemSize = this.worldUnitSize * 1
          if (this.map[y][x].ore) {
            let oreFloor = PIXI.Sprite.fromFrame('Cristal_2_shadow')
            oreFloor.width = buriedItemSize * 88 / 60
            oreFloor.height = buriedItemSize * 88 / 60
            oreFloor.anchor.set(0.5)
            oreFloor.rotation = [0, Math.PI, Math.PI / 2, -Math.PI / 2][Math.floor(Math.random() * 4)]

            oreFloor.visible = false
            const oreContainer = new PIXI.Container()
            oreContainer.position.copy(this.convertPosition({ x, y }))
            oreContainer.addChild(oreFloor)
            oreLayer.addChild(oreContainer)
            cell.ore = oreFloor
          }

          for (let item of [game.ITEM_TRAP, game.ITEM_RADAR]) {
            if (this.globalItemMap[item][x + ' ' + y]) {
              const frames = BURIED_ITEM_FRAMES[item]
              const buried = PIXI.extras.AnimatedSprite.fromFrames(frames)
              itemLayer.addChild(buried)
              buried.width = buriedItemSize * 118 / 60
              buried.height = buriedItemSize * 118 / 60

              cell.buried[item] = buried
              buried.anchor.set(0.5)
              if (item === game.ITEM_RADAR) {
                buried.pivot.x -= 1
              } else if (item === game.ITEM_TRAP) {
                buried.pivot.x -= 2
                buried.pivot.y -= 2
              }
              buried.position.copy(this.convertPosition({ x, y }))
              buried.gotoAndStop(frames.length - 1)
              buried.loop = false
              buried.animationSpeed = 0.35
              buried.countdownToAnimation = 0
              buried.render = (step) => {
                if (buried.visible && !buried.playing) {
                  if (buried.countdownToAnimation <= 0) {
                    buried.gotoAndPlay(0)
                    buried.countdownToAnimation = 1000 + Math.floor(Math.random() * 6000)
                  } else {
                    buried.countdownToAnimation -= step
                  }
                }
              }
              this.renderables.push(buried)
            }
          }

          if (this.globalHoleMap[x + ' ' + y]) {
            const hole = PIXI.Sprite.fromFrame('Terre')
            hole.tint = 0xAAAAAA
            cell.hole = hole
            hole.rotation = (Math.random() - 0.5) / 2
            holeLayer.addChild(hole)

            hole.width = buriedItemSize
            hole.height = buriedItemSize
            hole.baseScale = { x: hole.scale.x, y: hole.scale.x }
            hole.anchor.set(0.5)
            hole.position.copy(this.convertPosition({ x, y }))
          }
        }
      }

      this.cells.push(row)
    }

    floorLayer.position.set(this.padding.left, this.padding.top)

    layer.addChild(floorLayer)
    layer.addChild(holeLayer)
    layer.addChild(oreLayer)
    layer.addChild(itemLayer)
  }

  drawSquare (cellSize, alpha) {
    let cell = new PIXI.Graphics()
    cell.lineStyle(api.atLeastOnePixel(1), 0x0, 0.2)
    cell.beginFill(0xFFFFFF, alpha)
    cell.drawRect(0, 0, cellSize, cellSize)
    cell.endFill()
    return cell
  }

  // cellColorByOre (ore) {
  //   return ore ? 0xD4AF37 : 0xEDC9AF
  // }

  animateScene (delta) {
    let next = []
    for (let renderable of this.renderables) {
      if (!renderable.render(delta)) {
        next.push(renderable)
      }
    }
    this.renderables = next
  }

  handleGlobalData (players, raw) {
    const globalData = parseGlobalData(raw)

    return this._handleGlobalData(players, globalData)
  }

  _handleGlobalData (players, globalData) {
    this.globalData = {
      ...globalData,
      players: players,
      playerCount: players.length
    }
    api.globalData = globalData
    api.options.meInGame = !!players.find(p => p.isMe)

    this.map = []
    for (let y = 0; y < globalData.height; ++y) {
      let row = []
      for (let x = 0; x < globalData.width; ++x) {
        row.push({ ore: 0 })
      }
      this.map.push(row)
    }

    this.globalHoleMap = {}
    this.globalItemMap = {
      [game.ITEM_RADAR]: {},
      [game.ITEM_TRAP]: {}
      // [game.ITEM_ORE]: {}
    }

    for (let cellData of globalData.ore) {
      this.map[cellData.y][cellData.x].ore = cellData.ore
      // this.globalItemMap[game.ITEM_ORE][cellData.x + ' ' + cellData.y] = true
    }

    this.itemCount = 0
    api.globalData = globalData
  }
}

function distance (x1, y1, x2, y2) {
  return Math.abs(x1 - x2) + Math.abs(y1 - y2)
}

function angleDiff (a, b) {
  return Math.abs(utils.lerpAngle(a, b, 0) - utils.lerpAngle(a, b, 1))
}
