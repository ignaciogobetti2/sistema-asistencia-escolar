# sistema-asistencia-escolar
Este es el repositorio de la aplicación de escritorio para controlar la asistencia de alumnos. Está desarrollada en **Java** (con interfaz gráfica de usuario) y conectada de forma dinámica a una base de datos **MySQL**.

## ¿Qué hace la aplicación?
* **Interfaz visual:** Creada con Java Swing, fácil de usar.
* **Carga dinámica:** Al elegir un curso, el sistema busca automáticamente las materias y los alumnos que corresponden en la base de datos.
* **Asistencia real:** Te permite tildar quién está presente, quién está ausente y guardarlo directamente en el servidor MySQL con fecha y hora exacta.

## Tecnologías que usé
* **Lenguaje:** Java (SE 17)
* **IDE:** Eclipse
* **Base de Datos:** MySQL Server 8.4 (¡Ojo con la versión!)
* **Conector:** MySQL Connector/J

---

## ¿Cómo hacerlo correr en tu máquina? 

Para probar la aplicación y ver que carguen todos los datos, seguí estos tres pasos:

### 1. Levantar la Base de Datos
Antes de arrancar el programa en Java, tenés que ir a **MySQL Workbench** y ejecutar el script `.sql` que dejé adjunto en la carpeta del proyecto. Eso te va a crear la base de datos `control_asistencia_db` con las tablas y algunos alumnos de prueba para que no aparezca vacía.

### 2. Chequear el Puerto
El sistema está configurado para trabajar en el puerto por defecto de MySQL, que es el **`3306`**. Asegurate de que tu servidor local corra ahí.

### 3. Ajustar la Contraseña de MySQL 
**IMPORTANTE:** Para el desarrollo de este proyecto se configuró el acceso con contraseña para el usuario `root` en la base de datos. 

El código fuente tiene declarada la clave en la **línea 21** del archivo `AppAsistencia.java`. Si vas a correr el proyecto en tu entorno de evaluación, recordá abrir ese archivo y verificar que el campo `PASS` coincida con la contraseña de tu servidor local de MySQL:

```java
private static final String USER = "root";
private static final String PASS = "TuContraseñaAquí"; // <-- Cambiala por la tuya
