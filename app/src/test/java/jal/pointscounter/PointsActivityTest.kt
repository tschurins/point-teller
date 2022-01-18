package jal.pointscounter

import android.os.Looper.*
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PointsActivityTest {
    @After
    fun clear() {
        ApplicationState.clear()
    }

    @Test
    fun addRow_shouldUpdatePointModel() {
        ApplicationState.addPlayer("A1")
        ApplicationState.addPlayer("A2")

        val activity = Robolectric.buildActivity(PointsActivity::class.java)
            .create()
            .resume()
            .get()

        val addRowButton: View = activity.findViewById(R.id.addPointRow)
        addRowButton.performClick()

        assertEquals(2, ApplicationState.points.points.size)
    }

    @Test
    fun addRow_shouldSetFocusOnNewRow() {
        ApplicationState.addPlayer("A1")
        ApplicationState.addPlayer("A2")

        val activity = Robolectric.buildActivity(PointsActivity::class.java)
            .create()
            .resume()
            .visible()
            .get()

        val addRowButton: View = activity.findViewById(R.id.addPointRow)
        addRowButton.performClick()
        shadowOf(getMainLooper()).idle()

        val pointsView: RecyclerView = getRecyclerView(activity)
        assertEquals(4, pointsView.childCount)
        val cell: FrameLayout = pointsView.getChildAt(2) as FrameLayout
        assertEquals(1, cell.childCount)
        val cellText = cell.getChildAt(0)
        assertTrue(cellText.hasFocus())
    }

    @Test
    fun clickOnCell_shouldSetFocusOnText() {
        ApplicationState.addPlayer("A1")
        ApplicationState.addPlayer("A2")

        val activity = Robolectric.buildActivity(PointsActivity::class.java)
            .create()
            .resume()
            .visible()
            .get()

        val pointsView: RecyclerView = getRecyclerView(activity)
        assertEquals(2, pointsView.childCount)
        val cell: FrameLayout = getCell(pointsView, 0)
        assertEquals(1, cell.childCount)
        val cellText = cell.getChildAt(0)

        cell.performClick()
        assertTrue(cellText.hasFocus())
    }

    @Test
    fun enterPoints() {
        ApplicationState.addPlayer("A1")
        ApplicationState.addPlayer("A2")

        val activity = Robolectric.buildActivity(PointsActivity::class.java)
            .create()
            .resume()
            .visible()
            .get()

        val pointsView: RecyclerView = getRecyclerView(activity)
        assertEquals(2, pointsView.childCount)
        val cell = getCell(pointsView, 0)
        val cellText = getTextEditor(cell)

        cellText.text = "123"
        shadowOf(getMainLooper()).idle()
        assertEquals(mutableListOf(123, 0), ApplicationState.points.points[0])

        val totals = activity.findViewById(R.id.footer_view) as RecyclerView
        val totalPoints = getTextEditor(getCell(totals, 0)).text
        assertEquals("123", totalPoints)

    }

    private fun getTextEditor(cell: FrameLayout): TextView {
        assertEquals(1, cell.childCount)
        return cell.getChildAt(0) as TextView
    }

    private fun getCell(recyclerView: RecyclerView, cellIndex: Int): FrameLayout {
        return recyclerView.getChildAt(cellIndex) as FrameLayout
    }

    private fun getRecyclerView(activity: PointsActivity): RecyclerView {
        return activity.findViewById(R.id.recycler_view)
    }
}