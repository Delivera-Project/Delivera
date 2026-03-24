/**
 * Sets up authenticated localStorage state before Vue initializes.
 * Must be called before page.goto().
 */
export async function setupAuth(page, overrides = {}) {
  const defaults = {
    token: 'mock-jwt-token',
    role: 'COMPANY_ADMIN',
    companyId: '1',
    companyName: 'Acme Logística',
    orgHandle: 'grupo-acme',
    orgName: 'Grupo Acme',
  }
  await page.addInitScript((data) => {
    Object.entries(data).forEach(([k, v]) => localStorage.setItem(k, v))
  }, { ...defaults, ...overrides })
}

/**
 * Catches all /api/v2/ requests and returns [] by default.
 * Individual tests can add more specific routes on top (LIFO — last wins).
 */
export async function mockApiDefaults(page) {
  await page.route('**/api/v2/**', route => route.fulfill({ json: [] }))
}

export const mockAppConfig = { orderStatuses: [], orderPriorities: [], roleCapabilities: [] }
