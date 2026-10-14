package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;

/** Opt-in connected window/audio proof; ordinary clients never call it. */
final class GoatmanWindowClient {
    private static final Set<Boolean> forms=new HashSet<>();
    private static UUID original;
    private static SimpleSoundInstance vanilla;
    private static long probeAt;
    private static int aiming;
    private static boolean audioPassed,departed;
    private GoatmanWindowClient(){}
    static boolean ready(){return forms.size()==2&&audioPassed;}
    static String tick(Minecraft mc,List<GoatmanChild> children,BlockPos base,String role,boolean capture){
        long now=mc.level.getGameTime();
        if(vanilla==null&&GoatmanScore.playing()){
            vanilla=SimpleSoundInstance.forMusic(SoundEvents.MUSIC_GAME.value());probeAt=now;
            mc.getSoundManager().play(vanilla);
        }
        if(!audioPassed&&vanilla!=null&&now-probeAt>=10){
            if(mc.getSoundManager().isActive(vanilla)||!GoatmanScore.playing())throw new IllegalStateException("LIVE EXPEDITION vanilla music overlaps the trailer score or the original score is missing");
            audioPassed=true;write(role+"-trailer-score.txt","Original Goatman score active in the actual sound engine. A real vanilla MUSIC_GAME play request was suppressed; it remained inactive while the scene score played.\n");
        }
        if(!capture)return null;
        for(var c:children){
            var rel=c.position().subtract(base.getX(),base.getY(),base.getZ());
            if(!c.stalkingAppearance()||forms.contains(c.monsterAppearance())||rel.y<.85||Math.abs(rel.z+58.5)>.2||Math.abs(rel.x)<8)continue;
            var toward=c.position().add(0,c.monsterAppearance()?2.2:1.1,0).subtract(mc.player.getEyePosition());
            mc.player.setYRot((float)Math.toDegrees(Math.atan2(-toward.x,toward.z)));
            mc.player.setXRot((float)-Math.toDegrees(Math.atan2(toward.y,Math.hypot(toward.x,toward.z))));
            if(++aiming<4)return null;
            if(original!=null&&!original.equals(c.getUUID()))throw new IllegalStateException("LIVE EXPEDITION a window flash replaced the original Goatman body");
            original=c.getUUID();forms.add(c.monsterAppearance());aiming=0;
            String name=role+"-trailer-window-"+(c.monsterAppearance()?"goatman":"child");
            write(name+".txt","Original native body="+original+" form="+(c.monsterAppearance()?"goatman":"child")+" relative="+rel+" actual eye="+mc.player.getEyePosition()+" camera="+mc.gameRenderer.getMainCamera().getPosition()+"\n");
            return name;
        }
        aiming=0;return null;
    }
    static void leave(Minecraft mc,int step,int ticks,String role){
        if(departed||step!=37||ticks<15)return;
        if(GoatmanScore.active()||GoatmanScore.playing())throw new IllegalStateException("LIVE EXPEDITION the trailer score or vanilla suppression leaked into the next vignette");
        departed=true;write(role+"-trailer-score-left.txt","The original score stopped and its scene music suppression ended after the actual native departure.\n");
    }
    private static void write(String name,String value){try{Files.createDirectories(LiveExpeditionProof.folder());Files.writeString(LiveExpeditionProof.folder().resolve(name),value);}catch(Exception e){throw new IllegalStateException(e);}}
}
