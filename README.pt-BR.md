# CachyOS Optimizer

<p align="center">
  <strong>Diagnóstico Linux, análise de compatibilidade e uma base de otimização com foco em segurança.</strong><br>
  Desenvolvido para Linux, com suporte ampliado a Arch/CachyOS, Debian/Ubuntu, Fedora e openSUSE.
</p>

<p align="center">
  <a href="README.md">🇪🇸 Español</a> ·
  <a href="README.en.md">🇬🇧 English</a> ·
  <a href="README.pt-BR.md">🇧🇷 Português</a>
</p>

> **Estado atual: desenvolvimento inicial (0.1.0-SNAPSHOT).** O código executável disponível concentra-se em inspeção e diagnóstico somente leitura. O projeto possui bases para perfis, validação de segurança e backups, mas ainda não aplica ajustes de desempenho automaticamente. Consulte [o estado real do projeto](#estado-real-do-projeto).

## O que é o CachyOS Optimizer?

CachyOS Optimizer é um projeto de software livre em desenvolvimento que pretende reunir em uma única ferramenta informações do sistema, hardware, compatibilidade e componentes relacionados ao uso diário e a jogos no Linux.

O objetivo é ajudar a entender o computador antes de alterar configurações: detectar o ambiente, mostrar informações úteis, indicar ferramentas indisponíveis e preparar recomendações que possam ser avaliadas com segurança.

**O projeto não promete ganhos mágicos de FPS nem modifica o sistema às escondidas.** Primeiro observa e explica; futuras ações de otimização deverão ser específicas, reversíveis e aprovadas explicitamente pelo usuário.

## O que oferece atualmente

### 1. Informações do sistema
- Detecção básica do sistema Linux.
- Identificação da distribuição, kernel, ambiente gráfico e arquitetura quando esses dados são disponibilizados pelo sistema.
- Exibição das informações detectadas no terminal.

### 2. Informações de hardware
- Coleta de informações de hardware por meio das interfaces disponibilizadas pelo Linux.
- Resumo do computador para contextualizar os diagnósticos.
- Informações ausentes ou indisponíveis são indicadas como desconhecidas/não disponíveis, sem inventar dados.

### 3. Análise de compatibilidade
- Análise inicial de compatibilidade com base nas informações detectadas.
- Regras de compatibilidade configuráveis nos recursos do projeto.
- A avaliação depende dos dados disponíveis e não substitui testes reais de desempenho ou estabilidade.

### 4. Diagnósticos modulares

| Módulo | O que inspeciona |
|---|---|
| CPU | Informações do processador e dados acessíveis da CPU. |
| Memória | Informações de memória disponíveis para o sistema. |
| GPU | Informações gráficas expostas pelo Linux e ferramentas disponíveis. |
| Armazenamento | Dispositivos e informações de armazenamento detectáveis. |
| Energia e temperatura | Informações de energia e sensores térmicos disponíveis. |
| Rede | Interfaces e informações básicas de rede. |
| Áudio | Ambiente de áudio e sinais disponíveis do PipeWire/PulseAudio. |
| Gaming | Indicadores e ferramentas relacionados a jogos no Linux. |
| KDE Plasma | Informações do ambiente KDE quando presente. |
| Steam | Detecção do Steam e componentes relacionados que possam ser inspecionados. |
| Proton | Informações disponíveis sobre Proton e compatibilidade de jogos. |
| Diagnóstico geral | Reúne verificações gerais do sistema. |

A existência de um módulo não significa que todos os drivers, dispositivos ou programas estejam instalados. Cada módulo depende do que o sistema permite detectar.

### 5. Catálogo de perfis
O projeto inclui um catálogo inicial de perfis e descrições para diferentes formas de uso. Nesta etapa, os perfis são **referências informativas**: selecioná-los não altera governadores de CPU, drivers, serviços ou parâmetros do sistema.

### 6. Painel de análise do sistema

O CLI apresenta um painel ASCII com distribuição, base inferida, kernel, ambiente gráfico, arquitetura, CPU, GPU, RAM e avaliação de compatibilidade. Valores desconhecidos continuam identificados como desconhecidos; o estado não é marcado como excelente quando existem verificações desconhecidas.

Os valores de exemplo são ilustrativos. O aplicativo exibe os dados realmente detectados no computador.

### 7. Recursos de backup, auditoria e benchmark
- `BackupManager`: cria backups de arquivos selecionados explicitamente, gera um manifesto e verifica SHA-256.
- Restauração em uma pasta escolhida pelo usuário, com verificação de integridade e sem sobrescrever arquivos existentes.
- `AuditLogger`: registra localmente um resumo dos diagnósticos no diretório de estado do usuário.
- `BenchmarkEngine`: microbenchmark opcional e limitado de CPU, sem alterar configurações do sistema.
- Arquivos do sistema não são copiados nem restaurados automaticamente; a restauração nunca grava nos caminhos originais do sistema.

### 8. Limites de segurança
- As ações podem ser validadas e preparadas, mas o motor não executa modificações no sistema.
- Operações críticas são bloqueadas.
- Alterações exigem aprovação explícita; operações administrativas exigem backup.
- Os backups restauram cópias verificadas em outra pasta e não sobrescrevem automaticamente os arquivos originais.

## Estado real do projeto

| Funcionalidade | Estado |
|---|---|
| Detecção e informações básicas do Linux | Implementação inicial |
| Inspeção de hardware | Implementação inicial |
| Diagnósticos modulares | Implementação inicial |
| Análise de compatibilidade | Implementação inicial |
| Catálogo de perfis | Disponível como catálogo informativo |
| Validação e preparação de ações | Implementadas; as alterações reais no sistema permanecem desativadas |
| Backup de arquivos selecionados e restauração verificada em outra pasta | Implementado |
| Aplicação automática de otimizações | **Desativada; nenhuma alteração é executada** |
| Restauração nos caminhos originais do sistema e reversão completa | **Planejadas** |
| Registro local de auditoria | Implementado com rotação básica |
| Interface gráfica | **Planejada** |
| Microbenchmark local de CPU | Implementado como teste opcional; não mede FPS nem GPU |

O código Java compilado pelo Maven fica em `src/main/java`. Os arquivos em `src/main/resources/java` são recursos herdados e não são compilados como classes Java pela configuração Maven atual.

## Compatibilidade e foco

- **Detecção de distribuição:** famílias Arch/CachyOS, Debian/Ubuntu, Fedora/RHEL e openSUSE/SUSE.\n- **Instalação de pacotes:** somente ferramentas de diagnóstico específicas por família; o gerenciador de pacotes mantém a confirmação interativa.\n- **Suporte a kernel:** inventário somente leitura do kernel ativo, árvores de módulos, headers, variantes comuns e ambiente de inicialização. Não instala/remove kernels nem altera o bootloader.
- **Ambiente:** Linux.
- **Linguagem:** Java 21.
- **Desktop:** o KDE Plasma é identificado quando está disponível.
- **Jogos:** Steam, Proton e ferramentas gráficas são inspecionados quando podem ser detectados.
- **Outras distribuições:** podem fornecer dados úteis, mas ainda não há garantia do mesmo nível de detecção ou compatibilidade.

## Requisitos

- Linux para diagnósticos completos.
- Linux x86_64 ou ARM64 para a preparação automática do JDK.
- Acesso à Internet durante a preparação inicial.
- Ferramentas básicas: `curl`, `tar`, `sha256sum`, `sha512sum`, `awk`, `sed`, `python3` e `uname`.

Se o Java 21 estiver ausente, o bootstrap baixa o Temurin JDK 21 e verifica o SHA-256; se o Maven estiver ausente, baixa o Maven 3.9.9 e verifica o SHA-512. Ambos ficam em `.tools/` na cópia local do repositório. As dependências Java ficam em `.cache/`. O script não instala software globalmente nem executa comandos de administrador. Ferramentas opcionais de diagnóstico não são instaladas automaticamente.

## Instalação e execução

Clone o repositório:

```bash
git clone https://github.com/Fan-Made-Organization/CachyOS-Optimizer.git
cd CachyOS-Optimizer
```

Prepare o ambiente e execute os testes:

```bash
bash scripts/bootstrap.sh
```

Também é possível usar o Maven instalado no sistema:

```bash
mvn clean verify
mvn package
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar

# Faça backup somente dos arquivos escolhidos explicitamente
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --backup "$HOME/x504-backups" "$HOME/.config/example.conf"

# Restaure cópias verificadas em outra pasta, sem sobrescrever arquivos existentes
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --restore "$HOME/x504-backups/backup-..." "$HOME/x504-restored"
```

Scripts auxiliares disponíveis:
- `scripts/build.sh`: compilar o projeto.
- `scripts/test.sh`: executar os testes.
- `scripts/package.sh`: gerar o pacote JAR.
- `scripts/launch.sh`: iniciar o aplicativo empacotado.
- `scripts/clean.sh`: remover arquivos gerados.

Se algum comando falhar, consulte [Instalação](docs/INSTALLATION.md), [Dependências](docs/DEPENDENCIES.md) e [Solução de problemas](docs/TROUBLESHOOTING.md).

## Abordagem de segurança

1. Inspecionar o sistema antes de sugerir alterações.
2. Explicar o objetivo, benefício esperado, risco e compatibilidade de cada ação.
3. Exigir aprovação explícita antes de modificar o computador.
4. Preparar uma forma de reversão sempre que a alteração permitir.
5. Registrar o que foi sugerido e o que foi executado.

A inspeção de kernel é somente leitura. Instalação/remoção de kernels, regeneração de initramfs e alterações do bootloader não são automatizadas.\n\nO aplicativo não deve ser executado como root para diagnósticos normais. Comandos privilegiados silenciosos e concatenação de entradas do usuário em comandos de shell não são aceitáveis. Leia a [política de segurança](docs/SECURITY.md).

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md)
- [Gerenciador de backup](docs/BACKUPS.md)
- [Módulos](docs/MODULES.md)
- [Compatibilidade](docs/COMPATIBILITY.md)
- [Dependências e preparação](docs/DEPENDENCIES.md)
- [Instalação](docs/INSTALLATION.md)
- [Segurança](docs/SECURITY.md)
- [Abordagem de otimização](docs/OPTIMIZATIONS.md)
- [Desenvolvimento e contribuição](docs/DEVELOPMENT.md)
- [Solução de problemas](docs/TROUBLESHOOTING.md)
- [Registro de alterações](docs/CHANGELOG.md)

## Desenvolvimento e contribuições

Contribuições são bem-vindas. Antes de propor um recurso:
- Verifique se já existe uma implementação ou tarefa relacionada.
- Mantenha o diagnóstico somente leitura como comportamento padrão.
- Não apresente uma otimização como disponível se houver apenas uma classe, configuração ou ideia.
- Adicione testes para novos comportamentos.
- Documente compatibilidade, riscos, permissões e reversibilidade.
- Mantenha nomes de arquivos, pacotes, classes, métodos e variáveis em inglês; a documentação e as mensagens exibidas ao usuário são escritas em espanhol.

Para validar as alterações:

```bash
mvn clean verify
```

## Licença

Este projeto é licenciado sob a **GNU General Public License v3.0 (GPL-3.0-only)**. Consulte o texto completo em [LICENSE](LICENSE) e a explicação oficial em https://www.gnu.org/licenses/gpl-3.0.html.

---

**CachyOS Optimizer** — primeiro entenda o sistema; depois otimize com responsabilidade.
