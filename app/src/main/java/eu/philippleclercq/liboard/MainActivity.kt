package eu.philippleclercq.liboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), LiBoard.GameEventHandler {
    private lateinit var liBoard: LiBoard
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        liBoard = LiBoard(this, this)
    }

    override fun onGameStart() {
        runOnUiThread { textbox.text = liBoard.board.toString() }
    }

    override fun onMove() {
        runOnUiThread { textbox.text = liBoard.board.toString() }
    }
}