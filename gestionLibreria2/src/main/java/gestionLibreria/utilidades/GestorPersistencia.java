package gestionLibreria.utilidades;

import gestionLibreria.excepciones.*;
import gestionLibreria.extensiones.*;
import gestionLibreria.inventario.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javafx.collections.ObservableList;

/**
 * Responsable de la persistencia del inventario en archivos CSV.
 * <p>
 * Gestiona cuatro archivos dentro de un directorio base escribible en el
 * sistema de archivos:
 * <ul>
 *   <li>{@code secciones.csv} — nombres de las secciones del inventario.</li>
 *   <li>{@code libros.csv}    — todos los ejemplares con sus atributos y tipo.</li>
 *   <li>{@code socios.csv}    — datos de socios y los IDs de libros que tienen prestados.</li>
 *   <li>{@code config.csv}    — contador histórico de libros para generar IDs únicos.</li>
 * </ul>
 * </p>
 *
 * <p><b>Compatibilidad con Maven:</b> al construir el gestor, si alguno de los
 * archivos CSV no existe en la ruta de sistema de archivos, se intenta copiarlo
 * desde los recursos del classpath (p. ej. {@code src/main/resources/}). Si
 * tampoco existe ahí, se crea con solo su encabezado. Las escrituras siempre se
 * realizan en el sistema de archivos, nunca dentro del JAR.</p>
 *
 * <p>Los campos CSV se escapan correctamente para manejar comas, comillas y
 * saltos de línea dentro de los valores.</p>
 *
 * @see LectorCSV
 * @see Inventario
 */
public class GestorPersistencia {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    /** Ruta al archivo CSV de secciones. */
    private final String rutaSecciones;

    /** Ruta al archivo CSV de libros. */
    private final String rutaLibros;

    /** Ruta al archivo CSV de socios. */
    private final String rutaSocios;

    /** Ruta al archivo CSV de configuración (contador de libros). */
    private final String rutaConfig;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    /**
     * Construye el gestor de persistencia apuntando al directorio base indicado.
     * <p>
     * Si el directorio no existe, se crea. Para cada archivo CSV:
     * <ol>
     *   <li>Si ya existe en disco, no se toca.</li>
     *   <li>Si no existe, se intenta copiar desde los recursos del classpath
     *       (útil en proyectos Maven donde los CSV iniciales se ubican en
     *       {@code src/main/resources/}).</li>
     *   <li>Si tampoco existe en el classpath, se crea vacío con su encabezado.</li>
     * </ol>
     * La ruta base debe apuntar a un directorio escribible fuera del JAR, por
     * ejemplo {@code "data/"} (relativo al directorio de ejecución) o
     * {@code System.getProperty("user.home") + "/.gestionLibreria/"}.
     * </p>
     *
     * @param rutaBase ruta del directorio donde se almacenan los archivos CSV
     *                 (p. ej. {@code "data/"})
     * @throws IOException si ocurre un error al crear el directorio o los archivos
     */
    public GestorPersistencia(String rutaBase) throws IOException {
        this.rutaSecciones = rutaBase + "secciones.csv";
        this.rutaLibros    = rutaBase + "libros.csv";
        this.rutaSocios    = rutaBase + "socios.csv";
        this.rutaConfig    = rutaBase + "config.csv";

        File carpeta = new File(rutaBase);
        if (!carpeta.exists()) carpeta.mkdirs();

        crearArchivoSiNoExiste(rutaSecciones, "nombre_seccion\n");
        crearArchivoSiNoExiste(rutaLibros,
            "seccion,id,titulo,autores,edicion,categoria,paginas,fecha_pub,precio,tipo," +
            "memoria,formato_digital,disponibilidad,retraso,multa,f_prestamo,f_devolucion\n");
        crearArchivoSiNoExiste(rutaSocios,  "nombre,rut,contacto,ids_prestados\n");
        crearArchivoSiNoExiste(rutaConfig,  "numero_de_libros\n");
    }

    // ---------------------------------------------------------------
    // API pública: guardar y cargar
    // ---------------------------------------------------------------

    /**
     * Persiste el estado completo del inventario en los cuatro archivos CSV.
     *
     * @param inventario el inventario a guardar
     * @throws IOException si ocurre un error de escritura en algún archivo
     */
    public void guardarTodo(Inventario inventario) throws IOException {
        guardarSecciones(inventario);
        guardarLibros(inventario);
        guardarSocios(inventario);
        guardarConfiguracion(inventario);
    }

