<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCompanyRegistration } from '@/composables/useCompanyRegistration'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const overlayActive = ref(true)

function handleSignIn() {
  overlayActive.value = false
  setTimeout(() => router.push('/'), 300)
}

const { step, activityType, companyName, email, password, confirmPassword, error, errors, invalids, loading, goToStep2, submitRegistration } = useCompanyRegistration()

const P = (d) => `<path stroke-linecap="round" stroke-linejoin="round" d="${d}" />`
const SVG = (...paths) =>
  `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">${paths.join('')}</svg>`

function makeActivity(value, ...paths) {
  return { value, label: computed(() => t(`activity.${value.toLowerCase()}`)), icon: SVG(...paths) }
}

const activityTypes = [
  makeActivity('INDUSTRY',
    P('M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 011.37.49l1.296 2.247a1.125 1.125 0 01-.26 1.431l-1.003.827c-.293.24-.438.613-.431.992a6.759 6.759 0 010 .255c-.007.378.138.75.43.99l1.005.828c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 01-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.57 6.57 0 01-.22.128c-.331.183-.581.495-.644.869l-.213 1.28c-.09.543-.56.941-1.11.941h-2.594c-.55 0-1.02-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 01-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 01-1.369-.49l-1.297-2.247a1.125 1.125 0 01.26-1.431l1.004-.827c.292-.24.437-.613.43-.992a6.932 6.932 0 010-.255c.007-.378-.138-.75-.43-.99l-1.004-.828a1.125 1.125 0 01-.26-1.43l1.297-2.247a1.125 1.125 0 011.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.087.22-.128.332-.183.582-.495.644-.869l.214-1.281z'),
    P('M15 12a3 3 0 11-6 0 3 3 0 016 0z'),
  ),
  makeActivity('DISTRIBUTION',
    P('M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z'),
  ),
  makeActivity('FOOD',
    P('M4 11h16'),
    P('M4 11a8 6 0 0 1 16 0'),
    P('M9 10Q8 7 9 5'),
    P('M12 10Q11 7 12 5'),
    P('M15 10Q16 7 15 5'),
  ),
  makeActivity('RETAIL',
    P('M15.75 10.5V6a3.75 3.75 0 10-7.5 0v4.5m11.356-1.993l1.263 12c.07.665-.45 1.243-1.119 1.243H4.25a1.125 1.125 0 01-1.12-1.243l1.264-12A1.125 1.125 0 015.513 7.5h12.974c.576 0 1.059.435 1.119 1.007z'),
  ),
  makeActivity('TRANSPORT',
    P('M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12'),
  ),
  makeActivity('OTHER',
    P('M6.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM12.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM18.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0z'),
  ),
]
</script>

<template>
  <div class="bg-overlay bg-purple" :class="{ active: overlayActive }"></div>
  <BaseLayout>
    <div class="card card-wide theme-company">
      <!-- Step indicator -->
      <div class="steps">
        <div class="step-item" :class="{ done: step > 1, active: step === 1 }">
          <div class="step-dot" :class="{ active: step === 1, done: step > 1 }">
            <svg v-if="step > 1" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
            </svg>
            <span v-else>1</span>
          </div>
          <span class="step-label" :class="{ active: step === 1, done: step > 1 }">{{ t('register.stepActivity') }}</span>
        </div>
        <div class="step-item" :class="{ active: step === 2 }">
          <div class="step-dot" :class="{ active: step === 2 }">2</div>
          <span class="step-label" :class="{ active: step === 2 }">{{ t('register.stepAccount') }}</span>
        </div>
      </div>

      <!-- Step 1 -->
      <div v-if="step === 1">
        <PButton
          type="button"
          text
          severity="secondary"
          icon="pi pi-arrow-left"
          class="back-btn"
          rounded
          @click="router.push('/register')"
        />
        <h1>{{ t('register.companyTitle') }}</h1>
        <p class="subtitle">{{ t('register.activitySubtitle') }}</p>

        <div class="activity-grid">
          <button
            v-for="at in activityTypes"
            :key="at.value"
            type="button"
            class="activity-option"
            :class="{ selected: activityType === at.value }"
            @click="activityType = at.value; error = ''"
          >
            <span v-html="at.icon" style="display:contents"></span>
            <span>{{ at.label }}</span>
          </button>
        </div>

        <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>
        <PButton :label="t('common.next')" icon="pi pi-arrow-right" icon-pos="right" fluid @click="goToStep2" />
        <p class="form-link">{{ t('auth.hasAccount') }} <a href="/" @click.prevent="handleSignIn">{{ t('auth.signIn') }}</a></p>
      </div>

      <!-- Step 2 -->
      <form v-else @submit.prevent="submitRegistration">
        <PButton
          type="button"
          text
          severity="secondary"
          icon="pi pi-arrow-left"
          class="back-btn"
          rounded
          @click="step = 1; error = ''"
        />

        <h1>{{ t('register.companyTitle') }}</h1>
        <p class="subtitle">{{ t('register.accountSubtitle') }}</p>

        <div class="form-field">
          <label for="company-name">{{ t('fields.companyName') }}</label>
          <InputText
            id="company-name"
            v-model="companyName"
            :placeholder="t('fields.companyNamePlaceholder')"
            :invalid="!!invalids.companyName"
            maxlength="255"
            fluid
          />
          <small v-if="errors.companyName" class="field-error">{{ errors.companyName }}</small>
        </div>

        <div class="form-field">
          <label for="company-email">{{ t('fields.email') }}</label>
          <InputText id="company-email" v-model="email" type="email" :placeholder="t('fields.emailPlaceholder')" :invalid="!!invalids.email" fluid />
          <small v-if="errors.email" class="field-error">{{ errors.email }}</small>
        </div>

        <div class="form-field">
          <label for="company-password">{{ t('fields.password') }}</label>
          <PPassword id="company-password" v-model="password" :feedback="false" toggle-mask :placeholder="t('fields.passwordHint')" :invalid="!!invalids.password" fluid />
          <small v-if="errors.password" class="field-error">{{ errors.password }}</small>
        </div>

        <div class="form-field">
          <label for="company-confirm-password">{{ t('fields.confirmPassword') }}</label>
          <PPassword id="company-confirm-password" v-model="confirmPassword" :feedback="false" toggle-mask :placeholder="t('fields.confirmPassword')" :invalid="!!invalids.confirmPassword" fluid />
          <small v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</small>
        </div>

        <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>
        <PButton
          type="submit"
          :label="loading ? t('common.loading') : t('register.createCompany')"
          :loading="loading"
          fluid
          class="submit-btn"
        />
        <p class="form-link">{{ t('auth.hasAccount') }} <a href="/" @click.prevent="handleSignIn">{{ t('auth.signIn') }}</a></p>
      </form>
    </div>
  </BaseLayout>
</template>
