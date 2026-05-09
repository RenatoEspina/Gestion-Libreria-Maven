package gestionLibreria.excepciones;

/**
 * Excepción lanzada cuando se intenta realizar una operación sobre un socio 
 * (como un préstamo o una eliminación) y su RUT no está registrado en el sistema.
 * <p>
 * Proporciona una forma segura de manejar errores de búsqueda de usuarios 
 * sin interrumpir el flujo normal de la aplicación.
 * </p>
 * * @author TuNombre
 * @see gestionLibreria.inventario.Socio
 * @see gestionLibreria.inventario.Inventario
 */
public class SocioNoEncontradoException extends Exception {

    /**
     * Identificador único para la serialización de la clase.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construye una nueva excepción con el mensaje de error especificado.
     *
     * @param mensaje detalle del motivo por el cual no se encontró el socio
     */
    public SocioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}