import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import CompanyRegisterView from '@/views/CompanyRegisterView.vue'
import ProfileView from '@/views/ProfileView.vue'
import UnitsView from '@/views/UnitsView.vue'
import UnitFormView from '@/views/UnitFormView.vue'
import OrderFormView from '@/views/OrderFormView.vue'
import OrdersView from '@/views/OrdersView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import AppLayout from '@/components/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: LoginView, meta: { guest: true } },
    { path: '/register', component: RegisterView, meta: { guest: true } },
    { path: '/register/company', component: CompanyRegisterView, meta: { guest: true } },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: 'profile', component: ProfileView, meta: { requiresAuth: true } },
        { path: 'units', component: UnitsView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'] } },
        { path: 'units/new', component: UnitFormView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN'] } },
        { path: 'units/:id/edit', component: UnitFormView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN'] } },
        { path: 'orders', component: OrdersView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST'] } },
        { path: 'orders/new', component: OrderFormView, meta: { requiresAuth: true, roles: ['COMPANY_ADMIN', 'ANALYST'] } },
      ],
    },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  // Si la ruta necesita autenticación y el usuario no tiene token -> Pantalla de inicio
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return '/'
  }

  // Si la ruta es solo para invitados y el usuario tiene token -> redirigir según rol
  if (to.meta.guest && auth.isAuthenticated) {
    const companyRoles = ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR']
    return companyRoles.includes(auth.role) ? '/units' : '/profile'
  }

  // Si la ruta restringe por rol: primero verificar autenticación, luego el rol
  if (to.meta.roles) {
    if (!auth.isAuthenticated) return '/'
    if (!to.meta.roles.includes(auth.role)) return '/profile'
  }
})

export default router
