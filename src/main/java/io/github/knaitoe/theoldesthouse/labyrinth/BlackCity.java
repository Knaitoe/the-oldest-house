package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.CityPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.409, the city on the ice (docs/THE_BLACK_CITY_0_4_409.md): an expedition's camp with nobody in it, a pass between peaks too regular to
 * be peaks, and beyond it a city of dark stone.
 *
 * <p>The wind and the cold are everyone's: gusts push readers back down the upper pass, and out in it the cold builds, never far enough to
 * hurt; three nooks in the rock are out of it. The carvings along the ramp are each reader's own, read in order by standing and looking
 * at each one. At the bottom a tunnel goes on to a warm shore. When a reader who has read all twelve stands there, something pipes over
 * the water, and then what fills the tunnel comes up it, wall to wall, the lamps going out as it passes. It is everyone's, and it never
 * catches a reader who keeps moving; a reader it reaches is taken into the dark and comes to on the ice by the hut, with every carving
 * still read. A reader who reads the carvings, outruns it to the rim and then, on the way back over the pass, looks back at the far peaks
 * long enough to see the mist moving wrongly on them, has the account. After that the camp is at dawn for them.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BlackCity {
    private BlackCity(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BLACK_CITY;
    /** The chase: nothing yet; the piping over the water; what fills the tunnel coming up it; going back down. */
    public static final int IDLE=0,PIPING=1,CHASE=2,RECEDE=3;
    /** Ticks: standing on the shore before the piping, the piping before it comes, a blackout, and the looks at a carving and back. */
    public static final int SHORE_WAIT=20,PIPING_TICKS=50,BLACKOUT=40,READ=60,LOOK=60,RELIGHT=300,EMPTY_RESET=200;
    /** How fast it comes, in blocks a tick (slower than a walk), and how long its body is behind its front. */
    public static final double SPEED=.16;
    public static final int BODY=16;
    /** The dawn a reader has after outrunning it. */
    public static final long DAWN=23300L;

    static final class Site {
        final BlockPos base;int state=IDLE,since,empty,shore,quiet,filled=-1;long tick;double front=-1;boolean fresh=true;
        final Set<UUID> chased=new HashSet<>();
        Site(BlockPos base){this.base=base;}
    }
    private static final Map<Long,Site> SITES=new HashMap<>();
    /** Readers in the dark after it took them, and how long until they come to. */
    private static final Map<UUID,Integer> DARK=new HashMap<>();
    private static final Map<UUID,Long> SENT=new HashMap<>();
    public static void clearAll(){SITES.clear();DARK.clear();SENT.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SENT.remove(e.getEntity().getUUID());DARK.remove(e.getEntity().getUUID());}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Site site(BlockPos b){return SITES.get(b.asLong());}
    public static int state(BlockPos b){var s=site(b);return s==null?-1:s.state;}
    public static double front(BlockPos b){var s=site(b);return s==null?Double.NaN:s.front;}
    /** Moves what fills the tunnel straight to a place along the chase, filling what it passes. */
    public static void advance(ServerLevel l,BlockPos b,double to){var s=site(b);if(s==null||s.state!=CHASE)return;s.front=Math.max(s.front,to);catchUp(l,s);}
    public static boolean dark(UUID id){return DARK.containsKey(id);}
    public static void skipPiping(BlockPos b){var s=site(b);if(s!=null&&s.state==PIPING)s.since=PIPING_TICKS;}

    // ------------------------------------------------------------------------------------------------ helpers
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void hear(ServerPlayer p,Holder<SoundEvent> e,Vec3 at,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(e,SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    static boolean loaded(ServerLevel l,BlockPos b){var at=b.offset(CitySite.HUT_DOOR);return l.isLoaded(at)&&l.isLoaded(b.offset(0,CitySite.FLOOR,CitySite.TUNNEL_N));}

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;SITES.computeIfAbsent(b.asLong(),k->new Site(b));
        for(var key:List.of("MuralTicks","LookTicks","Chased","ChaseCredit","CrestCue","ShoreCue","RampCue","Skipped"))own.remove(key);
        cue(p,own.getBoolean("Survived")?"The camp again, and it is dawn. The wind has dropped a little. The radio is still on in the hut.":"A hut on the ice, a radio hissing on a crate, and outside a wind that blows the snow flat across a camp with nobody in it.");
    }

    // ------------------------------------------------------------------------------------------------ the scene's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        if(!DARK.isEmpty())for(var id:List.copyOf(DARK.keySet())){
            int left=DARK.get(id)-1;var p=server.getPlayerList().getPlayer(id);
            if(p==null){DARK.remove(id);continue;}
            if(left>0){DARK.put(id,left);p.setDeltaMovement(Vec3.ZERO);continue;}
            DARK.remove(id);wake(p);
        }
        if(SITES.isEmpty())return;var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var s:List.copyOf(SITES.values())){
            s.tick++;var here=readers(l,s.base);
            if(here.isEmpty()){if(++s.empty>=EMPTY_RESET&&loaded(l,s.base)){reset(l,s);SITES.remove(s.base.asLong());}continue;}
            s.empty=0;
            if(s.fresh){if(!loaded(l,s.base))continue;s.fresh=false;CitySite.clearAll(l,s.base);CitySite.lamps(l,s.base,true);}
            wind(l,s,here);
            chase(l,s,here);
            s.since++;
        }
    }

    // ------------------------------------------------------------------------------------------------ the wind
    /** In the upper pass the wind comes in gusts, half of every five seconds, and pushes back down it; out in it the cold builds. */
    static void wind(ServerLevel l,Site s,List<ServerPlayer> here){
        boolean gust=s.tick%100<50;
        for(var r:here){
            if(r.isSpectator()||r.getAbilities().flying||DARK.containsKey(r.getUUID()))continue;
            var at=rel(r.position(),s.base);if(!CitySite.exposed(at))continue;
            if(gust){double k=r.isShiftKeyDown()?.45:1;r.push(-.012*k,0,.05*k);r.hurtMarked=true;}
            if(s.tick%100==0)hear(r,CityRegistry.WIND,r.position().add(0,3,-6),.9F,.9F+r.getRandom().nextFloat()*.2F);
            // Vanilla takes two a tick back off; this leaves the cold rising slowly and stopping short of freezing.
            r.setTicksFrozen(Math.min(130,r.getTicksFrozen()+3));
        }
    }

    // ------------------------------------------------------------------------------------------------ the chase
    static void chase(ServerLevel l,Site s,List<ServerPlayer> here){
        switch(s.state){
            case IDLE->{
                boolean waiting=false;
                for(var r:here){if(DARK.containsKey(r.getUUID()))continue;var at=rel(r.position(),s.base);if(!CitySite.onShore(at))continue;
                    if(own(r).getInt("Murals")>=CitySite.MURALS.size())waiting=true;
                    else{var own=own(r);if(!own.getBoolean("ShoreCue")){own.putBoolean("ShoreCue",true);save(r,own);hear(r,CityRegistry.PIPING,world(s.base,new Vec3(0,-28,-188)),.5F,.9F);cue(r,"Far out over the water, something pipes. Nothing comes.");}}}
                s.shore=waiting?s.shore+1:0;
                if(s.shore>=SHORE_WAIT){
                    s.state=PIPING;s.since=0;s.shore=0;
                    l.playSound(null,s.base.offset(0,-28,-188),CityRegistry.PIPING.value(),SoundSource.HOSTILE,3F,.85F);
                    for(var r:here)join(s,r);
                }
            }
            case PIPING->{
                for(var r:here)join(s,r);
                if(s.since>=PIPING_TICKS){
                    s.state=CHASE;s.since=0;s.front=-1;s.filled=-1;
                    for(var r:here)if(s.chased.contains(r.getUUID()))cue(r,"The water heaves. Something is coming up out of it, filling the cavern from wall to wall.");
                }
            }
            case CHASE->{
                s.front+=SPEED;catchUp(l,s);
                if(s.since%40==0){var at=sliceCentre(s.filled);if(at!=null)l.playSound(null,s.base.offset(BlockPos.containing(at)),CityRegistry.MASS_PUSH.value(),SoundSource.HOSTILE,2.5F,.8F);}
                for(var r:here){
                    if(DARK.containsKey(r.getUUID()))continue;
                    var at=rel(r.position(),s.base);
                    if(CitySite.inChase(at))join(s,r);
                    if(!s.chased.contains(r.getUUID()))continue;
                    double c=CitySite.chase(at);
                    if(Double.isNaN(c)){escaped(r,s);continue;}
                    if(c<=s.front){taken(r,s);continue;}
                    // The air it shoves ahead of it, up the tunnel.
                    if(c-s.front<8&&CitySite.underground(at)){r.push(0,0,.02);r.hurtMarked=true;}
                }
                s.chased.removeIf(id->{var p=l.getServer().getPlayerList().getPlayer(id);return p==null||!here.contains(p);});
                if(s.chased.isEmpty()||s.front>CitySite.CHASE.size()+BODY){s.state=RECEDE;s.since=0;s.quiet=0;CitySite.clearAll(l,s.base);s.chased.clear();}
            }
            case RECEDE->{
                boolean anyone=false;for(var r:here)if(CitySite.inChase(rel(r.position(),s.base)))anyone=true;
                s.quiet=anyone?0:s.quiet+1;
                if(s.quiet>=RELIGHT){CitySite.lamps(l,s.base,true);s.state=IDLE;s.since=0;}
            }
            default->{}
        }
    }
    /** A reader in the chase when it begins, or who comes into it while it runs. It counts for them only if they had read every carving. */
    static void join(Site s,ServerPlayer r){
        if(s.chased.contains(r.getUUID())||DARK.containsKey(r.getUUID()))return;
        if(!CitySite.inChase(rel(r.position(),s.base)))return;
        s.chased.add(r.getUUID());var own=own(r);own.putBoolean("Chased",true);own.putBoolean("ChaseCredit",own.getInt("Murals")>=CitySite.MURALS.size());save(r,own);
    }
    /** It comes on through each slice it reaches, its body trailing behind; the lamps it passes go out. */
    static void catchUp(ServerLevel l,Site s){
        int now=(int)Math.floor(s.front);
        while(s.filled<now&&s.filled<CitySite.CHASE.size()+BODY){
            s.filled++;CitySite.fill(l,s.base,s.filled);CitySite.clear(l,s.base,s.filled-BODY);
            for(var lamp:CitySite.LAMPS){double c=CitySite.chase(Vec3.atCenterOf(lamp));if(!Double.isNaN(c)&&(int)Math.floor(c)==s.filled)CitySite.lamp(l,s.base,lamp,false);}
        }
    }
    @Nullable static Vec3 sliceCentre(int slice){if(slice<0||slice>=CitySite.CHASE.size()||CitySite.CHASE.get(slice).isEmpty())return null;var cells=CitySite.CHASE.get(slice);return Vec3.atCenterOf(cells.get(cells.size()/2));}
    /** Out of the pit ahead of it. */
    static void escaped(ServerPlayer r,Site s){
        s.chased.remove(r.getUUID());var own=own(r);own.remove("Chased");
        if(own.getBoolean("ChaseCredit")&&!own.getBoolean("Survived")){own.putBoolean("Survived",true);cue(r,"Up out of the pit. Behind you the lamps are out all the way down, and nothing comes over the rim.");}
        else cue(r,"Up out of the pit. Nothing comes over the rim.");
        own.remove("ChaseCredit");save(r,own);
    }
    /** It reached them: into the dark, and out of it on the ice by the hut, with nothing lost but the way back. */
    static void taken(ServerPlayer r,Site s){
        s.chased.remove(r.getUUID());var own=own(r);own.putInt("Caught",own.getInt("Caught")+1);own.remove("Chased");own.remove("ChaseCredit");save(r,own);
        DARK.put(r.getUUID(),BLACKOUT);
        r.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,BLACKOUT+30,0,false,false,false));
        hear(r,CityRegistry.MASS_PUSH,r.position(),1F,.7F);
        cue(r,"The dark closes over you, wet, and warm, and full of eyes.");
    }
    static void wake(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null||LiteraryVignettes.current(p)!=PLACE)return;
        var at=world(b,CitySite.WAKE);p.teleportTo(p.serverLevel(),at.x,at.y,at.z,180F,0F);p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();
        p.displayClientMessage(Component.literal("You come to on the ice by the hut, with the wind on your face. You still know what the carvings said."),false);
    }

    // ------------------------------------------------------------------------------------------------ each reader
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var own=own(p);var l=p.serverLevel();
        long hour=own.getBoolean("Survived")?DAWN:-1L;var sent=SENT.get(p.getUUID());
        if(sent==null||sent!=hour||l.getGameTime()%100==0){SENT.put(p.getUUID(),hour);HousePackets.send(p,new CityPayload(hour));}
        var s=site(b);if(s==null||DARK.containsKey(p.getUUID())){refresh(held,own);return;}
        var r=rel(p.position(),b);
        long beat=l.getGameTime()/5+p.getId();
        // Wind over the camp and the pass; the aeroplane's fabric snapping in it; wings in the dark under the city.
        if((CitySite.inCamp(r)||CitySite.onPass(r))&&beat%24==0)hear(p,CityRegistry.WIND,p.position().add(2,3,-6),CitySite.onPass(r)?.7F:.45F,.95F);
        if(CitySite.inCamp(r)&&Math.abs(r.x-27)<11&&Math.abs(r.z+26)<11&&beat%12==0)hear(p,CityRegistry.FLAP,world(b,new Vec3(27,1.5,-26)),.6F,1F);
        if(CitySite.underground(r)&&beat%16==0&&p.getRandom().nextInt(3)==0)hear(p,CityRegistry.BIRDS,p.position().add(p.getRandom().nextInt(9)-4,2,p.getRandom().nextInt(9)-4),.6F,.9F+p.getRandom().nextFloat()*.2F);
        murals(p,b,own,r);
        lookBack(p,b,own,r);
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")&&l.getGameTime()%100==0)account(p,b);
        refresh(held,own);
    }
    /** The next carving down, read by standing in front of it and looking at it; a later one looked at first says nothing yet. */
    static void murals(ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r){
        int next=own.getInt("Murals");
        if(CitySite.inPit(r)&&!own.getBoolean("RampCue")){own.putBoolean("RampCue",true);cue(p,"Carvings along the wall of the ramp, panel after panel, going down.");}
        if(next>=CitySite.MURALS.size())return;
        if(looking(p,b,CitySite.MURALS.get(next))){
            int t=own.getInt("MuralTicks")+5;
            if(t>=READ){own.putInt("Murals",next+1);own.remove("MuralTicks");cue(p,CityTexts.PANELS[next]);}
            else own.putInt("MuralTicks",t);
            return;
        }
        own.remove("MuralTicks");
        for(int j=next+1;j<CitySite.MURALS.size();j++)if(looking(p,b,CitySite.MURALS.get(j))){
            int skipped=own.getInt("Skipped");if((skipped&(1<<j))==0){own.putInt("Skipped",skipped|(1<<j));cue(p,"This one goes on from something further up the ramp.");}
            break;
        }
    }
    /** Standing in front of a carving, near it, at about its height, and looking at it. */
    public static boolean looking(ServerPlayer p,BlockPos b,CitySite.Mural m){
        var eye=rel(p.getEyePosition(),b);var to=m.centre().subtract(eye);
        if(to.length()>7||Math.abs(to.y)>3.5)return false;
        var n=new Vec3(m.facing().getStepX(),0,m.facing().getStepZ());if(eye.subtract(m.centre()).dot(n)<.3)return false;
        return p.getLookAngle().dot(to.normalize())>.88;
    }
    /** From the crest, on the way back, the far peaks: looked at long enough, the mist on them moves wrongly, for this reader alone. */
    static void lookBack(ServerPlayer p,BlockPos b,CompoundTag own,Vec3 r){
        if(!own.getBoolean("Survived")||own.getBoolean("Looked")||!CitySite.atLookout(r)){own.remove("LookTicks");return;}
        if(!own.getBoolean("CrestCue")){own.putBoolean("CrestCue",true);cue(p,"The top of the pass. The city is behind you now.");}
        var look=p.getLookAngle();double flat=Math.sqrt(look.x*look.x+look.z*look.z);
        if(flat<.3||-look.z/flat<.85||look.y<-.5||look.y>.7){own.remove("LookTicks");return;}
        int t=own.getInt("LookTicks")+5;own.putInt("LookTicks",t);
        mist(p,b,t);
        if(t>=LOOK){own.remove("LookTicks");own.putBoolean("Looked",true);cue(p,"The mist on the far peaks is moving against the wind.");resolve(p,own);}
    }
    /** The mist on the far peaks, sent to the one reader looking: it goes up and away north, into the wind, in ranks of five. */
    static void mist(ServerPlayer p,BlockPos b,int t){
        var l=p.serverLevel();
        for(int rank=0;rank<5;rank++)for(int i=0;i<4;i++){
            var at=world(b,new Vec3(-40+rank*20+i*3-4.5,30+(t/5%6)*3+i*2,-184));
            l.sendParticles(p,ParticleTypes.CLOUD,true,at.x,at.y,at.z,0,0,.25,-.6,.25);
        }
    }
    static void resolve(ServerPlayer p,CompoundTag own){
        if(own.getBoolean("Ready"))return;
        own.putString("Done","looked");
        LiteraryVignettes.ready(p,PLACE,own,"looked_back_from_the_pass");
        save(p,own);var b=LiteraryVignettes.base(p,PLACE);if(b!=null)account(p,b);
    }
    /** A page written on the message pad by the radio, for this reader alone. */
    static void account(ServerPlayer p,BlockPos b){var at=b.offset(CitySite.PAD);var st=p.serverLevel().getBlockState(at);if(CityBlock.is(st,CityBlock.Kind.PAD))p.connection.send(new ClientboundBlockUpdatePacket(at,st.setValue(CityBlock.STAGE,1)));}

    // ------------------------------------------------------------------------------------------------ touching things
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag held){
        save(p,held);boolean handled=clicked(p,b,rel,own(p));refresh(held,own(p));return handled;
    }
    /** What a hand on cut cloth is told, a tent's or the aeroplane's. */
    static final String FABRIC_CUT="The fabric has been opened in long straight cuts.";
    static boolean clicked(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var st=p.serverLevel().getBlockState(b.offset(rel));
        if(CityMural.is(st)){int i=st.getValue(CityMural.PANEL);cue(p,i<own.getInt("Murals")?CityTexts.PANELS[i]:"A carving in the dark stone, worn and very old. You'd have to stand and look at it properly.");return true;}
        if(st.getBlock() instanceof DoorBlock)return false;
        // The aeroplane: its fabric says what a tent's does; its skis, its struts and its engine say nothing.
        if(CityPlaneBlock.is(st)){if(!CityPlaneBlock.fabric(st))return false;cue(p,FABRIC_CUT);return true;}
        if(!st.is(CityRegistry.FITTING.get()))return false;
        cue(p,switch(st.getValue(CityBlock.KIND)){
            case RADIO->own.getBoolean("Ready")?"The set is on. Nobody is calling on it.":"The set is on. Nothing in it but the wind.";
            case CRATE->"A supply crate: tins, rope, a box of candles.";
            case STOVE->"An oil stove, still faintly warm.";
            case HARNESS->"An empty harness, its buckles still done up.";
            case CORES->"Rock cores in a labelled box. The labels stop halfway down.";
            case TABLE->"The straps on the table have been cut. The ends are clean, and they were cut from underneath.";
            case FABRIC->FABRIC_CUT;
            case PROP->"The propeller is whole. Nothing else is.";
            case LAMP->st.getValue(CityBlock.STAGE)==1?"A hurricane lamp, lit. Someone filled it.":"The lamp is out. The glass is still warm.";
            case PRINTS->"Prints in the grit, three-toed, bigger than a hand. They go both ways.";
            case PAD->"A message pad by the set. The top page is blank.";
            case LOGBOOK->"The radio log.";
        });
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the reset
    /** Nobody on the ice: the tunnel empties, its lamps lit again. */
    static void reset(ServerLevel l,Site s){CitySite.clearAll(l,s.base);CitySite.lamps(l,s.base,true);}
    /** For tests: the city set back as an empty one would be. */
    public static void resetNow(ServerLevel l,BlockPos b){var s=site(b);if(s!=null){reset(l,s);SITES.remove(b.asLong());}}
}
