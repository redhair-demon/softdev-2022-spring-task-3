import kotlin.random.Random

class Grid(private val width: Int, private val height: Int, private val mines: Int) {
    var field: List<MutableList<CellState>> = (0 until width).map { (0 until height).map { CellState.HIDDEN }.toMutableList() }
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
            if (getValue(x, y) == 0) localGrid = actionArea(x, y)
            return localGrid
        }
        return this
    }

    fun actionSecondary(x: Int, y: Int): Grid {
        if (field[x][y] != CellState.VISIBLE) {
            val localGrid = Grid(width, height, mines, field, minesSet, isFirstAction)
            localGrid.field[x][y] = localGrid.field[x][y].actionSecondary()
            return localGrid
        }
        if (field[x][y] == CellState.VISIBLE && getValue(x, y) != 0 && getValue(x, y) == getFlags(x, y)) return actionArea(x, y)
        return this
    }

    fun actionArea(x: Int, y: Int): Grid {
        var localGrid = this
            (x - 1).rangeTo(x + 1).map { i ->
                (y - 1).rangeTo(y + 1).map { j ->
                    if (i in 0 until width && j in 0 until height) localGrid = localGrid.action(i, j)
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

    private fun getFlags(x: Int, y: Int): Int {
        return (x - 1).rangeTo(x + 1).sumOf { i ->
            (y - 1).rangeTo(y + 1).count { j ->
                if (i in 0 until width && j in 0 until height) field[i][j] == CellState.FLAG else false
            } }
    }

    fun minesLeft(): Int = minesSet.size - field.sumOf { line -> line.count { it == CellState.FLAG } }

    private fun getHidEdge(): Set<Cell> {
        val edge = mutableSetOf<Cell>()
        for (x in 0 until width)
            for (y in 0 until height)
                if (field[x][y] != CellState.VISIBLE &&
                    (x - 1).rangeTo(x + 1).any { i ->
                        (y - 1).rangeTo(y + 1).any { j ->
                            (i in 0 until width)&&(j in 0 until height)&&(field[i][j] == CellState.VISIBLE)
                        } })
                    edge += Cell(x, y)
        return edge
    }

    private fun getVisEdge(): Set<Cell> {
        val edge = mutableSetOf<Cell>()
        for (x in 0 until width)
            for (y in 0 until height)
                if (field[x][y] == CellState.VISIBLE && getValue(x, y) != 0) edge += Cell(x, y)
        return edge
    }

    fun isSolvable(): Boolean {
        val hidEdge = getHidEdge()
        val visEdge = getVisEdge()
        var hidState: Map<Cell, Boolean?> = hidEdge.associateWith { null }
        val solves = emptySet<Map<Cell, Boolean?>>().toMutableSet()
        if (hidEdge.all { it in minesSet && field[it.x][it.y] == CellState.FLAG }) return false
        hidState = solve(visEdge, hidState)
        if (false in hidState.values) return true
        for (cell in hidEdge) {
            var localState = hidState.toMutableMap()
            localState[cell] = true
            localState = solve(visEdge, localState).toMutableMap()
            if (visEdge.all { getValue(it.x, it.y) >= getCells(it.x, it.y, localState, true)
                        && getValue(it.x, it.y) <= getCells(it.x, it.y, localState, true) + getCells(it.x, it.y, localState, null) }
                && localState.values.count { it == true } <= this.mines) {
                solves += localState
            }
        }

//        println(solves)

        for (solve in solves) {
            for (cell in solve.keys) {
                var isCellSolved = true
                for (other in solves - solve) {
                    isCellSolved = solve[cell] == other[cell] && solve[cell] == false && isCellSolved
                }
                if (isCellSolved) {
                    println(cell)
                    return true
                }
            }
        }
        return false
    }

    private fun solve(visEdge: Set<Cell>, hidState: Map<Cell, Boolean?>): Map<Cell, Boolean?> {
        val localState = hidState.toMutableMap()
        for (times in 0 until width) {
            for (cell in visEdge) {
                val emptyCells = (cell.x - 1).rangeTo(cell.x + 1).sumOf { i ->
                    (cell.y - 1).rangeTo(cell.y + 1).count { j -> ((Cell(i, j) in localState.keys) && (localState[Cell(i, j)] == null)) }
                }

                if (getValue(cell.x, cell.y) - getCells(cell.x, cell.y, localState, true) == emptyCells) {
                    (cell.x - 1).rangeTo(cell.x + 1).forEach { i ->
                        (cell.y - 1).rangeTo(cell.y + 1).forEach { j ->
                            if (Cell(i, j) in localState.keys && localState[Cell(i, j)] == null) {
                                localState[Cell(i, j)] = true
                            }
                        }
                    }
                }
                if (getValue(cell.x, cell.y) == getCells(cell.x, cell.y, localState, true)) {
                    (cell.x - 1).rangeTo(cell.x + 1).forEach { i ->
                        (cell.y - 1).rangeTo(cell.y + 1).forEach { j ->
                            if (Cell(i, j) in localState.keys && localState[Cell(i, j)] == null) {
                                localState[Cell(i, j)] = false
                            }
                        }
                    }
                }
            }
        }
        return localState
    }

    private fun getCells(x: Int, y: Int, localState: MutableMap<Cell, Boolean?>, predicate: Boolean?): Int {
        return (x - 1).rangeTo(x + 1).sumOf { i ->
            (y - 1).rangeTo(y + 1).count { j ->
                if (Cell(i, j) in localState.keys) localState[Cell(i, j)] == predicate else false
            } }
    }

    fun gameOver(): Grid {
        var localGrid = this
        (0 until width).forEach { i -> (0 until height).forEach { j -> localGrid = localGrid.action(i, j) } }
        return localGrid
    }

    fun openCell(): Grid {
        for (cell in getHidEdge().shuffled()) {
            if (cell !in minesSet) return action(cell.x, cell.y)
        }
        var cell = Cell(Random.nextInt(width), Random.nextInt(height))
        while (cell in minesSet || field[cell.x][cell.y] == CellState.VISIBLE) cell = Cell(Random.nextInt(width), Random.nextInt(height))
        return action(cell.x, cell.y)
    }

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