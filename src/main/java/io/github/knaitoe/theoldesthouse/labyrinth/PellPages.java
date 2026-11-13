package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.PlaytestLog;
import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import io.github.knaitoe.theoldesthouse.journal.PinnedPage;
import io.github.knaitoe.theoldesthouse.journal.TornPages;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.544, the torn false page (docs/PELL_HOUSE_PAGE_0_4_544.md), from the proposal's "Being framed": "The reader can tear such a page
 * out, but a torn false page always turns up again: in Gail's folder on the last visit, in her hands; after the vignette, pinned to a
 * wall in a later hall."
 *
 * <ul>
 * <li><b>Which.</b> A page torn out of a reader's journal with a line in their own hand on it (JournalPage.ownHand): the house's false
 * entries (PellEntries, journal.OwnHand). Such a page tears out even with nothing else on it.</li>
 * <li><b>In Gail's folder.</b> From first light on the last visit, every such page of each reader the morning is for is in Gail's folder,
 * wherever it was: out of any pocket, off any wall, up from any floor (TornPages.keep), never in front of anybody looking at it. When
 * Gail has come up the path, a reader whose page she has who comes near her has it held out to them, once: "June gave me this, love.
 * It's your writing, isn't it?" A hand on her then is a hand on the page, and they read it. Then she puts it back: "It'll have to go in
 * your file." After Tilly answered, Gail's car waits for the reader until she has.</li>
 * <li><b>Pinned in a later hall.</b> Once the reader's ending has landed, each such page of theirs not put up by the House (in Gail's
 * folder, or anywhere else) turns up, one a hall, pinned to a wall in the next ordinary hall they come into: at their head height, a
 * few blocks off, where nobody can see it go up, and the pin heard by them alone (TornPages.turnUp). Like any pinned page, anybody
 * can read it and only its writer can take it down; it does not count among the pages they may leave in the House. Taken down and
 * torn out again, or knocked down, it turns up again.</li>
 * </ul>
 */
public final class PellPages {
    private PellPages(){}
    static final int G=PellPeople.GAIL;
    /** How near Gail a reader whose page she has comes for her to hold it out to them, and how long she holds it out for (if they do not
     *  put a hand on it to read it sooner). */
    public static final double NEAR=5;
    public static final int SHOW=200;
    /** How far from the reader a page turns up, at least and at most, and how long after they come into a hall the House looks for a
     *  wall there that nobody can see. */
    public static final int FROM=3,REACH=12,LOOK=1200;

    // ------------------------------------------------------------------------------------------------ which pages
    /** A reader's false pages out of their journal, in page order: torn pages with a line in their own hand on them. */
    public static List<Integer> torn(MinecraftServer s,UUID reader){return TornPages.pages(s,reader,JournalPage::ownHand);}
    /** The first of a reader's false pages in Gail's folder, or 0. */
    public static int inFolder(MinecraftServer s,UUID reader){for(int i:torn(s,reader))if(TornPages.where(s,reader,i)==TornPages.Where.KEPT)return i;return 0;}

