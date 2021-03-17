package jal.pointscounter

import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class ApplicationStateTest {
    @After
    fun clear() {
        ApplicationState.clear()
    }

    @Test
    fun addPlayer() {
        with (ApplicationState) {
            initState()

            addPlayer("B")
            assertEquals(mutableListOf("A1", "A2", "B"), players)
            assertEquals(2, points.points.size)
            assertEquals(mutableListOf(1, 2, 0), points.points[0])
            assertEquals(mutableListOf(3, 4, 0), points.points[1])
            assertEquals(mutableListOf(4, 6, 0), points.total);
        }
    }

    @Test
    fun addRow() {
        with (ApplicationState) {
            initState()

            addRow()
            assertEquals(3, points.points.size)
            assertEquals(mutableListOf(0, 0), points.points[2])
            assertEquals(mutableListOf(4, 6), points.total);
        }
    }

    private fun ApplicationState.initState() {
        clear()
        players.add("A1")
        players.add("A2")
        points.points[0].add(1)
        points.points[0].add(2)
        points.points.add(mutableListOf(3, 4))
        points.recompute()
    }
}