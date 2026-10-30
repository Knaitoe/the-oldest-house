package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.labyrinth.RoadhouseBlock.Kind;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.joml.Vector3f;

/**
 * The Last Stop's night (0.4.78). One shared clock counts while anyone is in the scene. The bar is open first: Marla serves
 * on a tab, the jukebox plays what a reader picks, and four regulars talk if spoken to. Then the cow elk comes over the
 * highway fence, slams into a parked pickup (a real dent, an alarm, the headlights flashing) and stands in the lot looking
 * at whoever is there before going through the broken gap into the field, where it waits. Three of the regulars come out
 * with flashlights whose beams are real light, ask, then accuse the reader by name, and herd them toward the field; they
 * keep their distance and never touch anyone. In the field each reader's own loop, herd and ending are theirs alone.
 * When the scene has been empty a while, the night resets for whoever comes next.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ElkLotNight {
    static final LabyrinthPlace PLACE=ElkLot.PLACE;
    /** Occupied ticks before the crash; sooner once a reader is outside, so nobody waits in the dark for it. */
    public static final int CALM=1200,EARLY=400;
    /** The elk's beats, in ticks after the crash begins. */
    public static final int LEAP=12,IMPACT=54,LOOK_MAX=400,LOOKED=40,ALARM=300,MEN_OUT=70;
    /** How long the men search before they turn on the reader, and how long each stage of it lasts. */
    public static final int SEARCH=200,STAGE=300;
    public static final int MAX_DRINKS=6,EMPTY_RESET=100;
    public static final String DRINK="LastStopDrink";

    // ------------------------------------------------------------------------------------------------ the people
    /** One of the bar's people: their skin (a stranger phase), where they sit or stand, and which way they face. */
    public record Person(String key,String name,int phase,double x,double z,float yaw){
        public Vec3 home(BlockPos b){return new Vec3(b.getX()+x,b.getY(),b.getZ()+z);}
    }
    public static final Person MARLA=new Person("Marla","Marla",4,-24.5,-26.0,0),RUSS=new Person("Russ","Russ",5,-23.5,-23.5,180),
        DENNY=new Person("Denny","Denny",6,-23.5,-19.5,90),WADE=new Person("Wade","Wade",7,-30.5,-21.5,0),HOLLIS=new Person("Hollis","Hollis",8,-29.5,-23.5,180);
    public static final List<Person> CAST=List.of(MARLA,RUSS,DENNY,WADE,HOLLIS);
    /** The three who go out. */
    public static final List<Person> MEN=List.of(RUSS,DENNY,WADE);
    /** Each man's way out: away from his seat, along the room and out of the side door. */
    private static final double EXIT_Z=ElkLot.SIDE_DOOR.getZ()+.5;
    private static final double[][][] OUT={
        {{-23.5,-22.5},{-16.5,-22.5},{-14.5,EXIT_Z},{-12.5,EXIT_Z},{-10.5,EXIT_Z}},
        {{-20.5,-21.5},{-16.5,-22.5},{-14.5,EXIT_Z},{-12.5,EXIT_Z},{-10.5,EXIT_Z},{-2.5,EXIT_Z}},
        {{-29.5,-22.5},{-16.5,-22.5},{-14.5,EXIT_Z},{-12.5,EXIT_Z},{-10.5,EXIT_Z}}};
    /** Where each man stands while they search, and where once they herd: one on the road back, one by the bar, one at the gap. */
    private static final double[][] SEARCHING={{-8.5,-16.5},{-2.5,-15.5},{-4.5,-31.5}},HERDING={{0.5,-6.5},{-8.5,-13.0},{4.5,-36.5}};

    private static final Map<String,String[]> IDLE=Map.of(
        "Marla",new String[]{"We're past close, hon. You look rough. Sit a minute.","You're not from around here. That's all right. Nobody's from around here after midnight.","Kitchen's closed. There's pickled eggs if you're brave.","Put it on the tab. Everybody does.","Wipe your feet next time. The field comes in on everybody's boots."},
        "Russ",new String[]{"Drove eleven hours today. Every one of them looked the same.","You hear that? Out past the lot. Every night this week.","My cousin hit one on this road. Cow elk. Said it stood up after.","Don't go out past the fence. Ground's soft out there.","%s, right? Thought so. You look like a %s."},
        "Denny",new String[]{"You shoot? No? Then quit hovering.","Eight ball, corner pocket. Watch.","Wade owes me forty. Wade always owes me forty.","Lights went out across the road last night. Just a second. Like something walked in front of them."},
        "Wade",new String[]{"Sit down if you're sitting.","There's a herd comes down to that field. You don't see them. You see where they were.","Marla's got a clipping on the board by the door. Read it sometime. Or don't.","Song's almost over. They're always almost over."},
        "Hollis",new String[]{"Mm. Not yet.","...counted them going in. Never counted them coming out...","Tell her I paid.","Is it still out there? Don't tell me."});
    /** The men's lines once they are out: questions, then the reader, then accusations, then the field. */
    private static final String[][][] OUTSIDE={
        {{"Where'd it go? Anybody see where it went?","It came over the fence. Nothing comes over that fence."},{"Look at my truck. Look at it!","Door's caved right in. It went for the truck."},{"Field. It went for the field.","Nobody follow it. You hear me?"}},
        {{"%s. You were out here. You saw it.","You were out here when it happened, %s."},{"It looked right at you, %s. Why'd it look at you?","It knew you. It stood there like it knew you."},{"Stood there like it was waiting on you.","It's not leaving, %s. It's waiting."}},
        {{"What did you do, %s?","What did you bring here, %s?"},{"You brought it. Didn't you. You brought it here.","Every night this week. And now you."},{"Go on, then. Out there. It's waiting on you.","Nobody leaves till it's done, %s."}},
        {{"Field's that way, %s.","Road's closed, %s. Field's that way."},{"Go find it.","Go on. Go find it."},{"Go on, %s. Out past the fence.","It's in the stalks. Go see it."}}};
    private static final String[] WOUNDED={"You hit it. You actually hit it.","Now it'll remember you.","You hurt it, %s. You don't hurt them."};
    private static final String[] MARLA_AFTER={"They'll come back in. They always come back in.","Don't look at me, hon. I just pour.","Still serving. God knows why.","Close the door behind you. The cold gets in."};

    // ------------------------------------------------------------------------------------------------ helpers
    private static String said(Person who,String line,ServerPlayer p){return line.replace("%s",p.getGameProfile().getName());}
    private static @Nullable LiteraryActor cast(ServerLevel l,CompoundTag w,Person who){return w.hasUUID(who.key())&&l.getEntity(w.getUUID(who.key())) instanceof LiteraryActor a?a:null;}
    private static @Nullable LiteraryElk elk(ServerLevel l,CompoundTag w){return w.hasUUID("Elk")&&l.getEntity(w.getUUID("Elk")) instanceof LiteraryElk e?e:null;}
    public static @Nullable Person person(LiteraryActor a){if(a.role()!=LiteraryActor.STRANGER)return null;for(var p:CAST)if(p.phase()==a.phase())return p;return null;}
    private static Vec3 rel(BlockPos b,double x,double z){return new Vec3(b.getX()+x,b.getY(),b.getZ()+z);}
    private static float yawTo(Entity a,Vec3 at){var d=at.subtract(a.position());return (float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;}
    private static void turn(LivingEntity a,float yaw){a.setYRot(yaw);a.setYHeadRot(yaw);a.yBodyRot=yaw;a.yRotO=yaw;}
    /** One step toward {@code to}; true once there. */
    private static boolean walk(LivingEntity a,Vec3 to,double speed){
        return ElkLotEnding.step(a,to,speed);
    }
    private static void say(LiteraryActor a,String line){a.say(line);a.getPersistentData().putLong("Said0478",a.level().getGameTime());}
    /** A line said to the readers near enough to hear it. */
    private static void speak(LiteraryActor a,Person who,String line,List<ServerPlayer> near){say(a,line);for(var p:near)if(LiteraryVignettes.inside(p,PLACE)&&p.distanceToSqr(a)<400)p.displayClientMessage(Component.literal(who.name()+": “"+line+"”"),false);}
    private static @Nullable ServerPlayer nearest(Entity a,List<ServerPlayer> here,double reach){ServerPlayer best=null;double d=reach*reach;for(var p:here){double e=p.distanceToSqr(a);if(e<d){d=e;best=p;}}return best;}
    static List<ServerPlayer> here(MinecraftServer s){var out=new ArrayList<ServerPlayer>();for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.inside(p,PLACE)&&LiteraryVignettes.personal(LabyrinthData.get(s),p.getUUID(),PLACE).getBoolean("Here"))out.add(p);return out;}
    /** In the bar itself, under its roof. */
    public static boolean inBar(Vec3 at,BlockPos b){double x=at.x-b.getX(),z=at.z-b.getZ();return x>=-33&&x<=-12&&z>=-31&&z<=-13;}
    public static boolean inField(Vec3 at,BlockPos b){return at.z-b.getZ()<ElkLot.FIELD;}
    public static int stage(CompoundTag w){if(!w.contains("CrashAt"))return -1;int t=w.getInt("Night")-w.getInt("CrashAt")-MEN_OUT-SEARCH;return t<0?0:Math.min(3,1+t/STAGE);}

    // ------------------------------------------------------------------------------------------------ the shared clock
    public static void tick(MinecraftServer s){
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;var l=s.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;var b=LabyrinthPlaces.base(origin,PLACE);
        var d=LabyrinthData.get(s);if(!ElkLot.built(d,b))return;var w=LiteraryVignettes.shared(d,PLACE);var here=here(s);
        if(here.isEmpty()){
            if(w.contains("Night")){w.putInt("Empty",w.getInt("Empty")+1);
                if(w.getInt("Empty")>=EMPTY_RESET&&l.players().stream().noneMatch(p->p.getCamera().distanceToSqr(Vec3.atCenterOf(b.offset(0,0,-40)))<70*70))reset(w);
                LiteraryVignettes.shared(d,PLACE,w);}
            // Put back what was left out while nobody is there, as each piece is loaded.
            else if(w.getBoolean("Settle")){if(settle(l,b,w))w.remove("Settle");LiteraryVignettes.shared(d,PLACE,w);}
            return;
        }
        w.putInt("Empty",0);int night=w.getInt("Night")+1;w.putInt("Night",night);
        settle(l,b,w);
        if(night==1&&!w.contains("Song")){w.putInt("Song",1);play(l,b,1);}
        if(!w.contains("CrashAt")){
            boolean out=here.stream().anyMatch(p->!inBar(p.position(),b));
            if(night>=CALM||night>=EARLY&&out){w.putInt("CrashAt",night);w.putInt("ElkStage",1);w.putInt("ElkLeg",0);w.putInt("ElkSeen",0);}
            else if(night%240==120)idle(l,w,here);
        }
        if(w.contains("CrashAt")){int t=night-w.getInt("CrashAt");elk(l,b,w,t,here);alarm(l,b,w,t);men(l,b,w,t,here);}
        // A truck goes by now and then on the road, unseen.
        if(night%1100==700){var p=here.get(night/1100%here.size());l.playSound(null,BlockPos.containing(p.getX(),b.getY()+1,b.getZ()-6),LiteraryRegistry.LOT_HIGHWAY.get(),SoundSource.AMBIENT,2.5F,.9F+(night%7)*.03F);}
        // Lines fade from over their heads after a few seconds.
        if(night%10==0)for(var who:CAST){var a=cast(l,w,who);if(a!=null&&a.isCustomNameVisible()&&l.getGameTime()-a.getPersistentData().getLong("Said0478")>100)a.setCustomNameVisible(false);}
        LiteraryVignettes.shared(d,PLACE,w);
    }

    /** The night starts over for whoever comes next: the cast in their places, the elk out of sight, the truck mended. */
    private static void reset(CompoundTag w){
        int gen=w.getInt("Gen")+1;for(var k:List.of("Night","CrashAt","ElkStage","ElkLeg","ElkSeen","Alarm","Empty","Lines","Song"))w.remove(k);w.putInt("Gen",gen);w.putBoolean("Mend",true);w.putBoolean("Settle",true);
    }
    /** Puts each loaded piece back as the current night needs it; pieces not loaded yet are put back when they load. */
    private static boolean settle(ServerLevel l,BlockPos b,CompoundTag w){
        int gen=w.getInt("Gen");boolean all=true;
        for(var who:CAST){var a=cast(l,w,who);if(a==null){all&=!w.hasUUID(who.key());continue;}if(a.getPersistentData().getInt("Gen0478")==gen)continue;
            var home=who.home(b);a.moveTo(home.x,home.y,home.z,who.yaw(),0);turn(a,who.yaw());a.getPersistentData().putInt("Gen0478",gen);a.getPersistentData().remove("Leg0478");a.setCustomNameVisible(false);}
        var e=elk(l,w);if(e==null&&w.hasUUID("Elk"))all=false;if(e!=null&&e.getPersistentData().getInt("Gen0478")!=gen&&!w.contains("CrashAt")){hide(l,b,e);e.getPersistentData().putInt("Gen0478",gen);}
        if(w.getBoolean("Mend")&&l.hasChunkAt(b.offset(ElkLot.dent()))&&l.hasChunkAt(b.offset(ElkLot.SIDE_DOOR))){
            var at=b.offset(ElkLot.dent());var st=l.getBlockState(at);if(RoadhouseBlock.is(st,Kind.PU_DOOR_DENT))l.setBlock(at,st.setValue(RoadhouseBlock.KIND,Kind.PU_DOOR),Block.UPDATE_CLIENTS);
            door(l,b.offset(ElkLot.SIDE_DOOR),false);clearBeams(l,w);ElkLot.headlights(l,b,ElkLot.PICKUPS[ElkLot.STRUCK],true);w.remove("Mend");
        }
        return all&&!w.getBoolean("Mend");
    }
    private static void door(ServerLevel l,BlockPos at,boolean open){
        for(var p:List.of(at,at.above())){var st=l.getBlockState(p);if(st.getBlock() instanceof DoorBlock&&st.getValue(DoorBlock.OPEN)!=open)l.setBlock(p,st.setValue(DoorBlock.OPEN,open),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
    }
    private static void idle(ServerLevel l,CompoundTag w,List<ServerPlayer> here){
        int n=w.getInt("Lines");w.putInt("Lines",n+1);var who=CAST.get(n%CAST.size());var a=cast(l,w,who);var p=a==null?null:nearest(a,here,16);if(a==null||p==null)return;
        var lines=IDLE.get(who.key());say(a,said(who,lines[(n/CAST.size())%lines.length],p));
    }

    // ------------------------------------------------------------------------------------------------ the elk
    private static final double[][] CHARGE={{13.5,-6.5},{1.5,-12.5},{-2,-15.5},{-2,-21.0}},STUMBLE={{-2,-15.5},{-3.5,-13.5}},
        GO={{-2,-16.0},{-2,-25.0},{1.5,-36.5},{1.5,-41.0},{1.5,-44.0},{3.5,-52.0},{4.5,-72.5}};
    /** The way out, rounded off so the elk runs it in one long curve rather than corner to corner. */
    private static final double[][] GO_PATH=smooth(GO,3);
    private static double[][] smooth(double[][] points,int passes){
        var out=new ArrayList<double[]>(Arrays.asList(points));
        for(int n=0;n<passes;n++){var next=new ArrayList<double[]>();next.add(out.get(0));
            for(int i=0;i+1<out.size();i++){var a=out.get(i);var c=out.get(i+1);next.add(new double[]{a[0]*.75+c[0]*.25,a[1]*.75+c[1]*.25});next.add(new double[]{a[0]*.25+c[0]*.75,a[1]*.25+c[1]*.75});}
            next.add(out.get(out.size()-1));out=next;}
        return out.toArray(double[][]::new);
    }
    /** Where the elk waits in the field, ahead of anyone walking the lane. */
    public static final Vec3 REST=new Vec3(4.5,0,-72.5);
    static void hide(ServerLevel l,BlockPos b,LiteraryElk e){e.setInvisible(true);e.setNoGravity(true);e.moveTo(b.getX()+24.5,b.getY(),b.getZ()+.5,90,0);turn(e,90);e.setDeltaMovement(Vec3.ZERO);}
    /** After a re-carve: the elk is kept, out of sight, for the first night. */
    static void restElk(ServerLevel l,BlockPos b){var d=LabyrinthData.get(l.getServer());var w=LiteraryVignettes.shared(d,PLACE);var e=elk(l,w);if(e!=null)hide(l,b,e);}
    private static void elk(ServerLevel l,BlockPos b,CompoundTag w,int t,List<ServerPlayer> here){
        var e=elk(l,w);if(e==null)return;e.setInvisible(false);e.setNoGravity(true);int stage=w.getInt("ElkStage"),leg=w.getInt("ElkLeg");
        // Earlier saved nights may already be beyond impact on an unwounded body.
        if(stage>=3&&!e.wounded())e.wound();
        switch(stage){
            case 1->{ // Over the fence: an arc from the dark beyond the road.
                double f=Math.min(1,t/(double)LEAP);var at=rel(b,24.5+(21.5-24.5)*f,.5+(-2.5-.5)*f);e.moveTo(at.x,at.y+1.6*Math.sin(Math.PI*f),at.z,90,0);turn(e,120);
                if(t==1)l.playSound(null,e.blockPosition(),SoundEvents.FENCE_GATE_CLOSE,SoundSource.NEUTRAL,1.4F,.6F);
                if(f>=1){w.putInt("ElkStage",2);w.putInt("ElkLeg",0);}
            }
            case 2->{ // Across the road at a gallop and into the gap between the trucks.
                var to=rel(b,CHARGE[leg][0],CHARGE[leg][1]);if(walk(e,to,leg==CHARGE.length-1?.65:.85)){if(leg+1<CHARGE.length)w.putInt("ElkLeg",leg+1);else{impact(l,b,w,e,here);w.putInt("ElkStage",3);w.putInt("ElkLeg",0);}}
                if(t%4==0)l.playSound(null,e.blockPosition(),LiteraryRegistry.HOOVES.get(),SoundSource.NEUTRAL,1.2F,1.3F);
            }
            case 3->{ // Knocked back out of the gap.
                if(walk(e,rel(b,STUMBLE[leg][0],STUMBLE[leg][1]),leg==0?.2:.12)){if(leg+1<STUMBLE.length)w.putInt("ElkLeg",leg+1);else{w.putInt("ElkStage",4);w.putInt("ElkLook",t);}}
            }
            case 4->{ // It stands and looks at whoever is there, then goes.
                var p=nearest(e,here,48);if(p!=null)turn(e,yawTo(e,p.position()));
                int seen=w.getInt("ElkSeen");for(var r:here)if(HouseWatchers.sees(r,e.getEyePosition())){seen++;break;}w.putInt("ElkSeen",seen);
                boolean close=p!=null&&p.distanceToSqr(e)<25;
                if(seen>=LOOKED||close||t-w.getInt("ElkLook")>=LOOK_MAX){w.putInt("ElkStage",5);w.putInt("ElkLeg",0);l.playSound(null,e.blockPosition(),SoundEvents.CAMEL_AMBIENT,SoundSource.NEUTRAL,1F,.5F);}
            }
            case 5->{ // Through the broken gap, over the ditch and up the lane at a run, in one curve.
                double speed=leg<GO_PATH.length/3?.5:.42;
                for(int k=0;k<3&&w.getInt("ElkStage")==5;k++){leg=w.getInt("ElkLeg");if(!walk(e,rel(b,GO_PATH[leg][0],GO_PATH[leg][1]),speed))break;if(leg+1<GO_PATH.length)w.putInt("ElkLeg",leg+1);else w.putInt("ElkStage",6);}
                if(t%5==0)l.playSound(null,e.blockPosition(),LiteraryRegistry.HOOVES.get(),SoundSource.NEUTRAL,.8F,1.15F);
            }
            default->{ // Waiting in the field, watching whoever comes.
                var at=rel(b,REST.x,REST.z);if(e.position().distanceToSqr(at)>.04)walk(e,at,.16);
                var p=nearest(e,here.stream().filter(r->inField(r.position(),b)).toList(),24);turn(e,p==null?0:yawTo(e,p.position()));
            }
        }
    }
    private static void impact(ServerLevel l,BlockPos b,CompoundTag w,LiteraryElk e,List<ServerPlayer> here){
        e.wound();
        var at=b.offset(ElkLot.dent());var st=l.getBlockState(at);if(RoadhouseBlock.is(st,Kind.PU_DOOR))l.setBlock(at,st.setValue(RoadhouseBlock.KIND,Kind.PU_DOOR_DENT),Block.UPDATE_CLIENTS);
        l.playSound(null,at,LiteraryRegistry.LOT_CRASH.get(),SoundSource.BLOCKS,2.5F,1F);l.playSound(null,at,SoundEvents.GLASS_BREAK,SoundSource.BLOCKS,1.2F,.8F);
        l.sendParticles(ParticleTypes.CRIT,at.getX()+.2,at.getY()+1,at.getZ()+.5,18,.3,.4,.4,.2);l.sendParticles(ParticleTypes.POOF,at.getX()+.2,at.getY()+.6,at.getZ()+.5,8,.3,.3,.3,.02);
        w.putInt("Alarm",ALARM);
        var marla=cast(l,w,MARLA);if(marla!=null)speak(marla,MARLA,"What in God's name was that?",here);
        var hollis=cast(l,w,HOLLIS);if(hollis!=null)say(hollis,"Told you. Told all of you.");
    }
    private static void alarm(ServerLevel l,BlockPos b,CompoundTag w,int t){
        int left=w.getInt("Alarm");if(left<=0)return;w.putInt("Alarm",left-1);var p=ElkLot.PICKUPS[ElkLot.STRUCK];
        if(left%30==0)l.playSound(null,b.offset(ElkLot.dent()),LiteraryRegistry.LOT_ALARM.get(),SoundSource.BLOCKS,2.5F,1F);
        if(left%10==0)ElkLot.headlights(l,b,p,left==1||left/10%2==0);
        if(left==1)ElkLot.headlights(l,b,p,true);
    }

    // ------------------------------------------------------------------------------------------------ the men
    private static void men(ServerLevel l,BlockPos b,CompoundTag w,int t,List<ServerPlayer> here){
        if(t<MEN_OUT)return;
        if(t==MEN_OUT){door(l,b.offset(ElkLot.SIDE_DOOR),true);var russ=cast(l,w,RUSS);if(russ!=null)speak(russ,RUSS,"Get the lights. Come on.",here);}
        int stage=stage(w);
        for(int i=0;i<MEN.size();i++){
            var who=MEN.get(i);var a=cast(l,w,who);if(a==null)continue;var data=a.getPersistentData();int leg=data.getInt("Leg0478");
            var capture=ElkLotEnding.menTarget(l.getServer());if(capture!=null){beam(l,b,w,i,a,capture);continue;}
            if(a.gesture()==4){a.gesture(0);data.remove("ReturnToBar0492");}
            // A man left inside (after a capture, or held up) goes out the same way, by the side door, which he opens.
            if(leg>=OUT[i].length&&inBar(a.position(),b)){leg=1;data.putInt("Leg0478",leg);}
            openSideDoor(l,b,a);
            if(leg<OUT[i].length){if(walk(a,rel(b,OUT[i][leg][0],OUT[i][leg][1]),.16))data.putInt("Leg0478",leg+1);beam(l,b,w,i,a,null);continue;}
            var p=nearest(a,here,24);
            var spot=(stage>=2?HERDING:SEARCHING)[i];var goal=rel(b,spot[0],spot[1]);
            // Once they have stopped asking, they close in round the reader, as the herd does; their hands end it.
            boolean hostile=stage>=3&&p!=null&&!ElkLotNight.inField(p.position(),b)&&!ElkLotNight.inBar(p.position(),b);
            if(hostile){double angle=i*Math.PI*2/3;goal=p.position().add(Math.cos(angle)*.85,0,Math.sin(angle)*.85);}
            // Before that they keep their distance and never touch anyone: near a reader a man steps back and round them.
            if(hostile)walk(a,goal,.095);
            else if(p!=null&&p.distanceToSqr(a)<2.8*2.8){var away=new Vec3(a.getX()-p.getX(),0,a.getZ()-p.getZ());away=away.lengthSqr()<1e-4?new Vec3(1,0,0):away.normalize();
                var side=new Vec3(-away.z,0,away.x);if(side.dot(goal.subtract(a.position()))<0)side=side.scale(-1);var step=side.scale(.8).add(away.scale(.5)).normalize().scale(.6);
                walk(a,new Vec3(a.getX()+step.x,b.getY(),a.getZ()+step.z),.14);}
            else walk(a,goal,.12);
            if(p!=null)turn(a,yawTo(a,p.position()));else turn(a,180);
            beam(l,b,w,i,a,p);
        }
        // One of them speaks every few seconds, to whoever is nearest him.
        if(t>MEN_OUT+40&&(t-MEN_OUT)%110==0){
            int n=w.getInt("Lines");w.putInt("Lines",n+1);int i=n%MEN.size();var who=MEN.get(i);var a=cast(l,w,who);var p=a==null?null:nearest(a,here,32);
            if(a!=null&&p!=null){var own=LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),p.getUUID(),PLACE);
                // 0.4.187: every fourth line reads the reader's own record of animals killed back to them.
                String[] pool=own.getBoolean("ElkWounded")&&n%4==3?WOUNDED:n%4==1?new String[]{ElkFollows.accusation(p)}:OUTSIDE[Math.max(0,stage)][i];speak(a,who,said(who,pool[(n/MEN.size())%pool.length],p),here);}
        }
    }
    static void openSideDoor(ServerLevel l,BlockPos b,LivingEntity a){
        var doorway=b.offset(ElkLot.SIDE_DOOR);if(a.distanceToSqr(Vec3.atCenterOf(doorway))>6.25)return;var state=l.getBlockState(doorway);
        if(state.getBlock() instanceof DoorBlock door&&!state.getValue(DoorBlock.OPEN))door.setOpen(a,l,state,doorway,true);
    }
    /** A flashlight: a real light where its beam lands, moved as it swings, and a few motes along the way. */
    private static void beam(ServerLevel l,BlockPos b,CompoundTag w,int i,LiteraryActor a,@Nullable ServerPlayer target){
        var eye=a.position().add(0,1.45,0);Vec3 end;
        if(target!=null&&target.distanceToSqr(a)<18*18)end=target.getEyePosition();
        else{double sweep=Math.sin((a.level().getGameTime()+i*37)*.05)*.7;double yaw=Math.toRadians(a.getYRot())+sweep;end=eye.add(-Math.sin(yaw)*12,-1.1,Math.cos(yaw)*12);}
        var hit=l.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a));
        var cell=hit.getType()==HitResult.Type.BLOCK?hit.getBlockPos().relative(hit.getDirection()):BlockPos.containing(end);
        String key="Beam"+i;var old=w.contains(key)?BlockPos.of(w.getLong(key)):null;
        if(!cell.equals(old)){
            // Two beams can rest on one spot; the light goes out only when no beam is left on it.
            if(old!=null){boolean shared=false;for(int j=0;j<3;j++)if(j!=i&&w.contains("Beam"+j)&&w.getLong("Beam"+j)==old.asLong())shared=true;if(!shared&&l.getBlockState(old).is(Blocks.LIGHT))l.setBlock(old,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
            var there=l.getBlockState(cell);
            if(there.isAir()||there.is(Blocks.LIGHT)){if(there.isAir())l.setBlock(cell,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,11),Block.UPDATE_CLIENTS);w.putLong(key,cell.asLong());}else w.remove(key);
        }
        if(a.level().getGameTime()%4==i){var to=hit.getLocation();var dust=new DustParticleOptions(new Vector3f(1F,.95F,.72F),.7F);for(int k=1;k<=5;k++){var at=eye.lerp(to,k/6.0);l.sendParticles(dust,at.x,at.y,at.z,1,0,0,0,0);}}
    }
    private static void clearBeams(ServerLevel l,CompoundTag w){for(int i=0;i<3;i++){String key="Beam"+i;if(!w.contains(key))continue;var at=BlockPos.of(w.getLong(key));if(l.hasChunkAt(at)&&l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);w.remove(key);}}

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every reader's own part, five ticks at a time: meeting the cast, seeing the elk, and the field's loop. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        for(var who:CAST){var a=LiteraryVignettes.actor(p,PLACE,who.key(),LiteraryActor.STRANGER,BlockPos.containing(who.x(),0,who.z()),false);
            if(a!=null&&a.phase()!=who.phase()){a.appearance(LiteraryActor.STRANGER,who.phase());var home=who.home(b);a.moveTo(home.x,home.y,home.z,who.yaw(),0);turn(a,who.yaw());a.getPersistentData().putInt("Gen0478",LiteraryVignettes.shared(LabyrinthData.get(p.server),PLACE).getInt("Gen"));}}
        var d=LabyrinthData.get(p.server);var w=LiteraryVignettes.shared(d,PLACE);var l=p.serverLevel();
        LiteraryElk e=elk(l,w);
        if(e==null&&!w.hasUUID("Elk")){e=LiteraryRegistry.ELK.get().create(l);if(e!=null){hide(l,b,e);if(l.addFreshEntity(e)){w=LiteraryVignettes.shared(d,PLACE);w.putUUID("Elk",e.getUUID());e.getPersistentData().putInt("Gen0478",w.getInt("Gen"));LiteraryVignettes.shared(d,PLACE,w);}}}
        if(e!=null&&w.contains("CrashAt")&&!e.isInvisible()&&HouseWatchers.sees(p,e.getEyePosition()))own.putBoolean("ElkSeen",true);
        boolean field=inField(p.position(),b);
        if(field&&own.getBoolean("ElkSeen")&&!own.getBoolean("Ready")){
            int ticks=own.getInt("FieldTicks")+5;own.putInt("FieldTicks",ticks);int loops=own.getInt("FieldLoops");
            if(ticks%80==0)LiteraryVignettes.sound(p,LiteraryRegistry.HOOVES.get(),p.blockPosition().offset(3,0,-5),.75F,1.2F);
            // Continuous native steps, so the eyes creep rather than jumping between sound beats.
            double near=Math.max(2,9-loops*2-ticks/200.0);
            for(int side:new int[]{-1,1}){var eye=LiteraryVignettes.actor(p,PLACE,"Herd"+side,LiteraryActor.RED_FIGURE,BlockPos.containing(side*8,0,-75),true);
                if(eye!=null){eye.appearance(LiteraryActor.RED_FIGURE,3);eye.setInvisible(false);eye.setNoGravity(true);ElkLotEnding.step(eye,new Vec3(p.getX()+side*near,b.getY(),p.getZ()-near*.6),.1);}}
            // After the first time round, a cap lies by the broken post, only for this reader and only in the field they see.
            if(loops>=1&&ticks%20==0)for(int copy=0;copy<2;copy++){int z=ElkLot.FIELD_START-ElkLot.BROKEN_POST-1-copy*ElkLot.PERIOD;
                p.connection.send(new ClientboundBlockUpdatePacket(b.offset(ElkLot.POSTS_X+2,0,z),RoadhouseBlock.of(Kind.CAP,Direction.EAST)));
                p.connection.send(new ClientboundBlockUpdatePacket(b.offset(ElkLot.POSTS_X+2,1,z),Blocks.AIR.defaultBlockState()));}
            if(p.getZ()<b.getZ()+ElkLot.LOOP_AT){
                p.teleportTo(l,p.getX(),p.getY(),p.getZ()+ElkLot.PERIOD,p.getYRot(),p.getXRot());own.putInt("FieldLoops",loops+1);
                LiteraryVignettes.sound(p,LiteraryRegistry.HOOVES.get(),p.blockPosition().offset(-4,0,-6),1F,.9F);
            }
        }
        else if(field&&!own.getBoolean("Ready")&&p.getZ()<b.getZ()+ElkLot.LOOP_AT){
            // Without the elk seen, the field only turns a reader round at its end; it does not count.
            p.teleportTo(l,p.getX(),p.getY(),p.getZ()+ElkLot.PERIOD,p.getYRot(),p.getXRot());
        }
        ElkLotEnding.reader(p,b,own,LiteraryVignettes.shared(d,PLACE));
    }
    // ------------------------------------------------------------------------------------------------ talking and drinking
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var who=person(a);if(who==null||!LiteraryVignettes.inside(p,PLACE))return;var d=LabyrinthData.get(p.server);var w=LiteraryVignettes.shared(d,PLACE);var near=List.of(p);
        if(who==MARLA){
            if(own.getBoolean("Ready")||own.getBoolean("Completed")){
                String[] after={"Past close now, hon. Go on home.","Leave the glass. I'll see to it.","There's nothing left for you out in that field.","You look cold. Button your coat before you go.","You don't owe me for tonight.","Mind the ditch. Keep to the road."};
                int last=own.contains("MarlaLast130")?own.getInt("MarlaLast130"):-1;int n=p.getRandom().nextInt(last<0?after.length:after.length-1);if(last>=0&&n>=last)n++;own.putInt("MarlaLast130",n);speak(a,who,after[n],near);return;
            }
            var held=p.getMainHandItem();
            if(held.is(Items.GLASS_BOTTLE)){held.shrink(1);speak(a,who,"Thanks, hon.",near);return;}
            speak(a,who,w.contains("CrashAt")?MARLA_AFTER[own.getInt("Talk_Marla")%MARLA_AFTER.length]:IDLE.get("Marla")[own.getInt("Talk_Marla")%IDLE.get("Marla").length],near);own.putInt("Talk_Marla",own.getInt("Talk_Marla")+1);
            menu(p,a);return;
        }
        boolean out=a.getPersistentData().getInt("Leg0478")>0&&w.contains("CrashAt");
        int n=own.getInt("Talk_"+who.key());own.putInt("Talk_"+who.key(),n+1);
        String line;
        if(out){int i=MEN.indexOf(who);var pool=own.getBoolean("ElkWounded")&&n%3==2?WOUNDED:OUTSIDE[Math.max(0,stage(w))][i];line=pool[n%pool.length];}
        else{var pool=IDLE.get(who.key());line=pool[n%pool.length];}
        // Keep the shared original bark canonical. The reader's form of address
        // belongs only in this reply and must never enter the actor's nametag.
        String ordinary=said(who,line,p);say(a,ordinary);
        String reply=!out&&n%IDLE.get(who.key()).length==0?VignetteTailoring.lastStop(p,ordinary):ordinary;
        p.displayClientMessage(Component.literal(who.name()+": “"+reply+"”"),false);
    }
    private enum Drink {BEER("Draft beer",250,0xC98A1E),WHISKEY("Whiskey",400,0x8A4A16),COLA("Cola",100,0x3B1E12);
        final String label;final int cents,colour;Drink(String label,int cents,int colour){this.label=label;this.cents=cents;this.colour=colour;}}
    private static String money(int cents){return String.format(java.util.Locale.ROOT,"$%d.%02d",cents/100,cents%100);}
    private static void menu(ServerPlayer p,LiteraryActor marla){
        var icons=new HashMap<Integer,ItemStack>();
        for(var drink:Drink.values()){var icon=drinkItem(drink,p);icon.set(DataComponents.CUSTOM_NAME,Component.literal(drink.label+", "+money(drink.cents)));icons.put(1+drink.ordinal()*2,icon);}
        var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);
        icons.put(7,LiteraryChoiceMenu.icon(Items.PAPER,"Your tab: "+money(own.getInt("Tab"))));
        LiteraryChoiceMenu.open(p,"Marla: What'll it be?",icons,()->LiteraryVignettes.inside(p,PLACE)&&p.distanceToSqr(marla)<=36,slot->{
            if(slot==7){p.displayClientMessage(Component.literal("Marla: “Settle up when you're done, hon. Nobody's done yet.”"),false);return true;}
            int i=(slot-1)/2;if(slot%2!=1||i<0||i>=Drink.values().length)return false;order(p,marla,Drink.values()[i]);return true;});
    }
    /** One drink poured onto the reader's own tab, never more than {@value #MAX_DRINKS} a reader. */
    private static void order(ServerPlayer p,LiteraryActor marla,Drink drink){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);var near=List.of(p);
        if(own.getInt("Drinks")>=MAX_DRINKS){speak(marla,MARLA,"That's enough for tonight, sugar.",near);return;}
        own.putInt("Drinks",own.getInt("Drinks")+1);own.putInt("Tab",own.getInt("Tab")+drink.cents);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        var glass=drinkItem(drink,p);if(!p.getInventory().add(glass))p.drop(glass,false);
        p.serverLevel().playSound(null,marla.blockPosition(),SoundEvents.BOTTLE_FILL,SoundSource.NEUTRAL,.8F,drink==Drink.COLA?1.3F:.9F);
        speak(marla,MARLA,switch(drink){case BEER->"Here you go, hon.";case WHISKEY->"Neat. Don't let Russ talk you into a second.";default->"Flat, but cold.";},near);
        p.displayClientMessage(Component.literal("On your tab: "+money(own.getInt("Tab"))),true);
    }
    private static ItemStack drinkItem(Drink drink,ServerPlayer p){
        var glass=new ItemStack(Items.POTION);glass.set(DataComponents.POTION_CONTENTS,new PotionContents(Optional.empty(),Optional.of(drink.colour|0xFF000000),List.of()));
        glass.set(DataComponents.ITEM_NAME,Component.literal(drink.label));glass.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP,net.minecraft.util.Unit.INSTANCE);
        glass.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("On the tab at the Last Stop"))));
        var tag=new CompoundTag();tag.putString(DRINK,drink.name());glass.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return glass;
    }
    @SubscribeEvent public static void drunk(LivingEntityUseItemEvent.Finish e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var data=e.getItem().get(DataComponents.CUSTOM_DATA);if(data==null||!data.copyTag().contains(DRINK))return;
        var kind=data.copyTag().getString(DRINK);int drinks=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE).getInt("Drinks");
        if(kind.equals("WHISKEY"))p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,140,0,false,false));
        else if(kind.equals("BEER")&&drinks>=3)p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,80,0,false,false));
    }

    // ------------------------------------------------------------------------------------------------ the bar's things
    private static final net.minecraft.world.item.Item[] RECORDS={Items.MUSIC_DISC_STAL,Items.MUSIC_DISC_MALL,Items.MUSIC_DISC_CHIRP,Items.MUSIC_DISC_WAIT};
    private static void play(ServerLevel l,BlockPos b,int song){
        if(!(l.getBlockEntity(b.offset(ElkLot.JUKEBOX)) instanceof JukeboxBlockEntity box))return;
        box.setTheItem(ItemStack.EMPTY);if(song>0)box.setTheItem(new ItemStack(RECORDS[(song-1)%RECORDS.length]));
    }
    /** The bar's things answer a hand: the jukebox, the pool table, the darts and the board. True when handled. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var d=LabyrinthData.get(p.server);
        if(rel.equals(ElkLot.JUKEBOX)){var w=LiteraryVignettes.shared(d,PLACE);int song=(w.getInt("Song")+1)%(RECORDS.length+1);w.putInt("Song",song);LiteraryVignettes.shared(d,PLACE,w);play(l,b,song);
            p.displayClientMessage(Component.literal(song==0?"You press the reject button. The jukebox goes quiet.":"You press a button. The arm swings over and another record drops."),true);return true;}
        if(rel.equals(ElkLot.POOL_L)||rel.equals(ElkLot.POOL_R)){
            var left=b.offset(ElkLot.POOL_L);var st=l.getBlockState(left);if(!RoadhouseBlock.is(st,Kind.POOL_L))return true;int next=st.getValue(RoadhouseBlock.STAGE)==0?1:0;
            for(var at:List.of(left,b.offset(ElkLot.POOL_R))){var s=l.getBlockState(at);if(s.is(LiteraryRegistry.ROADHOUSE.get()))l.setBlock(at,s.setValue(RoadhouseBlock.STAGE,next),Block.UPDATE_CLIENTS);}
            l.playSound(null,left,LiteraryRegistry.LOT_POOL.get(),SoundSource.BLOCKS,1F,next==1?1F:.8F);
            p.displayClientMessage(Component.literal(next==1?"You break. Two stripes drop and the cue ball stops dead.":"You rack them up again."),true);
            var w=LiteraryVignettes.shared(d,PLACE);var denny=cast(l,w,DENNY);if(denny!=null&&next==1&&denny.getPersistentData().getInt("Leg0478")==0&&denny.distanceToSqr(p)<64)speak(denny,DENNY,"Not bad. Now sink something on purpose.",List.of(p));
            return true;}
        if(rel.equals(ElkLot.DARTS)){String[] throwsOf={"Three darts. One sticks, low, in the double ring.","Three darts. Two in the wall, one in the board.","Three darts. A bullseye. Nobody saw it."};int n=own.getInt("Darts");own.putInt("Darts",n+1);var at=b.offset(ElkLot.DARTS);var state=l.getBlockState(at);if(RoadhouseBlock.is(state,Kind.DARTBOARD))l.setBlock(at,state.setValue(RoadhouseBlock.STAGE,(n+1)%4),18);p.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            l.playSound(null,b.offset(ElkLot.DARTS),SoundEvents.ARROW_HIT,SoundSource.BLOCKS,.7F,1.6F);p.displayClientMessage(Component.literal(throwsOf[n%throwsOf.length]),true);return true;}
        if(rel.equals(ElkLot.BOARD)&&!own.getBoolean("Ready")){p.displayClientMessage(Component.literal("Flyers: a lost dog called Biscuit, a band on Thursday, a tractor for sale. One clipping is pinned face to the cork."),true);return true;}
        return false;
    }
    /** The clipping's last line, if the reader ran a tab. */
    public static String tab(CompoundTag own){int cents=own.getInt("Tab");return cents<=0?"":"\n\nThe bartender told the paper the tab was still open: "+money(cents)+", "+own.getInt("Drinks")+(own.getInt("Drinks")==1?" drink.":" drinks.");}
}
