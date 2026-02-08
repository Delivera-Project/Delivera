<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const profile = ref(null)
const editing = ref(false)
const error = ref('')
const success = ref('')

const form = ref({
  firstName: '',
  lastName: '',
  phone: '',
})

const changingPassword = ref(false)
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
})

function getToken() {
  return localStorage.getItem('token')
}

async function fetchProfile() {
  error.value = ''
  const token = getToken()
  if (!token) {
    router.push('/')
    return
  }

  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/user/profile`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (response.ok) {
      profile.value = await response.json()
      form.value.firstName = profile.value.firstName || ''
      form.value.lastName = profile.value.lastName || ''
      form.value.phone = profile.value.phone || ''
    } else {
      localStorage.removeItem('token')
      router.push('/')
    }
  } catch {
    error.value = 'Error de conexión con el servidor'
  }
}

function startEditing() {
  editing.value = true
  success.value = ''
}

function cancelEditing() {
  editing.value = false
  error.value = ''
  form.value.firstName = profile.value.firstName || ''
  form.value.lastName = profile.value.lastName || ''
  form.value.phone = profile.value.phone || ''
}

async function saveProfile() {
  error.value = ''
  success.value = ''

  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/user/profile`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${getToken()}`,
      },
      body: JSON.stringify(form.value),
    })
    if (response.ok) {
      profile.value = await response.json()
      editing.value = false
      success.value = 'Perfil actualizado correctamente'
    } else {
      const data = await response.json()
      error.value = data.message || 'Error al guardar'
    }
  } catch {
    error.value = 'Error de conexión con el servidor'
  }
}

function startChangingPassword() {
  changingPassword.value = true
  success.value = ''
  error.value = ''
  passwordForm.value = { currentPassword: '', newPassword: '' }
}

function cancelChangingPassword() {
  changingPassword.value = false
  error.value = ''
}

async function savePassword() {
  error.value = ''
  success.value = ''

  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/user/password`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${getToken()}`,
      },
      body: JSON.stringify({
        currentPassword: passwordForm.value.currentPassword,
        newPassword: passwordForm.value.newPassword,
      }),
    })
    const data = await response.json()
    if (response.ok) {
      changingPassword.value = false
      success.value = data.message
    } else {
      error.value = data.message || 'Error al cambiar la contraseña'
    }
  } catch {
    error.value = 'Error de conexión con el servidor'
  }
}

function handleLogout() {
  localStorage.removeItem('token')
  router.push('/')
}

onMounted(fetchProfile)
</script>

<template>
  <div class="profile-page">
    <div v-if="profile" class="card">
      <h1>Mi perfil</h1>

      <p v-if="success" class="success">{{ success }}</p>
      <p v-if="error" class="error">{{ error }}</p>

      <!-- Modo vista -->
      <div v-if="!editing" class="fields">
        <div class="field">
          <span class="label">Email</span>
          <span class="value">{{ profile.email }}</span>
        </div>
        <div class="field">
          <span class="label">Nombre</span>
          <span class="value">{{ profile.firstName || '—' }}</span>
        </div>
        <div class="field">
          <span class="label">Apellidos</span>
          <span class="value">{{ profile.lastName || '—' }}</span>
        </div>
        <div class="field">
          <span class="label">Teléfono</span>
          <span class="value">{{ profile.phone || '—' }}</span>
        </div>
        <button @click="startEditing">Editar</button>
      </div>

      <!-- Modo edición -->
      <form v-else @submit.prevent="saveProfile">
        <div class="field">
          <span class="label">Email</span>
          <span class="value">{{ profile.email }}</span>
        </div>
        <input v-model="form.firstName" type="text" placeholder="Nombre" maxlength="100" />
        <input v-model="form.lastName" type="text" placeholder="Apellidos" maxlength="100" />
        <input v-model="form.phone" type="tel" placeholder="Teléfono" maxlength="20" />
        <div class="actions">
          <button type="submit" class="btn-save">Guardar</button>
          <button type="button" class="btn-cancel" @click="cancelEditing">Cancelar</button>
        </div>
      </form>

      <!-- Cambiar contraseña -->
      <div v-if="changingPassword" class="password-section">
        <h2>Cambiar contraseña</h2>
        <form @submit.prevent="savePassword">
          <input
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="Contraseña actual"
            required
          />
          <input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="Nueva contraseña"
            minlength="8"
            required
          />
          <div class="actions">
            <button type="submit" class="btn-save">Guardar</button>
            <button type="button" class="btn-cancel" @click="cancelChangingPassword">Cancelar</button>
          </div>
        </form>
      </div>
      <button v-else-if="!editing" class="btn-password" @click="startChangingPassword">
        Cambiar contraseña
      </button>

      <button class="btn-logout" @click="handleLogout">Cerrar sesión</button>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f3f4f6;
}

.card {
  background: white;
  padding: 40px 32px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 380px;
  text-align: center;
}

h1 {
  margin: 0 0 24px;
  color: #1e293b;
}

.fields {
  text-align: left;
}

.field {
  margin-bottom: 16px;
}

.label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 2px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.value {
  font-size: 15px;
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

.actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.actions button {
  flex: 1;
}

.btn-cancel {
  background: #94a3b8;
}

.btn-cancel:hover {
  background: #64748b;
}

.password-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.password-section h2 {
  font-size: 16px;
  color: #1e293b;
  margin: 0 0 16px;
}

.btn-password {
  margin-top: 16px;
  background: transparent;
  color: #2563eb;
  border: 1px solid #2563eb;
}

.btn-password:hover {
  background: #eff6ff;
}

.btn-logout {
  margin-top: 24px;
  background: transparent;
  color: #dc2626;
  border: 1px solid #dc2626;
}

.btn-logout:hover {
  background: #fef2f2;
}

.error {
  color: #dc2626;
  font-size: 14px;
  margin: 8px 0;
}

.success {
  color: #16a34a;
  font-size: 14px;
  margin: 8px 0;
}
</style>
