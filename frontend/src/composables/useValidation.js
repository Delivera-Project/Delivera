import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

export function useValidation() {
  const { t } = useI18n()
  const errors = ref({})    // only format/logic error messages (shown below field)
  const invalids = ref({})  // all invalid fields, including required (red border only)

  function validate(rules) {
    errors.value = {}
    invalids.value = {}
    let valid = true

    for (const [field, checks] of Object.entries(rules)) {
      for (const check of checks) {
        const result = check()
        if (result) {
          invalids.value[field] = true
          errors.value[field] = result.message
          valid = false
          break
        }
      }
    }

    return valid
  }

  function required(value, fieldKey) {
    return () => {
      if (!value || !value.toString().trim()) {
        return { message: t('validation.required', { field: t(`fields.${fieldKey}`) }), type: 'required' }
      }
      return null
    }
  }

  function email(value) {
    return () => {
      const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (value && !regex.test(value)) {
        return { message: t('validation.email'), type: 'format' }
      }
      return null
    }
  }

  function minLength(value, min, fieldKey) {
    return () => {
      if (value && value.length < min) {
        return { message: t('validation.minLength', { field: t(`fields.${fieldKey}`), min }), type: 'format' }
      }
      return null
    }
  }

  function maxLength(value, max, fieldKey) {
    return () => {
      if (value && value.length > max) {
        return { message: t('validation.maxLength', { field: t(`fields.${fieldKey}`), max }), type: 'format' }
      }
      return null
    }
  }

  function pattern(value, regex, messageKey) {
    return () => {
      if (value && !regex.test(value)) {
        return { message: t(messageKey), type: 'format' }
      }
      return null
    }
  }

  function match(value1, value2) {
    return () => {
      if (value1 !== value2) {
        return { message: t('validation.passwordsDoNotMatch'), type: 'format' }
      }
      return null
    }
  }

  function passwordStrength(value) {
    return () => {
      if (value && !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/.test(value)) {
        return { message: t('validation.passwordStrength'), type: 'format' }
      }
      return null
    }
  }

  function usernameFormat(value) {
    return () => {
      if (value && !/^[a-z0-9_-]{3,50}$/.test(value)) {
        return { message: t('fields.usernameHint'), type: 'format' }
      }
      return null
    }
  }

  function firstError() {
    const keys = Object.keys(errors.value)
    return keys.length > 0 ? errors.value[keys[0]] : ''
  }

  return { errors, invalids, validate, required, email, minLength, maxLength, pattern, match, passwordStrength, usernameFormat, firstError }
}
