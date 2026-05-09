package gestionLibreria;

import gestionLibreria.excepciones.*;
import gestionLibreria.extensiones.*;
import gestionLibreria.inventario.*;
import gestionLibreria.utilidades.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * Ventana principal de la aplicación de gestión de librería en modo gráfico.
 * <p>
 * Se limita a construir y gestionar la UI (JavaFX). Toda la lógica de negocio
 * se delega en {@link Inventario}; esta clase solo lee el estado del modelo
 * para mostrarlo y llama a los métodos del modelo para modificarlo.
 * </p>
 */
public class Ventana {

    private final Stage primaryStage;
    private final Inventario inventario;
    private final GestorPersistencia gestor;

    // ---------------------------------------------------------------
    // Constructor e inicio
    // ---------------------------------------------------------------

    public Ventana(Stage primaryStage, Inventario inventario, GestorPersistencia gestor) {
        this.primaryStage = primaryStage;
        this.inventario   = inventario;
        this.gestor       = gestor;
    }

    public void iniciar() {
        primaryStage.setTitle("Gestión de Librería");
        primaryStage.setScene(new Scene(crearMenuPrincipal(), 460, 460));
        primaryStage.setOnCloseRequest(event -> { event.consume(); salirYGuardar(); });
        primaryStage.show();
    }

    // ---------------------------------------------------------------
    // Menú principal
    // ---------------------------------------------------------------

    private VBox crearMenuPrincipal() {
        Label titulo = new Label("Sistema de Gestión de Librería");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222;");

        String estiloAzul = "-fx-font-size: 14px; -fx-padding: 10 22; -fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 5;";
        String estiloRojo = "-fx-font-size: 14px; -fx-padding: 10 22; -fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5;";

        Button btnSecciones = boton("📚 Gestionar Secciones", estiloAzul, e -> abrirVentanaSecciones());
        Button btnLibros    = boton("📖 Gestionar Libros",    estiloAzul, e -> abrirVentanaLibros());
        Button btnSocios    = boton("👤 Gestionar Socios",    estiloAzul, e -> abrirVentanaSocios());
        Button btnFiltros   = boton("🔍 Filtros y Reportes",  estiloAzul, e -> logicaFiltros());
        Button btnSalir     = boton("💾 Guardar y Salir",      estiloRojo, e -> salirYGuardar());

        for (Button b : new Button[]{btnSecciones, btnLibros, btnSocios, btnFiltros, btnSalir})
            b.setMaxWidth(300);

        VBox root = new VBox(18, titulo, btnSecciones, btnLibros, btnSocios, btnFiltros, btnSalir);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #f4f4f9;");
        return root;
    }

    private Button boton(String texto, String estilo, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button b = new Button(texto);
        b.setStyle(estilo);
        b.setOnAction(handler);
        return b;
    }

    private void salirYGuardar() {
        try {
            gestor.guardarTodo(inventario);
            alerta(Alert.AlertType.INFORMATION, "Guardado", "Inventario guardado con éxito.", "¡Hasta luego!");
        } catch (IOException e) {
            alerta(Alert.AlertType.ERROR, "Error al guardar", "No se pudo guardar el inventario.", e.getMessage());
        }
        primaryStage.close();
    }

    // ---------------------------------------------------------------
    // Gestionar Secciones
    // ---------------------------------------------------------------

    private void abrirVentanaSecciones() {
        Stage ventana = ventanaModal("Gestionar Secciones");

        Label titulo = new Label("Secciones de la Librería");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        ListView<Seccion> lista = new ListView<>(inventario.getSeccionesAsObservableList());
        lista.setCellFactory(lv -> new ListCell<Seccion>() {
            @Override protected void updateItem(Seccion s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "" : s.getNombre() + "  (" + s.GetLlaves().size() + " título(s))");
            }
        });

        Button btnAgregar  = new Button("Agregar");
        Button btnEliminar = new Button("Eliminar");
        HBox botones = new HBox(10, btnAgregar, btnEliminar);
        botones.setAlignment(Pos.CENTER);

