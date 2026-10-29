package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class HouseReadPropBlock extends BaseEntityBlock {
    public enum Part implements StringRepresentable {
        WOOL,TRENCH,BARN,WOOL_BOOTS,TRENCH_BOOTS,BARN_BOOTS,TOWEL,MIRROR,MIRROR_TOP,UMBRELLA,RADIATOR,CURTAIN,RAIL,SWING,MAILBOX,PLATE,COVERED_MIRROR,
        CLOCK_BASE,CLOCK_FACE,BELL,CALENDAR,RECEPTION,CRT,COOLER,PAPER,MAGAZINES,GLASSES,PHONE,ARMCHAIR,
        SINK,TAP,CABINET,ROUTINE_A,ROUTINE_B,ROUTINE_C,ROUTINE_D,LIGHT_SWITCH,POSTCARD,JUKEBOX,STRING_LIGHT,ROWBOAT,BOTTLE,ASHTRAY,
        // 0.4.160, the owner's playtest notes: the quiet rooms' own furniture, fixtures and lights.
        TABLE_END,TABLE_MID,SIGN,WALL_CLOCK,OUTLET,CEILING_PANEL,VANITY,TUB_HEAD,TUB_FOOT,TOILET,TOWEL_BAR,VANITY_LIGHT,BATH_MAT,EMPTY_HANGER,SCARF_HOOK,BENCH,PENDANT,RUNNER,PORCH_LIGHT,CHAIR,LAMP,DECK_LANTERN,SIDE_TABLE,
        // 0.4.167: sixteen routine tools, two to a block, need eight blocks.
        ROUTINE_E,ROUTINE_F,ROUTINE_G,ROUTINE_H,
        // 0.4.168: the kitchen table. The two halves of the crayon tin, the drawing, the fridge and the radio.
        TIN_LEFT,TIN_RIGHT,SHEET,FRIDGE,FRIDGE_TOP,RADIO,
        // 0.4.169: the wardrobe. Nine more coats on the hall's hooks and three more pairs of boots on its mat (HouseReadWardrobe).
        RINGMASTER,PEA_COAT,CHORE_COAT,SWING_COAT,COLLARED_COAT,RAIN_MAC,PATCHED_JACKET,DUFFEL,LONG_CARDIGAN,RIDING_BOOTS,GALOSHES,PLIMSOLLS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final MapCodec<HouseReadPropBlock> CODEC=simpleCodec(HouseReadPropBlock::new);
    /** The routine blocks (0.4.167), each holding two tools: block {@code i} holds {@link HouseReadMoments#PAIRS}{@code [i]}. */
    public static final java.util.List<Part> ROUTINE_BLOCKS=java.util.List.of(Part.ROUTINE_A,Part.ROUTINE_B,Part.ROUTINE_C,Part.ROUTINE_D,Part.ROUTINE_E,Part.ROUTINE_F,Part.ROUTINE_G,Part.ROUTINE_H);
    public static int routine(Part part){return ROUTINE_BLOCKS.indexOf(part);}
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    public static final IntegerProperty LIFTED=IntegerProperty.create("lifted",0,3);
    public static final BooleanProperty TURNED=BooleanProperty.create("turned"),WRITING=BooleanProperty.create("writing");
    public HouseReadPropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PART,Part.WOOL).setValue(HorizontalDirectionalBlock.FACING,net.minecraft.core.Direction.SOUTH).setValue(TURNED,false).setValue(WRITING,false).setValue(LIFTED,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART,HorizontalDirectionalBlock.FACING,TURNED,WRITING,LIFTED);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Fitting(p,s);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return HouseReadGeometry.shape(s);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(PART)){
        // 0.4.166: the coatroom's curtain stops a body until it parts (turned), when the room is complete.
        case CURTAIN->s.getValue(TURNED)?Shapes.empty():Shapes.block();
        case TIN_LEFT,TIN_RIGHT,SHEET,RADIO,PLATE,TOWEL,PAPER,MAGAZINES,GLASSES,PHONE,ROUTINE_A,ROUTINE_B,ROUTINE_C,ROUTINE_D,ROUTINE_E,ROUTINE_F,ROUTINE_G,ROUTINE_H,LIGHT_SWITCH,POSTCARD,TAP,BELL,CALENDAR,STRING_LIGHT,BOTTLE,SIGN,WALL_CLOCK,OUTLET,BATH_MAT,RUNNER,VANITY_LIGHT,TOWEL_BAR,EMPTY_HANGER,SCARF_HOOK,PENDANT,PORCH_LIGHT,DECK_LANTERN,LAMP->Shapes.empty();default->getShape(s,l,p,c);};}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player player,BlockHitResult hit){
        if(player.isSpectator()||!player.isAlive()||player.distanceToSqr(at.getCenter())>36)return InteractionResult.PASS;
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(player instanceof ServerPlayer p){if(!HouseReadKitchen.use(p,at,s,hit)&&!HouseReadMoments.routineUse(p,at,s,hit))HouseReadSites.use(p,at,s.getValue(PART));}return InteractionResult.CONSUME;
    }
    /** How brightly a fitting lights its room; a switched-off lamp is TURNED. */
    public static int light(BlockState s){if(s.getValue(TURNED)&&s.getValue(PART)!=Part.STRING_LIGHT)return 0;return switch(s.getValue(PART)){case STRING_LIGHT->10;case CEILING_PANEL->15;case VANITY_LIGHT,PENDANT->13;case PORCH_LIGHT,DECK_LANTERN->12;case LAMP->11;default->0;};}
    /** 0.4.168: a fitting may carry what the client draws over its model: the crayons in the tin, or the colours on a drawing. */
    public static final class Fitting extends BlockEntity {
        private net.minecraft.nbt.CompoundTag read=new net.minecraft.nbt.CompoundTag();
        public Fitting(BlockPos at,BlockState s){super(HouseReadRegistry.FITTING_ENTITY.get(),at,s);}
        public net.minecraft.nbt.CompoundTag read(){return read.copy();}
        public void setRead(net.minecraft.nbt.CompoundTag tag){read=tag.copy();setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}
        @Override protected void saveAdditional(net.minecraft.nbt.CompoundTag t,net.minecraft.core.HolderLookup.Provider r){super.saveAdditional(t,r);if(!read.isEmpty())t.put("Read",read);}
        @Override protected void loadAdditional(net.minecraft.nbt.CompoundTag t,net.minecraft.core.HolderLookup.Provider r){super.loadAdditional(t,r);read=t.getCompound("Read");}
        @Override public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
    }
}
