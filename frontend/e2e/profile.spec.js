import { test, expect } from '@playwright/test'
import { setupAuth, mockApiDefaults } from './helpers/auth.js'

const mockProfile = {
  id: 'some-uuid',
  email: 'admin@empresa.com',
  username: 'adminacme',
  firstName: 'Admin',
  lastName: 'User',
  phone: '+34 600 000 000',
  createdAt: '2024-01-01T00:00:00Z',
}

test.beforeEach(async ({ page }) => {
  await setupAuth(page)
  await mockApiDefaults(page)
  await page.route('**/api/v1/user/profile', route =>
    route.fulfill({ json: mockProfile })
  )
})

test('profile page loads user data', { tag: '@profile' }, async ({ page }) => {
  await page.goto('/profile')
  await expect(page.locator('.profile-name')).toHaveText('Admin User')
  await expect(page.locator('.profile-email')).toHaveText('admin@empresa.com')
  await expect(page.locator('.field-value').filter({ hasText: 'adminacme' })).toBeVisible()
})

test('edit button shows edit form', { tag: '@profile' }, async ({ page }) => {
  await page.goto('/profile')
  await page.getByRole('button', { name: 'Editar' }).click()
  await expect(page.locator('#profile-first-name')).toBeVisible()
  await expect(page.locator('#profile-last-name')).toBeVisible()
})

test('cancel editing restores read view', { tag: '@profile' }, async ({ page }) => {
  await page.goto('/profile')
  await page.getByRole('button', { name: 'Editar' }).click()
  await page.getByRole('button', { name: 'Cancelar' }).click()
  await expect(page.locator('.profile-name')).toHaveText('Admin User')
  await expect(page.locator('#profile-first-name')).not.toBeVisible()
})

test('change password button shows password form', { tag: '@profile' }, async ({ page }) => {
  await page.goto('/profile')
  await page.getByRole('button', { name: 'Cambiar contraseña' }).click()
  await expect(page.locator('#current-password input')).toBeVisible()
  await expect(page.locator('#new-password input')).toBeVisible()
})

test('logout redirects to /', { tag: '@profile' }, async ({ page }) => {
  await page.goto('/profile')
  await page.getByRole('button', { name: 'Cerrar sesión' }).click()
  await expect(page).toHaveURL('/')
})

test('save profile updates display name', { tag: '@profile' }, async ({ page }) => {
  await page.route('**/api/v1/user/profile', async route => {
    if (route.request().method() === 'PUT') {
      await route.fulfill({ json: { ...mockProfile, firstName: 'Manuel', lastName: 'Niza' } })
    } else {
      await route.fulfill({ json: mockProfile })
    }
  })
  await page.goto('/profile')
  await page.getByRole('button', { name: 'Editar' }).click()
  await page.locator('#profile-first-name').fill('Manuel')
  await page.locator('#profile-last-name').fill('Niza')
  await page.getByRole('button', { name: 'Guardar' }).click()
  await expect(page.locator('.profile-name')).toHaveText('Manuel Niza')
})
