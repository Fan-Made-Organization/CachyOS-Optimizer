# X504 CachyOS Optimizer

<p align="center">
  <strong>Linux diagnostics, compatibility analysis, and a safety-first foundation for optimization.</strong><br>
  Designed with CachyOS and Arch Linux as the primary targets.
</p>

<p align="center">
  <a href="README.md">🇪🇸 Español</a> ·
  <a href="README.en.md">🇬🇧 English</a> ·
  <a href="README.pt-BR.md">🇧🇷 Português</a>
</p>

> **Current status: early development (0.1.0-SNAPSHOT).** The executable code currently focuses on read-only inspection and diagnostics. The project contains foundations for profiles, safety validation, and backups, but it does not yet apply performance tweaks automatically. See [the actual project status](#actual-project-status).

## What is X504 CachyOS Optimizer?

X504 is a free-software project under development that aims to bring system, hardware, compatibility, and Linux gaming diagnostics together in one tool.

Its goal is to help users understand their machines before changing settings: detect the environment, display useful information, identify unavailable tools, and prepare recommendations that can be evaluated safely.

**It does not promise magical FPS gains or secretly modify your system.** It observes and explains first. Future optimization actions must be specific, reversible, and explicitly approved by the user.

## What it currently offers

### 1. System information
- Basic Linux system detection.
- Identification of distribution, kernel, desktop environment, and architecture when exposed by the system.
- Display of detected information in the terminal.

### 2. Hardware information
- Collection of hardware information through interfaces exposed by Linux.
- A machine summary to provide context for diagnostics.
- Missing or unavailable information is reported as unknown/unavailable rather than fabricated.

### 3. Compatibility analysis
- Initial compatibility analysis based on detected system information.
- Configurable compatibility rules in project resources.
- Results depend on available data and do not replace real performance or stability testing.

### 4. Modular diagnostics

| Module | What it inspects |
|---|---|
| CPU | Processor information and accessible CPU data. |
| Memory | Memory information available to the system. |
| GPU | Graphics information exposed by Linux and available tools. |
| Storage | Detectable storage devices and information. |
| Power and thermals | Available power information and thermal sensors. |
| Network | Network interfaces and basic network information. |
| Audio | Audio environment and available PipeWire/PulseAudio signals. |
| Gaming | Indicators and tools related to Linux gaming. |
| KDE Plasma | KDE desktop information when present. |
| Steam | Steam and related components that can be inspected. |
| Proton | Available Proton and game-compatibility information. |
| General diagnostics | Groups general system checks. |

A module's presence does not mean every driver, device, or tool is installed. Each module depends on what the host system exposes.

### 5. Profile catalog
The project includes an initial catalog of profiles and descriptions for different usage scenarios. At this stage, profiles are **informational references**. Selecting one does not change CPU governors, drivers, services, or system parameters.

### 6. System analysis dashboard

The CLI displays an ASCII dashboard with distribution, inferred base, kernel, desktop, architecture, CPU, GPU, RAM, and compatibility assessment. Unknown values remain explicitly unknown; the status is not marked excellent when checks are unknown.

The values in the sample below are illustrative. The application prints data actually detected on the host.

### 7. Backup, audit, and benchmark foundations
- `BackupManager` creates backups of explicitly selected files, writes a manifest, and verifies SHA-256.
- Restore targets a user-selected directory, verifies integrity, and refuses to overwrite existing files.
- `AuditLogger` stores a local diagnostic summary in the user's state directory.
- `BenchmarkEngine` provides an optional bounded CPU microbenchmark without changing system configuration.
- System files are not backed up or restored automatically; restore never writes to original system paths.

### 8. Safety boundaries
- Actions can be validated and prepared, but the engine does not execute system modifications.
- Critical operations are blocked.
- Changes require explicit approval; administrative operations require a backup.
- Backups restore verified copies into another directory and do not automatically overwrite original files.

## Actual project status

| Feature | Status |
|---|---|
| Basic Linux detection and system information | Initial implementation |
| Hardware inspection | Initial implementation |
| Modular diagnostics | Initial implementation |
| Compatibility analysis | Initial implementation |
| Profile catalog | Available as an informational catalog |
| Action validation and planning | Implemented; actual system modifications remain disabled |
| Selected-file backup and verified restore to another directory | Implemented |
| Automatic optimization application | **Disabled; no changes are executed** |
| Restore to original system paths and full system rollback | **Planned** |
| Local audit log | Implemented with basic rotation |
| Graphical user interface | **Planned** |
| Local CPU microbenchmark | Implemented as an optional test; does not measure FPS or GPU |

The Java source compiled by Maven is located in `src/main/java`. Files under `src/main/resources/java` are legacy resources and are not compiled as Java classes by the current Maven configuration.

## Compatibility and focus

- **Primary targets:** CachyOS and Arch Linux.
- **Environment:** Linux.
- **Language:** Java 21.
- **Desktop:** KDE Plasma is detected when available.
- **Gaming:** Steam, Proton, and graphics-related tools are inspected when detectable.
- **Other distributions:** They may provide useful data, but equivalent detection coverage and compatibility are not guaranteed yet.

## Requirements

- Linux for complete diagnostics.
- **JDK 21** (not just a Java runtime).
- Internet access during initial setup to download Maven or missing dependencies.
- Base tools for the setup script: `curl`, `tar`, `sha512sum`, `awk`, and `sed`.

The bootstrap script can download Maven 3.9.9 into the local project checkout and verify the archive using SHA-512. Maven downloads Java dependencies into a local cache. **Java 21 must be installed beforehand**; the script does not install the JDK or run administrator commands. Optional diagnostic tools are not installed automatically.

## Installation and usage

Clone the repository:

```bash
git clone https://github.com/Fan-Made-Organization/CachyOS-Optimizer.git
cd CachyOS-Optimizer
```

Prepare the environment and run the tests:

```bash
bash scripts/bootstrap.sh
```

You can also use Maven installed on your system:

```bash
mvn clean verify
mvn package
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar

# Back up only files you explicitly select
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --backup "$HOME/x504-backups" "$HOME/.config/example.conf"

# Restore verified copies into a separate directory without overwriting
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --restore "$HOME/x504-backups/backup-..." "$HOME/x504-restored"
```

Available helper scripts:
- `scripts/build.sh`: build the project.
- `scripts/test.sh`: run tests.
- `scripts/package.sh`: create the JAR package.
- `scripts/launch.sh`: launch the packaged application.
- `scripts/clean.sh`: remove generated files.

If a command fails, check [Installation](docs/INSTALLATION.md), [Dependencies](docs/DEPENDENCIES.md), and [Troubleshooting](docs/TROUBLESHOOTING.md).

## Safety approach

1. Inspect the system before suggesting changes.
2. Explain each action's purpose, expected benefit, risk, and compatibility.
3. Require explicit approval before modifying the machine.
4. Prepare a rollback path whenever the change supports one.
5. Record what was proposed and what was executed.

The application should not run as root for normal diagnostics. Silent privileged commands and concatenating user input into shell commands are not acceptable. Read the [Security policy](docs/SECURITY.md).

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Modules](docs/MODULES.md)
- [Compatibility](docs/COMPATIBILITY.md)
- [Dependencies and setup](docs/DEPENDENCIES.md)
- [Installation](docs/INSTALLATION.md)
- [Security](docs/SECURITY.md)
- [Optimization approach](docs/OPTIMIZATIONS.md)
- [Development and contribution](docs/DEVELOPMENT.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Changelog](docs/CHANGELOG.md)

## Development and contributions

Contributions are welcome. Before proposing a feature:
- Check whether an implementation or related task already exists.
- Keep read-only diagnostics as the default behavior.
- Do not describe an optimization as available when only a class, configuration, or idea exists.
- Add tests for new behavior.
- Document compatibility, risks, permissions, and reversibility.
- Keep file, package, class, method, and variable names in English; user-facing documentation and messages are written in Spanish.

Validate changes with:

```bash
mvn clean verify
```

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0-only)**. See the complete [LICENSE](LICENSE) text and the official explanation at https://www.gnu.org/licenses/gpl-3.0.html.

---

**X504 CachyOS Optimizer** — understand the system first; optimize responsibly next.