    /**
     * Carga y reconstruye el inventario completo desde los archivos CSV.
     * <p>
     * El orden de carga es importante: primero secciones, luego libros (que
     * se asignan a sus secciones) y finalmente socios (que referencian libros
     * ya cargados por ID).
     * </p>
     *
     * @return el {@link Inventario} con todos los datos restaurados
     * @throws IOException si ocurre un error de lectura en algún archivo
     */
    public Inventario cargarTodo() throws IOException {
        Inventario inventario = new Inventario(new HashMap<>(), new HashMap<>());
        cargarSecciones(inventario);
        cargarLibros(inventario);
        cargarSocios(inventario);
        cargarConfiguracion(inventario);
        return inventario;
    }

    // ---------------------------------------------------------------
    // Guardado por archivo
    // ---------------------------------------------------------------

    /**
     * Escribe el archivo de configuración con el contador histórico de libros.
     *
     * @param inventario inventario del que se extrae el contador
     * @throws IOException si ocurre un error de escritura
     */
    private void guardarConfiguracion(Inventario inventario) throws IOException {
        try (FileWriter writer = new FileWriter(rutaConfig)) {
            writer.write("numero_de_libros\n");
            writer.write(inventario.getNumeroLibros() + "\n");
        }
    }

    /**
     * Escribe el archivo de secciones con el nombre de cada sección del inventario.
     *
     * @param inventario inventario del que se extraen las secciones
     * @throws IOException si ocurre un error de escritura
     */
    private void guardarSecciones(Inventario inventario) throws IOException {
        try (FileWriter writer = new FileWriter(rutaSecciones)) {
            writer.write("nombre_seccion\n");
            for (String nombre : inventario.getSecciones().keySet()) {
                writer.write(escapeCSV(nombre) + "\n");
            }
        }
    }

