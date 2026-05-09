package gestionLibreria.inventario;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import gestionLibreria.excepciones.*;
import gestionLibreria.extensiones.*;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Modelo central de la aplicación que agrupa todas las secciones, socios y
 * el contador histórico de libros registrados.
 * <p>
 * Es el punto de entrada para todas las operaciones de negocio sobre el
 * inventario: búsqueda de libros, préstamos y devoluciones.
 * </p>
 *
 * @see Seccion
 * @see Socio
 * @see gestionLibreria.utilidades.GestorPersistencia
 */
public class Inventario {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    /** Mapa de secciones indexadas por su nombre. */
    private final ObservableMap<String, Seccion> secciones;

    /** Mapa de socios indexados por su RUT ({@code xxxxxxxx-x}). */
    private final ObservableMap<String, Socio> socios;

    /**
     * Contador histórico del total de libros que han pasado por el inventario.
     * Se usa para generar IDs únicos al dar de alta nuevos ejemplares.
     */
    private final SimpleIntegerProperty numeroDeLibros;

    // ---------------------------------------------------------------
    // Constructores
    // ---------------------------------------------------------------

    /**
     * Construye un inventario a partir de mapas ya poblados.
     * Utilizado por {@link gestionLibreria.utilidades.GestorPersistencia#cargarTodo()}.
     */
    public Inventario(HashMap<String, Seccion> secciones, HashMap<String, Socio> socios) {
        this.secciones      = FXCollections.observableHashMap();
        this.secciones.putAll(secciones);
        this.socios         = FXCollections.observableHashMap();
        this.socios.putAll(socios);
        this.numeroDeLibros = new SimpleIntegerProperty(0);
    }

    /** Construye un inventario vacío. */
    public Inventario() {
        this.secciones      = FXCollections.observableHashMap();
        this.socios         = FXCollections.observableHashMap();
        this.numeroDeLibros = new SimpleIntegerProperty(0);
    }

    // ---------------------------------------------------------------
    // Secciones
    // ---------------------------------------------------------------

    public ObservableMap<String, Seccion> getSecciones() { return secciones; }

    public Seccion getSeccion(String nombre) { return secciones.get(nombre); }

    public ObservableList<Seccion> getSeccionesAsObservableList() {
        return FXCollections.observableArrayList(secciones.values());
    }

    public void setSeccion(String nombre, Seccion seccion) { secciones.put(nombre, seccion); }

    // ---------------------------------------------------------------
    // Socios
    // ---------------------------------------------------------------

    public ObservableMap<String, Socio> getSocios() { return socios; }

    /**
     * Busca y retorna un socio por su RUT.
     *
     * @param rut RUT del socio en formato {@code xxxxxxxx-x}
     * @return el socio encontrado
     * @throws SocioNoEncontradoException si no existe ningún socio con ese RUT
     */
    public Socio getSocio(String rut) throws SocioNoEncontradoException {
        Socio socio = socios.get(rut);
        if (socio == null) throw new SocioNoEncontradoException("Socio no encontrado con RUT: " + rut);
        return socio;
    }

    public ObservableList<Socio> getSociosAsObservableList() {
        return FXCollections.observableArrayList(socios.values());
    }

    public void setSocio(String rut, Socio socio) { socios.put(rut, socio); }

    public void eliminarSocio(String rut) { socios.remove(rut); }

    // ---------------------------------------------------------------
    // Contador de libros
    // ---------------------------------------------------------------

    public int getNumeroLibros() { return numeroDeLibros.get(); }
    public void setNumeroLibros(int numero) { numeroDeLibros.set(numero); }
    public void incrementarNumeroLibros() { numeroDeLibros.set(numeroDeLibros.get() + 1); }

    // ---------------------------------------------------------------
    // Búsqueda de libros
    // ---------------------------------------------------------------

    /**
     * Encuentra la sección que contiene un libro con el título dado.
     *
     * @param titulo título exacto del libro
     * @return la {@link Seccion} que lo contiene, o {@code null} si no se encuentra
     */
    public Seccion encontrarSeccionDeLibro(String titulo) {
        for (Seccion s : secciones.values()) {
            if (s.getLibros().containsKey(titulo)) return s;
        }
        return null;
    }

