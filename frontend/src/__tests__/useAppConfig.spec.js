import { describe, it, expect, vi, beforeEach } from 'vitest'

const freshModule = async () => {
  vi.resetModules()
  return import('@/composables/useAppConfig')
}

const mockConfig = {
  orderStatuses: [
    { status: 'PENDING', uiSeverity: 'warn', allowedTransitions: ['IN_TRANSIT'] },
  ],
  orderPriorities: [{ priority: 'HIGH', uiSeverity: 'danger' }],
  roleCapabilities: [{ role: 'OPERATOR', canEdit: false }],
  fileMaxUploadBytes: 5_000_000,
}

beforeEach(() => vi.unstubAllGlobals())

describe('useAppConfig — estado inicial', () => {
  it('getNextStatuses y getRoleCapabilities devuelven defaults antes de cargar', async () => {
    const { useAppConfig } = await freshModule()
    const { getNextStatuses, getRoleCapabilities } = useAppConfig()
    expect(getNextStatuses('PENDING')).toEqual([])
    expect(getRoleCapabilities('OPERATOR')).toEqual({})
  })
})

describe('useAppConfig — loadConfig', () => {
  it('popula statuses, priorities y roles tras fetch ok', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockConfig,
    }))
    const { useAppConfig } = await freshModule()
    const { load, statusSeverity, prioritySeverity, getNextStatuses, getRoleCapabilities, fileMaxUploadBytes } = useAppConfig()
    await load()
    expect(statusSeverity.value).toEqual({ PENDING: 'warn' })
    expect(prioritySeverity.value).toEqual({ HIGH: 'danger' })
    expect(getNextStatuses('PENDING')).toEqual(['IN_TRANSIT'])
    expect(getRoleCapabilities('OPERATOR')).toEqual({ role: 'OPERATOR', canEdit: false })
    expect(fileMaxUploadBytes.value).toBe(5_000_000)
  })

  it('segunda llamada a load no vuelve a hacer fetch (loaded=true)', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => mockConfig })
    vi.stubGlobal('fetch', fetchMock)
    const { useAppConfig } = await freshModule()
    const { load } = useAppConfig()
    await load()
    await load()
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('fetch !ok no lanza y deja los datos vacíos', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false }))
    const { useAppConfig } = await freshModule()
    const { load, statusSeverity } = useAppConfig()
    await expect(load()).resolves.toBeUndefined()
    expect(statusSeverity.value).toEqual({})
  })

  it('excepción de red no propaga y loading vuelve a false', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('net')))
    const { useAppConfig } = await freshModule()
    const { load } = useAppConfig()
    await expect(load()).resolves.toBeUndefined()
  })
})
