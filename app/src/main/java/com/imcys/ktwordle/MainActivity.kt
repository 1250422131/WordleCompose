package com.imcys.ktwordle

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.imcys.ktwordle.model.Grid
import com.imcys.ktwordle.model.GridStateEnum
import com.imcys.ktwordle.model.view.HomeIntent
import com.imcys.ktwordle.model.view.HomeViewState
import com.imcys.ktwordle.model.view.MainViewModel
import com.imcys.ktwordle.ui.theme.WordleComposeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

class MainActivity : ComponentActivity() {
    private lateinit var mCoroutineScope: CoroutineScope
    private var mainViewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordleComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    LaunchedEffect(Unit) {
                        mainViewModel.sendIntent(HomeIntent.InitGrid)
                    }
                    //全局协程
                    mCoroutineScope = rememberCoroutineScope()
                    HomeView()
                }
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HomeView() {
        //顶部导航高度
        val appBarHeight = remember { mutableStateOf(0) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    modifier = Modifier
                        .statusBarsPadding()
                        .onSizeChanged { appBarHeight.value = it.height },
                    title = {
                        Text(text = "KTWordle")
                    })
            },


            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                      mCoroutineScope.launch {
                          mainViewModel.sendIntent(HomeIntent.InitGrid)
                      }
                    },
                    icon = { Icon(Icons.Filled.Refresh, "Localized Description") },
                    text = { Text(text = "再来一次？") },
                )
            }
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, appBarHeight.value)
                    }
                }
            ) {
                Column(modifier = Modifier) {
                    //设置主要内容
                    HomeContent()
                }
            }


        }


    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun HomeContent() {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(20.dp)
            ) {

                mainViewModel.viewStates.gridList.forEach { grids ->
                    grids.forEach {
                        item {
                            MGrid(it)
                        }
                    }
                }


            }


            Column(Modifier.fillMaxSize()) {
                MKeyboardGrid()
            }


        }


    }


    @Composable
    private fun initGameOverTsp() {
        if (mainViewModel.viewStates.gameState == 1) {
            AlertDialog(
                onDismissRequest = {
                    mCoroutineScope.launch {
                        mainViewModel.sendIntent(HomeIntent.InitGrid)
                    }
                },
                title = {
                    Text(text = "奈斯！！！")
                },

                text = {
                    Text(text = "游戏成功")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mCoroutineScope.launch {
                                mainViewModel.sendIntent(HomeIntent.InitGrid)
                            }
                        }
                    ) {
                        Text("确定")
                    }
                },
            )
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    @SuppressLint("MutableCollectionMutableState")
    @Composable
    private fun MKeyboardGrid() {


        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {

            (0..9).forEach {
                Spacer(Modifier.width(2.dp))
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = when (mainViewModel.viewStates.keyGrid[it].stateEnum) {
                        GridStateEnum.WRONG -> Color(0xFF424242)
                        GridStateEnum.CORRECT -> Color(0xff98c25c)
                        GridStateEnum.WRONG_POSITION -> Color(0xffffd95c)
                        else -> {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    },
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            mainViewModel.sendIntent(HomeIntent.InputLetter(mainViewModel.viewStates.keyGrid[it].content))
                        },
                    ),
                ) {

                    Column(modifier = Modifier.padding(7.dp)) {
                        Text(
                            text = mainViewModel.viewStates.keyGrid[it].content,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (mainViewModel.viewStates.keyGrid[it].stateEnum == GridStateEnum.EMPTY) Color.Black else Color.White

                        )
                    }
                }
                Spacer(Modifier.width(2.dp))
            }


        }

        Spacer(Modifier.height(5.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {

            (10..18).forEach {
                Spacer(Modifier.width(2.dp))
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = when (mainViewModel.viewStates.keyGrid[it].stateEnum) {
                        GridStateEnum.WRONG -> Color(0xFF424242)
                        GridStateEnum.CORRECT -> Color(0xff98c25c)
                        GridStateEnum.WRONG_POSITION -> Color(0xffffd95c)
                        else -> {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    },
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            mainViewModel.sendIntent(HomeIntent.InputLetter(mainViewModel.viewStates.keyGrid[it].content))
                        },
                    ),
                ) {

                    Column(modifier = Modifier.padding(7.dp)) {
                        Text(
                            text = mainViewModel.viewStates.keyGrid[it].content,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (mainViewModel.viewStates.keyGrid[it].stateEnum == GridStateEnum.EMPTY) Color.Black else Color.White

                        )
                    }
                }
                Spacer(Modifier.width(2.dp))
            }


        }

        Spacer(Modifier.height(5.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.width(2.dp))
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.combinedClickable(
                    onClick = {
                        mainViewModel.sendIntent(HomeIntent.InputEnter)
                    },
                ),
            ) {
                Column(modifier = Modifier.padding(7.dp)) {
                    Text(text = "Enter", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }

            (19..25).forEach {

                Spacer(Modifier.width(2.dp))
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = when (mainViewModel.viewStates.keyGrid[it].stateEnum) {
                        GridStateEnum.WRONG -> Color(0xFF424242)
                        GridStateEnum.CORRECT -> Color(0xff98c25c)
                        GridStateEnum.WRONG_POSITION -> Color(0xffffd95c)
                        else -> {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    },
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            mainViewModel.sendIntent(HomeIntent.InputLetter(mainViewModel.viewStates.keyGrid[it].content))
                        },
                    ),
                ) {

                    Column(modifier = Modifier.padding(7.dp)) {
                        Text(
                            text = mainViewModel.viewStates.keyGrid[it].content,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (mainViewModel.viewStates.keyGrid[it].stateEnum == GridStateEnum.EMPTY) Color.Black else Color.White
                        )
                    }
                }
                Spacer(Modifier.width(2.dp))
            }

            Surface(
                shape = RoundedCornerShape(3.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.combinedClickable(
                    onClick = {
                        mainViewModel.sendIntent(HomeIntent.InputDelete)
                    },
                ),
            ) {
                Column(modifier = Modifier.padding(7.dp)) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "退格")
                }
            }


        }


    }

    @OptIn(ExperimentalFoundationApi::class)
    private
    @Composable
    fun MGrid(it: Grid) {

        Surface(
            shape = RoundedCornerShape(5.dp),
            color = when (it.state) {
                GridStateEnum.WRONG -> Color(0xFF424242)
                GridStateEnum.CORRECT -> Color(0xff98c25c)
                GridStateEnum.WRONG_POSITION -> Color(0xffffd95c)
                else -> {
                    MaterialTheme.colorScheme.background
                }
            },
            modifier = Modifier
                .run {
                    if (it.state == GridStateEnum.EMPTY) {
                        border(
                            width = 2.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(5.dp),
                        )
                    } else this
                }
                .fillMaxSize()
                .aspectRatio(1f)
                .combinedClickable(
                    onClick = {
                    },
                    onDoubleClick = {
                        // 双击
                    },
                    onLongClick = {
                        // 长按
                    }
                ),

            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = it.letter, fontWeight = FontWeight.Bold, fontSize = 25.sp,
                    color = if (it.state == GridStateEnum.EMPTY) Color.Black else Color.White
                )
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        WordleComposeTheme {

            mainViewModel.sendIntent(HomeIntent.InitGrid)

            //全局协程
            mCoroutineScope = rememberCoroutineScope()
            HomeView()
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
