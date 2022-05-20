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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

val fontFamily = FontFamily.Monospace
fun main() = application {
    var isOpen by remember { mutableStateOf(false) }
    var isChoosing by remember { mutableStateOf(true) }
    var isAskingForClose by remember { mutableStateOf(false) }
    var isShowingRecords by remember { mutableStateOf(false) }
    var width by remember { mutableStateOf(10) }
    var height by remember { mutableStateOf(10) }
    var mines by remember { mutableStateOf(10) }
    val grid = remember { mutableStateOf(Grid(0, 0, 0)) }
    var seconds by remember { mutableStateOf(0) }

    val timer = Timer()
    var timerTask = timerTask {}
    val numberColors = listOf(
        Color.White,
        Color.Blue,
        Color(0, 200, 0),
        Color.Red,
        Color(50, 50, 200),
        Color.Magenta,
        Color.Cyan,
        Color.Black,
        Color.LightGray
    )

    /* Options Window */
    if (isChoosing) Window(
        onCloseRequest = ::exitApplication,
        title = "Game Options",
        state = rememberWindowState(width = 300.dp, height = 350.dp)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Surface(modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp)) {
                Text("Welcome to Minesweeper", fontSize = 20.sp, fontFamily = fontFamily)
            }
            width = numberSelection(width, "Height")
            height = numberSelection(height, "Width")
            mines = numberSelection(mines, "Mines")
            Button(onClick = {
                if (width * height > mines) {
                    isOpen = true
                    isChoosing = false
                    grid.value = Grid(width, height, mines)
                    seconds = 0
                    timerTask = timerTask { if(!grid.value.isFirstAction && !grid.value.isGameOver()) seconds++ }
                    timer.scheduleAtFixedRate(timerTask, 0, 100)
                }
            }, modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
            ) { Text("START", fontFamily = fontFamily) }
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
                            "${if (!grid.value.isFirstAction) grid.value.minesLeft() else mines}    ${seconds / 10.0}",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold)
                        }
                    }
                    val vState = rememberLazyListState()
                    val hState = rememberScrollState()
                    Box(modifier = Modifier.fillMaxSize()){
                        LazyColumn(state = vState, modifier = Modifier.align(Alignment.Center)) {
                            items(width) { x ->
                                Row(modifier = Modifier.horizontalScroll(hState)) {
                                    (0 until height).forEach { y ->
                                        cellSurface(grid, x, y, numberColors)
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

            MenuBar {
                Menu("Game") {
                    Item("New Game", onClick = {
                        isOpen = false
                        isChoosing = true
                    })
                    Item("Is Not Solvable", onClick = {
                        grid.value = if (grid.value.isSolvable()) {
                            grid.value.gameOver()
                        } else {
                            grid.value.openCell()
                        }
                    }, enabled = !grid.value.isFirstAction)
                    Item("End Game", onClick = {
                        grid.value = grid.value.gameOver()
                    })
                }
                Menu("Saves") {
                    Item("Clear Records") {
                        File("records.txt").writeText("")
                    }
                    Item("See Records", enabled = File("records.txt").exists()) {
                        isShowingRecords = true
                    }
                }
            }
        }

        /* Game Over Window */
        if (grid.value.isGameOver()) {
            grid.value = grid.value.gameOver()
            timerTask.cancel()
            timer.cancel()
            Dialog(
                onCloseRequest = { isOpen = false },
                title = "Game Over",
                state = rememberDialogState(size = DpSize(200.dp, 200.dp)),
                resizable = false
            ) {
                MaterialTheme {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp),
                            text = if (grid.value.isWin()) {
                                writeRecord(width, height, mines, seconds)
                                "Winner!"
                            } else "Loser!",
                            fontFamily = fontFamily,
                            fontSize = 20.sp
                        )
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = "Field: $height * $width \nMines left: ${grid.value.minesLeft()}/$mines\nTime: ${seconds / 10.0}",
                            fontFamily = fontFamily
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
    /* Record List */
    if (isShowingRecords) {
        Window(
            onCloseRequest = { isShowingRecords = false },
            title = "Records",
            state = rememberWindowState(width = 300.dp, height = 400.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Your Best Scores",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp),
                    fontFamily = fontFamily,
                    fontSize = 20.sp
                )
                for (line in File("records.txt").readLines()) {
                    Surface(border = ButtonDefaults.outlinedBorder) {
                        Text(line)
                    }
                }
            }
        }
    }

    /* Close Confirmation */
    if (isAskingForClose) {
        Dialog(
            onCloseRequest = { isAskingForClose = false },
            title = "Close the window?",
            state = rememberDialogState(width = 200.dp, height = 150.dp)
        ) {
            Column {
                Text("You are going to close the window", fontFamily = fontFamily, modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = {
                        isOpen = false
                        isAskingForClose = false
                    }) { Text("Yes", fontFamily = fontFamily) }
                    Spacer(modifier = Modifier.fillMaxSize(0.2f))
                    Button(onClick = { isAskingForClose = false }) { Text("No", fontFamily = fontFamily) }
                }
            }
        }
    }

}

fun writeRecord(width: Int, height: Int, mines: Int, seconds: Int) {
    File("records.txt").appendText("Field ($width*$height)," +
            " Mines ($mines), Time (${seconds / 10.0})," +
            " Date (${Date(System.currentTimeMillis())})${System.lineSeparator()}")
}

@Composable
fun numberSelection(number: Int, text: String): Int {
    var count by remember { mutableStateOf(number) }
    Row {
        Button(
            onClick = { if (count > 1) count-- },
            modifier = Modifier.size(35.dp, 55.dp),
            contentPadding = PaddingValues()
        ) { Text("-") }
        Column(Modifier.weight(1f, true)) {
            TextField("$count",
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {count = try {
                if (it.toInt() > 0) it.toInt() else count
            } catch (e: NumberFormatException) {count}
            })
            Text(text, Modifier.align((Alignment.CenterHorizontally)))
        }
        Button(
            onClick = { count++ },
            modifier = Modifier.size(35.dp, 55.dp),
            contentPadding = PaddingValues()
        ) { Text("+") }
    }
    return count
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun cellSurface(grid: MutableState<Grid>, x: Int, y: Int, numberColors: List<Color>) {
    val cellState = grid.value.field[x][y]
    val value = grid.value.getValue(x, y)
    val isVisible = cellState == CellState.VISIBLE

    Surface(
        modifier = Modifier.size(25.dp).mouseClickable(onClick = {
            if (!grid.value.isGameOver()) {
                when {
                    buttons.isPrimaryPressed -> grid.value = grid.value.action(x, y)
                    buttons.isSecondaryPressed -> grid.value = grid.value.actionSecondary(x, y)
                }
            }
        }),
        color = if (isVisible) if (value in 0..8) Color.White else Color.Red else Color.DarkGray,
        border = ButtonDefaults.outlinedBorder,
        shape = RectangleShape
    ) {
        cellText(cellState, value, numberColors)
    }
}

@Composable
fun cellText(cellState: CellState, value: Int, numberColors: List<Color>) {
    when (cellState) {
        CellState.VISIBLE -> {
            if (value in 0..8)
                Text(
                    text = "$value",
                    color = numberColors[value],
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
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