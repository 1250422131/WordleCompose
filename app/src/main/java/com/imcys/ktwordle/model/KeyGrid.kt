package com.imcys.ktwordle.model

data class KeyGrid(
    val content: String,
    var stateEnum: GridStateEnum = GridStateEnum.EMPTY
)