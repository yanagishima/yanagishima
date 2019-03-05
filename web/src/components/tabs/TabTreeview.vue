<template>
  <div id="treeview">
    <div class="header row align-items-center pt-3">
      <div class="col-7">
        <div class="row align-items-end pt-3">
          <div class="col">
            <div v-if="datasources.length > 1" class="btn-group mr-2">
              <button type="button" class="btn btn-sm btn-secondary dropdown-toggle" data-toggle="dropdown">
                <strong>{{datasource}}</strong>
              </button>
              <div class="dropdown-menu">
                <a v-for="d in datasources" :key="d" class="dropdown-item" href="#"
                   @click.prevent="setDatasource(d)" :class="{active: d === datasource}">{{d}}</a>
              </div>
            </div>
            <div v-if="isPresto" class="btn-group">
              <button type="button" class="btn btn-sm btn-secondary dropdown-toggle" data-toggle="dropdown">
                <small class="text-muted mr-1">Catalog</small>
                <strong>{{catalog}}</strong></button>
              <div v-if="catalogs.length" class="dropdown-menu">
                <a v-for="c in catalogs" :key="c" class="dropdown-item" href="#"
                   @click.prevent="setCatalog(c)" :class="{active: c === catalog}">{{c}}</a>
              </div>
            </div>
          </div>
          <div class="col">
            <div v-if="isPresto" class="pull-right">
              <small><i class="fa fa-fw fa-circle table-base"></i>Base table</small>
              <small><i class="fa fa-fw fa-circle table-view"></i>View</small>
            </div>
          </div>
        </div>
      </div>
      <div class="col-5 text-right">
        <input v-if="isPresto" type="text" :placeholder="`Search by Table in ${catalog}`" v-model.lazy="tableQueryModel"
               class="form-control form-control-sm d-inline-block w-50" v-focus>
      </div>
    </div>

    <!-- main -->
    <div class="row">
      <div class="col-7">
        <template v-if="!tableQuery">
          <div class="row">
            <!-- schemata list -->
            <div class="col-6">
              <div class="card mb-3">
                <div class="card-header">
                  <strong>Schema</strong>
                  <span v-if="filteredSchemata.length" class="badge badge-default badge-pill">{{filteredSchemata.length}}</span>
                  <div class="pull-right form-filter">
                    <i class="fa fa-filter mt-1 mr-1"
                       :class="{'text-primary': filterSchema.length, 'text-muted': !filterSchema.length}"></i>
                    <input type="text" class="pull-right form-filter-input" v-model="filterSchemaModel">
                  </div>
                </div>
                <div class="list-group list-group-flush">
                  <template v-if="(isPresto && catalog || !isPresto) && filteredSchemata.length">
                    <a v-for="s in filteredSchemata" :key="s" href="#" class="list-group-item"
                       :class="{active: s === schema}" @click.prevent="setSchema(s)" :id="`schema-${s}`">
                      <BaseHighlight :sentence="s" :keyword="filterSchema"></BaseHighlight>
                    </a>
                  </template>
                  <template v-else>
                    <a href="#" class="list-group-item disabled">(N/A)</a>
                  </template>
                </div>
              </div>
            </div>

            <!-- tables list -->
            <div class="col-6">
              <div class="card mb-3">
                <div class="card-header">
                  <strong>Table</strong>
                  <span v-if="filteredTables.length" class="badge badge-default badge-pill">{{filteredTables.length}}</span>
                  <div class="pull-right form-filter">
                    <i class="fa fa-filter mt-1 mr-1"
                       :class="{'text-primary': filterTable.length, 'text-muted': !filterTable.length}"></i>
                    <input type="text" class="pull-right form-filter-input" v-model="filterTableModel">
                  </div>
                </div>
                <div class="list-group list-group-flush">
                  <template v-if="schema && filteredTables.length || isElasticsearch && filteredTables.length">
                    <a v-for="t in filteredTables" :key="t[0]" href="#" class="list-group-item"
                       :class="{active: t[0] === table, 'table-base': isPresto && t[1] !== 'VIEW', 'table-view': isPresto && t[1] === 'VIEW'}"
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
        </template>

        <!-- table name search -->
        <template v-else>
          <div class="card mb-3">
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
                  <a v-for="(item, i) in tableSearchResponse" :key="i" href="#" class="list-group-item ellipsis"
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

        <!-- Set/Run snippet buttons -->
        <div v-if="table" class="py-3 text-right">
          <label class="form-check-label mr-2">
            <input class="form-check-input mr-1" type="checkbox" v-model="isExpandColumns">Expand Columns
          </label>
          <select id="snippet" class="custom-select ace-font" style="width:60%;" v-model="snippetIndex">
            <template v-for="(item, i) in snippets" v-if="item.enable.includes(tableType)">
              <option :value="i" :key="i">{{item.label}}</option>
            </template>
          </select>
          <div class="btn-group">
            <button class="btn btn-primary" data-dismiss="modal" @click="setSnippet">
              <i class="far fa-fw fa-keyboard mr-1"></i>Set
            </button>
            <button class="btn btn-primary" data-dismiss="modal" @click="runSnippet">
              <i class="fa fa-fw fa-play mr-1"></i><strong>Run</strong>
            </button>
          </div>
        </div>
      </div>

      <!-- Columns table -->
      <div class="col-5">
        <template v-if="table && columns.length">
          <template v-if="isMetadataService">
            <template v-if="note">
              <div><pre><BaseAutoLink :text="note.escapeHTML()"></BaseAutoLink></pre></div>
            </template>
            <div id="columns">
              <table class="table table-striped table-hover table-fixed mb-0">
                <thead>
                <tr>
                  <th width="30%">Column<span
                    class="badge badge-default badge-pill ml-1">{{columns.length}}</span>
                  </th>
                  <th width="10%">Type</th>
                  <th width="10%">Extra</th>
                  <th width="10%">Comment</th>
                  <th width="30%">Note</th>
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
                  <td>{{column[2]}}</td>
                  <td>{{column[3]}}</td>
                  <td>{{column[4]}}</td>
                </tr>
                </tbody>
              </table>
            </div>
            <template v-if="meta">
              <div><pre>{{meta}}</pre></div>
            </template>
          </template>
          <template v-else>
            <div id="columns">
              <table class="table table-striped table-hover table-fixed mb-0">
                <thead>
                <tr>
                  <th width="40%">Column<span
                    class="badge badge-default badge-pill ml-1">{{columns.length}}</span>
                  </th>
                  <th width="20%">Type</th>
                  <th width="20%">Extra</th>
                  <th width="20%">Comment</th>
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
                  <td>{{column[2]}}</td>
                  <td>{{column[3]}}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </template>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import toastr from 'toastr'
