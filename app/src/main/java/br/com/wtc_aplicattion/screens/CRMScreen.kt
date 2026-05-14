package br.com.wtc_aplicattion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.compose.currentBackStackEntryAsState
import br.com.wtc_aplicattion.components.ClienteCard
import br.com.wtc_aplicattion.components.MenuItem
import br.com.wtc_aplicattion.viewmodel.AppState
import br.com.wtc_aplicattion.viewmodel.AuthViewModel
import br.com.wtc_aplicattion.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CRMScreen(navController: NavController, appState: AppState) {
    val authViewModel = remember { AuthViewModel() }
    val customerViewModel = remember { CustomerViewModel() }

    val navEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navEntry?.destination?.route
    // Recarrega ao voltar do perfil / chat / outras telas para tags e segmento refletirem o servidor.
    LaunchedEffect(rotaAtual) {
        if (rotaAtual == "crm") {
            customerViewModel.loadClientes()
        }
    }

    var busca by remember { mutableStateOf("") }
    var menuAberto by remember { mutableStateOf(false) }
    var confirmarLogout by remember { mutableStateOf(false) }

    val clientesFiltrados = customerViewModel.clientes.filter { cliente ->
        cliente.nome.contains(busca, ignoreCase = true) ||
            cliente.email.contains(busca, ignoreCase = true)
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (menuAberto) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB))
                ) {
                    Column {
                        MenuItem("📂 Segmentos") {
                            navController.navigate("segmentos")
                            menuAberto = false
                        }
                        MenuItem("📢 Campanhas Express") {
                            navController.navigate("campanhas")
                            menuAberto = false
                        }
                        MenuItem("🚪 Sair") {
                            menuAberto = false
                            confirmarLogout = true
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por nome ou e-mail...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (customerViewModel.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2563EB))
                        }
                    }
                }

                items(clientesFiltrados) { cliente ->
                    ClienteCard(
                        cliente = cliente,
                        onOpenChat = { navController.navigate("chat/${cliente.id}") },
                        onOpenProfile = { navController.navigate("perfil/${cliente.id}") }
                    )
                }
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
