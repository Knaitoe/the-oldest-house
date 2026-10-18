package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * Winter Lake's ice (0.4.104). Black ice is clear: it rings underfoot and you can see down through it. Snow-covered ice is
 * opaque and quiet. CRACKED and SPLIT are the two stages of a crack she knocks into it from beneath. The frozen cattails
 * ring the hole that never froze, and the boys' heap of snowballs gives one to anyone who reaches into it.
 */
public final class WinterPropBlock extends Block {
    public static final MapCodec<WinterPropBlock> CODEC=simpleCodec(WinterPropBlock::new);
    public enum Kind implements StringRepresentable {
        BLACK_ICE,SNOW_ICE,CRACKED,SPLIT,CATTAILS,SNOWBALLS;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        /** The four that make up the frozen surface. */
        public boolean ice(){return this==BLACK_ICE||this==SNOW_ICE||this==CRACKED||this==SPLIT;}
        /** Ice that rings underfoot and shows what is under it. */
        public boolean clear(){return this==BLACK_ICE||this==CRACKED||this==SPLIT;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public WinterPropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.BLACK_ICE));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND);}
    public static BlockState of(Kind k){return WinterLakeRegistry.PROP.get().defaultBlockState().setValue(KIND,k);}
    public static @Nullable Kind kind(BlockState s){return s.is(WinterLakeRegistry.PROP.get())?s.getValue(KIND):null;}
    public static boolean ice(BlockState s){var k=kind(s);return k!=null&&k.ice();}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        return switch(s.getValue(KIND)){case CATTAILS->box(3,0,3,13,14,13);case SNOWBALLS->box(2,0,2,14,7,14);default->Shapes.block();};
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        return s.getValue(KIND)==Kind.CATTAILS?Shapes.empty():getShape(s,l,at,c);
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos at){return s.getValue(KIND)!=Kind.SNOW_ICE;}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos at){return switch(s.getValue(KIND)){case SNOW_ICE->15;case CATTAILS,SNOWBALLS->0;default->1;};}
    @Override protected float getShadeBrightness(BlockState s,BlockGetter l,BlockPos at){return s.getValue(KIND)==Kind.SNOW_ICE?super.getShadeBrightness(s,l,at):1;}
    /** One sheet of ice: the faces between two of its cells are never drawn. */
    @Override protected boolean skipRendering(BlockState s,BlockState adjacent,Direction d){return s.getValue(KIND).ice()&&ice(adjacent)||super.skipRendering(s,adjacent,d);}
    @Override public float getFriction(BlockState s,LevelReader l,BlockPos at,@Nullable Entity e){return s.getValue(KIND).clear()?.98F:.6F;}
    @Override public SoundType getSoundType(BlockState s,LevelReader l,BlockPos at,@Nullable Entity e){
        return switch(s.getValue(KIND)){case SNOW_ICE,SNOWBALLS->SoundType.SNOW;case CATTAILS->SoundType.GRASS;default->SoundType.GLASS;};
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player player,BlockHitResult hit){
        if(s.getValue(KIND)!=Kind.SNOWBALLS)return InteractionResult.PASS;
        if(player instanceof ServerPlayer p)WinterLake.scoop(p);
        return InteractionResult.sidedSuccess(l.isClientSide());
    }
    /** Reaching into the heap with a snowball already in hand takes another; it never throws the one held. */
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos at,Player player,InteractionHand hand,BlockHitResult hit){
        if(s.getValue(KIND)!=Kind.SNOWBALLS)return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(player instanceof ServerPlayer p)WinterLake.scoop(p);
        return ItemInteractionResult.sidedSuccess(l.isClientSide());
    }
}
