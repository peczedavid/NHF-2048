package peczedavid.nhf.animation

import peczedavid.nhf.model.Point

data class MovementInfo (
    var start: Point,
    var end: Point,
    var startValue: Int,
    var endValue: Int,
    var sum: Boolean = false
)