package gestionLibreria.inventario;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Representa a un socio registrado en la librería.
 * <p>
 * Un socio puede tomar libros en préstamo. La clase mantiene sus datos
 * personales de contacto y un registro de los libros que tiene actualmente
 * en su poder. Los atributos de texto utilizan propiedades JavaFX para
 * facilitar el enlace de datos con la interfaz gráfica.
 * </p>
 *
 * <p>El RUT se usa como identificador único del socio en el sistema.</p>
 *
 * @see Inventario
 * @see gestionLibreria.extensiones.LibroPrestable
 */
public class Socio {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    private final SimpleStringProperty nombre;
    private final SimpleStringProperty rut;
    private final SimpleStringProperty numeroContacto;
    private final ObservableList<Libro> librosPrestados;

    // ---------------------------------------------------------------
    // Constructores
    // ---------------------------------------------------------------

    /**
     * Construye un {@code Socio} con todos sus atributos, incluyendo libros ya prestados.
     * Usado al cargar socios desde la capa de persistencia.
     */
    public Socio(String nombre, String rut, String numeroContacto, List<Libro> librosPrestados) {
        this.nombre          = new SimpleStringProperty(nombre);
        this.rut             = new SimpleStringProperty(rut);
        this.numeroContacto  = new SimpleStringProperty(numeroContacto);
        this.librosPrestados = FXCollections.observableArrayList();
        this.librosPrestados.addAll(librosPrestados);
    }

    /**
     * Construye un {@code Socio} nuevo sin libros prestados.
     * Usado al registrar un nuevo socio en el sistema.
     */
    public Socio(String nombre, String rut, String numeroContacto) {
        this.nombre          = new SimpleStringProperty(nombre);
        this.rut             = new SimpleStringProperty(rut);
        this.numeroContacto  = new SimpleStringProperty(numeroContacto);
        this.librosPrestados = FXCollections.observableArrayList();
    }

    // ---------------------------------------------------------------
    // Nombre
    // ---------------------------------------------------------------

    public String getNombre() { return nombre.get(); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public SimpleStringProperty nombreProperty() { return nombre; }

    // ---------------------------------------------------------------
    // RUT
    // ---------------------------------------------------------------

    public String getRut() { return rut.get(); }
    public void setRut(String rut) { this.rut.set(rut); }
    public SimpleStringProperty rutProperty() { return rut; }

    // ---------------------------------------------------------------
    // Número de contacto
    // ---------------------------------------------------------------

    public String getNumeroContacto() { return numeroContacto.get(); }
    public void setNumeroContacto(String numeroContacto) { this.numeroContacto.set(numeroContacto); }
    public SimpleStringProperty numeroContactoProperty() { return numeroContacto; }

    // ---------------------------------------------------------------
    // Libros prestados
    // ---------------------------------------------------------------

    public ObservableList<Libro> getLibrosPrestados() { return librosPrestados; }

    /**
     * Agrega un libro a la lista de préstamos del socio.
     *
     * @param libro libro a registrar como prestado; se ignora si es {@code null}
     */
    public void agregarLibroPrestado(Libro libro) {
        if (libro != null) librosPrestados.add(libro);
    }

    /**
     * Quita un libro de la lista de préstamos del socio (al ser devuelto).
     *
     * @param libro libro a remover
     * @return {@code true} si el libro estaba en la lista y fue removido
     */
    public boolean quitarLibroPrestado(Libro libro) {
        return librosPrestados.remove(libro);
    }
}