import { test, expect } from '@playwright/test'
import { setupAuth, mockApiDefaults } from './helpers/auth.js'

const unitsWithCoords = [
  { id: '1', name: 'Almacén Central', type: 'WAREHOUSE', address: 'Calle Mayor 1', latitude: '40.4168', longitude: '-3.7038', createdAt: '2024-01-01T00:00:00Z' },
  { id: '2', name: 'Tienda Norte', type: 'STORE', address: 'Paseo del Prado 5', latitude: '40.4200', longitude: '-3.6900', createdAt: '2024-01-02T00:00:00Z' },
]

const unitsNoCoords = [
  { id: '3', name: 'Almacén Sin Coords', type: 'WAREHOUSE', address: 'Sin dirección', latitude: null, longitude: null, createdAt: '2024-01-03T00:00:00Z' },
]

test.beforeEach(async ({ page }) => {
  await mockApiDefaults(page)
})

// ── Rendering ─────────────────────────────────────────────────────────────────

test('map renders when units have coordinates', { tag: '@units' }, async ({ page }) => {
  await setupAuth(page)
  await page.route('**/api/v2/units', route => route.fulfill({ json: unitsWithCoords }))
  await page.goto('/units/map')
  await expect(page.locator('[data-testid="units-map"]')).toBeVisible()
  await expect(page.locator('.leaflet-container')).toBeVisible()
})

test('map renders with no-coords message when no unit has coordinates', { tag: '@units' }, async ({ page }) => {
  await setupAuth(page)
  await page.route('**/api/v2/units', route => route.fulfill({ json: unitsNoCoords }))
  await page.goto('/units/map')
  await expect(page.locator('.leaflet-container')).toBeVisible()
  await expect(page.getByText(/sin coordenadas|no coordinates/i)).toBeVisible()
})

test('map renders with no-coords message when units list is empty', { tag: '@units' }, async ({ page }) => {
  await setupAuth(page)
  await page.route('**/api/v2/units', route => route.fulfill({ json: [] }))
  await page.goto('/units/map')
  await expect(page.locator('.leaflet-container')).toBeVisible()
  await expect(page.getByText(/sin coordenadas|no coordinates/i)).toBeVisible()
})

// ── Access control ────────────────────────────────────────────────────────────

test('unauthenticated /units/map redirects to /', { tag: '@units' }, async ({ page }) => {
  await page.goto('/units/map')
  await expect(page).toHaveURL('/')
})

test('OPERATOR can access /units/map', { tag: '@units' }, async ({ page }) => {
  await setupAuth(page, { role: 'OPERATOR' })
  await page.route('**/api/v2/units', route => route.fulfill({ json: [] }))
  await page.goto('/units/map')
  await expect(page).toHaveURL('/units/map')
  await expect(page.locator('.leaflet-container')).toBeVisible()
})
