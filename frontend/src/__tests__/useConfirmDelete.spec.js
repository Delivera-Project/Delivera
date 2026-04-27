import { describe, it, expect, vi } from 'vitest'
import { buildDeleteConfirmOptions } from '@/composables/useConfirmDelete'

describe('buildDeleteConfirmOptions', () => {
  const t = key => `T(${key})`

  it('rellena los campos estándar del ConfirmDialog', () => {
    const accept = vi.fn()
    const opts = buildDeleteConfirmOptions(t, 'Mensaje', accept)
    expect(opts).toEqual({
      message: 'Mensaje',
      header: 'T(common.delete)',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'T(common.delete)',
      rejectLabel: 'T(common.cancel)',
      acceptProps: { severity: 'danger' },
      accept,
    })
  })

  it('reenvía el handler de accept tal cual', async () => {
    const accept = vi.fn().mockResolvedValue('done')
    const opts = buildDeleteConfirmOptions(t, 'm', accept)
    await opts.accept()
    expect(accept).toHaveBeenCalledOnce()
  })
})
