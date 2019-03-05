<template>
  <div id="partition" class="modal fade">
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <strong>Partition list</strong> in
            <template v-if="isPresto">{{catalog}}.</template>{{schema}}.{{table}}
          </h5>
          <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        </div>
        <div class="modal-body p-4">
          <div class="card-deck">
            <div class="card" v-for="key in partitionKeys" :key="key">
              <div class="card-header">
                <i class="fa fa-fw mr-1" @click.prevent="toggleOrder(key)"
                   :class="{'fa-arrow-up': !orderDesc[key], 'fa-arrow-down': orderDesc[key]}"></i>
                {{key}}
                <span v-if="sortedPartitionValues[key] && sortedPartitionValues[key].length"
                      class="badge badge-default badge-pill">{{sortedPartitionValues[key].length}}</span>
                <div class="float-right form-filter">
                  <i class="fa fa-filter mt-1 mr-1" :class="{'text-primary': filterPartitionKeywords[key] && filterPartitionKeywords[key].length, 'text-muted': !filterPartitionKeywords[key] || !filterPartitionKeywords[key].length}"></i>
                  <input type="text" class="pull-right form-filter-input" v-model="filterPartitionKeywords[key]">
                </div>
              </div>
              <div v-if="loadingPartitions && key === nextPartitionKey" class="card-block">
                <i class="fa fa-fw fa-spinner fa-pulse mr-1"></i>Loading
              </div>
              <div v-else-if="!partitionValues[key] || !partitionValues[key].length" class="card-block">
                <span class="text-muted">No result</span>
              </div>
              <div v-else class="list-group list-group-flush">
                <template v-for="val in sortedPartitionValues[key]">
                  <div v-if="key === lastPartitionKey" :key="val" class="list-group-item">
                    <button class="btn btn-sm btn-secondary set" data-dismiss="modal"
                            @click="setPartitionToQuery(key, val)">
                      <i class="far fa-fw fa-keyboard mr-1"></i>Set
                    </button>
                    {{val.substring(0, 32)}}
                  </div>
                  <a v-else href="#" :key="val" @click.prevent="selectAndGetNextPartition(key, val)"
                     class="list-group-item" :class="{active: selectedPartitions[key] === val}">
                    <button class="btn btn-sm btn-secondary set" data-dismiss="modal"
                            @click="setPartitionToQuery(key, val)"><i
                      class="far fa-fw fa-keyboard mr-1"></i>Set
                    </button>
                    {{val.substring(0, 32)}}
                  </a>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'

export default {
  name: 'ModalPartition',
  data () {
    return {
      orderDesc: {},
      filterPartitionKeywords: {}
    }
  },
  computed: {
    ...mapState('treeview', [
      'catalog',
      'schema',
      'table',
      'partitionValues',
      'selectedPartitions',
      'loadingPartitions'
    ]),
    ...mapGetters([
      'isPresto',
      'isHive',
      'isSpark',
      'datasourceEngine'
    ]),
    ...mapState('editor', [
      'inputQuery'
    ]),
    ...mapGetters('treeview', [
      'partitionKeys',
      'partitionKeysTypes'
    ]),
    nextPartitionKey () {
      for (const k of this.partitionKeys) {
        if (!this.partitionValues[k]) {
          return k
        }
      }
    },
    lastPartitionKey () {
      return this.partitionKeys[this.partitionKeys.length - 1]
    },
    sortedPartitionValues () {
      const values = {}
      for (const k of this.partitionKeys) {
        if (!this.partitionValues[k]) {
          break
        }
        if (this.filterPartitionKeywords[k]) {
          values[k] = this.partitionValues[k].filter(v => v.includes(this.filterPartitionKeywords[k])).slice().sort()
        } else {
          values[k] = this.partitionValues[k].slice().sort()
        }
        if (this.orderDesc[k]) {
          values[k].reverse()
        }
      }
      return values
    },
    fullName () {
      return `${this.catalog}.${this.schema}.${this.table}`
    }
  },
  watch: {
    fullName () {
      this.$store.commit('treeview/setSelectedPartitions', {data: {}})
      this.orderDesc = {}
      this.filterPartitionKeywords = {}
    }
  },
  methods: {
    setSelectedPartitions (key, val) {
      const newVal = {}
      for (const k of this.partitionKeys) {
        if (k === key) {
          break
        }
        newVal[k] = this.selectedPartitions[k]
      }
      newVal[key] = val
      this.$store.commit('treeview/setSelectedPartitions', {data: newVal})
    },
    selectAndGetNextPartition (key, val) {
      this.setSelectedPartitions(key, val)
      this.$store.dispatch('treeview/getPartitions', {selectedPartitions: this.selectedPartitions})
    },
    setPartitionToQuery (key, val) {
      this.setSelectedPartitions(key, val)

      const where = []
      for (const [i, k] of this.partitionKeys.entries()) {
        if (this.partitionKeysTypes[i] === 'varchar' || this.partitionKeysTypes[i] === 'string') {
          where.push(`${k}='${this.selectedPartitions[k]}'`)
        } else {
          where.push(`${k}=${this.selectedPartitions[k]}`)
        }
        if (k === key) {
          break
        }
      }

      let from
      if (this.isPresto) {
        from = [this.catalog, this.schema, `"${this.table}"`]
      } else if (this.isHive) {
        from = [this.schema, `\`${this.table}\``]
      } else if (this.isSpark) {
        from = [this.schema, this.table]
      } else {
        throw new Error('not supported')
      }

      const query = `SELECT * FROM ${from.join('.')} WHERE ${where.join(' AND ')} LIMIT 100`

      this.$store.commit('editor/setInputQuery', {data: query})
    },
    toggleOrder (key) {
      this.orderDesc = Object.assign({}, this.orderDesc, {[key]: !this.orderDesc[key]})
    }
  }
}
</script>

<style scoped>
</style>
