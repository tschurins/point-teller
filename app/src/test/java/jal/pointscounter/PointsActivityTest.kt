package jal.pointscounter

import android.view.View
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PointsActivityTest {
    @After
    fun clear() {
        ApplicationState.clear()
    }

    @Test
    fun addRow() {
        ApplicationState.addPlayer("A1")
        ApplicationState.addPlayer("A2")

        var activity = Robolectric.buildActivity(PointsActivity::class.java)
            .create()
            .resume()
            .get()

        val addRowButton: View = activity.findViewById(R.id.addPointRow)
        addRowButton.performClick()

        assertEquals(2, ApplicationState.points.points.size)
    }
}