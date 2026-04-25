// Helpers reutilizables para gestionar la prioridad efectiva de un pedido,
// con herencia desde la configuración de empresa (DSI-23.1).

export const ORDER_PRIORITIES = ['HIGH', 'NORMAL', 'LOW']

// Construye las opciones para un selector de prioridad a partir de la función de traducción.
export function buildPriorityOptions(t) {
  return ORDER_PRIORITIES.map(value => ({ value, label: t(`orders.priority.${value}`) }))
}

// Resuelve la prioridad efectiva: solicitada > config empresa > NORMAL.
export function resolveOrderPriority(requested, companyDefault) {
  if (requested) return requested
  if (companyDefault) return companyDefault
  return 'NORMAL'
}
