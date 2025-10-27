package br.com.wtc_aplicattion.components


import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.wtc_aplicattion.models.Mensagem
import br.com.wtc_aplicattion.viewmodel.AppState

@Composable
fun MensagemItem(mensagem: Mensagem, appState: AppState) {
    var offsetX by remember { mutableStateOf(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 100) {
                            val index = appState.mensagens.indexOfFirst { it.id == mensagem.id }
                            if (index != -1) {
                                appState.mensagens[index] = mensagem.copy(importante = !mensagem.importante)
                            }
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            },
        horizontalArrangement = if (mensagem.remetente == "operador")
            Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (mensagem.remetente == "operador")
                    Color(0xFF2563EB) else Color(0xFFF3F4F6)
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (mensagem.remetente == "operador") 16.dp else 4.dp,
                bottomEnd = if (mensagem.remetente == "operador") 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (mensagem.importante) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Importante",
                            fontSize = 10.sp,
                            color = if (mensagem.remetente == "operador") Color.White else Color(0xFF1F2937),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    mensagem.conteudo,
                    color = if (mensagem.remetente == "operador") Color.White else Color(0xFF1F2937),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    mensagem.timestamp,
                    fontSize = 10.sp,
                    color = if (mensagem.remetente == "operador")
                        Color.White.copy(alpha = 0.7f)
                    else
                        Color.Gray
                )
            }
        }
    }
}