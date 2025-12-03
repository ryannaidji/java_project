/**
 * Column for numeric data. Stores values as Strings,
 * but parses them to double when needed.
 */
public class NumericColumn extends DataColumn {

    public NumericColumn(String name) {
        super(name);
    }

    private double toDouble(String s) {
        return Double.parseDouble(s.trim());
    }

    public double min() {
        if (values.isEmpty()) {
            throw new IllegalStateException("Column " + name + " is empty.");
        }
        double min = toDouble(values.get(0));
        for (int i = 1; i < values.size(); i++) {
            double v = toDouble(values.get(i));
            if (v < min) {
                min = v;
            }
        }
        return min;
    }

    public double max() {
        if (values.isEmpty()) {
            throw new IllegalStateException("Column " + name + " is empty.");
        }
        double max = toDouble(values.get(0));
        for (int i = 1; i < values.size(); i++) {
            double v = toDouble(values.get(i));
            if (v > max) {
                max = v;
            }
        }
        return max;
    }

    public double average() {
        if (values.isEmpty()) {
            throw new IllegalStateException("Column " + name + " is empty.");
        }
        double sum = 0.0;
        for (String s : values) {
            sum += toDouble(s);
        }
        return sum / values.size();
    }
}
