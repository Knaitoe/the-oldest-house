package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/**
 * The night ward's bed curtains (0.4.199): pleated cloth hung two blocks high from a ceiling track on drop rods, joining the
 * walls and each other like panes. Where a curtain is pulled back the track runs on alone. They are cloth: anyone walks
 * through them, with a rustle, and nobody is ever shut in by one.
 *
 * <p>0.4.590 (owner: "hospital curtains need improvement"): a curtain pulled back hangs bunched at the end of its run
 * ({@link Gather}), toward the lower coordinate or the higher along the track. A bunched curtain and a bare track are only as
 * big to the hand as they are to the eye: before, the track over an open bay answered to a pointer across the whole opening,
 * and a reader could not reach the bed behind it.
 */
public final class NightWardCurtainBlock extends IronBarsBlock {
    public static final MapCodec<NightWardCurtainBlock> CODEC=simpleCodec(NightWardCurtainBlock::new);
    public enum Part implements StringRepresentable {
        LOWER,UPPER,RAIL;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public enum Gather implements StringRepresentable {
        NONE,NEG,POS;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    public static final EnumProperty<Gather> GATHER=EnumProperty.create("gather",Gather.class);
    private static final java.util.Map<BlockState,VoxelShape> OWN=new java.util.concurrent.ConcurrentHashMap<>();
    public NightWardCurtainBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PART,Part.LOWER).setValue(GATHER,Gather.NONE));}
    @Override public MapCodec<? extends IronBarsBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(PART,GATHER);}
    public static BlockState of(Part part){return NovelRegistry.NIGHT_WARD_CURTAIN.get().defaultBlockState().setValue(PART,part);}
    public static BlockState of(Part part,Gather gather){return of(part).setValue(GATHER,gather);}
    /** Drawn cloth is a pane's shape; a bare track and a bunched curtain are their own. */
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return s.getValue(PART)!=Part.RAIL&&s.getValue(GATHER)==Gather.NONE?super.getShape(s,l,p,c):OWN.computeIfAbsent(s,NightWardCurtainBlock::own);
    }
    private static VoxelShape own(BlockState s){
        var part=s.getValue(PART);var out=Shapes.empty();
        if(part!=Part.LOWER){
            out=Block.box(7,15,7,9,16,9);
            if(s.getValue(NORTH))out=Shapes.or(out,Block.box(7,15,0,9,16,7));
            if(s.getValue(SOUTH))out=Shapes.or(out,Block.box(7,15,9,9,16,16));
            if(s.getValue(WEST))out=Shapes.or(out,Block.box(0,15,7,7,16,9));
            if(s.getValue(EAST))out=Shapes.or(out,Block.box(9,15,7,16,16,9));
        }
        double y0=part==Part.LOWER?3:0,y1=part==Part.LOWER?16:15;
        // As the model has it: the run lies north and south where the curtain joins either way or joins nothing, east and west
        // where it joins either of those, and the cloth hangs at the run's low end (neg) or its high end (pos).
        boolean northSouth=s.getValue(NORTH)||s.getValue(SOUTH)||!s.getValue(EAST)&&!s.getValue(WEST),eastWest=s.getValue(EAST)||s.getValue(WEST);
        switch(s.getValue(GATHER)){
            case NEG->{if(northSouth)out=Shapes.or(out,Block.box(6,y0,0,10,y1,5));if(eastWest)out=Shapes.or(out,Block.box(0,y0,6,5,y1,10));}
            case POS->{if(northSouth)out=Shapes.or(out,Block.box(6,y0,11,10,y1,16));if(eastWest)out=Shapes.or(out,Block.box(11,y0,6,16,y1,10));}
            default->{}
        }
        return out;
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected void entityInside(BlockState s,Level l,BlockPos pos,Entity e){
        if(!l.isClientSide&&s.getValue(PART)==Part.LOWER&&s.getValue(GATHER)==Gather.NONE&&e instanceof Player p&&!p.isSpectator()&&p.tickCount%12==0)
            l.playSound(null,pos,SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.BLOCKS,.3F,1.3F);
    }
}
