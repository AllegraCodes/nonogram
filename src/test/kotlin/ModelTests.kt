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
            assertEquals(Square.State.UNKNOWN, square1.state)
        }

        @Test
        fun `change state`() {
            square1.state = Square.State.EMPTY
            assertEquals(Square.State.EMPTY, square1.state)
            square1.state = Square.State.FILLED
            assertEquals(Square.State.FILLED, square1.state)
        }
    }

    @Nested
    inner class BoardTests {
        @Test
        fun `extract board state`() {
            val board = Board(3, 3)
            board[0, 0] = Square.State.FILLED
            board[1, 1] = Square.State.EMPTY
            board[2, 2] = Square.State.FILLED
            val answer = mutableMapOf(
                Pair(Pair(0, 0), Square.State.FILLED),
                Pair(Pair(1, 1), Square.State.EMPTY),
                Pair(Pair(2, 2), Square.State.FILLED)
            )
            for ((location, _) in board.map) {
                if (!answer.containsKey(location)) {
                    answer[location] = Square.State.UNKNOWN
                }
            }
            assertEquals(answer, board.getBoardState())
        }

        @Test
        fun `illegal arguments`() {
            assertThrows<IllegalArgumentException> { Board(0, 0) }
            assertThrows<IllegalArgumentException> { Board(0, 1) }
            assertThrows<IllegalArgumentException> { Board(1, 0) }
        }

        @Test
        fun `test toString`() {
            val board = Board(3, 3)
            board[0, 0] = Square.State.FILLED
            board[1, 1] = Square.State.EMPTY
            board[2, 2] = Square.State.FILLED
            val answer = StringBuilder()
            answer.appendLine("X__")
            answer.appendLine("_O_")
            answer.appendLine("__X")
            assertEquals(answer.toString(), board.toString())
        }

        @Test
        fun `test Board State`() {
            val board = Board(1, 1)
            assertEquals(Square.State.UNKNOWN, board[Pair(0, 0)]!!.state)
            board[Pair(0, 0)] = Square.State.EMPTY
            assertEquals(Square.State.EMPTY, board[0, 0]!!.state)
            board[0, 0] = Square.State.FILLED
            assertEquals(Square.State.FILLED, board[0, 0]!!.state)
        }

        @Test
        fun `test Board equality`() {
            val board1 = Board(3, 3)
            val board2 = Board(3, 3)
            val board3 = Board(3, 4)
            board1[0, 0] = Square.State.EMPTY
            board1[1, 1] = Square.State.FILLED
            board2[0, 0] = Square.State.EMPTY
            board2[1, 1] = Square.State.FILLED
            board3[0, 0] = Square.State.EMPTY
            board3[1, 1] = Square.State.FILLED
            assertTrue(board1.sameStates(board2))
            assertTrue(board2.sameStates(board1))
            assertFalse(board2.sameStates(board3))
            assertFalse(board3.sameStates(board2))
            board1[2, 2] = Square.State.FILLED
            assertFalse(board1.sameStates(board2))
        }

        @Test
        fun `test contains`() {
            val board1 = Board(3, 3)
            assertTrue(board1.contains(Square.State.UNKNOWN))
            assertFalse(board1.contains(Square.State.FILLED))
            assertFalse(board1.contains(Square.State.EMPTY))
            board1[0, 0] = Square.State.EMPTY
            board1[2, 2] = Square.State.FILLED
            assertTrue(board1.contains(Square.State.FILLED))
            assertTrue(board1.contains(Square.State.EMPTY))
        }

        @Test
        fun `apply state`() {
            val board = Board(3, 3)
            val boardState = mapOf(Pair(Pair(1, 2), Square.State.FILLED))
            board.applyState(boardState)
            for ((location, square) in board.map) {
                if (location == Pair(1, 2)) {
                    assertEquals(Square.State.FILLED, square.state)
                } else {
                    assertEquals(Square.State.UNKNOWN, square.state)
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
        fun `illegal arguments`() {
            assertThrows<IllegalArgumentException> { Hint(listOf(0)) }
            assertThrows<IllegalArgumentException> { Hint(listOf(1, 0)) }
            assertThrows<IllegalArgumentException> { Hint(listOf(1, -1)) }
        }

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
            line1[0] = Square.State.FILLED
            line1[1] = Square.State.FILLED
            line1[2] = Square.State.FILLED
            assertDoesNotThrow { Block(0, 3) }
        }
    }

    @Nested
    inner class LineTests {
        private val board1 = Board(4, 3)
        private val hint1 = Hint(listOf(1, 2))
        private val line1 = Line(hint1, listOf(board1[0, 0]!!, board1[0, 1]!!, board1[0, 2]!!))

        @Test
        fun `big hint`() {
            val board2 = Board(1, 3)
            val hint2 = Hint(listOf(2))
            val line2 = Line(hint2, listOf(board2[0, 0]!!, board2[0, 1]!!, board2[0, 2]!!))
            line2.bigHint()
            assertEquals(Square.State.FILLED, board2[0, 1]!!.state)
        }

        @Test
        fun `test indexed access`() {
            line1[1] = Square.State.EMPTY
            assertEquals(Square.State.EMPTY, line1[1].state)
        }

        @Test
        fun `test hint access`() {
            assertEquals(line1.hint[1], 2)
        }

        @Test
        fun `test contains`() {
            assertFalse(line1.contains(Square.State.FILLED))
            line1.squares[2].state = Square.State.FILLED
            assertTrue(line1.contains(Square.State.FILLED))
        }

        @Test
        fun `test block starts`() {
            line1[0] = Square.State.FILLED
            line1[1] = Square.State.EMPTY
            line1[2] = Square.State.FILLED
            assertEquals(listOf(0, 2), line1.getBlockStarts())
        }

        @Test
        fun `test getBlocks`() {
            line1[0] = Square.State.FILLED
            line1[1] = Square.State.EMPTY
            line1[2] = Square.State.FILLED
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
            puzzle.board[0, 0] = Square.State.FILLED
            puzzle.board[0, 1] = Square.State.EMPTY
            puzzle.board[0, 2] = Square.State.EMPTY
            puzzle.board[0, 3] = Square.State.EMPTY
            puzzle.board[0, 4] = Square.State.EMPTY
            puzzle.board[1, 0] = Square.State.FILLED
            puzzle.board[1, 1] = Square.State.FILLED
            puzzle.board[1, 2] = Square.State.FILLED
            puzzle.board[1, 3] = Square.State.FILLED
            puzzle.board[1, 4] = Square.State.FILLED
            puzzle.board[2, 0] = Square.State.EMPTY
            puzzle.board[2, 1] = Square.State.EMPTY
            puzzle.board[2, 2] = Square.State.EMPTY
            puzzle.board[2, 3] = Square.State.FILLED
            puzzle.board[2, 4] = Square.State.FILLED
            puzzle.board[3, 0] = Square.State.EMPTY
            puzzle.board[3, 1] = Square.State.FILLED
            puzzle.board[3, 2] = Square.State.FILLED
            puzzle.board[3, 3] = Square.State.FILLED
            puzzle.board[3, 4] = Square.State.FILLED
            puzzle.board[4, 0] = Square.State.EMPTY
            puzzle.board[4, 1] = Square.State.FILLED
            puzzle.board[4, 2] = Square.State.FILLED
            puzzle.board[4, 3] = Square.State.EMPTY
            puzzle.board[4, 4] = Square.State.FILLED
            puzzle.board[5, 0] = Square.State.FILLED
            puzzle.board[5, 1] = Square.State.FILLED
            puzzle.board[5, 2] = Square.State.EMPTY
            puzzle.board[5, 3] = Square.State.EMPTY
            puzzle.board[5, 4] = Square.State.EMPTY
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
                    testPuzzle.board[col, row] = Square.State.FILLED
                }
            }
            assertFalse(testPuzzle.isSolved())
            testPuzzle.board[0, 0] = Square.State.UNKNOWN
            assertFalse(testPuzzle.isSolved())
        }
    }
}