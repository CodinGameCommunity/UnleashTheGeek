import { ViewModule, api } from './graphics/ViewModule.js'
import { TooltipModule } from './tooltip/TooltipModule.js'
import { EndScreenModule } from './endscreen/EndScreenModule.js'

// List of viewer modules that you want to use in your game
export const modules = [
  ViewModule,
  TooltipModule,
  EndScreenModule
]

export const playerColors = [
  '#22a1e4', // curious blue
  '#ff1d5c' // radical red
]

export const gameName = 'UTG2019'

export const options = [{
  title: 'DESTINATIONS',
  get: function () {
    return api.options.destinations
  },
  set: function (value) {
    api.options.destinations = value
  },
  values: {
    'ON HOVER': false,
    'ALWAYS': true
  }
}, {
  title: 'RADAR RANGE',
  get: () => api.options.radarRange,
  set: (value) => {
    api.options.radarRange = value
    api.updateAllRadarOverlay()
  },
  values: {
    'OFF': -1,
    'BLUE': 0,
    'RED': 1
  }
}, {
  title: 'MY MESSAGES',
  get: function () {
    return api.options.showMyMessages
  },
  set: function (value) {
    api.options.showMyMessages = value
  },
  enabled: function () {
    return api.options.meInGame
  },
  values: {
    'ON': true,
    'OFF': false
  }
}, {
  title: 'OTHERS\' MESSAGES',
  get: function () {
    return api.options.showOthersMessages
  },
  set: function (value) {
    api.options.showOthersMessages = value
  },

  values: {
    'ON': true,
    'OFF': false
  }
}]
