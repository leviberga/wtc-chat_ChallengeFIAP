package br.com.wtc_aplicattion.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.models.TimelineResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilClienteScreen(
    navController: NavController,
    cliente: Cliente,
    timeline: TimelineResponse?
) {
    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil 360°", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2563EB),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (cliente.nome.firstOrNull() ?: '?').uppercase(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                cliente.nome,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when {
                    cliente.scoreSeguro >= 90 -> Color(0xFF10B981)
                    cliente.scoreSeguro >= 70 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }
            ) {
                Text(
                    "Score: ${cliente.scoreSeguro}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Email", cliente.email, Icons.Default.Email)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow("Telefone", cliente.telefone ?: "—", Icons.Default.Phone)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow("Segmento", cliente.segmentName ?: "—", Icons.Default.Category)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow("Status", cliente.status, Icons.Default.CheckCircle)
                    if (!cliente.notes.isNullOrBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Notas CRM", cliente.notes!!, Icons.Default.Info)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tags", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        cliente.tagsSeguras.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2563EB).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    tag,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            timeline?.lastMessages?.takeIf { it.isNotEmpty() }?.let { msgs ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Últimas mensagens (timeline)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        msgs.forEach { m ->
                            Text("• ${m.content}", fontSize = 13.sp, color = Color(0xFF374151))
                            Text(m.sentAt ?: "", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("chat/${cliente.id}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir conversa")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
