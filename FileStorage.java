import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Saves and loads lists of Storable objects as CSV text, one object per line. */
public class FileStorage<T extends Storable> {
    private String filePath;

    public FileStorage(String filePath) {
        this.filePath = filePath;
    }

    /** Overwrites the file with one CSV line per item. */
    public void save(List<T> items) {
        List<String> lines = items.stream().map(Storable::toCsv).collect(Collectors.toList());
        try {
            Files.write(Paths.get(filePath), lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + filePath, e);
        }
    }

    /** Reads every line and turns it back into an object using the given parser. */
    public List<T> load(Function<String, T> parser) {
        Path p = Paths.get(filePath);
        if (!Files.exists(p)) {
            return new ArrayList<>();   // nothing saved yet
        }
        try {
            return Files.readAllLines(p).stream()
                    .filter(line -> !line.trim().isEmpty())
                    .map(parser)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load " + filePath, e);
        }
    }

    /** Adds one item to the end of the file (creates the file if needed). */
    public void append(T item) {
        try {
            Files.write(Paths.get(filePath), Collections.singletonList(item.toCsv()),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not append to " + filePath, e);
        }
    }
}
