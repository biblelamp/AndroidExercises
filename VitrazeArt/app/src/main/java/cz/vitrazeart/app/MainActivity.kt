package cz.vitrazeart.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cz.vitrazeart.app.ui.VitrazeArtApp

const val MAIN_URL = "https://vitrazeart.cz/"
const val USER_AGENT = "Mozilla/5.0 (Android) VitrazeArtApp/1.0"
const val TIMEOUT = 15000

// ─── Activity ─────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VitrazeArtApp() }
    }
}
