export function parseStructured(text) {
  if (!text) return []
  let s = text.replace(/\r/g, '\n').trim()

  // 如果包含明显的编号（1. 2. ...），按编号切分
  const marker = /(^|\n)\s*(\d+)\.\s*/g
  const matches = []
  let m
  while ((m = marker.exec(s)) !== null) {
    matches.push({ index: m.index + m[1].length, num: m[2], len: m[0].length })
  }

  if (matches.length > 0) {
    const parts = []
    // intro: 内容在第一个编号之前
    const firstPos = matches[0].index
    const intro = s.slice(0, firstPos).trim()
    if (intro) parts.push({ type: 'intro', text: intro })

    for (let i = 0; i < matches.length; i++) {
      const start = matches[i].index + matches[i].len
      const end = i + 1 < matches.length ? matches[i + 1].index : s.length
      const itemText = s.slice(start, end).trim()
      const label = matches[i].num + '.'
      parts.push({ type: 'item', text: (label + ' ' + itemText).trim() })
    }

    // 检查编号后是否还有结尾段（编号后的文本末尾之外）
    const lastEnd = matches.length ? (matches[matches.length - 1].index + matches[matches.length - 1].len) : 0
    const after = s.slice(lastEnd).trim()
    // 如果 after 包含编号项之外的尾部文字（比如结尾段），但在上面我们已经包含至末尾，故这里尝试识别结尾：
    // 当最后一项已经到末尾则无额外结尾。
    // 为鲁棒性，如果末尾包含明显的结尾提示（如“希望”开头或短句），且不重复，则保留为结尾。
    // 简化处理：如果最后一个 item 的文本包含多个段落（\n\n），将最后的段落作为结尾。
    const lastPart = parts[parts.length - 1]
    if (lastPart && /\n\n+/.test(lastPart.text)) {
      const segs = lastPart.text.split(/\n\n+/).map(s => s.trim()).filter(Boolean)
      if (segs.length > 1) {
        lastPart.text = segs[0]
        parts.push({ type: 'closing', text: segs.slice(1).join('\n\n') })
      }
    }

    // 转为纯文本数组输出
    return parts.map(p => p.text)
  }

  // 如果没有编号，按双换行分段
  const paras = s.split(/\n\n+/).map(p => p.trim()).filter(Boolean)
  return paras
}
