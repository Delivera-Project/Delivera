import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/auth/LoginView.vue'
import OrgSelectView from '@/views/auth/OrgSelectView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import CompanyRegisterView from '@/views/auth/CompanyRegisterView.vue'
import SettingsView from '@/views/profile/SettingsView.vue'
import ProfileView from '@/views/profile/ProfileView.vue'
import UnitsView from '@/views/units/UnitsView.vue'
import UnitFormView from '@/views/units/UnitFormView.vue'
import UnitDetailView from '@/views/units/UnitDetailView.vue'
import OrderFormView from '@/views/orders/OrderFormView.vue'
import OrdersView from '@/views/orders/OrdersView.vue'
import OrderDetailView from '@/views/orders/OrderDetailView.vue'
import LoyalUsersView from '@/views/loyal-users/LoyalUsersView.vue'
import LoyalUserDetailView from '@/views/loyal-users/LoyalUserDetailView.vue'
import MyOrdersView from '@/views/orders/MyOrdersView.vue'
import MyOrderDetailView from '@/views/orders/MyOrderDetailView.vue'
import TrackingView from '@/views/public/TrackingView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import ActivityView from '@/views/ActivityView.vue'
import WorkersView from '@/views/workers/WorkersView.vue'
import HomeView from '@/views/home/HomeView.vue'
import UnitAssignWorkersView from '@/views/units/UnitAssignWorkersView.vue'
import AppLayout from '@/components/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: LoginView, meta: { guest: true } },
    { path: '/login/org-select', component: OrgSelectView, meta: { guest: true } },
    { path: '/register', component: RegisterView, meta: { guest: true } },
    { path: '/register/company', component: CompanyRegisterView, meta: { guest: true } },
    { path: '/track', component: TrackingView },
    { path: '/track/:token', component: TrackingView },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: 'home', component: HomeView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] } },
        { path: 'profile', component: ProfileView, meta: { requiresAuth: true } },
        { path: 'my-orders', component: MyOrdersView, meta: { requiresAuth: true } },
        { path: 'my-orders/detail', component: MyOrderDetailView, meta: { requiresAuth: true } },
        { path: 'units', component: UnitsView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] } },
        { path: 'units/new', component: UnitFormView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN'] } },
        { path: 'units/:id', component: UnitDetailView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] } },
        { path: 'units/:id/edit', component: UnitFormView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN'] } },
        { path: 'units/:id/assign-workers', component: UnitAssignWorkersView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN'] } },
        { path: 'orders', component: OrdersView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] } },
        { path: 'orders/new', component: OrderFormView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST'] } },
        { path: 'orders/:id', component: OrderDetailView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] } },
        { path: 'loyal-users', component: LoyalUsersView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST'] } },
        { path: 'loyal-users/:id', component: LoyalUserDetailView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST'] } },
        { path: 'activity', component: ActivityView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST'] } },
        { path: 'workers', component: WorkersView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] } },
        { path: 'settings', component: SettingsView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN'] } },
      ],
    },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return '/'
  }

  if (to.meta.guest && auth.isAuthenticated && to.path !== '/login/org-select') {
    const companyRoles = ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR']
    return companyRoles.includes(auth.role) ? '/home' : '/my-orders'
  }

  if (to.meta.roles) {
    if (!auth.isAuthenticated) return '/'
    if (!to.meta.roles.includes(auth.role)) return '/profile'
  }
})

export default router
