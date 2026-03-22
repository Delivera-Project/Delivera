import { createI18n } from 'vue-i18n'
import es from '@/locales/es.yml'
import en from '@/locales/en.yml'

// Default: idioma del navegador. La preferencia por usuario se aplica en AppLayout tras cargar el perfil.
const browserLocale = navigator.language?.startsWith('en') ? 'en' : 'es'

const i18n = createI18n({
  legacy: false,
  locale: browserLocale,
  fallbackLocale: 'es',
  messages: { es, en },
})

export default i18n
