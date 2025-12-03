import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console application to interact with the DataFrame via a text menu.
 */
public class ConsoleApp {

    private static DataFrame currentFrame = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();

            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.\n");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> handleLoadCSV();
                    case 2 -> handleViewData();
                    case 3 -> handleFilter();
                    case 4 -> handleSort();
                    case 5 -> handleStats();
                    case 6 -> handleUpdateCell();
                    case 7 -> handleAddRow();
                    case 8 -> handleDeleteRow();
                    case 9 -> handleSaveCSV();
                    case 10 -> {
                        System.out.println("Exiting program. Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("=============== J-DataFrame Menu ===============");
        System.out.println("1. Load CSV File");
        System.out.println("2. View Data (Head)");
        System.out.println("3. Filter Data");
        System.out.println("4. Sort Data");
        System.out.println("5. Show Statistics");
        System.out.println("6. Update a Value");
        System.out.println("7. Add New Record");
        System.out.println("8. Delete a Record");
        System.out.println("9. Save to CSV");
        System.out.println("10. Exit Program");
        System.out.println("================================================");
    }

    private static void handleLoadCSV() {
        System.out.print("Enter CSV file path: ");
        String path = scanner.nextLine().trim();

        try {
            currentFrame = CSVUtils.readCSV(path);
            System.out.println("CSV loaded successfully.");
            System.out.println("Columns: " + currentFrame.getColumnNames());
            System.out.println("Rows: " + currentFrame.getRowCount());
        } catch (IOException e) {
            System.out.println("Failed to load CSV: " + e.getMessage());
        }
    }

    private static void requireDataFrame() {
        if (currentFrame == null) {
            throw new IllegalStateException("No DataFrame loaded. Please load a CSV first.");
        }
    }

    private static void handleViewData() {
        requireDataFrame();
        System.out.print("How many rows to show? ");
        String s = scanner.nextLine();

        int n;
        try {
            n = Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number, defaulting to 5.");
            n = 5;
        }
        currentFrame.printHead(n);
    }

    private static void handleFilter() {
        requireDataFrame();

        System.out.println("Choose filter type:");
        System.out.println("1. Equals (String)");
        System.out.println("2. Numeric comparison");
        System.out.print("Your choice: ");

        String s = scanner.nextLine().trim();
        int t;
        try {
            t = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
            return;
        }

        System.out.print("Enter column name: ");
        String col = scanner.nextLine().trim();

        if (t == 1) {
            System.out.print("Enter value to match exactly: ");
            String val = scanner.nextLine();
            currentFrame = currentFrame.filterEquals(col, val);
            System.out.println("Filter applied. Rows now: " + currentFrame.getRowCount());
        } else if (t == 2) {
            System.out.print("Enter operator (>, >=, <, <=, ==, !=): ");
            String op = scanner.nextLine().trim();
            System.out.print("Enter numeric value: ");
            String sv = scanner.nextLine().trim();

            double v;
            try {
                v = Double.parseDouble(sv);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric value.");
                return;
            }

            currentFrame = currentFrame.filterNumeric(col, op, v);
            System.out.println("Filter applied. Rows now: " + currentFrame.getRowCount());
        } else {
            System.out.println("Invalid filter type.");
        }
    }

    private static void handleSort() {
        requireDataFrame();
        System.out.print("Enter column name to sort by: ");
        String col = scanner.nextLine().trim();

        System.out.print("Sort ascending? (true/false): ");
        String s = scanner.nextLine().trim();

        boolean asc = Boolean.parseBoolean(s);
        currentFrame.sortBy(col, asc);
        System.out.println("Data sorted by '" + col + "' (" + (asc ? "ascending" : "descending") + ").");
    }

    private static void handleStats() {
        requireDataFrame();
        currentFrame.describe();
    }

    private static void handleUpdateCell() {
        requireDataFrame();
        System.out.println("Current rows: 0 to " + (currentFrame.getRowCount() - 1));

        System.out.print("Enter row index to update (0-based): ");
        String sr = scanner.nextLine().trim();
        int row;
        try {
            row = Integer.parseInt(sr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid row index.");
            return;
        }

        System.out.print("Enter column name to update: ");
        String col = scanner.nextLine().trim();

        System.out.print("Enter new value: ");
        String newVal = scanner.nextLine();

        currentFrame.updateCell(row, col, newVal);
        System.out.println("Cell updated successfully.");
    }

    private static void handleAddRow() {
        requireDataFrame();
        List<String> colNames = new ArrayList<>(currentFrame.getColumnNames());
        List<String> values = new ArrayList<>();

        System.out.println("Adding new record. Please enter value for each column:");
        for (String col : colNames) {
            System.out.print(col + " = ");
            String val = scanner.nextLine();
            values.add(val);
        }

        currentFrame.addRow(values);
        System.out.println("New record added. Rows now: " + currentFrame.getRowCount());
    }

    private static void handleDeleteRow() {
        requireDataFrame();
        System.out.println("Current rows: 0 to " + (currentFrame.getRowCount() - 1));
        System.out.print("Enter row index to delete (0-based): ");
        String sr = scanner.nextLine().trim();

        int row;
        try {
            row = Integer.parseInt(sr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid row index.");
            return;
        }

        currentFrame.deleteRow(row);
        System.out.println("Row deleted. Rows now: " + currentFrame.getRowCount());
    }

    private static void handleSaveCSV() {
        requireDataFrame();
        System.out.print("Enter output CSV file path: ");
        String path = scanner.nextLine().trim();

        try {
            CSVUtils.writeCSV(currentFrame, path);
            System.out.println("DataFrame saved to " + path);
        } catch (IOException e) {
            System.out.println("Failed to save CSV: " + e.getMessage());
        }
    }
}
