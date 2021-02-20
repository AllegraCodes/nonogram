import java.lang.StringBuilder
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
        mLines.toList()
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
        var lastState = Square.State.EMPTY
        for ((index, square) in squares.withIndex()) {
            if (lastState != Square.State.FILLED) { // looking for block
                if (square.state == Square.State.FILLED) {
                    blockSize = 1
                    blockStart = index
                }
            } else { // building a block
                if (square.state == Square.State.FILLED) blockSize++
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

    /**
     * Applies the big hint rule by building a list of
     * which block each square is part of in a front
     * aligned and back aligned configuration. Squares
     * that are part of the same block in both configurations
     * are set to FILLED.
     */
    fun bigHint() {
        val core = mutableListOf<Int>()
        for ((index, block) in hint.values.withIndex()) {
            for (i in 0 until block) {
                core.add(index + 1)
            }
            if (index < hint.values.size - 1) core.add(0)
        }
        val pad = mutableListOf<Int>()
        for (i in core.size until squares.size) {
            pad.add(0)
        }
        val front = core + pad
        val back = pad + core
        for (index in front.indices) {
            if (front[index] != 0 &&
                front[index] == back[index]
            ) {
                squares[index].state = Square.State.FILLED
            }
        }
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

    /** Checks if the [Line] contains a [Square] with the given [Square.State]. */
    fun contains(state: Square.State): Boolean {
        for (square in squares) if (square.state == state) return true
        return false
    }

    /** Set the [Square.State] of a [Square] in this [Line] by index. */
    operator fun set(index: Int, newState: Square.State) {
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
 * Note that the empty hint should be represented with an empty list.
 * @property values the list of block sizes
 * @property size minimum size of the blocks and spaces
 */
class Hint(val values: List<Int>) : Comparable<Hint> {
    val size = values.size - 1 + values.sum()

    init {
        if (values.minOrNull() ?: 1 < 1) {
            throw IllegalArgumentException("Hint values must be > 0")
        }
    }

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
 * The Board is a map of [Location] to [Square].
 * Access the map with the indexed access operator (square brackets).
 * @property numCols the number of columns in the board
 * @property numRows the number of rows in the board
 * @property map the map representing the board
 * @constructor creates a blank ([Square.State].UNKNOWN) board of the given size
 */
class Board(val numCols: Int, val numRows: Int) {
    val map by lazy {
        val mMap = mutableMapOf<Location, Square>()
        for (c in 0 until numCols) {
            for (r in 0 until numRows) {
                mMap[Pair(c, r)] = Square()
            }
        }
        mMap.toMap()
    }

    init {
        if (numCols <= 0 || numRows <= 0) {
            throw IllegalArgumentException("Board must have numCols and numRows > 0")
        }
    }

    /** Checks if the [Board] contains a [Square] with the given [Square.State]. */
    fun contains(state: Square.State): Boolean {
        for ((_, square) in map) if (square.state == state) return true
        return false
    }

    /** Change the [Square.State] of a [Square] given its [Location]. */
    operator fun set(location: Location, newState: Square.State) {
        map[location]?.state = newState
    }

    /** Change the [Square.State] of a [Square] given its column and row. */
    operator fun set(column: Int, row: Int, newState: Square.State) {
        map[Pair(column, row)]?.state = newState
    }

    /** Access the [Square]s with a [Location]. */
    operator fun get(location: Location): Square? {
        return map[location]
    }

    /** Access the [Square]s with a column and row. */
    operator fun get(column: Int, row: Int): Square? {
        return map[Pair(column, row)]
    }

    /**
     * Does a deep comparison by the [Square.State]s of each [Board]'s [Square]s.
     * @return true if the corresponding [Square]s on each [Board]
     * have the same [Square.State], false otherwise
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

    /** Applies the given [Square.State]s to the board's [Square]s */
    fun applyState(boardState: BoardState) {
        for ((location, squareState) in boardState) {
            map[location]?.state = squareState
        }
    }

    /** Gets the current [BoardState] of the board */
    fun getBoardState(): BoardState {
        val mMap = mutableMapOf<Location, Square.State>()
        for ((location, square) in map) {
            mMap[location] = square.state
        }
        return mMap.toMap()
    }

    /**
     * Represent the board state with _ for UNKNOWN,
     * O for EMPTY, and X for FILLED.
     * */
    override fun toString(): String {
        val wholeString = StringBuilder()
        for (row in 0 until numRows) {
            val lineString = StringBuilder()
            for (col in 0 until numCols) {
                when (this[col, row]?.state) {
                    Square.State.UNKNOWN -> lineString.append('_')
                    Square.State.EMPTY -> lineString.append('O')
                    Square.State.FILLED -> lineString.append('X')
                }
            }
            wholeString.appendLine(lineString)
        }
        return wholeString.toString()
    }
}

/** A full or partial representation of a possible [Board] */
typealias BoardState = Map<Location, Square.State>

/** A space on the [Board] (column, row) */
typealias Location = Pair<Int, Int>

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

    /** The possible states of a [Square] on the [Board]: UNKNOWN, EMPTY, FILLED */
    enum class State {
        UNKNOWN, EMPTY, FILLED
    }
}

/**
 * Classes that want to update when a [Square]'s [Square.State] changes
 * can implement this interface and register with the [Square]s.
 */
interface SquareObserver {
    fun update()
}