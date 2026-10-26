package io.github.knaitoe.theoldesthouse.client;
import java.util.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;

/** Each actual recorder owns its own sound instance. Pause/unload never silences another recorder. */
public final class VignetteHandlingAudio {
 private record Playing(int mode,int segment,Clip sound){}
 private static final Map<VignetteHandlingBlockEntity,Playing> LIVE=new WeakHashMap<>();
 private VignetteHandlingAudio(){}
 public static void stop(VignetteHandlingBlockEntity b){var p=LIVE.remove(b);if(p!=null)Minecraft.getInstance().getSoundManager().stop(p.sound());}
 public static void tick(VignetteHandlingBlockEntity b){
  if(b.kind()!=VignetteHandlingBlock.Kind.TAPE)return;var mc=Minecraft.getInstance();var v=b.view();
  if(b.isRemoved()||b.getLevel()!=mc.level||v.mode()==0){stop(b);return;}
  int segment=v.mode()==2?-1:Math.min(31,v.cursor()/10);var current=LIVE.get(b);
  if(current!=null&&current.mode()==v.mode()&&current.segment()==segment)return;stop(b);
  var event=v.mode()==2?LiteraryRegistry.HANDLING_WIND.get():LiteraryRegistry.HANDLING_TAPE.get(segment).get();
  var clip=new Clip(b,event,v.mode(),segment);LIVE.put(b,new Playing(v.mode(),segment,clip));mc.getSoundManager().play(clip);
 }
 private static final class Clip extends AbstractTickableSoundInstance {
  final VignetteHandlingBlockEntity owner;final int mode,segment;
  Clip(VignetteHandlingBlockEntity b,SoundEvent event,int mode,int segment){super(event,SoundSource.BLOCKS,RandomSource.create());owner=b;this.mode=mode;this.segment=segment;looping=mode==2;delay=0;volume=.36F;x=b.getBlockPos().getX()+.5;y=b.getBlockPos().getY()+.3;z=b.getBlockPos().getZ()+.5;}
  @Override public void tick(){var v=owner.view();if(owner.isRemoved()||owner.getLevel()!=Minecraft.getInstance().level||v.mode()!=mode||mode==1&&v.cursor()/10!=segment)stop();}
 }
}
