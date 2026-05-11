import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useGeolocation } from '@/composables/useGeolocation'

function mockGeolocation(succeed, coords, errorCode) {
  const geo = succeed
    ? { getCurrentPosition: vi.fn((ok) => ok({ coords: { latitude: coords.lat, longitude: coords.lon } })) }
    : { getCurrentPosition: vi.fn((_ok, err) => err({ code: errorCode, message: 'denied' })) }
  Object.defineProperty(globalThis.navigator, 'geolocation', { value: geo, configurable: true })
}

beforeEach(() => {
  Object.defineProperty(globalThis.navigator, 'geolocation', { value: undefined, configurable: true })
})

describe('useGeolocation', () => {
  it('resuelve con lat/lon redondeados cuando la API tiene éxito', async () => {
    mockGeolocation(true, { lat: 40.41678912, lon: -3.70379123 })
    const { locating, getPosition } = useGeolocation()
    const pos = await getPosition()
    expect(pos).toEqual({ lat: 40.416789, lon: -3.703791 })
    expect(locating.value).toBe(false)
  })

  it('rechaza con Error cuando la API falla', async () => {
    mockGeolocation(false, null, 1)
    const { getPosition } = useGeolocation()
    await expect(getPosition()).rejects.toThrow('denied')
  })

  it('rechaza con GEOLOCATION_UNAVAILABLE cuando navigator.geolocation no existe', async () => {
    const { getPosition } = useGeolocation()
    await expect(getPosition()).rejects.toThrow('GEOLOCATION_UNAVAILABLE')
  })
})
