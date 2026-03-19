import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import CompanyRegisterView from '@/views/CompanyRegisterView.vue'
import ProfileView from '@/views/ProfileView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: LoginView, meta: { guest: true } },
    { path: '/register', component: RegisterView, meta: { guest: true } },
    { path: '/register/company', component: CompanyRegisterView, meta: { guest: true } },
    { path: '/profile', component: ProfileView, meta: { requiresAuth: true } },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  // Si la ruta necesita autenticación y el usuario no tiene token -> Pantalla de inicio
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return '/'
  }

  // Si la ruta es solo para invitados y el usuario tiene token -> Perfil
  if (to.meta.guest && auth.isAuthenticated) {
    return '/profile'
  }
})

export default router
