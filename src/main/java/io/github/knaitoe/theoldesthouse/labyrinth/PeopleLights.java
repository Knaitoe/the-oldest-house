package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.253, After the People Lights Have Gone Off (docs/PEOPLE_LIGHTS_0_4_253.md), the owner's design (docs/AFTER_THE_PEOPLE_LIGHTS_PROPOSAL.md)
 * after the title story of Stephen Graham Jones's collection. The reader is a houseguest on the evening Kelly is thinking about taking
 * her father's offer for the house. Supper, and Stan's visit; a change in the house Kelly asks for (the bathroom mirror, lowered to where
 * she can see herself), and others she or Mark agree to (the hall runner, Mark's work case brought down); her chair's sound on each
 * floor; footsteps over the study while Mark is at his desk, and a standing likeness of Kelly at the loft rail while she is downstairs;
 * then the night: Kelly asleep, pulling herself round the hall and the newel post without her chair, until she is woken; and the
 * conversation at the kitchen table where the couple decide together. One more night (the exchange behind the closed bedroom door, and
 * the morning), leaving together by the garage to Stan on the driveway, or sharing the downstairs. Any of the three resolves the story.
 * <p>
 * The evening belongs to the house (its base): readers who come in together spend it together; a reader on their own has their own
 * copy (SceneInstances). It starts again from the beginning when nobody has been in it for five seconds. Nothing in it can hurt.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PeopleLights {
    private PeopleLights(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PEOPLE_LIGHTS;
    /** The cast, on stranger phases: Kelly (standing, in her chair, on the floor, pulling herself along asleep, in bed), Mark, Stan, and the likeness. */
    public static final int KELLY=57,KELLY_CHAIR=58,KELLY_FLOOR=59,KELLY_DRAG=60,MARK=61,STAN=62,LIKENESS=63,KELLY_BED=64;
    public static boolean kelly(int phase){return phase==KELLY||phase==KELLY_CHAIR||phase==KELLY_FLOOR||phase==KELLY_DRAG||phase==LIKENESS||phase==KELLY_BED;}
    public static boolean inChair(int phase){return phase==KELLY_CHAIR;}
    public static boolean onFloor(int phase){return phase==KELLY_FLOOR;}
    public static boolean dragging(int phase){return phase==KELLY_DRAG;}
    public static boolean inBed(int phase){return phase==KELLY_BED;}
    public static boolean cast(int phase){return phase>=KELLY&&phase<=KELLY_BED;}
    /** The evening's stages. */
    public static final int ARRIVE=0,SUPPER=1,STAN_VISIT=2,HOUSEHOLD=3,BEDTIME=4,ROUND=5,AWAKE=6,ONE_MORE_NIGHT=7,MORNING=8,LEAVING=9,SHARING=10,OVER=11;
    public static final int EMPTY_RESET=100;
    /** Ticks: Stan comes this long into supper if nobody has asked anything; the night's quiet before the chair goes silent; Mark wakes her himself after this long. */
    public static final int STAN_AFTER=1200,NIGHT_QUIET=400,MARK_WAKES=1600;
    /** How many times her name must be said before she wakes (with Mark there for the last). */
    public static final int CALLS=3;
    static final String TAG="People253";
    static Vec3 v(double x,double y,double z){return new Vec3(x,y,z);}
    /** The stair, measured: tread i is stood on at height i+1, its centre at z -5.5-i; the landing and the study at height five. */
    static final List<Vec3> STAIR_UP=List.of(v(-3.5,0,-4.5),v(-3.5,1,-5.5),v(-3.5,2,-6.5),v(-3.5,3,-7.5),v(-3.5,4,-8.5),v(-3.5,5,-9.5),v(-3.5,5,-10.5),v(-3.5,5,-12.5));
    /** Mark between his desk (standing behind its chair) and the foyer at the foot of the stair. */
    static final List<Vec3> UP_TO_DESK,DOWN_TO_FOYER;
    static{var up=new ArrayList<Vec3>(List.of(v(.5,0,-14.5),v(.5,0,-5.5)));up.addAll(STAIR_UP);up.add(v(-3.5,5,-14.5));up.add(v(2.5,5,-14.5));UP_TO_DESK=List.copyOf(up);
        var down=new ArrayList<Vec3>(List.of(v(-3.5,5,-14.5)));var stair=new ArrayList<>(STAIR_UP);Collections.reverse(stair);down.addAll(stair);down.add(v(.5,0,-5.5));DOWN_TO_FOYER=List.copyOf(down);}
    static List<Vec3> then(List<Vec3> a,Vec3... more){var out=new ArrayList<>(a);out.addAll(List.of(more));return List.copyOf(out);}
    /** Out of the house by the side door, down the garage ramp (a top slab, then a bottom one) and out of the garage onto the drive. */
    static final List<Vec3> TO_GARAGE=List.of(v(6.5,0,-15.5),v(7.5,0,-15.5),v(8.5,0,-15.5),v(9.5,0,-15.5),v(10.5,-.5,-15.5),v(11.5,-1,-15.5));

    /** One house's evening. */
    static final class Evening {
        final BlockPos base;long tick,floorAt,likenessAt;int empty,stage=ARRIVE,since,step,lines,waypoint,calls,stuck,refusal;
        @Nullable UUID kelly,mark,stan,likeness;
        boolean overheard,greeted,stanCame,offerLeft,stanRedirected,mirrorDone,mirrorAsked,floorScene,cushion,helpOffered,backInChair,
            footsteps,likenessShown,toldKelly,nightFell,markDown,woken,markWoke,kellyWants,markAdmitted,uncertain,runnerAsked,caseAgreed,
            doorShut,nightPassed,spareBrought,codaDone,change,cardsDealt,departed,stanWaiting,likenessAtEnd,markComing,markUp;
        String plan="",runnerChoice="",changeChoice="";
        final List<Vec3> path=new ArrayList<>();int pathAt;@Nullable UUID walker;
        Evening(BlockPos b){base=b;}
    }
    private static final Map<Long,Evening> HOUSES=new HashMap<>();
    public static void clearAll(){HOUSES.clear();}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Evening evening(BlockPos b){return HOUSES.get(b.asLong());}
    public static int stage(BlockPos b){var e=evening(b);return e==null?-1:e.stage;}
    public static String plan(BlockPos b){var e=evening(b);return e==null?"":e.plan;}
    @Nullable public static LiteraryActor kellyActor(ServerLevel l,BlockPos b){var e=evening(b);return e==null?null:actor(l,e.kelly);}
    @Nullable public static LiteraryActor markActor(ServerLevel l,BlockPos b){var e=evening(b);return e==null?null:actor(l,e.mark);}
    @Nullable public static LiteraryActor stanActor(ServerLevel l,BlockPos b){var e=evening(b);return e==null?null:actor(l,e.stan);}
    @Nullable public static LiteraryActor likenessActor(ServerLevel l,BlockPos b){var e=evening(b);return e==null?null:actor(l,e.likeness);}
    public static boolean flag(BlockPos b,String name){var e=evening(b);if(e==null)return false;return switch(name){case "stan"->e.stanCame;case "offer"->e.offerLeft;case "mirror"->e.mirrorDone;case "likeness"->e.likenessShown;case "woken"->e.woken;case "markWoke"->e.markWoke;case "kellyWants"->e.kellyWants;case "markAdmitted"->e.markAdmitted;case "doorShut"->e.doorShut;case "departed"->e.departed;case "cards"->e.cardsDealt;case "footsteps"->e.footsteps;default->false;};}
    /** For tests: moves the evening on to a stage as if its conditions had been met. */
    public static void advance(ServerLevel l,BlockPos b,int to){var e=evening(b);if(e==null)return;while(e.stage<to)next(l,e,e.stage+1);}

    // ------------------------------------------------------------------------------------------------ speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** The owner's design keeps this story's speech in chat: who said it, then what they said. */
    static Component line(String who,String said){return Component.literal(who+": ").withStyle(ChatFormatting.GRAY).append(Component.literal(said).withStyle(ChatFormatting.WHITE));}
    static Component stage(String what){return Component.literal(what).withStyle(ChatFormatting.DARK_GRAY,ChatFormatting.ITALIC);}
    static void say(ServerLevel l,Evening e,String who,String said){for(var r:readers(l,e.base))r.sendSystemMessage(line(who,said));}
    static void note(ServerLevel l,Evening e,String what){for(var r:readers(l,e.base))r.sendSystemMessage(stage(what));}
    static void each(ServerLevel l,Evening e,java.util.function.Consumer<ServerPlayer> act){for(var r:readers(l,e.base))act.accept(r);}
    /** Marks a fact for every reader in the house who witnessed it, in their own record. */
    static void witnessed(ServerLevel l,Evening e,String key){for(var r:readers(l,e.base)){var own=own(r);if(own.getBoolean("Completed"))continue;own.putBoolean(key,true);save(r,own);}}

    // ------------------------------------------------------------------------------------------------ the cast
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    @Nullable static LiteraryActor spawn(ServerLevel l,BlockPos b,int phase,Vec3 at,float yaw){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,phase);a.moveTo(b.getX()+at.x,b.getY()+at.y,b.getZ()+at.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
        return l.addFreshEntity(a)?a:null;
    }
    static void clearActors(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-16,-3,-31)).minmax(new AABB(b.offset(18,16,1))),a->a.role()==LiteraryActor.STRANGER&&cast(a.phase())))a.discard();}
    static Vec3 at(BlockPos p){return Vec3.atBottomCenterOf(p);}
    static void place(LiteraryActor a,BlockPos b,Vec3 rel,float yaw){a.moveTo(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    static void seat(LiteraryActor a,BlockPos b,BlockPos at,Direction facing){place(a,b,at(at),facing.toYRot());}
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(!p.isSpectator()&&p.isAlive()&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE))&&PeopleLightsSite.inside(rel(p.position(),b)))out.add(p);
        return out;
    }
    @Nullable static ServerPlayer closest(ServerLevel l,BlockPos b,Vec3 from){ServerPlayer best=null;double d=Double.MAX_VALUE;for(var r:readers(l,b)){double dd=r.position().distanceToSqr(from);if(dd<d){d=dd;best=r;}}return best;}
    static boolean watched(ServerLevel l,BlockPos b,LiteraryActor a){for(var r:readers(l,b))if(r.hasLineOfSight(a))return true;return false;}
    /** One step of an actor along a list of positions (relative to the base); true when the last is reached. Unwatched, a stuck walker is set on at the next position. */
    static boolean follow(ServerLevel l,Evening e,LiteraryActor a,List<Vec3> route,double speed){
        if(!a.getUUID().equals(e.walker)||e.path!=route&&!e.path.equals(route)){e.walker=a.getUUID();e.path.clear();e.path.addAll(route);e.pathAt=0;e.stuck=0;}
        if(e.pathAt>=e.path.size())return true;
        var target=e.path.get(e.pathAt);var to=new Vec3(e.base.getX()+target.x,e.base.getY()+target.y,e.base.getZ()+target.z);
        var before=a.position();
        if(SceneFootpaths.walk(a,to,speed)){e.pathAt++;e.stuck=0;return e.pathAt>=e.path.size();}
        if(a.position().distanceToSqr(before)<1e-4){if(++e.stuck>80&&!watched(l,e.base,a)){a.moveTo(to.x,to.y,to.z,a.getYRot(),0);e.pathAt++;e.stuck=0;}}else e.stuck=0;
        return false;
    }
    static void stopWalking(Evening e){e.walker=null;e.path.clear();e.pathAt=0;}

    // ------------------------------------------------------------------------------------------------ the chair's sound on each floor
    @Nullable static SoundEvent wheels(PeopleLightsSite.Floor floor){
        return switch(floor){case TILE->PeopleLightsRegistry.WHEELS_TILE.get();case WOOD->PeopleLightsRegistry.WHEELS_WOOD.get();case KITCHEN->PeopleLightsRegistry.WHEELS_KITCHEN.get();case RUNNER->PeopleLightsRegistry.WHEELS_RUNNER.get();default->null;};
    }
    static void wheelSound(ServerLevel l,Evening e,LiteraryActor k,Vec3 before){
        if(k.position().distanceToSqr(before)<1e-4||e.tick%10!=0)return;
        var floor=PeopleLightsSite.floor(l,e.base,rel(k.position(),e.base));var sound=wheels(floor);
        if(sound!=null)l.playSound(null,k.getX(),k.getY(),k.getZ(),sound,SoundSource.NEUTRAL,floor==PeopleLightsSite.Floor.RUNNER?.25F:.8F,1F);
    }

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("Completed")){cue(p,PeopleLightsTexts.AFTER);return;}
        cue(p,"A porch light, and a narrow, tall house with every window warm. Someone inside is laughing at someone else.");
    }

    // ------------------------------------------------------------------------------------------------ the evening's stages
    static void next(ServerLevel l,Evening e,int to){
        e.stage=to;e.since=0;e.step=0;e.lines=0;stopWalking(e);
        var k=actor(l,e.kelly);var m=actor(l,e.mark);var b=e.base;
        switch(to){
            case SUPPER->{if(m!=null)m.appearance(LiteraryActor.STRANGER,MARK);}
            case HOUSEHOLD->{PeopleLightsSite.offer(l,b,true);e.offerLeft=true;}
            case BEDTIME->{}
            case ROUND->{PeopleLightsSite.lights(l,b,false);e.nightFell=true;
                if(k!=null){k.appearance(LiteraryActor.STRANGER,KELLY_DRAG);place(k,b,PeopleLightsSite.ROUND.get(0),180);}e.waypoint=1;e.calls=0;}
            case AWAKE->{e.refusal=0;
                if(k!=null){k.appearance(LiteraryActor.STRANGER,KELLY_CHAIR);seat(k,b,PeopleLightsSite.KELLY_PLACE,Direction.SOUTH);}
                if(m!=null)seat(m,b,PeopleLightsSite.MARK_SEAT,Direction.EAST);
                PeopleLightsSite.chair(l,b,PeopleLightsSite.BED_CHAIR,Direction.NORTH,false);PeopleLightsSite.lights(l,b,true);}
            default->{}
        }
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();var seen=new HashSet<Long>();
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||!p.isAlive()||LiteraryVignettes.current(p)!=PLACE)continue;
            var b=LiteraryVignettes.base(p,PLACE);if(b==null||!PeopleLightsSite.inside(rel(p.position(),b)))continue;
            var e=HOUSES.computeIfAbsent(b.asLong(),k->new Evening(b));
            if(seen.add(b.asLong()))step(p.serverLevel(),e);
            reader(p,b,own(p),e);
        }
        for(var it=HOUSES.values().iterator();it.hasNext();){var e=it.next();if(seen.contains(e.base.asLong())){e.empty=0;continue;}
            if(++e.empty>=EMPTY_RESET){var l=server.getLevel(NovelRooms.dimension(PLACE));if(l!=null&&l.isLoaded(e.base)){clearActors(l,e.base);PeopleLightsSite.reset(l,e.base);}it.remove();}}
        // Anything carried out of the house some other way goes back where it belongs.
        if(server.getTickCount()%20==0)for(var p:server.getPlayerList().getPlayers()){
            boolean in=LiteraryVignettes.current(p)==PLACE&&LiteraryVignettes.base(p,PLACE)!=null&&PeopleLightsSite.inside(rel(p.position(),LiteraryVignettes.base(p,PLACE)))&&p.isAlive();
            if(!in&&holding(p)!=0)release(p);
        }
    }

    static void step(ServerLevel l,Evening e){
        var b=e.base;
        if(e.tick==0){clearActors(l,b);PeopleLightsSite.reset(l,b);
            var k=spawn(l,b,KELLY_CHAIR,new Vec3(4.5,0,-9.5),90);if(k!=null)e.kelly=k.getUUID();
            var m=spawn(l,b,MARK,at(PeopleLightsSite.DESK_CHAIR),0);if(m!=null)e.mark=m.getUUID();}
        e.tick++;e.since++;
        var k=actor(l,e.kelly);var m=actor(l,e.mark);
        switch(e.stage){
            case ARRIVE->{if(k!=null&&!e.greeted){var r=closest(l,b,k.position());if(r!=null)SceneFootpaths.face(k,r.position(),10);}}
            case SUPPER->supper(l,e,k,m);
            case STAN_VISIT->stan(l,e,k,m);
            case HOUSEHOLD->household(l,e,k,m);
            case BEDTIME->bedtime(l,e,k,m);
            case ROUND->round(l,e,k,m);
            case AWAKE->{if(k!=null)seat(k,b,PeopleLightsSite.KELLY_PLACE,Direction.SOUTH);if(m!=null)seat(m,b,PeopleLightsSite.MARK_SEAT,Direction.EAST);}
            case ONE_MORE_NIGHT->oneMoreNight(l,e,k,m);
            case MORNING->morning(l,e,k,m);
            case LEAVING->leaving(l,e,k,m);
            case SHARING->sharing(l,e,k,m);
            default->{}
        }
        // Nothing of the household's is left lying about: something dropped goes back where it belongs.
        if(e.tick%10==0)for(var item:l.getEntitiesOfClass(ItemEntity.class,new AABB(b.offset(-16,-3,-31)).minmax(new AABB(b.offset(18,16,1))),i->carried(i.getItem())!=0)){
            if(carried(item.getItem())==1)PeopleLightsSite.caseUpstairs(l,b,!PeopleLightsSite.caseDownstairs(l,b));
            else PeopleLightsSite.chair(l,b,PeopleLightsSite.SPARE_CHAIR,Direction.SOUTH,true);
            item.discard();}
    }

    /** Supper: Kelly at her place, Mark comes down from the study to his; Stan comes once something has been asked, or after a while. */
    static void supper(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;
        if(k!=null){var before=k.position();if(follow(l,e,k,List.of(v(5.5,0,-11.5),v(5.5,0,-15.5),at(PeopleLightsSite.KELLY_PLACE)),.12))seat(k,b,PeopleLightsSite.KELLY_PLACE,Direction.SOUTH);wheelSound(l,e,k,before);}
        if(m!=null&&e.since>40){
            if(!e.markDown){if(moveSecond(l,e,m,then(DOWN_TO_FOYER,v(.5,0,-14.5),v(1.5,0,-14.5)),.15))e.markDown=true;}
            else seat(m,b,PeopleLightsSite.MARK_SEAT,Direction.EAST);
        }
        if(e.since==60)say(l,e,"Kelly","Sit down. Sit anywhere. He's coming. Mark!");
        if(e.since==140&&e.markDown)say(l,e,"Mark","Sorry. Somebody in Lisbon wanted me to say a number twice.");
        if(e.since>=STAN_AFTER||e.lines>0&&e.since>=300)next(l,e,STAN_VISIT);
    }
    /** A second walker (Mark while Kelly moves): his own cursor through the shared route helper is kept apart. */
    private static final Map<UUID,Integer> SECOND=new HashMap<>();
    static boolean moveSecond(ServerLevel l,Evening e,LiteraryActor a,List<Vec3> route,double speed){
        int i=SECOND.getOrDefault(a.getUUID(),0);if(i>=route.size())return true;var t=route.get(i);var to=new Vec3(e.base.getX()+t.x,e.base.getY()+t.y,e.base.getZ()+t.z);
        var before=a.position();
        if(SceneFootpaths.walk(a,to,speed)){SECOND.put(a.getUUID(),i+1);return i+1>=route.size();}
        if(a.position().distanceToSqr(before)<1e-4&&!watched(l,e.base,a)&&e.tick%60==0){a.moveTo(to.x,to.y,to.z,a.getYRot(),0);SECOND.put(a.getUUID(),i+1);}
        return false;
    }
    static void resetSecond(LiteraryActor a){if(a!=null)SECOND.remove(a.getUUID());}

    /** Stan's visit: in by the garage, the offer talked over Kelly's head until somebody turns him to her, the offer left on the table, and out. */
    static void stan(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;var s=actor(l,e.stan);
        if(k!=null)seat(k,b,PeopleLightsSite.KELLY_PLACE,Direction.SOUTH);if(m!=null)seat(m,b,PeopleLightsSite.MARK_SEAT,Direction.EAST);
        if(s==null&&!e.stanCame){s=spawn(l,b,STAN,v(11.5,PeopleLightsSite.YARD,-15.5),90);if(s!=null){e.stan=s.getUUID();e.stanCame=true;note(l,e,"The garage door grinds up on its long screw. Someone comes in through the side door without knocking.");}return;}
        if(s==null)return;
        if(e.step==0){if(follow(l,e,s,List.of(v(10.5,-.5,-15.5),v(9.5,0,-15.5),v(8.5,0,-15.5),v(7.5,0,-15.5),v(5.5,0,-15.5)),.14)){e.step=1;e.since=0;}return;}
        if(e.step==1){SceneFootpaths.face(s,m!=null?m.position():s.position(),20);
            int t=e.since;
            if(t==10)say(l,e,"Stan",PeopleLightsTexts.stanGreeting());
            if(t==60)say(l,e,"Stan","Mark. You've had a look at it? It's fair. It's more than fair. I'd have you both out of here and somewhere flat by the end of the month.");
            if(t==130)say(l,e,"Kelly","Dad. Where would the chair go in the truck?");
            if(t==190&&!e.stanRedirected)say(l,e,"Stan","Mark knows how I'd do it. We'd sort that out.");
            if(t==260){say(l,e,"Stan","Her mother used to get up in the night. Walk about. I'd find her in the—");}
            if(t==300){say(l,e,"Stan","Never mind. It's on the table. I'll answer the phone, whatever time it is.");PeopleLightsSite.offer(l,b,true);e.offerLeft=true;}
            if(t>=360){e.step=2;}
            return;}
        if(follow(l,e,s,then(TO_GARAGE,v(11.5,-1,-12.5),at(PeopleLightsSite.GARAGE_OUT),at(PeopleLightsSite.APRON),v(11.5,-1,-1.5)),.16)){
            s.discard();e.stan=null;note(l,e,"Tyres on gravel, and then nothing.");next(l,e,HOUSEHOLD);}
    }

    /** The ordinary house: the mirror, Kelly's pass through the rooms, the floor by the sofa, the sounds overhead and the likeness at the rail. */
    static void household(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;
        if(e.since==20&&!e.mirrorAsked){e.mirrorAsked=true;say(l,e,"Kelly","The bathroom mirror needs lowering. Mark's been planning it for three weeks.");}
        if(e.since==70)say(l,e,"Mark","I was going to do it after work.");
        if(e.since==110)say(l,e,"Kelly","Good. It's after work.");
        if(e.since==160){say(l,e,"Mark","I've got a call. Twenty minutes. Then the mirror's mine.");resetSecond(m);}
        // Nobody has lowered it: Mark keeps his word, after his call.
        if(e.since==2400&&!PeopleLightsSite.mirrorLowered(l,b)){PeopleLightsSite.mirror(l,b,true);note(l,e,"Mark comes down the stairs with a screwdriver, goes into the bathroom, and comes out again without saying anything.");}
        // Mark goes up to his desk and stays there, visibly, while the house does what it does.
        if(m!=null&&e.since>160){if(e.markUp||moveSecond(l,e,m,UP_TO_DESK,.15)){e.markUp=true;seat(m,b,PeopleLightsSite.DESK_CHAIR,Direction.SOUTH);}}
        if(k!=null){
            if(!e.mirrorDone){
                // Kelly's pass: the kitchen's tile, the foyer's large tiles, the living room's boards, the runner, and to the bathroom.
                if(e.since>180){var before=k.position();follow(l,e,k,List.of(v(1.5,0,-16.5),v(.5,0,-11.5),v(.5,0,-8.5),v(4.5,0,-8.5),v(.5,0,-10.5),v(-.5,0,-12.5),v(-.5,0,-17.5),v(3.5,0,-17.5),at(PeopleLightsSite.BATH_DOOR),v(3.5,0,-20.5),v(3.5,0,-22.5)),.11);wheelSound(l,e,k,before);}
                else seat(k,b,PeopleLightsSite.KELLY_PLACE,Direction.SOUTH);
            }else if(!e.floorScene){
                var before=k.position();
                if(follow(l,e,k,List.of(at(PeopleLightsSite.BATH_DOOR),v(3.5,0,-17.5),v(5.5,0,-16.5),v(5.5,0,-11.5),at(PeopleLightsSite.FLOOR_SPOT)),.11)){
                    e.floorScene=true;e.floorAt=e.tick;k.appearance(LiteraryActor.STRANGER,KELLY_FLOOR);place(k,b,at(PeopleLightsSite.FLOOR_SPOT),90);PeopleLightsSite.chair(l,b,PeopleLightsSite.CHAIR_BY_SOFA,Direction.WEST,true);
                    say(l,e,"Kelly","Could you pass the cushion?");}
                wheelSound(l,e,k,before);
            }else if(!e.backInChair){place(k,b,at(PeopleLightsSite.FLOOR_SPOT),90);
                // Nobody has offered: in a while she gets back into the chair herself.
                if(e.tick-e.floorAt>900){e.backInChair=true;k.appearance(LiteraryActor.STRANGER,KELLY_CHAIR);PeopleLightsSite.chair(l,b,PeopleLightsSite.CHAIR_BY_SOFA,Direction.WEST,false);place(k,b,new Vec3(4.5,0,-9.5),90);note(l,e,"She pulls the chair close, sets the brake, and lifts herself into it: one smooth movement she's done a thousand times.");}}
            if(!e.mirrorDone&&PeopleLightsSite.mirrorLowered(l,b)&&k.distanceToSqr(Vec3.atCenterOf(b.offset(PeopleLightsSite.MIRROR)))<9)mirrorChecked(l,e);
        }
        // Footsteps over the study while Mark is at his desk and Kelly is downstairs.
        if(!e.footsteps&&e.since>400&&m!=null&&rel(m.position(),b).y>=PeopleLightsSite.STUDY-1&&k!=null&&rel(k.position(),b).y<1)for(var r:readers(l,b))if(PeopleLightsSite.groundFloor(rel(r.position(),b))){
            e.footsteps=true;e.step=0;witnessed(l,e,"HeardOverhead253");break;}
        if(e.footsteps&&e.step<4&&e.tick%12==0){var at=b.offset(-2+e.step,PeopleLightsSite.LOFT,-18+e.step);l.playSound(null,at,SoundEvents.WOOD_STEP,SoundSource.AMBIENT,1F,.8F);e.step++;if(e.step==4)each(l,e,r->cue(r,"Footsteps overhead, above the study. Mark is at his desk."));}
        // The likeness: at the loft rail while Kelly is downstairs and someone below looks up.
        likeness(l,e,k);
        // Bedtime once the mirror is done, she is back in her chair, and half a minute has passed since the likeness was seen (time to tell
        // her about it); or after long enough anyway.
        if(e.mirrorDone&&e.floorScene&&e.backInChair&&(e.likenessShown&&e.tick-e.likenessAt>600||e.since>3600))next(l,e,BEDTIME);
    }
    static void likeness(ServerLevel l,Evening e,@Nullable LiteraryActor k){
        var b=e.base;var x=actor(l,e.likeness);
        if(x==null&&!e.likenessShown&&e.footsteps&&k!=null&&rel(k.position(),b).y<1){
            var rail=at(PeopleLightsSite.LIKENESS_AT).add(b.getX(),b.getY()+1.6,b.getZ());
            for(var r:readers(l,b)){if(!PeopleLightsSite.underWell(rel(r.position(),b)))continue;
                var look=r.getViewVector(1);var to=rail.subtract(r.getEyePosition()).normalize();
                if(look.dot(to)>.93){x=spawn(l,b,LIKENESS,at(PeopleLightsSite.LIKENESS_AT),0);if(x!=null){e.likeness=x.getUUID();e.likenessShown=true;e.likenessAt=e.tick;e.step=0;witnessed(l,e,"SawLikeness253");}break;}}
            return;}
        if(x==null)return;
        e.step++;
        if(e.step<90){var r=closest(l,b,x.position());if(r!=null)x.setYHeadRot((float)Math.toDegrees(Math.atan2(-(r.getX()-x.getX()),r.getZ()-x.getZ())));return;}
        // It withdraws behind the loft's boxes, then is not there.
        if(SceneFootpaths.walk(x,at(PeopleLightsSite.LIKENESS_AT).add(b.getX(),b.getY(),b.getZ()-4),.08)||e.step>200){x.discard();e.likeness=null;}
    }

    /** Bedtime: Kelly goes to bed, Mark stays up; after a while the chair's sound stops and something else starts. */
    static void bedtime(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;
        if(e.since==1){say(l,e,"Kelly","I'm going to bed. Mark. Don't stay up all night listening.");say(l,e,"Mark","I won't.");if(k!=null)k.appearance(LiteraryActor.STRANGER,KELLY_CHAIR);PeopleLightsSite.chair(l,b,PeopleLightsSite.CHAIR_BY_SOFA,Direction.WEST,false);resetSecond(m);}
        if(k!=null&&k.phase()==KELLY_CHAIR){var before=k.position();
            if(follow(l,e,k,List.of(v(.5,0,-10.5),v(-.5,0,-12.5),v(-.5,0,-19.5),v(-.5,0,-21.5),v(-2.5,0,-21.5)),.11)){
                k.appearance(LiteraryActor.STRANGER,KELLY_BED);place(k,b,at(PeopleLightsSite.BED),0);PeopleLightsSite.chair(l,b,PeopleLightsSite.BED_CHAIR,Direction.NORTH,true);
                note(l,e,"The bedroom light goes off. Upstairs, Mark's keys go on clacking.");e.since=2;}
            wheelSound(l,e,k,before);}
        if(m!=null){if(e.markUp||moveSecond(l,e,m,UP_TO_DESK,.15)){e.markUp=true;seat(m,b,PeopleLightsSite.DESK_CHAIR,Direction.SOUTH);}}
        if(k!=null&&k.phase()==KELLY_BED&&e.since>NIGHT_QUIET){PeopleLightsSite.lights(l,b,false);note(l,e,"The house is dark. The chair isn't making any sound at all. Something else is: a steady pulling, with breaths between.");next(l,e,ROUND);}
    }

    /** The night: Kelly, asleep, pulling herself round without the chair, until she is woken. */
    static void round(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;if(k==null)return;
        if(e.tick%40==0)l.playSound(null,k.getX(),k.getY(),k.getZ(),PeopleLightsRegistry.PULLING.get(),SoundSource.NEUTRAL,.7F,1F);
        if(e.tick%20==0)for(var r:readers(l,b))if(r.distanceToSqr(k)<64&&r.hasLineOfSight(k)){var own=own(r);if(!own.getBoolean("SawRound253")){own.putBoolean("SawRound253",true);save(r,own);}}
        // Someone in her way: she stops at them, and waits.
        boolean blocked=false;for(var r:readers(l,b))if(r.distanceToSqr(k)<1.2)blocked=true;
        if(!blocked){var w=PeopleLightsSite.ROUND.get(e.waypoint%PeopleLightsSite.ROUND.size());var to=new Vec3(b.getX()+w.x,b.getY()+w.y,b.getZ()+w.z);
            if(SceneFootpaths.walk(k,to,.035))e.waypoint++;}
        // Mark comes down once she has been called, and stands in the hall.
        if(m!=null&&e.markComing)moveSecond(l,e,m,then(DOWN_TO_FOYER,v(.5,0,-9.5)),.17);
        if(e.since>=MARK_WAKES&&!e.woken){e.markWoke=true;wake(l,e,k,m,true);}
    }
    static void wake(ServerLevel l,Evening e,LiteraryActor k,@Nullable LiteraryActor m,boolean byMark){
        e.woken=true;
        if(byMark){say(l,e,"Mark","Kel. Kelly. It's me. You're on the floor. You're all right.");}
        note(l,e,"She stops. She looks at her hands as if they were somebody else's, and then at the floor, and then up.");
        say(l,e,"Kelly","Where's my chair?");
        say(l,e,"Mark","Here. I've got it. I've got you.");
        witnessed(l,e,"SawWake253");
        next(l,e,AWAKE);
        say(l,e,"Kelly","Kitchen. All of us. I'm not doing this lying on the floor.");
    }

    /** One more night: they share the bedroom, the door shut on the danger they think is outside; the night passes when the reader lets it. */
    static void oneMoreNight(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;
        if(e.since==1){PeopleLightsSite.lights(l,b,false);PeopleLightsSite.lamp(l,b,true);
            if(k!=null){k.appearance(LiteraryActor.STRANGER,KELLY_BED);place(k,b,at(PeopleLightsSite.BED),0);}
            if(m!=null)place(m,b,v(-2.5,0,-21.5),90);PeopleLightsSite.chair(l,b,PeopleLightsSite.BED_CHAIR,Direction.NORTH,true);}
        if(e.since==20&&!e.doorShut){shutBedroom(l,e,true);note(l,e,"Mark checks the bedroom door twice, as if whatever he's afraid of were out here.");}
        if(m!=null&&e.since>1)place(m,b,v(-2.5,0,-21.5),90);
        if(e.since>60&&e.since%160==0&&!e.nightPassed){var door=b.offset(PeopleLightsSite.BEDROOM_DOOR);l.playSound(null,door,SoundEvents.WOOL_STEP,SoundSource.AMBIENT,.4F,.6F);}
    }
    static void shutBedroom(ServerLevel l,Evening e,boolean shut){var at=e.base.offset(PeopleLightsSite.BEDROOM_DOOR);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock d){d.setOpen(null,l,s,at,!shut);}e.doorShut=shut;}
    /** The morning: Mark beside the bed with his legs gone numb, Kelly in her own chair moving her foot a little; the spare chair if he needs it. */
    static void morning(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;
        if(e.since==1){shutBedroom(l,e,false);PeopleLightsSite.lights(l,b,true);
            if(k!=null){k.appearance(LiteraryActor.STRANGER,KELLY_CHAIR);place(k,b,v(-1.5,0,-22.5),90);}PeopleLightsSite.chair(l,b,PeopleLightsSite.BED_CHAIR,Direction.NORTH,false);
            if(m!=null){m.appearance(LiteraryActor.STRANGER,MARK);place(m,b,new Vec3(-4.5,0,-21.5),-90);}
            note(l,e,"Morning. Grey light in the bedroom. Mark is beside the bed, one hand flat on its frame, not moving his feet.");}
        if(m!=null&&e.spareBrought)seat(m,b,PeopleLightsSite.BED_CHAIR,Direction.NORTH);else if(m!=null&&e.since>1)place(m,b,new Vec3(-4.5,0,-21.5),-90);
        if(e.since==40)say(l,e,"Mark","My legs are asleep. Both of them. I can't— they're just not there.");
        if(e.since==90)note(l,e,"Kelly's left foot moves under the blanket over her knees. A little. Then again.");
        if(e.since==130&&!e.spareBrought)say(l,e,"Kelly","There's the other chair. Upstairs, at the top. If somebody would.");
        boolean ready=e.spareBrought&&e.since>140||e.since>1400;
        if(ready&&!e.codaDone){e.codaDone=true;e.step=e.since;}
        if(e.codaDone){int t=e.since-e.step;
            if(t==20)say(l,e,"Mark","Your foot.");if(t==60)say(l,e,"Kelly","I know.");if(t==100)say(l,e,"Mark","I couldn't feel mine.");if(t==150)say(l,e,"Kelly","Give me your hand.");
            if(t==200){witnessed(l,e,"SawMorning253");readyAll(l,e,"one_more_night");next(l,e,OVER);}}
    }
    /** Leaving: Kelly calls her father, each names one thing to take, and both go out by the garage to Stan on the driveway. */
    static void leaving(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;int t=e.since;
        if(t==1){if(k!=null)k.appearance(LiteraryActor.STRANGER,KELLY_CHAIR);say(l,e,"Kelly","Let me ring him. It should be me.");}
        if(t==20){l.playSound(null,b.offset(PeopleLightsSite.PHONE_AT),SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,.4F,1.6F);say(l,e,"Kelly","Dad. It's me. No— nobody's hurt. We want to come tonight. Can you come and get us? ... Yes. The garage.");}
        if(t==90)say(l,e,"Kelly","The photograph from the foyer. That's mine to take.");
        if(t==130)say(l,e,"Mark","The case. That's all I need.");
        if(t==170)PeopleLightsSite.caseCarriedOut(l,b);
        if(t>200){
            if(!e.stanWaiting){var s=spawn(l,b,STAN,at(PeopleLightsSite.APRON),0);if(s!=null){e.stan=s.getUUID();e.stanWaiting=true;note(l,e,"Headlights come up the drive and stop.");}}
            boolean kOut=false,mOut=false;
            if(k!=null){var before=k.position();kOut=follow(l,e,k,then(TO_GARAGE,v(11.5,-1,-10.5),at(PeopleLightsSite.GARAGE_OUT),v(11.5,-1,-5.5)),.11);wheelSound(l,e,k,before);}
            if(m!=null&&t>260){moveSecond(l,e,m,then(List.of(v(2.5,0,-15.5)),then(TO_GARAGE,v(12.5,-1,-15.5),v(12.5,-1,-10.5),v(12.5,-1,-5.5)).toArray(Vec3[]::new)),.12);mOut=PeopleLightsSite.onApron(rel(m.position(),b));}
            if(kOut&&!e.likenessAtEnd){e.likenessAtEnd=true;var x=spawn(l,b,LIKENESS,at(PeopleLightsSite.LIKENESS_AT),0);if(x!=null)e.likeness=x.getUUID();}
            if(kOut&&mOut&&!e.departed){e.departed=true;e.step=t;
                say(l,e,"Kelly","I wanted us to get to live here.");}
            if(e.departed){int u=t-e.step;
                if(u==50)say(l,e,"Mark","We did.");if(u==100)say(l,e,"Kelly","I know. That isn't what I mean.");if(u==150)say(l,e,"Mark","Tell me on the way.");
                if(u==200){witnessed(l,e,"SawLeave253");readyAll(l,e,"left_together");next(l,e,OVER);}}
        }
    }
    /** Sharing the downstairs: Kelly chooses the change, Mark brings his work down, and they finish a hand of cards; above, a movement at the rail. */
    static void sharing(ServerLevel l,Evening e,LiteraryActor k,LiteraryActor m){
        var b=e.base;int t=e.since;
        if(k!=null)seat(k,b,PeopleLightsSite.KELLY_PLACE,Direction.SOUTH);if(m!=null)seat(m,b,PeopleLightsSite.MARK_SEAT,Direction.EAST);
        if(t==1)say(l,e,"Kelly",e.changeChoice.isEmpty()?"Then something has to change tonight. Not everything. One thing.":"That one. Tonight.");
        if(t==60&&!PeopleLightsSite.caseDownstairs(l,b)){say(l,e,"Mark","I'll bring the case down.");}
        if(t==160&&!PeopleLightsSite.caseDownstairs(l,b))PeopleLightsSite.caseUpstairs(l,b,false);
        if(t==200)say(l,e,"Kelly","Put it where you can work.");
        if(t==240)say(l,e,"Mark","Here?");
        if(t==290)say(l,e,"Kelly","Where you can work. You don't have to sit facing me.");
        if(t==330){note(l,e,"He turns the case toward the table. She goes on with what she was doing: she deals.");PeopleLightsSite.cards(l,b,true);e.cardsDealt=true;}
        if(t==380){var x=spawn(l,b,LIKENESS,at(PeopleLightsSite.LIKENESS_AT),0);if(x!=null){e.likeness=x.getUUID();for(var r:readers(l,b))if(PeopleLightsSite.underWell(rel(r.position(),b))||r.hasLineOfSight(x))cue(r,"Above you, at the loft rail, something moves. Quietly.");}}
        if(t==480){var x=actor(l,e.likeness);if(x!=null)x.discard();e.likeness=null;witnessed(l,e,"SawShare253");readyAll(l,e,"shared_the_downstairs");next(l,e,OVER);}
    }
    /** The plan agreed resolves the story for everyone in the house who is not yet finished; each completes on the spot. */
    static void readyAll(ServerLevel l,Evening e,String outcome){
        for(var r:readers(l,e.base)){var own=own(r);if(own.getBoolean("Completed"))continue;own.putBoolean("Ready",true);own.putString("Outcome",outcome);own.putInt("Beat",own.getInt("Visit"));complete(r,own);save(r,own);}
    }

    // ------------------------------------------------------------------------------------------------ each reader
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,Evening e){
        var r=rel(p.position(),b);var l=p.serverLevel();
        if(own.getBoolean("Completed"))return;
        // Outside on the walk: the evening's first exchange, through the window.
        if(e.stage==ARRIVE&&!e.overheard&&r.z<-.5&&r.z>-3.5){e.overheard=true;e.step=0;}
        if(e.stage==ARRIVE&&e.overheard&&!e.greeted){e.step++;
            if(e.step==10)p.sendSystemMessage(line("Kelly (inside)","You said the weekend. Which weekend, is what I'm asking."));
            if(e.step==60)p.sendSystemMessage(line("Mark (upstairs)","The one with the mirror in it."));
            if(e.step==110)p.sendSystemMessage(line("Kelly (inside)","Ha. Funny man. Someone's at the door."));}
        if(e.stage==ARRIVE&&!e.greeted&&PeopleLightsSite.inHouse(r)){e.greeted=true;
            say(l,e,"Kelly","You found it. Come in. Mind the step: it's the only one I don't have to worry about.");
            var home=HouseRead.category(p,HouseRead.Trait.HOME).filter(c->c.strength()==HouseRead.Strength.SETTLED).map(HouseRead.Category::value).orElse("");
            if(home.equals("RUNAWAY"))say(l,e,"Kelly","Keep your things with you if you like. The garage is open, by the way. It always is. That's how we get in and out.");
            next(l,e,SUPPER);}
    }

    // ------------------------------------------------------------------------------------------------ right-clicks
    /**
     * The caller saves its copy of the reader's record after a click, so whatever this saves through a fresh read (carrying, putting
     * down, what everyone witnessed) is committed first and handed back in that copy.
     */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        save(p,own);boolean handled=clicked(p,b,rel,own(p));
        var fresh=own(p);for(var key:List.copyOf(own.getAllKeys()))own.remove(key);own.merge(fresh);
        return handled;
    }
    static boolean clicked(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var e=evening(b);if(e==null||own.getBoolean("Completed"))return false;
        var s=l.getBlockState(b.offset(rel));var kind=PeopleLightsBlock.kind(s);
        int held=holding(p);
        if(held!=0)return putDown(p,b,rel,own,e,held);
        if(kind!=null)switch(kind){
            case MIRROR->{
                if(PeopleLightsSite.mirrorLowered(l,b)){cue(p,e.mirrorDone?"Low enough to see into from a chair.":"Lowered. She'll want to see it herself.");return true;}
                if(e.stage!=HOUSEHOLD||!e.mirrorAsked){cue(p,"The mirror's hung high over the sink. Someone standing would see their face in it.");return true;}
                PeopleLightsSite.mirror(l,b,true);l.playSound(null,b.offset(rel),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.8F,1.2F);own.putBoolean("Mirror253",true);save(p,own);
                cue(p,"You take it off its screws and hang it again, a foot and a half lower.");
                if(e.stage==HOUSEHOLD){var k=actor(l,e.kelly);if(k!=null&&k.distanceToSqr(Vec3.atCenterOf(b.offset(rel)))<25){mirrorChecked(l,e);}}
                return true;}
            case CASE->{
                if(rel.equals(PeopleLightsSite.CASE_DOWN)){cue(p,"Mark's case, at the end of the table, where he can work.");return true;}
                if(!e.caseAgreed){cue(p,"Mark's work case. It's his: ask him before you move it.");return true;}
                l.setBlock(b.offset(PeopleLightsSite.CASE_AT),Blocks.AIR.defaultBlockState(),3);take(p,1,"Mark's work case");cue(p,"You pick up the case. It's lighter than it looks. Down to the kitchen table.");
                return true;}
            case CHAIR->{
                if(rel.equals(PeopleLightsSite.SPARE_CHAIR)){
                    if(e.stage==MORNING&&!e.spareBrought){l.setBlock(b.offset(rel),Blocks.AIR.defaultBlockState(),3);take(p,2,"The spare chair");cue(p,"You fold the spare chair and carry it down, careful on the turn of the stair.");return true;}
                    cue(p,"The spare chair, for upstairs. Kelly's own is downstairs with her.");return true;}
                cue(p,"Kelly's chair, parked where she can reach it.");return true;}
            case RUNNER->{runnerMenu(p,b,own,e);return true;}
            case OFFER->{cue(p,"Stan's offer, folded once. You can read it.");own.putBoolean("ReadOffer253",true);save(p,own);var book=PeopleLightsTexts.source();if(!p.getInventory().add(book))p.drop(book,false);return true;}
            case PHOTO->{cue(p,"The two of them on a step somewhere, before. She's laughing at whoever's holding the camera.");return true;}
            case NEWEL->{own.putBoolean("Newel253",true);save(p,own);cue(p,"The newel post: the banister swirling up into a knob of wood the size of a cantaloupe. The floor around it is tile.");return true;}
            case RAILING->{cue(p,"Stan's rail. It doesn't give at all.");return true;}
            case MATTRESS->{own.putBoolean("Mattress253",true);save(p,own);cue(p,"An air mattress, folded flat, the kind kept for guests. Nobody has slept up here for a long time.");return true;}
            case PHONE->{cue(p,e.stage==AWAKE?"The phone. If anyone calls Stan, it should be because they've both decided to.":"The kitchen phone, on the wall at a height someone sitting can reach.");return true;}
            case CARDS->{cue(p,"A hand of cards, dealt.");return true;}
            default->{}
        }
        // The sofa: the cushion Kelly asked for, and in the night, letting it pass.
        if(PeopleLightsSite.SOFA.equals(rel)||PeopleLightsSite.SOFA_B.equals(rel)){
            if(e.stage==HOUSEHOLD&&e.floorScene&&!e.cushion){e.cushion=true;own.putBoolean("Cushion253",true);save(p,own);cue(p,"You pass her the cushion.");say(l,e,"Kelly","Thanks. This is comfortable. I'm not stuck: I'm comfortable.");return true;}
            if(e.stage==ONE_MORE_NIGHT&&!e.nightPassed){nightMenu(p,b,own,e);return true;}
            return false;}
        // The kitchen table: the case set down where he can work.
        if(rel.getX()==3&&rel.getY()==0&&rel.getZ()<=-13&&rel.getZ()>=-15&&e.stage==AWAKE){tableMenu(p,b,own,e);return true;}
        // The bedroom door, shut on them in the night: calling through it wakes her.
        if((rel.equals(PeopleLightsSite.BEDROOM_DOOR)||rel.equals(PeopleLightsSite.BEDROOM_DOOR.above()))&&e.stage==ONE_MORE_NIGHT&&e.doorShut){
            own.putBoolean("CalledThroughDoor253",true);save(p,own);
            say(l,e,"Kelly","...What? Who's— I'm awake. I'm awake.");note(l,e,"Inside, Mark says something about his feet being cold. Kelly says hers aren't.");
            witnessed(l,e,"Interrupted253");shutBedroom(l,e,false);next(l,e,AWAKE);
            say(l,e,"Mark","All right. All right, kitchen. We'll talk again.");return true;}
        // The lamp: on, or off, in the night.
        if(rel.equals(PeopleLightsSite.LAMP)&&(e.stage==ROUND||e.stage==ONE_MORE_NIGHT)){boolean on=!PeopleLightsSite.lampOn(l,b);PeopleLightsSite.lamp(l,b,on);if(on&&e.stage==ROUND){own.putBoolean("Lamp253",true);save(p,own);cue(p,"The lamp. The route across the floor is easier to see now. Nothing else changes.");}return true;}
        return false;
    }
    /** A right-click on one of the household (the actor). */
    public static boolean talk(ServerPlayer p,LiteraryActor a){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return false;var e=evening(b);if(e==null)return false;var own=own(p);if(own.getBoolean("Completed"))return false;
        if(a.getUUID().equals(e.kelly))kellyMenu(p,b,own,e,a);
        else if(a.getUUID().equals(e.mark))markMenu(p,b,own,e,a);
        else if(a.getUUID().equals(e.stan))stanMenu(p,b,own,e);
        else if(a.getUUID().equals(e.likeness))cue(p,"Nobody is there.");
        else return false;
        return true;
    }

    // ------------------------------------------------------------------------------------------------ what can be said
    static ItemStack icon(net.minecraft.world.item.Item item,String label){return LiteraryChoiceMenu.icon(item,label);}
    static void kellyMenu(ServerPlayer p,BlockPos b,CompoundTag own,Evening e,LiteraryActor k){
        var l=p.serverLevel();var icons=new LinkedHashMap<Integer,ItemStack>();
        if(e.stage==ROUND){icons.put(2,icon(Items.BELL,"Say her name"));icons.put(4,icon(Items.FEATHER,"Step out of her way"));icons.put(6,icon(Items.LANTERN,"Stay close, and watch"));}
        else if(e.stage==AWAKE){tableMenu(p,b,own,e);return;}
        else if(e.stage==SUPPER||e.stage==STAN_VISIT||e.stage==HOUSEHOLD){
            if(!own.getBoolean("AskOffer253"))icons.put(0,icon(Items.PAPER,"Ask about Stan's offer"));
            if(!own.getBoolean("AskLove253"))icons.put(1,icon(Items.POPPY,"Ask what she still loves about the house"));
            if(!own.getBoolean("AskLoft253"))icons.put(2,icon(Items.WHITE_BED,"Ask why they sleep downstairs"));
            if(e.floorScene&&!e.backInChair)icons.put(4,icon(Items.LEATHER,e.helpOffered?"Offer again to help her back":"Offer a hand getting back"));
            if(e.likenessShown&&own.getBoolean("SawLikeness253")&&!own.getBoolean("Told253"))icons.put(6,icon(Items.ENDER_EYE,"Tell her what you saw at the loft rail"));
            if(e.stage==HOUSEHOLD&&PeopleLightsSite.mirrorLowered(l,b)&&!e.mirrorDone)icons.put(8,icon(Items.GLASS_PANE,"Ask her to look at the mirror"));
        }
        if(icons.isEmpty()){cue(p,e.stage==BEDTIME?"She's going to bed.":"She smiles at you, and goes on with what she was doing.");return;}
        LiteraryChoiceMenu.open(p,"Kelly",icons,()->LiteraryVignettes.current(p)==PLACE&&!own(p).getBoolean("Completed"),slot->{
            var o=own(p);boolean ok=true;
            switch(slot){
                case 0->{if(e.stage==ROUND)return false;o.putBoolean("AskOffer253",true);e.lines++;say(l,e,"Kelly","Dad wants to buy it from us. Fair price. Help moving. Somewhere flat.");say(l,e,"Kelly","Taking it feels like saying it beat us. Not taking it feels like— I don't know. Like staying in a room after the party.");}
                case 1->{if(e.stage==ROUND)return false;o.putBoolean("AskLove253",true);e.lines++;say(l,e,"Kelly","The light, first thing. It comes through the glass by the front door and lands on the tile in colours. I wheel through it on purpose.");
                    var home=HouseRead.category(p,HouseRead.Trait.HOME).filter(c->c.strength()==HouseRead.Strength.SETTLED).map(HouseRead.Category::value).orElse("");
                    if(home.equals("HAVEN"))say(l,e,"Kelly","Have you ever had to leave somewhere you loved? Does it feel like losing part of your life, or just a house?");}
                case 2->{if(e.stage==ROUND){call(p,b,o,e);break;}o.putBoolean("AskLoft253",true);o.putBoolean("AccidentTold253",true);e.lines++;
                    say(l,e,"Kelly","We slept up there the first night, in the loft. Before Dad had put a rail on it. On an air mattress, if you can believe that.");
                    say(l,e,"Kelly","I rolled off in my sleep. I remember floating. I thought it was a dream. It's the best part of it, honestly. Until the floor.");}
                case 4->{if(e.stage==ROUND){cue(p,"You step out of the way. She goes on past, without looking.");break;}
                    if(!e.helpOffered){e.helpOffered=true;say(l,e,"Kelly","In a minute. This is comfortable.");}
                    else{e.backInChair=true;o.putBoolean("Helped253",true);say(l,e,"Kelly","All right. Hold the chair still. On three.");k.appearance(LiteraryActor.STRANGER,KELLY_CHAIR);PeopleLightsSite.chair(l,b,PeopleLightsSite.CHAIR_BY_SOFA,Direction.WEST,false);place(k,b,new Vec3(4.5,0,-9.5),90);
                        say(l,e,"Kelly","I scraped my knee on something last week, coming back from the bathroom. I can't find it now. The scrape. It's just gone.");}}
                case 6->{if(e.stage==ROUND){o.putBoolean("Watched253",true);cue(p,"You stay close. She turns at the newel post, the way she did the last time, and the time before. Her face is slack. Her eyes are open.");break;}
                    o.putBoolean("Told253",true);say(l,e,"Kelly","Was it me?");
                    LiteraryChoiceMenu.open(p,"Was it her?",Map.of(3,icon(Items.ENDER_EYE,"It looked like you"),5,icon(Items.BOOK,"I don't know what it was")),()->LiteraryVignettes.current(p)==PLACE,s2->{
                        if(s2==3){say(l,e,"Kelly","Those aren't the same answer.");}else if(s2==5){say(l,e,"Kelly","No. Me neither. Thank you for saying that and not something else.");}else return false;return true;});}
                case 8->{mirrorChecked(l,e);}
                default->ok=false;
            }
            save(p,o);return ok;});
    }
    static void mirrorChecked(ServerLevel l,Evening e){if(e.mirrorDone)return;e.mirrorDone=true;say(l,e,"Kelly","Down a little— no. There. That's it. Hello, me.");}
    static void call(ServerPlayer p,BlockPos b,CompoundTag own,Evening e){
        var l=p.serverLevel();own.putBoolean("Called253",true);e.calls++;
        say(l,e,p.getGameProfile().getName(),"Kelly.");
        if(!e.markComing){e.markComing=true;e.markUp=false;resetSecond(actor(l,e.mark));note(l,e,"Upstairs, a chair pushed back. Mark's on the stairs, fast.");}
        var m=actor(l,e.mark);var k=actor(l,e.kelly);
        boolean markHere=m!=null&&k!=null&&m.distanceToSqr(k)<64;
        if(e.calls>=CALLS&&markHere&&k!=null){own.putBoolean("WokeHer253",true);say(l,e,"Mark","Kel. Kelly. Keep saying it. She hears it, in the end.");wake(l,e,k,m,false);}
        else if(e.calls==2)note(l,e,"She stops for a moment, at the sound, and goes on.");
    }
    static void markMenu(ServerPlayer p,BlockPos b,CompoundTag own,Evening e,LiteraryActor m){
        var l=p.serverLevel();var icons=new LinkedHashMap<Integer,ItemStack>();
        if(e.stage==AWAKE){tableMenu(p,b,own,e);return;}
        if(!own.getBoolean("AskWork253"))icons.put(0,icon(Items.CLOCK,"Ask about his work upstairs"));
        if(!own.getBoolean("AskNight253"))icons.put(1,icon(Items.CANDLE,"Ask about the night she fell"));
        if(!e.caseAgreed)icons.put(2,icon(Items.CHEST,"Ask whether his work could come downstairs"));
        if(e.stage==ROUND&&!own.getBoolean("AskBefore253"))icons.put(4,icon(Items.BONE,"Ask what he has seen before"));
        var home=HouseRead.category(p,HouseRead.Trait.HOME).filter(c->c.strength()==HouseRead.Strength.SETTLED).map(HouseRead.Category::value).orElse("");
        if(home.equals("DANGER")&&own.getBoolean("AskNight253")&&!own.getBoolean("Protect253"))icons.put(6,icon(Items.SHIELD,"Say that protecting someone can get hard to stop"));
        if(icons.isEmpty()){cue(p,"He nods at you, and goes back to listening to the house.");return;}
        LiteraryChoiceMenu.open(p,"Mark",icons,()->LiteraryVignettes.current(p)==PLACE&&!own(p).getBoolean("Completed"),slot->{
            var o=own(p);
            switch(slot){
                case 0->{o.putBoolean("AskWork253",true);e.lines++;say(l,e,"Mark","Consulting. Phones and screens, people in other time zones. Up there I can hear everything down here. Which is the point. Which is maybe the problem.");}
                case 1->{o.putBoolean("AskNight253",true);o.putBoolean("AccidentTold253",true);e.lines++;say(l,e,"Mark","I heard something that night.");
                    LiteraryChoiceMenu.open(p,"Mark",Map.of(4,icon(Items.CANDLE,"When she fell?")),()->LiteraryVignettes.current(p)==PLACE,s2->{if(s2!=4)return false;say(l,e,"Mark","I thought it was outside. A squirrel, I thought. I didn't even get out of bed.");return true;});}
                case 2->{e.caseAgreed=true;o.putBoolean("AskedCase253",true);say(l,e,"Mark","The desk can't. The case could. If somebody carried it, I'd— yes. All right.");}
                case 4->{o.putBoolean("AskBefore253",true);say(l,e,"Mark","It's not the first time. I tried to wake her once by holding her still. She doesn't know she does it.");note(l,e,"He holds up his left hand. A bandage across the knuckles.");note(l,e,"He rubs at his hip, through his jeans, without seeming to know he's doing it.");}
                case 6->{o.putBoolean("Protect253",true);say(l,e,"Mark","Yeah. You start because you have to. Then you can't hear yourself think over the listening.");}
                default->{return false;}
            }
            save(p,o);return true;});
    }
    static void stanMenu(ServerPlayer p,BlockPos b,CompoundTag own,Evening e){
        var l=p.serverLevel();var icons=new LinkedHashMap<Integer,ItemStack>();
        if(!e.stanRedirected)icons.put(2,icon(Items.LEAD,"Ask him to answer Kelly"));
        if(!own.getBoolean("AskRamp253"))icons.put(4,icon(Items.SMOOTH_STONE_SLAB,"Ask about the garage ramp"));
        if(icons.isEmpty()){cue(p,"Stan looks at the stairs as if they were his own handwriting.");return;}
        LiteraryChoiceMenu.open(p,"Stan",icons,()->LiteraryVignettes.current(p)==PLACE,slot->{
            var o=own(p);
            if(slot==2){e.stanRedirected=true;o.putBoolean("Redirected253",true);say(l,e,"Stan","...Right. Kel. The chair goes in the back, strapped, and you ride up front with me. I've measured.");}
            else if(slot==4){o.putBoolean("AskRamp253",true);say(l,e,"Stan","Didn't want it. Paid for it. Don't tell her that.");}
            else return false;
            save(p,o);return true;});
    }
    static void runnerMenu(ServerPlayer p,BlockPos b,CompoundTag own,Evening e){
        var l=p.serverLevel();var icons=new LinkedHashMap<Integer,ItemStack>();
        if(!e.runnerAsked){icons.put(2,icon(Items.WHITE_CARPET,"Ask before lifting the runner"));}
        else if(PeopleLightsSite.runnerLaid(l,b)){icons.put(2,icon(Items.WHITE_CARPET,"Leave it down, for the quiet (Kelly)"));icons.put(6,icon(Items.STRING,"Roll it up, to hear her (Mark)"));}
        else{cue(p,"Rolled up against the wall at the hall's end.");return;}
        LiteraryChoiceMenu.open(p,"The hall runner",icons,()->LiteraryVignettes.current(p)==PLACE,slot->{
            var o=own(p);
            if(!e.runnerAsked&&slot==2){e.runnerAsked=true;say(l,e,"Kelly","Leave it. It's the one quiet part of the house.");say(l,e,"Mark","I'd rather hear her.");say(l,e,"Kelly","I know you would.");}
            else if(e.runnerAsked&&slot==2){e.runnerChoice="kelly";o.putString("Runner253","kept");cue(p,"You leave it down.");}
            else if(e.runnerAsked&&slot==6){e.runnerChoice="mark";o.putString("Runner253","rolled");PeopleLightsSite.runner(l,b,false);cue(p,"You roll it up and stand it at the end of the hall.");}
            else return false;
            save(p,o);return true;});
    }
    /** The conversation at the kitchen table, after the night: the couple's plans, agreed by both or not at all. */
    static void tableMenu(ServerPlayer p,BlockPos b,CompoundTag own,Evening e){
        var l=p.serverLevel();var icons=new LinkedHashMap<Integer,ItemStack>();
        icons.put(0,icon(Items.POPPY,"Ask Kelly what she wants tonight"));
        icons.put(1,icon(Items.CLOCK,"Ask Mark what he has been leaving out"));
        icons.put(2,icon(Items.WRITABLE_BOOK,"Say you can't tell them what the house is doing"));
        if(e.kellyWants)icons.put(3,icon(Items.WHITE_BED,"One more night, and call Stan in the morning"));
        icons.put(4,icon(Items.MINECART,"Suggest calling Stan together, now"));
        icons.put(5,icon(Items.LANTERN,"Offer a way to spend the night downstairs together"));
        icons.put(8,icon(Items.BARRIER,"Ask for a little time"));
        LiteraryChoiceMenu.open(p,"At the kitchen table",icons,()->LiteraryVignettes.current(p)==PLACE&&e.stage==AWAKE,slot->{
            var o=own(p);
            switch(slot){
                case 0->{e.kellyWants=true;o.putBoolean("AskedKelly253",true);say(l,e,"Kelly","I want to call my father in the morning. Not now. Now I want tonight. And I want to not be listened to through the floor.");}
                case 1->{e.markAdmitted=true;o.putBoolean("AskedMark253",true);say(l,e,"Mark","The thing upstairs looks like you. Like you before. And when you go round the hall at night it frightens me, and I sit up there to keep you where I can hear you.");
                    say(l,e,"Kelly","You listen to me. Through the floor. For how long?");say(l,e,"Kelly","...And I don't remember any of it. That's the part. I don't remember.");}
                case 2->{e.uncertain=true;o.putBoolean("Uncertain253",true);say(l,e,"Mark","No. Nobody can.");say(l,e,"Kelly","But you saw it. That's something.");}
                case 3->{if(!e.kellyWants)return false;say(l,e,"Kelly","One more night. I'll call Dad in the morning.");say(l,e,"Mark","Then I'm staying with you tonight. In there. Not up there.");
                    agree(p,b,e,"one_more_night");}
                case 4->{if(!e.kellyWants){say(l,e,"Kelly","Don't decide that for me. Ask me first.");e.refusal|=1;}
                    else if(!e.markAdmitted){say(l,e,"Mark","Not tonight. Tomorrow. I can't— not tonight.");e.refusal|=2;}
                    else{say(l,e,"Kelly","...Yes. Now. I'll ring him.");say(l,e,"Mark","Yes.");agree(p,b,e,"left_together");}}
                case 5->{if(!e.markAdmitted){say(l,e,"Mark","Downstairs is hers. Upstairs is mine. That's how it works.");say(l,e,"Kelly","Is it?");e.refusal|=4;}
                    else changeMenu(p,b,e);}
                case 8->{cue(p,"Nobody has to decide yet. Ask again when you're ready.");}
                default->{return false;}
            }
            save(p,o);return slot!=5||!e.markAdmitted;});
    }
    static void changeMenu(ServerPlayer p,BlockPos b,Evening e){
        var l=p.serverLevel();
        say(l,e,"Kelly","If we stay, something changes tonight. I'll choose what.");
        var icons=new LinkedHashMap<Integer,ItemStack>();icons.put(2,icon(Items.CHEST,"Mark's work, down here at the table"));icons.put(4,icon(Items.LANTERN,"The lamp by the mirror, left on"));icons.put(6,icon(Items.WHITE_CARPET,"The runner, the way she wants it"));
        LiteraryChoiceMenu.open(p,"Kelly chooses",icons,()->LiteraryVignettes.current(p)==PLACE&&e.stage==AWAKE,slot->{
            String c=switch(slot){case 2->"case";case 4->"lamp";case 6->"runner";default->"";};if(c.isEmpty())return false;
            e.changeChoice=c;if(c.equals("runner"))PeopleLightsSite.runner(l,b,true);if(c.equals("lamp"))PeopleLightsSite.lamp(l,b,true);
            say(l,e,"Mark","And I bring my work down. And stop sitting up there with my ear to the floor.");agree(p,b,e,"shared_the_downstairs");return true;});
    }
    static void agree(ServerPlayer p,BlockPos b,Evening e,String plan){
        var l=p.serverLevel();e.plan=plan;
        for(var r:readers(l,b)){var o=own(r);o.putString("Plan253",plan);if(r==p)o.putBoolean("Proposed253",true);save(r,o);}
        if(plan.equals("one_more_night")){next(l,e,ONE_MORE_NIGHT);say(l,e,"Kelly","Will you stay? Until morning. You don't have to.");stayMenu(p,b,e);}
        else if(plan.equals("left_together"))next(l,e,LEAVING);
        else next(l,e,SHARING);
    }
    static void stayMenu(ServerPlayer p,BlockPos b,Evening e){
        var l=p.serverLevel();
        LiteraryChoiceMenu.open(p,"Until morning?",Map.of(2,icon(Items.CLOCK,"Stay until morning"),6,icon(Items.OAK_DOOR,"Give them the night, and go")),()->LiteraryVignettes.current(p)==PLACE&&e.stage==ONE_MORE_NIGHT,slot->{
            var o=own(p);
            if(slot==2){o.putBoolean("Stayed253",true);save(p,o);say(l,e,"Mark","Thank you. There's the sofa. The lamp's on.");cue(p,"When you're ready to let the night pass, sit by the sofa.");return true;}
            if(slot==6){o.putBoolean("GaveNight253",true);save(p,o);say(l,e,"Kelly","Thank you for tonight. Go on. The garage is open.");
                o.putBoolean("Ready",true);o.putString("Outcome","gave_them_the_night");o.putInt("Beat",o.getInt("Visit"));complete(p,o);save(p,o);return true;}
            return false;});
    }
    static void nightMenu(ServerPlayer p,BlockPos b,CompoundTag own,Evening e){
        var l=p.serverLevel();
        LiteraryChoiceMenu.open(p,"The night",Map.of(4,icon(Items.CLOCK,"Let the night pass")),()->LiteraryVignettes.current(p)==PLACE&&e.stage==ONE_MORE_NIGHT,slot->{
            if(slot!=4)return false;e.nightPassed=true;
            for(var r:readers(l,b)){r.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,80,0,false,false));r.addEffect(new MobEffectInstance(MobEffects.DARKNESS,80,0,false,false));}
            note(l,e,"Through the bedroom door, small sounds: the sheets, someone saying the other's name, once, surprised and then not. Then nothing for a long time.");
            next(l,e,MORNING);return true;});
    }

    // ------------------------------------------------------------------------------------------------ carried things
    /** What a reader is carrying for the household: 0 nothing, 1 Mark's work case, 2 the spare chair. */
    static int carried(ItemStack s){if(!s.is(PeopleLightsRegistry.CARRIED.get()))return 0;var c=s.get(DataComponents.CUSTOM_DATA);return c==null?0:c.copyTag().getInt(TAG);}
    static int holding(ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++){int c=carried(p.getInventory().getItem(i));if(c!=0)return c;}return carried(p.containerMenu.getCarried());}
    static void take(ServerPlayer p,int what,String name){
        var stack=new ItemStack(PeopleLightsRegistry.CARRIED.get());stack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(what));stack.set(DataComponents.ITEM_NAME,Component.literal(name));
        var t=new CompoundTag();t.putInt(TAG,what);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(t));
        var inv=p.getInventory();var before=inv.getItem(inv.selected);var own=own(p);own.put("Before253",before.saveOptional(p.registryAccess()));own.putInt("Slot253",inv.selected);save(p,own);
        inv.setItem(inv.selected,stack);p.inventoryMenu.broadcastChanges();
    }
    static void release(ServerPlayer p){
        var inv=p.getInventory();int was=0;
        for(int i=0;i<inv.getContainerSize();i++){int c=carried(inv.getItem(i));if(c!=0){was=c;inv.setItem(i,ItemStack.EMPTY);}}
        if(carried(p.containerMenu.getCarried())!=0){was=carried(p.containerMenu.getCarried());p.containerMenu.setCarried(ItemStack.EMPTY);}
        var own=own(p);
        if(own.contains("Before253")){var before=ItemStack.parseOptional(p.registryAccess(),own.getCompound("Before253"));int slot=own.getInt("Slot253");
            if(!before.isEmpty()){if(slot>=0&&slot<inv.getContainerSize()&&inv.getItem(slot).isEmpty())inv.setItem(slot,before);else inv.placeItemBackInInventory(before);}}
        own.remove("Before253");own.remove("Slot253");save(p,own);p.inventoryMenu.broadcastChanges();
        var b=LiteraryVignettes.base(p,PLACE);var l=p.serverLevel();
        if(b!=null&&was==1&&!PeopleLightsSite.caseDownstairs(l,b))PeopleLightsSite.caseUpstairs(l,b,true);
        if(b!=null&&was==2&&!PeopleLightsBlock.is(l.getBlockState(b.offset(PeopleLightsSite.BED_CHAIR)),PeopleLightsBlock.Kind.CHAIR))PeopleLightsSite.chair(l,b,PeopleLightsSite.SPARE_CHAIR,Direction.SOUTH,true);
    }
    static boolean putDown(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own,Evening e,int held){
        var l=p.serverLevel();
        if(held==1){
            if(rel.getX()==3&&rel.getY()==0&&rel.getZ()<=-13&&rel.getZ()>=-15){release(p);PeopleLightsSite.caseUpstairs(l,b,false);var o=own(p);o.putBoolean("CaseDown253",true);save(p,o);
                cue(p,"You set the case at the end of the kitchen table.");if(e.stage!=SHARING)say(l,e,"Mark","Oh. Thank you. I'll— yes. That works.");return true;}
            cue(p,"Set the case down on the kitchen table.");return true;}
        if(held==2){
            if(e.stage==MORNING&&rel(Vec3.atCenterOf(b.offset(rel)),b).distanceToSqr(at(PeopleLightsSite.BED))<9){release(p);PeopleLightsSite.chair(l,b,PeopleLightsSite.BED_CHAIR,Direction.NORTH,true);e.spareBrought=true;var o=own(p);o.putBoolean("BroughtChair253",true);save(p,o);
                cue(p,"You unfold the spare chair beside the bed.");say(l,e,"Mark","Thanks. Give me a— there. There.");return true;}
            cue(p,"Set the chair down beside the bed.");return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the ending
    /** The ending: the house's account in the reader's hand, and their Witness line. */
    public static void complete(ServerPlayer p,CompoundTag own){
        if(own.getBoolean("Completed")||!own.getBoolean("Ready"))return;
        if(holding(p)!=0)release(p);
        var book=LiteraryTexts.ending(p.getGameProfile().getName(),PLACE,own);if(!p.getInventory().add(book))p.drop(book,false);
        var story=WitnessAccount.Story.of(PLACE.id());if(story!=null)WitnessAccount.resolve(p,story,own.getString("Outcome"));
        own.putBoolean("Completed",true);LabyrinthData.get(p.server).setCompleted(PLACE.id(),true);
        cue(p,PeopleLightsTexts.ended(own.getString("Outcome")));
    }
}
