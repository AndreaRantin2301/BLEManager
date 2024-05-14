package com.andrearantin.blemanager.utils

import android.util.Log

/**
 * Utility class for logging. Useful if you want to do Unit tests
 * @property logsEnabled boolean to enable or disable logs for the entire BLEManager module
 */
object BLEManagerLogger {

    var logsEnabled = true

    fun d(tag: String?, msg: String): Int {
        if (logsEnabled) {
            Log.d(tag,msg)
        }
        return 0
    }

    fun i(tag: String?, msg: String): Int {
        if (logsEnabled) {
            Log.i(tag,msg)
        }
        return 0
    }

    fun w(tag: String?, msg: String): Int {
        if (logsEnabled) {
            Log.w(tag,msg)
        }
        return 0
    }

    fun e(tag: String?, msg: String): Int {
        if (logsEnabled) {
            Log.e(tag,msg)
        }
        return 0
    }
}