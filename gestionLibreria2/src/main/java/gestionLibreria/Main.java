package gestionLibreria;

import gestionLibreria.inventario.Inventario;
import gestionLibreria.utilidades.GestorPersistencia;
import gestionLibreria.utilidades.Consola;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Inventario inventario = new Inventario();
        GestorPersistencia gestor = new GestorPersistencia();

        System.out.println("Cargando datos...");
        try {
            gestor.cargarTodo(inventario);
            System.out.println("Datos cargados!!!");
        } catch (Exception e) {
            System.out.println("Error al cargar datos.");
        }

        Consola.enterParaContinuar();
        String modo = Consola.leerString("¿Desea usar la 'terminal' o la 'ventana'?: ");

        if (modo.equalsIgnoreCase("terminal")) {
            Terminal.modoTerminal(inventario, gestor);
            System.exit(0);
        } else {
            Ventana ventana = new Ventana();
            ventana.start(primaryStage);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}