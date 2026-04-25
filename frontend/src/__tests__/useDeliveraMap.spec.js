import { describe, it, expect, vi } from 'vitest'

const addToMock = vi.fn()
const bindPopupMock = vi.fn()
const closePopupMock = vi.fn()
const onMock = vi.fn()
const bringToFrontMock = vi.fn()
const circleMarkerMock = vi.fn(() => ({ addTo: addToMock, bindPopup: bindPopupMock }))
const polylineMock = vi.fn(() => ({
  addTo: addToMock, bindPopup: bindPopupMock, on: onMock, closePopup: closePopupMock, bringToFront: bringToFrontMock,
}))
addToMock.mockImplementation(() => ({
  addTo: addToMock, bindPopup: bindPopupMock, on: onMock, closePopup: closePopupMock, bringToFront: bringToFrontMock,
}))

vi.mock('leaflet', () => ({
  default: {
    Icon: { Default: { prototype: {}, mergeOptions: vi.fn() } },
    circleMarker: circleMarkerMock,
    polyline: polylineMock,
    DomEvent: { stopPropagation: vi.fn() },
  },
}))
vi.mock('leaflet.markercluster', () => ({}))

const { routeColorFor, ROUTE_STATUS_COLORS, ROUTE_COLOR, currentLocationOf, addCurrentLocationMarker, addRoute,
  isActiveOrder, hasOriginCoords } = await import('@/composables/useDeliveraMap')

describe('routeColorFor', () => {
  it('mapea cada estado conocido a su color', () => {
    expect(routeColorFor('PENDING')).toBe(ROUTE_STATUS_COLORS.PENDING)
    expect(routeColorFor('IN_TRANSIT')).toBe(ROUTE_STATUS_COLORS.IN_TRANSIT)
    expect(routeColorFor('DELIVERED')).toBe(ROUTE_STATUS_COLORS.DELIVERED)
    expect(routeColorFor('CANCELLED')).toBe(ROUTE_STATUS_COLORS.CANCELLED)
  })

  it('devuelve color por defecto cuando el estado es desconocido o nulo', () => {
    expect(routeColorFor(null)).toBe(ROUTE_COLOR)
    expect(routeColorFor(undefined)).toBe(ROUTE_COLOR)
    expect(routeColorFor('FOO')).toBe(ROUTE_COLOR)
  })
})

describe('currentLocationOf', () => {
  it('parsea las coordenadas a número cuando ambas existen', () => {
    expect(currentLocationOf({ currentLat: '40.4', currentLon: '-3.7' })).toEqual({ lat: 40.4, lon: -3.7 })
    expect(currentLocationOf({ currentLat: 1, currentLon: 2 })).toEqual({ lat: 1, lon: 2 })
  })

  it('devuelve null cuando faltan coordenadas o el pedido es nulo', () => {
    expect(currentLocationOf(null)).toBeNull()
    expect(currentLocationOf({})).toBeNull()
    expect(currentLocationOf({ currentLat: 1 })).toBeNull()
    expect(currentLocationOf({ currentLon: 1 })).toBeNull()
    expect(currentLocationOf({ currentLat: null, currentLon: 1 })).toBeNull()
  })
})

describe('addCurrentLocationMarker', () => {
  it('devuelve null si falta map, location, lat o lon', () => {
    expect(addCurrentLocationMarker(null, { lat: 1, lon: 2 }, '#000')).toBeNull()
    expect(addCurrentLocationMarker({}, null, '#000')).toBeNull()
    expect(addCurrentLocationMarker({}, { lat: null, lon: 2 }, '#000')).toBeNull()
    expect(addCurrentLocationMarker({}, { lat: 1, lon: null }, '#000')).toBeNull()
  })

  it('crea circleMarker con el color y vincula popup cuando hay título', () => {
    circleMarkerMock.mockClear()
    bindPopupMock.mockClear()
    const marker = addCurrentLocationMarker({}, { lat: 40, lon: -3 }, '#abc', 'ref-1', 'sub')
    expect(circleMarkerMock).toHaveBeenCalledWith([40, -3], expect.objectContaining({ fillColor: '#abc' }))
    expect(bindPopupMock).toHaveBeenCalled()
    expect(marker).not.toBeNull()
  })

  it('no vincula popup si no hay título', () => {
    bindPopupMock.mockClear()
    addCurrentLocationMarker({}, { lat: 1, lon: 2 }, '#000', null, null)
    expect(bindPopupMock).not.toHaveBeenCalled()
  })
})

describe('isActiveOrder', () => {
  it('true para PENDING e IN_TRANSIT', () => {
    expect(isActiveOrder({ status: 'PENDING' })).toBe(true)
    expect(isActiveOrder({ status: 'IN_TRANSIT' })).toBe(true)
  })
  it('false para otros estados, null o undefined', () => {
    expect(isActiveOrder({ status: 'DELIVERED' })).toBe(false)
    expect(isActiveOrder({ status: 'CANCELLED' })).toBe(false)
    expect(isActiveOrder(null)).toBe(false)
    expect(isActiveOrder(undefined)).toBe(false)
  })
})

describe('hasOriginCoords', () => {
  it('true cuando ambas coordenadas están presentes', () => {
    expect(hasOriginCoords({ originLat: 1, originLon: 2 })).toBe(true)
    expect(hasOriginCoords({ originLat: 0, originLon: 0 })).toBe(true)
  })
  it('false si alguna falta o el pedido es nulo', () => {
    expect(hasOriginCoords(null)).toBe(false)
    expect(hasOriginCoords({})).toBe(false)
    expect(hasOriginCoords({ originLat: 1 })).toBe(false)
    expect(hasOriginCoords({ originLat: null, originLon: 1 })).toBe(false)
  })
})

describe('addRoute', () => {
  it('crea polilínea discontinua con color del estado cuando OSRM falla', async () => {
    polylineMock.mockClear()
    globalThis.fetch = vi.fn().mockRejectedValue(new Error('osrm down'))
    const map = {}
    const entry = await addRoute(map, {
      orderId: 'o1',
      origin: { lat: 1, lon: 2 },
      dest: { lat: 3, lon: 4 },
      popupTitle: 'r-1', popupSubtitle: 'sub', actionLabel: null, router: null,
      status: 'IN_TRANSIT',
    })
    expect(polylineMock).toHaveBeenCalled()
    const optsArg = polylineMock.mock.calls[0][1]
    expect(optsArg.color).toBe(ROUTE_STATUS_COLORS.IN_TRANSIT)
    expect(entry.solid).toBe(false)
  })
})
