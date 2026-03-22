import { test, expect } from '@playwright/test'
import { setupAuth, mockApiDefaults } from './helpers/auth.js'

const mockUnit = {
  id: 1,
  name: 'Almacén Central',
  type: 'WAREHOUSE',
  address: 'Calle Mayor 1, Madrid',
  latitude: 40.4168,
  longitude: -3.7038,
}

test.beforeEach(async ({ page }) => {
  await setupAuth(page)
  await mockApiDefaults(page)
})

// ── List ──────────────────────────────────────────────────────────────────────

test('units page loads title', { tag: '@units' }, async ({ page }) => {
  await page.goto('/units')
  await expect(page.getByRole('heading', { name: 'Unidades operativas' })).toBeVisible()
})

test('units list shows empty state when no units', { tag: '@units' }, async ({ page }) => {
  await page.goto('/units')
  await expect(page.getByText('No hay unidades operativas')).toBeVisible()
})

test('units list shows rows when data is returned', { tag: '@units' }, async ({ page }) => {
  await page.route('**/api/v1/units', route =>
    route.fulfill({ json: [mockUnit] })
  )
  await page.goto('/units')
  await expect(page.getByText('Almacén Central')).toBeVisible()
  await expect(page.getByText('Calle Mayor 1, Madrid')).toBeVisible()
})

test('nueva unidad button navigates to /units/new', { tag: '@units' }, async ({ page }) => {
  await page.goto('/units')
  await page.getByRole('button', { name: 'Nueva unidad' }).first().click()
  await expect(page).toHaveURL('/units/new')
})

test('clicking a unit row navigates to detail', { tag: '@units' }, async ({ page }) => {
  await page.route('**/api/v1/units', route =>
    route.fulfill({ json: [mockUnit] })
  )
  await page.goto('/units')
  await page.getByText('Almacén Central').click()
  await expect(page).toHaveURL('/units/1')
})

// ── Filters ───────────────────────────────────────────────────────────────────

test('filter by name hides non-matching units', { tag: '@units' }, async ({ page }) => {
  await page.route('**/api/v1/units', route =>
    route.fulfill({ json: [
      { id: 1, name: 'Almacén Central', type: 'WAREHOUSE', address: 'Madrid' },
      { id: 2, name: 'Tienda Norte', type: 'STORE', address: 'Barcelona' },
    ] })
  )
  await page.goto('/units')
  await page.locator('.filter-search').fill('Almacén')
  await expect(page.getByText('Almacén Central')).toBeVisible()
  await expect(page.getByText('Tienda Norte')).not.toBeVisible()
})

// ── Form ──────────────────────────────────────────────────────────────────────

test('unit form page loads', { tag: '@units' }, async ({ page }) => {
  await page.goto('/units/new')
  await expect(page.locator('#unit-name')).toBeVisible()
})
