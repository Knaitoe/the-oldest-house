package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.404, the owner: "a head full of ghosts should allow multiple players too. only one version of the cast is rendered for the primary
 * player - the rest see nothing."
 * <p>
 * Each reader used to film a family of their own, seen by them alone. Several readers on the set at once were each in their own house of
 * private bodies. Now the set shoots one production at a time.
 * <ul>
 * <li><b>The lead.</b> The first reader filming on the set leads, and the cast is theirs. The lead keeps it while they stay.</li>
 * <li><b>The crew.</b> Every other reader filming there is a second camera. They see the lead's family and crew, hear their lines, and
 * roll on the lead's take. Their own cast stays out of their sight.</li>
 * <li><b>What the crew keeps.</b> Their own footage is theirs: what their own camera held goes into their own camera log and cut, and a
 * day filmed to the end counts as filmed for everyone there.</li>
 * <li><b>The last day.</b> A reader who is there when the last day ends has seen the filming through, whichever days they missed.</li>
 * <li><b>A lead who leaves.</b> The longest-present crew member takes over the same bodies, where they stand, and the take goes on.</li>
 * <li><b>A crew member who leaves.</b> They start from their first unfilmed day when they come back alone.</li>
 * </ul>
 */
public final class GhostsCrew {
    /** Game time the reader came onto the set this visit, for who leads. */
    static final String ON_SET="OnSet0404";
    /** Set while a reader is a second camera on someone else's take; their next day alone starts fresh. */
    static final String CREWED="Crewed0404";
    /** The reader was on the set when the last day ended. */
    static final String WATCHED="WatchedEnd0404";
    /** The reader has been a second camera at least once; their account says so. */
    static final String SECOND_CAMERA="SecondCamera0404";
    /** The set's current lead, kept in the place's shared state. */
    private static final String LEAD="CrewLead0404";
    /** Each crewing reader's lead. A reader not in it sees their own cast. */
    private static final Map<UUID,UUID> LEAD_OF=new ConcurrentHashMap<>();
    /** Each lead's crew, in the order they came onto the set. */
    private static final Map<UUID,List<UUID>> CREW=new ConcurrentHashMap<>();
    /**
     * Each reader keeps their own camera frames, lens settings and conversation progress.
     * A second camera follows the lead's other production keys.
     */
    private static final Set<String> PERSONAL=Set.of("FrameNonce","FilmIdle0490","GhostEyeX","GhostEyeY","GhostEyeZ","GhostYaw","GhostPitch","JumpBodyYaw",
            GhostsSet.MERRY_LINE_BEFORE,GhostsSet.MERRY_LINE_AFTER);
    /** The production keys a second camera follows besides the day's own keys. */
    private static final List<String> PRODUCTION=List.of("FilmDay","ShotClock","Rolling","DayDone","CastPlaced","BedroomPickup","PickupStep0","PickupStep1","AcceptedScene131","MerryExitStep0488","MerryLeft0488","LightsOut0242");
    private GhostsCrew(){}

    public static boolean castRole(int role){return role>=LiteraryActor.GHOST_MARJORIE&&role<=LiteraryActor.GHOST_DRAWER;}
    /** Whose cast this reader sees on the set: their lead's while they crew, otherwise their own. */
    public static UUID castOf(UUID reader){return LEAD_OF.getOrDefault(reader,reader);}
    public static boolean crewing(UUID reader){return LEAD_OF.containsKey(reader);}
    public static @Nullable ServerPlayer lead(ServerPlayer reader){var id=LEAD_OF.get(reader.getUUID());return id==null?null:reader.server.getPlayerList().getPlayer(id);}
    /** The lead's second cameras who are online and on the set now. */
    public static List<ServerPlayer> crew(ServerPlayer lead){
        var ids=CREW.get(lead.getUUID());if(ids==null||ids.isEmpty())return List.of();
        var out=new ArrayList<ServerPlayer>();for(var id:ids){var q=lead.server.getPlayerList().getPlayer(id);if(q!=null&&LiteraryVignettes.inside(q,GhostsSet.PLACE))out.add(q);}
        return out;
    }
    public static void clearAll(){LEAD_OF.clear();CREW.clear();}

