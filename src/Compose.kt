import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask


@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    var isOpen by remember { mutableStateOf(false) }
    var isChoosing by remember { mutableStateOf(true) }
    var isAskingForClose by remember { mutableStateOf(false) }
    var width by remember { mutableStateOf(10) }
    var height by remember { mutableStateOf(10) }
    var mines by remember { mutableStateOf(10) }
    var grid by remember { mutableStateOf(Grid(0, 0, 0)) }
    var seconds by remember { mutableStateOf(0) }

    val timer = Timer()
    val numberColors = listOf(
        Color.White,
        Color.Blue,
        Color.Green,
        Color.Red,
        Color(50, 50, 200),
        Color.Magenta,
        Color.Cyan,
        Color.Black,
        Color.LightGray
    )

    if (isChoosing) Window(
        onCloseRequest = ::exitApplication,
        title = "Choose the difficult",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        Column {
            width = numberSelection(width, "Height")
            height = numberSelection(height, "Width")
            mines = numberSelection(mines, "Mines")
            Button(onClick = {
                if (width * height > mines) {
                    isOpen = true
                    isChoosing = false
                    grid = Grid(width, height, mines)
                    seconds = 0
                    timer.scheduleAtFixedRate(timerTask { if(!grid.isFirstAction && !grid.isGameOver()) seconds++ }, 0, 100)
                }
            }, modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)) { Text("Start") }
        }
    }

    if (isOpen) {
        Window(
            onCloseRequest = { isAskingForClose = true },
            title = "Minesweeper for Desktop",
            state = rememberWindowState(width = (height * 30).dp, height = ((width + 1) * 30).dp)
        ) {
            Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.LightGray,
                            border = ButtonDefaults.outlinedBorder,
                            shape = RectangleShape
                        ) { Text(
                            "${grid.minesLeft()}    ${seconds / 10.0}",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold)
                        }
                    }
                    val vState = rememberLazyListState()
                    val hState = rememberScrollState()
                    Box(modifier = Modifier.fillMaxSize()){
                        LazyColumn(state = vState, modifier = Modifier.align(Alignment.Center)) {
                            items(grid.width) { x ->
                                Row(modifier = Modifier.horizontalScroll(hState)) {
                                    (0 until grid.height).forEach { y ->
                                        val cellState = grid.field[x][y]
                                        val value = grid.getValue(x, y)
                                        val isVisible = cellState == CellState.VISIBLE
                                        println("$x:$y is $value, $cellState")

                                        Surface(
                                            modifier = Modifier.size(25.dp).mouseClickable(onClick = {
                                                if (!grid.isGameOver()) {
                                                    when {
                                                        buttons.isPrimaryPressed -> grid = grid.action(x, y)
                                                        buttons.isSecondaryPressed -> grid = grid.actionSecondary(x, y)
                                                    }
                                                }
                                            }),
                                            color = if (isVisible) if (value in 0..8) Color.White else Color.Red else Color.DarkGray,
                                            border = ButtonDefaults.outlinedBorder,
                                            shape = RectangleShape
                                        ) {
                                            when (cellState) {
                                                CellState.VISIBLE -> {
                                                    if (value in 0..8)
                                                        Text(
                                                            text = "$value",
                                                            color = numberColors[value],
                                                            modifier = Modifier.padding(7.dp, 4.dp)
                                                        )
                                                    else
                                                        Text(
                                                            "💣",
                                                            color = Color.Black,
                                                            modifier = Modifier.padding(2.dp, 0.dp)
                                                        )
                                                }
                                                CellState.FLAG -> Text(
                                                    "⚑",
                                                    color = Color.Red,
                                                    modifier = Modifier.padding(7.dp, 2.dp)
                                                )
                                                else -> Text("")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(vState)
                        )
                        HorizontalScrollbar(
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                            adapter = rememberScrollbarAdapter(hState)
                        )
                    }
                }

            if (isAskingForClose) {
                Dialog(
                    onCloseRequest = { isAskingForClose = false },
                    title = "Close the window?",
                    state = rememberDialogState(width = 200.dp, height = 100.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                        Button(onClick = { isOpen = false }) { Text("Yes") }
                        Spacer(modifier = Modifier.fillMaxSize(0.2f))
                        Button(onClick = { isAskingForClose = false }) { Text("No") }
                    }
                }
            }
            MenuBar {
                Menu("Game") {
                    Item("New Game", onClick = {
                        isOpen = false
                        isChoosing = true
                    })
                }
                Menu("Saves") {
                    Item("Clear Records") {
                        File("records.txt").writeText("")
                    }
                }
            }
        }

        if (grid.isGameOver()) {
            timer.cancel()
            Dialog(
                onCloseRequest = { isOpen = false },
                title = "Game Over",
                state = rememberDialogState(size = DpSize(300.dp, 150.dp)),
                resizable = false
            ) {
                MaterialTheme {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = if (grid.isWin()) {
                                File("records.txt").appendText("FIELD ($width * $height), MINES ($mines), TIME (${seconds / 10.0}), DATE (${Date(System.currentTimeMillis())})${System.lineSeparator()}")
                                "Winner!"
                            } else "Loser!"
                        )
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = "$width X $height - ${grid.minesLeft()}/$mines mines left - time: ${seconds / 10.0}"
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                isOpen = false
                                isChoosing = true
                            }
                        ) { Text("Restart") }
                    }
                }
            }
        }
    }
}

@Composable
fun numberSelection(number: Int, text: String): Int {
    var count by remember { mutableStateOf(number) }
    Row {
        Button(
            onClick = { count-- },
            modifier = Modifier.size(35.dp),
            contentPadding = PaddingValues()
        ) { Text("-") }
        Column(Modifier.weight(1f, true)) {
            TextField("$count", onValueChange = {count = try {
                it.toInt()
            } catch (e: NumberFormatException) {count}
            })
            Text(text, Modifier.align((Alignment.CenterHorizontally)))
        }
        Button(
            onClick = { count++ },
            modifier = Modifier.size(35.dp),
            contentPadding = PaddingValues()
        ) { Text("+") }
    }
    return count
}