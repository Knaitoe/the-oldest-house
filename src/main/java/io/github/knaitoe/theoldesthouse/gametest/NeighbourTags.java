package io.github.knaitoe.theoldesthouse.gametest;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

/**
 * For the cases: puts a registered thing in a tag, or takes it out, as a neighbour's jar would list it there (docs/NEIGHBOURS.md). Only
 * the thing's own holder learns of it, which is what {@code is(tag)} asks; a reload of the data packs puts every tag back. A case puts
 * back what it changed in a {@code finally} or an {@code @AfterBatch} hook.
 */
final class NeighbourTags {
    private NeighbourTags() {
    }

    /** Puts {@code holder} in {@code tag}, or takes it out. */
    static <T> void bind(Holder.Reference<T> holder, TagKey<T> tag, boolean on) {
        var tags = holder.tags().collect(Collectors.toCollection(HashSet::new));
        if (on) {
            tags.add(tag);
        } else {
            tags.remove(tag);
        }
        try {
            Method bind = Holder.Reference.class.getDeclaredMethod("bindTags", Collection.class);
            bind.setAccessible(true);
            bind.invoke(holder, tags);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Runs {@code body} with {@code holder} in {@code tag} or out of it, and puts it back as it was. */
    static <T> void listed(Holder.Reference<T> holder, TagKey<T> tag, boolean on, Runnable body) {
        boolean was = holder.is(tag);
        bind(holder, tag, on);
        try {
            body.run();
        } finally {
            bind(holder, tag, was);
        }
    }
}
