package gestionLibreria;

import java.io.IOException;

import gestionLibreria.inventario.Inventario;
import gestionLibreria.utilidades.Consola;
import gestionLibreria.utilidades.GestorPersistencia;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación de gestión de librería.
 * <p>
 * Al iniciar, carga los datos desde los archivos CSV mediante {@link GestorPersistencia}.
 * Luego pregunta al usuario si desea operar en modo terminal (consola) o en modo
 * ventana (interfaz gráfica JavaFX).
 * </p>
 *
 * <p>El modo terminal se gestiona completamente en {@link Terminal#modoTerminal}.
 * El modo ventana delega la construcción de la interfaz a la clase {@code Ventana}.</p>
 *
 * @see Terminal
 * @see GestorPersistencia
 * @see Inventario
 */
public class Main extends Application {

    // ---------------------------------------------------------------
    // Estado compartido entre main() y start()
    // ---------------------------------------------------------------

    /** Gestor de persistencia compartido con la interfaz gráfica. */
    private static GestorPersistencia gestor;

    /** Inventario cargado al inicio, compartido con la interfaz gráfica. */
    private static Inventario inventario;

    // ---------------------------------------------------------------
    // Punto de entrada
    // ---------------------------------------------------------------

    /**
     * Método principal de la aplicación.
     * <p>
     * Carga el inventario desde disco, solicita al usuario el modo de operación
     * ({@code terminal} o {@code ventana}) y lanza el modo correspondiente.
     * Si la carga falla, inicia con un inventario vacío.
     * </p>
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        try {
            System.out.println("Cargando datos...");
            gestor    = new GestorPersistencia("data/");
            inventario = gestor.cargarTodo();
        } catch (IOException e) {
            Consola.limpiarPantalla();
            System.out.println("Error: No se pudo cargar el inventario. Iniciando vacío.");
            inventario = new Inventario();
        }

        System.out.println("Datos cargados!!!");
        Consola.enterParaContinuar();
        Consola.limpiarPantalla();

        String decision = Consola.leerString("¿Desea usar la 'terminal' o la 'ventana'?: ");
        Consola.limpiarPantalla();

        if (decision.equalsIgnoreCase("ventana")) {
            launch(args);
        } else {
            Terminal.modoTerminal(inventario, gestor);
        }
    }

    // ---------------------------------------------------------------
    // JavaFX Application
    // ---------------------------------------------------------------

    /**
     * Método requerido por {@link Application} para iniciar la interfaz gráfica JavaFX.
     * <p>
     * Construye y muestra la ventana principal de la aplicación delegando en la
     * clase {@code Ventana}.
     * </p>
     *
     * @param primaryStage escenario principal proporcionado por JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        new Ventana(primaryStage, inventario, gestor).iniciar();
    }
}