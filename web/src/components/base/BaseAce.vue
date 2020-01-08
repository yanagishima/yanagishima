<template>
  <div style="width: 100%; height: 100%"></div>
</template>

<script>
import ace from 'ace-builds/src-min-noconflict/ace'
import 'ace-builds/src-min-noconflict/ext-language_tools'
import './ace/themes'
import './ace/snippets'
import CustomSqlMode from './ace/CustomSqlMode'

export default {
  name: 'BaseAce',
  props: {
    code: {
      type: String,
      required: true
    },
    theme: {
      type: String,
      default: 'chrome'
    },
    readonly: {
      type: Boolean,
      default: false
    },
    gotoLine: {
      type: Number
    },
    errorLine: {
      type: Number
    },
    errorText: {
      type: String
    },
    focus: {
      type: Number
    },
    minLines: {
      type: Number
    },
    maxLines: {
      type: Number
    },
    showGutter: {
      type: Boolean,
      default: true
    },
    cssClass: {
      type: String,
      default: ''
    },
    completeWords: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      ace: null,
      prevCode: ''
    }
  },
  watch: {
    code (val) {
      this.ace.getSession().clearAnnotations()
      if (this.prevCode !== val) {
        this.ace.setValue(val, 1)
      }
    },
    theme (val) {
      this.ace.setTheme(`ace/theme/${val}`)
    },
    readonly (val) {
      this.ace.setReadOnly(val)
    },
    gotoLine (val, old) {
      if (val && val !== old) {
        this.ace.gotoLine(val)
      }
    },
    errorLine (val) {
      if (val !== -1) {
        this.ace.getSession().setAnnotations([{
          row: val,
          type: 'error',
          text: this.errorText || 'Error'
        }])
      }
    },
    focus (val) {
      if (val) {
        this.ace.focus()
      }
    },
    minLines (val) {
      this.ace.setOptions({minLines: val})
    },
    maxLines (val) {
      this.ace.setOptions({maxLines: val})
    },
    completeWords () {
      this.startAutoComplete()
    }
  },
  mounted () {
    const a = ace.edit(this.$el)

    a.$blockScrolling = Infinity
    a.setShowPrintMargin(false)
    a.setTheme(`ace/theme/${this.theme}`)
    a.getSession().setMode(new CustomSqlMode())
    a.getSession().setUseWrapMode(true)
    a.setFontSize(13)
    a.setValue(this.code, 1)
    a.setStyle(this.cssClass, true)
    a.commands.bindKey('Ctrl-P', 'golineup')
    a.commands.bindKey('Ctrl-T', '')
    a.commands.bindKey('Command-L', '')
    a.commands.removeCommand('find')
    a.renderer.setShowGutter(this.showGutter)

    if (this.readonly) {
      a.setReadOnly(true)
      a.gotoLine(1, 1)
      a.renderer.setShowGutter(false)
      a.setHighlightActiveLine(false)
      a.setOptions({
        minLines: 1,
        maxLines: this.maxLines || 8
      })
      a.renderer.$cursorLayer.element.style.display = 'none'
    } else {
      a.setOptions({
        minLines: this.minLines || 4,
        maxLines: this.maxLines || 16
      })

      a.on('change', () => {
        const value = a.getValue()
        this.prevCode = value
        this.$emit('change-code', value)
      })

      a.commands.addCommand({
        name: 'run',
        bindKey: {
          win: 'Ctrl-Enter',
          mac: 'Ctrl-Enter'
        },
        exec: () => this.$emit('run-code', a.getValue())
      })
      a.commands.addCommand({
        name: 'validate',
        bindKey: {
          win: 'Shift-Enter',
          mac: 'Shift-Enter'
        },
        exec: () => this.$emit('validate-code', a.getValue())
      })
      a.commands.addCommand({
        name: 'format',
        bindKey: {
          win: 'Ctrl-Shift-F',
          mac: 'Ctrl-Shift-F'
        },
        exec: () => this.$emit('format-code', a.getValue())
      })
    }

    this.ace = a
  },
  methods: {
    startAutoComplete () {
      const langTools = ace.require('ace/ext/language_tools')
      const completeWords = this.completeWords
      const completers = {
        getCompletions (editor, session, pos, prefix, callback) {
          callback(null, completeWords)
        }
      }
      langTools.setCompleters([completers])
      this.ace.setOptions({
        enableSnippets: true,
        enableBasicAutocompletion: true,
        enableLiveAutocompletion: false
      })
    }
  }
}
</script>

<style scoped>
</style>
