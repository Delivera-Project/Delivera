<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const api = useApi()
const { formatDateTime } = useFormatDate()

const keys = ref([])
const loadError = ref('')
const loading = ref(false)

const creating = ref(false)
const newKeyName = ref('')
const createError = ref('')
const createSaving = ref(false)
const createdToken = ref(null)
const tokenCopied = ref(false)

const revokingId = ref(null)
const revokeError = ref('')

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await api.get('/settings/api-keys')
    if (res.ok) keys.value = await res.json()
    else loadError.value = t('error.connection')
  } catch {
    loadError.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

function startCreate() {
  newKeyName.value = ''
  createError.value = ''
  createdToken.value = null
  creating.value = true
}

function cancelCreate() {
  creating.value = false
  createError.value = ''
}

async function submitCreate() {
  const name = newKeyName.value.trim()
  if (!name) { createError.value = t('validation.required', { field: t('apiKeys.name') }); return }
  createSaving.value = true
  createError.value = ''
  try {
    const res = await api.post('/settings/api-keys', { name })
    if (res.ok) {
      const data = await res.json()
      createdToken.value = data
      newKeyName.value = ''
      await load()
    } else {
      const data = await res.json()
      createError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    createError.value = t('error.connection')
  } finally {
    createSaving.value = false
  }
}

async function copyToken() {
  if (!createdToken.value?.token) return
  await navigator.clipboard.writeText(createdToken.value.token)
  tokenCopied.value = true
  setTimeout(() => { tokenCopied.value = false }, 2000)
}

function dismissCreated() {
  createdToken.value = null
  creating.value = false
}

async function revoke(id) {
  revokingId.value = id
  revokeError.value = ''
  try {
    const res = await api.del(`/settings/api-keys/${id}`)
    if (res.ok) await load()
    else {
      const data = await res.json()
      revokeError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    revokeError.value = t('error.connection')
  } finally {
    revokingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <div class="settings-section">
    <p class="api-keys-intro">{{ t('apiKeys.intro') }}</p>

    <PMessage v-if="loadError" severity="error" :closable="false" class="form-message">{{ loadError }}</PMessage>
    <PMessage v-if="revokeError" severity="error" :closable="false" class="form-message">{{ revokeError }}</PMessage>

    <div v-if="createdToken" class="api-key-created">
      <p class="api-key-created-title">{{ t('apiKeys.createdTitle') }}</p>
      <p class="api-key-created-warning"><i class="pi pi-exclamation-triangle" /> {{ t('apiKeys.createdWarning') }}</p>
      <div class="api-key-token-row">
        <code class="api-key-token">{{ createdToken.token }}</code>
        <PButton :icon="tokenCopied ? 'pi pi-check' : 'pi pi-copy'" :label="tokenCopied ? t('apiKeys.copied') : t('apiKeys.copy')" size="small" @click="copyToken" />
      </div>
      <div class="form-actions">
        <PButton :label="t('common.close')" severity="secondary" outlined size="small" @click="dismissCreated" />
      </div>
    </div>

    <div v-if="!createdToken && !creating" class="add-company-trigger">
      <PButton :label="t('apiKeys.create')" icon="pi pi-plus" size="small" @click="startCreate" />
    </div>

    <div v-if="!createdToken && creating" class="add-company-form">
      <div class="form-field">
        <label>{{ t('apiKeys.name') }}</label>
        <InputText v-model="newKeyName" :placeholder="t('apiKeys.namePlaceholder')" maxlength="100" fluid />
      </div>
      <PMessage v-if="createError" severity="error" :closable="false" class="form-message">{{ createError }}</PMessage>
      <div class="form-actions">
        <PButton :label="t('common.cancel')" icon="pi pi-times" severity="secondary" outlined size="small" @click="cancelCreate" />
        <PButton :label="t('apiKeys.create')" icon="pi pi-check" :loading="createSaving" size="small" @click="submitCreate" />
      </div>
    </div>

    <div v-if="keys.length === 0 && !loading && !loadError" class="api-keys-empty">
      <i class="pi pi-key" />
      <p>{{ t('apiKeys.empty') }}</p>
    </div>

    <ul v-else class="api-keys-list">
      <li v-for="k in keys" :key="k.id" class="api-key-item" :class="{ 'api-key-item--revoked': k.revokedAt }">
        <div class="api-key-info">
          <span class="api-key-name">{{ k.name }}</span>
          <code class="api-key-prefix">{{ k.prefix }}…</code>
          <span class="api-key-meta">
            {{ t('apiKeys.created') }}: {{ formatDateTime(k.createdAt) }}
            <template v-if="k.lastUsedAt"> · {{ t('apiKeys.lastUsed') }}: {{ formatDateTime(k.lastUsedAt) }}</template>
            <template v-else> · {{ t('apiKeys.neverUsed') }}</template>
          </span>
        </div>
        <div class="api-key-actions">
          <span v-if="k.revokedAt" class="api-key-status api-key-status--revoked">{{ t('apiKeys.revoked') }}</span>
          <span v-else class="api-key-status api-key-status--active">{{ t('apiKeys.active') }}</span>
          <PButton
            v-if="!k.revokedAt"
            :label="t('apiKeys.revoke')"
            icon="pi pi-ban"
            severity="danger"
            outlined
            size="small"
            :loading="revokingId === k.id"
            @click="revoke(k.id)"
          />
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.api-keys-intro { color: var(--text-color-secondary); margin: 0 0 1rem; }
.api-keys-empty { text-align: center; padding: 2rem; color: var(--text-color-secondary); }
.api-keys-empty i { font-size: 2rem; display: block; margin-bottom: 0.5rem; }
.api-keys-list { list-style: none; padding: 0; margin: 1rem 0 0; display: flex; flex-direction: column; gap: 0.5rem; }
.api-key-item { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 0.75rem 1rem; border: 1px solid var(--surface-border); border-radius: 8px; }
.api-key-item--revoked { opacity: 0.6; }
.api-key-info { display: flex; flex-direction: column; gap: 0.25rem; min-width: 0; }
.api-key-name { font-weight: 600; }
.api-key-prefix { font-family: monospace; font-size: 0.85rem; color: var(--text-color-secondary); }
.api-key-meta { font-size: 0.8rem; color: var(--text-color-secondary); }
.api-key-actions { display: flex; align-items: center; gap: 0.5rem; flex-shrink: 0; }
.api-key-status { font-size: 0.75rem; font-weight: 600; padding: 0.15rem 0.5rem; border-radius: 12px; text-transform: uppercase; }
.api-key-status--active { background: var(--green-100, #d1fae5); color: var(--green-700, #047857); }
.api-key-status--revoked { background: var(--surface-200, #e5e7eb); color: var(--text-color-secondary); }
.api-key-created { padding: 1rem; border: 1px solid var(--primary-color); border-radius: 8px; background: var(--primary-50, #eff6ff); margin-bottom: 1rem; }
.api-key-created-title { font-weight: 600; margin: 0 0 0.5rem; }
.api-key-created-warning { color: var(--orange-600, #d97706); margin: 0 0 0.75rem; font-size: 0.875rem; }
.api-key-token-row { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.75rem; }
.api-key-token { flex: 1; padding: 0.5rem; background: var(--surface-card); border: 1px solid var(--surface-border); border-radius: 4px; font-family: monospace; font-size: 0.85rem; word-break: break-all; }
</style>
