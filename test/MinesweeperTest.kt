import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MinesweeperTest {
    private val testGrid = Grid(9, 9, 10)
    private val minesSet = setOf(Cell(2, 0), Cell(3, 0), Cell(1, 1), Cell(7, 2), Cell(2, 3),
        Cell(1, 4), Cell(0, 6), Cell(5, 7), Cell(6, 7), Cell(0, 8))

    @Test
    fun actionTest() {
        var grid = testGrid
        assertEquals(CellState.HIDDEN, grid.field[0][0])
        grid = grid.action(0, 0)
        assertEquals(CellState.VISIBLE, grid.field[0][0])
    }

    @Test
    fun actionSecondaryTest() {
        var grid = testGrid
        assertEquals(CellState.HIDDEN, grid.field[0][0])
        grid = grid.actionSecondary(0, 0)
        assertEquals(CellState.FLAG, grid.field[0][0])
        grid = grid.actionSecondary(0, 0)
        assertEquals(CellState.HIDDEN, grid.field[0][0])
    }

    @Test
    fun actionAreaTest() {
        var grid = testGrid
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
        var grid = testGrid
        grid.isFirstAction = false
        grid.minesSet = minesSet

        grid.actionSecondary(2, 0)
        assertEquals(false, grid.isWin())
        grid.actionSecondary(2, 0)
        minesSet.forEach { grid = grid.actionSecondary(it.x, it.y) }
        assertEquals(true, grid.isWin())
        grid.actionSecondary(0, 0)
        assertEquals(false, grid.isWin())
    }

    @Test
    fun isGameOverTest() {
        var grid = testGrid
        grid.isFirstAction = false
        grid.minesSet = minesSet

        assertEquals(false, grid.isGameOver())
        grid = grid.action(1, 1)
        assertEquals(true, grid.isGameOver() || !grid.isWin())
        grid.field[1][1] = CellState.HIDDEN
        minesSet.forEach { grid = grid.actionSecondary(it.x, it.y) }
        assertEquals(true, grid.isGameOver())
    }

    @Test
    fun getValueTest() {
        val grid = testGrid
        grid.minesSet = minesSet

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
        var grid = testGrid
        grid.minesSet = minesSet

        assertEquals(10, grid.minesLeft())
        grid = grid.actionSecondary(1, 1)
        assertEquals(9, grid.minesLeft())
    }
}