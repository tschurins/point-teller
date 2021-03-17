package jal.pointscounter

import android.view.View

fun View.disable() {
    alpha = 0.5f
    setClickable(false)
}

fun View.enable() {
    alpha = 1f
    setClickable(true)
}