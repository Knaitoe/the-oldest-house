package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.*;
/** Uneven, continuous native footfalls for the original private injured woman. */
public final class TunnelStride {
    private record Goal(Vec3 at,long until){}
    private static final Map<LiteraryActor,Goal> GOALS=new WeakHashMap<>();
    private static final Map<LiteraryActor,Stride> STRIDES=new WeakHashMap<>();
    private static final class Stride {boolean far;double travelled;long impact=Long.MIN_VALUE;Vec3 at;}
    private TunnelStride(){}
    /** Relocate an unseen original with the same native position on every existing tracker. */
    public static void placeOriginal(LiteraryActor a,Vec3 at){
        a.moveTo(at);a.setDeltaMovement(Vec3.ZERO);a.hasImpulse=true;
        if(a.level() instanceof ServerLevel l)l.getChunkSource().broadcast(a,new net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket(a));
    }
    public static void seek(LiteraryActor a,Vec3 at){GOALS.put(a,new Goal(at,a.level().getGameTime()+10));}
    /** 0.4.240: where she stands folded, kept on her body so she can be put back while her reader is away. */
    static void remember(LiteraryActor a,BlockPos home){a.getPersistentData().putLong("TunnelHome240",home.asLong());}
    /** Folded and still at her place at the far end, facing down the tunnel, every stride forgotten. */
    static void home(LiteraryActor a,BlockPos home){
        remember(a,home);GOALS.remove(a);STRIDES.remove(a);a.gesture(0);a.appearance(LiteraryActor.UNCANNY_GIANT,0);
        a.setYRot(180);a.setYHeadRot(180);a.setYBodyRot(180);a.setXRot(0);placeOriginal(a,Vec3.atBottomCenterOf(home));
    }
    /** Whether her reader is in the tunnel now. */
    private static boolean attended(LiteraryActor a){
        if(!(a.level() instanceof ServerLevel l)||a.owner().isEmpty())return true;
        return l.getServer().getPlayerList().getPlayer(a.owner().get()) instanceof net.minecraft.server.level.ServerPlayer reader&&LiteraryVignettes.current(reader)==LabyrinthPlace.TUNNEL_PERIPHERY;
    }
    public static void tick(LiteraryActor a){
        if(a.role()!=LiteraryActor.UNCANNY_GIANT)return;
        // 0.4.240 (owner: "bent necked woman doesnt properly reset once players exit the vignette"): when her reader leaves, she goes
        // back to her place at the far end, folded, rather than standing where the chase left her.
        if(a.tickCount%10==0&&!attended(a)){var tag=a.getPersistentData();if(tag.contains("TunnelHome240")){var at=BlockPos.of(tag.getLong("TunnelHome240"));if(a.phase()!=0||a.blockPosition().distManhattan(at)>1)home(a,at);}else if(a.phase()!=0){GOALS.remove(a);a.appearance(LiteraryActor.UNCANNY_GIANT,0);}return;}
        var stride=STRIDES.computeIfAbsent(a,k->new Stride());long now=a.level().getGameTime(),echo=now-stride.impact;
        if(stride.at!=null&&(echo==3||echo==7||echo==11))sound(a,stride.at,echo==3?.36F:echo==7?.18F:.08F,.65F);
        var goal=GOALS.get(a);if(a.phase()!=2||goal==null||now>goal.until){a.gesture(0);stride.far=false;stride.travelled=0;a.setDeltaMovement(Vec3.ZERO);return;}
        double distance=a.position().subtract(goal.at).horizontalDistance();
        if(distance>=14)stride.far=true;else if(distance<=8)stride.far=false;
        a.gesture(stride.far?1:0);
        double step=stride.far?.40+.18*(.5+.5*Math.sin(a.tickCount*.13)):.26+.10*(.5+.5*Math.sin(a.tickCount*.17));
        var before=a.position();SceneFootpaths.walk(a,goal.at,step);double moved=a.position().subtract(before).horizontalDistance();
        if(stride.far&&moved>.001){stride.travelled+=moved;if(stride.travelled>=4.2){stride.travelled-=4.2;stride.impact=now;stride.at=a.position();sound(a,stride.at,1.15F,.75F+a.getRandom().nextFloat()*.1F);a.getPersistentData().putLong("TunnelQuakeUntil131",now+12);a.getPersistentData().putInt("TunnelSteps131",a.getPersistentData().getInt("TunnelSteps131")+1);}}
        else if(!stride.far)stride.travelled=0;
    }
    private static void sound(LiteraryActor a,Vec3 at,float volume,float pitch){
        if(!(a.level() instanceof ServerLevel l)||a.owner().isEmpty())return;var p=l.getPlayerByUUID(a.owner().get());
        if(!(p instanceof net.minecraft.server.level.ServerPlayer reader)||LiteraryVignettes.current(reader)!=LabyrinthPlace.TUNNEL_PERIPHERY||reader.distanceToSqr(a)>10000)return;
        reader.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WARDEN_STEP),SoundSource.HOSTILE,at.x,at.y,at.z,volume,pitch,a.getRandom().nextLong()));
    }
}
