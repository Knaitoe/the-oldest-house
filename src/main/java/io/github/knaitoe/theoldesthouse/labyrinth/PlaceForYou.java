package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.257, A Place for You, the first slice (docs/A_PLACE_FOR_YOU_0_4_257.md), the owner's proposal (docs/A_PLACE_FOR_YOU_PROPOSAL.md).
 * One private evening in Mara's house: the approach, the welcome and its one remembered thing, the place card (the source), the first
 * chair and one ordinary thing done together, the prepared bathroom and the routine laid out again, Kit's bag, Mara at the sink, dinner
 * again, and the confrontation; then the house opened (E1) or the road taken (E4). Each reader's evening is theirs (the scene is
 * copied, SceneInstances) and is kept in their record, chapter by chapter: leaving and coming back finds it where it was. Every exchange
 * that asks something opens the conversation panel (Talk); people speak above their heads too. Nothing here can hurt.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PlaceForYou {
    private PlaceForYou(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PLACE_FOR_YOU;
    /** The cast, on stranger phases: Mara, Kit, and the two strangers from the shelter in their own faces at the reader's age (Alex 67-69, Ren 70-72). */
    public static final int MARA=65,KIT=66,ALEX=67,REN=70;
    public static boolean cast(int phase){return phase>=MARA&&phase<=REN+2;}
    public static boolean alex(int phase){return phase>=ALEX&&phase<=ALEX+2;}
    public static boolean ren(int phase){return phase>=REN&&phase<=REN+2;}
    /** The evening's chapters. */
    public static final int APPROACH=0,WELCOME=1,SUPPER=2,BATHROOM=3,BAG=4,SINK=5,DINNER=6,OPEN=7,ROAD=8,DONE=9;
    static final String TAG="PlaceForYou257";
    public static final int EMPTY_RESET=200;

    /** One house's evening, in memory: its people and the timers between beats. Facts live in the reader's record. */
    static final class House {
        final BlockPos base;long tick;int empty;boolean spawned;
        @Nullable UUID mara,kit,alex,ren;
        int repeatAt=-1,bagAt=-1,swapAt=-1,sinkAt=-1,slam,radio,kitGoneAt=-1,alexGoneAt=-1,maraGoneAt=-1,sitFor,doorCool,routineSound;
        final Map<UUID,Long> said=new HashMap<>();
        House(BlockPos b){base=b;}
    }
    private static final Map<Long,House> HOUSES=new HashMap<>();
    public static void clearAll(){HOUSES.clear();}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static House house(BlockPos b){return HOUSES.get(b.asLong());}
    @Nullable public static LiteraryActor actor(ServerLevel l,BlockPos b,String who){var h=house(b);if(h==null)return null;return actor(l,switch(who){case "mara"->h.mara;case "kit"->h.kit;case "alex"->h.alex;default->h.ren;});}
    public static int chapter(ServerPlayer p){return own(p).getInt("Chapter");}

    // ------------------------------------------------------------------------------------------------ records, speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void edit(ServerPlayer p,java.util.function.Consumer<CompoundTag> change){var own=own(p);change.accept(own);save(p,own);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** A line said above someone's head, for a while. */
    static void above(House h,@Nullable LiteraryActor a,String line){if(a==null)return;a.say(line);h.said.put(a.getUUID(),a.level().getGameTime());}
    /** A line said in the reader's open exchange, and above the speaker. */
    static void line(ServerPlayer p,House h,String who,String text){Talk.line(p,who,text);above(h,actor(p.serverLevel(),h,who),text);}
    static boolean here(ServerPlayer p){return LiteraryVignettes.current(p)==PLACE&&p.isAlive();}

    // ------------------------------------------------------------------------------------------------ the cast
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    @Nullable static LiteraryActor actor(ServerLevel l,House h,String who){return actor(l,switch(who){case "Mara"->h.mara;case "Kit"->h.kit;case "Alex"->h.alex;case "Ren"->h.ren;default->null;});}
    static String who(LiteraryActor a){int phase=a.phase();return phase==MARA?"Mara":phase==KIT?"Kit":alex(phase)?"Alex":ren(phase)?"Ren":"";}
    @Nullable static LiteraryActor spawn(ServerLevel l,BlockPos b,UUID reader,int phase){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,reader);a.appearance(LiteraryActor.STRANGER,phase);a.moveTo(b.getX()+.5,b.getY(),b.getZ()-14.5,0,0);
        return l.addFreshEntity(a)?a:null;
    }
    static AABB bounds(BlockPos b){return new AABB(b.offset(-18,-3,-34).getCenter(),b.offset(18,18,0).getCenter()).inflate(1);}
    static void clearActors(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(LiteraryActor.class,bounds(b),a->a.role()==LiteraryActor.STRANGER&&cast(a.phase())))a.discard();}
    static boolean loaded(ServerLevel l,BlockPos b){for(int x=-18;x<=18;x+=16)for(int z=-34;z<=0;z+=16)if(!l.areEntitiesLoaded(ChunkPos.asLong(b.offset(x,0,z))))return false;return l.areEntitiesLoaded(ChunkPos.asLong(b.offset(18,0,-34)));}
    /** A mark: where someone should be and which way they face; absent marks have them gone. */
    record Mark(Vec3 at,float yaw,boolean present){static Mark gone(){return new Mark(Vec3.ZERO,0,false);}}
    static Mark seat(BlockPos chair){return new Mark(Vec3.atBottomCenterOf(chair),chair.getX()<5?-90:90,true);}
    static Mark stand(double x,double z,float yaw){return new Mark(new Vec3(x,0,z),yaw,true);}

    /** Where everyone is, chapter by chapter, from what the reader's record says has happened. */
    static Map<String,Mark> marks(CompoundTag own){
        var m=new HashMap<String,Mark>();int ch=own.getInt("Chapter");String home=own.getCompound("Snap").getString("Home");
        m.put("Mara",seat(PlaceForYouSite.MARA_CHAIR));m.put("Kit",seat(PlaceForYouSite.KIT_CHAIR));m.put("Alex",seat(PlaceForYouSite.ALEX_CHAIR));m.put("Ren",seat(PlaceForYouSite.REN_CHAIR));
        if(ch==APPROACH){
            m.put("Mara",home.equals("HAVEN")?stand(11.5,-19.5,90):stand(.5,-15.5,0));
            if(home.equals("DANGER"))m.put("Kit",stand(12.5,-17.5,0));
            m.put("Alex",home.equals("RUNAWAY")?stand(-5.5,-2.5,-90):new Mark(Vec3.atBottomCenterOf(PlaceForYouSite.SOFA),-90,true));
        }
        if(ch==WELCOME)m.put("Mara",own.getString("WelcomeAt").equals("kitchen")?stand(7.5,-19.5,90):stand(.5,-15.5,0));
        if(ch==SUPPER)switch(own.getString("Activity")){
            case "plates"->{if(!own.getBoolean("ActivityDone"))m.put("Mara",stand(5.5,-20.5,180));}
            case "tea"->{if(!own.getBoolean("ActivityDone"))m.put("Ren",stand(8.5,-20.5,180));}
            case "radio"->{if(!own.getBoolean("ActivityDone"))m.put("Alex",new Mark(Vec3.atBottomCenterOf(PlaceForYouSite.SOFA),-90,true));}
            default->{}
        }
        if(ch==BATHROOM){String helper=own.getString("Helper");if(helper.isEmpty())helper="Mara";
            m.put(helper,switch(own.getString("Care")){case "private"->stand(1.5,-24.5,-90);case "company"->stand(7.5,-25.5,90);default->stand(5.5,-24.5,180);});}
        if(ch==BAG){int stage=own.getInt("BagStage");
            if(stage==0)m.put("Kit",stand(1.5,-6.5,180));
            else{m.put("Mara",stand(7.5,-16.5,180));m.put("Kit",stand(2.5,-14.5,-90));}}
        if(ch==SINK){m.put("Mara",stand(5.5,-20.5,180));if(own.getBoolean("ChairTaken"))m.put("Kit",stand(1.5,-22.5,0));}
        if(ch>=DINNER&&ch<=ROAD){
            if(!own.getBoolean("SpoonTaken")&&ch==DINNER)m.put("Mara",stand(5.5,-18.5,0));
            if(own.getBoolean("ChairTaken"))m.put("Kit",stand(8.5,-14.5,90));
        }
        if(ch==OPEN||ch==DONE&&own.getString("Outcome").equals("opened_the_house")){
            if(own.getBoolean("KitLeft"))m.put("Kit",Mark.gone());else if(own.getBoolean("KitGoing"))m.put("Kit",stand(.5,-2.5,90));else m.put("Kit",stand(1.5,-6.5,0));
            if(!own.getString("AlexEnd").isEmpty()&&own.getBoolean("AlexGone"))m.put("Alex",Mark.gone());else if(own.getBoolean("Propped")&&own.getBoolean("PictureDown"))m.put("Alex",stand(2.5,-8.5,0));
            if(own.getString("MaraEnd").equals("gate"))m.put("Mara",own.getBoolean("MaraGone")?Mark.gone():stand(.5,-6.5,0));
            if(own.getString("RenEnd").equals("goes"))m.put("Ren",own.getBoolean("MaraGone")?Mark.gone():stand(-.5,-6.5,0));
        }
        if(ch==ROAD||ch==DONE&&own.getString("Outcome").equals("took_the_road")){if(own.getBoolean("KitLeft"))m.put("Kit",stand(1.5,-6.5,0));}
        return m;
    }
    /** Puts each person at their mark: straight there when the reader cannot see them, otherwise on foot. */
    static void stage(ServerLevel l,House h,ServerPlayer p,CompoundTag own){
        var b=h.base;var marks=marks(own);
        for(var who:List.of("Mara","Kit","Alex","Ren")){
            var mark=marks.get(who);var a=actor(l,h,who);
            if(!mark.present()){if(a!=null)a.discard();continue;}
            if(a==null){a=spawn(l,b,p.getUUID(),switch(who){case "Mara"->MARA;case "Kit"->KIT;case "Alex"->ALEX+Math.min(2,own.getCompound("Snap").getInt("CastAge"));default->REN+Math.min(2,own.getCompound("Snap").getInt("CastAge"));});if(a==null)continue;
                switch(who){case "Mara"->h.mara=a.getUUID();case "Kit"->h.kit=a.getUUID();case "Alex"->h.alex=a.getUUID();default->h.ren=a.getUUID();}
                a.moveTo(b.getX()+mark.at().x,b.getY()+mark.at().y,b.getZ()+mark.at().z,mark.yaw(),0);a.setYHeadRot(mark.yaw());a.setYBodyRot(mark.yaw());continue;}
            var to=new Vec3(b.getX()+mark.at().x,b.getY()+mark.at().y,b.getZ()+mark.at().z);
            if(a.position().distanceToSqr(to)<.09){a.setYRot(mark.yaw());a.setYBodyRot(mark.yaw());if(!Talk.waiting(p))a.setYHeadRot(mark.yaw());continue;}
            if(!p.hasLineOfSight(a)||a.position().distanceToSqr(to)>100||l.getBlockState(BlockPos.containing(to)).getBlock() instanceof io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock){a.moveTo(to.x,to.y,to.z,mark.yaw(),0);a.setYHeadRot(mark.yaw());a.setYBodyRot(mark.yaw());a.setDeltaMovement(Vec3.ZERO);}
            else SceneFootpaths.walk(a,to,.12);
        }
        // Nameplates fade after a while.
        for(var who:List.of("Mara","Kit","Alex","Ren")){var a=actor(l,h,who);if(a==null)continue;var at=h.said.get(a.getUUID());if(at!=null&&l.getGameTime()-at>120){a.setCustomNameVisible(false);h.said.remove(a.getUUID());}}
    }

    // ------------------------------------------------------------------------------------------------ what the House remembers
    /** The snapshot, taken once on first arrival; empty when inference is off or the quiet rooms never saw this reader. */
    static CompoundTag snapshot(ServerPlayer p){
        var t=new CompoundTag();var d=LabyrinthData.get(p.server);var id=p.getUUID();
        boolean read=HouseRead.enabled(HouseRead.Trait.AGE);t.putBoolean("Read",read);
        if(!read){t.putInt("Routine",-1);return t;}
        var seen=HouseRead.seen(d,id);
        var bath=seen.getCompound("bathroom");t.putInt("Routine",bath.contains("Used")?bath.getInt("Used"):bath.contains("Item")?bath.getInt("Item"):-1);
        t.putString("Glass",seen.getCompound("coatroom").getString("Mirror"));
        var shelter=seen.getCompound("shelter");t.putString("Stood",shelter.getString("Stood"));t.putBoolean("Danced",shelter.getBoolean("Danced"));
        t.putString("Porch",seen.getCompound("porch").getString("First"));
        var kitchen=seen.getCompound("kitchen");
        if(kitchen.getBoolean("Hung")){var dr=new CompoundTag();for(var key:List.of("Face","Hands","Hair","Clothes"))dr.putInt(key,kitchen.contains(key)?kitchen.getInt(key):-1);dr.putInt("Body",kitchen.getInt("Body"));dr.putInt("Mood",kitchen.getInt("Mood"));t.put("Drawing",dr);}
        t.putString("Age",HouseRead.value(d,id,HouseRead.Trait.AGE,true));
        t.putString("Home",HouseRead.value(d,id,HouseRead.Trait.HOME,false));
        t.putString("Drawn",HouseRead.drawnTo(p));
        t.putInt("CastAge",HouseReadShelter.age(p));
        return t;
    }
    static int glassStage(CompoundTag snap){String g=snap.getString("Glass");return g.startsWith("covered")?2:g.startsWith("turned")?1:0;}

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();
        if(own.getBoolean("Completed")){cue(p,PlaceForYouTexts.AFTER);return;}
        if(!own.contains("Snap")){
            var snap=snapshot(p);own.put("Snap",snap);own.putInt("Chapter",APPROACH);
            // A fresh evening: the house as it begins, the hall glass and the fridge as this reader left them in the quiet rooms.
            var h=HOUSES.remove(b.asLong());if(h!=null)clearActors(l,b);
            PlaceForYouSite.reset(l,b,snap.getCompound("Drawing"));PlaceForYouSite.glass(l,b,glassStage(snap));
        }
        cue(p,own.getInt("Chapter")==APPROACH?PlaceForYouTexts.arrival(own.getCompound("Snap").getString("Home")):"Mara's house, as you left it. The evening has waited.");
    }

    // ------------------------------------------------------------------------------------------------ the tick
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();var seen=new HashSet<Long>();
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||!p.isAlive()||LiteraryVignettes.current(p)!=PLACE)continue;
            var b=LiteraryVignettes.base(p,PLACE);if(b==null||!PlaceForYouSite.inside(rel(p.position(),b)))continue;
            var l=p.serverLevel();if(!loaded(l,b))continue;
            var h=HOUSES.computeIfAbsent(b.asLong(),k->new House(b));seen.add(b.asLong());
            var own=own(p);if(!own.contains("Snap"))continue;
            if(!h.spawned){clearActors(l,b);h.spawned=true;}
            h.tick++;
            reader(l,h,p,own);
            if(h.tick%5==0)stage(l,h,p,own(p));
        }
        for(var it=HOUSES.values().iterator();it.hasNext();){var h=it.next();if(seen.contains(h.base.asLong())){h.empty=0;continue;}
            if(++h.empty>=EMPTY_RESET){var l=server.getLevel(NovelRooms.dimension(PLACE));if(l!=null&&l.isLoaded(h.base))clearActors(l,h.base);it.remove();}}
        // Anything of the household's carried out of the yard goes back where it belongs.
        if(server.getTickCount()%20==0)for(var p:server.getPlayerList().getPlayers()){
            var b=LiteraryVignettes.current(p)==PLACE?LiteraryVignettes.base(p,PLACE):null;
            boolean in=b!=null&&p.isAlive()&&PlaceForYouSite.inYard(rel(p.position(),b));
            if(!in&&holding(p)!=0)release(p,b);
        }
    }

    static void reader(ServerLevel l,House h,ServerPlayer p,CompoundTag own){
        var b=h.base;var r=rel(p.position(),b);int ch=own.getInt("Chapter");
        switch(ch){
            case APPROACH->{
                if(PlaceForYouSite.inHouse(r)){own.putInt("Chapter",WELCOME);own.putString("WelcomeAt",PlaceForYouSite.inKitchen(r)?"kitchen":"hall");save(p,own);stage(l,h,p,own);}
                else if(h.tick%100==0){var home=own.getCompound("Snap").getString("Home");
                    if(home.equals("RUNAWAY"))above(h,actor(l,h,"Alex"),"Hang on a second. You don't have to go in.");
                    else if(home.equals("DANGER"))above(h,actor(l,h,"Kit"),"Gate sticks. I'll leave it open.");
                    else if(home.equals("HAVEN"))above(h,actor(l,h,"Mara"),"There you are. Come in, it's getting cold.");}
            }
            case WELCOME->{var m=actor(l,h,"Mara");if(m!=null&&p.distanceToSqr(m)<16&&!Talk.waiting(p)&&!own.getBoolean("WelcomeOpened")){own.putBoolean("WelcomeOpened",true);save(p,own);welcome(p,h);}}
            case SUPPER->supper(l,h,p,own,r);
            case BATHROOM->bathroom(l,h,p,own,r);
            case BAG->bag(l,h,p,own,r);
            case SINK->{if(h.sinkAt<0)h.sinkAt=(int)h.tick;if(h.tick-h.sinkAt>1800&&!Talk.waiting(p)){own.putInt("Chapter",DINNER);save(p,own);startDinner(l,h,p);}}
            case DINNER->dinner(l,h,p,own,r);
            case OPEN->open(l,h,p,own,r);
            case ROAD->{if(PlaceForYouSite.onRoad(r)){finish(p,"took_the_road");}}
            default->{}
        }
        // Kit, once going, is gone from the road when the reader isn't watching (or after half a minute), in whatever chapter the evening is in.
        own=own(p);
        if(own.getBoolean("KitGoing")&&!own.getBoolean("KitLeft")){if(h.kitGoneAt<0)h.kitGoneAt=(int)h.tick;var kit=actor(l,h,"Kit");
            if(h.tick-h.kitGoneAt>200&&(kit==null||!p.hasLineOfSight(kit)||h.tick-h.kitGoneAt>600)){own.putBoolean("KitLeft",true);save(p,own);}}
        // Kit's place goes while the reader watches it go.
        if(own.getBoolean("Delayed")&&!own.getBoolean("DoorTaken")&&ch>=BAG&&looking(p,b,PlaceForYouSite.KIT_DOOR,10)){
            PlaceForYouSite.kitsDoor(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.KIT_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.6F,.7F);
            cue(p,"Kit's door is a cupboard door. Painted shut, shelves behind it. As if it always was.");own.putBoolean("DoorTaken",true);save(p,own);}
        if(own.getBoolean("Delayed")&&!own.getBoolean("ChairTaken")&&own.getBoolean("DoorTaken")&&!PlaceForYouSite.inKitchen(r)){PlaceForYouSite.kitsChair(l,b,false);own.putBoolean("ChairTaken",true);save(p,own);}
        // The road taken once the house's trouble has been seen: asked once each time the reader gets to its end.
        if(own.getBoolean("Acknowledged")&&ch>=BAG&&ch<=DINNER&&!own.getBoolean("Ready")){
            if(PlaceForYouSite.roadEnd(r)){if(!own.getBoolean("RoadAsked")&&!Talk.waiting(p)){own.putBoolean("RoadAsked",true);save(p,own);roadAsk(p,h);}}
            else if(own.getBoolean("RoadAsked")&&!PlaceForYouSite.onRoad(r)){own.remove("RoadAsked");save(p,own);}
        }
    }
    /** Whether the reader is looking at a spot (within a cone, near enough, with nothing in between). */
    public static boolean looking(ServerPlayer p,BlockPos b,BlockPos at,double range){
        var to=Vec3.atCenterOf(b.offset(at)).subtract(p.getEyePosition());if(to.lengthSqr()>range*range)return false;
        return p.getViewVector(1).dot(to.normalize())>.82&&p.level().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),Vec3.atCenterOf(b.offset(at)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getBlockPos().closerThan(b.offset(at),1.5);
    }

    // ------------------------------------------------------------------------------------------------ the welcome
    static void welcome(ServerPlayer p,House h){
        var own=own(p);var snap=own.getCompound("Snap");String detail=PlaceForYouTexts.detail(snap);
        Talk.begin(p,"welcome","Mara");line(p,h,"Mara","You came.");line(p,h,"Mara",PlaceForYouTexts.detailLine(detail,snap));line(p,h,"Mara",PlaceForYouTexts.offer(snap.getString("Age")));
        edit(p,o->o.putString("Detail",detail));
        Talk.ask(p,
            Talk.reply("It's good to be back.",q->claim(q,h,"family","It is. It is good."))
            ,Talk.reply("I don't think I've been here before.",q->claim(q,h,"guest","No. No, of course not. I'm sorry. I don't know why I said it like that."))
            ,Talk.reply("Thank you. I'll stay for supper.",q->claim(q,h,"hosp","Supper, then. That's something.")));
    }
    static void claim(ServerPlayer p,House h,String claim,String answer){
        line(p,h,"Mara",answer);line(p,h,"Mara","Your place is set. Kit's in the kitchen pretending not to be hungry.");
        edit(p,o->{o.putString("Claim",claim);o.putInt("Chapter",SUPPER);});Talk.end(p);
        cue(p,"There's a card at your place at the table, folded once.");
    }

    // ------------------------------------------------------------------------------------------------ the first chair, and something ordinary together
    static void supper(ServerLevel l,House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;
        // The first familiar gesture: who it comes from follows a settled read, or nobody's in particular.
        if(!own.getBoolean("Gesture")&&PlaceForYouSite.inKitchen(r)){own.putBoolean("Gesture",true);save(p,own);
            switch(own.getCompound("Snap").getString("Drawn")){
                case "MEN"->above(h,actor(l,h,"Alex"),"Saved you the seat by me.");
                case "WOMEN"->above(h,actor(l,h,"Ren"),"There's room this side, if you'd rather.");
                case "BOTH"->{above(h,actor(l,h,"Alex"),"Saved you the seat by me.");above(h,actor(l,h,"Ren"),"Or this side. Your call.");}
                default->above(h,actor(l,h,"Ren"),"Evening.");
            }}
        String activity=own.getString("Activity");
        if(activity.equals("radio")&&!own.getBoolean("ActivityDone")){
            boolean sitting=p.getVehicle() instanceof SeatEntity&&PlaceForYouSite.inSitting(r);
            if(sitting){h.radio++;var a=actor(l,h,"Alex");
                if(h.radio%60==1)l.playSound(null,b.offset(PlaceForYouSite.RADIO),SoundEvents.NOTE_BLOCK_HARP.value(),SoundSource.BLOCKS,.35F,.7F+(h.radio%7)*.08F);
                if(h.radio==80)above(h,a,"This station only plays songs from before. I've stopped asking before what.");
                if(h.radio==220)above(h,a,"Ren hums along. Don't tell Ren I told you.");
                if(h.radio==360)above(h,a,"I keep thinking I'll go in the morning. Then it's evening again.");
                if(h.radio>=440)activityDone(p,h,"Alex","It's nice, though. Isn't it. That's the trouble.");}
        }
        if(own.getBoolean("ActivityDone")&&!Talk.waiting(p)){own.putInt("Chapter",BATHROOM);save(p,own);startBathroom(l,h,p);}
    }
    static void activityDone(ServerPlayer p,House h,String who,String said){
        above(h,actor(p.serverLevel(),h,who),said);edit(p,o->{o.putBoolean("ActivityDone",true);if(o.getString("Helper").isEmpty())o.putString("Helper",who);});
    }
    /** The first conversation with each person, once: an ordinary exchange, ending in what they'd like to do with the reader. */
    static void first(ServerPlayer p,House h,String who){
        var own=own(p);var snap=own.getCompound("Snap");boolean offered=!own.getString("Activity").isEmpty();
        Talk.begin(p,"first_"+who,who);
        edit(p,o->{o.putBoolean("Talked_"+who,true);if(o.getString("First").isEmpty())o.putString("First",who);});
        var offer=new ArrayList<Talk.Reply>();
        switch(who){
            case "Mara"->{line(p,h,"Mara","Would you pass me the— no, the other one. Thank you.");line(p,h,"Mara","That's Theo's chair. You can sit in it, if you like. It's only a chair.");
                Talk.ask(p,Talk.reply("Who's Theo?",q->{line(q,h,"Mara","My brother. He'd have liked you. He liked everyone; that was his trouble.");edit(q,o->o.putBoolean("TheoNamed",true));offerActivity(q,h,"Mara");})
                    ,Talk.reply("I'll leave it for him.",q->{line(q,h,"Mara","...Thank you.");offerActivity(q,h,"Mara");})
                    ,Talk.reply("It's a nice kitchen.",q->{line(q,h,"Mara","It's the same kitchen. That's what I like about it.");offerActivity(q,h,"Mara");}));}
            case "Kit"->{line(p,h,"Kit","Are you any good with catches? There's one on my bag that's been done for since—");line(p,h,"Kit","Since a while.");
                Talk.ask(p,Talk.reply("I can look at it.",q->{line(q,h,"Kit","After. Not in front of her.");offerActivity(q,h,"Kit");})
                    ,Talk.reply("Going somewhere?",q->{line(q,h,"Kit","Trying to.");offerActivity(q,h,"Kit");})
                    ,Talk.reply("Not really, no.",q->{line(q,h,"Kit","Me neither, apparently.");offerActivity(q,h,"Kit");}));}
            case "Alex"->{String stood=snap.getString("Stood");boolean met=snap.getBoolean("Read")&&(stood.equals("man")||stood.equals("middle"));
                line(p,h,"Alex",met?"You were under the eaves. In the rain. I remember that.":"Have we met? You look like you've been out in weather.");
                var replies=new ArrayList<Talk.Reply>();
                if(met)replies.add(Talk.reply("I remember.",q->{line(q,h,"Alex","Good. I wasn't sure if I'd made it up.");offerActivity(q,h,"Alex");}));
                replies.add(Talk.reply("I don't think so.",q->{line(q,h,"Alex","No. Probably not. She has a way of making everyone feel like an old friend.");offerActivity(q,h,"Alex");}));
                replies.add(Talk.reply("What are you doing here?",q->{line(q,h,"Alex","Waiting for the morning, same as everyone. I'm told it comes.");offerActivity(q,h,"Alex");}));
                Talk.ask(p,replies);}
            default->{String stood=snap.getString("Stood");boolean met=snap.getBoolean("Read")&&(stood.equals("woman")||stood.equals("middle"));
                line(p,h,"Ren",met?"You stood with me under the eaves. I didn't say much. I'm still not saying much.":"Is that chair all right? The back one wobbles.");
                line(p,h,"Ren","You don't have to stay, you know. If it's too much. Nobody says that here, so I'm saying it.");
                Talk.ask(p,Talk.reply("Thanks. I'm all right.",q->{line(q,h,"Ren","Good.");offerActivity(q,h,"Ren");})
                    ,Talk.reply("Too much how?",q->{line(q,h,"Ren","You'll see. Or you won't, and that'd be nice.");offerActivity(q,h,"Ren");})
                    ,Talk.reply("Do you want to stay?",q->{line(q,h,"Ren","I want to stay near her. It isn't quite the same thing.");offerActivity(q,h,"Ren");}));}
        }
    }
    static void offerActivity(ServerPlayer p,House h,String who){
        if(!own(p).getString("Activity").isEmpty()){Talk.end(p);return;}
        String ask=switch(who){case "Mara"->"Help me clear these, would you? Before Kit eats off everyone's plate.";case "Kit"->"Look at the clasp now, then. She's not watching.";case "Alex"->"Come and sit by the radio a while? It's quieter.";default->"Help me make the tea? I can never find anything.";};
        line(p,h,who,ask);
        Talk.ask(p,Talk.reply("Of course.",q->{String activity=switch(who){case "Mara"->"plates";case "Kit"->"clasp";case "Alex"->"radio";default->"tea";};
                edit(q,o->{o.putString("Activity",activity);o.putString("Helper",who);});startActivity(q,h,activity);Talk.end(q);})
            ,Talk.reply("In a minute.",q->{line(q,h,who,"Whenever you like.");Talk.end(q);}));
    }
    static void startActivity(ServerPlayer p,House h,String activity){
        var l=p.serverLevel();var b=h.base;
        switch(activity){
            case "plates"->cue(p,"Take the plates to the sink: Kit's, Ren's and Alex's.");
            case "clasp"->{PlaceForYouSite.bag(l,b,"kitchen");cue(p,"Kit's bag is on the floor by Kit's chair. The clasp won't sit.");}
            case "tea"->cue(p,"The kettle's on the stove. The cups are at the end of the counter.");
            case "radio"->cue(p,"Sit down in the sitting room. Alex has the radio on.");
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ the prepared bathroom
    static void startBathroom(ServerLevel l,House h,ServerPlayer p){
        var own=own(p);var b=h.base;String helper=own.getString("Helper");if(helper.isEmpty()){helper="Mara";String fixed=helper;edit(p,o->o.putString("Helper",fixed));}
        int item=own.getCompound("Snap").getInt("Routine");PlaceForYouSite.routine(l,b,item<0?11:item);
        above(h,actor(l,h,helper),"The bathroom's ready for you. I've put your things out.");
        cue(p,"The bathroom's at the back of the hall, on the right.");
    }
    static void bathroom(ServerLevel l,House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;String helper=own.getString("Helper");
        if(PlaceForYouSite.inBathroom(r)&&own.getString("Care").isEmpty()&&!Talk.waiting(p)&&!own.getBoolean("CareOpened")){
            own.putBoolean("CareOpened",true);save(p,own);
            Talk.begin(p,"care",helper);line(p,h,helper,"There. "+(own.getCompound("Snap").getInt("Routine")>=0?"Your "+PlaceForYouTexts.routine(own.getCompound("Snap").getInt("Routine"))+", where you like it.":"A clean washcloth."));
            line(p,h,helper,"Do you want a hand with it? Or shall I just keep you company. Or—");
            Talk.ask(p,Talk.reply("Would you help me?",q->care(q,h,"help","All right. Show me how you do it, and I'll do it the same."))
                ,Talk.reply("Sit with me while I do it?",q->care(q,h,"company","I'll sit on the edge here. I'll talk about the weather."))
                ,Talk.reply("I'd like to do this on my own.",q->care(q,h,"private","Of course. I'll be just outside.")));
        }
        if(own.getBoolean("RoutineUsed")&&!own.getBoolean("Repeated")&&h.repeatAt>0&&h.tick>=h.repeatAt){
            int item=own.getCompound("Snap").getInt("Routine");PlaceForYouSite.routine(l,b,item<0?11:item);
            l.playSound(null,b.offset(PlaceForYouSite.ROUTINE),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.4F,1.6F);
            own.putBoolean("Repeated",true);save(p,own);
            if(PlaceForYouSite.inBathroom(r))cue(p,"It's out on the counter again. Laid out exactly as before, ready for the same moment.");
            if(!own.getString("Care").equals("private")){var a=actor(l,h,helper);above(h,a,"I didn't move it. I was watching you.");}
        }
        if(own.getBoolean("Repeated")&&!PlaceForYouSite.inBathroom(r)&&!Talk.waiting(p)){own.putInt("Chapter",BAG);own.putInt("BagStage",0);save(p,own);PlaceForYouSite.bag(l,b,"gate");h.bagAt=(int)h.tick;cue(p,"Through the front windows: Kit, at the gate, with a packed bag.");}
    }
    static void care(ServerPlayer p,House h,String care,String answer){
        String helper=own(p).getString("Helper");line(p,h,helper,answer);edit(p,o->o.putString("Care",care));Talk.end(p);
        if(care.equals("private")){var l=p.serverLevel();var at=h.base.offset(PlaceForYouSite.BATH_DOOR);var s=l.getBlockState(at);if(s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock d)d.setOpen(null,l,s,at,false);}
    }

    // ------------------------------------------------------------------------------------------------ the bag
    static void bag(ServerLevel l,House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;int stage=own.getInt("BagStage");
        if(h.bagAt<0)h.bagAt=(int)h.tick;
        if(stage==0){
            var bag=Vec3.atCenterOf(b.offset(PlaceForYouSite.BAG_GATE));
            if(p.getEyePosition().distanceToSqr(bag)<400&&looking(p,b,PlaceForYouSite.BAG_GATE,20)||h.tick-h.bagAt>600){own.putBoolean("BagSeen",true);own.putInt("BagStage",1);save(p,own);h.swapAt=(int)h.tick;above(h,actor(l,h,"Kit"),"Just putting it out. In case.");}
        }else if(stage==1){
            // Mara brings it back in while the reader is indoors and not looking at the gate.
            if(h.tick-h.swapAt>100&&PlaceForYouSite.inHouse(r)&&!looking(p,b,PlaceForYouSite.BAG_GATE,40)){PlaceForYouSite.bag(l,b,"kitchen");own.putInt("BagStage",2);save(p,own);
                l.playSound(null,b.offset(PlaceForYouSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.6F,1F);cue(p,"The front door goes. Footsteps into the kitchen, and something set down.");}
        }else if(stage==2){
            if(PlaceForYouSite.inKitchen(r)&&!Talk.waiting(p)&&!own.getBoolean("BagOpened")){own.putBoolean("BagOpened",true);save(p,own);bagExchange(p,h,own);}
        }else if(stage>=3&&!Talk.waiting(p)&&holding(p)==0){own.putInt("Chapter",SINK);save(p,own);above(h,actor(l,h,"Mara"),"I'll just do these.");}
    }
    static void bagExchange(ServerPlayer p,House h,CompoundTag own){
        Talk.begin(p,"bag","Mara and Kit");
        line(p,h,"Mara","There were things loose in the bottom. I thought you meant to sort it.");
        line(p,h,"Kit",own.getString("Activity").equals("clasp")?"The clasp's fine now. Someone fixed it.":"I wrapped the letter round the clasp. It catches on the lining.");
        line(p,h,"Mara","You could have said.");line(p,h,"Kit","I did. Yesterday.");
        bagReplies(p,h);
    }
    static void bagReplies(ServerPlayer p,House h){
        var own=own(p);var replies=new ArrayList<Talk.Reply>();
        if(!own.getBoolean("AskedPut"))replies.add(Talk.reply("Mara, put it where Kit left it.",q->{line(q,h,"Mara","It's going to get damp out there.");line(q,h,"Kit","I'll carry it.");line(q,h,"Mara","I know you can carry it.");
            above(h,actor(q.serverLevel(),h,"Mara"),"...");edit(q,o->{o.putBoolean("AskedPut",true);o.putBoolean("BagFree",true);});bagSettled(q,h);}));
        if(!own.getBoolean("KitPlan"))replies.add(Talk.reply("Kit, where were you planning to go?",q->{line(q,h,"Kit","There's a room above the workshop. The letter says I can have it until the course starts.");
            line(q,h,"Mara","That letter could be from any year.");line(q,h,"Kit","Then I'll get there and ask.");edit(q,o->o.putBoolean("KitPlan",true));bagReplies(q,h);}));
        replies.add(Talk.reply("Could this wait until morning?",q->{line(q,h,"Mara","Yes. That's all I'm saying.");line(q,h,"Kit","...Sure. Morning.");
            above(h,actor(q.serverLevel(),h,"Kit"),"Sure. Morning.");edit(q,o->{o.putBoolean("AskedWait",true);o.putBoolean("Delayed",true);});bagSettled(q,h);}));
        replies.add(Talk.reply("I shouldn't be deciding whether somebody else leaves.",q->{line(q,h,"Kit","You're not. I was going to ask you to hold the door.");
            edit(q,o->{o.putBoolean("NotMine",true);o.putBoolean("BagFree",true);});bagSettled(q,h);}));
        Talk.ask(p,replies);
    }
    static void bagSettled(ServerPlayer p,House h){
        edit(p,o->{o.putBoolean("Acknowledged",true);o.putInt("BagStage",3);});
        Talk.end(p);
        if(own(p).getBoolean("BagFree"))cue(p,"Kit's bag is by Kit's chair. Nobody is stopping you picking it up.");
    }

    // ------------------------------------------------------------------------------------------------ Mara at the sink
    static void sink(ServerPlayer p,House h){
        Talk.begin(p,"sink","Mara");
        line(p,h,"Mara","Theo used to dry. I'd wash, he'd dry, and he'd put everything in the wrong cupboard. Every night. On purpose, I think.");
        line(p,h,"Mara","His keys are still in the drawer with the tea towels. I keep meaning to.");
        line(p,h,"Mara","I don't want another conversation in an empty kitchen.");
        Talk.ask(p,Talk.reply("I'll sit with you tonight.",q->{line(q,h,"Mara","Tonight. All right. I'll hold you to that.");sunk(q,h,"company");})
            ,Talk.reply("I'm listening.",q->{line(q,h,"Mara","That's more than most.");sunk(q,h,"listen");})
            ,Talk.reply("What does Kit want?",q->{line(q,h,"Mara","Kit wants to be somewhere else. Kit's always wanted to be somewhere else.");line(q,h,"Mara","...I know what Kit wants.");sunk(q,h,"kit");}));
    }
    static void sunk(ServerPlayer p,House h,String promise){
        edit(p,o->{o.putBoolean("TheoTold",true);o.putString("Promise",promise);o.putInt("Chapter",DINNER);});Talk.end(p);startDinner(p.serverLevel(),h,p);
    }

    // ------------------------------------------------------------------------------------------------ dinner again
    static void startDinner(ServerLevel l,House h,ServerPlayer p){
        var b=h.base;var own=own(p);
        PlaceForYouSite.table(l,b,true);PlaceForYouSite.spoon(l,b,false);
        if(own.getString("Activity").equals("plates"))cue(p,"The plates you cleared are back on the table, exactly where they were, with the same supper on them.");
        else cue(p,"Mara is laying the table again.");
        above(h,actor(l,h,"Mara"),"I'll just— there's one more—");
    }
    static void dinner(ServerLevel l,House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;
        if(!own.getBoolean("SpoonTaken")&&h.tick%120==0)above(h,actor(l,h,"Mara"),"There's one more— I'll just—");
        // The way out narrows for a reader who helped Kit: the front door swings shut ahead of them until they hold it.
        boolean helped=own.getString("BagWhere").equals("gate")||own.getString("BagWhere").equals("kit");
        if(helped&&!own.getBoolean("DoorHeld")&&PlaceForYouSite.doorOpen(l,b)&&p.position().distanceToSqr(Vec3.atCenterOf(b.offset(PlaceForYouSite.FRONT_DOOR)))<20&&h.doorCool<=0){
            PlaceForYouSite.frontDoor(l,b,false);l.playSound(null,b.offset(PlaceForYouSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,1F,.8F);h.doorCool=30;h.slam++;
            if(h.slam==1)cue(p,"The front door swings shut ahead of you. Open it again and hold it.");}
        if(h.doorCool>0)h.doorCool--;
        // A reader who asked for privacy: the routine goes on in the empty bathroom without them.
        if(own.getString("Care").equals("private")&&!own.getBoolean("RoutineStopped")){
            if(!PlaceForYouSite.routineOut(l,b)){int item=own.getCompound("Snap").getInt("Routine");PlaceForYouSite.routine(l,b,item<0?11:item);}
            if(h.tick%50==0)l.playSound(null,b.offset(PlaceForYouSite.ROUTINE),SoundEvents.BRUSH_GENERIC,SoundSource.BLOCKS,.5F,1F);
            if(h.tick%600==300&&!PlaceForYouSite.inBathroom(r))cue(p,"From the bathroom: the sound of your routine, going on without you.");}
        // Behind Kit's cupboard, someone saying the reader's name.
        if(own.getBoolean("DoorTaken")&&!own.getBoolean("CupboardOpened")&&h.tick%160==0&&p.position().distanceToSqr(Vec3.atCenterOf(b.offset(PlaceForYouSite.KIT_DOOR)))<64)
            cue(p,"Behind Kit's cupboard, someone is saying your name.");
    }
    static void spoon(ServerPlayer p,House h){
        Talk.begin(p,"spoon","Mara");line(p,h,"Mara","Take this off me. Would you? I can't seem to put it down.");
        Talk.ask(p,Talk.reply("Take the spoon.",q->{PlaceForYouSite.spoon(q.serverLevel(),h.base,true);edit(q,o->o.putBoolean("SpoonTaken",true));line(q,h,"Mara","Thank you. Thank you. Sit down, all of you, it's getting cold.");Talk.end(q);
                if(own(q).getString("Promise").equals("company"))edit(q,o->o.putBoolean("PromiseKept",true));})
            ,Talk.reply("Leave it.",q->{line(q,h,"Mara","There's one more. There's always one more.");Talk.end(q);}));
    }
    static void confront(ServerPlayer p,House h){
        var own=own(p);Talk.begin(p,"confront","At the table");line(p,h,"Mara","Sit down, all of you. It's getting cold.");
        Talk.ask(p,Talk.reply("I'd like to say this to everyone.",q->{
                var o=own(q);var said=new ArrayList<String>();
                if(o.getBoolean("BagSeen"))said.add("The bag keeps coming back in.");if(o.getBoolean("DoorTaken"))said.add("Kit's door isn't a door any more.");
                said.add("The table's laid the same as before.");if(o.getBoolean("Repeated"))said.add("The things you put out for me put themselves back out.");
                Talk.line(q,"",String.join(" ",said));line(q,h,"Kit","Thank you.");
                line(q,h,"Mara","I only wanted one more evening like this one. That's all it's ever been. One more.");line(q,h,"Ren","It's been a lot more than one.");line(q,h,"Alex","I'd like to see a morning. I don't mind whose.");
                line(q,h,"Mara","What would you have me do?");
                Talk.ask(q,Talk.reply("Let them go. Let yourself go, too.",z->confronted(z,h,"open")),Talk.reply("Let Kit go. The rest of you can decide for yourselves.",z->confronted(z,h,"open")));})
            ,Talk.reply("Mara, can we talk in the other room?",q->{line(q,h,"Mara","...All right.");
                line(q,h,"Mara","You've seen it, then. I'm not keeping anyone. I'm keeping the evening. They're in it.");
                Talk.ask(q,Talk.reply("They want to leave. You can let them.",z->{line(z,h,"Mara","Would you open the door for me? I don't think I can be the one.");confronted(z,h,"private");})
                    ,Talk.reply("You could leave too.",z->{line(z,h,"Mara","Would you open the door for me? I don't think I can be the one.");confronted(z,h,"private");}));})
            ,Talk.reply("Kit, go now. I'll keep them talking.",q->{line(q,h,"Kit","Now? Now. Right.");line(q,h,"Mara","Was that— Kit? Was that the door?");confronted(q,h,"kit");})
            ,Talk.reply("This is more than I can take on. I'm going to go.",q->{line(q,h,"Mara","Oh. Yes. Of course you are.");line(q,h,"Kit","Hold the gate on your way out?");
                edit(q,o->{o.putString("Confront","road");o.putInt("Chapter",ROAD);});Talk.end(q);cue(q,"The gate, and the road.");}));
    }
    static void confronted(ServerPlayer p,House h,String how){
        edit(p,o->{o.putString("Confront",how);o.putInt("Chapter",OPEN);});Talk.end(p);
        cue(p,"Make the way out stay open, and take your picture down from where they put it.");
        kitGoes(p,h);
    }

    // ------------------------------------------------------------------------------------------------ opening the house
    static void open(ServerLevel l,House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;
        // Kit goes as soon as the house is being opened, out through the gate with the bag; gone from the road once nobody is watching.
        if(!own.getBoolean("KitGoing")){kitGoes(p,h);own=own(p);}
        boolean ready=own.getBoolean("Propped")&&own.getBoolean("PictureDown");
        if(ready&&own.getString("MaraEnd").isEmpty()&&!Talk.waiting(p)&&PlaceForYouSite.inKitchen(r)&&!own.getBoolean("MaraAsked")){own.putBoolean("MaraAsked",true);save(p,own);maraDecides(p,h);}
        if(own.getString("MaraEnd").equals("sat")&&!own.getBoolean("SatDone")){
            if(p.getVehicle() instanceof SeatEntity&&PlaceForYouSite.inKitchen(r)){if(++h.sitFor>=160){own.putBoolean("SatDone",true);own.putBoolean("PromiseKept",true);save(p,own);above(h,actor(l,h,"Mara"),"I'll stay a while. Not because I have to. I'll try it, and see.");}}
        }
        if(own.getString("MaraEnd").equals("gate")&&!own.getBoolean("MaraGone")){if(h.maraGoneAt<0)h.maraGoneAt=(int)h.tick;if(h.tick-h.maraGoneAt>200){own.putBoolean("MaraGone",true);own.putBoolean("LightOff",true);save(p,own);PlaceForYouSite.kitchenLight(l,b,false);}}
        if(ready&&own.getString("AlexEnd").isEmpty()&&!Talk.waiting(p)&&!own.getBoolean("AlexAsked")){var a=actor(l,h,"Alex");if(a!=null&&p.distanceToSqr(a)<12){own.putBoolean("AlexAsked",true);save(p,own);alexGoes(p,h);}}
        if(!own.getString("AlexEnd").isEmpty()&&!own.getBoolean("AlexGone")){if(h.alexGoneAt<0)h.alexGoneAt=(int)h.tick;if(h.tick-h.alexGoneAt>160){own.putBoolean("AlexGone",true);save(p,own);}}
        own=own(p);
        boolean maraDone=own.getString("MaraEnd").equals("stays")||own.getString("MaraEnd").equals("gate")&&own.getBoolean("MaraGone")||own.getBoolean("SatDone");
        if(ready&&maraDone&&!own.getString("AlexEnd").isEmpty()&&!own.getBoolean("Ready"))finish(p,"opened_the_house");
    }
    /** Kit picks the bag up and goes: the front door and the gate left open behind, a line over the shoulder. */
    static void kitGoes(ServerPlayer p,House h){
        var own=own(p);if(own.getBoolean("KitGoing"))return;var l=p.serverLevel();var b=h.base;
        edit(p,o->o.putBoolean("KitGoing",true));h.kitGoneAt=(int)h.tick;PlaceForYouSite.bag(l,b,"none");PlaceForYouSite.frontDoor(l,b,true);PlaceForYouSite.gate(l,b,true);
        above(h,actor(l,h,"Kit"),"I'll write. I probably won't. But I'll mean to.");
    }
    static void maraDecides(ServerPlayer p,House h){
        var own=own(p);Talk.begin(p,"mara_end","Mara");
        if(own.getString("Confront").equals("private")){line(p,h,"Mara","I'll see Kit to the gate. Just to the gate.");edit(p,o->{o.putString("MaraEnd","gate");o.putString("RenEnd","goes");});line(p,h,"Ren","I'll come too. As far as the bend.");Talk.end(p);return;}
        if(own.getString("Promise").equals("company")){line(p,h,"Mara","Will you sit with me a minute? Before I decide.");
            Talk.ask(p,Talk.reply("Yes. I said I would.",q->{edit(q,o->{o.putString("MaraEnd","sat");o.putString("RenEnd","stays");});line(q,h,"Mara","Sit, then. Anywhere. Not that one.");Talk.end(q);cue(q,"Sit down at the table with her.");})
                ,Talk.reply("I can't. I'm sorry.",q->{edit(q,o->{o.putString("MaraEnd","stays");o.putString("RenEnd","stays");});line(q,h,"Mara","No. That's all right. It's still my kitchen.");Talk.end(q);}));return;}
        line(p,h,"Mara","I'll stay. For now. It's still my kitchen.");line(p,h,"Ren","I'll stay with her.");edit(p,o->{o.putString("MaraEnd","stays");o.putString("RenEnd","stays");});Talk.end(p);
    }
    static void alexGoes(ServerPlayer p,House h){
        Talk.begin(p,"alex_end","Alex");line(p,h,"Alex","There's a town on the coast the bus still goes to, supposedly. I'm going to see if it's true.");line(p,h,"Alex","Walk a while with me? You don't have to.");
        Talk.ask(p,Talk.reply("As far as the road.",q->{edit(q,o->o.putString("AlexEnd","walk"));line(q,h,"Alex","As far as the road, then.");Talk.end(q);})
            ,Talk.reply("No. Good luck.",q->{edit(q,o->o.putString("AlexEnd","alone"));line(q,h,"Alex","That's a whole answer. Thanks for it.");Talk.end(q);}));
    }
    static void roadAsk(ServerPlayer p,House h){
        Talk.begin(p,"road","The road");Talk.line(p,"","The road goes on into the trees both ways. Behind you, the house, and the people in it.");
        Talk.ask(p,Talk.reply("Keep walking.",q->{edit(q,o->o.putString("Confront","road"));finish(q,"took_the_road");Talk.end(q);})
            ,Talk.reply("Go back.",q->Talk.end(q)));
    }
    /** Readies the account in the mailbox by the gate. */
    static void finish(ServerPlayer p,String outcome){
        var own=own(p);if(own.getBoolean("Ready"))return;own.putInt("Chapter",DONE);LiteraryVignettes.ready(p,PLACE,own,outcome);save(p,own);
        cue(p,PlaceForYouTexts.ended(outcome,own));
    }

    // ------------------------------------------------------------------------------------------------ talking
    public static void talk(ServerPlayer p,LiteraryActor a){
        var own=own(p);if(!own.contains("Snap")||own.getBoolean("Completed"))return;var b=LiteraryVignettes.base(p,PLACE);var h=b==null?null:house(b);if(h==null)return;
        String who=who(a);if(who.isEmpty())return;int ch=own.getInt("Chapter");
        int held=holding(p);
        if(held==PlaceForYouRegistry.BAG&&(who.equals("Kit")||who.equals("Mara"))){giveBag(p,h,who);return;}
        if(Talk.resume(p,"first_"+who)||Talk.resume(p,Talk.key(p)))return;
        switch(ch){
            case APPROACH->{if(who.equals("Alex"))Talk.begin(p,"alex_road","Alex");else if(who.equals("Kit"))Talk.begin(p,"kit_porch","Kit");else{above(h,a,"Come in, come in.");return;}
                if(who.equals("Alex")){line(p,h,"Alex","You don't have to go in. I'm only saying it because nobody said it to me.");
                    Talk.ask(p,Talk.reply("Who lives here?",q->{line(q,h,"Alex","Mara. Kit. Ren, mostly. Me, tonight. She's been expecting you.");Talk.end(q);}),Talk.reply("I'm going in.",q->{line(q,h,"Alex","Then I'll come in too.");Talk.end(q);}),Talk.reply("I'll keep walking.",q->{line(q,h,"Alex","That's allowed.");Talk.end(q);}));}
                else{line(p,h,"Kit","If you want to go, the gate's open. I check it. Every time.");Talk.ask(p,Talk.reply("Thanks.",q->Talk.end(q)),Talk.reply("Why do you check it?",q->{line(q,h,"Kit","Habit.");Talk.end(q);}));}}
            case WELCOME->{if(who.equals("Mara")&&!own.getBoolean("WelcomeOpened")){edit(p,o->o.putBoolean("WelcomeOpened",true));welcome(p,h);}else above(h,a,"Go on, she's waiting for you.");}
            case SUPPER->{if(!own.getBoolean("Talked_"+who))first(p,h,who);
                else if(own.getString("Activity").isEmpty()){Talk.begin(p,"offer_"+who,who);offerActivity(p,h,who);}
                else above(h,a,switch(who){case "Mara"->"Eat something.";case "Kit"->"Don't mind me.";case "Alex"->"Still here.";default->"Mm?";});}
            case BAG->{if(own.getInt("BagStage")==2&&!own.getBoolean("BagOpened")){edit(p,o->o.putBoolean("BagOpened",true));bagExchange(p,h,own);}else above(h,a,who.equals("Kit")?"It's my bag.":"Not now.");}
            case SINK->{if(who.equals("Mara"))sink(p,h);else above(h,a,who.equals("Kit")?"She'll be at that sink a while.":"Go and talk to her.");}
            case DINNER->{if(who.equals("Mara")&&!own.getBoolean("SpoonTaken"))spoon(p,h);else if(own.getBoolean("SpoonTaken"))confront(p,h);else above(h,a,"Sit down.");}
            case OPEN->{if(who.equals("Mara")&&own.getBoolean("Propped")&&own.getBoolean("PictureDown")&&own.getString("MaraEnd").isEmpty()){edit(p,o->o.putBoolean("MaraAsked",true));maraDecides(p,h);}
                else if(who.equals("Alex")&&own.getBoolean("Propped")&&own.getBoolean("PictureDown")&&own.getString("AlexEnd").isEmpty()){edit(p,o->o.putBoolean("AlexAsked",true));alexGoes(p,h);}
                else above(h,a,who.equals("Kit")?"Go on. I'm right behind you.":"The door first.");}
            default->above(h,a,"...");
        }
    }

    // ------------------------------------------------------------------------------------------------ right-clicks
    /** The caller saves its copy of the reader's record after a click, so what this saves through fresh reads is handed back in that copy. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        save(p,own);boolean handled=clicked(p,b,rel,own(p));
        var fresh=own(p);for(var key:List.copyOf(own.getAllKeys()))own.remove(key);own.merge(fresh);
        return handled;
    }
    static boolean clicked(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var h=house(b);if(h==null||own.getBoolean("Completed")||!own.contains("Snap"))return false;
        int ch=own.getInt("Chapter");var s=l.getBlockState(b.offset(rel));var kind=PlaceForYouBlock.kind(s);int held=holding(p);
        // The upstairs doors, shut this evening; the back door's stiff catch.
        if(PlaceForYouSite.SHUT_DOORS.stream().anyMatch(d->d.equals(rel)||d.above().equals(rel))){cue(p,"Shut. Not tonight.");return true;}
        if(rel.equals(PlaceForYouSite.BACK_DOOR)||rel.equals(PlaceForYouSite.BACK_DOOR.above())){cue(p,"The back door's catch is stiff, and the cupboard stands in front of it.");return true;}
        if(held==PlaceForYouRegistry.BAG)return putBag(p,h,rel);
        if(held==PlaceForYouRegistry.PLATE){if(rel.equals(PlaceForYouSite.SINK)||rel.equals(PlaceForYouSite.SINK_TOP)){release(p,b);int n=own.getInt("Plates")+1;edit(p,o->o.putInt("Plates",n));
                l.setBlock(b.offset(PlaceForYouSite.SINK_TOP),PlaceForYouBlock.of(PlaceForYouBlock.Kind.PLATE,net.minecraft.core.Direction.NORTH,n>=2?2:1),3);l.playSound(null,b.offset(PlaceForYouSite.SINK),SoundEvents.DECORATED_POT_INSERT,SoundSource.BLOCKS,.6F,1.4F);
                if(n>=3)activityDone(p,h,"Mara","I've heated it up once already, you know. Tonight. It was better the first time.");else cue(p,"In the sink. "+(3-n)+" more.");return true;}
            cue(p,"The plate goes in the sink.");return true;}
        if(kind==PlaceForYouBlock.Kind.PLATE&&ch==SUPPER&&own.getString("Activity").equals("plates")&&!own.getBoolean("ActivityDone")&&PlaceForYouSite.PLATES.contains(rel)){
            l.setBlock(b.offset(rel),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);take(p,PlaceForYouRegistry.PLATE,"A plate");return true;}
        if(kind==PlaceForYouBlock.Kind.BAG){
            if(ch==SUPPER&&own.getString("Activity").equals("clasp")&&!own.getBoolean("ActivityDone")){int n=own.getInt("Clasp")+1;edit(p,o->o.putInt("Clasp",n));l.playSound(null,b.offset(rel),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.5F,1.5F);
                if(n==1)cue(p,"The clasp won't sit.");
                else if(n==2){above(h,actor(l,h,"Kit"),"You have to sort of— no. Like you're asking it nicely.");cue(p,"Kit holds the bag still, and laughs at their own explanation.");}
                else{PlaceForYouSite.bag(l,b,"none");activityDone(p,h,"Kit","Huh. It's never done that for me.");}return true;}
            if(ch>=BAG&&own.getInt("BagStage")>=2&&ch<OPEN){if(!own.getBoolean("Acknowledged")){cue(p,"Kit's bag. Mara's hand is on the strap.");return true;}
                PlaceForYouSite.bag(l,b,"none");take(p,PlaceForYouRegistry.BAG,"Kit's bag");cue(p,"Kit's bag. Heavier than it looks. Give it to someone, set it at the gate, or help unpack it.");return true;}
            cue(p,"Kit's bag, packed.");return true;}
        if(kind==PlaceForYouBlock.Kind.KETTLE||kind==PlaceForYouBlock.Kind.STOVE){
            if(ch==SUPPER&&own.getString("Activity").equals("tea")&&!own.getBoolean("ActivityDone")){edit(p,o->o.putBoolean("Kettle",true));l.playSound(null,b.offset(rel),SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.3F,1.8F);cue(p,"The kettle's warm already. It always is.");return true;}
            cue(p,"The kettle, on the stove. Warm.");return true;}
        if(kind==PlaceForYouBlock.Kind.CUPS){
            if(ch==SUPPER&&own.getString("Activity").equals("tea")&&!own.getBoolean("ActivityDone")){
                if(!own.getBoolean("Kettle")){cue(p,"The kettle first.");return true;}
                PlaceForYouSite.cups(l,b,true);l.playSound(null,b.offset(rel),SoundEvents.BOTTLE_EMPTY,SoundSource.BLOCKS,.6F,1F);
                activityDone(p,h,"Ren","Cups are in the— no. The— hang on. I've lived here weeks. Haven't I?");return true;}
            cue(p,"Two cups.");return true;}
        if(kind==PlaceForYouBlock.Kind.RADIO){l.playSound(null,b.offset(rel),SoundEvents.NOTE_BLOCK_HARP.value(),SoundSource.BLOCKS,.4F,.9F);cue(p,"A song from before. You almost know it.");return true;}
        if(kind==PlaceForYouBlock.Kind.PHOTO){cue(p,"A man half turned from the camera, in a work shirt. Somebody has dusted the frame and nothing else.");if(!own.getBoolean("TheoNamed"))edit(p,o->o.putBoolean("SawTheo",true));return true;}
        if(kind==PlaceForYouBlock.Kind.PICTURES){cue(p,"The household, in a row. There's a gap in the row, the width of one person.");return true;}
        if(kind==PlaceForYouBlock.Kind.GLASS){cue(p,switch(s.getValue(PlaceForYouBlock.STAGE)){case 1->"The hall glass, turned to face the wallpaper.";case 2->"The hall glass, with a cloth over it.";default->"The hall glass.";});return true;}
        if(kind==PlaceForYouBlock.Kind.CUPBOARD){
            if(ch==DINNER&&!own.getBoolean("CupboardOpened")){edit(p,o->o.putBoolean("CupboardOpened",true));cue(p,"Shelves. Kit's things, folded. Kit is behind you in the hall, saying your name like it's the second time.");above(h,actor(l,h,"Kit"),"Hey. Hey. I'm here.");return true;}
            cue(p,"Shelves, and Kit's folded things.");return true;}
        if(kind==PlaceForYouBlock.Kind.ROUTINE){
            if(ch==BATHROOM&&!own.getString("Care").isEmpty()&&!own.getBoolean("RoutineUsed")){
                String helper=own.getString("Helper");
                cue(p,switch(own.getString("Care")){case "help"->helper+" holds it out to you, and you do it together, slowly.";case "company"->"You do it yourself. "+helper+" talks about the weather the whole time, which helps.";default->"You do it on your own.";});
                l.playSound(null,b.offset(rel),SoundEvents.BRUSH_GENERIC,SoundSource.BLOCKS,.6F,1F);PlaceForYouSite.routine(l,b,-1);h.repeatAt=(int)h.tick+60;
                edit(p,o->o.putBoolean("RoutineUsed",true));cue(p,"You put it away in the cabinet.");return true;}
            if(ch==DINNER&&own.getString("Care").equals("private")&&!own.getBoolean("RoutineStopped")){PlaceForYouSite.routine(l,b,-1);edit(p,o->o.putBoolean("RoutineStopped",true));cue(p,"You put your hand over it. It's still warm. It stops.");return true;}
            cue(p,ch==BATHROOM&&own.getString("Care").isEmpty()?"Not yet.":"Laid out exactly as before, ready for the same moment.");return true;}
        if(kind==PlaceForYouBlock.Kind.CARD)return false;
        // The front door: held against the house's closing it, or propped open with the hall chair to open the house.
        if(rel.equals(PlaceForYouSite.FRONT_DOOR)||rel.equals(PlaceForYouSite.FRONT_DOOR.above())){
            if(ch==OPEN&&!own.getBoolean("Propped")&&PlaceForYouSite.inHouse(rel(p.position(),b))){PlaceForYouSite.propped(l,b,true);edit(p,o->o.putBoolean("Propped",true));cue(p,"You set the hall chair against it. It stays.");checkOpen(p,h);return true;}
            if(ch==DINNER&&h.slam>0&&!own.getBoolean("DoorHeld")){PlaceForYouSite.frontDoor(l,b,true);edit(p,o->o.putBoolean("DoorHeld",true));cue(p,"You hold it. It pulls once against your hand, and stops.");return true;}
            return false;}
        // The fridge: the reader's picture, taken down to open the house.
        if(rel.equals(PlaceForYouSite.FRIDGE)||rel.equals(PlaceForYouSite.FRIDGE.above())){
            if(ch==OPEN&&!own.getBoolean("PictureDown")){PlaceForYouSite.picture(l,b,own.getCompound("Snap").getCompound("Drawing"),false);edit(p,o->o.putBoolean("PictureDown",true));cue(p,"You take your picture down off the fridge.");checkOpen(p,h);return true;}
            cue(p,PlaceForYouSite.pictureUp(l,b)?(own.getCompound("Snap").contains("Drawing")?"Your picture, on their fridge.":"A pencil figure on the fridge door, with no face yet. It's your place they've drawn."):"The fridge door, bare.");return true;}
        // Kit's door, before it is taken: Kit's room is Kit's.
        if(rel.equals(PlaceForYouSite.KIT_DOOR)||rel.equals(PlaceForYouSite.KIT_DOOR.above())){if(!own.getBoolean("DoorTaken")){cue(p,"Kit's door. It's Kit's room.");return true;}return false;}
        return false;
    }
    static void checkOpen(ServerPlayer p,House h){var own=own(p);if(own.getBoolean("Propped")&&own.getBoolean("PictureDown"))cue(p,"The way out is open, and your picture is down. Now they decide for themselves.");}
    static void giveBag(ServerPlayer p,House h,String who){
        release(p,null);var l=p.serverLevel();var b=h.base;
        if(who.equals("Kit")){edit(p,o->{o.putString("BagWhere","kit");o.putBoolean("Acknowledged",true);});above(h,actor(l,h,"Kit"),"Thanks. I'll set it by the gate myself this time.");PlaceForYouSite.bag(l,b,"gate");}
        else{edit(p,o->{o.putString("BagWhere","mara");o.putBoolean("Delayed",true);o.putBoolean("Acknowledged",true);});above(h,actor(l,h,"Mara"),"Thank you. I'll just sort it.");PlaceForYouSite.bag(l,b,"unpacked");}
    }
    static boolean putBag(ServerPlayer p,House h,BlockPos rel){
        var l=p.serverLevel();var b=h.base;
        if(rel.distManhattan(PlaceForYouSite.GATE)<=3&&rel.getZ()<=PlaceForYouSite.GATE.getZ()){release(p,null);PlaceForYouSite.bag(l,b,"gate");edit(p,o->o.putString("BagWhere","gate"));cue(p,"You set it down inside the gate, where Kit left it.");return true;}
        if(rel.equals(PlaceForYouSite.UNPACKED.below())||PlaceForYouBlock.is(l.getBlockState(b.offset(rel)),PlaceForYouBlock.Kind.TABLE)){release(p,null);PlaceForYouSite.bag(l,b,"unpacked");edit(p,o->{o.putString("BagWhere","unpacked");o.putBoolean("Delayed",true);});cue(p,"You help take everything out. Folded, it hardly looks like leaving.");return true;}
        release(p,null);PlaceForYouSite.bag(l,b,"kitchen");cue(p,"You put the bag back by Kit's chair.");return true;
    }

    // ------------------------------------------------------------------------------------------------ carried things
    static ItemStack carried(ServerPlayer p,int what,String name){
        var s=new ItemStack(PlaceForYouRegistry.CARRIED.get());s.set(DataComponents.CUSTOM_NAME,Component.literal(name));s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(what));
        var t=new CompoundTag();t.putInt(TAG,what);t.putUUID("Owner",p.getUUID());s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;
    }
    static void take(ServerPlayer p,int what,String name){var s=carried(p,what,name);if(!p.getInventory().add(s))p.drop(s,false);}
    /** What the reader is carrying for the household, or 0. */
    public static int holding(ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);var c=s.get(DataComponents.CUSTOM_DATA);if(s.is(PlaceForYouRegistry.CARRIED.get())&&c!=null&&c.copyTag().contains(TAG))return c.copyTag().getInt(TAG);}return 0;}
    /** Takes back whatever the reader carries; a bag carried off goes back by Kit's chair, a plate to the sink. */
    static void release(ServerPlayer p,@Nullable BlockPos b){
        int held=0;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);var c=s.get(DataComponents.CUSTOM_DATA);if(s.is(PlaceForYouRegistry.CARRIED.get())&&c!=null&&c.copyTag().contains(TAG)){held=c.copyTag().getInt(TAG);p.getInventory().setItem(i,ItemStack.EMPTY);}}
        if(b!=null&&held==PlaceForYouRegistry.BAG){var l=p.server.getLevel(NovelRooms.dimension(PLACE));if(l!=null&&l.isLoaded(b.offset(PlaceForYouSite.BAG_KITCHEN))){PlaceForYouSite.bag(l,b,"gate");edit(p,o->{if(o.getString("BagWhere").isEmpty())o.putString("BagWhere","gate");});cue(p,"Not past the gate. You leave the bag inside it.");}}
    }
}
