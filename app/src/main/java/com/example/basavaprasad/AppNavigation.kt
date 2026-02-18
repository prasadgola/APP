package

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    VOICECHAT(label = "VoiceChat", Icons.Default.AccountBox),

    TEXTCHAT("TextChat", Icons.Default.Favorite),
    READTEXT("ReadText", Icons.Default.AccountBox),
}