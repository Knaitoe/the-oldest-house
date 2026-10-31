package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.labyrinth.BuildBlocks;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthBuilder;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

/**
 * Bounded, named waits for native readiness, in place of fixed delays. A fixture that waits a set number of ticks for a scene
 * passes on a quiet machine and fails on a loaded one; these poll until what the case needs is there, and fail naming what
 * was still missing.
 */
final class SceneReady {
    private SceneReady(){}
    /** Polls {@code missing} every {@code every} ticks; when it answers null runs {@code then}; after {@code maxTicks} fails with its last answer. */
    static void when(GameTestHelper h,Supplier<String> missing,int every,int maxTicks,Runnable then){step(h,missing,every,maxTicks,0,then);}
    private static void step(GameTestHelper h,Supplier<String> missing,int every,int maxTicks,int waited,Runnable then){
        String m=missing.get();
        if(m==null){then.run();return;}
        if(waited>=maxTicks){h.fail("not ready after "+waited+" ticks: "+m);return;}
        h.runAfterDelay(every,()->step(h,missing,every,maxTicks,waited+every,then));
    }
    /** What a box still lacks: a chunk, its entity sections, or construction still writing into the level; null once it is all there. */
    static String unloaded(ServerLevel l,AABB box){
        for(int x=((int)Math.floor(box.minX))>>4;x<=((int)Math.ceil(box.maxX)-1)>>4;x++)for(int z=((int)Math.floor(box.minZ))>>4;z<=((int)Math.ceil(box.maxZ)-1)>>4;z++){
            if(!l.hasChunk(x,z))return "chunk "+x+","+z;
            if(!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return "entity sections of chunk "+x+","+z;
        }
        if(BuildBlocks.isRecording(l))return "a recorded build still writing";
        if(LabyrinthBuilder.isCarving())return "the labyrinth still carving ("+LabyrinthBuilder.describePending()+")";
        return null;
    }
    /** Runs {@code then} once the box is loaded, entity sections included, and nothing is still building into it. */
    static void whenLoaded(GameTestHelper h,ServerLevel l,AABB box,int maxTicks,Runnable then){when(h,()->unloaded(l,box),2,maxTicks,then);}
    /** Runs {@code then} with the entity under a saved id once it is loaded and {@code ready}: on a busy server an entity loads a few ticks after its chunk. */
    static <T extends Entity> void whenEntity(GameTestHelper h,ServerLevel l,Supplier<UUID> id,Class<T> type,Predicate<T> ready,int maxTicks,Consumer<T> then){
        Entity[] found={null};
        when(h,()->{var u=id.get();if(u==null)return "no saved id yet";var e=l.getEntity(u);if(!type.isInstance(e))return type.getSimpleName()+" "+u+" not loaded";if(!ready.test(type.cast(e)))return type.getSimpleName()+" "+u+" not ready";found[0]=e;return null;},5,maxTicks,()->then.accept(type.cast(found[0])));
    }
}
