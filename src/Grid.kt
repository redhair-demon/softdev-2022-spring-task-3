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

    fun openCell(x: Int, y: Int): Grid {
        if (isFirstAction) {
            isFirstAction = false
            var localMines = setOf(Cell(x, y))
            while (Cell(x, y) in localMines) localMines = generateMines()
            minesSet = localMines
        }
        if (field[x][y] == CellState.HIDDEN) {
            var localGrid = Grid(width, height, mines, field, minesSet, isFirstAction)
            localGrid.field[x][y] = localGrid.field[x][y].openCell()
            if (getValue(x, y) == 0) localGrid = openAroundCells(x, y)
            return localGrid
        }
        return this
    }

    fun setFlag(x: Int, y: Int): Grid {
        if (field[x][y] != CellState.VISIBLE) {
            val localGrid = Grid(width, height, mines, field, minesSet, isFirstAction)
            localGrid.field[x][y] = localGrid.field[x][y].setFlag()
            return localGrid
        }
        if (field[x][y] == CellState.VISIBLE && getValue(x, y) != 0 && getValue(x, y) == getFlags(x, y)) return openAroundCells(x, y)
        return this
    }

    fun openAroundCells(x: Int, y: Int): Grid {
        var localGrid = this
            (x - 1).rangeTo(x + 1).map { i ->
                (y - 1).rangeTo(y + 1).map { j ->
                    if (i in 0 until width && j in 0 until height) localGrid = localGrid.openCell(i, j)
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
        if (hidEdge.all { it in minesSet && field[it.x][it.y] == CellState.FLAG }) return false
        hidState = solve(visEdge, hidState)
        if (false in hidState.values) return true

        val solves = getSolves(visEdge, hidEdge, hidState, value = true).toMutableSet()
        /* Здесь находятся клетки, которые никогда не рассматривались как false */
        val localEdge = mutableSetOf<Cell>()
        for (solve in solves) {
            for (cell in solve.keys) {
                var wasNotFalse = true
                for (other in solves - solve) {
                    wasNotFalse = solve[cell] == other[cell] && solve[cell] == true && wasNotFalse
                }
                if (wasNotFalse) {
                    localEdge += cell
                }
            }
        }
        solves += getSolves(visEdge, localEdge, hidState, value = false)

        for (solve in solves) {
            for (cell in solve.keys) {
                var isCellSolved = true
                for (other in solves - solve) {
                    isCellSolved = solve[cell] == other[cell] && solve[cell] == false && isCellSolved
                }
                if (isCellSolved) {
                    return true
                }
            }
        }
        return false
    }

    private fun getSolves(visEdge: Set<Cell>, hidEdge: Set<Cell>, hidState: Map<Cell, Boolean?>, globalState: Map<Cell, Boolean?> = emptyMap(), value: Boolean): MutableSet<Map<Cell, Boolean?>> {
        val solves = mutableSetOf<Map<Cell, Boolean?>>()
        for (cell in hidEdge) {
            var localState = hidState.toMutableMap()
            localState[cell] = value
            localState = solve(visEdge, localState).toMutableMap()
            val nullState = localState.filter { it.value == null }.toMutableMap()
            nullState.forEach { localState.remove(it.key) }

            if (nullState.isNotEmpty()) {
                val localSolves = getSolves(visEdge, nullState.keys, nullState, localState, value)
                localSolves.forEach {
                    if (isCorrectSolve(visEdge, globalState + localState + it)) solves += localState + it
                }
            } else if (isCorrectSolve(visEdge, globalState + localState)) {
                solves += localState
            }
        }
        return solves
    }

    private fun isCorrectSolve(visEdge: Set<Cell>, solve: Map<Cell, Boolean?>): Boolean =
        (visEdge.all { getValue(it.x, it.y) >= getCells(it.x, it.y, solve, true)
                    && getValue(it.x, it.y) <= getCells(it.x, it.y, solve, true) + getCells(it.x, it.y, solve) }
            && solve.values.count { it == true } <= this.mines)

    private fun solve(visEdge: Set<Cell>, hidState: Map<Cell, Boolean?>): Map<Cell, Boolean?> {
        val localState = hidState.toMutableMap()
        var lastNulls = Int.MAX_VALUE
        var currentNulls = localState.count { it.value == null }
        while (lastNulls - currentNulls > 0) {
            for (cell in visEdge) {
                val emptyCells = (cell.x - 1).rangeTo(cell.x + 1).sumOf { i ->
                    (cell.y - 1).rangeTo(cell.y + 1).count { j -> ((Cell(i, j) in localState.keys) && (localState[Cell(i, j)] == null)) }
                }

                if (getValue(cell.x, cell.y) - getCells(cell.x, cell.y, localState, true) == emptyCells) {
                    (cell.x - 1).rangeTo(cell.x + 1).forEach { i ->
                        (cell.y - 1).rangeTo(cell.y + 1).forEach { j ->
                            if (Cell(i, j) in localState.keys && localState[Cell(i, j)] == null) {
                                localState[Cell(i, j)] = true
                            } } }
                }
                if (getValue(cell.x, cell.y) == getCells(cell.x, cell.y, localState, true)) {
                    (cell.x - 1).rangeTo(cell.x + 1).forEach { i ->
                        (cell.y - 1).rangeTo(cell.y + 1).forEach { j ->
                            if (Cell(i, j) in localState.keys && localState[Cell(i, j)] == null) {
                                localState[Cell(i, j)] = false
                            } } }
                }

            }
            lastNulls = currentNulls
            currentNulls = localState.count { it.value == null }
        }
        return localState
    }

    private fun getCells(x: Int, y: Int, localState: Map<Cell, Boolean?>, predicate: Boolean? = null): Int {
        return (x - 1).rangeTo(x + 1).sumOf { i ->
            (y - 1).rangeTo(y + 1).count { j ->
                if (Cell(i, j) in localState.keys) localState[Cell(i, j)] == predicate else false
            } }
    }

    fun gameOver(): Grid {
        var localGrid = this
        (0 until width).forEach { i -> (0 until height).forEach { j -> localGrid = localGrid.openCell(i, j) } }
        return localGrid
    }

    fun openRandomCell(): Grid {
        for (cell in getHidEdge().shuffled()) {
            if (cell !in minesSet) return openCell(cell.x, cell.y)
        }
        var cell = Cell(Random.nextInt(width), Random.nextInt(height))
        while (cell in minesSet || field[cell.x][cell.y] == CellState.VISIBLE) cell = Cell(Random.nextInt(width), Random.nextInt(height))
        return openCell(cell.x, cell.y)
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