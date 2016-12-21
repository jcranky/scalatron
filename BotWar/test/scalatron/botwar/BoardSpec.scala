package scalatron.botwar

import org.specs2.mutable.Specification
import org.mockito.Mockito.{mock, when}
import org.mockito.Matchers.any

class BoardSpec extends Specification { override def is = s2"""

    newPositionAndValidCount should return
        initial position with 0 allowable count                 $initiallyZeroAllowableCount
        initial position when initially position is occupied    $initiallyPositionOccupied
        updated position when positions on board are valid      $updatedPositionWhenBoardPositionsValid
        updated position when one of bots on the path           $botOnPath
    """

    private val boardClass = new Board(nextId = 0, decorations = Map(), bots = Map()).getClass
    private val coordClass = new XY(0, 0).getClass

    def initiallyZeroAllowableCount = {
        val initPosition = XY(1, 1)
        val mockBoard = mock(boardClass)
        when(mockBoard.isValidAndVacant(any(coordClass), any(coordClass))).thenReturn(true)

        Board.newPositionAndValidCount(
            initPosition = initPosition,
            count = 0,
            board = mockBoard ,
            boardSize = XY(10, 10),
            step = XY(2, 1)
        ) must_== ((initPosition, 0))
    }

    def initiallyPositionOccupied = {
        val initPosition = XY(4, 5)
        val mockBoard = mock(boardClass)
        when(mockBoard.isValidAndVacant(any(coordClass), any(coordClass))).thenReturn(false)

        Board.newPositionAndValidCount(
            initPosition = initPosition,
            count = 3,
            board = mockBoard,
            boardSize = XY(10, 10),
            step = XY(1, 1)
        ) must_== ((initPosition, 0))
    }

    def updatedPositionWhenBoardPositionsValid = {
        val mockBoard = mock(boardClass)
        when(mockBoard.isValidAndVacant(any(coordClass), any(coordClass))).thenReturn(true)

        Board.newPositionAndValidCount(
            initPosition = XY(4, 9),
            count = 3,
            board = mockBoard,
            boardSize = XY(100, 100),
            step = XY(1, 1)
        ) must_== ((XY(6, 11), 2))
    }

    def botOnPath = {
        val mockBoard = mock(boardClass)
        when(mockBoard.isValidAndVacant(any(coordClass), any(coordClass))).thenReturn(true)
        when(mockBoard.isValidAndVacant(XY(11, 9), XY(100, 100))).thenReturn(false)

        Board.newPositionAndValidCount(
            initPosition = XY(7, 1),
            count = 10,
            board = mockBoard,
            boardSize = XY(100, 100),
            step = XY(1, 2)
        ) must_== ((XY(10, 7), 3))
    }
}
