package com.shaban.onliner.util

import java.time.Duration

class DurationUtils {
    companion object {
        fun toPrettyString(duration: Duration): String = toPrettyString(duration.toMillis())

        fun toPrettyString(duration: Long): String {
            if (duration == 0L) {
                return "0s"
            }

            val msPerHour = 1000 * 60 * 60
            val msPerMin = 1000 * 60
            val msPerSec = 1000

            val hours: Long = duration / msPerHour
            val minutes = (duration % msPerHour / msPerMin)
            val secs = (duration % msPerMin / msPerSec)
            val ms = (duration % msPerSec)
            val buf = StringBuilder()

            appendValue(buf, hours, "h")
            appendValue(buf, minutes, "m")
            appendValue(buf, secs, "s")
            appendValue(buf, ms, "ms")

            return buf.toString()
        }

        private fun appendValue(buf: StringBuilder, value: Long, letter: String) {
            if (value != 0L) {
                if (buf.isNotEmpty()) {
                    buf.append(' ')
                }
                buf.append(value).append(letter)
            }
        }
    }
}