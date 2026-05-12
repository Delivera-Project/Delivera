import { ref } from 'vue'

// Captura coordenadas GPS del navegador y las redondea a 6 decimales.
// Devuelve una promesa que resuelve con { lat, lon } o rechaza con un Error.
export function useGeolocation() {
  const locating = ref(false)

  function getPosition({ timeout = 8000, enableHighAccuracy = true } = {}) {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('GEOLOCATION_UNAVAILABLE'))
        return
      }
      locating.value = true
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          locating.value = false
          resolve({
            lat: Number(pos.coords.latitude.toFixed(6)),
            lon: Number(pos.coords.longitude.toFixed(6))
          })
        },
        (err) => {
          locating.value = false
          reject(new Error(err.message))
        },
        { enableHighAccuracy, timeout }
      )
    })
  }

  return { locating, getPosition }
}
