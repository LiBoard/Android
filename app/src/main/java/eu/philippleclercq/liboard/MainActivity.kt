package eu.philippleclercq.liboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}