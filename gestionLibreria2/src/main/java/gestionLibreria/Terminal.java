package gestionLibreria;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import gestionLibreria.excepciones.*;
import gestionLibreria.extensiones.*;
import gestionLibreria.inventario.*;
import gestionLibreria.utilidades.*;

import javafx.collections.ObservableList;

public class Terminal {

    public static void modoTerminal(Inventario inventario, GestorPersistencia gestor) {
        System.out.println("Bienvenido al modo terminal");
        Consola.enterParaContinuar();

        int decision = 0;
        while (decision != 11) {
            Consola.limpiarPantalla();
            System.out.println("+------------------------------+");
            System.out.println("|     Gestion de Libreria      |");
            System.out.println("+------------------------------+");
            System.out.println("|  1. Ver Inventario           |");
            System.out.println("|  2. Gestionar Secciones      |");
            System.out.println("|  3. Ver Socios               |");
            System.out.println("|  4. Registrar Socio          |");
            System.out.println("|  5. Eliminar Socio           |");
            System.out.println("|  6. Vender Libro             |");
            System.out.println("|  7. Prestar Libro a Socio    |");
            System.out.println("|  8. Devolver Libro           |");
            System.out.println("|  9. Buscar Libro por Nombre  |");
            System.out.println("| 10. Filtros y Reporte Excel  |");
            System.out.println("| 11. Guardar y Salir          |");
            System.out.println("+------------------------------+");
            decision = Consola.leerEntero("Opcion: ");
            Consola.limpiarPantalla();

            switch (decision) {
                case 1:  menuInventario(inventario);        break;
                case 2:  menuSecciones(inventario);         break;
                case 3:  menuSocios(inventario);            break;
                case 4:  registrarSocio(inventario);        break;
                case 5:  eliminarSocio(inventario);         break;
                case 6:  venderLibro(inventario);           break;
                case 7:  prestarLibro(inventario);          break;
                case 8:  devolverLibro(inventario);         break;
                case 9:  buscarLibro(inventario);           break;
                case 10: filtrosYReporte(inventario);       break;
                case 11:
                    guardarYSalir(gestor, inventario);
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    Consola.enterParaContinuar();
                    break;
            }
        }
    }

    private static void mostrarLibro(Libro libro) {
        System.out.println("- Titulo: " + libro.getTitulo());
        System.out.println("- Fecha de publicacion: " + libro.getFechaDePublicacion());

        if (libro.getAutores() != null && !libro.getAutores().isEmpty()) {
            System.out.print("- Autores: ");
            for (int i = 0; i < libro.getAutores().size(); i++) {
                System.out.print(libro.getAutores().get(i));
                if (i < libro.getAutores().size() - 1) System.out.print(", ");
            }
            System.out.println();
        } else {
            System.out.println("- Autores: No especificados");
        }

        System.out.println("- Categoria: " + libro.getCategoria());
        System.out.println("- Paginas: "   + libro.getPaginas());
        System.out.println("- Precio: "    + libro.getPrecio());
        System.out.println("- ID: "        + libro.getIdInterno());

        if (libro instanceof LibroPrestable) {
            LibroPrestable lp = (LibroPrestable) libro;
            System.out.println("Disponibilidad: "   + (lp.getDisponibilidad() ? "Disponible" : "Prestado"));
            System.out.println("Retraso: "          + lp.getRetraso());
            System.out.println("Multa: "            + lp.getMulta());
            System.out.println("Fecha de Entrega: " + lp.getFechaDevolucion());
            System.out.println("Fecha de Prestamo: "+ lp.getFechaPrestamo());
        } else if (libro instanceof LibroDigital) {
            LibroDigital ld = (LibroDigital) libro;
            System.out.println("Formato: "      + ld.getFormato());
            System.out.println("Memoria (MB): " + ld.getMemoria());
        }
    }

    private static void mostrarSocio(Socio socio) {
        System.out.println("- Nombre: "  + socio.getNombre());
        System.out.println("- RUT: "     + socio.getRut());
        System.out.println("- Numero: "  + socio.getNumeroContacto());
        System.out.print("- Libros prestados: ");
        ObservableList<Libro> prestados = socio.getLibrosPrestados();
        for (int i = 0; i < prestados.size(); i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(prestados.get(i).getTitulo());
        }
        System.out.println();
    }

    private static void menuInventario(Inventario inventario) {
        ObservableList<Seccion> secciones = inventario.getSeccionesAsObservableList();

        if (secciones.isEmpty()) {
            System.out.println("No existen secciones en el inventario.");
            Consola.enterParaContinuar();
            return;
        }

        Seccion seccion = seleccionarSeccion(inventario, secciones);
        if (seccion == null) return;

        ObservableList<String> llaves = seccion.GetLlaves();
        if (llaves.isEmpty()) {
            System.out.println("Esta seccion esta vacia.");
            Consola.enterParaContinuar();
            return;
        }

        System.out.println("\nLibros en \"" + seccion.getNombre() + "\":");
        for (String nombre : llaves) System.out.println("  - " + nombre);

        String opcion = Consola.leerString("\n¿Ver 'informacion' o 'agregar' libro?: ");

        if (opcion.equalsIgnoreCase("informacion")) {
            verInformacionLibro(seccion);
        } else if (opcion.equalsIgnoreCase("agregar")) {
            agregarLibroASeccion(inventario, seccion);
        } else {
            System.out.println("Opcion no reconocida.");
            Consola.enterParaContinuar();
        }
    }

