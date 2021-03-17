package jal.pointscounter

import android.app.AlertDialog
import android.content.Intent
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowView


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PlayerEditionActivityTest {
    @After
    fun clear() {
        ApplicationState.clear()
    }

    @Test
    fun pointViewDisabledOnStart() {
        var activity = Robolectric.buildActivity(PlayerEditionActivity::class.java)
            .create()
            .resume()
            .get()

        val toolbar: Toolbar = activity.findViewById(R.id.playerEditionToolbar)
        activity.onCreateOptionsMenu(toolbar.menu);

        val next = toolbar.menu.findItem(R.id.action_pe_next)
        assertFalse(next.isEnabled)

        val alertDialog = ShadowAlertDialog.getLatestDialog() as AlertDialog
        assertNotNull(alertDialog)
    }

    @Test
    fun pointViewEnabledWhenPlayerPresent() {
        ApplicationState.addPlayer("T")
        var activity = Robolectric.buildActivity(PlayerEditionActivity::class.java)
            .create()
            .resume()
            .get()

        val toolbar: Toolbar = activity.findViewById(R.id.playerEditionToolbar)
        activity.onCreateOptionsMenu(toolbar.menu);

        val next = toolbar.menu.findItem(R.id.action_pe_next)
        assertTrue(next.isEnabled)

        val alertDialog = ShadowAlertDialog.getLatestDialog() as? AlertDialog
        assertNull(alertDialog)
    }

    @Test
    fun clickAddPlayer() {
        var activity = Robolectric.buildActivity(PlayerEditionActivity::class.java)
            .create()
            .resume()
            .visible()
            .get()

        // enter name and press ok
        val alertDialog = ShadowAlertDialog.getLatestDialog() as AlertDialog
        assertNotNull(alertDialog)
        val input = shadowOf(alertDialog).view as EditText
        input.setText("Al")
        ShadowView.clickOn(alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL));
        ShadowView.clickOn(alertDialog.getButton(AlertDialog.BUTTON_POSITIVE));

        // player should be added
        assertEquals(1, ApplicationState.players.size)

        /* // TODO does not work: no children in recycler view
        val recycler: RecyclerView = activity.findViewById(R.id.recycler_view)
        recycler.measure(0,0)
        recycler.layout(0,0,100,1000)

        val firstItem = recycler.findViewHolderForItemId(0)
        assertNotNull(firstItem)
        */
    }


    @Test
    fun clickStartCount() {
        ApplicationState.addPlayer("T")
        var activity = Robolectric.buildActivity(PlayerEditionActivity::class.java)
            .create()
            .resume()
            .get()

        shadowOf(activity).clickMenuItem(R.id.action_pe_next)

        val expectedIntent = Intent(activity, PointsActivity::class.java)
        val actual = shadowOf(activity).getNextStartedActivity()
        assertEquals(expectedIntent.component, actual.component)
    }
}
