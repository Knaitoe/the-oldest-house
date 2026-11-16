package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.Story;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.596: the reader's own stories' people on the great staircase (docs/STAIR_COMPANY_0_4_596.md).
 *
 * <p>The owner, on 15 November: "I'd like to reintegrate vignettes, maybe on the Witness end? Key NPCs haunt the player on their
 * final exit?" What was agreed: only the stories that reader heard to the end; on the climb home their people stand at the reader's
 * five fires, the most recent deepest and the first by the door, silent, moving only while unseen, so the company behind is closer
 * each time the reader looks back; and on the way down the same people stand as unlit shapes at the edge of the book's light, gone
 * when reached.
 *
 * <p>Each reader's people are their own: nobody else sees them, nothing is saved of them, and none of them has a line. A figure here
 * is one of the cast's bodies ({@link LiteraryActor}) dressed as its story dressed it and set down on the stair; its story's scene
 * does not run it. {@link #FIGURES} is the list, one row a figure, so any of them can be changed there. The stories whose figure is
 * some other kind of body are listed in {@link #NOT_YET} and have nobody on the stair until that body is given a way to stand here.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StairCompany {
    private StairCompany(){}
    /** What a figure of the company is bound to in place of a story's place. */
    public static final String SCENE="stair_company";

    // ------------------------------------------------------------------------------------------------ who they are
    /**
     * One figure: what the notes call it, the body it is dressed as, how it is posed, its size where that is not the body's own
     * (0 leaves it), what it holds, and the story (or stories) it is from.
     */
    public record Figure(String name,int role,int phase,int gesture,double scale,ItemStack held,Story... stories){
        boolean from(Story story){for(var s:stories)if(s==story)return true;return false;}
    }
    private static Figure figure(String name,int role,int phase,Story... stories){return new Figure(name,role,phase,0,0,ItemStack.EMPTY,stories);}
    private static Figure posed(String name,int role,int phase,int gesture,ItemStack held,Story... stories){return new Figure(name,role,phase,gesture,0,held,stories);}
    private static Figure sized(String name,int role,int phase,double scale,Story... stories){return new Figure(name,role,phase,0,scale,ItemStack.EMPTY,stories);}
    /** In her room a reader is a child's height, a little over half their own; on the stair they are their own height, so she is made as much taller. */
    public static final double MOTHER=1.85;
    private static final int S=LiteraryActor.STRANGER;
    /** The owner's list of 15 November, as far as the cast's own bodies go. A story with two rows has two people. */
    public static final List<Figure> FIGURES=List.of(
            figure("the boy, crouched",LiteraryActor.SILHOUETTE,4,Story.PLAIN),
            figure("Holloway",S,KitchenVisitors.HOLLOWAY,Story.HOLLOWAY),
            figure("the red figure",LiteraryActor.RED_FIGURE,0,Story.MASQUE),
            figure("the woman from the coffin",LiteraryActor.COFFIN_WOMAN,0,Story.USHER),
            figure("the Lady",LiteraryActor.LADY,0,Story.BLY_ROUTE),
            figure("the father, as the last night has him",LiteraryActor.SILHOUETTE,3,Story.MAPPING_INTERIOR),
            figure("Father",LiteraryActor.FATHER,1,Story.HOLY_RABBIT),
            figure("the listener",LiteraryActor.VISITOR,0,Story.CONFESSION),
            figure("the man with the blade",LiteraryActor.KILLER,ElkHunt.WATCH,Story.ELK_CARCASSES,Story.CAMP_BLOOD),
            figure("old Hank",S,21,Story.COSTUME_NIGHT),
            figure("Ray, the projectionist",S,14,Story.MOVIE_NIGHT),
            figure("Hank as a boy",S,35,Story.WINTER_LAKE),
            figure("the one in Tommy's shape",LiteraryActor.SILHOUETTE,DevilsRockNights.DOUBLE_PHASE,Story.DEVILS_ROCK),
            sized("Mother",LiteraryActor.MOTHER,0,MOTHER,Story.WHEEL),
            figure("Marjorie",LiteraryActor.GHOST_MARJORIE,GhostsSet.STAND,Story.GHOSTS_SET),
            figure("Leonard",S,0,Story.END_WORLD_CABIN),
            figure("the father at the door",LiteraryActor.FAMILY_FATHER,0,Story.FAMILY_COPY),
            figure("the man from the mill",S,13,Story.THREE_FINGERS),
            figure("Tess",LiteraryActor.LAKE_TESS,0,Story.LAKE_HOUSE),
            figure("the Sheriff",LiteraryActor.BORRASCA_SHERIFF,0,Story.BORRASCA),
            figure("one of the Propped",LiteraryActor.UNCANNY_PROPPED,1,Story.PAINTED_INTERIOR),
            figure("the second waitress",LiteraryActor.UNCANNY_FALSE_WAITRESS,2,Story.SECOND_WAITRESS),
            figure("the bent-neck woman",LiteraryActor.UNCANNY_GIANT,1,Story.TUNNEL_PERIPHERY),
            figure("the man from the monitor",LiteraryActor.UNCANNY_MARIONETTE,1,Story.MISSING_SLEEP),
            figure("the man by the sump",LiteraryActor.UNCANNY_PEELER,0,Story.LAYERED_MAN),
            figure("the one at the glass",LiteraryActor.UNCANNY_VISITOR,1,Story.MOTION_LIGHT),
            figure("the whistler",LiteraryActor.WHISTLER,0,Story.WHISTLERS),
            figure("Chad",S,39,Story.CLOSING_HALL),
            figure("Daisy",S,40,Story.CLOSING_HALL),
            posed("the one with the reader's walk",LiteraryActor.LOOKOUT_DOUBLE,0,2,new ItemStack(Items.TORCH),Story.OWN_LIGHT),
            figure("one of the two men who wrote it down",S,41,Story.POLYBIUS),
            posed("the replacement",S,Replacement.PHASE,CostumeNight.CRAWL,ItemStack.EMPTY,Story.REPLACEMENT),
            figure("Kelly",S,PeopleLights.KELLY,Story.PEOPLE_LIGHTS),
            figure("the resident",LiteraryActor.SP_RESIDENT,0,Story.SECOND_PERSON),
            figure("Mara",S,PlaceForYou.MARA,Story.PLACE_FOR_YOU),
            figure("the tenant",S,VacantPossession.TENANT,Story.VACANT_POSSESSION),
            figure("the woman who stood up out of the doe",LiteraryActor.ROAD_WOMAN,DeerRoad.STAND,Story.DEER_ROAD),
            posed("John, with his spade",LiteraryActor.GHOST_JOHN,GhostsSet.STAND,0,new ItemStack(Items.IRON_SHOVEL),Story.GROWING_THINGS),
            figure("the driver who pulled over",S,TallGrass.DRIVER,Story.TALL_GRASS),
            figure("Corinne",S,MarshSupper.CORINNE,Story.MARSH_SUPPER),
            figure("Vera",S,LastCrossing.VERA,Story.LAST_CROSSING),
            figure("the one in the mask",S,Pinehook.MASKED,Story.PINEHOOK),
            figure("the man with the list",S,Drawing.ALBRIGHT,Story.THE_DRAWING),
            figure("the girl with one arm",S,Corner.ROMINA,Story.CORNER_HOUSE),
            figure("Silas",S,Kept.SILAS,Story.KEPT),
            figure("the pale man from booth four",S,LastOrders.STRANGER,Story.LAST_ORDERS),
            figure("Tully",S,BikeNightBullies.TULLY,Story.BIKE_NIGHT),
            figure("June",S,PellPeople.FIRST+PellPeople.JUNE,Story.PELL_HOUSE));
    /**
     * Stories on the owner's list whose figure is not one of the cast's bodies (the lake's people, the elk, the cousin, the hotel's
     * and the table's, the girl from the wardrobe, the ones drawn as blocks or in a mirror), or is one that is only ever drawn seated
     * or swimming (Mr. Harrigan, the swimmer), and the lodging house's people, drawn by their own renderer (the ones already inside). Nobody stands on the stair for these yet.
     */
    public static final Set<Story> NOT_YET=EnumSet.of(Story.CLAP,Story.HARRIGAN,Story.DROWNED_TOWN,Story.PRESERVED_CAVE,Story.SHALLOWS,Story.GOATMAN,
            Story.SEANCE,Story.HOTEL,Story.ELK_LOT,Story.ELK_FAN,Story.BACKSEAT,Story.DARE_IN_THE_DARK,Story.WORK_PRINT,Story.LEFT_RIGHT,Story.DEAD_AIR,
            Story.LAST_SWIMMER,Story.ONES_INSIDE);
    /** The Mother of Strays is not to be changed, so she is not made to stand here. */
    public static final Set<Story> LEFT_OUT=EnumSet.of(Story.MOTHER);
    /** Stories in which nobody is seen. */
    public static final Set<Story> NOBODY=EnumSet.of(Story.FLOORBOARDS,Story.MODEL_HOME,Story.PHONE_CANOE,Story.TED_CAVER,Story.ZAMPANO,Story.WHALE,
            Story.BARN_WELL,Story.HOSPITAL,Story.WALLPAPER,Story.HILL_NURSERY,Story.MINIATURES,Story.WINCHESTER,Story.CHILD_ROOM,Story.CRIMSON_HALL,
            Story.SILO,Story.FEET_UP,Story.POLAROID_TRAIL,Story.STOREROOM,Story.STILLNESS,Story.LINE,Story.STATE_ROUTE,Story.HOUSEHOLD,Story.BLACK_CITY);

    /** A reader's people, in the order their stories ended: only stories they heard to the end, each figure once. */
    public static List<Figure> people(ServerPlayer p){
        var data=LabyrinthData.get(p.server);var out=new LinkedHashSet<Figure>();
        for(var story:WitnessAccount.inOrder(WitnessAccount.record(data,p.getUUID())))if(WitnessAccount.has(data,p.getUUID(),story))
            for(var f:FIGURES)if(f.from(story))out.add(f);
        return List.copyOf(out);
    }
    /** Which of the five camps a reader's figure belongs to: the first of their stories by the door, the most recent deepest. */
    public static int camp(int index,int count){return count<=0?0:Math.min(StaircaseFire.REQUIRED-1,index*StaircaseFire.REQUIRED/count);}

    // ------------------------------------------------------------------------------------------------ the stair
    /** The route step nearest a point. The stair only falls, so the steps at about a point's height are few. */
    public static int stepAt(BlockPos origin,Vec3 at){
        var route=FinaleArchitecture.staircaseRoute(origin);int lo=0,hi=route.size()-1;
        while(lo<hi){int mid=(lo+hi)>>>1;if(route.get(mid).getY()>at.y+3)lo=mid+1;else hi=mid;}
        int best=lo;double nearest=Double.MAX_VALUE;
        for(int i=lo;i<route.size()&&route.get(i).getY()>=at.y-3;i++){
            var c=route.get(i);double dx=c.getX()+.5-at.x,dy=c.getY()-at.y,dz=c.getZ()+.5-at.z,d=dx*dx+dz*dz+dy*dy*.25;
            if(d<nearest){nearest=d;best=i;}
        }
        return best;
    }
    /** Where a body stands this far along the route (a step may be part of one) and this far to one side of the middle of the walk. */
    public static Vec3 at(ServerLevel level,BlockPos origin,double step,double side){
        var route=FinaleArchitecture.staircaseRoute(origin);int n=route.size();
        double s=Math.max(0,Math.min(n-1,step));int i=(int)Math.floor(s),j=Math.min(n-1,i+1);double t=s-i;
        var a=route.get(i);var b=route.get(j);var before=route.get(Math.max(0,i-1));var after=route.get(Math.min(n-1,i+2));
        double x=a.getX()+.5+(b.getX()-a.getX())*t,z=a.getZ()+.5+(b.getZ()-a.getZ())*t;
        double dx=Math.signum(after.getX()-before.getX()),dz=Math.signum(after.getZ()-before.getZ()),length=Math.max(1,Math.hypot(dx,dz));
        x+=-dz/length*side;z+=dx/length*side;
        return new Vec3(x,ground(level,x,Math.max(a.getY(),b.getY()),z),z);
    }
    /** The top of whatever is underfoot at a place on the stair; the route's own height where nothing is built. */
    static double ground(ServerLevel level,double x,int y,double z){
        var at=new BlockPos.MutableBlockPos();
        for(int dy=1;dy>=-3;dy--){
            at.set(Mth.floor(x),y+dy,Mth.floor(z));if(!level.isLoaded(at))break;
            var shape=level.getBlockState(at).getCollisionShape(level,at);
            if(!shape.isEmpty())return at.getY()+shape.max(Direction.Axis.Y);
        }
        return y;
    }
    /** Where round a camp's fire its people stand, counted out from the landing's corner toward the walls: the fire is at (3, 3). */
    public static final int[][] SPOTS={{1,3},{3,1},{5,5},{1,1},{5,1},{1,5},{5,3},{3,5},{0,2},{2,0},{0,4},{4,0},{6,5},{5,6},{6,1},{1,6},
            {0,6},{6,0},{7,5},{5,7},{7,1},{1,7},{8,3},{3,8}};
    private static Vec3 hearth(BlockPos origin,int camp){return StaircaseFire.braziers(origin).get(camp).getBottomCenter();}
    /** The place of the nth of a camp's people. */
    public static Vec3 spot(ServerLevel level,BlockPos origin,int camp,int n){
        var landing=StaircaseFire.landings(origin).get(camp);var b=FinaleArchitecture.base(origin);
        int sx=Integer.signum(landing.getX()-b.getX()),sz=Integer.signum(landing.getZ()-b.getZ());var s=SPOTS[n%SPOTS.length];
        double x=landing.getX()+sx*s[0]+.5,z=landing.getZ()+sz*s[1]+.5;
        return new Vec3(x,ground(level,x,landing.getY(),z),z);
    }
    private static float yaw(Vec3 from,Vec3 to){return (float)(Math.toDegrees(Math.atan2(to.z-from.z,to.x-from.x))-90);}
    /** How far a reader's eye takes a figure in, and how far to the side of where they look (the cosine of it). */
    public static final double FAR_SIGHT=72,VIEW=.2;
    /** Whether a reader has a place in view: in front of them, not too far off, and nothing solid between. */
    public static boolean sees(ServerPlayer p,Vec3 at){
        var eye=p.getEyePosition();var d=at.subtract(eye);double length=d.length();
        if(length<.5)return true;if(length>FAR_SIGHT)return false;
        if(p.getLookAngle().dot(d.scale(1/length))<VIEW)return false;
        return p.level().clip(new ClipContext(eye,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
    }
    private static boolean sees(ServerPlayer p,Vec3 feet,double height){return sees(p,feet.add(0,height*.9,0))||sees(p,feet.add(0,height*.4,0));}
    /** Sets one of a reader's people down: theirs alone to see, held up by nothing, moved by nothing, and nothing saved of it. */
    public static @Nullable LiteraryActor spawn(ServerLevel level,ServerPlayer p,Figure f,Vec3 at,float yaw){return spawn(level,p,f,at,yaw,false);}
    /** As a shape, it is never lit: a reader on their way down makes out a dim figure and nothing in it. */
    public static @Nullable LiteraryActor spawn(ServerLevel level,ServerPlayer p,Figure f,Vec3 at,float yaw,boolean shape){
        if(!level.isLoaded(BlockPos.containing(at)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(level);if(a==null)return null;
        a.company(p.getUUID());a.appearance(f.role(),f.phase());a.gesture(f.gesture());a.shape(shape);
        if(f.scale()>0){var size=a.getAttribute(Attributes.SCALE);if(size!=null){size.setBaseValue(f.scale());a.refreshDimensions();}}
        if(!f.held().isEmpty())a.setItemInHand(InteractionHand.MAIN_HAND,f.held().copy());
        a.noPhysics=true;a.setNoGravity(true);a.setSilent(true);a.setInvulnerable(true);
        a.moveTo(at.x,at.y,at.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
        return level.addFreshEntity(a)?a:null;
    }
    /**
     * A body that is still on the stair. The bodies themselves are held, not their ids: nothing of them is saved, so one whose part of
     * the stair has been unloaded is not dropped with it but hidden until it loads again, and a lookup by id would call it gone.
     */
    private static @Nullable LiteraryActor body(@Nullable LiteraryActor a){return a!=null&&!a.isRemoved()&&a.isAlive()?a:null;}

    // ------------------------------------------------------------------------------------------------ the way down
    /** How many route steps ahead of a reader a shape is set down: far enough off that nothing of it is made out yet. */
    public static final int AHEAD_NEAR=19,AHEAD_FAR=27;
    /**
     * From how far off a shape begins to come out of the dark, and from how near it is as plain as it gets. The stair is too dark
     * for a figure to be seen in it at all, so a shape is drawn by this and not by the light that is there (client.StairShapes):
     * at its plainest a pale body with no light on it and nothing to be read in it.
     */
    public static final double SHOWN_FROM=20,SHOWN_FULL=13;
    /** How much of a shape shows at a distance, of its plainest: nothing beyond SHOWN_FROM, all of it within SHOWN_FULL, and less of it as it goes. */
    public static float shown(double distance,int fade){
        return (float)Mth.clamp((SHOWN_FROM-distance)/(SHOWN_FROM-SHOWN_FULL),0,1)*(1-Mth.clamp(fade,0,100)/100F);
    }
    /** How near a reader comes before a shape goes; how much of it goes in a tick, of a hundred; how far off a reader gives it up. */
    public static final double REACHED=6,GIVEN_UP=40;
    public static final int FADE=6;
    /** The most of one camp's people to stand on the stretch above it; how far from a camp, in route steps, none of them stand. */
    public static final int MOST=5,CLEAR=40;
    private static final String SEEN="StairShapes0596";
    private static final class Shape{final LiteraryActor actor;final Figure figure;int fade;Shape(LiteraryActor actor,Figure figure){this.actor=actor;this.figure=figure;}}
    private static final Map<UUID,Shape> SHAPES=new HashMap<>();
    /**
     * Where on the way down a reader's people stand for them, as {route step, index among their people}. Each stretch between camps
     * is for the people who will stand at the camp at its foot, a few of them, spread down it, each in the middle of a flight.
     */
    public static List<int[]> sightings(BlockPos origin,int count){
        var out=new ArrayList<int[]>();if(count<=0)return out;
        int[] corners=StaircaseFire.cornerSteps(origin),camps=StaircaseFire.campSteps(origin);var used=new HashSet<Integer>();
        for(int c=0;c<StaircaseFire.REQUIRED;c++){
            var theirs=new ArrayList<Integer>();for(int i=0;i<count;i++)if(camp(i,count)==c)theirs.add(i);
            int k=Math.min(MOST,theirs.size());if(k==0)continue;
            int from=c==0?corners[1]:camps[c-1]+CLEAR,to=camps[c]-CLEAR;
            for(int j=0;j<k;j++){
                int want=from+(j+1)*(to-from)/(k+1),flight=0;
                while(flight+2<corners.length&&corners[flight+1]<=want)flight++;
                int step=(corners[flight]+corners[flight+1])/2;
                if(used.add(step))out.add(new int[]{step,theirs.get(j*theirs.size()/k)});
            }
        }
        return out;
    }
    public static boolean shown(CompoundTag record,Figure f){return record.getCompound(SEEN).getBoolean(f.name());}
    /** A reader's shape on the stair now, if one stands there. */
    public static @Nullable LiteraryActor shape(ServerPlayer p){var s=SHAPES.get(p.getUUID());return s==null?null:body(s.actor);}
    /** Each tick of a reader's way down: one of their people stands ahead in the dark, and is gone when they reach it. */
    public static void down(ServerPlayer p,BlockPos origin,CompoundTag record){
        var level=p.serverLevel();var shape=SHAPES.get(p.getUUID());
        if(shape!=null){
            var a=body(shape.actor);if(a==null){SHAPES.remove(p.getUUID());return;}
            double d=p.getEyePosition().distanceTo(a.position().add(0,a.getBbHeight()*.6,0));
            if(shape.fade>0||d<REACHED){
                shape.fade+=FADE;a.fade(shape.fade);
                if(shape.fade>=100){
                    var seen=record.getCompound(SEEN);seen.putBoolean(shape.figure.name(),true);record.put(SEEN,seen);
                    FinaleProgress.save(p.server,p.getUUID(),record);a.discard();SHAPES.remove(p.getUUID());
                }
            }else if(d>GIVEN_UP){a.discard();SHAPES.remove(p.getUUID());}
            return;
        }
        if(p.tickCount%5!=0||StaircaseFire.off(origin,p.position()))return;
        var people=people(p);if(people.isEmpty())return;
        int r=stepAt(origin,p.position());
        for(var s:sightings(origin,people.size())){
            int ahead=s[0]-r;if(ahead<AHEAD_NEAR||ahead>AHEAD_FAR)continue;
            var f=people.get(s[1]);if(shown(record,f))continue;
            var at=at(level,origin,s[0],0);
            if(p.getEyePosition().distanceTo(at.add(0,1,0))<SHOWN_FROM)continue;
            var a=spawn(level,p,f,at,yaw(at,p.position()),true);if(a!=null)SHAPES.put(p.getUUID(),new Shape(a,f));
            return;
        }
    }

    // ------------------------------------------------------------------------------------------------ the way home
    /** How far below a camp a reader is when its people are stood there; how far above it, in route steps, when they come after. */
    public static final int NEAR_CAMP=100,JOIN=8;
    /** How far behind a reader the first of those following stand, at first and at the least; a look back brings them one nearer. */
    public static final int GAP=10,LEAST=3;
    /** Route steps between the rows of those following, and the most a figure makes up in a tick or is left behind by. */
    public static final int ROW=2,LEFT_BEHIND=40;
    public static final double SPEED=1.5,ACROSS=2.2;
    /** How near two of them may stand, and how many steps behind its own place one looks for room. */
    public static final double APART=1.3;
    public static final int TRIES=14;
    /** How many steps ahead of where it would stand one looks for room when every step behind is taken: it passes whoever is in its way. */
    public static final int PASS=12;
    private static boolean clear(List<Vec3> places,int self,Vec3 at){
        for(int i=0;i<places.size();i++){
            var t=places.get(i);if(i==self||t==null)continue;
            double dx=t.x-at.x,dz=t.z-at.z;if(dx*dx+dz*dz<APART*APART&&Math.abs(t.y-at.y)<3)return false;
        }
        return true;
    }
    private static final class Member{
        final Figure figure;final int camp,spot;@Nullable LiteraryActor actor;boolean following;int slot;double step;
        Member(Figure figure,int camp,int spot){this.figure=figure;this.camp=camp;this.spot=spot;}
    }
    private static final class Company{final List<Member> members=new ArrayList<>();int joined,looks;boolean watched;}
    private static final Map<UUID,Company> COMPANIES=new HashMap<>();
    /** How many of a reader's people are coming up after them, and how many times the reader has looked back at them. */
    public static int following(ServerPlayer p){var c=COMPANIES.get(p.getUUID());return c==null?0:c.joined;}
    public static int looks(ServerPlayer p){var c=COMPANIES.get(p.getUUID());return c==null?0:c.looks;}
    /** A reader's people on the stair now, for a check to look at: each standing body, in the order of the reader's stories. */
    public static List<LiteraryActor> bodies(ServerPlayer p){
        var out=new ArrayList<LiteraryActor>();var c=COMPANIES.get(p.getUUID());if(c==null)return out;
        for(var m:c.members){var a=body(m.actor);if(a!=null)out.add(a);}
        return out;
    }
    /**
     * Each tick of a reader's climb home. Their people stand round the fires they lit, their backs to the stair. Once the reader has
     * climbed past a camp its people come after them, three abreast, and only while the reader cannot see them or where they are
     * going: looked at, they are standing still, and each look back finds them a step nearer.
     */
    public static void home(ServerPlayer p,BlockPos origin){
        var level=p.serverLevel();var c=COMPANIES.get(p.getUUID());
        if(c==null){
            var people=people(p);if(people.isEmpty())return;
            c=new Company();int[] filled=new int[StaircaseFire.REQUIRED];
            for(int i=0;i<people.size();i++){int camp=camp(i,people.size());c.members.add(new Member(people.get(i),camp,filled[camp]++));}
            COMPANIES.put(p.getUUID(),c);
        }
        int r=stepAt(origin,p.position());var camps=StaircaseFire.landings(origin);int[] steps=StaircaseFire.campSteps(origin);
        int gap=Math.max(LEAST,GAP-c.looks),n=c.members.size();boolean seenNow=false;
        var bodies=new LiteraryActor[n];var places=new ArrayList<Vec3>(n);
        for(int i=0;i<n;i++){
            var m=c.members.get(i);var a=body(m.actor);
            if(!m.following){
                if(r<steps[m.camp]-JOIN){m.following=true;m.slot=c.joined++;m.step=steps[m.camp];}
                else if(a==null&&p.getY()>camps.get(m.camp).getY()-NEAR_CAMP){
                    var at=spot(level,origin,m.camp,m.spot);a=spawn(level,p,m.figure,at,yaw(at,hearth(origin,m.camp)));m.actor=a;
                }
            }
            bodies[i]=a;places.add(a==null?null:a.position());
        }
        for(int i=0;i<n;i++){
            var m=c.members.get(i);if(!m.following)continue;var a=bodies[i];
            if(a!=null&&sees(p,a.position(),a.getBbHeight())){seenNow=true;continue;}
            double want=r+gap+ROW*(m.slot/3),side=(m.slot%3-1)*ACROSS,step=m.step;
            double height=a!=null?a.getBbHeight():1.8*(m.figure.scale()>0?m.figure.scale():1);
            if(step>want)step=step-want>LEFT_BEHIND?want+LEFT_BEHIND:Math.max(want,step-SPEED);
            // Where it stands is as near as it comes for now: it never goes back down the stair.
            else if(a!=null)continue;
            // Nobody is standing for it, and it has not come away from its fire: it is stood at the fire, where the reader is not looking.
            else if(m.step>=steps[m.camp]){
                var at=spot(level,origin,m.camp,m.spot);
                if(clear(places,i,at)&&!sees(p,at,height)){a=spawn(level,p,m.figure,at,yaw(at,hearth(origin,m.camp)));m.actor=a;if(a!=null)places.set(i,at);}
                continue;
            }else step=want;
            // It never stands where another of them is standing, or where the reader is looking: it tries the step behind, and one
            // that is standing somewhere goes no further back than that.
            Vec3 to=null;double found=step;
            for(int k=0;k<TRIES&&(a==null||step+k<=m.step);k++){
                var at=at(level,origin,step+k,side);if(!clear(places,i,at)||sees(p,at,height))continue;
                to=at;found=step+k;break;
            }
            // Every step behind taken (the ones still at a fire stand in the walk), it passes them: the nearest free step on, and no
            // nearer the reader than its own place.
            for(int k=1;to==null&&k<=PASS&&step-k>=want;k++){
                var at=at(level,origin,step-k,side);if(!clear(places,i,at)||sees(p,at,height))continue;
                to=at;found=step-k;
            }
            if(to==null)continue;
            step=found;
            m.step=step;float yaw=yaw(to,p.position());
            if(a==null){a=spawn(level,p,m.figure,to,yaw);m.actor=a;}
            else{a.moveTo(to.x,to.y,to.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);}
            if(a!=null)places.set(i,to);
        }
        if(seenNow&&!c.watched)c.looks++;
        c.watched=seenNow;
    }
    /** The shorter climb has carried a reader up: those coming after them are carried as far, as the stair they stand on is. */
    public static void lifted(ServerPlayer p,BlockPos origin,int lift){
        var c=COMPANIES.get(p.getUUID());if(c==null)return;
        int[] corners=StaircaseFire.cornerSteps(origin);int steps=(corners[4]-corners[0])*lift/StaircaseAscent.TURN;
        for(var m:c.members)if(m.following){
            m.step=Math.max(0,m.step-steps);var a=body(m.actor);
            if(a!=null)a.moveTo(a.getX(),a.getY()+lift,a.getZ(),a.getYRot(),a.getXRot());
        }
    }
    /** On the way home the shorter climb carries nobody from a camp's landing or past one: every camp is walked through. */
    public static boolean holds(ServerPlayer p,BlockPos origin,int y,int lift){
        if(FinaleProgress.phase(p.server,p.getUUID())!=FinaleProgress.Phase.HOMEWARD)return false;
        for(var camp:StaircaseFire.landings(origin))if(camp.getY()>=y&&camp.getY()<=y+lift)return true;
        return false;
    }

    // ------------------------------------------------------------------------------------------------ nobody left standing
    private static boolean here(MinecraftServer server,@Nullable BlockPos origin,UUID id,boolean home){
        var p=server.getPlayerList().getPlayer(id);
        if(p==null||origin==null||!p.isAlive()||p.isSpectator()||!p.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                ||!FinaleArchitecture.contains(origin,p.blockPosition()))return false;
        var phase=FinaleProgress.phase(server,id);
        return home?phase==FinaleProgress.Phase.HOMEWARD:phase==FinaleProgress.Phase.STAIRCASE||phase==FinaleProgress.Phase.UNSEEN;
    }
    /** A reader who has left the stair, or whose walk has changed, leaves nobody standing on it. */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(e.getServer().getTickCount()%20==0)sweep(e.getServer());}
    public static void sweep(MinecraftServer server){
        if(SHAPES.isEmpty()&&COMPANIES.isEmpty())return;
        var origin=HouseSavedData.get(server).houseOrigin();
        for(var it=SHAPES.entrySet().iterator();it.hasNext();){
            var entry=it.next();if(here(server,origin,entry.getKey(),false))continue;
            var a=body(entry.getValue().actor);if(a!=null)a.discard();
            it.remove();
        }
        for(var it=COMPANIES.entrySet().iterator();it.hasNext();){
            var entry=it.next();if(here(server,origin,entry.getKey(),true))continue;
            for(var m:entry.getValue().members){var a=body(m.actor);if(a!=null)a.discard();}
            it.remove();
        }
    }
    /** Forgets everyone, and takes their bodies off a stair that is still loaded. */
    public static void clearAll(@Nullable ServerLevel level){
        if(level!=null){
            for(var s:SHAPES.values()){var a=body(s.actor);if(a!=null)a.discard();}
            for(var c:COMPANIES.values())for(var m:c.members){var a=body(m.actor);if(a!=null)a.discard();}
        }
        SHAPES.clear();COMPANIES.clear();
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll(null);}
}
