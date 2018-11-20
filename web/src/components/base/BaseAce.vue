<template>
  <div style="width: 100%; height: 100%"></div>
</template>

<script>
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
    const ace = window.ace.edit(this.$el)

    ace.$blockScrolling = Infinity
    ace.setShowPrintMargin(false)
    ace.setTheme(`ace/theme/${this.theme}`)
    ace.getSession().setMode(new CustomSqlMode())
    ace.getSession().setUseWrapMode(true)
    ace.setFontSize(13)
    ace.setValue(this.code, 1)
    ace.setStyle(this.cssClass, true)
    ace.commands.bindKey('Ctrl-P', 'golineup')
    ace.commands.bindKey('Ctrl-T', '')
    ace.commands.bindKey('Command-L', '')
    ace.commands.removeCommand('find')
    ace.renderer.setShowGutter(this.showGutter)

    if (this.readonly) {
      ace.setReadOnly(true)
      ace.gotoLine(1, 1)
      ace.renderer.setShowGutter(false)
      ace.setHighlightActiveLine(false)
      ace.setOptions({
        minLines: 1,
        maxLines: this.maxLines || 8
      })
      ace.renderer.$cursorLayer.element.style.display = 'none'
    } else {
      ace.setOptions({
        minLines: this.minLines || 4,
        maxLines: this.maxLines || 16
      })

      ace.on('change', () => {
        const value = ace.getValue()
        this.prevCode = value
        this.$emit('change-code', value)
      })

      ace.commands.addCommand({
        name: 'run',
        bindKey: {
          win: 'Ctrl-Enter',
          mac: 'Ctrl-Enter'
        },
        exec: () => this.$emit('run-code', ace.getValue())
      })
      ace.commands.addCommand({
        name: 'validate',
        bindKey: {
          win: 'Shift-Enter',
          mac: 'Shift-Enter'
        },
        exec: () => this.$emit('validate-code', ace.getValue())
      })
      ace.commands.addCommand({
        name: 'format',
        bindKey: {
          win: 'Ctrl-Shift-F',
          mac: 'Ctrl-Shift-F'
        },
        exec: () => this.$emit('format-code', ace.getValue())
      })
    }

    this.ace = ace
  },
  methods: {
    startAutoComplete () {
      const langTools = window.ace.require('ace/ext/language_tools')
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
