<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useValidation } from '@/composables/useValidation'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const api = useApi()
const { validate, required, minLength, passwordStrength, firstError } = useValidation()

const profile = ref(null)
const editing = ref(false)
const error = ref('')
const success = ref('')

const form = ref({ firstName: '', lastName: '', phone: '' })
const changingPassword = ref(false)
const passwordForm = ref({ currentPassword: '', newPassword: '' })

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

function startEditing() { editing.value = true; success.value = '' }
function cancelEditing() {
  editing.value = false; error.value = ''
  form.value.firstName = profile.value.firstName || ''
  form.value.lastName = profile.value.lastName || ''
  form.value.phone = profile.value.phone || ''
}

async function saveProfile() {
  error.value = ''; success.value = ''
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

function startChangingPassword() { changingPassword.value = true; success.value = ''; error.value = ''; passwordForm.value = { currentPassword: '', newPassword: '' } }
function cancelChangingPassword() { changingPassword.value = false; error.value = '' }

async function savePassword() {
  error.value = ''; success.value = ''
  const valid = validate({
    currentPassword: [required(passwordForm.value.currentPassword, 'currentPassword')],
    newPassword: [
      required(passwordForm.value.newPassword, 'newPassword'),
      minLength(passwordForm.value.newPassword, 8, 'newPassword'),
      passwordStrength(passwordForm.value.newPassword),
    ],
  })
  if (!valid) { error.value = firstError(); return }
  try {
    const response = await api.put('/user/password', { currentPassword: passwordForm.value.currentPassword, newPassword: passwordForm.value.newPassword })
    if (response.ok) { changingPassword.value = false; success.value = t('profile.passwordChanged') }
    else { const data = await response.json(); error.value = api.translateError(data, 'error.passwordChangeFailed') }
  } catch {
    error.value = t('error.connection')
  }
}

function handleLogout() { auth.logout(); router.push('/') }

onMounted(fetchProfile)
</script>

<template>
  <div>
    <!-- Skeleton mientras carga el perfil -->
    <div v-if="!profile" class="card">
      <PSkeleton width="7rem" height="1.4rem" border-radius="8px" class="mb-4" style="margin-inline:auto" />
      <div class="profile-header">
        <PSkeleton shape="circle" size="3.25rem" />
        <div style="flex:1; text-align:left">
          <PSkeleton width="55%" height="0.9rem" class="mb-2" />
          <PSkeleton width="75%" height="0.75rem" />
        </div>
      </div>
      <div class="fields">
        <div v-for="i in 3" :key="i" class="field">
          <PSkeleton width="4rem" height="0.6rem" class="mb-2" border-radius="4px" />
          <PSkeleton width="65%" height="0.875rem" border-radius="4px" />
        </div>
      </div>
      <div class="profile-actions">
        <PSkeleton height="2.4rem" border-radius="8px" />
        <PSkeleton height="2.4rem" border-radius="8px" />
        <PSkeleton height="2.4rem" border-radius="8px" />
      </div>
    </div>

    <div v-else class="card">
      <h1>{{ t('profile.title') }}</h1>

      <div class="profile-header">
        <PAvatar :label="initials" size="large" shape="circle" class="profile-avatar" />
        <div>
          <div class="profile-name">{{ displayName }}</div>
          <div class="profile-email">{{ profile.email }}</div>
        </div>
      </div>

      <PMessage v-if="success" severity="success" :closable="false" class="form-message">{{ success }}</PMessage>
      <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>

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
          <div class="field">
            <span class="field-label">{{ t('profile.memberSince') }}</span>
            <span class="field-value">{{ profile.createdAt ? new Date(profile.createdAt).toLocaleDateString() : t('fields.empty') }}</span>
          </div>
        </div>
        <div class="profile-actions">
          <PButton :label="t('profile.edit')" fluid @click="startEditing" />
          <PButton :label="t('profile.changePassword')" severity="secondary" outlined fluid @click="startChangingPassword" />
          <PButton :label="t('auth.logout')" severity="danger" outlined fluid @click="handleLogout" />
        </div>
      </template>

      <!-- Modo edición -->
      <form v-else-if="editing" @submit.prevent="saveProfile">
        <div class="form-field">
          <label for="profile-first-name">{{ t('fields.firstName') }}</label>
          <InputText id="profile-first-name" v-model="form.firstName" :placeholder="t('fields.firstName')" maxlength="100" fluid />
        </div>
        <div class="form-field">
          <label for="profile-last-name">{{ t('fields.lastName') }}</label>
          <InputText id="profile-last-name" v-model="form.lastName" :placeholder="t('fields.lastName')" maxlength="100" fluid />
        </div>
        <div class="form-field">
          <label for="profile-phone">{{ t('fields.phone') }}</label>
          <InputText id="profile-phone" v-model="form.phone" type="tel" :placeholder="t('fields.phone')" maxlength="20" fluid />
        </div>
        <div class="actions">
          <PButton type="submit" :label="t('profile.save')" fluid />
          <PButton type="button" :label="t('profile.cancel')" severity="secondary" outlined fluid @click="cancelEditing" />
        </div>
      </form>

      <!-- Cambiar contraseña -->
      <div v-else>
        <p class="profile-section-title">{{ t('profile.changePassword') }}</p>
        <form @submit.prevent="savePassword">
          <div class="form-field">
            <label for="current-password">{{ t('fields.currentPassword') }}</label>
            <PPassword id="current-password" v-model="passwordForm.currentPassword" :feedback="false" toggle-mask :placeholder="t('fields.currentPassword')" fluid />
          </div>
          <div class="form-field">
            <label for="new-password">{{ t('fields.newPassword') }}</label>
            <PPassword id="new-password" v-model="passwordForm.newPassword" :feedback="false" toggle-mask :placeholder="t('fields.passwordHint')" fluid />
          </div>
          <div class="actions">
            <PButton type="submit" :label="t('profile.save')" fluid />
            <PButton type="button" :label="t('profile.cancel')" severity="secondary" outlined fluid @click="cancelChangingPassword" />
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
