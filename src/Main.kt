import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.mouseClickable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlin.concurrent.timer
import kotlin.math.round
import kotlin.math.roundToLong


@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    var isOpen by remember { mutableStateOf(false) }
    var isChoosing by remember { mutableStateOf(true) }
    var isAskingForClose by remember { mutableStateOf(false) }
    var gridWidth by remember { mutableStateOf(20) }
    var gridHeight by remember { mutableStateOf(30) }
    var mines by remember { mutableStateOf(8) }
    var grid by remember { mutableStateOf(Grid(0, 0, emptySet())) }

    if (isChoosing) Window(
        onCloseRequest = ::exitApplication,
        title = "Choose the difficult",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        Column {
            gridWidth = numberSelection(gridWidth, "Height")
            gridHeight = numberSelection(gridHeight, "Width")
            mines = numberSelection(mines, "Mines")
            Button(onClick = {
                isOpen = true
                isChoosing = false
                grid = Grid(gridWidth, gridHeight, generateMines(mines, gridWidth, gridHeight))
            }, modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)) { Text("Start") }
        }
    }

    if (isOpen) {
        Window(
            onCloseRequest = { isAskingForClose = true },
            title = "Compose for Desktop",
            state = rememberWindowState(width = (25.5 * gridHeight).dp, height = (25 * gridWidth + 80).dp),
            resizable = false
        ) {
            val seconds = remember{ mutableStateOf(0) }
            var timer = timer(initialDelay = 1000L, period = 100L) {
                seconds.value++
                if (grid.isGameOver()) this.cancel()
            }
            MaterialTheme {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.LightGray,
                            border = ButtonDefaults.outlinedBorder,
                            shape = RectangleShape
                        ) { Text("${grid.minesLeft()}     :    ${seconds.value/10.0}s", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }
                    }
                    LazyColumn() {
                        items(grid.width) { x ->
                            LazyRow {
                                items(grid.height) { y ->
                                    val cellState = grid.field[x][y]
                                    val numberColors = listOf(
                                        Color.White,
                                        Color.Blue,
                                        Color.Green,
                                        Color.Red,
                                        Color(50, 50, 200),
                                        Color.Magenta,
                                        Color.Cyan,
                                        Color.Yellow,
                                        Color.LightGray,
                                        Color.Black
                                    )
                                    val value = grid.getValue(x, y)
                                    val isVisible = cellState == CellState.VISIBLE
                                    println("$x:$y is $value, $cellState")

                                    Surface(
                                        modifier = Modifier.size(25.dp).mouseClickable(onClick = {
                                            when {
                                                buttons.isPrimaryPressed -> grid = grid.action(x, y)
                                                buttons.isSecondaryPressed -> grid = grid.actionRMB(x, y)
                                            }
                                        }),
                                        color = if (isVisible) Color.White else Color.DarkGray,
                                        border = ButtonDefaults.outlinedBorder,
                                        shape = RectangleShape
                                    ) {
                                        val modifier = Modifier.padding(7.dp, 4.dp)
                                        when (cellState) {
                                            CellState.VISIBLE -> {
                                                if (value in 0..8)
                                                    Text("$value", color = numberColors[value], modifier = modifier)
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
                Menu("Actions") {
                    Item("Restart", onClick = {
                        isOpen = false
                        isChoosing = true
                    })
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