    private static Seccion seleccionarSeccion(Inventario inventario, ObservableList<Seccion> secciones) {
        while (true) {
            System.out.println("\n--- Secciones Disponibles ---");
            for (Seccion s : secciones) System.out.println("  - " + s.getNombre());

            String nombre = Consola.leerString("\nNombre de la seccion (o 'cancelar'): ");
            if (nombre.equalsIgnoreCase("cancelar")) return null;

            Seccion s = inventario.getSeccion(nombre);
            if (s != null) return s;
            System.out.println("La seccion '" + nombre + "' no existe.");
        }
    }

    private static void verInformacionLibro(Seccion seccion) {
        String titulo = Consola.leerString("Ingrese el titulo del libro: ");
        ObservableList<Libro> encontrados = seccion.encontrarLibrosPorTitulo(titulo);

        if (encontrados == null || encontrados.isEmpty()) {
            System.out.println("Libro no encontrado.");
        } else {
            for (Libro l : encontrados) {
                System.out.println("----------------------------------------");
                mostrarLibro(l);
            }
            System.out.println("----------------------------------------");
        }
        Consola.enterParaContinuar();
    }

    private static void agregarLibroASeccion(Inventario inventario, Seccion seccion) {
        System.out.println("Tipo de libro:");
        System.out.println("  1. Normal");
        System.out.println("  2. Prestable");
        System.out.println("  3. Digital");
        int tipo = Consola.leerEntero("Tipo: ");

        LocalDate fechaP  = Consola.leerFecha("Fecha de publicacion (yyyy-MM-dd): ");
        String titulo     = Consola.leerString("Titulo: ");
        String edicion    = Consola.leerString("Edicion: ");
        String categoria  = Consola.leerString("Categoria: ");
        int pag           = Consola.leerEntero("Numero de paginas: ");
        int id            = inventario.getNumeroLibros() + 1;
        int precio        = Consola.leerEntero("Precio: ");
        List<String> autores = leerAutores();

        Libro nuevo;
        switch (tipo) {
            case 2:
                int multa = Consola.leerEntero("Multa por retraso: ");
                nuevo = new LibroPrestable(fechaP, titulo, edicion, categoria, pag, id, precio,
                                           (ArrayList<String>) autores, multa);
                break;
            case 3:
                int    memoria = Consola.leerEntero("Memoria (MB): ");
                String formato = Consola.leerString("Formato (ej. PDF, EPUB): ");
                nuevo = new LibroDigital(fechaP, titulo, edicion, categoria, pag, id, precio,
                                         (ArrayList<String>) autores, memoria, formato);
                break;
            default:
                nuevo = new Libro(fechaP, titulo, edicion, categoria, pag, id, precio, autores);
                break;
        }

        seccion.agregarLibro(nuevo);
        inventario.incrementarNumeroLibros();
        System.out.println("Libro agregado con exito! (ID asignado: " + id + ")");
        Consola.enterParaContinuar();
    }

    private static List<String> leerAutores() {
        int n = Consola.leerEntero("¿Cuantos autores?: ");
        List<String> autores = new ArrayList<>();
        for (int i = 0; i < n; i++) autores.add(Consola.leerString("Autor " + (i + 1) + ": "));
        return autores;
    }

    private static void menuSecciones(Inventario inventario) {
        System.out.println("=== Gestionar Secciones ===");
        System.out.println("1. Listar secciones");
        System.out.println("2. Agregar seccion");
        System.out.println("3. Eliminar seccion");
        int opcion = Consola.leerEntero("Opcion: ");
        Consola.limpiarPantalla();

        switch (opcion) {
            case 1: listarSecciones(inventario); break;
            case 2: agregarSeccion(inventario); break;
            case 3: eliminarSeccion(inventario); break;
            default:
                System.out.println("Opcion invalida.");
                Consola.enterParaContinuar();
                break;
        }
    }

    private static void listarSecciones(Inventario inventario) {
        ObservableList<Seccion> secciones = inventario.getSeccionesAsObservableList();
        if (secciones.isEmpty()) {
            System.out.println("No hay secciones registradas.");
            Consola.enterParaContinuar();
            return;
        }
        System.out.println("--- Secciones ---");
        for (Seccion s : secciones) {
            System.out.println("  - " + s.getNombre() + " (" + s.GetLlaves().size() + " titulos)");
        }
        Consola.enterParaContinuar();
    }

