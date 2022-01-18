package jal.pointscounter

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


/**
 * Activity for displaying and filling the points of a game.
 * It contains a table where each player corresponds to a column.
 * Each row of the table corresponds to a match.
 * The game total points are displayed below the table.
 */
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
            val layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
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

            // notify adapter for each new items
            for (i in 0..ApplicationState.players.size) {
                pointAdapter.notifyItemInserted(pointAdapter.getFirstCellFromLastRow() + i)
            }

            // scroll to first item of the new row
            pointView.smoothScrollToPosition(pointAdapter.getFirstCellFromLastRow())

            // edit the item
            pointView.post {
                pointView.post {
                    // need two post-s because on the first, the layout has not yet scrolled
                    val childOnNewRow = pointView.children.filter { child ->
                        pointView.getChildAdapterPosition(child) == pointAdapter.getFirstCellFromLastRow()
                    }.elementAtOrNull(0)
                    if (childOnNewRow != null) {
                        val childViewHolder =
                            pointView.getChildViewHolder(childOnNewRow) as PointAdapter.ViewHolder
                        childViewHolder.textView.requestFocus()
                    }
                }
            }
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

        R.id.action_po_share -> {
            val bitmap = createSnapshot()
            val sdf = SimpleDateFormat("yyyyMMdd-hhmm")
            val store = store(bitmap, "points-${sdf.format(Date())}.png")
            if (store != null) shareImage(store)
            else Toast.makeText(this, "No Storage Available", Toast.LENGTH_SHORT).show()
            //sendHtml()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun createSnapshot(): Bitmap {
        val columns = ApplicationState.players.size
        val rows = ApplicationState.points.points.size
        val columnSize = 100f
        val rowSize = 20f
        val bitmap = Bitmap.createBitmap(
            (columns * columnSize).toInt(),
            ((rows + 3.25) * rowSize).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawPaint(paint)

        paint.color = Color.BLACK
        paint.textSize = 16f
        paint.isAntiAlias = true
        val textXBase = 0.1f * columnSize
        for (p in 0 until columns) {
            canvas.drawText(ApplicationState.players[p], p * columnSize + textXBase, rowSize, paint)
        }
        canvas.drawLine(0f, 1.25f * rowSize, columns * columnSize + columnSize - 1, 1.25f * rowSize, paint)

        for (r in 0 until rows) {
            for (p in 0 until columns) {
                canvas.drawText("${ApplicationState.points.points[r][p]}", p * columnSize + textXBase, (2.5f + r) * rowSize, paint)
            }
        }
        canvas.drawLine(0f, (rows + 1.75f) * rowSize, columns * columnSize + columnSize - 1, (rows + 1.75f) * rowSize, paint)
        for (p in 0 until columns) {
            canvas.drawText("${ApplicationState.points.total[p]}", p * columnSize + textXBase, (rows + 3f) * rowSize, paint)
        }
        return bitmap
    }

    /**
     * Stores the image to the external storage for sharing.
     */
    private fun store(bm: Bitmap, fileName: String): File? {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (dir != null) {
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            try {
                val fOut = FileOutputStream(file)
                bm.compress(Bitmap.CompressFormat.PNG, 85, fOut)
                fOut.flush()
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            return file
        } else {
            return null
        }
    }

    /**
     * Open the share dialog
     */
    private fun shareImage(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName.toString() + ".provider",
            file
        )
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        val chooser = Intent.createChooser(intent, "Share Screenshot")
        val resInfoList = this.packageManager.queryIntentActivities(
            chooser,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No App Available", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Recycle view adapter for displaying and modifying one point of a player (column) in a match (row).
 */
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

    fun getFirstCellFromLastRow(): Int {
        return (dataSet.points.size - 1) * ApplicationState.players.size;
    }
}


/**
 * Recycle view adapter for displaying the total point for a player (column).
 */
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