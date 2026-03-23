import { describe, it, expect, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import MyOrderDetailView from '@/views/orders/MyOrderDetailView.vue'

// ── Mocks ────────────────────────────────────────────────────────────────────

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key) => key }),
}))

vi.mock('@/composables/useFormatDate', () => ({
  useFormatDate: () => ({ formatDateTime: (d) => String(d) }),
}))

vi.mock('@/composables/useAppConfig', () => ({
  useAppConfig: () => ({ load: vi.fn(), statusSeverity: {} }),
}))

const mockPush = vi.fn()
let mockRouteQuery = { q: 'DEL-20240101-0001' }
vi.mock('vue-router', () => ({
  useRoute: () => ({ query: mockRouteQuery }),
  useRouter: () => ({ push: mockPush }),
}))

// ── Helpers ───────────────────────────────────────────────────────────────────

const STUBS = {
  PButton: { template: '<button />' },
  PMessage: { template: '<div class="p-message"><slot /></div>' },
  PTag: { template: '<span />' },
}

function buildOrder(overrides = {}) {
  return {
    reference: 'DEL-20240101-0001',
    companyName: 'Acme',
    originName: 'Almacén Central',
    destinationName: null,
    recipientName: 'Juan García',
    recipientEmail: 'juan@test.com',
    status: 'PENDING',
    events: [],
    ...overrides,
  }
}

function mockFetch(response, ok = true) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok,
    json: async () => response,
  }))
}

function mountView() {
  return mount(MyOrderDetailView, { global: { stubs: STUBS } })
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('MyOrderDetailView', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    mockRouteQuery = { q: 'DEL-20240101-0001' }
    mockPush.mockClear()
  })

  it('renders order reference on successful fetch', async () => {
    mockFetch(buildOrder())
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.text()).toContain('DEL-20240101-0001')
  })

  it('renders company and origin info', async () => {
    mockFetch(buildOrder())
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.text()).toContain('Acme')
    expect(wrapper.text()).toContain('Almacén Central')
  })

  it('shows error message when fetch returns non-ok', async () => {
    mockFetch({}, false)
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.p-message').exists()).toBe(true)
  })

  it('shows error message on network exception', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('Network')))
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.p-message').exists()).toBe(true)
  })

  it('shows error when route has no query param', async () => {
    mockRouteQuery = {}
    vi.stubGlobal('fetch', vi.fn())
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.p-message').exists()).toBe(true)
  })

  it('renders timeline when order has events', async () => {
    mockFetch(buildOrder({
      events: [{ id: 'e1', status: 'PENDING', createdAt: '2024-01-01T00:00:00Z' }],
    }))
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.timeline').exists()).toBe(true)
    expect(wrapper.findAll('.timeline-item')).toHaveLength(1)
  })

  it('renders empty timeline message when no events', async () => {
    mockFetch(buildOrder({ events: [] }))
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.find('.timeline-empty').exists()).toBe(true)
  })

  it('back button pushes /my-orders on click', async () => {
    mockFetch(buildOrder())
    const wrapper = mountView()
    await flushPromises()
    await wrapper.find('button').trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/my-orders')
  })
})
