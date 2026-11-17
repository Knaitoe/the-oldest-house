package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.667, Last Seen, the first part (docs/LAST_SEEN_0_4_667.md), from the owner's approval of docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md.
 * The place alone: the municipal office at the edge of a city park, with its front desk, its poster wall and its archive of folders, and
 * the park itself in the morning, empty, with the avenue past its railing (LastSeenSite). Three people work in the office: Nora, who
 * keeps the archive, at her desk; Patricia and Luz behind the counter (stranger phases 253 to 255). Each has a line of their own, and every
 * thing answers a hand. Nobody comes back yet (part 2). The House does not deal it until part 2.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastSeen {
    private LastSeen(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_SEEN;
    /** The ten who come back are on stranger phases 243 to 252 (their skins are painted now; part 2 brings them); the office's three on 253 to 255. */
    public static final int CHILDREN_FIRST=243,CHILDREN=10,FIRST=253,NORA=0,PATRICIA=1,LUZ=2,PEOPLE=3;
    private static final String[] CHILD_SKINS={"lastseen_rocio","lastseen_tomas","lastseen_micaela","lastseen_brian","lastseen_daiana","lastseen_lautaro","lastseen_abril","lastseen_joel","lastseen_yanina","lastseen_santino"};
    private static final String[] SKINS={"lastseen_nora","lastseen_patricia","lastseen_luz"};
    private static final BlockPos[] SEATS={LastSeenSite.NORA_AT,LastSeenSite.PATRICIA_AT,LastSeenSite.LUZ_AT};
    public static boolean cast(int phase){return phase>=CHILDREN_FIRST&&phase<FIRST+PEOPLE;}
    public static boolean office(int phase){return phase>=FIRST&&phase<FIRST+PEOPLE;}
    public static String skin(int phase){return phase>=FIRST?SKINS[phase-FIRST]:CHILD_SKINS[phase-CHILDREN_FIRST];}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Not dealt until part 2, when the first of them comes back. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}

    /** A reader in: the office's three are put at their places if they are not there, and the morning is heard through the door. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();ensure(l,b);
        own.putInt("Visits",own.getInt("Visits")+1);
        ACTIVE.computeIfAbsent(b.asLong(),k->new Site(b));
        l.playSound(null,b.offset(0,1,-14),LastSeenRegistry.TRAFFIC.get(),SoundSource.AMBIENT,.35F,1F);
    }

    /** One of the office's three, if their body is at their place. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var at=b.offset(SEATS[who]);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(at).inflate(3),x->is(x)&&x.phase()==FIRST+who))if(a.isAlive())return a;
        return null;
    }
    /** Each of the three is sat in their chair if they are not there; nothing is made while the office's entities are unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        for(int who=0;who<PEOPLE;who++){
            var at=b.offset(SEATS[who]);
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var a=find(l,b,who);
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                sit(l,a,at);
                if(!l.addFreshEntity(a))continue;
            }
            sit(l,a,at);
        }
    }
    /** In the chair at the cell, turned the way the chair is (ActorSeat lowers the body onto its seat and keeps it there). */
    static void sit(ServerLevel l,LiteraryActor a,BlockPos at){
        var st=l.getBlockState(at);
        float yaw=LastSeenBlock.is(st,LastSeenBlock.Kind.CHAIR)?st.getValue(LastSeenBlock.FACING).toYRot():Direction.SOUTH.toYRot();
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.gesture(0);a.setDeltaMovement(Vec3.ZERO);
    }

    /** A word with one of the three: their own first line, then a second, then the second again. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!office(a.phase()))return;
        int who=a.phase()-FIRST,heard=own.getInt("Heard"+who);
        String line=LastSeenTexts.line(who,heard);
        own.putInt("Heard"+who,heard+1);
        a.say(LastSeenTexts.NAMES[who],line);a.speaks(line);
    }

    /** A hand on something of the office's or the park's. True if it was theirs to answer. The card is the first paper: the House's own reading answers it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        // The houses across the avenue keep their doors locked; only the way on opens (OnwardPlacements sets that one).
        if(st.getBlock() instanceof net.minecraft.world.level.block.DoorBlock&&rel.getZ()==LastSeenSite.HOUSE_FRONT&&rel.getX()!=LastSeenSite.ONWARD.getX()){cue(p,LastSeenTexts.HOUSE_DOOR);return true;}
        if(!LastSeenBlock.is(st))return false;
        // The park's gate is shut but not locked: a hand swings both leaves open, and they stay open for everyone.
        if(st.getValue(LastSeenBlock.KIND)==LastSeenBlock.Kind.GATE&&st.getValue(LastSeenBlock.STAGE)==LastSeenBlock.SHUT){
            LastSeenSite.gate(l,b,LastSeenBlock.OPEN);
            l.playSound(null,b.offset(LastSeenSite.GATE_W),net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.7F,.8F);
            cue(p,LastSeenTexts.GATE);return true;
        }
        int stage=st.getValue(LastSeenBlock.STAGE);
        String line=switch(st.getValue(LastSeenBlock.KIND)){
            case CARD->null;
            case COUNTER->stage==1?LastSeenTexts.FLAP:LastSeenTexts.COUNTER;
            case CHAIR->LastSeenTexts.CHAIR;
            case SHELF->stage==LastSeenBlock.ONE_OUT?LastSeenTexts.SHELF_GAP:stage==LastSeenBlock.BOXES?LastSeenTexts.BOXES:LastSeenTexts.SHELF;
            case POSTER->LastSeenTexts.poster(stage);
            case POSTER_B->LastSeenTexts.poster(8+stage);
            case TABLE->stage==1?LastSeenTexts.PILE:stage==2?LastSeenTexts.LAMP:LastSeenTexts.TABLE;
            case DESK->stage==LastSeenBlock.DRAWN?LastSeenTexts.DESK_OPEN:LastSeenTexts.DESK;
            case LOUVRE->LastSeenTexts.LOUVRE;
            case TUBE->LastSeenTexts.TUBE;
            case RAILING->stage==1?LastSeenTexts.POST:LastSeenTexts.RAILING;
            case GATE->stage==LastSeenBlock.CHAINED?LastSeenTexts.GATE_CHAINED:LastSeenTexts.GATE_OPEN;
            case LAMP_POST->stage==LastSeenBlock.DARK?LastSeenTexts.LAMP_DARK:LastSeenTexts.LAMP_POST;
            case BENCH->LastSeenTexts.BENCH;
            case FOUNTAIN->stage>=2?LastSeenTexts.BOWL:LastSeenTexts.FOUNTAIN;
            case KIOSK->LastSeenTexts.KIOSK;
            case TRUNK->LastSeenTexts.TRUNK;
            case LEAVES->stage==1?LastSeenTexts.SILK:LastSeenTexts.PLANE;
            case PLAY->LastSeenTexts.play(stage);
            case BANDSTAND->LastSeenTexts.BANDSTAND;
            case SHELTER->stage==2?LastSeenTexts.STOP:LastSeenTexts.SHELTER;
            case SHUTTER->LastSeenTexts.SHUTTER;
        };
        if(line==null)return false;
        cue(p,line);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the morning
    /** The sites a reader is in or has just left: their clock for the morning's sounds. */
    static final Map<Long,Site> ACTIVE=new HashMap<>();
    static final class Site{final BlockPos base;int tick,empty;Site(BlockPos b){base=b;}}
    static final int EMPTY_FORGET=100;
    /** Everyone in the scene: inside its railing, in the office or on the avenue. */
    public static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players()){
            if(p.isSpectator()||!p.isAlive())continue;
            var r=p.position().subtract(Vec3.atLowerCornerOf(b));
            if(r.x>=LastSeenSite.WEST&&r.x<=LastSeenSite.EAST+1&&r.z>=LastSeenSite.NORTH&&r.z<=1&&r.y>=-4&&r.y<=16)out.add(p);
        }
        return out;
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){ACTIVE.clear();}
    /**
     * Every few seconds while someone is there, the morning: the avenue's traffic beyond the railing, pigeons somewhere on the paths,
     * and, while a reader is in the office, the typewriter at Nora's desk. Each is heard where it is.
     */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(ACTIVE.isEmpty())return;
        var l=e.getServer().getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var it=ACTIVE.values().iterator();it.hasNext();){
            var s=it.next();var here=readers(l,s.base);
            if(here.isEmpty()){if(++s.empty>=EMPTY_FORGET)it.remove();continue;}
            s.empty=0;s.tick++;
            var rnd=l.getRandom();
            if(s.tick%240==60)play(l,s.base.offset(-8+rnd.nextInt(34)-17,1,LastSeenSite.ROAD_S-3),LastSeenRegistry.TRAFFIC.get(),.6F);
            if(s.tick%300==150)play(l,s.base.offset(LastSeenSite.PATH_X+rnd.nextInt(9)-4,0,LastSeenSite.PATH_Z+rnd.nextInt(30)-15),LastSeenRegistry.PIGEONS.get(),.5F);
            if(s.tick%180==90&&here.stream().anyMatch(p->LastSeenSite.inOffice(p.position().subtract(Vec3.atLowerCornerOf(s.base)))))
                play(l,s.base.offset(LastSeenSite.DESK).above(),LastSeenRegistry.TYPEWRITER.get(),.45F);
        }
    }
    static void play(ServerLevel l,BlockPos at,SoundEvent e,float volume){l.playSound(null,at,e,SoundSource.AMBIENT,volume,.95F+l.getRandom().nextFloat()*.1F);}
}
