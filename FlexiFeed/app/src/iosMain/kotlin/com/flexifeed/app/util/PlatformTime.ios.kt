package com.flexifeed.app.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun formatTimestamp(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "HH:mm:ss.SSS"
    }
    return formatter.stringFromDate(NSDate())
}

actual val platformEnvironment: String = "DEV"
