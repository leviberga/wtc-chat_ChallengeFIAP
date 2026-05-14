package br.com.wtc_aplicattion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.wtc_aplicattion.viewmodel.SegmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentsScreen(navController: NavController) {
    val vm = remember { SegmentViewModel() }
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var criando by remember { mutableStateOf(false) }
    var excluirId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Segmentos", fontWeight = FontWeight.Bold) },
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
                    "Criar segmento",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            item {
                Card(elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = nome,
                            onValueChange = { nome = it },
                            label = { Text("Nome") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = descricao,
                            onValueChange = { descricao = it },
                            label = { Text("Descrição (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                        vm.errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = Color.Red, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (nome.isBlank()) return@Button
                                criando = true
                                vm.create(nome, descricao) { ok ->
                                    criando = false
                                    if (ok) {
                                        nome = ""
                                        descricao = ""
                                    }
                                }
                            },
                            enabled = !criando && nome.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (criando) CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            else Text("Criar segmento")
                        }
                    }
                }
            }

            item {
                Text(
                    "Segmentos cadastrados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            if (vm.isLoading && vm.segmentos.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                }
            }

            items(vm.segmentos, key = { it.id }) { seg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(seg.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (!seg.description.isNullOrBlank()) {
                                Text(seg.description!!, fontSize = 13.sp, color = Color.Gray)
                            }
                            Text(
                                "${seg.customerCount} cliente(s)",
                                fontSize = 12.sp,
                                color = Color(0xFF2563EB)
                            )
                        }
                        IconButton(onClick = { excluirId = seg.id }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
    }

    excluirId?.let { id ->
        AlertDialog(
            onDismissRequest = { excluirId = null },
            title = { Text("Excluir segmento") },
            text = { Text("Tem certeza? Clientes deste segmento ficarão sem segmento se o backend assim definir.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.delete(id) { excluirId = null }
                    }
                ) { Text("Excluir", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { excluirId = null }) { Text("Cancelar") }
            }
        )
    }
}
