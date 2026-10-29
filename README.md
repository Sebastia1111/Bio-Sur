<div align="center">

# Bio-Sur

Aplicación Android nativa (Kotlin) para que los supervisores de BIOSUR registren
informes de trabajo en terreno **sin papel** y la empresa tenga visibilidad
centralizada de todos los informes.

> Stack: Kotlin · MVVM · Room · ViewBinding · Coroutines/Flow · CameraX · Material 3
> Mínimo: Android 8 (API 26) · Target: Android 14 (API 34)

---

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android Studio](https://img.shields.io/badge/Android_Studio-4285F4?style=for-the-badge&logo=androidstudio&logoColor=white)](https://developer.android.com/studio)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

</div>

---

## Stack Tecnológico

* **Lenguaje:** Kotlin
* **IDE:** Android Studio
* **Sistema de Construcción:** Gradle

---

## Esquema de base de datos (Room)

```
users          (id, email*, passwordHash, fullName, role, active, createdAt)
employees      (id, rut*, fullName, role, phone, email, active, createdAt)
reports        (id, supervisorId→users, date, startTime, endTime, comments,
                latitude, longitude, address, status, synced, createdAt, updatedAt)
materials      (id, reportId→reports, name, quantity, unit)
equipment      (id, reportId→reports, name, quantity, notes)
personnel      (id, reportId→reports, employeeId→employees?, name, functionInWork)
media          (id, reportId→reports, type, filePath, thumbnailPath, comment, capturedAt)
```

`*` = índice único.


---

## Configuración y Ejecución

### Requisitos Previos
* Android Studio instalado.
* Dispositivo físico o emulador (Android Virtual Device).

### Pasos para Ejecutar

1. **Clonar el repositorio:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>

2. **Abrir en Android Studio:**
* Abre Android Studio.
* Selecciona **Open** y navega hasta la carpeta del proyecto.

3. **Sincronizar Gradle:**
* Espera a que la sincronización de Gradle finalice automáticamente (`Sync Project with Gradle Files`).

4. **Ejecutar la aplicación:**
* Selecciona el dispositivo/emulador en la barra superior.
* Presiona el botón **Run** (▶) o `Shift + F10`.


---
## Errores comunes al abrir el proyecto

| Error | Causa | Solución |
|---|---|---|
| `Unresolved class 'BiosurApp'`, `'MainActivity'`, `'LocationService'`, etc. | Gradle no sincronizó | File → Sync Project with Gradle Files |
| `Attribute android:xxx is not allowed here` (en Manifest) | Gradle no sincronizó | Igual — sincronizar Gradle |
| `Unresolved package 'androidx'`, `'core'`, `'content'` | SDK no configurado | File → Project Structure → SDK Location → apuntar al Android SDK |
| `Typo: In word 'Permisos'`, `'cámara'`, `'ubicación'`, etc. | Corrector ortográfico de Android Studio en inglés | Settings → Editor → Spelling → Disable spellcheck, o añadir palabras al diccionario |
| `jlink executable ... does not exist` | JDK 21 en uso (Android necesita 17) | File → Project Structure → SDK Location → JDK → usar JBR 17 |


---

## Mapa de paquetes

```
cl.biosur.app
├── BiosurApp.kt          (Application)
├── MainActivity.kt       (host + bottom nav)
├── ServiceLocator.kt     (DI manual)
├── data
│   ├── local
│   │   ├── BiosurDatabase.kt
│   │   ├── Converters.kt
│   │   ├── dao/ (UserDao, EmployeeDao, ReportDao, MaterialDao, EquipmentDao, PersonnelDao, MediaDao)
│   │   ├── entity/ (UserEntity, EmployeeEntity, ReportEntity, MaterialEntity, EquipmentEntity, PersonnelEntity, MediaEntity)
│   │   └── relation/ (ReportWithDetails)
│   ├── model/ (UserRole, ReportStatus)
│   └── repository/ (AuthRepository, EmployeeRepository, ReportRepository, CompanyRepository)
├── ui
│   ├── auth/      (C1: LoginFragment, LoginViewModel)
│   ├── employees/ (C1: EmployeeListFragment, EmployeeFormFragment, EmployeeAdapter, EmployeeViewModel)
│   ├── report/    (C2: ReportFormFragment, ReportListFragment, ReportDetailFragment, ReportViewModel, ReportAdapter, MediaAdapter, SimpleItemAdapter, steps/)
│   ├── company/   (C3: CompanyDashboardFragment, CompanyDashboardViewModel)
│   └── theme/     (Color.kt - por si quieren colores programáticos)
└── util/ (SessionManager, Permissions, LocationService, OfflineManager, ExportManager, CameraHelper, BiosurAppContextHolder)
```

---
## Equipo de Desarrollo

<div align="center">
<table>
<tr>
<td align="center">
<a href="https://github.com/Sebastia1111">
<img src="https://github.com/Sebastia1111.png" width="100" style="border-radius:50%"><br>
<strong>Sebastian Orellana</strong><br>
</a>
</td>
<td align="center">
<a href="https://github.com/Montabesz">
<img src="https://github.com/Montabesz.png" width="100" style="border-radius:50%"><br>
<strong>Benjamin Montanarez</strong><br>
</a>
</td>
<td align="center">
<a href="https://github.com/chapu18">
<img src="https://github.com/soruly.png" width="100" style="border-radius:50%"><br>
<strong>Sebastian Saavedra</strong><br>
</a>
</td>
</tr>
</table>
</div>

