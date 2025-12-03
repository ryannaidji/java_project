import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Utility class for reading and writing CSV files.
 */
public class CSVUtils {

    public static DataFrame readCSV(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        List<String> lines = Files.readAllLines(path);
        if (lines.isEmpty()) {
            throw new IOException("CSV file is empty.");
        }

        // Header
        String headerLine = lines.get(0);
        String[] headers = headerLine.split(",");

        // Detect numeric columns
        boolean[] isNumeric = new boolean[headers.length];
        Arrays.fill(isNumeric, true);

        List<String[]> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            rows.add(parts);

            for (int c = 0; c < headers.length && c < parts.length; c++) {
                String val = parts[c].trim();
                if (val.isEmpty()) continue;
                try {
                    Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    isNumeric[c] = false;
                }
            }
        }

        Map<String, DataColumn> cols = new LinkedHashMap<>();
        for (int c = 0; c < headers.length; c++) {
            String name = headers[c].trim();
            DataColumn col = isNumeric[c] ? new NumericColumn(name) : new StringColumn(name);
            cols.put(name, col);
        }

        // Fill data
        for (String[] parts : rows) {
            for (int c = 0; c < headers.length; c++) {
                String name = headers[c].trim();
                DataColumn col = cols.get(name);
                String val = (c < parts.length) ? parts[c].trim() : "";
                col.add(val);
            }
        }

        DataFrame df = new DataFrame();
        df.setColumns(cols, rows.size());
        return df;
    }

    public static void writeCSV(DataFrame df, String filePath) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath))) {
            List<String> colNames = new ArrayList<>(df.getColumnNames());

            // Header
            for (int i = 0; i < colNames.size(); i++) {
                out.print(colNames.get(i));
                if (i < colNames.size() - 1) {
                    out.print(",");
                }
            }
            out.println();

            int rows = df.getRowCount();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < colNames.size(); c++) {
                    DataColumn col = df.getColumn(colNames.get(c));
                    out.print(col.get(r));
                    if (c < colNames.size() - 1) {
                        out.print(",");
                    }
                }
                out.println();
            }
        }
    }
}
