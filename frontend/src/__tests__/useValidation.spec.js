import { describe, it, expect, vi } from 'vitest'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key) => key }),
}))

const { useValidation } = await import('@/composables/useValidation')

describe('useValidation — validators', () => {
  it('required: empty / null / whitespace falla; valor real pasa', () => {
    const { required } = useValidation()
    expect(required('', 'field')()).not.toBeNull()
    expect(required(null, 'field')()).not.toBeNull()
    expect(required('   ', 'field')()).not.toBeNull()
    expect(required('value', 'field')()).toBeNull()
  })

  it('email: formato inválido falla; válido o vacío pasa', () => {
    const { email } = useValidation()
    expect(email('not-email')()).not.toBeNull()
    expect(email('a@b.com')()).toBeNull()
    expect(email('')()).toBeNull()
  })

  it('minLength: por debajo del mínimo falla; igual o vacío pasa', () => {
    const { minLength } = useValidation()
    expect(minLength('ab', 3, 'field')()).not.toBeNull()
    expect(minLength('abc', 3, 'field')()).toBeNull()
    expect(minLength('', 3, 'field')()).toBeNull()
  })

  it('maxLength: por encima del máximo falla; igual o vacío pasa', () => {
    const { maxLength } = useValidation()
    expect(maxLength('abcd', 3, 'field')()).not.toBeNull()
    expect(maxLength('abc', 3, 'field')()).toBeNull()
    expect(maxLength('', 3, 'field')()).toBeNull()
  })

  it('pattern: sin coincidencia falla; coincide o vacío pasa', () => {
    const { pattern } = useValidation()
    expect(pattern('abc', /^\d+$/, 'msg')()).not.toBeNull()
    expect(pattern('123', /^\d+$/, 'msg')()).toBeNull()
    expect(pattern('', /^\d+$/, 'msg')()).toBeNull()
  })

  it('match: valores distintos fallan; iguales pasan', () => {
    const { match } = useValidation()
    expect(match('a', 'b')()).not.toBeNull()
    expect(match('a', 'a')()).toBeNull()
  })

  it('passwordStrength: sin mayúscula/número falla; fuerte pasa; vacío pasa', () => {
    const { passwordStrength } = useValidation()
    expect(passwordStrength('alllowercase1')()).not.toBeNull()
    expect(passwordStrength('nouppercase1')()).not.toBeNull()
    expect(passwordStrength('StrongPass1')()).toBeNull()
    expect(passwordStrength('')()).toBeNull()
  })

  it('usernameFormat: inválido falla; válido o vacío pasa', () => {
    const { usernameFormat } = useValidation()
    expect(usernameFormat('AB')()).not.toBeNull()
    expect(usernameFormat('valid_user-1')()).toBeNull()
    expect(usernameFormat('')()).toBeNull()
  })
})

describe('useValidation — validate / firstError', () => {
  it('validate: establece errors/invalids y devuelve false al fallar', () => {
    const { validate, required, errors, invalids } = useValidation()
    expect(validate({ name: [required('', 'name')] })).toBe(false)
    expect(invalids.value.name).toBe(true)
    expect(errors.value.name).toBeTruthy()
  })

  it('validate: devuelve true cuando todas las reglas pasan', () => {
    const { validate, required, errors } = useValidation()
    expect(validate({ name: [required('Manuel', 'name')] })).toBe(true)
    expect(Object.keys(errors.value)).toHaveLength(0)
  })

  it('validate: detiene en el primer error del campo', () => {
    const { validate, required, email, errors } = useValidation()
    validate({ email: [required('', 'email'), email('')] })
    expect(Object.keys(errors.value)).toHaveLength(1)
  })

  it('firstError: devuelve string vacío sin errores; primer mensaje si los hay', () => {
    const { validate, required, firstError } = useValidation()
    expect(firstError()).toBe('')
    validate({ name: [required('', 'name')] })
    expect(firstError()).toBeTruthy()
  })
})
