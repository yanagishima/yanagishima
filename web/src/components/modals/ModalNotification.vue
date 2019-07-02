<template>
  <div id="notification" class="modal fade" role="dialog">
    <div class="modal-dialog modal-dialog-scrollable modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Notifications</h5>
          <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        </div>
        <div class="modal-body">
          <template v-if="notifications.length">
            <div v-for="n in notifications" :key="n.id" class="card mb-3">
              <div class="card-body">
                <h5 class="card-title" :class="{'font-weight-bolder': n.id > notificationReadId}">{{n.title}}</h5>
                <span class="d-block card-subtitle mb-2 text-muted">{{n.date}}</span>
                <BaseAutoLink :text="n.text" class="card-text"></BaseAutoLink>
              </div>
            </div>
          </template>
          <p v-else>There are no notifications.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState} from 'vuex'
import $ from 'jquery'

export default {
  name: 'ModalNotification',
  computed: {
    ...mapState('notification', [
      'notifications',
      'notificationReadId'
    ])
  },
  mounted () {
    $(this.$el).on('hidden.bs.modal', () => {
      this.$store.commit('notification/read')
    })
  }
}
</script>

<style lang="scss" scoped>
// want to use new bootstrap here without affecting other places so import it locally
//
// TODO: use latest bootstrap on yanagishima spa wholely and remove this line.
// yanagishima spa includes entire bootstrap source code copied from original.
// So new version of bootstrap is not used although it's defined in package.json dependency.
// We should overwrite only _custom.scss and leave the others as they are.
// It may be affect yanagishima spa wholely because the version of bootstrap yanagishima spa are using now is
// beta and many apis have been changed.
@import 'bootstrap/scss/bootstrap.scss';
</style>
