package io.github.knaitoe.theoldesthouse.house;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 0.4.704 (docs/FAVORITE_ITEM_0_4_704.md): what a reader has left on the hall stand, standing in its well. Only that reader is sent
 * it, so two readers at the stand each see their own thing there and not the other's; spectators see nothing on it. It is never
 * saved: the House puts it back whenever its reader is in the manor (HouseKeeping's tick), and takes it away when the stand is empty.
 * <p>
 * How each thing stands is {@link #look}: a long thing (a sword, a tool, a bow, a trident, a rod) tip down in the tray with its
 * handle above the rail, as a walking stick stands in an umbrella stand, flat to the wall so it is seen whole from the hall; a
 * shield upright in the tray; a block, or anything small, standing on the tray's floor.
 */
public final class HallStandDisplay extends Display.ItemDisplay {
    @Nullable private UUID reader;
    private ItemStack shown=ItemStack.EMPTY;
    /** Each reader's display, by reader. */
    private static final Map<UUID,UUID> SHOWN=new HashMap<>();

    public HallStandDisplay(EntityType<? extends Display.ItemDisplay> type,Level level){super(type,level);}
    public @Nullable UUID reader(){return reader;}
    public void reader(UUID id){reader=id;}
    public ItemStack shown(){return shown;}
    @Override public boolean broadcastToPlayer(ServerPlayer player){return reader!=null&&reader.equals(player.getUUID())&&!player.isSpectator()&&super.broadcastToPlayer(player);}
    @Override public boolean shouldBeSaved(){return false;}

    /** Where in the stand's block its well is, for a stand facing this way: the well's middle in the model drawn facing north, turned. */
    public static double[] well(Direction facing){
        double x=8/16.0,z=9/16.0;
        return switch(facing){
            case EAST->new double[]{1-z,x};
            case SOUTH->new double[]{1-x,1-z};
            case WEST->new double[]{z,1-x};
            default->new double[]{x,z};
        };
    }

    /** The reader's display made, changed or taken away to match what they have on the stand. Only while the stand's chunk is loaded. */
    public static void show(ServerPlayer p){
        var origin=HouseSavedData.get(p.server).houseOrigin();var level=p.server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||level==null)return;
        var at=origin.offset(HouseKeeping.STAND);
        if(!level.isLoaded(at)||!level.areEntitiesLoaded(ChunkPos.asLong(at)))return;
        var keep=HouseKeeping.of(p);
        var id=SHOWN.get(p.getUUID());var existing=id==null?null:level.getEntity(id) instanceof HallStandDisplay d&&d.isAlive()?d:null;
        var state=level.getBlockState(at);
        boolean standing=state.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&state.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.HALL_STAND;
        if(keep.kept.isEmpty()||!standing){if(existing!=null)existing.discard();SHOWN.remove(p.getUUID());return;}
        if(existing!=null&&ItemStack.matches(existing.shown,keep.kept))return;
        if(existing!=null)existing.discard();
        var made=HouseSitting.STAND_DISPLAY.get().create(level);if(made==null)return;
        made.reader=p.getUUID();made.present(keep.kept,state.getValue(HouseholdFurnitureBlock.FACING));
        var w=well(state.getValue(HouseholdFurnitureBlock.FACING));
        made.moveTo(at.getX()+w[0],at.getY(),at.getZ()+w[1],0,0);
        if(level.addFreshEntity(made))SHOWN.put(p.getUUID(),made.getUUID());
    }
    /** Forgets every display (tests). */
    public static void clearAll(){SHOWN.clear();}

    /** How a thing stands in the well. */
    enum Stance{LONG,SHIELD,BLOCK,SMALL}
    static Stance stance(ItemStack thing){
        var item=thing.getItem();
        if(item instanceof ShieldItem)return Stance.SHIELD;
        if(item instanceof BlockItem)return Stance.BLOCK;
        if(item instanceof TieredItem||item instanceof ProjectileWeaponItem||item instanceof TridentItem||item instanceof MaceItem||item instanceof FishingRodItem
            ||item instanceof FoodOnAStickItem||item instanceof BrushItem||item instanceof SpyglassItem||thing.is(Items.STICK)||thing.is(Items.BLAZE_ROD)||thing.is(Items.BREEZE_ROD)||thing.is(Items.BONE))return Stance.LONG;
        return Stance.SMALL;
    }
    /** Which of the game's ways of holding an item the display uses, and where it stands in the display's own space. */
    public record Look(ItemDisplayContext context,Transformation transform){}
    /**
     * How the thing stands in the well of a stand facing this way. The game turns every item display half round before it draws it
     * (DisplayRenderer.ItemDisplayRenderer); the item frame's way of holding a thing ({@code fixed}) turns it back, so a flat thing is
     * drawn face on as it is in a frame, and the trident and spyglass are drawn as their flat pictures. A block is held as it is
     * ({@code none}) and turned back here, so its face is to the hall.
     */
    public static Look look(ItemStack thing,Direction facing){
        float face=(float)Math.toRadians(-facing.toYRot());
        var stance=stance(thing);
        float size=switch(stance){case LONG->.7F;case SHIELD->.6F;case SMALL->.4F;case BLOCK->.3F;};
        // LONG: the picture's top corner (the blade's or the head's) turned down, so the thing stands on it; the half diagonal of a
        // picture at .7 is .49, so the tip stands a pixel above the tray's floor and the handle above the rail.
        float lift=switch(stance){case LONG->1/16F+.47F;case SHIELD->.5F/16+.23F;case SMALL->.5F/16+.2F;case BLOCK->.5F/16+size/2;};
        var turn=new Quaternionf().rotateY(stance==Stance.BLOCK?face+(float)Math.PI:face);
        if(stance==Stance.LONG)turn.rotateZ((float)(-3*Math.PI/4));
        return new Look(stance==Stance.BLOCK?ItemDisplayContext.NONE:ItemDisplayContext.FIXED,new Transformation(new Vector3f(0,lift,0),turn,new Vector3f(size,size,size),new Quaternionf()));
    }

    /** The thing, standing as {@link #look} has it. */
    public void present(ItemStack thing,Direction facing){
        shown=thing.copyWithCount(1);
        var look=look(shown,facing);
        var data=saveWithoutId(new CompoundTag());data.put("item",shown.save(level().registryAccess()));data.putString("item_display",look.context().getSerializedName());
        data.put("transformation",Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE,look.transform()).getOrThrow());
        load(data);
    }

    /** Where the stand is, in the manor, for a check that its display is on it. */
    public static @Nullable BlockPos stand(ServerLevel level){var origin=HouseSavedData.get(level.getServer()).houseOrigin();return origin==null?null:origin.offset(HouseKeeping.STAND);}
}
