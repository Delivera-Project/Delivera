<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useAppConfig } from '@/composables/useAppConfig'

const { t, locale } = useI18n()
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const api = useApi()
const { load: loadConfig } = useAppConfig()

const collapsed = ref(false)
const profileOpen = ref(false)
const profileRef = ref(null)
const companySwitcherOpen = ref(false)
const companySwitcherRef = ref(null)
const switching = ref(false)

const themeClass = computed(() =>
  ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'].includes(auth.role) ? 'theme-company' : 'theme-personal'
)

const navItems = [
  { path: '/units', icon: 'pi-building', labelKey: 'nav.units', roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] },
  { path: '/orders', icon: 'pi-send', labelKey: 'nav.orders', roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] },
  { path: '/loyal-users', icon: 'pi-users', labelKey: 'nav.loyalUsers', roles: ['COMPANY_ADMIN', 'ANALYST'] },
  { path: '/my-orders', icon: 'pi-inbox', labelKey: 'nav.myOrders', noRole: true },
  { path: '/settings', icon: 'pi-cog', labelKey: 'nav.settings', roles: ['COMPANY_ADMIN'] },
]

const visibleItems = computed(() =>
  navItems.filter(item => {
    if (item.noRole) return !auth.role
    return !item.roles || item.roles.includes(auth.role)
  })
)

const displayName = computed(() => {
  if (!auth.user) return '...'
  const { firstName, lastName } = auth.user
  return [firstName, lastName].filter(Boolean).join(' ') || auth.user.email || '...'
})

const initials = computed(() => {
  if (!auth.user) return '?'
  const f = auth.user.firstName?.[0] || ''
  const l = auth.user.lastName?.[0] || ''
  return (f + l).toUpperCase() || auth.user.email?.[0]?.toUpperCase() || '?'
})

const otherCompanies = computed(() =>
  auth.companies.filter(c => c.id !== auth.companyId)
)

function applyUserLocale(email) {
  const saved = localStorage.getItem(`locale_${email}`)
  if (saved) locale.value = saved
}

function handleLogout() {
  profileOpen.value = false
  locale.value = navigator.language?.startsWith('en') ? 'en' : 'es'
  auth.logout()
  router.push('/')
}

function onDocumentClick(e) {
  if (profileRef.value && !profileRef.value.contains(e.target)) {
    profileOpen.value = false
  }
  if (companySwitcherRef.value && !companySwitcherRef.value.contains(e.target)) {
    companySwitcherOpen.value = false
  }
}

function toggleProfile() {
  companySwitcherOpen.value = false
  profileOpen.value = !profileOpen.value
}

function toggleCompanySwitcher() {
  if (otherCompanies.value.length === 0) return
  profileOpen.value = false
  companySwitcherOpen.value = !companySwitcherOpen.value
}

async function switchCompany(companyId) {
  if (switching.value) return
  companySwitcherOpen.value = false
  switching.value = true
  try {
    const res = await api.post('/auth/switch-company', { companyId })
    if (res.ok) {
      const data = await res.json()
      auth.applyLoginData(data)
      router.push('/units')
    }
  } catch {
    // silencioso
  } finally {
    switching.value = false
  }
}

onMounted(async () => {
  document.addEventListener('click', onDocumentClick)
  loadConfig()

  if (!auth.user && auth.isAuthenticated) {
    try {
      const res = await api.get('/user/profile')
      if (res.ok) {
        const profile = await res.json()
        auth.setUser(profile)
        applyUserLocale(profile.email)
      }
    // eslint-disable-next-line no-empty
    } catch {}
  } else if (auth.user?.email) {
    applyUserLocale(auth.user.email)
  }

  if (auth.isWorker && auth.isCompanyAdmin) {
    auth.loadCompanies()
  }
})

onUnmounted(() => document.removeEventListener('click', onDocumentClick))
</script>

