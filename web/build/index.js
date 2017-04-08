/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;
/******/
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// identity function for calling harmony imports with the correct context
/******/ 	__webpack_require__.i = function(value) { return value; };
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, {
/******/ 				configurable: false,
/******/ 				enumerable: true,
/******/ 				get: getter
/******/ 			});
/******/ 		}
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 18);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports) {

module.exports = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMjAuODcgMTA0LjY3Ij48ZGVmcz48c3R5bGU+LmNscy0xe2ZpbGw6I2ZmZjtzdHJva2U6I2ZmZjtzdHJva2UtbWl0ZXJsaW1pdDo1O3N0cm9rZS13aWR0aDowLjVweDt9PC9zdHlsZT48L2RlZnM+PHRpdGxlPuOCouOCu+ODg+ODiCAyPC90aXRsZT48ZyBpZD0i44Os44Kk44Ok44O8XzIiIGRhdGEtbmFtZT0i44Os44Kk44Ok44O8IDIiPjxnIGlkPSJkYXJrIj48cGF0aCBpZD0ieWFuYWdpc2hpbWEiIGNsYXNzPSJjbHMtMSIgZD0iTTYwLjQzLDM0Ljg5LDQwLjQzLjI1aDQwWk0yMC40My4yNUguNDNsNDAsNjkuMjgsMTAtMTcuMzJabTgwLDAtMzAsNTItMjAsMzQuNjQsMTAsMTcuMzJMMTIwLjQzLjI1WiIvPjwvZz48L2c+PC9zdmc+"

/***/ }),
/* 1 */
/***/ (function(module, exports) {

yanagishima = {
	version: '2.0',
	sitename: 'yanagishima',
	domain: '',
	contact: '',
	apis: {
		datasource: '/datasource',
		presto: '/presto',
		query: '/query',
		history: '/history',
		download: '/download',
		csvdownload: '/csvdownload',
		kill: '/kill',
		detail: '/queryDetail?queryId={queryid}&datasource={datasource}'
	},
	snippets: [{
			label: "SELECT * FROM ... LIMIT 100",
			sql: "SELECT * FROM {catalog}.{schema}.{table} LIMIT 100",
			enable: ['BASE TABLE', 'VIEW']
		},
		{
			label: "SELECT * FROM ... WHERE LATEST PARTITION LIMIT 100",
			sql: "SELECT * FROM {catalog}.{schema}.{table} WHERE DT='{yesterday}' LIMIT 100",
			enable: ['BASE TABLE', 'VIEW']
		},
		{
			label: "SHOW PRESTO VIEW DDL",
			sql: "SELECT VIEW_DEFINITION FROM {catalog}.INFORMATION_SCHEMA.VIEWS WHERE table_catalog='{catalog}' AND table_schema='{schema}' AND table_name='{table}'",
			enable: ['VIEW']
		},
		{
			label: "SHOW CREATE TABLE ...",
			sql: "SHOW CREATE TABLE {catalog}.{schema}.{table}",
			enable: ['BASE TABLE']
		},
		{
			label: "SHOW PARTITIONS FROM ...",
			sql: "SHOW PARTITIONS FROM {catalog}.{schema}.{table}",
			enable: ['BASE TABLE']
		},
		{
			label: "DESCRIBE ...",
			sql: "DESCRIBE {catalog}.{schema}.{table}",
			enable: ['BASE TABLE', 'VIEW']
		}
	]
};

/***/ }),
/* 2 */
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(12);
__webpack_require__(15);
var VueClipboards = __webpack_require__(17);
var favicon = new Favico({
	animation: 'slide'
});

Sugar.extend();
Vue.use(VueClipboards);

jQuery(document).ready(function($) {
	var runXHR;
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
				hashKeys: [
					// key, Omission if no value, Required
					['datasource', false, true],
					['tab', false, true],
					['queryid', true, false],
				],
				loading: {
					qlist: false,
					result: false,
					history: false,
					bookmark: false,
					table: false,
				},
				error: {
					qlist: false,
					result: false,
					history: false,
					table: false,
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
				],
				layouts: [{
						name: 'Slim',
						class: 'container',
					},
					{
						name: 'Wide',
						class: 'container-fluid',
					},
				],
				themes: [
					'chrome',
					'clouds',
					'clouds_midnight',
					'cobalt',
					'crimson_editor',
					'dawn',
					'eclipse',
					'idle_fingers',
					'kr_theme',
					'merbivore',
					'merbivore_soft',
					'mono_industrial',
					'monokai',
					'pastel_on_dark',
					'solarized_dark',
					'solarized_light',
					'textmate',
					'tomorrow',
					'tomorrow_night',
					'tomorrow_night_blue',
					'tomorrow_night_bright',
					'tomorrow_night_eighties',
					'twilight',
					'vibrant_ink',
				],
				theme: localStorage.getItem('theme') || 'chrome',
				tab: '',
				layout: localStorage.getItem('layout') || 'container',
				now: '',
				is_modal: false,

				// setting
				maxlines: [
					16,
					32,
					Infinity
				],
				explains: {
					'explain': 'explain {0}',
					'explain distributed': 'explain (type distributed) {0}',
					'explain analyze': 'explain analyze {0}'
				},
				// treeview
				datasources: [],
				catalogs: [],
				schemata: [],
				tables: [],
				table_q_in: '',
				table_q: '',
				table_result: [],
				datasource: '',
				catalog: '',
				schema: '',
				table: '',
				table_type: '',
				snippets: yanagishima.snippets,
				snippet: yanagishima.snippets[0].sql,

				// query editoer
				qlist: [],
				input_query: '',
				query: '',
				gotoline: 0,
				focus: 1,
				maxline: Number(localStorage.getItem('maxline')) || 16,

				// qlist
				is_autoQlist: Number(localStorage.getItem('autoQlist')) && true,
				refresh_period: 1,
				filter_user: '',

				// bookmark/history
				bookmarks: [],
				bookmarkQlist: [],
				histories: [],
				historyQlist: [],

				// result
				queryid: '',
				showResult: '',
				result: {},
				explainResult: '',
				responses: {
					normal: '',
					show: '',
				},
				sample: '',
			}
		},
		created: function() {
			var self = this;
			// Migration (v1 -> v2)
			if (location.search) {
				location.replace('/#' + location.search + '&tab=result');
				return false;
			}

			// AIP
			!location.hash && self.fromDataHash(false);
			$(window).on('hashchange', function() {
				self.toDataHash();
			}).trigger('hashchange');

			// Get datasources
			$.getJSON(self.domain + self.apis.datasource, function(result) {
				self.datasources = result.datasources;
			})

			// Start
			$('#page').removeClass('unload');

			// iframe Modal
			$('body').magnificPopup({
				delegate: '.link_detail',
				type: 'iframe'
			});

			// detect Modal
			$(document).on('shown.bs.modal', '.modal', function(e) {
				self.is_modal = true;
				self.focus = 0;
			}).on('hidden.bs.modal', function(e) {
				self.is_modal = false;
				self.focus = 1;
			});

			$(document).on('shown.bs.modal', '#treeview', function(e) {
				self.getTree();
				$('#table_q_in').focus();
			}).on('hidden.bs.modal', function(e) {});

			$(document).on('show.bs.modal', '#bookmark', function(e) {
				self.getBookmark();
				$('button').blur();
			}).on('hide.bs.modal', function(e) {});

			// Hotkey
			$(window).keydown(function(e) {
				if (e.ctrlKey) {
					if (e.keyCode === 84) { // T
						$('#treeview').modal('show');
						e.preventDefault();
					}
				}
				if (self.is_modal) {
					(e.keyCode === 27) && $('.modal').modal('hide'); // ESC
				}
			});

			// detect Loading job
			$(window).on('beforeunload', function(event) {
				if (self.Loading.result) {
					return confirm('Do you want to do it?');
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
						var ommit = n[1];
						if ((ommit && self[key]) || !ommit) {
							config[key] = self[key];
						}
					});
					return Object.toQueryString(config, {
						deep: true
					});
				}
			},
			orderdQlist: function() {
				var self = this;
				var filter_user = self.filter_user;
				var qlist = self.qlist.filter(function(n) {
					if (filter_user === '' || filter_user === n.session.user) {
						return n;
					}
				});
				return qlist.sortBy(function(n) {
					return self.isRunning(n.state);
				}, true);
			},
			orderdFailQlist: function() {
				var self = this;
				var filter_user = self.filter_user;
				var qlist = self.qlist.filter(function(n) {
					if (filter_user === '' || filter_user === n.session.user && n.state === 'FAILED') {
						return n;
					}
				});
				return qlist;
			},
			explain: function() {
				var self = this;
				if (self.result && self.result.results) {
					var arr = [];
					self.result.results.map(function(n) {
						// arr.push(n[0]);
						arr.push(n[0].replace(/ {4}/g, ' '));
					});
					return arr.join('<br>')
				} else {
					return '';
				}
			},
			placeholder: function() {
				return 'Search by Table name in ' + this.catalog;
			},
		},
		methods: {
			init: function(val) {
				var self = this;
				self.result = '';
				self.responses = {
					normal: '',
					show: '',
					explain: ''
				};
				self.query = '';
				self.catalog = '';
				self.schema = '';
				self.table = '';
				self.table_type = '';
				self.catalogs = [];
				self.schemata = [];
				self.tables = [];
				self.filter_user = '';
				self.table_q_in = '',
					self.table_q = '',
					self.setTitle()
				self.searchTable();
				self.getTree();
				self.getBookmarkItems();
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
					pageTitle += ' #' + self.shortID(self.queryid);
				}
				document.title = '[{1}] {2} - {0}'.format(self.sitename, subTitle, pageTitle);
			},
			runTab: function() {
				var self = this;
				var tab = self.tab;
				self.setTitle();
				switch (tab) {
					case 'qlist':
						self.getQlist();
						self.autoQlist(self.is_autoQlist);
						break;
					case 'history':
						self.getHistory();
						break;
					case 'result':
						self.loadResult()
						break;
				}
			},
			changeQuery: function(query) {
				var self = this;
				(self.input_query !== query) && (self.input_query = query);
			},
			showQuery: function(query) {
				var self = this;
				var query = query || self.query;
				$.ajax({
					type: 'POST',
					url: self.domain + self.apis.presto,
					data: {
						datasource: self.datasource,
						query: query
					}
				}).done(function(result) {
					if (/^show partitions /i.test(query)) {
						result.results && result.results.sortBy(function(n) {
							return n[0];
						}, true);
					}
					self.responses.show = result;
					$('#show').modal('show');
				}).fail(function(xhr, status, error) {});
			},
			searchTable: function(q) {
				var self = this;
				var q = q || self.table_q_in;
				self.table_q = self.table_q_in;
				if (q === '') {
					self.table_result = [];
					return false;
				}
				self.loading.table = true;
				$.ajax({
					type: 'POST',
					url: self.domain + self.apis.presto,
					data: {
						datasource: self.datasource,
						query: "SELECT table_catalog, table_schema, table_name, table_type FROM {catalog}.information_schema.tables WHERE table_name LIKE '%{table}%'".format({
							catalog: self.catalog,
							table: q
						})
					}
				}).done(function(result) {
					self.table_result = result.results;
					self.loading.table = false;
				}).fail(function(xhr, status, error) {
					self.loading.table = false;
				});
			},
			loadResult: function(queryid) {
				var self = this;
				if (!self.loading.result) {
					self.loadQuery(self.queryid);
					// self.queryid = queryid;
				}
				self.tab = 'result';
			},
			runQuery: function(query) {
				var self = this;
				var query = query || self.query;
				if (/^show /i.test(query)) {
					self.showQuery(query);
					return false;
				}
				self.loading.result = true;
				self.error.result = false;
				self.queryid = '';
				favicon.badge(1);
				runXHR = $.ajax({
					type: 'POST',
					url: self.domain + self.apis.presto,
					timeout: 300000,
					data: {
						datasource: self.datasource,
						query: query
					}
				}).done(function(result) {
					self.query = query;
					self.result = result;
					self.responses.normal = result;
					self.loading.result = false;
					favicon.badge(0);
					self.gotoline = result.errorLineNumber || 0;
					self.queryid = result.errorLineNumber ? '' : result.queryid;
					if (!result.errorLineNumber && result.queryid) {
						self.addHistoryItem(result.queryid);
					}
					$(document).scrollTop(0);
				}).fail(function(xhr, status, error) {
					favicon.badge(0);
					self.loading.result = false;
					self.error.result = error || true;
				});
				self.tab = 'result';
			},
			loadQuery: function(queryid) {
				var self = this;
				var queryid = queryid || self.queryid;
				if (!queryid) {
					return false;
				}
				self.loading.result = true;
				self.error.result = false;
				runXHR = $.ajax({
					type: 'GET',
					url: self.domain + self.apis.history,
					timeout: 300000,
					data: {
						datasource: self.datasource,
						queryid: queryid
					}
				}).done(function(result) {
					self.result = result;
					self.responses.normal = result;
					self.query = result.queryString;
					self.queryid = queryid;
					self.loading.result = false;
					$(document).scrollTop(0);
				}).fail(function(xhr, status, error) {
					self.loading.result = false;
					self.error.result = error || true;
				});
			},
			abortQuery: function() {
				runXHR.abort();
				self.tab = 'qlist';
				self.queryid = '';
			},
			setSnippet: function() {
				var self = this;
				var config = {
					catalog: self.catalog,
					schema: self.schema,
					table: self.table,
					yesterday: Date.create().addDays(-1).format('{yyyy}{MM}{dd}')
				};
				self.input_query = self.snippet.format(config);
			},
			runSnippet: function() {
				var self = this;
				self.setSnippet();
				self.runQuery(self.input_query);
			},
			download: function(queryid, is_csv) {
				var self = this;
				var api = is_csv ? self.apis.csvdownload : self.apis.download;
				return '{api}?datasource={datasource}&queryid={queryid}'.format({
					api: self.domain + api,
					datasource: self.datasource,
					queryid: queryid
				});
			},
			killQuery: function(queryid) {
				var self = this;
				if (confirm('Do you want to do it?')) {
					$.ajax({
						type: 'GET',
						url: self.domain + self.apis.kill,
						data: {
							datasource: self.datasource,
							queryId: queryid
						}
					}).done(function(result) {
						self.getQlist();
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
				var ajaxs = [];
				// bookmarks.first(15).map(function(n) {
				bookmarks.first(15).map(function(n) {
					ajaxs.push(
						$.ajax({
							type: 'GET',
							url: self.domain + self.apis.history,
							data: {
								datasource: self.datasource,
								queryid: n
							}
						})
					);
				});
				$.when.apply(
					$, ajaxs
				).done(function() {
					var results = arguments,
						queries = [];
					bookmarks.map(function(val, key) {
						var result = (bookmarks.length === 1) ? results[0] : results[key] && results[key][0];
						if (result) {
							queries.push({
								queryid: val,
								query: result.queryString
							});
							self.bookmarkQlist = queries;
						}
					});
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
				var ajaxs = [];
				// histories.first(15).map(function(n) {
				histories.map(function(n) {
					ajaxs.push(
						$.ajax({
							type: 'GET',
							url: self.domain + self.apis.history,
							data: {
								datasource: self.datasource,
								queryid: n
							}
						})
					);
				});
				$.when.apply(
					$, ajaxs
				).done(function() {
					var results = arguments,
						queries = [];
					histories.map(function(val, key) {
						var result = (histories.length === 1) ? results[0] : results[key] && results[key][0];
						if (result) {
							queries.push({
								queryid: val,
								query: result.queryString,
								rawDataSize: result.rawDataSize,
							});
							self.historyQlist = queries;
						}
					});
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
					url: self.domain + self.apis.query,
					data: {
						datasource: self.datasource,
					}
				}).done(function(result) {
					self.qlist = result;
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
			getTree: function() {
				var self = this;
				var catalog = self.catalog;
				var schema = self.schema;
				var table = self.table;
				if (!catalog) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.presto,
						data: {
							datasource: self.datasource,
							query: 'show catalogs'
						}
					}).done(function(result) {
						self.catalogs = result.results.map(function(n) {
							return n[0];
						});
						self.catalog = 'hive'; // for DataLabs
					}).fail(function(xhr, status, error) {});
				} else if (!schema) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.presto,
						data: {
							datasource: self.datasource,
							query: 'show schemas from {0}'.format(catalog)
						}
					}).done(function(result) {
						self.schemata = result.results.map(function(n) {
							return n[0];
						});
					}).fail(function(xhr, status, error) {});
				} else if (!table) {
					$.ajax({
						type: 'POST',
						url: self.domain + self.apis.presto,
						data: {
							datasource: self.datasource,
							query: "SELECT table_name, table_type FROM {0}.information_schema.tables WHERE table_schema='{1}'".format(catalog, schema)
						}
					}).done(function(result) {
						self.tables = result.results.map(function(n) {
							var table_name = n[0];
							var table_type = n[1];
							return [table_name, table_type];
						});
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
				self.bookmarks = self.getItem('bookmarks_' + self.datasource, max);
			},
			addBookmarkItem: function(queryid) {
				var self = this;
				self.setItem('bookmarks_' + self.datasource, queryid);
				self.getBookmarkItems();
			},
			delBookmarkItem: function(item) {
				var self = this;
				self.delItem('bookmarks_' + self.datasource, item);
				self.getBookmarkItems();
			},
			getHistoryItems: function(max) {
				var self = this;
				var max = max || 100;
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
				if (confirm('Do you want to do it?')) {
					localStorage.removeItem('histories_' + self.datasource);
					self.getHistory();
				}
			},
			linkDetail: function(val) {
				var self = this;
				return self.domain + self.apis.detail.format({
					queryid: val,
					datasource: self.datasource
				});
			},
			shortID: function(val) {
				if (val) {
					return val.split('_')[2];
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
		},
		watch: {
			hash: function() {
				var self = this;
				self.fromDataHash();
				self.setTitle();
			},
			datasources: function(val) {
				var self = this;
				self.datasource = self.datasource || self.datasources[0];
				self.tab = self.tab || self.tabs[0].id;
			},
			datasource: function(val) {
				var self = this;
				self.init();
				self.runTab();
				localStorage.setItem('datasource', val);
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
			catalog: function(val) {
				var self = this;
				self.getTree();
				localStorage.setItem('catalog', val);
			},
			schema: function(val) {
				var self = this;
				self.getTree();
				localStorage.setItem('schema', val);
			},
			table: function(val) {
				var self = this;
				localStorage.setItem('table', val);
			},
			table_result: function(val) {
				var self = this;
				if (!val.length) {
					self.catalog = 'hive';
					self.schema = '';
					self.table = '';
				}
			},
			is_autoQlist: function(val) {
				var self = this;
				self.autoQlist(val);
				localStorage.setItem('autoQlist', Number(val));
			},
			maxline: function(val) {
				var self = this;
				localStorage.setItem('maxline', val);
			},
			layout: function(val) {
				var self = this;
				localStorage.setItem('layout', val);
			},
			theme: function(val) {
				var self = this;
				localStorage.setItem('theme', val);
			},
		}
	});
});

/***/ }),
/* 3 */
/***/ (function(module, exports) {

// Vue component for Ace Editor
Vue.component('ace', {
	template: '<div :id="aceId" style="width: 100%; height: 100%;"></div>',
	props: ['code', 'lang', 'theme', 'readonly', 'gotoline', 'focus', 'maxline', 'css_class'],
	data: function() {
		return {
			ace: Object,
			aceId: 'ace-' + this._uid,
			prevCode: ''
		}
	},
	watch: {
		'code': function(value) {
			if (this.prevCode !== value) {
				this.ace.setValue(value, 1);
			}
		},
		'gotoline': function(value, oldValue) {
			if (oldValue !== value) {
				if (value) {
					this.ace.gotoLine(value);
				}
			}
		},
		'focus': function(value, oldValue) {
			value && this.ace.focus();
		},
		'maxline': function(value, oldValue) {
			this.ace.setOptions({
				maxLines: value,
			});
		},
		'theme': function(value, oldValue) {
			this.ace.setTheme(`ace/theme/` + value)
		},
	},
	mounted: function() {
		var self = this;
		var lang = self.lang || 'sql';
		var theme = self.theme || 'chrome';
		var style = self.css_class || '';
		var readonly = Boolean(self.readonly) || false;

		self.ace = window.ace.edit(self.aceId);

		self.ace.setShowPrintMargin(false);
		self.ace.setTheme(`ace/theme/` + theme)
		self.ace.getSession().setMode(`ace/mode/` + lang)
		self.ace.getSession().setUseWrapMode(true);
		self.ace.setFontSize(13);
		self.ace.setValue(self.code, 1);
		self.ace.setStyle(style);
		self.ace.commands.bindKey("Ctrl-P", "golineup");
		self.ace.commands.bindKey("Ctrl-T", console.log());

		if (readonly) {
			self.ace.setReadOnly(readonly);
			self.ace.renderer.setShowGutter(false);
			self.ace.setHighlightActiveLine(false);
			self.ace.setOptions({
				minLines: 1,
				maxLines: 8
			});
			self.ace.gotoLine(1);
			self.ace.renderer.$cursorLayer.element.style.display = "none"
		} else {
			self.ace.setOptions({
				minLines: 5,
				maxLines: self.maxline || 16
			});
			self.focus && self.ace.focus();
			self.ace.on('change', function() {
				self.prevCode = self.ace.getValue();
				self.$emit('change-code', self.ace.getValue());
			});
			self.ace.commands.addCommand({
				name: 'run',
				bindKey: {
					win: 'Ctrl-Enter',
					mac: 'Ctrl-Enter'
				},
				exec: function() {
					self.$emit('run-code', self.ace.getValue());
				}
			});
		}
	}
});

/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;(function (global, factory) {
    if (true) {
        !(__WEBPACK_AMD_DEFINE_ARRAY__ = [module, __webpack_require__(13)], __WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__)) : __WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
    } else if (typeof exports !== "undefined") {
        factory(module, require('select'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod, global.select);
        global.clipboardAction = mod.exports;
    }
})(this, function (module, _select) {
    'use strict';

    var _select2 = _interopRequireDefault(_select);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
        return typeof obj;
    } : function (obj) {
        return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
    };

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var _createClass = function () {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;
                if ("value" in descriptor) descriptor.writable = true;
                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function (Constructor, protoProps, staticProps) {
            if (protoProps) defineProperties(Constructor.prototype, protoProps);
            if (staticProps) defineProperties(Constructor, staticProps);
            return Constructor;
        };
    }();

    var ClipboardAction = function () {
        /**
         * @param {Object} options
         */
        function ClipboardAction(options) {
            _classCallCheck(this, ClipboardAction);

            this.resolveOptions(options);
            this.initSelection();
        }

        /**
         * Defines base properties passed from constructor.
         * @param {Object} options
         */


        _createClass(ClipboardAction, [{
            key: 'resolveOptions',
            value: function resolveOptions() {
                var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

                this.action = options.action;
                this.emitter = options.emitter;
                this.target = options.target;
                this.text = options.text;
                this.trigger = options.trigger;

                this.selectedText = '';
            }
        }, {
            key: 'initSelection',
            value: function initSelection() {
                if (this.text) {
                    this.selectFake();
                } else if (this.target) {
                    this.selectTarget();
                }
            }
        }, {
            key: 'selectFake',
            value: function selectFake() {
                var _this = this;

                var isRTL = document.documentElement.getAttribute('dir') == 'rtl';

                this.removeFake();

                this.fakeHandlerCallback = function () {
                    return _this.removeFake();
                };
                this.fakeHandler = document.body.addEventListener('click', this.fakeHandlerCallback) || true;

                this.fakeElem = document.createElement('textarea');
                // Prevent zooming on iOS
                this.fakeElem.style.fontSize = '12pt';
                // Reset box model
                this.fakeElem.style.border = '0';
                this.fakeElem.style.padding = '0';
                this.fakeElem.style.margin = '0';
                // Move element out of screen horizontally
                this.fakeElem.style.position = 'absolute';
                this.fakeElem.style[isRTL ? 'right' : 'left'] = '-9999px';
                // Move element to the same position vertically
                var yPosition = window.pageYOffset || document.documentElement.scrollTop;
                this.fakeElem.style.top = yPosition + 'px';

                this.fakeElem.setAttribute('readonly', '');
                this.fakeElem.value = this.text;

                document.body.appendChild(this.fakeElem);

                this.selectedText = (0, _select2.default)(this.fakeElem);
                this.copyText();
            }
        }, {
            key: 'removeFake',
            value: function removeFake() {
                if (this.fakeHandler) {
                    document.body.removeEventListener('click', this.fakeHandlerCallback);
                    this.fakeHandler = null;
                    this.fakeHandlerCallback = null;
                }

                if (this.fakeElem) {
                    document.body.removeChild(this.fakeElem);
                    this.fakeElem = null;
                }
            }
        }, {
            key: 'selectTarget',
            value: function selectTarget() {
                this.selectedText = (0, _select2.default)(this.target);
                this.copyText();
            }
        }, {
            key: 'copyText',
            value: function copyText() {
                var succeeded = void 0;

                try {
                    succeeded = document.execCommand(this.action);
                } catch (err) {
                    succeeded = false;
                }

                this.handleResult(succeeded);
            }
        }, {
            key: 'handleResult',
            value: function handleResult(succeeded) {
                this.emitter.emit(succeeded ? 'success' : 'error', {
                    action: this.action,
                    text: this.selectedText,
                    trigger: this.trigger,
                    clearSelection: this.clearSelection.bind(this)
                });
            }
        }, {
            key: 'clearSelection',
            value: function clearSelection() {
                if (this.target) {
                    this.target.blur();
                }

                window.getSelection().removeAllRanges();
            }
        }, {
            key: 'destroy',
            value: function destroy() {
                this.removeFake();
            }
        }, {
            key: 'action',
            set: function set() {
                var action = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'copy';

                this._action = action;

                if (this._action !== 'copy' && this._action !== 'cut') {
                    throw new Error('Invalid "action" value, use either "copy" or "cut"');
                }
            },
            get: function get() {
                return this._action;
            }
        }, {
            key: 'target',
            set: function set(target) {
                if (target !== undefined) {
                    if (target && (typeof target === 'undefined' ? 'undefined' : _typeof(target)) === 'object' && target.nodeType === 1) {
                        if (this.action === 'copy' && target.hasAttribute('disabled')) {
                            throw new Error('Invalid "target" attribute. Please use "readonly" instead of "disabled" attribute');
                        }

                        if (this.action === 'cut' && (target.hasAttribute('readonly') || target.hasAttribute('disabled'))) {
                            throw new Error('Invalid "target" attribute. You can\'t cut text from elements with "readonly" or "disabled" attributes');
                        }

                        this._target = target;
                    } else {
                        throw new Error('Invalid "target" value, use a valid Element');
                    }
                }
            },
            get: function get() {
                return this._target;
            }
        }]);

        return ClipboardAction;
    }();

    module.exports = ClipboardAction;
});

/***/ }),
/* 5 */
/***/ (function(module, exports, __webpack_require__) {

var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;(function (global, factory) {
    if (true) {
        !(__WEBPACK_AMD_DEFINE_ARRAY__ = [module, __webpack_require__(4), __webpack_require__(16), __webpack_require__(11)], __WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__)) : __WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
    } else if (typeof exports !== "undefined") {
        factory(module, require('./clipboard-action'), require('tiny-emitter'), require('good-listener'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod, global.clipboardAction, global.tinyEmitter, global.goodListener);
        global.clipboard = mod.exports;
    }
})(this, function (module, _clipboardAction, _tinyEmitter, _goodListener) {
    'use strict';

    var _clipboardAction2 = _interopRequireDefault(_clipboardAction);

    var _tinyEmitter2 = _interopRequireDefault(_tinyEmitter);

    var _goodListener2 = _interopRequireDefault(_goodListener);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var _createClass = function () {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;
                if ("value" in descriptor) descriptor.writable = true;
                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function (Constructor, protoProps, staticProps) {
            if (protoProps) defineProperties(Constructor.prototype, protoProps);
            if (staticProps) defineProperties(Constructor, staticProps);
            return Constructor;
        };
    }();

    function _possibleConstructorReturn(self, call) {
        if (!self) {
            throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
        }

        return call && (typeof call === "object" || typeof call === "function") ? call : self;
    }

    function _inherits(subClass, superClass) {
        if (typeof superClass !== "function" && superClass !== null) {
            throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
        }

        subClass.prototype = Object.create(superClass && superClass.prototype, {
            constructor: {
                value: subClass,
                enumerable: false,
                writable: true,
                configurable: true
            }
        });
        if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
    }

    var Clipboard = function (_Emitter) {
        _inherits(Clipboard, _Emitter);

        /**
         * @param {String|HTMLElement|HTMLCollection|NodeList} trigger
         * @param {Object} options
         */
        function Clipboard(trigger, options) {
            _classCallCheck(this, Clipboard);

            var _this = _possibleConstructorReturn(this, (Clipboard.__proto__ || Object.getPrototypeOf(Clipboard)).call(this));

            _this.resolveOptions(options);
            _this.listenClick(trigger);
            return _this;
        }

        /**
         * Defines if attributes would be resolved using internal setter functions
         * or custom functions that were passed in the constructor.
         * @param {Object} options
         */


        _createClass(Clipboard, [{
            key: 'resolveOptions',
            value: function resolveOptions() {
                var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

                this.action = typeof options.action === 'function' ? options.action : this.defaultAction;
                this.target = typeof options.target === 'function' ? options.target : this.defaultTarget;
                this.text = typeof options.text === 'function' ? options.text : this.defaultText;
            }
        }, {
            key: 'listenClick',
            value: function listenClick(trigger) {
                var _this2 = this;

                this.listener = (0, _goodListener2.default)(trigger, 'click', function (e) {
                    return _this2.onClick(e);
                });
            }
        }, {
            key: 'onClick',
            value: function onClick(e) {
                var trigger = e.delegateTarget || e.currentTarget;

                if (this.clipboardAction) {
                    this.clipboardAction = null;
                }

                this.clipboardAction = new _clipboardAction2.default({
                    action: this.action(trigger),
                    target: this.target(trigger),
                    text: this.text(trigger),
                    trigger: trigger,
                    emitter: this
                });
            }
        }, {
            key: 'defaultAction',
            value: function defaultAction(trigger) {
                return getAttributeValue('action', trigger);
            }
        }, {
            key: 'defaultTarget',
            value: function defaultTarget(trigger) {
                var selector = getAttributeValue('target', trigger);

                if (selector) {
                    return document.querySelector(selector);
                }
            }
        }, {
            key: 'defaultText',
            value: function defaultText(trigger) {
                return getAttributeValue('text', trigger);
            }
        }, {
            key: 'destroy',
            value: function destroy() {
                this.listener.destroy();

                if (this.clipboardAction) {
                    this.clipboardAction.destroy();
                    this.clipboardAction = null;
                }
            }
        }], [{
            key: 'isSupported',
            value: function isSupported() {
                var action = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : ['copy', 'cut'];

                var actions = typeof action === 'string' ? [action] : action;
                var support = !!document.queryCommandSupported;

                actions.forEach(function (action) {
                    support = support && !!document.queryCommandSupported(action);
                });

                return support;
            }
        }]);

        return Clipboard;
    }(_tinyEmitter2.default);

    /**
     * Helper function to retrieve attribute value.
     * @param {String} suffix
     * @param {Element} element
     */
    function getAttributeValue(suffix, element) {
        var attribute = 'data-clipboard-' + suffix;

        if (!element.hasAttribute(attribute)) {
            return;
        }

        return element.getAttribute(attribute);
    }

    module.exports = Clipboard;
});

/***/ }),
/* 6 */
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(7)();
// imports


