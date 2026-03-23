import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { defineComponent, nextTick } from 'vue'
import { useOrderForm } from '@/composables/useOrderForm'

// ── Mocks ────────────────────────────────────────────────────────────────────

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key) => key }),
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

const mockValidate = vi.fn().mockReturnValue(true)
vi.mock('@/composables/useValidation', () => ({
  useValidation: () => ({
    validate: mockValidate,
    required: () => null,
    email: () => null,
    errors: {},
    invalids: {},
  }),
}))

const mockGet = vi.fn()
const mockPost = vi.fn()
vi.mock('@/composables/useApi', () => ({
  useApi: () => ({
    get: mockGet,
    post: mockPost,
    translateError: (_data, fallback) => fallback,
  }),
}))

// ── Helpers ───────────────────────────────────────────────────────────────────

function makeOkResponse(data = []) {
  return { ok: true, json: async () => data }
}

function defaultGetMock() {
  mockGet.mockResolvedValue(makeOkResponse([]))
}

let composable

function mountComposable() {
  const Wrapper = defineComponent({
    setup() {
      composable = useOrderForm()
      return () => null
    },
  })
  mount(Wrapper)
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('useOrderForm — computed: destinationOptions', () => {
  afterEach(() => vi.clearAllMocks())

  it('excludes the unit matching originId', async () => {
    mockGet.mockResolvedValue(makeOkResponse([{ id: 'a' }, { id: 'b' }, { id: 'c' }]))
    mountComposable()
    await flushPromises()
    composable.originId.value = 'b'
    await nextTick()
    expect(composable.destinationOptions.value.map((u) => u.id)).toEqual(['a', 'c'])
  })

  it('returns all units when originId is empty', async () => {
    mockGet.mockResolvedValue(makeOkResponse([{ id: 'a' }, { id: 'b' }]))
    mountComposable()
    await flushPromises()
    expect(composable.destinationOptions.value).toHaveLength(2)
  })
})

describe('useOrderForm — computed: b2bCompanies', () => {
  afterEach(() => vi.clearAllMocks())

  it('deduplicates external units by companyId', async () => {
    const external = [
      { id: '1', companyId: 'c1', companyName: 'CompA' },
      { id: '2', companyId: 'c1', companyName: 'CompA' },
      { id: '3', companyId: 'c2', companyName: 'CompB' },
    ]
    mockGet
      .mockResolvedValueOnce(makeOkResponse([]))
      .mockResolvedValueOnce(makeOkResponse(external))
      .mockResolvedValueOnce(makeOkResponse([]))
    mountComposable()
    await flushPromises()
    expect(composable.b2bCompanies.value).toHaveLength(2)
    expect(composable.b2bCompanies.value[0]).toEqual({ id: 'c1', name: 'CompA' })
  })
})

describe('useOrderForm — computed: b2bUnitOptions', () => {
  afterEach(() => vi.clearAllMocks())

  it('filters external units by b2bCompanyId', async () => {
    const external = [
      { id: '1', companyId: 'c1', companyName: 'A' },
      { id: '2', companyId: 'c2', companyName: 'B' },
    ]
    mockGet
      .mockResolvedValueOnce(makeOkResponse([]))
      .mockResolvedValueOnce(makeOkResponse(external))
      .mockResolvedValueOnce(makeOkResponse([]))
    mountComposable()
    await flushPromises()
    composable.b2bCompanyId.value = 'c1'
    await nextTick()
    expect(composable.b2bUnitOptions.value).toHaveLength(1)
    expect(composable.b2bUnitOptions.value[0].id).toBe('1')
  })

  it('clears b2bDestinationId when b2bCompanyId changes', async () => {
    defaultGetMock()
    mountComposable()
    await flushPromises()
    composable.b2bDestinationId.value = 'unit-x'
    composable.b2bCompanyId.value = 'c2'
    await nextTick()
    expect(composable.b2bDestinationId.value).toBe('')
  })
})

describe('useOrderForm — computed: loyalUserMatch', () => {
  afterEach(() => vi.clearAllMocks())

  it('returns match for B2C when email matches (case-insensitive)', async () => {
    mockGet
      .mockResolvedValueOnce(makeOkResponse([]))
      .mockResolvedValueOnce(makeOkResponse([]))
      .mockResolvedValueOnce(makeOkResponse([{ email: 'Juan@test.com', name: 'Juan' }]))
    mountComposable()
    await flushPromises()
    composable.orderType.value = 'B2C'
    composable.recipientEmail.value = 'juan@test.com'
    await nextTick()
    expect(composable.loyalUserMatch.value).not.toBeNull()
  })

  it('returns null when orderType is not B2C', async () => {
    defaultGetMock()
    mountComposable()
    await flushPromises()
    composable.orderType.value = 'INTERNAL'
    composable.recipientEmail.value = 'test@test.com'
    await nextTick()
    expect(composable.loyalUserMatch.value).toBeNull()
  })

  it('returns null when email is empty', async () => {
    defaultGetMock()
    mountComposable()
    await flushPromises()
    composable.orderType.value = 'B2C'
    composable.recipientEmail.value = ''
    await nextTick()
    expect(composable.loyalUserMatch.value).toBeNull()
  })
})

describe('useOrderForm — onMounted error handling', () => {
  afterEach(() => vi.clearAllMocks())

  it('sets loadError when units fetch returns non-ok', async () => {
    mockGet
      .mockResolvedValueOnce({ ok: false, json: async () => ({ code: 'SOME_ERROR' }) })
      .mockResolvedValueOnce(makeOkResponse([]))
      .mockResolvedValueOnce(makeOkResponse([]))
    mountComposable()
    await flushPromises()
    expect(composable.loadError.value).toBeTruthy()
  })

  it('sets loadError on network exception', async () => {
    mockGet.mockRejectedValue(new Error('Network error'))
    mountComposable()
    await flushPromises()
    expect(composable.loadError.value).toBe('error.connection')
  })
})

describe('useOrderForm — handleSubmit', () => {
  beforeEach(() => {
    defaultGetMock()
    mockValidate.mockReturnValue(true)
  })
  afterEach(() => {
    vi.clearAllMocks()
    mockPush.mockClear()
  })

  it('navigates to /orders with reference on success', async () => {
    mockPost.mockResolvedValue({ ok: true, json: async () => ({ reference: 'DEL-001' }) })
    mountComposable()
    await flushPromises()
    composable.originId.value = 'unit-1'
    composable.orderType.value = 'INTERNAL'
    composable.destinationId.value = 'unit-2'
    await composable.handleSubmit()
    await flushPromises()
    expect(mockPush).toHaveBeenCalledWith({ path: '/orders', query: { created: 'DEL-001' } })
  })

  it('sets error when API returns non-ok response', async () => {
    mockPost.mockResolvedValue({ ok: false, json: async () => ({ code: 'INVALID_ORDER_UNITS' }) })
    mountComposable()
    await flushPromises()
    composable.originId.value = 'unit-1'
    composable.orderType.value = 'B2C'
    composable.recipientEmail.value = 'test@test.com'
    await composable.handleSubmit()
    await flushPromises()
    expect(composable.error.value).toBeTruthy()
  })

  it('sets error on network exception', async () => {
    mockPost.mockRejectedValue(new Error('Network'))
    mountComposable()
    await flushPromises()
    composable.originId.value = 'unit-1'
    composable.orderType.value = 'B2B'
    composable.b2bCompanyId.value = 'c1'
    composable.b2bDestinationId.value = 'unit-ext-1'
    await composable.handleSubmit()
    await flushPromises()
    expect(composable.error.value).toBe('error.connection')
  })

  it('aborts early when validate returns false', async () => {
    mockValidate.mockReturnValue(false)
    mountComposable()
    await flushPromises()
    await composable.handleSubmit()
    expect(mockPost).not.toHaveBeenCalled()
  })

  it('does not submit when loading is already true', async () => {
    mountComposable()
    await flushPromises()
    composable.loading.value = true
    await composable.handleSubmit()
    expect(mockPost).not.toHaveBeenCalled()
  })
})