<template>
  <div class="app-layout" :class="themeClass">
    <nav class="sidebar" :class="{ 'sidebar--collapsed': collapsed }">

      <!-- Brand / Company switcher -->
      <div ref="companySwitcherRef" class="sidebar-brand-wrapper">
        <Transition name="menu-slide">
          <ul v-if="companySwitcherOpen && otherCompanies.length" class="company-dropdown">
            <li class="company-dropdown-label">{{ t('settings.myCompanies') }}</li>
            <li v-for="c in otherCompanies" :key="c.id">
              <button class="company-dropdown-item" type="button" @click="switchCompany(c.id)">
                <span class="company-dropdown-avatar">{{ c.name[0].toUpperCase() }}</span>
                <span>{{ c.name }}</span>
              </button>
            </li>
          </ul>
        </Transition>

        <!-- Modo empresa: muestra nombre de empresa -->
        <button
          v-if="auth.isWorker"
          class="sidebar-brand sidebar-brand--company"
          :class="{ 'sidebar-brand--clickable': otherCompanies.length > 0 }"
          type="button"
          @click="toggleCompanySwitcher"
        >
          <span class="sidebar-company-avatar">
            {{ auth.companyName?.[0]?.toUpperCase() ?? '?' }}
          </span>
          <div class="sidebar-brand-info">
            <span class="sidebar-brand-company">{{ auth.companyName ?? '...' }}</span>
            <span class="sidebar-brand-org">{{ auth.orgName ?? '' }}</span>
          </div>
          <i
            v-if="otherCompanies.length > 0"
            :class="['pi', companySwitcherOpen ? 'pi-angle-up' : 'pi-angle-down', 'sidebar-brand-chevron']"
          />
        </button>

        <!-- Modo personal: muestra "Delivera" -->
        <div v-else class="sidebar-brand">
          <i class="pi pi-truck sidebar-logo" />
          <span class="sidebar-brand-name">Delivera</span>
        </div>
      </div>

      <!-- Nav items -->
      <ul class="sidebar-nav">
        <li v-for="item in visibleItems" :key="item.path">
          <RouterLink
            :to="item.path"
            class="sidebar-item"
            :class="{ 'sidebar-item--active': route.path.startsWith(item.path) }"
            v-tooltip.right="collapsed ? t(item.labelKey) : null"
          >
            <i :class="['pi', item.icon, 'sidebar-item-icon']" />
            <span class="sidebar-item-label">{{ t(item.labelKey) }}</span>
          </RouterLink>
        </li>
      </ul>

      <!-- Toggle -->
      <button class="sidebar-toggle" type="button" @click="collapsed = !collapsed">
        <i :class="['pi', collapsed ? 'pi-angle-right' : 'pi-angle-left']" />
      </button>

      <!-- Profile -->
      <div ref="profileRef" class="sidebar-profile-wrapper">
        <Transition name="menu-slide">
          <ul v-if="profileOpen" class="profile-dropdown">
            <li>
              <RouterLink to="/profile" class="profile-dropdown-item" @click="profileOpen = false">
                <i class="pi pi-user" />
                <span>{{ t('nav.profile') }}</span>
              </RouterLink>
            </li>
            <li>
              <button class="profile-dropdown-item profile-dropdown-logout" type="button" @click="handleLogout">
                <i class="pi pi-sign-out" />
                <span>{{ t('auth.logout') }}</span>
              </button>
            </li>
          </ul>
        </Transition>

        <button class="sidebar-profile" type="button" @click="toggleProfile">
          <PAvatar :label="initials" shape="circle" class="sidebar-avatar" />
          <div class="sidebar-profile-info">
            <span class="sidebar-profile-name">{{ displayName }}</span>
            <span class="sidebar-plan-badge">FREE</span>
          </div>
          <i :class="['pi', profileOpen ? 'pi-angle-up' : 'pi-angle-down', 'sidebar-profile-chevron']" />
        </button>
      </div>
    </nav>

    <main class="app-main">
      <RouterView v-slot="{ Component }">
        <Transition name="fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  background: #f8fafc;
}

.sidebar {
  width: 220px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  flex-shrink: 0;
}

.sidebar--collapsed { width: 64px; }

/* Brand */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px 12px;
}

.sidebar--collapsed .sidebar-brand {
  justify-content: center;
  padding: 16px 0 12px;
  gap: 0;
}

.sidebar-logo {
  font-size: 20px;
  color: var(--p-primary-color, #7c3aed);
  flex-shrink: 0;
}

.sidebar-brand-name {
  font-size: 13px;
  font-weight: 700;
  color: #94a3b8;
  letter-spacing: 0.04em;
  white-space: nowrap;
  overflow: hidden;
  transition: opacity 0.2s, max-width 0.25s;
  max-width: 140px;
}

.sidebar--collapsed .sidebar-brand-name {
  opacity: 0;
  max-width: 0;
}

/* Brand wrapper */
.sidebar-brand-wrapper {
  position: relative;
  border-bottom: 1px solid #e2e8f0;
}

.sidebar-brand--company {
  border: none;
  cursor: default;
  width: 100%;
  text-align: left;
}

.sidebar-brand--clickable {
  cursor: pointer;
}

.sidebar-brand--clickable:hover {
  background: #f8fafc;
}

.sidebar-brand-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  transition: opacity 0.2s, max-width 0.25s;
  max-width: 130px;
}

.sidebar--collapsed .sidebar-brand-info {
  opacity: 0;
  max-width: 0;
  flex: 0;
}