// module
exports.push([module.i, "/*!\n * Bootstrap v4.0.0-alpha.6 (https://getbootstrap.com)\n * Copyright 2011-2017 The Bootstrap Authors\n * Copyright 2011-2017 Twitter, Inc.\n * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)\n */\n.datasource_0 #header #header-main {\n  background: linear-gradient(#00529a 0%, #006dcd 100%); }\n  .datasource_0 #header #header-main .btn:not(.btn-secondary) {\n    background: #003d74; }\n\n.datasource_0-color {\n  color: #004B8D; }\n\n.datasource_1 #header #header-main {\n  background: linear-gradient(#2b3940 0%, #40545e 100%); }\n  .datasource_1 #header #header-main .btn:not(.btn-secondary) {\n    background: #1c2429; }\n\n.datasource_1-color {\n  color: #263238; }\n\n.datasource_2 #header #header-main {\n  background: linear-gradient(#563933 0%, #764f46 100%); }\n  .datasource_2 #header #header-main .btn:not(.btn-secondary) {\n    background: #3e2925; }\n\n.datasource_2-color {\n  color: #4E342E; }\n\n.datasource_3 #header #header-main {\n  background: linear-gradient(#005a4b 0%, #008d75 100%); }\n  .datasource_3 #header #header-main .btn:not(.btn-secondary) {\n    background: #00342b; }\n\n.datasource_3-color {\n  color: #004D40; }\n\n.datasource_4 #header #header-main {\n  background: linear-gradient(#351d9d 0%, #4325c8 100%); }\n  .datasource_4 #header #header-main .btn:not(.btn-secondary) {\n    background: #2a177c; }\n\n.datasource_4-color {\n  color: #311B92; }\n\n#header #header-main {\n  color: #fff; }\n\n#header #header-sub {\n  background: #f7f7f9;\n  padding-bottom: 1rem; }\n\n#header #logo {\n  font-size: 1rem;\n  font-weight: bold;\n  margin-bottom: 0; }\n  #header #logo a {\n    color: #fff;\n    text-decoration: none; }\n  #header #logo #logo-figure {\n    display: inline-block;\n    vertical-align: middle;\n    width: 1.2rem;\n    height: 1.6rem;\n    background: url(" + __webpack_require__(0) + ") 0 no-repeat;\n    background-size: contain; }\n\n#header .nav-tabs {\n  border-bottom: 0; }\n\n#header .nav-link {\n  border-width: 0;\n  color: #eee; }\n  #header .nav-link:hover, #header .nav-link:focus {\n    color: #fff; }\n  #header .nav-link.active {\n    color: #004B8D; }\n\n#content {\n  min-height: 640px;\n  background: url(" + __webpack_require__(0) + ") 50% 65% no-repeat;\n  background-size: 320px;\n  background-attachment: fixed;\n  position: relative; }\n\n#treeview .list-group {\n  max-height: 345px;\n  overflow-y: scroll; }\n  #treeview .list-group .list-group-item {\n    flex: none; }\n\n#explain {\n  background: #000;\n  color: #fff;\n  padding: 0.5rem; }\n\n.editor {\n  border-radius: 0.15rem;\n  border: 1px solid #ccc;\n  background: #fff; }\n\n.table.table-auto {\n  width: auto;\n  max-width: auto; }\n\n.table.table-fixed {\n  table-layout: fixed; }\n  .table.table-fixed th,\n  .table.table-fixed td {\n    overflow: hidden;\n    white-space: nowrap;\n    text-overflow: ellipsis; }\n\n.table thead {\n  background: #dee2e3; }\n\n.table tbody {\n  background-color: #fff; }\n\n.card-header + .list-group {\n  margin-top: -1px; }\n\n.ellipsis {\n  overflow: hidden;\n  white-space: nowrap;\n  text-overflow: ellipsis; }\n\n.monospace {\n  font-family: \"Monaco\", \"Menlo\", \"Ubuntu Mono\", \"Consolas\", \"source-code-pro\", monospace; }\n\n.modal-body {\n  background: #f7f7f9; }\n\n.btn.clip {\n  position: absolute;\n  margin-top: -0.1rem;\n  right: 0.4rem;\n  display: none;\n  z-index: 10; }\n\na:hover .btn.clip {\n  display: inline-block; }\n\n.table-base {\n  color: #004B8D; }\n\n.table-view {\n  color: #2196F3; }\n\n.mfp-iframe-holder {\n  padding-top: 0;\n  padding-bottom: 0; }\n\n.mfp-iframe-holder .mfp-content {\n  height: 90%;\n  max-width: 1280px; }\n\n.clone {\n  position: fixed;\n  color: #fff;\n  text-shadow: 1px 1px 1px #000;\n  padding: 0.5rem;\n  background: #000;\n  z-index: 15; }\n\n.pointer {\n  cursor: pointer; }\n\n.bg-transparent {\n  background: transparent !important; }\n\n/*! normalize.css v5.0.0 | MIT License | github.com/necolas/normalize.css */\nhtml {\n  font-family: sans-serif;\n  line-height: 1.15;\n  -ms-text-size-adjust: 100%;\n  -webkit-text-size-adjust: 100%; }\n\nbody {\n  margin: 0; }\n\narticle,\naside,\nfooter,\nheader,\nnav,\nsection {\n  display: block; }\n\nh1 {\n  font-size: 2em;\n  margin: 0.67em 0; }\n\nfigcaption,\nfigure,\nmain {\n  display: block; }\n\nfigure {\n  margin: 1em 40px; }\n\nhr {\n  box-sizing: content-box;\n  height: 0;\n  overflow: visible; }\n\npre {\n  font-family: monospace, monospace;\n  font-size: 1em; }\n\na {\n  background-color: transparent;\n  -webkit-text-decoration-skip: objects; }\n\na:active,\na:hover {\n  outline-width: 0; }\n\nabbr[title] {\n  border-bottom: none;\n  text-decoration: underline;\n  text-decoration: underline dotted; }\n\nb,\nstrong {\n  font-weight: inherit; }\n\nb,\nstrong {\n  font-weight: bolder; }\n\ncode,\nkbd,\nsamp {\n  font-family: monospace, monospace;\n  font-size: 1em; }\n\ndfn {\n  font-style: italic; }\n\nmark {\n  background-color: #ff0;\n  color: #000; }\n\nsmall {\n  font-size: 80%; }\n\nsub,\nsup {\n  font-size: 75%;\n  line-height: 0;\n  position: relative;\n  vertical-align: baseline; }\n\nsub {\n  bottom: -0.25em; }\n\nsup {\n  top: -0.5em; }\n\naudio,\nvideo {\n  display: inline-block; }\n\naudio:not([controls]) {\n  display: none;\n  height: 0; }\n\nimg {\n  border-style: none; }\n\nsvg:not(:root) {\n  overflow: hidden; }\n\nbutton,\ninput,\noptgroup,\nselect,\ntextarea {\n  font-family: sans-serif;\n  font-size: 100%;\n  line-height: 1.15;\n  margin: 0; }\n\nbutton,\ninput {\n  overflow: visible; }\n\nbutton,\nselect {\n  text-transform: none; }\n\nbutton,\nhtml [type=\"button\"],\n[type=\"reset\"],\n[type=\"submit\"] {\n  -webkit-appearance: button; }\n\nbutton::-moz-focus-inner,\n[type=\"button\"]::-moz-focus-inner,\n[type=\"reset\"]::-moz-focus-inner,\n[type=\"submit\"]::-moz-focus-inner {\n  border-style: none;\n  padding: 0; }\n\nbutton:-moz-focusring,\n[type=\"button\"]:-moz-focusring,\n[type=\"reset\"]:-moz-focusring,\n[type=\"submit\"]:-moz-focusring {\n  outline: 1px dotted ButtonText; }\n\nfieldset {\n  border: 1px solid #c0c0c0;\n  margin: 0 2px;\n  padding: 0.35em 0.625em 0.75em; }\n\nlegend {\n  box-sizing: border-box;\n  color: inherit;\n  display: table;\n  max-width: 100%;\n  padding: 0;\n  white-space: normal; }\n\nprogress {\n  display: inline-block;\n  vertical-align: baseline; }\n\ntextarea {\n  overflow: auto; }\n\n[type=\"checkbox\"],\n[type=\"radio\"] {\n  box-sizing: border-box;\n  padding: 0; }\n\n[type=\"number\"]::-webkit-inner-spin-button,\n[type=\"number\"]::-webkit-outer-spin-button {\n  height: auto; }\n\n[type=\"search\"] {\n  -webkit-appearance: textfield;\n  outline-offset: -2px; }\n\n[type=\"search\"]::-webkit-search-cancel-button,\n[type=\"search\"]::-webkit-search-decoration {\n  -webkit-appearance: none; }\n\n::-webkit-file-upload-button {\n  -webkit-appearance: button;\n  font: inherit; }\n\ndetails,\nmenu {\n  display: block; }\n\nsummary {\n  display: list-item; }\n\ncanvas {\n  display: inline-block; }\n\ntemplate {\n  display: none; }\n\n[hidden] {\n  display: none; }\n\n@media print {\n  *,\n  *::before,\n  *::after,\n  p::first-letter,\n  div::first-letter,\n  blockquote::first-letter,\n  li::first-letter,\n  p::first-line,\n  div::first-line,\n  blockquote::first-line,\n  li::first-line {\n    text-shadow: none !important;\n    box-shadow: none !important; }\n  a,\n  a:visited {\n    text-decoration: underline; }\n  abbr[title]::after {\n    content: \" (\" attr(title) \")\"; }\n  pre {\n    white-space: pre-wrap !important; }\n  pre,\n  blockquote {\n    border: 1px solid #999;\n    page-break-inside: avoid; }\n  thead {\n    display: table-header-group; }\n  tr,\n  img {\n    page-break-inside: avoid; }\n  p,\n  h2,\n  h3 {\n    orphans: 3;\n    widows: 3; }\n  h2,\n  h3 {\n    page-break-after: avoid; }\n  .navbar {\n    display: none; }\n  .badge {\n    border: 1px solid #000; }\n  .table {\n    border-collapse: collapse !important; }\n    .table td,\n    .table th {\n      background-color: #fff !important; }\n  .table-bordered th,\n  .table-bordered td {\n    border: 1px solid #ddd !important; } }\n\nhtml {\n  box-sizing: border-box; }\n\n*,\n*::before,\n*::after {\n  box-sizing: inherit; }\n\n@-ms-viewport {\n  width: device-width; }\n\nhtml {\n  -ms-overflow-style: scrollbar;\n  -webkit-tap-highlight-color: transparent; }\n\nbody {\n  font-family: \"Droid Sans\", Roboto, \"Helvetica Neue\", Arial, sans-serif;\n  font-size: 0.8rem;\n  font-weight: normal;\n  line-height: 1.5;\n  color: #292b2c;\n  background-color: #f7f7f9; }\n\n[tabindex=\"-1\"]:focus {\n  outline: none !important; }\n\nh1, h2, h3, h4, h5, h6 {\n  margin-top: 0;\n  margin-bottom: .5rem; }\n\np {\n  margin-top: 0;\n  margin-bottom: 1rem; }\n\nabbr[title],\nabbr[data-original-title] {\n  cursor: help; }\n\naddress {\n  margin-bottom: 1rem;\n  font-style: normal;\n  line-height: inherit; }\n\nol,\nul,\ndl {\n  margin-top: 0;\n  margin-bottom: 1rem; }\n\nol ol,\nul ul,\nol ul,\nul ol {\n  margin-bottom: 0; }\n\ndt {\n  font-weight: bold; }\n\ndd {\n  margin-bottom: .5rem;\n  margin-left: 0; }\n\nblockquote {\n  margin: 0 0 1rem; }\n\na {\n  color: #004B8D;\n  text-decoration: none; }\n  a:focus, a:hover {\n    color: #002241;\n    text-decoration: underline; }\n\na:not([href]):not([tabindex]) {\n  color: inherit;\n  text-decoration: none; }\n  a:not([href]):not([tabindex]):focus, a:not([href]):not([tabindex]):hover {\n    color: inherit;\n    text-decoration: none; }\n  a:not([href]):not([tabindex]):focus {\n    outline: 0; }\n\npre {\n  margin-top: 0;\n  margin-bottom: 1rem;\n  overflow: auto; }\n\nfigure {\n  margin: 0 0 1rem; }\n\nimg {\n  vertical-align: middle; }\n\nbutton,\n[role=\"button\"] {\n  cursor: pointer; }\n\na,\narea,\nbutton,\n[role=\"button\"],\ninput,\nlabel,\nselect,\nsummary,\ntextarea {\n  touch-action: manipulation; }\n\ntable {\n  border-collapse: collapse;\n  background-color: transparent;\n  font-family: inherit;\n  font-size: inherit; }\n\ncaption {\n  padding-top: 0.4rem 0.6rem;\n  padding-bottom: 0.4rem 0.6rem;\n  color: #999;\n  text-align: left;\n  caption-side: bottom; }\n\nth {\n  text-align: left; }\n\nlabel {\n  display: inline-block;\n  margin-bottom: .5rem; }\n\nbutton:focus {\n  outline: 1px dotted;\n  outline: 5px auto -webkit-focus-ring-color; }\n\ninput,\nbutton,\nselect,\ntextarea {\n  line-height: inherit;\n  font-family: inherit; }\n\ninput[type=\"radio\"]:disabled,\ninput[type=\"checkbox\"]:disabled {\n  cursor: not-allowed; }\n\ninput[type=\"date\"],\ninput[type=\"time\"],\ninput[type=\"datetime-local\"],\ninput[type=\"month\"] {\n  -webkit-appearance: listbox; }\n\ntextarea {\n  resize: vertical; }\n\nfieldset {\n  min-width: 0;\n  padding: 0;\n  margin: 0;\n  border: 0; }\n\nlegend {\n  display: block;\n  width: 100%;\n  padding: 0;\n  margin-bottom: .5rem;\n  font-size: 1.5rem;\n  line-height: inherit; }\n\ninput[type=\"search\"] {\n  -webkit-appearance: none; }\n\noutput {\n  display: inline-block; }\n\n[hidden] {\n  display: none !important; }\n\nh1, h2, h3, h4, h5, h6,\n.h1, .h2, .h3, .h4, .h5, .h6 {\n  margin-bottom: 0.5rem;\n  font-family: inherit;\n  font-weight: 500;\n  line-height: 1.1;\n  color: inherit; }\n\nh1, .h1 {\n  font-size: 1.1rem; }\n\nh2, .h2 {\n  font-size: 1.05rem; }\n\nh3, .h3 {\n  font-size: 1rem; }\n\nh4, .h4 {\n  font-size: 0.95rem; }\n\nh5, .h5 {\n  font-size: 0.9rem; }\n\nh6, .h6 {\n  font-size: 0.85rem; }\n\n.lead {\n  font-size: 1.25rem;\n  font-weight: 300; }\n\n.display-1 {\n  font-size: 6rem;\n  font-weight: 300;\n  line-height: 1.1; }\n\n.display-2 {\n  font-size: 5.5rem;\n  font-weight: 300;\n  line-height: 1.1; }\n\n.display-3 {\n  font-size: 4.5rem;\n  font-weight: 300;\n  line-height: 1.1; }\n\n.display-4 {\n  font-size: 3.5rem;\n  font-weight: 300;\n  line-height: 1.1; }\n\nhr {\n  margin-top: 1rem;\n  margin-bottom: 1rem;\n  border: 0;\n  border-top: 1px solid rgba(0, 0, 0, 0.1); }\n\nsmall,\n.small {\n  font-size: 80%;\n  font-weight: normal; }\n\nmark,\n.mark {\n  padding: 0.2em;\n  background-color: #fcf8e3; }\n\n.list-unstyled {\n  padding-left: 0;\n  list-style: none; }\n\n.list-inline {\n  padding-left: 0;\n  list-style: none; }\n\n.list-inline-item {\n  display: inline-block; }\n  .list-inline-item:not(:last-child) {\n    margin-right: 5px; }\n\n.initialism {\n  font-size: 90%;\n  text-transform: uppercase; }\n\n.blockquote {\n  padding: 0.5rem 1rem;\n  margin-bottom: 1rem;\n  font-size: 1rem;\n  border-left: 0.25rem solid #eceeef; }\n\n.blockquote-footer {\n  display: block;\n  font-size: 80%;\n  color: #aaa; }\n  .blockquote-footer::before {\n    content: \"\\2014   \\A0\"; }\n\n.blockquote-reverse {\n  padding-right: 1rem;\n  padding-left: 0;\n  text-align: right;\n  border-right: 0.25rem solid #eceeef;\n  border-left: 0; }\n\n.blockquote-reverse .blockquote-footer::before {\n  content: \"\"; }\n\n.blockquote-reverse .blockquote-footer::after {\n  content: \"\\A0   \\2014\"; }\n\n.img-fluid {\n  max-width: 100%;\n  height: auto; }\n\n.img-thumbnail {\n  padding: 0.25rem;\n  background-color: #f7f7f9;\n  border: 1px solid #ddd;\n  border-radius: 0.15rem;\n  transition: all 0.2s ease-in-out;\n  max-width: 100%;\n  height: auto; }\n\n.figure {\n  display: inline-block; }\n\n.figure-img {\n  margin-bottom: 0.5rem;\n  line-height: 1; }\n\n.figure-caption {\n  font-size: 90%;\n  color: #aaa; }\n\ncode,\nkbd,\npre,\nsamp {\n  font-family: \"Monaco\", \"Menlo\", \"Ubuntu Mono\", \"Consolas\", \"source-code-pro\", monospace; }\n\ncode {\n  padding: 0.2rem 0.4rem;\n  font-size: 90%;\n  color: #bd4147;\n  background-color: #f7f7f9;\n  border-radius: 0.15rem; }\n  a > code {\n    padding: 0;\n    color: inherit;\n    background-color: inherit; }\n\nkbd {\n  padding: 0.2rem 0.4rem;\n  font-size: 90%;\n  color: #fff;\n  background-color: #292b2c;\n  border-radius: 0.1rem; }\n  kbd kbd {\n    padding: 0;\n    font-size: 100%;\n    font-weight: bold; }\n\npre {\n  display: block;\n  margin-top: 0;\n  margin-bottom: 1rem;\n  font-size: 90%;\n  color: #292b2c; }\n  pre code {\n    padding: 0;\n    font-size: inherit;\n    color: inherit;\n    background-color: transparent;\n    border-radius: 0; }\n\n.pre-scrollable {\n  max-height: 340px;\n  overflow-y: scroll; }\n\n.container {\n  position: relative;\n  margin-left: auto;\n  margin-right: auto;\n  padding-right: 8px;\n  padding-left: 8px; }\n  @media (min-width: 576px) {\n    .container {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 768px) {\n    .container {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 992px) {\n    .container {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 1340px) {\n    .container {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 576px) {\n    .container {\n      width: 540px;\n      max-width: 100%; } }\n  @media (min-width: 768px) {\n    .container {\n      width: 720px;\n      max-width: 100%; } }\n  @media (min-width: 992px) {\n    .container {\n      width: 960px;\n      max-width: 100%; } }\n  @media (min-width: 1340px) {\n    .container {\n      width: 1280px;\n      max-width: 100%; } }\n\n.container-fluid {\n  position: relative;\n  margin-left: auto;\n  margin-right: auto;\n  padding-right: 8px;\n  padding-left: 8px; }\n  @media (min-width: 576px) {\n    .container-fluid {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 768px) {\n    .container-fluid {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 992px) {\n    .container-fluid {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 1340px) {\n    .container-fluid {\n      padding-right: 8px;\n      padding-left: 8px; } }\n\n.row {\n  display: flex;\n  flex-wrap: wrap;\n  margin-right: -8px;\n  margin-left: -8px; }\n  @media (min-width: 576px) {\n    .row {\n      margin-right: -8px;\n      margin-left: -8px; } }\n  @media (min-width: 768px) {\n    .row {\n      margin-right: -8px;\n      margin-left: -8px; } }\n  @media (min-width: 992px) {\n    .row {\n      margin-right: -8px;\n      margin-left: -8px; } }\n  @media (min-width: 1340px) {\n    .row {\n      margin-right: -8px;\n      margin-left: -8px; } }\n\n.no-gutters {\n  margin-right: 0;\n  margin-left: 0; }\n  .no-gutters > .col,\n  .no-gutters > [class*=\"col-\"] {\n    padding-right: 0;\n    padding-left: 0; }\n\n.col-1, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-10, .col-11, .col-12, .col, .col-sm-1, .col-sm-2, .col-sm-3, .col-sm-4, .col-sm-5, .col-sm-6, .col-sm-7, .col-sm-8, .col-sm-9, .col-sm-10, .col-sm-11, .col-sm-12, .col-sm, .col-md-1, .col-md-2, .col-md-3, .col-md-4, .col-md-5, .col-md-6, .col-md-7, .col-md-8, .col-md-9, .col-md-10, .col-md-11, .col-md-12, .col-md, .col-lg-1, .col-lg-2, .col-lg-3, .col-lg-4, .col-lg-5, .col-lg-6, .col-lg-7, .col-lg-8, .col-lg-9, .col-lg-10, .col-lg-11, .col-lg-12, .col-lg, .col-xl-1, .col-xl-2, .col-xl-3, .col-xl-4, .col-xl-5, .col-xl-6, .col-xl-7, .col-xl-8, .col-xl-9, .col-xl-10, .col-xl-11, .col-xl-12, .col-xl {\n  position: relative;\n  width: 100%;\n  min-height: 1px;\n  padding-right: 8px;\n  padding-left: 8px; }\n  @media (min-width: 576px) {\n    .col-1, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-10, .col-11, .col-12, .col, .col-sm-1, .col-sm-2, .col-sm-3, .col-sm-4, .col-sm-5, .col-sm-6, .col-sm-7, .col-sm-8, .col-sm-9, .col-sm-10, .col-sm-11, .col-sm-12, .col-sm, .col-md-1, .col-md-2, .col-md-3, .col-md-4, .col-md-5, .col-md-6, .col-md-7, .col-md-8, .col-md-9, .col-md-10, .col-md-11, .col-md-12, .col-md, .col-lg-1, .col-lg-2, .col-lg-3, .col-lg-4, .col-lg-5, .col-lg-6, .col-lg-7, .col-lg-8, .col-lg-9, .col-lg-10, .col-lg-11, .col-lg-12, .col-lg, .col-xl-1, .col-xl-2, .col-xl-3, .col-xl-4, .col-xl-5, .col-xl-6, .col-xl-7, .col-xl-8, .col-xl-9, .col-xl-10, .col-xl-11, .col-xl-12, .col-xl {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 768px) {\n    .col-1, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-10, .col-11, .col-12, .col, .col-sm-1, .col-sm-2, .col-sm-3, .col-sm-4, .col-sm-5, .col-sm-6, .col-sm-7, .col-sm-8, .col-sm-9, .col-sm-10, .col-sm-11, .col-sm-12, .col-sm, .col-md-1, .col-md-2, .col-md-3, .col-md-4, .col-md-5, .col-md-6, .col-md-7, .col-md-8, .col-md-9, .col-md-10, .col-md-11, .col-md-12, .col-md, .col-lg-1, .col-lg-2, .col-lg-3, .col-lg-4, .col-lg-5, .col-lg-6, .col-lg-7, .col-lg-8, .col-lg-9, .col-lg-10, .col-lg-11, .col-lg-12, .col-lg, .col-xl-1, .col-xl-2, .col-xl-3, .col-xl-4, .col-xl-5, .col-xl-6, .col-xl-7, .col-xl-8, .col-xl-9, .col-xl-10, .col-xl-11, .col-xl-12, .col-xl {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 992px) {\n    .col-1, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-10, .col-11, .col-12, .col, .col-sm-1, .col-sm-2, .col-sm-3, .col-sm-4, .col-sm-5, .col-sm-6, .col-sm-7, .col-sm-8, .col-sm-9, .col-sm-10, .col-sm-11, .col-sm-12, .col-sm, .col-md-1, .col-md-2, .col-md-3, .col-md-4, .col-md-5, .col-md-6, .col-md-7, .col-md-8, .col-md-9, .col-md-10, .col-md-11, .col-md-12, .col-md, .col-lg-1, .col-lg-2, .col-lg-3, .col-lg-4, .col-lg-5, .col-lg-6, .col-lg-7, .col-lg-8, .col-lg-9, .col-lg-10, .col-lg-11, .col-lg-12, .col-lg, .col-xl-1, .col-xl-2, .col-xl-3, .col-xl-4, .col-xl-5, .col-xl-6, .col-xl-7, .col-xl-8, .col-xl-9, .col-xl-10, .col-xl-11, .col-xl-12, .col-xl {\n      padding-right: 8px;\n      padding-left: 8px; } }\n  @media (min-width: 1340px) {\n    .col-1, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-10, .col-11, .col-12, .col, .col-sm-1, .col-sm-2, .col-sm-3, .col-sm-4, .col-sm-5, .col-sm-6, .col-sm-7, .col-sm-8, .col-sm-9, .col-sm-10, .col-sm-11, .col-sm-12, .col-sm, .col-md-1, .col-md-2, .col-md-3, .col-md-4, .col-md-5, .col-md-6, .col-md-7, .col-md-8, .col-md-9, .col-md-10, .col-md-11, .col-md-12, .col-md, .col-lg-1, .col-lg-2, .col-lg-3, .col-lg-4, .col-lg-5, .col-lg-6, .col-lg-7, .col-lg-8, .col-lg-9, .col-lg-10, .col-lg-11, .col-lg-12, .col-lg, .col-xl-1, .col-xl-2, .col-xl-3, .col-xl-4, .col-xl-5, .col-xl-6, .col-xl-7, .col-xl-8, .col-xl-9, .col-xl-10, .col-xl-11, .col-xl-12, .col-xl {\n      padding-right: 8px;\n      padding-left: 8px; } }\n\n.col {\n  flex-basis: 0;\n  flex-grow: 1;\n  max-width: 100%; }\n\n.col-auto {\n  flex: 0 0 auto;\n  width: auto; }\n\n.col-1 {\n  flex: 0 0 8.33333%;\n  max-width: 8.33333%; }\n\n.col-2 {\n  flex: 0 0 16.66667%;\n  max-width: 16.66667%; }\n\n.col-3 {\n  flex: 0 0 25%;\n  max-width: 25%; }\n\n.col-4 {\n  flex: 0 0 33.33333%;\n  max-width: 33.33333%; }\n\n.col-5 {\n  flex: 0 0 41.66667%;\n  max-width: 41.66667%; }\n\n.col-6 {\n  flex: 0 0 50%;\n  max-width: 50%; }\n\n.col-7 {\n  flex: 0 0 58.33333%;\n  max-width: 58.33333%; }\n\n.col-8 {\n  flex: 0 0 66.66667%;\n  max-width: 66.66667%; }\n\n.col-9 {\n  flex: 0 0 75%;\n  max-width: 75%; }\n\n.col-10 {\n  flex: 0 0 83.33333%;\n  max-width: 83.33333%; }\n\n.col-11 {\n  flex: 0 0 91.66667%;\n  max-width: 91.66667%; }\n\n.col-12 {\n  flex: 0 0 100%;\n  max-width: 100%; }\n\n.pull-0 {\n  right: auto; }\n\n.pull-1 {\n  right: 8.33333%; }\n\n.pull-2 {\n  right: 16.66667%; }\n\n.pull-3 {\n  right: 25%; }\n\n.pull-4 {\n  right: 33.33333%; }\n\n.pull-5 {\n  right: 41.66667%; }\n\n.pull-6 {\n  right: 50%; }\n\n.pull-7 {\n  right: 58.33333%; }\n\n.pull-8 {\n  right: 66.66667%; }\n\n.pull-9 {\n  right: 75%; }\n\n.pull-10 {\n  right: 83.33333%; }\n\n.pull-11 {\n  right: 91.66667%; }\n\n.pull-12 {\n  right: 100%; }\n\n.push-0 {\n  left: auto; }\n\n.push-1 {\n  left: 8.33333%; }\n\n.push-2 {\n  left: 16.66667%; }\n\n.push-3 {\n  left: 25%; }\n\n.push-4 {\n  left: 33.33333%; }\n\n.push-5 {\n  left: 41.66667%; }\n\n.push-6 {\n  left: 50%; }\n\n.push-7 {\n  left: 58.33333%; }\n\n.push-8 {\n  left: 66.66667%; }\n\n.push-9 {\n  left: 75%; }\n\n.push-10 {\n  left: 83.33333%; }\n\n.push-11 {\n  left: 91.66667%; }\n\n.push-12 {\n  left: 100%; }\n\n.offset-1 {\n  margin-left: 8.33333%; }\n\n.offset-2 {\n  margin-left: 16.66667%; }\n\n.offset-3 {\n  margin-left: 25%; }\n\n.offset-4 {\n  margin-left: 33.33333%; }\n\n.offset-5 {\n  margin-left: 41.66667%; }\n\n.offset-6 {\n  margin-left: 50%; }\n\n.offset-7 {\n  margin-left: 58.33333%; }\n\n.offset-8 {\n  margin-left: 66.66667%; }\n\n.offset-9 {\n  margin-left: 75%; }\n\n.offset-10 {\n  margin-left: 83.33333%; }\n\n.offset-11 {\n  margin-left: 91.66667%; }\n\n@media (min-width: 576px) {\n  .col-sm {\n    flex-basis: 0;\n    flex-grow: 1;\n    max-width: 100%; }\n  .col-sm-auto {\n    flex: 0 0 auto;\n    width: auto; }\n  .col-sm-1 {\n    flex: 0 0 8.33333%;\n    max-width: 8.33333%; }\n  .col-sm-2 {\n    flex: 0 0 16.66667%;\n    max-width: 16.66667%; }\n  .col-sm-3 {\n    flex: 0 0 25%;\n    max-width: 25%; }\n  .col-sm-4 {\n    flex: 0 0 33.33333%;\n    max-width: 33.33333%; }\n  .col-sm-5 {\n    flex: 0 0 41.66667%;\n    max-width: 41.66667%; }\n  .col-sm-6 {\n    flex: 0 0 50%;\n    max-width: 50%; }\n  .col-sm-7 {\n    flex: 0 0 58.33333%;\n    max-width: 58.33333%; }\n  .col-sm-8 {\n    flex: 0 0 66.66667%;\n    max-width: 66.66667%; }\n  .col-sm-9 {\n    flex: 0 0 75%;\n    max-width: 75%; }\n  .col-sm-10 {\n    flex: 0 0 83.33333%;\n    max-width: 83.33333%; }\n  .col-sm-11 {\n    flex: 0 0 91.66667%;\n    max-width: 91.66667%; }\n  .col-sm-12 {\n    flex: 0 0 100%;\n    max-width: 100%; }\n  .pull-sm-0 {\n    right: auto; }\n  .pull-sm-1 {\n    right: 8.33333%; }\n  .pull-sm-2 {\n    right: 16.66667%; }\n  .pull-sm-3 {\n    right: 25%; }\n  .pull-sm-4 {\n    right: 33.33333%; }\n  .pull-sm-5 {\n    right: 41.66667%; }\n  .pull-sm-6 {\n    right: 50%; }\n  .pull-sm-7 {\n    right: 58.33333%; }\n  .pull-sm-8 {\n    right: 66.66667%; }\n  .pull-sm-9 {\n    right: 75%; }\n  .pull-sm-10 {\n    right: 83.33333%; }\n  .pull-sm-11 {\n    right: 91.66667%; }\n  .pull-sm-12 {\n    right: 100%; }\n  .push-sm-0 {\n    left: auto; }\n  .push-sm-1 {\n    left: 8.33333%; }\n  .push-sm-2 {\n    left: 16.66667%; }\n  .push-sm-3 {\n    left: 25%; }\n  .push-sm-4 {\n    left: 33.33333%; }\n  .push-sm-5 {\n    left: 41.66667%; }\n  .push-sm-6 {\n    left: 50%; }\n  .push-sm-7 {\n    left: 58.33333%; }\n  .push-sm-8 {\n    left: 66.66667%; }\n  .push-sm-9 {\n    left: 75%; }\n  .push-sm-10 {\n    left: 83.33333%; }\n  .push-sm-11 {\n    left: 91.66667%; }\n  .push-sm-12 {\n    left: 100%; }\n  .offset-sm-0 {\n    margin-left: 0%; }\n  .offset-sm-1 {\n    margin-left: 8.33333%; }\n  .offset-sm-2 {\n    margin-left: 16.66667%; }\n  .offset-sm-3 {\n    margin-left: 25%; }\n  .offset-sm-4 {\n    margin-left: 33.33333%; }\n  .offset-sm-5 {\n    margin-left: 41.66667%; }\n  .offset-sm-6 {\n    margin-left: 50%; }\n  .offset-sm-7 {\n    margin-left: 58.33333%; }\n  .offset-sm-8 {\n    margin-left: 66.66667%; }\n  .offset-sm-9 {\n    margin-left: 75%; }\n  .offset-sm-10 {\n    margin-left: 83.33333%; }\n  .offset-sm-11 {\n    margin-left: 91.66667%; } }\n\n@media (min-width: 768px) {\n  .col-md {\n    flex-basis: 0;\n    flex-grow: 1;\n    max-width: 100%; }\n  .col-md-auto {\n    flex: 0 0 auto;\n    width: auto; }\n  .col-md-1 {\n    flex: 0 0 8.33333%;\n    max-width: 8.33333%; }\n  .col-md-2 {\n    flex: 0 0 16.66667%;\n    max-width: 16.66667%; }\n  .col-md-3 {\n    flex: 0 0 25%;\n    max-width: 25%; }\n  .col-md-4 {\n    flex: 0 0 33.33333%;\n    max-width: 33.33333%; }\n  .col-md-5 {\n    flex: 0 0 41.66667%;\n    max-width: 41.66667%; }\n  .col-md-6 {\n    flex: 0 0 50%;\n    max-width: 50%; }\n  .col-md-7 {\n    flex: 0 0 58.33333%;\n    max-width: 58.33333%; }\n  .col-md-8 {\n    flex: 0 0 66.66667%;\n    max-width: 66.66667%; }\n  .col-md-9 {\n    flex: 0 0 75%;\n    max-width: 75%; }\n  .col-md-10 {\n    flex: 0 0 83.33333%;\n    max-width: 83.33333%; }\n  .col-md-11 {\n    flex: 0 0 91.66667%;\n    max-width: 91.66667%; }\n  .col-md-12 {\n    flex: 0 0 100%;\n    max-width: 100%; }\n  .pull-md-0 {\n    right: auto; }\n  .pull-md-1 {\n    right: 8.33333%; }\n  .pull-md-2 {\n    right: 16.66667%; }\n  .pull-md-3 {\n    right: 25%; }\n  .pull-md-4 {\n    right: 33.33333%; }\n  .pull-md-5 {\n    right: 41.66667%; }\n  .pull-md-6 {\n    right: 50%; }\n  .pull-md-7 {\n    right: 58.33333%; }\n  .pull-md-8 {\n    right: 66.66667%; }\n  .pull-md-9 {\n    right: 75%; }\n  .pull-md-10 {\n    right: 83.33333%; }\n  .pull-md-11 {\n    right: 91.66667%; }\n  .pull-md-12 {\n    right: 100%; }\n  .push-md-0 {\n    left: auto; }\n  .push-md-1 {\n    left: 8.33333%; }\n  .push-md-2 {\n    left: 16.66667%; }\n  .push-md-3 {\n    left: 25%; }\n  .push-md-4 {\n    left: 33.33333%; }\n  .push-md-5 {\n    left: 41.66667%; }\n  .push-md-6 {\n    left: 50%; }\n  .push-md-7 {\n    left: 58.33333%; }\n  .push-md-8 {\n    left: 66.66667%; }\n  .push-md-9 {\n    left: 75%; }\n  .push-md-10 {\n    left: 83.33333%; }\n  .push-md-11 {\n    left: 91.66667%; }\n  .push-md-12 {\n    left: 100%; }\n  .offset-md-0 {\n    margin-left: 0%; }\n  .offset-md-1 {\n    margin-left: 8.33333%; }\n  .offset-md-2 {\n    margin-left: 16.66667%; }\n  .offset-md-3 {\n    margin-left: 25%; }\n  .offset-md-4 {\n    margin-left: 33.33333%; }\n  .offset-md-5 {\n    margin-left: 41.66667%; }\n  .offset-md-6 {\n    margin-left: 50%; }\n  .offset-md-7 {\n    margin-left: 58.33333%; }\n  .offset-md-8 {\n    margin-left: 66.66667%; }\n  .offset-md-9 {\n    margin-left: 75%; }\n  .offset-md-10 {\n    margin-left: 83.33333%; }\n  .offset-md-11 {\n    margin-left: 91.66667%; } }\n\n@media (min-width: 992px) {\n  .col-lg {\n    flex-basis: 0;\n    flex-grow: 1;\n    max-width: 100%; }\n  .col-lg-auto {\n    flex: 0 0 auto;\n    width: auto; }\n  .col-lg-1 {\n    flex: 0 0 8.33333%;\n    max-width: 8.33333%; }\n  .col-lg-2 {\n    flex: 0 0 16.66667%;\n    max-width: 16.66667%; }\n  .col-lg-3 {\n    flex: 0 0 25%;\n    max-width: 25%; }\n  .col-lg-4 {\n    flex: 0 0 33.33333%;\n    max-width: 33.33333%; }\n  .col-lg-5 {\n    flex: 0 0 41.66667%;\n    max-width: 41.66667%; }\n  .col-lg-6 {\n    flex: 0 0 50%;\n    max-width: 50%; }\n  .col-lg-7 {\n    flex: 0 0 58.33333%;\n    max-width: 58.33333%; }\n  .col-lg-8 {\n    flex: 0 0 66.66667%;\n    max-width: 66.66667%; }\n  .col-lg-9 {\n    flex: 0 0 75%;\n    max-width: 75%; }\n  .col-lg-10 {\n    flex: 0 0 83.33333%;\n    max-width: 83.33333%; }\n  .col-lg-11 {\n    flex: 0 0 91.66667%;\n    max-width: 91.66667%; }\n  .col-lg-12 {\n    flex: 0 0 100%;\n    max-width: 100%; }\n  .pull-lg-0 {\n    right: auto; }\n  .pull-lg-1 {\n    right: 8.33333%; }\n  .pull-lg-2 {\n    right: 16.66667%; }\n  .pull-lg-3 {\n    right: 25%; }\n  .pull-lg-4 {\n    right: 33.33333%; }\n  .pull-lg-5 {\n    right: 41.66667%; }\n  .pull-lg-6 {\n    right: 50%; }\n  .pull-lg-7 {\n    right: 58.33333%; }\n  .pull-lg-8 {\n    right: 66.66667%; }\n  .pull-lg-9 {\n    right: 75%; }\n  .pull-lg-10 {\n    right: 83.33333%; }\n  .pull-lg-11 {\n    right: 91.66667%; }\n  .pull-lg-12 {\n    right: 100%; }\n  .push-lg-0 {\n    left: auto; }\n  .push-lg-1 {\n    left: 8.33333%; }\n  .push-lg-2 {\n    left: 16.66667%; }\n  .push-lg-3 {\n    left: 25%; }\n  .push-lg-4 {\n    left: 33.33333%; }\n  .push-lg-5 {\n    left: 41.66667%; }\n  .push-lg-6 {\n    left: 50%; }\n  .push-lg-7 {\n    left: 58.33333%; }\n  .push-lg-8 {\n    left: 66.66667%; }\n  .push-lg-9 {\n    left: 75%; }\n  .push-lg-10 {\n    left: 83.33333%; }\n  .push-lg-11 {\n    left: 91.66667%; }\n  .push-lg-12 {\n    left: 100%; }\n  .offset-lg-0 {\n    margin-left: 0%; }\n  .offset-lg-1 {\n    margin-left: 8.33333%; }\n  .offset-lg-2 {\n    margin-left: 16.66667%; }\n  .offset-lg-3 {\n    margin-left: 25%; }\n  .offset-lg-4 {\n    margin-left: 33.33333%; }\n  .offset-lg-5 {\n    margin-left: 41.66667%; }\n  .offset-lg-6 {\n    margin-left: 50%; }\n  .offset-lg-7 {\n    margin-left: 58.33333%; }\n  .offset-lg-8 {\n    margin-left: 66.66667%; }\n  .offset-lg-9 {\n    margin-left: 75%; }\n  .offset-lg-10 {\n    margin-left: 83.33333%; }\n  .offset-lg-11 {\n    margin-left: 91.66667%; } }\n\n@media (min-width: 1340px) {\n  .col-xl {\n    flex-basis: 0;\n    flex-grow: 1;\n    max-width: 100%; }\n  .col-xl-auto {\n    flex: 0 0 auto;\n    width: auto; }\n  .col-xl-1 {\n    flex: 0 0 8.33333%;\n    max-width: 8.33333%; }\n  .col-xl-2 {\n    flex: 0 0 16.66667%;\n    max-width: 16.66667%; }\n  .col-xl-3 {\n    flex: 0 0 25%;\n    max-width: 25%; }\n  .col-xl-4 {\n    flex: 0 0 33.33333%;\n    max-width: 33.33333%; }\n  .col-xl-5 {\n    flex: 0 0 41.66667%;\n    max-width: 41.66667%; }\n  .col-xl-6 {\n    flex: 0 0 50%;\n    max-width: 50%; }\n  .col-xl-7 {\n    flex: 0 0 58.33333%;\n    max-width: 58.33333%; }\n  .col-xl-8 {\n    flex: 0 0 66.66667%;\n    max-width: 66.66667%; }\n  .col-xl-9 {\n    flex: 0 0 75%;\n    max-width: 75%; }\n  .col-xl-10 {\n    flex: 0 0 83.33333%;\n    max-width: 83.33333%; }\n  .col-xl-11 {\n    flex: 0 0 91.66667%;\n    max-width: 91.66667%; }\n  .col-xl-12 {\n    flex: 0 0 100%;\n    max-width: 100%; }\n  .pull-xl-0 {\n    right: auto; }\n  .pull-xl-1 {\n    right: 8.33333%; }\n  .pull-xl-2 {\n    right: 16.66667%; }\n  .pull-xl-3 {\n    right: 25%; }\n  .pull-xl-4 {\n    right: 33.33333%; }\n  .pull-xl-5 {\n    right: 41.66667%; }\n  .pull-xl-6 {\n    right: 50%; }\n  .pull-xl-7 {\n    right: 58.33333%; }\n  .pull-xl-8 {\n    right: 66.66667%; }\n  .pull-xl-9 {\n    right: 75%; }\n  .pull-xl-10 {\n    right: 83.33333%; }\n  .pull-xl-11 {\n    right: 91.66667%; }\n  .pull-xl-12 {\n    right: 100%; }\n  .push-xl-0 {\n    left: auto; }\n  .push-xl-1 {\n    left: 8.33333%; }\n  .push-xl-2 {\n    left: 16.66667%; }\n  .push-xl-3 {\n    left: 25%; }\n  .push-xl-4 {\n    left: 33.33333%; }\n  .push-xl-5 {\n    left: 41.66667%; }\n  .push-xl-6 {\n    left: 50%; }\n  .push-xl-7 {\n    left: 58.33333%; }\n  .push-xl-8 {\n    left: 66.66667%; }\n  .push-xl-9 {\n    left: 75%; }\n  .push-xl-10 {\n    left: 83.33333%; }\n  .push-xl-11 {\n    left: 91.66667%; }\n  .push-xl-12 {\n    left: 100%; }\n  .offset-xl-0 {\n    margin-left: 0%; }\n  .offset-xl-1 {\n    margin-left: 8.33333%; }\n  .offset-xl-2 {\n    margin-left: 16.66667%; }\n  .offset-xl-3 {\n    margin-left: 25%; }\n  .offset-xl-4 {\n    margin-left: 33.33333%; }\n  .offset-xl-5 {\n    margin-left: 41.66667%; }\n  .offset-xl-6 {\n    margin-left: 50%; }\n  .offset-xl-7 {\n    margin-left: 58.33333%; }\n  .offset-xl-8 {\n    margin-left: 66.66667%; }\n  .offset-xl-9 {\n    margin-left: 75%; }\n  .offset-xl-10 {\n    margin-left: 83.33333%; }\n  .offset-xl-11 {\n    margin-left: 91.66667%; } }\n\n.table {\n  width: 100%;\n  max-width: 100%;\n  margin-bottom: 1rem; }\n  .table th,\n  .table td {\n    padding: 0.4rem 0.6rem;\n    vertical-align: top;\n    border-top: 1px solid #eceeef; }\n  .table thead th {\n    vertical-align: bottom;\n    border-bottom: 2px solid #eceeef; }\n  .table tbody + tbody {\n    border-top: 2px solid #eceeef; }\n  .table .table {\n    background-color: #f7f7f9; }\n\n.table-sm th,\n.table-sm td {\n  padding: 0.25rem; }\n\n.table-bordered {\n  border: 1px solid #eceeef; }\n  .table-bordered th,\n  .table-bordered td {\n    border: 1px solid #eceeef; }\n  .table-bordered thead th,\n  .table-bordered thead td {\n    border-bottom-width: 2px; }\n\n.table-striped tbody tr:nth-of-type(odd) {\n  background-color: rgba(0, 0, 0, 0.1); }\n\n.table-hover tbody tr:hover {\n  background-color: rgba(0, 0, 0, 0.03); }\n\n.table-active,\n.table-active > th,\n.table-active > td {\n  background-color: rgba(0, 0, 0, 0.03); }\n\n.table-hover .table-active:hover {\n  background-color: rgba(0, 0, 0, 0.03); }\n  .table-hover .table-active:hover > td,\n  .table-hover .table-active:hover > th {\n    background-color: rgba(0, 0, 0, 0.03); }\n\n.table-success,\n.table-success > th,\n.table-success > td {\n  background-color: #dff0d8; }\n\n.table-hover .table-success:hover {\n  background-color: #d0e9c6; }\n  .table-hover .table-success:hover > td,\n  .table-hover .table-success:hover > th {\n    background-color: #d0e9c6; }\n\n.table-info,\n.table-info > th,\n.table-info > td {\n  background-color: #d9edf7; }\n\n.table-hover .table-info:hover {\n  background-color: #c4e3f3; }\n  .table-hover .table-info:hover > td,\n  .table-hover .table-info:hover > th {\n    background-color: #c4e3f3; }\n\n.table-warning,\n.table-warning > th,\n.table-warning > td {\n  background-color: #fcf8e3; }\n\n.table-hover .table-warning:hover {\n  background-color: #faf2cc; }\n  .table-hover .table-warning:hover > td,\n  .table-hover .table-warning:hover > th {\n    background-color: #faf2cc; }\n\n.table-danger,\n.table-danger > th,\n.table-danger > td {\n  background-color: #f2dede; }\n\n.table-hover .table-danger:hover {\n  background-color: #ebcccc; }\n  .table-hover .table-danger:hover > td,\n  .table-hover .table-danger:hover > th {\n    background-color: #ebcccc; }\n\n.thead-inverse th {\n  color: #f7f7f9;\n  background-color: #292b2c; }\n\n.thead-default th {\n  color: #fff;\n  background-color: #aaa; }\n\n.table-inverse {\n  color: #f7f7f9;\n  background-color: #292b2c; }\n  .table-inverse th,\n  .table-inverse td,\n  .table-inverse thead th {\n    border-color: #f7f7f9; }\n  .table-inverse.table-bordered {\n    border: 0; }\n\n.table-responsive {\n  display: block;\n  width: 100%;\n  overflow-x: auto;\n  -ms-overflow-style: -ms-autohiding-scrollbar; }\n  .table-responsive.table-bordered {\n    border: 0; }\n\n.form-control {\n  display: block;\n  width: 100%;\n  padding: 0.5rem 0.75rem;\n  font-size: 0.8rem;\n  line-height: 1.25;\n  color: #464a4c;\n  background-color: #fff;\n  background-image: none;\n  background-clip: padding-box;\n  border: 1px solid rgba(0, 0, 0, 0.15);\n  border-radius: 0.15rem;\n  transition: border-color ease-in-out 0.15s, box-shadow ease-in-out 0.15s; }\n  .form-control::-ms-expand {\n    background-color: transparent;\n    border: 0; }\n  .form-control:focus {\n    color: #464a4c;\n    background-color: #fff;\n    border-color: #0e8eff;\n    outline: none; }\n  .form-control::placeholder {\n    color: #aaa;\n    opacity: 1; }\n  .form-control:disabled, .form-control[readonly] {\n    background-color: #eceeef;\n    opacity: 1; }\n  .form-control:disabled {\n    cursor: not-allowed; }\n\nselect.form-control:not([size]):not([multiple]) {\n  height: calc(2rem + 2px); }\n\nselect.form-control:focus::-ms-value {\n  color: #464a4c;\n  background-color: #fff; }\n\n.form-control-file,\n.form-control-range {\n  display: block; }\n\n.col-form-label {\n  padding-top: calc(0.5rem - 1px * 2);\n  padding-bottom: calc(0.5rem - 1px * 2);\n  margin-bottom: 0; }\n\n.col-form-label-lg {\n  padding-top: calc(0.75rem - 1px * 2);\n  padding-bottom: calc(0.75rem - 1px * 2);\n  font-size: 1rem; }\n\n.col-form-label-sm {\n  padding-top: calc(0.25rem - 1px * 2);\n  padding-bottom: calc(0.25rem - 1px * 2);\n  font-size: 0.75rem; }\n\n.col-form-legend {\n  padding-top: 0.5rem;\n  padding-bottom: 0.5rem;\n  margin-bottom: 0;\n  font-size: 0.8rem; }\n\n.form-control-static {\n  padding-top: 0.5rem;\n  padding-bottom: 0.5rem;\n  margin-bottom: 0;\n  line-height: 1.25;\n  border: solid transparent;\n  border-width: 1px 0; }\n  .form-control-static.form-control-sm, .input-group-sm > .form-control-static.form-control,\n  .input-group-sm > .form-control-static.input-group-addon,\n  .input-group-sm > .input-group-btn > .form-control-static.btn, .form-control-static.form-control-lg, .input-group-lg > .form-control-static.form-control,\n  .input-group-lg > .form-control-static.input-group-addon,\n  .input-group-lg > .input-group-btn > .form-control-static.btn {\n    padding-right: 0;\n    padding-left: 0; }\n\n.form-control-sm, .input-group-sm > .form-control,\n.input-group-sm > .input-group-addon,\n.input-group-sm > .input-group-btn > .btn {\n  padding: 0.25rem 0.5rem;\n  font-size: 0.75rem;\n  border-radius: 0.1rem; }\n\nselect.form-control-sm:not([size]):not([multiple]), .input-group-sm > select.form-control:not([size]):not([multiple]),\n.input-group-sm > select.input-group-addon:not([size]):not([multiple]),\n.input-group-sm > .input-group-btn > select.btn:not([size]):not([multiple]) {\n  height: 1.625rem; }\n\n.form-control-lg, .input-group-lg > .form-control,\n.input-group-lg > .input-group-addon,\n.input-group-lg > .input-group-btn > .btn {\n  padding: 0.75rem 1.5rem;\n  font-size: 1rem;\n  border-radius: 0.2rem; }\n\nselect.form-control-lg:not([size]):not([multiple]), .input-group-lg > select.form-control:not([size]):not([multiple]),\n.input-group-lg > select.input-group-addon:not([size]):not([multiple]),\n.input-group-lg > .input-group-btn > select.btn:not([size]):not([multiple]) {\n  height: 2.83333rem; }\n\n.form-group {\n  margin-bottom: 1rem; }\n\n.form-text {\n  display: block;\n  margin-top: 0.25rem; }\n\n.form-check {\n  position: relative;\n  display: block;\n  margin-bottom: 0.5rem; }\n  .form-check.disabled .form-check-label {\n    color: #999;\n    cursor: not-allowed; }\n\n.form-check-label {\n  padding-left: 1.25rem;\n  margin-bottom: 0;\n  cursor: pointer; }\n\n.form-check-input {\n  position: absolute;\n  margin-top: 0.25rem;\n  margin-left: -1.25rem; }\n  .form-check-input:only-child {\n    position: static; }\n\n.form-check-inline {\n  display: inline-block; }\n  .form-check-inline .form-check-label {\n    vertical-align: middle; }\n  .form-check-inline + .form-check-inline {\n    margin-left: 0.75rem; }\n\n.form-control-feedback {\n  margin-top: 0.25rem; }\n\n.form-control-success,\n.form-control-warning,\n.form-control-danger {\n  padding-right: 2.25rem;\n  background-repeat: no-repeat;\n  background-position: center right 0.5rem;\n  background-size: 1rem 1rem; }\n\n.has-success .form-control-feedback,\n.has-success .form-control-label,\n.has-success .col-form-label,\n.has-success .form-check-label,\n.has-success .custom-control {\n  color: #5cb85c; }\n\n.has-success .form-control {\n  border-color: #5cb85c; }\n\n.has-success .input-group-addon {\n  color: #5cb85c;\n  border-color: #5cb85c;\n  background-color: #eaf6ea; }\n\n.has-success .form-control-success {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 8 8'%3E%3Cpath fill='%235cb85c' d='M2.3 6.73L.6 4.53c-.4-1.04.46-1.4 1.1-.8l1.1 1.4 3.4-3.8c.6-.63 1.6-.27 1.2.7l-4 4.6c-.43.5-.8.4-1.1.1z'/%3E%3C/svg%3E\"); }\n\n.has-warning .form-control-feedback,\n.has-warning .form-control-label,\n.has-warning .col-form-label,\n.has-warning .form-check-label,\n.has-warning .custom-control {\n  color: #f0ad4e; }\n\n.has-warning .form-control {\n  border-color: #f0ad4e; }\n\n.has-warning .input-group-addon {\n  color: #f0ad4e;\n  border-color: #f0ad4e;\n  background-color: white; }\n\n.has-warning .form-control-warning {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 8 8'%3E%3Cpath fill='%23f0ad4e' d='M4.4 5.324h-.8v-2.46h.8zm0 1.42h-.8V5.89h.8zM3.76.63L.04 7.075c-.115.2.016.425.26.426h7.397c.242 0 .372-.226.258-.426C6.726 4.924 5.47 2.79 4.253.63c-.113-.174-.39-.174-.494 0z'/%3E%3C/svg%3E\"); }\n\n.has-danger .form-control-feedback,\n.has-danger .form-control-label,\n.has-danger .col-form-label,\n.has-danger .form-check-label,\n.has-danger .custom-control {\n  color: #d50000; }\n\n.has-danger .form-control {\n  border-color: #d50000; }\n\n.has-danger .input-group-addon {\n  color: #d50000;\n  border-color: #d50000;\n  background-color: #ffa2a2; }\n\n.has-danger .form-control-danger {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='%23d50000' viewBox='-2 -2 7 7'%3E%3Cpath stroke='%23d9534f' d='M0 0l3 3m0-3L0 3'/%3E%3Ccircle r='.5'/%3E%3Ccircle cx='3' r='.5'/%3E%3Ccircle cy='3' r='.5'/%3E%3Ccircle cx='3' cy='3' r='.5'/%3E%3C/svg%3E\"); }\n\n.form-inline {\n  display: flex;\n  flex-flow: row wrap;\n  align-items: center; }\n  .form-inline .form-check {\n    width: 100%; }\n  @media (min-width: 576px) {\n    .form-inline label {\n      display: flex;\n      align-items: center;\n      justify-content: center;\n      margin-bottom: 0; }\n    .form-inline .form-group {\n      display: flex;\n      flex: 0 0 auto;\n      flex-flow: row wrap;\n      align-items: center;\n      margin-bottom: 0; }\n    .form-inline .form-control {\n      display: inline-block;\n      width: auto;\n      vertical-align: middle; }\n    .form-inline .form-control-static {\n      display: inline-block; }\n    .form-inline .input-group {\n      width: auto; }\n    .form-inline .form-control-label {\n      margin-bottom: 0;\n      vertical-align: middle; }\n    .form-inline .form-check {\n      display: flex;\n      align-items: center;\n      justify-content: center;\n      width: auto;\n      margin-top: 0;\n      margin-bottom: 0; }\n    .form-inline .form-check-label {\n      padding-left: 0; }\n    .form-inline .form-check-input {\n      position: relative;\n      margin-top: 0;\n      margin-right: 0.25rem;\n      margin-left: 0; }\n    .form-inline .custom-control {\n      display: flex;\n      align-items: center;\n      justify-content: center;\n      padding-left: 0; }\n    .form-inline .custom-control-indicator {\n      position: static;\n      display: inline-block;\n      margin-right: 0.25rem;\n      vertical-align: text-bottom; }\n    .form-inline .has-feedback .form-control-feedback {\n      top: 0; } }\n\n.btn {\n  display: inline-block;\n  font-weight: normal;\n  line-height: 1.25;\n  text-align: center;\n  white-space: nowrap;\n  vertical-align: middle;\n  user-select: none;\n  border: 1px solid transparent;\n  padding: 0.4rem 0.8rem;\n  font-size: 0.8rem;\n  border-radius: 0.15rem;\n  transition: all 0.2s ease-in-out; }\n  .btn:focus, .btn:hover {\n    text-decoration: none; }\n  .btn:focus, .btn.focus {\n    outline: 0;\n    box-shadow: 0 0 0 2px rgba(0, 75, 141, 0.25); }\n  .btn.disabled, .btn:disabled {\n    cursor: not-allowed;\n    opacity: .65; }\n  .btn:active, .btn.active {\n    background-image: none; }\n\na.btn.disabled,\nfieldset[disabled] a.btn {\n  pointer-events: none; }\n\n.btn-primary {\n  color: #fff;\n  background-color: #004B8D;\n  border-color: rgba(204, 204, 204, 0.75); }\n  .btn-primary:hover {\n    color: #fff;\n    background-color: #00305a;\n    border-color: rgba(173, 173, 173, 0.75); }\n  .btn-primary:focus, .btn-primary.focus {\n    box-shadow: 0 0 0 2px rgba(204, 204, 204, 0.5); }\n  .btn-primary.disabled, .btn-primary:disabled {\n    background-color: #004B8D;\n    border-color: rgba(204, 204, 204, 0.75); }\n  .btn-primary:active, .btn-primary.active,\n  .show > .btn-primary.dropdown-toggle {\n    color: #fff;\n    background-color: #00305a;\n    background-image: none;\n    border-color: rgba(173, 173, 173, 0.75); }\n\n.btn-secondary {\n  color: #292b2c;\n  background-color: #fff;\n  border-color: #ccc; }\n  .btn-secondary:hover {\n    color: #292b2c;\n    background-color: #e6e6e6;\n    border-color: #adadad; }\n  .btn-secondary:focus, .btn-secondary.focus {\n    box-shadow: 0 0 0 2px rgba(204, 204, 204, 0.5); }\n  .btn-secondary.disabled, .btn-secondary:disabled {\n    background-color: #fff;\n    border-color: #ccc; }\n  .btn-secondary:active, .btn-secondary.active,\n  .show > .btn-secondary.dropdown-toggle {\n    color: #292b2c;\n    background-color: #e6e6e6;\n    background-image: none;\n    border-color: #adadad; }\n\n.btn-info {\n  color: #fff;\n  background-color: #2196F3;\n  border-color: #2196F3; }\n  .btn-info:hover {\n    color: #fff;\n    background-color: #0c7cd5;\n    border-color: #0b76cc; }\n  .btn-info:focus, .btn-info.focus {\n    box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.5); }\n  .btn-info.disabled, .btn-info:disabled {\n    background-color: #2196F3;\n    border-color: #2196F3; }\n  .btn-info:active, .btn-info.active,\n  .show > .btn-info.dropdown-toggle {\n    color: #fff;\n    background-color: #0c7cd5;\n    background-image: none;\n    border-color: #0b76cc; }\n\n.btn-success {\n  color: #fff;\n  background-color: #5cb85c;\n  border-color: #5cb85c; }\n  .btn-success:hover {\n    color: #fff;\n    background-color: #449d44;\n    border-color: #419641; }\n  .btn-success:focus, .btn-success.focus {\n    box-shadow: 0 0 0 2px rgba(92, 184, 92, 0.5); }\n  .btn-success.disabled, .btn-success:disabled {\n    background-color: #5cb85c;\n    border-color: #5cb85c; }\n  .btn-success:active, .btn-success.active,\n  .show > .btn-success.dropdown-toggle {\n    color: #fff;\n    background-color: #449d44;\n    background-image: none;\n    border-color: #419641; }\n\n.btn-warning {\n  color: #fff;\n  background-color: #f0ad4e;\n  border-color: #f0ad4e; }\n  .btn-warning:hover {\n    color: #fff;\n    background-color: #ec971f;\n    border-color: #eb9316; }\n  .btn-warning:focus, .btn-warning.focus {\n    box-shadow: 0 0 0 2px rgba(240, 173, 78, 0.5); }\n  .btn-warning.disabled, .btn-warning:disabled {\n    background-color: #f0ad4e;\n    border-color: #f0ad4e; }\n  .btn-warning:active, .btn-warning.active,\n  .show > .btn-warning.dropdown-toggle {\n    color: #fff;\n    background-color: #ec971f;\n    background-image: none;\n    border-color: #eb9316; }\n\n.btn-danger {\n  color: #fff;\n  background-color: #d50000;\n  border-color: #d50000; }\n  .btn-danger:hover {\n    color: #fff;\n    background-color: #a20000;\n    border-color: #980000; }\n  .btn-danger:focus, .btn-danger.focus {\n    box-shadow: 0 0 0 2px rgba(213, 0, 0, 0.5); }\n  .btn-danger.disabled, .btn-danger:disabled {\n    background-color: #d50000;\n    border-color: #d50000; }\n  .btn-danger:active, .btn-danger.active,\n  .show > .btn-danger.dropdown-toggle {\n    color: #fff;\n    background-color: #a20000;\n    background-image: none;\n    border-color: #980000; }\n\n.btn-outline-primary {\n  color: #004B8D;\n  background-image: none;\n  background-color: transparent;\n  border-color: #004B8D; }\n  .btn-outline-primary:hover {\n    color: #fff;\n    background-color: #004B8D;\n    border-color: #004B8D; }\n  .btn-outline-primary:focus, .btn-outline-primary.focus {\n    box-shadow: 0 0 0 2px rgba(0, 75, 141, 0.5); }\n  .btn-outline-primary.disabled, .btn-outline-primary:disabled {\n    color: #004B8D;\n    background-color: transparent; }\n  .btn-outline-primary:active, .btn-outline-primary.active,\n  .show > .btn-outline-primary.dropdown-toggle {\n    color: #fff;\n    background-color: #004B8D;\n    border-color: #004B8D; }\n\n.btn-outline-secondary {\n  color: #ccc;\n  background-image: none;\n  background-color: transparent;\n  border-color: #ccc; }\n  .btn-outline-secondary:hover {\n    color: #fff;\n    background-color: #ccc;\n    border-color: #ccc; }\n  .btn-outline-secondary:focus, .btn-outline-secondary.focus {\n    box-shadow: 0 0 0 2px rgba(204, 204, 204, 0.5); }\n  .btn-outline-secondary.disabled, .btn-outline-secondary:disabled {\n    color: #ccc;\n    background-color: transparent; }\n  .btn-outline-secondary:active, .btn-outline-secondary.active,\n  .show > .btn-outline-secondary.dropdown-toggle {\n    color: #fff;\n    background-color: #ccc;\n    border-color: #ccc; }\n\n.btn-outline-info {\n  color: #2196F3;\n  background-image: none;\n  background-color: transparent;\n  border-color: #2196F3; }\n  .btn-outline-info:hover {\n    color: #fff;\n    background-color: #2196F3;\n    border-color: #2196F3; }\n  .btn-outline-info:focus, .btn-outline-info.focus {\n    box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.5); }\n  .btn-outline-info.disabled, .btn-outline-info:disabled {\n    color: #2196F3;\n    background-color: transparent; }\n  .btn-outline-info:active, .btn-outline-info.active,\n  .show > .btn-outline-info.dropdown-toggle {\n    color: #fff;\n    background-color: #2196F3;\n    border-color: #2196F3; }\n\n.btn-outline-success {\n  color: #5cb85c;\n  background-image: none;\n  background-color: transparent;\n  border-color: #5cb85c; }\n  .btn-outline-success:hover {\n    color: #fff;\n    background-color: #5cb85c;\n    border-color: #5cb85c; }\n  .btn-outline-success:focus, .btn-outline-success.focus {\n    box-shadow: 0 0 0 2px rgba(92, 184, 92, 0.5); }\n  .btn-outline-success.disabled, .btn-outline-success:disabled {\n    color: #5cb85c;\n    background-color: transparent; }\n  .btn-outline-success:active, .btn-outline-success.active,\n  .show > .btn-outline-success.dropdown-toggle {\n    color: #fff;\n    background-color: #5cb85c;\n    border-color: #5cb85c; }\n\n.btn-outline-warning {\n  color: #f0ad4e;\n  background-image: none;\n  background-color: transparent;\n  border-color: #f0ad4e; }\n  .btn-outline-warning:hover {\n    color: #fff;\n    background-color: #f0ad4e;\n    border-color: #f0ad4e; }\n  .btn-outline-warning:focus, .btn-outline-warning.focus {\n    box-shadow: 0 0 0 2px rgba(240, 173, 78, 0.5); }\n  .btn-outline-warning.disabled, .btn-outline-warning:disabled {\n    color: #f0ad4e;\n    background-color: transparent; }\n  .btn-outline-warning:active, .btn-outline-warning.active,\n  .show > .btn-outline-warning.dropdown-toggle {\n    color: #fff;\n    background-color: #f0ad4e;\n    border-color: #f0ad4e; }\n\n.btn-outline-danger {\n  color: #d50000;\n  background-image: none;\n  background-color: transparent;\n  border-color: #d50000; }\n  .btn-outline-danger:hover {\n    color: #fff;\n    background-color: #d50000;\n    border-color: #d50000; }\n  .btn-outline-danger:focus, .btn-outline-danger.focus {\n    box-shadow: 0 0 0 2px rgba(213, 0, 0, 0.5); }\n  .btn-outline-danger.disabled, .btn-outline-danger:disabled {\n    color: #d50000;\n    background-color: transparent; }\n  .btn-outline-danger:active, .btn-outline-danger.active,\n  .show > .btn-outline-danger.dropdown-toggle {\n    color: #fff;\n    background-color: #d50000;\n    border-color: #d50000; }\n\n.btn-link {\n  font-weight: normal;\n  color: #004B8D;\n  border-radius: 0; }\n  .btn-link, .btn-link:active, .btn-link.active, .btn-link:disabled {\n    background-color: transparent; }\n  .btn-link, .btn-link:focus, .btn-link:active {\n    border-color: transparent; }\n  .btn-link:hover {\n    border-color: transparent; }\n  .btn-link:focus, .btn-link:hover {\n    color: #002241;\n    text-decoration: underline;\n    background-color: transparent; }\n  .btn-link:disabled {\n    color: #aaa; }\n    .btn-link:disabled:focus, .btn-link:disabled:hover {\n      text-decoration: none; }\n\n.btn-lg, .btn-group-lg > .btn {\n  padding: 0.75rem 1.5rem;\n  font-size: 1rem;\n  border-radius: 0.2rem; }\n\n.btn-sm, .btn-group-sm > .btn {\n  padding: 0.25rem 0.5rem;\n  font-size: 0.75rem;\n  border-radius: 0.1rem; }\n\n.btn-block {\n  display: block;\n  width: 100%; }\n\n.btn-block + .btn-block {\n  margin-top: 0.5rem; }\n\ninput[type=\"submit\"].btn-block,\ninput[type=\"reset\"].btn-block,\ninput[type=\"button\"].btn-block {\n  width: 100%; }\n\n.fade {\n  opacity: 0;\n  transition: opacity 0.15s linear; }\n  .fade.show {\n    opacity: 1; }\n\n.collapse {\n  display: none; }\n  .collapse.show {\n    display: block; }\n\ntr.collapse.show {\n  display: table-row; }\n\ntbody.collapse.show {\n  display: table-row-group; }\n\n.collapsing {\n  position: relative;\n  height: 0;\n  overflow: hidden;\n  transition: height 0.35s ease; }\n\n.dropup,\n.dropdown {\n  position: relative; }\n\n.dropdown-toggle::after {\n  display: inline-block;\n  width: 0;\n  height: 0;\n  margin-left: 0.3em;\n  vertical-align: middle;\n  content: \"\";\n  border-top: 0.3em solid;\n  border-right: 0.3em solid transparent;\n  border-left: 0.3em solid transparent; }\n\n.dropdown-toggle:focus {\n  outline: 0; }\n\n.dropup .dropdown-toggle::after {\n  border-top: 0;\n  border-bottom: 0.3em solid; }\n\n.dropdown-menu {\n  position: absolute;\n  top: 100%;\n  left: 0;\n  z-index: 1000;\n  display: none;\n  float: left;\n  min-width: 6rem;\n  padding: 0 0;\n  margin: 0.125rem 0 0;\n  font-size: 0.8rem;\n  color: #292b2c;\n  text-align: left;\n  list-style: none;\n  background-color: #fff;\n  background-clip: padding-box;\n  border: 1px solid rgba(0, 0, 0, 0.15);\n  border-radius: 0.15rem; }\n\n.dropdown-divider {\n  height: 1px;\n  margin: 0.5rem 0;\n  overflow: hidden;\n  background-color: #eceeef; }\n\n.dropdown-item {\n  display: block;\n  width: 100%;\n  padding: 3px 1rem;\n  clear: both;\n  font-weight: normal;\n  color: #292b2c;\n  text-align: inherit;\n  white-space: nowrap;\n  background: none;\n  border: 0; }\n  .dropdown-item:focus, .dropdown-item:hover {\n    color: #1d1e1f;\n    text-decoration: none;\n    background-color: #f7f7f9; }\n  .dropdown-item.active, .dropdown-item:active {\n    color: #000;\n    text-decoration: none;\n    background-color: rgba(14, 142, 255, 0.2); }\n  .dropdown-item.disabled, .dropdown-item:disabled {\n    color: #aaa;\n    cursor: not-allowed;\n    background-color: transparent; }\n\n.show > .dropdown-menu {\n  display: block; }\n\n.show > a {\n  outline: 0; }\n\n.dropdown-menu-right {\n  right: 0;\n  left: auto; }\n\n.dropdown-menu-left {\n  right: auto;\n  left: 0; }\n\n.dropdown-header {\n  display: block;\n  padding: 0 1rem;\n  margin-bottom: 0;\n  font-size: 0.75rem;\n  color: #aaa;\n  white-space: nowrap; }\n\n.dropdown-backdrop {\n  position: fixed;\n  top: 0;\n  right: 0;\n  bottom: 0;\n  left: 0;\n  z-index: 990; }\n\n.dropup .dropdown-menu {\n  top: auto;\n  bottom: 100%;\n  margin-bottom: 0.125rem; }\n\n.btn-group,\n.btn-group-vertical {\n  position: relative;\n  display: inline-flex;\n  vertical-align: middle; }\n  .btn-group > .btn,\n  .btn-group-vertical > .btn {\n    position: relative;\n    flex: 0 1 auto; }\n    .btn-group > .btn:hover,\n    .btn-group-vertical > .btn:hover {\n      z-index: 2; }\n    .btn-group > .btn:focus, .btn-group > .btn:active, .btn-group > .btn.active,\n    .btn-group-vertical > .btn:focus,\n    .btn-group-vertical > .btn:active,\n    .btn-group-vertical > .btn.active {\n      z-index: 2; }\n  .btn-group .btn + .btn,\n  .btn-group .btn + .btn-group,\n  .btn-group .btn-group + .btn,\n  .btn-group .btn-group + .btn-group,\n  .btn-group-vertical .btn + .btn,\n  .btn-group-vertical .btn + .btn-group,\n  .btn-group-vertical .btn-group + .btn,\n  .btn-group-vertical .btn-group + .btn-group {\n    margin-left: -1px; }\n\n.btn-toolbar {\n  display: flex;\n  justify-content: flex-start; }\n  .btn-toolbar .input-group {\n    width: auto; }\n\n.btn-group > .btn:not(:first-child):not(:last-child):not(.dropdown-toggle) {\n  border-radius: 0; }\n\n.btn-group > .btn:first-child {\n  margin-left: 0; }\n  .btn-group > .btn:first-child:not(:last-child):not(.dropdown-toggle) {\n    border-bottom-right-radius: 0;\n    border-top-right-radius: 0; }\n\n.btn-group > .btn:last-child:not(:first-child),\n.btn-group > .dropdown-toggle:not(:first-child) {\n  border-bottom-left-radius: 0;\n  border-top-left-radius: 0; }\n\n.btn-group > .btn-group {\n  float: left; }\n\n.btn-group > .btn-group:not(:first-child):not(:last-child) > .btn {\n  border-radius: 0; }\n\n.btn-group > .btn-group:first-child:not(:last-child) > .btn:last-child,\n.btn-group > .btn-group:first-child:not(:last-child) > .dropdown-toggle {\n  border-bottom-right-radius: 0;\n  border-top-right-radius: 0; }\n\n.btn-group > .btn-group:last-child:not(:first-child) > .btn:first-child {\n  border-bottom-left-radius: 0;\n  border-top-left-radius: 0; }\n\n.btn-group .dropdown-toggle:active,\n.btn-group.open .dropdown-toggle {\n  outline: 0; }\n\n.btn + .dropdown-toggle-split {\n  padding-right: 0.6rem;\n  padding-left: 0.6rem; }\n  .btn + .dropdown-toggle-split::after {\n    margin-left: 0; }\n\n.btn-sm + .dropdown-toggle-split, .btn-group-sm > .btn + .dropdown-toggle-split {\n  padding-right: 0.375rem;\n  padding-left: 0.375rem; }\n\n.btn-lg + .dropdown-toggle-split, .btn-group-lg > .btn + .dropdown-toggle-split {\n  padding-right: 1.125rem;\n  padding-left: 1.125rem; }\n\n.btn-group-vertical {\n  display: inline-flex;\n  flex-direction: column;\n  align-items: flex-start;\n  justify-content: center; }\n  .btn-group-vertical .btn,\n  .btn-group-vertical .btn-group {\n    width: 100%; }\n  .btn-group-vertical > .btn + .btn,\n  .btn-group-vertical > .btn + .btn-group,\n  .btn-group-vertical > .btn-group + .btn,\n  .btn-group-vertical > .btn-group + .btn-group {\n    margin-top: -1px;\n    margin-left: 0; }\n\n.btn-group-vertical > .btn:not(:first-child):not(:last-child) {\n  border-radius: 0; }\n\n.btn-group-vertical > .btn:first-child:not(:last-child) {\n  border-bottom-right-radius: 0;\n  border-bottom-left-radius: 0; }\n\n.btn-group-vertical > .btn:last-child:not(:first-child) {\n  border-top-right-radius: 0;\n  border-top-left-radius: 0; }\n\n.btn-group-vertical > .btn-group:not(:first-child):not(:last-child) > .btn {\n  border-radius: 0; }\n\n.btn-group-vertical > .btn-group:first-child:not(:last-child) > .btn:last-child,\n.btn-group-vertical > .btn-group:first-child:not(:last-child) > .dropdown-toggle {\n  border-bottom-right-radius: 0;\n  border-bottom-left-radius: 0; }\n\n.btn-group-vertical > .btn-group:last-child:not(:first-child) > .btn:first-child {\n  border-top-right-radius: 0;\n  border-top-left-radius: 0; }\n\n[data-toggle=\"buttons\"] > .btn input[type=\"radio\"],\n[data-toggle=\"buttons\"] > .btn input[type=\"checkbox\"],\n[data-toggle=\"buttons\"] > .btn-group > .btn input[type=\"radio\"],\n[data-toggle=\"buttons\"] > .btn-group > .btn input[type=\"checkbox\"] {\n  position: absolute;\n  clip: rect(0, 0, 0, 0);\n  pointer-events: none; }\n\n.input-group {\n  position: relative;\n  display: flex;\n  width: 100%; }\n  .input-group .form-control {\n    position: relative;\n    z-index: 2;\n    flex: 1 1 auto;\n    width: 1%;\n    margin-bottom: 0; }\n    .input-group .form-control:focus, .input-group .form-control:active, .input-group .form-control:hover {\n      z-index: 3; }\n\n.input-group-addon,\n.input-group-btn,\n.input-group .form-control {\n  display: flex;\n  flex-direction: column;\n  justify-content: center; }\n  .input-group-addon:not(:first-child):not(:last-child),\n  .input-group-btn:not(:first-child):not(:last-child),\n  .input-group .form-control:not(:first-child):not(:last-child) {\n    border-radius: 0; }\n\n.input-group-addon,\n.input-group-btn {\n  white-space: nowrap;\n  vertical-align: middle; }\n\n.input-group-addon {\n  padding: 0.5rem 0.75rem;\n  margin-bottom: 0;\n  font-size: 0.8rem;\n  font-weight: normal;\n  line-height: 1.25;\n  color: #464a4c;\n  text-align: center;\n  background-color: #eceeef;\n  border: 1px solid rgba(0, 0, 0, 0.15);\n  border-radius: 0.15rem; }\n  .input-group-addon.form-control-sm,\n  .input-group-sm > .input-group-addon,\n  .input-group-sm > .input-group-btn > .input-group-addon.btn {\n    padding: 0.25rem 0.5rem;\n    font-size: 0.75rem;\n    border-radius: 0.1rem; }\n  .input-group-addon.form-control-lg,\n  .input-group-lg > .input-group-addon,\n  .input-group-lg > .input-group-btn > .input-group-addon.btn {\n    padding: 0.75rem 1.5rem;\n    font-size: 1rem;\n    border-radius: 0.2rem; }\n  .input-group-addon input[type=\"radio\"],\n  .input-group-addon input[type=\"checkbox\"] {\n    margin-top: 0; }\n\n.input-group .form-control:not(:last-child),\n.input-group-addon:not(:last-child),\n.input-group-btn:not(:last-child) > .btn,\n.input-group-btn:not(:last-child) > .btn-group > .btn,\n.input-group-btn:not(:last-child) > .dropdown-toggle,\n.input-group-btn:not(:first-child) > .btn:not(:last-child):not(.dropdown-toggle),\n.input-group-btn:not(:first-child) > .btn-group:not(:last-child) > .btn {\n  border-bottom-right-radius: 0;\n  border-top-right-radius: 0; }\n\n.input-group-addon:not(:last-child) {\n  border-right: 0; }\n\n.input-group .form-control:not(:first-child),\n.input-group-addon:not(:first-child),\n.input-group-btn:not(:first-child) > .btn,\n.input-group-btn:not(:first-child) > .btn-group > .btn,\n.input-group-btn:not(:first-child) > .dropdown-toggle,\n.input-group-btn:not(:last-child) > .btn:not(:first-child),\n.input-group-btn:not(:last-child) > .btn-group:not(:first-child) > .btn {\n  border-bottom-left-radius: 0;\n  border-top-left-radius: 0; }\n\n.form-control + .input-group-addon:not(:first-child) {\n  border-left: 0; }\n\n.input-group-btn {\n  position: relative;\n  font-size: 0;\n  white-space: nowrap; }\n  .input-group-btn > .btn {\n    position: relative;\n    flex: 1; }\n    .input-group-btn > .btn + .btn {\n      margin-left: -1px; }\n    .input-group-btn > .btn:focus, .input-group-btn > .btn:active, .input-group-btn > .btn:hover {\n      z-index: 3; }\n  .input-group-btn:not(:last-child) > .btn,\n  .input-group-btn:not(:last-child) > .btn-group {\n    margin-right: -1px; }\n  .input-group-btn:not(:first-child) > .btn,\n  .input-group-btn:not(:first-child) > .btn-group {\n    z-index: 2;\n    margin-left: -1px; }\n    .input-group-btn:not(:first-child) > .btn:focus, .input-group-btn:not(:first-child) > .btn:active, .input-group-btn:not(:first-child) > .btn:hover,\n    .input-group-btn:not(:first-child) > .btn-group:focus,\n    .input-group-btn:not(:first-child) > .btn-group:active,\n    .input-group-btn:not(:first-child) > .btn-group:hover {\n      z-index: 3; }\n\n.custom-control {\n  position: relative;\n  display: inline-flex;\n  min-height: 1.5rem;\n  padding-left: 1.5rem;\n  margin-right: 1rem;\n  cursor: pointer; }\n\n.custom-control-input {\n  position: absolute;\n  z-index: -1;\n  opacity: 0; }\n  .custom-control-input:checked ~ .custom-control-indicator {\n    color: #fff;\n    background-color: #004B8D; }\n  .custom-control-input:focus ~ .custom-control-indicator {\n    box-shadow: 0 0 0 1px #f7f7f9, 0 0 0 3px #004B8D; }\n  .custom-control-input:active ~ .custom-control-indicator {\n    color: #fff;\n    background-color: #41a6ff; }\n  .custom-control-input:disabled ~ .custom-control-indicator {\n    cursor: not-allowed;\n    background-color: #eceeef; }\n  .custom-control-input:disabled ~ .custom-control-description {\n    color: #aaa;\n    cursor: not-allowed; }\n\n.custom-control-indicator {\n  position: absolute;\n  top: 0.25rem;\n  left: 0;\n  display: block;\n  width: 1rem;\n  height: 1rem;\n  pointer-events: none;\n  user-select: none;\n  background-color: #ddd;\n  background-repeat: no-repeat;\n  background-position: center center;\n  background-size: 50% 50%; }\n\n.custom-checkbox .custom-control-indicator {\n  border-radius: 0.15rem; }\n\n.custom-checkbox .custom-control-input:checked ~ .custom-control-indicator {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 8 8'%3E%3Cpath fill='%23fff' d='M6.564.75l-3.59 3.612-1.538-1.55L0 4.26 2.974 7.25 8 2.193z'/%3E%3C/svg%3E\"); }\n\n.custom-checkbox .custom-control-input:indeterminate ~ .custom-control-indicator {\n  background-color: #004B8D;\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 4 4'%3E%3Cpath stroke='%23fff' d='M0 2h4'/%3E%3C/svg%3E\"); }\n\n.custom-radio .custom-control-indicator {\n  border-radius: 50%; }\n\n.custom-radio .custom-control-input:checked ~ .custom-control-indicator {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='-4 -4 8 8'%3E%3Ccircle r='3' fill='%23fff'/%3E%3C/svg%3E\"); }\n\n.custom-controls-stacked {\n  display: flex;\n  flex-direction: column; }\n  .custom-controls-stacked .custom-control {\n    margin-bottom: 0.25rem; }\n    .custom-controls-stacked .custom-control + .custom-control {\n      margin-left: 0; }\n\n.custom-select {\n  display: inline-block;\n  max-width: 100%;\n  height: calc(2rem + 2px);\n  padding: 0.375rem 1.75rem 0.375rem 0.75rem;\n  line-height: 1.25;\n  color: #464a4c;\n  vertical-align: middle;\n  background: #fff url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 4 5'%3E%3Cpath fill='%23333' d='M2 0L0 2h4zm0 5L0 3h4z'/%3E%3C/svg%3E\") no-repeat right 0.75rem center;\n  background-size: 8px 10px;\n  border: 1px solid rgba(0, 0, 0, 0.15);\n  border-radius: 0.15rem;\n  -moz-appearance: none;\n  -webkit-appearance: none; }\n  .custom-select:focus {\n    border-color: #0e8eff;\n    outline: none; }\n    .custom-select:focus::-ms-value {\n      color: #464a4c;\n      background-color: #fff; }\n  .custom-select:disabled {\n    color: #aaa;\n    cursor: not-allowed;\n    background-color: #eceeef; }\n  .custom-select::-ms-expand {\n    opacity: 0; }\n\n.custom-select-sm {\n  padding-top: 0.375rem;\n  padding-bottom: 0.375rem;\n  font-size: 75%; }\n\n.custom-file {\n  position: relative;\n  display: inline-block;\n  max-width: 100%;\n  height: 2.5rem;\n  margin-bottom: 0;\n  cursor: pointer; }\n\n.custom-file-input {\n  min-width: 14rem;\n  max-width: 100%;\n  height: 2.5rem;\n  margin: 0;\n  filter: alpha(opacity=0);\n  opacity: 0; }\n\n.custom-file-control {\n  position: absolute;\n  top: 0;\n  right: 0;\n  left: 0;\n  z-index: 5;\n  height: 2.5rem;\n  padding: 0.5rem 1rem;\n  line-height: 1.5;\n  color: #464a4c;\n  pointer-events: none;\n  user-select: none;\n  background-color: #fff;\n  border: 1px solid rgba(0, 0, 0, 0.15);\n  border-radius: 0.15rem; }\n  .custom-file-control:lang(en)::after {\n    content: \"Choose file...\"; }\n  .custom-file-control::before {\n    position: absolute;\n    top: -1px;\n    right: -1px;\n    bottom: -1px;\n    z-index: 6;\n    display: block;\n    height: 2.5rem;\n    padding: 0.5rem 1rem;\n    line-height: 1.5;\n    color: #464a4c;\n    background-color: #eceeef;\n    border: 1px solid rgba(0, 0, 0, 0.15);\n    border-radius: 0 0.15rem 0.15rem 0; }\n  .custom-file-control:lang(en)::before {\n    content: \"Browse\"; }\n\n.nav {\n  display: flex;\n  padding-left: 0;\n  margin-bottom: 0;\n  list-style: none; }\n\n.nav-link {\n  display: block;\n  padding: 0.6rem 1rem; }\n  .nav-link:focus, .nav-link:hover {\n    text-decoration: none; }\n  .nav-link.disabled {\n    color: #aaa;\n    cursor: not-allowed; }\n\n.nav-tabs {\n  border-bottom: 1px solid #ddd; }\n  .nav-tabs .nav-item {\n    margin-bottom: -1px; }\n  .nav-tabs .nav-link {\n    border: 1px solid transparent;\n    border-top-right-radius: 0.15rem;\n    border-top-left-radius: 0.15rem; }\n    .nav-tabs .nav-link:focus, .nav-tabs .nav-link:hover {\n      border-color: transparent transparent #ddd; }\n    .nav-tabs .nav-link.disabled {\n      color: #aaa;\n      background-color: transparent;\n      border-color: transparent; }\n  .nav-tabs .nav-link.active,\n  .nav-tabs .nav-item.show .nav-link {\n    color: #464a4c;\n    background-color: #f7f7f9;\n    border-color: #ddd #ddd #f7f7f9; }\n  .nav-tabs .dropdown-menu {\n    margin-top: -1px;\n    border-top-right-radius: 0;\n    border-top-left-radius: 0; }\n\n.nav-pills .nav-link {\n  border-radius: 0.15rem; }\n\n.nav-pills .nav-link.active,\n.nav-pills .nav-item.show .nav-link {\n  color: #000;\n  cursor: default;\n  background-color: rgba(14, 142, 255, 0.2); }\n\n.nav-fill .nav-item {\n  flex: 1 1 auto;\n  text-align: center; }\n\n.nav-justified .nav-item {\n  flex: 1 1 100%;\n  text-align: center; }\n\n.tab-content > .tab-pane {\n  display: none; }\n\n.tab-content > .active {\n  display: block; }\n\n.navbar {\n  position: relative;\n  display: flex;\n  flex-direction: column;\n  padding: 0.5rem 1rem; }\n\n.navbar-brand {\n  display: inline-block;\n  padding-top: .25rem;\n  padding-bottom: .25rem;\n  margin-right: 1rem;\n  font-size: 1rem;\n  line-height: inherit;\n  white-space: nowrap; }\n  .navbar-brand:focus, .navbar-brand:hover {\n    text-decoration: none; }\n\n.navbar-nav {\n  display: flex;\n  flex-direction: column;\n  padding-left: 0;\n  margin-bottom: 0;\n  list-style: none; }\n  .navbar-nav .nav-link {\n    padding-right: 0;\n    padding-left: 0; }\n\n.navbar-text {\n  display: inline-block;\n  padding-top: .425rem;\n  padding-bottom: .425rem; }\n\n.navbar-toggler {\n  align-self: flex-start;\n  padding: 0.25rem 0.75rem;\n  font-size: 1rem;\n  line-height: 1;\n  background: transparent;\n  border: 1px solid transparent;\n  border-radius: 0.15rem; }\n  .navbar-toggler:focus, .navbar-toggler:hover {\n    text-decoration: none; }\n\n.navbar-toggler-icon {\n  display: inline-block;\n  width: 1.5em;\n  height: 1.5em;\n  vertical-align: middle;\n  content: \"\";\n  background: no-repeat center center;\n  background-size: 100% 100%; }\n\n.navbar-toggler-left {\n  position: absolute;\n  left: 1rem; }\n\n.navbar-toggler-right {\n  position: absolute;\n  right: 1rem; }\n\n@media (max-width: 575px) {\n  .navbar-toggleable .navbar-nav .dropdown-menu {\n    position: static;\n    float: none; }\n  .navbar-toggleable > .container {\n    padding-right: 0;\n    padding-left: 0; } }\n\n@media (min-width: 576px) {\n  .navbar-toggleable {\n    flex-direction: row;\n    flex-wrap: nowrap;\n    align-items: center; }\n    .navbar-toggleable .navbar-nav {\n      flex-direction: row; }\n      .navbar-toggleable .navbar-nav .nav-link {\n        padding-right: .5rem;\n        padding-left: .5rem; }\n    .navbar-toggleable > .container {\n      display: flex;\n      flex-wrap: nowrap;\n      align-items: center; }\n    .navbar-toggleable .navbar-collapse {\n      display: flex !important;\n      width: 100%; }\n    .navbar-toggleable .navbar-toggler {\n      display: none; } }\n\n@media (max-width: 767px) {\n  .navbar-toggleable-sm .navbar-nav .dropdown-menu {\n    position: static;\n    float: none; }\n  .navbar-toggleable-sm > .container {\n    padding-right: 0;\n    padding-left: 0; } }\n\n@media (min-width: 768px) {\n  .navbar-toggleable-sm {\n    flex-direction: row;\n    flex-wrap: nowrap;\n    align-items: center; }\n    .navbar-toggleable-sm .navbar-nav {\n      flex-direction: row; }\n      .navbar-toggleable-sm .navbar-nav .nav-link {\n        padding-right: .5rem;\n        padding-left: .5rem; }\n    .navbar-toggleable-sm > .container {\n      display: flex;\n      flex-wrap: nowrap;\n      align-items: center; }\n    .navbar-toggleable-sm .navbar-collapse {\n      display: flex !important;\n      width: 100%; }\n    .navbar-toggleable-sm .navbar-toggler {\n      display: none; } }\n\n@media (max-width: 991px) {\n  .navbar-toggleable-md .navbar-nav .dropdown-menu {\n    position: static;\n    float: none; }\n  .navbar-toggleable-md > .container {\n    padding-right: 0;\n    padding-left: 0; } }\n\n@media (min-width: 992px) {\n  .navbar-toggleable-md {\n    flex-direction: row;\n    flex-wrap: nowrap;\n    align-items: center; }\n    .navbar-toggleable-md .navbar-nav {\n      flex-direction: row; }\n      .navbar-toggleable-md .navbar-nav .nav-link {\n        padding-right: .5rem;\n        padding-left: .5rem; }\n    .navbar-toggleable-md > .container {\n      display: flex;\n      flex-wrap: nowrap;\n      align-items: center; }\n    .navbar-toggleable-md .navbar-collapse {\n      display: flex !important;\n      width: 100%; }\n    .navbar-toggleable-md .navbar-toggler {\n      display: none; } }\n\n@media (max-width: 1339px) {\n  .navbar-toggleable-lg .navbar-nav .dropdown-menu {\n    position: static;\n    float: none; }\n  .navbar-toggleable-lg > .container {\n    padding-right: 0;\n    padding-left: 0; } }\n\n@media (min-width: 1340px) {\n  .navbar-toggleable-lg {\n    flex-direction: row;\n    flex-wrap: nowrap;\n    align-items: center; }\n    .navbar-toggleable-lg .navbar-nav {\n      flex-direction: row; }\n      .navbar-toggleable-lg .navbar-nav .nav-link {\n        padding-right: .5rem;\n        padding-left: .5rem; }\n    .navbar-toggleable-lg > .container {\n      display: flex;\n      flex-wrap: nowrap;\n      align-items: center; }\n    .navbar-toggleable-lg .navbar-collapse {\n      display: flex !important;\n      width: 100%; }\n    .navbar-toggleable-lg .navbar-toggler {\n      display: none; } }\n\n.navbar-toggleable-xl {\n  flex-direction: row;\n  flex-wrap: nowrap;\n  align-items: center; }\n  .navbar-toggleable-xl .navbar-nav .dropdown-menu {\n    position: static;\n    float: none; }\n  .navbar-toggleable-xl > .container {\n    padding-right: 0;\n    padding-left: 0; }\n  .navbar-toggleable-xl .navbar-nav {\n    flex-direction: row; }\n    .navbar-toggleable-xl .navbar-nav .nav-link {\n      padding-right: .5rem;\n      padding-left: .5rem; }\n  .navbar-toggleable-xl > .container {\n    display: flex;\n    flex-wrap: nowrap;\n    align-items: center; }\n  .navbar-toggleable-xl .navbar-collapse {\n    display: flex !important;\n    width: 100%; }\n  .navbar-toggleable-xl .navbar-toggler {\n    display: none; }\n\n.navbar-light .navbar-brand,\n.navbar-light .navbar-toggler {\n  color: rgba(0, 0, 0, 0.9); }\n  .navbar-light .navbar-brand:focus, .navbar-light .navbar-brand:hover,\n  .navbar-light .navbar-toggler:focus,\n  .navbar-light .navbar-toggler:hover {\n    color: rgba(0, 0, 0, 0.9); }\n\n.navbar-light .navbar-nav .nav-link {\n  color: rgba(0, 0, 0, 0.5); }\n  .navbar-light .navbar-nav .nav-link:focus, .navbar-light .navbar-nav .nav-link:hover {\n    color: rgba(0, 0, 0, 0.7); }\n  .navbar-light .navbar-nav .nav-link.disabled {\n    color: rgba(0, 0, 0, 0.3); }\n\n.navbar-light .navbar-nav .open > .nav-link,\n.navbar-light .navbar-nav .active > .nav-link,\n.navbar-light .navbar-nav .nav-link.open,\n.navbar-light .navbar-nav .nav-link.active {\n  color: rgba(0, 0, 0, 0.9); }\n\n.navbar-light .navbar-toggler {\n  border-color: rgba(0, 0, 0, 0.1); }\n\n.navbar-light .navbar-toggler-icon {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg viewBox='0 0 32 32' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath stroke='rgba(0, 0, 0, 0.5)' stroke-width='2' stroke-linecap='round' stroke-miterlimit='10' d='M4 8h24M4 16h24M4 24h24'/%3E%3C/svg%3E\"); }\n\n.navbar-light .navbar-text {\n  color: rgba(0, 0, 0, 0.5); }\n\n.navbar-inverse .navbar-brand,\n.navbar-inverse .navbar-toggler {\n  color: white; }\n  .navbar-inverse .navbar-brand:focus, .navbar-inverse .navbar-brand:hover,\n  .navbar-inverse .navbar-toggler:focus,\n  .navbar-inverse .navbar-toggler:hover {\n    color: white; }\n\n.navbar-inverse .navbar-nav .nav-link {\n  color: rgba(255, 255, 255, 0.5); }\n  .navbar-inverse .navbar-nav .nav-link:focus, .navbar-inverse .navbar-nav .nav-link:hover {\n    color: rgba(255, 255, 255, 0.75); }\n  .navbar-inverse .navbar-nav .nav-link.disabled {\n    color: rgba(255, 255, 255, 0.25); }\n\n.navbar-inverse .navbar-nav .open > .nav-link,\n.navbar-inverse .navbar-nav .active > .nav-link,\n.navbar-inverse .navbar-nav .nav-link.open,\n.navbar-inverse .navbar-nav .nav-link.active {\n  color: white; }\n\n.navbar-inverse .navbar-toggler {\n  border-color: rgba(255, 255, 255, 0.1); }\n\n.navbar-inverse .navbar-toggler-icon {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg viewBox='0 0 32 32' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath stroke='rgba(255, 255, 255, 0.5)' stroke-width='2' stroke-linecap='round' stroke-miterlimit='10' d='M4 8h24M4 16h24M4 24h24'/%3E%3C/svg%3E\"); }\n\n.navbar-inverse .navbar-text {\n  color: rgba(255, 255, 255, 0.5); }\n\n.card {\n  position: relative;\n  display: flex;\n  flex-direction: column;\n  background-color: #fff;\n  border: 1px solid rgba(0, 0, 0, 0.125);\n  border-radius: 0.15rem; }\n\n.card-block {\n  flex: 1 1 auto;\n  padding: 0.8rem; }\n\n.card-title {\n  margin-bottom: 0.5rem; }\n\n.card-subtitle {\n  margin-top: -0.25rem;\n  margin-bottom: 0; }\n\n.card-text:last-child {\n  margin-bottom: 0; }\n\n.card-link:hover {\n  text-decoration: none; }\n\n.card-link + .card-link {\n  margin-left: 0.8rem; }\n\n.card > .list-group:first-child .list-group-item:first-child {\n  border-top-right-radius: 0.15rem;\n  border-top-left-radius: 0.15rem; }\n\n.card > .list-group:last-child .list-group-item:last-child {\n  border-bottom-right-radius: 0.15rem;\n  border-bottom-left-radius: 0.15rem; }\n\n.card-header {\n  padding: 0.5rem 0.8rem;\n  margin-bottom: 0;\n  background-color: #eceeef;\n  border-bottom: 1px solid rgba(0, 0, 0, 0.125); }\n  .card-header:first-child {\n    border-radius: calc(0.15rem - 1px) calc(0.15rem - 1px) 0 0; }\n\n.card-footer {\n  padding: 0.5rem 0.8rem;\n  background-color: #eceeef;\n  border-top: 1px solid rgba(0, 0, 0, 0.125); }\n  .card-footer:last-child {\n    border-radius: 0 0 calc(0.15rem - 1px) calc(0.15rem - 1px); }\n\n.card-header-tabs {\n  margin-right: -0.4rem;\n  margin-bottom: -0.5rem;\n  margin-left: -0.4rem;\n  border-bottom: 0; }\n\n.card-header-pills {\n  margin-right: -0.4rem;\n  margin-left: -0.4rem; }\n\n.card-primary {\n  background-color: #004B8D;\n  border-color: #004B8D; }\n  .card-primary .card-header,\n  .card-primary .card-footer {\n    background-color: transparent; }\n\n.card-success {\n  background-color: #5cb85c;\n  border-color: #5cb85c; }\n  .card-success .card-header,\n  .card-success .card-footer {\n    background-color: transparent; }\n\n.card-info {\n  background-color: #2196F3;\n  border-color: #2196F3; }\n  .card-info .card-header,\n  .card-info .card-footer {\n    background-color: transparent; }\n\n.card-warning {\n  background-color: #f0ad4e;\n  border-color: #f0ad4e; }\n  .card-warning .card-header,\n  .card-warning .card-footer {\n    background-color: transparent; }\n\n.card-danger {\n  background-color: #d50000;\n  border-color: #d50000; }\n  .card-danger .card-header,\n  .card-danger .card-footer {\n    background-color: transparent; }\n\n.card-outline-primary {\n  background-color: transparent;\n  border-color: #004B8D; }\n\n.card-outline-secondary {\n  background-color: transparent;\n  border-color: #ccc; }\n\n.card-outline-info {\n  background-color: transparent;\n  border-color: #2196F3; }\n\n.card-outline-success {\n  background-color: transparent;\n  border-color: #5cb85c; }\n\n.card-outline-warning {\n  background-color: transparent;\n  border-color: #f0ad4e; }\n\n.card-outline-danger {\n  background-color: transparent;\n  border-color: #d50000; }\n\n.card-inverse {\n  color: rgba(255, 255, 255, 0.65); }\n  .card-inverse .card-header,\n  .card-inverse .card-footer {\n    background-color: transparent;\n    border-color: rgba(255, 255, 255, 0.2); }\n  .card-inverse .card-header,\n  .card-inverse .card-footer,\n  .card-inverse .card-title,\n  .card-inverse .card-blockquote {\n    color: #fff; }\n  .card-inverse .card-link,\n  .card-inverse .card-text,\n  .card-inverse .card-subtitle,\n  .card-inverse .card-blockquote .blockquote-footer {\n    color: rgba(255, 255, 255, 0.65); }\n  .card-inverse .card-link:focus, .card-inverse .card-link:hover {\n    color: #fff; }\n\n.card-blockquote {\n  padding: 0;\n  margin-bottom: 0;\n  border-left: 0; }\n\n.card-img {\n  border-radius: calc(0.15rem - 1px); }\n\n.card-img-overlay {\n  position: absolute;\n  top: 0;\n  right: 0;\n  bottom: 0;\n  left: 0;\n  padding: 1.25rem; }\n\n.card-img-top {\n  border-top-right-radius: calc(0.15rem - 1px);\n  border-top-left-radius: calc(0.15rem - 1px); }\n\n.card-img-bottom {\n  border-bottom-right-radius: calc(0.15rem - 1px);\n  border-bottom-left-radius: calc(0.15rem - 1px); }\n\n@media (min-width: 576px) {\n  .card-deck {\n    display: flex;\n    flex-flow: row wrap; }\n    .card-deck .card {\n      display: flex;\n      flex: 1 0 0;\n      flex-direction: column; }\n      .card-deck .card:not(:first-child) {\n        margin-left: 8px; }\n      .card-deck .card:not(:last-child) {\n        margin-right: 8px; } }\n\n@media (min-width: 576px) {\n  .card-group {\n    display: flex;\n    flex-flow: row wrap; }\n    .card-group .card {\n      flex: 1 0 0; }\n      .card-group .card + .card {\n        margin-left: 0;\n        border-left: 0; }\n      .card-group .card:first-child {\n        border-bottom-right-radius: 0;\n        border-top-right-radius: 0; }\n        .card-group .card:first-child .card-img-top {\n          border-top-right-radius: 0; }\n        .card-group .card:first-child .card-img-bottom {\n          border-bottom-right-radius: 0; }\n      .card-group .card:last-child {\n        border-bottom-left-radius: 0;\n        border-top-left-radius: 0; }\n        .card-group .card:last-child .card-img-top {\n          border-top-left-radius: 0; }\n        .card-group .card:last-child .card-img-bottom {\n          border-bottom-left-radius: 0; }\n      .card-group .card:not(:first-child):not(:last-child) {\n        border-radius: 0; }\n        .card-group .card:not(:first-child):not(:last-child) .card-img-top,\n        .card-group .card:not(:first-child):not(:last-child) .card-img-bottom {\n          border-radius: 0; } }\n\n@media (min-width: 576px) {\n  .card-columns {\n    column-count: 3;\n    column-gap: 1.25rem; }\n    .card-columns .card {\n      display: inline-block;\n      width: 100%;\n      margin-bottom: 0.5rem; } }\n\n.breadcrumb {\n  padding: 0.75rem 1rem;\n  margin-bottom: 1rem;\n  list-style: none;\n  background-color: #eceeef;\n  border-radius: 0.15rem; }\n  .breadcrumb::after {\n    display: block;\n    content: \"\";\n    clear: both; }\n\n.breadcrumb-item {\n  float: left; }\n  .breadcrumb-item + .breadcrumb-item::before {\n    display: inline-block;\n    padding-right: 0.5rem;\n    padding-left: 0.5rem;\n    color: #aaa;\n    content: \"/\"; }\n  .breadcrumb-item + .breadcrumb-item:hover::before {\n    text-decoration: underline; }\n  .breadcrumb-item + .breadcrumb-item:hover::before {\n    text-decoration: none; }\n  .breadcrumb-item.active {\n    color: #aaa; }\n\n.pagination {\n  display: flex;\n  padding-left: 0;\n  list-style: none;\n  border-radius: 0.15rem; }\n\n.page-item:first-child .page-link {\n  margin-left: 0;\n  border-bottom-left-radius: 0.15rem;\n  border-top-left-radius: 0.15rem; }\n\n.page-item:last-child .page-link {\n  border-bottom-right-radius: 0.15rem;\n  border-top-right-radius: 0.15rem; }\n\n.page-item.active .page-link {\n  z-index: 2;\n  color: #fff;\n  background-color: #004B8D;\n  border-color: #004B8D; }\n\n.page-item.disabled .page-link {\n  color: #aaa;\n  pointer-events: none;\n  cursor: not-allowed;\n  background-color: #fff;\n  border-color: #ddd; }\n\n.page-link {\n  position: relative;\n  display: block;\n  padding: 0.5rem 0.75rem;\n  margin-left: -1px;\n  line-height: 1.25;\n  color: #004B8D;\n  background-color: #fff;\n  border: 1px solid #ddd; }\n  .page-link:focus, .page-link:hover {\n    color: #002241;\n    text-decoration: none;\n    background-color: #eceeef;\n    border-color: #ddd; }\n\n.pagination-lg .page-link {\n  padding: 0.75rem 1.5rem;\n  font-size: 1rem; }\n\n.pagination-lg .page-item:first-child .page-link {\n  border-bottom-left-radius: 0.2rem;\n  border-top-left-radius: 0.2rem; }\n\n.pagination-lg .page-item:last-child .page-link {\n  border-bottom-right-radius: 0.2rem;\n  border-top-right-radius: 0.2rem; }\n\n.pagination-sm .page-link {\n  padding: 0.25rem 0.5rem;\n  font-size: 0.75rem; }\n\n.pagination-sm .page-item:first-child .page-link {\n  border-bottom-left-radius: 0.1rem;\n  border-top-left-radius: 0.1rem; }\n\n.pagination-sm .page-item:last-child .page-link {\n  border-bottom-right-radius: 0.1rem;\n  border-top-right-radius: 0.1rem; }\n\n.badge {\n  display: inline-block;\n  padding: 0.25em 0.4em;\n  font-size: 75%;\n  font-weight: bold;\n  line-height: 1;\n  color: #fff;\n  text-align: center;\n  white-space: nowrap;\n  vertical-align: baseline;\n  border-radius: 0.15rem; }\n  .badge:empty {\n    display: none; }\n\n.btn .badge {\n  position: relative;\n  top: -1px; }\n\na.badge:focus, a.badge:hover {\n  color: #fff;\n  text-decoration: none;\n  cursor: pointer; }\n\n.badge-pill {\n  padding-right: 0.6em;\n  padding-left: 0.6em;\n  border-radius: 10rem; }\n\n.badge-default {\n  background-color: #aaa; }\n  .badge-default[href]:focus, .badge-default[href]:hover {\n    background-color: #919191; }\n\n.badge-primary {\n  background-color: #004B8D; }\n  .badge-primary[href]:focus, .badge-primary[href]:hover {\n    background-color: #00305a; }\n\n.badge-success {\n  background-color: #5cb85c; }\n  .badge-success[href]:focus, .badge-success[href]:hover {\n    background-color: #449d44; }\n\n.badge-info {\n  background-color: #2196F3; }\n  .badge-info[href]:focus, .badge-info[href]:hover {\n    background-color: #0c7cd5; }\n\n.badge-warning {\n  background-color: #f0ad4e; }\n  .badge-warning[href]:focus, .badge-warning[href]:hover {\n    background-color: #ec971f; }\n\n.badge-danger {\n  background-color: #d50000; }\n  .badge-danger[href]:focus, .badge-danger[href]:hover {\n    background-color: #a20000; }\n\n.jumbotron {\n  padding: 2rem 1rem;\n  margin-bottom: 2rem;\n  background-color: #eceeef;\n  border-radius: 0.2rem; }\n  @media (min-width: 576px) {\n    .jumbotron {\n      padding: 4rem 2rem; } }\n\n.jumbotron-hr {\n  border-top-color: #d0d5d8; }\n\n.jumbotron-fluid {\n  padding-right: 0;\n  padding-left: 0;\n  border-radius: 0; }\n\n.alert {\n  padding: 0.6rem 1rem;\n  margin-bottom: 1rem;\n  border: 1px solid transparent;\n  border-radius: 0.15rem; }\n\n.alert-heading {\n  color: inherit; }\n\n.alert-link {\n  font-weight: bold; }\n\n.alert-dismissible .close {\n  position: relative;\n  top: -0.6rem;\n  right: -1rem;\n  padding: 0.6rem 1rem;\n  color: inherit; }\n\n.alert-success {\n  background-color: #dff0d8;\n  border-color: #d0e9c6;\n  color: #3c763d; }\n  .alert-success hr {\n    border-top-color: #c1e2b3; }\n  .alert-success .alert-link {\n    color: #2b542c; }\n\n.alert-info {\n  background-color: #d9edf7;\n  border-color: #bcdff1;\n  color: #31708f; }\n  .alert-info hr {\n    border-top-color: #a6d5ec; }\n  .alert-info .alert-link {\n    color: #245269; }\n\n.alert-warning {\n  background-color: #fcf8e3;\n  border-color: #faf2cc;\n  color: #8a6d3b; }\n  .alert-warning hr {\n    border-top-color: #f7ecb5; }\n  .alert-warning .alert-link {\n    color: #66512c; }\n\n.alert-danger {\n  background-color: #f2dede;\n  border-color: #ebcccc;\n  color: #a94442; }\n  .alert-danger hr {\n    border-top-color: #e4b9b9; }\n  .alert-danger .alert-link {\n    color: #843534; }\n\n@keyframes progress-bar-stripes {\n  from {\n    background-position: 1rem 0; }\n  to {\n    background-position: 0 0; } }\n\n.progress {\n  display: flex;\n  overflow: hidden;\n  font-size: 0.75rem;\n  line-height: 1rem;\n  text-align: center;\n  background-color: #eceeef;\n  border-radius: 0.15rem; }\n\n.progress-bar {\n  height: 1rem;\n  color: #fff;\n  background-color: #004B8D; }\n\n.progress-bar-striped {\n  background-image: linear-gradient(45deg, rgba(255, 255, 255, 0.15) 25%, transparent 25%, transparent 50%, rgba(255, 255, 255, 0.15) 50%, rgba(255, 255, 255, 0.15) 75%, transparent 75%, transparent);\n  background-size: 1rem 1rem; }\n\n.progress-bar-animated {\n  animation: progress-bar-stripes 1s linear infinite; }\n\n.media {\n  display: flex;\n  align-items: flex-start; }\n\n.media-body {\n  flex: 1; }\n\n.list-group {\n  display: flex;\n  flex-direction: column;\n  padding-left: 0;\n  margin-bottom: 0; }\n\n.list-group-item-action {\n  width: 100%;\n  color: #464a4c;\n  text-align: inherit; }\n  .list-group-item-action .list-group-item-heading {\n    color: #292b2c; }\n  .list-group-item-action:focus, .list-group-item-action:hover {\n    color: #464a4c;\n    text-decoration: none;\n    background-color: #f7f7f9; }\n  .list-group-item-action:active {\n    color: #292b2c;\n    background-color: #eceeef; }\n\n.list-group-item {\n  position: relative;\n  display: flex;\n  flex-flow: row wrap;\n  align-items: center;\n  padding: 0.4rem 0.8rem;\n  margin-bottom: -1px;\n  background-color: #fff;\n  border: 1px solid rgba(0, 0, 0, 0.125); }\n  .list-group-item:first-child {\n    border-top-right-radius: 0.15rem;\n    border-top-left-radius: 0.15rem; }\n  .list-group-item:last-child {\n    margin-bottom: 0;\n    border-bottom-right-radius: 0.15rem;\n    border-bottom-left-radius: 0.15rem; }\n  .list-group-item:focus, .list-group-item:hover {\n    text-decoration: none; }\n  .list-group-item.disabled, .list-group-item:disabled {\n    color: #aaa;\n    cursor: not-allowed;\n    background-color: #fff; }\n    .list-group-item.disabled .list-group-item-heading, .list-group-item:disabled .list-group-item-heading {\n      color: inherit; }\n    .list-group-item.disabled .list-group-item-text, .list-group-item:disabled .list-group-item-text {\n      color: #aaa; }\n  .list-group-item.active {\n    z-index: 2;\n    color: #000;\n    background-color: rgba(14, 142, 255, 0.2);\n    border-color: rgba(14, 142, 255, 0.2); }\n    .list-group-item.active .list-group-item-heading,\n    .list-group-item.active .list-group-item-heading > small,\n    .list-group-item.active .list-group-item-heading > .small {\n      color: inherit; }\n    .list-group-item.active .list-group-item-text {\n      color: rgba(255, 255, 255, 0.2); }\n\n.list-group-flush .list-group-item {\n  border-right: 0;\n  border-left: 0;\n  border-radius: 0; }\n\n.list-group-flush:first-child .list-group-item:first-child {\n  border-top: 0; }\n\n.list-group-flush:last-child .list-group-item:last-child {\n  border-bottom: 0; }\n\n.list-group-item-success {\n  color: #3c763d;\n  background-color: #dff0d8; }\n\na.list-group-item-success,\nbutton.list-group-item-success {\n  color: #3c763d; }\n  a.list-group-item-success .list-group-item-heading,\n  button.list-group-item-success .list-group-item-heading {\n    color: inherit; }\n  a.list-group-item-success:focus, a.list-group-item-success:hover,\n  button.list-group-item-success:focus,\n  button.list-group-item-success:hover {\n    color: #3c763d;\n    background-color: #d0e9c6; }\n  a.list-group-item-success.active,\n  button.list-group-item-success.active {\n    color: #fff;\n    background-color: #3c763d;\n    border-color: #3c763d; }\n\n.list-group-item-info {\n  color: #31708f;\n  background-color: #d9edf7; }\n\na.list-group-item-info,\nbutton.list-group-item-info {\n  color: #31708f; }\n  a.list-group-item-info .list-group-item-heading,\n  button.list-group-item-info .list-group-item-heading {\n    color: inherit; }\n  a.list-group-item-info:focus, a.list-group-item-info:hover,\n  button.list-group-item-info:focus,\n  button.list-group-item-info:hover {\n    color: #31708f;\n    background-color: #c4e3f3; }\n  a.list-group-item-info.active,\n  button.list-group-item-info.active {\n    color: #fff;\n    background-color: #31708f;\n    border-color: #31708f; }\n\n.list-group-item-warning {\n  color: #8a6d3b;\n  background-color: #fcf8e3; }\n\na.list-group-item-warning,\nbutton.list-group-item-warning {\n  color: #8a6d3b; }\n  a.list-group-item-warning .list-group-item-heading,\n  button.list-group-item-warning .list-group-item-heading {\n    color: inherit; }\n  a.list-group-item-warning:focus, a.list-group-item-warning:hover,\n  button.list-group-item-warning:focus,\n  button.list-group-item-warning:hover {\n    color: #8a6d3b;\n    background-color: #faf2cc; }\n  a.list-group-item-warning.active,\n  button.list-group-item-warning.active {\n    color: #fff;\n    background-color: #8a6d3b;\n    border-color: #8a6d3b; }\n\n.list-group-item-danger {\n  color: #a94442;\n  background-color: #f2dede; }\n\na.list-group-item-danger,\nbutton.list-group-item-danger {\n  color: #a94442; }\n  a.list-group-item-danger .list-group-item-heading,\n  button.list-group-item-danger .list-group-item-heading {\n    color: inherit; }\n  a.list-group-item-danger:focus, a.list-group-item-danger:hover,\n  button.list-group-item-danger:focus,\n  button.list-group-item-danger:hover {\n    color: #a94442;\n    background-color: #ebcccc; }\n  a.list-group-item-danger.active,\n  button.list-group-item-danger.active {\n    color: #fff;\n    background-color: #a94442;\n    border-color: #a94442; }\n\n.embed-responsive {\n  position: relative;\n  display: block;\n  width: 100%;\n  padding: 0;\n  overflow: hidden; }\n  .embed-responsive::before {\n    display: block;\n    content: \"\"; }\n  .embed-responsive .embed-responsive-item,\n  .embed-responsive iframe,\n  .embed-responsive embed,\n  .embed-responsive object,\n  .embed-responsive video {\n    position: absolute;\n    top: 0;\n    bottom: 0;\n    left: 0;\n    width: 100%;\n    height: 100%;\n    border: 0; }\n\n.embed-responsive-21by9::before {\n  padding-top: 42.85714%; }\n\n.embed-responsive-16by9::before {\n  padding-top: 56.25%; }\n\n.embed-responsive-4by3::before {\n  padding-top: 75%; }\n\n.embed-responsive-1by1::before {\n  padding-top: 100%; }\n\n.close {\n  float: right;\n  font-size: 1.2rem;\n  font-weight: bold;\n  line-height: 1;\n  color: #000;\n  text-shadow: 0 1px 0 #fff;\n  opacity: .5; }\n  .close:focus, .close:hover {\n    color: #000;\n    text-decoration: none;\n    cursor: pointer;\n    opacity: .75; }\n\nbutton.close {\n  padding: 0;\n  cursor: pointer;\n  background: transparent;\n  border: 0;\n  -webkit-appearance: none; }\n\n.modal-open {\n  overflow: hidden; }\n\n.modal {\n  position: fixed;\n  top: 0;\n  right: 0;\n  bottom: 0;\n  left: 0;\n  z-index: 1050;\n  display: none;\n  overflow: hidden;\n  outline: 0; }\n  .modal.fade .modal-dialog {\n    transition: transform 0.3s ease-out;\n    transform: translate(0, -25%); }\n  .modal.show .modal-dialog {\n    transform: translate(0, 0); }\n\n.modal-open .modal {\n  overflow-x: hidden;\n  overflow-y: auto; }\n\n.modal-dialog {\n  position: relative;\n  width: auto;\n  margin: 10px; }\n\n.modal-content {\n  position: relative;\n  display: flex;\n  flex-direction: column;\n  background-color: #fff;\n  background-clip: padding-box;\n  border: 1px solid rgba(0, 0, 0, 0.2);\n  border-radius: 0.2rem;\n  outline: 0; }\n\n.modal-backdrop {\n  position: fixed;\n  top: 0;\n  right: 0;\n  bottom: 0;\n  left: 0;\n  z-index: 1040;\n  background-color: #000; }\n  .modal-backdrop.fade {\n    opacity: 0; }\n  .modal-backdrop.show {\n    opacity: 0.5; }\n\n.modal-header {\n  display: flex;\n  align-items: center;\n  justify-content: space-between;\n  padding: 0.6rem 1.2rem;\n  border-bottom: 1px solid #eceeef; }\n\n.modal-title {\n  margin-bottom: 0;\n  line-height: 1.5; }\n\n.modal-body {\n  position: relative;\n  flex: 1 1 auto;\n  padding: 15px; }\n\n.modal-footer {\n  display: flex;\n  align-items: center;\n  justify-content: flex-end;\n  padding: 15px;\n  border-top: 1px solid #eceeef; }\n  .modal-footer > :not(:first-child) {\n    margin-left: .25rem; }\n  .modal-footer > :not(:last-child) {\n    margin-right: .25rem; }\n\n.modal-scrollbar-measure {\n  position: absolute;\n  top: -9999px;\n  width: 50px;\n  height: 50px;\n  overflow: scroll; }\n\n@media (min-width: 576px) {\n  .modal-dialog {\n    max-width: 500px;\n    margin: 30px auto; }\n  .modal-sm {\n    max-width: 300px; } }\n\n@media (min-width: 992px) {\n  .modal-lg {\n    max-width: 800px; } }\n\n.tooltip {\n  position: absolute;\n  z-index: 1070;\n  display: block;\n  font-family: \"Droid Sans\", Roboto, \"Helvetica Neue\", Arial, sans-serif;\n  font-style: normal;\n  font-weight: normal;\n  letter-spacing: normal;\n  line-break: auto;\n  line-height: 1.5;\n  text-align: left;\n  text-align: start;\n  text-decoration: none;\n  text-shadow: none;\n  text-transform: none;\n  white-space: normal;\n  word-break: normal;\n  word-spacing: normal;\n  font-size: 0.75rem;\n  word-wrap: break-word;\n  opacity: 0; }\n  .tooltip.show {\n    opacity: 0.9; }\n  .tooltip.tooltip-top, .tooltip.bs-tether-element-attached-bottom {\n    padding: 5px 0;\n    margin-top: -3px; }\n    .tooltip.tooltip-top .tooltip-inner::before, .tooltip.bs-tether-element-attached-bottom .tooltip-inner::before {\n      bottom: 0;\n      left: 50%;\n      margin-left: -5px;\n      content: \"\";\n      border-width: 5px 5px 0;\n      border-top-color: black; }\n  .tooltip.tooltip-right, .tooltip.bs-tether-element-attached-left {\n    padding: 0 5px;\n    margin-left: 3px; }\n    .tooltip.tooltip-right .tooltip-inner::before, .tooltip.bs-tether-element-attached-left .tooltip-inner::before {\n      top: 50%;\n      left: 0;\n      margin-top: -5px;\n      content: \"\";\n      border-width: 5px 5px 5px 0;\n      border-right-color: black; }\n  .tooltip.tooltip-bottom, .tooltip.bs-tether-element-attached-top {\n    padding: 5px 0;\n    margin-top: 3px; }\n    .tooltip.tooltip-bottom .tooltip-inner::before, .tooltip.bs-tether-element-attached-top .tooltip-inner::before {\n      top: 0;\n      left: 50%;\n      margin-left: -5px;\n      content: \"\";\n      border-width: 0 5px 5px;\n      border-bottom-color: black; }\n  .tooltip.tooltip-left, .tooltip.bs-tether-element-attached-right {\n    padding: 0 5px;\n    margin-left: -3px; }\n    .tooltip.tooltip-left .tooltip-inner::before, .tooltip.bs-tether-element-attached-right .tooltip-inner::before {\n      top: 50%;\n      right: 0;\n      margin-top: -5px;\n      content: \"\";\n      border-width: 5px 0 5px 5px;\n      border-left-color: black; }\n\n.tooltip-inner {\n  max-width: 200px;\n  padding: 3px 8px;\n  color: #fff;\n  text-align: center;\n  background-color: black;\n  border-radius: 0.15rem; }\n  .tooltip-inner::before {\n    position: absolute;\n    width: 0;\n    height: 0;\n    border-color: transparent;\n    border-style: solid; }\n\n.popover {\n  position: absolute;\n  top: 0;\n  left: 0;\n  z-index: 1060;\n  display: block;\n  max-width: 800px;\n  padding: 1px;\n  font-family: \"Droid Sans\", Roboto, \"Helvetica Neue\", Arial, sans-serif;\n  font-style: normal;\n  font-weight: normal;\n  letter-spacing: normal;\n  line-break: auto;\n  line-height: 1.5;\n  text-align: left;\n  text-align: start;\n  text-decoration: none;\n  text-shadow: none;\n  text-transform: none;\n  white-space: normal;\n  word-break: normal;\n  word-spacing: normal;\n  font-size: 0.75rem;\n  word-wrap: break-word;\n  background-color: #fff;\n  background-clip: padding-box;\n  border: 1px solid rgba(0, 0, 0, 0.2);\n  border-radius: 0.2rem; }\n  .popover.popover-top, .popover.bs-tether-element-attached-bottom {\n    margin-top: -10px; }\n    .popover.popover-top::before, .popover.popover-top::after, .popover.bs-tether-element-attached-bottom::before, .popover.bs-tether-element-attached-bottom::after {\n      left: 50%;\n      border-bottom-width: 0; }\n    .popover.popover-top::before, .popover.bs-tether-element-attached-bottom::before {\n      bottom: -11px;\n      margin-left: -11px;\n      border-top-color: rgba(0, 0, 0, 0.25); }\n    .popover.popover-top::after, .popover.bs-tether-element-attached-bottom::after {\n      bottom: -10px;\n      margin-left: -10px;\n      border-top-color: #fff; }\n  .popover.popover-right, .popover.bs-tether-element-attached-left {\n    margin-left: 10px; }\n    .popover.popover-right::before, .popover.popover-right::after, .popover.bs-tether-element-attached-left::before, .popover.bs-tether-element-attached-left::after {\n      top: 50%;\n      border-left-width: 0; }\n    .popover.popover-right::before, .popover.bs-tether-element-attached-left::before {\n      left: -11px;\n      margin-top: -11px;\n      border-right-color: rgba(0, 0, 0, 0.25); }\n    .popover.popover-right::after, .popover.bs-tether-element-attached-left::after {\n      left: -10px;\n      margin-top: -10px;\n      border-right-color: #fff; }\n  .popover.popover-bottom, .popover.bs-tether-element-attached-top {\n    margin-top: 10px; }\n    .popover.popover-bottom::before, .popover.popover-bottom::after, .popover.bs-tether-element-attached-top::before, .popover.bs-tether-element-attached-top::after {\n      left: 50%;\n      border-top-width: 0; }\n    .popover.popover-bottom::before, .popover.bs-tether-element-attached-top::before {\n      top: -11px;\n      margin-left: -11px;\n      border-bottom-color: rgba(0, 0, 0, 0.25); }\n    .popover.popover-bottom::after, .popover.bs-tether-element-attached-top::after {\n      top: -10px;\n      margin-left: -10px;\n      border-bottom-color: #f7f7f7; }\n    .popover.popover-bottom .popover-title::before, .popover.bs-tether-element-attached-top .popover-title::before {\n      position: absolute;\n      top: 0;\n      left: 50%;\n      display: block;\n      width: 20px;\n      margin-left: -10px;\n      content: \"\";\n      border-bottom: 1px solid #f7f7f7; }\n  .popover.popover-left, .popover.bs-tether-element-attached-right {\n    margin-left: -10px; }\n    .popover.popover-left::before, .popover.popover-left::after, .popover.bs-tether-element-attached-right::before, .popover.bs-tether-element-attached-right::after {\n      top: 50%;\n      border-right-width: 0; }\n    .popover.popover-left::before, .popover.bs-tether-element-attached-right::before {\n      right: -11px;\n      margin-top: -11px;\n      border-left-color: rgba(0, 0, 0, 0.25); }\n    .popover.popover-left::after, .popover.bs-tether-element-attached-right::after {\n      right: -10px;\n      margin-top: -10px;\n      border-left-color: #fff; }\n\n.popover-title {\n  padding: 8px 14px;\n  margin-bottom: 0;\n  font-size: 0.8rem;\n  background-color: #f7f7f7;\n  border-bottom: 1px solid #ebebeb;\n  border-top-right-radius: calc(0.2rem - 1px);\n  border-top-left-radius: calc(0.2rem - 1px); }\n  .popover-title:empty {\n    display: none; }\n\n.popover-content {\n  padding: 9px 14px; }\n\n.popover::before,\n.popover::after {\n  position: absolute;\n  display: block;\n  width: 0;\n  height: 0;\n  border-color: transparent;\n  border-style: solid; }\n\n.popover::before {\n  content: \"\";\n  border-width: 11px; }\n\n.popover::after {\n  content: \"\";\n  border-width: 10px; }\n\n.carousel {\n  position: relative; }\n\n.carousel-inner {\n  position: relative;\n  width: 100%;\n  overflow: hidden; }\n\n.carousel-item {\n  position: relative;\n  display: none;\n  width: 100%; }\n  @media (-webkit-transform-3d) {\n    .carousel-item {\n      transition: transform 0.6s ease-in-out;\n      backface-visibility: hidden;\n      perspective: 1000px; } }\n  @supports (transform: translate3d(0, 0, 0)) {\n    .carousel-item {\n      transition: transform 0.6s ease-in-out;\n      backface-visibility: hidden;\n      perspective: 1000px; } }\n\n.carousel-item.active,\n.carousel-item-next,\n.carousel-item-prev {\n  display: flex; }\n\n.carousel-item-next,\n.carousel-item-prev {\n  position: absolute;\n  top: 0; }\n\n@media (-webkit-transform-3d) {\n  .carousel-item-next.carousel-item-left,\n  .carousel-item-prev.carousel-item-right {\n    transform: translate3d(0, 0, 0); }\n  .carousel-item-next,\n  .active.carousel-item-right {\n    transform: translate3d(100%, 0, 0); }\n  .carousel-item-prev,\n  .active.carousel-item-left {\n    transform: translate3d(-100%, 0, 0); } }\n\n@supports (transform: translate3d(0, 0, 0)) {\n  .carousel-item-next.carousel-item-left,\n  .carousel-item-prev.carousel-item-right {\n    transform: translate3d(0, 0, 0); }\n  .carousel-item-next,\n  .active.carousel-item-right {\n    transform: translate3d(100%, 0, 0); }\n  .carousel-item-prev,\n  .active.carousel-item-left {\n    transform: translate3d(-100%, 0, 0); } }\n\n.carousel-control-prev,\n.carousel-control-next {\n  position: absolute;\n  top: 0;\n  bottom: 0;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  width: 15%;\n  color: #fff;\n  text-align: center;\n  opacity: 0.5; }\n  .carousel-control-prev:focus, .carousel-control-prev:hover,\n  .carousel-control-next:focus,\n  .carousel-control-next:hover {\n    color: #fff;\n    text-decoration: none;\n    outline: 0;\n    opacity: .9; }\n\n.carousel-control-prev {\n  left: 0; }\n\n.carousel-control-next {\n  right: 0; }\n\n.carousel-control-prev-icon,\n.carousel-control-next-icon {\n  display: inline-block;\n  width: 20px;\n  height: 20px;\n  background: transparent no-repeat center center;\n  background-size: 100% 100%; }\n\n.carousel-control-prev-icon {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='%23fff' viewBox='0 0 8 8'%3E%3Cpath d='M4 0l-4 4 4 4 1.5-1.5-2.5-2.5 2.5-2.5-1.5-1.5z'/%3E%3C/svg%3E\"); }\n\n.carousel-control-next-icon {\n  background-image: url(\"data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='%23fff' viewBox='0 0 8 8'%3E%3Cpath d='M1.5 0l-1.5 1.5 2.5 2.5-2.5 2.5 1.5 1.5 4-4-4-4z'/%3E%3C/svg%3E\"); }\n\n.carousel-indicators {\n  position: absolute;\n  right: 0;\n  bottom: 10px;\n  left: 0;\n  z-index: 15;\n  display: flex;\n  justify-content: center;\n  padding-left: 0;\n  margin-right: 15%;\n  margin-left: 15%;\n  list-style: none; }\n  .carousel-indicators li {\n    position: relative;\n    flex: 1 0 auto;\n    max-width: 30px;\n    height: 3px;\n    margin-right: 3px;\n    margin-left: 3px;\n    text-indent: -999px;\n    cursor: pointer;\n    background-color: rgba(255, 255, 255, 0.5); }\n    .carousel-indicators li::before {\n      position: absolute;\n      top: -10px;\n      left: 0;\n      display: inline-block;\n      width: 100%;\n      height: 10px;\n      content: \"\"; }\n    .carousel-indicators li::after {\n      position: absolute;\n      bottom: -10px;\n      left: 0;\n      display: inline-block;\n      width: 100%;\n      height: 10px;\n      content: \"\"; }\n  .carousel-indicators .active {\n    background-color: #fff; }\n\n.carousel-caption {\n  position: absolute;\n  right: 15%;\n  bottom: 20px;\n  left: 15%;\n  z-index: 10;\n  padding-top: 20px;\n  padding-bottom: 20px;\n  color: #fff;\n  text-align: center; }\n\n.align-baseline {\n  vertical-align: baseline !important; }\n\n.align-top {\n  vertical-align: top !important; }\n\n.align-middle {\n  vertical-align: middle !important; }\n\n.align-bottom {\n  vertical-align: bottom !important; }\n\n.align-text-bottom {\n  vertical-align: text-bottom !important; }\n\n.align-text-top {\n  vertical-align: text-top !important; }\n\n.bg-faded {\n  background-color: #eeeef2; }\n\n.bg-primary {\n  background-color: #004B8D !important; }\n\na.bg-primary:focus, a.bg-primary:hover {\n  background-color: #00305a !important; }\n\n.bg-success {\n  background-color: #5cb85c !important; }\n\na.bg-success:focus, a.bg-success:hover {\n  background-color: #449d44 !important; }\n\n.bg-info {\n  background-color: #2196F3 !important; }\n\na.bg-info:focus, a.bg-info:hover {\n  background-color: #0c7cd5 !important; }\n\n.bg-warning {\n  background-color: #f0ad4e !important; }\n\na.bg-warning:focus, a.bg-warning:hover {\n  background-color: #ec971f !important; }\n\n.bg-danger {\n  background-color: #d50000 !important; }\n\na.bg-danger:focus, a.bg-danger:hover {\n  background-color: #a20000 !important; }\n\n.bg-inverse {\n  background-color: #292b2c !important; }\n\na.bg-inverse:focus, a.bg-inverse:hover {\n  background-color: #101112 !important; }\n\n.border-0 {\n  border: 0 !important; }\n\n.border-top-0 {\n  border-top: 0 !important; }\n\n.border-right-0 {\n  border-right: 0 !important; }\n\n.border-bottom-0 {\n  border-bottom: 0 !important; }\n\n.border-left-0 {\n  border-left: 0 !important; }\n\n.rounded {\n  border-radius: 0.15rem; }\n\n.rounded-top {\n  border-top-right-radius: 0.15rem;\n  border-top-left-radius: 0.15rem; }\n\n.rounded-right {\n  border-bottom-right-radius: 0.15rem;\n  border-top-right-radius: 0.15rem; }\n\n.rounded-bottom {\n  border-bottom-right-radius: 0.15rem;\n  border-bottom-left-radius: 0.15rem; }\n\n.rounded-left {\n  border-bottom-left-radius: 0.15rem;\n  border-top-left-radius: 0.15rem; }\n\n.rounded-circle {\n  border-radius: 50%; }\n\n.rounded-0 {\n  border-radius: 0; }\n\n.clearfix::after {\n  display: block;\n  content: \"\";\n  clear: both; }\n\n.d-none {\n  display: none !important; }\n\n.d-inline {\n  display: inline !important; }\n\n.d-inline-block {\n  display: inline-block !important; }\n\n.d-block {\n  display: block !important; }\n\n.d-table {\n  display: table !important; }\n\n.d-table-cell {\n  display: table-cell !important; }\n\n.d-flex {\n  display: flex !important; }\n\n.d-inline-flex {\n  display: inline-flex !important; }\n\n@media (min-width: 576px) {\n  .d-sm-none {\n    display: none !important; }\n  .d-sm-inline {\n    display: inline !important; }\n  .d-sm-inline-block {\n    display: inline-block !important; }\n  .d-sm-block {\n    display: block !important; }\n  .d-sm-table {\n    display: table !important; }\n  .d-sm-table-cell {\n    display: table-cell !important; }\n  .d-sm-flex {\n    display: flex !important; }\n  .d-sm-inline-flex {\n    display: inline-flex !important; } }\n\n@media (min-width: 768px) {\n  .d-md-none {\n    display: none !important; }\n  .d-md-inline {\n    display: inline !important; }\n  .d-md-inline-block {\n    display: inline-block !important; }\n  .d-md-block {\n    display: block !important; }\n  .d-md-table {\n    display: table !important; }\n  .d-md-table-cell {\n    display: table-cell !important; }\n  .d-md-flex {\n    display: flex !important; }\n  .d-md-inline-flex {\n    display: inline-flex !important; } }\n\n@media (min-width: 992px) {\n  .d-lg-none {\n    display: none !important; }\n  .d-lg-inline {\n    display: inline !important; }\n  .d-lg-inline-block {\n    display: inline-block !important; }\n  .d-lg-block {\n    display: block !important; }\n  .d-lg-table {\n    display: table !important; }\n  .d-lg-table-cell {\n    display: table-cell !important; }\n  .d-lg-flex {\n    display: flex !important; }\n  .d-lg-inline-flex {\n    display: inline-flex !important; } }\n\n@media (min-width: 1340px) {\n  .d-xl-none {\n    display: none !important; }\n  .d-xl-inline {\n    display: inline !important; }\n  .d-xl-inline-block {\n    display: inline-block !important; }\n  .d-xl-block {\n    display: block !important; }\n  .d-xl-table {\n    display: table !important; }\n  .d-xl-table-cell {\n    display: table-cell !important; }\n  .d-xl-flex {\n    display: flex !important; }\n  .d-xl-inline-flex {\n    display: inline-flex !important; } }\n\n.flex-first {\n  order: -1; }\n\n.flex-last {\n  order: 1; }\n\n.flex-unordered {\n  order: 0; }\n\n.flex-row {\n  flex-direction: row !important; }\n\n.flex-column {\n  flex-direction: column !important; }\n\n.flex-row-reverse {\n  flex-direction: row-reverse !important; }\n\n.flex-column-reverse {\n  flex-direction: column-reverse !important; }\n\n.flex-wrap {\n  flex-wrap: wrap !important; }\n\n.flex-nowrap {\n  flex-wrap: nowrap !important; }\n\n.flex-wrap-reverse {\n  flex-wrap: wrap-reverse !important; }\n\n.justify-content-start {\n  justify-content: flex-start !important; }\n\n.justify-content-end {\n  justify-content: flex-end !important; }\n\n.justify-content-center {\n  justify-content: center !important; }\n\n.justify-content-between {\n  justify-content: space-between !important; }\n\n.justify-content-around {\n  justify-content: space-around !important; }\n\n.align-items-start {\n  align-items: flex-start !important; }\n\n.align-items-end {\n  align-items: flex-end !important; }\n\n.align-items-center {\n  align-items: center !important; }\n\n.align-items-baseline {\n  align-items: baseline !important; }\n\n.align-items-stretch {\n  align-items: stretch !important; }\n\n.align-content-start {\n  align-content: flex-start !important; }\n\n.align-content-end {\n  align-content: flex-end !important; }\n\n.align-content-center {\n  align-content: center !important; }\n\n.align-content-between {\n  align-content: space-between !important; }\n\n.align-content-around {\n  align-content: space-around !important; }\n\n.align-content-stretch {\n  align-content: stretch !important; }\n\n.align-self-auto {\n  align-self: auto !important; }\n\n.align-self-start {\n  align-self: flex-start !important; }\n\n.align-self-end {\n  align-self: flex-end !important; }\n\n.align-self-center {\n  align-self: center !important; }\n\n.align-self-baseline {\n  align-self: baseline !important; }\n\n.align-self-stretch {\n  align-self: stretch !important; }\n\n@media (min-width: 576px) {\n  .flex-sm-first {\n    order: -1; }\n  .flex-sm-last {\n    order: 1; }\n  .flex-sm-unordered {\n    order: 0; }\n  .flex-sm-row {\n    flex-direction: row !important; }\n  .flex-sm-column {\n    flex-direction: column !important; }\n  .flex-sm-row-reverse {\n    flex-direction: row-reverse !important; }\n  .flex-sm-column-reverse {\n    flex-direction: column-reverse !important; }\n  .flex-sm-wrap {\n    flex-wrap: wrap !important; }\n  .flex-sm-nowrap {\n    flex-wrap: nowrap !important; }\n  .flex-sm-wrap-reverse {\n    flex-wrap: wrap-reverse !important; }\n  .justify-content-sm-start {\n    justify-content: flex-start !important; }\n  .justify-content-sm-end {\n    justify-content: flex-end !important; }\n  .justify-content-sm-center {\n    justify-content: center !important; }\n  .justify-content-sm-between {\n    justify-content: space-between !important; }\n  .justify-content-sm-around {\n    justify-content: space-around !important; }\n  .align-items-sm-start {\n    align-items: flex-start !important; }\n  .align-items-sm-end {\n    align-items: flex-end !important; }\n  .align-items-sm-center {\n    align-items: center !important; }\n  .align-items-sm-baseline {\n    align-items: baseline !important; }\n  .align-items-sm-stretch {\n    align-items: stretch !important; }\n  .align-content-sm-start {\n    align-content: flex-start !important; }\n  .align-content-sm-end {\n    align-content: flex-end !important; }\n  .align-content-sm-center {\n    align-content: center !important; }\n  .align-content-sm-between {\n    align-content: space-between !important; }\n  .align-content-sm-around {\n    align-content: space-around !important; }\n  .align-content-sm-stretch {\n    align-content: stretch !important; }\n  .align-self-sm-auto {\n    align-self: auto !important; }\n  .align-self-sm-start {\n    align-self: flex-start !important; }\n  .align-self-sm-end {\n    align-self: flex-end !important; }\n  .align-self-sm-center {\n    align-self: center !important; }\n  .align-self-sm-baseline {\n    align-self: baseline !important; }\n  .align-self-sm-stretch {\n    align-self: stretch !important; } }\n\n@media (min-width: 768px) {\n  .flex-md-first {\n    order: -1; }\n  .flex-md-last {\n    order: 1; }\n  .flex-md-unordered {\n    order: 0; }\n  .flex-md-row {\n    flex-direction: row !important; }\n  .flex-md-column {\n    flex-direction: column !important; }\n  .flex-md-row-reverse {\n    flex-direction: row-reverse !important; }\n  .flex-md-column-reverse {\n    flex-direction: column-reverse !important; }\n  .flex-md-wrap {\n    flex-wrap: wrap !important; }\n  .flex-md-nowrap {\n    flex-wrap: nowrap !important; }\n  .flex-md-wrap-reverse {\n    flex-wrap: wrap-reverse !important; }\n  .justify-content-md-start {\n    justify-content: flex-start !important; }\n  .justify-content-md-end {\n    justify-content: flex-end !important; }\n  .justify-content-md-center {\n    justify-content: center !important; }\n  .justify-content-md-between {\n    justify-content: space-between !important; }\n  .justify-content-md-around {\n    justify-content: space-around !important; }\n  .align-items-md-start {\n    align-items: flex-start !important; }\n  .align-items-md-end {\n    align-items: flex-end !important; }\n  .align-items-md-center {\n    align-items: center !important; }\n  .align-items-md-baseline {\n    align-items: baseline !important; }\n  .align-items-md-stretch {\n    align-items: stretch !important; }\n  .align-content-md-start {\n    align-content: flex-start !important; }\n  .align-content-md-end {\n    align-content: flex-end !important; }\n  .align-content-md-center {\n    align-content: center !important; }\n  .align-content-md-between {\n    align-content: space-between !important; }\n  .align-content-md-around {\n    align-content: space-around !important; }\n  .align-content-md-stretch {\n    align-content: stretch !important; }\n  .align-self-md-auto {\n    align-self: auto !important; }\n  .align-self-md-start {\n    align-self: flex-start !important; }\n  .align-self-md-end {\n    align-self: flex-end !important; }\n  .align-self-md-center {\n    align-self: center !important; }\n  .align-self-md-baseline {\n    align-self: baseline !important; }\n  .align-self-md-stretch {\n    align-self: stretch !important; } }\n\n@media (min-width: 992px) {\n  .flex-lg-first {\n    order: -1; }\n  .flex-lg-last {\n    order: 1; }\n  .flex-lg-unordered {\n    order: 0; }\n  .flex-lg-row {\n    flex-direction: row !important; }\n  .flex-lg-column {\n    flex-direction: column !important; }\n  .flex-lg-row-reverse {\n    flex-direction: row-reverse !important; }\n  .flex-lg-column-reverse {\n    flex-direction: column-reverse !important; }\n  .flex-lg-wrap {\n    flex-wrap: wrap !important; }\n  .flex-lg-nowrap {\n    flex-wrap: nowrap !important; }\n  .flex-lg-wrap-reverse {\n    flex-wrap: wrap-reverse !important; }\n  .justify-content-lg-start {\n    justify-content: flex-start !important; }\n  .justify-content-lg-end {\n    justify-content: flex-end !important; }\n  .justify-content-lg-center {\n    justify-content: center !important; }\n  .justify-content-lg-between {\n    justify-content: space-between !important; }\n  .justify-content-lg-around {\n    justify-content: space-around !important; }\n  .align-items-lg-start {\n    align-items: flex-start !important; }\n  .align-items-lg-end {\n    align-items: flex-end !important; }\n  .align-items-lg-center {\n    align-items: center !important; }\n  .align-items-lg-baseline {\n    align-items: baseline !important; }\n  .align-items-lg-stretch {\n    align-items: stretch !important; }\n  .align-content-lg-start {\n    align-content: flex-start !important; }\n  .align-content-lg-end {\n    align-content: flex-end !important; }\n  .align-content-lg-center {\n    align-content: center !important; }\n  .align-content-lg-between {\n    align-content: space-between !important; }\n  .align-content-lg-around {\n    align-content: space-around !important; }\n  .align-content-lg-stretch {\n    align-content: stretch !important; }\n  .align-self-lg-auto {\n    align-self: auto !important; }\n  .align-self-lg-start {\n    align-self: flex-start !important; }\n  .align-self-lg-end {\n    align-self: flex-end !important; }\n  .align-self-lg-center {\n    align-self: center !important; }\n  .align-self-lg-baseline {\n    align-self: baseline !important; }\n  .align-self-lg-stretch {\n    align-self: stretch !important; } }\n\n@media (min-width: 1340px) {\n  .flex-xl-first {\n    order: -1; }\n  .flex-xl-last {\n    order: 1; }\n  .flex-xl-unordered {\n    order: 0; }\n  .flex-xl-row {\n    flex-direction: row !important; }\n  .flex-xl-column {\n    flex-direction: column !important; }\n  .flex-xl-row-reverse {\n    flex-direction: row-reverse !important; }\n  .flex-xl-column-reverse {\n    flex-direction: column-reverse !important; }\n  .flex-xl-wrap {\n    flex-wrap: wrap !important; }\n  .flex-xl-nowrap {\n    flex-wrap: nowrap !important; }\n  .flex-xl-wrap-reverse {\n    flex-wrap: wrap-reverse !important; }\n  .justify-content-xl-start {\n    justify-content: flex-start !important; }\n  .justify-content-xl-end {\n    justify-content: flex-end !important; }\n  .justify-content-xl-center {\n    justify-content: center !important; }\n  .justify-content-xl-between {\n    justify-content: space-between !important; }\n  .justify-content-xl-around {\n    justify-content: space-around !important; }\n  .align-items-xl-start {\n    align-items: flex-start !important; }\n  .align-items-xl-end {\n    align-items: flex-end !important; }\n  .align-items-xl-center {\n    align-items: center !important; }\n  .align-items-xl-baseline {\n    align-items: baseline !important; }\n  .align-items-xl-stretch {\n    align-items: stretch !important; }\n  .align-content-xl-start {\n    align-content: flex-start !important; }\n  .align-content-xl-end {\n    align-content: flex-end !important; }\n  .align-content-xl-center {\n    align-content: center !important; }\n  .align-content-xl-between {\n    align-content: space-between !important; }\n  .align-content-xl-around {\n    align-content: space-around !important; }\n  .align-content-xl-stretch {\n    align-content: stretch !important; }\n  .align-self-xl-auto {\n    align-self: auto !important; }\n  .align-self-xl-start {\n    align-self: flex-start !important; }\n  .align-self-xl-end {\n    align-self: flex-end !important; }\n  .align-self-xl-center {\n    align-self: center !important; }\n  .align-self-xl-baseline {\n    align-self: baseline !important; }\n  .align-self-xl-stretch {\n    align-self: stretch !important; } }\n\n.float-left {\n  float: left !important; }\n\n.float-right {\n  float: right !important; }\n\n.float-none {\n  float: none !important; }\n\n@media (min-width: 576px) {\n  .float-sm-left {\n    float: left !important; }\n  .float-sm-right {\n    float: right !important; }\n  .float-sm-none {\n    float: none !important; } }\n\n@media (min-width: 768px) {\n  .float-md-left {\n    float: left !important; }\n  .float-md-right {\n    float: right !important; }\n  .float-md-none {\n    float: none !important; } }\n\n@media (min-width: 992px) {\n  .float-lg-left {\n    float: left !important; }\n  .float-lg-right {\n    float: right !important; }\n  .float-lg-none {\n    float: none !important; } }\n\n@media (min-width: 1340px) {\n  .float-xl-left {\n    float: left !important; }\n  .float-xl-right {\n    float: right !important; }\n  .float-xl-none {\n    float: none !important; } }\n\n.fixed-top {\n  position: fixed;\n  top: 0;\n  right: 0;\n  left: 0;\n  z-index: 1030; }\n\n.fixed-bottom {\n  position: fixed;\n  right: 0;\n  bottom: 0;\n  left: 0;\n  z-index: 1030; }\n\n.sticky-top {\n  position: sticky;\n  top: 0;\n  z-index: 1030; }\n\n.sr-only {\n  position: absolute;\n  width: 1px;\n  height: 1px;\n  padding: 0;\n  margin: -1px;\n  overflow: hidden;\n  clip: rect(0, 0, 0, 0);\n  border: 0; }\n\n.sr-only-focusable:active, .sr-only-focusable:focus {\n  position: static;\n  width: auto;\n  height: auto;\n  margin: 0;\n  overflow: visible;\n  clip: auto; }\n\n.w-25 {\n  width: 25% !important; }\n\n.w-50 {\n  width: 50% !important; }\n\n.w-75 {\n  width: 75% !important; }\n\n.w-100 {\n  width: 100% !important; }\n\n.h-25 {\n  height: 25% !important; }\n\n.h-50 {\n  height: 50% !important; }\n\n.h-75 {\n  height: 75% !important; }\n\n.h-100 {\n  height: 100% !important; }\n\n.mw-100 {\n  max-width: 100% !important; }\n\n.mh-100 {\n  max-height: 100% !important; }\n\n.m-0 {\n  margin: 0 0 !important; }\n\n.mt-0 {\n  margin-top: 0 !important; }\n\n.mr-0 {\n  margin-right: 0 !important; }\n\n.mb-0 {\n  margin-bottom: 0 !important; }\n\n.ml-0 {\n  margin-left: 0 !important; }\n\n.mx-0 {\n  margin-right: 0 !important;\n  margin-left: 0 !important; }\n\n.my-0 {\n  margin-top: 0 !important;\n  margin-bottom: 0 !important; }\n\n.m-1 {\n  margin: 0.25rem 0.25rem !important; }\n\n.mt-1 {\n  margin-top: 0.25rem !important; }\n\n.mr-1 {\n  margin-right: 0.25rem !important; }\n\n.mb-1 {\n  margin-bottom: 0.25rem !important; }\n\n.ml-1 {\n  margin-left: 0.25rem !important; }\n\n.mx-1 {\n  margin-right: 0.25rem !important;\n  margin-left: 0.25rem !important; }\n\n.my-1 {\n  margin-top: 0.25rem !important;\n  margin-bottom: 0.25rem !important; }\n\n.m-2 {\n  margin: 0.5rem 0.5rem !important; }\n\n.mt-2 {\n  margin-top: 0.5rem !important; }\n\n.mr-2 {\n  margin-right: 0.5rem !important; }\n\n.mb-2 {\n  margin-bottom: 0.5rem !important; }\n\n.ml-2 {\n  margin-left: 0.5rem !important; }\n\n.mx-2 {\n  margin-right: 0.5rem !important;\n  margin-left: 0.5rem !important; }\n\n.my-2 {\n  margin-top: 0.5rem !important;\n  margin-bottom: 0.5rem !important; }\n\n.m-3 {\n  margin: 1rem 1rem !important; }\n\n.mt-3 {\n  margin-top: 1rem !important; }\n\n.mr-3 {\n  margin-right: 1rem !important; }\n\n.mb-3 {\n  margin-bottom: 1rem !important; }\n\n.ml-3 {\n  margin-left: 1rem !important; }\n\n.mx-3 {\n  margin-right: 1rem !important;\n  margin-left: 1rem !important; }\n\n.my-3 {\n  margin-top: 1rem !important;\n  margin-bottom: 1rem !important; }\n\n.m-4 {\n  margin: 1.5rem 1.5rem !important; }\n\n.mt-4 {\n  margin-top: 1.5rem !important; }\n\n.mr-4 {\n  margin-right: 1.5rem !important; }\n\n.mb-4 {\n  margin-bottom: 1.5rem !important; }\n\n.ml-4 {\n  margin-left: 1.5rem !important; }\n\n.mx-4 {\n  margin-right: 1.5rem !important;\n  margin-left: 1.5rem !important; }\n\n.my-4 {\n  margin-top: 1.5rem !important;\n  margin-bottom: 1.5rem !important; }\n\n.m-5 {\n  margin: 3rem 3rem !important; }\n\n.mt-5 {\n  margin-top: 3rem !important; }\n\n.mr-5 {\n  margin-right: 3rem !important; }\n\n.mb-5 {\n  margin-bottom: 3rem !important; }\n\n.ml-5 {\n  margin-left: 3rem !important; }\n\n.mx-5 {\n  margin-right: 3rem !important;\n  margin-left: 3rem !important; }\n\n.my-5 {\n  margin-top: 3rem !important;\n  margin-bottom: 3rem !important; }\n\n.p-0 {\n  padding: 0 0 !important; }\n\n.pt-0 {\n  padding-top: 0 !important; }\n\n.pr-0 {\n  padding-right: 0 !important; }\n\n.pb-0 {\n  padding-bottom: 0 !important; }\n\n.pl-0 {\n  padding-left: 0 !important; }\n\n.px-0 {\n  padding-right: 0 !important;\n  padding-left: 0 !important; }\n\n.py-0 {\n  padding-top: 0 !important;\n  padding-bottom: 0 !important; }\n\n.p-1 {\n  padding: 0.25rem 0.25rem !important; }\n\n.pt-1 {\n  padding-top: 0.25rem !important; }\n\n.pr-1 {\n  padding-right: 0.25rem !important; }\n\n.pb-1 {\n  padding-bottom: 0.25rem !important; }\n\n.pl-1 {\n  padding-left: 0.25rem !important; }\n\n.px-1 {\n  padding-right: 0.25rem !important;\n  padding-left: 0.25rem !important; }\n\n.py-1 {\n  padding-top: 0.25rem !important;\n  padding-bottom: 0.25rem !important; }\n\n.p-2 {\n  padding: 0.5rem 0.5rem !important; }\n\n.pt-2 {\n  padding-top: 0.5rem !important; }\n\n.pr-2 {\n  padding-right: 0.5rem !important; }\n\n.pb-2 {\n  padding-bottom: 0.5rem !important; }\n\n.pl-2 {\n  padding-left: 0.5rem !important; }\n\n.px-2 {\n  padding-right: 0.5rem !important;\n  padding-left: 0.5rem !important; }\n\n.py-2 {\n  padding-top: 0.5rem !important;\n  padding-bottom: 0.5rem !important; }\n\n.p-3 {\n  padding: 1rem 1rem !important; }\n\n.pt-3 {\n  padding-top: 1rem !important; }\n\n.pr-3 {\n  padding-right: 1rem !important; }\n\n.pb-3 {\n  padding-bottom: 1rem !important; }\n\n.pl-3 {\n  padding-left: 1rem !important; }\n\n.px-3 {\n  padding-right: 1rem !important;\n  padding-left: 1rem !important; }\n\n.py-3 {\n  padding-top: 1rem !important;\n  padding-bottom: 1rem !important; }\n\n.p-4 {\n  padding: 1.5rem 1.5rem !important; }\n\n.pt-4 {\n  padding-top: 1.5rem !important; }\n\n.pr-4 {\n  padding-right: 1.5rem !important; }\n\n.pb-4 {\n  padding-bottom: 1.5rem !important; }\n\n.pl-4 {\n  padding-left: 1.5rem !important; }\n\n.px-4 {\n  padding-right: 1.5rem !important;\n  padding-left: 1.5rem !important; }\n\n.py-4 {\n  padding-top: 1.5rem !important;\n  padding-bottom: 1.5rem !important; }\n\n.p-5 {\n  padding: 3rem 3rem !important; }\n\n.pt-5 {\n  padding-top: 3rem !important; }\n\n.pr-5 {\n  padding-right: 3rem !important; }\n\n.pb-5 {\n  padding-bottom: 3rem !important; }\n\n.pl-5 {\n  padding-left: 3rem !important; }\n\n.px-5 {\n  padding-right: 3rem !important;\n  padding-left: 3rem !important; }\n\n.py-5 {\n  padding-top: 3rem !important;\n  padding-bottom: 3rem !important; }\n\n.m-auto {\n  margin: auto !important; }\n\n.mt-auto {\n  margin-top: auto !important; }\n\n.mr-auto {\n  margin-right: auto !important; }\n\n.mb-auto {\n  margin-bottom: auto !important; }\n\n.ml-auto {\n  margin-left: auto !important; }\n\n.mx-auto {\n  margin-right: auto !important;\n  margin-left: auto !important; }\n\n.my-auto {\n  margin-top: auto !important;\n  margin-bottom: auto !important; }\n\n@media (min-width: 576px) {\n  .m-sm-0 {\n    margin: 0 0 !important; }\n  .mt-sm-0 {\n    margin-top: 0 !important; }\n  .mr-sm-0 {\n    margin-right: 0 !important; }\n  .mb-sm-0 {\n    margin-bottom: 0 !important; }\n  .ml-sm-0 {\n    margin-left: 0 !important; }\n  .mx-sm-0 {\n    margin-right: 0 !important;\n    margin-left: 0 !important; }\n  .my-sm-0 {\n    margin-top: 0 !important;\n    margin-bottom: 0 !important; }\n  .m-sm-1 {\n    margin: 0.25rem 0.25rem !important; }\n  .mt-sm-1 {\n    margin-top: 0.25rem !important; }\n  .mr-sm-1 {\n    margin-right: 0.25rem !important; }\n  .mb-sm-1 {\n    margin-bottom: 0.25rem !important; }\n  .ml-sm-1 {\n    margin-left: 0.25rem !important; }\n  .mx-sm-1 {\n    margin-right: 0.25rem !important;\n    margin-left: 0.25rem !important; }\n  .my-sm-1 {\n    margin-top: 0.25rem !important;\n    margin-bottom: 0.25rem !important; }\n  .m-sm-2 {\n    margin: 0.5rem 0.5rem !important; }\n  .mt-sm-2 {\n    margin-top: 0.5rem !important; }\n  .mr-sm-2 {\n    margin-right: 0.5rem !important; }\n  .mb-sm-2 {\n    margin-bottom: 0.5rem !important; }\n  .ml-sm-2 {\n    margin-left: 0.5rem !important; }\n  .mx-sm-2 {\n    margin-right: 0.5rem !important;\n    margin-left: 0.5rem !important; }\n  .my-sm-2 {\n    margin-top: 0.5rem !important;\n    margin-bottom: 0.5rem !important; }\n  .m-sm-3 {\n    margin: 1rem 1rem !important; }\n  .mt-sm-3 {\n    margin-top: 1rem !important; }\n  .mr-sm-3 {\n    margin-right: 1rem !important; }\n  .mb-sm-3 {\n    margin-bottom: 1rem !important; }\n  .ml-sm-3 {\n    margin-left: 1rem !important; }\n  .mx-sm-3 {\n    margin-right: 1rem !important;\n    margin-left: 1rem !important; }\n  .my-sm-3 {\n    margin-top: 1rem !important;\n    margin-bottom: 1rem !important; }\n  .m-sm-4 {\n    margin: 1.5rem 1.5rem !important; }\n  .mt-sm-4 {\n    margin-top: 1.5rem !important; }\n  .mr-sm-4 {\n    margin-right: 1.5rem !important; }\n  .mb-sm-4 {\n    margin-bottom: 1.5rem !important; }\n  .ml-sm-4 {\n    margin-left: 1.5rem !important; }\n  .mx-sm-4 {\n    margin-right: 1.5rem !important;\n    margin-left: 1.5rem !important; }\n  .my-sm-4 {\n    margin-top: 1.5rem !important;\n    margin-bottom: 1.5rem !important; }\n  .m-sm-5 {\n    margin: 3rem 3rem !important; }\n  .mt-sm-5 {\n    margin-top: 3rem !important; }\n  .mr-sm-5 {\n    margin-right: 3rem !important; }\n  .mb-sm-5 {\n    margin-bottom: 3rem !important; }\n  .ml-sm-5 {\n    margin-left: 3rem !important; }\n  .mx-sm-5 {\n    margin-right: 3rem !important;\n    margin-left: 3rem !important; }\n  .my-sm-5 {\n    margin-top: 3rem !important;\n    margin-bottom: 3rem !important; }\n  .p-sm-0 {\n    padding: 0 0 !important; }\n  .pt-sm-0 {\n    padding-top: 0 !important; }\n  .pr-sm-0 {\n    padding-right: 0 !important; }\n  .pb-sm-0 {\n    padding-bottom: 0 !important; }\n  .pl-sm-0 {\n    padding-left: 0 !important; }\n  .px-sm-0 {\n    padding-right: 0 !important;\n    padding-left: 0 !important; }\n  .py-sm-0 {\n    padding-top: 0 !important;\n    padding-bottom: 0 !important; }\n  .p-sm-1 {\n    padding: 0.25rem 0.25rem !important; }\n  .pt-sm-1 {\n    padding-top: 0.25rem !important; }\n  .pr-sm-1 {\n    padding-right: 0.25rem !important; }\n  .pb-sm-1 {\n    padding-bottom: 0.25rem !important; }\n  .pl-sm-1 {\n    padding-left: 0.25rem !important; }\n  .px-sm-1 {\n    padding-right: 0.25rem !important;\n    padding-left: 0.25rem !important; }\n  .py-sm-1 {\n    padding-top: 0.25rem !important;\n    padding-bottom: 0.25rem !important; }\n  .p-sm-2 {\n    padding: 0.5rem 0.5rem !important; }\n  .pt-sm-2 {\n    padding-top: 0.5rem !important; }\n  .pr-sm-2 {\n    padding-right: 0.5rem !important; }\n  .pb-sm-2 {\n    padding-bottom: 0.5rem !important; }\n  .pl-sm-2 {\n    padding-left: 0.5rem !important; }\n  .px-sm-2 {\n    padding-right: 0.5rem !important;\n    padding-left: 0.5rem !important; }\n  .py-sm-2 {\n    padding-top: 0.5rem !important;\n    padding-bottom: 0.5rem !important; }\n  .p-sm-3 {\n    padding: 1rem 1rem !important; }\n  .pt-sm-3 {\n    padding-top: 1rem !important; }\n  .pr-sm-3 {\n    padding-right: 1rem !important; }\n  .pb-sm-3 {\n    padding-bottom: 1rem !important; }\n  .pl-sm-3 {\n    padding-left: 1rem !important; }\n  .px-sm-3 {\n    padding-right: 1rem !important;\n    padding-left: 1rem !important; }\n  .py-sm-3 {\n    padding-top: 1rem !important;\n    padding-bottom: 1rem !important; }\n  .p-sm-4 {\n    padding: 1.5rem 1.5rem !important; }\n  .pt-sm-4 {\n    padding-top: 1.5rem !important; }\n  .pr-sm-4 {\n    padding-right: 1.5rem !important; }\n  .pb-sm-4 {\n    padding-bottom: 1.5rem !important; }\n  .pl-sm-4 {\n    padding-left: 1.5rem !important; }\n  .px-sm-4 {\n    padding-right: 1.5rem !important;\n    padding-left: 1.5rem !important; }\n  .py-sm-4 {\n    padding-top: 1.5rem !important;\n    padding-bottom: 1.5rem !important; }\n  .p-sm-5 {\n    padding: 3rem 3rem !important; }\n  .pt-sm-5 {\n    padding-top: 3rem !important; }\n  .pr-sm-5 {\n    padding-right: 3rem !important; }\n  .pb-sm-5 {\n    padding-bottom: 3rem !important; }\n  .pl-sm-5 {\n    padding-left: 3rem !important; }\n  .px-sm-5 {\n    padding-right: 3rem !important;\n    padding-left: 3rem !important; }\n  .py-sm-5 {\n    padding-top: 3rem !important;\n    padding-bottom: 3rem !important; }\n  .m-sm-auto {\n    margin: auto !important; }\n  .mt-sm-auto {\n    margin-top: auto !important; }\n  .mr-sm-auto {\n    margin-right: auto !important; }\n  .mb-sm-auto {\n    margin-bottom: auto !important; }\n  .ml-sm-auto {\n    margin-left: auto !important; }\n  .mx-sm-auto {\n    margin-right: auto !important;\n    margin-left: auto !important; }\n  .my-sm-auto {\n    margin-top: auto !important;\n    margin-bottom: auto !important; } }\n\n@media (min-width: 768px) {\n  .m-md-0 {\n    margin: 0 0 !important; }\n  .mt-md-0 {\n    margin-top: 0 !important; }\n  .mr-md-0 {\n    margin-right: 0 !important; }\n  .mb-md-0 {\n    margin-bottom: 0 !important; }\n  .ml-md-0 {\n    margin-left: 0 !important; }\n  .mx-md-0 {\n    margin-right: 0 !important;\n    margin-left: 0 !important; }\n  .my-md-0 {\n    margin-top: 0 !important;\n    margin-bottom: 0 !important; }\n  .m-md-1 {\n    margin: 0.25rem 0.25rem !important; }\n  .mt-md-1 {\n    margin-top: 0.25rem !important; }\n  .mr-md-1 {\n    margin-right: 0.25rem !important; }\n  .mb-md-1 {\n    margin-bottom: 0.25rem !important; }\n  .ml-md-1 {\n    margin-left: 0.25rem !important; }\n  .mx-md-1 {\n    margin-right: 0.25rem !important;\n    margin-left: 0.25rem !important; }\n  .my-md-1 {\n    margin-top: 0.25rem !important;\n    margin-bottom: 0.25rem !important; }\n  .m-md-2 {\n    margin: 0.5rem 0.5rem !important; }\n  .mt-md-2 {\n    margin-top: 0.5rem !important; }\n  .mr-md-2 {\n    margin-right: 0.5rem !important; }\n  .mb-md-2 {\n    margin-bottom: 0.5rem !important; }\n  .ml-md-2 {\n    margin-left: 0.5rem !important; }\n  .mx-md-2 {\n    margin-right: 0.5rem !important;\n    margin-left: 0.5rem !important; }\n  .my-md-2 {\n    margin-top: 0.5rem !important;\n    margin-bottom: 0.5rem !important; }\n  .m-md-3 {\n    margin: 1rem 1rem !important; }\n  .mt-md-3 {\n    margin-top: 1rem !important; }\n  .mr-md-3 {\n    margin-right: 1rem !important; }\n  .mb-md-3 {\n    margin-bottom: 1rem !important; }\n  .ml-md-3 {\n    margin-left: 1rem !important; }\n  .mx-md-3 {\n    margin-right: 1rem !important;\n    margin-left: 1rem !important; }\n  .my-md-3 {\n    margin-top: 1rem !important;\n    margin-bottom: 1rem !important; }\n  .m-md-4 {\n    margin: 1.5rem 1.5rem !important; }\n  .mt-md-4 {\n    margin-top: 1.5rem !important; }\n  .mr-md-4 {\n    margin-right: 1.5rem !important; }\n  .mb-md-4 {\n    margin-bottom: 1.5rem !important; }\n  .ml-md-4 {\n    margin-left: 1.5rem !important; }\n  .mx-md-4 {\n    margin-right: 1.5rem !important;\n    margin-left: 1.5rem !important; }\n  .my-md-4 {\n    margin-top: 1.5rem !important;\n    margin-bottom: 1.5rem !important; }\n  .m-md-5 {\n    margin: 3rem 3rem !important; }\n  .mt-md-5 {\n    margin-top: 3rem !important; }\n  .mr-md-5 {\n    margin-right: 3rem !important; }\n  .mb-md-5 {\n    margin-bottom: 3rem !important; }\n  .ml-md-5 {\n    margin-left: 3rem !important; }\n  .mx-md-5 {\n    margin-right: 3rem !important;\n    margin-left: 3rem !important; }\n  .my-md-5 {\n    margin-top: 3rem !important;\n    margin-bottom: 3rem !important; }\n  .p-md-0 {\n    padding: 0 0 !important; }\n  .pt-md-0 {\n    padding-top: 0 !important; }\n  .pr-md-0 {\n    padding-right: 0 !important; }\n  .pb-md-0 {\n    padding-bottom: 0 !important; }\n  .pl-md-0 {\n    padding-left: 0 !important; }\n  .px-md-0 {\n    padding-right: 0 !important;\n    padding-left: 0 !important; }\n  .py-md-0 {\n    padding-top: 0 !important;\n    padding-bottom: 0 !important; }\n  .p-md-1 {\n    padding: 0.25rem 0.25rem !important; }\n  .pt-md-1 {\n    padding-top: 0.25rem !important; }\n  .pr-md-1 {\n    padding-right: 0.25rem !important; }\n  .pb-md-1 {\n    padding-bottom: 0.25rem !important; }\n  .pl-md-1 {\n    padding-left: 0.25rem !important; }\n  .px-md-1 {\n    padding-right: 0.25rem !important;\n    padding-left: 0.25rem !important; }\n  .py-md-1 {\n    padding-top: 0.25rem !important;\n    padding-bottom: 0.25rem !important; }\n  .p-md-2 {\n    padding: 0.5rem 0.5rem !important; }\n  .pt-md-2 {\n    padding-top: 0.5rem !important; }\n  .pr-md-2 {\n    padding-right: 0.5rem !important; }\n  .pb-md-2 {\n    padding-bottom: 0.5rem !important; }\n  .pl-md-2 {\n    padding-left: 0.5rem !important; }\n  .px-md-2 {\n    padding-right: 0.5rem !important;\n    padding-left: 0.5rem !important; }\n  .py-md-2 {\n    padding-top: 0.5rem !important;\n    padding-bottom: 0.5rem !important; }\n  .p-md-3 {\n    padding: 1rem 1rem !important; }\n  .pt-md-3 {\n    padding-top: 1rem !important; }\n  .pr-md-3 {\n    padding-right: 1rem !important; }\n  .pb-md-3 {\n    padding-bottom: 1rem !important; }\n  .pl-md-3 {\n    padding-left: 1rem !important; }\n  .px-md-3 {\n    padding-right: 1rem !important;\n    padding-left: 1rem !important; }\n  .py-md-3 {\n    padding-top: 1rem !important;\n    padding-bottom: 1rem !important; }\n  .p-md-4 {\n    padding: 1.5rem 1.5rem !important; }\n  .pt-md-4 {\n    padding-top: 1.5rem !important; }\n  .pr-md-4 {\n    padding-right: 1.5rem !important; }\n  .pb-md-4 {\n    padding-bottom: 1.5rem !important; }\n  .pl-md-4 {\n    padding-left: 1.5rem !important; }\n  .px-md-4 {\n    padding-right: 1.5rem !important;\n    padding-left: 1.5rem !important; }\n  .py-md-4 {\n    padding-top: 1.5rem !important;\n    padding-bottom: 1.5rem !important; }\n  .p-md-5 {\n    padding: 3rem 3rem !important; }\n  .pt-md-5 {\n    padding-top: 3rem !important; }\n  .pr-md-5 {\n    padding-right: 3rem !important; }\n  .pb-md-5 {\n    padding-bottom: 3rem !important; }\n  .pl-md-5 {\n    padding-left: 3rem !important; }\n  .px-md-5 {\n    padding-right: 3rem !important;\n    padding-left: 3rem !important; }\n  .py-md-5 {\n    padding-top: 3rem !important;\n    padding-bottom: 3rem !important; }\n  .m-md-auto {\n    margin: auto !important; }\n  .mt-md-auto {\n    margin-top: auto !important; }\n  .mr-md-auto {\n    margin-right: auto !important; }\n  .mb-md-auto {\n    margin-bottom: auto !important; }\n  .ml-md-auto {\n    margin-left: auto !important; }\n  .mx-md-auto {\n    margin-right: auto !important;\n    margin-left: auto !important; }\n  .my-md-auto {\n    margin-top: auto !important;\n    margin-bottom: auto !important; } }\n\n@media (min-width: 992px) {\n  .m-lg-0 {\n    margin: 0 0 !important; }\n  .mt-lg-0 {\n    margin-top: 0 !important; }\n  .mr-lg-0 {\n    margin-right: 0 !important; }\n  .mb-lg-0 {\n    margin-bottom: 0 !important; }\n  .ml-lg-0 {\n    margin-left: 0 !important; }\n  .mx-lg-0 {\n    margin-right: 0 !important;\n    margin-left: 0 !important; }\n  .my-lg-0 {\n    margin-top: 0 !important;\n    margin-bottom: 0 !important; }\n  .m-lg-1 {\n    margin: 0.25rem 0.25rem !important; }\n  .mt-lg-1 {\n    margin-top: 0.25rem !important; }\n  .mr-lg-1 {\n    margin-right: 0.25rem !important; }\n  .mb-lg-1 {\n    margin-bottom: 0.25rem !important; }\n  .ml-lg-1 {\n    margin-left: 0.25rem !important; }\n  .mx-lg-1 {\n    margin-right: 0.25rem !important;\n    margin-left: 0.25rem !important; }\n  .my-lg-1 {\n    margin-top: 0.25rem !important;\n    margin-bottom: 0.25rem !important; }\n  .m-lg-2 {\n    margin: 0.5rem 0.5rem !important; }\n  .mt-lg-2 {\n    margin-top: 0.5rem !important; }\n  .mr-lg-2 {\n    margin-right: 0.5rem !important; }\n  .mb-lg-2 {\n    margin-bottom: 0.5rem !important; }\n  .ml-lg-2 {\n    margin-left: 0.5rem !important; }\n  .mx-lg-2 {\n    margin-right: 0.5rem !important;\n    margin-left: 0.5rem !important; }\n  .my-lg-2 {\n    margin-top: 0.5rem !important;\n    margin-bottom: 0.5rem !important; }\n  .m-lg-3 {\n    margin: 1rem 1rem !important; }\n  .mt-lg-3 {\n    margin-top: 1rem !important; }\n  .mr-lg-3 {\n    margin-right: 1rem !important; }\n  .mb-lg-3 {\n    margin-bottom: 1rem !important; }\n  .ml-lg-3 {\n    margin-left: 1rem !important; }\n  .mx-lg-3 {\n    margin-right: 1rem !important;\n    margin-left: 1rem !important; }\n  .my-lg-3 {\n    margin-top: 1rem !important;\n    margin-bottom: 1rem !important; }\n  .m-lg-4 {\n    margin: 1.5rem 1.5rem !important; }\n  .mt-lg-4 {\n    margin-top: 1.5rem !important; }\n  .mr-lg-4 {\n    margin-right: 1.5rem !important; }\n  .mb-lg-4 {\n    margin-bottom: 1.5rem !important; }\n  .ml-lg-4 {\n    margin-left: 1.5rem !important; }\n  .mx-lg-4 {\n    margin-right: 1.5rem !important;\n    margin-left: 1.5rem !important; }\n  .my-lg-4 {\n    margin-top: 1.5rem !important;\n    margin-bottom: 1.5rem !important; }\n  .m-lg-5 {\n    margin: 3rem 3rem !important; }\n  .mt-lg-5 {\n    margin-top: 3rem !important; }\n  .mr-lg-5 {\n    margin-right: 3rem !important; }\n  .mb-lg-5 {\n    margin-bottom: 3rem !important; }\n  .ml-lg-5 {\n    margin-left: 3rem !important; }\n  .mx-lg-5 {\n    margin-right: 3rem !important;\n    margin-left: 3rem !important; }\n  .my-lg-5 {\n    margin-top: 3rem !important;\n    margin-bottom: 3rem !important; }\n  .p-lg-0 {\n    padding: 0 0 !important; }\n  .pt-lg-0 {\n    padding-top: 0 !important; }\n  .pr-lg-0 {\n    padding-right: 0 !important; }\n  .pb-lg-0 {\n    padding-bottom: 0 !important; }\n  .pl-lg-0 {\n    padding-left: 0 !important; }\n  .px-lg-0 {\n    padding-right: 0 !important;\n    padding-left: 0 !important; }\n  .py-lg-0 {\n    padding-top: 0 !important;\n    padding-bottom: 0 !important; }\n  .p-lg-1 {\n    padding: 0.25rem 0.25rem !important; }\n  .pt-lg-1 {\n    padding-top: 0.25rem !important; }\n  .pr-lg-1 {\n    padding-right: 0.25rem !important; }\n  .pb-lg-1 {\n    padding-bottom: 0.25rem !important; }\n  .pl-lg-1 {\n    padding-left: 0.25rem !important; }\n  .px-lg-1 {\n    padding-right: 0.25rem !important;\n    padding-left: 0.25rem !important; }\n  .py-lg-1 {\n    padding-top: 0.25rem !important;\n    padding-bottom: 0.25rem !important; }\n  .p-lg-2 {\n    padding: 0.5rem 0.5rem !important; }\n  .pt-lg-2 {\n    padding-top: 0.5rem !important; }\n  .pr-lg-2 {\n    padding-right: 0.5rem !important; }\n  .pb-lg-2 {\n    padding-bottom: 0.5rem !important; }\n  .pl-lg-2 {\n    padding-left: 0.5rem !important; }\n  .px-lg-2 {\n    padding-right: 0.5rem !important;\n    padding-left: 0.5rem !important; }\n  .py-lg-2 {\n    padding-top: 0.5rem !important;\n    padding-bottom: 0.5rem !important; }\n  .p-lg-3 {\n    padding: 1rem 1rem !important; }\n  .pt-lg-3 {\n    padding-top: 1rem !important; }\n  .pr-lg-3 {\n    padding-right: 1rem !important; }\n  .pb-lg-3 {\n    padding-bottom: 1rem !important; }\n  .pl-lg-3 {\n    padding-left: 1rem !important; }\n  .px-lg-3 {\n    padding-right: 1rem !important;\n    padding-left: 1rem !important; }\n  .py-lg-3 {\n    padding-top: 1rem !important;\n    padding-bottom: 1rem !important; }\n  .p-lg-4 {\n    padding: 1.5rem 1.5rem !important; }\n  .pt-lg-4 {\n    padding-top: 1.5rem !important; }\n  .pr-lg-4 {\n    padding-right: 1.5rem !important; }\n  .pb-lg-4 {\n    padding-bottom: 1.5rem !important; }\n  .pl-lg-4 {\n    padding-left: 1.5rem !important; }\n  .px-lg-4 {\n    padding-right: 1.5rem !important;\n    padding-left: 1.5rem !important; }\n  .py-lg-4 {\n    padding-top: 1.5rem !important;\n    padding-bottom: 1.5rem !important; }\n  .p-lg-5 {\n    padding: 3rem 3rem !important; }\n  .pt-lg-5 {\n    padding-top: 3rem !important; }\n  .pr-lg-5 {\n    padding-right: 3rem !important; }\n  .pb-lg-5 {\n    padding-bottom: 3rem !important; }\n  .pl-lg-5 {\n    padding-left: 3rem !important; }\n  .px-lg-5 {\n    padding-right: 3rem !important;\n    padding-left: 3rem !important; }\n  .py-lg-5 {\n    padding-top: 3rem !important;\n    padding-bottom: 3rem !important; }\n  .m-lg-auto {\n    margin: auto !important; }\n  .mt-lg-auto {\n    margin-top: auto !important; }\n  .mr-lg-auto {\n    margin-right: auto !important; }\n  .mb-lg-auto {\n    margin-bottom: auto !important; }\n  .ml-lg-auto {\n    margin-left: auto !important; }\n  .mx-lg-auto {\n    margin-right: auto !important;\n    margin-left: auto !important; }\n  .my-lg-auto {\n    margin-top: auto !important;\n    margin-bottom: auto !important; } }\n\n@media (min-width: 1340px) {\n  .m-xl-0 {\n    margin: 0 0 !important; }\n  .mt-xl-0 {\n    margin-top: 0 !important; }\n  .mr-xl-0 {\n    margin-right: 0 !important; }\n  .mb-xl-0 {\n    margin-bottom: 0 !important; }\n  .ml-xl-0 {\n    margin-left: 0 !important; }\n  .mx-xl-0 {\n    margin-right: 0 !important;\n    margin-left: 0 !important; }\n  .my-xl-0 {\n    margin-top: 0 !important;\n    margin-bottom: 0 !important; }\n  .m-xl-1 {\n    margin: 0.25rem 0.25rem !important; }\n  .mt-xl-1 {\n    margin-top: 0.25rem !important; }\n  .mr-xl-1 {\n    margin-right: 0.25rem !important; }\n  .mb-xl-1 {\n    margin-bottom: 0.25rem !important; }\n  .ml-xl-1 {\n    margin-left: 0.25rem !important; }\n  .mx-xl-1 {\n    margin-right: 0.25rem !important;\n    margin-left: 0.25rem !important; }\n  .my-xl-1 {\n    margin-top: 0.25rem !important;\n    margin-bottom: 0.25rem !important; }\n  .m-xl-2 {\n    margin: 0.5rem 0.5rem !important; }\n  .mt-xl-2 {\n    margin-top: 0.5rem !important; }\n  .mr-xl-2 {\n    margin-right: 0.5rem !important; }\n  .mb-xl-2 {\n    margin-bottom: 0.5rem !important; }\n  .ml-xl-2 {\n    margin-left: 0.5rem !important; }\n  .mx-xl-2 {\n    margin-right: 0.5rem !important;\n    margin-left: 0.5rem !important; }\n  .my-xl-2 {\n    margin-top: 0.5rem !important;\n    margin-bottom: 0.5rem !important; }\n  .m-xl-3 {\n    margin: 1rem 1rem !important; }\n  .mt-xl-3 {\n    margin-top: 1rem !important; }\n  .mr-xl-3 {\n    margin-right: 1rem !important; }\n  .mb-xl-3 {\n    margin-bottom: 1rem !important; }\n  .ml-xl-3 {\n    margin-left: 1rem !important; }\n  .mx-xl-3 {\n    margin-right: 1rem !important;\n    margin-left: 1rem !important; }\n  .my-xl-3 {\n    margin-top: 1rem !important;\n    margin-bottom: 1rem !important; }\n  .m-xl-4 {\n    margin: 1.5rem 1.5rem !important; }\n  .mt-xl-4 {\n    margin-top: 1.5rem !important; }\n  .mr-xl-4 {\n    margin-right: 1.5rem !important; }\n  .mb-xl-4 {\n    margin-bottom: 1.5rem !important; }\n  .ml-xl-4 {\n    margin-left: 1.5rem !important; }\n  .mx-xl-4 {\n    margin-right: 1.5rem !important;\n    margin-left: 1.5rem !important; }\n  .my-xl-4 {\n    margin-top: 1.5rem !important;\n    margin-bottom: 1.5rem !important; }\n  .m-xl-5 {\n    margin: 3rem 3rem !important; }\n  .mt-xl-5 {\n    margin-top: 3rem !important; }\n  .mr-xl-5 {\n    margin-right: 3rem !important; }\n  .mb-xl-5 {\n    margin-bottom: 3rem !important; }\n  .ml-xl-5 {\n    margin-left: 3rem !important; }\n  .mx-xl-5 {\n    margin-right: 3rem !important;\n    margin-left: 3rem !important; }\n  .my-xl-5 {\n    margin-top: 3rem !important;\n    margin-bottom: 3rem !important; }\n  .p-xl-0 {\n    padding: 0 0 !important; }\n  .pt-xl-0 {\n    padding-top: 0 !important; }\n  .pr-xl-0 {\n    padding-right: 0 !important; }\n  .pb-xl-0 {\n    padding-bottom: 0 !important; }\n  .pl-xl-0 {\n    padding-left: 0 !important; }\n  .px-xl-0 {\n    padding-right: 0 !important;\n    padding-left: 0 !important; }\n  .py-xl-0 {\n    padding-top: 0 !important;\n    padding-bottom: 0 !important; }\n  .p-xl-1 {\n    padding: 0.25rem 0.25rem !important; }\n  .pt-xl-1 {\n    padding-top: 0.25rem !important; }\n  .pr-xl-1 {\n    padding-right: 0.25rem !important; }\n  .pb-xl-1 {\n    padding-bottom: 0.25rem !important; }\n  .pl-xl-1 {\n    padding-left: 0.25rem !important; }\n  .px-xl-1 {\n    padding-right: 0.25rem !important;\n    padding-left: 0.25rem !important; }\n  .py-xl-1 {\n    padding-top: 0.25rem !important;\n    padding-bottom: 0.25rem !important; }\n  .p-xl-2 {\n    padding: 0.5rem 0.5rem !important; }\n  .pt-xl-2 {\n    padding-top: 0.5rem !important; }\n  .pr-xl-2 {\n    padding-right: 0.5rem !important; }\n  .pb-xl-2 {\n    padding-bottom: 0.5rem !important; }\n  .pl-xl-2 {\n    padding-left: 0.5rem !important; }\n  .px-xl-2 {\n    padding-right: 0.5rem !important;\n    padding-left: 0.5rem !important; }\n  .py-xl-2 {\n    padding-top: 0.5rem !important;\n    padding-bottom: 0.5rem !important; }\n  .p-xl-3 {\n    padding: 1rem 1rem !important; }\n  .pt-xl-3 {\n    padding-top: 1rem !important; }\n  .pr-xl-3 {\n    padding-right: 1rem !important; }\n  .pb-xl-3 {\n    padding-bottom: 1rem !important; }\n  .pl-xl-3 {\n    padding-left: 1rem !important; }\n  .px-xl-3 {\n    padding-right: 1rem !important;\n    padding-left: 1rem !important; }\n  .py-xl-3 {\n    padding-top: 1rem !important;\n    padding-bottom: 1rem !important; }\n  .p-xl-4 {\n    padding: 1.5rem 1.5rem !important; }\n  .pt-xl-4 {\n    padding-top: 1.5rem !important; }\n  .pr-xl-4 {\n    padding-right: 1.5rem !important; }\n  .pb-xl-4 {\n    padding-bottom: 1.5rem !important; }\n  .pl-xl-4 {\n    padding-left: 1.5rem !important; }\n  .px-xl-4 {\n    padding-right: 1.5rem !important;\n    padding-left: 1.5rem !important; }\n  .py-xl-4 {\n    padding-top: 1.5rem !important;\n    padding-bottom: 1.5rem !important; }\n  .p-xl-5 {\n    padding: 3rem 3rem !important; }\n  .pt-xl-5 {\n    padding-top: 3rem !important; }\n  .pr-xl-5 {\n    padding-right: 3rem !important; }\n  .pb-xl-5 {\n    padding-bottom: 3rem !important; }\n  .pl-xl-5 {\n    padding-left: 3rem !important; }\n  .px-xl-5 {\n    padding-right: 3rem !important;\n    padding-left: 3rem !important; }\n  .py-xl-5 {\n    padding-top: 3rem !important;\n    padding-bottom: 3rem !important; }\n  .m-xl-auto {\n    margin: auto !important; }\n  .mt-xl-auto {\n    margin-top: auto !important; }\n  .mr-xl-auto {\n    margin-right: auto !important; }\n  .mb-xl-auto {\n    margin-bottom: auto !important; }\n  .ml-xl-auto {\n    margin-left: auto !important; }\n  .mx-xl-auto {\n    margin-right: auto !important;\n    margin-left: auto !important; }\n  .my-xl-auto {\n    margin-top: auto !important;\n    margin-bottom: auto !important; } }\n\n.text-justify {\n  text-align: justify !important; }\n\n.text-nowrap {\n  white-space: nowrap !important; }\n\n.text-truncate {\n  overflow: hidden;\n  text-overflow: ellipsis;\n  white-space: nowrap; }\n\n.text-left {\n  text-align: left !important; }\n\n.text-right {\n  text-align: right !important; }\n\n.text-center {\n  text-align: center !important; }\n\n@media (min-width: 576px) {\n  .text-sm-left {\n    text-align: left !important; }\n  .text-sm-right {\n    text-align: right !important; }\n  .text-sm-center {\n    text-align: center !important; } }\n\n@media (min-width: 768px) {\n  .text-md-left {\n    text-align: left !important; }\n  .text-md-right {\n    text-align: right !important; }\n  .text-md-center {\n    text-align: center !important; } }\n\n@media (min-width: 992px) {\n  .text-lg-left {\n    text-align: left !important; }\n  .text-lg-right {\n    text-align: right !important; }\n  .text-lg-center {\n    text-align: center !important; } }\n\n@media (min-width: 1340px) {\n  .text-xl-left {\n    text-align: left !important; }\n  .text-xl-right {\n    text-align: right !important; }\n  .text-xl-center {\n    text-align: center !important; } }\n\n.text-lowercase {\n  text-transform: lowercase !important; }\n\n.text-uppercase {\n  text-transform: uppercase !important; }\n\n.text-capitalize {\n  text-transform: capitalize !important; }\n\n.font-weight-normal {\n  font-weight: normal; }\n\n.font-weight-bold {\n  font-weight: bold; }\n\n.font-italic {\n  font-style: italic; }\n\n.text-white {\n  color: #fff !important; }\n\n.text-muted {\n  color: #999 !important; }\n\na.text-muted:focus, a.text-muted:hover {\n  color: gray !important; }\n\n.text-primary {\n  color: #004B8D !important; }\n\na.text-primary:focus, a.text-primary:hover {\n  color: #00305a !important; }\n\n.text-success {\n  color: #5cb85c !important; }\n\na.text-success:focus, a.text-success:hover {\n  color: #449d44 !important; }\n\n.text-info {\n  color: #2196F3 !important; }\n\na.text-info:focus, a.text-info:hover {\n  color: #0c7cd5 !important; }\n\n.text-warning {\n  color: #f0ad4e !important; }\n\na.text-warning:focus, a.text-warning:hover {\n  color: #ec971f !important; }\n\n.text-danger {\n  color: #d50000 !important; }\n\na.text-danger:focus, a.text-danger:hover {\n  color: #a20000 !important; }\n\n.text-gray-dark {\n  color: #292b2c !important; }\n\na.text-gray-dark:focus, a.text-gray-dark:hover {\n  color: #101112 !important; }\n\n.text-hide {\n  font: 0/0 a;\n  color: transparent;\n  text-shadow: none;\n  background-color: transparent;\n  border: 0; }\n\n.invisible {\n  visibility: hidden !important; }\n\n.hidden-xs-up {\n  display: none !important; }\n\n@media (max-width: 575px) {\n  .hidden-xs-down {\n    display: none !important; } }\n\n@media (min-width: 576px) {\n  .hidden-sm-up {\n    display: none !important; } }\n\n@media (max-width: 767px) {\n  .hidden-sm-down {\n    display: none !important; } }\n\n@media (min-width: 768px) {\n  .hidden-md-up {\n    display: none !important; } }\n\n@media (max-width: 991px) {\n  .hidden-md-down {\n    display: none !important; } }\n\n@media (min-width: 992px) {\n  .hidden-lg-up {\n    display: none !important; } }\n\n@media (max-width: 1339px) {\n  .hidden-lg-down {\n    display: none !important; } }\n\n@media (min-width: 1340px) {\n  .hidden-xl-up {\n    display: none !important; } }\n\n.hidden-xl-down {\n  display: none !important; }\n\n.visible-print-block {\n  display: none !important; }\n  @media print {\n    .visible-print-block {\n      display: block !important; } }\n\n.visible-print-inline {\n  display: none !important; }\n  @media print {\n    .visible-print-inline {\n      display: inline !important; } }\n\n.visible-print-inline-block {\n  display: none !important; }\n  @media print {\n    .visible-print-inline-block {\n      display: inline-block !important; } }\n\n@media print {\n  .hidden-print {\n    display: none !important; } }\n", ""]);

