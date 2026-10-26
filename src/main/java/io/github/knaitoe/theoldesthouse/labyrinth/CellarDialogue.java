package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

/** Sparse, present-tense exchanges from the worker and the same growing body. */
public final class CellarDialogue {
    private static final String[] WORKER={"It started with a swelling. I thought it would stop.","I thought there'd be less of him by morning.","He still knows when I leave.","Keep the steps clear. I need to be able to see them."};
    private static final String[] MAN={"Stop... please.","I didn't want this.","Is it morning?","Don't leave me like this."};
    private CellarDialogue(){}
    public static void worker(ServerPlayer p,CompoundTag own){int line=own.getInt("WorkerStory130");own.putInt("WorkerStory130",line+1);String text=!own.getBoolean("CleaverTaken")?"The cleaver's on the bench, by the stairs. My hands won't last.":WORKER[Math.floorMod(line,WORKER.length)];p.displayClientMessage(Component.literal("“"+text+"”"),false);}
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag shared){
        boolean below=p.getY()<b.getY()-1;if(!below){own.remove("CellarWasBelow130");return;}long now=p.serverLevel().getGameTime();
        if(!own.getBoolean("CellarWasBelow130")){
            own.putBoolean("CellarWasBelow130",true);
            if(!shared.getBoolean("Rescued")){own.putBoolean("CleaverDirected130",true);p.displayClientMessage(Component.literal(own.getBoolean("CleaverTaken")?"“You came back. Keep it away from the steps.”":"“The cleaver. On the bench, by the stairs. Please.”"),false);}
            if(!own.contains("NextManVoice130"))own.putLong("NextManVoice130",now+180);
        }
        // Serous fluid drips out around the feet and lowest folds, against the real floor.
        if(now%20==0){
            p.serverLevel().sendParticles(p,ParticleTypes.FALLING_HONEY,true,b.getX()-3.04,b.getY()-4.6,b.getZ()-20.5,2,.04,.1,.2,.005);
            p.serverLevel().sendParticles(p,ParticleTypes.DRIPPING_HONEY,true,b.getX()+4.02,b.getY()-4.5,b.getZ()-22.5,2,.04,.1,.2,0);
        }
        boolean head=false;for(int x=-1;x<=1;x++)head|=UncannyFleshBlock.is(p.serverLevel().getBlockState(b.offset(x,-2,-22)));
        if(!head||now<own.getLong("NextManVoice130"))return;
        int n=own.getInt("ManVoice130");String text=MAN[Math.floorMod(n,MAN.length)];own.putInt("ManVoice130",n+1);own.putLong("NextManVoice130",now+600+p.getRandom().nextInt(301));own.putLong("CellarRumbleUntil130",now+60);
        p.displayClientMessage(Component.literal("“"+text+"”"),false);UncannyStories.heard(p,SoundEvents.WARDEN_HEARTBEAT,b.offset(0,-4,-22).getCenter(),.7F,.45F);UncannyStories.heard(p,NovelRegistry.WELL_BREATH.get(),b.offset(0,-2,-21).getCenter(),.45F,.6F);
    }
}
