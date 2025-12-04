## Alunos

- Rafael Fernando dos Reis Mecabô
- Robert Czelen Vitorino

## Pré-requisitos

Antes de compilar e executar o projeto, certifique-se de ter instalado:

- **Java Development Kit (JDK)** versão 8 ou superior
- **Git** (opcional, para clonar o repositório)

### Verificar se Java está instalado

Execute o comando abaixo no terminal/prompt de comando:

```bash
java -version
```

Se não tiver Java instalado, veja as instruções de instalação para seu sistema operacional abaixo.

## Instalação do Java

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install openjdk-11-jdk
```

### Linux (Fedora/RHEL/CentOS)

```bash
sudo dnf install java-11-openjdk-devel
```

### Windows

1. Acesse [java.com](https://www.java.com) ou [oracle.com](https://www.oracle.com/java/technologies/downloads/)
2. Baixe o instalador do JDK (Java Development Kit)
3. Execute o instalador e siga as instruções
4. Configure as variáveis de ambiente `JAVA_HOME` apontando para o diretório de instalação do JDK

### macOS

Usando Homebrew:

```bash
brew install openjdk@11
```

Ou baixe direto de [oracle.com](https://www.oracle.com/java/technologies/downloads/)

## Compilação

### Linux e macOS

1. Abra um terminal e navegue até o diretório do projeto:

```bash
cd /caminho/para/Univali_Compilador
```

2. Compile o projeto:

```bash
javac -d bin -sourcepath src src/Main.java
```

Ou, se precisar compilar todos os arquivos Java:

```bash
find src -name "*.java" -print0 | xargs -0 javac -d bin
```

### Windows

1. Abra o Prompt de Comando (cmd) ou PowerShell
2. Navegue até o diretório do projeto:

```cmd
cd C:\caminho\para\Univali_Compilador
```

3. Compile o projeto:

```cmd
javac -d bin -sourcepath src src/Main.java
```

Ou, compile todos os arquivos:

```cmd
for /r src %f in (*.java) do javac -d bin "%f"
```

## Build

O projeto utiliza a estrutura padrão de diretórios Java. Após compilar com sucesso, os arquivos `.class` estarão na pasta `bin/`.

### Criar um JAR executável (opcional)

Se desejar criar um arquivo JAR executável:

#### Linux e macOS

```bash
cd bin
jar cvfe Compilador.jar Main .
cd ..
```

#### Windows

```cmd
cd bin
jar cvfe Compilador.jar Main .
cd ..
```

## Execução

### Linux e macOS

#### Executar a classe principal

```bash
java -cp bin Main
```

#### Executar com um arquivo JAR (se criado)

```bash
java -jar bin/Compilador.jar
```

### Windows

#### Executar a classe principal

```cmd
java -cp bin Main
```

#### Executar com um arquivo JAR (se criado)

```cmd
java -jar bin\Compilador.jar
```

## Estrutura do Projeto

```
Univali_Compilador/
├── IDE/
│   ├── bin/                      # Arquivos compilados (.class)
│   ├── src/
│   │   ├── Main.java             # Classe principal
│   │   ├── gals/                 # Análise léxica, sintática e semântica
│   │   │   ├── Lexico.java
│   │   │   ├── Sintatico.java
│   │   │   ├── Semantico.java
│   │   │   └── ...
│   │   ├── gui/                  # Interface gráfica
│   │   │   ├── IDE.java
│   │   │   └── ListaSimbolosJanela.java
│   │   └── simbulo/              # Gerenciamento de símbolos
│   │       └── Simbolo.java
│   └── META-INF/
│       └── MANIFEST.MF
└── README.md                     # Este arquivo
```

