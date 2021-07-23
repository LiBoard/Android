package de.pleclercq.liboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

internal class CreatePgnDocument: ActivityResultContract<String, Uri>() {
    override fun createIntent(context: Context, input: String?) = Intent().apply {
        action = Intent.ACTION_CREATE_DOCUMENT
        type = "application/x-chess-pgn"
        putExtra(Intent.EXTRA_TITLE, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        if (intent == null || resultCode != Activity.RESULT_OK) null
        else intent.data
}