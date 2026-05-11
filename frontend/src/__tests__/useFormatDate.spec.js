import { describe, it, expect, vi } from 'vitest'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ locale: { value: 'es' } }),
}))

const { useFormatDate } = await import('@/composables/useFormatDate')

describe('useFormatDate', () => {
  it('formatDate: null devuelve null; fecha válida devuelve string', () => {
    const { formatDate } = useFormatDate()
    expect(formatDate(null)).toBeNull()
    expect(formatDate(undefined)).toBeNull()
    const result = formatDate('2024-06-15')
    expect(typeof result).toBe('string')
    expect(result.length).toBeGreaterThan(0)
  })

  it('formatDateTime: null devuelve null; fecha válida devuelve string', () => {
    const { formatDateTime } = useFormatDate()
    expect(formatDateTime(null)).toBeNull()
    const result = formatDateTime('2024-06-15T10:30:00')
    expect(typeof result).toBe('string')
    expect(result.length).toBeGreaterThan(0)
  })
})
