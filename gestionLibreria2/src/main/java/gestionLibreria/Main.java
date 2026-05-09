package gestionLibreria;

import gestionLibreria.inventario.Inventario;
import gestionLibreria.utilidades.GestorPersistencia;
import gestionLibreria.utilidades.Consola;
import javafx.application.Application;
import javafx.stage.Stage;
import java.util.HashMap;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        GestorPersistencia gestor = null;
        Inventario inventario = null;

        System.out.println("Cargando datos...");
        try {
            // Se define la ruta directamente a resources
            gestor = new GestorPersistencia("src/main/resources/");
            // cargarTodo() ahora devuelve el objeto Inventario
            inventario = gestor.cargarTodo();
            System.out.println("Datos cargados!!!");
        } catch (Exception e) {
            System.out.println("Error al cargar datos: " + e.getMessage());
            // Inventario de respaldo en caso de error
            inventario = new Inventario(new HashMap<>(), new HashMap<>());
        }

        Consola.enterParaContinuar();
        String modo = Consola.leerString("¿Desea usar la 'terminal' o la 'ventana'?: ");

        if (modo.equalsIgnoreCase("terminal")) {
            Terminal.modoTerminal(inventario, gestor);
            System.exit(0);
        } else {
            // El constructor de Ventana requiere (Stage, Inventario, GestorPersistencia)
            Ventana ventana = new Ventana(primaryStage, inventario, gestor);
            // El metodo correcto es iniciar()
            ventana.iniciar();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}