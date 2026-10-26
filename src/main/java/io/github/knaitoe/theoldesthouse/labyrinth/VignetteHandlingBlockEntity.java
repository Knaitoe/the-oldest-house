package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** Native saved object state. The practice model never changes any of the three story model records. */
public final class VignetteHandlingBlockEntity extends BlockEntity {
 public static final int LENGTH=320;
 public static Consumer<VignetteHandlingBlockEntity> clientAudio=b->{},clientStop=b->{};
 private int key=-1,slide,cursor,mode,held=-1;private boolean tray,lamp,lid;private int[] cells={0,4,8},turns=new int[3];
 private UUID holder;private long releaseAt,heldUntil;private int ticks;
 public VignetteHandlingBlockEntity(BlockPos p,BlockState s){super(LiteraryRegistry.HANDLING_ENTITY.get(),p,s);}
 public VignetteHandlingBlock.Kind kind(){return getBlockState().getValue(VignetteHandlingBlock.KIND);}
 public VignetteHandlingGeometry.View view(){return new VignetteHandlingGeometry.View(key,slide,tray,lamp,cursor,mode,lid,cells,turns,held);}
 private void notifyState(){setChanged();if(level!=null){boolean active=kind()==VignetteHandlingBlock.Kind.SLIDES?lamp:kind()==VignetteHandlingBlock.Kind.TAPE?mode!=0:key>=0;
  var s=getBlockState();if(s.getValue(VignetteHandlingBlock.ACTIVE)!=active)level.setBlock(worldPosition,s.setValue(VignetteHandlingBlock.ACTIVE,active),Block.UPDATE_CLIENTS);
  level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);
 }}
 private void sound(SoundEvent e,float pitch){level.playSound(null,worldPosition,e,SoundSource.BLOCKS,.32F,pitch);level.gameEvent(null,GameEvent.BLOCK_CHANGE,worldPosition);}
 public static int pianoKey(Vec3 v){double x=16-v.x*16,z=v.z*16;int[] gaps={1,2,4,5,6},notes={1,3,6,8,10};if(z>=5&&z<=10)for(int i=0;i<5;i++)if(Math.abs(x-(1+2*gaps[i]))<=.55)return notes[i];return new int[]{0,2,4,5,7,9,11}[Math.clamp((int)((x-1)/2),0,6)];}
 public InteractionResult use(Player p,Vec3 local){
  if(level==null||level.isClientSide||p.isSpectator()||p.distanceToSqr(worldPosition.getCenter())>36)return InteractionResult.PASS;
  var before=view();int oldHeld=held;UUID oldHolder=holder;long oldUntil=heldUntil,oldRelease=releaseAt;var saved=new CompoundTag();saveState(saved);SoundEvent effect=LiteraryRegistry.HANDLING_CLICK.get();float pitch=1;
  switch(kind()){
   case TOY_PIANO,SPINET->{
    if(local.z*16>10){if(kind()==VignetteHandlingBlock.Kind.SPINET)lid=!lid;else return InteractionResult.PASS;}
    else{key=pianoKey(local);releaseAt=level.getGameTime()+6;effect=(kind()==VignetteHandlingBlock.Kind.TOY_PIANO?LiteraryRegistry.HANDLING_TOY:LiteraryRegistry.HANDLING_SPINET).get();pitch=(float)Math.pow(2,key/12.0);}
   }
   case SLIDES->{
    double x=local.x*16,z=local.z*16;
    if(x>=12.5&&z>=12.5)lamp=!lamp;
    else if(p.isShiftKeyDown()||z<5&&x>=5&&x<=11)tray=!tray;
    else{slide=Math.floorMod(slide+(x>11?-1:1),4);effect=LiteraryRegistry.HANDLING_SLIDE.get();}
   }
   case MODEL_KIT->{
    int cell=Math.clamp((int)((local.x*16-2)/4),0,2)+3*Math.clamp((int)((local.z*16-2)/4),0,2),piece=-1;
    for(int i=0;i<3;i++)if(cells[i]==cell)piece=i;
    if(held>=0&&holder!=null&&!holder.equals(p.getUUID())&&level.getGameTime()<heldUntil)return InteractionResult.CONSUME;
    if(p.isShiftKeyDown()){if(piece<0)return InteractionResult.PASS;turns[piece]=(turns[piece]+1)%4;}
    else if(held>=0&&p.getUUID().equals(holder)){if(piece>=0&&piece!=held)return InteractionResult.CONSUME;cells[held]=cell;held=-1;holder=null;}
    else{if(piece<0)return InteractionResult.PASS;held=piece;holder=p.getUUID();heldUntil=level.getGameTime()+100;}
    effect=LiteraryRegistry.HANDLING_WOOD.get();
   }
   case TAPE->{
    double x=local.x*16,z=local.z*16;
    if(z>6){lid=!lid;mode=0;}
    else if(x>10){mode=cursor>0?2:0;}
    else if(x>6){if(lid)return InteractionResult.CONSUME;if(cursor==LENGTH)cursor=0;mode=1;}
    else mode=0;
   }
  }
  // Newly occupied geometry cannot close through somebody or bury an original dropped item.
  var added=Shapes.joinUnoptimized(VignetteHandlingBlock.shape(getBlockState(),view()),VignetteHandlingBlock.shape(getBlockState(),before),BooleanOp.ONLY_FIRST);
  for(var box:added.toAabbs())if(!level.getEntities((Entity)null,box.move(worldPosition).inflate(.001),e->e.isAlive()&&!e.isSpectator()).isEmpty()){readState(saved);held=oldHeld;holder=oldHolder;heldUntil=oldUntil;releaseAt=oldRelease;return InteractionResult.CONSUME;}
  notifyState();sound(effect,pitch);return InteractionResult.CONSUME;
 }
 /** Preserve an old switched light or open lid without inventing a recording or resetting originals. */
 public void restoreOrdinaryState(){if(kind()==VignetteHandlingBlock.Kind.SLIDES)lamp=true;else if(kind()==VignetteHandlingBlock.Kind.TAPE)lid=true;notifyState();}
 public void tick(){if(level==null)return;
  if(kind()==VignetteHandlingBlock.Kind.TAPE){if(mode==1){cursor=Math.min(LENGTH,cursor+1);if(cursor==LENGTH)mode=0;}else if(mode==2){cursor=Math.max(0,cursor-8);if(cursor==0)mode=0;}}
  if(level.isClientSide){clientAudio.accept(this);return;}
  boolean update=false;if(key>=0&&level.getGameTime()>=releaseAt){key=-1;update=true;}
  if(held>=0&&level.getGameTime()>=heldUntil){held=-1;holder=null;update=true;}
  if(kind()==VignetteHandlingBlock.Kind.TAPE&&++ticks%20==0){setChanged();update=true;}
  if(update)notifyState();
 }
 private void saveState(CompoundTag t){t.putInt("Key",key);t.putInt("Slide",slide);t.putBoolean("Tray",tray);t.putBoolean("Lamp",lamp);t.putBoolean("Lid",lid);t.putInt("Cursor",cursor);t.putInt("Mode",mode);t.putIntArray("Cells",cells);t.putIntArray("Turns",turns);}
 private void readState(CompoundTag t){key=t.contains("Key")?Math.clamp(t.getInt("Key"),-1,11):-1;slide=Math.floorMod(t.getInt("Slide"),4);tray=t.getBoolean("Tray");lamp=t.getBoolean("Lamp");lid=t.getBoolean("Lid");cursor=Math.clamp(t.getInt("Cursor"),0,LENGTH);mode=Math.clamp(t.getInt("Mode"),0,2);
  int[] c=t.getIntArray("Cells"),r=t.getIntArray("Turns");cells=new int[]{0,4,8};turns=new int[3];if(c.length==3&&r.length==3&&Arrays.stream(c).allMatch(i->i>=0&&i<9)&&Arrays.stream(c).distinct().count()==3){cells=c.clone();for(int i=0;i<3;i++)turns[i]=Math.floorMod(r[i],4);}held=-1;holder=null;
 }
 @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);saveState(t);}
 @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);readState(t);if(level!=null&&!level.isClientSide){key=-1;releaseAt=0;}}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveState(t);t.putInt("Held",held);return t;}
 @Override public void handleUpdateTag(CompoundTag t,HolderLookup.Provider r){readState(t);held=Math.clamp(t.getInt("Held"),-1,2);}
 @Override public void onDataPacket(net.minecraft.network.Connection c,ClientboundBlockEntityDataPacket packet,HolderLookup.Provider r){handleUpdateTag(packet.getTag(),r);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 @Override public void setRemoved(){if(level!=null&&level.isClientSide)clientStop.accept(this);super.setRemoved();}
}
