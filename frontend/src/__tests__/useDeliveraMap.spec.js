import { describe, it, expect } from 'vitest'
import { routeColorFor, ROUTE_STATUS_COLORS, ROUTE_COLOR, currentLocationOf } from '@/composables/useDeliveraMap'

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
