package gestionLibreria.excepciones;

/**
 * Excepción lanzada cuando una operación de búsqueda de un libro falla 
 * debido a que el ejemplar no existe en el inventario o en la sección.
 * <p>
 * Se utiliza para evitar el manejo de valores {@code null} y permitir que 
 * la interfaz de usuario capture el error y muestre un mensaje descriptivo.
 * </p>
 * * @author TuNombre
 * @see gestionLibreria.inventario.Inventario
 * @see gestionLibreria.inventario.Seccion
 */
public class LibroNoEncontradoException extends Exception {

    /**
     * Identificador único para la serialización de la clase.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construye una nueva excepción con el mensaje de error especificado.
     *
     * @param mensaje detalle del motivo por el cual no se encontró el libro
     */
    public LibroNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}