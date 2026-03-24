import { test, expect } from '@playwright/test'

const loginResponse = {
  token: 'mock-jwt-token',
  role: 'COMPANY_ADMIN',
  companyId: 1,
  companyName: 'Acme Logística',
  orgHandle: 'grupo-acme',
  orgName: 'Grupo Acme',
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v2/**', async route => {
    const url = route.request().url()
    if (url.includes('/auth/login')) {
      await route.fulfill({ json: loginResponse })
    } else {
      await route.fulfill({ json: [] })
    }
  })
})

test('shows login form', { tag: '@auth' }, async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('#login-identifier')).toBeVisible()
  await expect(page.locator('#login-password')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Iniciar sesión' })).toBeVisible()
})

test('valid credentials redirect to /units', { tag: '@auth' }, async ({ page }) => {
  await page.goto('/')
  await page.locator('#login-identifier').fill('admin@empresa.com')
  await page.locator('#login-password input').fill('Password1')
  await page.getByRole('button', { name: 'Iniciar sesión' }).click()
  await expect(page).toHaveURL('/units')
})

test('invalid credentials shows error message', { tag: '@auth' }, async ({ page }) => {
  await page.route('**/api/v2/auth/login', route =>
    route.fulfill({ status: 401, json: { code: 'INVALID_CREDENTIALS' } })
  )
  await page.goto('/')
  await page.locator('#login-identifier').fill('bad@user.com')
  await page.locator('#login-password input').fill('wrongpass')
  await page.getByRole('button', { name: 'Iniciar sesión' }).click()
  await expect(page.getByText('Credenciales incorrectas')).toBeVisible()
})

test('empty submit stays on login page', { tag: '@auth' }, async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Iniciar sesión' }).click()
  // Validation rejects empty form — no navigation
  await expect(page).toHaveURL('/')
  await expect(page.locator('#login-identifier')).toBeVisible()
})
