package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;

/**
 * Vanilla 1.21.1 gives every render section five GPU vertex buffers up front.
 * The House interior is 4,064 blocks tall, so at an ordinary render distance
 * the client holds about 159,000 sections there, nearly all of them empty air,
 * and leaving the House deletes millions of GPU objects in one frame. Sections
 * now receive buffers only once they hold blocks, created on the render thread
 * when their compilation is scheduled.
 */
public final class LazySectionBuffers {
    private static final AtomicInteger LIVE = new AtomicInteger();
    private static final String SECTION = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection";
    private static boolean warned;

    private LazySectionBuffers() {
    }

    /** Live section vertex buffers, for transition timing logs. */
    public static int live() {
        return LIVE.get();
    }

    /** Whether a section can produce geometry: only its own blocks are meshed. */
    public static boolean holdsBlocks(BlockPos origin) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return true;
        int index = level.getSectionIndex(origin.getY());
        if (index < 0 || index >= level.getSectionsCount()) return false;
        var chunk = level.getChunkSource().getChunk(origin.getX() >> 4, origin.getZ() >> 4, false);
        if (chunk == null) return true;
        return !chunk.getSection(index).hasOnlyAir();
    }

    /** Creates the section's buffers on the render thread; a worker waits for the render thread instead. */
    public static void ensure(Map<RenderType, VertexBuffer> buffers) {
        if (!buffers.isEmpty()) return;
        if (RenderSystem.isOnRenderThread()) {
            create(buffers);
            return;
        }
        if (!warned) {
            warned = true;
            TheOldestHouse.LOGGER.warn("A render section needed vertex buffers off the render thread; creating them there.");
        }
        Minecraft.getInstance().submit(() -> create(buffers)).join();
    }

    private static synchronized void create(Map<RenderType, VertexBuffer> buffers) {
        if (!buffers.isEmpty()) return;
        for (RenderType type : RenderType.chunkBufferLayers()) {
            buffers.put(type, new VertexBuffer(VertexBuffer.Usage.STATIC));
            LIVE.incrementAndGet();
        }
    }

    public static void released(Map<RenderType, VertexBuffer> buffers) {
        LIVE.addAndGet(-buffers.size());
    }

    /** True for the stream that vanilla collects into a section's eager buffer map. */
    public static boolean isLayerList(List<?> items) {
        return items.equals(RenderType.chunkBufferLayers());
    }

    /**
     * Builds one real section outside any level: it must start without buffers,
     * create all layers when a caller asks for one, and release every one of them.
     */
    private static void exerciseSection(Class<?> section) throws ReflectiveOperationException {
        var unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        var unsafe = unsafeField.get(null);
        Object dispatcher = unsafe.getClass().getMethod("allocateInstance", Class.class)
                .invoke(unsafe, net.minecraft.client.renderer.chunk.SectionRenderDispatcher.class);
        var constructor = section.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object[] args = new Object[constructor.getParameterCount()];
        Class<?>[] types = constructor.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            args[i] = types[i] == int.class ? 0 : types[i] == long.class ? 0L : dispatcher;
        }
        Object created = constructor.newInstance(args);
        java.lang.reflect.Field map = null;
        for (var field : section.getDeclaredFields()) if (Map.class.isAssignableFrom(field.getType())) map = field;
        if (map == null) throw new IllegalStateException("Render section has no buffer map");
        map.setAccessible(true);
        int before = live();
        if (!((Map<?, ?>) map.get(created)).isEmpty()) throw new IllegalStateException("A new render section still allocates GPU buffers eagerly");
        section.getMethod("getBuffer", RenderType.class).invoke(created, RenderType.solid());
        int layers = RenderType.chunkBufferLayers().size();
        if (live() - before != layers || ((Map<?, ?>) map.get(created)).size() != layers)
            throw new IllegalStateException("Asking for a buffer did not create every layer: " + (live() - before));
        section.getMethod("releaseBuffers").invoke(created);
        if (live() != before || !((Map<?, ?>) map.get(created)).isEmpty())
            throw new IllegalStateException("Released section buffers are still counted or held");
    }

    /** Fails the client proof run if the mixin did not apply to the real render section class. */
    public static void verifyInstalled() {
        try {
            Class<?> section = Class.forName(SECTION);
            boolean installed = Arrays.stream(section.getDeclaredMethods())
                    .anyMatch(method -> method.getName().contains("theOldestHouse$prepareBuffers"));
            if (!installed) throw new IllegalStateException("Lazy section buffers are not installed on " + SECTION);
            exerciseSection(section);
            TheOldestHouse.LOGGER.info("LAZY SECTION BUFFER CHECK: installed on {}; a new section holds no GPU buffers until asked", SECTION);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not exercise the native render section", exception);
        }
    }
}
