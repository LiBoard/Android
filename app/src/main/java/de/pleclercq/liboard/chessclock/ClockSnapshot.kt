package de.pleclercq.liboard.chessclock

data class ClockSnapshot(val tWhite: Int, val tBlack: Int, val flagged: Int?, val running: Boolean, val side: Int) {
	constructor(clock: ChessClock) : this(
		clock.getCurrentTime(ChessClock.WHITE),
		clock.getCurrentTime(ChessClock.BLACK),
		clock.flagged,
		clock.running,
		clock.side
	)

	fun getCurrentTime(side: Int) = if (side == ChessClock.WHITE) tWhite else tBlack
}