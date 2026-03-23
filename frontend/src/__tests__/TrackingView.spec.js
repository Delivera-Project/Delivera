import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import TrackingView from '@/views/public/TrackingView.vue'

// ── Mocks ────────────────────────────────────────────────────────────────────

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key, params) => (params ? `${key}:${JSON.stringify(params)}` : key),
  }),
}))

vi.mock('@/composables/useFormatDate', () => ({
  useFormatDate: () => ({ formatDateTime: (d) => String(d) }),
}))

vi.mock('@/composables/useAppConfig', () => ({
  useAppConfig: () => ({ load: vi.fn(), statusSeverity: {}, prioritySeverity: {} }),
}))

const mockPush = vi.fn()
let mockRouteParams = { token: 'testtoken123' }
let mockRouteQuery = {}
vi.mock('vue-router', () => ({
  useRoute: () => ({ params: mockRouteParams, query: mockRouteQuery }),
  useRouter: () => ({ push: mockPush }),
}))

const mockApplyLoginData = vi.fn()
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({ applyLoginData: mockApplyLoginData }),
}))

// ── Helpers ───────────────────────────────────────────────────────────────────

const STUBS = {
  PInputText: { template: '<input />' },
  PButton: { template: '<button type="submit"><slot /></button>', props: ['loading', 'label'] },
  PMessage: { template: '<div class="p-message"><slot /></div>' },
  PTag: { template: '<span />' },
  PPassword: { template: '<div><input type="password" /></div>' },
}

function buildOrder(overrides = {}) {
  return {
    reference: 'DEL-20240101-0001',
    companyName: 'Acme',
    originName: 'Almacén',
    destinationName: null,
    recipientName: 'Juan García',
    status: 'PENDING',
    priority: 'NORMAL',
    createdAt: '2024-01-01T00:00:00Z',
    events: [],
    claimable: false,
    recipientEmailHint: null,
    ...overrides,
  }
}

function mockFetch(response, status = 200) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    json: async () => response,
  }))
}

function mountView() {
  return mount(TrackingView, {
    global: { stubs: STUBS },
  })
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('TrackingView — claim panel visibility', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('does not show claim panel when claimable=false', async () => {
    mockFetch(buildOrder({ claimable: false }))
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.claim-panel').exists()).toBe(false)
  })

  it('shows claim panel when claimable=true and route has token', async () => {
    mockFetch(buildOrder({ claimable: true, recipientEmailHint: 'j***@gmail.com' }))
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.claim-panel').exists()).toBe(true)
  })

  it('shows email hint when recipientEmailHint is present', async () => {
    mockFetch(buildOrder({ claimable: true, recipientEmailHint: 'j***@gmail.com' }))
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.claim-hint').text()).toContain('j***@gmail.com')
  })

  it('does not show email hint when recipientEmailHint is null', async () => {
    mockFetch(buildOrder({ claimable: true, recipientEmailHint: null }))
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.claim-hint').exists()).toBe(false)
  })
})

describe('TrackingView — claim panel without token (search result)', () => {
  beforeEach(() => {
    mockRouteParams = {}
    mockRouteQuery = { q: 'DEL-001' }
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    mockRouteParams = { token: 'testtoken123' }
    mockRouteQuery = {}
  })

  it('does not show claim panel even when claimable=true on search result', async () => {
    mockFetch(buildOrder({ claimable: true }))
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.claim-panel').exists()).toBe(false)
  })
})

describe('TrackingView — submitClaim', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    mockPush.mockClear()
    mockApplyLoginData.mockClear()
  })

  async function mountWithClaimableOrder() {
    // First fetch returns the claimable order (onMounted)
    const fetchMock = vi.fn()
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => buildOrder({ claimable: true, recipientEmailHint: 'j***@gmail.com' }),
    })
    vi.stubGlobal('fetch', fetchMock)
    const wrapper = mountView()
    await flushPromises()
    return { wrapper, fetchMock }
  }

  it('success — calls applyLoginData and redirects to /my-orders', async () => {
    const { wrapper, fetchMock } = await mountWithClaimableOrder()

    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => ({ token: 'jwt-abc', email: 'juan@gmail.com' }),
    })

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockApplyLoginData).toHaveBeenCalledWith({ token: 'jwt-abc', email: 'juan@gmail.com' })
    expect(mockPush).toHaveBeenCalledWith('/my-orders')
  })

  it('ORDER_ALREADY_CLAIMED — shows error message', async () => {
    const { wrapper, fetchMock } = await mountWithClaimableOrder()

    fetchMock.mockResolvedValueOnce({
      ok: false,
      status: 409,
      json: async () => ({ code: 'ORDER_ALREADY_CLAIMED' }),
    })

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.p-message').text()).toContain('tracking.claim.alreadyClaimed')
    expect(mockPush).not.toHaveBeenCalled()
  })

  it('ORDER_CLAIM_EMAIL_MISMATCH — shows error message', async () => {
    const { wrapper, fetchMock } = await mountWithClaimableOrder()

    fetchMock.mockResolvedValueOnce({
      ok: false,
      status: 422,
      json: async () => ({ code: 'ORDER_CLAIM_EMAIL_MISMATCH' }),
    })

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.p-message').text()).toContain('tracking.claim.emailMismatch')
  })

  it('EMAIL_ALREADY_EXISTS — shows error message', async () => {
    const { wrapper, fetchMock } = await mountWithClaimableOrder()

    fetchMock.mockResolvedValueOnce({
      ok: false,
      status: 409,
      json: async () => ({ code: 'EMAIL_ALREADY_EXISTS' }),
    })

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.p-message').text()).toContain('tracking.claim.emailExists')
  })

  it('network error — shows generic connection error', async () => {
    const { wrapper, fetchMock } = await mountWithClaimableOrder()
    fetchMock.mockRejectedValueOnce(new Error('Network error'))

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.p-message').text()).toContain('error.connection')
  })
})
