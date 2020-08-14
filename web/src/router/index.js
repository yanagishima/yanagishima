import Vue from 'vue'
import Router from 'vue-router'
import DefaultView from '@/views/DefaultView'
import ShareView from '@/views/ShareView'
import ErrorView from '@/views/ErrorView'

Vue.use(Router)

export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/diff',
      component: () => import(/* webpackChunkName: "diff-view" */ '@/views/DiffView')
    },
    {
      path: '/share',
      component: ShareView
    },
    {
      path: '/error',
      component: ErrorView
    },
    {
      path: '/',
      component: DefaultView
    }
  ]
})
