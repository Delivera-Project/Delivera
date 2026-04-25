import { describe, it, expect } from 'vitest'
import { routeColorFor, ROUTE_STATUS_COLORS, ROUTE_COLOR } from '@/composables/useDeliveraMap'

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
