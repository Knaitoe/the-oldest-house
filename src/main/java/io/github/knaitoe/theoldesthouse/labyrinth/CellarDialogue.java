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
    private static final String[] MAN={"Cut deeper. Please.","I didn't want this.","Is it morning?","Don't leave me like this.","It keeps coming back. Get all of it.","I can still feel your hands."};
    private CellarDialogue(){}
    public static void worker(ServerPlayer p,CompoundTag own){
        int line=own.getInt("WorkerStory130");own.putInt("WorkerStory130",line+1);var b=LiteraryVignettes.base(p,LabyrinthPlace.LAYERED_MAN);var shared=b==null?new CompoundTag():UncannyStories.world(p.serverLevel(),b,LabyrinthPlace.LAYERED_MAN);
        String text=shared.getBoolean("Eradicated131")?"There's nothing left. It's finally quiet.":!own.getBoolean("CleaverTaken")?"The cleaver's on the bench, by the stairs. My hands won't last.":shared.getBoolean("MouthCut131")?"He can't speak now. Keep cutting. Don't leave any of it.":own.getInt("Peeled")>0?"I'm cutting what I can. You need to get deeper.":WORKER[Math.floorMod(line,WORKER.length)];
        p.displayClientMessage(Component.literal("“"+text+"”"),false);
    }
    private static void gurgle(ServerPlayer p,BlockPos b,CompoundTag own){UncannyStories.heard(p,CellarSounds.gurgle(),b.offset(0,-3,-21).getCenter(),.65F+p.getRandom().nextFloat()*.15F,.76F+p.getRandom().nextFloat()*.24F);own.putLong("CellarRumbleUntil130",p.serverLevel().getGameTime()+70);}
    public static void touched(ServerPlayer p,CompoundTag own){
        long now=p.serverLevel().getGameTime();if(own.contains("NextTouchWarning131")&&now<own.getLong("NextTouchWarning131"))return;
        own.putLong("NextTouchWarning131",now+1200);own.putInt("TouchWarnings131",own.getInt("TouchWarnings131")+1);
        p.displayClientMessage(Component.literal("“Don't - don't let him touch you, he'll consume you too.”"),false);
    }
    public static void actions(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag shared){
        long now=p.serverLevel().getGameTime();
        if(own.getBoolean("Escorting")&&shared.getBoolean("Rescued")&&shared.hasUUID("RescuedBy")&&shared.getUUID("RescuedBy").equals(p.getUUID()))own.putBoolean("WorkerRescued",true);
        if(own.getBoolean("CleaverTaken")&&!own.getBoolean("CleaverPrompt131")&&p.getInventory().countItem(UncannyRegistry.CLEAVER.get())>0){
            own.putBoolean("CleaverPrompt131",true);own.putLong("NextManVoice130",now+400);
            if(!shared.getBoolean("Eradicated131")){if(shared.getBoolean("MouthCut131"))gurgle(p,b,own);else{p.displayClientMessage(Component.literal("“Cut. Cut me.”"),false);UncannyStories.heard(p,NovelRegistry.WELL_BREATH.get(),b.offset(0,-3,-21).getCenter(),.5F,.6F);}}
        }
        if(UncannyCellar.carriesKey(p)&&!own.getBoolean("Completed")&&!UncannyStories.failed(own)){own.putBoolean("Helped",true);own.putBoolean("KeyRecovered131",true);UncannyStories.ready(p,LabyrinthPlace.LAYERED_MAN,own,"retrieved_the_cellar_key");}
        if(p.getY()<b.getY()-3&&p.getZ()<b.getZ()-13)own.putBoolean("CellarWorkedBelow131",true);
        if(own.getBoolean("CellarWorkedBelow131")&&p.getY()>=b.getY()-3&&p.getZ()>b.getZ()-13&&!shared.getBoolean("Eradicated131")&&now>=own.getLong("NextLeavePlea131")){
            own.putBoolean("CellarWorkedBelow131",false);own.putLong("NextLeavePlea131",now+400);p.displayClientMessage(Component.literal("“Don't go. Please. He'll keep growing. I can't do this alone.”"),false);
        }
    }
    public static void cut(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own,CompoundTag shared){
        boolean mouth=rel.equals(new BlockPos(0,-3,-21));boolean face=rel.equals(new BlockPos(0,-2,-20))||rel.getZ()==-21&&rel.getY()==-2&&Math.abs(rel.getX())==1;
        if(mouth){shared.putBoolean("MouthCut131",true);gurgle(p,b,own);own.putLong("NextManVoice130",p.serverLevel().getGameTime()+400);}
        else if(face&&!shared.getBoolean("FaceCut131")){shared.putBoolean("FaceCut131",true);own.putBoolean("FaceCutReply131",true);if(shared.getBoolean("MouthCut131"))gurgle(p,b,own);else p.displayClientMessage(Component.literal("“I can't see. it still hurts. please.”"),false);}
    }
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
        if(shared.getBoolean("Eradicated131")||!head&&!shared.getBoolean("MouthCut131")||now<own.getLong("NextManVoice130"))return;
        int n=own.getInt("ManVoice130");String text=MAN[Math.floorMod(n,MAN.length)];own.putInt("ManVoice130",n+1);own.putLong("NextManVoice130",now+600+p.getRandom().nextInt(301));own.putLong("CellarRumbleUntil130",now+60);
        if(shared.getBoolean("MouthCut131")){gurgle(p,b,own);return;}
        p.displayClientMessage(Component.literal("“"+text+"”"),false);UncannyStories.heard(p,SoundEvents.WARDEN_HEARTBEAT,b.offset(0,-4,-22).getCenter(),.7F,.45F);UncannyStories.heard(p,NovelRegistry.WELL_BREATH.get(),b.offset(0,-2,-21).getCenter(),.45F,.6F);
    }
}
