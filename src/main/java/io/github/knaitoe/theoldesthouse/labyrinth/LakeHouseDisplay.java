package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.math.Transformation;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block shown in the lake house, either to everyone (the swaying half door, the fire in the kitchen hearth) or to one
 * reader alone (a piece of the house lifting when they ask how, and later the dresses and the figure). The vanilla block
 * display draws it; this only decides who receives it.
 */
public final class LakeHouseDisplay extends Display.BlockDisplay {
    public static final String TAG="LakeHouseProp";
    private @Nullable UUID owner;
    public LakeHouseDisplay(EntityType<?> type,Level level){super(type,level);}

    /** Shown only to its reader, if it has one. */
    @Override public boolean broadcastToPlayer(ServerPlayer player){return owner==null||owner.equals(player.getUUID());}
    public @Nullable UUID owner(){return owner;}

    @Override protected void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(owner!=null)tag.putUUID("Owner",owner);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;}

    /** A display of this block at this cell, for everyone or for one reader. */
    public static @Nullable LakeHouseDisplay show(ServerLevel l,BlockPos at,BlockState state,@Nullable ServerPlayer only){
        var d=LakeHouseRegistry.DISPLAY.get().create(l);if(d==null)return null;
        d.owner=only==null?null:only.getUUID();d.addTag(TAG);
        d.moveTo(at.getX(),at.getY(),at.getZ(),0,0);
        var tag=d.saveWithoutId(new CompoundTag());tag.put("block_state",NbtUtils.writeBlockState(state));d.load(tag);
        return l.addFreshEntity(d)?d:null;
    }
    /** Moves the display to a new pose, gliding there over the given ticks. */
    public void pose(Transformation to,int ticks){
        var parts=new Transformation(to.getTranslation(),to.getLeftRotation(),to.getScale(),to.getRightRotation());
        var tag=saveWithoutId(new CompoundTag());
        tag.put("transformation",Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE,parts).getOrThrow());
        tag.putInt("interpolation_duration",ticks);tag.putInt("start_interpolation",0);
        load(tag);
    }
}
