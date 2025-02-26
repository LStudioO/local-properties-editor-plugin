package com.github.lstudioo.propertieseditor.utils

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class SystemClipboardManager : ClipboardManager {
    override fun setClipboardContents(text: String) {
        val stringSelection = StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
    }
}