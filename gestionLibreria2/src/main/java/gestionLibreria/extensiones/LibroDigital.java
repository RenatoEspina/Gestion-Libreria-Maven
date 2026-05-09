package gestionLibreria.extensiones;

import java.time.LocalDate;
import java.util.ArrayList;

import gestionLibreria.inventario.Libro;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Extiende {@link Libro} para representar un libro en formato digital.
 * <p>
 * Agrega los atributos específicos de un libro electrónico: el tamaño en
 * megabytes que ocupa el archivo y el formato del mismo (p. ej. PDF, EPUB, MOBI).
 * </p>
 *
 * @see Libro
 */
public class LibroDigital extends Libro {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    /** Tamaño del archivo digital en megabytes como propiedad JavaFX. */
    private final SimpleIntegerProperty memoria;

    /** Formato del archivo digital (p. ej. "PDF", "EPUB") como propiedad JavaFX. */
    private final SimpleStringProperty  formato;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    /**
     * Construye un nuevo {@code LibroDigital} con todos sus atributos.
     *
     * @param fechaDePublicacion fecha de publicación del libro
     * @param titulo             título del libro
     * @param edicion            edición del libro
     * @param categoria          categoría del libro
     * @param paginas            número de páginas
     * @param idInterno          identificador interno en el inventario
     * @param precio             precio del libro
     * @param autores            lista de autores
     * @param memoria            tamaño del archivo en megabytes
     * @param formato            formato del archivo (p. ej. "PDF", "EPUB")
     */
    public LibroDigital(LocalDate fechaDePublicacion, String titulo, String edicion,
                        String categoria, int paginas, int idInterno, int precio,
                        ArrayList<String> autores, int memoria, String formato) {
        super(fechaDePublicacion, titulo, edicion, categoria, paginas, idInterno, precio, autores);
        this.memoria = new SimpleIntegerProperty(memoria);
        this.formato = new SimpleStringProperty(formato);
    }

    // ---------------------------------------------------------------
    // Memoria (tamaño en MB)
    // ---------------------------------------------------------------

    public int getMemoria() { return memoria.get(); }
    public void setMemoria(int memoria) { this.memoria.set(memoria); }
    public SimpleIntegerProperty memoriaProperty() { return memoria; }

    // ---------------------------------------------------------------
    // Formato
    // ---------------------------------------------------------------

    public String getFormato() { return formato.get(); }
    public void setFormato(String formato) { this.formato.set(formato); }
    public SimpleStringProperty formatoProperty() { return formato; }
}