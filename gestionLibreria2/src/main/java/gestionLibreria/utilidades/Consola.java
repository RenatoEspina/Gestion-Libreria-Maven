package gestionLibreria.utilidades;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Clase utilitaria para manejar la entrada y salida por consola de manera segura y robusta.
 * <p>
 * Proporciona métodos estáticos para leer diferentes tipos de datos desde la consola
 * con validación integrada y manejo de errores. También incluye utilidades para
 * el control de la interfaz de usuario, como limpieza de pantalla y pausas.
 * </p>
 *
 * <p>Todos los métodos de lectura validan los datos ingresados y repiten la solicitud
 * en caso de error, garantizando que el valor retornado sea siempre válido.</p>
 *
 * @see Scanner
 * @see LocalDate
 */
public class Consola {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    /** Scanner compartido para toda la vida de la aplicación. */
    private static Scanner sc = new Scanner(System.in);

    // ---------------------------------------------------------------
    // Control de pantalla
    // ---------------------------------------------------------------

    /**
     * Limpia la pantalla de la consola usando secuencias de escape ANSI.
     * <p>
     * Posiciona el cursor en la esquina superior izquierda tras borrar el contenido.
     * </p>
     */
    public static void limpiarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Pausa la ejecución hasta que el usuario presione {@code ENTER}.
     * <p>
     * Útil para permitir al usuario leer mensajes antes de continuar con la siguiente
     * operación.
     * </p>
     */
    public static void enterParaContinuar() {
        System.out.println("\nPresione ENTER para continuar...");
        sc.nextLine();
    }

    // ---------------------------------------------------------------
    // Lectura de enteros
    // ---------------------------------------------------------------

    /**
     * Lee un número entero desde la consola con validación.
     * <p>
     * Si el usuario ingresa un valor no válido, muestra un mensaje de error
     * y solicita nuevamente el dato.
     * </p>
     *
     * @param mensaje el mensaje a mostrar antes de solicitar el dato (puede ser {@code null})
     * @return el número entero válido ingresado por el usuario
     */
    public static int leerEntero(String mensaje) {
        while (true) {
            try {
                if (mensaje != null) System.out.print(mensaje);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes ingresar un número entero válido.");
            }
        }
    }

    /**
     * Lee un número entero desde la consola, con opción de limpiar la pantalla previamente.
     *
     * @param mensaje              el mensaje a mostrar antes de solicitar el dato (puede ser {@code null})
     * @param limpiarPantallaAntes si {@code true}, limpia la pantalla antes de mostrar el mensaje
     * @return el número entero válido ingresado por el usuario
     */
    public static int leerEntero(String mensaje, boolean limpiarPantallaAntes) {
        while (true) {
            if (limpiarPantallaAntes) limpiarPantalla();
            try {
                if (mensaje != null) System.out.print(mensaje);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes ingresar un número entero válido.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Lectura de texto
    // ---------------------------------------------------------------

    /**
     * Lee una cadena de texto no vacía desde la consola.
     * <p>
     * Si el usuario ingresa una cadena vacía, muestra un mensaje de error
     * y solicita nuevamente el dato.
     * </p>
     *
     * @param mensaje el mensaje a mostrar antes de solicitar el dato (puede ser {@code null})
     * @return la cadena de texto no vacía ingresada por el usuario
     */
    public static String leerString(String mensaje) {
        while (true) {
            if (mensaje != null) System.out.print(mensaje);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Error: No puedes dejar el campo vacío.");
        }
    }

    // ---------------------------------------------------------------
    // Lectura de fechas
    // ---------------------------------------------------------------

    /**
     * Lee una fecha desde la consola con validación de formato ISO ({@code YYYY-MM-DD}).
     * <p>
     * Si el formato es incorrecto o la fecha no es válida, muestra un mensaje
     * de error y solicita nuevamente el dato.
     * </p>
     *
     * @param mensaje el mensaje a mostrar antes de solicitar el dato (puede ser {@code null})
     * @return la fecha válida ingresada por el usuario
     * @see LocalDate#parse(CharSequence)
     */
    public static LocalDate leerFecha(String mensaje) {
        while (true) {
            if (mensaje != null) System.out.print(mensaje);
            String input = sc.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Debes ingresar una fecha válida en formato YYYY-MM-DD.");
            }
        }
    }
}