require('../build/index.html');
require('./scss/bootstrap.scss');
var VueClipboards = require('vue-clipboards');
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