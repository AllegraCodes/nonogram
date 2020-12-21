import kotlin.properties.Delegates

/**
 * A Puzzle is a list of [Hint]s for the columns and rows.
 * @property colHints list of hints for columns from left to right
 * @property rowHints list of hints for rows from top to bottom
 * @property board the [Board] for this Puzzle
 * @property numCols the number of columns
 * @property numRows the number of rows
 * @property lines the [Line]s derived from the [Hint]s and [Board]
 */
class Puzzle(
    val colHints: List<Hint>,
    val rowHints: List<Hint>,
    val board: Board = Board(colHints.size, rowHints.size),
) {
    val numCols = colHints.size
    val numRows = rowHints.size

    val lines by lazy {
        val mLines = mutableListOf<Line>()
        for ((col, hint) in colHints.withIndex()) {
            val squares = mutableListOf<Square>()
            for (row in 0 until numRows) {
                board[col, row]?.let { squares.add(it) }
            }
            mLines.add(Line(hint, squares.toList()))
        }
        for ((row, hint) in rowHints.withIndex()) {
            val squares = mutableListOf<Square>()
            for (col in 0 until numCols) {
                board[col, row]?.let { squares.add(it) }
            }
            mLines.add(Line(hint, squares.toList()))
        }
        mLines.sortedByDescending { it.hint }
    }

    /** Checks if the current [Board] solves the puzzle */
    fun isSolved(): Boolean {
        for (line in lines) {
            if (line.getBlockSizes() != line.hint.values) return false
        }
        return true
    }
}

/**
 * A Line is a column or row of [Square]s and the corresponding [Hint].
 * Access the [Square]s using the indexed access operator (square brackets).
 * @property hint the [Hint] that restricts the line
 * @property squares the [Square]s that make up this line
 * @property blocks the current [Block]s in this line
 */
class Line(val hint: Hint, val squares: List<Square>) : SquareObserver {
    lateinit var blocks: List<Block>

    init {
        squares.forEach { it.observers.add(this) }
        update()
    }

    /** Computes the current [Block]s in the line */
    override fun update() {
        var blockSize = 0
        var blockStart = 0
        val mBlocks = mutableListOf<Block>()
        var lastState = State.EMPTY
        for ((index, square) in squares.withIndex()) {
            if (lastState != State.FILLED) { // looking for block
                if (square.state == State.FILLED) {
                    blockSize = 1
                    blockStart = index
                }
            } else { // building a block
                if (square.state == State.FILLED) blockSize++
                else {
                    mBlocks.add(Block(blockStart, blockSize))
                    blockSize = 0
                }
            }
            lastState = square.state
        }
        if (blockSize > 0) mBlocks.add(Block(blockStart, blockSize))
        blocks = mBlocks.toList()
    }

    /** Gets the current sizes of blocks in the line */
    fun getBlockSizes(): List<Int> {
        val sizes = mutableListOf<Int>()
        blocks.forEach { sizes.add(it.size) }
        return sizes.toList()
    }

    /** Gets the current starting indices of blocks in the line */
    fun getBlockStarts(): List<Int> {
        val starts = mutableListOf<Int>()
        blocks.forEach { starts.add(it.startIndex) }
        return starts.toList()
    }

    /** Checks if the [Line] contains a [Square] with the given [State]. */
    fun contains(state: State): Boolean {
        for (square in squares) if (square.state == state) return true
        return false
    }

    /** Set the [State] of a [Square] in this [Line] by index. */
    operator fun set(index: Int, newState: State) {
        squares[index].state = newState
    }

    /** Access the [Square]s in this [Line] by index. */
    operator fun get(index: Int): Square {
        return squares[index]
    }
}

/**
 * A Block is a group of adjacent Squares.
 * @property startIndex the starting location of this block in the line
 * @property size the length of the block
 */
data class Block(val startIndex: Int, val size: Int)

/**
 * A Hint is a list of integers representing the block sizes in the column/row.
 * Access the list with the indexed access operator (square brackets).
 * @property values the list of block sizes
 * @property size minimum size of the blocks and spaces
 */
class Hint(val values: List<Int>) : Comparable<Hint> {
    val size = values.size - 1 + values.sum()

    /**
     * The [Hint]'s size is used for comparison.
     * @return a negative number if this [Hint]'s size is less than the other
     */
    override operator fun compareTo(other: Hint): Int {
        return this.size - other.size
    }

    /** Access the [Hint]'s values by index. */
    operator fun get(index: Int): Int {
        return values[index]
    }
}

/**
 * The Board is a map of locations as Pair(column, row) to [Square].
 * Access the map with the indexed access operator (square brackets).
 * @property numCols the number of columns in the board
 * @property numRows the number of rows in the board
 * @constructor creates a blank ([State].UNKNOWN) board of the given size
 */
class Board(val numCols: Int, val numRows: Int) {
    val map by lazy {
        val mMap = mutableMapOf<Pair<Int, Int>, Square>()
        for (c in 0 until numCols) {
            for (r in 0 until numRows) {
                mMap[Pair(c, r)] = Square()
            }
        }
        mMap.toMap()
    }

    /** Checks if the [Board] contains a [Square] with the given [State]. */
    fun contains(state: State): Boolean {
        for ((_, square) in map) if (square.state == state) return true
        return false
    }

    /** Change the [State] of a [Square] given its location as a Pair. */
    operator fun set(location: Pair<Int, Int>, newState: State) {
        map[location]?.state = newState
    }

    /** Change the [State] of a [Square] given its column and row. */
    operator fun set(column: Int, row: Int, newState: State) {
        map[Pair(column, row)]?.state = newState
    }

    /** Access the [Square]s with a location Pair. */
    operator fun get(location: Pair<Int, Int>): Square? {
        return map[location]
    }

    /** Access the [Square]s with a column and row. */
    operator fun get(column: Int, row: Int): Square? {
        return map[Pair(column, row)]
    }

    /**
     * Does a deep comparison by the [State]s of each [Board]'s [Square]s.
     * @return true if the corresponding [Square]s on each [Board]
     * have the same [State], false otherwise
     */
    fun sameStates(other: Board): Boolean {
        if (other.numCols != this.numCols ||
            other.numRows != this.numRows
        ) {
            return false
        }
        for (col in 0 until numCols) {
            for (row in 0 until numRows) {
                if (other[col, row]?.state != this[col, row]?.state) return false
            }
        }
        return true
    }

    fun applyState(boardState: BoardState) {
        for ((location, squareState) in boardState) {
            map[location]?.state = squareState
        }
    }
}

typealias BoardState = Map<Pair<Int, Int>, State>

/**
 * A single space on the [Board].
 * @property observers objects to notify when the state changes
 * @property state the current [State] of this space
 */
class Square {
    val observers = mutableSetOf<SquareObserver>()
    var state by Delegates.observable(State.UNKNOWN) { _, _, _ ->
        observers.forEach { it.update() }
    }
}

/**
 * Classes that want to update when a [Square]'s [State] changes
 * can implement this interface and register with the [Square]s.
 */
interface SquareObserver {
    fun update()
}

/** The possible states of a [Square] on the [Board]: UNKNOWN, EMPTY, FILLED */
enum class State {
    UNKNOWN, EMPTY, FILLED
}