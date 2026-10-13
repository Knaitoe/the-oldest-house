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
    private static final String SHELTER="rabbit_shelter0488",MARKER="the_oldest_house:holy_rabbit:";
    public static final BlockPos RABBIT=new BlockPos(6,0,-23);
    private static final double[][] WALK={{3.5,-22.5},{3.5,-18.5},{6.5,-18.5},{6.5,-23.5},{6.5,-18.5},{3.5,-18.5},{3.5,-22.5}};
    private static final double[][] LAST_HUNT={{3.5,-18.5},{6.5,-18.5},{6.5,-30.5},{.5,-30.5},{.5,-36.5}};
    private record Frost(ServerPlayer player,int before){}
    private static final Map<UUID,Frost> FROST=new HashMap<>();
    private HolyRabbit(){}

    public static void hunger(ServerPlayer p){p.getFoodData().setFoodLevel(1);p.getFoodData().setSaturation(0);p.getFoodData().setExhaustion(0);}
    private static void cold(ServerPlayer p,BlockPos b){
        var before=FROST.get(p.getUUID());if(before!=null&&before.player()!=p)release(before.player());
        FROST.computeIfAbsent(p.getUUID(),id->new Frost(p,p.getTicksFrozen()));
        boolean sheltered=Math.abs(p.getX()-b.getX())<3.5&&p.getZ()<b.getZ()-20&&p.getZ()>b.getZ()-29;
        p.setTicksFrozen(Math.max(p.getTicksFrozen(),sheltered?65:140));
    }
    public static void release(ServerPlayer p){var f=FROST.remove(p.getUUID());if(f!=null){f.player().setTicksFrozen(f.before());HousePackets.send(f.player(),new LiteraryViewPayload(-1,0,0,new CompoundTag()));}}
    public static void clearAll(){for(var f:new ArrayList<>(FROST.values()))release(f.player());}
    @SubscribeEvent public static void readerTick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(p.isAlive()&&LiteraryVignettes.inside(p,PLACE)&&LiteraryVignettes.retreatLocked(p)){hunger(p);cold(p,LiteraryVignettes.base(p,PLACE));}
        else release(p);
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
            rabbit.setVariant(Rabbit.Variant.WHITE_SPLOTCHED);rabbit.setNoAi(true);rabbit.setNoGravity(true);rabbit.setInvulnerable(true);rabbit.setPersistenceRequired();rabbit.addTag("HolyRabbit");
            rabbit.moveTo(Vec3.atBottomCenterOf(b.offset(RABBIT)));rabbit.setYRot(90);
            if(!p.serverLevel().addFreshEntity(rabbit))return null;
            world.putUUID(key,rabbit.getUUID());LiteraryVignettes.shared(data,PLACE,world);own.putBoolean("RabbitAnchor0488",true);
        }
        if(rabbit==null)return null;
        // Migrate the original circulating rabbit once, when no real camera can see the move.
        if(!own.getBoolean("RabbitAnchor0488")&&!LiteraryVignettes.watched(p.serverLevel(),rabbit.getEyePosition())&&!LiteraryVignettes.watched(p.serverLevel(),b.offset(RABBIT).getCenter())){
            rabbit.moveTo(Vec3.atBottomCenterOf(b.offset(RABBIT)));own.putBoolean("RabbitAnchor0488",true);
        }
        rabbit.setNoAi(true);rabbit.setNoGravity(true);rabbit.setDeltaMovement(Vec3.ZERO);
        var name=rabbit.getCustomName();if(name!=null&&!name.getString().isBlank())own.putString("RabbitName",name.getString());
        if(owner(rabbit).filter(p.getUUID()::equals).isEmpty())rabbit.setCustomName((name==null?Component.empty():name.copy()).withStyle(style->style.withInsertion(MARKER+p.getUUID())));
        return rabbit;
    }
    /** Both poles bear his weight. The same visible body takes the last route into the trees. */
    private static void lastHunt(ServerPlayer p,LiteraryActor father,BlockPos b,CompoundTag own,int ticks){
        int next=own.getInt("LastHuntStep0490");if(next>=LAST_HUNT.length){own.putBoolean("FatherGone",true);return;}
        var at=LAST_HUNT[next];var target=new Vec3(b.getX()+at[0],b.getY(),b.getZ()+at[1]);
        if(!p.serverLevel().hasChunkAt(BlockPos.containing(target))||!p.serverLevel().areEntitiesLoaded(ChunkPos.asLong(BlockPos.containing(target).getX()>>4,BlockPos.containing(target).getZ()>>4)))return;
        var delta=target.subtract(father.position());if(delta.lengthSqr()<.04){own.putInt("LastHuntStep0490",next+1);return;}
        float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));father.setYRot(yaw);father.setYHeadRot(yaw);father.yBodyRot=yaw;
        father.move(MoverType.SELF,delta.normalize().scale(Math.min(delta.length(),ticks%30<20?.22:.035)));father.setDeltaMovement(Vec3.ZERO);
    }
    private static void walk(LiteraryActor father,BlockPos b,CompoundTag own,int night,int ticks){
        if(night>=3){father.setDeltaMovement(Vec3.ZERO);return;}
        if(ticks<40)return;int next=own.contains("FatherWaypoint0488")?own.getInt("FatherWaypoint0488"):1;
        if(next>=WALK.length)return;
        var target=new Vec3(b.getX()+WALK[next][0],father.getY(),b.getZ()+WALK[next][1]);var delta=target.subtract(father.position()).multiply(1,0,1);
        if(delta.lengthSqr()<.04){own.putInt("FatherWaypoint0488",next+1);return;}
        father.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));father.setYHeadRot(father.getYRot());father.yBodyRot=father.getYRot();
        // A short weight-bearing step followed by a pause, with less distance covered on the second night.
        double gait=(ticks%30)<20?1:.22,speed=night==1?.09:.04;
        father.move(MoverType.SELF,delta.normalize().scale(Math.min(delta.length(),speed*5*gait)));
    }
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var father=LiteraryVignettes.actor(p,PLACE,"Father",LiteraryActor.FATHER,new BlockPos(3,0,-22),true);
        if(!LiteraryVignettes.retreatLocked(p)){release(p);return;}
        hunger(p);cold(p,b);p.stopRiding();p.stopFallFlying();
        int night=Math.max(1,own.getInt("Night"));own.putInt("Night",night);
        int ticks=own.getInt("NightTicks")+5;own.putInt("NightTicks",ticks);
        int drowse=own.getInt("Drowse")+5;own.putInt("Drowse",drowse);
        HousePackets.send(p,new LiteraryViewPayload(-1,20,Math.min(90,drowse/4),new CompoundTag()));
        if(father!=null){
            father.appearance(LiteraryActor.FATHER,own.getBoolean("LastHunt0490")?4:Math.min(3,night));
            if(own.getBoolean("LastHunt0490"))lastHunt(p,father,b,own,ticks);else walk(father,b,own,night,ticks);
            if(!own.getBoolean("LastHunt0490")&&drowse>=320&&father.distanceToSqr(p)<36&&p.hurt(p.damageSources().mobAttack(father),1)){own.putInt("Drowse",0);father.say("Stay with me.");}
            if(night>=3&&!father.isInvisible()&&p.distanceToSqr(father)<64&&HouseWatchers.sees(p,father.position().add(.18,.45,0)))own.putBoolean("WoundSeen0488",true);
        }
        var rabbit=rabbit(p,b,own);
        if(rabbit!=null&&ticks<100&&HouseWatchers.sees(p,rabbit.getEyePosition()))own.putInt("RabbitMornings",own.getInt("RabbitMornings")|(1<<Math.min(7,night)));
        boolean returned=night>=3||own.getInt("FatherWaypoint0488")>=WALK.length;
        if(ticks>=200&&returned&&own.getInt("MealNight")<night&&p.distanceToSqr(b.offset(0,0,-24).getCenter())<100){
            var meal=LiteraryVignettes.mark(p,PLACE,"Meal"+night,new ItemStack(LiteraryRegistry.MEAL.get()));var token=UUID.randomUUID();
            CustomData.update(DataComponents.CUSTOM_DATA,meal,t->{t.putInt("Night",night);t.putUUID("MealToken",token);});
            own.putUUID("MealToken",token);own.putInt("MealNight",night);LiteraryVignettes.give(p,meal);
            if(father!=null)father.say(night==1?"A rabbit. Keep the skin out of the snow. You need to eat.":night==2?"I went past the creek. Here. Take the meat first.":"There is some left. I ate on the way back.");
            if(night>=2)p.displayClientMessage(Component.literal("The same small skin is folded around the meat. His trousers are wet above the knees."),false);
        }
        if(ticks>=500&&own.getInt("MealNight")>=night&&own.getInt("AteNight")==night){
            own.putInt("Night",Math.min(8,night+1));own.putInt("NightTicks",0);own.remove("FatherWaypoint0488");
            if(night>=2&&!LiteraryVignettes.watched(p.serverLevel(),b.offset(0,3,-25).getCenter()))own.putBoolean("CarvingAppeared",true);
        }
        if(own.getBoolean("CarvingAppeared")&&HouseWatchers.sees(p,b.offset(0,3,-25).getCenter()))own.putBoolean("CarvingRead",true);
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
            if(!own.getBoolean("TrailOpen")){p.setTicksFrozen(200);p.hurt(p.damageSources().freeze(),2);}
            else{
                own.putInt("TrailTicks",own.getInt("TrailTicks")+5);if(own.getInt("TrailTicks")>600)p.hurt(p.damageSources().freeze(),2);
                if(p.getZ()<b.getZ()-118&&own.getInt("Meals")>=2&&own.getBoolean("FatherGone")&&own.getBoolean("CarvingRead")&&Integer.bitCount(own.getInt("RabbitMornings"))>=2){
                    if(!own.getBoolean("Rescue0490")){own.putBoolean("Rescue0490",true);p.playNotifySound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.AMBIENT,.55F,.6F);p.displayClientMessage(Component.literal("A light sweeps between the trees. A voice answers a radio. Behind it, the two poles keep moving. The rabbit is still alive."),false);}
                    own.putBoolean("Committed",false);LiteraryVignettes.ready(p,PLACE,own,"ate_and_followed_the_fathers_drag_trail");release(p);
                }
            }
        }
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