        btnAgregar.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog();
            d.setTitle("Nueva Sección"); d.setHeaderText("Nombre:"); d.setContentText("Nombre:");
            d.showAndWait().ifPresent(nombre -> {
                nombre = nombre.trim();
                if (nombre.isEmpty()) { alerta(Alert.AlertType.WARNING, "Inválido", "Nombre vacío.", null); return; }
                if (inventario.getSecciones().containsKey(nombre)) { alerta(Alert.AlertType.ERROR, "Error", "Ya existe esa sección.", null); return; }
                inventario.setSeccion(nombre, new Seccion(nombre));
                lista.setItems(inventario.getSeccionesAsObservableList());
            });
        });

        btnEliminar.setOnAction(e -> {
            Seccion sel = lista.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione una sección.", null); return; }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar '" + sel.getNombre() + "'? Se perderán todos sus libros.", ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    inventario.getSecciones().remove(sel.getNombre());
                    lista.setItems(inventario.getSeccionesAsObservableList());
                }
            });
        });

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));
        layout.setTop(titulo); BorderPane.setAlignment(titulo, Pos.CENTER); BorderPane.setMargin(titulo, new Insets(0,0,12,0));
        layout.setCenter(lista);
        layout.setBottom(botones); BorderPane.setMargin(botones, new Insets(12,0,0,0));

        ventana.setScene(new Scene(layout, 420, 480));
        ventana.showAndWait();
    }

    // ---------------------------------------------------------------
    // Gestionar Libros
    // ---------------------------------------------------------------

    private void abrirVentanaLibros() {
        Stage ventana = ventanaModal("Gestionar Libros");

        Label lblSec = new Label("Sección:");
        ObservableList<Seccion> seccionesItems = FXCollections.observableArrayList();
        seccionesItems.add(null);
        seccionesItems.addAll(inventario.getSeccionesAsObservableList());

        ComboBox<Seccion> comboSec = new ComboBox<>(seccionesItems);
        comboSec.setCellFactory(lv -> celdaSeccion());
        comboSec.setButtonCell(celdaSeccion());
        comboSec.getSelectionModel().selectFirst();

        TextField buscador = new TextField();
        buscador.setPromptText("Buscar por título...");
        HBox.setHgrow(buscador, Priority.ALWAYS);

        HBox topBar = new HBox(10, lblSec, comboSec, buscador);
        topBar.setAlignment(Pos.CENTER_LEFT);

        TableView<Libro> tabla = new TableView<>();
        configurarTablaLibros(tabla);

        ObservableList<Libro> listaMaestra = FXCollections.observableArrayList(getAllLibros());
        FilteredList<Libro> listaFiltrada  = new FilteredList<>(listaMaestra, p -> true);
        tabla.setItems(listaFiltrada);

        comboSec.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo == null) {
                listaMaestra.setAll(getAllLibros());
            } else {
                List<Libro> librosSeccion = new ArrayList<>();
                for (ObservableList<Libro> l : nuevo.getLibros().values()) librosSeccion.addAll(l);
                listaMaestra.setAll(librosSeccion);
            }
            filtrarTabla(listaFiltrada, buscador.getText());
        });

        buscador.textProperty().addListener((obs, old, nuevo) -> filtrarTabla(listaFiltrada, nuevo));

        Button btnAgregar  = new Button("Agregar Libro...");
        Button btnEliminar = new Button("Eliminar Libro");
        Button btnPrestar  = new Button("Prestar Libro...");
        Button btnDevolver = new Button("Devolver Libro...");
        Button btnVender   = new Button("Vender Libro");

        VBox panelBotones = new VBox(8, btnAgregar, btnEliminar, new Separator(), btnPrestar, btnDevolver, new Separator(), btnVender);
        panelBotones.setPadding(new Insets(0,0,0,12));
        for (Button b : new Button[]{btnAgregar, btnEliminar, btnPrestar, btnDevolver, btnVender})
            b.setMaxWidth(Double.MAX_VALUE);

        btnAgregar.setOnAction(e -> {
            Seccion sec = comboSec.getValue();
            if (sec == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione una sección específica para agregar.", null); return; }
            mostrarDialogoAgregarLibro(sec);
            List<Libro> librosActualizados = new ArrayList<>();
            for (ObservableList<Libro> l : sec.getLibros().values()) librosActualizados.addAll(l);
            listaMaestra.setAll(librosActualizados);
        });

        btnEliminar.setOnAction(e -> {
            Libro sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un libro.", null); return; }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar '" + sel.getTitulo() + "'?", ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    Seccion s = inventario.encontrarSeccionDeLibro(sel.getTitulo());
                    if (s != null) s.eliminarLibro(sel);
                    listaMaestra.remove(sel);
                }
            });
        });

        btnPrestar.setOnAction(e -> {
            Libro sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un libro.", null); return; }
            logicaPrestarLibro(sel, tabla);
        });

        btnDevolver.setOnAction(e -> {
            Libro sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un libro.", null); return; }
            logicaDevolverLibro(sel, tabla);
        });

        btnVender.setOnAction(e -> {
            Libro sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un libro.", null); return; }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Vender '" + sel.getTitulo() + "'?", ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    Seccion s = inventario.encontrarSeccionDeLibro(sel.getTitulo());
                    if (s != null && s.venderLibro(sel.getTitulo(), sel.getIdInterno())) {
                        listaMaestra.remove(sel);
                        alerta(Alert.AlertType.INFORMATION, "Éxito", "Libro vendido.", null);
                    }
                }
            });
        });

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));
        layout.setTop(topBar);    BorderPane.setMargin(topBar, new Insets(0,0,10,0));
        layout.setCenter(tabla);
        layout.setRight(panelBotones);

        ventana.setScene(new Scene(layout, 980, 620));
        ventana.showAndWait();
    }

    private ListCell<Seccion> celdaSeccion() {
        return new ListCell<Seccion>() {
            @Override protected void updateItem(Seccion s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "Todas las secciones" : s.getNombre());
            }
        };
    }

    private void filtrarTabla(FilteredList<Libro> lista, String texto) {
        lista.setPredicate(l -> texto == null || texto.isEmpty()
            || l.getTitulo().toLowerCase().contains(texto.toLowerCase()));
    }

    @SuppressWarnings("unchecked")
    private void configurarTablaLibros(TableView<Libro> tabla) {
        TableColumn<Libro, String>  colSec  = new TableColumn<>("Sección");
        TableColumn<Libro, Integer> colId   = new TableColumn<>("ID");
        TableColumn<Libro, String>  colTit  = new TableColumn<>("Título");
        TableColumn<Libro, String>  colCat  = new TableColumn<>("Categoría");
        TableColumn<Libro, Integer> colPag  = new TableColumn<>("Páginas");
        TableColumn<Libro, Integer> colPre  = new TableColumn<>("Precio");
        TableColumn<Libro, String>  colTipo = new TableColumn<>("Tipo");
        TableColumn<Libro, String>  colDisp = new TableColumn<>("Disponible");

        colSec.setCellValueFactory(d -> {
            Seccion s = inventario.encontrarSeccionDeLibro(d.getValue().getTitulo());
            return new javafx.beans.property.SimpleStringProperty(s != null ? s.getNombre() : "-");
        });
        colId.setCellValueFactory(new PropertyValueFactory<>("idInterno"));
        colTit.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPag.setCellValueFactory(new PropertyValueFactory<>("paginas"));
        colPre.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colTipo.setCellValueFactory(d -> {
            Libro l = d.getValue();
            String t = l instanceof LibroDigital ? "Digital" : l instanceof LibroPrestable ? "Prestable" : "Base";
            return new javafx.beans.property.SimpleStringProperty(t);
        });
        colDisp.setCellValueFactory(d -> {
            Libro l = d.getValue();
            if (l instanceof LibroPrestable)
                return new javafx.beans.property.SimpleStringProperty(((LibroPrestable) l).getDisponibilidad() ? "Sí" : "No");
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        colSec.setPrefWidth(100); colId.setPrefWidth(45);  colTit.setPrefWidth(190);
        colCat.setPrefWidth(100); colPag.setPrefWidth(65); colPre.setPrefWidth(65);
        colTipo.setPrefWidth(75); colDisp.setPrefWidth(80);

        tabla.getColumns().addAll(colSec, colId, colTit, colCat, colPag, colPre, colTipo, colDisp);
        tabla.setPlaceholder(new Label("No hay libros que mostrar."));
    }

    private void mostrarDialogoAgregarLibro(Seccion seccion) {
        Dialog<Libro> dialog = new Dialog<>();
        dialog.setTitle("Agregar Libro");
        dialog.setHeaderText("Sección: " + seccion.getNombre());

        ButtonType btnCrear = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCrear, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(15));

        TextField fTitulo    = field("Título");
        TextField fEdicion   = field("Edición (ej. 1ra)");
        TextField fCategoria = field("Categoría");
        TextField fPaginas   = field("Número entero");
        TextField fPrecio    = field("Número entero");
        TextField fAutores   = field("Autor1; Autor2; ...");
        TextField fFechaPub  = field("YYYY-MM-DD (vacío = hoy)");

        ComboBox<String> comboTipo = new ComboBox<>(FXCollections.observableArrayList("Base", "Digital", "Prestable"));
        comboTipo.setValue("Base");

        TextField fMemoria = field("MB"); TextField fFormato = field("PDF, EPUB...");
        TextField fMulta   = field("Entero");
        Label lblM1 = new Label(), lblM2 = new Label(), lblMulita = new Label();

        grid.add(new Label("Título:"),     0, 0); grid.add(fTitulo,    1, 0);
        grid.add(new Label("Edición:"),    0, 1); grid.add(fEdicion,   1, 1);
        grid.add(new Label("Categoría:"),  0, 2); grid.add(fCategoria, 1, 2);
        grid.add(new Label("Páginas:"),    0, 3); grid.add(fPaginas,   1, 3);
        grid.add(new Label("Precio:"),     0, 4); grid.add(fPrecio,    1, 4);
        grid.add(new Label("Autores:"),    0, 5); grid.add(fAutores,   1, 5);
        grid.add(new Label("Fecha pub.:"), 0, 6); grid.add(fFechaPub,  1, 6);
        grid.add(new Label("Tipo:"),       0, 7); grid.add(comboTipo,  1, 7);

        comboTipo.valueProperty().addListener((obs, old, nuevo) -> {
            grid.getChildren().removeAll(lblM1, fMemoria, lblM2, fFormato, lblMulita, fMulta);
            if ("Digital".equals(nuevo)) {
                lblM1.setText("Memoria (MB):"); lblM2.setText("Formato:");
                grid.add(lblM1, 0, 8); grid.add(fMemoria, 1, 8);
                grid.add(lblM2, 0, 9); grid.add(fFormato, 1, 9);
            } else if ("Prestable".equals(nuevo)) {
                lblMulita.setText("Multa:");
                grid.add(lblMulita, 0, 8); grid.add(fMulta, 1, 8);
            }
        });

        Node crearBtn = dialog.getDialogPane().lookupButton(btnCrear);
        Runnable validar = () -> crearBtn.setDisable(
            fTitulo.getText().trim().isEmpty() || fCategoria.getText().trim().isEmpty() ||
            fPaginas.getText().trim().isEmpty() || fPrecio.getText().trim().isEmpty());
        for (TextField f : new TextField[]{fTitulo, fCategoria, fPaginas, fPrecio})
            f.textProperty().addListener((o, a, b) -> validar.run());
        validar.run();

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn != btnCrear) return null;
            try {
                String titulo    = fTitulo.getText().trim();
                String edicion   = fEdicion.getText().trim().isEmpty() ? "1" : fEdicion.getText().trim();
                String categoria = fCategoria.getText().trim();
                int    paginas   = Integer.parseInt(fPaginas.getText().trim());
                int    precio    = Integer.parseInt(fPrecio.getText().trim());
                int    id        = inventario.getNumeroLibros() + 1;
                ArrayList<String> autores = new ArrayList<>();
                for (String a : fAutores.getText().split(";")) { String at = a.trim(); if (!at.isEmpty()) autores.add(at); }
                if (autores.isEmpty()) autores.add("Desconocido");
                LocalDate fechaPub = fFechaPub.getText().trim().isEmpty() ? LocalDate.now() : LocalDate.parse(fFechaPub.getText().trim());

                Libro nuevo;
                if ("Digital".equals(comboTipo.getValue())) {
                    int mem = Integer.parseInt(fMemoria.getText().trim());
                    String fmt = fFormato.getText().trim().isEmpty() ? "PDF" : fFormato.getText().trim();
                    nuevo = new LibroDigital(fechaPub, titulo, edicion, categoria, paginas, id, precio, autores, mem, fmt);
                } else if ("Prestable".equals(comboTipo.getValue())) {
                    int multa = fMulta.getText().trim().isEmpty() ? 0 : Integer.parseInt(fMulta.getText().trim());
                    nuevo = new LibroPrestable(fechaPub, titulo, edicion, categoria, paginas, id, precio, autores, multa);
                } else {
                    nuevo = new Libro(fechaPub, titulo, edicion, categoria, paginas, id, precio, autores);
                }
                seccion.agregarLibro(nuevo);
                inventario.incrementarNumeroLibros();
                return nuevo;
            } catch (Exception ex) {
                alerta(Alert.AlertType.ERROR, "Error", "Datos inválidos.", ex.getMessage());
                return null;
            }
        });
        dialog.showAndWait();
    }

    private void logicaPrestarLibro(Libro libro, TableView<Libro> tabla) {
        if (!(libro instanceof LibroPrestable)) {
            alerta(Alert.AlertType.WARNING, "No prestable", "Este libro no admite préstamos.", null); return;
        }
        LibroPrestable lp = (LibroPrestable) libro;
        if (!lp.getDisponibilidad()) {
            alerta(Alert.AlertType.WARNING, "No disponible", "Este libro ya está prestado.", null); return;
        }

        TextInputDialog dRut = new TextInputDialog();
        dRut.setTitle("Prestar Libro"); 
        dRut.setHeaderText("Prestar: " + libro.getTitulo()); 
        dRut.setContentText("RUT del socio:");
        
        dRut.showAndWait().ifPresent(rut -> {
            try {
                Socio socio = inventario.getSocio(rut.trim());
                
                TextInputDialog dDias = new TextInputDialog("0");
                dDias.setTitle("Plazo del Préstamo");
                dDias.setHeaderText("Socio: " + socio.getNombre());
                dDias.setContentText("Días de plazo (0 para indefinido):");
                
                dDias.showAndWait().ifPresent(diasStr -> {
                    try {
                        int dias = Integer.parseInt(diasStr.trim());
                        boolean exito;
                        if (dias > 0) {
                            exito = inventario.prestarLibro(socio, libro, dias);
                        } else {
                            exito = inventario.prestarLibro(socio, libro);
                        }
                        
                        if (exito) {
                            tabla.refresh();
                            alerta(Alert.AlertType.INFORMATION, "Éxito", "Libro prestado a " + socio.getNombre() + ".", null);
                        } else {
                            alerta(Alert.AlertType.ERROR, "Error", "No se pudo realizar el préstamo.", null);
                        }
                        
                    } catch (NumberFormatException ex) {
                        alerta(Alert.AlertType.ERROR, "Error", "Debes ingresar un número válido de días.", null);
                    }
                });

            } catch (SocioNoEncontradoException e) {
                alerta(Alert.AlertType.ERROR, "Error", e.getMessage(), null);
            }
        });
    }

    /**
     * Ejecuta la devolución de un libro delegando la lógica de negocio en
     * {@link Inventario#devolverLibro(Socio, LibroPrestable)}.
     */
    private void logicaDevolverLibro(Libro libro, TableView<Libro> tabla) {
        if (!(libro instanceof LibroPrestable)) {
            alerta(Alert.AlertType.WARNING, "No prestable", "Este libro no es de tipo prestable.", null); return;
        }
        LibroPrestable lp = (LibroPrestable) libro;
        if (lp.getDisponibilidad()) {
            alerta(Alert.AlertType.WARNING, "Disponible", "Este libro no está prestado actualmente.", null); return;
        }

        Socio duenio = null;
        for (Socio s : inventario.getSocios().values())
            if (s.getLibrosPrestados().contains(libro)) { duenio = s; break; }

        String info = duenio != null ? " (socio: " + duenio.getNombre() + ")" : "";
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Confirmar devolución de '" + libro.getTitulo() + "'" + info + "?", ButtonType.YES, ButtonType.NO);
        final Socio socioFinal = duenio;
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                inventario.devolverLibro(socioFinal, lp);
                tabla.refresh();
                alerta(Alert.AlertType.INFORMATION, "Éxito", "Libro devuelto correctamente.", null);
            }
        });
    }

    // ---------------------------------------------------------------
    // Gestionar Socios
    // ---------------------------------------------------------------

    private void abrirVentanaSocios() {
        Stage ventana = ventanaModal("Gestionar Socios");

        Label titulo = new Label("Socios Registrados");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        ListView<Socio> lista = new ListView<>(inventario.getSociosAsObservableList());
        lista.setCellFactory(lv -> new ListCell<Socio>() {
            @Override protected void updateItem(Socio s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(""); return; }
                setText(s.getNombre() + " | " + s.getRut() + " | Préstamos activos: " + s.getLibrosPrestados().size());
            }
        });

        Button btnAgregar  = new Button("Registrar Socio");
        Button btnDetalle  = new Button("Ver Detalle");
        Button btnEliminar = new Button("Eliminar Socio");
        HBox botones = new HBox(10, btnAgregar, btnDetalle, btnEliminar);
        botones.setAlignment(Pos.CENTER);

        btnAgregar.setOnAction(e -> {
            mostrarDialogoRegistrarSocio();
            lista.setItems(inventario.getSociosAsObservableList());
        });

        btnDetalle.setOnAction(e -> {
            Socio sel = lista.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un socio.", null); return; }
            mostrarDetalleSocio(sel);
            lista.refresh();
        });

        btnEliminar.setOnAction(e -> {
            Socio sel = lista.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un socio.", null); return; }
            if (!sel.getLibrosPrestados().isEmpty()) {
                alerta(Alert.AlertType.WARNING, "No permitido", "El socio tiene libros pendientes de devolución.", null); return;
            }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar a '" + sel.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    inventario.eliminarSocio(sel.getRut());
                    lista.setItems(inventario.getSociosAsObservableList());
                }
            });
        });

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));
        layout.setTop(titulo);     BorderPane.setAlignment(titulo, Pos.CENTER); BorderPane.setMargin(titulo, new Insets(0,0,12,0));
        layout.setCenter(lista);
        layout.setBottom(botones); BorderPane.setMargin(botones, new Insets(12,0,0,0));

        ventana.setScene(new Scene(layout, 500, 520));
        ventana.showAndWait();
    }

    private void mostrarDialogoRegistrarSocio() {
        Dialog<Socio> dialog = new Dialog<>();
        dialog.setTitle("Registrar Socio"); dialog.setHeaderText("Datos del nuevo socio");

        ButtonType btnReg = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnReg, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8); grid.setPadding(new Insets(15));

        TextField fNombre   = field("Nombre completo");
        TextField fRut      = field("xxxxxxxx-x");
        TextField fContacto = field("+569xxxxxxxx");

        grid.add(new Label("Nombre:"),   0, 0); grid.add(fNombre,   1, 0);
        grid.add(new Label("RUT:"),      0, 1); grid.add(fRut,      1, 1);
        grid.add(new Label("Contacto:"), 0, 2); grid.add(fContacto, 1, 2);

        Node regBtn = dialog.getDialogPane().lookupButton(btnReg);
        Runnable validar = () -> regBtn.setDisable(
            fNombre.getText().trim().isEmpty() || fRut.getText().trim().isEmpty() || fContacto.getText().trim().isEmpty());
        for (TextField f : new TextField[]{fNombre, fRut, fContacto})
            f.textProperty().addListener((o,a,b) -> validar.run());
        validar.run();

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn != btnReg ? null
            : new Socio(fNombre.getText().trim(), fRut.getText().trim(), fContacto.getText().trim()));

        dialog.showAndWait().ifPresent(socio -> {
            if (inventario.getSocios().containsKey(socio.getRut())) {
                alerta(Alert.AlertType.ERROR, "Error", "Ya existe un socio con RUT: " + socio.getRut(), null);
            } else {
                inventario.setSocio(socio.getRut(), socio);
                alerta(Alert.AlertType.INFORMATION, "Éxito", "Socio registrado correctamente.", null);
            }
        });
    }

    /**
     * Muestra el detalle de un socio y permite gestionar devoluciones delegando
     * en {@link Inventario#devolverLibro(Socio, LibroPrestable)}.
     */
    private void mostrarDetalleSocio(Socio socio) {
        Stage ventana = ventanaModal("Detalle: " + socio.getNombre());

        Label info = new Label(
            "Nombre:    " + socio.getNombre() + "\n" +
            "RUT:       " + socio.getRut()     + "\n" +
            "Contacto:  " + socio.getNumeroContacto()
        );
        info.setStyle("-fx-font-size: 13px;");

        Label lblPrestados = new Label("Libros prestados:");
        lblPrestados.setStyle("-fx-font-weight: bold;");

        ListView<Libro> listaLibros = new ListView<>(socio.getLibrosPrestados());
        listaLibros.setCellFactory(lv -> new ListCell<Libro>() {
            @Override protected void updateItem(Libro l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) { setText(""); return; }
                String fecha = (l instanceof LibroPrestable && ((LibroPrestable) l).getFechaPrestamo() != null)
                    ? "  (desde: " + ((LibroPrestable) l).getFechaPrestamo() + ")" : "";
                setText(l.getTitulo() + "  [ID: " + l.getIdInterno() + "]" + fecha);
            }
        });
        listaLibros.setPrefHeight(160);

        Button btnDevolver = new Button("Devolver libro seleccionado");
        btnDevolver.setMaxWidth(Double.MAX_VALUE);
        btnDevolver.setOnAction(e -> {
            Libro sel = listaLibros.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un libro.", null); return; }
            if (sel instanceof LibroPrestable) {
                // Delegar lógica de negocio al modelo
                inventario.devolverLibro(socio, (LibroPrestable) sel);
                listaLibros.refresh();
                alerta(Alert.AlertType.INFORMATION, "Éxito", "Libro devuelto.", null);
            }
        });

        VBox layout = new VBox(12, info, lblPrestados, listaLibros, btnDevolver);
        layout.setPadding(new Insets(20));
        ventana.setScene(new Scene(layout, 440, 380));
        ventana.showAndWait();
    }

    // ---------------------------------------------------------------
    // Filtros y Reportes
    // ---------------------------------------------------------------

    private void logicaFiltros() {
        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Filtros y Reportes");
        dialog.setHeaderText("Seleccione el criterio de filtro");

        ButtonType btnFiltrar = new ButtonType("Filtrar y Ver Gráfico", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnFiltrar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20, 80, 10, 10));

        ToggleGroup group = new ToggleGroup();
        RadioButton rbCat   = new RadioButton("Por categoría:");               rbCat.setToggleGroup(group); rbCat.setSelected(true);
        RadioButton rbPre   = new RadioButton("Precio mínimo:");               rbPre.setToggleGroup(group);
        RadioButton rbDisp  = new RadioButton("Libros prestables disponibles");rbDisp.setToggleGroup(group);
        RadioButton rbPrest = new RadioButton("Libros en préstamo activo");    rbPrest.setToggleGroup(group);

        TextField txtCat = field("ej. Novela");
        TextField txtPre = new TextField("0"); txtPre.setDisable(true);

        group.selectedToggleProperty().addListener((obs, old, nuevo) -> {
            txtCat.setDisable(nuevo != rbCat);
            txtPre.setDisable(nuevo != rbPre);
        });

        grid.add(rbCat,   0, 0); grid.add(txtCat, 1, 0);
        grid.add(rbPre,   0, 1); grid.add(txtPre, 1, 1);
        grid.add(rbDisp,  0, 2);
        grid.add(rbPrest, 0, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn != btnFiltrar) return null;
            try {
                if (rbCat.isSelected())   return new Object[]{"CATEGORIA", txtCat.getText().trim()};
                if (rbPre.isSelected())   return new Object[]{"PRECIO",    Integer.parseInt(txtPre.getText().trim())};
                if (rbDisp.isSelected())  return new Object[]{"DISPONIBLES", null};
                if (rbPrest.isSelected()) return new Object[]{"PRESTADOS",   null};
            } catch (Exception e) {
                alerta(Alert.AlertType.ERROR, "Error", "Dato inválido.", e.getMessage());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(filtro -> {
            String tipo = (String) filtro[0];
            List<Libro> filtrados = filtrarLibros(tipo, filtro[1]);

            String tituloGrafico;
            switch (tipo) {
                case "CATEGORIA":   tituloGrafico = "Categoría: '" + filtro[1] + "'"; break;
                case "PRECIO":      tituloGrafico = "Precio ≥ $" + filtro[1];         break;
                case "DISPONIBLES": tituloGrafico = "Libros prestables disponibles";  break;
                default:            tituloGrafico = "Libros en préstamo activo";      break;
            }

            if (filtrados.isEmpty()) {
                alerta(Alert.AlertType.INFORMATION, "Sin resultados", "No hay libros que coincidan.", null); return;
            }
            mostrarVentanaGraficoYReporte(filtrados, tipo, tituloGrafico);
        });
    }

    private List<Libro> filtrarLibros(String tipo, Object valor) {
        List<Libro> resultado = new ArrayList<>();
        for (Libro l : getAllLibros()) {
            switch (tipo) {
                case "CATEGORIA":
                    if (l.getCategoria().toLowerCase().contains(((String) valor).toLowerCase())) resultado.add(l);
                    break;
                case "PRECIO":
                    if (l.getPrecio() >= (int) valor) resultado.add(l);
                    break;
                case "DISPONIBLES":
                    if (l instanceof LibroPrestable && ((LibroPrestable) l).getDisponibilidad()) resultado.add(l);
                    break;
                case "PRESTADOS":
                    if (l instanceof LibroPrestable && !((LibroPrestable) l).getDisponibilidad()) resultado.add(l);
                    break;
            }
        }
        return resultado;
    }

    private void mostrarVentanaGraficoYReporte(List<Libro> libros, String tipo, String tituloGrafico) {
        Stage ventana = ventanaModal("Reporte: " + tituloGrafico);

        JFreeChart chart = GraficoUtilidades.crearGraficoLibrosPorCategoria(libros, tituloGrafico);
        ChartPanel chartPanel = new ChartPanel(chart);

        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> swingNode.setContent(chartPanel));

        Button btnExcel = new Button("Exportar a Excel (.xlsx)");
        btnExcel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 13px;");
        btnExcel.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar Reporte Excel");
            fc.setInitialFileName("Reporte_" + tipo + "_" + LocalDate.now() + ".xlsx");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
            File archivo = fc.showSaveDialog(primaryStage);
            if (archivo != null) {
                try {
                    ExportadorExcel.generarReporteLibros(inventario, libros, archivo.getAbsolutePath());
                    alerta(Alert.AlertType.INFORMATION, "Éxito", "Reporte guardado.", "Archivo: " + archivo.getName());
                } catch (IOException ex) {
                    alerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el Excel.", ex.getMessage());
                }
            }
        });

        Label lblTotal = new Label("Libros en el reporte: " + libros.size());
        lblTotal.setStyle("-fx-font-style: italic;");

        HBox botonesBar = new HBox(15, lblTotal, btnExcel);
        botonesBar.setAlignment(Pos.CENTER);
        botonesBar.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setCenter(swingNode);
        layout.setBottom(botonesBar);

        ventana.setScene(new Scene(layout, 820, 570));
        ventana.showAndWait();
    }

    // ---------------------------------------------------------------
    // Helpers reutilizables
    // ---------------------------------------------------------------

    private List<Libro> getAllLibros() {
        List<Libro> all = new ArrayList<>();
        for (Seccion s : inventario.getSecciones().values())
            for (ObservableList<Libro> lista : s.getLibros().values())
                all.addAll(lista);
        return all;
    }

    private Stage ventanaModal(String titulo) {
        Stage v = new Stage();
        v.initModality(Modality.APPLICATION_MODAL);
        v.setTitle(titulo);
        return v;
    }

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        return f;
    }

    private void alerta(Alert.AlertType tipo, String titulo, String encabezado, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(encabezado);
        if (contenido != null) a.setContentText(contenido);
        a.showAndWait();
    }
}