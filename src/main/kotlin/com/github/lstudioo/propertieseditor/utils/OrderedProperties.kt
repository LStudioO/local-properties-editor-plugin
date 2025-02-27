package com.github.lstudioo.propertieseditor.utils

import java.io.*
import java.util.*
import java.util.regex.Pattern

/**
 * A custom Properties implementation that preserves the order of properties
 * and maintains comments when saving.
 */
class OrderedProperties : Properties() {
    private val keyOrder = LinkedHashSet<Any>()
    private val commentMap = mutableMapOf<String, String>()
    private var headerComment: String? = null
    
    // Pattern to match date comments like "#Thu Feb 27 20:09:21 EET 2025"
    private val dateCommentPattern = Pattern.compile(
        "#(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s+\\w+\\s+\\d+\\s+\\d+:\\d+:\\d+\\s+\\w+\\s+\\d{4}"
    )

    private fun clearData() {
        keyOrder.clear()
        commentMap.clear()
        headerComment = null
    }

    /**
     * Loads properties from the given input stream, preserving comments.
     */
    @Synchronized
    override fun load(inStream: InputStream) {
        try {
            // Clear existing data
            clearData()
            
            // Load the file as lines first to capture comments
            val reader = BufferedReader(InputStreamReader(inStream))
            val lines = reader.readLines()

            // Process the file to extract comments
            var currentComment = StringBuilder()
            var lineIndex = 0

            while (lineIndex < lines.size) {
                val line = lines[lineIndex]
                val trimmedLine = line.trim()
                
                // Check if this is the first line and it's a date comment
                if (lineIndex == 0 && (trimmedLine.startsWith("#") || trimmedLine.startsWith("!")) && 
                    (dateCommentPattern.matcher(trimmedLine).matches() || trimmedLine.contains("Property file created on"))) {
                    // This is a header date comment, save it separately
                    headerComment = line
                    lineIndex++
                    continue
                }
                
                when {
                    trimmedLine.isEmpty() -> {
                        // Add empty lines to the comment if we're building one
                        if (currentComment.isNotEmpty()) {
                            currentComment.append("\n")
                        }
                    }
                    trimmedLine.startsWith("#") || trimmedLine.startsWith("!") -> {
                        // Add to current comment
                        if (currentComment.isNotEmpty()) currentComment.append("\n")
                        currentComment.append(line)
                    }
                    else -> {
                        // This is likely a property line
                        val equalsPos = line.indexOf('=')
                        val colonPos = line.indexOf(':')

                        if (equalsPos > 0 || colonPos > 0) {
                            val separator = if (equalsPos > 0 && (colonPos <= 0 || equalsPos < colonPos)) equalsPos else colonPos
                            val key = line.substring(0, separator).trim()

                            // Save the comment for this key if we have one
                            if (currentComment.isNotEmpty()) {
                                commentMap[key] = currentComment.toString()
                                currentComment = StringBuilder()
                            }

                            // Add the key to our ordered set to maintain original order
                            keyOrder.add(key)
                        }
                    }
                }
                lineIndex++
            }

            // Now load the properties normally
            val tempStream = ByteArrayInputStream(lines.joinToString("\n").toByteArray())
            super.load(tempStream)
        } catch (e: IOException) {
            // If there's an error, still try to load properties the standard way
            super.load(inStream)
        }
    }

    /**
     * Stores the properties to the specified output stream, preserving order and comments.
     */
    @Synchronized
    override fun store(out: OutputStream, comments: String?) {
        try {
            val writer = BufferedWriter(OutputStreamWriter(out))

            // Write the header comment first (usually modification date)
            if (headerComment != null) {
                headerComment?.let { writer.write(it) }
                writer.newLine()
            } else if (!comments.isNullOrEmpty()) {
                // If no header comment was found but comments were provided, use those
                writer.write("# $comments")
                writer.newLine()
            }

            // Write properties in the original order with their comments
            for (key in keyOrder) {
                val keyStr = key.toString()
                // Skip if the property no longer exists
                val value = getProperty(keyStr) ?: continue

                // Write the comment for this key if it exists
                val comment = commentMap[keyStr]
                if (!comment.isNullOrEmpty()) {
                    writer.write(comment)
                    writer.newLine()
                }

                // Write the property
                writer.write("$keyStr=$value")
                writer.newLine()
            }

            // Write any properties that might not be in the keyOrder
            for (key in stringPropertyNames()) {
                if (!keyOrder.contains(key)) {
                    val value = getProperty(key)
                    writer.write("$key=$value")
                    writer.newLine()
                }
            }

            writer.flush()
        } catch (e: IOException) {
            // If there's an error, fall back to the standard implementation
            val tempOut = ByteArrayOutputStream()
            super.store(tempOut, comments)
            out.write(tempOut.toByteArray())
        }
    }

    /**
     * Sets a property and maintains the order.
     */
    @Synchronized
    override fun setProperty(key: String, value: String): Any? {
        if (!keyOrder.contains(key)) {
            keyOrder.add(key)
        }
        return super.setProperty(key, value)
    }

    /**
     * Removes a property and updates the order.
     */
    @Synchronized
    override fun remove(key: Any): Any? {
        keyOrder.remove(key)
        val keyStr = key.toString()
        commentMap.remove(keyStr)
        return super.remove(key)
    }

    /**
     * Clears all properties and comments.
     */
    @Synchronized
    override fun clear() {
        clearData()
        super.clear()
    }
}