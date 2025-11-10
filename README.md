# WTC Chat - Challenge FIAP

## Visão geral

WTC Chat é um projeto Android (Kotlin/Gradle) criado como parte do Challenge da FIAP em colabopração com o World Trade Center. O objetivo é uma aplicação que forneça comunicação clara, transparente e otimizada, além de atalhos e comandos que facilitem esse processo.

Este repositório contém a aplicação móvel, recursos e configuração necessária para compilar e executar o app localmente.


## Funcionalidades

- Lista das principais funcionalidades implementadas:
	- Autenticação de usuário (e-mail/senha ou provedor social)
	- Envio e recebimento de mensagens em tempo real
	- Visualização de histórico de conversas
	- Notificações push (via Firebase Cloud Messaging)

## Prints das telas

Algumas imagens das principais telas do app.


![Login](docs/screenshots/LoginScreen.png)

Telas do operador:

![Dashboard](docs/screenshots/Dashboard.png)
![Chat](docs/screenshots/ChatScreen.png)
![Notas](docs/screenshots/Notes.png)
![Criar campanhas](docs/screenshots/NewCampaign.png)

Visão do cliente (Chat e notificações):
![PopUp](docs/screenshots/PopUp.png)


## Tecnologias

As tecnologias e ferramentas utilizadas neste projeto:

- Kotlin: (1.9.0)
- Android Gradle Plugin (AGP): 8.12.3
- Android (compileSdk = 34, targetSdk = 34, minSdk = 24)
- Jetpack Compose
- Firebase (Messaging, Analytics, Auth) via Firebase BoM
- Retrofit + OkHttp para comunicação HTTP
- Kotlin Coroutines (kotlinx-coroutines)
- Coil (imagem, via `coil-compose`)

O projeto usa também bibliotecas do AndroidX (Core KTX, Lifecycle, Activity-Compose) e configurações para testes (JUnit, Espresso, Compose UI testing).

## Dependências

Dependências principais:

- AndroidX & Core
	- androidx.core:core-ktx:1.12.0
	- androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
	- androidx.activity:activity-compose:1.8.2

- Jetpack Compose (BOM + módulos)
	- platform: androidx.compose:compose-bom:2024.02.00
	- androidx.compose.ui:ui
	- androidx.compose.ui:ui-graphics
	- androidx.compose.ui:ui-tooling-preview
	- androidx.compose.material3:material3
	- androidx.compose.material:material-icons-extended

- Navegação
	- androidx.navigation:navigation-compose:2.7.6

- Firebase (via BoM)
	- platform: com.google.firebase:firebase-bom:32.7.0
	- com.google.firebase:firebase-messaging-ktx
	- com.google.firebase:firebase-analytics-ktx
	- com.google.firebase:firebase-auth-ktx

- Rede / API
	- com.squareup.retrofit2:retrofit:2.9.0
	- com.squareup.retrofit2:converter-gson:2.9.0
	- com.squareup.okhttp3:logging-interceptor:4.12.0

- Coroutines
	- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
	- org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3

- Imagens
	- io.coil-kt:coil-compose:2.5.0

- Debug / Testing
	- debugImplementation: androidx.compose.ui:ui-tooling
	- testImplementation: junit:junit:4.13.2
	- androidTestImplementation: androidx.test.ext:junit:1.1.5
	- androidTestImplementation: androidx.test.espresso:espresso-core:3.5.1
	- androidTestImplementation (compose testing): androidx.compose.ui:ui-test-junit4 (via compose BOM)

Observação: o projeto também referencia versões e aliases em `gradle/libs.versions.toml` (ex.: compose BOM, lifecycle e activity-compose). Consulte `app/build.gradle.kts` para confirmar qualquer versão específica.

## Requisitos para rodar localmente

Requisitos mínimos:

- JDK 11 ou superior (recomendado: Temurin / OpenJDK)
- Android Studio compatível com AGP 8.12.3 (versões mais recentes do Android Studio são recomendadas)
- Android SDK com API 34 (compileSdk = 34) e build-tools compatíveis
- Dispositivo Android ou emulador com API >= 24 (minSdk = 24)

Configurações específicas:

1. Instale o JDK e o Android Studio.
2. Abra o projeto no Android Studio (arquivo `settings.gradle.kts`).
3. Caso o projeto utilize Firebase, substitua `app/google-services.json` pelo seu arquivo do Firebase.
4. Sincronize o Gradle pelo Android Studio ou use o Gradle wrapper.

### Comandos (Windows PowerShell)

No diretório raiz do projeto (`WTC_APLICATTION`), você pode usar o Gradle wrapper:

```powershell
# Limpar e compilar
.\gradlew.bat clean assembleDebug

# Instalar no dispositivo/emulador conectado
.\gradlew.bat installDebug

# Rodar testes unitários (se existirem)
.\gradlew.bat test
```

Observação: executar pela interface do Android Studio facilita o uso de emuladores e depuração.

## Estrutura do repositório (resumo)

- `app/` — módulo Android com código-fonte, recursos e `build.gradle.kts`
- `gradle/`, `build/`, wrappers — configuração do Gradle
- `README.md` — este arquivo

## Como contribuir

1. Fork o repositório e crie uma branch com a sua feature/bugfix: `git checkout -b feature/nome-do-recurso`.
2. Faça commits pequenos e claros.
3. Abra um Pull Request descrevendo as mudanças.

Dicas:
- Execute e verifique a aplicação localmente antes de submeter PRs.
- Inclua screenshots quando a UI for alterada.

## Sugestões para o futuro

- Testes automatizados (instrumentation + unit tests)
- CI/CD (GitHub Actions) para build e lint automático
- Suporte a múltiplas linguagens (i18n)
- Melhorias de UX (preview de mensagens, envio de mídia)
- Otimizações de performance e redução de consumo de dados

## Troubleshooting

- Erro de SDK / compileSdk: Verifique o Android SDK instalado e a variável `ANDROID_HOME`.
- Problemas com `google-services.json`: confirme que o arquivo corresponde ao pacote (applicationId) do app.
- Build falhando por versão do JDK: tente instalar/usar JDK 11.

## Contato

Desenvolvido por:

![Levi Bergamascki](https://github.com/leviberga)
![Lanna Fábia](https://github.com/Lannizz)