# 🚀 Appium Wikipedia Framework - Mobile Test Automation

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Appium](https://img.shields.io/badge/Appium-2.x-blue.svg)](http://appium.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> Framework robusto de automação mobile para Android, aplicando pirâmide de testes, Page Object Model e melhores práticas de QA Engineering.

---

## Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Execução dos Testes](#execução-dos-testes)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Relatórios](#relatórios)
- [Cobertura de Código](#cobertura-de-código)
- [CI/CD](#cicd)
- [Roadmap](#roadmap)
- [Casos de Teste](#casos-de-teste)
- [Autor](#autor)
- [Contribuindo](#contribuindo)
- [Licença](#licença)
- [Agradecimentos](#agradecimentos)
- [Status do Projeto](#status-do-projeto)


---

## Sobre o Projeto

Este projeto implementa um **framework de automação mobile completo** para testar o aplicativo Wikipedia (Android), seguindo as melhores práticas de mercado:

- **Pirâmide de Testes**: 70% unitários, 30% E2E
- **Page Object Model (POM)**: Separação de responsabilidades
- **Cobertura de Código**: ≥80% via JaCoCo
- **Gestão de Flakiness**: Waits explícitos, retry mechanism
- **Relatórios Visuais**: Allure Report com screenshots
- **CI/CD**: GitHub Actions automatizado

### Objetivo

Desenvolvimento de competências técnicas em **QA Engineering** e **Test Automation**, servindo como portfólio profissional.

---

## Arquitetura

### Pirâmide de Testes
```
         /\   
        /  \
       /E2E \          7 testes críticos (Appium + TestNG)
      /------\
     /        \       (Conceitual - futuro)
    /----------\
   /  Unitários \    10-15 testes (JUnit 5 + Mockito)
  /--------------\
```

### Estratégia de Testes

| Camada | Quantidade | Ferramenta | Foco |
|--------|-----------|------------|------|
| **Unitários** | 10-15 | JUnit 5 + Mockito | Commons/Actions, Helpers |
| **Integração** | 0 (futuro) | REST Assured | API Wikipedia (conceitual) |
| **E2E** | 7 | Appium + TestNG | Fluxos críticos de negócio |

---

## Tecnologias

### Core
- **Java 17+** - Linguagem base
- **Maven 3.9+** - Gerenciamento de dependências
- **Appium 2.x** - Automação mobile
- **Appium Java Client 10.x** - Bindings Java

### Test Frameworks
- **TestNG** - Runner para testes E2E
- **JUnit 5** - Testes unitários
- **Mockito 5.x** - Mocking para unitários

### Quality & Reports
- **JaCoCo 0.8.11** - Cobertura de código
- **Allure 2.24+** - Relatórios visuais
- **SLF4J + Logback** - Logging estruturado

### CI/CD
- **GitHub Actions** - Pipeline automatizado
- **Android Emulator** - Execução de testes

---

## Pré-requisitos

### Sistema
- **SO**: Windows 10+, macOS 11+, ou Linux (Ubuntu 20.04+)
- **RAM**: 8GB mínimo (16GB recomendado)
- **Disco**: 10GB livres

### Ferramentas Obrigatórias

#### 1. Java Development Kit (JDK)
```bash
# Verificar instalação
java -version
javac -version

# Deve retornar: Java 17 ou superior
```

**Download:** [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) ou [OpenJDK](https://adoptium.net/)

#### 2. Maven
```bash
# Verificar instalação
mvn -version

# Deve retornar: Maven 3.9+
```

**Download:** [Apache Maven](https://maven.apache.org/download.cgi)

#### 3. Node.js e NPM
```bash
# Verificar instalação
node -v
npm -v

# Deve retornar: Node 16+ e NPM 8+
```

**Download:** [Node.js](https://nodejs.org/)

#### 4. Appium Server
```bash
# Instalar globalmente
npm install -g appium

# Verificar instalação
appium -v

# Instalar driver UiAutomator2
appium driver install uiautomator2
```

#### 5. Android Studio
- **Download:** [Android Studio](https://developer.android.com/studio)
- **Componentes necessários:**
    - Android SDK Platform 34 (API Level 34)
    - Android SDK Build-Tools
    - Android Emulator
    - Android SDK Platform-Tools

#### 6. Variáveis de Ambiente

**Windows (PowerShell):**
```powershell
# JAVA_HOME
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")

# ANDROID_HOME
[System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "%LOCALAPPDATA%\Android\Sdk", "Machine")

# PATH (adicionar)
$env:Path += ";%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools"
```

**macOS/Linux (Bash/Zsh):**
```bash
# Adicionar ao ~/.bashrc ou ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools
```

---

## Instalação

### 1. Clonar o Repositório
```bash
git clone https://github.com/spencerarq/appium-java-wikipedia-framework.git
cd appium-java-wikipedia-framework
```

### 2. Instalar Dependências
```bash
mvn clean install -DskipTests
```

### 3. Configurar Emulador Android

#### Criar AVD (Android Virtual Device)
```bash
# Listar AVDs disponíveis
avdmanager list avd

# Criar novo AVD (Pixel 6a API 34)
avdmanager create avd \
  -n Pixel_6a_API_34 \
  -k "system-images;android-34;google_apis;x86_64" \
  -d "pixel_6a"
```

#### Iniciar Emulador
```bash
# Listar emuladores
emulator -list-avds

# Iniciar emulador
emulator -avd Pixel_6a_API_34 -no-snapshot-load
```

**Aguardar até o emulador inicializar completamente (~2-3 minutos).**

### 4. Instalar APK Wikipedia
```bash
# Download do APK (versão Alpha)
# Link: https://github.com/wikimedia/apps-android-wikipedia/releases

# Instalar no emulador
adb install wikipedia-alpha.apk

# Verificar instalação
adb shell pm list packages | grep wikipedia
```

### 5. Iniciar Appium Server
```bash
# Terminal separado
appium

# Deve exibir: [Appium] Welcome to Appium v2.x.x
```

---

## Execução dos Testes

### Testes Unitários (JUnit 5)
```bash
# Executar todos os testes unitários
mvn test -Dtest="**/*UnitTest"

# Executar teste específico
mvn test -Dtest="CommonActionsUnitTest"

# Com cobertura JaCoCo
mvn clean test jacoco:report
```

**Relatório:** `target/site/jacoco/index.html`

### Testes E2E (TestNG)
```bash
# Executar todos os testes E2E
mvn test -Dtest="**/*E2ETest"

# Executar suite específica
mvn test -DsuiteXmlFile=testng-smoke.xml

# Com relatório Allure
mvn clean test allure:serve
```

**Relatório:** Abre automaticamente no navegador

### Executar Todos os Testes
```bash
# Unitários + E2E + Relatórios
mvn clean test allure:serve
```

---

## Estrutura do Projeto
```
appium-java-wikipedia-framework/
│
├── src/
│   ├── main/java/com/wikipedia/
│   │   ├── pages/              # Page Object Model
│   │   │   ├── SearchPage.java
│   │   │   ├── ArticlePage.java
│   │   │   ├── SavedPage.java
│   │   │   └── SettingsPage.java
│   │   │
│   │   ├── commons/            # Ações reutilizáveis
│   │   │   ├── CommonActions.java
│   │   │   └── DriverFactory.java
│   │   │
│   │   └── utils/              # Utilitários
│   │       ├── ConfigReader.java
│   │       ├── TestHelper.java
│   │       └── LoggerHelper.java
│   │
│   └── test/java/com/wikipedia/
│     │  ├── unit/               # Testes Unitários (JUnit 5)
│     │  │   ├── CommonActionsUnitTest.java
│     │  │   ├── DriverFactoryUnitTest.java
│     │  │   └── TestHelperUnitTest.java
│     │  │
│     │  └── e2e/                # Testes E2E (TestNG)
│     │      ├── TC01_SearchArticleE2ETest.java
│     │      ├── TC02_NoResultsE2ETest.java
│     │      ├── TC03a_SaveArticleE2ETest.java
│     │      ├── TC03b_RemoveArticleE2ETest.java
│     │      ├── TC04_ChangeLanguageE2ETest.java
│     │      ├── TC05_BackgroundE2ETest.java
│     │      ├── TC06_RotationE2ETest.java
│     │      └── TC07_ScrollE2ETest.java
│     │
│     └── integration.driver/      # Testes Integração
│            └── DriverFactoryIntegrationTest.java
│
├── src/test/resources/
│   ├── config.properties       # Configurações do projeto
│   ├── testng.xml              # Suite completa
│   ├── testng-smoke.xml        # Suite smoke
│   └── allure.properties       # Configurações Allure
│
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions pipeline
│
├── pom.xml                     # Dependências Maven
├── README.md                   # Este arquivo
├── .gitignore
└── LICENSE
```

---

## Relatórios

### Allure Report

**Visualização completa dos testes E2E:**
- Status de execução (Passed/Failed/Skipped)
- Screenshots em falhas
- Logs detalhados
- Tempo de execução
- Histórico de execuções

**Gerar e visualizar:**
```bash
mvn clean test allure:serve
```

**Exemplo de visualização:**

[Allure Report](https://github.com/allure-framework/allure2/raw/master/.github/readme-img.png)

### JaCoCo Report

**Cobertura de código do framework:**
- Cobertura de instrução (≥80%)
- Cobertura de branches
- Análise por pacote/classe

**Gerar relatório:**
```bash
mvn clean test jacoco:report
```

**Abrir:** `target/site/jacoco/index.html`

---

## Cobertura de Código

### Meta de Cobertura

| Camada | Meta | Medição |
|--------|------|---------|
| Commons/Actions | ≥80% | JaCoCo |
| Utils (ConfigReader, TestHelper) | ≥80% | JaCoCo |
| Page Objects | N/A | Coberto por testes E2E |

### Verificar Cobertura
```bash
# Executar testes unitários com cobertura
mvn clean test jacoco:report

# Verificar se atingiu a meta (falha se <80%)
mvn jacoco:check
```

---

## CI/CD

### GitHub Actions

**Pipeline automatizado em cada push/PR:**

1. Build do projeto (Maven)
2. Testes unitários (JUnit 5)
3. Verificação de cobertura (JaCoCo)
4. Testes E2E (Appium + Emulador)
5. Geração de relatórios (Allure)
6. Deploy de relatórios (GitHub Pages)

**Status do Build:**

[CI/CD](https://github.com/spencerarq/appium-java-wikipedia-framework/actions)

**Configuração:** `.github/workflows/ci.yml`

---

## Roadmap

### Fase 1 - Fundação (Concluída)
- [x] Setup do projeto Maven
- [x] Implementação do POM
- [x] Commons/Actions
- [x] Testes unitários (10-15)
- [x] Testes E2E críticos (7)
- [x] JaCoCo + Allure
- [ ] GitHub Actions

### Fase 2 - Expansão (Planejado)
- [ ] Testes de integração (API Wikipedia)
- [ ] Paralelização de testes
- [ ] Suporte a múltiplos devices (BrowserStack)
- [ ] Testes de acessibilidade
- [ ] Visual regression testing

### Fase 3 - Otimização (Futuro)
- [ ] Integração com Jira/TestRail
- [ ] Docker para ambiente isolado
- [ ] AI-powered test generation
- [ ] Performance testing (K6/JMeter)

---

## Casos de Teste

### Testes E2E (7 casos críticos)

| ID | Cenário | Tipo | Prioridade | Status |
|----|---------|------|------------|--------|
| TC01 | Pesquisar e visualizar artigo "Appium" | Positivo | P0 | ✅ |
| TC02 | Pesquisa sem resultados (UUID dinâmico) | Negativo | P1 | ✅ |
| TC03a | Salvar artigo "Java" | Positivo | P0 | ✅ |
| TC03b | Remover artigo "Java" | Positivo | P0 | ✅ |
| TC04 | Alterar idioma para Espanhol | Positivo | P1 | ✅ |
| TC05 | App retorna do background | Positivo | P1 | ✅ |
| TC06 | Rotação de tela durante pesquisa | Positivo | P2 | ✅ |
| TC07 | Scroll em artigo longo | Positivo | P2 | ✅ |

### Testes Unitários (10 casos)

| ID | Classe | Método | Status |
|----|--------|--------|--------|
| UT01 | CommonActions | waitForElementVisible | ✅ |
| UT02 | CommonActions | clickElement | ✅ |
| UT03 | CommonActions | inputText | ✅ |
| UT04 | CommonActions | getTextFromElement | ✅ |
| UT05 | CommonActions | swipe | ✅ |
| UT06 | DriverFactory | initializeAndroidDriver | ✅ |
| UT07 | DriverFactory | quitDriver | ✅ |
| UT08 | TestHelper | takeScreenshot | ✅ |
| UT09 | ConfigReader | getProperty | ✅ |
| UT10 | LoggerHelper | logInfo | ✅ |

---

## Reportar Bugs

Encontrou um bug? Abra uma [issue](https://github.com/spencerarq/appium-java-wikipedia-framework/issues) com:

- **Título:** Descrição curta do problema
- **Descrição:** Passos para reproduzir
- **Esperado vs Atual:** O que deveria acontecer vs o que aconteceu
- **Ambiente:** SO, versão do Java, Appium, etc.
- **Screenshots:** Se aplicável

---

## Documentação Adicional

- [Plano de Testes Completo](https://drive.google.com/file/d/1lIEQVdGKY63wsEEeFCQaSYCpACTSqNF-/view?usp=sharing)
- [Guia de Contribuição - em construção](CONTRIBUTING.md)
- [Arquitetura Detalhada - em construção](docs/ARCHITECTURE.md)
- [FAQ - em construção](docs/FAQ.md)

---

## Autor

**[RENATO SPENCER]**

- 💼 LinkedIn: [https://www.linkedin.com/in/renatospencer/](https://www.linkedin.com/in/renatospencer/)
- 🐙 GitHub: [github.com/spencerarq](https://github.com/spencerarq)

---

## Contribuindo

Contribuições são bem-vindas! Veja [CONTRIBUTING.md](CONTRIBUTING.md) para detalhes.

**Passos básicos:**
1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

---

## Licença

Este projeto está sob a licença MIT. Veja [LICENSE](LICENSE) para mais detalhes.

---

## Agradecimentos

- [Appium](http://appium.io/) - Framework de automação mobile
- [TestNG](https://testng.org/) - Test runner
- [Allure](https://docs.qameta.io/allure/) - Relatórios visuais
- [JaCoCo](https://www.jacoco.org/) - Cobertura de código
- [Wikipedia](https://www.wikipedia.org/) - App de testes

---

## Status do Projeto

![GitHub last commit](https://img.shields.io/github/last-commit/spencerarq/appium-java-wikipedia-framework)
![GitHub issues](https://img.shields.io/github/issues/spencerarq/appium-java-wikipedia-framework)
![GitHub pull requests](https://img.shields.io/github/issues-pr/spencerarq/appium-java-wikipedia-framework)
![GitHub stars](https://img.shields.io/github/stars/spencerarq/appium-java-wikipedia-framework?style=social)

---

<div align="center">

**Se este projeto te ajudou, considere dar uma ⭐!**

Made with lots of coffee by Renato Spencer

</div>