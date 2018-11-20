<template>
  <div id="comment" class="card" :class="{conpact: !visibleComment}"
       v-if="!isBottomPanelOpen && tab === 'result' && queryid">
    <div class="card-header">
      <div class="d-flex justify-content-between align-items-center">
        <div>
          <template v-if="visibleComment">
            <i class="fa fa-commenting-o fa-flip-horizontal mr-1"></i>
            <template v-if="comment.edit">
              Comment on this result
            </template>
            <template v-else>
              <strong>{{comment.user || 'Someone'}}</strong> commented {{comment.update | relativeDate}}
            </template>
          </template>
          <template v-else>
            <a href="#" @click.prevent="setVisibleComment(true)">
              <i class="fa fa-commenting fa-flip-horizontal mr-1"></i>
              <template v-if="comment.edit">
                Comment on this result
              </template>
              <template v-else>
                <strong>{{comment.user || 'Someone'}}</strong> commented {{comment.update | relativeDate}}
              </template>
            </a>
          </template>
        </div>
        <div v-if="visibleComment">
          <a href="#" @click.prevent="setVisibleComment(false)"><i class="fa fa-fw fa-times"></i></a>
        </div>
      </div>
    </div>
    <div class="card-block">
      <div id="comment-body">
        <template v-if="comment.edit">
          <textarea class="form-control mb-3" v-model="inputCommentModel" :rows="inputCommentRows"
                    placeholder="Input your comment."></textarea>
        </template>
        <template v-else>
          <pre class="comment"><template v-if="comment.content"><BaseAutoLink
            :text="comment.content.escapeHTML()"></BaseAutoLink></template><template v-else><span class="text-muted">(none)</span></template></pre>
        </template>
      </div>
      <div class="d-flex justify-content-between align-items-center mb-2">
        <div>
          <button type="button" class="btn btn-sm btn-secondary" :disabled="comment.edit" @click="postCommentLike">
            <i class="fa fa-thumbs-o-up mr-1"></i>Like
            <span class="badge badge-pill badge-primary ml-1" v-if="comment.like">{{comment.like}}</span>
          </button>
        </div>
        <div>
          <template v-if="comment.edit">
            <button type="button" class="btn btn-sm btn-primary px-3" @click="postComment"
                    :disabled="!inputComment.length">Post
            </button>
            <button type="button" class="btn btn-sm btn-secondary ml-1" @click="setComment({edit: false})"
                    v-if="comment.content">Cancel
            </button>
          </template>
          <template v-else>
            <div class="btn-group dropup">
              <button type="button" class="btn btn-sm btn-secondary dropdown-toggle" data-toggle="dropdown">Action
              </button>
              <div class="dropdown-menu dropdown-menu-right">
                <a class="dropdown-item" href="#" @click.prevent="setComment({edit: true})">Edit</a>
                <a class="dropdown-item" href="#" @click.prevent="deleteComment">Delete</a>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'

export default {
  name: 'TheComment',
  computed: {
    ...mapState({
      tab: state => state.hash.tab,
      queryid: state => state.hash.queryid
    }),
    ...mapGetters([
      'isBottomPanelOpen'
    ]),
    ...mapState('result', [
      'comment',
      'inputComment',
      'visibleComment'
    ]),
    inputCommentModel: {
      get () {
        return this.inputComment
      },
      set (val) {
        this.$store.commit('result/setInputComment', {data: val})
      }
    },
    inputCommentRows () {
      const rows = this.inputComment ? this.inputComment.split('\n').length : 0
      return (rows > 32) ? 32 : (rows > 8) ? rows : 8
    }
  },
  methods: {
    setComment (data) {
      this.$store.commit('result/setComment', data)
    },
    setVisibleComment (val) {
      this.$store.commit('result/setVisibleComment', {data: val})
    },
    postComment () {
      this.$store.dispatch('result/postComment')
    },
    postCommentLike () {
      this.$store.dispatch('result/postCommentLike')
    },
    deleteComment () {
      this.$store.dispatch('result/deleteComment')
    }
  }
}
</script>

<style scoped>
</style>
