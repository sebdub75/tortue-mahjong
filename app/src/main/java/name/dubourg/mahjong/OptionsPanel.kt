package name.dubourg.mahjong

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Écran d'options, posé par-dessus le plateau ; « Fermer » ou le retour le referment. */
@Composable
fun OptionsPanel(settings: Settings, onClose: () -> Unit) {
    ModalPanel(onDismiss = onClose) {
        BasicText("Options", style = TextStyle(TextColor, 28.sp, FontWeight.Bold))
        Column {
            CheckOption("Difficile", settings.hard) { settings.hard = it }
            // Sans objet en mode normal : la donne y est toujours soluble.
            if (settings.hard) {
                CheckOption("Solution garantie", settings.guaranteed, Modifier.padding(start = 32.dp)) {
                    settings.guaranteed = it
                }
            }
            // Les donnes expert sont choisies parmi les solubles : sans objet sans garantie.
            if (settings.hard && settings.guaranteed) {
                CheckOption("Difficulté garantie", settings.expert, Modifier.padding(start = 64.dp)) {
                    settings.expert = it
                }
            }
        }
        ActionButton("Fermer", onClick = onClose)
    }
}

/**
 * Panneau centré sur un voile sombre, par-dessus le plateau. Un toucher sur le voile ou
 * le retour appellent [onDismiss].
 */
@Composable
internal fun ModalPanel(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onDismiss)
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            // Le voile absorbe les touchers : le plateau dessous ne réagit pas.
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .background(Color(0xEE0D2B21), RoundedCornerShape(16.dp))
                .clickable(remember { MutableInteractionSource() }, indication = null) {}
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

@Composable
private fun CheckOption(text: String, checked: Boolean, modifier: Modifier = Modifier, onChange: (Boolean) -> Unit) {
    Row(
        modifier
            .clickable { onChange(!checked) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(28.dp)
                .background(if (checked) ButtonColor else Color.Transparent, RoundedCornerShape(6.dp))
                .border(2.dp, TextColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) BasicText("✓", style = TextStyle(TextColor, 20.sp, FontWeight.Bold))
        }
        BasicText(text, style = TextStyle(TextColor, 18.sp))
    }
}
