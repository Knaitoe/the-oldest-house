package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.LiteraryViewPayload;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.*;

/** The same father and rabbit persist through the cold; another reader has their own account and cast. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HolyRabbit {
    private static final LabyrinthPlace PLACE=LabyrinthPlace.HOLY_RABBIT;
    public static final int DAY_TICKS=1800;
    public static long skyTime(int ticks){int t=Math.max(0,Math.min(DAY_TICKS,ticks));return t<900?1000+t*6000L/900:t<1200?7000+(t-900)*6000L/300:13000+(t-1200)*10000L/600;}
    private static Vec3 carving(ServerLevel l,BlockPos b){return new Vec3(b.getX()+.5,b.getY()+3.3,b.getZ()-23.95);}
    private static final String SHELTER="rabbit_shelter0488",MARKER="the_oldest_house:holy_rabbit:";
    public static final BlockPos RABBIT=new BlockPos(1,0,-18);
    private static final double[][] WALK={{3.5,-22.5},{3.5,-18.5},{6.5,-18.5},{1.5,-18.5},{6.5,-18.5},{3.5,-18.5},{3.5,-22.5}};
    private static final double[][] LAST_HUNT={{3.5,-18.5},{6.5,-18.5},{6.5,-30.5},{.5,-30.5},{.5,-36.5}};
    /** Three hearts: the cold and Father's shaking hurt, but the scene is about staying alive, and they never take the last of them. */
    public static final float FLOOR=6;
    /** Once Father has gone, a minute on his trail before the cold starts to bite (0.4.101; the walk takes about forty seconds). */
    public static final int TRAIL_GRACE=1200;
    private record Frost(ServerPlayer player,int before){}
    private static final Map<UUID,Frost> FROST=new HashMap<>();
    private HolyRabbit(){}

    public static void hunger(ServerPlayer p){p.getFoodData().setFoodLevel(1);p.getFoodData().setSaturation(0);p.getFoodData().setExhaustion(0);}
    private static boolean sheltered(ServerPlayer p,BlockPos b){return Math.abs(p.getX()-b.getX())<3.5&&p.getZ()<b.getZ()-20&&p.getZ()>b.getZ()-29;}
    private static void cold(ServerPlayer p,BlockPos b){
        var before=FROST.get(p.getUUID());if(before!=null&&before.player()!=p)release(before.player());
        FROST.computeIfAbsent(p.getUUID(),id->new Frost(p,p.getTicksFrozen()));
        frost(p,sheltered(p,b)?65:140);
    }
    /** Native freezing, kept just short of fully frozen (where vanilla's own freeze damage begins) once only three hearts are left. */
    private static void frost(ServerPlayer p,int ticks){
        int cap=p.getHealth()<=FLOOR+1?p.getTicksRequiredToFreeze()-1:Integer.MAX_VALUE;
        p.setTicksFrozen(Math.min(cap,Math.max(p.getTicksFrozen(),ticks)));
    }
    /** Hurts only while the hurt, even scaled up by difficulty, leaves three hearts. */
    private static boolean nip(ServerPlayer p,net.minecraft.world.damagesource.DamageSource source,float amount){
        return p.getHealth()-amount*1.5F>=FLOOR&&p.hurt(source,amount);
    }
    public static void release(ServerPlayer p){var f=FROST.remove(p.getUUID());if(f!=null){f.player().setTicksFrozen(f.before());HousePackets.send(f.player(),new LiteraryViewPayload(-1,0,0,new CompoundTag()));}}
    public static void clearAll(){for(var f:new ArrayList<>(FROST.values()))release(f.player());}
    @SubscribeEvent public static void readerTick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(p.isAlive()&&LiteraryVignettes.inside(p,PLACE)&&LiteraryVignettes.retreatLocked(p)){hunger(p);cold(p,LiteraryVignettes.base(p,PLACE));}
        else release(p);
    }

    /** Interpolate the original scripted throw through native collision on every server tick. */
    @SubscribeEvent public static void rabbitTick(EntityTickEvent.Post e){
        if(!(e.getEntity() instanceof Rabbit rabbit)||!(rabbit.level() instanceof ServerLevel level)||rabbit.isPassenger())return;
        var id=owner(rabbit).orElse(null);if(id==null)return;
        var reader=level.getServer().getPlayerList().getPlayer(id);
        if(reader==null||!LiteraryVignettes.inside(reader,PLACE)||!LiteraryVignettes.retreatLocked(reader))return;
        var own=LiteraryVignettes.personal(LabyrinthData.get(level.getServer()),id,PLACE);
        int t=own.getInt("HarvestTicks0491");if(t<35||t>75||!own.contains("TossScriptTick0496"))return;
        var b=IndianLakeRooms.base(level.getServer(),PLACE);if(b==null)return;
        double sub=Math.max(0L,Math.min(5L,level.getGameTime()-own.getLong("TossScriptTick0496")));
        double f=Math.min(1,(t+sub-35)/40D);
        var from=new Vec3(own.getDouble("TossX0491"),own.getDouble("TossY0491"),own.getDouble("TossZ0491"));
        var target=from.lerp(Vec3.atBottomCenterOf(b.offset(RABBIT)),f).add(0,2.4*f*(1-f),0);
        if(!level.hasChunkAt(BlockPos.containing(target)))return;
        rabbit.move(MoverType.SELF,target.subtract(rabbit.position()));rabbit.setDeltaMovement(Vec3.ZERO);
    }

    /** Native synced name styling carries the private renderer marker without changing a named rabbit's words. */
    public static Optional<UUID> owner(Rabbit rabbit){
        var name=rabbit.getCustomName();var insertion=name==null?null:name.getStyle().getInsertion();
        if(insertion==null||!insertion.startsWith(MARKER))return Optional.empty();
        try{return Optional.of(UUID.fromString(insertion.substring(MARKER.length())));}catch(IllegalArgumentException ignored){return Optional.empty();}
    }
    private static Rabbit rabbit(ServerPlayer p,BlockPos b,CompoundTag own){
        var data=LabyrinthData.get(p.server);var world=LiteraryVignettes.shared(data,PLACE);String key="Rabbit_"+p.getUUID();Rabbit rabbit=null;
        if(world.hasUUID(key)){if(p.serverLevel().getEntity(world.getUUID(key)) instanceof Rabbit existing)rabbit=existing;}
        else{
            rabbit=EntityType.RABBIT.create(p.serverLevel());if(rabbit==null)return null;
            rabbit.setVariant(Rabbit.Variant.WHITE_SPLOTCHED);rabbit.setCustomName(Component.literal("Slaney"));rabbit.setNoAi(true);rabbit.setNoGravity(true);rabbit.setInvulnerable(true);rabbit.setPersistenceRequired();rabbit.addTag("HolyRabbit");
            rabbit.moveTo(Vec3.atBottomCenterOf(b.offset(RABBIT)));rabbit.setYRot(90);
            if(!p.serverLevel().addFreshEntity(rabbit))return null;
            world.putUUID(key,rabbit.getUUID());LiteraryVignettes.shared(data,PLACE,world);own.putBoolean("RabbitAnchor0488",true);own.putBoolean("RabbitFront0491",true);
        }
        if(rabbit==null)return null;
        // Migrate the original circulating rabbit once, when no real camera can see the move.
        if(!own.getBoolean("RabbitFront0491")&&!rabbit.isPassenger()&&!LiteraryVignettes.watched(p.serverLevel(),rabbit.getEyePosition())&&!LiteraryVignettes.watched(p.serverLevel(),b.offset(RABBIT).getCenter())){
            rabbit.moveTo(Vec3.atBottomCenterOf(b.offset(RABBIT)));own.putBoolean("RabbitAnchor0488",true);own.putBoolean("RabbitFront0491",true);
        }
        rabbit.setNoAi(true);rabbit.setNoGravity(true);rabbit.setDeltaMovement(Vec3.ZERO);
        var name=rabbit.getCustomName();if(name!=null&&!name.getString().isBlank())own.putString("RabbitName",name.getString());
        if(owner(rabbit).filter(p.getUUID()::equals).isEmpty())rabbit.setCustomName((name==null?Component.empty():name.copy()).withStyle(style->style.withInsertion(MARKER+p.getUUID())));
        return rabbit;
    }
    /** Both poles bear his weight. The same visible body takes the last route into the trees. */
    private static void lastHunt(ServerPlayer p,LiteraryActor father,BlockPos b,CompoundTag own,int ticks){
        int next=own.getInt("LastHuntStep0490");if(next>=LAST_HUNT.length){LiteraryMotion.stop(father);own.putBoolean("FatherGone",true);return;}
        var at=LAST_HUNT[next];var target=new Vec3(b.getX()+at[0],b.getY(),b.getZ()+at[1]);
        if(!p.serverLevel().hasChunkAt(BlockPos.containing(target))||!p.serverLevel().areEntitiesLoaded(ChunkPos.asLong(BlockPos.containing(target).getX()>>4,BlockPos.containing(target).getZ()>>4)))return;
        var delta=target.subtract(father.position());if(delta.lengthSqr()<.04){own.putInt("LastHuntStep0490",next+1);return;}
        LiteraryMotion.toward(father,target,ticks%30<20?.22:.035);father.setDeltaMovement(Vec3.ZERO);
    }
    private static void walk(LiteraryActor father,BlockPos b,CompoundTag own,int night,int ticks){
        if(night>=3){LiteraryMotion.stop(father);return;}
        if(ticks<40)return;int next=own.contains("FatherWaypoint0488")?own.getInt("FatherWaypoint0488"):1;
        if(next>=WALK.length){LiteraryMotion.stop(father);return;}
        var target=new Vec3(b.getX()+WALK[next][0],father.getY(),b.getZ()+WALK[next][1]);var delta=target.subtract(father.position()).multiply(1,0,1);
        if(delta.lengthSqr()<.04){own.putInt("FatherWaypoint0488",next+1);return;}
        // A short weight-bearing step followed by a pause, with less distance covered on the second night.
        double gait=(ticks%30)<20?1:.22,speed=night==1?.09:.04;
        LiteraryMotion.toward(father,target,speed*5*gait);
    }
    /** The apparent harvest uses the same living native rabbit, carried and tossed without replacing it. */
    private static boolean harvest(ServerPlayer p,LiteraryActor father,Rabbit rabbit,BlockPos b,CompoundTag own,int day){
        if(day>=3||rabbit==null||own.getInt("HarvestDay0491")>=day)return false;
        if(own.getInt("MealNight")>=day){own.putInt("HarvestDay0491",day);return false;}
        if(own.getInt("FatherWaypoint0488")<4)return false;
        // He will not take the rabbit until the reader has seen it alive this morning; he stands and points until they look.
        if(own.getInt("HarvestTicks0491")==0&&(own.getInt("RabbitMornings")&(1<<Math.min(7,day)))==0){
            LiteraryMotion.stop(father);father.gesture(1);
            float yaw=(float)Math.toDegrees(Math.atan2(-(rabbit.getX()-father.getX()),rabbit.getZ()-father.getZ()));father.setYRot(yaw);father.setYHeadRot(yaw);father.yBodyRot=yaw;
            int wait=own.getInt("RabbitWait0501")+5;own.putInt("RabbitWait0501",wait);
            if(wait%200==5){String line=day==1?"Look. By the snow. Look at him before I go.":"He came back. Go on, look at him.";
                father.say(line);father.getPersistentData().putLong("SayUntil0488",p.level().getGameTime()+90);p.displayClientMessage(Component.literal("Father: "+line),false);}
            return true;
        }
        int t=own.getInt("HarvestTicks0491")+5;own.putInt("HarvestTicks0491",t);father.gesture(2);LiteraryMotion.stop(father);
        if(t<=30){rabbit.setPose(Pose.DYING);if(!rabbit.isPassenger())rabbit.startRiding(father,true);if(t==5){father.say(day==1?"Look away a moment. I'll make something for us.":"Small bites again. You remember how to eat it.");father.getPersistentData().putLong("SayUntil0488",p.level().getGameTime()+90);}return true;}
        if(t==35){rabbit.stopRiding();var from=rabbit.position();own.putDouble("TossX0491",from.x);own.putDouble("TossY0491",from.y);own.putDouble("TossZ0491",from.z);}
        if(t<=75){own.putLong("TossScriptTick0496",p.serverLevel().getGameTime());return true;}
        own.putInt("HarvestDay0491",day);own.putBoolean("RabbitDiscarded0491",true);father.gesture(0);return false;
    }
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var father=LiteraryVignettes.actor(p,PLACE,"Father",LiteraryActor.FATHER,new BlockPos(3,0,-22),true);
        if(!LiteraryVignettes.retreatLocked(p)){release(p);return;}
        hunger(p);cold(p,b);p.stopRiding();p.stopFallFlying();
        int night=Math.max(1,own.getInt("Night"));own.putInt("Night",night);
        int ticks=own.getInt("NightTicks")+5;own.putInt("NightTicks",ticks);
        int drowse=own.getInt("Drowse")+5;own.putInt("Drowse",drowse);
        var sky=new CompoundTag();sky.putUUID("Reader",p.getUUID());sky.putBoolean("RabbitScene0491",true);sky.putInt("RabbitDayTicks",ticks);sky.putIntArray("RabbitBase",new int[]{b.getX(),b.getY(),b.getZ()});
        HousePackets.send(p,new LiteraryViewPayload(-1,20,Math.min(28,drowse/20),sky));
        var rabbit=rabbit(p,b,own);
        if(ticks<=10&&rabbit!=null){rabbit.stopRiding();rabbit.setPose(Pose.STANDING);own.putBoolean("RabbitDiscarded0491",false);own.putInt("HarvestTicks0491",0);}
        if(father!=null){
            father.appearance(LiteraryActor.FATHER,own.getBoolean("LastHunt0490")?4:Math.min(3,night));
            father.gesture(ticks<100?(night<3?1:3):0);
            if(own.getBoolean("LastHunt0490"))lastHunt(p,father,b,own,ticks);else if(!harvest(p,father,rabbit,b,own,night))walk(father,b,own,night,ticks);
            if(ticks==15&&rabbit!=null){String name=own.getString("RabbitName");String line=night==1?"There. By the snow. The little one from your dream.":night==2?"Look. "+name+"'s there again. Same brown ear.":"He's there. I can see him. You'll have to look for yourself today.";father.say(line);father.getPersistentData().putLong("SayUntil0488",p.level().getGameTime()+100);p.displayClientMessage(Component.literal("Father: "+line),false);}
            if(!own.getBoolean("LastHunt0490")&&drowse>=320&&father.distanceToSqr(p)<36){nip(p,p.damageSources().mobAttack(father),1);own.putInt("Drowse",0);father.say("Stay with me.");}
            if(night>=3&&ticks>=120&&!father.isInvisible()&&p.distanceToSqr(father)<64&&HouseWatchers.sees(p,father.position().add(.18,.45,0)))own.putBoolean("WoundSeen0488",true);
        }
        if(rabbit!=null&&own.getInt("HarvestTicks0491")==0&&!own.getBoolean("RabbitDiscarded0491")&&HouseWatchers.sees(p,rabbit.getEyePosition()))own.putInt("RabbitMornings",own.getInt("RabbitMornings")|(1<<Math.min(7,night)));
        HolyRabbitDialogue.tick(p,father,own,night,ticks);
        boolean returned=night>=3||own.getInt("FatherWaypoint0488")>=WALK.length;
        if(ticks>=200&&returned&&own.getInt("MealNight")<night&&p.distanceToSqr(b.offset(0,0,-24).getCenter())<100){
            var meal=LiteraryVignettes.mark(p,PLACE,"Meal"+night,new ItemStack(LiteraryRegistry.MEAL.get()));var token=UUID.randomUUID();
            CustomData.update(DataComponents.CUSTOM_DATA,meal,t->{t.putInt("Night",night);t.putUUID("MealToken",token);});
            own.putUUID("MealToken",token);own.putInt("MealNight",night);LiteraryVignettes.give(p,meal);
            HolyRabbitDialogue.meal(p,father,own,night);
        }
        if(night<3&&ticks>=DAY_TICKS&&own.getInt("MealNight")>=night&&own.getInt("AteNight")==night){
            own.putInt("Night",night+1);own.putInt("NightTicks",0);own.putInt("Drowse",0);own.remove("FatherWaypoint0488");own.putInt("HarvestTicks0491",0);own.remove("RabbitWait0501");
        }
        // The carving is cut while nobody is looking: on the last morning, or the first moment after it that nobody is.
        if(night>=3&&!own.getBoolean("CarvingAppeared")&&!LiteraryVignettes.watched(p.serverLevel(),carving(p.serverLevel(),b)))own.putBoolean("CarvingAppeared",true);
        if(own.getBoolean("CarvingAppeared")&&HouseWatchers.sees(p,carving(p.serverLevel(),b)))own.putBoolean("CarvingRead",true);
        // Older earned trails remain earned. Fresh trails require seeing why the rabbit is still alive.
        boolean revealed=own.getBoolean("WoundSeen0488")||own.getBoolean("TrailOpen")&&own.getBoolean("FatherGone");
        if(night>=3&&own.getInt("Meals")>=2&&revealed){
            own.putBoolean("TrailOpen",true);
            if(father!=null&&p.getZ()<b.getZ()-33&&!own.getBoolean("LastHunt0490")&&!own.getBoolean("FatherGone")){
                own.putBoolean("LastHunt0490",true);father.setInvisible(false);father.say("I have to go again.");
                p.displayClientMessage(Component.literal("The boot marks cross the creek trail. Blood runs into both tracks. He plants two poles and lifts himself after them."),false);
            }
        }
        if(p.getZ()<b.getZ()-33){
            if(!own.getBoolean("TrailOpen")){
                frost(p,200);nip(p,p.damageSources().freeze(),2);
                if(!own.getBoolean("TooSoon0501")){own.putBoolean("TooSoon0501",true);p.displayClientMessage(Component.literal("Past the trees the wind takes your breath away. Father hasn't gone that way."),false);}
            }else{
                // The escape cold starts only once his retreat has finished (0.4.90), and under the tree it starts over.
                if(own.getBoolean("FatherGone"))own.putInt("TrailTicks",own.getInt("TrailTicks")+5);
                if(own.getInt("TrailTicks")>TRAIL_GRACE)nip(p,p.damageSources().freeze(),2);
                // The carving is something to remember him by, not a key: the way out needs the meals, the mornings and his going.
                if(p.getZ()<b.getZ()-118&&own.getInt("Meals")>=2&&own.getBoolean("FatherGone")&&Integer.bitCount(own.getInt("RabbitMornings"))>=2){
                    if(!own.getBoolean("Rescue0490")){own.putBoolean("Rescue0490",true);p.playNotifySound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.AMBIENT,.55F,.6F);p.displayClientMessage(Component.literal("A light sweeps between the trees. A voice answers a radio. Behind it, the two poles keep moving. The rabbit is still alive."),false);}
                    own.putBoolean("Committed",false);LiteraryVignettes.ready(p,PLACE,own,"ate_and_followed_the_fathers_drag_trail");release(p);
                }
            }
        }
        else if(sheltered(p,b))own.putInt("TrailTicks",0);
        if(own.getInt("Present")%100==0)p.playNotifySound(LiteraryRegistry.BLIZZARD.get(),SoundSource.AMBIENT,.8F,1);
    }

    /** The father's narrow entrance has actual standing headroom; the carved tree and child's hollow stay intact. */
    public static void shelter(ServerLevel l,BlockPos b){
        LiteraryRooms.box(l,b,3,0,-23,4,2,-18,Blocks.AIR);
        var data=LabyrinthData.get(l.getServer());var state=data.state(SHELTER);state.putBoolean(Long.toString(b.asLong()),true);data.setState(SHELTER,state);
    }
    public static boolean repairShelter(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());var state=data.state(SHELTER);String key=Long.toString(b.asLong());if(state.getBoolean(key))return true;
        var area=new AABB(Vec3.atLowerCornerOf(b.offset(3,0,-23)),Vec3.atLowerCornerOf(b.offset(5,3,-17)));
        for(int x=3;x<=4;x++)for(int z=-23;z<=-18;z++){var at=b.offset(x,0,z);if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;}
        if(l.players().stream().anyMatch(p->area.inflate(48).intersects(p.getCamera().getBoundingBox()))||!l.getEntitiesOfClass(LivingEntity.class,area.inflate(.3),e->e.isAlive()&&!(e instanceof LiteraryActor a&&a.role()==LiteraryActor.FATHER&&a.scene().equals(PLACE.id())&&Math.abs(a.getY()-b.getY())<.1)).isEmpty())return false;
        for(int x=3;x<=4;x++)for(int y=0;y<=2;y++)for(int z=-23;z<=-18;z++){var at=b.offset(x,y,z);if(l.getBlockEntity(at)==null&&l.getBlockState(at).is(Blocks.SPRUCE_LEAVES))l.setBlock(at,Blocks.AIR.defaultBlockState(),18);}
        state.putBoolean(key,true);data.setState(SHELTER,state);return true;
    }
    @SubscribeEvent public static void repairTick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=17||LabyrinthBuilder.isCarving()||!LabyrinthBuilder.isPlaceReady(s,PLACE))return;
        var origin=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(NovelRooms.dimension(PLACE));if(origin!=null&&l!=null)repairShelter(l,LabyrinthPlaces.base(origin,PLACE));
    }
}
