package gestionLibreria.utilidades;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria para leer y parsear archivos CSV.
 * <p>
 * Soporta campos encerrados entre comillas dobles, comillas escapadas dentro
 * de un campo ({@code ""}) y delimitadores dentro de campos entrecomillados.
 * </p>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>{@code
 * LectorCSV lector = new LectorCSV("data/libros.csv");
 * List<List<String>> datos = lector.readAll();
 * }</pre>
 *
 * @see GestorPersistencia
 */
public class LectorCSV {

    // ---------------------------------------------------------------
    // Campos
    // ---------------------------------------------------------------

    /** Ruta del archivo CSV que se leerá. */
    private final String filePath;

    /** Delimitador usado para separar los valores del CSV. */
    private final String delimiter;

    /** Codificación del archivo (p. ej. {@code "UTF-8"}). */
    private final String encoding;

    // ---------------------------------------------------------------
    // Constructores
    // ---------------------------------------------------------------

    /**
     * Construye un lector CSV con valores por defecto: coma como delimitador
     * y UTF-8 como codificación.
     *
     * @param filePath ruta del archivo CSV a leer
     */
    public LectorCSV(String filePath) {
        this(filePath, ",", StandardCharsets.UTF_8.name());
    }

    /**
     * Construye un lector CSV con configuración personalizada.
     *
     * @param filePath  ruta del archivo CSV
     * @param delimiter delimitador usado en el archivo (p. ej. {@code ";"})
     * @param encoding  codificación del archivo (p. ej. {@code "UTF-8"})
     */
    public LectorCSV(String filePath, String delimiter, String encoding) {
        this.filePath  = filePath;
        this.delimiter = delimiter;
        this.encoding  = encoding;
    }

    // ---------------------------------------------------------------
    // Lectura
    // ---------------------------------------------------------------

    /**
     * Lee y parsea todo el contenido del archivo CSV.
     * <p>
     * Las líneas vacías son ignoradas. La primera línea suele ser el encabezado
     * y se incluye como primer elemento de la lista retornada.
     * </p>
     *
     * @return lista de líneas, donde cada línea es a su vez una lista de campos
     * @throws IOException si ocurre un error durante la lectura del archivo
     */
    public List<List<String>> readAll() throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), encoding))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                records.add(parseCSVLine(line));
            }
        }
        return records;
    }

    // ---------------------------------------------------------------
    // Utilidades internas
    // ---------------------------------------------------------------

    /**
     * Parsea una línea de texto CSV considerando campos entre comillas y comillas escapadas.
     * <p>
     * Reglas de parseo:
     * <ul>
     *   <li>Un campo encerrado entre {@code "} puede contener comas o saltos de línea.</li>
     *   <li>Una comilla literal dentro de un campo se representa como {@code ""}.</li>
     * </ul>
     * </p>
     *
     * @param line la línea de texto CSV a parsear
     * @return lista de campos parseados
     */
    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Comilla escapada dentro de campo entrecomillado
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter.charAt(0) && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString().trim());
        return fields;
    }
}