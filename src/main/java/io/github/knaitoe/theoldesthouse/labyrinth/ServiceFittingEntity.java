package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
/** Each original paper is filed once. Empty native drawers stay empty. */
public final class ServiceFittingEntity extends BaseContainerBlockEntity {
 private NonNullList<ItemStack> items=NonNullList.withSize(9,ItemStack.EMPTY);private boolean filed;private String fileLabel="Readers’ files";
 private final ContainerOpenersCounter openers=new ContainerOpenersCounter(){
  @Override protected void onOpen(Level l,BlockPos p,BlockState s){lid(l,p,s,true);}
  @Override protected void onClose(Level l,BlockPos p,BlockState s){lid(l,p,s,false);}
  @Override protected void openerCountChanged(Level l,BlockPos p,BlockState s,int a,int b){}
  @Override protected boolean isOwnContainer(Player p){return p.containerMenu instanceof ChestMenu m&&m.getContainer()==ServiceFittingEntity.this;}
 };
 public ServiceFittingEntity(BlockPos at,BlockState s){super(LiteraryRegistry.DETAIL_ENTITY.get(),at,s);}
 private static void lid(Level l,BlockPos p,BlockState s,boolean on){l.setBlock(p,s.setValue(ServiceFittingBlock.ACTIVE,on),3);l.playSound(null,p,on?SoundEvents.BARREL_OPEN:SoundEvents.BARREL_CLOSE,SoundSource.BLOCKS,.3F,.8F);}
 public void file(int reader,int drawer){
  if(filed||level==null||getBlockState().getValue(ServiceFittingBlock.KIND)!=ServiceFittingBlock.Kind.DRAWER)return;filed=true;
  String name=ArchiveReaderFiles.NAMES[reader],title=drawer==2?"Appointments":drawer==1?"Cross references":"Loose leaves";fileLabel=name+" — "+title;
  // 0.4.201 (owner: "the books should simply show the intended text rather than describing it"): each file is the paper
  // itself, the blank left blank and the line drawn, not a sentence about them.
  String[] hours={"10:00","2:30","9:15","4:00","11:30","1:00","3:45"};var list=new StringBuilder("READERS\u2019 APPOINTMENTS\n\n");
  for(int i=0;i<ArchiveReaderFiles.NAMES.length;i++)list.append(ArchiveReaderFiles.NAMES[i]).append("   ").append(i==reader?"____":hours[i]).append('\n');
  var pages=drawer==2?java.util.List.of(list.toString(),name+"\n\nNext visit:\n\n__________"):
   drawer==1?(reader%2==0?java.util.List.of("CROSS REFERENCES\n\n"+name+"\n\nDoorway \u2192 passage\nPassage \u2192 stair\nStair \u2192 landing\nLanding \u2192 doorway","door\n    passage\n        stair\n            landing\n\n            landing\n        stair\n    passage\ndoor\n\n      no room"):
    java.util.List.of("CROSS REFERENCES\n\n"+name+"\n\nDoorway \u2192 passage\nPassage \u2192 stair\nStair \u2192 landing\nLanding \u2192 doorway","landing\n\n-------------------->","-------------------\n\n          doorway")):
   java.util.List.of("LOOSE LEAVES\n\n"+name,"stair      |      stair\n           |\n-----------+-----------\n           |\nlanding  |  landing"," ");
  var paper=HouseWriting.book(name+" — "+title,"",HouseWriting.WritingStyle.ZAMPANO,pages);CustomData.update(DataComponents.CUSTOM_DATA,paper,t->{t.putUUID("ArchivePaper133",java.util.UUID.randomUUID());t.putString("FiledUnder133",name);});items.set(0,paper);setChanged();
 }
 @Override public int getContainerSize(){return 9;}
 @Override protected NonNullList<ItemStack> getItems(){return items;}
 @Override protected void setItems(NonNullList<ItemStack> s){items=s;}
 @Override protected Component getDefaultName(){return Component.literal(fileLabel);}
 @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return new ChestMenu(MenuType.GENERIC_9x1,id,inv,this,1);}
 @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);ContainerHelper.saveAllItems(t,items,r);t.putBoolean("Filed133",filed);t.putString("FileLabel133",fileLabel);}
 @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);items=NonNullList.withSize(9,ItemStack.EMPTY);ContainerHelper.loadAllItems(t,items,r);filed=t.getBoolean("Filed133");if(t.contains("FileLabel133"))fileLabel=t.getString("FileLabel133");}
 @Override public void startOpen(Player p){if(!isRemoved()&&!p.isSpectator())openers.incrementOpeners(p,level,worldPosition,getBlockState());}
 @Override public void stopOpen(Player p){if(!isRemoved()&&!p.isSpectator())openers.decrementOpeners(p,level,worldPosition,getBlockState());}
 @Override public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide&&getBlockState().getValue(ServiceFittingBlock.KIND)==ServiceFittingBlock.Kind.DRAWER&&getBlockState().getValue(ServiceFittingBlock.ACTIVE))level.scheduleTick(worldPosition,getBlockState().getBlock(),5);}
 public void recheck(){if(!isRemoved()&&level!=null){openers.recheckOpeners(level,worldPosition,getBlockState());if(openers.getOpenerCount()==0&&getBlockState().getValue(ServiceFittingBlock.ACTIVE))lid(level,worldPosition,getBlockState(),false);}}
}
