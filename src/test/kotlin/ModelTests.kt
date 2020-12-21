import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModelTest {
    @Nested
    inner class SquareTests {
        private val square1 = Square()

        @Test
        fun `initialized to UNKNOWN`() {
            assertEquals(State.UNKNOWN, square1.state)
        }

        @Test
        fun `change state`() {
            square1.state = State.EMPTY
            assertEquals(State.EMPTY, square1.state)
            square1.state = State.FILLED
            assertEquals(State.FILLED, square1.state)
        }
    }

    @Nested
    inner class BoardTests {
        @Test
        fun `test Board State`() {
            val board = Board(1, 1)
            assertEquals(State.UNKNOWN, board[Pair(0, 0)]!!.state)
            board[Pair(0, 0)] = State.EMPTY
            assertEquals(State.EMPTY, board[0, 0]!!.state)
            board[0, 0] = State.FILLED
            assertEquals(State.FILLED, board[0, 0]!!.state)
        }

        @Test
        fun `test Board equality`() {
            val board1 = Board(3, 3)
            val board2 = Board(3, 3)
            val board3 = Board(3, 4)
            board1[0, 0] = State.EMPTY
            board1[1, 1] = State.FILLED
            board2[0, 0] = State.EMPTY
            board2[1, 1] = State.FILLED
            board3[0, 0] = State.EMPTY
            board3[1, 1] = State.FILLED
            assertTrue(board1.sameStates(board2))
            assertTrue(board2.sameStates(board1))
            assertFalse(board2.sameStates(board3))
            assertFalse(board3.sameStates(board2))
            board1[2, 2] = State.FILLED
            assertFalse(board1.sameStates(board2))
        }

        @Test
        fun `test contains`() {
            val board1 = Board(3, 3)
            assertTrue(board1.contains(State.UNKNOWN))
            assertFalse(board1.contains(State.FILLED))
            assertFalse(board1.contains(State.EMPTY))
            board1[0, 0] = State.EMPTY
            board1[2, 2] = State.FILLED
            assertTrue(board1.contains(State.FILLED))
            assertTrue(board1.contains(State.EMPTY))
        }

        @Test
        fun `apply state`() {
            val board = Board(3, 3)
            val boardState = mapOf(Pair(Pair(1, 2), State.FILLED))
            board.applyState(boardState)
            for ((location, square) in board.map) {
                if (location == Pair(1, 2)) {
                    assertEquals(State.FILLED, square.state)
                } else {
                    assertEquals(State.UNKNOWN, square.state)
                }
            }
        }
    }

    @Nested
    inner class HintTests {
        private val hint1 = Hint(listOf(2, 5))
        private val hint2 = Hint(listOf(3, 5))
        private val hint3 = Hint(listOf(1, 2, 3))

        @Test
        fun `test constructor`() {
            assertEquals(5, hint1[1])
            assertEquals(9, hint2.size)
        }

        @Test
        fun `test comparison`() {
            assertTrue(hint1 < hint2)
            assertTrue(hint1 <= hint3)
        }
    }

    @Nested
    inner class BlockTests {
        private val board1 = Board(4, 3)
        private val hint1 = Hint(listOf(1, 2))
        private val line1 = Line(hint1, listOf(board1[0, 0]!!, board1[0, 1]!!, board1[0, 2]!!))

        @Test
        fun `test constructor`() {
            line1[0] = State.FILLED
            line1[1] = State.FILLED
            line1[2] = State.FILLED
            assertDoesNotThrow { Block(0, 3) }
        }
    }

    @Nested
    inner class LineTests {
        private val board1 = Board(4, 3)
        private val hint1 = Hint(listOf(1, 2))
        private val line1 = Line(hint1, listOf(board1[0, 0]!!, board1[0, 1]!!, board1[0, 2]!!))

        @Test
        fun `test indexed access`() {
            line1[1] = State.EMPTY
            assertEquals(State.EMPTY, line1[1].state)
        }

        @Test
        fun `test hint access`() {
            assertEquals(line1.hint[1], 2)
        }

        @Test
        fun `test contains`() {
            assertFalse(line1.contains(State.FILLED))
            line1.squares[2].state = State.FILLED
            assertTrue(line1.contains(State.FILLED))
        }

        @Test
        fun `test block starts`() {
            line1[0] = State.FILLED
            line1[1] = State.EMPTY
            line1[2] = State.FILLED
            assertEquals(listOf(0, 2), line1.getBlockStarts())
        }

        @Test
        fun `test getBlocks`() {
            line1[0] = State.FILLED
            line1[1] = State.EMPTY
            line1[2] = State.FILLED
            val blocks = line1.blocks
            assertEquals(2, blocks.size)
            assertEquals(0, blocks[0].startIndex)
            assertEquals(2, blocks[1].startIndex)
            assertEquals(1, blocks[1].size)
        }
    }

    @Nested
    inner class PuzzleTests {
        private val testPuzzle by lazy {
            val hintCol1 = Hint(listOf(1))
            val hintCol2 = Hint(listOf(5))
            val hintCol3 = Hint(listOf(2))
            val hintCol4 = Hint(listOf(4))
            val hintCol5 = Hint(listOf(2, 1))
            val hintCol6 = Hint(listOf(2))
            val colHints = listOf(hintCol1, hintCol2, hintCol3, hintCol4, hintCol5, hintCol6)
            val hintRow1 = Hint(listOf(2, 1))
            val hintRow2 = Hint(listOf(1, 3))
            val hintRow3 = Hint(listOf(1, 2))
            val hintRow4 = Hint(listOf(3))
            val hintRow5 = Hint(listOf(4))
            val rowHints = listOf(hintRow1, hintRow2, hintRow3, hintRow4, hintRow5)
            val puzzle = Puzzle(colHints, rowHints)
            puzzle.board[0, 0] = State.FILLED
            puzzle.board[0, 1] = State.EMPTY
            puzzle.board[0, 2] = State.EMPTY
            puzzle.board[0, 3] = State.EMPTY
            puzzle.board[0, 4] = State.EMPTY
            puzzle.board[1, 0] = State.FILLED
            puzzle.board[1, 1] = State.FILLED
            puzzle.board[1, 2] = State.FILLED
            puzzle.board[1, 3] = State.FILLED
            puzzle.board[1, 4] = State.FILLED
            puzzle.board[2, 0] = State.EMPTY
            puzzle.board[2, 1] = State.EMPTY
            puzzle.board[2, 2] = State.EMPTY
            puzzle.board[2, 3] = State.FILLED
            puzzle.board[2, 4] = State.FILLED
            puzzle.board[3, 0] = State.EMPTY
            puzzle.board[3, 1] = State.FILLED
            puzzle.board[3, 2] = State.FILLED
            puzzle.board[3, 3] = State.FILLED
            puzzle.board[3, 4] = State.FILLED
            puzzle.board[4, 0] = State.EMPTY
            puzzle.board[4, 1] = State.FILLED
            puzzle.board[4, 2] = State.FILLED
            puzzle.board[4, 3] = State.EMPTY
            puzzle.board[4, 4] = State.FILLED
            puzzle.board[5, 0] = State.FILLED
            puzzle.board[5, 1] = State.FILLED
            puzzle.board[5, 2] = State.EMPTY
            puzzle.board[5, 3] = State.EMPTY
            puzzle.board[5, 4] = State.EMPTY
            puzzle
        }

        @Test
        fun `test puzzle size`() {
            assertEquals(6, testPuzzle.numCols)
            assertEquals(5, testPuzzle.numRows)
            assertEquals(6, testPuzzle.colHints.size)
            assertEquals(5, testPuzzle.rowHints.size)
        }

        @Test
        fun `test line parsing`() {
            assertEquals(5, testPuzzle.lines[1].hint[0])
        }

        @Test
        fun `test isSolved`() {
            assertTrue(testPuzzle.isSolved())
            for (col in 0 until testPuzzle.numCols) {
                for (row in 0 until testPuzzle.numRows) {
                    testPuzzle.board[col, row] = State.FILLED
                }
            }
            assertFalse(testPuzzle.isSolved())
            testPuzzle.board[0, 0] = State.UNKNOWN
            assertFalse(testPuzzle.isSolved())
        }
    }
}