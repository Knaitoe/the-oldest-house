package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PittPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * 0.4.207, the God of the Pitt (docs/GOD_OF_THE_PITT_0_4_207.md): the owner's third proposal of 31 October, as Ted the Caver's
 * deeper visits. Dealt only to a reader who has already retraced the squeeze; the Ted the Caver Witness story stays the one
 * resolution, and this adds none.
 *
 * <p>One draft cycle for everyone in the passages: fourteen seconds of draft (white dust along the crawls, Brad's lamp flashing
 * twice), three of warning (the dust turning, the barometer's needle shaking, Brad's lamp strobing amber), six of still air. In
 * Floyd's Tomb in the still air, moving, swinging or holding a lit lamp steps the slab over that reader down a stage a tick; at
 * the last stage they are pinned, and hurt, until the draft comes back and the slab lifts. Pinned to their last heart, they black
 * out and wake on the staging ledge. The Seam-Crawler in the fissure beside the Tomb comes along it in the still air and reaches
 * out at movement or light.
 *
 * <p>Four visits: the four seams, cut only in the draft, to the fist-sized hole; the orange line clipped bolt by bolt to the
 * Round Stone Room, and the collapse that follows the reader back along it in the dark; Brad gone, the stone pushed out from
 * inside and the chimney changed, down to the Pre-Echo Ledge; and the ledge, the reader's own footprints two steps ahead over
 * bridges nobody can see, to the rusted ladder and the hatch and the moor, where the field journal waits.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PittGame {
    private PittGame(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PITT;
    /**
     * 0.4.220 (owner: Brad's textures were "not in line with the mod"): Brad is a painted actor on stranger phases forty-four
     * (lamp dark), forty-five (lamp lit white) and forty-six (lamp lit amber), sitting on his kit box at {@link PittCave#BRAD}.
     * He is there exactly while the box is; the lamp's light is a light block at his head.
     */
    public static final int BRAD_PHASE=44;
    public static boolean bradPhase(int phase){return phase>=BRAD_PHASE&&phase<=BRAD_PHASE+2;}
    /** The draft cycle, in ticks: draft, warning, still air. */
    public static final int SAFE=280,WARN=60,CRUSH=120,CYCLE=SAFE+WARN+CRUSH;
    public static final int P_OFF=-1,P_SAFE=0,P_WARN=1,P_CRUSH=2,P_STILL=3,P_COLLAPSE=4;
    /** A slab's last stage, where it pins whoever is under it. */
    public static final int PINNED=6;
    /** Hammer strokes to cut a seam one stage, and the fastest a reader can swing it. */
    public static final int STROKES=6,STROKE_TICKS=10;
    /** The collapse: a pause before it starts, then it comes back along the Tomb one block every so many ticks. */
    public static final int COLLAPSE_DELAY=40,COLLAPSE_STEP=24;
    /** Test hook: hammer strokes are not rate-limited. */
    public static boolean TEST_FAST=false;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final DustParticleOptions CLAY=new DustParticleOptions(new Vector3f(.43F,.18F,.15F),1.2F);
    private static final DustParticleOptions LINE=new DustParticleOptions(new Vector3f(1F,.33F,0F),.7F);
    private static final DustParticleOptions PRINT=new DustParticleOptions(new Vector3f(.86F,.84F,.8F),.6F);
    private static final BlockParticleOption SILT=new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.GRAVEL.defaultBlockState());

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static CompoundTag set(MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),PLACE);}
    static void set(MinecraftServer s,CompoundTag t){LiteraryVignettes.shared(LabyrinthData.get(s),PLACE,t);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void brad(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GOLD),true);}
    static void sound(ServerLevel l,Vec3 at,SoundEvent s,float volume,float pitch){l.playSound(null,at.x,at.y,at.z,s,SoundSource.AMBIENT,volume,pitch);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.Holder.direct(s),SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    static List<ServerPlayer> group(MinecraftServer s,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var q:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(q)&&inside(q)&&b.equals(base(q)))out.add(q);return out;}
    static boolean has(ServerPlayer p,Item item){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item))return true;return false;}
    static boolean lit(ServerPlayer p,CompoundTag t){return phase(t)!=P_COLLAPSE&&(p.getMainHandItem().is(PittRegistry.LAMP.get())||p.getOffhandItem().is(PittRegistry.LAMP.get()));}

    /** Where the draft is: draft, warning, still air; steady on the fourth visit; nothing but the collapse while it lasts. */
    public static int phase(CompoundTag t){
        if(!t.getBoolean("Run"))return P_OFF;if(t.getInt("Collapse")>0)return P_COLLAPSE;if(t.getInt("RunVisit")>=4)return P_STILL;
        int d=Math.floorMod(t.getInt("Draft"),CYCLE);return d<SAFE?P_SAFE:d<SAFE+WARN?P_WARN:P_CRUSH;
    }
    static int left(CompoundTag t){int d=Math.floorMod(t.getInt("Draft"),CYCLE);return d<SAFE?SAFE-d:d<SAFE+WARN?SAFE+WARN-d:CYCLE-d;}
    /** Test hook: puts the draft cycle at the start of a phase. */
    public static void force(MinecraftServer s,int phase){var t=set(s);t.putInt("Draft",phase==P_WARN?SAFE:phase==P_CRUSH?SAFE+WARN:0);set(s,t);}

    // ------------------------------------------------------------------------------------------------ the passages
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var t=set(p.server);var others=group(p.server,b);others.remove(p);
        if(!t.getBoolean("Run")||others.isEmpty()||t.getLong("Base")!=b.asLong())t=start(p.serverLevel(),b,t,own.getInt("Visit"));
        set(p.server,t);send(p,t);
        int visit=own.getInt("Visit");
        if(visit<=1)brad(p,"Brad, at the belay: \"You came back. Gear's in the crate. Four seams in the Tomb, and you only cut while the draft's blowing.\"");
        else if(visit==2)brad(p,"Brad: \"Clip the line in at every bolt. Whatever's past that hole, I want a line to it.\"");
        else if(visit==3)cue(p,"Nobody at the belay. Brad's line hangs off the post, untied. His crate stands open.");
        else cue(p,"Nobody at the belay. The draft is steady tonight, and warm, and it comes from very far down.");
    }
    static CompoundTag start(ServerLevel l,BlockPos b,CompoundTag old,int visit){
        visit=Math.max(1,Math.min(4,visit));PittCave.visit(l,b,visit);
        var t=new CompoundTag();t.putBoolean("Run",true);t.putLong("Base",b.asLong());t.putInt("RunVisit",visit);t.putInt("Draft",0);t.putInt("CrawlerZ",-26);t.putInt("EyeZ",0);
        t.putBoolean("Awake",visit>=2&&visit<=3);return t;
    }
    /** The passages left empty: back to the first visit's shape, Brad at the belay, the lights gone. */
    static void reset(ServerLevel l,BlockPos b){PittCave.visit(l,b,1);set(l.getServer(),new CompoundTag());}
    static void send(ServerPlayer p,CompoundTag t){
        int flags=8|(has(p,PittRegistry.BAROMETER.get())?1:0)|(PINS.containsKey(p.getUUID())?2:0)|(phase(t)==P_COLLAPSE?4:0);
        HousePackets.send(p,new PittPayload(phase(t),left(t),flags));
    }

    // ------------------------------------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var t=set(p.server);int run=t.getInt("RunVisit");
        if(rel.equals(PittCave.HATCH))return hatch(p,b,own,t);
        var s=l.getBlockState(b.offset(rel));var kind=PittBlock.kind(s);if(kind==null)return false;
        switch(kind){
            case CRATE->{
                int given=0;for(var item:List.of(PittRegistry.LAMP.get(),PittRegistry.HAMMER.get(),PittRegistry.BAROMETER.get()))if(!has(p,item)){LiteraryVignettes.give(p,new ItemStack(item));given++;}
                own.putBoolean("GotGear207",true);send(p,t);
                cue(p,given>0?"Brad's crate: a carbide lamp, his masonry hammer, a brass barometer with its needle in the green.":"The crate has carbide canisters and a smell of rope. You have everything that was in it.");return true;}
            case BRAD,BRAD_HEAD->{talk(p);return true;}
            case BELAY->{belay(p,b,own,t);return true;}
            case SEAM->{seam(p,b,rel,own,t,s);return true;}
            case HOLE->{
                if(own.getInt("Visit")==1&&own.getInt("Beat")<1){own.putBoolean("Hole207",true);beat(p,own,1);}
                cue(p,"A hole the size of a fist. Air comes out of it, steady and cold, and it smells of wet copper.");return true;}
            case ANCHOR->{anchor(p,b,rel,own,t);return true;}
            case GROOVE->{groove(p,b,own,t);return true;}
            case WHEEL,WHEEL_LOW->{cue(p,run>=3?"The round stone, pushed out along its grooves. From the other side. Behind where it stood, a hole, and air coming out of it.":"A round stone, taller than you, set in copper grooves against the wall. It is cold. Air moves behind it.");return true;}
            case HELMET->{own.putBoolean("Helmet207",true);cue(p,"Brad's helmet. The shell is cracked right across. The lamp is still on, blinking amber, pointed at the hole behind the stone.");return true;}
            case CUT->{own.putBoolean("Cut207",true);cue(p,"Brad's line. Not cut: pulled apart, the strands frayed out. It goes into a crack two fingers wide and does not come out.");return true;}
            case LINE->{cue(p,"Brad's orange line, from where the stone stood to the hole, and on down.");return true;}
            case RUBBLE->{cue(p,run>=3?"Rubble, packed tight. The chimney you remember is not there.":"Rubble.");return true;}
            case FISSURE->{cue(p,s.getValue(PittBlock.STAGE)>0?"Two green points in the crack. They do not blink.":"A crack two fingers wide. Beyond it, another crawl, unlit, running alongside this one.");return true;}
            case CANISTER->{cue(p,"Carbide canisters, dented, the drip valves crusted white.");return true;}
            case TINS->{cue(p,"Tins of pitons, sorted by length in Brad's hand.");return true;}
            case COIL->{cue(p,"Rope, coiled the way Brad coils it.");return true;}
            case LANTERN->{cue(p,"A carbide lantern, hissing.");return true;}
            case SLAB->{cue(p,"The ceiling is a slab, broken free of the rock above it and resting on nothing you can see.");return true;}
            default->{return false;}
        }
    }
    static void seam(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own,CompoundTag t,net.minecraft.world.level.block.state.BlockState s){
        var l=p.serverLevel();int a=s.getValue(PittBlock.STAGE);
        if(a>=4){cue(p,"Cut through.");return;}
        if(!p.getMainHandItem().is(PittRegistry.HAMMER.get())){cue(p,"A seam of harder stone across the Tomb. Brad's hammer would cut it, a little at a time.");return;}
        int ph=phase(t);
        if(ph==P_WARN){cue(p,"The draft is turning. Leave it.");return;}
        if(ph==P_COLLAPSE)return;
        if(ph==P_CRUSH){
            var over=b.offset(0,1,Mth.floor(rel(p,b).z));lower(l,b,over,3);t.putBoolean("Awake",true);t.putInt("CrawlerZ",Mth.floor(rel(p,b).z));set(p.server,t);
            sound(l,Vec3.atCenterOf(b.offset(rel)),PittRegistry.CHISEL.get(),1F,.7F);cue(p,"The hammer rings in the still air, and the ceiling answers it.");return;}
        long now=l.getGameTime();if(!TEST_FAST&&now-STROKED.getOrDefault(p.getUUID(),0L)<STROKE_TICKS)return;STROKED.put(p.getUUID(),now);
        String key="SeamStrokes207_"+rel.getZ();int strokes=own.getInt(key)+1;own.putInt("Strokes207",own.getInt("Strokes207")+1);
        sound(l,Vec3.atCenterOf(b.offset(rel)),PittRegistry.CHISEL.get(),.9F,.9F+l.getRandom().nextFloat()*.2F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,PittRegistry.material("limestone").defaultBlockState()),b.getX()+rel.getX()+.5,b.getY()+rel.getY()+.5,b.getZ()+rel.getZ()+1,4,.2,.2,.1,0);
        if(strokes>=STROKES){strokes=0;a++;l.setBlock(b.offset(rel),s.setValue(PittBlock.STAGE,a),F);
            if(a>=4){own.putInt("Seams207",own.getInt("Seams207")+1);cue(p,"The seam gives. You can get through.");}else cue(p,"A piece of the seam comes away.");}
        own.putInt(key,strokes);
    }
    static void anchor(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own,CompoundTag t){
        int i=-1;for(int k=0;k<PittCave.ANCHORS.length;k++)if(PittCave.ANCHORS[k].equals(rel))i=k;if(i<0)return;
        if(t.getInt("RunVisit")<2){cue(p,"A bolt, old and rusted. Nothing on it.");return;}
        int mask=own.getInt("Anchors207");
        if((mask&1<<i)!=0){cue(p,"Clipped.");return;}
        if(i>0&&(mask&1<<(i-1))==0){cue(p,"The line won't reach that bolt yet. Clip the one before it.");return;}
        mask|=1<<i;own.putInt("Anchors207",mask);var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));l.setBlock(b.offset(rel),st.setValue(PittBlock.STAGE,1),F);
        cue(p,mask==(1<<PittCave.ANCHORS.length)-1?"The orange line runs all the way back to the belay.":"You clip the orange line to the bolt. "+Integer.bitCount(mask)+" of "+PittCave.ANCHORS.length+".");
    }
    static boolean lined(CompoundTag own){return own.getInt("Anchors207")==(1<<PittCave.ANCHORS.length)-1;}
    static void groove(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag t){
        if(t.getInt("RunVisit")!=2||own.getInt("Visit")!=2){cue(p,"Copper, sheathing a groove in the floor, worn bright in the middle as if something heavy had rolled along it many times.");return;}
        if(!lined(own)){cue(p,"Clip the line in all the way before you go poking at anything.");return;}
        if(t.getInt("Collapse")>0)return;
        own.putBoolean("Grooves207",true);t.putInt("Collapse",1);t.putInt("Front",-41);set(p.server,t);
        var l=p.serverLevel();sound(l,PittCave.at(b,0,ROOM_Y()+1,-45),PittRegistry.RUMBLE.get(),1.5F,.8F);
        for(var q:group(p.server,b)){cue(q,q==p?"You kneel to look at the track. The copper is worn bright. Under the floor, something heavy shifts, and the room starts coming down.":"Somewhere ahead, the rock lets go. The lamps gutter out.");send(q,t);}
    }
    static int ROOM_Y(){return PittCave.ROOM_Y;}
    static void belay(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag t){
        if(t.getInt("RunVisit")>=3){cue(p,"The belay line hangs off the post, untied. Brad's knot is still in the end of it.");return;}
        var belayer=t.hasUUID("Belayer")?t.getUUID("Belayer"):null;var group=group(p.server,b);
        if(p.getUUID().equals(belayer)){
            for(var q:group){var pin=PINS.get(q.getUUID());if(pin==null)continue;
                haul(q,b,p);own.putString("Hauled207",(own.getString("Hauled207").isEmpty()?"":own.getString("Hauled207")+", ")+q.getGameProfile().getName());
                cue(p,"You take in the line hand over hand, and the weight on the end of it comes free.");return;}
            cue(p,"You keep the line snug. Nobody on the other end is stuck.");return;}
        if(belayer!=null){cue(p,"Someone else has the belay.");return;}
        if(group.size()<2){brad(p,"Brad: \"I've got the belay. You've got the Tomb.\"");return;}
        t.putUUID("Belayer",p.getUUID());set(p.server,t);own.putBoolean("HeldBelay207",true);PittCave.brad(p.serverLevel(),b,false);
        brad(p,"Brad hands you the line: \"Two flashes good, amber bad. Haul if they get stuck. I'm going up for carbide.\" He goes up the rope.");
    }
    /** Pulled back by the line: the slab lifts off them and they come two blocks nearer the ledge. */
    static void haul(ServerPlayer q,BlockPos b,ServerPlayer by){
        var pin=PINS.remove(q.getUUID());if(pin==null)return;var l=q.serverLevel();var r=pin.subtract(b.getX(),b.getY(),b.getZ());
        var over=b.offset(0,1,Mth.floor(r.z));var s=l.getBlockState(over);if(PittBlock.is(s,PittBlock.Kind.SLAB))l.setBlock(over,s.setValue(PittBlock.STAGE,0),F);
        double z=Math.min(PittCave.TOMB_Z0+.5,r.z+2);q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+z,q.getYRot(),q.getXRot());q.setDeltaMovement(Vec3.ZERO);
        var o=own(q);o.putString("HauledBy207",by.getGameProfile().getName());save(q,o);cue(q,"The line at your harness goes tight, and you are dragged out from under it.");
    }
    static boolean hatch(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag t){
        var l=p.serverLevel();var at=b.offset(PittCave.HATCH);var s=l.getBlockState(at);if(!s.is(Blocks.IRON_TRAPDOOR))return false;
        if(rel(p,b).y>=PittCave.MOOR_Y){cue(p,"Rusted shut from this side. There is a ring for a hand on the underside.");return true;}
        if(own.getInt("Visit")<4||t.getInt("RunVisit")<4){cue(p,"Rusted shut.");return true;}
        own.putBoolean("Hatch207",true);if(!s.getValue(TrapDoorBlock.OPEN))l.setBlock(at,s.setValue(TrapDoorBlock.OPEN,true),Block.UPDATE_ALL);
        t.putInt("Hatch",200);set(p.server,t);l.playSound(null,at,net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,1F,.7F);
        cue(p,"The hatch gives, all at once, and there is air on your face that has never been underground.");return true;
    }
    /** A beat for the reader who earned it, and for whoever held the belay for them on the same visit. */
    static void beat(ServerPlayer p,CompoundTag own,int n){
        if(own.getInt("Visit")==n&&own.getInt("Beat")<n)own.putInt("Beat",n);
        var t=set(p.server);if(!t.hasUUID("Belayer"))return;var q=p.server.getPlayerList().getPlayer(t.getUUID("Belayer"));if(q==null||q==p)return;
        var o=own(q);if(o.getInt("Visit")==n&&o.getInt("Beat")<n){o.putInt("Beat",n);o.putString("BelayFor207",p.getGameProfile().getName());save(q,o);cue(q,"The line goes slack, then three tugs: they made it.");}
    }

    // ------------------------------------------------------------------------------------------------ the rock
    static void lower(ServerLevel l,BlockPos b,BlockPos over,int by){
        var s=l.getBlockState(over);if(!PittBlock.is(s,PittBlock.Kind.SLAB))return;int st=Math.min(PINNED,s.getValue(PittBlock.STAGE)+by);
        if(st!=s.getValue(PittBlock.STAGE)){l.setBlock(over,s.setValue(PittBlock.STAGE,st),F);sound(l,Vec3.atCenterOf(over),PittRegistry.GRIND.get(),.8F,.6F+st*.05F);
            l.sendParticles(SILT,over.getX()+.5,over.getY()-.1,over.getZ()+.5,3,.3,0,.3,0);}
    }
    static int stage(ServerLevel l,BlockPos over){var s=l.getBlockState(over);return PittBlock.is(s,PittBlock.Kind.SLAB)?s.getValue(PittBlock.STAGE):0;}
    /** Lights out, and the next thing is the staging ledge under their back. */
    static void blackout(ServerPlayer p,BlockPos b,String why){
        var pin=PINS.remove(p.getUUID());var l=p.serverLevel();
        if(pin!=null){var over=b.offset(0,1,Mth.floor(pin.z-b.getZ()));var s=l.getBlockState(over);if(PittBlock.is(s,PittBlock.Kind.SLAB))l.setBlock(over,s.setValue(PittBlock.STAGE,0),F);}
        var o=own(p);o.putInt("Blackouts207",o.getInt("Blackouts207")+1);save(p,o);
        HousePackets.send(p,new HouseFadePayload(2,30,30));p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-6.5,180,0);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
        cue(p,why);
    }

    // ------------------------------------------------------------------------------------------------ every tick
    private static final Map<UUID,Vec3> PINS=new HashMap<>();
    private static final Map<UUID,Vec3> LAST=new HashMap<>();
    private static final Map<UUID,BlockPos> LIGHTS=new HashMap<>();
    private static final Map<UUID,Long> STROKED=new HashMap<>();
    private static final Map<UUID,Integer> PRINTS=new HashMap<>();
    private static final Set<UUID> SENT=new HashSet<>();
    /** Readers who were out past the stoopway while the collapse came, and readers it caught: only the first, uncaught, get out ahead of it. */
    private static final Set<UUID> DEEP=new HashSet<>(),CAUGHT=new HashSet<>();
    public static boolean pinned(ServerPlayer p){return PINS.containsKey(p.getUUID());}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();
        if(tick%20==0)tools(s);
        BlockPos b=null;var group=new ArrayList<ServerPlayer>();
        for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(p)&&inside(p)){var pb=base(p);if(pb==null)continue;if(b==null)b=pb;if(pb.equals(b))group.add(p);}
        for(var it=SENT.iterator();it.hasNext();){var id=it.next();var p=s.getPlayerList().getPlayer(id);if(p!=null&&group.contains(p))continue;if(p!=null)HousePackets.send(p,new PittPayload(P_OFF,0,0));unlight(s,id);PINS.remove(id);LAST.remove(id);it.remove();}
        var t=set(s);
        if(b==null){if(!t.getBoolean("Run"))return;int empty=t.getInt("Empty")+1;t.putInt("Empty",empty);
            if(empty<100){set(s,t);return;}var l=s.getLevel(NovelRooms.dimension(PLACE));var rb=BlockPos.of(t.getLong("Base"));if(l==null||!l.isLoaded(rb)){set(s,new CompoundTag());return;}reset(l,rb);return;}
        var l=group.get(0).serverLevel();
        if(!t.getBoolean("Run")||t.getLong("Base")!=b.asLong())t=start(l,b,t,own(group.get(0)).getInt("Visit"));
        t.putInt("Empty",0);int before=phase(t);
        if(before==P_SAFE||before==P_WARN||before==P_CRUSH)t.putInt("Draft",t.getInt("Draft")+1);
        int ph=phase(t);
        if(ph!=before)changed(l,b,t,group,ph);
        for(var p:group){if(SENT.add(p.getUUID())||tick%10==0||ph!=before)send(p,t);}
        // The belay: let go when its holder walks off.
        if(t.hasUUID("Belayer")){var q=s.getPlayerList().getPlayer(t.getUUID("Belayer"));if(q==null||!group.contains(q)||q.position().distanceTo(Vec3.atCenterOf(b.offset(PittCave.BELAY)))>4.5){if(q!=null)cue(q,"You let the line go.");t.remove("Belayer");}}
        for(var p:group)reader(l,b,t,p,ph,tick);
        if(tick%10==0&&(ph==P_SAFE||ph==P_STILL))lift(l,b);
        if(t.getInt("RunVisit")<=2&&!t.hasUUID("Belayer"))bradLamp(l,b,ph,tick);else if(tick%10==0)bradActor(l,b);
        if(t.getInt("RunVisit")>=3&&tick%6==0){var h=b.offset(PittCave.HELMET);var hs=l.getBlockState(h);if(PittBlock.is(hs,PittBlock.Kind.HELMET))l.setBlock(h,hs.setValue(PittBlock.STAGE,(tick/6)%2==0?1:0),F);}
        if(tick%8==0)crawler(l,b,t,group,ph);
        if(ph==P_COLLAPSE)collapse(l,b,t,group);
        if(t.getInt("Hatch")>0){int h=t.getInt("Hatch")-1;t.putInt("Hatch",h);if(h==0){var at=b.offset(PittCave.HATCH);var hs=l.getBlockState(at);if(hs.is(Blocks.IRON_TRAPDOOR)&&hs.getValue(TrapDoorBlock.OPEN))l.setBlock(at,hs.setValue(TrapDoorBlock.OPEN,false),Block.UPDATE_ALL);}}
        set(s,t);
    }
    static void changed(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,int ph){
        for(var p:group){var r=rel(p,b);boolean under=!PittCave.onMoor(r)&&!PittCave.onStaging(r);
            if(ph==P_WARN){if(under)cue(p,"The draft is dying. The needle jumps.");heard(p,p.getEyePosition(),PittRegistry.TICK.get(),.7F,1F);}
            else if(ph==P_CRUSH){if(under)cue(p,"Still air. Don't move. Lamp away.");heard(p,p.getEyePosition().add(0,1,0),PittRegistry.GROAN.get(),1F,.8F);}
            else if(ph==P_SAFE&&under)cue(p,"The draft comes back, cold against your face.");}
    }
    /** Each reader's tick: their lamp's light, the draft on them, the still air's rule, the pin, the ledge and the moor. */
    static void reader(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer p,int ph,long tick){
        var r=rel(p,b);var last=LAST.put(p.getUUID(),p.position());double speed=last==null?0:Math.hypot(p.getX()-last.x,p.getZ()-last.z);
        boolean lamp=lit(p,t);light(l,p,lamp);
        var own=own(p);boolean dirty=false;int run=t.getInt("RunVisit");
        // The still air's rule, in Floyd's Tomb.
        if(ph==P_CRUSH&&PittCave.inTomb(r)){
            var over=b.offset(0,1,Mth.floor(r.z));
            if(speed>.02||p.swinging||lamp)lower(l,b,over,1);
            if(stage(l,over)>=PINNED&&!PINS.containsKey(p.getUUID())){PINS.put(p.getUUID(),p.position());own.putInt("Pinned207",own.getInt("Pinned207")+1);dirty=true;
                cue(p,"The slab comes down onto your back and stays there. You cannot move.");}
        }
        var pin=PINS.get(p.getUUID());
        if(pin!=null){var over=b.offset(0,1,Mth.floor(pin.z-b.getZ()));
            if(stage(l,over)<PINNED){PINS.remove(p.getUUID());cue(p,"The weight lifts off you a hair at a time.");}
            else{if(p.position().distanceToSqr(pin)>.0025)p.teleportTo(l,pin.x,pin.y,pin.z,p.getYRot(),p.getXRot());p.setDeltaMovement(Vec3.ZERO);
                if(tick%10==0){if(p.getHealth()-1F<2F){if(dirty)save(p,own);blackout(p,b,run>=3?"The dark closes over you, and the next thing is the staging ledge under your back. Nobody is talking.":"The dark closes over you, and the next thing is Brad's voice, and the staging ledge under your back.");return;}
                    p.hurt(p.damageSources().inWall(),1F);}}}
        // The draft, along the crawls: white dust going out toward the ledge, slowing and turning, then silt falling in the still air.
        if(tick%4==0&&!PittCave.onMoor(r)){
            if(ph==P_SAFE||ph==P_STILL)l.sendParticles(p,ParticleTypes.WHITE_ASH,true,p.getX(),p.getY()+.4,p.getZ(),4,1.2,.3,1.2,.02);
            else if(ph==P_WARN)l.sendParticles(p,ParticleTypes.WHITE_ASH,true,p.getX(),p.getY()+.4,p.getZ(),1,.8,.2,.8,.002);
            else l.sendParticles(p,SILT,true,p.getX(),p.getY()+1.2,p.getZ(),3,1.2,.2,1.2,0);
        }
        if(ph==P_SAFE&&tick%80==0&&!PittCave.onMoor(r))heard(p,p.getEyePosition(),PittRegistry.DRAFT.get(),.35F,1F);
        // The orange line, for its owner: from the belay through every bolt they have clipped.
        if(tick%10==0&&own.getInt("Anchors207")!=0)line(l,b,p,own);
        // The ledge: on the fourth visit the reader's own footprints two steps ahead and their steps heard early; falls caught.
        if(PittCave.onLedge(r)&&run>=4&&tick%5==0)echo(l,b,p,r,speed);
        if(PittCave.onLedge(r)&&run==3&&tick%60==0&&l.getRandom().nextInt(2)==0){var c=PittCave.PATH.get(1+l.getRandom().nextInt(PittCave.PATH.size()-2));heard(p,PittCave.at(b,c.getX()+.5,c.getY()+1,c.getZ()+.5),PittRegistry.STEP.get(),.6F,.9F);}
        if(r.y<PittCave.LEDGE_Y-6){own.putInt("Falls207",own.getInt("Falls207")+1);dirty=true;HousePackets.send(p,new HouseFadePayload(2,20,30));
            p.teleportTo(l,b.getX()+PittCave.LANDING.getX()+.5,b.getY()+PittCave.LANDING.getY(),b.getZ()+PittCave.LANDING.getZ()+.5,0,0);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
            cue(p,"The dark takes your feet out from under you, and then you are on the ledge again, on your knees, and you never hit anything.");}
        if(run==3&&own.getInt("Visit")==3&&own.getInt("Beat")<3&&PittCave.onLanding(r)){own.putBoolean("Landing207",true);beat(p,own,3);dirty=true;
            cue(p,"The chimney lets you out onto a ledge. Past its edge there is nothing at all. Out over the nothing, a footstep. Then another.");}
        if(r.y>0&&Math.abs(r.x-(PittCave.LADDER.getX()+.5))<1&&Math.abs(r.z-(PittCave.LADDER.getZ()+.5))<1&&run>=4&&!own.getBoolean("Hatch207")){own.putBoolean("Hatch207",true);dirty=true;}
        if(run>=4&&own.getInt("Visit")>=4&&PittCave.onMoor(r)&&own.getBoolean("Hatch207")&&!own.getBoolean("Ready")){own.putBoolean("Surface207",true);beat(p,own,4);
            LiteraryVignettes.ready(p,PLACE,own,"climbed_out");dirty=true;
            cue(p,"Grass. Wind. The sky going grey in the east. Somebody has left your field journal open on a flat stone.");}
        if(dirty)save(p,own);
    }
    /** Slabs lift a stage at a time while the draft blows. */
    static void lift(ServerLevel l,BlockPos b){for(int z=PittCave.TOMB_Z0;z>=PittCave.TOMB_Z1;z--){var at=b.offset(0,1,z);var s=l.getBlockState(at);if(PittBlock.is(s,PittBlock.Kind.SLAB)&&s.getValue(PittBlock.STAGE)>0)l.setBlock(at,s.setValue(PittBlock.STAGE,s.getValue(PittBlock.STAGE)-1),F);}}
    /** What Brad says when a reader speaks to him (or to the box he sits on), by where the draft is. */
    public static void talk(ServerPlayer p){
        var t=set(p.server);int run=t.getInt("RunVisit");int ph=phase(t);
        brad(p,ph==P_CRUSH?"Brad, very low: \"Don't move. Lamp away. It comes back.\"":ph==P_WARN?"Brad: \"Needle's going. Get flat.\"":run<=1?"Brad: \"Two flashes, you're good. Amber, you lie still. Four seams, then the hole.\"":"Brad: \"Clip in at every bolt. I've got you.\"");
    }
    /**
     * Brad himself: on his box while it stands at {@link PittCave#BRAD}, gone the moment it is gone (a reader holding the belay
     * in his place, the third visit on). Found by looking on the box, so a reset or a reload never leaves two of him; made only
     * where the cave is ticking.
     */
    @Nullable static LiteraryActor bradActor(ServerLevel l,BlockPos b){
        var seat=b.offset(PittCave.BRAD);boolean there=PittBlock.is(l.getBlockState(seat),PittBlock.Kind.BRAD);
        var found=l.getEntitiesOfClass(LiteraryActor.class,new net.minecraft.world.phys.AABB(seat).inflate(2),a->a.scene().equals(PLACE.id())&&a.role()==LiteraryActor.STRANGER&&bradPhase(a.phase()));
        var lamp=seat.above();
        if(!there){found.forEach(net.minecraft.world.entity.Entity::discard);if(l.getBlockState(lamp).is(Blocks.LIGHT))l.setBlock(lamp,Blocks.AIR.defaultBlockState(),F);return null;}
        for(int i=1;i<found.size();i++)found.get(i).discard();
        if(!found.isEmpty())return found.get(0);
        if(!l.isPositionEntityTicking(seat))return null;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,BRAD_PHASE);
        float yaw=l.getBlockState(seat).getValue(PittBlock.FACING).toYRot();
        a.moveTo(seat.getX()+.5,seat.getY(),seat.getZ()+.5,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
        l.addFreshEntity(a);return a;
    }
    /** Brad's lamp, on his helmet at the belay: two white flashes in the draft, an amber strobe when it dies. */
    static void bradLamp(ServerLevel l,BlockPos b,int ph,long tick){
        var a=bradActor(l,b);if(a==null)return;
        int k=(int)(tick%40);int want=ph==P_SAFE?(k<4||k>=8&&k<12?1:0):ph==P_WARN||ph==P_CRUSH||ph==P_COLLAPSE?((tick/3)%2==0?2:0):1;
        if(a.phase()!=BRAD_PHASE+want)a.appearance(LiteraryActor.STRANGER,BRAD_PHASE+want);
        var lamp=b.offset(PittCave.BRAD.above());var now=l.getBlockState(lamp);
        if(want==0){if(now.is(Blocks.LIGHT))l.setBlock(lamp,Blocks.AIR.defaultBlockState(),F);}
        else if(now.isAir()||now.is(Blocks.LIGHT)){int level=want==1?12:9;if(!now.is(Blocks.LIGHT)||now.getValue(LightBlock.LEVEL)!=level)l.setBlock(lamp,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,level),F);}
    }
    /** The Seam-Crawler: its eyes in the fissure, keeping pace; in the still air it comes along it, and reaches. */
    static void crawler(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,int ph){
        int run=t.getInt("RunVisit");boolean active=t.getBoolean("Awake")&&run<=3;
        int eye=t.getInt("EyeZ");if(eye!=0&&!active){eyes(l,b,eye,0);t.putInt("EyeZ",0);}
        if(!active)return;
        if(t.getInt("HandUntil")>0){t.putInt("HandUntil",t.getInt("HandUntil")-8);if(t.getInt("HandUntil")<=0&&eye!=0)eyes(l,b,eye,1);return;}
        if(run==3)chimneyEyes(l,b,t,group);
        ServerPlayer near=null;int nz=0;double best=1e9;
        for(var p:group){var r=rel(p,b);if(!PittCave.inTomb(r))continue;int z=Mth.floor(r.z);double d=Math.abs(z-t.getInt("CrawlerZ"));if(d<best){best=d;near=p;nz=z;}}
        boolean show=near!=null&&(ph==P_WARN||ph==P_CRUSH||run==3);
        if(!show){if(eye!=0){eyes(l,b,eye,0);t.putInt("EyeZ",0);}return;}
        int cz=t.getInt("CrawlerZ");if(cz!=nz)cz+=Integer.signum(nz-cz);cz=Mth.clamp(cz,PittCave.TOMB_Z1,PittCave.TOMB_Z0);t.putInt("CrawlerZ",cz);
        if(eye!=cz){if(eye!=0)eyes(l,b,eye,0);eyes(l,b,cz,1);t.putInt("EyeZ",cz);
            l.sendParticles(CLAY,b.getX()+1,b.getY()+.5,b.getZ()+cz+.5,4,.05,.3,.3,0);if(l.getRandom().nextInt(3)==0)sound(l,PittCave.at(b,1.5,.5,cz+.5),PittRegistry.SCRAPE.get(),.7F,.8F+l.getRandom().nextFloat()*.3F);}
        var o=own(near);if(!o.getBoolean("Eyes207")){o.putBoolean("Eyes207",true);save(near,o);cue(near,"Two green points in the crack beside you. They do not blink.");}
        if(ph==P_CRUSH&&Math.abs(cz-nz)<=1){var last=LAST.get(near.getUUID());
            boolean moving=near.swinging||lit(near,t);if(moving||last!=null&&near.position().distanceTo(last)>.02){
                eyes(l,b,cz,2);t.putInt("HandUntil",16);lower(l,b,b.offset(0,1,nz),2);var oo=own(near);oo.putInt("Reached207",oo.getInt("Reached207")+1);save(near,oo);
                sound(l,PittCave.at(b,1,.5,cz+.5),PittRegistry.SCRAPE.get(),1F,.6F);cue(near,"Something pale comes out of the crack beside you, and presses down.");}}
    }
    /** On the third visit the eyes are in the second chimney's cracks too, beside whoever is in its squeezes. */
    static void chimneyEyes(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        BlockPos want=null;double best=6.25;
        for(var p:group){var r=rel(p,b);if(PittCave.inTomb(r)||!PittCave.CRAWL.contains(PittCave.cell(r)))continue;
            for(var f:PittCave.CHIMNEY_FISSURES){double d=Vec3.atCenterOf(f).distanceToSqr(r);if(d<best){best=d;want=f;}}}
        var had=t.contains("ChimEye")?BlockPos.of(t.getLong("ChimEye")):null;if(Objects.equals(had,want))return;
        if(had!=null){var s=l.getBlockState(b.offset(had));if(PittBlock.is(s,PittBlock.Kind.FISSURE))l.setBlock(b.offset(had),s.setValue(PittBlock.STAGE,0),F);t.remove("ChimEye");}
        if(want!=null){var s=l.getBlockState(b.offset(want));if(PittBlock.is(s,PittBlock.Kind.FISSURE)){l.setBlock(b.offset(want),s.setValue(PittBlock.STAGE,1),F);t.putLong("ChimEye",want.asLong());
            l.sendParticles(CLAY,b.getX()+want.getX()+.5,b.getY()+want.getY()+.5,b.getZ()+want.getZ()+.5,3,.2,.2,.2,0);}}
    }
    static void eyes(ServerLevel l,BlockPos b,int z,int stage){var at=b.offset(1,0,z);var s=l.getBlockState(at);if(PittBlock.is(s,PittBlock.Kind.FISSURE)&&s.getValue(PittBlock.STAGE)!=stage)l.setBlock(at,s.setValue(PittBlock.STAGE,stage),F);}
    /** The collapse, following the reader back along the Tomb toward the ledge; rubble down the round room's sides; lamps out. */
    static void collapse(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        int c=t.getInt("Collapse")+1;t.putInt("Collapse",c);int front=t.getInt("Front");
        if(c>COLLAPSE_DELAY&&(c-COLLAPSE_DELAY)%COLLAPSE_STEP==0&&front<PittCave.TOMB_Z0){front++;t.putInt("Front",front);
            var over=b.offset(0,1,front);var s=l.getBlockState(over);if(PittBlock.is(s,PittBlock.Kind.SLAB))l.setBlock(over,s.setValue(PittBlock.STAGE,PINNED),F);
            sound(l,PittCave.at(b,.5,.5,front+.5),PittRegistry.GRIND.get(),1F,.5F);}
        if(c%15==0){for(var cell:PittCave.RUBBLE_CELLS){var at=b.offset(cell);if(!l.getBlockState(at).isAir())continue;boolean taken=false;for(var p:group)if(p.getBoundingBox().intersects(new net.minecraft.world.phys.AABB(at)))taken=true;
            if(!taken){l.setBlock(at,PittCave.fit(PittBlock.Kind.RUBBLE,Direction.NORTH),F);break;}}}
        if(c%40==0)sound(l,PittCave.at(b,.5,0,front+.5),PittRegistry.RUMBLE.get(),1.2F,.7F+l.getRandom().nextFloat()*.2F);
        boolean anyoneDeep=false;
        for(var p:group){var r=rel(p,b);
            if(!PittCave.onStaging(r)){anyoneDeep=true;DEEP.add(p.getUUID());p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,40,0,false,false));if(c%4==0)l.sendParticles(p,SILT,true,p.getX(),p.getY()+1.5,p.getZ(),6,1.5,.3,1.5,0);}
            // Behind the front once it is moving: in the Tomb, in the Wind Tunnel, or still in the round room, cut off.
            if((PittCave.inTomb(r)||PittCave.inWindTunnel(r)||PittCave.inRoom(r))&&Mth.floor(r.z)<=front&&front>-41){CAUGHT.add(p.getUUID());blackout(p,b,"The ceiling comes down behind you, then on you. The next thing is Brad's voice, and the ledge, and the line still tied to your harness.");continue;}
            var o=own(p);if(DEEP.contains(p.getUUID())&&!CAUGHT.contains(p.getUUID())&&o.getInt("Visit")==2&&o.getInt("Beat")<2&&lined(o)&&PittCave.onStaging(r)&&r.z>-15&&c>COLLAPSE_DELAY){o.putBoolean("Escaped207",true);beat(p,o,2);save(p,o);
                brad(p,"Brad hauls the last of the line in, hand over hand: \"Breathe. You're out. Breathe.\"");}
        }
        if(c>COLLAPSE_DELAY+COLLAPSE_STEP&&(!anyoneDeep||front>=PittCave.TOMB_Z0)){t.putInt("Collapse",0);t.remove("Front");t.putInt("Draft",0);
            for(int z=PittCave.TOMB_Z0;z>=PittCave.TOMB_Z1;z--){var over=b.offset(0,1,z);var s=l.getBlockState(over);if(PittBlock.is(s,PittBlock.Kind.SLAB))l.setBlock(over,s.setValue(PittBlock.STAGE,0),F);}
            PittCave.rubble(l,b,false);DEEP.clear();CAUGHT.clear();for(var p:group)send(p,t);}
    }
    /** The orange line, drawn for its owner alone, from the belay post through each bolt they clipped. */
    static void line(ServerLevel l,BlockPos b,ServerPlayer p,CompoundTag own){
        int mask=own.getInt("Anchors207");var from=PittCave.at(b,PittCave.BELAY.getX()+.5,.6,PittCave.BELAY.getZ()+.5);
        for(int i=0;i<PittCave.ANCHORS.length;i++){if((mask&1<<i)==0)break;var a=PittCave.ANCHORS[i];var to=PittCave.at(b,a.getX()+1.1,a.getY()+.5,a.getZ()+.5);
            int n=(int)Math.max(2,from.distanceTo(to)*1.5);for(int k=0;k<=n;k++){var at=from.lerp(to,k/(double)n);if(at.distanceToSqr(p.position())<900)l.sendParticles(p,LINE,true,at.x,at.y,at.z,1,0,0,0,0);}from=to;}
    }
    /** The pre-echo: the reader's own footprints two steps ahead along the bridges, and their next steps heard before they take them. */
    static void echo(ServerLevel l,BlockPos b,ServerPlayer p,Vec3 r,double speed){
        var path=PittCave.PATH;int idx=-2;
        for(int i=0;i<path.size();i++){var c=path.get(i);if(Math.abs(r.x-(c.getX()+.5))<.8&&Math.abs(r.z-(c.getZ()+.5))<.8&&Math.abs(r.y-(c.getY()+1))<1.2)idx=i;}
        // On the landing or the shelf, before the first bridge: the prints start at the shelf's lip.
        if(idx==-2){if(PittCave.onLanding(r)||r.z>-55.2&&r.x>-.5&&r.x<10.5)idx=-1;else return;}
        for(int k=1;k<=2;k++){int j=idx+k;if(j<0||j>=path.size())continue;var c=path.get(j);
            l.sendParticles(p,PRINT,true,b.getX()+c.getX()+.35,b.getY()+c.getY()+1.02,b.getZ()+c.getZ()+.5,2,.04,0,.08,0);
            l.sendParticles(p,PRINT,true,b.getX()+c.getX()+.65,b.getY()+c.getY()+1.02,b.getZ()+c.getZ()+.5,2,.04,0,.08,0);}
        int prev=PRINTS.getOrDefault(p.getUUID(),-9);
        if(idx!=prev||speed>.05&&l.getRandom().nextInt(3)==0){PRINTS.put(p.getUUID(),idx);int j=Math.min(path.size()-1,idx+2);var c=path.get(j);heard(p,PittCave.at(b,c.getX()+.5,c.getY()+1,c.getZ()+.5),PittRegistry.STEP.get(),.8F,1F);
            if(idx>=0){var o=own(p);int far=Math.max(o.getInt("Prints207"),idx+1);if(far!=o.getInt("Prints207")){o.putInt("Prints207",far);save(p,o);}}}
        // A faint arc where the prints turn.
        int j=idx+2;if(j>=0&&j+1<path.size()){var c=path.get(j);var n=path.get(j+1);var d=new Vec3(n.getX()-c.getX(),0,n.getZ()-c.getZ());
            for(int k=0;k<4;k++){var at=PittCave.at(b,c.getX()+.5,c.getY()+1.05,c.getZ()+.5).add(d.scale(.2+k*.2));l.sendParticles(p,PRINT,true,at.x,at.y,at.z,1,0,0,0,0);}}
    }
    /** The lamp's light: a light block at the reader's eyes while the lamp is in their hand, gone when it is not. */
    static void light(ServerLevel l,ServerPlayer p,boolean lit){
        var old=LIGHTS.get(p.getUUID());var want=lit?BlockPos.containing(p.getEyePosition()):null;
        if(Objects.equals(old,want))return;
        if(old!=null&&l.getBlockState(old).is(Blocks.LIGHT))l.setBlock(old,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        if(want!=null&&l.getBlockState(want).isAir()){l.setBlock(want,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12),Block.UPDATE_CLIENTS);LIGHTS.put(p.getUUID(),want);}else LIGHTS.remove(p.getUUID());
    }
    static void unlight(MinecraftServer s,UUID id){var old=LIGHTS.remove(id);if(old==null)return;var l=s.getLevel(NovelRooms.dimension(PLACE));if(l!=null&&l.isLoaded(old)&&l.getBlockState(old).is(Blocks.LIGHT))l.setBlock(old,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
    /** Brad's tools stay in the passages: a reader who leaves without them in hand has left them in the crate. */
    static void tools(MinecraftServer s){
        for(var p:s.getPlayerList().getPlayers()){if(inside(p))continue;var inv=p.getInventory();
            for(int i=0;i<inv.getContainerSize();i++){var st=inv.getItem(i);if(st.is(PittRegistry.HAMMER.get())||st.is(PittRegistry.LAMP.get())||st.is(PittRegistry.BAROMETER.get()))inv.setItem(i,ItemStack.EMPTY);}}
    }
    /** On hands and knees in the Tomb and the squeezes, and in front of each, facing in. */
    public static boolean crawling(ServerPlayer p,BlockPos b){
        var r=rel(p,b);var cell=PittCave.cell(r);if(PittCave.CRAWL.contains(cell))return true;
        var into=PittCave.APPROACH.get(cell);if(into==null)return false;
        float yaw=Mth.wrapDegrees(p.getYRot()-into.toYRot());return Math.abs(yaw)<70&&p.onGround();
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().getUUID();unlight(e.getEntity().getServer(),id);SENT.remove(id);PINS.remove(id);LAST.remove(id);STROKED.remove(id);PRINTS.remove(id);}
    public static void clearAll(){DEEP.clear();CAUGHT.clear();SENT.clear();PINS.clear();LAST.clear();LIGHTS.clear();STROKED.clear();PRINTS.clear();TEST_FAST=false;}
}