    private static boolean filming(LabyrinthData d,ServerPlayer p){
        if(!LiteraryVignettes.inside(p,GhostsSet.PLACE))return false;
        var own=LiteraryVignettes.personal(d,p.getUUID(),GhostsSet.PLACE);
        return own.getBoolean("Here")&&own.getBoolean("Filming0485")&&!own.getBoolean("Completed")&&!own.getBoolean("Ready");
    }
    /** Called once per scene pass, before the readers' own ticks: who leads the set, and who crews for them. */
    public static void refresh(MinecraftServer server){
        var d=LabyrinthData.get(server);var world=LiteraryVignettes.shared(d,GhostsSet.PLACE);
        var here=new ArrayList<ServerPlayer>();for(var p:server.getPlayerList().getPlayers())if(filming(d,p))here.add(p);
        UUID stored=world.hasUUID(LEAD)?world.getUUID(LEAD):null;
        here.sort(Comparator.comparingLong((ServerPlayer p)->{var own=LiteraryVignettes.personal(d,p.getUUID(),GhostsSet.PLACE);return own.contains(ON_SET)?own.getLong(ON_SET):Long.MAX_VALUE;}).thenComparing(p->p.getUUID().toString()));
        ServerPlayer lead=null;for(var p:here)if(p.getUUID().equals(stored))lead=p;
        if(lead==null&&!here.isEmpty()){
            lead=here.get(0);
            // The lead left with a crew still rolling: the next camera takes over the same bodies where they stand.
            if(stored!=null&&stored.equals(LEAD_OF.get(lead.getUUID()))){
                var old=LiteraryVignettes.personal(d,stored,GhostsSet.PLACE);
                if(!old.getBoolean("Ready")&&!old.getBoolean("Completed"))handOver(server,d,stored,lead);
                else{var own=LiteraryVignettes.personal(d,lead.getUUID(),GhostsSet.PLACE);own.remove(CREWED);own.putBoolean("CastPlaced",false);LiteraryVignettes.save(d,lead.getUUID(),GhostsSet.PLACE,own);}
            }
        }
        LEAD_OF.clear();CREW.clear();
        if(lead==null){if(stored!=null){world.remove(LEAD);LiteraryVignettes.shared(d,GhostsSet.PLACE,world);}return;}
        if(!lead.getUUID().equals(stored)){world=LiteraryVignettes.shared(d,GhostsSet.PLACE);world.putUUID(LEAD,lead.getUUID());LiteraryVignettes.shared(d,GhostsSet.PLACE,world);}
        var crew=new ArrayList<UUID>();for(var p:here)if(p!=lead){crew.add(p.getUUID());LEAD_OF.put(p.getUUID(),lead.getUUID());}
        if(!crew.isEmpty())CREW.put(lead.getUUID(),List.copyOf(crew));
    }
    /** The bodies follow the take: the old lead's cast becomes the new lead's, and theirs (if any) the old lead's. */
    static void handOver(MinecraftServer server,LabyrinthData d,UUID from,ServerPlayer to){
        var world=LiteraryVignettes.shared(d,GhostsSet.PLACE);var level=to.serverLevel();
        var keys=new ArrayList<String>();for(var who:GhostsSet.PEOPLE)keys.add("Film"+who);keys.add("FilmPlayhouse");keys.add("FilmDrawer");
        for(var key:keys){
            String a=key+"_"+from,b=key+"_"+to.getUUID();UUID theirs=world.hasUUID(a)?world.getUUID(a):null,mine=world.hasUUID(b)?world.getUUID(b):null;
            world.remove(a);world.remove(b);
            if(theirs!=null){world.putUUID(b,theirs);if(level.getEntity(theirs) instanceof LiteraryActor actor)actor.bind(GhostsSet.PLACE,to.getUUID());}
            if(mine!=null){world.putUUID(a,mine);if(level.getEntity(mine) instanceof LiteraryActor actor)actor.bind(GhostsSet.PLACE,from);}
        }
        LiteraryVignettes.shared(d,GhostsSet.PLACE,world);
        // A take they walked out of went on without them; when they come back alone they start their next day afresh. A day already
        // done is theirs as it was, and their next arrival goes on to the next day as usual.
        var old=LiteraryVignettes.personal(d,from,GhostsSet.PLACE);old.putBoolean("CastPlaced",false);if(!old.getBoolean("DayDone"))old.putBoolean(CREWED,true);LiteraryVignettes.save(d,from,GhostsSet.PLACE,old);
        var own=LiteraryVignettes.personal(d,to.getUUID(),GhostsSet.PLACE);own.remove(CREWED);LiteraryVignettes.save(d,to.getUUID(),GhostsSet.PLACE,own);
    }

