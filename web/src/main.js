// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Sugar from 'sugar'
import 'bootstrap/dist/js/bootstrap.bundle'
import '@/assets/scss/bootstrap.scss'
import 'toastr/toastr.scss'
import 'lity'
import 'lity/dist/lity.min.css'
import toastr from 'toastr'
import Vue from 'vue'
import App from '@/App'
import router from '@/router'
import store from '@/store'
import filters from '@/filters'
import VueClipboards from 'vue-clipboards'
import VueScrollTo from 'vue-scrollto'
import VueCharts from 'vue-charts'
import BaseAce from '@/components/base/BaseAce'
import BaseAutoLink from '@/components/base/BaseAutoLink'
import BaseHighlight from '@/components/base/BaseHighlight'

Sugar.extend()

toastr.options = {
  escapeHtml: false,
  closeButton: true,
  debug: false,
  newestOnTop: false,
  progressBar: false,
  positionClass: 'toast-bottom-right',
  preventDuplicates: false,
  onclick: null,
  showDuration: 300,
  hideDuration: 1000,
  timeOut: 30000,
  extendedTimeOut: 1000,
  showEasing: 'swing',
  hideEasing: 'linear',
  showMethod: 'fadeIn',
  hideMethod: 'fadeOut'
}

Vue.config.productionTip = false

Vue.use(VueClipboards)
Vue.use(VueScrollTo)
Vue.use(VueCharts)

Vue.component('BaseAce', BaseAce)
Vue.component('BaseAutoLink', BaseAutoLink)
Vue.component('BaseHighlight', BaseHighlight)

Vue.directive('focus', {
  inserted (el) {
    el.focus()
  }
})

for (const [key, val] of Object.entries(filters)) {
  Vue.filter(key, val)
}

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: {App},
  template: '<App/>'
})
