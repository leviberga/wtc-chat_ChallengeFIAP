package br.com.wtc_aplicattion.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.wtc_aplicattion.models.Cliente

private fun statusLabel(status: String): String = when (status) {
    "ACTIVE" -> "Ativo"
    "INACTIVE" -> "Inativo"
    "LEAD" -> "Lead"
    else -> status
}

@Composable
fun ClienteCard(
    cliente: Cliente,
    onOpenChat: () -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenChat),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        cliente.nome,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        cliente.email,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = onOpenProfile) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil 360°",
                        tint = Color(0xFF2563EB)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = when {
                        cliente.scoreSeguro >= 90 -> Color(0xFF10B981)
                        cliente.scoreSeguro >= 70 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                ) {
                    Text(
                        cliente.scoreSeguro.toString(),
                        modifier = Modifier.padding(8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cliente.tagsSeguras.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2563EB).copy(alpha = 0.1f)
                    ) {
                        Text(
                            tag,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color(0xFF2563EB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                val label = statusLabel(cliente.status)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (cliente.status == "ACTIVE")
                        Color(0xFF10B981).copy(alpha = 0.1f)
                    else
                        Color(0xFF6B7280).copy(alpha = 0.1f)
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = if (cliente.status == "ACTIVE") Color(0xFF10B981) else Color(0xFF6B7280),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "📱 ${cliente.telefone ?: "—"}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (!cliente.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "📝 ${cliente.notes}",
                    fontSize = 12.sp,
                    color = Color(0xFF4F46E5),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