// exports


/***/ }),
/* 7 */
/***/ (function(module, exports) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/
// css base code, injected by the css-loader
module.exports = function() {
	var list = [];

	// return the list of modules as css string
	list.toString = function toString() {
		var result = [];
		for(var i = 0; i < this.length; i++) {
			var item = this[i];
			if(item[2]) {
				result.push("@media " + item[2] + "{" + item[1] + "}");
			} else {
				result.push(item[1]);
			}
		}
		return result.join("");
	};

	// import a list of modules into the list
	list.i = function(modules, mediaQuery) {
		if(typeof modules === "string")
			modules = [[null, modules, ""]];
		var alreadyImportedModules = {};
		for(var i = 0; i < this.length; i++) {
			var id = this[i][0];
			if(typeof id === "number")
				alreadyImportedModules[id] = true;
		}
		for(i = 0; i < modules.length; i++) {
			var item = modules[i];
			// skip already imported module
			// this implementation is not 100% perfect for weird media query combinations
			//  when a module is imported multiple times with different media queries.
			//  I hope this will never occur (Hey this way we have smaller bundles)
			if(typeof item[0] !== "number" || !alreadyImportedModules[item[0]]) {
				if(mediaQuery && !item[2]) {
					item[2] = mediaQuery;
				} else if(mediaQuery) {
					item[2] = "(" + item[2] + ") and (" + mediaQuery + ")";
				}
				list.push(item);
			}
		}
	};
	return list;
};


/***/ }),
/* 8 */
/***/ (function(module, exports) {

var DOCUMENT_NODE_TYPE = 9;

/**
 * A polyfill for Element.matches()
 */
if (typeof Element !== 'undefined' && !Element.prototype.matches) {
    var proto = Element.prototype;

    proto.matches = proto.matchesSelector ||
                    proto.mozMatchesSelector ||
                    proto.msMatchesSelector ||
                    proto.oMatchesSelector ||
                    proto.webkitMatchesSelector;
}

/**
 * Finds the closest parent that matches a selector.
 *
 * @param {Element} element
 * @param {String} selector
 * @return {Function}
 */
function closest (element, selector) {
    while (element && element.nodeType !== DOCUMENT_NODE_TYPE) {
        if (element.matches(selector)) return element;
        element = element.parentNode;
    }
}

module.exports = closest;


/***/ }),
/* 9 */
/***/ (function(module, exports, __webpack_require__) {

var closest = __webpack_require__(8);

/**
 * Delegates event to a selector.
 *
 * @param {Element} element
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @param {Boolean} useCapture
 * @return {Object}
 */
function delegate(element, selector, type, callback, useCapture) {
    var listenerFn = listener.apply(this, arguments);

    element.addEventListener(type, listenerFn, useCapture);

    return {
        destroy: function() {
            element.removeEventListener(type, listenerFn, useCapture);
        }
    }
}

/**
 * Finds closest match and invokes callback.
 *
 * @param {Element} element
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @return {Function}
 */
function listener(element, selector, type, callback) {
    return function(e) {
        e.delegateTarget = closest(e.target, selector);

        if (e.delegateTarget) {
            callback.call(element, e);
        }
    }
}

module.exports = delegate;


/***/ }),
/* 10 */
/***/ (function(module, exports) {

/**
 * Check if argument is a HTML element.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.node = function(value) {
    return value !== undefined
        && value instanceof HTMLElement
        && value.nodeType === 1;
};

/**
 * Check if argument is a list of HTML elements.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.nodeList = function(value) {
    var type = Object.prototype.toString.call(value);

    return value !== undefined
        && (type === '[object NodeList]' || type === '[object HTMLCollection]')
        && ('length' in value)
        && (value.length === 0 || exports.node(value[0]));
};

/**
 * Check if argument is a string.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.string = function(value) {
    return typeof value === 'string'
        || value instanceof String;
};

/**
 * Check if argument is a function.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.fn = function(value) {
    var type = Object.prototype.toString.call(value);

    return type === '[object Function]';
};


/***/ }),
/* 11 */
/***/ (function(module, exports, __webpack_require__) {

var is = __webpack_require__(10);
var delegate = __webpack_require__(9);

/**
 * Validates all params and calls the right
 * listener function based on its target type.
 *
 * @param {String|HTMLElement|HTMLCollection|NodeList} target
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listen(target, type, callback) {
    if (!target && !type && !callback) {
        throw new Error('Missing required arguments');
    }

    if (!is.string(type)) {
        throw new TypeError('Second argument must be a String');
    }

    if (!is.fn(callback)) {
        throw new TypeError('Third argument must be a Function');
    }

    if (is.node(target)) {
        return listenNode(target, type, callback);
    }
    else if (is.nodeList(target)) {
        return listenNodeList(target, type, callback);
    }
    else if (is.string(target)) {
        return listenSelector(target, type, callback);
    }
    else {
        throw new TypeError('First argument must be a String, HTMLElement, HTMLCollection, or NodeList');
    }
}

/**
 * Adds an event listener to a HTML element
 * and returns a remove listener function.
 *
 * @param {HTMLElement} node
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenNode(node, type, callback) {
    node.addEventListener(type, callback);

    return {
        destroy: function() {
            node.removeEventListener(type, callback);
        }
    }
}

/**
 * Add an event listener to a list of HTML elements
 * and returns a remove listener function.
 *
 * @param {NodeList|HTMLCollection} nodeList
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenNodeList(nodeList, type, callback) {
    Array.prototype.forEach.call(nodeList, function(node) {
        node.addEventListener(type, callback);
    });

    return {
        destroy: function() {
            Array.prototype.forEach.call(nodeList, function(node) {
                node.removeEventListener(type, callback);
            });
        }
    }
}

/**
 * Add an event listener to a selector
 * and returns a remove listener function.
 *
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenSelector(selector, type, callback) {
    return delegate(document.body, selector, type, callback);
}

module.exports = listen;


/***/ }),
/* 12 */
/***/ (function(module, exports) {

module.exports = "<html>\n<head>\n<title>yanagishima</title>\n<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"favicon.ico\">\n<link href=\"https://fonts.googleapis.com/css?family=Droid+Sans\" rel=\"stylesheet\">\n<link href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\" rel=\"stylesheet\">\n<link href=\"https://cdnjs.cloudflare.com/ajax/libs/magnific-popup.js/1.1.0/magnific-popup.css\" rel=\"stylesheet\">\n<style>.unload {display: none;}</style>\n</head>\n<body>\n<div id=\"page\" class=\"unload\" :class=\"'datasource_' + datasources.indexOf(datasource)\">\n<header id=\"header\" :class=\"{'sticky-top': maxline !== Infinity}\">\n\t<div id=\"header-main\">\n\t\t<div class=\"container\">\n\t\t\t<div id=\"header-upper\" class=\"py-2\">\n\t\t\t\t<div class=\"row align-items-center\">\n\t\t\t\t\t<div class=\"col-6\">\n\t\t\t\t\t\t<h1 id=\"logo\"><a :href=\"'#datasource={0}'.format(datasource)\" @click.prevent=\"queryid = '', tab = tabs[0].id, init()\"><span id=\"logo-figure\" class=\"mr-2\"></span>{{sitename}}</a><small class=\"ml-2\">Ver {{version}}</small></h1>\n\t\t\t\t\t<!-- /col --></div>\n\t\t\t\t\t<div class=\"col-6 text-right\">\n\t\t\t\t\t\t<div id=\"setting\" class=\"d-inline-block\">\n\t\t\t\t\t\t\t<div class=\"d-inline-block ml-2\">\n\t\t\t\t\t\t\t\t<small class=\"mr-1\">Screen Size</small>\n\t\t\t\t\t\t\t\t<div class=\"btn-group\">\n\t\t\t\t\t\t\t\t\t<a class=\"btn btn-sm\" href=\"#\" v-for=\"item in layouts\" @click.prevent=\"layout = item.class\" :class=\"{'btn-primary': layout != item.class, 'btn-secondary': layout == item.class}\">{{item.name}}</a>\n\t\t\t\t\t\t\t\t<!-- /btn-group --></div>\n\t\t\t\t\t\t\t</div>\n\t\t\t\t\t\t\t<div class=\"d-inline-block ml-2\">\n\t\t\t\t\t\t\t\t<small class=\"mr-1\">Editor Maxline</small>\n\t\t\t\t\t\t\t\t<div class=\"btn-group\">\n\t\t\t\t\t\t\t\t\t<a class=\"btn btn-sm\" href=\"#\" v-for=\"item in maxlines\" @click.prevent=\"maxline = item\" :class=\"{'btn-primary': item != maxline, 'btn-secondary': item == maxline}\"><template v-if=\"item != Infinity\">{{item}}</template><template v-else>&infin;</template></a>\n\t\t\t\t\t\t\t\t<!-- /btn-group --></div>\n\t\t\t\t\t\t\t</div>\n\t\t\t\t\t\t\t<div class=\"d-inline-block ml-2\">\n\t\t\t\t\t\t\t\t<small class=\"mr-1\">Editor Thema</small>\n\t\t\t\t\t\t\t\t<button class=\"btn btn-sm btn-primary\" @click=\"$('#theme').modal('show')\">{{theme | humanize}}</button>\n\t\t\t\t\t\t\t</div>\n\t\t\t\t\t\t<!-- /setting --></div>\n\t\t\t\t\t<!-- /col --></div>\n\t\t\t\t<!-- /row --></div>\n\t\t\t<!-- /header-upper --></div>\n\t\t\t<div id=\"header-loower\">\n\t\t\t\t<div class=\"editor mb-2\">\n\t\t\t\t\t<ace :code=\"input_query\" :gotoline=\"gotoline\" :focus=\"focus\" :maxline=\"maxline\" :theme=\"theme\" @change-code=\"changeQuery\" @run-code=\"runQuery\"></ace>\n\t\t\t\t</div>\n\t\t\t\t<div class=\"row align-items-end\">\n\t\t\t\t\t<div class=\"col\">\n\t\t\t\t\t\t<ul class=\"nav nav-tabs\">\n\t\t\t\t\t\t\t<li class=\"nav-item\" v-for=\"item in tabs\">\n\t\t\t\t\t\t\t\t<a class=\"nav-link\" href=\"#\" @click.prevent=\"tab = item.id\" :class=\"{active: tab === item.id}\">\n\t\t\t\t\t\t\t\t\t<i class=\"fa fa-fw mr-1\" :class=\"'fa-' + item.icon\"></i>{{item.name}}\n\t\t\t\t\t\t\t\t</a>\n\t\t\t\t\t\t\t</li>\n\t\t\t\t\t\t</ul>\n\t\t\t\t\t<!-- /col --></div>\n\t\t\t\t\t<div class=\"col text-right pb-2\">\n\t\t\t\t\t\t<div id=\"control\" class=\"d-inline-block\">\n\t\t\t\t\t\t\t<button type=\"button\" class=\"btn btn-primary ml-1\" @click=\"$('#treeview').modal('show')\"><i class=\"fa fa-fw fa-sitemap mr-1\"></i><strong>{{datasource}}</strong><small class=\"ml-1\">(Ctrl+T)</small></button>\n\t\t\t\t\t\t\t<button class=\"btn btn-primary ml-1\" :class=\"{disabled: !bookmarks.length}\" @click=\"bookmarks.length && $('#bookmark').modal('show')\"><i class=\"fa fa-fw fa-star mr-1\"></i>Bookmark<small class=\"ml-2\" v-if=\"bookmarks.length\">{{bookmarks.length}}</small></button>\n\t\t\t\t\t\t\t<div class=\"btn-group ml-3\">\n\t\t\t\t\t\t\t\t<button type=\"button\" class=\"btn btn-primary\" @click=\"runQuery(input_query)\"><i class=\"fa fa-fw fa-play mr-1\"></i><strong>Run</strong><small class=\"ml-1\">(Ctrl+Enter)</small></button>\n\t\t\t\t\t\t\t\t<div class=\"btn-group\">\n\t\t\t\t\t\t\t\t\t<button type=\"button\" class=\"btn btn-primary\" data-toggle=\"dropdown\"><i class=\"fa fa-ellipsis-v\"></i></button>\n\t\t\t\t\t\t\t\t\t<div class=\"dropdown-menu dropdown-menu-right\">\n\t\t\t\t\t\t\t\t\t\t<a class=\"dropdown-item\" href=\"#\" v-for=\"(val, key) in explains\" @click.prevent=\"runQuery(val.format(input_query))\">{{key | humanize}}</a>\n\t\t\t\t\t\t\t\t\t<!-- /dropdown-menu --></div>\n\t\t\t\t\t\t\t\t<!-- /btn-group --></div>\n\t\t\t\t\t\t\t<!-- /btn-group --></div>\n\t\t\t\t\t\t<!-- /control --></div>\n\t\t\t\t\t<!-- /col --></div>\n\t\t\t\t<!-- /row --></div>\n\t\t\t<!-- /header-lower --></div>\n\t\t<!-- /container --></div>\n\t<!-- /header-main --></div>\n\t<div id=\"header-sub\">\n\t\t<div :class=\"layout\">\n\t\t\t<div class=\"row align-items-center pt-3\">\n\t\t\t\t<div class=\"col\">\n\t\t\t\t\t<template v-if=\"tab === 'qlist'\">\n\t\t\t\t\t\t<span>As of<strong class=\"ml-2\">{{now}}</strong></span>\n\t\t\t\t\t</template>\n\t\t\t\t\t<template v-if=\"tab === 'history'\">\n\t\t\t\t\t\t<span v-if=\"histories.length\"><strong class=\"mr-1\">Excuted History</strong> {{histories.length}}</span>\n\t\t\t\t\t\t<span v-else><strong>No history</strong></span>\n\t\t\t\t\t</template>\n\t\t\t\t\t<template v-if=\"tab === 'result'\">\n\t\t\t\t\t\t<template v-if=\"loading.result\">\n\t\t\t\t\t\t\t<strong>Now Loading</strong> \n\t\t\t\t\t\t</template>\n\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t<template v-if=\"responses.normal.error || error.result\">\n\t\t\t\t\t\t\t\t<strong><i class=\"fa fa-exclamation-triangle text-danger mr-1\"></i>Error</strong>\n\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t<span v-if=\"responses.normal.results\">\n\t\t\t\t\t\t\t\t\t<span v-if=\"responses.normal.lineNumber\">\n\t\t\t\t\t\t\t\t\t\t<span class=\"mr-3\">\n\t\t\t\t\t\t\t\t\t\t\t<i class=\"fa fa-fw fa-file-text-o\" title=\"ID\" data-toggle=\"tooltip\" data-placement=\"left\"></i>\n\t\t\t\t\t\t\t\t\t\t\t<strong>{{shortID(queryid)}}</strong>\n\t\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t\t\t<span class=\"mr-3\">\n\t\t\t\t\t\t\t\t\t\t\t{{queryid | extractDate}}\n\t\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t\t\t<strong>{{result.results.length | formatNumber}}</strong><template v-if=\"responses.normal.results.length != result.lineNumber -1\"><span class=\"mx-1\">/</span><strong>{{result.lineNumber -1 | formatNumber}}</strong></template><span class=\"text-muted ml-1\">results</span>\n\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t\t<span class=\"ml-2\" v-if=\"responses.normal.elapsedTimeMillis\">\n\t\t\t\t\t\t\t\t\t\t<strong>{{(result.elapsedTimeMillis / 1000).ceil(2)}}</strong><span class=\"text-muted ml-1\">sec</span>\n\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t\t<span class=\"ml-2\" v-if=\"responses.normal.rawDataSize\">\n\t\t\t\t\t\t\t\t\t\t<strong>{{responses.normal.rawDataSize.remove('B')}}</strong><span class=\"text-muted ml-1\">byte</span>\n\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t<span v-else><strong>No result</strong></span>\n\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t</template>\n\t\t\t\t\t</template>\n\t\t\t\t<!-- /col --></div>\n\t\t\t\t<div class=\"col text-right\">\n\t\t\t\t\t<template v-if=\"tab === 'qlist'\">\n\t\t\t\t\t\t<label class=\"form-check-label mr-2\"><input class=\"form-check-input mr-1\" type=\"checkbox\" v-model=\"is_autoQlist\">Auto</label>\n\t\t\t\t\t\t<button class=\"btn btn-sm btn-secondary\" @click=\"getQlist()\"><i class=\"fa fa-fw fa-refresh mr-1\"></i>Refresh</button>\n\t\t\t\t\t</template>\n\t\t\t\t\t<template v-if=\"tab === 'history'\">\n\t\t\t\t\t\t<button class=\"btn btn-sm btn-secondary\" :class=\"{disabled: !histories.length}\" @click=\"delHistoryAll()\"><i class=\"fa fa-fw fa-trash mr-1\"></i>Delete All History</button>\n\t\t\t\t\t</template>\n\t\t\t\t\t<template v-if=\"tab === 'result'\">\n\t\t\t\t\t\t<div class=\"btn-group mr-2\" data-toggle=\"tooltip\">\n\t\t\t\t\t\t\t<template  v-if=\"bookmarks.includes(queryid)\">\n\t\t\t\t\t\t\t\t<a href=\"#\" class=\"btn btn-sm btn-secondary\" @click.prevent=\"delBookmarkItem(queryid)\" :class=\"{disabled: result.error || !queryid || loading.result}\"><i class=\"fa fa-fw fa-star mr-1\"></i>Added</a>\n\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t<a href=\"#\" class=\"btn btn-sm btn-secondary\" @click.prevent=\"addBookmarkItem(queryid)\" :class=\"{disabled: result.error || !queryid || loading.result}\"><i class=\"fa fa-fw fa-star-o mr-1\"></i>Add</a>\n\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t<a href=\"#\" class=\"btn btn-sm btn-secondary\" @click.prevent=\"input_query = query, focus++\" v-if=\"!result.error && !loading.result\" :class=\"{disabled: input_query == query}\"><i class=\"fa fa-fw fa-pencil mr-1\"></i>Set</a>\n\t\t\t\t\t\t<!-- /btn-group --></div>\n\t\t\t\t\t\t<div class=\"btn-group\" data-toggle=\"tooltip\">\n\t\t\t\t\t\t\t<a :href=\"download(queryid)\" class=\"btn btn-sm btn-secondary\" :class=\"{disabled: result.error || !queryid || loading.result}\"><i class=\"fa fa-fw fa-download mr-1\"></i>TSV</a>\n\t\t\t\t\t\t\t<a :href=\"download(queryid, 'csv')\" class=\"btn btn-sm btn-secondary\" :class=\"{disabled: result.error || !queryid || loading.result}\"><i class=\"fa fa-fw fa-download mr-1\"></i>CSV</a>\n\t\t\t\t\t\t<!-- /btn-group --></div>\n\t\t\t\t\t</template>\n\t\t\t\t<!-- /col --></div>\n\t\t\t<!-- /row --></div>\n\t\t<!-- /container --></div>\n\t<!-- /header-sub --></div>\n</header>\n<div id=\"content\">\n\t<div :class=\"layout\">\n\t\t<template v-if=\"tab === 'qlist'\">\n\t\t\t<template v-if=\"loading.qlist\">\n\t\t\t\t<div class=\"alert alert-info\" role=\"alert\">\n\t\t\t\t\t<div class=\"row align-items-center\">\n\t\t\t\t\t\t<div class=\"col\">\n\t\t\t\t\t\t\t<i class=\"fa fa-fw fa-spinner fa-pulse mr-1\"></i>Loading\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</template>\n\t\t\t<template v-else>\n\t\t\t\t<div class=\"alert alert-info\" v-if=\"filter_user !== ''\">\n\t\t\t\t\t<button class=\"close\" @click=\"filter_user = ''\"><span>&times;</span></button>\n\t\t\t\t\t<span class=\"mr-3\">Filter by <strong>{{filter_user}}</strong></span>\n\t\t\t\t\t<span class=\"mr-2\"><strong>{{orderdQlist.length}}</strong> Results</span>\n\t\t\t\t\t<span class=\"mr-2\" v-if=\"orderdFailQlist.length\"><strong>{{orderdFailQlist.length}}</strong> Fails</span>\n\t\t\t\t</div>\n\t\t\t\t<table class=\"table table-bordered table-fixed table-hover\">\n\t\t\t\t\t<thead>\n\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t<th width=\"7%\">State</th>\n\t\t\t\t\t\t\t<th width=\"5%\" class=\"text-right\">Elapsed</th>\n\t\t\t\t\t\t\t<th width=\"55%\">Query</th>\n\t\t\t\t\t\t\t<th width=\"12.5%\">Source</th>\n\t\t\t\t\t\t\t<th width=\"12.5%\">User</th>\n\t\t\t\t\t\t\t<th width=\"4%\" class=\"text-center\">Kill</th>\n\t\t\t\t\t\t\t<th width=\"4%\" class=\"text-center\">Info</th>\n\t\t\t\t\t\t</tr>\n\t\t\t\t\t</thead>\n\t\t\t\t\t<tbody>\n\t\t\t\t\t\t<tr v-for=\"item in orderdQlist\" :class=\"{'table-danger': item.state === 'FAILED', 'table-warning': isRunning(item.state)}\">\n\t\t\t\t\t\t\t<td>{{item.state}}</td>\n\t\t\t\t\t\t\t<td class=\"text-right\">{{item.queryStats.elapsedTime | formatUnit}}</td>\n\t\t\t\t\t\t\t<td>{{item.query.compact()}}</td>\n\t\t\t\t\t\t\t<td>{{item.session.source}}</td>\n\t\t\t\t\t\t\t<td><a href=\"#\" @click.prevent=\"filter_user = item.session.user\">{{item.session.user}}</a></td>\n\t\t\t\t\t\t\t<td class=\"text-center\"><a href=\"#\" class=\"btn btn-sm btn-secondary p-1\" @click.prevent=\"killQuery(item.queryId)\" v-if=\"isRunning(item.state)\"><i class=\"fa fa-fw fa-times text-danger\"></i></a></td>\n\t\t\t\t\t\t\t<td class=\"text-center\"><a target=\"_blank\" class=\"btn btn-sm btn-secondary p-1 link_detail\" :href=\"linkDetail(item.queryId)\"><i class=\"fa fa-fw fa-info\"></i></a></td>\n\t\t\t\t\t\t</tr>\n\t\t\t\t\t</tbody>\n\t\t\t\t</table>\n\t\t\t</template>\n\t\t</template>\n\t\t<template v-if=\"tab === 'history'\">\n\t\t\t<template v-if=\"loading.history\">\n\t\t\t\t<div class=\"alert alert-info\" role=\"alert\">\n\t\t\t\t\t<div class=\"row align-items-center\">\n\t\t\t\t\t\t<div class=\"col\">\n\t\t\t\t\t\t\t<i class=\"fa fa-fw fa-spinner fa-pulse mr-1\"></i>Loading\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</template>\n\t\t\t<template v-else>\n\t\t\t\t<table class=\"table table-bordered table-fixed table-hover\" v-if=\"historyQlist.length\">\n\t\t\t\t\t<thead>\n\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t<th width=\"5%\" class=\"text-center\">ID</th>\n\t\t\t\t\t\t\t<th width=\"12%\" class=\"text-center\">Excuted Date</th>\n\t\t\t\t\t\t\t<th width=\"6%\" class=\"text-right\">Size</th>\n\t\t\t\t\t\t\t<th width=\"67%\">Query</th>\n\t\t\t\t\t\t\t<th width=\"5%\" class=\"text-center\">Set</th>\n\t\t\t\t\t\t\t<th width=\"5%\" class=\"text-center\">Del</th>\n\t\t\t\t\t\t</tr>\n\t\t\t\t\t</thead>\n\t\t\t\t\t<tbody>\n\t\t\t\t\t\t<tr v-for=\"item in historyQlist\">\n\t\t\t\t\t\t\t<td class=\"text-center\">\n\t\t\t\t\t\t\t\t<a :href=\"'#datasource={0}&amp;tab=result&amp;queryid={1}'.format(datasource, item.queryid)\">{{shortID(item.queryid)}}</a>\n\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t<td class=\"text-center\">\n\t\t\t\t\t\t\t\t{{item.queryid | extractDate}}\n\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t<td class=\"text-right\">\n\t\t\t\t\t\t\t\t{{item.rawDataSize}}\n\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t<td>\n\t\t\t\t\t\t\t\t<ace :code=\"item.query\" readonly=\"true\" css_class=\"bg-transparent\"></ace>\n\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t<td class=\"text-center\">\n\t\t\t\t\t\t\t\t<a href=\"#\" class=\"btn btn-sm btn-secondary\" @click.prevent=\"input_query = item.query, focus++\"><i class=\"fa fa-fw fa-pencil\"></i></a>\n\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t<td class=\"text-center\">\n\t\t\t\t\t\t\t\t<a href=\"#\" class=\"btn btn-sm btn-secondary\" @click.prevent=\"delHistoryItem(item.queryid), historyQlist.remove(item)\"><i class=\"fa fa-fw fa-trash\"></i></a>\n\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t</tr>\n\t\t\t\t\t</tbody>\n\t\t\t\t</table>\n\t\t\t</template>\n\t\t</template>\n\t\t<template v-if=\"tab === 'result'\">\n\t\t\t<template v-if=\"loading.result\">\n\t\t\t\t<div class=\"alert alert-info\" role=\"alert\">\n\t\t\t\t\t<div class=\"row align-items-center\">\n\t\t\t\t\t\t<div class=\"col\">\n\t\t\t\t\t\t\t<i class=\"fa fa-fw fa-spinner fa-pulse mr-1\"></i>Loading\n\t\t\t\t\t\t\tYou can check status by <a href=\"#\" @click.prevent=\"tab = 'qlist', queryid = ''\" class=\"alert-link\">Que List</a>.</strong>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t\t<div class=\"col text-right\">\n\t\t\t\t\t\t\t<button class=\"btn btn-sm btn-secondary\" @click=\"abortQuery()\">Cancel <span class=\"text-muted\">(not Kill)</span></button>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</template>\n\t\t\t<template v-else>\n\t\t\t\t<template v-if=\"error.result\">\n\t\t\t\t\t<div class=\"alert alert-danger\" role=\"alert\">{{error.result}}</div>\n\t\t\t\t</template>\n\t\t\t\t<template v-else>\n\t\t\t\t\t<template v-if=\"responses.normal.error\">\n\t\t\t\t\t\t<div class=\"alert alert-danger\" role=\"alert\">{{result.error}}</div>\n\t\t\t\t\t</template>\n\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t<div class=\"editor p-2 mb-3\" v-if=\"query\">\n\t\t\t\t\t\t\t<ace :code=\"query\" readonly=\"true\" css_class=\"bg-transparent\"></ace>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t\t<template v-if=\"/^explain /i.test(query)\">\n\t\t\t\t\t\t\t<pre id=\"explain\"><code v-html=\"explain\"></code></pre>\n\t\t\t\t\t\t</template>\n\t\t\t\t\t\t<table class=\"table table-auto table-bordered table-hover table-responsive\" v-else>\n\t\t\t\t\t\t\t<thead>\n\t\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t\t<th v-for=\"item in result.headers\">{{item}}</th>\n\t\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t\t</thead>\n\t\t\t\t\t\t\t<tbody>\n\t\t\t\t\t\t\t\t<tr v-for=\"items in result.results\">\n\t\t\t\t\t\t\t\t\t<td v-for=\"item in items\">\n\t\t\t\t\t\t\t\t\t\t<template v-if=\"item === null\">\n\t\t\t\t\t\t\t\t\t\t\t<span class=\"text-muted\">(null)</span>\n\t\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t\t\t\t{{item}}\n\t\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t\t</tbody>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t</template>\n\t\t\t\t</template>\n\t\t\t</template>\n\t\t</template>\n\t<!-- /container --></div>\n<!-- /content --></div>\n<footer id=\"footer\" class=\"py-3\">\n\t<address class=\"text-center\">\n\t\t<span><strong><a href=\"https://github.com/wyukawa/yanagishima\"><i class=\"fa fa-github mr-1\"></i>yanagishima</a></strong> by wyukawa and okazou</span>\n\t\t<a :href=\"contact\" target=\"_blank\" class=\"ml-2\" v-if=\"contact\"><i class=\"fa fa-commenting-o mr-1\"></i>Contact</a>\n\t</address>\n</footer>\n<div id=\"theme\" class=\"modal fade\">\n\t<div class=\"modal-dialog modal-lg\" role=\"document\">\n\t\t<div class=\"modal-content\">\n\t\t\t<div class=\"modal-header\">\n\t\t\t\t<h5 class=\"modal-title\"><strong class=\"mr-1\">{{themes.length}}</strong>themes</h5>\n\t\t\t\t<button type=\"button\" class=\"close\" data-dismiss=\"modal\"><span>&times;</span></button>\n\t\t\t<!-- /modal-header --></div>\n\t\t\t<div class=\"modal-body p-3\">\n\t\t\t\t<table class=\"table table-bordered table-fixed table-hover mb-0\">\n\t\t\t\t\t<tbody>\n\t\t\t\t\t\t<tr v-for=\"item in themes\">\n\t\t\t\t\t\t\t<td width=\"25%\">\n\t\t\t\t\t\t\t\t<label class=\"form-check-label\" :class=\"{'font-weight-bold': item == theme}\">\n\t\t\t\t\t\t\t\t\t<input type=\"radio\" class=\"form-check-input mr-2\" v-model=\"theme\" :value=\"item\" @change=\"$('#theme').modal('hide')\">{{item | humanize}}\n\t\t\t\t\t\t\t\t</label>\n\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t<td width=\"75%\">\n\t\t\t\t\t\t\t\t<ace code=\"SELECT * FROM catalog.schema.table LIMIT 100\" :theme=\"item\" readonly=\"true\"></ace>\n\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t</tr>\n\t\t\t\t\t</tbody>\n\t\t\t\t</table>\n\t\t\t<!-- /modal-body --></div>\n\t\t<!-- /modal-content --></div>\n\t<!-- /modal-dialog --></div>\n<!-- /modal --></div>\n<div id=\"treeview\" class=\"modal fade\">\n\t<div class=\"modal-dialog modal-lg\" role=\"document\">\n\t\t<div class=\"modal-content\">\n\t\t\t<div class=\"modal-header\">\n\t\t\t\t<h5 class=\"modal-title\">Datasource</h5>\n\t\t\t\t<button type=\"button\" class=\"close\" data-dismiss=\"modal\"><span>&times;</span></button>\n\t\t\t<!-- /modal-header --></div>\n\t\t\t<div class=\"modal-body py-2 px-4\">\n\t\t\t\t<div class=\"row align-items-center mb-3\">\n\t\t\t\t\t<div class=\"col\">\n\t\t\t\t\t\t<template v-if=\"datasources.length\">\n\t\t\t\t\t\t\t<template v-if=\"datasources.length === 1\">\n\t\t\t\t\t\t\t\t<span class=\"font-weight-bold\"><i class=\"fa fa-fw fa-plug mr-1\"></i>{{datasource}}</span>\n\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t<label class=\"form-check-label mr-3\" :class=\"{'font-weight-bold': item == datasource}\" v-for=\"item in datasources\">\n\t\t\t\t\t\t\t\t\t<input type=\"radio\" class=\"form-check-input mr-1\" v-model=\"datasource\" :value=\"item\">\n\t\t\t\t\t\t\t\t\t<i class=\"fa fa-fw fa-plug mr-1\" :class=\"'datasource_{0}-color'.format(datasources.indexOf(item))\"></i>{{item}}\n\t\t\t\t\t\t\t\t</label>\n\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t</template>\n\t\t\t\t\t</div>\n\t\t\t\t\t<div class=\"col-4 text-right\">\n\t\t\t\t\t\t<input type=\"text\" id=\"table_q_in\" class=\"form-control\" :placeholder=\"placeholder\" v-model=\"table_q_in\" @keyup.enter=\"searchTable()\">\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t\t<template v-if=\"!table_result.length && !table_q.length\">\n\t\t\t\t\t<div class=\"row\">\n\t\t\t\t\t\t<div class=\"col\">\n\t\t\t\t\t\t\t<div class=\"card mb-3\">\n\t\t\t\t\t\t\t\t<div class=\"card-header\">\n\t\t\t\t\t\t\t\t\t<i class=\"fa fa-fw fa-server mr-1\"></i>Catalog\n\t\t\t\t\t\t\t\t\t<span class=\"badge badge-default badge-pill\" v-if=\"catalogs.length\">{{catalogs.length}}</span>\n\t\t\t\t\t\t\t\t<!-- /card-header --></div>\n\t\t\t\t\t\t\t\t<div class=\"list-group list-group-flush\">\n\t\t\t\t\t\t\t\t\t<template v-if=\"catalogs.length\">\n\t\t\t\t\t\t\t\t\t<a href=\"#\" class=\"list-group-item\" v-for=\"item in catalogs\" :class=\"{active: item == catalog}\" @click.prevent=\"catalog = item, schema = '', table = ''\">{{item}}</a>\n\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t\t\t<a href=\"#\" class=\"list-group-item disabled\">(N/A)</a>\n\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t<!-- /list-group --></div>\n\t\t\t\t\t\t\t<!-- /card --></div>\n\t\t\t\t\t\t<!-- /col --></div>\n\t\t\t\t\t\t<div class=\"col\">\n\t\t\t\t\t\t\t<div class=\"card mb-3\">\n\t\t\t\t\t\t\t\t<div class=\"card-header\">\n\t\t\t\t\t\t\t\t\t<i class=\"fa fa-fw fa-database mr-1\"></i>Schema\n\t\t\t\t\t\t\t\t\t<span class=\"badge badge-default badge-pill\" v-if=\"schemata.length\">{{schemata.length}}</span>\n\t\t\t\t\t\t\t\t<!-- /card-header --></div>\n\t\t\t\t\t\t\t\t<div class=\"list-group list-group-flush\">\n\t\t\t\t\t\t\t\t\t<template v-if=\"catalog && schemata.length\">\n\t\t\t\t\t\t\t\t\t\t<a href=\"#\" class=\"list-group-item\" v-for=\"item in schemata\" :class=\"{active: item == schema}\" @click.prevent=\"schema = item, table = ''\">{{item}}</a>\n\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t\t\t<a href=\"#\" class=\"list-group-item disabled\">(N/A)</a>\n\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t<!-- /list-group --></div>\n\t\t\t\t\t\t\t<!-- /card --></div>\n\t\t\t\t\t\t<!-- /col --></div>\n\t\t\t\t\t\t<div class=\"col\">\n\t\t\t\t\t\t\t<div class=\"card mb-3\">\n\t\t\t\t\t\t\t\t<div class=\"card-header\">\n\t\t\t\t\t\t\t\t\t<i class=\"fa fa-fw fa-table mr-1\"></i>Table\n\t\t\t\t\t\t\t\t\t<span class=\"badge badge-default badge-pill\" v-if=\"tables.length\">{{tables.length}}</span>\n\t\t\t\t\t\t\t\t\t<div class=\"pull-right\">\n\t\t\t\t\t\t\t\t\t\t<small><i class=\"fa fa-fw fa-circle table-base\"></i>Base table</small>\n\t\t\t\t\t\t\t\t\t\t<small><i class=\"fa fa-fw fa-circle table-view\"></i>View</small>\n\t\t\t\t\t\t\t\t\t</div>\n\t\t\t\t\t\t\t\t<!-- /card-header --></div>\n\t\t\t\t\t\t\t\t<div class=\"list-group list-group-flush\">\n\t\t\t\t\t\t\t\t\t<template v-if=\"schema && tables.length\">\n\t\t\t\t\t\t\t\t\t<a href=\"#\" class=\"list-group-item\" v-for=\"items in tables\" :class=\"{active: items[0] == table, 'table-view': items[1] === 'VIEW'}\" @click.prevent=\"table = items[0], table_type = items[1]\">\n\t\t\t\t\t\t\t\t\t\t<button class=\"btn btn-sm btn-secondary clip\" v-clipboard=\"[catalog, schema, items[0]].join('.')\" data-toggle=\"tooltip\" data-placement=\"right\"><small>Copy</small></button>\n\t\t\t\t\t\t\t\t\t\t{{items[0]}}\n\t\t\t\t\t\t\t\t\t</a>\n\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t\t\t<a href=\"#\" class=\"list-group-item disabled\">(N/A)</a>\n\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t<!-- /list-group --></div>\n\t\t\t\t\t\t\t<!-- /card --></div>\n\t\t\t\t\t\t<!-- /col --></div>\n\t\t\t\t\t<!-- /row --></div>\n\t\t\t\t</template>\n\t\t\t\t<template v-else>\n\t\t\t\t\t<div class=\"card mb-4\">\n\t\t\t\t\t\t<div class=\"card-header\">\n\t\t\t\t\t\t\t<a href=\"#\" class=\"text-muted mr-2\" @click.prevent=\"table_q_in = '', searchTable()\"><i class=\"fa fa-times\"></i></a>\n\t\t\t\t\t\t\t\"<strong>{{table_q}}</strong>\" in {{catalog}}\n\t\t\t\t\t\t\t<template class=\"ml-2\" v-if=\"table_result.length\"><strong>{{table_result.length}}</strong> results</template>\n\t\t\t\t\t\t\t<div class=\"pull-right\">\n\t\t\t\t\t\t\t\t<small><i class=\"fa fa-fw fa-circle table-base\"></i>Base table</small>\n\t\t\t\t\t\t\t\t<small><i class=\"fa fa-fw fa-circle table-view\"></i>View</small>\n\t\t\t\t\t\t\t</div>\n\t\t\t\t\t\t<!-- /card-header --></div>\n\t\t\t\t\t\t<div class=\"list-group list-group-flush\">\n\t\t\t\t\t\t\t<template v-if=\"table_result.length\">\n\t\t\t\t\t\t\t<a href=\"#\" class=\"list-group-item ellipsis\" v-for=\"items in table_result\" @click.prevent=\"catalog = items[0], schema = items[1], table = items[2], table_type = items[3]\" :class=\"{active: items[0] == catalog && items[1] == schema && items[2] == table, 'table-view': items[3] == 'VIEW'}\">\n\t\t\t\t\t\t\t\t<button class=\"btn btn-sm btn-secondary clip\" v-clipboard=\"[items[0], items[1], items[2]].join('.')\" data-toggle=\"tooltip\" data-placement=\"right\"><small>Copy</small></button>\n\t\t\t\t\t\t\t\t{{items[0]}} / {{items[1]}} / {{items[2]}}\n\t\t\t\t\t\t\t</a>\n\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t<a href=\"#\" class=\"list-group-item disabled\">(Not Found)</a>\n\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t<!-- /list-group --></div>\n\t\t\t\t\t<!-- /card --></div>\n\t\t\t\t</template>\n\t\t\t<!-- /modal-body --></div>\n\t\t\t<div class=\"modal-footer\" v-if=\"table\">\n\t\t\t\t<select id=\"snippet\" class=\"custom-select\" v-model=\"snippet\">\n\t\t\t\t\t<option v-for=\"item in snippets\" :value=\"item.sql\" v-if=\"item.enable.includes(table_type)\">{{item.label}}</option>\n\t\t\t\t</select>\n\t\t\t\t<div class=\"btn-group\">\n\t\t\t\t\t<button class=\"btn btn-primary\" data-dismiss=\"modal\" @click=\"setSnippet\"><i class=\"fa fa-fw fa-pencil mr-1\"></i>Set</button>\n\t\t\t\t\t<button class=\"btn btn-primary\" data-dismiss=\"modal\" @click=\"runSnippet\"><i class=\"fa fa-fw fa-play mr-1\"></i><strong>Run</strong></button>\n\t\t\t\t</div>\n\t\t\t<!-- /modal-footer --></div>\n\t\t<!-- /modal-content --></div>\n\t<!-- /modal-dialog --></div>\n<!-- /modal --></div>\n<div id=\"bookmark\" class=\"modal fade\">\n\t<div class=\"modal-dialog modal-lg\" role=\"document\">\n\t\t<div class=\"modal-content\">\n\t\t\t<div class=\"modal-header\">\n\t\t\t\t<h5 class=\"modal-title\"><strong class=\"mr-1\">{{bookmarks.length}}</strong>bookmarks</h5>\n\t\t\t\t<button type=\"button\" class=\"close\" data-dismiss=\"modal\"><span>&times;</span></button>\n\t\t\t<!-- /modal-header --></div>\n\t\t\t<div class=\"modal-body p-3\">\n\t\t\t\t<template v-if=\"loading.bookmark\">\n\t\t\t\t</template>\n\t\t\t\t<template v-else>\n\t\t\t\t\t<table class=\"table table-bordered table-fixed table-hover mb-0\" v-if=\"bookmarkQlist.length\">\n\t\t\t\t\t\t<thead>\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<th width=\"85%\">Query</th>\n\t\t\t\t\t\t\t\t<th width=\"7.5%\" class=\"text-center\">Set</th>\n\t\t\t\t\t\t\t\t<th width=\"7.5%\" class=\"text-center\">Del</th>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</thead>\n\t\t\t\t\t\t<tbody>\n\t\t\t\t\t\t\t<tr v-for=\"item in bookmarkQlist\">\n\t\t\t\t\t\t\t\t<td>\n\t\t\t\t\t\t\t\t\t<ace :code=\"item.query\" readonly=\"true\" css_class=\"bg-transparent\"></ace>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t<td class=\"text-center\">\n\t\t\t\t\t\t\t\t\t<a href=\"#\" class=\"btn btn-sm btn-secondary\" @click.prevent=\"input_query = item.query, focus++\" data-dismiss=\"modal\"><i class=\"fa fa-fw fa-pencil\"></i></a>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t<td class=\"text-center\">\n\t\t\t\t\t\t\t\t\t<a href=\"#\" class=\"btn btn-sm btn-secondary\" @click.prevent=\"delBookmarkItem(item.queryid), bookmarkQlist.remove(item)\"><i class=\"fa fa-fw fa-trash\"></i></a>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</tbody>\n\t\t\t\t\t</table>\n\t\t\t\t\t<p v-else><strong>No bookmark</strong></p>\n\t\t\t\t</template>\n\t\t\t<!-- /modal-body --></div>\n\t\t<!-- /modal-content --></div>\n\t<!-- /modal-dialog --></div>\n<!-- /modal --></div>\n<div id=\"show\" class=\"modal fade\">\n\t<div class=\"modal-dialog modal-lg\" role=\"document\">\n\t\t<div class=\"modal-content\">\n\t\t\t<div class=\"modal-header\">\n\t\t\t\t<h5 class=\"modal-title\">{{input_query.compact().truncate(30)}}</h5>\n\t\t\t\t<button type=\"button\" class=\"close\" data-dismiss=\"modal\"><span>&times;</span></button>\n\t\t\t<!-- /modal-header --></div>\n\t\t\t<div class=\"modal-body p-3\">\n\t\t\t\t<template v-if=\"responses.show.error\">\n\t\t\t\t\t<div class=\"alert alert-danger\" role=\"alert\">{{responses.show.error}}</div>\n\t\t\t\t</template>\n\t\t\t\t<template v-else>\n\t\t\t\t\t<table class=\"table table-auto table-bordered table-hover table-responsive\">\n\t\t\t\t\t\t<thead>\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<th v-for=\"item in responses.show.headers\">{{item}}</th>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</thead>\n\t\t\t\t\t\t<tbody>\n\t\t\t\t\t\t\t<tr v-for=\"items in responses.show.results\">\n\t\t\t\t\t\t\t\t<td v-for=\"item in items\">\n\t\t\t\t\t\t\t\t\t<template v-if=\"item === null\">\n\t\t\t\t\t\t\t\t\t\t<span class=\"text-muted\">(null)</span>\n\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t\t<template v-else>\n\t\t\t\t\t\t\t\t\t\t{{item}}\n\t\t\t\t\t\t\t\t\t</template>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</tbody>\n\t\t\t\t\t</table>\n\t\t\t\t</template>\n\t\t\t<!-- /modal-body --></div>\n\t\t<!-- /modal-content --></div>\n\t<!-- /modal-dialog --></div>\n<!-- /modal --></div>\n<!-- /page --></div>\n<script src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js\"></script>\n<script src=\"https://cdnjs.cloudflare.com/ajax/libs/tether/1.4.0/js/tether.min.js\"></script>\n<script src=\"https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.0.0-alpha.6/js/bootstrap.min.js\"></script>\n<script src=\"https://cdnjs.cloudflare.com/ajax/libs/ace/1.2.6/ace.js\"></script>\n<script src=\"https://cdnjs.cloudflare.com/ajax/libs/vue/2.2.1/vue.min.js\"></script>\n<script src=\"https://cdnjs.cloudflare.com/ajax/libs/sugar/2.0.4/sugar.min.js\"></script>\n<script src=\"https://cdnjs.cloudflare.com/ajax/libs/favico.js/0.3.10/favico.min.js\"></script>\n<script src=\"https://cdnjs.cloudflare.com/ajax/libs/magnific-popup.js/1.1.0/jquery.magnific-popup.min.js\"></script>\n<script src=\"./index.js\"></script>\n</body>"

/***/ }),
/* 13 */
/***/ (function(module, exports) {

function select(element) {
    var selectedText;

    if (element.nodeName === 'SELECT') {
        element.focus();

        selectedText = element.value;
    }
    else if (element.nodeName === 'INPUT' || element.nodeName === 'TEXTAREA') {
        var isReadOnly = element.hasAttribute('readonly');

        if (!isReadOnly) {
            element.setAttribute('readonly', '');
        }

        element.select();
        element.setSelectionRange(0, element.value.length);

        if (!isReadOnly) {
            element.removeAttribute('readonly');
        }

        selectedText = element.value;
    }
    else {
        if (element.hasAttribute('contenteditable')) {
            element.focus();
        }

        var selection = window.getSelection();
        var range = document.createRange();

        range.selectNodeContents(element);
        selection.removeAllRanges();
        selection.addRange(range);

        selectedText = selection.toString();
    }

    return selectedText;
}

module.exports = select;


/***/ }),
/* 14 */
/***/ (function(module, exports) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/
var stylesInDom = {},
	memoize = function(fn) {
		var memo;
		return function () {
			if (typeof memo === "undefined") memo = fn.apply(this, arguments);
			return memo;
		};
	},
	isOldIE = memoize(function() {
		return /msie [6-9]\b/.test(self.navigator.userAgent.toLowerCase());
	}),
	getHeadElement = memoize(function () {
		return document.head || document.getElementsByTagName("head")[0];
	}),
	singletonElement = null,
	singletonCounter = 0,
	styleElementsInsertedAtTop = [];

module.exports = function(list, options) {
	if(typeof DEBUG !== "undefined" && DEBUG) {
		if(typeof document !== "object") throw new Error("The style-loader cannot be used in a non-browser environment");
	}

	options = options || {};
	// Force single-tag solution on IE6-9, which has a hard limit on the # of <style>
	// tags it will allow on a page
	if (typeof options.singleton === "undefined") options.singleton = isOldIE();

	// By default, add <style> tags to the bottom of <head>.
	if (typeof options.insertAt === "undefined") options.insertAt = "bottom";

	var styles = listToStyles(list);
	addStylesToDom(styles, options);

	return function update(newList) {
		var mayRemove = [];
		for(var i = 0; i < styles.length; i++) {
			var item = styles[i];
			var domStyle = stylesInDom[item.id];
			domStyle.refs--;
			mayRemove.push(domStyle);
		}
		if(newList) {
			var newStyles = listToStyles(newList);
			addStylesToDom(newStyles, options);
		}
		for(var i = 0; i < mayRemove.length; i++) {
			var domStyle = mayRemove[i];
			if(domStyle.refs === 0) {
				for(var j = 0; j < domStyle.parts.length; j++)
					domStyle.parts[j]();
				delete stylesInDom[domStyle.id];
			}
		}
	};
}

function addStylesToDom(styles, options) {
	for(var i = 0; i < styles.length; i++) {
		var item = styles[i];
		var domStyle = stylesInDom[item.id];
		if(domStyle) {
			domStyle.refs++;
			for(var j = 0; j < domStyle.parts.length; j++) {
				domStyle.parts[j](item.parts[j]);
			}
			for(; j < item.parts.length; j++) {
				domStyle.parts.push(addStyle(item.parts[j], options));
			}
		} else {
			var parts = [];
			for(var j = 0; j < item.parts.length; j++) {
				parts.push(addStyle(item.parts[j], options));
			}
			stylesInDom[item.id] = {id: item.id, refs: 1, parts: parts};
		}
	}
}

function listToStyles(list) {
	var styles = [];
	var newStyles = {};
	for(var i = 0; i < list.length; i++) {
		var item = list[i];
		var id = item[0];
		var css = item[1];
		var media = item[2];
		var sourceMap = item[3];
		var part = {css: css, media: media, sourceMap: sourceMap};
		if(!newStyles[id])
			styles.push(newStyles[id] = {id: id, parts: [part]});
		else
			newStyles[id].parts.push(part);
	}
	return styles;
}

function insertStyleElement(options, styleElement) {
	var head = getHeadElement();
	var lastStyleElementInsertedAtTop = styleElementsInsertedAtTop[styleElementsInsertedAtTop.length - 1];
	if (options.insertAt === "top") {
		if(!lastStyleElementInsertedAtTop) {
			head.insertBefore(styleElement, head.firstChild);
		} else if(lastStyleElementInsertedAtTop.nextSibling) {
			head.insertBefore(styleElement, lastStyleElementInsertedAtTop.nextSibling);
		} else {
			head.appendChild(styleElement);
		}
		styleElementsInsertedAtTop.push(styleElement);
	} else if (options.insertAt === "bottom") {
		head.appendChild(styleElement);
	} else {
		throw new Error("Invalid value for parameter 'insertAt'. Must be 'top' or 'bottom'.");
	}
}

function removeStyleElement(styleElement) {
	styleElement.parentNode.removeChild(styleElement);
	var idx = styleElementsInsertedAtTop.indexOf(styleElement);
	if(idx >= 0) {
		styleElementsInsertedAtTop.splice(idx, 1);
	}
}

function createStyleElement(options) {
	var styleElement = document.createElement("style");
	styleElement.type = "text/css";
	insertStyleElement(options, styleElement);
	return styleElement;
}

function createLinkElement(options) {
	var linkElement = document.createElement("link");
	linkElement.rel = "stylesheet";
	insertStyleElement(options, linkElement);
	return linkElement;
}

function addStyle(obj, options) {
	var styleElement, update, remove;

	if (options.singleton) {
		var styleIndex = singletonCounter++;
		styleElement = singletonElement || (singletonElement = createStyleElement(options));
		update = applyToSingletonTag.bind(null, styleElement, styleIndex, false);
		remove = applyToSingletonTag.bind(null, styleElement, styleIndex, true);
	} else if(obj.sourceMap &&
		typeof URL === "function" &&
		typeof URL.createObjectURL === "function" &&
		typeof URL.revokeObjectURL === "function" &&
		typeof Blob === "function" &&
		typeof btoa === "function") {
		styleElement = createLinkElement(options);
		update = updateLink.bind(null, styleElement);
		remove = function() {
			removeStyleElement(styleElement);
			if(styleElement.href)
				URL.revokeObjectURL(styleElement.href);
		};
	} else {
		styleElement = createStyleElement(options);
		update = applyToTag.bind(null, styleElement);
		remove = function() {
			removeStyleElement(styleElement);
		};
	}

	update(obj);

	return function updateStyle(newObj) {
		if(newObj) {
			if(newObj.css === obj.css && newObj.media === obj.media && newObj.sourceMap === obj.sourceMap)
				return;
			update(obj = newObj);
		} else {
			remove();
		}
	};
}

var replaceText = (function () {
	var textStore = [];

	return function (index, replacement) {
		textStore[index] = replacement;
		return textStore.filter(Boolean).join('\n');
	};
})();

function applyToSingletonTag(styleElement, index, remove, obj) {
	var css = remove ? "" : obj.css;

	if (styleElement.styleSheet) {
		styleElement.styleSheet.cssText = replaceText(index, css);
	} else {
		var cssNode = document.createTextNode(css);
		var childNodes = styleElement.childNodes;
		if (childNodes[index]) styleElement.removeChild(childNodes[index]);
		if (childNodes.length) {
			styleElement.insertBefore(cssNode, childNodes[index]);
		} else {
			styleElement.appendChild(cssNode);
		}
	}
}

function applyToTag(styleElement, obj) {
	var css = obj.css;
	var media = obj.media;

	if(media) {
		styleElement.setAttribute("media", media)
	}

	if(styleElement.styleSheet) {
		styleElement.styleSheet.cssText = css;
	} else {
		while(styleElement.firstChild) {
			styleElement.removeChild(styleElement.firstChild);
		}
		styleElement.appendChild(document.createTextNode(css));
	}
}

function updateLink(linkElement, obj) {
	var css = obj.css;
	var sourceMap = obj.sourceMap;

	if(sourceMap) {
		// http://stackoverflow.com/a/26603875
		css += "\n/*# sourceMappingURL=data:application/json;base64," + btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))) + " */";
	}

	var blob = new Blob([css], { type: "text/css" });

	var oldSrc = linkElement.href;

	linkElement.href = URL.createObjectURL(blob);

	if(oldSrc)
		URL.revokeObjectURL(oldSrc);
}


