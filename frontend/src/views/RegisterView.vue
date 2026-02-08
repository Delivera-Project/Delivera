<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')

async function handleRegister() {
  error.value = ''

  if (password.value !== confirmPassword.value) {
    error.value = 'Las contraseñas no coinciden'
    return
  }

  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value }),
    })
    const data = await response.json()
    if (response.ok) {
      localStorage.setItem('token', data.token)
      router.push('/')
    } else {
      error.value = data.message || 'Error al registrarse'
    }
  } catch {
    error.value = 'Error de conexión con el servidor'
  }
}
</script>

<template>
  <div class="register-page">
    <form @submit.prevent="handleRegister">
      <h1>Delivera</h1>
      <p class="subtitle">Crear cuenta</p>
      <input v-model="email" type="email" placeholder="Correo" required />
      <input v-model="password" type="password" placeholder="Contraseña" minlength="8" required />
      <input
        v-model="confirmPassword"
        type="password"
        placeholder="Confirmar contraseña"
        minlength="8"
        required
      />
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit">Registrarse</button>
      <p class="link">¿Ya tienes cuenta? <router-link to="/">Inicia sesión</router-link></p>
    </form>
  </div>
</template>

<style scoped>
.register-page {
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
  margin: 0 0 4px;
  color: #1e293b;
}

.subtitle {
  color: #64748b;
  margin: 0 0 24px;
  font-size: 14px;
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