.sidebar-brand-company {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-brand-org {
  display: block;
  font-size: 11px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-brand-chevron {
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
  transition: opacity 0.2s, max-width 0.25s;
  max-width: 16px;
}

.sidebar--collapsed .sidebar-brand-chevron {
  opacity: 0;
  max-width: 0;
}

.sidebar-company-avatar {
  width: 30px;
  height: 30px;
  min-width: 30px;
  border-radius: 7px;
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 14%, white);
  color: var(--p-primary-color, #7c3aed);
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* Company dropdown */
.company-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 8px;
  right: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(12, 20, 69, 0.12);
  list-style: none;
  padding: 4px;
  z-index: 200;
}

.sidebar--collapsed .company-dropdown {
  left: calc(100% + 8px);
  right: auto;
  top: 8px;
  min-width: 200px;
}

.company-dropdown-label {
  font-size: 10px;
  font-weight: 600;
  color: #94a3b8;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  padding: 6px 12px 4px;
}

.company-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 7px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  transition: background 0.12s;
}

.company-dropdown-item:hover {
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 8%, white);
  color: var(--p-primary-color, #7c3aed);
}

.company-dropdown-avatar {
  width: 24px;
  height: 24px;
  min-width: 24px;
  border-radius: 5px;
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 12%, white);
  color: var(--p-primary-color, #7c3aed);
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Nav */
.sidebar-nav {
  list-style: none;
  padding: 12px 8px;
  flex: 1;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  color: #64748b;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.15s, color 0.15s;
  margin-bottom: 2px;
  white-space: nowrap;
}

.sidebar--collapsed .sidebar-item {
  justify-content: center;
  padding: 10px 0;
  gap: 0;
}

.sidebar-item:hover { background: #f1f5f9; color: #1e293b; }

.sidebar-item.router-link-active,
.sidebar-item--active {
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 8%, white);
  color: var(--p-primary-color, #7c3aed);
}

.sidebar-item.router-link-active:hover,
.sidebar-item--active:hover {
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 12%, white);
  color: var(--p-primary-color, #7c3aed);
}

.sidebar-item-icon { font-size: 17px; flex-shrink: 0; width: 20px; text-align: center; }

.sidebar-item-label {
  overflow: hidden;
  white-space: nowrap;
  transition: opacity 0.2s, max-width 0.25s;
  max-width: 140px;
}

.sidebar--collapsed .sidebar-item-label { opacity: 0; max-width: 0; }

/* Toggle */
.sidebar-toggle {
  padding: 10px;
  border: none;
  background: none;
  cursor: pointer;
  color: #94a3b8;
  display: flex;
  justify-content: center;
  align-items: center;
  border-top: 1px solid #e2e8f0;
  transition: color 0.15s, background 0.15s;
}

.sidebar-toggle:hover { color: var(--p-primary-color, #7c3aed); background: #f8fafc; }

/* Profile */
.sidebar-profile-wrapper {
  position: relative;
  border-top: 1px solid #e2e8f0;
  padding-bottom: 12px;
}

.profile-dropdown {
  position: absolute;
  bottom: calc(100% + 6px);
  left: 8px;
  right: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(12, 20, 69, 0.12);
  list-style: none;
  padding: 4px;
  z-index: 200;
}

.sidebar--collapsed .profile-dropdown {
  left: calc(100% + 8px);
  right: auto;
  bottom: 12px;
  min-width: 180px;
}

.profile-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 7px;
  font-size: 13px;
  color: #374151;
  text-decoration: none;
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  transition: background 0.12s;
}

.profile-dropdown-item:hover { background: #f1f5f9; }
.profile-dropdown-logout { color: #ef4444; }
.profile-dropdown-logout:hover { background: #fef2f2; color: #dc2626; }

.sidebar-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  transition: background 0.15s;
}

.sidebar--collapsed .sidebar-profile { justify-content: center; padding: 12px 0; gap: 0; }
.sidebar-profile:hover { background: #f8fafc; }

.sidebar-avatar {
  flex-shrink: 0;
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 10%, white) !important;
  color: var(--p-primary-color, #7c3aed) !important;
  font-weight: 700 !important;
  font-size: 12px !important;
  width: 32px !important;
  height: 32px !important;
  min-width: 32px;
}

.sidebar-profile-info {
  flex: 1;
  min-width: 0;
  text-align: left;
  overflow: hidden;
  transition: opacity 0.2s, max-width 0.25s;
  max-width: 120px;
}

.sidebar--collapsed .sidebar-profile-info { opacity: 0; max-width: 0; flex: 0; }

.sidebar-profile-name {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-plan-badge {
  display: inline-block;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.06em;
  padding: 1px 6px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 12%, white);
  color: var(--p-primary-color, #7c3aed);
  border: 1px solid color-mix(in srgb, var(--p-primary-color, #7c3aed) 22%, white);
  white-space: nowrap;
  line-height: 1.6;
  margin-top: 2px;
}

.sidebar-profile-chevron {
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
  transition: opacity 0.2s, max-width 0.25s;
  max-width: 20px;
}

.sidebar--collapsed .sidebar-profile-chevron { opacity: 0; max-width: 0; }

/* Main */
.app-main {
  flex: 1;
  height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
  background: #f8fafc;
  padding: 32px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

/* Animations */
.menu-slide-enter-active, .menu-slide-leave-active { transition: opacity 0.15s, transform 0.15s; }
.menu-slide-enter-from, .menu-slide-leave-to { opacity: 0; transform: translateY(-6px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
