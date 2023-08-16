<template>
  <div id="partition" class="modal fade">
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <strong>Partition list</strong> in
            <template v-if="isPresto || isTrino">{{catalog}}.</template>{{schema}}.{{table}}
          </h5>
          <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        </div>
        <div class="modal-body p-4">
          <div class="card-deck">
            <div class="card ml-0" v-for="key in partitionKeys" :key="key">
              <div class="card-header d-flex justify-content-between align-items-center p-2">
                <div class="col-7 d-flex align-items-center px-0">
                  <span role="button" @click.prevent="toggleOrder(key)" style="cursor: pointer"><i class="fa fa-fw" :class="{'fa-arrow-up': !orderDesc[key], 'fa-arrow-down': orderDesc[key]}"></i>{{key}}</span><span v-if="sortedPartitionValues[key] && sortedPartitionValues[key].length"
                  class="badge badge-secondary badge-pill ml-1">{{sortedPartitionValues[key].length}}</span>
                </div>
                <div class="col-5 px-0 form-filter d-flex align-items-center">
                  <small><i class="fa fa-filter mr-1" :class="{'text-primary': filterPartitionKeywords[key] && filterPartitionKeywords[key].length, 'text-muted': !filterPartitionKeywords[key] || !filterPartitionKeywords[key].length}"></i></small>
                  <input type="text" class="form-filter-input w-100" v-model="filterPartitionKeywords[key]">
                </div>
              </div>
              <div v-if="loadingPartitions && key === nextPartitionKey" class="card-body">
                <i class="fa fa-fw fa-spinner fa-pulse mr-1"></i>Loading
              </div>
              <div v-else-if="!partitionValues[key] || !partitionValues[key].length" class="card-body">
                <span class="text-muted">No result</span>
              </div>
              <div v-else class="list-group list-group-flush">
                <template v-for="val in sortedPartitionValues[key]">
                  <div v-if="key === lastPartitionKey" :key="val" class="list-group-item">
                    <button class="btn btn-sm btn-secondary set" data-dismiss="modal"
                            @click="setPartitionToQuery(key, val)">
                      <i class="far fa-fw fa-keyboard mr-1"></i>Set
                    </button>
                    {{val.substring(0, 50)}}
                  </div>
                  <a v-else href="#" :key="val" @click.prevent="selectAndGetNextPartition(key, val)"
                     class="list-group-item text-truncate" :class="{active: selectedPartitions[key] === val}" :title="val">
                    <button class="btn btn-sm btn-secondary set" data-dismiss="modal"
                            @click="setPartitionToQuery(key, val)"><i
                      class="far fa-fw fa-keyboard mr-1"></i>Set
                    </button>
                    {{val.substring(0, 50)}}
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
      'isTrino',
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
        } else if (this.partitionKeysTypes[i] === 'date') {
          where.push(`${k}=DATE '${this.selectedPartitions[k]}'`)
        } else {
          where.push(`${k}=${this.selectedPartitions[k]}`)
        }
        if (k === key) {
          break
        }
      }

      let from
      if (this.isPresto || this.isTrino) {
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
