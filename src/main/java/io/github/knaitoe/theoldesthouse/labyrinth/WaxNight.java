package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.WaxBlinkPayload;
import io.github.knaitoe.theoldesthouse.network.WaxLookPayload;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.750, the waxworks, the second part (docs/WAXWORKS_0_4_750.md): the night guard's flashlight and what the figures do around it.
 *
 * <ul>
 * <li><b>The flashlight.</b> The NIGHTS locker gives each reader one, lent: it lights where it is pointed while it is in either hand, and
 *   it stays in the museum.</li>
 * <li><b>The look.</b> A figure that a reader has at the edge of their sight, while nobody has it in front of them, turns its head
 *   toward that reader, a little at a time, and keeps the turn; it is that reader's own (0.4.751), drawn on their client
 *   (WaxLookPayload), and nobody else sees the head move.</li>
 * <li><b>The blink.</b> A figure whose face a reader holds in the beam, close, for four seconds, blinks once for that reader alone. Nobody
 *   else sees it, and nothing is heard.</li>
 * <li><b>The mirror.</b> The statesman across from the Hall's door, for a reader standing in front of him and facing him, does what they
 *   do a moment after they do it, turned as a reflection is. Each reader sees it for themselves (drawn by their own client).</li>
 * </ul>
 * Spectators are not on the shift: they borrow no light, turn no head and see no blink.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WaxNight {
    private WaxNight(){}
    public static final String LENT="WaxworksLent";
    /** The beam's reach and the light it leaves where it lands. */
    public static final double BEAM=9.5;public static final int BEAM_LIGHT=12;
    /** The look: who has a figure at the edge of their sight (between these angles off where they look, within this distance), how far
     *  a head goes in a step, and how far it can turn from where the modeller set it. Nobody may be looking straight at it. */
    public static final double EDGE_NEAR=35,EDGE_FAR=75,STRAIGHT=22,SIGHT=16;public static final float STEP=2.5F,TURN_MAX=55;
    /** The blink: the beam on the face, within this distance and this angle, for this long. */
    public static final double FACE_NEAR=8,FACE_ANGLE=9;public static final int HOLD=80,BLINK_TICKS=5,AGAIN=120;
    /** The mirror: in front of him (within this cone of his facing), this close, facing him, this long before it counts as seen. */
    public static final double MIRROR_NEAR=7,MIRROR_CONE=60,MIRROR_FACE=35;public static final int MIRROR_SEEN=40;

    /** Every reader's lit cell, and where each figure's head has got to at each museum. */
    static final Map<UUID,BlockPos> BEAMS=new ConcurrentHashMap<>();
    /** 0.4.751: each reader's own head turns, figure by figure, and what was last sent to them. */
    static final Map<UUID,float[]> TURNED=new ConcurrentHashMap<>();
    static final Map<UUID,float[]> SENT=new ConcurrentHashMap<>();
    /** Per reader: the figure under the beam and for how long; the mirror's count. */
    record Focus(int who,int ticks){}
    static final Map<UUID,Focus> FOCUS=new ConcurrentHashMap<>();
    static final Map<UUID,Integer> MIRRORED=new ConcurrentHashMap<>();

    // ------------------------------------------------------------------------------------------------ the flashlight
    public static ItemStack lend(ServerPlayer p){
        var s=new ItemStack(LastSeenRegistry.FLASHLIGHT.get());var t=new CompoundTag();t.putUUID(LENT,p.getUUID());
        s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));s.set(DataComponents.CUSTOM_NAME,Component.literal("Night guard's flashlight").withStyle(s1->s1.withItalic(false)));
        return s;
    }
    public static @Nullable UUID lentTo(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);if(d==null)return null;var t=d.copyTag();return t.hasUUID(LENT)?t.getUUID(LENT):null;}
    public static boolean lent(ItemStack s){return lentTo(s)!=null;}
    public static boolean mine(ItemStack s,ServerPlayer p){return s.is(LastSeenRegistry.FLASHLIGHT.get())&&p.getUUID().equals(lentTo(s));}
    public static boolean has(ServerPlayer p){return p.getInventory().hasAnyMatching(s->mine(s,p))||mine(p.containerMenu.getCarried(),p);}
    public static boolean holding(ServerPlayer p){return mine(p.getMainHandItem(),p)||mine(p.getOffhandItem(),p);}
    /** The NIGHTS locker: one flashlight to each reader, once a visit; a second hand on it finds the hook empty. */
    public static void locker(ServerPlayer p,CompoundTag own){
        if(p.isSpectator())return;
        if(has(p)){Waxworks.cue(p,WaxTexts.LOCKER_EMPTY);return;}
        LiteraryVignettes.give(p,lend(p));own.putBoolean("Light",true);
        Waxworks.cue(p,WaxTexts.LOCKER_OPEN);
    }
    static void reclaim(ServerPlayer p){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(lent(inv.getItem(i)))inv.setItem(i,ItemStack.EMPTY);
        if(lent(p.containerMenu.getCarried()))p.containerMenu.setCarried(ItemStack.EMPTY);
    }
    /** Dropped, it stays in hand: it is the museum's. */
    @SubscribeEvent public static void toss(ItemTossEvent e){
        var s=e.getEntity().getItem();if(!lent(s))return;
        e.setCanceled(true);
        if(e.getPlayer() instanceof ServerPlayer p&&p.getUUID().equals(lentTo(s))&&site(p)!=null){p.getInventory().add(s.copy());Waxworks.cue(p,WaxTexts.LIGHT_KEPT);}
    }
    @SubscribeEvent public static void drops(LivingDropsEvent e){e.getDrops().removeIf(i->lent(i.getItem()));}

    // ------------------------------------------------------------------------------------------------ who is where
    /** The museum a reader is in, or null. */
    public static @Nullable BlockPos site(ServerPlayer p){
        if(!p.level().dimension().equals(NovelRooms.dimension(Waxworks.PLACE)))return null;
        for(long s:Waxworks.SITES){var b=BlockPos.of(s);if(WaxSite.inside(p.position().subtract(b.getX(),b.getY(),b.getZ())))return b;}
        return null;
    }
    static boolean onShift(ServerPlayer p){return p.isAlive()&&!p.isSpectator();}
    /** Where a figure's eyes are. */
    static Vec3 eyes(LiteraryActor a){return a.getEyePosition();}
    /** How far off where a reader looks something is, in degrees. */
    static double off(ServerPlayer p,Vec3 at){var to=at.subtract(p.getEyePosition());if(to.lengthSqr()<1e-6)return 0;return Math.toDegrees(Math.acos(Mth.clamp(p.getLookAngle().dot(to.normalize()),-1,1)));}
    /** Nothing in the way of the eye but what can be seen past: the booth's glass, a rope, a plaque. */
    static boolean clear(ServerLevel l,ServerPlayer p,Vec3 at){
        var from=p.getEyePosition();
        for(int tries=0;tries<5;tries++){
            var hit=l.clip(new ClipContext(from,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
            if(hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(at)<.36)return true;
            var st=l.getBlockState(hit.getBlockPos());
            if(!(WaxBlock.is(st,WaxBlock.Kind.BOOTH_TOP)||WaxBlock.is(st,WaxBlock.Kind.ROPE)||WaxBlock.is(st,WaxBlock.Kind.PLAQUE)))return false;
            var step=at.subtract(from).normalize();from=hit.getLocation().add(step.scale(.05));
            while(l.getBlockState(BlockPos.containing(from)).equals(st)&&BlockPos.containing(from).equals(hit.getBlockPos())&&from.distanceToSqr(at)>.04)from=from.add(step.scale(.1));
        }
        return false;
    }
    /** How far this figure's head has turned for this reader. */
    public static float turned(UUID reader,int who){var t=TURNED.get(reader);return t==null?0:t[who];}

    // ------------------------------------------------------------------------------------------------ each tick
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        var l=server.getLevel(NovelRooms.dimension(Waxworks.PLACE));
        if(l==null){return;}
        // Lights out where the readers have gone, and lent lights back from anyone outside the museum.
        for(var id:List.copyOf(BEAMS.keySet())){var p=server.getPlayerList().getPlayer(id);if(p==null||p.level()!=l||!onShift(p)||!holding(p)||site(p)==null)unbeam(l,id);}
        if(server.getTickCount()%10==3)for(var p:server.getPlayerList().getPlayers())if(site(p)==null&&has(p))reclaim(p);
        if(Waxworks.SITES.isEmpty()){if(!TURNED.isEmpty()){TURNED.clear();SENT.clear();}return;}
        // A reader who has left forgets what turned.
        if(server.getTickCount()%20==7)for(var id:List.copyOf(TURNED.keySet())){var p=server.getPlayerList().getPlayer(id);if(p==null||site(p)==null){TURNED.remove(id);SENT.remove(id);if(p!=null)HousePackets.send(p,new WaxLookPayload(new int[0],new float[0]));}}
        var data=LabyrinthData.get(server);
        for(long s:List.copyOf(Waxworks.SITES)){
            var b=BlockPos.of(s);
            var readers=new ArrayList<ServerPlayer>();
            for(var p:l.players())if(onShift(p)&&b.equals(site(p)))readers.add(p);
            for(var p:readers)if(holding(p))beam(l,p);
            if(readers.isEmpty())continue;
            look(l,b,readers);
            if(server.getTickCount()%4==0)for(var p:readers)sendLook(l,b,p);
            for(var p:readers){blink(l,b,p,data);mirror(l,b,p,data);}
        }
    }
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){
        var l=e.getServer().getLevel(NovelRooms.dimension(Waxworks.PLACE));
        if(l!=null)for(var id:List.copyOf(BEAMS.keySet()))unbeam(l,id);
        BEAMS.clear();TURNED.clear();SENT.clear();FOCUS.clear();MIRRORED.clear();
    }

    /** The beam: a lit cell where it lands, in front of whatever it hits. */
    static void beam(ServerLevel l,ServerPlayer p){
        var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(BEAM));
        var hit=l.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        var cell=hit.getType()==HitResult.Type.BLOCK?hit.getBlockPos().relative(hit.getDirection()):BlockPos.containing(end);
        var old=BEAMS.get(p.getUUID());if(cell.equals(old))return;
        var st=l.getBlockState(cell);
        if(!st.isAir()&&!st.is(Blocks.LIGHT)){cell=BlockPos.containing(hit.getLocation().subtract(p.getLookAngle().scale(.5)));st=l.getBlockState(cell);if(cell.equals(old))return;}
        unbeam(l,p.getUUID());
        if(st.isAir()){l.setBlock(cell,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,BEAM_LIGHT),Block.UPDATE_CLIENTS);BEAMS.put(p.getUUID(),cell.immutable());}
        else if(st.is(Blocks.LIGHT))BEAMS.put(p.getUUID(),cell.immutable());
    }
    static void unbeam(ServerLevel l,UUID id){
        var old=BEAMS.remove(id);if(old==null)return;
        for(var other:BEAMS.values())if(old.equals(other))return;
        if(l.isLoaded(old)&&l.getBlockState(old).is(Blocks.LIGHT))l.setBlock(old,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
    }
    public static @Nullable BlockPos beamOf(UUID id){return BEAMS.get(id);}

    /** The look, each reader's own: a figure nobody is looking straight at, that a reader has at the edge of their sight, turns its head
     *  a little toward that reader. */
    static void look(ServerLevel l,BlockPos b,List<ServerPlayer> readers){
        for(int who=0;who<Waxworks.FIGURES;who++){
            if(who==WaxSite.MIRROR||WaxSite.FIGURES.get(who).pose()==WaxSite.COVERED)continue;
            var a=Waxworks.find(l,b,who);if(a==null||a.isInvisible())continue;
            var at=eyes(a);boolean watched=false;
            for(var p:readers){double d=p.getEyePosition().distanceTo(at);if(d<=SIGHT&&off(p,at)<STRAIGHT&&clear(l,p,at)){watched=true;break;}}
            if(watched)continue;
            float set=a.getYRot();
            for(var p:readers){
                double d=p.getEyePosition().distanceTo(at);if(d>SIGHT)continue;
                double off=off(p,at);if(off<EDGE_NEAR||off>EDGE_FAR||!clear(l,p,at))continue;
                var to=p.getEyePosition().subtract(at);
                float want=Mth.clamp(Mth.wrapDegrees((float)(Mth.atan2(to.z,to.x)*Mth.RAD_TO_DEG)-90-set),-TURN_MAX,TURN_MAX);
                var turn=TURNED.computeIfAbsent(p.getUUID(),k->new float[Waxworks.FIGURES]);
                float now=turn[who];if(Math.abs(want-now)<.5F)continue;
                turn[who]=now+Mth.clamp(want-now,-STEP,STEP);
            }
        }
    }
    /** What has turned for this reader, sent to their client when it changes. */
    static void sendLook(ServerLevel l,BlockPos b,ServerPlayer p){
        var turn=TURNED.get(p.getUUID());if(turn==null)return;
        var last=SENT.get(p.getUUID());if(last!=null&&java.util.Arrays.equals(last,turn))return;
        int n=0;for(float t:turn)if(t!=0)n++;
        int[] ids=new int[n];float[] by=new float[n];int k=0;
        for(int who=0;who<Waxworks.FIGURES;who++)if(turn[who]!=0){var a=Waxworks.find(l,b,who);ids[k]=a==null?-1:a.getId();by[k]=turn[who];k++;}
        HousePackets.send(p,new WaxLookPayload(ids,by));SENT.put(p.getUUID(),turn.clone());
    }
    /** A figure that has been moved (WaxShift) starts again from where it was set: nobody's turn of its head carries over. */
    static void unturn(int who){for(var t:TURNED.values())t[who]=0;}

    /** The blink, private: the beam held on a face, close, for four seconds. */
    static void blink(ServerLevel l,BlockPos b,ServerPlayer p,LabyrinthData data){
        int who=-1;
        if(holding(p))for(int i=0;i<Waxworks.FIGURES;i++){
            if(WaxSite.FIGURES.get(i).pose()==WaxSite.COVERED)continue;
            var a=Waxworks.find(l,b,i);if(a==null||a.isInvisible())continue;var at=eyes(a);
            if(p.getEyePosition().distanceTo(at)<=FACE_NEAR&&off(p,at)<=FACE_ANGLE&&clear(l,p,at)){who=i;break;}
        }
        var f=FOCUS.get(p.getUUID());
        if(who<0){if(f!=null)FOCUS.remove(p.getUUID());return;}
        int t=f!=null&&f.who()==who?f.ticks()+1:1;
        if(t<HOLD){FOCUS.put(p.getUUID(),new Focus(who,t));return;}
        var a=Waxworks.find(l,b,who);
        if(a!=null)HousePackets.send(p,new WaxBlinkPayload(a.getId(),BLINK_TICKS));
        FOCUS.put(p.getUUID(),new Focus(who,HOLD-AGAIN));
        var own=LiteraryVignettes.personal(data,p.getUUID(),Waxworks.PLACE);
        own.putInt("Blinked",own.getInt("Blinked")|1<<who);own.putInt("Blinks",own.getInt("Blinks")+1);
        LiteraryVignettes.save(data,p.getUUID(),Waxworks.PLACE,own);
    }

    /** The mirror: counted once a reader has stood in front of him, facing him, for two seconds. What he does is drawn on their client. */
    public static boolean before(Vec3 reader,Vec3 look,Vec3 feet,float yaw){
        var to=reader.subtract(feet);double d=Math.sqrt(to.x*to.x+to.z*to.z);if(d>MIRROR_NEAR||d<.5)return false;
        double facing=Math.toRadians(yaw);double fx=-Math.sin(facing),fz=Math.cos(facing);
        if(Math.toDegrees(Math.acos(Mth.clamp((to.x*fx+to.z*fz)/d,-1,1)))>MIRROR_CONE)return false;
        double lx=look.x,lz=look.z,ll=Math.sqrt(lx*lx+lz*lz);if(ll<1e-4)return false;
        return Math.toDegrees(Math.acos(Mth.clamp((-to.x*lx-to.z*lz)/(d*ll),-1,1)))<=MIRROR_FACE;
    }
    static void mirror(ServerLevel l,BlockPos b,ServerPlayer p,LabyrinthData data){
        var f=WaxSite.FIGURES.get(WaxSite.MIRROR);
        var feet=WaxSite.feet(f).add(b.getX(),b.getY(),b.getZ());
        if(!before(p.position(),p.getLookAngle(),feet,f.yaw())||!clear(l,p,feet.add(0,1.6,0))){MIRRORED.remove(p.getUUID());return;}
        int t=MIRRORED.merge(p.getUUID(),1,Integer::sum);
        if(t!=MIRROR_SEEN)return;
        var own=LiteraryVignettes.personal(data,p.getUUID(),Waxworks.PLACE);
        if(own.getBoolean("Mirrored"))return;
        own.putBoolean("Mirrored",true);LiteraryVignettes.save(data,p.getUUID(),Waxworks.PLACE,own);
    }
}
