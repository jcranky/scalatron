import org.specs2.{ScalaCheck, Specification}
import org.specs2.matcher.ThrownExpectations
import org.specs2.specification.Tables

class XYSpec extends Specification with Tables with ThrownExpectations with ScalaCheck {
  def is =
    s2"""
      transform the Direction45 in the correct XY        $directionToXy45
      get a default XY.Right for and invalid Direction45 $defaultXy45
      transform the Direction90 in the correct XY        $directionToXy90
      get a default XY.Right for and invalid Direction45 $defaultXy90
    """

  def directionToXy45 = {
    "Direction45"         | "XY"          |>
    Direction45.Right     ! XY.Right      |
    Direction45.RightUp   ! XY.RightUp    |
    Direction45.Up        ! XY.Up         |
    Direction45.UpLeft    ! XY.UpLeft     |
    Direction45.Left      ! XY.Left       |
    Direction45.LeftDown  ! XY.LeftDown   |
    Direction45.Down      ! XY.Down       |
    Direction45.DownRight ! XY.DownRight  |
    { (dir, xy) => XY.fromDirection45(dir.id) must_=== xy }
  }

  def defaultXy45 = prop { (index: Int) =>
    if (Direction45.values.map(_.id).contains(index)) ok
    else XY.fromDirection45(index) must_=== XY.Right
  }

  def directionToXy90 = {
    "Direction90"         | "XY"          |>
    Direction90.Right     ! XY.Right      |
    Direction90.Up        ! XY.Up         |
    Direction90.Left      ! XY.Left       |
    Direction90.Down      ! XY.Down       |
    { (dir, xy) => XY.fromDirection90(dir.id) must_=== xy }
  }

  def defaultXy90 = prop { (index: Int) =>
    if (Direction90.values.map(_.id).contains(index)) ok
    else XY.fromDirection90(index) must_=== XY.Right
  }
}
