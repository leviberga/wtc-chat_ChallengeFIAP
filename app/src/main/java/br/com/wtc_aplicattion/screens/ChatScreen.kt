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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.wtc_aplicattion.viewmodel.AppState
import br.com.wtc_aplicattion.components.MensagemItem
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.models.Mensagem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, appState: AppState, cliente: Cliente) {
    var novaMensagem by remember { mutableStateOf("") }
    var mostrarNotas by remember { mutableStateOf(false) }
    var notasTemp by remember { mutableStateOf(cliente.notas) }

    val mensagensCliente = appState.mensagens.filter { it.clienteId == cliente.id }

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
                                        if (cliente.status == "Ativo") Color(0xFF10B981)
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

                                appState.mensagens.add(
                                    Mensagem(
                                        id = appState.mensagens.size + 1,
                                        clienteId = cliente.id,
                                        remetente = "operador",
                                        conteudo = conteudo,
                                        timestamp = AppState.Companion.getCurrentTime()
                                    )
                                )
                                novaMensagem = ""
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mensagensCliente) { mensagem ->
                    MensagemItem(mensagem, appState)
                }
            }

            // Dialog de Notas
            if (mostrarNotas) {
                AlertDialog(
                    onDismissRequest = { mostrarNotas = false },
                    title = { Text("Notas do Cliente") },
                    text = {
                        OutlinedTextField(
                            value = notasTemp,
                            onValueChange = { notasTemp = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Adicione observações sobre o cliente...") },
                            minLines = 4
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            cliente.notas = notasTemp
                            mostrarNotas = false
                        }) {
                            Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            notasTemp = cliente.notas
                            mostrarNotas = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }

    }