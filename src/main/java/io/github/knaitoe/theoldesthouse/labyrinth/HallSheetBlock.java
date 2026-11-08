package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.390, the laundry corridor (OrdinaryHalls, docs/LAUNDRY_CORRIDOR_0_4_390.md): washing hung on a line, two blocks high, drawn
 * by tools/draw_ordinary_hall_art.py. The upper block holds the line and the top of the cloth; the lower block the rest, and the
 * swing. Nothing about it stops a body or a hand: it has no collision and no outline, so a door behind it can still be reached.
 *
 * <p>{@link #AXIS} is the way the line runs. A body walking into the cloth pushes it ahead of them ({@link #SWAY} 1, or 4 the other
 * way); once they are through it swings back past where it hung (2 or 5) and settles (3 or 6, then 0). Its neighbours on the line
 * stir a little. The line and the bare stretches of it never move.
 */
public final class HallSheetBlock extends Block {
    public static final MapCodec<HallSheetBlock> CODEC=simpleCodec(HallSheetBlock::new);
    public enum Kind implements StringRepresentable {
        /** A plain sheet, to a hand's width off the floor. */
        LINEN,
        /** A striped ticking cover, as long. */
        TICKING,
        /** A towel, the top half only: a gap under it at knee height. */
        TOWEL,
        /** The line and nothing on it. */
        BARE;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean cloth(){return this!=BARE;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final EnumProperty<Direction.Axis> AXIS=BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty UPPER=BooleanProperty.create("upper");
    public static final IntegerProperty SWAY=IntegerProperty.create("sway",0,6);
    /** Ticks between one stage of the swing and the next. */
    public static final int STEP=5;

    public HallSheetBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.LINEN).setValue(AXIS,Direction.Axis.X).setValue(UPPER,false).setValue(SWAY,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,AXIS,UPPER,SWAY);}
    public static BlockState of(Kind kind,Direction.Axis axis,boolean upper){return OrdinaryHallRegistry.SHEET.get().defaultBlockState().setValue(KIND,kind).setValue(AXIS,axis).setValue(UPPER,upper);}
    public static boolean is(BlockState s){return s.is(OrdinaryHallRegistry.SHEET.get());}

    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}

    @Override protected void entityInside(BlockState s,Level level,BlockPos pos,Entity entity){
        if(level instanceof ServerLevel l&&entity instanceof LivingEntity)push(l,pos,entity);
    }
    /**
     * A body in the cloth at {@code pos} (either block): it is pushed away from the side the body came in on, and held there while
     * the body stays in it. Public for the tests, whose readers are never ticked.
     */
    public static void push(ServerLevel l,BlockPos pos,Entity body){
        var s=l.getBlockState(pos);if(!is(s))return;
        BlockPos lower=s.getValue(UPPER)?pos.below():pos;var low=l.getBlockState(lower);
        if(!is(low)||!low.getValue(KIND).cloth())return;
        var axis=low.getValue(AXIS);
        // The cloth hangs across the middle of its block. A body on the south (or west) side of it pushes it north (or east).
        // In the model, sways 1 to 3 start toward +z, which a quarter turn makes -x for a line running north to south.
        double side=axis==Direction.Axis.X?body.getZ()-(lower.getZ()+.5):-(body.getX()-(lower.getX()+.5));
        int sway=side>0?4:1;
        int was=low.getValue(SWAY);
        if(was==1||was==4){l.scheduleTick(lower,low.getBlock(),STEP);return;}
        set(l,lower,sway);
        if(was==0)l.playSound(null,lower.above(),SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.BLOCKS,.3F,1.25F+l.random.nextFloat()*.15F);
        // The next sheets along the line stir.
        for(int d:new int[]{-1,1}){
            var next=axis==Direction.Axis.X?lower.offset(d,0,0):lower.offset(0,0,d);var n=l.getBlockState(next);
            if(is(n)&&!n.getValue(UPPER)&&n.getValue(AXIS)==axis&&n.getValue(KIND).cloth()&&n.getValue(SWAY)==0)set(l,next,sway==1?3:6);
        }
    }
    /** Sets the swing of the cloth whose lower block is {@code lower}, both blocks, and when it next moves on. */
    private static void set(ServerLevel l,BlockPos lower,int sway){
        var low=l.getBlockState(lower);l.setBlock(lower,low.setValue(SWAY,sway),Block.UPDATE_CLIENTS);
        var up=l.getBlockState(lower.above());if(is(up)&&up.getValue(UPPER))l.setBlock(lower.above(),up.setValue(SWAY,sway),Block.UPDATE_CLIENTS);
        if(sway!=0)l.scheduleTick(lower,low.getBlock(),STEP);
    }
    /** Whether a living body still stands in the cloth. */
    private static boolean held(ServerLevel l,BlockPos lower){
        return !l.getEntitiesOfClass(LivingEntity.class,new AABB(lower).expandTowards(0,1,0),LivingEntity::isAlive).isEmpty();
    }
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos pos,RandomSource random){step(l,pos);}
    /** One stage of the swing, as its scheduled tick takes it: held while a body is in it, then back, then settling. */
    public static void step(ServerLevel l,BlockPos lower){
        var s=l.getBlockState(lower);if(!is(s)||s.getValue(UPPER))return;
        int sway=s.getValue(SWAY);if(sway==0)return;
        if((sway==1||sway==4)&&held(l,lower)){l.scheduleTick(lower,s.getBlock(),STEP);return;}
        set(l,lower,switch(sway){case 1->2;case 2->3;case 4->5;case 5->6;default->0;});
    }
}
