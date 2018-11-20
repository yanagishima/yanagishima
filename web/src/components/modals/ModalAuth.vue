<template>
  <div id="auth" class="modal fade">
    <div class="modal-dialog modal-sm" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title"><strong>{{datasource}}</strong></h5>
          <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        </div>
        <form class="mb-0" @submit.prevent="submit">
          <div class="modal-body p-3">
            <div class="mb-3">
              <label class="mb-1">User account</label>
              <input type="text" autocomplete="username" class="form-control" v-model.lazy="form.user">
            </div>
            <div class="mb-3">
              <label class="mb-1">Password</label>
              <input type="password" autocomplete="current-password" class="form-control" v-model.lazy="form.pass">
            </div>
            <div class="text-center">
              <button type="button" class="btn btn-sm btn-secondary" @click="testAuth"><i
                class="fa fa-fw fa-check mr-1"></i>Login Check
              </button>
            </div>
          </div>
          <div class="modal-footer justify-content-center" v-if="authDone">
            <button type="submit" class="btn btn-primary w-25">Save</button>
            <button type="button" class="btn btn-secondary w-25" data-dismiss="modal">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState} from 'vuex'
import util from '@/mixins/util'

export default {
  name: 'ModalAuth',
  mixins: [util],
  data () {
    return {
      authDone: false,
      form: {
        user: '',
        pass: ''
      }
    }
  },
  computed: {
    ...mapState([
      'authUser',
      'authPass'
    ]),
    ...mapState({
      datasource: state => state.hash.datasource
    })
  },
  watch: {
    authUser (val) {
      this.form.user = val
    },
    authPass (val) {
      this.form.pass = val
    }
  },
  methods: {
    submit () {
      this.$store.commit('setAuth', this.form)
      this.hideModal('auth')
    },
    testAuth () {
      this.$store.dispatch('testAuth', {user: this.form.user, password: this.form.pass})
        .then(() => {
          this.authDone = true
        })
        .catch(() => {
        })
    }
  }
}
</script>

<style scoped>
</style>
