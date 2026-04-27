<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { WORKER_ROLES } from '@/constants/roles'

const { t } = useI18n()
const router = useRouter()
const api = useApi()

const email = ref('')
const role = ref('OPERATOR')
const loading = ref(false)
const error = ref('')
const invited = ref(null)
const tempPassword = ref(null)
const copied = ref(false)

const roleOptions = computed(() =>
  WORKER_ROLES.map(r => ({ label: t('workers.roles.' + r), value: r }))
)

async function submit() {
  if (!email.value.trim()) {
    error.value = t('validation.required', { field: t('workers.email') })
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await api.post('/workers/invite', { email: email.value.trim(), role: role.value })
    if (res.ok) {
      const created = await res.json()
      invited.value = created.email
      tempPassword.value = created.tempPassword ?? null
    } else {
      const data = await res.json()
      error.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

function copyPassword() {
  navigator.clipboard.writeText(tempPassword.value)
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}
</script>

<template>
  <div class="invite-page">
    <div class="surface-card invite-card">
      <template v-if="invited">
        <div class="success-icon"><i class="pi pi-check-circle" /></div>
        <h1 class="invite-title">{{ t('workers.invited') }}</h1>
        <p class="invite-subtitle">
          {{ tempPassword ? t('workers.invitedNewUser', { email: invited }) : t('workers.invitedExisting', { email: invited }) }}
        </p>

        <div v-if="tempPassword" class="temp-password-box">
          <span class="temp-password-label">{{ t('workers.tempPasswordHint') }}</span>
          <div class="temp-password-row">
            <code class="temp-password-value">{{ tempPassword }}</code>
            <PButton
              :label="copied ? t('workers.passwordCopied') : t('workers.copyPassword')"
              :icon="copied ? 'pi pi-check' : 'pi pi-copy'"
              size="small"
              severity="secondary"
              outlined
              @click="copyPassword"
            />
          </div>
          <small class="temp-password-hint">{{ t('workers.tempPasswordChangeHint') }}</small>
        </div>

        <div class="form-actions">
          <PButton :label="t('common.back')" icon="pi pi-arrow-left" @click="router.push('/workers')" />
        </div>
      </template>

      <template v-else>
        <h1 class="invite-title">{{ t('workers.inviteTitle') }}</h1>
        <p class="invite-subtitle">{{ t('workers.inviteHint') }}</p>

        <div class="invite-form">
          <div class="form-field">
            <label>{{ t('workers.email') }}</label>
            <InputText v-model="email" type="email" :placeholder="t('fields.emailPlaceholder')" maxlength="255" :invalid="!!error && !email.trim()" @input="error = ''" fluid />
          </div>
          <div class="form-field">
            <label>{{ t('workers.role') }}</label>
            <PSelect v-model="role" :options="roleOptions" option-label="label" option-value="value" fluid />
          </div>
        </div>

        <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>

        <div class="form-actions">
          <PButton :label="t('common.cancel')" severity="secondary" outlined icon="pi pi-times" @click="router.push('/workers')" />
          <PButton :label="loading ? t('common.loading') : t('workers.invite')" icon="pi pi-user-plus" :loading="loading" @click="submit" />
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped src="./InviteWorkerView.css"></style>
