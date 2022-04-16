enum class CellState {
    FLAG, VISIBLE, HIDDEN;

    fun action(): CellState = if (this == FLAG) HIDDEN else FLAG
}