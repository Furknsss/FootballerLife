package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.Display
import kotlin.math.abs
import kotlin.math.roundToInt

object FrameRateController {
    fun activity(context: Context): Activity? = when (context) {
        is Activity -> context
        is ContextWrapper -> activity(context.baseContext)
        else -> null
    }

    @Suppress("DEPRECATION")
    private fun display(activity: Activity): Display = activity.windowManager.defaultDisplay

    fun supportedRates(activity: Activity): List<Int> {
        val display = display(activity)
        val current = display.mode
        return display.supportedModes.filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
            .map { it.refreshRate.roundToInt() }.distinct().sorted()
    }

    fun apply(activity: Activity, requested: Int, window: android.view.Window = activity.window): Float {
        val display = display(activity)
        val current = display.mode
        val modes = display.supportedModes.filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
        val mode = if (requested == 0) modes.maxByOrNull { it.refreshRate } else modes.minByOrNull { abs(it.refreshRate - requested) }
        val target = mode?.refreshRate ?: display.refreshRate
        window.attributes = window.attributes.apply {
            preferredDisplayModeId = mode?.modeId ?: 0
            preferredRefreshRate = target
        }
        if (Build.VERSION.SDK_INT >= 35) window.decorView.requestedFrameRate = target
        return target
    }
}
