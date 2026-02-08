<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const email = ref('')
const password = ref('')
const error = ref('')

async function handleLogin() {
  error.value = ''
  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value }),
    })
    if (response.ok) {
      const data = await response.json()
      localStorage.setItem('token', data.token)
      router.push('/profile')
    } else {
      error.value = 'Correo o contraseña incorrectos'
    }
  } catch {
    error.value = 'Error de conexión con el servidor'
  }
}
</script>

<template>
  <div class="login-page">
    <form @submit.prevent="handleLogin">
      <h1>Delivera</h1>
      <input v-model="email" type="email" placeholder="Correo" required />
      <input v-model="password" type="password" placeholder="Contraseña" required />
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit">Iniciar sesión</button>
      <p class="link">¿No tienes cuenta? <router-link to="/register">Regístrate</router-link></p>
    </form>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f3f4f6;
}

form {
  background: white;
  padding: 40px 32px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 320px;
  text-align: center;
}

h1 {
  margin: 0 0 24px;
  color: #1e293b;
}

input {
  display: block;
  width: 100%;
  padding: 10px 12px;
  margin-bottom: 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

input:focus {
  outline: none;
  border-color: #2563eb;
}

button {
  width: 100%;
  padding: 10px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  cursor: pointer;
  margin-top: 4px;
}

button:hover {
  background: #1d4ed8;
}

.error {
  color: #dc2626;
  font-size: 14px;
  margin: 8px 0;
}

.link {
  margin-top: 16px;
  font-size: 14px;
  color: #64748b;
}

.link a {
  color: #2563eb;
  text-decoration: none;
}

.link a:hover {
  text-decoration: underline;
}
</style>
