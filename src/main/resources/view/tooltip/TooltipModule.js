import { WIDTH, HEIGHT } from '../core/constants.js'
import { api as ViewModule } from '../graphics/ViewModule.js'
import * as game from '../graphics/gameConstants.js'

/* global PIXI */

ViewModule.getMouseOverFunc = function (id, tooltip) {
  return function () {
    tooltip.inside[id] = true
  }
}

ViewModule.getMouseOutFunc = function (id, tooltip) {
  return function () {
    delete tooltip.inside[id]
  }
}

const ITEM_NAMES = {
  2: 'radar',
  3: 'trap',
  4: 'ore'
}

const ITEMS = {
  RADAR: 2,
  TRAP: 3
}

const COLOUR_NAMES = ['blue', 'red']

function getMouseMoveFunc (tooltip, container, module) {
  return function (ev) {
    if (tooltip) {
      var pos = ev.data.getLocalPosition(container)
      tooltip.x = pos.x
      tooltip.y = pos.y
      var point = ViewModule.unconvertPosition(pos)

      point.x = Math.round(point.x)
      point.y = Math.round(point.y)

      const showing = []
      const ids = Object.keys(tooltip.inside).map(n => +n)
      const tooltipBlocks = []

      for (let id of ids) {
        if (!ViewModule.robots) {
          break
        } else if (tooltip.inside[id]) {
          const entity = ViewModule.robots[id]
          if (!entity) {
            delete tooltip.inside[id]
          } else {
            showing.push(id)
          }
        }
      }

      if (showing.length) {
        for (let show of showing) {
          const entity = ViewModule.robots[show]

          if (entity) {
            const data = (ViewModule.progress === 1 ? ViewModule.currentData : ViewModule.previousData) || ViewModule.currentData

            let robot = ViewModule.currentData.agents[show]
            let displayedRobot = data.agents[show]
            let tooltipBlock = `Robot ${show} (${COLOUR_NAMES[ViewModule.playerIndexFromAgentId(robot.id)]})`
            if (robot != null) {
              let dig = robot.events.find(e => e.type === game.EV_BURY)
              if (dig) {
                tooltipBlock += `\naction: DIG ${dig.x} ${dig.y}`
              } else {
                let req = robot.events.find(e => e.type === game.EV_REQUEST)
                if (req) {
                  tooltipBlock += `\naction: REQUEST ${req.item === ITEMS.RADAR ? 'RADAR' : 'TRAP'}`
                }
              }
              if (displayedRobot.item && displayedRobot.item.type !== game.ITEM_NONE) {
                tooltipBlock += `\nitem: ${ITEM_NAMES[displayedRobot.item.type]}`
              }
            }

            tooltipBlocks.push(tooltipBlock)
          }
        }
      }

      if (point.y >= 0 && point.x >= 0 && point.x < ViewModule.globalData.width && point.y < ViewModule.globalData.height) {
        const x = point.x
        const y = point.y

        let tooltipBlock = 'x: ' + x + '\ny: ' + y

        let data = (ViewModule.progress === 1 ? ViewModule.currentData : ViewModule.previousData) || ViewModule.currentData

        if (x > 0) {
          tooltipBlock += '\nore: ' + data.map[y][x].ore
        }

        for (let itemName in ITEMS) {
          let itemId = ITEMS[itemName]
          let hasItem = []
          for (let player = 0; player < 2; ++player) {
            hasItem.push(data.items[player][itemId][x + ' ' + y])
          }

          if (hasItem.some(v => v != null)) {
            tooltipBlock += `\n${itemName}`
            if (hasItem.every(v => v != null)) {
              tooltipBlock += ' (both players)'
            } else {
              let playerIdx = hasItem.map(v => v != null).indexOf(true)

              let playerName = COLOUR_NAMES[playerIdx]
              tooltipBlock += ` (${playerName} player)`
            }
          }
        }
        tooltipBlocks.push(tooltipBlock)
      }

      if (tooltipBlocks.length) {
        tooltip.label.text = tooltipBlocks.join('\n──────────\n')
        tooltip.visible = true
      } else {
        tooltip.visible = false
      }

      tooltip.background.width = tooltip.label.width + 20
      tooltip.background.height = tooltip.label.height + 20

      tooltip.pivot.x = -30
      tooltip.pivot.y = -50

      if (tooltip.y - tooltip.pivot.y + tooltip.height > HEIGHT) {
        tooltip.pivot.y = 10 + tooltip.height
        tooltip.y -= tooltip.y - tooltip.pivot.y + tooltip.height - HEIGHT
      }

      if (tooltip.x - tooltip.pivot.x + tooltip.width > WIDTH) {
        tooltip.pivot.x = tooltip.width
      }
    }
  }
};

export class TooltipModule {
  constructor (assets) {
    this.interactive = {}
    this.previousFrame = {
      registrations: {},
      extra: {}
    }
    this.lastProgress = 1
    this.lastFrame = 0
  }

  static get name () {
    return 'tooltips'
  }

  updateScene (previousData, currentData, progress) {
    this.currentFrame = currentData
    this.currentProgress = progress
  }

  handleFrameData (frameInfo) {
    return {}
  }

  reinitScene (container, canvasData) {
    this.tooltip = this.initTooltip()
    ViewModule.tooltip = this.tooltip
    this.container = container
    container.interactive = true
    container.mousemove = getMouseMoveFunc(this.tooltip, container, this)
    container.addChild(this.tooltip)
  }

  generateText (text, size, color, align) {
    var textEl = new PIXI.Text(text, {
      fontSize: Math.round(size / 1.2) + 'px',
      fontFamily: 'Lato',
      fontWeight: 'bold',
      fill: color
    })

    textEl.lineHeight = Math.round(size / 1.2)
    if (align === 'right') {
      textEl.anchor.x = 1
    } else if (align === 'center') {
      textEl.anchor.x = 0.5
    }

    return textEl
  };

  initTooltip () {
    var tooltip = new PIXI.Container()
    var background = tooltip.background = new PIXI.Graphics()
    var label = tooltip.label = this.generateText('', 36, 0xFFFFFF, 'left')

    background.beginFill(0x0, 0.7)
    background.drawRect(0, 0, 200, 185)
    background.endFill()
    background.x = -10
    background.y = -10

    tooltip.visible = false
    tooltip.inside = {}

    tooltip.addChild(background)
    tooltip.addChild(label)

    tooltip.interactiveChildren = false
    return tooltip
  };

  animateScene (delta) {

  }

  handleGlobalData (players, globalData) {

  }
}
