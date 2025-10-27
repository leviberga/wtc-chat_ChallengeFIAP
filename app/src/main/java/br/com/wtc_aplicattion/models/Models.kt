package br.com.wtc_aplicattion.models

data class Cliente(
    val id: Int,
    val nome: String,
    val email: String,
    val telefone: String,
    val tags: List<String>,
    val score: Int,
    val status: String,
    val ultimaCompra: String,
    var notas: String = ""
)

data class Mensagem(
    val id: Int,
    val clienteId: Int,
    val remetente: String,
    val conteudo: String,
    val timestamp: String,
    var importante: Boolean = false,
    var tipo: TipoMensagem = TipoMensagem.TEXTO
)

enum class TipoMensagem {
    TEXTO, CAMPANHA
}

data class Campanha(
    val id: Int,
    val title: String,
    val body: String,
    val url: String?,
    val actions: List<CampanhaAction>,
    val segmento: String,
    val data: String
)

data class CampanhaAction(
    val action: String,
    val title: String,
    val url: String? = null
)

data class Usuario(
    val nome: String,
    val tipo: TipoUsuario
)

enum class TipoUsuario {
    OPERADOR, CLIENTE
}