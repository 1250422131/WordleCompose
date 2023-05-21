package com.imcys.ktwordle.info

interface IViewModelHandle<S : UiState, I : UiIntent> {

    fun handleEvent(event: I, state: S)

}