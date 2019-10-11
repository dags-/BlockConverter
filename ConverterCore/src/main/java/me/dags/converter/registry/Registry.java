package me.dags.converter.registry;

import org.jnbt.CompoundTag;
import org.jnbt.Tag;

import java.text.ParseException;
import java.util.List;

public interface Registry<T extends RegistryItem> extends Iterable<T> {

    /**
     * Get the number of elements in this registry
     */
    int size();

    /**
     * Get the id that the provided item is registered to
     */
    int getId(T val);

    /**
     * Get the output item for the provided input id
     */
    T getValue(int id);

    /**
     * Get the data version of this registry
     */
    String getVersion();

    /**
     * Get the value parser for this registry
     */
    Parser<T> getParser();

    /**
     * Get the item directly registered to the given id
     */
    default T getInput(int id) {
        return getValue(id);
    }

    /**
     * Get the item that the provided input value is mapped to
     */
    default T getOutput(T input) {
        return input;
    }

    /**
     * Test the provided item to see if is equal to the default/fallback item for this registry
     */
    default boolean isDefault(T t) {
        return false;
    }

    interface Writer<T extends RegistryItem> extends Iterable<T> {

        int getOrCreateId(T val);

        int size();
    }

    interface Reader<T extends RegistryItem> {

        T getValue(int id);
    }

    interface Mapper<T extends RegistryItem> {

        T apply(T in);

        String getVersion();
    }

    interface Parser<T extends RegistryItem> {

        T parse(String in) throws ParseException;

        T parse(CompoundTag in) throws ParseException;

        Reader<T> parsePalette(List<Tag<CompoundTag>> list) throws ParseException;
    }
}
