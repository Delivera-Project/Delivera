import { describe, it, expect, vi } from 'vitest'

const addToMock = vi.fn()
const bindPopupMock = vi.fn()
const circleMarkerMock = vi.fn(() => ({ addTo: addToMock, bindPopup: bindPopupMock }))
addToMock.mockImplementation(() => ({ addTo: addToMock, bindPopup: bindPopupMock }))

vi.mock('leaflet', () => ({
  default: {
    Icon: { Default: { prototype: {}, mergeOptions: vi.fn() } },
    circleMarker: circleMarkerMock,
  },
}))
vi.mock('leaflet.markercluster', () => ({}))

const { routeColorFor, ROUTE_STATUS_COLORS, ROUTE_COLOR, currentLocationOf, addCurrentLocationMarker } =
  await import('@/composables/useDeliveraMap')

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
