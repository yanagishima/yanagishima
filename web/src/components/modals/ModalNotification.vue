<template>
  <div id="notification" class="modal fade" role="dialog">
    <div class="modal-dialog modal-dialog-scrollable modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title"><strong>Notifications</strong></h5>
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
</style>
