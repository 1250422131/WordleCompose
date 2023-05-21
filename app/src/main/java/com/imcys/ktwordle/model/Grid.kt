package com.imcys.ktwordle.model

data class Grid(
    var x: Int? = null,
    var y: Int? = null,
    var letter: String = "",
    var state: GridStateEnum = GridStateEnum.EMPTY,
)

