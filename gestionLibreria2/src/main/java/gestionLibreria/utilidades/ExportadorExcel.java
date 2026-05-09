package gestionLibreria.utilidades;

import gestionLibreria.extensiones.LibroDigital;
import gestionLibreria.extensiones.LibroPrestable;
import gestionLibreria.inventario.Inventario;
import gestionLibreria.inventario.Libro;
import gestionLibreria.inventario.Seccion;
import gestionLibreria.inventario.Socio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Clase utilitaria para generar reportes en formato Excel (.xlsx) del inventario de libros.
 */
public class ExportadorExcel {

    public ExportadorExcel() {}

    /**
     * Genera un reporte Excel con los libros indicados.
     * Incluye columnas adicionales según el tipo de libro (Prestable o Digital).
     *
     * @param inventario  El inventario para obtener la sección de cada libro.
     * @param libros      Lista de libros a incluir en el reporte.
     * @param nombreArchivo Ruta completa del archivo .xlsx a generar.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static void generarReporteLibros(Inventario inventario, List<Libro> libros,
                                            String nombreArchivo) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Libros");
            CellStyle headerStyle = crearEstiloEncabezado(workbook);

            String[] titulos = {
                "Sección", "ID", "Título", "Autores", "Categoría", "Edición",
                "Páginas", "Fecha Pub.", "Precio", "Tipo",
                "Disponible", "Multa", "Retraso", "F. Préstamo", "F. Devolución",
                "Memoria (MB)", "Formato"
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < titulos.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(titulos[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Libro l : libros) {
                Seccion sec = inventario.encontrarSeccionDeLibro(l.getTitulo());
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(sec != null ? sec.getNombre() : "N/A");
                row.createCell(1).setCellValue(l.getIdInterno());
                row.createCell(2).setCellValue(l.getTitulo());
                row.createCell(3).setCellValue(String.join("; ", l.getAutores()));
                row.createCell(4).setCellValue(l.getCategoria());
                row.createCell(5).setCellValue(l.getEdicion());
                row.createCell(6).setCellValue(l.getPaginas());
                row.createCell(7).setCellValue(l.getFechaDePublicacion() != null ? l.getFechaDePublicacion().toString() : "");
                row.createCell(8).setCellValue(l.getPrecio());

                if (l instanceof LibroPrestable) {
                    LibroPrestable lp = (LibroPrestable) l;
                    row.createCell(9).setCellValue("Prestable");
                    row.createCell(10).setCellValue(lp.getDisponibilidad() ? "Sí" : "No");
                    row.createCell(11).setCellValue(lp.getMulta());
                    row.createCell(12).setCellValue(lp.getRetraso());
                    row.createCell(13).setCellValue(lp.getFechaPrestamo()   != null ? lp.getFechaPrestamo().toString()   : "");
                    row.createCell(14).setCellValue(lp.getFechaDevolucion() != null ? lp.getFechaDevolucion().toString() : "");
                } else if (l instanceof LibroDigital) {
                    LibroDigital ld = (LibroDigital) l;
                    row.createCell(9).setCellValue("Digital");
                    row.createCell(15).setCellValue(ld.getMemoria());
                    row.createCell(16).setCellValue(ld.getFormato());
                } else {
                    row.createCell(9).setCellValue("Base");
                }
            }

            for (int i = 0; i < titulos.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(nombreArchivo)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Genera un reporte Excel con la lista de socios y sus libros prestados.
     *
     * @param socios        Lista de socios a incluir.
     * @param nombreArchivo Ruta completa del archivo .xlsx a generar.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static void generarReporteSocios(List<Socio> socios, String nombreArchivo) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Socios");
            CellStyle headerStyle = crearEstiloEncabezado(workbook);

            String[] titulos = {"Nombre", "RUT", "Contacto", "Libros Prestados", "Títulos"};

            Row header = sheet.createRow(0);
            for (int i = 0; i < titulos.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(titulos[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Socio s : socios) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(s.getNombre());
                row.createCell(1).setCellValue(s.getRut());
                row.createCell(2).setCellValue(s.getNumeroContacto());
                row.createCell(3).setCellValue(s.getLibrosPrestados().size());

                StringBuilder sb = new StringBuilder();
                for (Libro l : s.getLibrosPrestados()) {
                    if (sb.length() > 0) sb.append("; ");
                    sb.append(l.getTitulo()).append(" [ID:").append(l.getIdInterno()).append("]");
                }
                row.createCell(4).setCellValue(sb.toString());
            }

            for (int i = 0; i < titulos.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(nombreArchivo)) {
                workbook.write(fos);
            }
        }
    }

    private static CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}