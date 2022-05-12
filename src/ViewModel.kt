//import kotlin.random.Random
//
//class Game(val width: Int, val height: Int, val mines: Int, var grid: Grid = Grid(width, height, setOf(Cell(0, 0)))) {
//    private var isFirstAction = true
//
//    fun action(x: Int, y: Int): Game {
//        if (isFirstAction) {
//            isFirstAction = false
//            var localMines = setOf(Cell(x, y))
//            while (Cell(x, y) in localMines) {
//                localMines = generateMines()
//                grid = Grid(width, height, localMines)
//            }
//        }
//        grid = grid.action(x, y)
//        return this
//    }
//
//    fun actionSecondary(x: Int, y: Int): Game {
//        grid = grid.actionSecondary(x, y)
//        return this
//    }
//
//    fun cellState(x: Int, y: Int): CellState = grid.field[x][y]
//
//    fun getValue(x: Int, y: Int): Int = grid.getValue(x, y)
//
//
//}