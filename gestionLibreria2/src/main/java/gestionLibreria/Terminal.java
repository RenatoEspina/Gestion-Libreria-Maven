package gestionLibreria;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import gestionLibreria.excepciones.*;
import gestionLibreria.extensiones.*;
import gestionLibreria.inventario.*;
import gestionLibreria.utilidades.*;

import javafx.collections.ObservableList;

/**
 * Modo terminal de la aplicación de gestión de librería.
 * <p>
 * Presenta un menú interactivo en consola que permite gestionar el inventario
 * de libros, gestionar secciones, registrar y eliminar socios, realizar
 * préstamos y devoluciones, buscar libros y generar reportes en formato Excel
 * con filtros.
 * </p>
 *
 * <p>Toda la lógica de negocio se delega en {@link Inventario}; esta clase
 * solo se ocupa de la interacción con el usuario (lectura de datos, presentación
 * de resultados).</p>
 *
 * @see Inventario
 * @see GestorPersistencia
 */
public class Terminal {

    // ---------------------------------------------------------------
    // Punto de entrada del modo terminal
    // ---------------------------------------------------------------

    /**
     * Inicia el modo terminal, mostrando el menú principal en bucle hasta que
     * el usuario elija la opción de guardar y salir (11).
     *
     * @param inventario inventario con los datos cargados al inicio
     * @param gestor     gestor de persistencia usado para guardar al salir
     */
    public static void modoTerminal(Inventario inventario, GestorPersistencia gestor) {
        System.out.println("Bienvenido al modo terminal");
        Consola.enterParaContinuar();

        int decision = 0;
        while (decision != 10) {
            Consola.limpiarPantalla();
            System.out.println("╔══════════════════════════════╗");
            System.out.println("║   Gestión de Librería        ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Ver Inventario           ║");
            System.out.println("║  2. Gestionar Secciones      ║");
            System.out.println("║  3. Ver Socios               ║");
            System.out.println("║  4. Registrar Socio          ║");
            System.out.println("║  5. Eliminar Socio           ║");
            System.out.println("║  6. Vender Libro             ║");
            System.out.println("║  7. Prestar Libro a Socio    ║");
            System.out.println("║  8. Devolver Libro           ║");
            System.out.println("║  9. Buscar Libro por Nombre  ║");
            System.out.println("║ 10. Filtros y Reporte Excel  ║");
            System.out.println("║ 11. Guardar y Salir          ║");
            System.out.println("╚══════════════════════════════╝");
            decision = Consola.leerEntero("Opción: ");
            Consola.limpiarPantalla();

            switch (decision) {
                case 1:  menuInventario(inventario);        break;
                case 2:  menuSecciones(inventario);         break;
                case 3:  menuSocios(inventario);            break;
                case 4:  registrarSocio(inventario);        break;
                case 5:  eliminarSocio(inventario);         break;
                case 6:  venderLibro(inventario);           break;
                case 7:  prestarLibro(inventario);          break;
                case 8:  devolverLibro(inventario);         break;
                case 9:  buscarLibro(inventario);           break;
                case 10: filtrosYReporte(inventario);       break;
                case 11:
                    guardarYSalir(gestor, inventario);
                    decision = 10;   // fuerza salida del bucle
                    break;
                default:
                    System.out.println("Opción inválida.");
                    Consola.enterParaContinuar();
                    break;
            }
        }
    }

    // ---------------------------------------------------------------
    // Helpers de presentación (Vista)
    // ---------------------------------------------------------------