/***/ }),
/* 15 */
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__(6);
if(typeof content === 'string') content = [[module.i, content, '']];
// add the styles to the DOM
var update = __webpack_require__(14)(content, {});
if(content.locals) module.exports = content.locals;
// Hot Module Replacement
if(false) {
	// When the styles change, update the <style> tags
	if(!content.locals) {
		module.hot.accept("!!../../node_modules/css-loader/index.js!../../node_modules/sass-loader/lib/loader.js!./bootstrap.scss", function() {
			var newContent = require("!!../../node_modules/css-loader/index.js!../../node_modules/sass-loader/lib/loader.js!./bootstrap.scss");
			if(typeof newContent === 'string') newContent = [[module.id, newContent, '']];
			update(newContent);
		});
	}
	// When the module is disposed, remove the <style> tags
	module.hot.dispose(function() { update(); });
}

/***/ }),
/* 16 */
/***/ (function(module, exports) {

function E () {
  // Keep this empty so it's easier to inherit from
  // (via https://github.com/lipsmack from https://github.com/scottcorgan/tiny-emitter/issues/3)
}

E.prototype = {
  on: function (name, callback, ctx) {
    var e = this.e || (this.e = {});

    (e[name] || (e[name] = [])).push({
      fn: callback,
      ctx: ctx
    });

    return this;
  },

  once: function (name, callback, ctx) {
    var self = this;
    function listener () {
      self.off(name, listener);
      callback.apply(ctx, arguments);
    };

    listener._ = callback
    return this.on(name, listener, ctx);
  },

  emit: function (name) {
    var data = [].slice.call(arguments, 1);
    var evtArr = ((this.e || (this.e = {}))[name] || []).slice();
    var i = 0;
    var len = evtArr.length;

    for (i; i < len; i++) {
      evtArr[i].fn.apply(evtArr[i].ctx, data);
    }

    return this;
  },

  off: function (name, callback) {
    var e = this.e || (this.e = {});
    var evts = e[name];
    var liveEvents = [];

    if (evts && callback) {
      for (var i = 0, len = evts.length; i < len; i++) {
        if (evts[i].fn !== callback && evts[i].fn._ !== callback)
          liveEvents.push(evts[i]);
      }
    }

    // Remove event from queue to prevent memory leak
    // Suggested by https://github.com/lazd
    // Ref: https://github.com/scottcorgan/tiny-emitter/commit/c6ebfaa9bc973b33d110a84a307742b7cf94c953#commitcomment-5024910

    (liveEvents.length)
      ? e[name] = liveEvents
      : delete e[name];

    return this;
  }
};

module.exports = E;


/***/ }),
/* 17 */
/***/ (function(module, exports, __webpack_require__) {

var __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;/*!
 * vue-clipboards v0.2.5
 * (c) 2017 卓文理 <531840344@qq.com>
 * Released under the MIT License.
 */
(function (global, factory) {
   true ? module.exports = factory() :
  typeof define === 'function' && define.amd ? define(factory) :
  (global.vueClipboard = factory());
}(this, function () { 'use strict';

  var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
    return typeof obj;
  } : function (obj) {
    return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
  };

  var asyncGenerator = function () {
    function AwaitValue(value) {
      this.value = value;
    }

    function AsyncGenerator(gen) {
      var front, back;

      function send(key, arg) {
        return new Promise(function (resolve, reject) {
          var request = {
            key: key,
            arg: arg,
            resolve: resolve,
            reject: reject,
            next: null
          };

          if (back) {
            back = back.next = request;
          } else {
            front = back = request;
            resume(key, arg);
          }
        });
      }

      function resume(key, arg) {
        try {
          var result = gen[key](arg);
          var value = result.value;

          if (value instanceof AwaitValue) {
            Promise.resolve(value.value).then(function (arg) {
              resume("next", arg);
            }, function (arg) {
              resume("throw", arg);
            });
          } else {
            settle(result.done ? "return" : "normal", result.value);
          }
        } catch (err) {
          settle("throw", err);
        }
      }

      function settle(type, value) {
        switch (type) {
          case "return":
            front.resolve({
              value: value,
              done: true
            });
            break;

          case "throw":
            front.reject(value);
            break;

          default:
            front.resolve({
              value: value,
              done: false
            });
            break;
        }

        front = front.next;

        if (front) {
          resume(front.key, front.arg);
        } else {
          back = null;
        }
      }

      this._invoke = send;

      if (typeof gen.return !== "function") {
        this.return = undefined;
      }
    }

    if (typeof Symbol === "function" && Symbol.asyncIterator) {
      AsyncGenerator.prototype[Symbol.asyncIterator] = function () {
        return this;
      };
    }

    AsyncGenerator.prototype.next = function (arg) {
      return this._invoke("next", arg);
    };

    AsyncGenerator.prototype.throw = function (arg) {
      return this._invoke("throw", arg);
    };

    AsyncGenerator.prototype.return = function (arg) {
      return this._invoke("return", arg);
    };

    return {
      wrap: function (fn) {
        return function () {
          return new AsyncGenerator(fn.apply(this, arguments));
        };
      },
      await: function (value) {
        return new AwaitValue(value);
      }
    };
  }();

  /**
   * @author: 卓文理
   * @email : 531840344@qq.com
   * @desc  : VueClipboard
   */

  var Clipboard = __webpack_require__(5);

  if (!Clipboard) {
      throw new Error('[vue-clipboards] cannot locate Clipboard.');
  }

  function VueClipboard (Vue) {
      var clipboards = void 0;

      Vue.directive('clipboard', {
          bind: function bind(container, binding, vnode) {
              var value = binding.value;

              var option = {};

              if (value && typeof value === 'string') {
                  option.text = function () {
                      return value;
                  };
              }

              clipboards = new Clipboard(container, option);

              var componentOptions = vnode.componentOptions,
                  data = vnode.data;

              var listeners = componentOptions ? componentOptions.listeners : null;
              var on = data ? data.on : null;
              var events = listeners && listeners || on && on;

              if (events && (typeof events === 'undefined' ? 'undefined' : _typeof(events)) === 'object' && Object.keys(events).length) {
                  // fixed with Vue 2.2.x, event object `fn` rename to `fns`
                  Object.keys(events).map(function (cb) {
                      return clipboards.on(cb, events[cb].fn || events[cb].fns);
                  });
              }
          },
          unbind: function unbind() {
              if (clipboards && clipboards.destroy) {
                  clipboards.destroy();
              }
          },
          update: function update(container, binding, vnode) {
              binding.def.unbind();
              binding.def.bind(container, binding, vnode);
          }
      });
  }

  function plugin(Vue) {
      var option = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

      if (plugin.installed) {
          console.warn('[vue-clipboards] already installed.');
      }

      VueClipboard(Vue);
  }

  if (true) {
      !(__WEBPACK_AMD_DEFINE_ARRAY__ = [], __WEBPACK_AMD_DEFINE_RESULT__ = function () {
          return { plugin: plugin };
      }.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
  } else if (window.Vue) {
      window.Vue.use(plugin);
  }

  return plugin;

}));

/***/ }),
/* 18 */
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(1);
__webpack_require__(3);
module.exports = __webpack_require__(2);


/***/ })
/******/ ]);