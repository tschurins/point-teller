package jal.pointscounter

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class PointsActivity : AppCompatActivity() {
    private lateinit var pointAdapter: PointAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points)

        setSupportActionBar(findViewById(R.id.pointsToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val headerView: LinearLayout = findViewById(R.id.header_view)
        for (player in ApplicationState.players) {
            val playerTitle = TextView(this)
            val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            layoutParams.rightMargin = 8
            layoutParams.leftMargin = 8
            playerTitle.layoutParams = layoutParams
            playerTitle.text = player
            headerView.addView(playerTitle)
        }

        val totalView: RecyclerView = findViewById(R.id.footer_view)
        totalView.layoutManager = GridLayoutManager(this, ApplicationState.players.size)
        val totalAdapter = TotalAdapter(ApplicationState.points)
        totalView.adapter = totalAdapter

        val pointView: RecyclerView = findViewById(R.id.recycler_view)
        pointView.layoutManager = GridLayoutManager(this, ApplicationState.players.size)
        pointAdapter = PointAdapter(ApplicationState.points, totalAdapter)
        pointView.adapter = pointAdapter

        val addRowButton: View = findViewById(R.id.addPointRow)
        addRowButton.setOnClickListener {
            ApplicationState.addRow()
            pointAdapter.notifyDataSetChanged()
            totalAdapter.notifyDataSetChanged()
            pointView.smoothScrollToPosition(pointAdapter.itemCount - 1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.points_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_po_clear -> {
            ApplicationState.points.clear()
            pointAdapter.notifyDataSetChanged()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}


class PointAdapter(private val dataSet: Points, private val totalAdapter: TotalAdapter) : RecyclerView.Adapter<PointAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        var row: Int = 0
        var col: Int = 0

        init {
            textView = view.findViewById(R.id.point_text)

            // making sure that the content is selected on focus
            textView.setSelectAllOnFocus(true)

            // clicking on the box -> focus in the text field
            val frame: FrameLayout = view.findViewById(R.id.point_frame)
            frame.setOnClickListener { v ->
                textView.requestFocus()

                val imm: InputMethodManager? = v.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.point_item, viewGroup, false)
        val viewHolder = ViewHolder(view)
        viewHolder.textView.addTextChangedListener(afterTextChanged = {
            try {
                val newValue = it.toString().toInt()
                dataSet.set(viewHolder.row, viewHolder.col, newValue)
                totalAdapter.notifyDataSetChanged()
            } catch (ex: NumberFormatException) {
                // temporary state - keep the previous value
            }
        })
        return viewHolder
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val row = position / ApplicationState.players.size
        val col = position % ApplicationState.players.size

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.row = row
        viewHolder.col = col
        viewHolder.textView.text = dataSet.points[row][col].toString()
    }

    override fun getItemCount(): Int {
        return dataSet.points.size * ApplicationState.players.size
    }
}


class TotalAdapter(private val dataSet: Points) : RecyclerView.Adapter<TotalAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.total_text)
        }
    }
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.total_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet.total[position].toString()
    }

    override fun getItemCount(): Int {
        return ApplicationState.players.size
    }
}