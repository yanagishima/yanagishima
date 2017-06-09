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
					['tab', true],
					['queryid', false],
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

				// setting
				setting: Number(localStorage.getItem('setting')) || 0,
				fixedHeader: Number(localStorage.getItem('fixedHeader')) || 0,
				rememberDatasource: Number(localStorage.getItem('rememberDatasource')) || 0,
				minlines: [
					2,
					4,
					8,
					16,
				],
				explains: {
					'explain': 'EXPLAIN {0}',
					'explain distributed': 'EXPLAIN (TYPE DISTRIBUTED) {0}',
					'explain analyze': 'EXPLAIN ANALYZE {0}'
				},
				historySizes: [
					10,
					25,
					50,
					100,
					256
				],

				// treeview
				datasources: [],
				catalogs: [],
				schemata: [],
				tables: [],
				columns: [],
				table_q_in: '',
				table_q: '',
				datasource: '',
				catalog: '',
				schema: '',
				table: '',
				table_type: '',
				cols: [],
				col_date: '',
				snippets: yanagishima.snippets,
				snippet: yanagishima.snippets[0].sql,
				filter_schema: '',
				is_searchTable: false,
				is_expandColumns: false,

				// query editoer
				input_query: '',
				query: '',
				queryString: '',
				queryString_collapse: false,
				gotoline: 0,
				errorline: -1,
				errortext: '',
				focus: 1,
				minline: Number(localStorage.getItem('minline')) || 4,
				historySize: Number(localStorage.getItem('historySize')) || 25,

				// qlist
				is_autoQlist: Number(localStorage.getItem('autoQlist')) && true,
				refresh_period: 1,
				filter_user: '',
				is_openQuery: Number(localStorage.getItem('openQuery')) || false,
				is_adminMode: Number(localStorage.getItem('adminMode')) || false,
				is_superadminMode: false,

				// bookmark/history
				bookmarks: [],
				bookmark_addId: false,
				histories: [],
				filter_history: '',

				// queryid
				queryid: '',
				running_queryid: '',
				running_progress: -1,
				running_time: '',
				running_queries: 0,

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

				// users
				traffic: {
					users: 0,
					events: 0,
					hits: [],
					loading: false,
				},
				traffic_ranges: [
					['15 minutes', 15],
					['30 minutes', 30],
					['1 hour', 60],
					['4 hours', 3 * 60],
					['12 hours', 12 * 60],
					['24 hours', 24 * 60],
				],
				traffic_range: 0,

				// demo
				demo: {
					variables: 'SELECT ${x} FROM ${y} LIMIT ${z}',
					chart: 'SELECT * FROM (VALUES(2013,1000,400),(2014,1170,460),(2015,660,1120),(2016,1030,540),(2017,1220,890)) AS t (year,A,B)',
				},
			}
		},
		created: function() {
			var self = this;

			// Migration (v1 -> v2)
			if (location.search && !self.is_share && !self.is_error) {
				location.replace('/#' + location.search + '&tab=result');
				return false;
			}

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
			if (self.domain) {
				$.ajaxSetup({
					headers: {
						'X-yanagishima-datasources': '*'
					},
				});
			}

			// Get datasources
			$.ajax({
				type: 'GET',
				url: self.domain + self.apis.datasource
			}).done(function(data) {
				if (data.datasources && data.datasources.length) {
					self.datasources = data.datasources;
				} else {
					location.replace('/error/?403');
				}
			}).fail(function(xhr, status, error) {});

			// Start
			$('#page').removeClass('unload');

			// iframe Modal
			$('body').magnificPopup({
				delegate: '.link-iframe',
				type: 'iframe'
			});
		},
		mounted: function() {
			var self = this;

			// detect Modal
			$(document).on('shown.bs.modal', '.modal', function(e) {
				self.is_modal = true;
				self.focus = 0;
				self.trm('modal', $(this).attr('id'));
			}).on('hidden.bs.modal', function(e) {
				self.is_modal = false;
				self.focus = 1;
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
				'traffic': [17, 17, 17, 17, 17],
			};
			$(window).keyup(function(e) {
				inputs.push(e.keyCode);
				if (inputs.toString().indexOf(configs.konami) >= 0) {
					self.superadminMode = true;
					toastr.success('You can watch all queries.', 'Super Admin Mode');
				}
				if (inputs.toString().indexOf(configs.traffic) >= 0) {
					inputs = [];
					self.getTraffic();
					$('#traffic').modal('show');
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
			sortedHits: function() {
				var self = this;
				return self.traffic.hits.sortBy(function(n) {
					return n[3];
				}, true);
			},
			is_error: function() {
				return $('body').attr('id') === 'error';
			},
			is_share: function() {
				return $('body').attr('id') === 'share';
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
			filteredHistory: function() {
				var self = this;
				var filter_history = self.filter_history;
				var history = self.response.history.filter(function(n) {
					if (n[1].includes(filter_history)) {
						return n;
					}
				});
				return history;
			},
			variables: function() {
				var self = this;
				var variables = [];
				var detected_variables = self.input_query.match(/\$\{[a-z]([a-z0-9]+)?\}/g);
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
		},
		methods: {
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
					url: self.domain + self.apis.publish.format({
						datasource: self.datasource,
						queryid: queryid
					}),
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
				}).fail(function(xhr, status, error) {});
				self.trm('result', 'publish');
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
					}).fail(function(xhr, status, error) {});
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
								if (columns[0].type === 'date' && /^[0-9]{8}$/.test(n[0])) {
									n[0] = Date.create(n[0]);
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
				}
				Object.map(self.response, function(val, key) {
					self.response[key] = Object.isArray(val) ? [] : '';
				});
				$('[data-toggle="tooltip"]').tooltip('hide');
				self.query = '';
				self.queryString = '';
				self.queryString_collapse = false;
				self.catalog = '';
				self.schema = '';
				self.table = '';
				self.table_type = '';
				self.column_date = '';
				self.catalogs = [];
				self.schemata = [];
				self.tables = [];
				self.columns = [];
				self.filter_schema = '';
				self.filter_user = '';
				self.filter_history = '';
				self.table_q_in = '';
				self.table_q = '';
				self.running_queryid = '';
				self.running_queries = 0;
				self.setTitle();
				self.getHistory();
				self.getBookmark();
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
						self.getHistory();
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
				var q = q || self.table_q_in;
				self.table_q = self.table_q_in;
				self.response.table = [];
				if (q === '') {
					self.is_searchTable = 0;
					return false;
				} else {
					self.is_searchTable = 1;
					self.trm('table_search', q);
				}
				self.loading.table = true;
				$.ajax({
					type: 'POST',
					url: self.domain + self.apis.presto.format({
						datasource: self.datasource
					}),
					data: {
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
					url: self.domain + self.apis.presto.format({
						datasource: self.datasource
					}),
					data: {
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
				}).fail(function(xhr, status, error) {});
			},
			runQuery: function(query, is_bg) {
				var self = this;
				var query = query || self.query;
				var is_bg = is_bg || false;
				var is_checking = true;
				var run_timer;

				self.line = 0;
				self.chart = 0;
				if (!is_bg) {
					if (self.loading.result) {
						return false;
					}
					self.trm('run', query);
				} else {
					self.trm('run-bg', query);
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
						self.trm('variables', self.variables.length);
					}
				}

				self.running_queries++;
				if (!is_bg) {
					self.loading.result = true;
					self.error.result = false;
					self.queryid = '';

					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.prestoAsync.format({
							datasource: self.datasource
						}),
						data: {
							query: query
						}
					}).done(function(data) {
						var queryid = data.queryid;
						if (queryid) {
							self.running_queryid = queryid;
							self.running_progress = -1;
							self.running_time = '';
							clearInterval(run_timer);
							if (self.loading.result) {
								run_timer = setInterval(function() {
									getResult(queryid);
								}, 500);
							}
						} else {
							self.loading.result = false;
							self.error.result = data.error;
							self.running_queries--;
						}
					}).fail(function(xhr, status, error) {
						self.loading.result = false;
						self.error.result = error || true;
						self.running_queries--;
					});
					self.tab = 'result';
				} else {
					toastr.info(query.compact().truncate(64), "Run in the background", {
						timeOut: '1000'
					});
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.presto.format({
							datasource: self.datasource
						}),
						data: {
							store: true,
							query: query
						}
					}).done(function(data) {
						var queryid = data.queryid;
						if (!data.error) {
							toastr.success(query.compact().truncate(64), 'Done (Click Here)', {
								onclick: function() {
									self.queryid = data.queryid;
									self.tab = 'result';
									self.loadResult();
								}
							});
							self.addHistoryItem(queryid);
						} else {
							toastr.error(data.error, "Error");
						}
						self.running_queries--;
					}).fail(function(xhr, status, error) {
						self.running_queries--;
					});
				}

				function getResult(queryid) {
					var def = $.Deferred();
					$.ajax({
						type: 'GET',
						url: self.domain + self.apis.queryStatus.format({
							datasource: self.datasource,
							queryid: queryid
						})
					}).done(function(status) {
						var state = status.state;
						if (state === 'FINISHED' || state === 'FAILED') {
							clearInterval(run_timer);
							self.running_queryid = '';
							self.running_progress = 0;
							self.running_time = '';
							if (state === 'FINISHED') {
								self.addHistoryItem(queryid);
							} else if (state === 'FAILED') {
								if (status.failureInfo.errorLocation) {
									self.gotoline = status.failureInfo.errorLocation.lineNumber;
									self.errorline = status.failureInfo.errorLocation.lineNumber - 1;
									self.errortext = status.failureInfo.message;
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
							var stats = status.queryStats;
							self.running_progress = self.progress(stats, 1);
							self.running_time = stats.elapsedTime;
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
						}
					}).fail(function(xhr, status, error) {
						self.loading.result = false;
						self.error.result = error || true;
						self.running_queries--;
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
				$.ajax({
					type: 'GET',
					url: self.domain + self.apis.history.format({
						datasource: self.datasource,
						queryid: queryid
					}),
					timeout: 300000,
				}).done(function(data) {
					if (!Object.isEmpty(data)) {
						self.queryString = data.queryString;
						self.queryid = queryid;
						self.loading.result = false;
						self.input_query = data.queryString.remove(/^EXPLAIN( \(TYPE DISTRIBUTED\)| \(TYPE VALIDATE\)| ANALYZE|) /i);
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
					self.error.result = error || true;
				});
			},
			abortQuery: function(queryid) {
				var self = this;
				self.killQuery(queryid);
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
				self.input_query = self.snippet.format(config);
				self.trm('snippet', self.snippet);
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
			killQuery: function(queryid) {
				var self = this;
				$.ajax({
					type: 'GET',
					url: self.domain + self.apis.kill.format({
						datasource: self.datasource,
						queryid: queryid
					})
				}).done(function(data) {
					// self.init();
				}).fail(function(xhr, status, error) {});
			},
			formatQuery: function(queryid) {
				var self = this;
				var query = self.input_query;
				if (query) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.format,
						data: {
							query: query
						}
					}).done(function(data) {
						if (data.formattedQuery) {
							self.input_query = data.formattedQuery;
						} else if (data.error) {
							self.errortext = data.error;
							self.errorline = data.errorLineNumber - 1;
						}
					}).fail(function(xhr, status, error) {});
				}
			},
			toValuesQuery: function(values) {
				var self = this;
				if (values) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.toValuesQuery,
						data: {
							csv: values
						}
					}).done(function(data) {
						if (data.error) {
							alert(data.error);
						} else {
							self.input_query = data.query;
						}
					}).fail(function(xhr, status, error) {});
				}
			},
			getBookmark: function() {
				var self = this;
				self.getBookmarkItems();

				var bookmarks = self.bookmarks;
				if (!bookmarks.length) {
					return false;
				}
				self.loading.bookmark = true;
				$.ajax({
					type: 'GET',
					url: self.domain + self.apis.bookmark.format({
						datasource: self.datasource
					}),
					data: {
						bookmark_id: bookmarks.join(',')
					},
					timeout: 300000,
				}).done(function(data) {
					if (data.bookmarkList) {
						self.response.bookmark = data.bookmarkList.sortBy(function(n) {
							return n.bookmark_id;
						}, true);
					}
					self.loading.bookmark = false;
				}).fail(function(xhr, status, error) {
					self.loading.bookmark = false;
				});
			},
			getHistory: function() {
				var self = this;
				self.getHistoryItems();
				var histories = self.histories;
				if (!histories.length) {
					return false;
				}
				self.loading.history = true;
				$.ajax({
					type: 'POST',
					url: self.domain + self.apis.queryHistory.format({
						datasource: self.datasource,
					}),
					data: {
						queryids: histories.join(',')
					},
					timeout: 300000,
				}).done(function(data) {
					if (data.results) {
						self.response.history = data.results.sortBy(function(n) {
							return n[0];
						}, true);
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
				$.ajax({
					type: 'GET',
					url: self.domain + self.apis.query.format({
						datasource: self.datasource,
					}),
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
				clearInterval(self.timer);
				if (enable) {
					self.timer = setInterval(function() {
						time--;
						if (time === 0) {
							if (self.tab === 'qlist' && !self.is_modal) {
								self.getQlist(true);
							}
							time = max;
						}
					}, 1000);
				}
			},
			getTable: function() {
				var self = this;
				$.ajax({
					type: 'GET',
					url: self.domain + self.apis.tableList.format({
						datasource: self.datasource,
						catalog: self.catalog,
					}),
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
			getTree: function() {
				var self = this;
				var catalog = self.catalog;
				var schema = self.schema;
				var table = self.table;
				var table_type = self.table_type;
				var hiddenQuery_prefix = self.hiddenQuery_prefix;
				if (!self.catalogs.length) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.presto.format({
							datasource: self.datasource
						}),
						data: {
							query: '{0}SHOW catalogs'.format(hiddenQuery_prefix)
						}
					}).done(function(data) {
						self.catalogs = data.results.map(function(n) {
							return n[0];
						});
						self.catalog = yanagishima.default_catalog || self.catalogs[0];
					}).fail(function(xhr, status, error) {});
				}
				if (!schema) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.presto.format({
							datasource: self.datasource
						}),
						data: {
							query: '{0}SHOW schemas from {1}'.format(hiddenQuery_prefix, catalog)
						}
					}).done(function(data) {
						self.schemata = data.results.map(function(n) {
							return n[0];
						});
					}).fail(function(xhr, status, error) {});
				} else if (!table) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.presto.format({
							datasource: self.datasource
						}),
						data: {
							query: "{0}SELECT table_name, table_type FROM {1}.information_schema.tables WHERE table_schema='{2}'".format(hiddenQuery_prefix, catalog, schema)
						}
					}).done(function(data) {
						self.tables = data.results.map(function(n) {
							var table_name = n[0];
							var table_type = n[1];
							return [table_name, table_type];
						});
					}).fail(function(xhr, status, error) {});
				} else {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.presto.format({
							datasource: self.datasource
						}),
						data: {
							query: "{0}DESCRIBE {1}".format(hiddenQuery_prefix, [catalog, schema, table].join('.'))
						}
					}).done(function(data) {
						var col_date = '';
						var cols = [];
						self.columns = data.results;
						data.results.map(function(n) {
							if (self.columnDate_names.includes(n[0])) {
								col_date = n[0];
							} else {
								cols.push(n[0]);
							}
						});
						self.cols = col_date ? cols.add(col_date, 0) : cols;
						self.col_date = col_date;
					}).fail(function(xhr, status, error) {});
				}
			},
			isRunning: function(val) {
				return !['FINISHED', 'FAILED', 'CANCELED'].includes(val);
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
				$.ajax({
					type: 'POSt',
					url: self.domain + self.apis.bookmark.format({
						datasource: self.datasource
					}),
					data: {
						query: query
					},
					timeout: 300000,
				}).done(function(data) {
					var bookmark_id = data.bookmark_id;
					self.setItem('bookmarks_' + self.datasource, bookmark_id);
					self.bookmark_addId = bookmark_id;
					self.getBookmarkItems();
					$('#bookmark').modal('show');
				}).fail(function(xhr, status, error) {
					console.log('error');
				});
			},
			delBookmarkItem: function(bookmark_id) {
				var self = this;
				self.delItem('bookmarks_' + self.datasource, String(bookmark_id));
				self.getBookmarkItems();
			},
			delBookmarkAll: function() {
				var self = this;
				if (confirm('Do you want to remove it?')) {
					localStorage.removeItem('bookmarks_' + self.datasource);
					self.getBookmark();
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
					self.getBookmark();
				}
			},
			getHistoryItems: function(max) {
				var self = this;
				var max = max || 256;
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
					self.getHistory();
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
					self.getHistory();
				}
			},
			trm: function(action, label) {
				var self = this;
				var category = self.datasource;
			},
			linkDetail: function(val) {
				var self = this;
				return self.domain + self.apis.detail.format({
					queryid: val,
					datasource: self.datasource
				});
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
					return val;
				}
			},
			extractDate: function(val) {
				if (val) {
					var arr = val.split('_');
					if (arr.length !== 4) {
						return false;
					}
					var ymd = arr[0].insert('/', 4).insert('/', -2);
					var hms = arr[1].insert(':', 2).insert(':', -2);
					return Date.create('{0} {1}'.format(ymd, hms)).addHours(9).format('{yyyy}/{MM}/{dd} {24hr}:{mm}:{ss}');
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
				self.datasource = self.datasource || self.rememberDatasource ? (localStorage.getItem('datasource') || self.datasources[0]) : self.datasources[0];
				self.tab = self.tab || self.tabs[0].id;
			},
			datasource: function(val, oldVal) {
				var self = this;
				if (oldVal !== '') {
					self.queryid = '';
					self.input_query = '';
					if (self.tab !== 'treeview') {
						self.tab = 'qlist';
					}
				}
				self.init();
				self.runTab();
				localStorage.setItem('datasource', val);
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
				self.is_searchTable && self.searchTable();
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
				localStorage.setItem('table', val);
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
			theme: function(val) {
				var self = this;
				localStorage.setItem('theme', val);
				self.trm('theme', val);
			},
			historySize: function(val) {
				var self = this;
				localStorage.setItem('historySize', val);
			},
			setting: function(val) {
				var self = this;
				localStorage.setItem('setting', val);
			},
			rememberDatasource: function(val) {
				var self = this;
				localStorage.setItem('rememberDatasource', Number(val));
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
			is_searchTable: function(val) {
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
					self.getBookmark();
				}
			},
			running_queries: function(val) {
				favicon.badge(val);
			},
			traffic_range: function(val) {
				var self = this;
				self.getTraffic();
			},
		}
	});
});