    /**
     * Escribe el archivo de libros con todos los ejemplares del inventario.
     * <p>
     * Cada libro se serializa según su tipo ({@code BASE}, {@code PRESTABLE} o
     * {@code DIGITAL}), rellenando con vacíos las columnas que no apliquen.
     * </p>
     *
     * @param inventario inventario del que se extraen los libros
     * @throws IOException si ocurre un error de escritura
     */
    private void guardarLibros(Inventario inventario) throws IOException {
        try (FileWriter writer = new FileWriter(rutaLibros)) {
            writer.write("seccion,id,titulo,autores,edicion,categoria,paginas,fecha_pub,precio,tipo," +
                         "memoria,formato_digital,disponibilidad,retraso,multa,f_prestamo,f_devolucion\n");

            for (Seccion s : inventario.getSecciones().values()) {
                for (ObservableList<Libro> lista : s.getLibros().values()) {
                    for (Libro l : lista) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(escapeCSV(s.getNombre())).append(",");
                        sb.append(escapeCSV(l.getIdInterno())).append(",");
                        sb.append(escapeCSV(l.getTitulo())).append(",");
                        sb.append(escapeCSV(String.join(";", l.getAutores()))).append(",");
                        sb.append(escapeCSV(l.getEdicion())).append(",");
                        sb.append(escapeCSV(l.getCategoria())).append(",");
                        sb.append(escapeCSV(l.getPaginas())).append(",");
                        sb.append(escapeCSV(l.getFechaDePublicacion())).append(",");
                        sb.append(escapeCSV(l.getPrecio())).append(",");

                        if (l instanceof LibroDigital) {
                            LibroDigital ld = (LibroDigital) l;
                            sb.append("DIGITAL,")
                              .append(escapeCSV(ld.getMemoria())).append(",")
                              .append(escapeCSV(ld.getFormato())).append(",,,,,");
                        } else if (l instanceof LibroPrestable) {
                            LibroPrestable lp = (LibroPrestable) l;
                            sb.append("PRESTABLE,,,")
                              .append(escapeCSV(lp.getDisponibilidad())).append(",")
                              .append(escapeCSV(lp.getRetraso())).append(",")
                              .append(escapeCSV(lp.getMulta())).append(",")
                              .append(escapeCSV(lp.getFechaPrestamo())).append(",")
                              .append(escapeCSV(lp.getFechaDevolucion()));
                        } else {
                            sb.append("BASE,,,,,,,,");
                        }

                        writer.write(sb.toString() + "\n");
                    }
                }
            }
        }
    }

    /**
     * Escribe el archivo de socios con sus datos personales y los IDs de los
     * libros que tienen actualmente en préstamo.
     *
     * @param inventario inventario del que se extraen los socios
     * @throws IOException si ocurre un error de escritura
     */
    private void guardarSocios(Inventario inventario) throws IOException {
        try (FileWriter writer = new FileWriter(rutaSocios)) {
            writer.write("nombre,rut,contacto,ids_prestados\n");
            for (Socio socio : inventario.getSocios().values()) {
                List<String> ids = new ArrayList<>();
                for (Libro l : socio.getLibrosPrestados()) {
                    ids.add(String.valueOf(l.getIdInterno()));
                }
                writer.write(String.format("%s,%s,%s,%s\n",
                    escapeCSV(socio.getNombre()),
                    escapeCSV(socio.getRut()),
                    escapeCSV(socio.getNumeroContacto()),
                    escapeCSV(String.join(";", ids))));
            }
        }
    }

    // ---------------------------------------------------------------
    // Carga por archivo
    // ---------------------------------------------------------------

    /**
     * Carga la configuración (contador histórico de libros) desde {@code config.csv}.
     *
     * @param inv inventario al que se aplica el contador
     * @throws IOException si ocurre un error de lectura
     */
    private void cargarConfiguracion(Inventario inv) throws IOException {
        File f = new File(rutaConfig);
        if (!f.exists()) return;

        LectorCSV lector = new LectorCSV(rutaConfig);
        List<List<String>> datos = lector.readAll();

        if (datos.size() > 1) {
            try {
                int numLibros = Integer.parseInt(datos.get(1).get(0));
                inv.setNumeroLibros(numLibros);
            } catch (Exception e) {
                System.err.println("Error al cargar configuración: " + e.getMessage());
            }
        }
    }

    /**
     * Carga las secciones desde {@code secciones.csv} y las registra en el inventario.
     *
     * @param inv inventario donde se registran las secciones
     * @throws IOException si ocurre un error de lectura
     */
    private void cargarSecciones(Inventario inv) throws IOException {
        LectorCSV lector = new LectorCSV(rutaSecciones);
        List<List<String>> datos = lector.readAll();
        for (int i = 1; i < datos.size(); i++) {
            String nombre = unescapeCSV(datos.get(i).get(0));
            inv.setSeccion(nombre, new Seccion(nombre));
        }
    }

    /**
     * Carga los libros desde {@code libros.csv}, los instancia según su tipo y los
     * agrega a las secciones correspondientes del inventario.
     *
     * @param inv inventario donde se registran los libros
     * @return mapa de ID → {@link Libro} para que {@link #cargarSocios} pueda
     *         referenciar los objetos ya instanciados
     * @throws IOException si ocurre un error de lectura
     */
    private HashMap<Integer, Libro> cargarLibros(Inventario inv) throws IOException {
        HashMap<Integer, Libro> librosCargados = new HashMap<>();
        LectorCSV lector = new LectorCSV(rutaLibros);
        List<List<String>> datos = lector.readAll();

        for (int i = 1; i < datos.size(); i++) {
            List<String> f = datos.get(i);
            try {
                String    secNombre = unescapeCSV(f.get(0));
                int       id        = Integer.parseInt(f.get(1));
                String    titulo    = unescapeCSV(f.get(2));
                ArrayList<String> autores = new ArrayList<>(
                        Arrays.asList(unescapeCSV(f.get(3)).split(";")));
                String    edicion   = unescapeCSV(f.get(4));
                String    cat       = unescapeCSV(f.get(5));
                int       pag       = Integer.parseInt(f.get(6));
                LocalDate fecha     = LocalDate.parse(f.get(7));
                int       precio    = Integer.parseInt(f.get(8));
                String    tipo      = f.get(9);

                Libro libro;
                if ("DIGITAL".equals(tipo)) {
                    int    memoria        = Integer.parseInt(f.get(10));
                    String formatoDigital = unescapeCSV(f.get(11));
                    libro = new LibroDigital(fecha, titulo, edicion, cat, pag, id, precio,
                                             autores, memoria, formatoDigital);
                } else if ("PRESTABLE".equals(tipo)) {
                    boolean   disponibilidad = Boolean.parseBoolean(unescapeCSV(f.get(12)));
                    int       retraso        = Integer.parseInt(f.get(13));
                    int       multa          = Integer.parseInt(f.get(14));
                    LocalDate fPrestamo      = f.get(15).isEmpty() ? null : LocalDate.parse(f.get(15));
                    LocalDate fDevolucion    = f.get(16).isEmpty() ? null : LocalDate.parse(f.get(16));
                    libro = new LibroPrestable(fecha, titulo, edicion, cat, pag, id, precio,
                                               autores, disponibilidad, retraso, multa,
                                               fPrestamo, fDevolucion);
                } else {
                    libro = new Libro(fecha, titulo, edicion, cat, pag, id, precio, autores);
                }

                if (inv.getSeccion(secNombre) != null) {
                    inv.getSeccion(secNombre).agregarLibro(libro);
                    librosCargados.put(id, libro);
                }
            } catch (Exception e) {
                System.err.println("Error en línea " + i + ": " + e.getMessage());
            }
        }
        return librosCargados;
    }

    /**
     * Carga los socios desde {@code socios.csv} y les asigna los libros que tienen
     * prestados, buscándolos en el inventario ya cargado por su ID.
     *
     * @param inv inventario con los libros ya cargados
     * @throws IOException si ocurre un error de lectura en el archivo de socios
     */
    private void cargarSocios(Inventario inv) throws IOException {
        LectorCSV lector = new LectorCSV(rutaSocios);
        List<List<String>> datos = lector.readAll();

        for (int i = 1; i < datos.size(); i++) {
            List<String> f  = datos.get(i);
            String nombre   = unescapeCSV(f.get(0));
            String rut      = unescapeCSV(f.get(1));
            String contacto = unescapeCSV(f.get(2));
            String idsStr   = f.size() > 3 ? unescapeCSV(f.get(3)) : "";

            List<Libro> prestados = new ArrayList<>();
            if (!idsStr.isEmpty()) {
                for (String idStr : idsStr.split(";")) {
                    try {
                        Libro l = inv.encontrarLibro(Integer.parseInt(idStr));
                        prestados.add(l);
                    } catch (LibroNoEncontradoException e) {
                        System.err.println("Advertencia al cargar socio '" + nombre
                            + "': " + e.getMessage());
                    }
                }
            }
            inv.setSocio(rut, new Socio(nombre, rut, contacto, prestados));
        }
    }

    // ---------------------------------------------------------------
    // Utilidades CSV
    // ---------------------------------------------------------------

    /**
     * Escapa un valor de texto para uso seguro en CSV.
     * <p>
     * Si el valor contiene comas, comillas o saltos de línea, se encierra entre
     * comillas dobles y las comillas internas se duplican ({@code ""}).
     * </p>
     *
     * @param value valor a escapar; retorna cadena vacía si es {@code null}
     * @return valor escapado para CSV
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Convierte un entero a su representación de texto para CSV.
     *
     * @param value valor entero
     * @return representación en texto
     */
    private String escapeCSV(int value) {
        return String.valueOf(value);
    }

    /**
     * Convierte un booleano a su representación de texto para CSV.
     *
     * @param value valor booleano
     * @return {@code "true"} o {@code "false"}
     */
    private String escapeCSV(boolean value) {
        return String.valueOf(value);
    }

    /**
     * Convierte una fecha a su representación ISO para CSV.
     *
     * @param value fecha; retorna cadena vacía si es {@code null}
     * @return fecha en formato {@code YYYY-MM-DD} o cadena vacía
     */
    private String escapeCSV(LocalDate value) {
        return value != null ? value.toString() : "";
    }

    /**
     * Revierte el escapado CSV de un campo de texto.
     * <p>
     * Quita las comillas exteriores si las hay y convierte las dobles comillas
     * internas en comillas simples.
     * </p>
     *
     * @param value valor leído del CSV
     * @return valor original sin escapado
     */
    private String unescapeCSV(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            return value.replace("\"\"", "\"");
        }
        return value;
    }

    // ---------------------------------------------------------------
    // Inicialización de archivos
    // ---------------------------------------------------------------

    /**
     * Crea un archivo CSV en disco si todavía no existe.
     * <p>
     * La lógica de creación sigue este orden de prioridad:
     * <ol>
     *   <li>Si el archivo ya existe en disco, no se hace nada.</li>
     *   <li>Si existe un recurso con el mismo nombre de archivo en el classpath
     *       (p. ej. empaquetado por Maven desde {@code src/main/resources/}),
     *       se copia a la ruta de destino en disco.</li>
     *   <li>Si tampoco existe en el classpath, se crea el archivo vacío con el
     *       encabezado CSV proporcionado.</li>
     * </ol>
     * Esto permite que en proyectos Maven los datos iniciales definidos en
     * {@code src/main/resources/} se propaguen al directorio de trabajo
     * escribible la primera vez que se ejecuta la aplicación.
     * </p>
     *
     * @param ruta       ruta del archivo a crear en el sistema de archivos
     * @param encabezado primera línea (encabezado) a escribir si no hay recurso
     *                   en el classpath del que copiar
     * @throws IOException si ocurre un error al crear o escribir el archivo
     */
    private void crearArchivoSiNoExiste(String ruta, String encabezado) throws IOException {
        File archivo = new File(ruta);
        if (archivo.exists()) return;

        // Intenta copiar desde los recursos del classpath (Maven)
        String resourceName = "/" + archivo.getName();
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            if (is != null) {
                Files.copy(is, archivo.toPath());
            } else {
                // No existe en classpath: crear vacío con solo el encabezado
                try (FileWriter writer = new FileWriter(archivo)) {
                    writer.write(encabezado);
                }
            }
        }
    }
}