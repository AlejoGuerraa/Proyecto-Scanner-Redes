# IPVision

Este repositorio contiene **IPVision**, una herramienta desarrollada en Java con **programación orientada a objetos**, que combina un **scanner de redes** y un módulo de **información de red tipo Netstat**. Permite escanear rangos de IPs, verificar su estado, obtener información de host, PID y puertos abiertos, y exportar los resultados a **TXT, CSV o LOG**.

## Cómo usar el programa

### 1. Scanner de redes
1. Ejecutar el programa (`IPVision.jar`) y abrir la sección de **Scanner de redes**.
2. Ingresar la IP de inicio y la IP final del rango a escanear.
3. Configurar el **tiempo máximo de respuesta** y la **cantidad de pings**, si es necesario.
4. Iniciar el escaneo. Se mostrará una barra de progreso y un contador de IPs escaneadas.
5. Ver resultados en la ventana correspondiente, donde se pueden filtrar por **Activas/Inactivas/Ambas** y ordenar por **IP** o **Tiempo de respuesta**.
6. Hacer doble clic en una fila para ver los detalles completos de cada IP.
7. Exportar resultados en **TXT**, **CSV** o **LOG**.

### 2. Información de red / Netstat
1. Abrir la sección **Netstat** dentro de IPVision.
2. En la **tabla izquierda** se muestran todos los datos recolectados de la red: IP local, IP remota, puertos, protocolo, estado de conexión y PID.
3. En la **ventana derecha** se muestran los resultados finales procesados, incluyendo el resumen de conexiones activas y procesos asociados.
4. Los colores de las filas indican el estado de cada conexión: **Rojo** para conexiones cerradas o fallidas, **Amarillo** para advertencias o conexiones inestables, **Verde** para conexiones activas.
5. Se pueden exportar los resultados a **TXT, CSV o LOG**, que incluirá toda la información de la tabla con formato legible.

## Requisitos
- Java 1.8 (puede ser necesario adaptar para versiones más recientes).
- Sistema operativo: Windows, MacOS o Linux.
- No requiere instalación adicional, solo descargar `IPVision.jar`.

## Sobre mi
Hola, soy **Alejo**, estudiante de computación de 5°1.  
Me interesa aprender sobre programación y desarrollo de herramientas para redes y sistemas.
