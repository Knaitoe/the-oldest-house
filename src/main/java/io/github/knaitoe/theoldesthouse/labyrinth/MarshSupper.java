package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.348, The Supper (docs/MARSH_SUPPER_0_4_348.md), the owner's own story from the draft "The House Has a Yard": a corn maze opens
 * onto a farmhouse clearing; the only drive is blocked by a fallen oak; "Wipe your boots" is the first thing heard inside. The first meal
 * is given time to feel ordinary. A guest, Corinne, keeps checking the porch light for her ride. Between courses she goes out to look at
 * it, and when the readers are back at the table her chair is gone and Ida says her ride came; the drive gives them reason to doubt it.
 * A look into the kitchen (the swing door as Del comes through, the serving hatch, the window off the porch) shows her keys and coat by
 * the butcher's block before the meat is served. If a reader says her name, Del admits he heard her calling from the kitchen and Nana
 * says she thought it came from the corn. Then the meat. Each reader eats, gets up and leaves, or lays her keys on the table in front of
 * Ida; any of the three resolves the story.
 *
 * <p>One supper for the party (the house at its base): one table, one Corinne, one meal. Keeping Ida talking keeps her eyes off the
 * kitchen door. The keys can be passed from hand to hand. Each reader's account is built from their own record. Nothing here hurts.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class MarshSupper {
    private MarshSupper(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.MARSH_SUPPER;
    /** The household and the guest, on stranger phases: Ida, Royce, Del, Nana and Corinne. */
    public static final int IDA=110,ROYCE=111,DEL=112,NANA=113,CORINNE=114;
    static final String[] NAMES={"Ida","Royce","Del","Nana","Corinne"};
    public static boolean cast(int phase){return phase>=IDA&&phase<=CORINNE;}
    public static String skin(int phase){return "marsh_"+NAMES[phase-IDA].toLowerCase(Locale.ROOT);}
    /** The supper's stages. */
    public static final int ARRIVE=0,SOUP=1,CORINNE_OUT=2,CHAIR_GONE=3,NAME=4,MEAT=5,OVER=6;
    /** Ticks: how long readers are in the house before the meal starts with nobody sat down; the first meal; the longest it takes the
     * household to clear; the longest the table waits for the meat; how long the kitchen door swings open; an empty farm's reset. */
    public static final int START_AFTER=400,SOUP_LENGTH=1000,CLEAR_LIMIT=900,NAME_LENGTH=900,SWING_OPEN=40,EMPTY_RESET=200;
    public static final String TALK="marsh_supper";
    /** 0.4.555: how many readers one farm's supper holds, one to each chair laid for a guest; the sixth is given a farm of their own (SceneInstances). */
    public static final int PLACES=5;
    static final String WITNESS="put_her_keys_on_the_table",ATE="ate_at_the_marsh_table",LEFT="left_before_the_meat";

    static final class Supper {
        final BlockPos base;int stage=ARRIVE,since,empty,inHouse,doorOpen,nameAt=-1;long tick;
        final UUID[] cast=new UUID[5];
        boolean corinneGone,propsOut,chairGone,rideSaid,nameSaid,meatServed,coatTaken,backUnlatched,walked;
        String keysBy="",nameBy="";@Nullable UUID royceBehind;
        /** Errands: who is walking, along which points (local), where they are on it, and how long they wait at the end. */
        final Map<Integer,List<Vec3>> routes=new HashMap<>();final Map<Integer,Integer> leg=new HashMap<>(),dwell=new HashMap<>(),paused=new HashMap<>();
        /** A supper found without its household (a new one, or one the server forgot across a restart): the farm is set back first. */
        boolean fresh=true;
        final Map<UUID,Long> said=new HashMap<>();
        final Set<UUID> asked=new HashSet<>(),poured=new HashSet<>(),served=new HashSet<>();
        Supper(BlockPos b){base=b;}
    }
    private static final Map<Long,Supper> SUPPERS=new HashMap<>();
    /** Readers walking away through the maze, and when they turned their backs on the house: the windows go out behind them. */
    private static final Map<UUID,Long> LEAVING=new HashMap<>();
    public static void clearAll(){SUPPERS.clear();LEAVING.clear();WITH.clear();CLAPPER.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Supper supper(BlockPos b){return SUPPERS.get(b.asLong());}
    public static int stage(BlockPos b){var s=supper(b);return s==null?-1:s.stage;}
    public static boolean flag(BlockPos b,String name){var s=supper(b);if(s==null)return false;return switch(name){case "corinneGone"->s.corinneGone;case "props"->s.propsOut;case "chair"->s.chairGone;case "ride"->s.rideSaid;case "name"->s.nameSaid;case "meat"->s.meatServed;case "back"->s.backUnlatched;default->false;};}
    @Nullable public static LiteraryActor actor(ServerLevel l,BlockPos b,int phase){var s=supper(b);return s==null?null:actor(l,s.cast[phase-IDA]);}
    /** For tests: the clock of the current stage moved on to at least this many ticks. */
    public static void skip(BlockPos b,int since){var s=supper(b);if(s!=null)s.since=Math.max(s.since,since);}
    /** For tests: the supper moved on to a stage as if its conditions had been met. */
    public static void advance(ServerLevel l,BlockPos b,int to){var s=supper(b);if(s==null)return;while(s.stage<to&&s.stage<OVER)next(l,s);}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static Vec3 seatAt(BlockPos seat){return Vec3.atBottomCenterOf(seat);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Someone at the farm says something: over their head, and in the action bar for readers near enough to hear it. */
    static void speak(ServerLevel l,Supper s,int who,String line){
        var a=actor(l,s.cast[who]);if(a==null)return;a.say(line);a.speaks(line);s.said.put(a.getUUID(),s.tick);
        for(var r:readers(l,s.base))if(r.distanceToSqr(a)<14*14)r.displayClientMessage(Component.literal(NAMES[who]+": “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    static void sound(ServerLevel l,BlockPos at,SoundEvent e,float volume,float pitch){l.playSound(null,at,e,SoundSource.BLOCKS,volume,pitch);}
    /** Marks a fact in the record of every reader in the house who was there for it. */
    static void witnessed(ServerLevel l,Supper s,String key){for(var r:readers(l,s.base)){if(!MarshSupperSite.inHouse(rel(r.position(),s.base)))continue;var own=own(r);if(own.getBoolean("Completed"))continue;own.putBoolean(key,true);save(r,own);}}

    // ------------------------------------------------------------------------------------------------ who is here
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    /** The chair a reader is sat in, if any. */
    @Nullable static BlockPos seated(ServerPlayer p,BlockPos b){var v=p.getVehicle();if(!(v instanceof io.github.knaitoe.theoldesthouse.house.SeatEntity))return null;var at=v.blockPosition().subtract(b);for(var c:MarshSupperSite.chairs())if(c.getX()==at.getX()&&c.getZ()==at.getZ())return c;return null;}
    static boolean atTable(ServerPlayer p,BlockPos b){var r=rel(p.position(),b);return seated(p,b)!=null||MarshSupperSite.inDining(r)&&r.x>MarshSupperSite.TABLE_W-3&&r.x<MarshSupperSite.TABLE_E+3&&r.z<MarshSupperSite.TABLE_S+3&&r.z>MarshSupperSite.TABLE_N-3;}
    /**
     * Whether a reader could see a point: within reach, before their eyes, and nothing in the way but glass, open doors and the
     * furniture. The household only does what it does where nobody can see it.
     */
    static boolean sees(ServerPlayer p,Vec3 point,double reach,double cone){
        var eye=p.getEyePosition();var to=point.subtract(eye);double d=to.length();if(d>reach)return false;if(d<.8)return true;
        if(p.getLookAngle().dot(to.scale(1/d))<cone)return false;
        var l=p.level();int steps=(int)(d/.1);
        for(int i=1;i<steps;i++){var at=eye.add(to.scale(i/(double)steps));var pos=BlockPos.containing(at);var st=l.getBlockState(pos);
            if(st.isAir()||transparent(st))continue;
            var shape=st.getBlock() instanceof LeavesBlock||YardBlock.is(st,YardBlock.Kind.STALK)?net.minecraft.world.phys.shapes.Shapes.block():st.getCollisionShape(l,pos);
            if(shape.isEmpty())continue;
            var local=at.subtract(pos.getX(),pos.getY(),pos.getZ());for(var box:shape.toAabbs())if(box.inflate(.02).contains(local))return false;}
        return true;
    }
    static boolean transparent(BlockState st){
        return st.getBlock() instanceof IronBarsBlock||st.getBlock() instanceof FenceBlock||st.getBlock() instanceof FenceGateBlock||st.getBlock() instanceof DoorBlock&&st.getValue(DoorBlock.OPEN)
            ||st.is(LiteraryRegistry.GHOST_SET.get())||st.getBlock() instanceof HouseholdFurnitureBlock||st.is(MarshRegistry.FITTING.get())&&st.getValue(MarshBlock.KIND)!=MarshBlock.Kind.WINDOW&&st.getValue(MarshBlock.KIND)!=MarshBlock.Kind.BUTCHER
            ||st.getBlock() instanceof LanternBlock||st.getBlock() instanceof CarpetBlock;
    }
    static boolean anyoneSees(ServerLevel l,Supper s,Vec3 worldPoint){for(var r:readers(l,s.base))if(sees(r,worldPoint,48,.35))return true;return false;}

    // ------------------------------------------------------------------------------------------------ the cast
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static BlockPos seatOf(int who){return switch(who){case 0->MarshSupperSite.IDA_SEAT;case 1->MarshSupperSite.ROYCE_SEAT;case 2->MarshSupperSite.DEL_SEAT;case 3->MarshSupperSite.NANA_SEAT;default->MarshSupperSite.CORINNE_SEAT;};}
    @Nullable static LiteraryActor spawn(ServerLevel l,BlockPos b,int who){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,IDA+who);place(a,b,seatAt(seatOf(who)),MarshSupperSite.facing(seatOf(who)).toYRot());
        return l.addFreshEntity(a)?a:null;
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 r,float yaw){a.moveTo(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    static void clearActors(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(MarshSupperSite.WEST,-4,MarshSupperSite.NORTH)).minmax(new AABB(b.offset(MarshSupperSite.EAST,18,1))),a->a.role()==LiteraryActor.STRANGER&&cast(a.phase())))a.discard();}
    /** Everyone who should be at the farm is there, in their chair unless they are on an errand. */
    static void ensureCast(ServerLevel l,Supper s){
        if(!l.isLoaded(s.base.offset(MarshSupperSite.IDA_SEAT))||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(s.base.offset(MarshSupperSite.IDA_SEAT))))return;
        for(int who=0;who<5;who++){if(who==4&&s.corinneGone)continue;if(actor(l,s.cast[who])==null){var a=spawn(l,s.base,who);if(a!=null)s.cast[who]=a.getUUID();}}
    }
    /** Sends someone on an errand along local points, waiting a while at the end. */
    static void send(Supper s,int who,int wait,Vec3... points){s.routes.put(who,new ArrayList<>(List.of(points)));s.leg.put(who,0);s.dwell.put(who,wait);}
    static boolean busy(Supper s,int who){return s.routes.containsKey(who);}
    /** A wait in the middle of an errand. */
    static Vec3 pause(int ticks){return new Vec3(ticks,-99,0);}
    /** One step along everyone's errand; back in their chair when it ends. Returns whether anyone is still walking. */
    static void walk(ServerLevel l,Supper s){
        for(var who:List.copyOf(s.routes.keySet())){
            var a=actor(l,s.cast[who]);if(a==null){s.routes.remove(who);continue;}
            var route=s.routes.get(who);int i=s.leg.get(who);
            if(i<route.size()&&route.get(i).y<-50){int left=s.paused.getOrDefault(who,(int)route.get(i).x);if(left>0){s.paused.put(who,left-1);continue;}s.paused.remove(who);s.leg.put(who,i+1);continue;}
            if(i<route.size()){
                var target=world(s.base,route.get(i));
                openDoorsNear(l,s,a.position());
                if(SceneFootpaths.walk(a,target,.13)||a.position().distanceToSqr(target)<.09){s.leg.put(who,i+1);}
                continue;
            }
            int wait=s.dwell.getOrDefault(who,0);if(wait>0){s.dwell.put(who,wait-1);continue;}
            s.routes.remove(who);s.leg.remove(who);s.dwell.remove(who);
            if(!(who==4&&s.stage>=CORINNE_OUT))place(a,s.base,seatAt(seatOf(who)),MarshSupperSite.facing(seatOf(who)).toYRot());
        }
    }
    /** The household opens the doors it walks through; the kitchen's swings shut after them. */
    static void openDoorsNear(ServerLevel l,Supper s,Vec3 at){
        for(var d:new BlockPos[]{MarshSupperSite.KITCHEN_DOOR,MarshSupperSite.FRONT_DOOR,MarshSupperSite.BACK_DOOR}){
            var pos=s.base.offset(d);if(at.distanceToSqr(Vec3.atBottomCenterOf(pos))>6.8)continue;
            var st=l.getBlockState(pos);if(st.getBlock() instanceof DoorBlock door&&!st.getValue(DoorBlock.OPEN)){door.setOpen(null,l,st,pos,true);if(d==MarshSupperSite.KITCHEN_DOOR){s.doorOpen=SWING_OPEN;sound(l,pos,MarshRegistry.SWING.get(),.8F,1F);}}
            if(d==MarshSupperSite.KITCHEN_DOOR)s.doorOpen=Math.max(s.doorOpen,SWING_OPEN/2);
        }
    }
    /** Heads turn to whoever is nearest at the table; still once the keys are down. */
    static void look(ServerLevel l,Supper s){
        var here=readers(l,s.base);
        for(int who=0;who<5;who++){var a=actor(l,s.cast[who]);if(a==null||busy(s,who))continue;
            if(s.stage==OVER){a.setYHeadRot(MarshSupperSite.facing(seatOf(who)).toYRot());continue;}
            ServerPlayer near=null;double best=64;for(var r:here){double d=r.distanceToSqr(a);if(d<best){best=d;near=r;}}
            if(near!=null)SceneFootpaths.face(a,near.getEyePosition(),70);
            var t=s.said.get(a.getUUID());if(t!=null&&!a.saying()){a.setCustomNameVisible(false);a.silent();s.said.remove(a.getUUID());}
        }
    }

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var s=SUPPERS.computeIfAbsent(b.asLong(),k->new Supper(b));
        own.putBoolean("SupperHere",true);
        if(s.stage>=CHAIR_GONE&&!own.getBoolean("Completed"))own.putBoolean("Partway",true);
        var d=LabyrinthData.get(p.server);
        var age=HouseRead.value(d,p.getUUID(),HouseRead.Trait.AGE,false);own.putString("SupperDrink",age.equals("ANALOG")||age.equals("TRANSITIONAL")?"tea":"milk");
        var home=HouseRead.value(d,p.getUUID(),HouseRead.Trait.HOME,true);if(!home.isEmpty())own.putString("SupperHome",home);
        if(home.equals("HAVEN")&&own.getInt("Visit")>=2)own.putBoolean("PlaceKept",true);
        cue(p,s.stage>=OVER?"The corn is quiet. The farmhouse is dark but for the windows upstairs.":"Corn, cut into paths. Over it, a long way off, a farmhouse's upstairs windows are lit.");
    }

    // ------------------------------------------------------------------------------------------------ the supper's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(SUPPERS.isEmpty())return;var server=e.getServer();var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var s:List.copyOf(SUPPERS.values())){
            s.tick++;var here=readers(l,s.base);
            if(here.isEmpty()){if(++s.empty>=EMPTY_RESET&&l.isLoaded(s.base.offset(MarshSupperSite.IDA_SEAT))){reset(l,s);SUPPERS.remove(s.base.asLong());}continue;}
            s.empty=0;
            if(s.fresh){if(!l.isLoaded(s.base.offset(MarshSupperSite.IDA_SEAT))||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(s.base.offset(MarshSupperSite.IDA_SEAT))))continue;s.fresh=false;reset(l,s);}
            ensureCast(l,s);
            walk(l,s);
            if(s.doorOpen>0&&--s.doorOpen==0){var pos=s.base.offset(MarshSupperSite.KITCHEN_DOOR);var st=l.getBlockState(pos);if(st.getBlock() instanceof DoorBlock door&&st.getValue(DoorBlock.OPEN)){door.setOpen(null,l,st,pos,false);sound(l,pos,MarshRegistry.SWING.get(),.6F,.9F);}}
            if(s.tick%2==0)look(l,s);
            boolean anyInHouse=false;for(var r:here)if(MarshSupperSite.inHouse(rel(r.position(),s.base))){anyInHouse=true;break;}
            if(anyInHouse)s.inHouse++;
            stage(l,s,here);
            s.since++;
        }
        // Readers walking away: the windows go out behind them, one at a time, for them alone.
        if(server.getTickCount()%10==0)for(var id:List.copyOf(LEAVING.keySet())){var p=server.getPlayerList().getPlayer(id);if(p==null||LiteraryVignettes.current(p)!=PLACE){LEAVING.remove(id);continue;}darken(p,LiteraryVignettes.base(p,PLACE),(int)((l.getGameTime()-LEAVING.get(id))/30));}
    }
    static void next(ServerLevel l,Supper s){
        s.stage++;s.since=0;
        switch(s.stage){
            case SOUP->{for(var seat:MarshSupperSite.chairs())set(l,s,MarshSupperSite.setting(seat),GhostsSetBlock.of(GhostsSetBlock.Kind.MEAL_SOUP,Direction.NORTH));}
            case CORINNE_OUT->{}
            case CHAIR_GONE->{corinneGone(l,s);props(l,s);chairGone(l,s);s.backUnlatched=true;}
            case NAME->{}
            case MEAT->{}
            default->{}
        }
    }
    static void set(ServerLevel l,Supper s,BlockPos rel,BlockState st){l.setBlock(s.base.offset(rel),st,Block.UPDATE_CLIENTS);}

    static void stage(ServerLevel l,Supper s,List<ServerPlayer> here){
        int t=s.since;
        switch(s.stage){
            case ARRIVE->{boolean sat=false;for(var r:here)if(seated(r,s.base)!=null)sat=true;
                if(sat||s.inHouse>=START_AFTER){speak(l,s,0,"Sit wherever there's a plate. Royce, pass the bread down.");next(l,s);}}
            case SOUP->soup(l,s,here,t);
            case CORINNE_OUT->clearing(l,s,here,t);
            case CHAIR_GONE->{
                if(!s.rideSaid){boolean dining=false;for(var r:here)if(MarshSupperSite.inDining(rel(r.position(),s.base)))dining=true;
                    if(dining||t>600){speak(l,s,0,"Her ride came.");s.rideSaid=true;witnessed(l,s,"HeardRide");s.since=0;
                        for(var r:here){var own=own(r);if(own.getBoolean("SawTree")&&MarshSupperSite.inHouse(rel(r.position(),s.base))&&!own.getBoolean("Completed")){own.putBoolean("Doubted",true);save(r,own);cue(r,"You saw the oak across the drive. Nothing has come up it in a long while.");}}}
                    return;}
                if(t==60)send(s,2,0,new Vec3(-1.5,1,-64.5),new Vec3(-1.5,1,-70.5),new Vec3(1.5,1,-70.5),new Vec3(3.5,1,-70.5),pause(20),new Vec3(1.5,1,-70.5),new Vec3(-1.5,1,-70.5),new Vec3(-1.5,1,-64.5));
                if(t>=100)next(l,s);
            }
            case NAME->{
                if(s.nameSaid){int k=t-s.nameAt;
                    if(k==20)speak(l,s,2,"I heard her calling. From the kitchen.");
                    if(k==60)speak(l,s,3,"I thought it came from the corn.");
                    if(k==100){speak(l,s,0,"The meat's ready. Royce.");next(l,s);}
                    return;}
                if(t==300&&!busy(s,1))speak(l,s,1,"I'll see to it.");
                if(t>=NAME_LENGTH){speak(l,s,0,"That'll be the meat. Royce.");next(l,s);}
            }
            case MEAT->meat(l,s,here,t);
            default->{}
        }
    }

    /** The first meal, given time to feel ordinary. */
    static void soup(ServerLevel l,Supper s,List<ServerPlayer> here,int t){
        if(t==70)speak(l,s,3,"Speak up when you talk to me. I only get half of it.");
        if(t>=150)for(var r:here)if(seated(r,s.base)!=null&&s.asked.add(r.getUUID()))askWhere(r);
        if(t==240)send(s,4,0,new Vec3(-8.5,1,-66.5),new Vec3(-7.5,1,-60.5),pause(40),new Vec3(-8.5,1,-66.5));
        if(t==300)speak(l,s,4,"Not yet.");
        if(t==330)speak(l,s,2,"Nobody finds this place in the dark.");
        if(t==400)speak(l,s,4,"My brother's coming for me. He said an hour. He's never been on time in his life.");
        if(t>=470)for(var r:here)if(seated(r,s.base)!=null&&s.poured.add(r.getUUID()))pour(l,s,r);
        if(t==560)speak(l,s,2,"Her car quit on the county road. She walked up through the corn to the light.");
        if(t==620)speak(l,s,4,"It was the only light.");
        if(t==700)send(s,4,0,new Vec3(-8.5,1,-66.5),new Vec3(-7.5,1,-60.5),pause(40),new Vec3(-8.5,1,-66.5));
        if(t==760)speak(l,s,4,"Still nothing.");
        if(t==820)speak(l,s,3,"Good soup, Ida.");
        if(t==860)speak(l,s,0,"It's the same soup it always is.");
        if(t==930)speak(l,s,2,"We don't get many come up the drive.");
        if(t>=SOUP_LENGTH&&!busy(s,4)){speak(l,s,4,"I'll just look at the porch light. I won't be a minute.");next(l,s);startOut(l,s);}
    }
    static void pour(ServerLevel l,Supper s,ServerPlayer r){
        var own=own(r);String drink=own.getString("SupperDrink");if(drink.isEmpty())drink="milk";
        own.putString("Poured",drink);save(r,own);
        sound(l,s.base.offset(MarshSupperSite.JUG),SoundEvents.BOTTLE_EMPTY,.5F,.8F);
        cue(r,"Ida fills your glass from the jug without asking. "+(drink.equals("tea")?"Tea, cold and sweet.":"Milk, from the cold."));
    }
    /** Corinne goes out to look at the porch light. On her way she takes her coat from the hook. The household clears the soup. */
    static void startOut(ServerLevel l,Supper s){
        send(s,4,0,new Vec3(-8.5,1,-66.5),new Vec3(-8.5,1,-62.5),new Vec3(-6.5,1,-60.5),new Vec3(-3.5,1,-60.5),new Vec3(-3.5,1,-58.5),new Vec3(-1.5,1,-57.5),new Vec3(10.5,1,-57.5),new Vec3(11.5,1,-60.5),new Vec3(11.5,1,-69.5),new Vec3(10.5,1,-69.5),new Vec3(8.5,1,-69.5),new Vec3(7.5,1,-69.5));
        for(var seat:MarshSupperSite.chairs())if(!seat.equals(MarshSupperSite.CORINNE_SEAT))set(l,s,MarshSupperSite.setting(seat),GhostsSetBlock.of(GhostsSetBlock.Kind.PLATE,Direction.NORTH));
    }
    /** Between courses: Corinne out of the front door and gone where nobody sees; her coat and keys in the kitchen; her chair taken away. */
    static void clearing(ServerLevel l,Supper s,List<ServerPlayer> here,int t){
        var c=actor(l,s.cast[4]);
        if(!s.coatTaken&&c!=null&&c.position().distanceToSqr(world(s.base,new Vec3(-6.5,1,-60.5)))<2.5){s.coatTaken=true;set(l,s,MarshSupperSite.COAT_HOOK,Blocks.AIR.defaultBlockState());speak(l,s,0,"Take your coat, it's turned cold.");}
        if(t==50)send(s,2,0,new Vec3(-1.5,1,-64.5),new Vec3(-1.5,1,-70.5),new Vec3(1.5,1,-70.5),new Vec3(3.5,1,-70.5),pause(20),new Vec3(1.5,1,-70.5),new Vec3(-1.5,1,-70.5),new Vec3(-1.5,1,-64.5));
        if(c!=null&&!s.corinneGone){
            var r=rel(c.position(),s.base);boolean outside=r.z>MarshSupperSite.HOUSE_S+.2;boolean inKitchen=MarshSupperSite.inKitchen(r);
            if(outside&&!s.walked){s.walked=true;var door=s.base.offset(MarshSupperSite.FRONT_DOOR);var st=l.getBlockState(door);if(st.getBlock() instanceof DoorBlock d2&&st.getValue(DoorBlock.OPEN))d2.setOpen(null,l,st,door,false);}
            boolean watched=anyoneSees(l,s,c.position().add(0,1,0))||anyoneSees(l,s,c.getEyePosition());
            if((outside||inKitchen)&&!watched)corinneGone(l,s);
            else if(inKitchen&&!busy(s,4)&&t>CLEAR_LIMIT/2)corinneGone(l,s);
            else if(outside&&watched)for(var p:here)if(MarshSupperSite.onEastPorch(rel(p.position(),s.base))||rel(p.position(),s.base).x>MarshSupperSite.HOUSE_E){var own=own(p);if(!own.getBoolean("SawRound")){own.putBoolean("SawRound",true);save(p,own);}}
        }
        if(s.corinneGone&&!s.propsOut&&!anyoneSees(l,s,Vec3.atCenterOf(s.base.offset(MarshSupperSite.KEYS)))&&!anyoneSees(l,s,Vec3.atCenterOf(s.base.offset(MarshSupperSite.COAT_CHAIR))))props(l,s);
        if(s.corinneGone&&!s.chairGone&&!anyoneSees(l,s,Vec3.atCenterOf(s.base.offset(MarshSupperSite.CORINNE_SEAT)))&&!anyoneSees(l,s,Vec3.atCenterOf(s.base.offset(MarshSupperSite.setting(MarshSupperSite.CORINNE_SEAT)))))chairGone(l,s);
        if(t>=CLEAR_LIMIT){corinneGone(l,s);props(l,s);chairGone(l,s);}
        if(s.corinneGone&&s.propsOut&&s.chairGone&&!busy(s,2)){s.backUnlatched=true;s.stage=CHAIR_GONE;s.since=0;}
    }
    static void corinneGone(ServerLevel l,Supper s){
        if(s.corinneGone)return;s.corinneGone=true;var c=actor(l,s.cast[4]);if(c!=null)c.discard();s.routes.remove(4);
        if(!s.coatTaken){s.coatTaken=true;set(l,s,MarshSupperSite.COAT_HOOK,Blocks.AIR.defaultBlockState());}
        var door=s.base.offset(MarshSupperSite.BACK_DOOR);var st=l.getBlockState(door);if(st.getBlock() instanceof DoorBlock d&&st.getValue(DoorBlock.OPEN))d.setOpen(null,l,st,door,false);
    }
    static void props(ServerLevel l,Supper s){if(s.propsOut)return;s.propsOut=true;MarshSupperSite.kitchen(l,s.base,true);set(l,s,MarshSupperSite.COAT_HOOK,Blocks.AIR.defaultBlockState());}
    static void chairGone(ServerLevel l,Supper s){if(s.chairGone)return;s.chairGone=true;set(l,s,MarshSupperSite.CORINNE_SEAT,Blocks.AIR.defaultBlockState());set(l,s,MarshSupperSite.setting(MarshSupperSite.CORINNE_SEAT),Blocks.AIR.defaultBlockState());}

    /** Royce brings in the meat and carves it; DANGER readers find him standing behind their chair while he does. */
    static void meat(ServerLevel l,Supper s,List<ServerPlayer> here,int t){
        if(t==0)send(s,1,0,new Vec3(-4.5,1,-71.5),new Vec3(-1.5,1,-70.5),new Vec3(1.5,1,-70.5),new Vec3(5.5,1,-72.5),pause(400));
        if(t==120){var route=new ArrayList<Vec3>(List.of(new Vec3(1.5,1,-70.5),new Vec3(-1.5,1,-70.5),new Vec3(-4.5,1,-71.5)));
            for(var r:here){var seat=seated(r,s.base);if(seat!=null&&"DANGER".equals(own(r).getString("SupperHome"))){s.royceBehind=r.getUUID();var back=seat.relative(MarshSupperSite.facing(seat).getOpposite());route.add(seatAt(back));break;}}
            s.routes.put(1,route);s.leg.put(1,0);s.dwell.put(1,140);s.paused.remove(1);}
        if(t==200){set(l,s,MarshSupperSite.ROAST,MarshBlock.of(MarshBlock.Kind.ROAST,Direction.NORTH));speak(l,s,0,"Here it is.");}
        if(t==240){set(l,s,MarshSupperSite.ROAST,MarshBlock.of(MarshBlock.Kind.ROAST,Direction.NORTH,1));serve(l,s,here);s.meatServed=true;speak(l,s,0,"Eat while it's hot.");
            if(s.royceBehind!=null){var p=l.getServer().getPlayerList().getPlayer(s.royceBehind);if(p!=null){var own=own(p);own.putBoolean("RoyceBehind",true);save(p,own);cue(p,"Royce carves standing behind your chair. You hear the knife, and not him.");}}}
        if(s.meatServed)for(var r:here)if(seated(r,s.base)!=null&&!s.served.contains(r.getUUID()))serve(l,s,List.of(r));
    }
    static void serve(ServerLevel l,Supper s,List<ServerPlayer> who){
        for(var seat:List.of(MarshSupperSite.IDA_SEAT,MarshSupperSite.ROYCE_SEAT,MarshSupperSite.DEL_SEAT,MarshSupperSite.NANA_SEAT))set(l,s,MarshSupperSite.setting(seat),GhostsSetBlock.of(GhostsSetBlock.Kind.MEAL_ROAST,Direction.NORTH));
        for(var r:who){var seat=seated(r,s.base);if(seat==null||!s.served.add(r.getUUID()))continue;set(l,s,MarshSupperSite.setting(seat),GhostsSetBlock.of(GhostsSetBlock.Kind.MEAL_ROAST,Direction.NORTH));var own=own(r);own.putBoolean("Served",true);save(r,own);}
    }

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every five ticks, for each reader at the farm. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var own=own(p);var s=supper(b);var l=p.serverLevel();
        if(s==null||own.getBoolean("Completed")){refresh(held,own);return;}
        var r=rel(p.position(),b);
        if(!own.getBoolean("SawTree")&&(MarshSupperSite.nearTree(r)||sees(p,world(b,new Vec3(MarshSupperSite.TREE_X+.5,1,-58)),24,.8))){own.putBoolean("SawTree",true);cue(p,"An oak lies across the drive, roots and all. Nothing on wheels has come up this drive in a long while.");}
        if(MarshSupperSite.onPorch(r)&&!own.getBoolean("Greeted")){own.putBoolean("Greeted",true);
            var ida=actor(l,s.cast[0]);if(ida!=null&&s.stage<OVER){ida.say("Wipe your boots.");ida.speaks("Wipe your boots.");s.said.put(ida.getUUID(),s.tick);p.displayClientMessage(Component.literal("From inside: “Wipe your boots.”").withStyle(ChatFormatting.GRAY),true);}
            if(own.getBoolean("PlaceKept"))own.putBoolean("KeptAgain",true);}
        if(!own.getBoolean("Wiped")&&p.blockPosition().equals(b.offset(MarshSupperSite.MAT))){own.putBoolean("Wiped",true);sound(l,p.blockPosition(),MarshRegistry.WIPE.get(),.8F,1F);cue(p,"You wipe your boots on the mat.");}
        var seat=seated(p,b);
        if(seat!=null){
            boolean theirs=false;for(int who=0;who<5;who++)if(seatOf(who).equals(seat)&&actor(l,s.cast[who])!=null&&!(who==4&&s.corinneGone))theirs=true;
            if(theirs){p.stopRiding();cue(p,"Somebody's already sitting there.");}
            else{if(!own.getBoolean("Seated")&&s.stage<OVER){own.putBoolean("Seated",true);if(own.getBoolean("PlaceKept")&&!own.getBoolean("KeptSaid")){own.putBoolean("KeptSaid",true);speak(l,s,0,"I kept your place.");}}
                own.putInt("Seat",MarshSupperSite.READER_SEATS.indexOf(seat));}
        }
        if(s.propsOut&&!own.getBoolean("SawKitchen")&&l.getBlockState(b.offset(MarshSupperSite.KEYS)).is(MarshRegistry.FITTING.get())&&sees(p,Vec3.atCenterOf(b.offset(MarshSupperSite.KEYS)),14,.85)){
            own.putBoolean("SawKitchen",true);
            String how=MarshSupperSite.inKitchen(r)?"inside":MarshSupperSite.onEastPorch(r)?"window":Math.abs(r.z-MarshSupperSite.HATCH.getZ()-.5)<2.5&&r.x>-3?"hatch":"door";
            own.putString("KitchenHow",how);
            cue(p,"On the side by the butcher's block: a set of car keys on their fob. Her coat is over the back of a chair.");}
        // Leaving: back into the corn, or out past the oak, once her chair was gone.
        if((own.getBoolean("HeardRide")||own.getBoolean("Seated")&&s.stage>=CHAIR_GONE)&&(MarshSupperSite.inMaze(r)||MarshSupperSite.pastTree(r))&&!own.getBoolean("Ate"))left(p,own,MarshSupperSite.inMaze(r)?"corn":"drive");
        save(p,own);refresh(held,own);
    }
    static void darken(ServerPlayer p,@Nullable BlockPos b,int count){
        if(b==null)return;var l=p.serverLevel();var windows=new ArrayList<BlockPos>();
        for(int x=MarshSupperSite.HOUSE_W;x<=MarshSupperSite.HOUSE_E;x++)for(int y=6;y<=13;y++)for(int z:new int[]{MarshSupperSite.HOUSE_S,MarshSupperSite.HOUSE_N,-64,-67,-70}){var at=b.offset(x,y,z);if(MarshBlock.is(l.getBlockState(at),MarshBlock.Kind.WINDOW))windows.add(at);}
        windows.sort(Comparator.comparingLong(BlockPos::asLong));
        for(int i=0;i<Math.min(count,windows.size());i++){var at=windows.get(i);p.connection.send(new ClientboundBlockUpdatePacket(at,l.getBlockState(at).setValue(MarshBlock.STAGE,1)));}
        if(count>windows.size()){var at=b.offset(MarshSupperSite.PORCH_LIGHT);var st=l.getBlockState(at);if(MarshBlock.is(st,MarshBlock.Kind.PORCH_LIGHT))p.connection.send(new ClientboundBlockUpdatePacket(at,st.setValue(MarshBlock.STAGE,1)));}
    }

    // ------------------------------------------------------------------------------------------------ talk
    static void askWhere(ServerPlayer r){
        begin(r,"Ida");Talk.line(r,"Ida","Where have you come from, then?");
        Talk.ask(r,Talk.reply("From the road.",p->answer(p,"road","The road. Well. You found us.")),
            Talk.reply("From a house a long way back.",p->answer(p,"house","A long way. You'll be hungry, then.")),
            Talk.reply("I'd rather not say.",p->answer(p,"quiet","That's all right. Eat.")));
    }
    static void answer(ServerPlayer p,String key,String reply){
        var own=own(p);if(!own.contains("Answer")){own.putString("Answer",key);save(p,own);}
        Talk.line(p,"Ida",reply);var s=supper(LiteraryVignettes.base(p,PLACE));if(s!=null){var a=actor(p.serverLevel(),s.cast[0]);if(a!=null){a.say(reply);a.speaks(reply);s.said.put(a.getUUID(),s.tick);}}
        smallTalk(p,0);
    }
    /** Ida will go on talking as long as a reader keeps her at it; while she does, she does not look up at the kitchen door. */
    static void smallTalk(ServerPlayer p,int asked){
        var s=supper(LiteraryVignettes.base(p,PLACE));if(s==null){Talk.end(p);return;}
        var replies=new ArrayList<Talk.Reply>();
        if((asked&1)==0)replies.add(Talk.reply("Ask about the farm.",q->{Talk.line(q,"Ida","Corn on three sides and the creek on the fourth. Royce's father cleared it with a mule.");smallTalk(q,asked|1);}));
        if((asked&2)==0)replies.add(Talk.reply("Ask about Nana.",q->{Talk.line(q,"Ida","She'll tell you she's deaf. She hears what she wants.");smallTalk(q,asked|2);}));
        if((asked&4)==0)replies.add(Talk.reply("Ask about the supper.",q->{Talk.line(q,"Ida","Ham and bean soup to start, the way it's always been, and then the meat.");smallTalk(q,asked|4);}));
        if((asked&8)==0&&s.stage<=CORINNE_OUT)replies.add(Talk.reply("Ask about Corinne.",q->{Talk.line(q,"Ida","Car trouble on the county road. She's welcome here till her ride comes.");smallTalk(q,asked|8);}));
        if(s.stage>=CHAIR_GONE&&s.stage<OVER&&!s.nameSaid)nameReplies(replies);
        if((asked&16)==0)replies.add(Talk.reply("Tell her about the drive.",q->{var own=own(q);Talk.line(q,"Ida",own.getBoolean("SawTree")?"That old tree. Royce keeps saying he'll get to it.":"What about it?");smallTalk(q,asked|16);}));
        if(replies.isEmpty()){Talk.line(p,"Ida","That's enough about us. Eat.");Talk.end(p);return;}
        replies.add(Talk.act("Leave it there.",Talk::close));
        Talk.ask(p,replies);
    }
    static void nameReplies(List<Talk.Reply> replies){
        replies.add(Talk.reply("Where's Corinne?",q->{Talk.close(q);name(q,"where");}));
        replies.add(Talk.reply("Corinne's still here.",q->{Talk.close(q);name(q,"here");}));
    }
    /** Her name said at the table: Del heard her calling from the kitchen, Nana thought the voice came from the corn, and Ida says the meat is ready. */
    static void name(ServerPlayer p,String line){
        var b=LiteraryVignettes.base(p,PLACE);var s=supper(b);var l=p.serverLevel();if(s==null||s.nameSaid||s.stage<CHAIR_GONE||s.stage>=MEAT)return;
        if(!MarshSupperSite.inDining(rel(p.position(),b))){cue(p,"Nobody at the table can hear you from here.");return;}
        if(s.stage==CHAIR_GONE){s.stage=NAME;s.since=0;}
        s.nameSaid=true;s.nameAt=s.since;s.nameBy=p.getGameProfile().getName();
        var own=own(p);own.putBoolean("SaidName",true);own.putString("NameLine",line);save(p,own);
        for(var r:readers(l,b)){if(r==p||!MarshSupperSite.inHouse(rel(r.position(),b)))continue;var o=own(r);if(o.getBoolean("Completed"))continue;o.putBoolean("HeardName",true);o.putString("NameAsker",s.nameBy);save(r,o);}
        var me=own(p);me.putBoolean("HeardName",true);save(p,me);
    }
    /** Right-clicking someone at the farm. */
    public static void talk(ServerPlayer p,LiteraryActor actor){
        var b=LiteraryVignettes.base(p,PLACE);var s=supper(b);if(s==null||!cast(actor.phase()))return;int who=actor.phase()-IDA;var own=own(p);
        if(own.getBoolean("Completed")){cue(p,switch(who){case 0->"Ida doesn't look up.";case 4->"She doesn't answer.";default->NAMES[who]+" doesn't look at you.";});return;}
        if(s.stage==OVER){cue(p,"Nobody at the table moves.");return;}
        if(holdsKeys(p)){if(who==0)lay(p,b,s);else cue(p,NAMES[who]+" looks at the keys in your hand, then down the table at Ida.");return;}
        switch(who){
            case 0->{if(s.stage==SOUP&&seated(p,b)!=null&&s.asked.add(p.getUUID()))askWhere(p);
                else{begin(p,"Ida");Talk.line(p,"Ida",s.stage==ARRIVE?"Come in, sit down. There's a plate for you. There's always a plate.":s.stage>=CHAIR_GONE?"Something on your mind?":"You eat. There's plenty.");smallTalk(p,0);}}
            case 1->cue(p,"Royce nods and goes back to his plate.");
            case 2->{begin(p,"Del");Talk.line(p,"Del",s.stage<CORINNE_OUT?"You came up through the corn? I used to get lost in there on purpose.":"Mom does a good roast. You'll see.");
                var replies=new ArrayList<Talk.Reply>();
                replies.add(Talk.reply("Lost on purpose?",q->{Talk.line(q,"Del","You can hear everything in the corn and nobody can find you. Then the bell goes and you come in.");Talk.end(q);}));
                if(s.stage>=CHAIR_GONE&&!s.nameSaid)nameReplies(replies);
                replies.add(Talk.act("Leave it there.",Talk::close));Talk.ask(p,replies);}
            case 3->{begin(p,"Nana");Talk.line(p,"Nana","Speak up!");
                var replies=new ArrayList<Talk.Reply>();
                replies.add(Talk.reply("Say it louder.",q->{Talk.line(q,"Nana","I heard you the first time. I was being polite.");Talk.end(q);}));
                if(s.stage>=CHAIR_GONE&&!s.nameSaid)nameReplies(replies);
                replies.add(Talk.act("Leave it there.",Talk::close));Talk.ask(p,replies);}
            default->{var o=own(p);o.putBoolean("TalkedCorinne",true);save(p,o);
                begin(p,"Corinne");Talk.line(p,"Corinne","You didn't pass a car on the county road? Mine's in the ditch past the second bend.");
                Talk.ask(p,Talk.reply("Who's coming for you?",q->{Talk.line(q,"Corinne","My brother. He said an hour. It's been an hour.");Talk.end(q);}),
                    Talk.reply("How did you find this place?",q->{Talk.line(q,"Corinne","The windows upstairs. You can see them from the road, over the corn. I just walked at them.");Talk.end(q);}),
                    Talk.act("Leave it there.",Talk::close));}
        }
    }
    /** Whether anyone is keeping Ida talking just now. */
    static boolean idaTalking(ServerLevel l,BlockPos b){for(var r:readers(l,b))if(Talk.waiting(r,TALK)&&"Ida".equals(talkingTo(r)))return true;return false;}
    /** Who each reader last began talking with at the farm. */
    static final Map<UUID,String> WITH=new HashMap<>();
    static String talkingTo(ServerPlayer r){return WITH.getOrDefault(r.getUUID(),"");}
    static void begin(ServerPlayer p,String who){WITH.put(p.getUUID(),who);Talk.begin(p,TALK,who);}

    // ------------------------------------------------------------------------------------------------ the things in the house
    static boolean holdsKeys(ServerPlayer p){return p.getMainHandItem().is(MarshRegistry.KEYS.get());}
    /** Clicks at the farm: the keys, the plates, the doors, the bell, the card's post, the mat. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag held){
        save(p,held);boolean handled=clicked(p,b,rel,own(p));refresh(held,own(p));return handled;
    }
    static boolean clicked(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var s=supper(b);if(s==null)return false;var st=l.getBlockState(b.offset(rel));
        if(rel.equals(MarshSupperSite.BELL)){sound(l,b.offset(rel),MarshRegistry.BELL.get(),1F,1F);cue(p,"The bell carries out over the corn. Somewhere out there, faintly, it rings back.");return true;}
        if(own.getBoolean("Completed"))return false;
        if(rel.equals(MarshSupperSite.KEYS)&&MarshBlock.is(st,MarshBlock.Kind.KEYS)){
            l.setBlock(b.offset(rel),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
            var keys=new ItemStack(MarshRegistry.KEYS.get());if(!p.getInventory().add(keys))p.drop(keys,false);
            own.putBoolean("TookKeys",true);save(p,own);cue(p,"Her car keys, cold, on a fob from a garage in a town you don't know.");return true;}
        if(MarshBlock.is(st,MarshBlock.Kind.COAT)&&st.getValue(MarshBlock.STAGE)==1){cue(p,"Her coat, over the back of the chair. The pockets are empty.");return true;}
        // Laying the keys on the table in front of Ida.
        if(holdsKeys(p)&&rel.getY()>=MarshSupperSite.GROUND&&rel.getY()<=MarshSupperSite.GROUND+1&&Vec3.atCenterOf(rel).distanceTo(Vec3.atCenterOf(MarshSupperSite.setting(MarshSupperSite.IDA_SEAT)))<2.6){lay(p,b,s);return true;}
        // Eating: the reader's own plate, once the meat is on it.
        if(st.is(LiteraryRegistry.GHOST_SET.get())&&st.getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.MEAL_ROAST){var seat=seated(p,b);
            if(seat==null||!MarshSupperSite.setting(seat).equals(rel)){cue(p,"That's somebody else's plate.");return true;}
            eat(p,b,s,own,rel);return true;}
        // Ida keeps her kitchen, unless somebody is keeping her talking.
        boolean kitchenDoor=rel.equals(MarshSupperSite.KITCHEN_DOOR)||rel.equals(MarshSupperSite.KITCHEN_DOOR.above());
        if(kitchenDoor&&s.stage>=SOUP&&s.stage<OVER&&p.getX()-b.getX()<MarshSupperSite.PARTITION_X+.5&&actor(l,s.cast[0])!=null){
            if(!idaTalking(l,b)){speak(l,s,0,"Not in my kitchen. Sit down, it's nearly ready.");own.putInt("TurnedBack",own.getInt("TurnedBack")+1);save(p,own);return true;}
            own.putBoolean("SlippedIn",true);save(p,own);
            for(var r:readers(l,b))if(r!=p&&Talk.waiting(r,TALK)&&"Ida".equals(talkingTo(r))){var o=own(r);if(!o.getBoolean("Completed")){o.putString("CoveredFor",p.getGameProfile().getName());save(r,o);}}
            s.doorOpen=SWING_OPEN;sound(l,b.offset(MarshSupperSite.KITCHEN_DOOR),MarshRegistry.SWING.get(),.8F,1F);return false;}
        if(kitchenDoor&&p.getX()-b.getX()>MarshSupperSite.PARTITION_X+.5){s.doorOpen=SWING_OPEN;sound(l,b.offset(MarshSupperSite.KITCHEN_DOOR),MarshRegistry.SWING.get(),.8F,1F);return false;}
        boolean back=rel.equals(MarshSupperSite.BACK_DOOR)||rel.equals(MarshSupperSite.BACK_DOOR.above());
        if(back&&!s.backUnlatched&&p.getX()-b.getX()>MarshSupperSite.HOUSE_E+.5){cue(p,"The back door is latched from inside.");return true;}
        if(rel.equals(MarshSupperSite.CARD)&&own.getBoolean("Read_Source"))return false;
        return false;
    }
    static void eat(ServerPlayer p,BlockPos b,Supper s,CompoundTag own,BlockPos rel){
        var l=p.serverLevel();l.setBlock(b.offset(rel),GhostsSetBlock.of(GhostsSetBlock.Kind.PLATE,Direction.NORTH),Block.UPDATE_CLIENTS);
        sound(l,p.blockPosition(),SoundEvents.GENERIC_EAT,.8F,1F);p.getFoodData().eat(6,.6F);
        own.putBoolean("Ate",true);resolve(p,own,ATE,VignetteRole.ACTOR,"");
        cue(p,"You eat what was served. It is good. It is very good.");
        LabyrinthDoors.sendBack(p,30,40,40,q->cue(q,"You wake in the House with the taste of it still in your mouth."));
    }
    static void left(ServerPlayer p,CompoundTag own,String way){
        if(own.getBoolean("Completed"))return;own.putBoolean("Left",true);own.putString("LeftBy",way);
        resolve(p,own,LEFT,VignetteRole.ACTOR,"");
        LEAVING.put(p.getUUID(),p.serverLevel().getGameTime());
        cue(p,way.equals("corn")?"Behind you, over the corn, the farmhouse windows begin to go out.":"You squeeze past the roots. Behind you the porch light goes on burning.");
    }
    /** The keys laid on the table in front of Ida: the household goes still and the porch light goes out. Everyone at the table gets an account. */
    static void lay(ServerPlayer p,BlockPos b,Supper s){
        var l=p.serverLevel();if(s.stage<CHAIR_GONE||s.stage>=OVER)return;
        for(var st:p.getInventory().items)if(st.is(MarshRegistry.KEYS.get()))st.setCount(0);for(var st:p.getInventory().offhand)if(st.is(MarshRegistry.KEYS.get()))st.setCount(0);
        set(l,s,MarshSupperSite.setting(MarshSupperSite.IDA_SEAT),MarshBlock.of(MarshBlock.Kind.KEYS,Direction.SOUTH));
        s.stage=OVER;s.since=0;s.keysBy=p.getGameProfile().getName();s.routes.clear();
        var light=b.offset(MarshSupperSite.PORCH_LIGHT);var ls=l.getBlockState(light);if(MarshBlock.is(ls,MarshBlock.Kind.PORCH_LIGHT))l.setBlock(light,ls.setValue(MarshBlock.STAGE,1),Block.UPDATE_CLIENTS);
        for(int who=0;who<4;who++){var a=actor(l,s.cast[who]);if(a!=null){a.setCustomNameVisible(false);a.silent();}}
        for(var r:readers(l,b)){var own=own(r);if(own.getBoolean("Completed"))continue;boolean me=r==p;
            if(!me&&!MarshSupperSite.inHouse(rel(r.position(),b)))continue;
            own.putBoolean("KeysLaid",true);own.putString("KeysBy",me?"":s.keysBy);
            resolve(r,own,WITNESS,me?VignetteRole.ACTOR:own.getBoolean("Partway")?VignetteRole.PARTWAY:VignetteRole.WITNESS,me?"":s.keysBy);
            cue(r,me?"You put her keys down on the table in front of Ida. Nobody moves. Outside, the porch light goes out.":s.keysBy+" puts her keys down on the table in front of Ida. Nobody moves. Outside, the porch light goes out.");}
    }
    /** A reader's ending: their record finished, their Witness resolution, their account in hand. */
    static void resolve(ServerPlayer p,CompoundTag own,String outcome,VignetteRole role,String actor){
        if(own.getBoolean("Completed"))return;var d=LabyrinthData.get(p.server);
        if(role==VignetteRole.ACTOR&&own.getBoolean("Partway"))role=VignetteRole.PARTWAY;
        own.putBoolean("Ready",true);own.putString("Outcome",outcome);own.putString("SupperRole",role.id());own.putBoolean("Completed",true);own.putBoolean("Taken_Ending",true);own.putInt("Beat",own.getInt("Visit"));
        save(p,own);
        if(role==VignetteRole.ACTOR)WitnessAccount.resolve(p,WitnessAccount.Story.MARSH_SUPPER,outcome);else WitnessAccount.resolveAs(p,WitnessAccount.Story.MARSH_SUPPER,outcome,role,actor);
        d.setCompleted(PLACE.id(),true);
        LiteraryVignettes.reward(p,PLACE,own,"Ending",MarshSupperTexts.account(p.getGameProfile().getName(),own));
        LiteraryVignettes.reward(p,PLACE,own,"Clapper",new ItemStack(MarshRegistry.CLAPPER.get()));
        var fresh=own(p);refresh(own,fresh);
    }

    // ------------------------------------------------------------------------------------------------ the keys and the clapper
    /** The keys never leave the farm: anyone carrying them out finds them gone, and they are back on the side by the butcher's block. */
    @SubscribeEvent public static void keys(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=0)return;
        for(var p:server.getPlayerList().getPlayers()){
            boolean carrying=false;for(var st:p.getInventory().items)if(st.is(MarshRegistry.KEYS.get()))carrying=true;for(var st:p.getInventory().offhand)if(st.is(MarshRegistry.KEYS.get()))carrying=true;
            if(carrying&&LiteraryVignettes.current(p)!=PLACE){for(var st:p.getInventory().items)if(st.is(MarshRegistry.KEYS.get()))st.setCount(0);for(var st:p.getInventory().offhand)if(st.is(MarshRegistry.KEYS.get()))st.setCount(0);keysBack(server,p.getUUID());}
            boolean clapper=false;for(var st:p.getInventory().items)if(st.is(MarshRegistry.CLAPPER.get()))clapper=true;
            if(clapper)CLAPPER.add(p.getUUID());else CLAPPER.remove(p.getUUID());
        }
    }
    /** 0.4.555: back by the butcher's block of the farm the keys were carried from, the one their carrier keeps. */
    static void keysBack(net.minecraft.server.MinecraftServer server,UUID carrier){
        var l=server.getLevel(HouseDimensions.OUTSIDE);if(l==null)return;
        var b=SceneInstances.base(server,PLACE,Math.max(0,SceneInstances.kept(LabyrinthData.get(server),carrier,PLACE)));if(b==null)return;var s=supper(b);
        if(s==null||s.stage>=OVER||!s.propsOut||!l.isLoaded(b.offset(MarshSupperSite.KEYS)))return;
        if(l.getBlockState(b.offset(MarshSupperSite.KEYS)).isAir())l.setBlock(b.offset(MarshSupperSite.KEYS),MarshBlock.of(MarshBlock.Kind.KEYS,Direction.EAST),Block.UPDATE_CLIENTS);
    }
    private static final Set<UUID> CLAPPER=new HashSet<>();
    /** A reader carrying the Marsh dinner bell's clapper: the doors to outside places come up for them a little more often. */
    public static boolean carriesClapper(UUID p){return CLAPPER.contains(p);}

    // ------------------------------------------------------------------------------------------------ dealing, leaving and the reset
    /** Dealt once each, and only to a reader who has crossed the House's yard. */
    public static boolean dealable(LabyrinthData d,UUID p){
        if(LiteraryVignettes.personal(d,p,PLACE).getBoolean("Completed"))return false;
        var seen=d.visited(p);for(var y:YardCrossings.ORDINARY)if(seen.contains(y.id()))return true;return false;
    }
    /** Back through the House's door, or through the gate onto the county road. */
    public static void returned(ServerPlayer p,LabyrinthData.Door door){if(SceneInstances.plain(door.id).equals(PLACE.entryDoorId()))leaving(p,"door");}
    public static void onward(ServerPlayer p,LabyrinthData.Door door){if(door.id.equals(OnwardDoors.id(PLACE)))leaving(p,"gate");}
    static void leaving(ServerPlayer p,String way){
        LEAVING.remove(p.getUUID());
        var own=own(p);if(own.getBoolean("Completed"))return;
        if(own.getBoolean("HeardRide")||own.getBoolean("Seated")&&stage(LiteraryVignettes.base(p,PLACE)==null?BlockPos.ZERO:LiteraryVignettes.base(p,PLACE))>=CHAIR_GONE){left(p,own,way);save(p,own);}
    }
    /** Nobody at the farm: the next reader finds the table laid, Corinne at it, the corn standing, every light on. */
    static void reset(ServerLevel l,Supper s){
        var b=s.base;clearActors(l,b);
        MarshSupperSite.regrowBent(l,b);
        for(var seat:MarshSupperSite.chairs())l.setBlock(b.offset(seat),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,MarshSupperSite.facing(seat)),Block.UPDATE_CLIENTS);
        MarshSupperSite.lay(l,b);MarshSupperSite.kitchen(l,b,false);
        var light=b.offset(MarshSupperSite.PORCH_LIGHT);var ls=l.getBlockState(light);if(MarshBlock.is(ls,MarshBlock.Kind.PORCH_LIGHT))l.setBlock(light,ls.setValue(MarshBlock.STAGE,0),Block.UPDATE_CLIENTS);
        for(var door:new BlockPos[]{MarshSupperSite.FRONT_DOOR,MarshSupperSite.KITCHEN_DOOR,MarshSupperSite.BACK_DOOR}){var st=l.getBlockState(b.offset(door));if(st.getBlock() instanceof DoorBlock d&&st.getValue(DoorBlock.OPEN))d.setOpen(null,l,st,b.offset(door),false);}
        var area=new AABB(b.offset(MarshSupperSite.WEST,-4,MarshSupperSite.NORTH)).minmax(new AABB(b.offset(MarshSupperSite.EAST,18,1)));
        for(var item:l.getEntitiesOfClass(ItemEntity.class,area,i->i.getItem().is(MarshRegistry.KEYS.get())))item.discard();
    }
    /** For tests: the farm set back as an empty farm would be. */
    public static void resetNow(ServerLevel l,BlockPos b){var s=supper(b);if(s!=null){reset(l,s);SUPPERS.remove(b.asLong());}}
}
