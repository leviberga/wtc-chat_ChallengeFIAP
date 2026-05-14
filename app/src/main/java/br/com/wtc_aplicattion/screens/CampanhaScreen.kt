package br.com.wtc_aplicattion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.wtc_aplicattion.components.CampanhaCard
import br.com.wtc_aplicattion.viewmodel.CampaignViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampanhasScreen(navController: NavController) {
    val campaignVm = remember { CampaignViewModel() }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var segmentId by remember { mutableStateOf<String?>(null) }
    var expandedSeg by remember { mutableStateOf(false) }
    var deeplinkUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        campaignVm.loadCampaignsAndSegments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campanhas Express", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2563EB),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Nova Campanha",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            item {
                Card(elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título da Campanha") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Mensagem") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedSeg,
                            onExpandedChange = { expandedSeg = !expandedSeg }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                value = campaignVm.segmentos.find { it.id == segmentId }?.name
                                    ?: "Selecione o segmento",
                                onValueChange = {},
                                label = { Text("Segmento") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSeg) }
                            )
                            ExposedDropdownMenu(
                                expanded = expandedSeg,
                                onDismissRequest = { expandedSeg = false }
                            ) {
                                campaignVm.segmentos.forEach { seg ->
                                    DropdownMenuItem(
                                        text = { Text("${seg.name} (${seg.customerCount})") },
                                        onClick = {
                                            segmentId = seg.id
                                            expandedSeg = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = deeplinkUrl,
                            onValueChange = { deeplinkUrl = it },
                            label = { Text("Deeplink URL (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("https://wtc.com/evento") }
                        )

                        campaignVm.errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = Color.Red, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (title.isNotBlank() && content.isNotBlank()) {
                                    campaignVm.createCampaign(
                                        title = title,
                                        content = content,
                                        segmentId = segmentId,
                                        deeplinkUrl = deeplinkUrl
                                    ) {
                                        title = ""
                                        content = ""
                                        segmentId = null
                                        deeplinkUrl = ""
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            ),
                            enabled = !campaignVm.isLoading
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Campanha")
                        }
                    }
                }
            }

            item {
                Text(
                    "Campanhas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            items(campaignVm.campanhas.reversed()) { campanha ->
                CampanhaCard(campanha)
            }
        }
    }
}
