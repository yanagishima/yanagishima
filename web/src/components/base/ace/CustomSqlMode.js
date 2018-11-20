const oop = ace.require('ace/lib/oop')
const BaseFoldMode = ace.require('ace/mode/folding/fold_mode').FoldMode

const FoldMode = function () {
}
oop.inherits(FoldMode, BaseFoldMode)

;(function () {
  // There is "(" and there is no ")" until the end
  this.foldingStartMarker = /(\()[^)]*$/
  // There is no "(" from start and there is ")"
  this.foldingStopMarker = /^[^(]*\)/
  this.getFoldWidgetRange = function (session, foldStyle, row) {
    const line = session.getLine(row)
    const match = line.match(this.foldingStartMarker)
    if (match) {
      const i = match.index
      return this.openingBracketBlock(session, match[1], row, i)
    }
  }
}).call(FoldMode.prototype)

const TextMode = ace.require('ace/mode/text').Mode
const SqlHighlightRules = ace.require('ace/mode/sql_highlight_rules').SqlHighlightRules

const Mode = function () {
  this.HighlightRules = SqlHighlightRules
  this.$behaviour = this.$defaultBehaviour
  this.foldingRules = new FoldMode()
}
oop.inherits(Mode, TextMode)

;(function () {
  this.lineCommentStart = '--'
}).call(Mode.prototype)

export default Mode
