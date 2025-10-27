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
import br.com.wtc_aplicattion.models.Campanha
import br.com.wtc_aplicattion.models.CampanhaAction
import br.com.wtc_aplicattion.models.Mensagem // Importar o modelo Mensagem
import br.com.wtc_aplicattion.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampanhasScreen(navController: NavController, appState: AppState) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var segmento by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

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
                            value = body,
                            onValueChange = { body = it },
                            label = { Text("Mensagem") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = segmento,
                            onValueChange = { segmento = it },
                            label = { Text("Segmento (VIP, Premium, Regular)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("URL (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("https://wtc.com/evento") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (title.isNotBlank() && body.isNotBlank()) {
                                    val targetSegmento = segmento.ifBlank { "Todos" }
                                    val currentTime = AppState.getCurrentTime()

                                    // 1. Criar o objeto da campanha para salvar no histórico
                                    val novaCampanha = Campanha(
                                        id = appState.campanhas.size + 1,
                                        title = title,
                                        body = body,
                                        url = url.ifBlank { null },
                                        actions = listOf(
                                            CampanhaAction("btn1", "Saiba Mais"),
                                            CampanhaAction("btn2", "Inscrever-se")
                                        ),
                                        segmento = targetSegmento,
                                        data = currentTime
                                    )
                                    appState.campanhas.add(novaCampanha)

                                    // 2. Formatar a mensagem a ser enviada
                                    val campaignMessage = """
                                        ${title.trim()}
                                        
                                        ${body.trim()}
                                        ${if (url.isNotBlank()) "\nSaiba mais: ${url.trim()}" else ""}
                                    """.trimIndent()

                                    // 3. Encontrar clientes-alvo
                                    val targetClients = if (targetSegmento == "Todos") {
                                        appState.clientes
                                    } else {
                                        // Encontra clientes que tenham a tag do segmento
                                        appState.clientes.filter { cliente ->
                                            cliente.tags.any { tag ->
                                                tag.equals(targetSegmento, ignoreCase = true)
                                            }
                                        }
                                    }

                                    // 4. Enviar a mensagem para cada cliente-alvo
                                    targetClients.forEach { cliente ->
                                        appState.mensagens.add(
                                            Mensagem(
                                                id = appState.mensagens.size + 1,
                                                clienteId = cliente.id,
                                                remetente = "operador", // Enviado pelo CRM
                                                conteudo = campaignMessage,
                                                timestamp = currentTime
                                            )
                                        )
                                    }

                                    // 5. Limpar campos
                                    title = ""
                                    body = ""
                                    segmento = ""
                                    url = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            )
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
                    "Campanhas Enviadas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            items(appState.campanhas.reversed()) { campanha ->
                CampanhaCard(campanha)
            }
        }
    }
}