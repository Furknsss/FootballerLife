package com.example.engine

/** Rendering interpolation never changes simulation time or match outcomes. */
object MatchRenderInterpolation {
    fun coordinate(from: Float, to: Float, progress: Float): Float = from + (to - from) * progress.coerceIn(0f, 1f)
    const val SIMULATION_STEP_MS = 55
}
