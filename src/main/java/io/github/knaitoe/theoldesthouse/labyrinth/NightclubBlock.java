package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
/** Dark architectural surfaces and actual, selectable music controls. */
public final class NightclubBlock extends Block {
    public static final MapCodec<NightclubBlock> CODEC=simpleCodec(NightclubBlock::new);
    public enum Kind implements StringRepresentable{WALL,FLOOR,CEILING,TRIM,SPEAKER,MIXER,BAR,GLASS,STRIP,SPOT,BENCH,STROBE;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}public boolean cube(){return ordinal()<4;}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    public NightclubBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.WALL).setValue(FACING,Direction.NORTH).setValue(STAGE,1));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,STAGE);}
    public static BlockState of(Kind k,Direction d){return NightclubRegistry.BLOCK.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
    public static int light(BlockState s){return s.getValue(KIND)==Kind.STROBE?(s.getValue(STAGE)>0?15:0):s.getValue(KIND)==Kind.SPOT||s.getValue(KIND)==Kind.STRIP?15:s.getValue(KIND)==Kind.MIXER&&s.getValue(STAGE)>0?2:0;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){case SPEAKER->Block.box(1,0,1,15,32,15);case MIXER->Block.box(1,0,1,15,9,15);case BAR->Block.box(0,0,0,16,16,16);case GLASS->Block.box(5,0,5,11,6,11);case STRIP,SPOT,STROBE->Block.box(0,14,0,16,16,16);case BENCH->Block.box(0,0,0,16,17,16);default->Shapes.block();};}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.GLASS||s.getValue(KIND)==Kind.STRIP||s.getValue(KIND)==Kind.SPOT||s.getValue(KIND)==Kind.STROBE?Shapes.empty():getShape(s,l,p,c);}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?Shapes.block():Shapes.empty();}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return s.getValue(KIND).cube()?15:0;}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return !s.getValue(KIND).cube();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player user,BlockHitResult hit){
        if(l.isClientSide)return s.getValue(KIND).cube()?InteractionResult.PASS:InteractionResult.SUCCESS;
        if(!(user instanceof ServerPlayer p)||!Nightclub.inside(p)||!p.isAlive()||p.isSpectator()||p.distanceToSqr(at.getCenter())>25)return InteractionResult.PASS;
        if(s.getValue(KIND)==Kind.MIXER){int level=(s.getValue(STAGE)+1)%3;l.setBlock(at,s.setValue(STAGE,level),3);p.displayClientMessage(net.minecraft.network.chat.Component.literal(level==0?"The speakers fall silent. Feet keep moving.":level==2?"The bass drops low. The dancers keep their pace.":"The bass returns."),true);return InteractionResult.SUCCESS;}
        if(s.getValue(KIND)==Kind.SPEAKER){
            var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();
            var mixer=l.getBlockState(LabyrinthPlaces.base(origin,LabyrinthPlace.NIGHTCLUB).offset(Nightclub.MIXER));
            boolean audible=mixer.is(NightclubRegistry.BLOCK.get())&&mixer.getValue(STAGE)>0;
            if(audible)l.playSound(null,at,net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASEDRUM.value(),net.minecraft.sounds.SoundSource.RECORDS,.4F,.6F);
            p.displayClientMessage(net.minecraft.network.chat.Component.literal(audible?"The cabinet pulses beneath your hand.":"The cabinet is still. The feet behind you are not."),true);return InteractionResult.SUCCESS;
        }
        if(s.getValue(KIND)==Kind.GLASS){
            // Removing the actual block before transferring it makes a second reader's click harmless.
            if(!l.getBlockState(at).equals(s)||!l.setBlock(at,Blocks.AIR.defaultBlockState(),3))return InteractionResult.PASS;
            var glass=NightclubGlassItem.glass(s.getValue(STAGE)==0);
            if(!p.getInventory().add(glass))p.drop(glass,false);
            l.playSound(null,at,net.minecraft.sounds.SoundEvents.GLASS_PLACE,net.minecraft.sounds.SoundSource.BLOCKS,.25F,1.3F);return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
