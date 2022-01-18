package jal.pointscounter

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView


/**
 * Starting activity, allowing the user to fill the names of the players.
 * It contains a table with the player names.
 * After filling the player names, a button allows to go to the next activity: the point activity.
 */
class PlayerEditionActivity : AppCompatActivity() {
    private lateinit var playerAdapter: PlayerAdapter
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_edition)

        setSupportActionBar(findViewById(R.id.playerEditionToolbar))


        playerAdapter = PlayerAdapter(ApplicationState.players)
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.adapter = playerAdapter

        val addPlayerButton: View = findViewById(R.id.playerAdd)
        addPlayerButton.setOnClickListener {
            addPlayer()
        }

        checkStart()
        if (ApplicationState.players.isEmpty()) {
            addPlayer()
        }
    }

    private fun addPlayer() {
        // Set up the input
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT

        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.add_dialog)
            .setView(input)
            .setNeutralButton(R.string.button_add_player_add, null)
            .setPositiveButton(R.string.button_add_player_cancel) { dialog, _ -> dialog.cancel() }
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                val playerName = input.text.toString()
                if (playerName.isNotEmpty()) {
                    ApplicationState.addPlayer(playerName)
                    playerAdapter.notifyDataSetChanged()
                    enableStart()
                    input.setText("")
                }
            }
        }
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.player_edition_menu, menu)
        this.menu = menu
        checkStart()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_pe_next -> {
            val intent = Intent(this, PointsActivity::class.java)
            startActivity(intent)
            true
        }

        R.id.action_pe_clear -> {
            ApplicationState.clear()
            playerAdapter.notifyDataSetChanged()
            disableStart()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun checkStart() {
        if (ApplicationState.players.isEmpty()) {
            disableStart()
        } else {
            enableStart()
        }
    }

    private fun enableStart() {
        menu?.findItem(R.id.action_pe_next)?.isEnabled = true
    }

    private fun disableStart() {
        menu?.findItem(R.id.action_pe_next)?.isEnabled = false
    }

}

/**
 * Recycle View adapter for displaying a player.
 */
class PlayerAdapter(private val dataSet: List<String>) : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.player_text)
        }
    }
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.player_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position]
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}