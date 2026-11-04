# Pantallas de paciente

La interfaz toma como referencia el capítulo 3 de `SafeDiary_Doc`, los mockups MU-M01, MU-M03 y MU-M08–MU-M25, y los wireframes `rutines-bc`. Se conserva el chat de AssistantAI existente.

## Recorridos disponibles

- **Inicio:** selección de ánimo, confirmación local, acceso a Diarito, inspiración y accesos a rutinas/citas.
- **Rutinas:** lista, preferencias de recordatorio, creación con validación, respiración con temporizador y pausa, ejercicio de cinco sentidos y confirmación de actividad completada.
- **Psicólogos:** búsqueda por nombre/especialidad, categorías, tarifa máxima, guardados, ficha, reseñas, formulario de contacto y coordinación en Agenda.
- **Agenda:** próximas citas, pendientes e historial; conversaciones; aceptación de horario; reserva de una hora; vencimiento; pago aprobado/fallido de ejemplo; cancelación; detalle; consentimiento opcional; comprobante de ejemplo; sala de espera y videollamada visual; calificación de sesión completada.

## Organización

Se sigue la separación de AssistantAI: modelos en `domain/model`, vistas en `presentation/views`, componentes reutilizables en `presentation/components` y estado observable con `StateFlow` en `presentation/viewmodels`. Las fichas de muestra viven en `professionals/infrastructure/local/PreviewProfessionals.kt`.

`MainTabPagerScreen` comparte `SchedulingViewModel` entre el directorio y Agenda para conservar la conversación al cambiar de pestaña. Cada sección conserva su propio ViewModel. Solo la pestaña activa maneja el botón Atrás; la barra inferior sigue siendo responsabilidad del pager.

## Alcance local

Las pantallas nuevas no incorporan Retrofit, API, pagos, notificaciones, videollamadas ni permisos de cámara/micrófono. Las operaciones se ejecutan en memoria y se reinician al cerrar el proceso. El ánimo usa `SavedStateHandle` para conservar la selección al recrear la actividad. Los perfiles, reseñas, mensajes y citas son datos de ejemplo.

El temporizador de reserva usa su vencimiento absoluto y se actualiza al volver a Agenda. El de respiración se pausa al abandonar la pestaña. El pago de ejemplo no puede confirmar una reserva vencida o cancelada. El acceso a la sala se habilita para una cita confirmada desde diez minutos antes y hasta el fin de sus cincuenta minutos. El consentimiento no se activa por defecto y no comparte datos reales.

Se respetó el funcionamiento previo de AssistantAI y la modificación existente en `gradle/libs.versions.toml`.

## Revisión

`app/src/debug/java/com/mindcluster/safediary/PatientScreenPreviews.kt` contiene cuatro previews de 393 × 852 para Android Studio, sin inicializar AssistantAI ni llamar al backend.

Pruebas de comportamiento local: `PatientPreviewTest`. Pruebas de interacción y capturas: `PatientScreensTest`. Las capturas del emulador se guardan en `getExternalFilesDir()/patient-screens`.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --max-workers=1
.\gradlew.bat :app:connectedDebugAndroidTest --max-workers=1
```

Verificación realizada: compilación debug y 25 pruebas unitarias aprobadas; las cuatro pruebas de interacción de `PatientScreensTest` aprobaron en el emulador. Se revisaron nueve capturas, disponibles en `../artifacts/patient-ui` desde la raíz de SafeDiary-Android. La revisión de lint quedó sin completar.

El APK guardado junto a las capturas es un paquete de pruebas (`testOnly`): se instala mediante `adb install -r -t`, o se puede ejecutar el proyecto desde Android Studio. Las capturas corresponden a un emulador configurado en inglés, por lo que la barra inferior usa las traducciones inglesas existentes; con el idioma español se muestran Rutinas, Inicio, Psicólogos y Agenda.
