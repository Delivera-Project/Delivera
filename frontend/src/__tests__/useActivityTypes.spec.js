import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ locale: { value: 'es' } }),
}))

const mockGet = vi.fn()
vi.mock('@/composables/useApi', () => ({
  useApi: () => ({ get: mockGet }),
}))

// Resetear el módulo entre describe blocks para limpiar el cache singleton
const freshModule = async () => {
  vi.resetModules()
  vi.mock('vue-i18n', () => ({ useI18n: () => ({ locale: { value: 'es' } }) }))
  vi.mock('@/composables/useApi', () => ({ useApi: () => ({ get: mockGet }) }))
  return import('@/composables/useActivityTypes')
}

describe('useActivityTypes — load', () => {
  beforeEach(() => mockGet.mockReset())

  it('load popula el cache y activityTypes devuelve los items mapeados (locale es)', async () => {
    const { useActivityTypes } = await freshModule()
    mockGet.mockResolvedValue({
      ok: true,
      json: async () => [{ code: 'FOOD', labelEs: 'Alimentación', labelEn: 'Food' }],
    })
    const { activityTypes, load } = useActivityTypes()
    expect(activityTypes.value).toEqual([])
    await load()
    expect(activityTypes.value).toEqual([
      { value: 'FOOD', label: 'Alimentación', labelEs: 'Alimentación', labelEn: 'Food' },
    ])
  })

  it('una segunda llamada a load no vuelve a hacer fetch (cache hit)', async () => {
    const { useActivityTypes } = await freshModule()
    mockGet.mockResolvedValue({ ok: true, json: async () => [] })
    const { load } = useActivityTypes()
    await load()
    await load()
    expect(mockGet).toHaveBeenCalledTimes(1)
  })

  it('si la respuesta no es ok, el cache queda vacío y no lanza', async () => {
    const { useActivityTypes } = await freshModule()
    mockGet.mockResolvedValue({ ok: false })
    const { activityTypes, load } = useActivityTypes()
    await expect(load()).resolves.toBeUndefined()
    expect(activityTypes.value).toEqual([])
  })

})
