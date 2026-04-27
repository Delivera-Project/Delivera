// Wrapper alrededor del ConfirmDialog de PrimeVue para flujos de borrado.
// Mantiene la cabecera, el icono y los labels coherentes en toda la app y
// permite testear la lógica sin montar el ConfirmDialog real.

export function buildDeleteConfirmOptions(t, message, accept) {
  return {
    message,
    header: t('common.delete'),
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: t('common.delete'),
    rejectLabel: t('common.cancel'),
    acceptProps: { severity: 'danger' },
    accept,
  }
}
