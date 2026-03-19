<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useValidation } from '@/composables/useValidation'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const api = useApi()
const { validate, required, minLength, passwordStrength, firstError } = useValidation()

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

const initials = computed(() => {
  if (!profile.value) return '?'
  const f = profile.value.firstName?.[0] || ''
  const l = profile.value.lastName?.[0] || ''
  return (f + l).toUpperCase() || profile.value.email?.[0]?.toUpperCase() || '?'
})

const displayName = computed(() => {
  if (!profile.value) return ''
  const parts = [profile.value.firstName, profile.value.lastName].filter(Boolean)
  return parts.length ? parts.join(' ') : profile.value.email
})

async function fetchProfile() {
  error.value = ''
  try {
    const response = await api.get('/user/profile')
    if (response.ok) {
      profile.value = await response.json()
      auth.setUser(profile.value)
      form.value.firstName = profile.value.firstName || ''
      form.value.lastName = profile.value.lastName || ''
      form.value.phone = profile.value.phone || ''
    }
  } catch {
    error.value = t('error.connection')
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
    const response = await api.put('/user/profile', form.value)
    if (response.ok) {
      profile.value = await response.json()
      auth.setUser(profile.value)
      editing.value = false
      success.value = t('profile.updated')
    } else {
      const data = await response.json()
      error.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    error.value = t('error.connection')
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

  const valid = validate({
    currentPassword: [required(passwordForm.value.currentPassword, 'currentPassword')],
    newPassword: [
      required(passwordForm.value.newPassword, 'newPassword'),
      minLength(passwordForm.value.newPassword, 8, 'newPassword'),
      passwordStrength(passwordForm.value.newPassword),
    ],
  })
  if (!valid) {
    error.value = firstError()
    return
  }

  try {
    const response = await api.put('/user/password', {
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
    })
    if (response.ok) {
      changingPassword.value = false
      success.value = t('profile.passwordChanged')
    } else {
      const data = await response.json()
      error.value = api.translateError(data, 'error.passwordChangeFailed')
    }
  } catch {
    error.value = t('error.connection')
  }
}

function handleLogout() {
  auth.logout()
  router.push('/')
}

onMounted(fetchProfile)
</script>

<template>
  <BaseLayout>
    <div v-if="profile" class="card">
      <h1>{{ t('profile.title') }}</h1>

      <!-- Cabecera con avatar -->
      <div class="profile-header">
        <div class="avatar">{{ initials }}</div>
        <div>
          <div class="profile-name">{{ displayName }}</div>
          <div class="profile-email">{{ profile.email }}</div>
        </div>
      </div>

      <p v-if="success" class="msg-success">{{ success }}</p>
      <p v-if="error" class="msg-error">{{ error }}</p>

      <!-- Modo vista -->
      <template v-if="!editing && !changingPassword">
        <div class="fields">
          <div class="field">
            <span class="field-label">{{ t('fields.firstName') }}</span>
            <span class="field-value">{{ profile.firstName || t('fields.empty') }}</span>
          </div>
          <div class="field">
            <span class="field-label">{{ t('fields.lastName') }}</span>
            <span class="field-value">{{ profile.lastName || t('fields.empty') }}</span>
          </div>
          <div class="field">
            <span class="field-label">{{ t('fields.phone') }}</span>
            <span class="field-value">{{ profile.phone || t('fields.empty') }}</span>
          </div>
        </div>
        <div class="profile-actions">
          <button class="btn" @click="startEditing">{{ t('profile.edit') }}</button>
          <button class="btn btn-outline" @click="startChangingPassword">{{ t('profile.changePassword') }}</button>
          <button class="btn btn-danger" @click="handleLogout">{{ t('auth.logout') }}</button>
        </div>
      </template>

      <!-- Modo edición -->
      <form v-else-if="editing" @submit.prevent="saveProfile">
        <div class="form-field">
          <label for="profile-first-name">{{ t('fields.firstName') }}</label>
          <input id="profile-first-name" v-model="form.firstName" class="form-input" type="text" :placeholder="t('fields.firstName')" maxlength="100" />
        </div>
        <div class="form-field">
          <label for="profile-last-name">{{ t('fields.lastName') }}</label>
          <input id="profile-last-name" v-model="form.lastName" class="form-input" type="text" :placeholder="t('fields.lastName')" maxlength="100" />
        </div>
        <div class="form-field">
          <label for="profile-phone">{{ t('fields.phone') }}</label>
          <input id="profile-phone" v-model="form.phone" class="form-input" type="tel" :placeholder="t('fields.phone')" maxlength="20" />
        </div>
        <div class="actions">
          <button type="submit" class="btn">{{ t('profile.save') }}</button>
          <button type="button" class="btn btn-secondary" @click="cancelEditing">{{ t('profile.cancel') }}</button>
        </div>
      </form>

      <!-- Cambiar contraseña -->
      <div v-else>
        <p class="profile-section-title">{{ t('profile.changePassword') }}</p>
        <form @submit.prevent="savePassword">
          <div class="form-field">
            <label for="current-password">{{ t('fields.currentPassword') }}</label>
            <input
              id="current-password"
              v-model="passwordForm.currentPassword"
              class="form-input"
              type="password"
              :placeholder="t('fields.currentPassword')"
              required
            />
          </div>
          <div class="form-field">
            <label for="new-password">{{ t('fields.newPassword') }}</label>
            <input
              id="new-password"
              v-model="passwordForm.newPassword"
              class="form-input"
              type="password"
              :placeholder="t('fields.passwordHint')"
              minlength="8"
              required
            />
          </div>
          <div class="actions">
            <button type="submit" class="btn">{{ t('profile.save') }}</button>
            <button type="button" class="btn btn-secondary" @click="cancelChangingPassword">{{ t('profile.cancel') }}</button>
          </div>
        </form>
      </div>
    </div>
  </BaseLayout>
</template>
