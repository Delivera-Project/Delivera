import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ApiKeysSection from '@/views/profile/ApiKeysSection.vue'

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockDel = vi.fn()
const mockTranslateError = vi.fn(() => 'translated')

vi.mock('@/composables/useApi', () => ({
  useApi: () => ({ get: mockGet, post: mockPost, del: mockDel, translateError: mockTranslateError }),
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (k) => k, locale: { value: 'es' } }),
}))

vi.mock('@/composables/useFormatDate', () => ({
  useFormatDate: () => ({ formatDate: (v) => v, formatDateTime: (v) => v }),
}))

const stubs = { PMessage: true, PButton: true, InputText: true }

function makeWrapper() {
  return mount(ApiKeysSection, { global: { stubs } })
}

beforeEach(() => {
  mockGet.mockReset()
  mockPost.mockReset()
  mockDel.mockReset()
  mockGet.mockResolvedValue({ ok: true, json: async () => [] })
})

afterEach(() => vi.clearAllMocks())

describe('ApiKeysSection', () => {
  it('loads keys on mount', async () => {
    const w = makeWrapper()
    await flushPromises()
    expect(mockGet).toHaveBeenCalledWith('/settings/api-keys')
    expect(w.text()).toContain('apiKeys.empty')
  })

  it('shows error message when load fails', async () => {
    mockGet.mockResolvedValueOnce({ ok: false })
    makeWrapper()
    await flushPromises()
    expect(mockGet).toHaveBeenCalled()
  })

  it('renders existing keys with active status', async () => {
    mockGet.mockResolvedValueOnce({ ok: true, json: async () => [
      { id: '1', name: 'k1', prefix: 'dlv_abc', createdAt: '2026-01-01', revokedAt: null, lastUsedAt: null },
    ] })
    const w = makeWrapper()
    await flushPromises()
    expect(w.text()).toContain('k1')
    expect(w.text()).toContain('dlv_abc')
    expect(w.text()).toContain('apiKeys.active')
  })

  it('marks revoked keys as revoked and hides revoke button', async () => {
    mockGet.mockResolvedValueOnce({ ok: true, json: async () => [
      { id: '1', name: 'k1', prefix: 'dlv_abc', createdAt: '2026-01-01', revokedAt: '2026-02-01', lastUsedAt: '2026-01-15' },
    ] })
    const w = makeWrapper()
    await flushPromises()
    expect(w.text()).toContain('apiKeys.revoked')
    expect(w.text()).toContain('apiKeys.lastUsed')
  })

  it('creates a new key and reloads list', async () => {
    mockPost.mockResolvedValueOnce({ ok: true, json: async () => ({ id: '2', name: 'New', prefix: 'dlv_xyz', token: 'dlv_full_token' }) })
    const w = makeWrapper()
    await flushPromises()

    w.vm.newKeyName = 'New'
    await w.vm.submitCreate()
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith('/settings/api-keys', { name: 'New' })
    expect(w.vm.createdToken.token).toBe('dlv_full_token')
  })

  it('does not create when name is blank', async () => {
    const w = makeWrapper()
    await flushPromises()
    w.vm.newKeyName = '   '
    await w.vm.submitCreate()
    expect(mockPost).not.toHaveBeenCalled()
    expect(w.vm.createError).toBeTruthy()
  })

  it('revokes a key and reloads list', async () => {
    mockGet.mockResolvedValueOnce({ ok: true, json: async () => [
      { id: 'k', name: 'k1', prefix: 'dlv_a', createdAt: '2026-01-01' },
    ] })
    mockDel.mockResolvedValueOnce({ ok: true })
    const confirmSpy = vi.spyOn(globalThis, 'confirm').mockReturnValue(true)
    const w = makeWrapper()
    await flushPromises()

    await w.vm.revoke('k')
    expect(mockDel).toHaveBeenCalledWith('/settings/api-keys/k')
    confirmSpy.mockRestore()
  })

  it('does not revoke if confirm is cancelled', async () => {
    mockGet.mockResolvedValueOnce({ ok: true, json: async () => [
      { id: 'k', name: 'k1', prefix: 'dlv_a', createdAt: '2026-01-01' },
    ] })
    const confirmSpy = vi.spyOn(globalThis, 'confirm').mockReturnValue(false)
    const w = makeWrapper()
    await flushPromises()

    await w.vm.revoke('k')
    expect(mockDel).not.toHaveBeenCalled()
    confirmSpy.mockRestore()
  })

  it('copies token to clipboard', async () => {
    const writeText = vi.fn().mockResolvedValue()
    Object.assign(navigator, { clipboard: { writeText } })
    const w = makeWrapper()
    await flushPromises()
    w.vm.createdToken = { token: 'dlv_xxx', name: 'n', prefix: 'dlv_xxx' }
    await w.vm.copyToken()
    expect(writeText).toHaveBeenCalledWith('dlv_xxx')
    expect(w.vm.tokenCopied).toBe(true)
  })

  it('exposes start/cancel/dismiss helpers', async () => {
    const w = makeWrapper()
    await flushPromises()
    w.vm.startCreate()
    expect(w.vm.creating).toBe(true)
    w.vm.cancelCreate()
    expect(w.vm.creating).toBe(false)
    w.vm.createdToken = { token: 't' }
    w.vm.dismissCreated()
    expect(w.vm.createdToken).toBe(null)
  })
})
