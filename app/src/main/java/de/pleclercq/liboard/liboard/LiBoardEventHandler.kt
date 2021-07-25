package de.pleclercq.liboard.liboard

/**
 * Handler for game starts, moves and connection related events.
 */
interface LiBoardEventHandler {
    /**
     * Called when a new game starts.
     */
    fun onGameStart()

    /**
     * Called when a legal move is detected.
     */
    fun onMove()

    /**
     * Called when the physical board position changes.
     */
    fun onNewPhysicalPosition()

    /**
     * Called when a LiBoard is connected.
     */
    fun onConnect()

    /**
     * Called when the LiBoard is disconnected.
     */
    fun onDisconnect()
}