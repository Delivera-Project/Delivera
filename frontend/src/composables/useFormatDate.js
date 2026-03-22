import { useI18n } from 'vue-i18n'

export function useFormatDate() {
  const { locale } = useI18n()

  function formatDate(value) {
    if (!value) return null
    return new Date(value).toLocaleDateString(locale.value)
  }

  function formatDateTime(value) {
    if (!value) return null
    return new Date(value).toLocaleString(locale.value)
  }

  return { formatDate, formatDateTime }
}