    /**
     * Muestra en consola la información bibliográfica de un libro.
     * Incluye campos extendidos según el tipo concreto del ejemplar.
     *
     * @param libro libro cuya información se va a imprimir
     */
    private static void mostrarLibro(Libro libro) {
        System.out.println("- Título: " + libro.getTitulo());
        System.out.println("- Fecha de publicación: " + libro.getFechaDePublicacion());

        if (libro.getAutores() != null && !libro.getAutores().isEmpty()) {
            System.out.print("- Autores: ");
            for (int i = 0; i < libro.getAutores().size(); i++) {
                System.out.print(libro.getAutores().get(i));
                if (i < libro.getAutores().size() - 1) System.out.print(", ");
            }
            System.out.println();
        } else {
            System.out.println("- Autores: No especificados");
        }

        System.out.println("- Categoría: " + libro.getCategoria());
        System.out.println("- Páginas: "   + libro.getPaginas());
        System.out.println("- Precio: "    + libro.getPrecio());
        System.out.println("- ID: "        + libro.getIdInterno());

        if (libro instanceof LibroPrestable) {
            LibroPrestable lp = (LibroPrestable) libro;
            System.out.println("Disponibilidad: "   + lp.getDisponibilidad());
            System.out.println("Retraso: "          + lp.getRetraso());
            System.out.println("Multa: "            + lp.getMulta());
            System.out.println("Fecha de Entrega: " + lp.getFechaDevolucion());
            System.out.println("Fecha de Prestamo: "+ lp.getFechaPrestamo());
        } else if (libro instanceof LibroDigital) {
            LibroDigital ld = (LibroDigital) libro;
            System.out.println("Formato: "      + ld.getFormato());
            System.out.println("Memoria (MB): " + ld.getMemoria());
        }
    }

