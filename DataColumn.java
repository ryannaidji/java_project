import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class representing a single column of data.
 */
public abstract class DataColumn {
    protected String name;
    protected List<String> values;

    public DataColumn(String name) {
        this.name = name;
        this.values = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int size() {
        return values.size();
    }

    public String get(int index) {
        return values.get(index);
    }

    public void set(int index, String value) {
        values.set(index, value);
    }

    public void add(String value) {
        values.add(value);
    }

    public List<String> getValues() {
        return values;
    }
}