    /**
     * Retorna todos los ejemplares cuyo título coincida exactamente con el dado,
     * buscando en todas las secciones.
     *
     * @param titulo título exacto del libro
     * @return lista observable de ejemplares; lista vacía si no existe ninguno
     */
    public ObservableList<Libro> encontrarLibro(String titulo) {
        Seccion seccion = encontrarSeccionDeLibro(titulo);
        if (seccion == null) return FXCollections.emptyObservableList();
        ObservableList<Libro> resultado = seccion.encontrarLibrosPorTitulo(titulo);
        return resultado != null ? resultado : FXCollections.emptyObservableList();
    }

    /**
     * Busca un ejemplar específico por su ID interno, recorriendo todas las secciones.
     *
     * @param id identificador interno del libro
     * @return el {@link Libro} con ese ID
     * @throws LibroNoEncontradoException si no existe ningún libro con ese ID
     */
    public Libro encontrarLibro(int id) throws LibroNoEncontradoException {
        for (Seccion s : secciones.values()) {
            for (ObservableList<Libro> lista : s.getLibros().values()) {
                for (Libro l : lista) {
                    if (l.getIdInterno() == id) return l;
                }
            }
        }
        throw new LibroNoEncontradoException("Libro no encontrado con ID: " + id);
    }

    // ---------------------------------------------------------------
    // Operaciones de préstamo y devolución
    // ---------------------------------------------------------------

    /**
     * Registra el préstamo de un libro a un socio de manera indefinida.
     * <p>
     * Solo funciona con instancias de {@link LibroPrestable} disponibles.
     * Al prestarse, el libro se marca como no disponible y se registra la
     * fecha actual como fecha de préstamo.
     * </p>
     *
     * @param socio socio que recibe el préstamo
     * @param libro libro a prestar
     * @return {@code true} si el préstamo se realizó con éxito;
     *         {@code false} si el libro no es prestable o no está disponible
     */
    public boolean prestarLibro(Socio socio, Libro libro) {
        if (!(libro instanceof LibroPrestable)) return false;
        LibroPrestable lp = (LibroPrestable) libro;
        if (!lp.getDisponibilidad()) return false;
        lp.setDisponibilidad(false);
        lp.setFechaPrestamo(LocalDate.now());
        socio.agregarLibroPrestado(libro);
        return true;
    }

    /**
     *Registra el préstamo de un libro estableciendo un plazo
     * de días para su devolución.
     * <p>
     * Utiliza la lógica original de préstamo y, si tiene éxito, calcula y 
     * asigna automáticamente la fecha límite en la que el socio debe devolverlo.
     * </p>
     *
     * @param socio     Socio que recibe el préstamo.
     * @param libro     Libro a prestar.
     * @param diasPlazo Cantidad de días que el socio tiene para devolver el libro.
     * @return true si el préstamo se realizó con éxito; false en caso contrario.
     */
    public boolean prestarLibro(Socio socio, Libro libro, int diasPlazo) {
        // 1. Reutilizamos toda la lógica de validación del método original
        boolean exito = prestarLibro(socio, libro);
        
        // 2. Si el préstamo fue exitoso, le agregamos la fecha límite
        if (exito) {
            LibroPrestable lp = (LibroPrestable) libro;
            // Calculamos la fecha actual + los días de plazo
            lp.setFechaDevolucion(LocalDate.now().plusDays(diasPlazo));
        }
        
        return exito;
    }
    
    /**
     * Registra la devolución de un libro prestable por parte de su socio.
     * <p>
     * Calcula la multa total si la devolución es tardía, restablece el estado
     * del libro a disponible y lo retira de la lista de préstamos del socio.
     * </p>
     *
     * @param socio socio que devuelve el libro; si es {@code null} solo se
     *              restablece el estado del libro
     * @param lp    libro prestable a devolver
     * @return monto total de multa acumulada ({@code 0} si no hay retraso)
     */
    public int devolverLibro(Socio socio, LibroPrestable lp) {
        int totalMulta = 0;
        if (lp.getFechaDevolucion() != null && lp.getFechaDevolucion().isBefore(LocalDate.now())) {
            long dias = ChronoUnit.DAYS.between(lp.getFechaDevolucion(), LocalDate.now());
            if (dias > 0) totalMulta = (int) dias * lp.getMulta();
        }
        lp.setDisponibilidad(true);
        lp.setFechaPrestamo(null);
        lp.setFechaDevolucion(null);
        lp.setRetraso(0);
        if (socio != null) socio.quitarLibroPrestado(lp);
        return totalMulta;
    }
}