package com.imcys.ktwordle.model.view

import android.text.TextUtils.split
import com.imcys.ktwordle.R
import com.imcys.ktwordle.app.App
import com.imcys.ktwordle.info.UiIntent
import com.imcys.ktwordle.info.UiState
import com.imcys.ktwordle.model.Grid
import com.imcys.ktwordle.model.GridStateEnum
import com.imcys.ktwordle.model.KeyGrid


sealed interface HomeIntent : UiIntent {
    object InitGrid : HomeIntent
    data class InputLetter(val letter: String) : HomeIntent
    object InputEnter : HomeIntent
    object InputDelete : HomeIntent

}


data class HomeViewState(
    var correctWords: String = "APPLE",
    var gridList: MutableList<MutableList<Grid>> = mutableListOf(),
    val thisRow: Int = 0,
    val thisCol: Int = 0,
    //0正常，1成功，2失败
    var gameState: Int = 0,
    var keyGrid: MutableList<KeyGrid> = mutableListOf<KeyGrid>(
        KeyGrid("Q"),
        KeyGrid("W"),
        KeyGrid("E"),
        KeyGrid("R"),
        KeyGrid("T"),
        KeyGrid("Y"),
        KeyGrid("U"),
        KeyGrid("I"),
        KeyGrid("O"),
        KeyGrid("P"),
        KeyGrid("A"),
        KeyGrid("S"),
        KeyGrid("D"),
        KeyGrid("F"),
        KeyGrid("G"),
        KeyGrid("H"),
        KeyGrid("J"),
        KeyGrid("K"),
        KeyGrid("L"),
        KeyGrid("Z"),
        KeyGrid("X"),
        KeyGrid("C"),
        KeyGrid("V"),
        KeyGrid("B"),
        KeyGrid("N"),
        KeyGrid("M"),
    )
) : UiState


class MainViewModel : ComposeBaseViewModel<HomeViewState, HomeIntent>(HomeViewState()) {

    //
    val rowCount = 6
    val colCount = 5


    override fun handleEvent(event: HomeIntent, state: HomeViewState) {
        when (event) {
            is HomeIntent.InitGrid -> initGrid()
            is HomeIntent.InputLetter -> inputLetter(event.letter)
            is HomeIntent.InputEnter -> inputEnter()
            is HomeIntent.InputDelete -> inputDelete()

        }

    }

    private fun inputDelete() {

        if (viewStates.thisCol == 0) return

        val gridList: MutableList<MutableList<Grid>> =
            viewStates.gridList.map { it.toMutableList() }.toMutableList()


        gridList[viewStates.thisRow][viewStates.thisCol - 1] =
            gridList[viewStates.thisRow][viewStates.thisCol - 1].copy(
                letter = "",
            )

        viewStates = viewStates.copy(
            gridList = gridList,
        )

        viewStates =
            viewStates.copy(thisCol = if (viewStates.thisCol == 0) 0 else viewStates.thisCol - 1)


    }

    private fun inputEnter() {
        if (viewStates.thisCol <= 4) return

        val gridList: MutableList<MutableList<Grid>> =
            viewStates.gridList.map { it.toMutableList() }.toMutableList()


        //检查是否合情合理
        val sysLetter = viewStates.correctWords.toCharArray()

        viewStates.gridList[viewStates.thisRow].forEachIndexed { index, grid ->
            var state = false
            sysLetter.forEachIndexed fe@{ sysIndex, letterChar ->
                if (letterChar.toString() == grid.letter) {
                    if (sysIndex == index) {
                        gridList[viewStates.thisRow][index] =
                            gridList[viewStates.thisRow][index].copy(state = GridStateEnum.CORRECT)
                        state = true

                        //更新键盘
                        viewStates.keyGrid.forEachIndexed { keyIndex, keyGrid ->
                            if (grid.letter == keyGrid.content) {
                                viewStates.keyGrid[keyIndex] =
                                    viewStates.keyGrid[keyIndex].copy(stateEnum = GridStateEnum.CORRECT)
                            }
                        }

                        return@forEachIndexed

                    } else {
                        gridList[viewStates.thisRow][index] =
                            gridList[viewStates.thisRow][index].copy(state = GridStateEnum.WRONG_POSITION)
                        state = true

                        //更新键盘
                        viewStates.keyGrid.forEachIndexed { keyIndex, keyGrid ->
                            if (grid.letter == keyGrid.content) {
                                viewStates.keyGrid[keyIndex] =
                                    viewStates.keyGrid[keyIndex].copy(stateEnum = GridStateEnum.WRONG_POSITION)
                            }
                        }

                    }
                }
            }
            if (!state) {
                gridList[viewStates.thisRow][index] =
                    gridList[viewStates.thisRow][index].copy(state = GridStateEnum.WRONG)

                //更新键盘
                viewStates.keyGrid.forEachIndexed { keyIndex, keyGrid ->
                    if (grid.letter == keyGrid.content) {
                        viewStates.keyGrid[keyIndex] =
                            viewStates.keyGrid[keyIndex].copy(stateEnum = GridStateEnum.WRONG)
                    }
                }
            }
        }

        viewStates = viewStates.copy(thisCol = 0)

        //正确的Num
        var zqNum = 0
        viewStates.gridList[viewStates.thisRow].forEach {
            if (it.state == GridStateEnum.CORRECT) zqNum++
        }

        if (zqNum == 5) {
            viewStates = viewStates.copy(gameState = 1)
            return
        }

        if (viewStates.thisCol >= 5) return

        viewStates = viewStates.copy(thisCol = 0, thisRow = viewStates.thisRow + 1)
        viewStates = viewStates.copy(gridList = gridList)

    }

    private fun inputLetter(letter: String) {


        if (viewStates.thisCol > 4) return

        val gridList: MutableList<MutableList<Grid>> =
            viewStates.gridList.map { it.toMutableList() }.toMutableList()

        gridList[viewStates.thisRow][viewStates.thisCol] =
            gridList[viewStates.thisRow][viewStates.thisCol].copy(
                letter = letter
            )

        viewStates = viewStates.copy(gridList = gridList, thisCol = viewStates.thisCol + 1)


    }

    private fun initGrid() {

        val gridList: MutableList<MutableList<Grid>> = mutableListOf()

        repeat(rowCount) { row ->
            val colList = mutableListOf<Grid>()
            repeat(colCount) { col ->
                colList.add(Grid(x = col, y = row))
            }
            gridList.add(colList)
        }

        val newKeyGrid =
            viewStates.keyGrid.map {
                it.stateEnum = GridStateEnum.EMPTY
                it
            }.toMutableList()

        viewStates = viewStates.copy(gridList = gridList, keyGrid = newKeyGrid)

        val wordleList = App.wordles.split(" ")
        val wordleIndex = wordleList.indices.random()
        viewStates = viewStates.copy(
            correctWords = wordleList[wordleIndex],
            gameState = 0,
            thisRow = 0,
            thisCol = 0
        )


    }


}