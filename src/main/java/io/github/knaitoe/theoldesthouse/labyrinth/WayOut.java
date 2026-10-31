package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.journal.Journals;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.WayOutPayload;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.199, the way out (the owner: "a simulacrum of the overworld as a vignette"). A door opens onto daylight: a meadow, a pond,
 * animals, a path to a cottage, the game's own music. It looks as if the House has let the reader go. It is slightly off, then
 * wrong: the music flat and wavering, the sun still, the animals turning to watch, footsteps coming back late, static blipping over
 * the screen more and more often. At the cottage door, or after long enough, or at a death there, the static covers everything and
 * one line is left: "you really thought you were out?". The same line goes into the reader's journal, and they wake in the hall
 * they came from. A death there is no death. Once to each reader, one reader at a time, and no Witness source. Docs: WAY_OUT_0_4_199.md.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WayOut {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.WAY_OUT;
    public static final String STATE="way_out_0199";
    public static final String LINE="you really thought you were out?";
    /** The cottage on the far side of the meadow: its door is the way on, and reaching it ends the dream. */
    public static final BlockPos COTTAGE_DOOR=new BlockPos(0,0,-76);
    /** Occupied ticks: calm; then the animals and the late footsteps; then static; then the end on its own. */
    public static final int CALM=600, STATIC_FROM=900, LONG_ENOUGH=3600;
    /** The static thickening to cover everything, then the line on black. */
    public static final int RAMP=60, CARD=100;
    /** How near the cottage door ends it. */
    public static final double HOME=3.5;
    /** Witness resolutions a reader has before it is dealt: long enough in the House to want out. */
    public static final int RESOLUTIONS=8;
    static final double STRIDE=1.7;
    static final int ECHO=7;
    private static final Set<UUID> HERE=new HashSet<>();
    private record Echo(long at,Vec3 pos,net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound){}
    private static final Map<UUID,ArrayDeque<Echo>> ECHOES=new HashMap<>();
    private static final Map<UUID,Vec3> STEPPED=new HashMap<>();
    private WayOut(){}

    public static CompoundTag own(MinecraftServer s,UUID id){return LabyrinthData.get(s).stateEntry(STATE,id.toString());}
    static void save(MinecraftServer s,UUID id,CompoundTag t){LabyrinthData.get(s).setStateEntry(STATE,id.toString(),t);}
    public static boolean seen(MinecraftServer s,UUID id){return own(s,id).getBoolean("Seen");}
    /** Dealt once, to a reader who has been in the House long enough to want out. */
    public static boolean canDeal(LabyrinthData d,UUID id){return !d.stateEntry(STATE,id.toString()).getBoolean("Seen")&&WitnessAccount.count(d,id)>=RESOLUTIONS;}
    public static BlockPos base(MinecraftServer s){var o=HouseSavedData.get(s).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,PLACE);}
    public static AABB bounds(BlockPos b){var r=PLACE.room();return new AABB(Vec3.atLowerCornerOf(b.offset(r.minX(),r.minY(),r.minZ())),Vec3.atLowerCornerOf(b.offset(r.maxX()+1,r.maxY()+1,r.maxZ()+1)));}
    public static boolean inside(ServerPlayer p){
        if(!p.isAlive()||p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR||!p.level().dimension().equals(HouseDimensions.OUTSIDE))return false;
        var b=base(p.server);return b!=null&&bounds(b).contains(p.position());
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();var l=s.getLevel(HouseDimensions.OUTSIDE);var b=base(s);
        for(var p:s.getPlayerList().getPlayers()){
            boolean in=l!=null&&b!=null&&inside(p);
            if(in)reader(p,l,b);else if(HERE.contains(p.getUUID()))left(p);
        }
    }
    static void reader(ServerPlayer p,ServerLevel l,BlockPos b){
        var own=own(p.server,p.getUUID());
        if(own.getBoolean("Seen")&&own.getInt("Ending")==0)return;
        if(HERE.add(p.getUUID())){HousePackets.send(p,new WayOutPayload(WayOutPayload.ENTER,0));STEPPED.put(p.getUUID(),p.position());}
        echoes(p,l);
        int ending=own.getInt("Ending");
        if(ending>0){
            own.putInt("Ending",++ending);
            if(ending==RAMP){
                HousePackets.send(p,new WayOutPayload(WayOutPayload.CARD,CARD));
                Journals.houseLine(p.server,p.getUUID(),LINE,.12F,.38F);own.putBoolean("Seen",true);
                save(p.server,p.getUUID(),own);
                wake(p);return;
            }
            // Should the way back be busy a moment, the reader is sent again until they are gone.
            if(ending>RAMP&&(ending-RAMP)%40==0&&!LabyrinthDoors.isBusy(p))wake(p);
            save(p.server,p.getUUID(),own);return;
        }
        int t=own.getInt("Ticks")+1;own.putInt("Ticks",t);
        if(t>=STATIC_FROM&&t>=own.getInt("NextBlip")){
            float progress=Mth.clamp((t-STATIC_FROM)/(float)(LONG_ENOUGH-STATIC_FROM),0F,1F);
            HousePackets.send(p,new WayOutPayload(WayOutPayload.BLIP,3+Math.round(progress*7)));
            own.putInt("NextBlip",t+Math.round(Mth.lerp(progress,600F,80F))+p.getRandom().nextInt(60));
        }
        if(t>=CALM){footsteps(p,l);if(t%5==0)watch(p,l,b);}
        double home=Vec3.atBottomCenterOf(b.offset(COTTAGE_DOOR)).subtract(p.position()).horizontalDistance();
        save(p.server,p.getUUID(),own);
        if(home<HOME||t>=LONG_ENOUGH)begin(p);
    }
    /** Back in the hall they came from, as if they had never left it. */
    static void wake(ServerPlayer p){
        LabyrinthDoors.sendBack(p,0,CARD/2,CARD/4,arrived->{var t=own(arrived.server,arrived.getUUID());t.putInt("Ending",0);save(arrived.server,arrived.getUUID(),t);left(arrived);});
    }
    /** The end begins: the static thickens and the music drops away. */
    public static void begin(ServerPlayer p){
        var own=own(p.server,p.getUUID());if(own.getInt("Ending")>0)return;
        own.putInt("Ending",1);save(p.server,p.getUUID(),own);
        HousePackets.send(p,new WayOutPayload(WayOutPayload.END,RAMP));
        var b=base(p.server);var l=p.server.getLevel(HouseDimensions.OUTSIDE);if(b!=null&&l!=null)release(l,b);
    }
    static void left(ServerPlayer p){
        HERE.remove(p.getUUID());ECHOES.remove(p.getUUID());STEPPED.remove(p.getUUID());
        var own=own(p.server,p.getUUID());
        // A reader who goes back through the door before the end has not had it: it may be dealt to them again, from the start.
        if(!own.getBoolean("Seen")&&own.getInt("Ending")==0){own.putInt("Ticks",0);own.putInt("NextBlip",0);save(p.server,p.getUUID(),own);}
        HousePackets.send(p,new WayOutPayload(WayOutPayload.LEAVE,0));
        var b=base(p.server);var l=p.server.getLevel(HouseDimensions.OUTSIDE);if(b!=null&&l!=null)release(l,b);
    }
    /** The animals stop where they are, and any the reader is not looking at turns to face them. */
    static void watch(ServerPlayer p,ServerLevel l,BlockPos b){
        var eye=p.getEyePosition();var look=p.getViewVector(1F);
        for(var a:l.getEntitiesOfClass(Animal.class,bounds(b))){
            a.setNoAi(true);a.getNavigation().stop();
            if(seen(p,a,eye,look))continue;
            var to=eye.subtract(a.getEyePosition());float yaw=(float)(Mth.atan2(to.z,to.x)*Mth.RAD_TO_DEG)-90F;
            a.setYRot(yaw);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.yRotO=yaw;
        }
    }
    public static boolean seen(ServerPlayer p,Animal a,Vec3 eye,Vec3 look){
        var to=a.getEyePosition().subtract(eye);if(to.lengthSqr()<1E-4)return true;
        if(to.normalize().dot(look)<Math.cos(Math.toRadians(55)))return false;
        return p.level().clip(new ClipContext(eye,a.getEyePosition(),ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
    }
    /** Animals go back to grazing once nobody is being watched by them. */
    static void release(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(Animal.class,bounds(b)))a.setNoAi(false);}
    /** Every stride, the step comes back a moment late, for the reader alone. */
    static void footsteps(ServerPlayer p,ServerLevel l){
        var last=STEPPED.get(p.getUUID());var now=p.position();
        if(last==null||!p.onGround()){STEPPED.put(p.getUUID(),now);return;}
        if(now.subtract(last).horizontalDistance()<STRIDE)return;
        STEPPED.put(p.getUUID(),now);
        var under=l.getBlockState(p.getOnPos());
        ECHOES.computeIfAbsent(p.getUUID(),k->new ArrayDeque<>()).add(new Echo(l.getGameTime()+ECHO,now,net.minecraft.core.Holder.direct(under.getSoundType().getStepSound())));
    }
    static void echoes(ServerPlayer p,ServerLevel l){
        var queue=ECHOES.get(p.getUUID());if(queue==null)return;
        while(!queue.isEmpty()&&queue.peek().at()<=l.getGameTime()){
            var e=queue.poll();p.connection.send(new ClientboundSoundPacket(e.sound(),SoundSource.PLAYERS,e.pos().x,e.pos().y,e.pos().z,.3F,.92F,p.getRandom().nextLong()));
        }
    }
    /** How many echoes are waiting for a reader (for tests). */
    public static int echoing(UUID id){var q=ECHOES.get(id);return q==null?0:q.size();}

    /** A death there is no death: it is healed, nothing is dropped, and the end begins at once. */
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void dying(LivingDeathEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!p.level().dimension().equals(HouseDimensions.OUTSIDE))return;
        var b=base(p.server);if(b==null||!bounds(b).contains(p.position())||p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR)return;
        e.setCanceled(true);p.setHealth(p.getMaxHealth());p.clearFire();p.setAirSupply(p.getMaxAirSupply());p.fallDistance=0;
        var own=own(p.server,p.getUUID());if(!own.getBoolean("Seen")||own.getInt("Ending")>0)begin(p);
        else LabyrinthDoors.sendBack(p,10,20,20);
    }
}
