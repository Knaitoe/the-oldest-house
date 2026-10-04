package io.github.knaitoe.theoldesthouse.client.mixin;

import com.mojang.blaze3d.vertex.VertexBuffer;
import io.github.knaitoe.theoldesthouse.client.LazySectionBuffers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Stream;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** See {@link LazySectionBuffers}: no GPU buffers for render sections that hold only air. */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class LazySectionBuffersMixin {
    @Shadow @Final private Map<RenderType, VertexBuffer> buffers;

    @Shadow public abstract BlockPos getOrigin();

    /** Replaces only the eager per-layer buffer map; any other collection in the constructor is untouched. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;"))
    private Object theOldestHouse$deferBuffers(Stream stream, Collector collector) {
        List<?> items = stream.toList();
        if (LazySectionBuffers.isLayerList(items)) return new ConcurrentHashMap<RenderType, VertexBuffer>();
        return items.stream().collect(collector);
    }

    /** Compilation is scheduled on the render thread: create buffers there, and only for sections with blocks. */
    @Inject(method = "createCompileTask", at = @At("HEAD"))
    private void theOldestHouse$prepareBuffers(RenderRegionCache cache, CallbackInfoReturnable<?> cir) {
        if (buffers.isEmpty() && LazySectionBuffers.holdsBlocks(getOrigin())) LazySectionBuffers.ensure(buffers);
    }

    /** Any other caller still receives a real buffer. */
    @Inject(method = "getBuffer", at = @At("HEAD"))
    private void theOldestHouse$bufferOnDemand(RenderType type, CallbackInfoReturnable<VertexBuffer> cir) {
        if (!buffers.containsKey(type)) LazySectionBuffers.ensure(buffers);
    }

    @Inject(method = "releaseBuffers", at = @At("HEAD"))
    private void theOldestHouse$countRelease(CallbackInfo ci) {
        LazySectionBuffers.released(buffers);
    }

    /** Closed buffers are never reused; a section used again creates fresh ones. */
    @Inject(method = "releaseBuffers", at = @At("TAIL"))
    private void theOldestHouse$forgetReleased(CallbackInfo ci) {
        buffers.clear();
    }
}
