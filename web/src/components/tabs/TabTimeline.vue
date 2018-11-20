<template>
  <div>
    <div class="header row align-items-center pt-3">
      <div class="col">
        <strong class="mr-1">Timeline</strong>
        <span v-if="timelines.length">{{timelines.length}}</span>
      </div>
      <div class="col text-right">
        <input type="text" class="form-control form-control-sm d-inline-block w-50"
               placeholder="Search by Comment" v-model="filter" v-focus @keyup.enter="getTimeline">
      </div>
    </div>

    <div>
      <template v-if="loading.timeline">
        <div class="alert alert-info">
          <i class="fa fa-fw fa-spinner fa-pulse mr-1"></i>Loading
        </div>
      </template>
      <template v-else>
        <table class="table table-bordered table-fixed table-hover" v-if="timelines.length">
          <thead>
          <tr>
            <th width="42.5%">Comment</th>
            <th width="12.5%">When</th>
            <th width="5%">Like</th>
            <th width="20%">User</th>
            <th width="17.5%">query ID</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(comment, i) in timelines" :key="i" class="vertical-top">
            <td>
              <pre class="comment"><BaseAutoLink :text="comment.content.escapeHTML()"></BaseAutoLink></pre>
            </td>
            <td>
              {{comment.updateTimeString | relativeDate}}
            </td>
            <td class="text-right">
              <template v-if="comment.likeCount">{{comment.likeCount}}</template>
            </td>
            <td>
              {{comment.user || 'Someone'}}
            </td>
            <td>
              <a :href="buildUrl({datasource, engine, tab: 'result', queryid: comment.queryid})">{{comment.queryid}}</a>
            </td>
          </tr>
          </tbody>
        </table>
        <div class="alert alert-warning" v-else>
          <i class="fa fa-fw fa-frown-o mr-1"></i>No result
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import {mapState} from 'vuex'
import util from '@/mixins/util'

export default {
  name: 'TabTimeline',
  mixins: [util],
  computed: {
    ...mapState({
      datasource: state => state.hash.datasource,
      engine: state => state.hash.engine
    }),
    ...mapState('timeline', [
      'loading'
    ]),
    ...mapState('timeline', {
      filterOrig: state => state.filter,
      timelines: state => state.response
    }),
    filter: {
      get () {
        return this.filterOrig
      },
      set (val) {
        this.$store.commit('timeline/setFilter', {data: val})
      }
    }
  },
  created () {
    this.$store.dispatch('timeline/getTimeline')
  },
  methods: {
    getTimeline () {
      this.$store.dispatch('timeline/getTimeline')
    }
  }
}
</script>

<style scoped>
</style>