import {mapState, mapGetters} from 'vuex'

export default {
  name: 'TabTreeview',
  data () {
    return {
      isExpandColumns: false,
      snippetIndex: 0,
      scrollTo: {}
    }
  },
  computed: {
    ...mapState({
      datasources: state => state.datasources,
      datasource: state => state.hash.datasource,
      initialTable: state => state.hash.table
    }),
    ...mapState('treeview', [
      'catalogs',
      'schemata',
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
      'isElasticsearch',
      'datasourceEngine',
      'isMetadataService'
    ]),
    ...mapGetters('treeview', [
      'dateColumn',
      'otherColumns',
      'partitionKeys'
    ]),
    filteredSchemata () {
      return this.schemata.filter(s => s.includes(this.filterSchema))
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
      if (this.isPresto) {
        const snippets = [
          {
            label: 'SHOW PRESTO VIEW DDL',
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
          const yesterday = Date.create().addDays(-1).format('{yyyy}{MM}{dd}')
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
          const yesterday = Date.create().addDays(-1).format('{yyyy}{MM}{dd}')
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
          const yesterday = Date.create().addDays(-1).format('{yyyy}{MM}{dd}')
          defaultSnippet = {
            label: `SELECT * FROM ... WHERE ${this.dateColumn}='${yesterday}' LIMIT 100`,
            sql: 'SELECT {columns} FROM {schema}.{table} WHERE {column_date}=\'{yesterday}\' LIMIT 100',
            enable: ['BASE TABLE', 'VIEW']
          }
        }
        snippets.unshift(defaultSnippet)
        return snippets
      }

      if (this.isElasticsearch) {
        const snippets = [
          {
            label: 'SHOW CREATE TABLE ...',
            sql: 'SHOW CREATE TABLE "{table}"',
            enable: ['BASE TABLE']
          },
          {
            label: 'DESCRIBE ...',
            sql: 'DESCRIBE "{table}"',
            enable: ['BASE TABLE', 'VIEW']
          }
        ]
        const defaultSnippet = {
          label: 'SELECT * FROM ... LIMIT 100',
          sql: 'SELECT {columns} FROM "{table}" LIMIT 100',
          enable: ['BASE TABLE', 'VIEW']
        }
        snippets.unshift(defaultSnippet)
        return snippets
      }
    }
  },
  watch: {
    datasourceEngine () {
      this.$store.commit('setHashItem', {table: ''})
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
            }
          })
      }
    },
    table (val) {
      if (val) {
        this.$store.dispatch('treeview/getColumns')
      }
    },
    tableQuery () {
      this.searchTable()
    }
  },
  created () {
    if (this.initialTable) {
      if (this.isPresto || this.isHive || this.isSpark) {
        this.$store.commit('treeview/init')
        const t = this.initialTable.split('.')
        if (this.isPresto) {
          this.scrollTo = {catalog: t[0], schema: t[1], table: t[2]}
        } else {
          this.scrollTo = {schema: t[0], table: t[1]}
        }
      }
    }

    this.$store.dispatch('treeview/getRoot')
  },
  beforeDestroy () {
    this.$store.commit('setHashItem', {table: ''})
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
      if (this.isPresto) {
        return [this.catalog, this.schema, table].join('.')
      } else if (this.isHive || this.isSpark) {
        return [this.schema, table].join('.')
      } else if (this.isElasticsearch) {
        return table
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
          columns: this.isExpandColumns ? this.otherColumns : '*',
          yesterday: Date.create().addDays(-1).format('{yyyy}{MM}{dd}')
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
    }
  }
}
</script>

<style scoped>
.pull-right {
  float: right;
}
</style>
