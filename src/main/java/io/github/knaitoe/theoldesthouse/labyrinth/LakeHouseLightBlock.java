package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The house light (left the lights on): a bulb under a milk-glass shade, hung from a ceiling or stood on a floor, under
 * water too. Hung anywhere in the Overworld by the reader it was given to, it makes that place home: they respawn under it
 * without a bed. It switches itself on as they come home at night, and now and then they hear wet steps near it.
 */
public final class LakeHouseLightBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<LakeHouseLightBlock> CODEC=simpleCodec(LakeHouseLightBlock::new);
    public static final BooleanProperty HANGING=BlockStateProperties.HANGING,LIT=BlockStateProperties.LIT,WATERLOGGED=BlockStateProperties.WATERLOGGED;
    private static final VoxelShape STANDING=box(4,0,4,12,9,12),HUNG=box(4,6,4,12,16,12);
    /** How near its reader must be, at night, for it to switch on. */
    static final int HOME=24;
    public LakeHouseLightBlock(BlockBehaviour.Properties properties){
        super(properties);registerDefaultState(stateDefinition.any().setValue(HANGING,true).setValue(LIT,false).setValue(WATERLOGGED,false));
    }
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HANGING,LIT,WATERLOGGED);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return s.getValue(HANGING)?HUNG:STANDING;}
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext c){
        boolean hung=c.getClickedFace()==Direction.DOWN;
        return defaultBlockState().setValue(HANGING,hung).setValue(WATERLOGGED,c.getLevel().getFluidState(c.getClickedPos()).getType()==Fluids.WATER);
    }
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos at){
        var support=s.getValue(HANGING)?at.above():at.below();return Block.canSupportCenter(l,support,s.getValue(HANGING)?Direction.DOWN:Direction.UP);
    }
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos at,BlockPos np){
        if(s.getValue(WATERLOGGED))l.scheduleTick(at,Fluids.WATER,Fluids.WATER.getTickDelay(l));
        return (d==(s.getValue(HANGING)?Direction.UP:Direction.DOWN))&&!canSurvive(s,l,at)?Blocks.AIR.defaultBlockState():super.updateShape(s,d,n,l,at,np);
    }
    @Override protected FluidState getFluidState(BlockState s){return s.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(s);}
    public static int light(BlockState s){return s.getValue(LIT)?14:0;}

    /** Hung by its own reader in the Overworld, the light makes the place home: they come back here when they die. */
    @Override public void setPlacedBy(Level l,BlockPos at,BlockState s,@Nullable LivingEntity placer,ItemStack stack){
        if(!(placer instanceof ServerPlayer p)||l.dimension()!=Level.OVERWORLD)return;
        var data=stack.get(DataComponents.CUSTOM_DATA);
        if(data!=null&&data.copyTag().hasUUID(LiteraryVignettes.OWNER)&&!data.copyTag().getUUID(LiteraryVignettes.OWNER).equals(p.getUUID()))return;
        var home=home((ServerLevel)l,at,s);if(home==null)return;
        p.setRespawnPosition(l.dimension(),home,p.getYRot(),true,false);
        p.displayClientMessage(Component.literal("You'll come back here now."),true);
    }
    /** Where the reader stands when they come home: under the light, or beside it. */
    static @Nullable BlockPos home(ServerLevel l,BlockPos at,BlockState s){
        var first=s.getValue(HANGING)?at.below(2):at.above();
        for(var c:new BlockPos[]{first,at.below(2),at.north(),at.south(),at.east(),at.west()})
            if(!l.getBlockState(c).isSuffocating(l,c)&&!l.getBlockState(c.above()).isSuffocating(l,c.above())&&!c.equals(at))return c;
        return null;
    }
    @Override protected void onPlace(BlockState s,Level l,BlockPos at,BlockState old,boolean moved){if(!l.isClientSide)l.scheduleTick(at,this,40);}
    /** Every two seconds: on at night when somebody who calls it home is near; and, rarely, steps near it that are not theirs. */
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos at,RandomSource random){
        l.scheduleTick(at,this,40);
        ServerPlayer near=null;
        for(var p:l.players())if(p.distanceToSqr(at.getCenter())<HOME*HOME&&at.equals(homeOf(p,l,at,s)))near=p;
        boolean on=near!=null&&l.isNight();
        if(on!=s.getValue(LIT))l.setBlock(at,s.setValue(LIT,on),Block.UPDATE_ALL);
        if(near!=null&&random.nextInt(60)==0)LakeHouseRules.hear(near,SoundEvents.SLIME_BLOCK_STEP,at.offset(random.nextInt(5)-2,-1,random.nextInt(5)-2),.5F,.7F);
    }
    /** The light whose home a reader's respawn point is, if it is this one. */
    private static @Nullable BlockPos homeOf(ServerPlayer p,ServerLevel l,BlockPos at,BlockState s){
        if(p.getRespawnDimension()!=l.dimension()||p.getRespawnPosition()==null)return null;
        return p.getRespawnPosition().equals(home(l,at,s))?at:null;
    }
}
