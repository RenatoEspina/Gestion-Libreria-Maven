package gestionLibreria.inventario;

import java.time.LocalDate;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Representa un libro físico en el inventario de la librería.
 * <p>
 * Encapsula todos los atributos bibliográficos básicos de un libro y utiliza
 * propiedades JavaFX ({@link SimpleStringProperty}, {@link SimpleIntegerProperty})
 * para permitir el enlace de datos con la interfaz gráfica.
 * </p>
 *
 * <p>Esta clase sirve como base para las subclases {@code LibroPrestable} y
 * {@code LibroDigital}, que extienden su comportamiento.</p>
 *
 * @see gestionLibreria.extensiones.LibroPrestable
 * @see gestionLibreria.extensiones.LibroDigital
 */
public class Libro {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    /** Fecha en que fue publicado el libro. */
    private LocalDate fechaDePublicacion;

    /** Título del libro como propiedad JavaFX. */
    private final SimpleStringProperty titulo;

    /** Edición del libro (p. ej. "2da edición") como propiedad JavaFX. */
    private final SimpleStringProperty edicion;

    /** Categoría o género del libro como propiedad JavaFX. */
    private final SimpleStringProperty categoria;

    /** Número de páginas del libro como propiedad JavaFX. */
    private final SimpleIntegerProperty paginas;

    /** Identificador interno único del libro como propiedad JavaFX. */
    private final SimpleIntegerProperty idInterno;

    /** Precio del libro en la moneda local como propiedad JavaFX. */
    private final SimpleIntegerProperty precio;

    /** Lista observable de nombres de los autores del libro. */
    private final ObservableList<String> autores;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    /**
     * Construye un nuevo {@code Libro} con todos sus atributos bibliográficos.
     *
     * @param fechaDePublicacion fecha en que se publicó el libro
     * @param titulo             título del libro
     * @param edicion            edición del libro (p. ej. "1ra", "2da")
     * @param categoria          categoría o género (p. ej. "Ficción", "Ciencia")
     * @param paginas            número de páginas
     * @param idInterno          identificador interno único en el inventario
     * @param precio             precio de venta
     * @param autores            lista de nombres de los autores
     */
    public Libro(LocalDate fechaDePublicacion, String titulo, String edicion,
                 String categoria, int paginas, int idInterno, int precio,
                 List<String> autores) {
        this.fechaDePublicacion = fechaDePublicacion;
        this.titulo    = new SimpleStringProperty(titulo);
        this.edicion   = new SimpleStringProperty(edicion);
        this.categoria = new SimpleStringProperty(categoria);
        this.paginas   = new SimpleIntegerProperty(paginas);
        this.idInterno = new SimpleIntegerProperty(idInterno);
        this.precio    = new SimpleIntegerProperty(precio);
        this.autores   = FXCollections.observableArrayList();
        this.autores.addAll(autores);
    }

    // ---------------------------------------------------------------
    // Fecha de publicación
    // ---------------------------------------------------------------

    public LocalDate getFechaDePublicacion() { return fechaDePublicacion; }
    public void setFechaDePublicacion(LocalDate fechaDePublicacion) { this.fechaDePublicacion = fechaDePublicacion; }

    // ---------------------------------------------------------------
    // Título
    // ---------------------------------------------------------------

    public String getTitulo() { return titulo.get(); }
    public void setTitulo(String titulo) { this.titulo.set(titulo); }
    public SimpleStringProperty tituloProperty() { return titulo; }

    // ---------------------------------------------------------------
    // Edición
    // ---------------------------------------------------------------

    public String getEdicion() { return edicion.get(); }
    public void setEdicion(String edicion) { this.edicion.set(edicion); }
    public SimpleStringProperty edicionProperty() { return edicion; }

    // ---------------------------------------------------------------
    // Categoría
    // ---------------------------------------------------------------

    public String getCategoria() { return categoria.get(); }
    public void setCategoria(String categoria) { this.categoria.set(categoria); }
    public SimpleStringProperty categoriaProperty() { return categoria; }

    // ---------------------------------------------------------------
    // Páginas
    // ---------------------------------------------------------------

    public int getPaginas() { return paginas.get(); }
    public void setPaginas(int paginas) { this.paginas.set(paginas); }
    public SimpleIntegerProperty paginasProperty() { return paginas; }

    // ---------------------------------------------------------------
    // ID Interno
    // ---------------------------------------------------------------

    public int getIdInterno() { return idInterno.get(); }
    public void setIdInterno(int idInterno) { this.idInterno.set(idInterno); }
    public SimpleIntegerProperty idInternoProperty() { return idInterno; }

    // ---------------------------------------------------------------
    // Precio
    // ---------------------------------------------------------------

    public int getPrecio() { return precio.get(); }
    public void setprecio(int precio) { this.precio.set(precio); }
    public SimpleIntegerProperty precioProperty() { return precio; }

    // ---------------------------------------------------------------
    // Autores
    // ---------------------------------------------------------------

    public ObservableList<String> getAutores() { return autores; }
    public void setAutores(List<String> autores) { this.autores.setAll(autores); }
}