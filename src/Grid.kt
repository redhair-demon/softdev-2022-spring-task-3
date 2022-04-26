import kotlin.random.Random

class Grid(val width: Int, val height: Int, private val mines: Set<Cell>,
           val field: List<List<CellState>> = 0.rangeTo(width).map { 0.rangeTo(height).map { CellState.HIDDEN } }) {


    fun action(x: Int, y: Int): Grid {
//        if (field[x][y] != CellState.HIDDEN) return this
        if (field[x][y] == CellState.HIDDEN) {
            val localField = field.map { it.toMutableList() }
            localField[x][y] = CellState.VISIBLE
            var localGrid = Grid(width, height, mines, localField)
            if (getValue(x, y) == 0)
                (x - 1).rangeTo(x + 1).map { i ->
                    (y - 1).rangeTo(y + 1).map { j ->
                        if (i in 0..width && j in 0..height) localGrid = localGrid.action(i, j)
                    }
                }
            println("action $x:$y is ${localGrid.field[x][y]}")
            return localGrid
        }
        return this
    }

    fun actionSecondary(x: Int, y: Int): Grid {
        if (field[x][y] != CellState.VISIBLE) {
            val localField = field.map { it.toMutableList() }
            localField[x][y] = localField[x][y].action()
            println("actionRMB $x:$y is now ${localField[x][y]}")
            return Grid(width, height, mines, localField)
        }
        return this
    }

    fun isWin(): Boolean =
        mines.all { field[it.x][it.y] == CellState.FLAG } && field.sumOf { line -> line.count { it == CellState.FLAG } } == mines.size

    fun isGameOver(): Boolean = isWin() || mines.any { field[it.x][it.y] == CellState.VISIBLE }

    fun getValue(x: Int, y: Int): Int {
        return if (mines.contains(Cell(x, y))) 9
        else (x - 1).rangeTo(x + 1).sumOf { i -> (y - 1).rangeTo(y + 1).count { j -> mines.contains(Cell(i, j)) } }
    }

    fun minesLeft(): Int = mines.size - field.sumOf { line -> line.count { it == CellState.FLAG } }

}

fun generateMines(number: Int, width: Int, height: Int): Set<Cell> {
    val result = emptySet<Cell>().toMutableSet()
    for (i in 0..number) result += Cell(Random.nextInt(width), Random.nextInt(height))
    return result
}