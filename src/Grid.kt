import kotlin.random.Random

class Grid(val width: Int, val height: Int, private val mines: Int) {
    var field: List<MutableList<CellState>> = 0.rangeTo(width).map { 0.rangeTo(height).map { CellState.HIDDEN }.toMutableList() }
    var minesSet: Set<Cell> = setOf(Cell(0, 0))
    var isFirstAction = true

    constructor(width: Int, height: Int, mines: Int, field: List<MutableList<CellState>>, minesSet: Set<Cell>, isFirstAction: Boolean)
            : this(width, height, mines) {
        this.field = field
        this.minesSet = minesSet
        this.isFirstAction = isFirstAction
            }

    fun action(x: Int, y: Int): Grid {
        if (isFirstAction) {
            isFirstAction = false
            var localMines = setOf(Cell(x, y))
            while (Cell(x, y) in localMines) localMines = generateMines()
            minesSet = localMines
        }
        if (field[x][y] == CellState.HIDDEN) {
            var localGrid = Grid(width, height, mines, field, minesSet, isFirstAction)
            localGrid.field[x][y] = localGrid.field[x][y].action()
            println("action $x:$y is ${this.field[x][y]}")
            if (getValue(x, y) == 0) localGrid = actionArea(x, y)
            return localGrid
        }
        return this
    }

    fun actionSecondary(x: Int, y: Int): Grid {
        if (field[x][y] != CellState.VISIBLE) {
            val localGrid = Grid(width, height, mines, field, minesSet, isFirstAction)
            localGrid.field[x][y] = localGrid.field[x][y].actionSecondary()
            println("actionSecondary -> $x:$y is now ${this.field[x][y]}")
            return localGrid
        }
        if (field[x][y] == CellState.VISIBLE && getValue(x, y) != 0 &&
            getValue(x, y) == (x - 1).rangeTo(x + 1).sumOf { i ->
                (y - 1).rangeTo(y + 1).count { j ->
                    if (i in 0..width && j in 0..height) field[i][j] == CellState.FLAG else false
                }
            }) return actionArea(x, y)
        return this
    }

    fun actionArea(x: Int, y: Int): Grid {
        var localGrid = this
            (x - 1).rangeTo(x + 1).map { i ->
                (y - 1).rangeTo(y + 1).map { j ->
                    if (i in 0..width && j in 0..height) localGrid = localGrid.action(i, j)
                }
            }
        return localGrid
    }

    fun isWin(): Boolean =
        minesSet.all { field[it.x][it.y] == CellState.FLAG } && field.sumOf { line -> line.count { it == CellState.FLAG } } == minesSet.size

    fun isGameOver(): Boolean = isWin() || minesSet.any { field[it.x][it.y] == CellState.VISIBLE }

    fun getValue(x: Int, y: Int): Int {
        return if (minesSet.contains(Cell(x, y))) 9
        else (x - 1).rangeTo(x + 1).sumOf { i -> (y - 1).rangeTo(y + 1).count { j -> minesSet.contains(Cell(i, j)) } }
    }

    fun minesLeft(): Int = minesSet.size - field.sumOf { line -> line.count { it == CellState.FLAG } }

    private fun generateMines(): Set<Cell> {
        val result = mutableSetOf<Cell>()
        for (i in 0 until mines) {
            var cell = Cell(Random.nextInt(width), Random.nextInt(height))
            while (cell in result) cell = Cell(Random.nextInt(width), Random.nextInt(height))
            result += cell
        }
        return result
    }
}