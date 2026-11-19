# Motor de recomendaciones y optimizaciones

## Estado

El proyecto incorpora un asesor de solo lectura. Analiza resultados concretos y recomienda verificaciones cuando existe evidencia suficiente.

Cada recomendación explica:
- Evidencia observada.
- Propósito.
- Beneficio esperado, sin garantizar resultados.
- Nivel de riesgo.
- Reversibilidad.
- Siguiente paso.

## Política para futuras acciones

Una optimización ejecutable deberá contar con:
1. Compatibilidad comprobada para distribución, kernel, hardware y entorno.
2. Comando o cambio definido mediante argumentos separados, nunca shell concatenado.
3. Nivel de riesgo y explicación visible.
4. Aprobación explícita e independiente.
5. Respaldo y procedimiento de reversión cuando corresponda.
6. Registro de auditoría.
7. Pruebas automatizadas y documentación.

No se permiten ajustes universales a ciegas para CPU, GPU, Mesa, controladores, servicios, audio, memoria o almacenamiento. El programa no debe ejecutarse como root para realizar diagnósticos.

## Ciclo recomendado de una optimización

1. **Medir:** guardar un diagnóstico inicial y anotar las condiciones del equipo.
2. **Entender:** comprobar qué servicio, controlador o componente administra el ajuste.
3. **Preparar:** mostrar valor actual, propuesta, alcance y riesgos.
4. **Autorizar:** pedir confirmación explícita y elevar privilegios solo para la operación necesaria.
5. **Aplicar:** modificar únicamente el ajuste seleccionado y comprobar el resultado.
6. **Revertir:** ofrecer restauración del valor anterior cuando sea técnicamente posible.
7. **Verificar:** repetir comprobaciones y no afirmar mejoras que no se hayan medido.

Un perfil es una colección de recomendaciones, no una orden para aplicar todos los ajustes. El resultado depende del hardware, la carga, el kernel y los servicios activos.


## Perfiles energéticos por tipo de equipo

El asesor `PowerDeviceAdvisor` busca dispositivos de tipo `Battery` en `/sys/class/power_supply`. Si encuentra uno clasifica el equipo como laptop; si la interfaz existe sin batería, como escritorio; en caso contrario conserva el tipo desconocido. Sugiere `balanced` como punto de partida y explica cuándo considerar ahorro o rendimiento. No cambia automáticamente perfiles ni gobernadores. La aplicación de perfiles sigue dependiendo de `power-profiles-daemon` y requiere aprobación explícita.

## Almacenamiento y diagnóstico avanzado

El inventario avanzado consulta `/sys/block`, muestra indicador rotacional y scheduler, y excluye dispositivos virtuales comunes. El valor rotacional cero significa dispositivo no rotacional; no prueba por sí solo que sea NVMe. TRIM programado sigue siendo una acción separada y solo se propone cuando se detecta almacenamiento no rotacional. No se cambia scheduler, sistema de archivos, opciones de montaje, firmware ni parámetros NVMe automáticamente.

## Gráficos y gaming entre distribuciones

El catálogo gráfico ofrece paquetes específicos para Arch, Debian, Fedora, SUSE y Alpine donde se ha definido un perfil. Los perfiles NVIDIA propietarios se restringen cuando requieren repositorios externos o combinaciones que el programa no puede validar con seguridad. Gentoo, Void y NixOS permanecen sin instalación automática de drivers. DXVK/VKD3D/Wine/Proton se tratan como capas de compatibilidad, no como drivers nativos DirectX. No se instalan repositorios externos, no se eliminan drivers y no se garantiza rendimiento/FPS.

El diagnóstico avanzado consulta unidades fallidas de systemd, módulos cargados (con atención a controladores gráficos y virtualización), indicador kernel-tainted y herramientas comunes de Steam/Wine/Gamescope. Son pistas para investigación, no una reparación automática ni una declaración concluyente de conflicto.
