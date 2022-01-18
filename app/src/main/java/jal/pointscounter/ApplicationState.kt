package jal.pointscounter

/**
 * Singleton holding the players and their points.
 */
object ApplicationState {
    val players = mutableListOf<String>()
    val points = Points()

    fun addPlayer(name: String) {
        players.add(name)
        points.addPlayer()
    }

    fun removePlayer(name: String) {
        val place = players.indexOf(name)
        players.removeAt(place)
        points.removePlayer(place)
    }

    fun addRow() {
        points.addRow(players.size)
    }

    fun clear() {
        players.clear()
        points.clear()
    }
}

/**
 * Holder for the points of the players, and their total.
 */
class Points() {
    val points = mutableListOf(mutableListOf<Int>())
    val total = mutableListOf<Int>()

    fun clear() {
        points.clear()
        addRow(ApplicationState.players.size)
        recompute()
    }

    fun addPlayer() {
        points.forEach { list -> list.add(0) }
        total.add(0)
    }

    fun removePlayer(place: Int) {
        points.forEach { list -> list.removeAt(place) }
        total.removeAt(place)
    }

    fun addRow(size: Int) {
        points.add(MutableList(size) { 0 })
    }

    fun set(row: Int, col: Int, newValue: Int) {
        points[row][col] = newValue
        recompute(col)
    }

    fun recompute() {
        val size = points[0].size
        total.clear()
        for (i in 1 .. size) {
            total.add(0)
            recompute(i - 1)
        }
    }

    private fun recompute(col: Int) {
        var t: Int = 0;
        points.forEach { list -> t += list[col] }
        total[col] = t
    }
}