    // ------------------------------------------------------------------------------------------------ Gail's folder
    /** From first light on the last visit, once a second: each false page of each reader the morning is for into Gail's folder, wherever
     *  it is, unless somebody can see it where it is (then the next second). */
    static void folder(MinecraftServer s,PellPeople.Household h,long now){
        if(now%20!=0)return;
        for(var id:h.atEnd)for(int i:torn(s,id)){
            if(TornPages.where(s,id,i)==TornPages.Where.KEPT)continue;
            var left=TornPages.leftAs(s,id,i);
            if(left!=null&&left.level() instanceof ServerLevel l&&LiteraryVignettes.watched(l,left.getBoundingBox().getCenter()))continue;
            TornPages.keep(s,id,i);
            var p=s.getPlayerList().getPlayer(id);if(p!=null)PlaytestLog.event(p,"pell_page_folder","page",i);
        }
    }
    /** Whether Gail is holding a page out now, to anybody; and to this reader. */
    static boolean showing(PellPeople.Household h){return h.showing!=null;}
    static boolean showingTo(PellPeople.Household h,UUID reader){return reader.equals(h.showing);}
    /** For tests: whose page Gail is holding out at this house now, or null. */
    public static @Nullable UUID showing(BlockPos b){return PellPeople.household(b).showing;}
    /** Whether the car waits for this reader: Gail has a page of theirs to hold out to them, or is holding it out. */
    static boolean owed(MinecraftServer s,PellPeople.Household h,UUID reader,net.minecraft.nbt.CompoundTag own){
        return showingTo(h,reader)||!own.getBoolean("PageShown")&&inFolder(s,reader)>0;
    }
    /**
     * Each tick at the curb once Gail is at the foot of the path (PellDawn): she holds a reader's page out to them when they come near,
     * once each, and puts it back in her folder after. {@code free}: she may begin now (she is not in the middle of what she says to
     * Tilly).
     */
    static void gail(ServerLevel l,PellPeople.Household h,LiteraryActor gail,List<ServerPlayer> here,long now,boolean free){
        var s=l.getServer();
        if(h.showing!=null){
            var p=s.getPlayerList().getPlayer(h.showing);var ref=TornPages.current(s,h.showing,h.showPage);
            boolean still=ref!=null&&ref.serial()==h.showSerial&&TornPages.where(s,h.showing,h.showPage)==TornPages.Where.KEPT;
            boolean gone=p==null||p.level()!=l||!p.isAlive()||p.distanceToSqr(gail)>16*16;
            if(!still||gone){putBack(gail,h);return;}
            h.rest[G]=PellPeople.yawTo(gail,p.position());
            if((now-h.showAt>=SHOW||h.showRead)&&PellVisit.ready(h,now,gail)){PellVisit.say(h,gail,G,PellTexts.GAIL_PAGE_FILE,now);putBack(gail,h);}
            return;
        }
        if(!free||!PellVisit.ready(h,now,gail))return;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||!h.atEnd.contains(p.getUUID()))continue;
            if(Math.hypot(p.getX()-gail.getX(),p.getZ()-gail.getZ())>NEAR)continue;
            var own=PellHouse.own(p);if(own.getBoolean("PageShown"))continue;
            int page=inFolder(s,p.getUUID());var ref=page==0?null:TornPages.current(s,p.getUUID(),page);if(ref==null)continue;
            h.showing=p.getUUID();h.showPage=page;h.showSerial=ref.serial();h.showAt=now;h.showRead=false;
            gail.setItemInHand(InteractionHand.MAIN_HAND,TornPages.stack(p.getUUID(),TornPages.writer(s,p.getUUID()),page,ref.serial()));
            gail.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(PellRegistry.FOLDER.get()));
            gail.gesture(PellPeople.SHOW);h.rest[G]=PellPeople.yawTo(gail,p.position());
            PellVisit.say(h,gail,G,PellTexts.GAIL_PAGE,now);
            PellHouse.cue(p,PellTexts.PAGE_HELD_OUT);
            own.putBoolean("PageShown",true);PellHouse.save(p,own);
            PlaytestLog.event(p,"pell_page_shown","page",page);
            return;
        }
    }
    /** Gail with her folder held to her chest again, as she stands, and holding nothing out. */
    private static void putBack(LiteraryActor gail,PellPeople.Household h){
        gail.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.FOLDER.get()));gail.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        gail.gesture(PellPeople.FOLDER);h.rest[G]=PellDawn.GAIL_LOOK;h.showing=null;h.showRead=false;
    }
    /** A hand on Gail while she holds a reader's page out to them: the page, to read, as any torn page is read. True if it answered. */
    static boolean talk(ServerPlayer p,PellPeople.Household h){
        if(!showingTo(h,p.getUUID()))return false;
        var ref=TornPages.current(p.server,p.getUUID(),h.showPage);
        if(ref!=null)TornPages.read(p,ref,TornPages.writer(p.server,p.getUUID()));
        h.showRead=true;return true;
    }

    // ------------------------------------------------------------------------------------------------ pinned in a later hall
    /** Readers come into an ordinary hall with a page to turn up there: the hall, and when they came in. */
    private record Want(LabyrinthPlace place,long since){}
    private static final Map<UUID,Want> WANT=new HashMap<>();
    /** The reader's next page to turn up: their ending at the house has landed, and it is out of their journal and the House has not put
     *  it where it is. 0 if none. */
    public static int next(ServerPlayer p){
        if(!PellHouse.own(p).getBoolean("Ended"))return 0;
        for(int i:torn(p.server,p.getUUID()))if(!TornPages.byHouse(p.server,p.getUUID(),i))return i;
        return 0;
    }
    /** A reader through a door into a place of the House (LabyrinthDoors.arrive): into an ordinary hall with a page to turn up, the House
     *  looks for a wall there for it. */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        if(p.isSpectator()||place==null||!place.isGray()||next(p)==0){WANT.remove(p.getUUID());return;}
        WANT.put(p.getUUID(),new Want(place,p.server.getTickCount()));
    }
    /** Every second for a reader in the House (PellAfterlife): a page of theirs up on a wall nobody sees, in the hall they have come into. */
    static void tick(ServerPlayer p,long now){
        var want=WANT.get(p.getUUID());if(want==null)return;
        if(now-want.since()>LOOK||!p.level().dimension().equals(HouseDimensions.INTERIOR)){WANT.remove(p.getUUID());return;}
        var origin=HouseSavedData.get(p.server).houseOrigin();
        if(origin==null||LabyrinthDoors.isBusy(p)||LabyrinthPlaces.placeAt(origin,p.blockPosition())!=want.place())return;
        if(turnUp(p,cell->LabyrinthPlaces.placeAt(origin,cell)==want.place())!=null)WANT.remove(p.getUUID());
    }
    /**
     * The reader's next page put up on a wall near them, in the space {@code inside} says is the hall's, where nobody can see it go up;
     * the pin is heard by them alone. Returns the page, or null if there is none to put up or no wall for it now.
     */
    public static @Nullable PinnedPage turnUp(ServerPlayer p,Predicate<BlockPos> inside){
        int page=next(p);if(page==0)return null;
        var spot=spot(p,inside);if(spot==null)return null;
        var pinned=TornPages.turnUp(p.serverLevel(),p.getUUID(),page,spot.support(),spot.face());if(pinned==null)return null;
        LiteraryVignettes.heard(p,SoundEvents.ITEM_FRAME_ADD_ITEM,spot.support().relative(spot.face()),.6F,1.3F);
        PlaytestLog.event(p,"pell_page_turned_up","page",page);
        return pinned;
    }
    /** A wall's face for a page: the block, and the side the page is on. */
    public record Spot(BlockPos support,Direction face){}
    /**
     * A wall near the reader for a page: a solid block at their head height whose face is clear in front, in the hall, not beside a door
     * and with no page on it, FROM to REACH from their eyes, and seen by nobody (spectators too); behind them if it can be, about six
     * blocks off.
     */
    public static @Nullable Spot spot(ServerPlayer p,Predicate<BlockPos> inside){
        var l=p.serverLevel();var head=p.blockPosition().above();var eye=p.getEyePosition();var look=p.getLookAngle();
        Spot best=null;double score=Double.MAX_VALUE;
        for(var at:BlockPos.betweenClosed(head.offset(-REACH,0,-REACH),head.offset(REACH,0,REACH))){
            var state=l.getBlockState(at);
            if(!state.isSolidRender(l,at)||l.getBlockEntity(at)!=null)continue;
            for(var face:Direction.Plane.HORIZONTAL){
                var front=at.relative(face);
                if(!inside.test(front)||!l.getBlockState(front).getCollisionShape(l,front).isEmpty()||!state.isFaceSturdy(l,at,face)||byDoor(l,front))continue;
                double out=.5+PinnedPage.THICK/2;var page=Vec3.atCenterOf(at).add(face.getStepX()*out,0,face.getStepZ()*out);
                double d=page.distanceTo(eye);if(d<FROM||d>REACH)continue;
                if(!l.getEntitiesOfClass(PinnedPage.class,new AABB(page,page).inflate(.6)).isEmpty()||LiteraryVignettes.watched(l,page))continue;
                double s=look.dot(page.subtract(eye).normalize())+Math.abs(d-6)*.02;
                if(s<score){score=s;best=new Spot(at.immutable(),face);}
            }
        }
        return best;
    }
    private static boolean byDoor(ServerLevel l,BlockPos front){
        for(var at:BlockPos.betweenClosed(front.offset(-1,-1,-1),front.offset(1,1,1)))if(l.getBlockState(at).getBlock() instanceof DoorBlock)return true;
        return false;
    }
    public static void clearAll(){WANT.clear();}
}
