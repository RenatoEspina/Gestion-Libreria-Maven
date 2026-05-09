package gestionLibreria.inventario;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Representa una sección temática del inventario de la librería.
 * <p>
 * Cada sección agrupa libros bajo un nombre (p. ej. "Ficción", "Ciencia").
 * Internamente, los libros se organizan en un {@link ObservableMap} donde la
 * clave es el título del libro y el valor es una lista de ejemplares con ese
 * título, lo que permite manejar múltiples copias de una misma obra.
 * </p>
 *
 * @see Libro
 * @see Inventario
 */
public class Seccion {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    private final SimpleStringProperty nombre;

    /**
     * Mapa de libros organizado por título.
     * Clave: título del libro. Valor: lista observable de ejemplares con ese título.
     */
    private final ObservableMap<String, ObservableList<Libro>> libros;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    /**
     * Construye una nueva sección con el nombre dado y sin libros.
     *
     * @param nombre nombre de la sección (p. ej. "Ficción")
     */
    public Seccion(String nombre) {
        this.nombre = new SimpleStringProperty(nombre);
        this.libros = FXCollections.observableHashMap();
    }

    // ---------------------------------------------------------------
    // Nombre
    // ---------------------------------------------------------------

    public String getNombre() { return nombre.get(); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public SimpleStringProperty nombreProperty() { return nombre; }

    // ---------------------------------------------------------------
    // Acceso al mapa de libros
    // ---------------------------------------------------------------

    /**
     * Retorna el mapa completo de libros de la sección.
     * Clave: título; valor: lista de ejemplares con ese título.
     */
    public ObservableMap<String, ObservableList<Libro>> getLibros() { return libros; }

    /** Retorna una lista observable con todos los títulos del mapa. */
    public ObservableList<String> GetLlaves() {
        return FXCollections.observableArrayList(libros.keySet());
    }

    // ---------------------------------------------------------------
    // Gestión de ejemplares
    // ---------------------------------------------------------------

    /**
     * Agrega un libro a la sección.
     * Si ya existe una lista para ese título, añade el ejemplar a ella;
     * si no, crea una nueva lista.
     *
     * @param libro libro a agregar; se ignora si es {@code null}
     */
    public void agregarLibro(Libro libro) {
        if (libro != null) {
            libros.computeIfAbsent(libro.getTitulo(), k -> FXCollections.observableArrayList())
                  .add(libro);
        }
    }

    /**
     * Elimina un ejemplar específico de la sección.
     * Si la lista del título queda vacía, se remueve la entrada del mapa.
     *
     * @param libro libro a eliminar
     * @return {@code true} si el libro existía y fue eliminado
     */
    public boolean eliminarLibro(Libro libro) {
        if (libro == null) return false;
        ObservableList<Libro> lista = libros.get(libro.getTitulo());
        if (lista == null) return false;
        boolean removido = lista.remove(libro);
        if (lista.isEmpty()) libros.remove(libro.getTitulo());
        return removido;
    }

    /** Elimina todos los libros de la sección. */
    public void vaciarSeccion() { libros.clear(); }

    /**
     * Busca y retorna todos los ejemplares de un título dado.
     *
     * @param titulo título exacto a buscar
     * @return lista de ejemplares, o {@code null} si no existe ninguno
     */
    public ObservableList<Libro> encontrarLibrosPorTitulo(String titulo) {
        return libros.get(titulo);
    }

    // ---------------------------------------------------------------
    // Venta de libros
    // ---------------------------------------------------------------

    /**
     * Vende (elimina) el ejemplar que coincida con el título y el ID dados.
     *
     * @param nombreLibro título del libro a vender
     * @param id          ID interno del ejemplar a eliminar
     * @return {@code true} si el ejemplar fue encontrado y eliminado
     */
    public boolean venderLibro(String nombreLibro, int id) {
        ObservableList<Libro> lista = libros.get(nombreLibro);
        if (lista == null || lista.isEmpty()) return false;
        boolean removido = lista.removeIf(l -> l.getIdInterno() == id);
        if (removido && lista.isEmpty()) libros.remove(nombreLibro);
        return removido;
    }
}