require('./scss/bootstrap.scss');

Sugar.extend();
Vue.use(require('vue-clipboards'));
Vue.use(require('vue-charts'));
Vue.use(require('vue-scrollto'));
Vue.directive('focus', {
	inserted: function(el) {
		el.focus()
	}
});
var favicon = new Favico({});

jQuery(document).ready(function($) {
	$('body').tooltip({
		selector: '[data-toggle="tooltip"]',
		html: true
	}).popover({
		selector: '[data-toggle="popover"]',
		html: true
	});

	var vm = new Vue({
		el: '#page',
		data: function() {
			return {
				// general
				sitename: yanagishima.sitename,
				version: yanagishima.version,
				domain: yanagishima.domain,
				contact: yanagishima.contact,
				apis: yanagishima.apis,
				links: yanagishima.links,
				columnDate_names: yanagishima.columnDate_names,
				themes: yanagishima.themes,
				hiddenQuery_prefix: yanagishima.hiddenQuery_prefix,
				complate_words: [],
				hashKeys: [
					// key, Required
					['datasource', true],
					['engine', true],
					['tab', true],
					['queryid', false],
					['bookmark_id', false],
					['chart', false],
					['line', false],
				],
				loading: {
					qlist: false,
					result: false,
					history: false,
					bookmark: false,
					table: false,
					share: false,
					partition: false,
				},
				error: {
					qlist: false,
					result: false,
					history: false,
					bookmark: false,
					table: false,
					share: false,
				},
				response: {
					qlist: [],
					result: '',
					history: [],
					historyTotal: 0,
					historyHit: 0,
					bookmark: [],
					table: [],
					share: '',
				},
				tabs: [{
						id: 'qlist',
						icon: 'tasks',
						name: 'Query List',
					},
					{
						id: 'history',
						icon: 'history',
						name: 'History',
					},
					{
						id: 'bookmark',
						icon: 'star',
						name: 'Bookmark',
					},
					{
						id: 'result',
						icon: 'table',
						name: 'Result',
					},
					{
						id: 'treeview',
						icon: 'sitemap',
						name: 'Treeview',
					},
				],
				tab: '',
				now: '',
				line: '',
				timer: '',
				is_modal: false,
				theme: localStorage.getItem('theme') || 'chrome',
				is_wide: Number(localStorage.getItem('wide')) || 0,
				is_panel: Number(localStorage.getItem('panel')) || 0,

				// setting
				setting: Number(localStorage.getItem('setting')) || 0,
				fixedHeader: Number(localStorage.getItem('fixedHeader')) || 0,
				desktopNotification: Number(localStorage.getItem('desktopNotification')) || 0,
				rememberDatasource: Number(localStorage.getItem('rememberDatasource')) || 0,
				rememberEngine: Number(localStorage.getItem('rememberEngine')) || 0,
				minlines: [
					2,
					4,
					8,
					16,
				],
				explains: {
					'explain': {
						engines: ['presto', 'hive'],
						sql: 'EXPLAIN {0}',
					},
					'explain distributed': {
						engines: ['presto'],
						sql: 'EXPLAIN (TYPE DISTRIBUTED) {0}',
					},
					'explain analyze': {
						engines: ['presto'],
						sql: 'EXPLAIN ANALYZE {0}',
					},
				},

				// auth
				auth_user: null,
				auth_pass: null,
				auth_done: false,

				// treeview
				datasources: [],
				auths: {},
				engines: {},
				catalogs: [],
				schemata: [],
				tables: [],
				columns: [],
				partition_keys: [],
				partition_vals: [],
				partition_lists: [],
				partition_index: 0,
				table_q: '',
				datasource: '',
				engine: '',
				catalog: '',
				schema: '',
				table: '',
				table_type: '',
				column_date: '',
				val: '',
				cols: [],
				col_date: '',
				snippet: 0,
				filter_schema: '',
				filter_table: '',
				is_expandColumns: false,

				// query editor
				input_query: '',
				query: '',
				queryString: '',
				queryString_collapse: false,
				gotoline: 0,
				errorline: -1,
				errortext: '',
				focus: 1,
				minline: Number(localStorage.getItem('minline')) || 4,

				// qlist
				is_autoQlist: Number(localStorage.getItem('autoQlist')),
				refresh_period: 1,
				filter_user: '',
				is_openQuery: Number(localStorage.getItem('openQuery')) || false,
				is_adminMode: Number(localStorage.getItem('adminMode')) || false,
				is_superadminMode: false,

				// bookmark/history
				bookmark_id: '',
				bookmarks: [],
				bookmark_addId: false,
				history_limit: 100,
				histories: [],
				filter_history: '',

				// queryid
				queryid: '',
				running_queryid: '',
				running_progress: -1,
				running_time: '',
				running_queries: 0,
				history_queryid: '',
				history_result: [],

				// error page
				status_code: '200',

				// share
				short_url: '',
				publish_id: '',

				// chart
				enable_chart: true,
				chart: 0,
				chart_types: {
					1: {
						name: 'Line Chart',
						type: 'LineChart',
						minRows: 2,
						option: {}
					},
					2: {
						name: 'Stacked Area Chart',
						type: 'AreaChart',
						minRows: 3,
						option: {
							isStacked: true
						}
					},
					3: {
						name: 'Full-Stacked Area Chart',
						type: 'AreaChart',
						minRows: 3,
						option: {
							isStacked: 'relative',
						}
					},
					4: {
						name: 'Column Chart',
						type: 'ColumnChart',
						minRows: 2,
						option: {
							isStacked: false
						}
					},
					5: {
						name: 'Stacked Column Chart',
						type: 'ColumnChart',
						minRows: 3,
						option: {
							isStacked: true
						}
					},
				},
				chart_columns: [],
				chart_rows: [],
				chart_options: {
					width: '100%',
					height: 360,
					fontName: 'Droid Sans',
					fontSize: 12,
					chartArea: {
						width: '80%',
					},
					legend: {
						position: 'bottom',
						textStyle: {
							fontName: 'Droid Sans',
							fontSize: 12
						}
					},
					tooltip: {
						textStyle: {
							fontName: 'Droid Sans',
							fontSize: 12
						}
					},
					vAxis: {
						minValue: 0,
						gridlines: {
							color: '#eee'
						},
						titleTextStyle: {
							italic: false
						}
					},
					hAxis: {
						gridlines: {
							color: '#eee'
						},
						titleTextStyle: {
							italic: false
						}
					},
				},

				// expand
				is_pretty: false,
				is_localstorage: (localStorage.getItem('localstorage') == null ) ? true : Number(localStorage.getItem('localstorage')),

				// demo
				demo: {
					variables: 'SELECT ${x} FROM ${y} LIMIT ${z}',
					chart: 'SELECT * FROM (VALUES(2013,1000,400),(2014,1170,460),(2015,660,1120),(2016,1030,540),(2017,1220,890)) AS t (year,A,B)',
				},
			}
		},
		created: function() {
			var self = this;

			// Share mode
			if (self.is_share) {
				self.viewShare();
				return false;
			} else if (self.is_error) {
				self.viewError();
				return false;
			} else {
				// AIP
				!location.hash && self.fromDataHash(false);
				$(window).on('hashchange', function() {
					self.toDataHash();
				}).trigger('hashchange');
			}

			// Toastr config
			toastr.options = {
				escapeHtml: false,
				closeButton: true,
				debug: false,
				newestOnTop: false,
				progressBar: false,
				positionClass: "toast-bottom-right",
				preventDuplicates: false,
				onclick: null,
				showDuration: 300,
				hideDuration: 1000,
				timeOut: 30000,
				extendedTimeOut: 1000,
				showEasing: "swing",
				hideEasing: "linear",
				showMethod: "fadeIn",
				hideMethod: "fadeOut"
			};

			// Ajax setup and preflight
			$.ajaxSetup({
				statusCode: {
					403: function() {
						location.replace('/error/?403');
					},
					500: function() {
						toastr.error('Please inform to admin', '500');
						// location.replace('/error/?500');
					}
				}
			});

			// Get datasources
			$.ajax({
				type: 'GET',
				url: self.domain + self.apis.datasourceAuth
			}).done(function(data) {
				if (data.datasources && data.datasources.length) {
					var datasources = [];
					var auths = {};
					var engines = {};
					data.datasources.map(function(n) {
						Object.map(n, function(val, key) {
							datasources.push(key);
							engines[key] = val.engines;
							auths[key] = val.auth;
						});
					});
					self.datasources = datasources;
					self.auths = auths;
					self.engines = engines;
				} else {
					location.replace('/error/?403');
				}
			}).fail(function(xhr, status, error) {
			});

			// Start
			$('#page').removeClass('unload');

		},
		mounted: function() {
			var self = this;

			// detect Modal
			$(document).on('shown.bs.modal', '.modal', function(e) {
				self.is_modal = true;
				self.focus = 0;
			}).on('hidden.bs.modal', function(e) {
				self.is_modal = false;
				self.focus = 1;
				$(document).scrollTop(0);
			});

			$(document).on('show.bs.modal', '#bookmark', function(e) {
				$('button').blur();
			}).on('hide.bs.modal', function(e) {});

			// Hotkey
			$(window).keydown(function(e) {
				if (e.ctrlKey) {
					if (e.keyCode === 84) { // T
						self.tab = 'treeview';
						e.preventDefault();
					}
				}
				if (self.is_modal) {
					(e.keyCode === 27) && $('.modal').modal('hide'); // ESC
				}
			});

			// detect Loading job
			$(window).on('beforeunload', function(event) {
				if (self.loading.result) {
					return confirm('Do you want to do it?');
				}
			});

			// hidden command
			var inputs = [];
			var configs = {
				'konami': [38, 38, 40, 40, 37, 39, 37, 39, 66, 65],
			};
			$(window).keyup(function(e) {
				inputs.push(e.keyCode);
				if (inputs.toString().indexOf(configs.konami) >= 0) {
					self.superadminMode = true;
					toastr.success('You can watch all queries.', 'Super Admin Mode');
					inputs = [];
				}
			});
		},
		computed: {
			hash: {
				cache: false,
				get: function() {
					var self = this;
					var config = {};
					self.hashKeys.map(function(n) {
						var key = n[0];
						var required = n[1];
						if ((!required && self[key]) || required) {
							config[key] = self[key];
						}
					});
					return Object.toQueryString(config, {
						deep: true
					});
				}
			},
			auth_info: function() {
				var self = this;
				return !self.auths[self.datasource] ? {} : {
					user: self.auth_user,
					password: self.auth_pass
				};
			},
			auth_userInfo: function() {
				var self = this;
				return !self.auths[self.datasource] ? {} : {
					user: self.auth_user,
				};
			},
			datasource_engine: function() {
				var self = this;
				return [self.datasource, self.engine].join('_');
			},
			is_error: function() {
				return $('body').attr('id') === 'error';
			},
			is_share: function() {
				return $('body').attr('id') === 'share';
			},
			is_presto: function() {
				return this.engine == 'presto';
			},
			exist_bookmark: function() {
				var self = this;
				var query = self.input_query;
				var queries = [];
				self.response.bookmark.map(function(n) {
					queries.push(n.query);
				});
				return queries.includes(query);
			},
			orderedQlist: function() {
				var self = this;
				var filter_user = self.filter_user;
				if (self.is_presto) {
					var qlist = self.response.qlist.filter(function(n) {
						if (filter_user === '' || filter_user === n.session.user) {
							if (!n.query.includes(self.hiddenQuery_prefix) || self.superadminMode) {
								if (self.is_adminMode || self.superadminMode) {
									return n;
								} else {
									if (n.existdb || (n.session.source === 'yanagishima' && n.state !== 'FINISHED')) {
										return n;
									}
								}
							}
						}
					});
					var finished_qlist = [],
						running_qlist = [];
					qlist.map(function(n) {
						if (self.isRunning(n.state)) {
							running_qlist.append(n);
						} else {
							finished_qlist.append(n);
						}
					});
					return running_qlist.append(finished_qlist);
				} else {
					// return self.response.qlist;
					var qlist = self.response.qlist.filter(function(n) {
						if (n.name.includes('yanagishima') || self.is_adminMode || self.superadminMode) {
							return n;
						}
					});
					var finished_qlist = [],
						running_qlist = [];
					qlist.map(function(n) {
						if (self.isRunning(n.state)) {
							running_qlist.append(n);
						} else {
							finished_qlist.append(n);
						}
					});
					return running_qlist.append(finished_qlist);
				}
			},
			orderedFailQlist: function() {
				var self = this;
				var filter_user = self.filter_user;
				var qlist = self.response.qlist.filter(function(n) {
					if (filter_user === '' || filter_user === n.session.user && n.state === 'FAILED') {
						return n;
					}
				});
				return qlist;
			},
			filteredSchemata: function() {
				var self = this;
				var filter_schema = self.filter_schema;
				var schemata = self.schemata.filter(function(n) {
					if (n.includes(filter_schema)) {
						return n;
					}
				});
				return schemata;
			},
			filteredTables: function() {
				var self = this;
				var filter_table = self.filter_table;
				var tables = self.tables.filter(function(n) {
					if (n[0].includes(filter_table)) {
						return n;
					}
				});
				return tables;
			},
			filteredHistory: function() {
				var self = this;
				var is_localstorage = self.is_localstorage;
				var filter_history = self.filter_history.trim();
				var filters = filter_history.includes(' ') ? filter_history.split(' ').unique() : [filter_history];
				var history_data = self.response.history.filter(function(n) {
					return n[4] == self.engine;
				});

				if (is_localstorage && filters.length) {
					var history = history_data.filter(function(n) {
						var enable = true;
						filters.map(function(m) {
							var exp = new RegExp(RegExp.escape(m), 'ig');
							enable = enable && exp.test(n[1]);
						});
						return enable;
					});
				} else {
					var history = history_data;
				}
				return history;
			},
			variables: function() {
				var self = this;
				var variables = [];
				var detected_variables = self.input_query.match(/\$\{[a-zA-Z]([a-zA-Z0-9]+)?\}/g);
				if (detected_variables !== null) {
					detected_variables.unique().map(function(n) {
						var variable = n;
						var variable_id = n.remove(/[\$\{\}]/g);
						var value = '';
						variables.push([variable, variable_id, value]);
					});
				}
				return variables;
			},
			explain: function() {
				var self = this;
				if (self.response.result && self.response.result.results) {
					var arr = [];
					self.response.result.results.map(function(n) {
						arr.push(n[0].replace(/ {4}/g, ' '));
					});
					return arr.join('<br>')
				} else {
					return '';
				}
			},
			ctable: function() {
				var self = this;
				if (self.response.result && self.response.result.results) {
					var arr = [];
					self.response.result.results.map(function(n) {
						arr.push(n[0]);
					});
					return arr.join('\n')
				} else {
					return '';
				}
			},
			snippets: function() {
				var self = this;
				var snippets = [
					{
						label: "SHOW PRESTO VIEW DDL",
						sql: "SELECT VIEW_DEFINITION FROM {catalog}.INFORMATION_SCHEMA.VIEWS WHERE table_catalog='{catalog}' AND table_schema='{schema}' AND table_name='{table}'",
						enable: ['VIEW'],
					},
					{
						label: "SHOW CREATE TABLE ...",
						sql: "SHOW CREATE TABLE {catalog}.{schema}.{table}",
						enable: ['BASE TABLE'],
					},
					{
						label: "DESCRIBE ...",
						sql: "DESCRIBE {catalog}.{schema}.{table}",
						enable: ['BASE TABLE', 'VIEW'],
					}
				];
				var defaultSnippet = self.partition_keys.length ? {
					label: "SELECT * FROM ... WHERE ${column_date}=${yesterday} LIMIT 100",
					sql: "SELECT {columns} FROM {catalog}.{schema}.{table} WHERE {column_date}='{yesterday}' LIMIT 100",
					enable: ['BASE TABLE', 'VIEW'],
				} : {
					label: "SELECT * FROM ... LIMIT 100",
					sql: "SELECT {columns} FROM {catalog}.{schema}.{table} LIMIT 100",
					enable: ['BASE TABLE', 'VIEW'],
				};
				return snippets.add(defaultSnippet, 0);
			},
		},
		methods: {
			checkAuth: function() {
				var self = this;
				var auths = self.auths;
				var datasource = self.datasource;

				if (!auths[datasource]) {
					return false;
				}
				self.getAuth();
				if (self.auth_user && self.auth_pass) {
					self.auth_verify = true;
				} else {
					$('#auth').modal('show');
				}
			},
			testAuth: function() {
				var self = this;
				var api = self.is_presto ? self.apis.presto : self.apis.hive;
				var params = Object.merge(
					{
						datasource: self.datasource,
						query: 'SELECT -1'
					},
					self.auth_info,
				);
				$.ajax({
					type: 'POST',
					url: self.domain + api,
					data: params,
				}).done(function(data) {
					if (data.results && data.results.length) {
						toastr.success('Your can access.', 'Success', {
							positionClass: "toast-top-right"
						});
						self.auth_done = true;
					} else {
						toastr.error(data.error || 'Error', 'Fail', {
							positionClass: "toast-top-right"
						});
					}
				}).fail(function(xhr, status, error) {});
			},
			getAuth: function() {
				var self = this;
				var datasource = self.datasource;

				if (datasource) {
					self.auth_user = localStorage.getItem(datasource + '_user');
					self.auth_pass = localStorage.getItem(datasource + '_pass');
				}
			},
			setAuth: function() {
				var self = this;
				var datasource = self.datasource;

				if (datasource) {
					localStorage.setItem(datasource + '_user', self.auth_user);
					localStorage.setItem(datasource + '_pass', self.auth_pass);
				}
			},
			infoBookmark: function(query) {
				var self = this;
				var info = '';
				self.bookmarks.map(function(n) {
					if (n.query == query) {
						info = n;
					}
				});
				return info;
			},
			publish: function(queryid) {
				var self = this;
				$.ajax({
					type: 'POST',
					url: self.domain + self.apis.publish,
					data: {
						datasource: self.datasource,
						engine: self.engine,
						queryid: queryid
					},
				}).done(function(data) {
					var publish_id = data.publish_id;
					if (publish_id) {
						var chart = self.chart;
						var line = self.line;
						var uri = '/share/?' + publish_id + (chart ? '&' + chart : '') + (line ? '#L' + line : '');
						toastr.success(queryid, 'Published (Click Here)', {
							onclick: function() {
								window.open(uri, '_blank');
							}
						});
					}
				}).fail(function(xhr, status, error) {
				});
			},
			viewError: function() {
				var self = this;
				var status_code = location.search.remove('?') || 'Error';
				self.status_code = status_code;
				document.title = '{1} - {0}'.format(self.sitename, status_code);
				$('#page').removeClass('unload');
			},
			viewShare: function() {
				var self = this;
				var arr = location.search.remove('?').split('&');
				var publish_id = arr[0];
				var chart = (arr[1] && arr[1].length === 1) ? Number(arr[1]) : 0;
				self.line = location.hash.remove('#L');

				if (/^[a-z0-9]{32}$/.test(publish_id)) {
					self.publish_id = publish_id;
					document.title = '#{1} - {0}'.format(self.sitename, publish_id);
					$.ajax({
						type: 'GET',
						url: self.domain + self.apis.shareHistory.format({
							publish_id: publish_id
						}),
					}).done(function(data) {
						self.response.share = data;
						self.drawChart(data);
						self.chart = chart;
						$('#page').removeClass('unload');
					}).fail(function(xhr, status, error) {
					});
				}
			},
			drawChart: function(data) {
				var self = this;
				var columns = [],
					rows = [],
					enable = true;
				if (data.headers && data.results) {
					if (data.headers.length <= 1 || Object.isEmpty(data.results)) {
						self.enable_chart = false;
						return;
					}
					var i = 0;
					data.headers.map(function(n) {
						columns.push({
							type: (i === 0) ? ((self.columnDate_names.includes(n)) ? 'date' : 'string') : 'number',
							label: n
						});
						i++;
					});
					data.results.map(function(arr) {
						var i = 0,
							n = [].concat(arr);
						n.map(function(m) {
							if (i === 0) {
								if (columns[0].type === 'date') {
									if (/^[0-9]{8}$/.test(n[0])) {
										n[0] = Date.create(n[0]);
									} else {
										enable = false;
									}
								}
							} else {
								n[i] = (n[i] == 'null') ? 0 : Number(n[i]);
								enable = enable && !Number.isNaN(n[i]);
							}
							i++;
						});
						rows.push(n);
					});
					self.enable_chart = enable;
					self.chart_columns = columns;
					self.chart_rows = rows;
				}
			},
			init: function(all) {
				var self = this;
				if (all) {
					self.input_query = '';
					self.queryid = '';
					self.tab = self.tabs[0].id;
					self.line = 0;
					self.chart = 0;
					self.bookmark_id = '';
				}
				if (self.bookmark_id.length) {
					self.getBookmark(self.bookmark_id);
				};
				
				Object.map(self.response, function(val, key) {
					self.response[key] = Object.isArray(val) ? [] : Object.isNumber(val) ? 0 : '';
				});
				$('[data-toggle="tooltip"]').tooltip('hide');
				self.query = '';
				self.queryString = '';
				self.queryString_collapse = false;
				self.catalog = yanagishima.default_catalog || self.catalogs[0];
				self.schema = '';
				self.table = '';
				self.table_type = '';
				self.column_date = '';
				self.catalogs = [];
				self.schemata = [];
				self.tables = [];
				self.columns = [];
				self.partition_keys = [];
				self.partition_vals = [];
				self.partition_lists = [];
				self.partition_index = 0;
				self.filter_schema = '';
				self.filter_table = '';
				self.filter_user = '';
				self.filter_history = '';
				self.table_q = '';
				self.running_queryid = '';
				self.running_queries = 0;
				self.history_queryid = '';
				self.history_result = [];
				self.setTitle();
				self.getHistories();
				self.getBookmarks();
				self.getQlist();
				$(document).scrollTop(0);
			},
			setTitle: function(val) {
				var self = this;
				var subTitle = val || self.datasource;
				var tab = self.tabs.find(function(n) {
					return n.id === self.tab;
				});
				var pageTitle = tab.name;
				if (self.tab === 'result' && self.queryid) {
					pageTitle = '#' + self.queryid;
				}
				document.title = '[{1}] {2} - {0}'.format(self.sitename, subTitle, pageTitle);
			},
			runTab: function() {
				var self = this;
				var tab = self.tab;
				self.setTitle();
				switch (tab) {
					case 'qlist':
						if (self.is_autoQlist) {
							self.autoQlist(self.is_autoQlist);
						} else {
							self.getQlist();
						}
						break;
					case 'history':
						self.getHistories();
						break;
					case 'result':
						self.loadResult()
						break;
					case 'treeview':
						self.getTree();
						break;
				}
			},
			changeQuery: function(query) {
				var self = this;
				(self.input_query !== query) && (self.input_query = query);
			},
			searchTable: function(q) {
				var self = this;
				var q = q || self.table_q;
				self.response.table = [];
				if (q === '') {
					return false;
				}
				self.loading.table = true;
				$.ajax({
					type: 'POST',
					url: self.domain + self.apis.presto,
					data: {
						datasource: self.datasource,
						query: "SELECT table_catalog, table_schema, table_name, table_type FROM {catalog}.information_schema.tables WHERE table_name LIKE '%{q}%'".format({
							catalog: self.catalog,
							q: q
						})
					}
				}).done(function(data) {
					self.response.table = data.results;
					self.loading.table = false;
				}).fail(function(xhr, status, error) {
					self.loading.table = false;
				});
			},
			loadResult: function(queryid) {
				var self = this;
				if (!self.loading.result) {
					self.loadQuery(self.queryid);
				}
				self.tab = 'result';
			},
			validateQuery: function(query) {
				var self = this;
				var query = query || self.query;
				$.ajax({
					type: 'POST',
					url: self.domain + self.apis.presto,
					data: {
						datasource: self.datasource,
						query: "EXPLAIN (TYPE VALIDATE) {0}".format(query)
					}
				}).done(function(data) {
					if (data.error) {
						self.errortext = data.error;
						self.errorline = (data.errorLineNumber == undefined) ? 0 : data.errorLineNumber - 1;
					} else {
						self.ettortext = '';
						self.errorline = -1;
					}
				}).fail(function(xhr, status, error) {
				});
			},
			runQuery: function(query) {
				var self = this;
				var query = query || self.query;
				var is_checking = true;
				var run_timer;

				self.line = 0;
				self.chart = 0;
				if (self.loading.result) {
					return false;
				}

				// variables expansion
				if (self.variables.length) {
					var error_variables = [];
					self.variables.map(function(n) {
						var variable = new RegExp(RegExp.escape(n[0]), 'g');
						var variable_id = n[1];
						var value = n[2];
						if (value.length) {
							query = query.replace(variable, value);
						} else {
							error_variables.push(variable_id);
						}
					});
					if (error_variables.length) {
						alert('Input to variables ' + error_variables.join(', '));
						return false;
					} else {
						self.input_query = query;
					}
				}

				self.running_queries++;
				self.loading.result = true;
				self.error.result = false;
				self.queryid = '';

				var api = self.is_presto ? self.apis.prestoAsync : self.apis.hiveAsync;
				var params = Object.merge(
					{
						datasource: self.datasource,
						query: query,
					},
					self.auth_info,
				);

				$.ajax({
					type: 'POST',
					url: self.domain + api,
					data: params,
				}).done(function(data) {
					var queryid = data.queryid;
					var period = self.is_presto ? 500 : 5000;
					if (queryid) {
						self.running_queryid = queryid;
						self.running_progress = -1;
						self.running_time = '';
						clearInterval(run_timer);
						if (self.loading.result) {
							run_timer = setInterval(function() {
								getResult(queryid);
							}, period);
						}
					} else {
						self.loading.result = false;
						self.error.result = data.error;
						self.running_queries--;
					}
				}).fail(function(xhr, status, error) {
					self.loading.result = false;
					self.error.result = error || yanagishima.messages.error;
					self.running_queries--;
				});
				self.tab = 'result';

				function getResult(queryid) {
					var def = $.Deferred();
					var api = self.is_presto ? self.apis.queryStatus : self.apis.hiveQueryStatus;
					var params = Object.merge(
						{
							datasource: self.datasource,
							queryid: queryid,
						},
						self.is_presto ? self.auth_info : self.auth_userInfo,
					);

					$.ajax({
						type: 'POST',
						url: self.domain + api,
						data: params,
					}).done(function(status) {
						var state = status.state;
						if (state === 'FINISHED' || state === 'FAILED' || state === 'KILLED' || Object.isEmpty(status)) {
							clearInterval(run_timer);
							self.running_queryid = '';
							self.running_progress = 0;
							self.running_time = '';
							if (state === 'FINISHED' || Object.isEmpty(status)) {
								self.addHistoryItem(queryid);
							} else if (state === 'FAILED' || state === 'KILLED') {
								if (self.is_presto) {
									if (status.failureInfo.errorLocation) {
										self.gotoline = status.failureInfo.errorLocation.lineNumber;
										self.errorline = status.failureInfo.errorLocation.lineNumber - 1;
										self.errortext = status.failureInfo.message;
									}
								}
							}
							var wait = 10;
							var step = 1000; //[ms]
							var count = wait * step;
							run_timer = setInterval(function() {
								if (is_checking) {
									if (count -= step) {
										wgetResult(queryid);
									}
								} else {
									clearInterval(run_timer);
								}
							}, step);
						} else if (state === 'RUNNING') {
							if (self.is_presto) {
								var stats = status.queryStats;
								self.running_progress = self.progress(stats, 1);
								self.running_time = stats.elapsedTime;
							} else {
								self.running_progress = status.progress;
								self.running_time = (status.elapsedTime/1000).ceil(1) + 's';
							}
						}
					});
				}

				function wgetResult(queryid) {
					$.ajax({
						type: 'GET',
						url: self.domain + self.apis.historyStatus.format({
							datasource: self.datasource,
							queryid: queryid
						}),
					}).done(function(data) {
						if (data.status === 'ok') {
							is_checking = false;
							self.loadQuery(queryid);
							self.running_queries--;

							if (self.desktopNotification && document.visibilityState != 'visible') {
								Push.create('Done (Click here)', {
									body: self.input_query.compact().truncate(64),
									icon: 'favicon.ico',
									timeout: 60*60*1000,
									onClick: function() {
										window.focus();
										this.close();
									}
								});
							}
						}
					}).fail(function(xhr, status, error) {
						self.loading.result = false;
						self.error.result = error || yanagishima.messages.error;
						self.running_queries--;
						
						if (self.desktopNotification && document.visibilityState != 'visible') {
							Push.create('Error', {
								body: self.input_query.compact().truncate(64),
								icon: 'favicon.ico',
								timeout: 60*60*1000,
							});
						}
					});
				}
			},
			loadQuery: function(queryid) {
				var self = this;
				var queryid = queryid || self.queryid;
				if (!queryid) {
					return false;
				}
				self.loading.result = true;
				self.error.result = false;
				self.bookmark_id = '';
				$.ajax({
					type: 'GET',
					url: self.domain + self.apis.history.format({
						datasource: self.datasource,
						queryid: queryid
					}),
					timeout: 300000,
				}).done(function(data, status, xhr) {
					if (!Object.isEmpty(data)) {
						self.queryString = data.queryString;
						self.queryid = queryid;
						self.loading.result = false;
						self.input_query = data.queryString.remove(/^EXPLAIN( \(TYPE DISTRIBUTED\)| \(TYPE VALIDATE\)| ANALYZE|) /i);
						self.engine = data.engine;
						if (!data.error) {
							if (data.results && /^show partitions /i.test(data.queryString)) {
								data.results.sortBy(function(n) {
									return n[0];
								}, true);
							}
							self.response.result = data;
							self.drawChart(data);
						} else {
							self.error.result = data.error;
						}
						$(document).scrollTop(0);
					} else {
						self.queryString = '';
						self.loading.result = false;
					}
				}).fail(function(xhr, status, error) {
					self.loading.result = false;
					self.error.result = error || yanagishima.messages.error;
				});
			},
			abortQuery: function(queryid) {
				var self = this;
				self.killQuery(queryid);
			},
			loadHistoryQuery: function(queryid) {
				var self = this;
				if (!queryid) {
					return false;
				}
				$.ajax({
					type: 'GET',
					url: self.domain + self.apis.history.format({
						datasource: self.datasource,
						queryid: queryid
					}),
					timeout: 300000,
				}).done(function(data, status, xhr) {
					if (!Object.isEmpty(data)) {
						self.history_result = data;
					}
				}).fail(function(xhr, status, error) {
				});
			},
			setSnippet: function() {
				var self = this;
				var config = {
					catalog: self.catalog,
					schema: self.schema,
					table: self.table,
					column_date: self.col_date,
					columns: self.is_expandColumns ? self.cols : '*',
					yesterday: Date.create().addDays(-1).format('{yyyy}{MM}{dd}')
				};
				var snippet = self.snippets[self.snippet].sql;
				!self.is_presto && (snippet = snippet.remove('{catalog}.'));
				self.input_query = snippet.format(config);
			},
			setWhere: function(index) {
				var self = this;
				var conditions = [];
				var keys = self.partition_keys.first(index);
				var vals = self.partition_vals.first(index);
				var keyVals = keys.zip(vals);
				keyVals.map(function(n) {
					conditions.push("{0}='{1}'".format(n));
				});
				var config = {
					catalog: self.catalog,
					schema: self.schema,
					table: self.table,
					condition: conditions.join(' AND '),
				};
				var template = "SELECT * FROM {catalog}.{schema}.{table} WHERE {condition} LIMIT 100";
				var where = self.is_presto ? template : template.remove('{catalog}.');
				self.input_query = where.format(config);
			},
			runSnippet: function() {
				var self = this;
				self.setSnippet();
				self.runQuery(self.input_query);
			},
			download: function(queryid, is_csv) {
				var self = this;
				var api = is_csv ? self.apis.csvdownload : self.apis.download;
				var uri = self.domain + api.format({
					datasource: self.datasource,
					queryid: queryid
				});
				return uri;
			},
			killQuery: function(val) {
				var self = this;
				var api = self.is_presto ? self.apis.kill : self.apis.killHive;
				var params = Object.merge(
					{
						datasource: self.datasource,
					},
					self.is_presto ? self.auth_info : self.auth_userInfo,
				);
				if (self.is_presto) {
					params.queryid = val;
				} else {
					params.id = val;
				}
				$.ajax({
					type: 'POST',
					url: self.domain + api,
					data: params,
				}).done(function(data) {
				}).fail(function(xhr, status, error) {
				});
			},
			formatQuery: function(queryid) {
				var self = this;
				var query = self.input_query;
				if (query) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.format,
						data: {
							query: query,
						},
					}).done(function(data) {
						if (data.formattedQuery) {
							self.input_query = data.formattedQuery;
						} else if (data.error) {
							self.errortext = data.error;
							self.errorline = data.errorLineNumber - 1;
						}
					}).fail(function(xhr, status, error) {
					});
				}
			},
			toValuesQuery: function(values) {
				var self = this;
				if (values) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.toValuesQuery,
						data: {
							csv: values,
						},
					}).done(function(data) {
						if (data.error) {
							alert(data.error);
						} else {
							self.input_query = data.query;
						}
					}).fail(function(xhr, status, error) {
					});
				}
			},
			getBookmark: function(bookmark_id) {
				var self = this;
				if (!bookmark_id.length) {
					return false;
				}
				$.ajax({
					type: 'GET',
					url: self.domain + self.apis.bookmark.format({
						datasource: self.datasource,
						engine: self.engine,
					}),
					data: {
						bookmark_id: bookmark_id
					},
					timeout: 300000,
				}).done(function(data) {
					if (data.bookmarkList) {
						self.input_query = data.bookmarkList[0].query;
						self.queryString = '';
					}
				}).fail(function(xhr, status, error) {
				});
			},
			getBookmarks: function() {
				var self = this;
				var is_localstorage = self.is_localstorage;
				if (is_localstorage) {
					self.getBookmarkItems();
					if (self.bookmarks.length == 0) {
						return false;
					}
				}
				if (!(self.datasource && self.engine)) {
					return false;
				}

				self.loading.bookmark = true;
				if (is_localstorage) {
					$.ajax({
						type: 'GET',
						url: self.domain + self.apis.bookmark,
						data: {
							datasource: self.datasource,
							engine: self.engine,
							bookmark_id: self.bookmarks.join(','),
						},
						timeout: 300000,
					}).done(function(data) {
						sortBookmarks(data);
						self.loading.bookmark = false;
					}).fail(function(xhr, status, error) {
						self.loading.bookmark = false;
					});
				} else {
					$.ajax({
						type: 'GET',
						url: self.domain + self.apis.bookmarkUser,
						data: {
							datasource: self.datasource,
							engine: self.engine,
						},
						timeout: 300000,
					}).done(function(data) {
						sortBookmarks(data);
						self.loading.bookmark = false;
					}).fail(function(xhr, status, error) {
						self.loading.bookmark = false;
					});
				}
				function sortBookmarks(data) {
					if (data.bookmarkList) {
						self.response.bookmark = data.bookmarkList.filter(function(n) {
							return n.engine == self.engine;
						}).sortBy(function(n) {
							return n.bookmark_id;
						}, true);
					}
				}
			},
			getHistories: function(not_loading) {
				var self = this;
				var not_loading = not_loading || false;
				var is_localstorage = self.is_localstorage;
				if (is_localstorage) {
					self.getHistoryItems();
					if (self.histories.length == 0) {
						return false;
					}
				}
				if (!not_loading) {
					self.history_limit = 100;
					self.loading.history = true;
				}

				var api = {
					url: is_localstorage ? self.domain + self.apis.queryHistory : self.domain + self.apis.queryHistoryUser,
					data: is_localstorage ? {
						queryids: self.histories.join(',')
					} : {
						search: self.filter_history,
						offset: 0,
						limit: self.history_limit,
					},
					type: is_localstorage ? 'POST' : 'GET',
				};

				$.ajax({
					type: api.type,
					url: api.url.format({
						datasource: self.datasource,
						engine: self.engine,
					}),
					data: api.data,
					timeout: 300000,
				}).done(function(data) {
					if (data.results) {
						self.response.history = data.results.sortBy(function(n) {
							return n[0];
						}, true);
					}
					if (!is_localstorage) {
						self.response.historyTotal = data.total;
						self.response.historyHit = data.hit;
					}
					self.loading.history = false;
				}).fail(function(xhr, status, error) {
					self.loading.history = false;
				});
			},
			getQlist: function(is_autoQlist) {
				var self = this;
				var is_autoQlist = is_autoQlist || false;
				self.now = Date.create().format('{yyyy}/{M}/{d} {24hr}:{mm}:{ss}');
				self.loading.qlist = !is_autoQlist;
				var api = self.is_presto ? self.apis.query : self.apis.yarnJobList;
				var type = self.is_presto ? 'POST' : 'GET';
				var params = {
					datasource: self.datasource,
				};
				if (self.is_presto) {
					params = Object.merge(params, self.auth_info);
				}
				$.ajax({
					type: type,
					url: self.domain + api,
					data: params,
				}).done(function(data) {
					self.response.qlist = data;
					self.loading.qlist = false;
				}).fail(function(xhr, status, error) {
					self.loading.qlist = false;
				});
			},
			autoQlist: function(enable) {
				var self = this;
				var enable = enable || false;
				var max = self.refresh_period;
				var time = max;
				var period = self.is_presto ? 1000 : 1000 * 10;

				clearInterval(self.timer);
				self.getQlist();
				if (enable) {
					self.timer = setInterval(function() {
						time--;
						if (time === 0) {
							if (self.tab === 'qlist' && !self.is_modal) {
								self.getQlist(true);
							}
							time = max;
						}
					}, period);
				}
			},
			getTable: function() {
				var self = this;

				if (!(self.datasource && self.catalog)) {
					return false;
				}
				var params = Object.merge(
					{
						datasource: self.datasource,
						catalog: self.catalog,
					},
					self.auth_info,
				);

				$.ajax({
					type: 'POST',
					url: self.domain + self.apis.tableList,
					data: params,
				}).done(function(data) {
					if (data.tableList) {
						var complate_words = [];
						var complate_list = Object.merge(
							yanagishima.complate_list, {
								table: data.tableList
							}, {
								deep: true
							}
						);
						Object.map(complate_list, function(vals, key) {
							vals.map(function(val) {
								if (key === 'snippet') {
									val = val.format({
										yesterday: Date.create().addDays(-1).format('{yyyy}{MM}{dd}')
									});
								}
								var caption = val.truncate(70, 'left');
								var value = val;
								var meta = key;
								complate_words.push({
									caption: caption,
									value: value,
									meta: meta
								});
							});
						});
						self.complate_words = complate_words;
					}
				}).fail(function(xhr, status, error) {});
			},
			getPartition: function(index) {
				var self = this;
				var api = self.is_presto ? self.apis.prestoPartition : self.apis.hivePartition;
				var params = Object.merge(
					{
						datasource: self.datasource,
						schema: self.schema,
						table: self.table
					},
					self.auth_info,
				);
				if (self.is_presto) {
					params.catalog = self.catalog;
				}
				if (index) {
					self.partition_index = index;
					var keys = self.partition_keys.first(index);
					var vals = self.partition_vals.first(index);
					params.partitionColumn = keys.join(',');
					params.partitionValue = vals.join(',');
				}
				self.loading.partition = true;
				$.ajax({
					type: 'POST',
					url: self.domain + api,
					data: params,
				}).done(function(data) {
					if (data.error) {
						toastr.error(data.error);
					} else if (data.column && data.partitions && data.partitions.length) {
						var index = self.partition_keys.indexOf(data.column);
						self.partition_lists.splice(index, 1, data.partitions);
					}
					self.loading.partition = false;
				}).fail(function(xhr, status, error) {
					self.loading.partition = true;
				});
			},
			getTree: function() {
				var self = this;
				var catalog = self.catalog;
				var schema = self.schema;
				var table = self.table;
				var table_type = self.table_type;
				var hiddenQuery_prefix = self.hiddenQuery_prefix;

				if (self.is_presto) {
					if (!(self.catalogs.length && catalog)) {
						var params = Object.merge(
							{
								datasource: self.datasource,
								query: '{0}SHOW catalogs'.format(hiddenQuery_prefix)
							},
							self.auth_info,
						);
						$.ajax({
							type: 'POST',
							url: self.domain + self.apis.presto,
							data: params,
						}).done(function(data) {
							if (data.results && data.results.length) {
								self.catalogs = data.results.map(function(n) {
									return n[0];
								});
								self.catalog = yanagishima.default_catalog || self.catalogs[0];
							}
						}).fail(function(xhr, status, error) {});
					}
					if (!schema && catalog) {
						var params = Object.merge(
							{
								datasource: self.datasource,
								query: '{0}SHOW schemas from {1}'.format(hiddenQuery_prefix, catalog)
							},
							self.auth_info,
						);
						$.ajax({
							type: 'POST',
							url: self.domain + self.apis.presto,
							data: params,
						}).done(function(data) {
							if (data.results && data.results.length) {
								self.schemata = data.results.map(function(n) {
									return n[0];
								});
								self.schema = self.schema || self.schemata[0];
							} else {
								self.schemata = [];
							}
						}).fail(function(xhr, status, error) {});
					} else if (!table && catalog && schema) {
						var params = Object.merge(
							{
								datasource: self.datasource,
								query: "{0}SELECT table_name, table_type FROM {1}.information_schema.tables WHERE table_schema='{2}'".format(hiddenQuery_prefix, catalog, schema)
							},
							self.auth_info,
						);
						$.ajax({
							type: 'POST',
							url: self.domain + self.apis.presto,
							data: params,
						}).done(function(data) {
							if (data.results && data.results.length) {
								self.tables = data.results.map(function(n) {
									var table_name = n[0];
									var table_type = n[1];
									return [table_name, table_type];
								});
							} else {
								self.tables = [];
							}
						}).fail(function(xhr, status, error) {});
					} else {
						if (!(catalog && schema && table)) {
							return false;
						}

						var params = Object.merge(
							{
								datasource: self.datasource,
								query: "{0}DESCRIBE {1}".format(hiddenQuery_prefix, [catalog, schema, table].join('.'))
							},
							self.auth_info,
						);
						$.ajax({
							type: 'POST',
							url: self.domain + self.apis.presto,
							data: params,
						}).done(function(data) {
							var col_date = '';
							var cols = [];
							var partition_keys = [];

							if (data.results && data.results.length) {
								self.columns = data.results;
								data.results.map(function(n) {
									if (self.columnDate_names.includes(n[0])) {
										col_date = n[0];
									} else {
										cols.push(n[0]);
									}
									if (n[2] == 'partition key') {
										partition_keys.push(n[0]);
									}
								});
								self.cols = col_date ? cols.add(col_date, 0) : cols;
								self.partition_keys = partition_keys;
								self.col_date = col_date;
							} else {
								self.columns = [];
							}
						}).fail(function(xhr, status, error) {});
					}
				} else {
					if (!schema) {
						var params = Object.merge(
							{
								datasource: self.datasource,
								query: 'SHOW schemas'
							},
							self.auth_info,
						);
						$.ajax({
							type: 'POST',
							url: self.domain + self.apis.hive,
							data: params,
						}).done(function(data) {
							if (data.results && data.results.length) {
								self.schemata = data.results.map(function(n) {
									return n[0];
								});
								self.schema = self.schema || self.schemata[0];
							}
						}).fail(function(xhr, status, error) {});
					} else if (!table && schema) {
						var params = Object.merge(
							{
								datasource: self.datasource,
								query: "SHOW tables IN {0}".format(schema)
							},
							self.auth_info,
						);
						$.ajax({
							type: 'POST',
							url: self.domain + self.apis.hive,
							data: params,
						}).done(function(data) {
							if (data.results && data.results.length) {
								self.tables = data.results.map(function(n) {
									var table_name = n[0];
									var table_type = 'BASE TABLE';
									return [table_name, table_type];
								});
							}
						}).fail(function(xhr, status, error) {});
					} else {
						if (!(schema && table)) {
							return false;
						}

						var params = Object.merge(
							{
								datasource: self.datasource,
								query: "DESCRIBE {0}".format([schema, table].join('.'))
							},
							self.auth_info,
						);
						$.ajax({
							type: 'POST',
							url: self.domain + self.apis.hive,
							data: params,
						}).done(function(data) {
							var col_date = '';
							var cols = [];
							var columns = [];
							var partition_keys = [];

							if (data.results && data.results.length) {
								data.results.map(function(n) {
									if (self.columnDate_names.includes(n[0])) {
										col_date = n[0];
									} else {
										cols.push(n[0]);
									}
									if (!(n[0] == '' || n[0].includes('#'))) {
										var column = n.add(null, 2);
										var indexOfColumn = columns.findIndex(function(m) {
											return n[0] == m[0];
										});
										if (indexOfColumn == -1) {
											columns.push(column);
										} else {
											columns[indexOfColumn][2] = 'partition key';
											partition_keys.push(n[0]);
										}
									}
								});
								self.columns = columns;
								self.cols = col_date ? cols.add(col_date, 0) : cols;
								self.partition_keys = partition_keys;
								self.col_date = col_date;
							}
						}).fail(function(xhr, status, error) {});
					}
				}
			},
			isRunning: function(val) {
				var self = this;
				if (self.is_presto) {
					return !['FINISHED', 'FAILED', 'CANCELED'].includes(val);
				} else {
					return !['FINISHED', 'FAILED', 'KILLED'].includes(val);
				}
			},
			setItem: function(key, item) {
				var self = this;
				var items = item ? self.getItem(key).add(item, 0).unique() : [];
				localStorage.setItem(key, items);
			},
			delItem: function(key, item) {
				var self = this;
				var items = item ? self.getItem(key).add(item, 0).unique() : [];
				localStorage.setItem(key, items.remove(item));
			},
			getItem: function(key, max) {
				var self = this;
				var storage = localStorage.getItem(key);
				var items = storage && storage.split(',') || [];
				return items.first(max || items.length);
			},
			fromDataHash: function(add_history) {
				var self = this;
				var add_history = add_history || true;
				var hash = location.hash.remove('#');
				if (hash !== self.hash) {
					if (add_history) {
						location.hash = self.hash;
					} else {
						location.replace('#' + self.hash);
					}
				}
			},
			toDataHash: function() {
				var self = this;
				var hash = location.hash.remove('#');
				var is_match = true;
				if (hash !== self.hash) {
					var params = Object.fromQueryString(hash, {
						deep: true,
						auto: false
					});
					self.hashKeys.map(function(n) {
						var key = n[0];
						var ommit = n[1];
						if ((ommit && params[key]) || !ommit) {
							is_match = is_match && ((self[key] === params[key]) || params[key]);
							if (params[key] !== undefined) {
								self[key] = params[key];
							}
						}
					});
				}
				self.fromDataHash(!is_match);
			},
			getBookmarkItems: function(max) {
				var self = this;
				var max = max || 100;
				var bookmarks = self.getItem('bookmarks_' + self.datasource, max);
				if (bookmarks.length) {
					if (bookmarks[0].includes('_')) {
						localStorage.removeItem('bookmarks_' + self.datasource);
						bookmarks = [];
					}
				}
				self.bookmarks = bookmarks;
			},
			addBookmarkItem: function() {
				var self = this;
				var query = self.input_query;
				var defaultTitle = Date.create().format('{yyyy}/{MM}/{dd} {24hr}:{mm}:{ss}');
				var title = prompt('Input bookmark title. (default: {0})'.format(defaultTitle));
				if (title === null) {
					return false;
				}
				$.ajax({
					type: 'POSt',
					url: self.domain + self.apis.bookmark,
					data: {
						datasource: self.datasource,
						engine: self.engine,
						title: title || defaultTitle,
						query: query
					},
					timeout: 300000,
				}).done(function(data) {
					var bookmark_id = data.bookmark_id;
					self.setItem('bookmarks_' + self.datasource, bookmark_id);
					self.bookmark_addId = bookmark_id;
					self.getBookmarkItems();
					self.queryid = '';
					self.queryString = '';
					self.response.result = '';
					self.bookmark_id = bookmark_id;
					self.tab = 'bookmark';
					// $('#bookmark').modal('show');
				}).fail(function(xhr, status, error) {
					console.log('error');
				});
			},
			delBookmarkItem: function(bookmark_id) {
				var self = this;
				self.delItem('bookmarks_' + self.datasource, String(bookmark_id));
				$.ajax({
					type: 'DELETE',
					url: self.domain + self.apis.bookmark.format({
						datasource: self.datasource,
						engine: self.engine,
					}) + '&bookmark_id=' + bookmark_id,
					timeout: 300000,
				}).done(function(data) {
					self.is_localstorage && self.getBookmarkItems();
				}).fail(function(xhr, status, error) {
				});
			},
			delBookmarkAll: function() {
				var self = this;
				if (confirm('Do you want to remove it?')) {
					localStorage.removeItem('bookmarks_' + self.datasource);
					self.getBookmarks();
				}
			},
			importBookmark: function() {
				var self = this;
				if (!self.input_query.length) {
					alert('Editor is empty.');
					return false;
				}
				if (confirm('Do you want to overwrite it?')) {
					localStorage.setItem('bookmarks_' + self.datasource, self.input_query);
					self.getBookmarks();
				}
			},
			getHistoryItems: function(max) {
				var self = this;
				var max = max || 1024;
				self.histories = self.getItem('histories_' + self.datasource, max);
			},
			addHistoryItem: function(queryid) {
				var self = this;
				self.setItem('histories_' + self.datasource, queryid);
				self.getHistoryItems();
			},
			delHistoryItem: function(item) {
				var self = this;
				self.delItem('histories_' + self.datasource, item);
				self.getHistoryItems();
			},
			delHistoryAll: function() {
				var self = this;
				if (confirm('Do you want to remove it?')) {
					localStorage.removeItem('histories_' + self.datasource);
					self.getHistories();
				}
			},
			importHistory: function() {
				var self = this;
				if (!self.input_query.length) {
					alert('Editor is empty.');
					return false;
				}
				if (confirm('Do you want to overwrite it?')) {
					localStorage.setItem('histories_' + self.datasource, self.input_query);
					self.getHistories();
				}
			},
			linkDetail: function(val) {
				var self = this;
				if (self.is_presto) {
					return self.domain + self.apis.detail.format({
						queryid: val,
						datasource: self.datasource
					});
				} else {
					return self.domain + self.apis.hiveQueryDetail.format({
						id: val,
						datasource: self.datasource,
					});
				}
			},
			progress: function(stats, digit) {
				var digit = digit || 0;
				if (stats.completedDrivers !== undefined && stats.totalDrivers !== undefined) {
					var p = ((stats.completedDrivers / stats.totalDrivers) * 100).ceil(digit);
					return Number.isNaN(p) ? 0 : p;
				} else {
					return 0;
				}
			},
			getHiveQueryid: function(application_id) {
				var self = this;
				if (application_id.includes('yanagishima-hive')) {
					return application_id.split('-').last();
				} else {
					return application_id;
				}
			},
		},
		filters: {
			formatNumber: function(val, option) {
				var option = option || null;
				return Number(val).format(option);
			},
			formatUnit: function(val) {
				var reg = /^([0-9\.]+)([a-z]+)$/i;
				if (reg.test(val)) {
					var value = Number(RegExp.$1);
					var unit = RegExp.$2;
					switch (unit) {
						case 'ms':
							value = value / 1000;
							unit = 's';
							break;
						case 'us':
							value = value / 1000000;
							unit = 's';
							break;
						case 'ns':
							value = value / 1000000000;
							unit = 's';
							break;
						default:
					}
					return '{0}{1}'.format(Number(value).ceil(1), unit);
				} else {
					if (Object.isNumber(val)) {
						value = val / 1000;
						unit = 's';
						return '{0}{1}'.format(Number(value).ceil(1), unit);
					} else {
						return val;
					}
				}
			},
			extractDate: function(val) {
				if (val) {
					var dt = val.split('+');
					return Date.create(dt[0]).format('{yyyy}/{MM}/{dd} {24hr}:{mm}:{ss}');
				}
			},
			relativeDate: function(val) {
				if (val) {
					var dt = val.split('+');
					var d = Date.create(dt[0]);
					return d.isToday() ? d.format('{24hr}:{mm}') : d.relative();
				}
			},
			humanize: function(val) {
				if (val) {
					return val.replace(/_/g, ' ').capitalize(true, true);
				}
			},
			tinyError: function(val) {
				if (val) {
					return val.remove(/^Query failed \(#[0-9a-z_]+\): /).remove(/^line [0-9]+:[0-9]+: /).truncate(192);
				} else {
					return val;
				}
			},
		},
		watch: {
			hash: function() {
				var self = this;
				if (!self.is_share) {
					self.fromDataHash();
					self.setTitle();
				}
			},
			datasources: function(val) {
				var self = this;
				if (!val.length) {
					location.replace('/error/?403');
				}
				self.datasource = self.datasource || (self.rememberDatasource ? (localStorage.getItem('datasource') || self.datasources[0]) : self.datasources[0]);
				self.tab = self.tab || self.tabs[0].id;
			},
			datasource: function(val, oldVal) {
				var self = this;
				if (oldVal !== '') {
					self.queryid = '';
					self.bookmark_id = '';
					self.input_query = '';
					if (self.tab !== 'treeview') {
						self.tab = 'qlist';
					}
				}
				if (self.engines[val] && !self.engines[val].includes(self.engine)) {
					self.engine = self.engines[val][0];
				}
				if (self.datasources.includes(val)) {
					localStorage.setItem('datasource', val);
				}
				self.checkAuth();
			},
			auths: function(val, oldVal) {
				var self = this;
				self.checkAuth();
			},
			engines: function(val, oldVal) {
				var self = this;
				self.engine = self.engine || (self.rememberEngine ? (localStorage.getItem('engine') || self.engines[self.datasource][0]) : self.engines[self.datasource][0]);
			},
			engine: function(val, oldVal) {
				var self = this;
				if (oldVal !== '') {
					self.queryid = '';
					self.bookmark_id = '';
					self.input_query = '';
					if (self.tab !== 'treeview') {
						self.tab = 'qlist';
					}
				}
				localStorage.setItem('engine', val);
			},
			datasource_engine: function(val, oldVal) {
				var self = this;
				self.init();
				self.runTab();
				oldVal && self.getTable();
			},
			is_modal: function(val) {
				$('[data-toggle="tooltip"]').tooltip('hide');
			},
			tab: function(val) {
				var self = this;
				self.runTab();
				$(document).scrollTop(0);
			},
			query: function(val) {
				var self = this;
				localStorage.setItem('query', val);
			},
			input_query: function(val) {
				var self = this;
				localStorage.setItem('input_query', val);
				self.bookmark_addId = false;
				self.errortext = '';
				self.errorline = -1;
			},
			catalog: function(val) {
				var self = this;
				self.getTree();
				val && self.getTable();
				self.searchTable();
				localStorage.setItem('catalog', val);
			},
			schema: function(val) {
				var self = this;
				self.getTree();
				localStorage.setItem('schema', val);
			},
			table: function(val) {
				var self = this;
				self.getTree();
				self.partition_lists = [];
				self.partition_keys = [];
				self.partition_vals = [];
				self.partition_index = 0;
				localStorage.setItem('table', val);
			},
			partition_keys: function(vals) {
				var self = this;
				var i = 0;
				vals.map(function(n) {
					self.partition_lists[i] = [];
					i++;
				});
			},
			is_autoQlist: function(val) {
				var self = this;
				self.autoQlist(val);
				localStorage.setItem('autoQlist', Number(val));
			},
			minline: function(val) {
				var self = this;
				localStorage.setItem('minline', val);
			},
			is_wide: function(val) {
				var self = this;
				localStorage.setItem('wide', Number(val));
			},
			is_panel: function(val) {
				var self = this;
				localStorage.setItem('panel', Number(val));
			},
			theme: function(val) {
				var self = this;
				localStorage.setItem('theme', val);
			},
			setting: function(val) {
				var self = this;
				localStorage.setItem('setting', val);
			},
			desktopNotification: function(val) {
				var self = this;
				val && Push.Permission.request();
				localStorage.setItem('desktopNotification', Number(val));
			},
			rememberDatasource: function(val) {
				var self = this;
				localStorage.setItem('rememberDatasource', Number(val));
			},
			rememberEngine: function(val) {
				var self = this;
				localStorage.setItem('rememberEngine', Number(val));
			},
			fixedHeader: function(val) {
				var self = this;
				localStorage.setItem('fixedHeader', Number(val));
			},
			is_openQuery: function(val) {
				var self = this;
				localStorage.setItem('openQuery', Number(val));
			},
			is_adminMode: function(val) {
				var self = this;
				localStorage.setItem('adminMode', Number(val));
			},
			is_localstorage: function(val) {
				var self = this;
				localStorage.setItem('localstorage', Number(val));
				self.init();
			},
			table_q: function(val) {
				var self = this;
				self.table = '';
			},
			queryid: function(val) {
				var self = this;
				self.bookmark_addId = false;
			},
			bookmarks: function(vals, oldVals) {
				var self = this;
				if (vals.length > oldVals.length) {
					self.getBookmarks();
				}
			},
			running_queries: function(val) {
				var self = this;
				favicon.badge(val);
				self.getHistories();
			},
		}
	});
});