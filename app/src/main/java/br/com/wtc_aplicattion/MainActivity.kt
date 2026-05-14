package br.com.wtc_aplicattion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.wtc_aplicattion.components.NotificacaoPopup
import br.com.wtc_aplicattion.models.TipoUsuario
import br.com.wtc_aplicattion.screens.*
import br.com.wtc_aplicattion.services.TokenManager
import br.com.wtc_aplicattion.ui.theme.WTCApplicationTheme
import br.com.wtc_aplicattion.viewmodel.AppState
import br.com.wtc_aplicattion.viewmodel.CustomerDetailViewModel
import br.com.wtc_aplicattion.viewmodel.CustomerViewModel

/**
 * Activity principal do aplicativo WTC CRM.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(applicationContext)
        setContent {
            WTCApplicationTheme {
                WTCApplicationApp()
            }
        }
    }
}

@Composable
fun WTCApplicationApp() {
    val navController = rememberNavController()
    val appState = remember { AppState() }

    LaunchedEffect(Unit) {
        appState.checkAuthState()
    }

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
        composable("login") {
            LoginScreen(
                navController = navController,
                appState = appState
            )
        }

        composable("crm") {
            CRMScreen(
                navController = navController,
                appState = appState
            )
        }

        composable("chat/{clienteId}") { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: return@composable
            val customerVm = remember { CustomerViewModel() }

            LaunchedEffect(clienteId) {
                customerVm.loadClientePorId(clienteId)
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                customerVm.clienteSelecionado?.let { cliente ->
                    ChatScreen(
                        navController = navController,
                        cliente = cliente
                    )
                } ?: CircularProgressIndicator()
            }
        }

        composable("segmentos") {
            SegmentsScreen(navController = navController)
        }

        composable("campanhas") {
            CampanhasScreen(navController = navController)
        }

        composable("chat_cliente") {
            ChatClienteScreen(
                navController = navController,
                appState = appState
            )
        }

        composable("perfil/{clienteId}") { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: return@composable
            val detailVm = remember(clienteId) { CustomerDetailViewModel() }

            LaunchedEffect(clienteId) {
                detailVm.load(clienteId)
            }

            val c = detailVm.cliente
            when {
                c != null -> {
                    PerfilClienteScreen(
                        navController = navController,
                        cliente = c,
                        timeline = detailVm.timeline,
                        detailVm = detailVm
                    )
                }
                detailVm.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    // Carga terminou sem cliente (erro de rede, 404, corpo vazio, etc.)
                    LaunchedEffect(clienteId) {
                        navController.navigateUp()
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        composable("configuracoes") {
            ConfiguracoesScreen(
                navController = navController,
                appState = appState
            )
        }
    }

    if (appState.mostrarCampanhaPopup && appState.campanhaPopup != null) {
        NotificacaoPopup(
            campanha = appState.campanhaPopup!!,
            onDismiss = { appState.dismissCampanhaPopup() }
        )
    }
}
