package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight

@Composable
fun LedgerBadge(
    modifier: Modifier = Modifier,
    isValid: Boolean = true,
    hash: String? = null,
    onClick: (() -> Unit)? = null
) {
    val bgColor = if (isValid) PaidGreenLight else Color(0xFFFEE2E2)
    val contentColor = if (isValid) PaidGreen else Color(0xFFDC2626)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isValid) Icons.Default.Lock else Icons.Default.Warning,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = if (hash != null) "🔒 SHA-256: ${hash.take(8)}..." else if (isValid) "🔒 Registre Certifié" else "⚠️ Alerte Altération",
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
