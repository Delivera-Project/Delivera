import { test, expect } from '@playwright/test'
import { setupAuth, mockApiDefaults, mockAppConfig } from './helpers/auth.js'

const orderId = 'order-uuid-1'

const mockOrder = {
  id: orderId,
  reference: 'DEL-20260101-0001',
  status: 'IN_TRANSIT',
  priority: 'NORMAL',
  orderType: 'INTERNAL',
  originName: 'Almacén A',
  originCompanyName: 'Acme',
  destinationName: 'Tienda B',
  createdAt: '2026-01-01T10:00:00Z',
  events: [],
}

const mockMessages = [
  { id: 'msg-1', senderId: 'user-1', senderName: 'Ana López', content: 'Hola, ¿está en camino?', createdAt: '2026-01-01T11:00:00Z' },
  { id: 'msg-2', senderId: 'user-2', senderName: 'Carlos Pérez', content: 'Sí, sale hoy.', createdAt: '2026-01-01T11:05:00Z' },
]

test.beforeEach(async ({ page }) => {
  await mockApiDefaults(page)
  await page.route('**/api/v2/app-config/**', route => route.fulfill({ json: mockAppConfig }))
  await page.route(`**/api/v2/orders/${orderId}`, route => route.fulfill({ json: mockOrder }))
})

// ── Chat tab rendering ────────────────────────────────────────────────────────

test('chat tab shows messages', { tag: '@orders' }, async ({ page }) => {
  await setupAuth(page)
  await page.route(`**/api/v2/orders/${orderId}/messages`, route =>
    route.fulfill({ json: mockMessages })
  )
  await page.goto(`/orders/${orderId}`)
  await page.getByRole('tab', { name: /chat/i }).click()
  await expect(page.locator('[data-testid="chat-panel"]')).toBeVisible()
  await expect(page.getByText('Ana López')).toBeVisible()
  await expect(page.getByText('Hola, ¿está en camino?')).toBeVisible()
  await expect(page.getByText('Sí, sale hoy.')).toBeVisible()
})

test('chat tab shows empty state when no messages', { tag: '@orders' }, async ({ page }) => {
  await setupAuth(page)
  await page.route(`**/api/v2/orders/${orderId}/messages`, route =>
    route.fulfill({ json: [] })
  )
  await page.goto(`/orders/${orderId}`)
  await page.getByRole('tab', { name: /chat/i }).click()
  await expect(page.getByText(/no hay mensajes|no messages/i)).toBeVisible()
})

// ── Send message ──────────────────────────────────────────────────────────────

test('send message appends it to chat', { tag: '@orders' }, async ({ page }) => {
  await setupAuth(page)
  await page.route(`**/api/v2/orders/${orderId}/messages`, route => {
    if (route.request().method() === 'GET') {
      return route.fulfill({ json: [] })
    }
    return route.fulfill({
      status: 201,
      json: { id: 'msg-new', senderId: 'user-1', senderName: 'Ana López', content: 'Nuevo mensaje', createdAt: '2026-01-01T12:00:00Z' },
    })
  })
  await page.goto(`/orders/${orderId}`)
  await page.getByRole('tab', { name: /chat/i }).click()
  await page.getByPlaceholder(/mensaje|message/i).fill('Nuevo mensaje')
  await page.locator('[data-testid="chat-send"]').click()
  await expect(page.getByText('Nuevo mensaje')).toBeVisible()
})

test('send button is disabled when input is empty', { tag: '@orders' }, async ({ page }) => {
  await setupAuth(page)
  await page.route(`**/api/v2/orders/${orderId}/messages`, route => route.fulfill({ json: [] }))
  await page.goto(`/orders/${orderId}`)
  await page.getByRole('tab', { name: /chat/i }).click()
  const sendBtn = page.locator('[data-testid="chat-send"]')
  await expect(sendBtn).toBeDisabled()
})
