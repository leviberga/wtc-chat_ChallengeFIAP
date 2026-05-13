package br.com.wtc_aplicattion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.wtc_aplicattion.components.MensagemClienteItem
import br.com.wtc_aplicattion.models.Mensagem
import br.com.wtc_aplicattion.viewmodel.AppState
import br.com.wtc_aplicattion.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatClienteScreen(navController: NavController, appState: AppState) {
    val authViewModel = remember { AuthViewModel() }
    var novaMensagem by remember { mutableStateOf("") }

    val mensagensCliente = appState.mensagens.filter { it.clienteId == "conv_cliente" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat WTC", fontWeight = FontWeight.Bold)
                        Text(
                            "Atendimento ao Cliente",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4F46E5),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        appState.notificacaoAtual = appState.campanhas.firstOrNull()
                        appState.mostrarNotificacao = true
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificação",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        authViewModel.signOut()
                        appState.usuarioLogado = null
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Sair",
                            tint = Color.White
                        )
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
                        placeholder = { Text("Digite sua mensagem...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (novaMensagem.isNotBlank()) {
                                appState.mensagens.add(
                                    Mensagem(
                                        id = (appState.mensagens.size + 1).toString(),
                                        conversationId = "conv_cliente",
                                        senderId = "cliente",
                                        conteudo = novaMensagem,
                                        tipo = "TEXT",
                                        mediaUrl = null,
                                        deeplinkUrl = null,
                                        status = "SENT",
                                        timestamp = AppState.getCurrentTime()
                                    )
                                )
                                novaMensagem = ""
                            }
                        },
                        containerColor = Color(0xFF4F46E5)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mensagensCliente) { mensagem ->
                MensagemClienteItem(mensagem)
            }
        }
    }
}