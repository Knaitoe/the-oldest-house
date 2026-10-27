package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** A finite original on a physical support. Taking it does not print another on re-entry. */
public final class SiloPaperBlock extends BaseEntityBlock {
    public static final MapCodec<SiloPaperBlock> CODEC=simpleCodec(SiloPaperBlock::new);
    public static final BooleanProperty PRESENT=BooleanProperty.create("present");
    public SiloPaperBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PRESENT,true));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PRESENT);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new Paper(at,s);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return Block.box(2,0,2,14,.5,14);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos at){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){
        if(p.isSpectator()||!p.isAlive()||p.distanceToSqr(at.getCenter())>36)return InteractionResult.PASS;
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(p instanceof ServerPlayer reader&&l.getBlockEntity(at) instanceof Paper paper)paper.open(reader);
        return InteractionResult.CONSUME;
    }
    public static final class Paper extends BlockEntity {
        private ItemStack book=ItemStack.EMPTY;private UUID owner;private boolean initialized;private int piece;
        public Paper(BlockPos at,BlockState s){super(SiloRegistry.PAPER_ENTITY.get(),at,s);}
        public ItemStack original(){return book.copy();}
        public void seed(UUID reader,int index){if(initialized)return;initialized=true;owner=reader;piece=index;book=SiloTexts.original(reader,index);setChanged();}
        private boolean valid(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&p.level()==level&&p.getUUID().equals(owner)&&p.distanceToSqr(worldPosition.getCenter())<=64&&level.getBlockEntity(worldPosition)==this&&!book.isEmpty();}
        public void open(ServerPlayer p){if(!valid(p))return;if(p.openMenu(new SimpleMenuProvider((id,inv,reader)->new Pages(id,p,this),book.getHoverName())).isPresent())SiloSites.examined(p,piece);}
        private boolean give(ServerPlayer p){
            var original=book;book=ItemStack.EMPTY;boolean given=false;
            int slot=p.getInventory().getFreeSlot();if(slot>=0){p.getInventory().setItem(slot,original);p.inventoryMenu.broadcastChanges();given=true;}
            else{var item=new net.minecraft.world.entity.item.ItemEntity(p.serverLevel(),p.getX(),p.getY()+.2,p.getZ(),original);item.setTarget(p.getUUID());given=p.serverLevel().addFreshEntity(item);}
            if(!given){book=original;return false;}setChanged();level.setBlock(worldPosition,getBlockState().setValue(PRESENT,false),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);return true;
        }
        @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);initialized=t.getBoolean("Initialized");owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;piece=t.getInt("Piece");book=ItemStack.parseOptional(r,t.getCompound("Book"));}
        @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putBoolean("Initialized",initialized);if(owner!=null)t.putUUID("Owner",owner);t.putInt("Piece",piece);if(!book.isEmpty())t.put("Book",book.save(r));}
    }
    private static final class Pages extends LecternMenu {
        private final ServerPlayer reader;private final Paper original;
        private Pages(int id,ServerPlayer p,Paper be){super(id,container(be.book),new SimpleContainerData(1));reader=p;original=be;}
        private static Container container(ItemStack book){var c=new SimpleContainer(1);c.setItem(0,book.copy());return c;}
        @Override public boolean stillValid(Player p){return p==reader&&original.valid(reader);}
        @Override public void clicked(int slot,int button,ClickType type,Player p){}
        @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
        @Override public boolean clickMenuButton(Player p,int button){if(!stillValid(p))return false;if(button==3){if(!original.give(reader))return false;reader.closeContainer();return true;}return super.clickMenuButton(p,button);}
    }
}