    /** A second camera follows the lead's take: the day, the clock and where every beat of it has got to. */
    static void follow(ServerPlayer q,CompoundTag ownQ,CompoundTag ownL){
        var keys=new LinkedHashSet<String>(PRODUCTION);keys.addAll(GhostsSet.dayKeys());keys.removeAll(PERSONAL);
        for(var key:keys){var v=ownL.get(key);if(v==null)ownQ.remove(key);else ownQ.put(key,v.copy());}
        ownQ.putBoolean(CREWED,true);ownQ.putBoolean(SECOND_CAMERA,true);
        // On the sixth day the lead's lens holds on Marjorie mid-leap; a second camera's holds from where that camera stands.
        if(ownL.getBoolean("JumpFrozen0488")&&!ownL.getBoolean("DayDone")){
            if(!ownQ.contains("GhostEyeX")&&q.serverLevel().getEntity(ownL.getInt("GhostFreezeTarget")) instanceof LiteraryActor marjorie){
                var eye=q.getEyePosition();var aim=marjorie.getEyePosition().subtract(eye);
                ownQ.putDouble("GhostEyeX",eye.x);ownQ.putDouble("GhostEyeY",eye.y);ownQ.putDouble("GhostEyeZ",eye.z);
                ownQ.putFloat("GhostYaw",(float)(Math.toDegrees(Math.atan2(aim.z,aim.x))-90));ownQ.putFloat("GhostPitch",(float)-Math.toDegrees(Math.atan2(aim.y,Math.sqrt(aim.x*aim.x+aim.z*aim.z))));
            }
        }else for(var key:List.of("GhostEyeX","GhostEyeY","GhostEyeZ","GhostYaw","GhostPitch","JumpBodyYaw"))ownQ.remove(key);
    }
    /** A day filmed to the end is filmed for every camera there; the last one ends the filming for them. */
    static void dayEnded(CompoundTag own,int day){
        own.putInt("FilmedDays",own.getInt("FilmedDays")|(1<<(day-1)));own.putInt("Beat",own.getInt("Visit"));if(day==GhostsSet.DAYS)own.putBoolean(WATCHED,true);
    }
    /** The first day this reader has not filmed, or the last day once every one is filmed. */
    static int firstUnfilmed(CompoundTag own){int filmed=own.getInt("FilmedDays");for(int d=1;d<=GhostsSet.DAYS;d++)if((filmed&(1<<(d-1)))==0)return d;return GhostsSet.DAYS;}
    /** For the status line and tests. */
    public static String describe(ServerPlayer p){var lead=LEAD_OF.get(p.getUUID());if(lead!=null)return "second camera for "+lead;var crew=CREW.get(p.getUUID());return crew==null?"alone":"lead with "+crew.size();}
}
