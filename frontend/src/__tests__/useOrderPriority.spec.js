import { describe, it, expect } from 'vitest'
import { ORDER_PRIORITIES, buildPriorityOptions, resolveOrderPriority } from '@/composables/useOrderPriority'

describe('buildPriorityOptions', () => {
  it('genera 3 opciones HIGH/NORMAL/LOW con label traducido', () => {
    const t = key => `T(${key})`
    expect(buildPriorityOptions(t)).toEqual([
      { value: 'HIGH', label: 'T(orders.priority.HIGH)' },
      { value: 'NORMAL', label: 'T(orders.priority.NORMAL)' },
      { value: 'LOW', label: 'T(orders.priority.LOW)' },
    ])
  })
})

describe('resolveOrderPriority', () => {
  it('prefiere la solicitada sobre cualquier default', () => {
    expect(resolveOrderPriority('HIGH', 'LOW')).toBe('HIGH')
  })
  it('cae en el default de empresa cuando no hay solicitada', () => {
    expect(resolveOrderPriority(null, 'LOW')).toBe('LOW')
    expect(resolveOrderPriority(undefined, 'HIGH')).toBe('HIGH')
  })
  it('NORMAL como fallback final', () => {
    expect(resolveOrderPriority(null, null)).toBe('NORMAL')
    expect(resolveOrderPriority('', '')).toBe('NORMAL')
  })
})

describe('ORDER_PRIORITIES', () => {
  it('expone las 3 prioridades válidas', () => {
    expect(ORDER_PRIORITIES).toEqual(['HIGH', 'NORMAL', 'LOW'])
  })
})
