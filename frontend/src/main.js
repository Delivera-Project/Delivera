import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { definePreset } from '@primevue/themes'
import Aura from '@primevue/themes/aura'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import SelectButton from 'primevue/selectbutton'
import Avatar from 'primevue/avatar'
import Skeleton from 'primevue/skeleton'
import Tooltip from 'primevue/tooltip'
import 'primeicons/primeicons.css'

import App from './App.vue'
import router from './router'
import i18n from './i18n'
import './assets/main.css'

const DeliverPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{blue.50}',
      100: '{blue.100}',
      200: '{blue.200}',
      300: '{blue.300}',
      400: '{blue.400}',
      500: '{blue.500}',
      600: '{blue.600}',
      700: '{blue.700}',
      800: '{blue.800}',
      900: '{blue.900}',
      950: '{blue.950}',
    },
  },
})

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(PrimeVue, {
  theme: {
    preset: DeliverPreset,
    options: { darkModeSelector: false },
  },
})

app.component('InputText', InputText)
app.component('PPassword', Password)
app.component('PButton', Button)
app.component('PMessage', Message)
app.component('PSelect', Select)
app.component('PTextarea', Textarea)
app.component('DataTable', DataTable)
app.component('Column', Column) // eslint-disable-line vue/multi-word-component-names
app.component('PTag', Tag)
app.component('SelectButton', SelectButton)
app.component('PAvatar', Avatar)
app.component('PSkeleton', Skeleton)
app.directive('tooltip', Tooltip)

app.mount('#app')
