enum class CellState {
    FLAG, VISIBLE, HIDDEN;

    fun actionSecondary(): CellState = if (this == FLAG) HIDDEN else FLAG
}