enum class CellState {
    FLAG, VISIBLE, HIDDEN;

    fun actionSecondary(): CellState = if (this == FLAG) HIDDEN else FLAG

    fun action(): CellState = if (this == HIDDEN) VISIBLE else this
}