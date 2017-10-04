// Vue component for Ace Editor
Vue.component('ace', {
	template: '<div :id="aceId" style="width: 100%; height: 100%;"></div>',
	props: ['code', 'lang', 'theme', 'disabled', 'readonly', 'gotoline', 'errorline', 'errortext', 'focus', 'minline', 'maxline', 'css_class', 'complate_words'],
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
Vue.component('column', {
	template: '<div v-html="result"></div>',
	props: ['value', 'disable'],
	computed: {
		result: function() {
			var self = this;
			var value = self.value;
			var disable = self.disable || false;
			if (value === null) {
				value = '<span class="text-muted">(null)</span>';
			} else {
				if (!disable) {
					if (isJSON(value)) {
						var o = (new Function("return " + value))();
						if (Object.isObject(o)) {
							value = '<pre>{0}</pre>'.format(JSON.stringify(o, undefined, 4));
						}
					}
					function isJSON(arg) {
						var arg = (typeof arg === 'function') ? arg() : arg;
						if (typeof arg  !== 'string') {
							return false;
						}
						try {
							arg = (!JSON) ? eval('(' + arg + ')') : JSON.parse(arg);
							return true;
						} catch (e) {
							return false;
						}
					};
				}
			}
			return value;
		}
	}
});