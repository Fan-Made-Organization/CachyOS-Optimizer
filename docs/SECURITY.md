# Seguridad

## Principios obligatorios
- El modo predeterminado es de solo lectura.
- No ejecutar optimizaciones automáticamente.
- No ejecutar la aplicación completa como administrador.
- No construir comandos concatenando cadenas de shell.
- Usar listas de argumentos y una lista permitida de herramientas de diagnóstico.
- Toda modificación futura debe mostrar motivo, alcance, riesgo y reversibilidad.
- Solicitar aprobación explícita antes de modificar el sistema.
- Crear y validar respaldos antes de cambios que lo requieran.
- Registrar las acciones y ofrecer reversión cuando sea técnicamente posible.

## Clasificación
- READ_ONLY: consulta sin cambios.
- LOW: cambio limitado y reversible.
- MEDIUM: cambio con impacto potencial en servicios o rendimiento.
- HIGH: cambio sensible que requiere respaldo y revisión adicional.
- CRITICAL: operación bloqueada por la política de seguridad inicial.

## Privilegios
Los privilegios deben limitarse a una operación concreta mediante un mecanismo autorizado. Nunca se debe pedir al usuario que habilite sudo sin contraseña ni ejecutar comandos arbitrarios como root.

## Estado
La capa de validación inicial existe. La ejecución de modificaciones del sistema permanece deshabilitada.

## Privilegios y datos locales

La aplicación debe ejecutarse como usuario normal. Si una operación necesita privilegios, debe invocar únicamente el comando mínimo previsto y dejar que el mecanismo de autorización del sistema solicite credenciales. No debe almacenar contraseñas, aceptar privilegios en silencio ni desactivar controles de seguridad. Los informes pueden contener nombres de dispositivos, rutas, versiones y detalles del entorno; revísalos antes de compartirlos públicamente. Los registros no deben incluir secretos ni datos de autenticación.
