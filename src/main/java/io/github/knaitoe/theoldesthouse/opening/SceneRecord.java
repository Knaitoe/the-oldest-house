package io.github.knaitoe.theoldesthouse.opening;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
/** Reuses the actual server photograph renderer, eight rows per tick. No shader capture. */
public final class SceneRecord {
    private final ServerLevel level;private final SnapshotRenderer.Render render;
    public SceneRecord(ServerLevel l,BlockPos min,BlockPos max,Vec3 eye,Vec3 look){level=l;var target=eye.add(look.scale(12));
        render=new SnapshotRenderer.Render(NavidsonPhoto.sceneOf(l,min,max),new SnapshotRenderer.Camera(eye.x,eye.y,eye.z,look.x,look.y,look.z,70),target.x,target.y,target.z,null,min.asLong());}
    public boolean tick(){return render.step(8);}
    public ItemStack finish(){return NavidsonLetter.createSnapshot(level,render.finish(LevelSnapshotScene.paletteRgb(),LevelSnapshotScene.paletteIds()).pixels());}
}
