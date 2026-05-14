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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import br.com.wtc_aplicattion.viewmodel.CustomerDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilClienteScreen(
    navController: NavController,
    cliente: Cliente,
    timeline: TimelineResponse?,
    detailVm: CustomerDetailViewModel
) {
    val scroll = rememberScrollState()
    var tagInput by remember(cliente.id) {
        mutableStateOf(cliente.tagsSeguras.joinToString(", "))
    }
    var selectedSegmentId by remember(cliente.id) {
        mutableStateOf(cliente.segmentId)
    }
    var segmentMenuExpanded by remember { mutableStateOf(false) }
    var crmFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        detailVm.loadSegments()
    }

    LaunchedEffect(cliente.tagsSeguras, cliente.segmentId, cliente.id) {
        tagInput = cliente.tagsSeguras.joinToString(", ")
        selectedSegmentId = cliente.segmentId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil CRM", fontWeight = FontWeight.Bold) },
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
                    InfoRow("Segmento atual", cliente.segmentName ?: "—", Icons.Default.Category)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow("Status", cliente.status, Icons.Default.CheckCircle)
                    if (!cliente.notes.isNullOrBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Notas CRM", cliente.notes, Icons.Default.Info)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Classificação CRM", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Defina o segmento e as tags deste cliente.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Segmento", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = segmentMenuExpanded,
                        onExpandedChange = { segmentMenuExpanded = !segmentMenuExpanded }
                    ) {
                        val label = when (val segId = selectedSegmentId) {
                            null -> "Sem segmento"
                            else -> detailVm.segmentos.find { it.id == segId }?.name ?: segId
                        }
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            value = label,
                            onValueChange = {},
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = segmentMenuExpanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = segmentMenuExpanded,
                            onDismissRequest = { segmentMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sem segmento") },
                                onClick = {
                                    selectedSegmentId = null
                                    segmentMenuExpanded = false
                                    crmFeedback = null
                                }
                            )
                            detailVm.segmentos.forEach { seg ->
                                DropdownMenuItem(
                                    text = { Text("${seg.name} (${seg.customerCount})") },
                                    onClick = {
                                        selectedSegmentId = seg.id
                                        segmentMenuExpanded = false
                                        crmFeedback = null
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tags", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text(
                        "Separe por vírgula (ex.: vip, finance)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = {
                            tagInput = it
                            crmFeedback = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("vip, finance") },
                        singleLine = false,
                        minLines = 2
                    )

                    crmFeedback?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            it,
                            color = if (it == "Dados salvos.") Color(0xFF059669) else Color.Red,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Não há segmentos? Use o menu do CRM → Segmentos para criar.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val tags = tagInput.split(",")
                                .map { s -> s.trim() }
                                .filter { s -> s.isNotEmpty() }
                            detailVm.saveCrmFields(cliente.id, tags, selectedSegmentId) { ok, err ->
                                crmFeedback = if (ok) "Dados salvos." else (err ?: "Erro ao salvar")
                            }
                        },
                        enabled = !detailVm.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (detailVm.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar segmento e tags")
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
