<template>
  <div id="treeview">
    <div class="header col-12 pt-4 pb-0">
      <div class="d-flex mb-4">
        <div v-if="datasources.length > 1" class="btn-group mr-1">
            <button type="button" class="btn btn-sm btn-secondary dropdown-toggle" data-toggle="dropdown">
              <strong>{{datasource}}</strong>
            </button>
            <div class="dropdown-menu">
              <a v-for="d in datasources" :key="d" class="dropdown-item" href="#"
                  @click.prevent="setDatasource(d)" :class="{active: d === datasource}">{{d}}</a>
            </div>
          </div>
          <div v-if="isPresto || isTrino" class="btn-group mr-1">
            <button type="button" class="btn btn-sm btn-secondary dropdown-toggle" data-toggle="dropdown">
              <small class="text-muted mr-1">Catalog</small>
              <strong>{{catalog}}</strong></button>
            <div v-if="catalogs.length" class="dropdown-menu">
              <a v-for="c in catalogs" :key="c" class="dropdown-item" href="#"
                  @click.prevent="setCatalog(c)" :class="{active: c === catalog}">{{c}}</a>
            </div>
          </div>
          <div class="ml-auto align-self-end">
            <input v-if="isPresto || isTrino" type="text" :placeholder="`Search by Table in ${catalog}`" v-model.lazy="tableQueryModel"
               class="form-control form-control-sm" v-focus>
          </div>
      </div>
      <div class="col-6 text-right">
        <span v-if="isPresto || isTrino">
          <small><i class="fa fa-fw fa-circle table-base"></i>table</small>
          <small><i class="fa fa-fw fa-circle table-view"></i>view</small>
        </span>
      </div>
    </div>

    <!-- main -->
    <div class="row">
      <div class="col-6">
        <template v-if="!tableQuery">
          <div class="row">
            <!-- schemata list -->
            <div class="col-6">
              <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                  <p class="col-6 col-md-8 px-0 mb-0">
                    <strong>Schemas </strong><span v-if="filteredSchemata.length" class="badge badge-secondary badge-pill">{{filteredSchemata.length}}</span>
                  </p>
                  <div class="col-6 col-md-4 px-0 form-filter d-flex">
                    <small><i class="fa fa-filter mt-1 mr-1" :class="{'text-primary': filterSchema.length, 'text-muted': !filterSchema.length}"></i></small>
                    <input type="text" class="pull-right form-filter-input" v-model="filterSchemaModel">
                  </div>
                </div>
                <div class="list-group list-group-flush">
                  <template v-if="((isPresto || isTrino) && catalog || (!isPresto && !isTrino)) && filteredSchemata.length">
                    <transition-group name="schema-list" tag="div">
                      <a v-for="s in filteredSchemata" :key="s.name" href="#" class="list-group-item pr-0"
                        :class="{active: s.name === schema, starring: !!starTargetSchema, target: s.name === starTargetSchema}"
                        @click.prevent="setSchema(s.name)" @transitionend="s.name === starTargetSchema && finishStarring()" :id="`schema-${s.name}`">
                        <i class="fas fa-star mr-1" :class="s.star ? 'text-warning' : 'text-muted'" :style="!s.star && 'opacity: 0.4'" @click.stop="toggleStarSchema(s)"></i>
                        <BaseHighlight :sentence="s.name" :keyword="filterSchema"></BaseHighlight>
                      </a>
                    </transition-group>
                  </template>
                  <template v-else>
                    <a href="#" class="list-group-item disabled">(N/A)</a>
                  </template>
                </div>
              </div>
            </div>

            <!-- tables list -->
            <div class="col-6 px-0">
              <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                  <p class="col-6 col-md-8 px-0 mb-0">
                    <strong>Tables</strong> <span v-if="filteredTables.length" class="badge badge-secondary badge-pill">{{filteredTables.length}}</span>
                  </p>
                  <div class="col-6 col-md-4 px-0 form-filter d-flex">
                    <small><i class="fa fa-filter mt-1 mr-1" :class="{'text-primary': filterTable.length, 'text-muted': !filterTable.length}"></i></small>
                    <input type="text" class="pull-right form-filter-input" v-model="filterTableModel">
                  </div>
                </div>
                <div class="list-group list-group-flush">
                  <template v-if="schema && filteredTables.length">
                    <a v-for="t in filteredTables" :key="t[0]" href="#" class="list-group-item"
                       :class="{active: t[0] === table, 'table-base': (isPresto || isTrino) && t[1] !== 'VIEW', 'table-view': (isPresto || isTrino) && t[1] === 'VIEW'}"
                       @click.prevent="setTable(t)" :id="`table-${t[0]}`">
                      <button class="btn btn-sm btn-secondary clip px-2" v-clipboard="fullName(t[0])" @click.stop.prevent="">
                        <i class="fa fa-fw fa-clipboard"></i>
                      </button>
                      <BaseHighlight :sentence="t[0]" :keyword="filterTable"></BaseHighlight>
                    </a>
                  </template>
                  <template v-else>
                    <a href="#" class="list-group-item disabled">(N/A)</a>
                  </template>
                </div>
              </div>
            </div>
          </div>
          <!-- Set/Run snippet buttons -->
          <div v-if="table" class="mt-3 py-3 text-right">
            <select id="snippet" class="custom-select custom-select-sm ace-font mb-2" v-model="snippetIndex">
              <template v-for="(item, i) in snippets">
                <option v-if="item.enable.includes(tableType)" :value="i" :key="i">{{item.label}}</option>
              </template>
            </select>
            <label class="form-check-label mr-2">
              <input class="form-check-input mr-1" type="checkbox" v-model="isExpandColumns"><small>Expand Columns</small>
            </label>
            <div class="btn-group">
              <button class="btn btn-sm btn-primary" data-dismiss="modal" @click="setSnippet">
                <small><i class="far fa-fw fa-keyboard mr-1"></i>Set</small>
              </button>
              <button class="btn btn-sm btn-primary" data-dismiss="modal" @click="runSnippet">
                <small><i class="fa fa-fw fa-play mr-1"></i><strong>Run</strong></small>
              </button>
            </div>
          </div>
        </template>

        <!-- table name search -->
        <template v-else>
          <div class="card">
            <div class="card-header">
              <a href="#" class="text-muted mr-2" @click.prevent="clearTableQuery"><i class="fa fa-times"></i></a>
              "<strong>{{tableQuery}}</strong>" in {{catalog}}
              <template v-if="tableSearchResponse.length">
                <strong class="ml-2">{{tableSearchResponse.length}}</strong>
                results
              </template>
            </div>
            <div class="list-group list-group-flush">
              <template v-if="loadingTableSearch">
                <div class="list-group-item">
                  <i class="fa fa-fw fa-spinner fa-pulse mr-1"></i>Searching
                </div>
              </template>
              <template v-else>
                <template v-if="tableSearchResponse">
                  <a v-for="(item, i) in tableSearchResponse" :key="i" href="#" class="list-group-item text-truncate"
                     @click.prevent="setSearchedTable(item)"
                     :class="{active: item[0] === catalog && item[1] === schema && item[2] === table, 'table-view': item[3] === 'VIEW'}">
                    <button class="btn btn-sm btn-secondary clip"
                            v-clipboard="[item[0], item[1], item[2]].join('.')"><i
                      class="fa fa-fw fa-clipboard"></i></button>
                    <BaseHighlight :sentence="item[0]" :keyword="tableQuery"></BaseHighlight>
                    <span class="mx-1">/</span>
                    <BaseHighlight :sentence="item[1]" :keyword="tableQuery"></BaseHighlight>
                    <span class="mx-1">/</span>
                    <BaseHighlight :sentence="item[2]" :keyword="tableQuery"></BaseHighlight>
                  </a>
                </template>
                <template v-else>
                  <a href="#" class="list-group-item disabled">(Not Found)</a>
                </template>
              </template>
            </div>
          </div>
        </template>
      </div>

      <!-- Columns table -->
      <div class="col-6">
        <template v-if="table && columns.length">
            <div id="columns">
              <table class="table table-striped table-hover table-fixed mb-0">
                <thead>
                <tr>
                  <th>
                    <span class="d-inline-flex align-items-center">
                      Columns
                      <span class="badge badge-secondary badge-pill ml-1">{{columns.length}}</span>
                    </span>
                  </th>
                  <th width="20%">Type</th>
                  <th>Comment</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="column in columns" :key="column[0]">
                  <td :title="column[0]">
                    <a v-if="column[0] === partitionKeys[0]" href="#partition" data-toggle="modal"
                      @click.prevent="getPartitions">
                      {{column[0]}}
                    </a>
                    <template v-else>
                      {{column[0]}}
                    </template>
                  </td>
                  <td class="text-muted">{{column[1]}}</td>
                  <td>{{column[3]}}</td>
                </tr>
                </tbody>
              </table>
            </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import toastr from 'toastr'
