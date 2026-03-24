import { test, expect } from '@playwright/test'
import { mockAppConfig } from './helpers/auth.js'

const mockOrder = {
  id: 1,
  reference: 'DEL-20240101-0001',
  status: 'IN_TRANSIT',
  companyName: 'Acme Logística',
  originName: 'Almacén Central',
  destinationName: 'Oficina Madrid',
  recipientName: null,
  events: [
    { id: 1, status: 'PENDING', createdAt: '2024-01-01T10:00:00Z', note: null },
    { id: 2, status: 'IN_TRANSIT', createdAt: '2024-01-01T12:00:00Z', note: 'En camino' },
  ],
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v2/app-config', route =>
    route.fulfill({ json: mockAppConfig })
  )
})

test('shows search form', { tag: '@tracking' }, async ({ page }) => {
  await page.goto('/track')
  await expect(page.getByText('Delivera')).toBeVisible()
  await expect(page.getByText('Seguimiento de pedido')).toBeVisible()
  await expect(page.locator('.search-row')).toBeVisible()
})

test('search by reference shows order info', { tag: '@tracking' }, async ({ page }) => {
  await page.route('**/api/v2/orders/public/search**', route =>
    route.fulfill({ json: mockOrder })
  )
  // Use ?q= param so onMounted triggers fetchByReference without needing the input
  await page.goto('/track?q=DEL-20240101-0001')
  await expect(page.getByText('DEL-20240101-0001')).toBeVisible()
  await expect(page.getByText('Acme Logística')).toBeVisible()
  await expect(page.getByText('Almacén Central')).toBeVisible()
})

test('direct token URL shows order info', { tag: '@tracking' }, async ({ page }) => {
  await page.route('**/api/v2/orders/public/track/**', route =>
    route.fulfill({ json: mockOrder })
  )
  await page.goto('/track/ABC123TOKEN')
  await expect(page.getByText('DEL-20240101-0001')).toBeVisible()
  await expect(page.getByText('En camino')).toBeVisible()
})

test('not found reference shows error', { tag: '@tracking' }, async ({ page }) => {
  await page.route('**/api/v2/orders/public/search**', route =>
    route.fulfill({ status: 404, json: {} })
  )
  await page.goto('/track?q=XXXX')
  await expect(page.getByText('No se encontró ningún pedido')).toBeVisible()
})
