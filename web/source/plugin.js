// Vue component for Ace Editor
Vue.component('ace', {
	template: '<div :id="aceId" style="width: 100%; height: 100%;"></div>',
	props: ['code', 'lang', 'theme', 'readonly', 'gotoline', 'errorline', 'focus', 'maxline', 'css_class'],
	data: function() {
		return {
			ace: Object,
			aceId: 'ace-' + this._uid,
			prevCode: ''
		}
	},
	watch: {
		'code': function(value) {
			this.ace.getSession().clearAnnotations();
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
		'errorline': function(value, oldValue) {
			if (value !== -1) {
				this.ace.getSession().setAnnotations([{
					row: value,
					type: 'error',
					text: 'Error'
				}]);
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
		var wordList = [];

		self.ace = window.ace.edit(self.aceId);

		self.ace.$blockScrolling = Infinity;
		self.ace.setShowPrintMargin(false);
		self.ace.setTheme(`ace/theme/` + theme)
		self.ace.getSession().setMode(`ace/mode/` + lang)
		self.ace.getSession().setUseWrapMode(true);
		self.ace.setFontSize(13);
		self.ace.setValue(self.code, 1);
		self.ace.setStyle(style);
		self.ace.commands.bindKey("Ctrl-P", "golineup");
		self.ace.commands.bindKey("Ctrl-T", "");
		self.ace.commands.addCommand({
			name: "myCommand",
			bindKey: {
				win: "Esc",
				mac: "Esc"
			},
			exec: function(editor) {
				toggleAutoComplate();
			}
		});
		var langTools = ace.require("ace/ext/language_tools");

		function toggleAutoComplate() {
			wordList = wordList.length ? [] : functions;
			var functionCompleter = {
				getCompletions: function(editor, session, pos, prefix, callback) {
					callback(null, wordList.map(function(word) {
						return {
							caption: word,
							value: word,
							meta: "function"
						};
					}));
				}
			}
			langTools.setCompleters([functionCompleter]);
			self.ace.setOptions({
				enableSnippets: true,
				enableBasicAutocompletion: true,
				enableLiveAutocompletion: true,
			});
		}

		if (readonly) {
			self.ace.setReadOnly(readonly);
			self.ace.gotoLine(1, 1);
			self.ace.renderer.setShowGutter(false);
			self.ace.setHighlightActiveLine(false);
			self.ace.setOptions({
				minLines: 1,
				maxLines: 8
			});
			self.ace.renderer.$cursorLayer.element.style.display = "none"
		} else {
			self.ace.setOptions({
				minLines: 4,
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
var functions = [
	"abs",
	"acos",
	"approx_distinct",
	"approx_percentile",
	"approx_set",
	"arbitrary",
	"array_agg",
	"array_distinct",
	"array_intersect",
	"array_join",
	"array_max",
	"array_min",
	"array_position",
	"array_remove",
	"array_sort",
	"array_union",
	"asin",
	"atan",
	"atan2",
	"avg",
	"bar",
	"bit_count",
	"bitwise_and",
	"bitwise_and_agg",
	"bitwise_not",
	"bitwise_or",
	"bitwise_or_agg",
	"bitwise_xor",
	"bool_and",
	"bool_or",
	"cardinality",
	"cbrt",
	"ceil",
	"ceiling",
	"char2hexint",
	"checksum",
	"chr",
	"classify",
	"coalesce",
	"color",
	"concat",
	"contains",
	"corr",
	"cos",
	"cosh",
	"cosine_similarity",
	"count",
	"count_if",
	"covar_pop",
	"covar_samp",
	"cume_dist",
	"current_date",
	"current_time",
	"current_timestamp",
	"current_timezone",
	"date",
	"date_add",
	"date_diff",
	"date_format",
	"date_parse",
	"date_trunc",
	"day",
	"day_of_month",
	"day_of_week",
	"day_of_year",
	"degrees",
	"dense_rank",
	"dow",
	"doy",
	"e",
	"element_at",
	"empty_approx_set",
	"evaluate_classifier_predictions",
	"every",
	"exp",
	"features",
	"filter",
	"first_value",
	"flatten",
	"floor",
	"format_datetime",
	"from_base",
	"from_base64",
	"from_base64url",
	"from_big_endian_64",
	"from_hex",
	"from_iso8601_date",
	"from_iso8601_timestamp",
	"from_unixtime",
	"from_utf8",
	"geometric_mean",
	"greatest",
	"histogram",
	"hour",
	"index",
	"infinity",
	"is_finite",
	"is_infinite",
	"is_nan",
	"json_array_contains",
	"json_array_get",
	"json_array_length",
	"json_extract",
	"json_extract_scalar",
	"json_format",
	"json_parse",
	"json_size",
	"lag",
	"last_value",
	"lead",
	"learn_classifier",
	"learn_libsvm_classifier",
	"learn_libsvm_regressor",
	"learn_regressor",
	"least",
	"length",
	"like_pattern",
	"ln",
	"localtime",
	"localtimestamp",
	"log",
	"log10",
	"log2",
	"lower",
	"lpad",
	"ltrim",
	"map",
	"map_agg",
	"map_concat",
	"map_filter",
	"map_keys",
	"map_union",
	"map_values",
	"max",
	"max_by",
	"md5",
	"merge",
	"min",
	"min_by",
	"minute",
	"mod",
	"month",
	"multimap_agg",
	"nan",
	"normalize",
	"now",
	"nth_value",
	"ntile",
	"numeric_histogram",
	"objectid",
	"parse_datetime",
	"percent_rank",
	"pi",
	"pow",
	"power",
	"quarter",
	"radians",
	"rand",
	"random",
	"rank",
	"reduce",
	"regexp_extract",
	"regexp_extract_all",
	"regexp_like",
	"regexp_replace",
	"regexp_split",
	"regr_intercept",
	"regr_slope",
	"regress",
	"render",
	"replace",
	"reverse",
	"rgb",
	"round",
	"row_number",
	"rpad",
	"rtrim",
	"second",
	"sequence",
	"sha1",
	"sha256",
	"sha512",
	"shuffle",
	"sign",
	"sin",
	"slice",
	"split",
	"split_part",
	"split_to_map",
	"sqrt",
	"stddev",
	"stddev_pop",
	"stddev_samp",
	"strpos",
	"substr",
	"substring",
	"sum",
	"tan",
	"tanh",
	"timezone_hour",
	"timezone_minute",
	"to_base",
	"to_base64",
	"to_base64url",
	"to_big_endian_64",
	"to_char",
	"to_date",
	"to_hex",
	"to_iso8601",
	"to_timestamp",
	"to_unixtime",
	"to_utf8",
	"transform",
	"trim",
	"truncate",
	"typeof",
	"upper",
	"url_decode",
	"url_encode",
	"url_extract_fragment",
	"url_extract_host",
	"url_extract_parameter",
	"url_extract_path",
	"url_extract_port",
	"url_extract_protocol",
	"url_extract_query",
	"uuid",
	"var_pop",
	"var_samp",
	"variance",
	"week",
	"week_of_year",
	"width_bucket",
	"xxhash64",
	"year",
	"year_of_week",
	"yow",
	"zip",
];