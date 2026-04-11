<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const api = useApi()

const unit = ref(null)
const allWorkers = ref([])
const loading = ref(false)
const error = ref('')

const assignedIds = computed(() => new Set((unit.value?.workers || []).map(w => w.id)))

async function load() {
  loading.value = true
  try {
    const [unitRes, workersRes] = await Promise.all([
      api.get(`/units/${route.params.id}`),
      api.get('/workers'),
    ])
    if (unitRes.ok) unit.value = await unitRes.json()
    else error.value = t('error.connection')
    if (workersRes.ok) allWorkers.value = await workersRes.json()
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

async function toggle(worker) {
  if (assignedIds.value.has(worker.id)) {
    const res = await api.del(`/units/${route.params.id}/workers/${worker.id}`)
    if (res.ok) unit.value = await res.json()
  } else {
    const res = await api.post(`/units/${route.params.id}/workers/${worker.id}`)
    if (res.ok) unit.value = await res.json()
  }
}

onMounted(load)
</script>

<template>
  <div class="card card-wide">
    <PButton type="button" text severity="secondary" icon="pi pi-arrow-left" class="detail-back-btn"
      @click="router.push(`/units/${route.params.id}`)" />

    <h1>{{ t('units.assignWorkers') }}</h1>
    <p v-if="unit" class="subtitle">{{ unit.name }}</p>

    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <div v-if="!loading && allWorkers.length" class="worker-list">
      <div v-for="w in allWorkers" :key="w.id" class="worker-row" @click="toggle(w)">
        <PCheckbox :model-value="assignedIds.has(w.id)" :binary="true" @click.stop="toggle(w)" />
        <span class="worker-info">
          <span class="worker-name">{{ w.firstName }} {{ w.lastName }}</span>
          <span class="worker-email">{{ w.email }}</span>
        </span>
        <PTag :value="t('workers.roles.' + w.role)" severity="secondary" />
      </div>
    </div>
    <p v-else-if="!loading" class="empty">{{ t('units.noAssignableWorkers') }}</p>
  </div>
</template>

<style scoped>
.card { text-align: left; }
.subtitle { font-size: 14px; color: #64748b; margin: -8px 0 20px; }
.worker-list { display: flex; flex-direction: column; gap: 8px; }
.worker-row { display: flex; align-items: center; gap: 12px; padding: 12px 14px; border: 1px solid #e2e8f0; border-radius: 8px; cursor: pointer; transition: background 0.1s; }
.worker-row:hover { background: #f8fafc; }
.worker-info { flex: 1; min-width: 0; }
.worker-name { display: block; font-size: 14px; font-weight: 500; color: #1e293b; }
.worker-email { display: block; font-size: 12px; color: #94a3b8; }
.empty { font-size: 13px; color: #94a3b8; }
</style>
