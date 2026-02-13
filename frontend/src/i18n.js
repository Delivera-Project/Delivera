import { createI18n } from 'vue-i18n'
import es from '@/locales/es.yml'
import en from '@/locales/en.yml'

// Configuración de internacionalización (i18n)
const i18n = createI18n({
  legacy: false,
  locale: 'es', // idioma por defecto
  fallbackLocale: 'es',
  messages: { es, en },
})

export default i18n
