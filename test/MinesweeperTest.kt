import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MinesweeperTest {
    private val testGrid1 = Grid(9, 9, 10)
    private val minesSet1 = setOf(Cell(2, 0), Cell(3, 0), Cell(1, 1), Cell(7, 2), Cell(2, 3),
        Cell(1, 4), Cell(0, 6), Cell(5, 7), Cell(6, 7), Cell(0, 8))

    @Test
    fun actionTest() {
        var grid = testGrid1
        assertEquals(CellState.HIDDEN, grid.field[0][0])
        grid = grid.action(0, 0)
        assertEquals(CellState.VISIBLE, grid.field[0][0])
    }

    @Test
    fun actionSecondaryTest() {
        var grid = testGrid1
        assertEquals(CellState.HIDDEN, grid.field[0][0])
        grid = grid.actionSecondary(0, 0)
        assertEquals(CellState.FLAG, grid.field[0][0])
        grid = grid.actionSecondary(0, 0)
        assertEquals(CellState.HIDDEN, grid.field[0][0])
    }

    @Test
    fun actionAreaTest() {
        var grid = testGrid1
        assertEquals(CellState.HIDDEN, grid.field[7][7])
        assertEquals(CellState.HIDDEN, grid.field[8][7])
        assertEquals(CellState.HIDDEN, grid.field[7][8])
        assertEquals(CellState.HIDDEN, grid.field[8][8])
        grid = grid.actionArea(8, 8)
        assertEquals(CellState.VISIBLE, grid.field[7][7])
        assertEquals(CellState.VISIBLE, grid.field[8][7])
        assertEquals(CellState.VISIBLE, grid.field[7][8])
        assertEquals(CellState.VISIBLE, grid.field[8][8])
    }

    @Test
    fun isWinTest() {
        var grid = testGrid1
        grid.isFirstAction = false
        grid.minesSet = minesSet1

        grid.actionSecondary(2, 0)
        assertEquals(false, grid.isWin())
        grid.actionSecondary(2, 0)
        minesSet1.forEach { grid = grid.actionSecondary(it.x, it.y) }
        assertEquals(true, grid.isWin())
        grid.actionSecondary(0, 0)
        assertEquals(false, grid.isWin())
    }

    @Test
    fun isGameOverTest() {
        var grid = testGrid1
        grid.isFirstAction = false
        grid.minesSet = minesSet1

        assertEquals(false, grid.isGameOver())
        grid = grid.action(1, 1)
        assertEquals(true, grid.isGameOver() || !grid.isWin())
        grid.field[1][1] = CellState.HIDDEN
        minesSet1.forEach { grid = grid.actionSecondary(it.x, it.y) }
        assertEquals(true, grid.isGameOver())
    }

    @Test
    fun getValueTest() {
        val grid = testGrid1
        grid.minesSet = minesSet1

        assertEquals(3, grid.getValue(2, 1))
        assertEquals(2, grid.getValue(1, 0))
        assertEquals(2, grid.getValue(1, 3))
        assertEquals(2, grid.getValue(0, 7))
        assertEquals(1, grid.getValue(6, 1))
        assertEquals(1, grid.getValue(6, 2))
        assertEquals(1, grid.getValue(6, 3))
        assertEquals(0, grid.getValue(6, 0))
        assertEquals(0, grid.getValue(8, 8))
        assertEquals(0, grid.getValue(3, 5))
    }

    @Test
    fun minesLeftTest() {
        var grid = testGrid1
        grid.minesSet = minesSet1

        assertEquals(10, grid.minesLeft())
        grid = grid.actionSecondary(1, 1)
        assertEquals(9, grid.minesLeft())
    }

    private val testGrid2 = Grid(6, 6, 9)
    private val minesSet2 = setOf(Cell(1, 1), Cell(2, 1), Cell(3, 2), Cell(4, 2), Cell(2, 3),
        Cell(2, 4), Cell(4, 4), Cell(0, 5), Cell(1, 5))

    private val testGrid3 = Grid(4, 4, 5)
    private val minesSet3 = setOf(Cell(0, 0), Cell(1, 0), Cell(0, 1), Cell(2, 1), Cell(2, 2))

    private val testGrid4 = Grid(4, 3, 2)
    private val minesSet4 = setOf(Cell(0, 0), Cell(2, 0))

    private val testGrid5 = Grid(5, 3, 4)
    private val minesSet5 = setOf(Cell(1, 0), Cell(3, 0), Cell(4, 1), Cell(0, 2))

    private val testGrid6 = Grid(5, 3, 2)
    private val minesSet6 = setOf(Cell(1, 0), Cell(4, 0))

    private val testGrid7 = Grid(4, 4, 2)
    private val minesSet7 = setOf(Cell(2, 0), Cell(0, 3))
    @Test
    fun isSolvableTest() {
        var grid = testGrid2
        grid.minesSet = minesSet2
        val flags = listOf(7, 8, 15, 26, 30, 31)
        flags.forEach { grid.field[it % 6][it / 6] = CellState.FLAG }
        var visible = listOf(0, 1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 18, 19, 24, 25)
        visible.forEach { grid.field[it % 6][it / 6] = CellState.VISIBLE }
        assertEquals(false, grid.isSolvable())

        grid = testGrid3
        grid.minesSet = minesSet3
        visible = listOf(5, 8, 9, 12, 13, 14)
        visible.forEach { grid.field[it % 4][it / 4] = CellState.VISIBLE }
        assertEquals(true, grid.isSolvable())

        grid = testGrid4
        grid.minesSet = minesSet4
        visible = listOf(4, 5, 6, 7, 8, 9, 10, 11)
        visible.forEach { grid.field[it % 4][it / 4] = CellState.VISIBLE }
        assertEquals(true, grid.isSolvable())

        grid = testGrid5
        grid.minesSet = minesSet5
        visible = listOf(5, 6, 7, 9, 10, 11)
        visible.forEach { grid.field[it % 5][it / 5] = CellState.VISIBLE }
        assertEquals(true, grid.isSolvable())

        grid = testGrid6
        grid.minesSet = minesSet6
        visible = listOf(5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
        visible.forEach { grid.field[it % 5][it / 5] = CellState.VISIBLE }
        assertEquals(true, grid.isSolvable())

        grid = testGrid7
        grid.minesSet = minesSet7
        visible = listOf(5, 6)
        visible.forEach { grid.field[it % 4][it / 4] = CellState.VISIBLE }
        assertEquals(false, grid.isSolvable())
    }
}