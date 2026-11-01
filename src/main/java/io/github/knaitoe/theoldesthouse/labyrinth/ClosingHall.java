package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.209, the closing hall (docs/CLOSING_HALL_0_4_209.md), the first of the dark halls' horrors: Chad and Daisy Navidson at the
 * end of a long dark hall, too frightened to move.
 *
 * <p>Reading Daisy's drawing starts it, for everyone in the hall. The way in is sealed behind them, a squeeze cracks open low in
 * the west wall near it, and the walls and ceiling step in, a stage at a time, with something breathing under every step, until
 * the hall is shut. The children come only while held: by the hand, at a reader's heel, or on a reader's back, which slows them
 * to a crawl's pace. The hall closes on readers, pushing and crushing; it never closes on a child, and when a step would, it
 * waits, up to five seconds, a hand's width off them. In the squeeze a child goes first: right-click one held at its mouth and
 * they crawl through and run off into the dark past it; a reader who goes into the squeeze holding a child ahead of them loses
 * the child, who will not follow. With the hall nearly shut, going back to a child still in it pushes the child through and the
 * hall shuts on the reader: a real House death. A child nobody brings out is left in the wall.
 *
 * <p>Both children through, and the reader through after them, readies Karen's interview ({@code sent_them_through}); dying to
 * push the last one through readies it too ({@code went_back}), to be read on a later visit at the copy by the way in. Leaving a
 * child fails the visit: the child calls the reader's name from inside the wall for as long as they stay, and the first door they
 * open on every later visit to the dark halls has a small handprint on its inside, until they bring both children out.
 *
 * <p>Two readers or more present at the start: the hall closes twice as fast; each can take a hand and nobody need carry.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ClosingHall {
    private ClosingHall(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.CLOSING_HALL;
    public static final String[] CHILDREN={"Chad","Daisy"};
    /** Stranger phases for the two children (painted by tools/paint_character_skins.py). */
    public static final int CHAD_PHASE=39,DAISY_PHASE=40;
    public static boolean child(int phase){return phase==CHAD_PHASE||phase==DAISY_PHASE;}
    /** Ticks between stages (alone), the last stage's longer wait, how long a step waits for a child, the squeeze's fill. */
    public static final int INTERVAL=80,FINAL=120,WAIT_MAX=100,TUNNEL_DELAY=20,TUNNEL_STEP=16;
    /** A crushed reader's damage, and how often it can be dealt. */
    public static final float CRUSH=3F;public static final int CRUSH_TICKS=20;
    /** Test hooks: stages come every tick once started; or never on their own, only by {@link #stage}. */
    public static boolean TEST_FAST=false,TEST_HOLD=false;
    /** Crushed by the hall: through armour and through the moment's grace after joining, because a wall that has shut does not wait for either. */
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> CRUSHED_IN=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"closing_hall"));
    static net.minecraft.world.damagesource.DamageSource crushing(ServerLevel l){return l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolder(CRUSHED_IN).map(net.minecraft.world.damagesource.DamageSource::new).orElse(l.damageSources().inWall());}
    static final ResourceLocation CARRY=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"closing_carry");
    private static final Map<UUID,Long> CRUSHED=new HashMap<>(),SAID=new HashMap<>(),CALLED=new HashMap<>();
    private static final List<long[]> VOICES=new ArrayList<>();
    private static final Map<UUID,Long> PRINTS=new HashMap<>();

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static CompoundTag set(MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),PLACE);}
    static void set(MinecraftServer s,CompoundTag t){LiteraryVignettes.shared(LabyrinthData.get(s),PLACE,t);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.WHITE),true);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(s),SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    static List<ServerPlayer> group(MinecraftServer s,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var q:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(q)&&inside(q)&&b.equals(base(q)))out.add(q);return out;}
    public static int stage(CompoundTag t){return t.getInt("Stage");}
    public static boolean running(CompoundTag t){return t.getBoolean("Run")&&!t.getBoolean("Over");}
    static int interval(CompoundTag t,int nextStage){if(TEST_FAST)return 1;int i=nextStage>=9?FINAL:INTERVAL;return t.getBoolean("Coop")?i/2:i;}

    // ------------------------------------------------------------------------------------------------ the children
    @Nullable public static LiteraryActor child(ServerPlayer p,String who){
        var a=LiteraryVignettes.actor(p,PLACE,who,LiteraryActor.STRANGER,who.equals("Chad")?ClosingHallSite.CHAD:ClosingHallSite.DAISY,false);
        if(a!=null&&a.phase()!=phase(who)){a.appearance(LiteraryActor.STRANGER,phase(who));a.setYRot(0);a.setYBodyRot(0);a.setYHeadRot(0);}
        return a;
    }
    static int phase(String who){return who.equals("Chad")?CHAD_PHASE:DAISY_PHASE;}
    @Nullable static LiteraryActor find(ServerLevel l,CompoundTag t,String who){if(!t.hasUUID(who))return null;var e=l.getEntity(t.getUUID(who));return e instanceof LiteraryActor a?a:null;}
    @Nullable static String who(LiteraryActor a){return a.phase()==CHAD_PHASE?"Chad":a.phase()==DAISY_PHASE?"Daisy":null;}
    /** A child still in the hall: not yet sent through, run off or left in the wall. */
    static boolean stillIn(CompoundTag t,String who){return t.getInt(who+"Through")==0&&!t.getBoolean(who+"Gone")&&!t.getBoolean(who+"Left");}
    @Nullable static UUID holder(CompoundTag t,String who){return t.hasUUID(who+"Hand")?t.getUUID(who+"Hand"):t.hasUUID(who+"Back")?t.getUUID(who+"Back"):null;}
    static void release(CompoundTag t,String who){t.remove(who+"Hand");t.remove(who+"Back");}
    static boolean carrying(CompoundTag t,UUID id){for(var who:CHILDREN)if(t.hasUUID(who+"Back")&&t.getUUID(who+"Back").equals(id))return true;return false;}
    static boolean leading(CompoundTag t,UUID id){for(var who:CHILDREN)if(t.hasUUID(who+"Hand")&&t.getUUID(who+"Hand").equals(id))return true;return false;}

    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        for(var who:CHILDREN)child(p,who);
        var t=set(p.server);
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")){cue(p,"A folder of typed pages lies just inside the door, where you can reach it.");return;}
        if(running(t))cue(p,"Stone grinding, far up the hall. It is already closing.");
        else if(t.getBoolean("Over"))cue(p,"Ash, a hand's breadth in front of your face. The hall is shut.");
        else cue(p,"Somewhere at the far end of the hall, very small, two children are crying.");
    }

    /** Daisy's drawing is opened: the closing waits until the reader looks up from it. */
    public static void drawing(ServerPlayer p,BlockPos b){
        var t=set(p.server);if(t.getBoolean("Run")||t.getBoolean("Over"))return;
        if(!ClosingHallSite.inHall(rel(p,b)))return;
        t.putUUID("Pending",p.getUUID());t.putLong("Base",b.asLong());set(p.server,t);
    }
    static void start(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        t.putBoolean("Run",true);t.putLong("Base",b.asLong());t.putInt("Stage",0);t.remove("Pending");
        var present=new net.minecraft.nbt.ListTag();int n=0;
        for(var q:group)if(ClosingHallSite.inHall(rel(q,b))){present.add(net.minecraft.nbt.StringTag.valueOf(q.getStringUUID()));n++;var own=own(q);own.putInt("Runs209",own.getInt("Runs209")+1);save(q,own);}
        t.put("Present",present);t.putBoolean("Coop",n>=2);
        advance(l,b,t,group,1);
        for(var q:group){var r=rel(q,b);cue(q,"Far behind you, where you came in, stone meets stone. Low in the west wall near it, something cracks open.");
            heard(q,Vec3.atCenterOf(b.offset(0,1,-1)),HorrorRegistry.CLOSING_CRACK.get(),1F,.9F);
            if(r.z<-30)say(q,ClosingHallTexts.START[q.getRandom().nextInt(ClosingHallTexts.START.length)]);}
    }
    static boolean present(CompoundTag t,UUID id){for(var e:t.getList("Present",8))if(e.getAsString().equals(id.toString()))return true;return false;}

    /** Whether any child still in the hall stands in a cell. */
    static boolean childAt(ServerLevel l,CompoundTag t,BlockPos at){
        var box=new AABB(at);
        for(var who:CHILDREN){if(!stillIn(t,who)&&t.getInt(who+"Through")==0)continue;var a=find(l,t,who);if(a!=null&&a.getBoundingBox().intersects(box))return true;}
        return false;
    }
    /** The next stage, if no child stands in a cell it would close; otherwise the hall waits, up to {@link #WAIT_MAX}. */
    static boolean advance(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,int next){
        int now=stage(t);
        if(t.getInt("Wait")<WAIT_MAX){
            for(int z=ClosingHallSite.FAR_Z;z<=ClosingHallSite.NEAR_Z;z++)for(int x=-ClosingHallSite.HALF;x<=ClosingHallSite.HALF;x++)for(int y=0;y<ClosingHallSite.HIGH;y++){
                if(!ClosingHallSite.closed(next,x,y,z)||ClosingHallSite.closed(now,x,y,z))continue;
                if(childAt(l,t,b.offset(x,y,z))){
                    int w=t.getInt("Wait");t.putInt("Wait",w+1);
                    if(w%20==0)for(var who:CHILDREN){var a=find(l,t,who);if(a==null||!stillIn(t,who))continue;
                        for(var q:group)if(q.distanceToSqr(a)<400){cue(q,ClosingHallTexts.waiting(who));heard(q,a.position(),HorrorRegistry.CLOSING_HOLD.get(),.8F,1F);}}
                    return false;
                }
            }
        }
        t.putInt("Wait",0);t.putInt("Stage",next);t.putInt("Next",interval(t,next+1));
        ClosingHallSite.close(l,b,next,at->childAt(l,t,at));
        for(var q:group){var r=rel(q,b);
            Vec3 at=ClosingHallSite.inHall(r)?q.position():Vec3.atCenterOf(b.offset(0,1,-20));
            heard(q,at.add(q.getLookAngle().scale(-2)),HorrorRegistry.CLOSING_GRIND.get(),1F,.85F+q.getRandom().nextFloat()*.1F);
            q.connection.send(Growl.packet(new Growl.Voice(Growl.Kind.BELOW,q.position().add(0,-6,0),.55F,.8F,q.getRandom().nextLong())));
        }
        crush(l,b,t,group);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(rel.equals(ClosingHallSite.TRANSCRIPT)||rel.equals(ClosingHallSite.TRANSCRIPT_DOOR)){
            if(own.getBoolean("Ready"))LiteraryVignettes.open(p,PLACE,own,"Ending",LiteraryTexts.ending(p,PLACE,own),b.offset(rel));
            else cue(p,"Typed pages in a folder. The top page is blank.");
            return true;
        }
        // At the squeeze's mouth, a child held or carried can be put through by reaching into it.
        var t=set(p.server);
        if(running(t)&&rel.distManhattan(ClosingHallSite.mouth(stage(t)))<=2){
            for(String key:new String[]{"Back","Hand"})for(var who:CHILDREN){if(!t.hasUUID(who+key)||!t.getUUID(who+key).equals(p.getUUID()))continue;var a=find(p.serverLevel(),t,who);if(a==null)continue;
                say(p,ClosingHallTexts.squeeze(who));release(t,who);send(p.serverLevel(),b,t,who,a);own.putInt("Sent209",own.getInt("Sent209")+1);set(p.server,t);return true;}
            return false;
        }
        if(rel.equals(ClosingHallSite.CRAYONS)){cue(p,"Crayons, worn down to stubs, the paper peeled off them. Most of the dark ones are gone.");return true;}
        return false;
    }
    /** A child right-clicked: take their hand, take them on your back, or put them through the squeeze. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        String who=who(a);var b=base(p);if(who==null||b==null)return;var l=p.serverLevel();var t=set(p.server);var id=p.getUUID();
        if(!stillIn(t,who))return;
        if(!running(t)){say(p,ClosingHallTexts.WAITING[(who.equals("Chad")?0:1)+2*p.getRandom().nextInt(2)]);return;}
        int st=stage(t);UUID h=holder(t,who);
        // With the hall nearly shut, going back for a child still in it: they go through, and it shuts on you.
        if(st>=8&&(h==null||!h.equals(id))&&ClosingHallSite.inHall(rel(p,b))){
            cue(p,ClosingHallTexts.wentBack(who));release(t,who);send(l,b,t,who,a);
            own.putString("WentBack209",who);
            boolean bothOut=true;for(var c:CHILDREN)if(stillIn(t,c))bothOut=false;
            if(bothOut){LiteraryVignettes.ready(p,PLACE,own,"went_back");}
            t.putInt("Stage",9);t.putInt("Next",interval(t,10));ClosingHallSite.close(l,b,9,at->childAt(l,t,at));crush(l,b,t,group(p.server,b));
            set(p.server,t);return;
        }
        if(h!=null&&!h.equals(id)){cue(p,who+" won't let go of the other hand to take yours.");return;}
        var mouth=Vec3.atCenterOf(b.offset(ClosingHallSite.mouth(st)));
        if(h!=null){
            if(a.position().distanceTo(mouth)<=3.2||p.position().distanceTo(mouth)<=2.5){say(p,ClosingHallTexts.squeeze(who));release(t,who);send(l,b,t,who,a);own.putInt("Sent209",own.getInt("Sent209")+1);set(p.server,t);return;}
            cue(p,who+" holds on tighter.");return;
        }
        if(!leading(t,id)){t.putUUID(who+"Hand",id);say(p,ClosingHallTexts.hand(who));}
        else if(!carrying(t,id)){t.putUUID(who+"Back",id);say(p,ClosingHallTexts.back(who));}
        else{cue(p,"Your hands are full.");return;}
        own.putBoolean("Held209",true);set(p.server,t);
    }
    /** Sends a child through the squeeze: they crawl along it from its mouth and run off up the passage past it. */
    static void send(ServerLevel l,BlockPos b,CompoundTag t,String who,LiteraryActor a){
        t.putInt(who+"Through",1);var m=b.offset(ClosingHallSite.mouth(stage(t)));
        a.moveTo(m.getX()+.5,m.getY(),m.getZ()+.5,90,0);a.setNoGravity(true);a.noPhysics=true;a.gesture(CostumeNight.CRAWL);
    }

    // ------------------------------------------------------------------------------------------------ the hall
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();
        voices(s,tick);
        BlockPos b=null;var group=new ArrayList<ServerPlayer>();
        for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(p)&&inside(p)){var pb=base(p);if(pb==null)continue;if(b==null)b=pb;if(pb.equals(b))group.add(p);}
        var t=set(s);
        for(var p:s.getPlayerList().getPlayers())carry(p,b!=null&&group.contains(p)&&carrying(t,p.getUUID()));
        if(b==null){
            if(!t.getBoolean("Run")&&!t.contains("Pending"))return;
            int empty=t.getInt("Empty")+1;t.putInt("Empty",empty);
            if(empty<100){set(s,t);return;}
            var l=s.getLevel(NovelRooms.dimension(PLACE));var rb=BlockPos.of(t.getLong("Base"));
            if(l==null||!l.isLoaded(rb)||LiteraryVignettes.watched(l,Vec3.atCenterOf(rb.offset(0,1,-20)))){set(s,t);return;}
            reset(l,rb,t);return;
        }
        var l=group.get(0).serverLevel();t.putInt("Empty",0);
        if(t.contains("Pending")&&!t.getBoolean("Run")){
            var reader=s.getPlayerList().getPlayer(t.getUUID("Pending"));
            if(reader==null||!group.contains(reader))t.remove("Pending");
            else if(!(reader.containerMenu instanceof LiteraryVignettes.Pages))start(l,b,t,group);
        }
        if(!t.getBoolean("Run")){idle(l,b,t,group,tick);set(s,t);return;}
        if(!t.getBoolean("Over")){
            int st=stage(t);
            if(st<9){if(!TEST_HOLD){int next=t.getInt("Next")-1;t.putInt("Next",next);if(next<=0)advance(l,b,t,group,st+1);}}
            else squeezeFill(l,b,t,group);
            if(tick%10==0&&stage(t)>=1){ClosingHallSite.close(l,b,stage(t),at->childAt(l,t,at));crush(l,b,t,group);}
            leftBehind(l,b,t,group);
        }
        children(l,b,t,group,tick);
        outcomes(l,b,t,group);
        for(var p:group)if(t.getBoolean("Failed")&&ClosingHallSite.past(rel(p,b))&&present(t,p.getUUID())){long last=CALLED.getOrDefault(p.getUUID(),0L);if(tick-last>=120){CALLED.put(p.getUUID(),tick);
            String left=t.getString("LeftNames");String one=left.contains("Daisy")?"Daisy":"Chad";heard(p,Vec3.atCenterOf(b.offset(ClosingHallSite.TUNNEL_IN,0,ClosingHallSite.SQUEEZE_Z)),HorrorRegistry.CLOSING_WALL.get(),.9F,1F);cue(p,ClosingHallTexts.calling(one,p.getGameProfile().getName()));}}
        set(s,t);
    }
    /** Before it starts: the children crying at the far end, a word to a reader who comes near. */
    static void idle(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,long tick){
        for(var who:CHILDREN){var a=find(l,t,who);if(a==null)continue;var start=who.equals("Chad")?ClosingHallSite.CHAD:ClosingHallSite.DAISY;
            if(a.blockPosition().distManhattan(b.offset(start))>1){a.moveTo(Vec3.atBottomCenterOf(b.offset(start)));a.setYRot(0);a.setYBodyRot(0);}}
        if(tick%90==0)for(var q:group){var r=rel(q,b);if(r.z<-12)heard(q,Vec3.atCenterOf(b.offset(0,1,-44)),HorrorRegistry.CLOSING_CRY.get(),.5F+(float)Math.min(.5,(-r.z-12)/60),1F);}
        for(var q:group){var r=rel(q,b);if(r.z>-36)continue;long last=SAID.getOrDefault(q.getUUID(),0L);if(tick-last<200)continue;SAID.put(q.getUUID(),tick);say(q,ClosingHallTexts.WAITING[q.getRandom().nextInt(ClosingHallTexts.WAITING.length)]);}
    }
    /** Every reader in a cell that has closed is pushed out of it, hurt, into the nearest room left; with no room left, crushed. */
    static void crush(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        int st=stage(t);long now=l.getGameTime();
        for(var q:group){var r=rel(q,b);if(!ClosingHallSite.inHall(r)||q.getAbilities().invulnerable)continue;
            var feet=q.blockPosition();boolean in=!l.getBlockState(feet).isAir()&&l.getBlockState(feet).is(DarkHallRegistry.material("ash"))||st<8&&l.getBlockState(feet.above()).is(DarkHallRegistry.material("ash"));
            if(!in&&st<9)continue;
            if(st>=9){q.hurt(crushing(l),1000F);continue;}
            var to=room(l,b,st,q);
            if(to==null){q.hurt(crushing(l),1000F);continue;}
            q.teleportTo(to.x,to.y,to.z);
            if(now-CRUSHED.getOrDefault(q.getUUID(),0L)>=CRUSH_TICKS){CRUSHED.put(q.getUUID(),now);q.hurt(crushing(l),CRUSH);cue(q,"The wall shoves you aside.");}
        }
    }
    /** The nearest open cell in the hall a reader fits in at this stage (lying flat, at the last stage). */
    @Nullable static Vec3 room(ServerLevel l,BlockPos b,int st,ServerPlayer q){
        var r=rel(q,b);int hw=ClosingHallSite.halfWidth(st);int z=Mth.clamp(Mth.floor(r.z),ClosingHallSite.FAR_Z,ClosingHallSite.NEAR_Z-1);
        double best=1e9;Vec3 out=null;
        for(int dz=-2;dz<=2;dz++)for(int x=-hw;x<=hw;x++){int cz=z+dz;if(cz>ClosingHallSite.NEAR_Z-1||cz<ClosingHallSite.FAR_Z)continue;var at=b.offset(x,0,cz);
            if(!l.getBlockState(at).isAir())continue;if(st<8&&!l.getBlockState(at.above()).isAir())continue;
            double d=Math.abs(x+.5-r.x)+Math.abs(cz+.5-r.z);if(d<best){best=d;out=new Vec3(at.getX()+.5,at.getY(),at.getZ()+.5);}}
        return out;
    }
    /** After the hall shuts, the squeeze fills behind it from the hall side, a cell at a time; it waits for a child, pushes a reader. */
    static void squeezeFill(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        int delay=t.getInt("TunnelDelay");if(delay<TUNNEL_DELAY){t.putInt("TunnelDelay",delay+1);return;}
        int x=t.contains("TunnelX")?t.getInt("TunnelX"):ClosingHallSite.TUNNEL_IN;
        int wait=t.getInt("TunnelWait")+1;t.putInt("TunnelWait",wait);if(wait<(TEST_FAST?1:TUNNEL_STEP))return;t.putInt("TunnelWait",0);
        if(x<ClosingHallSite.TUNNEL_OUT){t.putBoolean("Over",true);return;}
        var at=b.offset(x,0,ClosingHallSite.SQUEEZE_Z);
        if(childAt(l,t,at))return;
        for(var q:group)if(q.getBoundingBox().intersects(new AABB(at))){var to=at.west();q.teleportTo(to.getX()+.5,to.getY(),to.getZ()+.5);q.hurt(crushing(l),2F);cue(q,"The stone comes down on your heels.");}
        ClosingHallSite.fillTunnel(l,b,x);t.putInt("TunnelX",x-1);
        for(var q:group)heard(q,Vec3.atCenterOf(at),HorrorRegistry.CLOSING_GRIND.get(),.7F,1.1F);
    }
    /** No reader left in the hall, a child still in it: the child is left in the wall. */
    static void leftBehind(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        if(stage(t)<1)return;
        for(var q:group)if(ClosingHallSite.inHall(rel(q,b))&&q.isAlive())return;
        var names=new ArrayList<String>();
        for(var who:CHILDREN){if(!stillIn(t,who))continue;var a=find(l,t,who);
            if(a!=null&&LiteraryVignettes.watched(l,a.getEyePosition()))continue;
            t.putBoolean(who+"Left",true);release(t,who);if(a!=null)a.discard();t.remove(who);names.add(who);}
        if(names.isEmpty())return;
        String all=t.getString("LeftNames");for(var n:names)all=all.isEmpty()?n:all+" and "+n;t.putString("LeftNames",all);t.putBoolean("Failed",true);
        if(stage(t)<9){t.putInt("Stage",8);t.putInt("Next",1);}
    }
    /** Where each child goes this tick. */
    static void children(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,long tick){
        var players=l.getServer().getPlayerList();
        for(var who:CHILDREN){var a=find(l,t,who);if(a==null)continue;
            int through=t.getInt(who+"Through");
            if(through>0){through++;t.putInt(who+"Through",through);if(!crawl(l,b,t,who,a,through)){a.discard();t.remove(who);t.putBoolean(who+"Gone",true);t.putInt(who+"Through",0);away(l,b,group,who,tick);}continue;}
            if(!stillIn(t,who))continue;
            UUID h=holder(t,who);var q=h==null?null:players.getPlayer(h);
            if(h!=null&&(q==null||!group.contains(q)||!q.isAlive())){release(t,who);continue;}
            if(q==null)continue;
            if(ClosingHallSite.inSqueeze(rel(q,b),stage(t))){release(t,who);say(q,ClosingHallTexts.refuse(who));continue;}
            boolean back=t.hasUUID(who+"Back");
            Vec3 look=new Vec3(q.getLookAngle().x,0,q.getLookAngle().z);if(look.lengthSqr()<1e-4)look=new Vec3(0,0,1);look=look.normalize();
            Vec3 goal=back?q.position().add(look.scale(-.32)).add(0,q.getBbHeight()*.48,0):q.position().add(look.scale(-1.1)).add(new Vec3(-look.z,0,look.x).scale(.45));
            if(!back){var gr=rel(goal,b);int hw=ClosingHallSite.halfWidth(stage(t));goal=new Vec3(b.getX()+Mth.clamp(gr.x,-hw+.3,hw+.7),q.getY(),b.getZ()+Mth.clamp(gr.z,ClosingHallSite.FAR_Z+.3,ClosingHallSite.NEAR_Z+.7));}
            var from=a.position();var step=goal.subtract(from);double d=step.length();
            Vec3 to=d<=.6||back?goal:from.add(step.scale(.6/d));
            a.setNoGravity(true);a.noPhysics=true;a.setDeltaMovement(Vec3.ZERO);
            float yaw=q.getYRot();a.moveTo(to.x,to.y,to.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        }
    }
    /** A child sent through: along the squeeze on their stomach, into the room past it, up the passage north, and gone. */
    static boolean crawl(ServerLevel l,BlockPos b,CompoundTag t,String who,LiteraryActor a,int ticks){
        var r=rel(a.position(),b);Vec3 goal;double speed;
        if(r.x>ClosingHallSite.ROOM_X1+1){goal=new Vec3(ClosingHallSite.ROOM_X1+.5,0,ClosingHallSite.SQUEEZE_Z+.5);speed=.14;}
        else if(r.z>ClosingHallSite.ROOM_Z0-1){a.gesture(0);goal=new Vec3(ClosingHallSite.RUN_X0+1,0,ClosingHallSite.ROOM_Z0-1.5);speed=.22;}
        else{a.gesture(0);goal=new Vec3(ClosingHallSite.RUN_X0+1,0,ClosingHallSite.RUN_Z_END+1.5);speed=.26;if(r.z<ClosingHallSite.RUN_Z_END+3)return false;}
        var step=goal.subtract(r);double d=step.length();var to=d<=speed?goal:r.add(step.scale(speed/d));
        float yaw=(float)Math.toDegrees(Math.atan2(-step.x,step.z));
        a.moveTo(b.getX()+to.x,b.getY()+to.y,b.getZ()+to.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        return ticks<2400;
    }
    /** The children's voices up the passage and on, much further than there is passage, for much too long. */
    static void away(ServerLevel l,BlockPos b,List<ServerPlayer> group,String who,long tick){
        for(var q:group)for(int k=0;k<10;k++)VOICES.add(new long[]{tick+20+k*24,q.getUUID().getMostSignificantBits(),q.getUUID().getLeastSignificantBits(),b.getX()+ClosingHallSite.RUN_X0,b.getY()+1,b.getZ()+ClosingHallSite.RUN_Z_END-k*9,k});
    }
    static void voices(MinecraftServer s,long tick){
        for(var it=VOICES.iterator();it.hasNext();){var v=it.next();if(tick<v[0])continue;it.remove();var p=s.getPlayerList().getPlayer(new UUID(v[1],v[2]));if(p==null)continue;
            heard(p,new Vec3(v[3],v[4],v[5]),HorrorRegistry.CLOSING_CALL.get(),Math.max(.15F,1F-v[6]*.09F),1F);}
    }
    /** Readies or fails each reader who was there at the start, once everyone is out of the hall. */
    static void outcomes(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        boolean anyIn=false;for(var who:CHILDREN)if(stillIn(t,who)||t.getInt(who+"Through")>0)anyIn=true;
        if(anyIn&&!t.getBoolean("Failed"))return;
        for(var q:group){if(!present(t,q.getUUID())||!ClosingHallSite.past(rel(q,b)))continue;var own=own(q);
            if(own.getInt("Settled209")==own.getInt("Runs209"))continue;own.putInt("Settled209",own.getInt("Runs209"));
            if(t.getBoolean("Failed")){
                String left=t.getString("LeftNames");own.putString("LeftBehind209",left);
                cue(q,left+(left.contains(" and ")?" are":" is")+" still in there.");
            }else{
                LiteraryVignettes.ready(q,PLACE,own,"sent_them_through");
                cue(q,"Far up the passage, two children are calling to each other, running, further and further away.");
            }
            save(q,own);
        }
    }
    /** The hall empty and unwatched: open again, the children back at its end, the squeeze shut. */
    static void reset(ServerLevel l,BlockPos b,CompoundTag t){
        for(var who:CHILDREN){var a=find(l,t,who);if(a!=null)a.discard();}
        ClosingHallSite.build(l,b);
        var fresh=new CompoundTag();fresh.putLong("Base",b.asLong());set(l.getServer(),fresh);
    }
    /** Carrying a child slows a reader to a crawl's pace. */
    static void carry(ServerPlayer p,boolean carrying){var a=p.getAttribute(Attributes.MOVEMENT_SPEED);if(a==null)return;
        if(carrying&&!a.hasModifier(CARRY))a.addTransientModifier(new AttributeModifier(CARRY,-.55,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        else if(!carrying&&a.hasModifier(CARRY))a.removeModifier(CARRY);}

    /** In the squeeze, and at its mouth, a reader goes on their stomach; at the last stage the whole hall is a crawl. */
    public static boolean crawling(ServerPlayer p,BlockPos b){
        var t=set(p.server);if(!t.getBoolean("Run"))return false;int st=stage(t);var r=rel(p,b);
        if(ClosingHallSite.inTunnel(r))return true;
        if(st>=1&&st<9&&Mth.floor(r.z)==ClosingHallSite.SQUEEZE_Z&&Mth.floor(r.x)<=-ClosingHallSite.halfWidth(st)&&r.y<1)return true;
        return st==8&&ClosingHallSite.inHall(r);
    }
    /** While it closes, nobody in the hall goes back out the way they came. */
    public static boolean locked(ServerPlayer p){if(!inside(p))return false;var b=base(p);if(b==null)return false;var t=set(p.server);return running(t)&&ClosingHallSite.inHall(rel(p,b));}
    public static boolean leftSomeone(ServerPlayer p){var own=own(p);return !own.getString("LeftBehind209").isEmpty()&&!own.getBoolean("Completed");}

    /** The first door a reader opens on a visit to the dark halls: a small handprint on its inside, for a reader who left a child. */
    public static void firstDarkDoor(ServerPlayer p,BlockPos inside){if(!leftSomeone(p))return;PRINTS.put(p.getUUID(),p.server.getTickCount()+20L);PRINT_AT.put(p.getUUID(),inside);}
    private static final Map<UUID,BlockPos> PRINT_AT=new HashMap<>();
    @SubscribeEvent public static void prints(ServerTickEvent.Post e){var s=e.getServer();long tick=s.getTickCount();
        for(var it=PRINTS.entrySet().iterator();it.hasNext();){var en=it.next();if(tick<en.getValue())continue;it.remove();var p=s.getPlayerList().getPlayer(en.getKey());var at=PRINT_AT.remove(en.getKey());
            if(p!=null&&at!=null&&p.serverLevel().getBlockState(at).isAir())p.connection.send(new ClientboundBlockUpdatePacket(at,HorrorBlock.of(HorrorBlock.Kind.HANDPRINT,Direction.SOUTH)));}}

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().getUUID();if(e.getEntity() instanceof ServerPlayer p)carry(p,false);CRUSHED.remove(id);SAID.remove(id);CALLED.remove(id);PRINTS.remove(id);PRINT_AT.remove(id);}
    /** Test hook: brings the hall to a stage at once, as a step would, children spared and readers shoved. */
    public static void stage(ServerPlayer p,int st){var b=base(p);if(b==null)return;var t=set(p.server);t.putInt("Stage",st);t.putInt("Next",interval(t,st+1));t.putInt("Wait",0);
        ClosingHallSite.close(p.serverLevel(),b,st,at->childAt(p.serverLevel(),t,at));crush(p.serverLevel(),b,t,group(p.server,b));set(p.server,t);}
    /** Test hook: changes the shared state directly. */
    public static void edit(MinecraftServer s,java.util.function.Consumer<CompoundTag> change){var t=set(s);change.accept(t);set(s,t);}
    public static boolean printPending(ServerPlayer p){return PRINTS.containsKey(p.getUUID());}
    public static void clearAll(){TEST_FAST=false;TEST_HOLD=false;CRUSHED.clear();SAID.clear();CALLED.clear();VOICES.clear();PRINTS.clear();PRINT_AT.clear();}
}
