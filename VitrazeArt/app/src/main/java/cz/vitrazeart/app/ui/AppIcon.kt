package cz.vitrazeart.app.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import cz.vitrazeart.app.R

// ─── Иконка приложения ────────────────────────────────────────────────────────

@Composable
fun AppIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        androidx.core.content.res.ResourcesCompat.getDrawable(
            context.resources, R.mipmap.ic_launcher_round, context.theme
        )?.let { drawable ->
            val bmp = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            android.graphics.Canvas(bmp).also {
                drawable.setBounds(0, 0, it.width, it.height)
                drawable.draw(it)
            }
            bmp
        }
    }
    if (bitmap != null) {
        Image(
            bitmap             = bitmap.asImageBitmap(),
            contentDescription = "Vitrazeart",
            modifier           = modifier
        )
    }
}