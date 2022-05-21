enum class CellState {
    FLAG, VISIBLE, HIDDEN;

    fun setFlag(): CellState = if (this == FLAG) HIDDEN else FLAG

    fun openCell(): CellState = if (this == HIDDEN) VISIBLE else this
}