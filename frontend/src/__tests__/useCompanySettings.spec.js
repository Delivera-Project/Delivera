import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockGet = vi.fn()
vi.mock('@/composables/useApi', () => ({
  useApi: () => ({ get: mockGet }),
}))

const { useCompanySettings, canUnitOverridePriority } = await import('@/composables/useCompanySettings')

beforeEach(() => mockGet.mockReset())

describe('useCompanySettings.load', () => {
  it('guarda el body cuando la respuesta es ok', async () => {
    mockGet.mockResolvedValue({ ok: true, json: async () => ({ defaultPriorityLocked: true }) })
    const { settings, load } = useCompanySettings()
    const result = await load()
    expect(result).toEqual({ defaultPriorityLocked: true })
    expect(settings.value).toEqual({ defaultPriorityLocked: true })
  })

  it('mantiene settings nulo cuando la respuesta no es ok', async () => {
    mockGet.mockResolvedValue({ ok: false })
    const { settings, load } = useCompanySettings()
    const result = await load()
    expect(result).toBeNull()
    expect(settings.value).toBeNull()
  })

})

describe('canUnitOverridePriority', () => {
  it('true si no hay settings (fail-open)', () => {
    expect(canUnitOverridePriority(null)).toBe(true)
    expect(canUnitOverridePriority(undefined)).toBe(true)
  })
  it('true cuando la empresa no bloquea', () => {
    expect(canUnitOverridePriority({ defaultPriorityLocked: false })).toBe(true)
    expect(canUnitOverridePriority({})).toBe(true)
  })
  it('false cuando la empresa bloquea', () => {
    expect(canUnitOverridePriority({ defaultPriorityLocked: true })).toBe(false)
  })
})
