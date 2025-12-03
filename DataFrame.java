import java.util.*;

/**
 * DataFrame: holds multiple columns of equal length
 * and provides operations like head(), filter(), sortBy(), describe(),
 * update cell, add row, delete row.
 */
public class DataFrame {
    private Map<String, DataColumn> columns;
    private int rowCount;

    public DataFrame() {
        this.columns = new LinkedHashMap<>();
        this.rowCount = 0;
    }

    /**
     * Used by CSVUtils to set columns after reading a CSV file.
     */
    public void setColumns(Map<String, DataColumn> columns, int rowCount) {
        this.columns = columns;
        this.rowCount = rowCount;
    }

    public static DataFrame loadFromCSV(String filePath) throws java.io.IOException {
        return CSVUtils.readCSV(filePath);
    }

    public Set<String> getColumnNames() {
        return columns.keySet();
    }

    public DataColumn getColumn(String name) {
        return columns.get(name);
    }

    public int getRowCount() {
        return rowCount;
    }

    public void addColumn(String name, DataColumn column) {
        if (rowCount != 0 && column.size() != rowCount) {
            throw new IllegalArgumentException("Column length mismatch.");
        }
        columns.put(name, column);
        if (rowCount == 0) {
            rowCount = column.size();
        }
    }

    public void removeColumn(String name) {
        columns.remove(name);
    }

    /**
     * Print first n rows of the DataFrame.
     */
    public void printHead(int n) {
        if (rowCount == 0) {
            System.out.println("DataFrame is empty.");
            return;
        }

        int limit = Math.min(n, rowCount);
        List<String> colNames = new ArrayList<>(columns.keySet());

        // Header
        for (String col : colNames) {
            System.out.print(col + "\t");
        }
        System.out.println();

        // Separator
        System.out.println("-".repeat(colNames.size() * 8));

        // Rows
        for (int row = 0; row < limit; row++) {
            for (String col : colNames) {
                DataColumn c = columns.get(col);
                System.out.print(c.get(row) + "\t");
            }
            System.out.println();
        }

        System.out.println("Showing " + limit + " of " + rowCount + " rows.");
    }

    /**
     * Filter rows where a string column == value.
     */
    public DataFrame filterEquals(String columnName, String value) {
        DataColumn col = columns.get(columnName);
        if (col == null) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            if (col.get(i).equals(value)) {
                indices.add(i);
            }
        }

        return buildFilteredDataFrame(indices);
    }

    /**
     * Filter rows where a numeric column satisfies a comparison.
     */
    public DataFrame filterNumeric(String columnName, String operator, double value) {
        DataColumn col = columns.get(columnName);
        if (!(col instanceof NumericColumn)) {
            throw new IllegalArgumentException("Column is not numeric: " + columnName);
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            double v = Double.parseDouble(col.get(i));
            boolean keep;
            switch (operator) {
                case ">"  -> keep = v > value;
                case ">=" -> keep = v >= value;
                case "<"  -> keep = v < value;
                case "<=" -> keep = v <= value;
                case "==" -> keep = v == value;
                case "!=" -> keep = v != value;
                default   -> throw new IllegalArgumentException("Invalid operator: " + operator);
            }
            if (keep) {
                indices.add(i);
            }
        }

        return buildFilteredDataFrame(indices);
    }

    /**
     * Helper: build a new DataFrame with only selected row indices.
     */
    private DataFrame buildFilteredDataFrame(List<Integer> indices) {
        DataFrame result = new DataFrame();
        Map<String, DataColumn> newCols = new LinkedHashMap<>();

        for (String name : columns.keySet()) {
            DataColumn oldCol = columns.get(name);
            DataColumn newCol;
            if (oldCol instanceof NumericColumn) {
                newCol = new NumericColumn(name);
            } else {
                newCol = new StringColumn(name);
            }

            for (int idx : indices) {
                newCol.add(oldCol.get(idx));
            }

            newCols.put(name, newCol);
        }

        result.setColumns(newCols, indices.size());
        return result;
    }

    /**
     * Sort rows in-place by one column, ascending or descending.
     */
    public void sortBy(String columnName, boolean ascending) {
        DataColumn col = columns.get(columnName);
        if (col == null) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }

        // Build index list [0, 1, 2, ...]
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            indices.add(i);
        }

        // Comparator based on column type
        Comparator<Integer> comparator;
        if (col instanceof NumericColumn) {
            comparator = (i, j) -> {
                double vi = Double.parseDouble(col.get(i));
                double vj = Double.parseDouble(col.get(j));
                return Double.compare(vi, vj);
            };
        } else {
            comparator = (i, j) -> col.get(i).compareTo(col.get(j));
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        indices.sort(comparator);

        // Reorder all columns based on sorted indices
        for (String name : columns.keySet()) {
            DataColumn c = columns.get(name);
            List<String> newValues = new ArrayList<>();
            for (int idx : indices) {
                newValues.add(c.get(idx));
            }
            c.getValues().clear();
            c.getValues().addAll(newValues);
        }
    }

    /**
     * Print statistics (count, min, max, avg) for numeric columns.
     */
    public void describe() {
        System.out.println("Statistics for numeric columns:");
        boolean foundNumeric = false;

        for (String name : columns.keySet()) {
            DataColumn col = columns.get(name);
            if (col instanceof NumericColumn) {
                NumericColumn ncol = (NumericColumn) col;
                foundNumeric = true;

                System.out.println("Column: " + name);
                System.out.println("  count = " + ncol.size());
                try {
                    System.out.println("  min   = " + ncol.min());
                    System.out.println("  max   = " + ncol.max());
                    System.out.println("  avg   = " + ncol.average());
                } catch (NumberFormatException e) {
                    System.out.println("  (Error parsing numbers in this column)");
                }
                System.out.println();
            }
        }

        if (!foundNumeric) {
            System.out.println("No numeric columns found.");
        }
    }

    /**
     * Update one cell: row index and column name.
     * rowIndex is 0-based.
     */
    public void updateCell(int rowIndex, String columnName, String newValue) {
        if (rowIndex < 0 || rowIndex >= rowCount) {
            throw new IndexOutOfBoundsException("Row index out of range.");
        }
        DataColumn col = columns.get(columnName);
        if (col == null) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
        col.set(rowIndex, newValue);
    }

    /**
     * Add a new row at the end. Values must be in the same order as getColumnNames().
     */
    public void addRow(List<String> valuesForAllColumns) {
        List<String> colNames = new ArrayList<>(columns.keySet());
        if (valuesForAllColumns.size() != colNames.size()) {
            throw new IllegalArgumentException("Number of values does not match number of columns.");
        }

        for (int i = 0; i < colNames.size(); i++) {
            String colName = colNames.get(i);
            DataColumn col = columns.get(colName);
            col.add(valuesForAllColumns.get(i));
        }
        rowCount++;
    }

    /**
     * Delete a row (0-based index) from all columns.
     */
    public void deleteRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rowCount) {
            throw new IndexOutOfBoundsException("Row index out of range.");
        }

        for (String name : columns.keySet()) {
            DataColumn col = columns.get(name);
            col.getValues().remove(rowIndex);
        }
        rowCount--;
    }
}
