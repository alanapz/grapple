package org.grapple.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.grapple.core.Chainable;
import org.grapple.query.QueryField;
import org.grapple.query.RootFetchSet;
import org.grapple.utils.UnexpectedException;
import org.jetbrains.annotations.NotNull;

public final class EntityContextBuilder<T> implements Chainable<EntityContextBuilder<T>> {

    private final Class<T> klazz;

    private final Map<String, String> definitions = new HashMap<>();

    private EntityContextBuilder(@NotNull Class<T> klazz) {
        this.klazz = klazz;
    }

    public static void main(String[] args)
    {

    }

    public void go() {
        if (!klazz.isInterface()) {
            throw new UnexpectedException("Class is not an interface");
        }
    }

    public static <T> T buildContext(@NotNull Class<T> klazz)
    {

    }

    public static <T> EntityContextBuilder<T> entityContextBuilder(Class<T> klazz)
    {

    }


    public interface X {

        RootFetchSet<List<String>> listUsers();

        RootFetchSet<Optional<User>> getUser(int id);
    }

    public interface User {

        QueryField<User, Integer> id();
    }
}
