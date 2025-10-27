package br.com.wtc_aplicattion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.wtc_aplicattion.ui.theme.WTCApplicationTheme
import br.com.wtc_aplicattion.screens.*
import br.com.wtc_aplicattion.viewmodel.AppState
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.models.TipoUsuario
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

/**
 * Activity principal do aplicativo WTC CRM.
 * Responsável por inicializar o tema e a navegação.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplica o tema do aplicativo
            WTCApplicationTheme {
                WTCApplicationApp()
            }
        }
    }
}

/**
 * Composable principal que configura a navegação do aplicativo.
 * Gerencia o fluxo de navegação entre todas as telas.
 */
@Composable
fun WTCApplicationApp() {
    // Cria o NavController para gerenciar a navegação
    val navController = rememberNavController()

    // Cria o estado compartilhado do aplicativo
    val appState = remember { AppState() }

    // Contexto local para possíveis integrações futuras
    val context = LocalContext.current

    // Verifica o estado de autenticação na inicialização
    LaunchedEffect(Unit) {
        appState.checkAuthState()
    }

    // Simular notificação push após login do cliente
    LaunchedEffect(appState.usuarioLogado?.tipo) {
        if (appState.usuarioLogado?.tipo == TipoUsuario.CLIENTE) {
            kotlinx.coroutines.delay(3000)
            // Verificar se ainda é cliente após o delay
            if (appState.usuarioLogado?.tipo == TipoUsuario.CLIENTE && !appState.mostrarNotificacao) {
                appState.notificacaoAtual = appState.campanhas.firstOrNull()
                appState.mostrarNotificacao = true
            }
        } else {
            // Limpar notificação quando não for cliente
            appState.mostrarNotificacao = false
            appState.notificacaoAtual = null
        }
    }

    // Debug: Log do estado da notificação (remover após teste)
    LaunchedEffect(appState.mostrarNotificacao, appState.notificacaoAtual) {
        println("🔔 DEBUG: mostrarNotificacao = ${appState.mostrarNotificacao}, notificacaoAtual = ${appState.notificacaoAtual?.title}")
    }

    // Define o grafo de navegação do aplicativo
    NavHost(
        navController = navController,
        startDestination = if (appState.isUserAuthenticated()) {
            when (appState.usuarioLogado?.tipo) {
                TipoUsuario.OPERADOR -> "crm"
                TipoUsuario.CLIENTE -> "chat_cliente"
                null -> "login"
            }
        } else "login"
    ) {
        // Tela de Login
        composable("login") {
            LoginScreen(
                navController = navController,
                appState = appState
            )
        }

        // Tela CRM - Lista de clientes (Operador)
        composable("crm") {
            CRMScreen(
                navController = navController,
                appState = appState
            )
        }

        // Tela de Chat - Conversa com cliente específico
        composable("chat/{clienteId}") { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId")?.toIntOrNull()
            val cliente = appState.clientes.find { it.id == clienteId }

            if (cliente != null) {
                ChatScreen(
                    navController = navController,
                    appState = appState,
                    cliente = cliente
                )
            } else {
                // Se cliente não encontrado, volta para CRM
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        // Tela de Campanhas Express
        composable("campanhas") {
            CampanhasScreen(
                navController = navController,
                appState = appState
            )
        }

        // Tela de Chat do Cliente
        composable("chat_cliente") {
            ChatClienteScreen(
                navController = navController,
                appState = appState
            )
        }

        // Tela de Perfil do Cliente (adicional)
        composable("perfil/{clienteId}") { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId")?.toIntOrNull()
            val cliente = appState.clientes.find { it.id == clienteId }

            if (cliente != null) {
                PerfilClienteScreen(
                    navController = navController,
                    appState = appState,
                    cliente = cliente
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        // Tela de Configurações (adicional)
        composable("configuracoes") {
            ConfiguracoesScreen(
                navController = navController,
                appState = appState
            )
        }
    }

    // Exibir notificação popup quando necessário (fora do NavHost)
    if (appState.mostrarNotificacao && appState.notificacaoAtual != null) {
        println("🔔 RENDER: Tentando renderizar popup")
        
        // Usar o componente NotificacaoPopup original
        br.com.wtc_aplicattion.components.NotificacaoPopup(
            campanha = appState.notificacaoAtual!!,
            onDismiss = {
                println("🔔 DISMISS: Popup foi fechado")
                appState.mostrarNotificacao = false
                appState.notificacaoAtual = null
            }
        )
    } else {
        println("🔔 RENDER: Condições não atendidas - mostrarNotificacao: ${appState.mostrarNotificacao}, notificacaoAtual: ${appState.notificacaoAtual?.title}")
    }
}

// Funções movidas para arquivos separados
