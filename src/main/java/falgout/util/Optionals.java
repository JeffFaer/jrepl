package falgout.util;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Optionals {
    public static <E> Collection<? extends Optional<? extends E>> optionalize(List<? extends E> execute) {
        return execute.stream().map(e -> Optional.of(e)).collect(toList());
    }
}
