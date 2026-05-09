package gestionLibreria.extensiones;

import java.time.LocalDate;
import java.util.ArrayList;

import gestionLibreria.inventario.Libro;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Extiende {@link Libro} para representar un ejemplar que puede ser prestado a un socio.
 * <p>
 * Agrega los atributos necesarios para gestionar el ciclo de vida de un préstamo:
 * disponibilidad, fechas de préstamo y devolución, días de retraso y monto de multa
 * por día de atraso.
 * </p>
 *
 * @see Libro
 * @see gestionLibreria.inventario.Inventario#prestarLibro(gestionLibreria.inventario.Socio, Libro)
 * @see gestionLibreria.inventario.Inventario#devolverLibro(gestionLibreria.inventario.Socio, LibroPrestable)
 */
public class LibroPrestable extends Libro {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    /**
     * Indica si el ejemplar está disponible para ser prestado.
     * {@code true} = disponible; {@code false} = actualmente prestado.
     */
    private boolean disponibilidad;

    /** Días de retraso acumulados en la devolución actual como propiedad JavaFX. */
    private final SimpleIntegerProperty retraso;

    /** Monto de multa por día de retraso como propiedad JavaFX. */
    private final SimpleIntegerProperty multa;

    /** Fecha en que se realizó el préstamo actual; {@code null} si no está prestado. */
    private LocalDate fechaPrestamo;

    /**
     * Fecha comprometida de devolución del préstamo actual;
     * {@code null} si no está prestado o no se acordó una fecha.
     */
    private LocalDate fechaDevolucion;

    // ---------------------------------------------------------------
    // Constructores
    // ---------------------------------------------------------------

    /**
     * Construye un {@code LibroPrestable} con todos sus atributos explícitos.
     * Usado principalmente al cargar datos desde la capa de persistencia.
     */
    public LibroPrestable(LocalDate fechaDePublicacion, String titulo, String edicion,
                          String categoria, int paginas, int idInterno, int precio,
                          ArrayList<String> autores, boolean disponibilidad,
                          int retraso, int multa,
                          LocalDate fechaPrestamo, LocalDate fechaDevolucion) {
        super(fechaDePublicacion, titulo, edicion, categoria, paginas, idInterno, precio, autores);
        this.disponibilidad  = disponibilidad;
        this.retraso         = new SimpleIntegerProperty(retraso);
        this.multa           = new SimpleIntegerProperty(multa);
        this.fechaPrestamo   = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
    }

    /**
     * Construye un {@code LibroPrestable} nuevo listo para ser dado de alta.
     * El libro se inicializa como disponible, sin retraso y sin fechas de préstamo.
     */
    public LibroPrestable(LocalDate fechaDePublicacion, String titulo, String edicion,
                          String categoria, int paginas, int idInterno, int precio,
                          ArrayList<String> autores, int multa) {
        super(fechaDePublicacion, titulo, edicion, categoria, paginas, idInterno, precio, autores);
        this.disponibilidad  = true;
        this.retraso         = new SimpleIntegerProperty(0);
        this.multa           = new SimpleIntegerProperty(multa);
        this.fechaPrestamo   = null;
        this.fechaDevolucion = null;
    }

    // ---------------------------------------------------------------
    // Disponibilidad
    // ---------------------------------------------------------------

    public boolean getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(boolean disponibilidad) { this.disponibilidad = disponibilidad; }

    // ---------------------------------------------------------------
    // Retraso
    // ---------------------------------------------------------------

    public int getRetraso() { return retraso.get(); }
    public void setRetraso(int retraso) { this.retraso.set(retraso); }
    public SimpleIntegerProperty retrasoProperty() { return retraso; }

    // ---------------------------------------------------------------
    // Multa
    // ---------------------------------------------------------------

    public int getMulta() { return multa.get(); }
    public void setMulta(int multa) { this.multa.set(multa); }
    public SimpleIntegerProperty multaProperty() { return multa; }

    // ---------------------------------------------------------------
    // Fechas de préstamo y devolución
    // ---------------------------------------------------------------

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
}