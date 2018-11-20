<template>
  <div id="theme" class="modal fade">
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title"><strong class="mr-1">{{themes.length}}</strong>themes</h5>
          <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        </div>
        <div class="modal-body p-3">
          <table class="table table-bordered table-fixed table-hover mb-0">
            <tbody>
            <tr v-for="item in themes" :key="item">
              <td width="25%">
                <label class="form-check-label" :class="{'font-weight-bold': item === theme}">
                  <input type="radio" class="form-check-input mr-2" v-model="themeModel" :value="item"
                         @change="hideModal('theme')">{{item | humanize}}
                </label>
              </td>
              <td width="75%">
                <BaseAce code="SELECT * FROM catalog.schema.table LIMIT 100" :theme="item" :readonly="true"></BaseAce>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState} from 'vuex'
import util from '@/mixins/util'

export default {
  name: 'ModalTheme',
  mixins: [util],
  data () {
    return {
      themes: [
        'ambiance',
        'chaos',
        'chrome',
        'clouds',
        'clouds_midnight',
        'cobalt',
        'crimson_editor',
        'dawn',
        'eclipse',
        'github',
        'idle_fingers',
        'iplastic',
        'katzenmilch',
        'kuroir',
        'kr_theme',
        'merbivore',
        'merbivore_soft',
        'mono_industrial',
        'monokai',
        'pastel_on_dark',
        'solarized_dark',
        'solarized_light',
        'sqlserver',
        'terminal',
        'textmate',
        'tomorrow',
        'tomorrow_night',
        'tomorrow_night_blue',
        'tomorrow_night_bright',
        'tomorrow_night_eighties',
        'twilight',
        'vibrant_ink',
        'xcode'
      ]
    }
  },
  computed: {
    ...mapState({
      theme: state => state.settings.theme
    }),
    themeModel: {
      get () {
        return this.theme
      },
      set (val) {
        this.$store.commit('setSettings', {theme: val})
      }
    }
  }
}
</script>

<style scoped>
</style>
