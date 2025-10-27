package br.com.wtc_aplicattion.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import br.com.wtc_aplicattion.viewmodel.AppState
import br.com.wtc_aplicattion.viewmodel.AuthViewModel

import br.com.wtc_aplicattion.components.ClienteCard
import br.com.wtc_aplicattion.components.MenuItem
import kotlinx.coroutines.launch // <-- IMPORTAR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CRMScreen(navController: NavController, appState: AppState) {
    val authViewModel = remember { AuthViewModel() }
    var busca by remember { mutableStateOf("") }
    var filtroTag by remember { mutableStateOf("") }
    var filtroStatus by remember { mutableStateOf("") }
    var menuAberto by remember { mutableStateOf(false) }

    // --- ADIÇÕES PARA O POPUP IN-APP ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Armazena o ID da última mensagem de cliente que gerou notificação
    var lastNotifiedMsgId by remember { mutableStateOf(-1) }

    // Efeito que observa mudanças na lista de mensagens
    LaunchedEffect(appState.mensagens.size) {
        // Pega a última mensagem, se existir
        val ultimaMensagem = appState.mensagens.lastOrNull()

        if (ultimaMensagem != null) {
            // Verifica se é uma mensagem nova, do cliente, e que ainda não foi notificada
            if (ultimaMensagem.remetente == "cliente" && ultimaMensagem.id > lastNotifiedMsgId) {
                // Atualiza o ID para não notificar de novo
                lastNotifiedMsgId = ultimaMensagem.id

                // Busca o nome do cliente
                val cliente = appState.clientes.find { it.id == ultimaMensagem.clienteId }
                val nomeCliente = cliente?.nome ?: "Cliente"
                val conteudo = ultimaMensagem.conteudo.take(40) // Limita o tamanho

                // Lança o popup (Snackbar)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Nova mensagem de $nomeCliente: $conteudo..."
                    )
                }
            }
        }
    }
    // --- FIM DAS ADIÇÕES ---


    val clientesFiltrados = appState.clientes.filter { cliente ->
        val matchBusca = cliente.nome.contains(busca, ignoreCase = true) ||
                cliente.email.contains(busca, ignoreCase = true)
        val matchTag = filtroTag.isEmpty() || cliente.tags.contains(filtroTag)
        val matchStatus = filtroStatus.isEmpty() || cliente.status == filtroStatus
        matchBusca && matchTag && matchStatus
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WTC CRM", fontWeight = FontWeight.Bold)
                        Text(
                            appState.usuarioLogado?.nome ?: "",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2563EB),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { menuAberto = !menuAberto }) {
                        Icon(
                            if (menuAberto) Icons.Default.Close else Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        // --- ADIÇÃO DO SNACKBARHOST ---
        snackbarHost = { SnackbarHost(snackbarHostState) }
        // --- FIM DA ADIÇÃO ---
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Menu Dropdown
            if (menuAberto) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB))
                ) {
                    Column {
                        MenuItem("📊 Dashboard") {
                            menuAberto = false
                        }
                        MenuItem("📢 Campanhas Express") {
                            navController.navigate("campanhas")
                            menuAberto = false
                        }
                        MenuItem("🚪 Sair") {
                            authViewModel.signOut()
                            appState.usuarioLogado = null
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                            menuAberto = false
                        }
                    }
                }
            }

            // Busca
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar cliente...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtros
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filtroTag.isEmpty(),
                            onClick = { filtroTag = "" },
                            label = { Text("Todos") }
                        )
                        FilterChip(
                            selected = filtroTag == "VIP",
                            onClick = { filtroTag = if (filtroTag == "VIP") "" else "VIP" },
                            label = { Text("VIP") }
                        )
                        FilterChip(
                            selected = filtroTag == "Premium",
                            onClick = { filtroTag = if (filtroTag == "Premium") "" else "Premium" },
                            label = { Text("Premium") }
                        )
                        FilterChip(
                            selected = filtroStatus == "Ativo",
                            onClick = { filtroStatus = if (filtroStatus == "Ativo") "" else "Ativo" },
                            label = { Text("Ativos") }
                        )
                    }
                }
            }

            // Lista de Clientes
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clientesFiltrados) { cliente ->
                    ClienteCard(cliente) {
                        navController.navigate("chat/${cliente.id}")
                    }
                }
            }
        }
    }
}