import {mapState, mapGetters} from 'vuex'
import util from '@/mixins/util'
import $ from 'jquery'

export default {
  name: 'TabTreeview',
  mixins: [util],
  data () {
    return {
      isExpandColumns: false,
      snippetIndex: 0,
      scrollTo: {},
      starTargetSchema: null
    }
  },
  computed: {
    ...mapState({
      datasources: state => state.datasources,
      datasource: state => state.hash.datasource,
      initialTable: state => state.hash.table,
      where: state => state.hash.where
    }),
    ...mapState('treeview', [
      'catalogs',
      'schemata',
      'starredSchemata',
      'tables',
      'catalog',
      'schema',
      'table',
      'note',
      'meta',
      'tableType',
      'columns',
      'tableQuery',
      'filterSchema',
      'filterTable',
      'loadingTableSearch',
      'tableSearchResponse'
    ]),
    ...mapGetters([
      'isPresto',
      'isHive',
      'isSpark',
      'isTrino',
      'datasourceEngine',
      'isMetadataService',
      'datetimePartitionFormat'
    ]),
    ...mapGetters('treeview', [
      'dateColumn',
      'allColumns',
      'partitionKeys'
    ]),
    schemaObjects () {
      return this.schemata
        .map(s => ({ name: s, star: this.starredSchemata.find(ss => ss.schema === s) }))
        .sortBy([s => s.star ? 0 : 1, 'name'])
    },
    filteredSchemata () {
      return this.schemaObjects.filter(s => s.name.includes(this.filterSchema))
    },
    filteredTables () {
      return this.tables.filter(s => s[0].includes(this.filterTable))
    },
    tableQueryModel: {
      get () {
        return this.tableQuery
      },
      set (val) {
        this.$store.commit('treeview/setTableQuery', {data: val})
      }
    },
    filterSchemaModel: {
      get () {
        return this.filterSchema
      },
      set (val) {
        this.$store.commit('treeview/setFilterSchema', {data: val})
      }
    },
    filterTableModel: {
      get () {
        return this.filterTable
      },
      set (val) {
        this.$store.commit('treeview/setFilterTable', {data: val})
      }
    },
    snippets () {
      if (this.isPresto || this.isTrino) {
        const snippets = [
          {
            label: 'SHOW VIEW DDL',
            sql: "SELECT VIEW_DEFINITION FROM {catalog}.INFORMATION_SCHEMA.VIEWS WHERE table_catalog='{catalog}' AND table_schema='{schema}' AND table_name='{table}'",
            enable: ['VIEW']
          },
          {
            label: 'SHOW CREATE TABLE ...',
            sql: 'SHOW CREATE TABLE {catalog}.{schema}."{table}"',
            enable: ['BASE TABLE']
          },
          {
            label: 'DESCRIBE ...',
            sql: 'DESCRIBE {catalog}.{schema}."{table}"',
            enable: ['BASE TABLE', 'VIEW']
          },
          {
            label: 'SHOW STATS ...',
            sql: 'SHOW STATS FOR {catalog}.{schema}."{table}"',
            enable: ['BASE TABLE']
          }
        ]

        let defaultSnippet = {
          label: 'SELECT * FROM ... LIMIT 100',
          sql: 'SELECT {columns} FROM {catalog}.{schema}."{table}" LIMIT 100',
          enable: ['BASE TABLE', 'VIEW']
        }

        if (this.dateColumn) {
          const yesterday = Date.create().addDays(-1).format(this.datetimePartitionFormat)
          defaultSnippet = {
            label: `SELECT * FROM ... WHERE ${this.dateColumn}='${yesterday}' LIMIT 100`,
            sql: `SELECT {columns} FROM {catalog}.{schema}."{table}" WHERE {column_date}='{yesterday}' LIMIT 100`,
            enable: ['BASE TABLE', 'VIEW']
          }
        }
        snippets.unshift(defaultSnippet)
        return snippets
      }

      if (this.isHive) {
        const snippets = [
          {
            label: 'SHOW CREATE TABLE ...',
            sql: 'SHOW CREATE TABLE {schema}.`{table}`',
            enable: ['BASE TABLE']
          },
          {
            label: 'DESCRIBE ...',
            sql: 'DESCRIBE {schema}.`{table}`',
            enable: ['BASE TABLE', 'VIEW']
          }
        ]

        let defaultSnippet = {
          label: 'SELECT * FROM ... LIMIT 100',
          sql: 'SELECT {columns} FROM {schema}.`{table}` LIMIT 100',
          enable: ['BASE TABLE', 'VIEW']
        }

        if (this.dateColumn) {
          const yesterday = Date.create().addDays(-1).format(this.datetimePartitionFormat)
          defaultSnippet = {
            label: `SELECT * FROM ... WHERE ${this.dateColumn}='${yesterday}' LIMIT 100`,
            sql: 'SELECT {columns} FROM {schema}.`{table}` WHERE {column_date}=\'{yesterday}\' LIMIT 100',
            enable: ['BASE TABLE', 'VIEW']
          }
        }
        snippets.unshift(defaultSnippet)
        return snippets
      }

      if (this.isSpark) {
        const snippets = [
          {
            label: 'SHOW CREATE TABLE ...',
            sql: 'SHOW CREATE TABLE {schema}.{table}',
            enable: ['BASE TABLE']
          },
          {
            label: 'DESCRIBE ...',
            sql: 'DESCRIBE {schema}.{table}',
            enable: ['BASE TABLE', 'VIEW']
          }
        ]

        let defaultSnippet = {
          label: 'SELECT * FROM ... LIMIT 100',
          sql: 'SELECT {columns} FROM {schema}.{table} LIMIT 100',
          enable: ['BASE TABLE', 'VIEW']
        }

        if (this.dateColumn) {
          const yesterday = Date.create().addDays(-1).format(this.datetimePartitionFormat)
          defaultSnippet = {
            label: `SELECT * FROM ... WHERE ${this.dateColumn}='${yesterday}' LIMIT 100`,
            sql: 'SELECT {columns} FROM {schema}.{table} WHERE {column_date}=\'{yesterday}\' LIMIT 100',
            enable: ['BASE TABLE', 'VIEW']
          }
        }
        snippets.unshift(defaultSnippet)
        return snippets
      }
    }
  },
  watch: {
    datasourceEngine () {
      this.$store.commit('setHashItem', {table: '', where: ''})
      this.$store.dispatch('treeview/getRoot')
    },
    catalog (val) {
      if (val) {
        if (this.scrollTo.catalog && this.scrollTo.catalog !== val) {
          this.setCatalog(this.scrollTo.catalog)
          this.scrollTo.catalog = null
          return
        }
        this.$store.dispatch('treeview/getSchemata')
        this.$store.dispatch('editor/getCompleteWords')
      }
      this.$store.commit('treeview/setTableQuery', {data: ''})
    },
    schema (val) {
      if (val) {
        if (this.scrollTo.schema && this.scrollTo.schema !== val) {
          if (!this.schemata.includes(this.scrollTo.schema)) {
            toastr.error(`schema not found: ${this.scrollTo.schema}`)
            this.scrollTo = {}
            return
          }
          this.setSchema(this.scrollTo.schema)
          return
        }
        this.$store.dispatch('treeview/getTables')
          .then(() => {
            if (this.scrollTo.schema) {
              document.getElementById(`schema-${this.scrollTo.schema}`).scrollIntoView()
              window.scrollTo(0, 0)
              this.scrollTo.schema = null
            }
            if (this.scrollTo.table) {
              const table = this.tables.find(t => t[0] === this.scrollTo.table)
              if (!table) {
                toastr.error(`table not found: ${this.scrollTo.table}`)
                this.scrollTo = {}
                return
              }
              this.setTable(table)
              document.getElementById(`table-${table[0]}`).scrollIntoView()
              window.scrollTo(0, 0)
              this.scrollTo.table = null
              this.setUrlSnippet()
            }
          })
      }
    },
    table (val) {
      if (val) {
        this.$store.dispatch('treeview/getColumns')
        $('#columnsTab li:first-child a').tab('show')
      }
    },
    tableQuery () {
      this.searchTable()
    }
  },
  created () {
    if (this.initialTable) {
      if (this.isPresto || this.isTrino || this.isHive || this.isSpark) {
        this.$store.commit('treeview/init')
        const t = this.initialTable.split('.')
        if (this.isPresto || this.isTrino) {
          this.scrollTo = {catalog: t[0], schema: t[1], table: t[2]}
        } else {
          this.scrollTo = {schema: t[0], table: t[1]}
        }
      }
    }

    this.$store.dispatch('treeview/getRoot')
  },
  beforeDestroy () {
    this.$store.commit('setHashItem', {table: '', where: ''})
  },
  methods: {
    setDatasource (datasource) {
      this.$store.commit('setHashItem', {datasource})
    },
    setCatalog (catalog) {
      this.$store.commit('treeview/setCatalog', {data: catalog})
      this.$store.commit('treeview/setSchema', {data: ''})
      this.$store.commit('treeview/setTable', {data: ['', '']})
    },
    setSchema (schema) {
      this.$store.commit('treeview/setSchema', {data: schema})
      this.$store.commit('treeview/setTable', {data: ['', '']})
    },
    setTable (table) {
      this.$store.commit('treeview/setTable', {data: table})
    },
    getPartitions () {
      this.$store.dispatch('treeview/getPartitions')
    },
    fullName (table) {
      if (this.isPresto || this.isTrino) {
        return [this.catalog, this.schema, table].join('.')
      } else if (this.isHive || this.isSpark) {
        return [this.schema, table].join('.')
      } else {
        throw new Error('not supported')
      }
    },
    setSnippet () {
      let snippet = this.snippets[this.snippetIndex].sql
      this.$store.commit('editor/setInputQuery', {
        data: snippet.format({
          catalog: this.catalog,
          schema: this.schema,
          table: this.table,
          column_date: this.dateColumn,
          columns: this.isExpandColumns ? this.allColumns : '*',
          yesterday: Date.create().addDays(-1).format(this.datetimePartitionFormat)
        })
      })
    },
    runSnippet () {
      this.setSnippet()
      this.$store.dispatch('result/runQuery')
      this.$store.commit('setHashItem', {tab: 'result'})
    },
    searchTable () {
      this.$store.dispatch('treeview/searchTable')
    },
    setSearchedTable (item) {
      const [catalog, schema, table, tableType] = item
      this.$store.commit('treeview/setCatalog', {data: catalog})
      this.$store.commit('treeview/setSchema', {data: schema})
      this.$store.commit('treeview/setTable', {data: [table, tableType]})
    },
    clearTableQuery () {
      this.tableQueryModel = ''
      this.$store.commit('treeview/setTableSearchResponse', {data: []})
      this.$store.commit('treeview/setTable', {data: ['', '']})
    },
    setUrlSnippet () {
      if (this.table && this.where) {
        if ((this.isPresto || this.isTrino) && this.catalog && this.schema) {
          const snippet = `SELECT * FROM ${this.catalog}.${this.schema}."${this.table}" WHERE ${this.where} LIMIT 100`
          this.$store.commit('editor/setInputQuery', {data: snippet})
          return
        }
        if (this.isHive && this.schema) {
          const snippet = `SELECT * FROM ${this.schema}.\`${this.table}\` WHERE ${this.where} LIMIT 100`
          this.$store.commit('editor/setInputQuery', {data: snippet})
          return
        }
        if (this.isSpark && this.schema) {
          const snippet = `SELECT * FROM ${this.schema}.${this.table} WHERE ${this.where} LIMIT 100`
          this.$store.commit('editor/setInputQuery', {data: snippet})
        }
      }
    },
    async toggleStarSchema (schemaObj) {
      const {name, star} = schemaObj
      this.starTargetSchema = name
      const beforeIndex = this.filteredSchemata.findIndex(s => s.name === name)
      if (star) {
        await this.$store.dispatch('treeview/deleteStarredSchema', {id: star.starred_schema_id})
      } else {
        await this.$store.dispatch('treeview/postStarredSchema', {schema: name})
      }
      const afterIndex = this.filteredSchemata.findIndex(s => s.name === name)
      if (beforeIndex === afterIndex) {
        this.finishStarring()
      }
    },
    finishStarring () {
      if (this.starTargetSchema) {
        this.starTargetSchema = null
      }
    }
  }
}
</script>

<style scoped>
.pull-right {
  float: right;
}
.schema-list-move.starring {
  transition: transform .2s ease-in-out .1s;
}
.schema-list-move.starring.target {
  z-index: 100;
}
.right-table {
  position: relative;
  top: -36px;
}
.tab-content pre {
  white-space: pre-wrap;
}
</style>
