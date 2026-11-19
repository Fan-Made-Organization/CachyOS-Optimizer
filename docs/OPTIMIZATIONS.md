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
