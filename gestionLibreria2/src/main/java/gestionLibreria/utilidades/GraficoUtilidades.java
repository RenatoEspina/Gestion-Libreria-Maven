package gestionLibreria.utilidades;

import gestionLibreria.inventario.Libro;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Clase utilitaria para crear gráficos JFreeChart a partir de datos del inventario de libros.
 */
public class GraficoUtilidades {

    /**
     * Crea un gráfico de barras con la cantidad de libros por categoría.
     *
     * @param libros Lista de libros a representar.
     * @param titulo Título del gráfico.
     * @return JFreeChart listo para mostrar.
     */
    public static JFreeChart crearGraficoLibrosPorCategoria(List<Libro> libros, String titulo) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Libro l : libros) {
            String cat = l.getCategoria() != null ? l.getCategoria() : "Sin categoría";
            conteo.put(cat, conteo.getOrDefault(cat, 0) + 1);
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            dataset.addValue(entry.getValue(), "Libros", entry.getKey());
        }

        return ChartFactory.createBarChart(
            titulo,
            "Categoría",
            "Cantidad de Libros",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );
    }

    /**
     * Crea un gráfico de barras con el precio promedio de libros por categoría.
     *
     * @param libros Lista de libros a representar.
     * @param titulo Título del gráfico.
     * @return JFreeChart listo para mostrar.
     */
    public static JFreeChart crearGraficoPrecioPromedioPorCategoria(List<Libro> libros, String titulo) {
        // [0] = suma de precios, [1] = cantidad
        Map<String, int[]> datos = new LinkedHashMap<>();
        for (Libro l : libros) {
            String cat = l.getCategoria() != null ? l.getCategoria() : "Sin categoría";
            if (!datos.containsKey(cat)) datos.put(cat, new int[]{0, 0});
            datos.get(cat)[0] += l.getPrecio();
            datos.get(cat)[1]++;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, int[]> entry : datos.entrySet()) {
            int[] vals = entry.getValue();
            double promedio = vals[1] > 0 ? (double) vals[0] / vals[1] : 0;
            dataset.addValue(promedio, "Precio Promedio", entry.getKey());
        }

        return ChartFactory.createBarChart(
            titulo,
            "Categoría",
            "Precio Promedio ($)",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );
    }
}