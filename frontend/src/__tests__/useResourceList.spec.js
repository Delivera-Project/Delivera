import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key) => key }),
}))

// onMounted no se dispara fuera de un componente; lo ignoramos.
vi.mock('vue', async (importOriginal) => {
  const actual = await importOriginal()
  return { ...actual, onMounted: vi.fn() }
})

const mockGet = vi.fn()
vi.mock('@/composables/useApi', () => ({
  useApi: () => ({ get: mockGet }),
}))

const { useResourceList } = await import('@/composables/useResourceList')

beforeEach(() => mockGet.mockReset())

describe('useResourceList', () => {
  it('load: respuesta ok popula items y loading vuelve a false', async () => {
    const data = [{ id: '1', name: 'A' }]
    mockGet.mockResolvedValue({ ok: true, json: async () => data })
    const { items, loading, error, load } = useResourceList('/units')
    await load()
    expect(items.value).toEqual(data)
    expect(loading.value).toBe(false)
    expect(error.value).toBe('')
  })

  it('load: respuesta !ok establece error.connection', async () => {
    mockGet.mockResolvedValue({ ok: false })
    const { items, error, load } = useResourceList('/units')
    await load()
    expect(items.value).toEqual([])
    expect(error.value).toBe('error.connection')
  })

})