    private static void agregarSeccion(Inventario inventario) {
        String nombre = Consola.leerString("Nombre de la nueva seccion: ").trim();
        if (inventario.getSecciones().containsKey(nombre)) {
            System.out.println("Error: Ya existe una seccion con ese nombre.");
            Consola.enterParaContinuar();
            return;
        }
        inventario.setSeccion(nombre, new Seccion(nombre));
        System.out.println("Seccion \"" + nombre + "\" creada con exito.");
        Consola.enterParaContinuar();
    }

    private static void eliminarSeccion(Inventario inventario) {
        String nombre = Consola.leerString("Nombre de la seccion a eliminar: ").trim();
        if (!inventario.getSecciones().containsKey(nombre)) {
            System.out.println("Error: La seccion no existe.");
            Consola.enterParaContinuar();
            return;
        }
        System.out.println("Atencion: Se eliminaran todos los libros de esta seccion.");
        String conf = Consola.leerString("¿Confirmar? (si/no): ");
        if (conf.equalsIgnoreCase("si")) {
            inventario.getSecciones().remove(nombre);
            System.out.println("Seccion eliminada.");
        }
        Consola.enterParaContinuar();
    }

    private static void menuSocios(Inventario inventario) {
        ObservableList<Socio> socios = inventario.getSociosAsObservableList();
        if (socios.isEmpty()) {
            System.out.println("No hay socios registrados.");
            Consola.enterParaContinuar();
            return;
        }
        for (Socio s : socios) System.out.println(" - " + s.getNombre() + " (RUT: " + s.getRut() + ")");
        String rut = Consola.leerString("\nRUT del socio a consultar: ");
        try {
            mostrarSocio(inventario.getSocio(rut));
        } catch (SocioNoEncontradoException e) {
            System.out.println(e.getMessage());
        }
        Consola.enterParaContinuar();
    }

    private static void registrarSocio(Inventario inventario) {
        String nombre = Consola.leerString("Nombre: ");
        String rut = Consola.leerString("RUT: ");
        String fono = Consola.leerString("Telefono: ");
        inventario.setSocio(rut, new Socio(nombre, rut, fono));
        System.out.println("Socio registrado.");
        Consola.enterParaContinuar();
    }

    private static void eliminarSocio(Inventario inventario) {
        String rut = Consola.leerString("RUT del socio a eliminar: ");
        inventario.eliminarSocio(rut);
        System.out.println("Socio eliminado si existia.");
        Consola.enterParaContinuar();
    }

    private static void venderLibro(Inventario inventario) {
        String nombre = Consola.leerString("Nombre del libro: ");
        Seccion s = inventario.encontrarSeccionDeLibro(nombre);
        if (s != null) {
            ObservableList<Libro> libros = s.encontrarLibrosPorTitulo(nombre);
            if (!libros.isEmpty()) {
                s.venderLibro(nombre, libros.get(0).getIdInterno());
                System.out.println("Vendido con exito.");
            }
        } else {
            System.out.println("Libro no encontrado.");
        }
        Consola.enterParaContinuar();
    }

    private static void prestarLibro(Inventario inventario) {
        String rut = Consola.leerString("RUT socio: ");
        String titulo = Consola.leerString("Titulo libro: ");
        try {
            Socio s = inventario.getSocio(rut);
            ObservableList<Libro> libros = inventario.encontrarLibro(titulo);
            if (!libros.isEmpty()) {
                inventario.prestarLibro(s, libros.get(0));
                System.out.println("Prestamo realizado.");
            } else {
                System.out.println("Libro no encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        Consola.enterParaContinuar();
    }

    private static void devolverLibro(Inventario inventario) {
        String rut = Consola.leerString("RUT socio: ");
        try {
            Socio s = inventario.getSocio(rut);
            if (s.getLibrosPrestados().isEmpty()) {
                System.out.println("No tiene libros.");
            } else {
                Libro l = s.getLibrosPrestados().get(0);
                if (l instanceof LibroPrestable) {
                    inventario.devolverLibro(s, (LibroPrestable) l);
                    System.out.println("Devuelto.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        Consola.enterParaContinuar();
    }

    private static void buscarLibro(Inventario inventario) {
        String titulo = Consola.leerString("Titulo: ");
        ObservableList<Libro> lista = inventario.encontrarLibro(titulo);
        for (Libro l : lista) mostrarLibro(l);
        Consola.enterParaContinuar();
    }

    private static void filtrosYReporte(Inventario inventario) {
        System.out.println("1. Por categoria\n2. Por precio minimo");
        int opt = Consola.leerEntero("Opcion: ");
        System.out.println("Filtro aplicado. Reporte no generado en este ejemplo simplificado.");
        Consola.enterParaContinuar();
    }

    private static void guardarYSalir(GestorPersistencia gestor, Inventario inventario) {
        try {
            gestor.guardarTodo(inventario);
            System.out.println("Datos guardados. Saliendo...");
        } catch (Exception e) {
            System.out.println("Error al guardar.");
        }
    }
}