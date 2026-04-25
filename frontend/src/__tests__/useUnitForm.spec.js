import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key) => key }),
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

vi.mock('@/composables/useValidation', () => ({
  useValidation: () => ({
    validate: vi.fn().mockReturnValue(true),
    required: () => null,
    errors: {},
    invalids: {},
  }),
}))

const mockPost = vi.fn()
const mockPut = vi.fn()
vi.mock('@/composables/useApi', () => ({
  useApi: () => ({
    post: mockPost,
    put: mockPut,
    translateError: (_d, fallback) => fallback,
  }),
}))

const { useUnitForm } = await import('@/composables/useUnitForm')

beforeEach(() => {
  mockPost.mockReset()
  mockPut.mockReset()
  mockPush.mockReset()
})

describe('useUnitForm submitUnit', () => {
  it('envía defaultPriority cuando se indica', async () => {
    mockPost.mockResolvedValue({ ok: true })
    const f = useUnitForm()
    f.name.value = 'U1'
    f.unitType.value = 'WAREHOUSE'
    f.address.value = 'calle'
    f.defaultPriority.value = 'HIGH'

    await f.submitUnit({ isEdit: false })

    expect(mockPost).toHaveBeenCalledWith('/units', expect.objectContaining({
      name: 'U1', defaultPriority: 'HIGH',
    }))
  })

  it('envía defaultPriority null cuando se deja vacío', async () => {
    mockPut.mockResolvedValue({ ok: true })
    const f = useUnitForm()
    f.name.value = 'U2'
    f.unitType.value = 'STORE'
    f.address.value = ''
    f.defaultPriority.value = null

    await f.submitUnit({ isEdit: true, unitId: 'abc' })

    expect(mockPut).toHaveBeenCalledWith('/units/abc', expect.objectContaining({
      defaultPriority: null,
    }))
  })
})
