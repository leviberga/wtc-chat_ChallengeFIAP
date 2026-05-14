package br.com.wtc_aplicattion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.wtc_aplicattion.components.MensagemClienteItem
import br.com.wtc_aplicattion.viewmodel.AppState
import br.com.wtc_aplicattion.viewmodel.AuthViewModel
import br.com.wtc_aplicattion.viewmodel.CampaignViewModel
import br.com.wtc_aplicattion.viewmodel.ConversationViewModel
import br.com.wtc_aplicattion.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatClienteScreen(navController: NavController, appState: AppState) {
    val authViewModel = remember { AuthViewModel() }
    val customerVm = remember { CustomerViewModel() }
    val conversationVm = remember { ConversationViewModel() }
    val campaignVm = remember { CampaignViewModel() }

    var customerId by remember { mutableStateOf<String?>(null) }
    var segmentId by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var novaMensagem by remember { mutableStateOf("") }
    var confirmarLogout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        customerVm.resolveLoggedInCustomerId { id ->
            customerId = id
            if (id == null) {
                aviso =
                    "Não encontramos seu cadastro de cliente no CRM. Peça ao atendente para cadastrar seu e-mail."
            } else {
                customerVm.loadClientePorId(id)
                campaignVm.loadCampaignsAndSegments()
            }
        }
    }

    LaunchedEffect(customerId) {
        val id = customerId ?: return@LaunchedEffect
        conversationVm.loadMensagens(id, asOperator = false)
        conversationVm.startPolling(id, asOperator = false)
    }

    var ultimaCampanhaPopupId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(campaignVm.campanhas, customerId, segmentId) {
        val id = customerId ?: return@LaunchedEffect
        val camp = campaignVm.pickLatestForCustomer(id, segmentId)
        if (camp != null && camp.id != ultimaCampanhaPopupId) {
            ultimaCampanhaPopupId = camp.id
            appState.campanhaPopup = camp
            appState.mostrarCampanhaPopup = true
        }
    }

    LaunchedEffect(customerVm.clienteSelecionado) {
        segmentId = customerVm.clienteSelecionado?.segmentId
    }

    DisposableEffect(Unit) {
        onDispose { conversationVm.stopPolling() }
    }

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
                        campaignVm.loadCampaignsAndSegments()
                        val c = campaignVm.pickLatestForCustomer(customerId, segmentId)
                        if (c != null) {
                            appState.campanhaPopup = c
                            appState.mostrarCampanhaPopup = true
                        }
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificação",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { confirmarLogout = true }) {
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
                        maxLines = 3,
                        enabled = customerId != null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            val id = customerId ?: return@FloatingActionButton
                            if (novaMensagem.isNotBlank()) {
                                conversationVm.enviarMensagem(
                                    id,
                                    novaMensagem,
                                    asOperator = false
                                ) {
                                    novaMensagem = ""
                                }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            aviso?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                ) {
                    Text(it, modifier = Modifier.padding(12.dp), color = Color(0xFF9A3412))
                }
            }

            if (conversationVm.isLoading && conversationVm.mensagens.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4F46E5))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversationVm.mensagens) { mensagem ->
                        MensagemClienteItem(mensagem)
                    }
                }
            }

            conversationVm.errorMessage?.let { err ->
                Text(
                    err,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp
                )
            }
        }
    }

    if (confirmarLogout) {
        AlertDialog(
            onDismissRequest = { confirmarLogout = false },
            title = { Text("Sair") },
            text = { Text("Tem certeza que deseja sair?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.signOut()
                        appState.logout()
                        confirmarLogout = false
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Sair", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLogout = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