    /**
     * Muestra en consola la información de un socio y sus libros en préstamo.
     *
     * @param socio socio cuya información se va a imprimir
     */
    private static void mostrarSocio(Socio socio) {
        System.out.println("- Nombre: "  + socio.getNombre());
        System.out.println("- RUT: "     + socio.getRut());
        System.out.println("- Numero: "  + socio.getNumeroContacto());
        System.out.print("- Libros prestados: ");
        ObservableList<Libro> prestados = socio.getLibrosPrestados();
        for (int i = 0; i < prestados.size(); i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(prestados.get(i).getTitulo());
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // Opción 1 — Inventario
    // ---------------------------------------------------------------

    private static void menuInventario(Inventario inventario) {
        ObservableList<Seccion> secciones = inventario.getSeccionesAsObservableList();

        if (secciones.isEmpty()) {
            System.out.println("No existen secciones en el inventario.");
            Consola.enterParaContinuar();
            return;
        }

        Seccion seccion = seleccionarSeccion(inventario, secciones);
        if (seccion == null) return;

        ObservableList<String> llaves = seccion.GetLlaves();
        if (llaves.isEmpty()) {
            System.out.println("Esta sección está vacía.");
            Consola.enterParaContinuar();
            return;
        }

        System.out.println("\nLibros en \"" + seccion.getNombre() + "\":");
        for (String nombre : llaves) System.out.println("  - " + nombre);

        String opcion = Consola.leerString("\n¿Ver 'informacion' o 'agregar' libro?: ");

        if (opcion.equalsIgnoreCase("informacion")) {
            verInformacionLibro(seccion);
        } else if (opcion.equalsIgnoreCase("agregar")) {
            agregarLibroASeccion(inventario, seccion);
        } else {
            System.out.println("Opción no reconocida.");
            Consola.enterParaContinuar();
        }
    }

    private static Seccion seleccionarSeccion(Inventario inventario,
                                               ObservableList<Seccion> secciones) {
        while (true) {
            System.out.println("\n--- Secciones Disponibles ---");
            for (Seccion s : secciones) System.out.println("  - " + s.getNombre());

            String nombre = Consola.leerString("\nNombre de la sección (o 'cancelar'): ");
            if (nombre.equalsIgnoreCase("cancelar")) return null;

            Seccion s = inventario.getSeccion(nombre);
            if (s != null) return s;
            System.out.println("La sección '" + nombre + "' no existe.");
        }
    }

    private static void verInformacionLibro(Seccion seccion) {
        String titulo = Consola.leerString("Ingrese el título del libro: ");
        ObservableList<Libro> encontrados = seccion.encontrarLibrosPorTitulo(titulo);

        if (encontrados == null || encontrados.isEmpty()) {
            System.out.println("Libro no encontrado.");
        } else {
            for (Libro l : encontrados) {
                System.out.println("----------------------------------------");
                mostrarLibro(l);
            }
            System.out.println("----------------------------------------");
        }
        Consola.enterParaContinuar();
    }

    private static void agregarLibroASeccion(Inventario inventario, Seccion seccion) {
        System.out.println("Tipo de libro:");
        System.out.println("  1. Normal");
        System.out.println("  2. Prestable");
        System.out.println("  3. Digital");
        int tipo = Consola.leerEntero("Tipo: ");

        LocalDate fechaP  = Consola.leerFecha("Fecha de publicación (yyyy-MM-dd): ");
        String titulo     = Consola.leerString("Título: ");
        String edicion    = Consola.leerString("Edición: ");
        String categoria  = Consola.leerString("Categoría: ");
        int pag           = Consola.leerEntero("Número de páginas: ");
        int id            = inventario.getNumeroLibros() + 1;
        int precio        = Consola.leerEntero("Precio: ");
        List<String> autores = leerAutores();

        Libro nuevo;
        switch (tipo) {
            case 2:
                int multa = Consola.leerEntero("Multa por retraso: ");
                nuevo = new LibroPrestable(fechaP, titulo, edicion, categoria, pag, id, precio,
                                           (ArrayList<String>) autores, multa);
                break;
            case 3:
                int    memoria = Consola.leerEntero("Memoria (MB): ");
                String formato = Consola.leerString("Formato (ej. PDF, EPUB): ");
                nuevo = new LibroDigital(fechaP, titulo, edicion, categoria, pag, id, precio,
                                         (ArrayList<String>) autores, memoria, formato);
                break;
            default:
                nuevo = new Libro(fechaP, titulo, edicion, categoria, pag, id, precio, autores);
                break;
        }

        seccion.agregarLibro(nuevo);
        inventario.incrementarNumeroLibros();
        System.out.println("Libro agregado con éxito! (ID asignado: " + id + ")");
        Consola.enterParaContinuar();
    }

    private static List<String> leerAutores() {
        int n = Consola.leerEntero("¿Cuántos autores?: ");
        List<String> autores = new ArrayList<>();
        for (int i = 0; i < n; i++) autores.add(Consola.leerString("Autor " + (i + 1) + ": "));
        return autores;
    }

    // ---------------------------------------------------------------
    // Opción 2 — Gestionar Secciones
    // ---------------------------------------------------------------

    /**
     * Presenta un submenú para administrar las secciones del inventario.
     * <p>
     * Las operaciones disponibles son:
     * <ul>
     *   <li><b>1 - Listar</b>: muestra todas las secciones con la cantidad
     *       de títulos que contiene cada una.</li>
     *   <li><b>2 - Agregar</b>: solicita un nombre y crea una nueva sección
     *       vacía, rechazando nombres duplicados.</li>
     *   <li><b>3 - Eliminar</b>: solicita el nombre de la sección a borrar y
     *       pide confirmación antes de eliminarla junto con todos sus libros.</li>
     * </ul>
     * </p>
     *
     * @param inventario inventario sobre el que se operará
     */
    private static void menuSecciones(Inventario inventario) {
        System.out.println("=== Gestionar Secciones ===");
        System.out.println("1. Listar secciones");
        System.out.println("2. Agregar sección");
        System.out.println("3. Eliminar sección");
        int opcion = Consola.leerEntero("Opción: ");
        Consola.limpiarPantalla();

        switch (opcion) {
            case 1:
                listarSecciones(inventario);
                break;
            case 2:
                agregarSeccion(inventario);
                break;
            case 3:
                eliminarSeccion(inventario);
                break;
            default:
                System.out.println("Opción inválida.");
                Consola.enterParaContinuar();
                break;
        }
    }

    /**
     * Lista por consola todas las secciones del inventario junto con el número
     * de títulos distintos que contiene cada una.
     * <p>
     * Si no existe ninguna sección informa al usuario y regresa al menú.
     * </p>
     *
     * @param inventario inventario del que se leerán las secciones
     */
    private static void listarSecciones(Inventario inventario) {
        ObservableList<Seccion> secciones = inventario.getSeccionesAsObservableList();

        if (secciones.isEmpty()) {
            System.out.println("No hay secciones registradas.");
            Consola.enterParaContinuar();
            return;
        }

        System.out.println("--- Secciones ---");
        for (Seccion s : secciones) {
            System.out.println("  - " + s.getNombre()
                + "  (" + s.GetLlaves().size() + " título(s))");
        }
        Consola.enterParaContinuar();
    }

    /**
     * Solicita al usuario el nombre de una nueva sección y la registra en el
     * inventario.
     * <p>
     * El nombre no puede estar vacío ni coincidir con una sección ya existente
     * (comparación exacta). Si alguna de estas condiciones no se cumple, se
     * informa del error sin registrar nada.
     * </p>
     *
     * @param inventario inventario donde se dará de alta la sección
     */
    private static void agregarSeccion(Inventario inventario) {
        String nombre = Consola.leerString("Nombre de la nueva sección: ").trim();

        if (inventario.getSecciones().containsKey(nombre)) {
            System.out.println("Error: Ya existe una sección con ese nombre.");
            Consola.enterParaContinuar();
            return;
        }

        inventario.setSeccion(nombre, new Seccion(nombre));
        System.out.println("Sección \"" + nombre + "\" creada con éxito.");
        Consola.enterParaContinuar();
    }

    /**
     * Solicita al usuario el nombre de la sección a eliminar y pide confirmación
     * antes de borrarla definitivamente junto con todos sus libros.
     * <p>
     * Si la sección no existe se notifica al usuario. La confirmación se realiza
     * escribiendo {@code "si"}; cualquier otra entrada cancela la operación.
     * </p>
     *
     * @param inventario inventario del que se eliminará la sección
     */
    private static void eliminarSeccion(Inventario inventario) {
        ObservableList<Seccion> secciones = inventario.getSeccionesAsObservableList();
        if (secciones.isEmpty()) {
            System.out.println("No hay secciones registradas.");
            Consola.enterParaContinuar();
            return;
        }

        System.out.println("--- Secciones disponibles ---");
        for (Seccion s : secciones) {
            System.out.println("  - " + s.getNombre()
                + "  (" + s.GetLlaves().size() + " título(s))");
        }

        String nombre = Consola.leerString("\nNombre de la sección a eliminar (o 'cancelar'): ").trim();
        if (nombre.equalsIgnoreCase("cancelar")) return;

        if (!inventario.getSecciones().containsKey(nombre)) {
            System.out.println("Error: La sección \"" + nombre + "\" no existe.");
            Consola.enterParaContinuar();
            return;
        }

        Seccion seccion = inventario.getSeccion(nombre);
        int totalLibros = seccion.GetLlaves().size();

        System.out.println("⚠ Se eliminará la sección \"" + nombre + "\" con "
            + totalLibros + " título(s). Esta acción no se puede deshacer.");
        String confirmacion = Consola.leerString("¿Confirmar? (si/no): ");

        if (confirmacion.equalsIgnoreCase("si")) {
            inventario.getSecciones().remove(nombre);
            System.out.println("Sección eliminada con éxito.");
        } else {
            System.out.println("Operación cancelada.");
        }
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 3 — Ver socios
    // ---------------------------------------------------------------

    private static void menuSocios(Inventario inventario) {
        ObservableList<Socio> socios = inventario.getSociosAsObservableList();

        if (socios.isEmpty()) {
            System.out.println("No hay socios registrados.");
            Consola.enterParaContinuar();
            return;
        }

        System.out.println("\n--- Socios Registrados ---");
        for (Socio s : socios) {
            System.out.println("  " + s.getNombre() + " | RUT: " + s.getRut()
                + " | Préstamos: " + s.getLibrosPrestados().size());
        }

        String rut = Consola.leerString("\nRUT del socio (o 'cancelar'): ");
        if (rut.equalsIgnoreCase("cancelar")) return;

        try {
            Socio socio = inventario.getSocio(rut);
            System.out.println("----------------------------------------");
            mostrarSocio(socio);
            System.out.println("----------------------------------------");
        } catch (SocioNoEncontradoException e) {
            System.out.println(e.getMessage());
        }
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 4 — Registrar socio
    // ---------------------------------------------------------------

    private static void registrarSocio(Inventario inventario) {
        String nombre = Consola.leerString("Nombre del nuevo socio: ");
        String rut    = Consola.leerString("RUT (xxxxxxxx-x): ");

        if (inventario.getSocios().containsKey(rut)) {
            System.out.println("Error: Ya existe un socio con ese RUT.");
            Consola.enterParaContinuar();
            return;
        }

        String numero = Consola.leerString("Teléfono (+569xxxxxxxx): ");
        inventario.setSocio(rut, new Socio(nombre, rut, numero));
        System.out.println("Socio registrado con éxito!");
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 5 — Eliminar socio
    // ---------------------------------------------------------------

    /**
     * Solicita el RUT de un socio y lo elimina del sistema previa confirmación.
     * <p>
     * No se permite eliminar a un socio que tenga libros pendientes de
     * devolución; en ese caso se informa el error y no se realiza ninguna
     * modificación.
     * </p>
     *
     * @param inventario inventario del que se eliminará el socio
     */
    private static void eliminarSocio(Inventario inventario) {
        ObservableList<Socio> socios = inventario.getSociosAsObservableList();

        if (socios.isEmpty()) {
            System.out.println("No hay socios registrados.");
            Consola.enterParaContinuar();
            return;
        }

        System.out.println("\n--- Socios Registrados ---");
        for (Socio s : socios) {
            System.out.println("  " + s.getNombre() + " | RUT: " + s.getRut()
                + " | Préstamos activos: " + s.getLibrosPrestados().size());
        }

        String rut = Consola.leerString("\nRUT del socio a eliminar (o 'cancelar'): ");
        if (rut.equalsIgnoreCase("cancelar")) return;

        Socio socio;
        try {
            socio = inventario.getSocio(rut);
        } catch (SocioNoEncontradoException e) {
            System.out.println(e.getMessage());
            Consola.enterParaContinuar();
            return;
        }

        if (!socio.getLibrosPrestados().isEmpty()) {
            System.out.println("Error: El socio tiene "
                + socio.getLibrosPrestados().size()
                + " libro(s) pendiente(s) de devolución. "
                + "Deben devolverse antes de eliminar el socio.");
            Consola.enterParaContinuar();
            return;
        }

        String confirmacion = Consola.leerString(
            "¿Eliminar a \"" + socio.getNombre() + "\" (RUT: " + rut + ")? (si/no): ");

        if (confirmacion.equalsIgnoreCase("si")) {
            inventario.eliminarSocio(rut);
            System.out.println("Socio eliminado con éxito.");
        } else {
            System.out.println("Operación cancelada.");
        }
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 6 — Vender libro
    // ---------------------------------------------------------------

    private static void venderLibro(Inventario inventario) {
        String nombre = Consola.leerString("Nombre del libro a vender: ");
        Seccion seccion = inventario.encontrarSeccionDeLibro(nombre);
        if (seccion == null) {
            System.out.println("Libro no encontrado en ninguna sección.");
            Consola.enterParaContinuar();
            return;
        }

        ObservableList<Libro> lista = seccion.encontrarLibrosPorTitulo(nombre);
        if (lista == null || lista.isEmpty()) {
            System.out.println("Libro No Existe!!");
        } else if (lista.size() == 1) {
            seccion.venderLibro(nombre, lista.get(0).getIdInterno());
            System.out.println("Libro Vendido con Exito!!!");
        } else {
            System.out.println("Múltiples ejemplares encontrados:");
            for (Libro l : lista) System.out.println("  ID " + l.getIdInterno() + " - " + l.getTitulo());
            int id = Consola.leerEntero("Ingrese id del libro: ");
            if (seccion.venderLibro(nombre, id)) {
                System.out.println("Libro Vendido con Exito!!!");
            } else {
                System.out.println("No se encontró un libro con ese ID.");
            }
        }
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 7 — Prestar libro
    // ---------------------------------------------------------------

    private static void prestarLibro(Inventario inventario) {
        String rut = Consola.leerString("RUT del socio: ");
        Socio socio;
        try {
            socio = inventario.getSocio(rut);
        } catch (SocioNoEncontradoException e) {
            System.out.println(e.getMessage());
            Consola.enterParaContinuar();
            return;
        }

        String nombre = Consola.leerString("Nombre del libro: ");
        ObservableList<Libro> libros = inventario.encontrarLibro(nombre);
        if (libros == null || libros.isEmpty()) {
            System.out.println("Libro no encontrado.");
            Consola.enterParaContinuar();
            return;
        }

        Libro libro;
        try {
            if (libros.size() == 1) {
                libro = libros.get(0);
            } else {
                System.out.println("Múltiples ejemplares encontrados:");
                for (Libro l : libros) {
                    String disp = (l instanceof LibroPrestable)
                        ? (((LibroPrestable) l).getDisponibilidad() ? " [Disponible]" : " [Prestado]")
                        : "";
                    System.out.println("  ID " + l.getIdInterno() + " - " + l.getTitulo() + disp);
                }
                int idL = Consola.leerEntero("ID del libro: ");
                libro = libros.stream()
                    .filter(l -> l.getIdInterno() == idL)
                    .findFirst()
                    .orElseThrow(() -> new LibroNoEncontradoException("No se encontró un libro con ID: " + idL));
            }
        } catch (LibroNoEncontradoException e) {
            System.out.println(e.getMessage());
            Consola.enterParaContinuar();
            return;
        }

        int dias = Consola.leerEntero("Días de plazo (0 para indefinido): ");
        boolean ok;
        if (dias > 0) {
            ok = inventario.prestarLibro(socio, libro, dias);
        } else {
            ok = inventario.prestarLibro(socio, libro);
        }
        System.out.println(ok
            ? "Préstamo realizado con éxito!"
            : "El libro no está disponible para préstamo.");
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 8 — Devolver libro
    // ---------------------------------------------------------------

    private static void devolverLibro(Inventario inventario) {
        String rut = Consola.leerString("RUT del socio: ");
        Socio socio;
        try {
            socio = inventario.getSocio(rut);
        } catch (SocioNoEncontradoException e) {
            System.out.println(e.getMessage());
            Consola.enterParaContinuar();
            return;
        }

        ObservableList<Libro> prestados = socio.getLibrosPrestados();
        if (prestados.isEmpty()) {
            System.out.println(socio.getNombre() + " no tiene libros prestados.");
            Consola.enterParaContinuar();
            return;
        }

        System.out.println("\nLibros prestados a " + socio.getNombre() + ":");
        for (Libro l : prestados) {
            String fechaStr = "";
            if (l instanceof LibroPrestable && ((LibroPrestable) l).getFechaPrestamo() != null) {
                fechaStr = " (desde: " + ((LibroPrestable) l).getFechaPrestamo() + ")";
            }
            System.out.println("  ID " + l.getIdInterno() + " - " + l.getTitulo() + fechaStr);
        }

        int id = Consola.leerEntero("ID del libro a devolver: ");
        Libro libro = prestados.stream()
                               .filter(l -> l.getIdInterno() == id)
                               .findFirst()
                               .orElse(null);

        if (libro == null) {
            System.out.println("No se encontró ese libro en los préstamos del socio.");
            Consola.enterParaContinuar();
            return;
        }
        if (!(libro instanceof LibroPrestable)) {
            System.out.println("Error interno: el libro no es de tipo prestable.");
            Consola.enterParaContinuar();
            return;
        }

        int multa = inventario.devolverLibro(socio, (LibroPrestable) libro);
        if (multa > 0) {
            System.out.println("⚠ Libro devuelto con retraso. Multa aplicada: $" + multa);
        }
        System.out.println("Libro devuelto con éxito!");
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 9 — Buscar libro
    // ---------------------------------------------------------------

    private static void buscarLibro(Inventario inventario) {
        String nombre = Consola.leerString("Nombre del libro: ");
        ObservableList<Libro> libros = inventario.encontrarLibro(nombre);
        if (libros == null || libros.isEmpty()) {
            System.out.println("Libro no encontrado.");
        } else {
            for (Libro l : libros) {
                System.out.println("----------------------------------------");
                mostrarLibro(l);
            }
            System.out.println("----------------------------------------");
        }
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 10 — Filtros y Reporte Excel
    // ---------------------------------------------------------------

    private static void filtrosYReporte(Inventario inventario) {
        System.out.println("=== Filtros y Reporte Excel ===");
        System.out.println("1. Filtrar por categoría");
        System.out.println("2. Filtrar por precio mínimo");
        System.out.println("3. Ver libros prestables disponibles");
        System.out.println("4. Ver libros actualmente en préstamo");
        int opcion = Consola.leerEntero("Tipo de filtro: ");

        List<Libro> todos     = getAllLibros(inventario);
        List<Libro> filtrados = new ArrayList<>();
        String descripcion;

        switch (opcion) {
            case 1:
                String cat = Consola.leerString("Categoría a buscar (texto parcial): ");
                descripcion = "Categoría contiene: '" + cat + "'";
                for (Libro l : todos) {
                    if (l.getCategoria().toLowerCase().contains(cat.toLowerCase())) filtrados.add(l);
                }
                break;
            case 2:
                int precioMin = Consola.leerEntero("Precio mínimo: ");
                descripcion = "Precio >= " + precioMin;
                for (Libro l : todos) {
                    if (l.getPrecio() >= precioMin) filtrados.add(l);
                }
                break;
            case 3:
                descripcion = "Libros prestables disponibles";
                for (Libro l : todos) {
                    if (l instanceof LibroPrestable && ((LibroPrestable) l).getDisponibilidad()) filtrados.add(l);
                }
                break;
            case 4:
                descripcion = "Libros actualmente en préstamo";
                for (Libro l : todos) {
                    if (l instanceof LibroPrestable && !((LibroPrestable) l).getDisponibilidad()) filtrados.add(l);
                }
                break;
            default:
                System.out.println("Opción inválida.");
                Consola.enterParaContinuar();
                return;
        }

        if (filtrados.isEmpty()) {
            System.out.println("No se encontraron libros con ese criterio.");
            Consola.enterParaContinuar();
            return;
        }

        System.out.println("\n--- Resultados (" + filtrados.size() + " libro(s)) ---");
        System.out.println("Filtro: " + descripcion);
        System.out.println("----------------------------------------");
        for (Libro l : filtrados) {
            Seccion sec  = inventario.encontrarSeccionDeLibro(l.getTitulo());
            String tipo  = l instanceof LibroDigital   ? "Digital"
                         : l instanceof LibroPrestable ? "Prestable"
                         : "Base";
            String dispStr = (l instanceof LibroPrestable)
                ? " | " + (((LibroPrestable) l).getDisponibilidad() ? "Disponible" : "Prestado")
                : "";
            System.out.printf("  [%s] ID:%-4d %-35s Categoría: %-15s Precio: $%-6d Tipo: %s%s%n",
                sec != null ? sec.getNombre() : "N/A",
                l.getIdInterno(), l.getTitulo(), l.getCategoria(), l.getPrecio(), tipo, dispStr);
        }
        System.out.println("----------------------------------------");

        String exportar = Consola.leerString("\n¿Exportar a Excel? (si/no): ");
        if (exportar.equalsIgnoreCase("si")) {
            String filename = "Reporte_" + opcion + "_" + LocalDate.now() + ".xlsx";
            try {
                ExportadorExcel.generarReporteLibros(inventario, filtrados, filename);
                System.out.println("Reporte guardado como: " + filename);
            } catch (IOException e) {
                System.out.println("Error al generar Excel: " + e.getMessage());
            }
        }
        Consola.enterParaContinuar();
    }

    // ---------------------------------------------------------------
    // Opción 11 — Guardar y salir
    // ---------------------------------------------------------------

    private static void guardarYSalir(GestorPersistencia gestor, Inventario inventario) {
        try {
            System.out.println("Guardando datos...");
            gestor.guardarTodo(inventario);
            System.out.println("Datos guardados con éxito. ¡Hasta luego!");
        } catch (Exception e) {
            System.err.println("Error al guardar: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Utilidades internas
    // ---------------------------------------------------------------

    private static List<Libro> getAllLibros(Inventario inventario) {
        List<Libro> all = new ArrayList<>();
        for (Seccion s : inventario.getSecciones().values()) {
            for (ObservableList<Libro> lista : s.getLibros().values()) all.addAll(lista);
        }
        return all;
    }
}