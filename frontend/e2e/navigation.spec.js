import { test, expect } from '@playwright/test'
import { setupAuth, mockApiDefaults } from './helpers/auth.js'

test.beforeEach(async ({ page }) => {
  await mockApiDefaults(page)
})

// ── Auth guards ───────────────────────────────────────────────────────────────

test('unauthenticated /units redirects to /', { tag: '@navigation' }, async ({ page }) => {
  await page.goto('/units')
  await expect(page).toHaveURL('/')
})

test('unauthenticated /profile redirects to /', { tag: '@navigation' }, async ({ page }) => {
  await page.goto('/profile')
  await expect(page).toHaveURL('/')
})

test('unauthenticated /orders redirects to /', { tag: '@navigation' }, async ({ page }) => {
  await page.goto('/orders')
  await expect(page).toHaveURL('/')
})

test('unauthenticated /settings redirects to /', { tag: '@navigation' }, async ({ page }) => {
  await page.goto('/settings')
  await expect(page).toHaveURL('/')
})

test('authenticated worker accessing / redirects to /units', { tag: '@navigation' }, async ({ page }) => {
  await setupAuth(page)
  await page.goto('/')
  await expect(page).toHaveURL('/units')
})

test('authenticated non-worker accessing / redirects to /my-orders', { tag: '@navigation' }, async ({ page }) => {
  await setupAuth(page, { role: 'USER' })
  await page.goto('/')
  await expect(page).toHaveURL('/my-orders')
})

test('worker without COMPANY_ADMIN role cannot access /settings', { tag: '@navigation' }, async ({ page }) => {
  await setupAuth(page, { role: 'OPERATOR' })
  await page.goto('/settings')
  await expect(page).toHaveURL('/profile')
})

// ── 404 ───────────────────────────────────────────────────────────────────────

test('unknown route shows 404 page', { tag: '@navigation' }, async ({ page }) => {
  await page.goto('/esta-pagina-no-existe')
  await expect(page.getByText('La página que buscas no existe')).toBeVisible()
})
