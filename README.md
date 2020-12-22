# nonogram
a toolkit for nonogram puzzles in Kotlin

## How it works
### Square
The Square is the fundamental unit of the Board.
It can have one of three states: UNKNOWN, FILLED, or EMPTY.
This abstraction is meant to facilitate reasoning
over two different Lines, the column and the row.

### Board
The Board is a collection of Squares, each at a unique 
location as Pair(column, row). The Board is created as a 
rectangle starting at (0, 0). The backing map is immutable,
so the Squares can't change, but their state can.

### Hint
The Hint is the list of block sizes in that column or row.
It also stores the size of the blocks with one space 
between them, and uses this value for comparison.

### Line
The Line contains the Squares in a certain column or row
along with the relevant Hint. It also keeps track of the
blocks present, updating every time one of its Squares
changes state.

### Puzzle
The Puzzle is the top level collection of Hints, Lines,
and Board. It must be supplied the Hints in order, one 
list with the columns from left to right, and another
list with the rows from top to bottom.