// Vue component for Ace Editor
Vue.component('ace', {
	template: '<div :id="aceId" style="width: 100%; height: 100%;"></div>',
	props: ['code', 'lang', 'theme', 'disabled', 'readonly', 'gotoline', 'errorline', 'errortext', 'focus', 'minline', 'maxline', 'gutter', 'css_class', 'complate_words'],
	data: function() {
		return {
			ace: Object,
			aceId: 'ace-' + this._uid,
			prevCode: '',
			is_autocomplate: false,
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
		'disabled': function(value, oldValue) {
			this.ace.setReadOnly(value);
		},
		'errorline': function(value, oldValue) {
			if (value !== -1) {
				this.ace.getSession().setAnnotations([{
					row: value,
					type: 'error',
					text: this.errortext || 'Error'
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
		'minline': function(value, oldValue) {
			this.ace.setOptions({
				minLines: value,
			});
		},
		'theme': function(value, oldValue) {
			this.ace.setTheme(`ace/theme/` + value)
		},
		'complate_words': function(value, oldValue) {
			this.startAutoComplate();
		},
	},
	methods: {
		startAutoComplate: function() {
			var self = this;
			var langTools = ace.require("ace/ext/language_tools");
			var complate_words = self.complate_words || [];
			var functionCompleter = {
				getCompletions: function(editor, session, pos, prefix, callback) {
					callback(null, complate_words);
				}
			}
			langTools.setCompleters([functionCompleter]);
			self.ace.setOptions({
				enableSnippets: true,
				enableBasicAutocompletion: true,
				enableLiveAutocompletion: false,
			});
		}
	},
	mounted: function() {
		var self = this;
		var lang = self.lang || 'sql';
		var theme = self.theme || 'chrome';
		var readonly = Boolean(self.readonly) || false;
		var gutter = (self.gutter == undefined) ? true : Number(self.gutter);

		self.ace = window.ace.edit(self.aceId);

		self.ace.$blockScrolling = Infinity;
		self.ace.setShowPrintMargin(false);
		self.ace.setTheme(`ace/theme/` + theme)
		self.ace.getSession().setMode(`ace/mode/` + lang)
		self.ace.getSession().setUseWrapMode(true);
		self.ace.setFontSize(13);
		self.ace.setValue(self.code, 1);
		self.ace.setStyle(self.css_class || '', true);
		self.ace.commands.bindKey("Ctrl-P", "golineup");
		self.ace.commands.bindKey("Ctrl-T", "");
		self.ace.commands.bindKey("Command-L", "");
		self.ace.commands.removeCommand('find');
		self.ace.renderer.setShowGutter(gutter);

		if (readonly) {
			self.ace.setReadOnly(readonly);
			self.ace.gotoLine(1, 1);
			self.ace.renderer.setShowGutter(false);
			self.ace.setHighlightActiveLine(false);
			self.ace.setOptions({
				minLines: 1,
				maxLines: self.maxline || 8
			});
			self.ace.renderer.$cursorLayer.element.style.display = "none"
		} else {
			self.ace.setOptions({
				minLines: self.minline || 4,
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
			self.ace.commands.addCommand({
				name: 'validate',
				bindKey: {
					win: 'Shift-Enter',
					mac: 'Shift-Enter'
				},
				exec: function() {
					self.$emit('validate-code', self.ace.getValue());
				}
			});
			self.ace.commands.addCommand({
				name: 'format',
				bindKey: {
					win: 'Ctrl-Shift-F',
					mac: 'Ctrl-Shift-F'
				},
				exec: function() {
					self.$emit('format-code', self.ace.getValue());
				}
			});
		}
	}
});
Vue.component('highlight', {
	template: '<span v-html="result"></span>',
	props: ['sentense', 'keyword'],
	computed: {
		result: function() {
			var self = this;
			var sentense = self.sentense;
			var keyword = self.keyword.trim();
			if (keyword.length) {
				var keywords = keyword.includes(' ') ? keyword.split(' ').unique() : [keyword];
				keywords.map(function(n) {
					var before, after;
					before = new RegExp('({0})'.format(RegExp.escape(n)), 'ig');
					after = '<mark>$1</mark>';
					sentense = sentense.replace(before, after);
				});
			}
			return sentense;
		}
	}
});
Vue.component('autolink', {
	template: '<span v-html="result"></span>',
	props: ['content'],
	computed: {
		result: function() {
			var self = this;
			var content= self.content;
			return content.replace(/((http|https|ftp)(:\/\/[-_.!~*\'()a-zA-Z0-9;\/?:\@&=+\$,%#]+))/g, '<a href="$1" target="_blank">$1</a>');
		}
	}
});
Vue.component('result-table', {
	template: `
<div>
	<table class="table table-auto table-bordered table-hover table-responsive">
		<thead>
			<tr>
				<th class="text-muted">line</th>
				<th v-for="item in result.headers">{{item}}</th>
			</tr>
		</thead>
		<tbody>
			<tr v-for="(items, index) in result.results" :class="{'table-info': line == (index + 1)}" :id="'L' + (index + 1)">
				<td class="text-right"><a href="#" @click.prevent="setLine(index)" class="text-muted">{{index + 1}}</a></td>
				<td v-for="item in items">
					<column :value="item" :disable="!is_pretty"></column>
				</td>
			</tr>
		</tbody>
	</table>
	<div class="text-left" v-if="result.lineNumber > 501">This data is only top 500.</div>
</div>`,
	props: ['result', 'is_pretty', 'line'],
	methods: {
		setLine: function (index) {
			const self = this;
			self.$emit('set-line', self.line === (index + 1) ? 0 : (index + 1));
		}
	}
});
Vue.component('column', {
	template: '<div v-html="result"></div>',
	props: ['value', 'disable'],
	computed: {
		result: function() {
			var self = this;
			var value = self.value;
			var formatedValue = null;
			var disable = self.disable || false;
			if (value === null) {
				formatedValue = '<span class="text-muted">(null)</span>';
			} else {
				if (!disable) {
					if (isJSON(value)) {
						var o = (new Function("return " + value))();
						if (Object.isObject(o)) {
							formatedValue = '<pre class="mb-0">{0}</pre>'.format((JSON.stringify(o, undefined, 4)).escapeHTML());
						} else {
							formatedValue = value.escapeHTML();
						}
					} else {
						if (isMAP(value)) {
							var lines = [];
							var values = value.removeAll(/[\{\}]/).split(', ');
							var lastIndex = values.length - 1;
							values.map(function(n, i) {
								var item = n.trim().replace('=', ' = ');
								var delimiter = i != lastIndex ? ',' : '';
								lines.push('    {0}{1}'.format(item, delimiter));
							});
							formatedValue = '<pre class="mb-0">{{\n{0}\n}}</pre>'.format(lines.join('\n').escapeHTML());
						} else {
							formatedValue = value.escapeHTML();
						}
					}
					function isJSON(arg) {
						var arg = (typeof arg === 'function') ? arg() : arg;
						if (typeof arg !== 'string') {
							return false;
						}
						try {
							arg = (!JSON) ? eval('(' + arg + ')') : JSON.parse(arg);
							return true;
						} catch (e) {
							return false;
						}
					};
					function isMAP(arg) {
						return /^\{.*=.*\}$/.test(arg);
					}
				} else {
					formatedValue = value.escapeHTML();
				}
			}
			return formatedValue;
		}
	}
});
