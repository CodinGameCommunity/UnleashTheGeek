
function parseIntList (values) {
  return values.map(v => +v)
}
function parseIntMap (values) {
  let ints = {}
  for (let i = 0; i < values.length; i += 2) {
    let id = values[i]
    let int = values[i + 1]
    ints[id] = +int
  }
  return ints
}

function parseNullableIntList (values) {
  return values.map(v => v === '_' ? null : +v)
}

function parseFromTokens (tokens, callback) {
  return (values) => {
    const res = []
    for (let i = 0; i < values.length; i += tokens) {
      res.push(callback(values.slice(i, i + tokens)))
    }
    return res
  }
}

export function parseData (raw, globalData) {
  let idx = 0
  const lines = raw.split('\n').map(line => line === '' ? [] : line.split(' '))
  const [score0, score1, eventCount] = parseIntList(lines[idx++])
  const agents = []
  const events = []
  const scores = [score0, score1]
  for (let k = 0; k < globalData.agentsPerPlayer * globalData.playerCount; ++k) {
    const [id, x, y, item, dead] = parseIntList(lines[idx++])
    const message = lines[idx++].join(' ').trim()
    let tx = null
    let ty = null
    if (idx < lines.length && lines[idx].length === 2) {
      [tx, ty] = parseIntList(lines[idx++])
    }
    agents.push({ id, x, y, tx, ty, item, dead: dead === 1, message })
  }
  for (let k = 0; k < eventCount; ++k) {
    const [type, item, agent, x, y] = parseNullableIntList(lines[idx++])
    events.push({ type, item, agent, x, y })
  }

  return {
    agents, scores, events
  }
}

export function parseGlobalData (raw) {
  let idx = 0
  const lines = raw.split('\n').map(line => line === '' ? [] : line.split(' '))
  const [width, height, agentsPerPlayer, veinCount] = parseIntList(lines[idx++])
  const ore = []
  for (let k = 0; k < veinCount; k++) {
    const [x, y, amount] = parseIntList(lines[idx++])
    ore.push({ x, y, ore: amount })
  }
  return { width, height, agentsPerPlayer, ore }
}
