package br.com.wtc_aplicattion.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.wtc_aplicattion.components.MensagemItem
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.models.CustomerUpdateRequest
import br.com.wtc_aplicattion.services.RetrofitClient
import br.com.wtc_aplicattion.services.TokenManager
import br.com.wtc_aplicattion.viewmodel.ConversationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, cliente: Cliente) {
    var novaMensagem by remember { mutableStateOf("") }
    var mostrarNotas by remember { mutableStateOf(false) }
    var notasTemp by remember(cliente.id) { mutableStateOf(cliente.notes ?: "") }
    var salvandoNotas by remember { mutableStateOf(false) }
    var erroNotas by remember { mutableStateOf<String?>(null) }

    val conversationViewModel = remember { ConversationViewModel() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cliente.id) {
        notasTemp = cliente.notes ?: ""
        conversationViewModel.loadMensagens(cliente.id, asOperator = true)
        conversationViewModel.startPolling(cliente.id, asOperator = true)
    }

    DisposableEffect(cliente.id) {
        onDispose {
            conversationViewModel.stopPolling()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(cliente.nome, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (cliente.status == "ACTIVE") Color(0xFF10B981)
                                        else Color(0xFF6B7280)
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                cliente.status,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2563EB),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { mostrarNotas = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Notas", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = novaMensagem,
                        onValueChange = { novaMensagem = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Digite /promo, /boleto ou sua mensagem...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (novaMensagem.isNotBlank()) {
                                val conteudo = when {
                                    novaMensagem.contains("/promo", ignoreCase = true) ->
                                        "🎉 Confira nossas promoções especiais! Acesse: wtc.com/promocoes"
                                    novaMensagem.contains("/boleto", ignoreCase = true) ->
                                        "📄 Segunda via de boleto: wtc.com/boletos"
                                    novaMensagem.contains("/agradecer", ignoreCase = true) ->
                                        "🙏 Obrigado por sua preferência! Estamos sempre à disposição."
                                    else -> novaMensagem
                                }
                                conversationViewModel.enviarMensagem(
                                    cliente.id,
                                    conteudo,
                                    asOperator = true
                                ) {
                                    novaMensagem = ""
                                }
                            }
                        },
                        containerColor = Color(0xFF2563EB)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (conversationViewModel.isLoading && conversationViewModel.mensagens.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2563EB)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversationViewModel.mensagens) { mensagem ->
                        MensagemItem(mensagem)
                    }
                }
            }

            conversationViewModel.errorMessage?.let { err ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(err, color = Color.White, fontSize = 13.sp)
                        TextButton(onClick = { conversationViewModel.clearError() }) {
                            Text("OK", color = Color.White)
                        }
                    }
                }
            }

            if (mostrarNotas) {
                AlertDialog(
                    onDismissRequest = { mostrarNotas = false },
                    title = { Text("Notas do Cliente") },
                    text = {
                        Column {
                            if (erroNotas != null) {
                                Text(erroNotas!!, color = Color.Red, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            OutlinedTextField(
                                value = notasTemp,
                                onValueChange = { notasTemp = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Adicione observações sobre o cliente...") },
                                minLines = 4
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    salvandoNotas = true
                                    erroNotas = null
                                    try {
                                        val token = TokenManager.getBearerToken() ?: return@launch
                                        val body = CustomerUpdateRequest(
                                            name = cliente.nome,
                                            email = cliente.email,
                                            phone = cliente.telefone,
                                            segmentId = cliente.segmentId,
                                            tags = cliente.tagsSeguras,
                                            score = cliente.scoreSeguro,
                                            customerStatus = cliente.status,
                                            notes = notasTemp.ifBlank { null }
                                        )
                                        val r = RetrofitClient.instance.updateCustomer(
                                            token,
                                            cliente.id,
                                            body
                                        )
                                        if (r.isSuccessful) {
                                            mostrarNotas = false
                                        } else {
                                            erroNotas = r.errorBody()?.string()?.take(120)
                                                ?: "Erro ao salvar"
                                        }
                                    } catch (e: Exception) {
                                        erroNotas = e.message
                                    } finally {
                                        salvandoNotas = false
                                    }
                                }
                            },
                            enabled = !salvandoNotas
                        ) {
                            if (salvandoNotas) CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            else Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarNotas = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
