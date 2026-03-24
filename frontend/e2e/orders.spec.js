import { test, expect } from '@playwright/test'
import { setupAuth, mockApiDefaults, mockAppConfig } from './helpers/auth.js'

const mockOrder = {
  id: 1,
  reference: 'DEL-20240101-0001',
  status: 'PENDING',
  priority: 'NORMAL',
  originName: 'Almacén Central',
  destinationName: 'Oficina Madrid',
  recipientName: null,
  recipientEmail: null,
  createdAt: '2024-01-01T10:00:00Z',
}

test.beforeEach(async ({ page }) => {
  await setupAuth(page)
  await mockApiDefaults(page)
  await page.route('**/api/v2/app-config', route =>
    route.fulfill({ json: mockAppConfig })
  )
})

// ── List ──────────────────────────────────────────────────────────────────────

test('orders page loads title', { tag: '@orders' }, async ({ page }) => {
  await page.goto('/orders')
  await expect(page.getByRole('heading', { name: 'Pedidos' })).toBeVisible()
})

test('orders list shows empty state when no orders', { tag: '@orders' }, async ({ page }) => {
  await page.goto('/orders')
  await expect(page.getByText('No hay pedidos todavía')).toBeVisible()
})

test('orders list shows rows when data is returned', { tag: '@orders' }, async ({ page }) => {
  await page.route('**/api/v2/orders', route =>
    route.fulfill({ json: [mockOrder] })
  )
  await page.goto('/orders')
  await expect(page.getByText('DEL-20240101-0001')).toBeVisible()
  await expect(page.getByText('Almacén Central')).toBeVisible()
})

test('nuevo pedido button navigates to /orders/new', { tag: '@orders' }, async ({ page }) => {
  await page.goto('/orders')
  await page.getByRole('button', { name: 'Nuevo pedido' }).first().click()
  await expect(page).toHaveURL('/orders/new')
})

test('clicking an order row navigates to detail', { tag: '@orders' }, async ({ page }) => {
  await page.route('**/api/v2/orders', route =>
    route.fulfill({ json: [mockOrder] })
  )
  await page.goto('/orders')
  await page.getByText('DEL-20240101-0001').click()
  await expect(page).toHaveURL('/orders/1')
})

// ── Filters ───────────────────────────────────────────────────────────────────

test('filter by reference hides non-matching orders', { tag: '@orders' }, async ({ page }) => {
  await page.route('**/api/v2/orders', route =>
    route.fulfill({ json: [
      { ...mockOrder, id: 1, reference: 'DEL-20240101-0001' },
      { ...mockOrder, id: 2, reference: 'DEL-20240102-0002', originName: 'Tienda Norte', destinationName: 'Almacén Sur' },
    ] })
  )
  await page.goto('/orders')
  await page.locator('.filter-search').fill('0001')
  await expect(page.getByText('DEL-20240101-0001')).toBeVisible()
  await expect(page.getByText('DEL-20240102-0002')).not.toBeVisible()
})
