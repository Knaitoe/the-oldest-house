package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.UncannyPropBlock.Kind.*;

/** Actual verbs and consequences. Clocks advance only for present, living readers outside a reading menu. */
public final class UncannyStories {
    public static final String WORLD="uncanny_world_04113";
    private static final int F=UncannyRooms.FLAGS;
    private UncannyStories(){}
    public static boolean role(int role){return role>=LiteraryActor.UNCANNY_PROPPED&&role<=LiteraryActor.UNCANNY_VISITOR;}
    public static boolean green(CompoundTag own){return own.getInt("UncannyAge")%240>=140;}
    public static boolean failed(CompoundTag own){return own.getBoolean("UncannyFailed");}
    private static CompoundTag world(ServerLevel l,BlockPos b,LabyrinthPlace place){return LabyrinthData.get(l.getServer()).stateEntry(WORLD,place.id()+":"+b.asLong());}
    private static void world(ServerLevel l,BlockPos b,LabyrinthPlace place,CompoundTag tag){LabyrinthData.get(l.getServer()).setStateEntry(WORLD,place.id()+":"+b.asLong(),tag);}
    public static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag own,String key){return own.hasUUID(key)&&p.serverLevel().getEntity(own.getUUID(key)) instanceof LiteraryActor a?a:null;}
    private static @Nullable LiteraryActor create(ServerLevel l,BlockPos b,LabyrinthPlace place,CompoundTag tag,String key,int role,BlockPos rel,@Nullable UUID reader){
        if(tag.hasUUID(key))return l.getEntity(tag.getUUID(key)) instanceof LiteraryActor a?a:null;
        var at=b.offset(rel);if(!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(place,reader);a.appearance(role,0);a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);a.setDeltaMovement(Vec3.ZERO);
        if(role==LiteraryActor.UNCANNY_FALSE_WAITRESS){a.noPhysics=true;a.setNoGravity(true);}
        if(!l.addFreshEntity(a))return null;tag.putUUID(key,a.getUUID());return a;
    }
    public static void prepare(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var shared=world(l,b,place);
        if(place==LabyrinthPlace.SECOND_WAITRESS)create(l,b,place,shared,"Waitress",LiteraryActor.UNCANNY_WAITRESS,new BlockPos(-8,0,-9),null);
        if(place==LabyrinthPlace.LAYERED_MAN){var worker=create(l,b,place,shared,"Peeler",LiteraryActor.UNCANNY_PEELER,new BlockPos(-3,-5,-19),null);if(worker!=null&&!shared.getBoolean("ToolGiven")){var blade=new ItemStack(Items.IRON_AXE);blade.setDamageValue(blade.getMaxDamage()-9);worker.setItemSlot(EquipmentSlot.MAINHAND,blade);shared.putBoolean("ToolGiven",true);}}
        world(l,b,place,shared);
    }
    public static void arrive(ServerPlayer p,LabyrinthPlace place,CompoundTag own){
        var b=LiteraryVignettes.base(p,place);if(b==null)return;prepare(p.serverLevel(),b,place);
        if(!own.contains("UncannyAge")){own.putInt("UncannyAge",0);own.putInt("NightClock",0);}
        if(place==LabyrinthPlace.PAINTED_INTERIOR)for(int i=0;i<4;i++)create(p.serverLevel(),b,place,own,"CastP"+i,LiteraryActor.UNCANNY_PROPPED,new BlockPos(i%2==0?-6:6,0,i<2?-18:-25),p.getUUID());
        if(place==LabyrinthPlace.SECOND_WAITRESS){
            create(p.serverLevel(),b,place,own,"CastFake",LiteraryActor.UNCANNY_FALSE_WAITRESS,new BlockPos(-7,0,-23),p.getUUID());
            if(!own.contains("OriginalMeal")&&!own.getBoolean("MealReleased")){
                var s=new ItemStack(UncannyRegistry.MEAL.get());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID("UncannyMealOwner",p.getUUID());t.putUUID("UncannyMeal",UUID.randomUUID());});own.put("OriginalMeal",s.save(p.registryAccess()));
            }
        }
        if(place==LabyrinthPlace.TUNNEL_PERIPHERY)create(p.serverLevel(),b,place,own,"CastGiant",LiteraryActor.UNCANNY_GIANT,new BlockPos(0,0,-2),p.getUUID());
        if(place==LabyrinthPlace.MISSING_SLEEP){var doll=create(p.serverLevel(),b,place,own,"CastDoll",LiteraryActor.UNCANNY_MARIONETTE,new BlockPos(1,5,-27),p.getUUID());if(doll!=null&&!own.getBoolean("DollBatGiven")){doll.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(UncannyRegistry.BAT.get()));own.putBoolean("DollBatGiven",true);}}
        if(place==LabyrinthPlace.MOTION_LIGHT)create(p.serverLevel(),b,place,own,"CastVisitor",LiteraryActor.UNCANNY_VISITOR,new BlockPos(-6,0,-23),p.getUUID());
        own.putBoolean("UncannyArrived",true);
    }
    public static void reader(ServerPlayer p,BlockPos b,LabyrinthPlace place,CompoundTag own){
        arrive(p,place,own);
        if(own.getBoolean("Completed")||failed(own)||p.containerMenu!=p.inventoryMenu)return;
        int age=own.getInt("UncannyAge");if(own.getBoolean("Read_Source"))age=Math.min(72000,age+5);own.putInt("UncannyAge",age);
        switch(place){
            case PAINTED_INTERIOR->boarded(p,b,own);case SECOND_WAITRESS->diner(p,b,own);case TUNNEL_PERIPHERY->tunnel(p,b,own);
            case MISSING_SLEEP->bedroom(p,b,own);case LAYERED_MAN->cellar(p,b,own);case MOTION_LIGHT->foyer(p,b,own);default->{}
        }
    }
    private static void ready(ServerPlayer p,LabyrinthPlace place,CompoundTag own,String outcome){if(own.getBoolean("Read_Source")&&!failed(own))LiteraryVignettes.ready(p,place,own,outcome);}
    private static void heard(ServerPlayer p,SoundEvent sound,Vec3 at,float volume,float pitch){
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.BLOCKS,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    private static void cue(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text).withStyle(net.minecraft.ChatFormatting.GRAY),true);}
    private static void caught(ServerPlayer p,BlockPos b,LabyrinthPlace place,CompoundTag own,String outcome){
        if(failed(own))return;own.putBoolean("UncannyFailed",true);own.putString("Failure",outcome);own.putBoolean("Ready",false);
        HousePackets.send(p,new HouseFadePayload(10,8,16));var safe=Vec3.atBottomCenterOf(b.offset(0,0,-3));
        p.teleportTo(p.serverLevel(),safe.x,safe.y,safe.z,0,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);
        cue(p,switch(place){case SECOND_WAITRESS->"The place setting in front of you is empty.";case TUNNEL_PERIPHERY->"A hand on the back of your neck.";case MISSING_SLEEP->"The monitor is still showing the empty bed.";case MOTION_LIGHT->"The buzzer stops.";default->"You are facing the painted sun again.";});
    }
    private static void boarded(ServerPlayer p,BlockPos b,CompoundTag own){
        int age=own.getInt("UncannyAge");if(age==0)return;int nearest=0;double proximity=100;
        for(int i=0;i<4;i++){
            var a=actor(p,own,"CastP"+i);if(a==null)continue;a.appearance(LiteraryActor.UNCANNY_PROPPED,1);
            double angle=UncannySenses.angle(p,a.getEyePosition()),half=UncannySenses.halfFov(p);
            if(angle>half*.52&&angle<half*1.04&&UncannySenses.clear(p,a.getEyePosition())){
                double d=a.distanceToSqr(p);if(d<proximity){proximity=d;var look=UncannySenses.look(p);var delta=a.position().subtract(p.position());nearest=look.x*delta.z-look.z*delta.x>0?1:-1;}
                own.putInt("EdgeSeen",own.getInt("EdgeSeen")|1<<i);
            }else if(age%30==0&&!UncannySenses.mirror(p)){
                double yaw=Math.atan2(UncannySenses.look(p).z,UncannySenses.look(p).x)+(i%2==0?-1:1)*half*.91;
                double radius=Math.max(1.15,4-age/650.);var target=p.position().add(Math.cos(yaw)*radius,0,Math.sin(yaw)*radius);
                // The original crosses physical floor space while hidden by the direct-gaze rule.
                if(target.x>b.getX()-7&&target.x<b.getX()+7&&target.z<b.getZ()-15&&target.z>b.getZ()-27)SceneFootpaths.step(a,target,.32);
            }
        }
        own.putInt("Edge",nearest);
        if(nearest!=0&&age%30==0){var right=new Vec3(-p.getLookAngle().z,0,p.getLookAngle().x).scale(nearest*2.5);heard(p,SoundEvents.WOOD_STEP,p.position().add(right),.65F,.8F);cue(p,nearest<0?"< Wood creaks":"Wood creaks >");}
        if(own.getInt("EdgeSeen")!=0&&own.getBoolean("BoardOpened")&&p.getX()>b.getX()+8.5&&p.getZ()<b.getZ()-16)ready(p,LabyrinthPlace.PAINTED_INTERIOR,own,"left_by_the_unpainted_door");
        if(proximity<1.1&&corner(p)&&!own.getBoolean("Ready"))caught(p,b,LabyrinthPlace.PAINTED_INTERIOR,own,"backed_into_the_corners");
    }
    private static boolean corner(ServerPlayer p){
        var at=p.blockPosition();int sides=0;for(var d:Direction.Plane.HORIZONTAL)if(!p.serverLevel().getBlockState(at.relative(d)).getCollisionShape(p.serverLevel(),at.relative(d)).isEmpty())sides++;return sides>=2;
    }
    private static void diner(ServerPlayer p,BlockPos b,CompoundTag own){
        int age=own.getInt("UncannyAge");var fake=actor(p,own,"CastFake");if(fake==null)return;
        int stage=age<240?0:age<600?1:age<1200?2:3;own.putInt("UncannyStage",stage);fake.appearance(LiteraryActor.UNCANNY_FALSE_WAITRESS,stage);fake.noPhysics=stage<2;fake.setNoGravity(stage<2);
        if(age>0&&age%60==0){heard(p,SoundEvents.CHAIN_STEP,Vec3.atCenterOf(b.offset(UncannyRooms.DINER_TABLE)),.55F,1.65F);p.serverLevel().sendParticles(p,ParticleTypes.SNOWFLAKE,true,b.getX()+8,b.getY()+1.1,b.getZ()-19,7,.35,.2,.4,.005);cue(p,"Silverware rattles. Frost gathers by booth four.");}
        if(stage==0&&age>0&&age%100==0){var wind=p.position().subtract(Vec3.atCenterOf(b.offset(8,1,-19))).normalize().scale(.045);p.push(wind.x,0,wind.z);p.hurtMarked=true;}
        if(stage==1){var target=Vec3.atBottomCenterOf(b.offset(-5,0,-20));nativeStep(fake,target,.35);}
        if(stage>=2&&!own.getBoolean("MealReleased")){
            var target=Vec3.atBottomCenterOf(b.offset(3,0,-19));SceneFootpaths.step(fake,target,.42);
            if(fake.position().distanceToSqr(target)<.4&&own.contains("OriginalMeal")){
                var meal=ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalMeal"));
                fake.setItemSlot(EquipmentSlot.MAINHAND,meal);own.remove("OriginalMeal");own.putBoolean("MealReleased",true);
            }
        }
        if(own.getBoolean("MealReleased")&&!own.getBoolean("MealServed")&&!fake.getMainHandItem().isEmpty()){
            fake.gesture(1);var plate=fake.getMainHandItem();fake.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            var e=new ItemEntity(p.serverLevel(),b.getX()+5.5,b.getY()+1.08,b.getZ()-18.5,plate);e.setTarget(p.getUUID());e.setNoGravity(true);e.setDeltaMovement(Vec3.ZERO);
            if(p.serverLevel().addFreshEntity(e)){own.putUUID("PlateEntity",e.getUUID());own.putBoolean("MealServed",true);}else fake.setItemSlot(EquipmentSlot.MAINHAND,plate);
        }
        if(stage>=3&&!own.getBoolean("Refused")&&!own.getBoolean("Ready")){
            SceneFootpaths.step(fake,p.position(),.52);if(fake.distanceToSqr(p)<1.1)caught(p,b,LabyrinthPlace.SECOND_WAITRESS,own,"let_her_clear_the_table");
        }
        if(own.getBoolean("Refused")&&own.getBoolean("MealServed"))ready(p,LabyrinthPlace.SECOND_WAITRESS,own,"left_the_second_waitress_meal");
    }
    public static void ate(ServerPlayer p,ItemStack s){
        var c=s.get(DataComponents.CUSTOM_DATA);if(c==null||!c.copyTag().hasUUID("UncannyMealOwner")||!c.copyTag().getUUID("UncannyMealOwner").equals(p.getUUID())||!LiteraryVignettes.inside(p,LabyrinthPlace.SECOND_WAITRESS))return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.SECOND_WAITRESS);caught(p,LiteraryVignettes.base(p,LabyrinthPlace.SECOND_WAITRESS),LabyrinthPlace.SECOND_WAITRESS,own,"ate_what_she_brought");LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.SECOND_WAITRESS,own);
    }
    private static void tunnel(ServerPlayer p,BlockPos b,CompoundTag own){
        int age=own.getInt("UncannyAge");var giant=actor(p,own,"CastGiant");if(age==0||giant==null)return;
        giant.appearance(LiteraryActor.UNCANNY_GIANT,green(own)?1:2);own.putInt("UncannyStage",green(own)?1:2);
        if(age%40==0){heard(p,green(own)?SoundEvents.NOTE_BLOCK_HAT.value():SoundEvents.NOTE_BLOCK_BASS.value(),p.position().add(0,2,-4),.55F,green(own)?1.7F:.6F);cue(p,green(own)?"GREEN / || / She is still":"RED / — / Footsteps");}
        if(green(own)){own.putInt("GreenSteps",own.getInt("GreenSteps")+(own.contains("LastZ")&&p.getZ()<own.getDouble("LastZ")-.08?1:0));giant.setDeltaMovement(Vec3.ZERO);}
        else{
            double moved=own.contains("LastX")?Math.hypot(p.getX()-own.getDouble("LastX"),p.getZ()-own.getDouble("LastZ")):0;
            SceneFootpaths.step(giant,p.position().add(0,0,1),Math.max(.55,Math.min(1.65,moved)));
            if(giant.distanceToSqr(p)<1.7&&age>120&&!own.getBoolean("Ready"))caught(p,b,LabyrinthPlace.TUNNEL_PERIPHERY,own,"paced_to_the_last_red_light");
        }
        own.putDouble("LastX",p.getX());own.putDouble("LastZ",p.getZ());
        if(p.getZ()<b.getZ()-66&&own.getInt("GreenSteps")>=5)ready(p,LabyrinthPlace.TUNNEL_PERIPHERY,own,"passed_the_last_green_signal");
    }
    private static void bedroom(ServerPlayer p,BlockPos b,CompoundTag own){
        int sleeps=own.getInt("Sleeps");if(sleeps==1&&own.getInt("UncannyAge")>own.getInt("FirstSleepAge")+1800){caught(p,b,LabyrinthPlace.MISSING_SLEEP,own,"waited_out_the_first_interval");return;}var doll=actor(p,own,"CastDoll");if(doll==null)return;
        doll.appearance(LiteraryActor.UNCANNY_MARIONETTE,sleeps>=2?1:0);own.putInt("UncannyStage",sleeps>=2?2:sleeps);
        var lens=p.getPersistentData().getCompound(UncannySenses.LENS);
        if(UncannySenses.fresh(p)&&lens.getInt("Monitor")>=0&&p.distanceToSqr(b.offset(UncannyRooms.MONITOR_AT).getCenter())<25&&UncannySenses.clear(p,Vec3.atLowerCornerOf(b.offset(UncannyRooms.MONITOR_AT)).add(.5,.6,.93))){
            int channel=lens.getInt("Monitor");own.putInt("FeedsSeen",own.getInt("FeedsSeen")|1<<channel);own.putInt("MonitorTicks",own.getInt("MonitorTicks")+5);
            if(sleeps>=2&&channel==1&&doll.getZ()<b.getZ()-15)own.putBoolean("OwnFaceSeen",true);
        }
        if(sleeps>=2){
            var delta=doll.getEyePosition().subtract(UncannySenses.eye(p));if(delta.lengthSqr()<100&&UncannySenses.look(p).dot(delta.normalize())>.8&&UncannySenses.clear(p,doll.getEyePosition()))own.putBoolean("MarionetteSeen",true);
            if(!own.getBoolean("BlockedDoor")){
                var target=doll.getZ()<b.getZ()-15.5?new Vec3(b.getX()+1.5,b.getY()+5,b.getZ()-15.5):new Vec3(b.getX()+4,b.getY()+5,b.getZ()-15.5);
                SceneFootpaths.step(doll,target,.13);if(doll.getX()>b.getX()+3.1){doll.gesture(2);if(own.getInt("UncannyAge")>own.getInt("SecondSleepAge")+300)caught(p,b,LabyrinthPlace.MISSING_SLEEP,own,"watched_the_hand_reach_the_door");}
            }
            if(own.getInt("UncannyAge")>own.getInt("SecondSleepAge")+2400&&!own.getBoolean("BlockedDoor"))caught(p,b,LabyrinthPlace.MISSING_SLEEP,own,"waited_out_the_empty_bed");
            if(own.getInt("UncannyAge")%45==0){heard(p,SoundEvents.FOX_SCREECH,doll.getEyePosition(),.12F,.55F);cue(p,"Hallway / clicking. CAM 02 / subject whistling.");}
        }
    }
    private static void cellar(ServerPlayer p,BlockPos b,CompoundTag own){
        var place=LabyrinthPlace.LAYERED_MAN;prepare(p.serverLevel(),b,place);var shared=world(p.serverLevel(),b,place);
        if(own.getInt("UncannyAge")==0)return;
        long now=p.serverLevel().getGameTime();
        if(shared.getLong("Tick")!=now){
            shared.putLong("Tick",now);shared.putInt("Age",shared.getInt("Age")+5);
            var worker=shared.hasUUID("Peeler")&&p.serverLevel().getEntity(shared.getUUID("Peeler")) instanceof LiteraryActor a?a:null;
            if(worker!=null){int stage=shared.getInt("Age")<400?0:shared.getInt("Age")<800?1:2;worker.appearance(LiteraryActor.UNCANNY_PEELER,stage);worker.gesture(1);if(stage>=1)worker.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                if(stage==0&&shared.getInt("Age")%40==0){var cut=b.offset(-1,-3,-19);var skin=p.serverLevel().getBlockState(cut);if(skin.is(UncannyRegistry.PROP.get())&&skin.getValue(UncannyPropBlock.KIND)==FLESH)p.serverLevel().setBlock(cut,Blocks.AIR.defaultBlockState(),F);if(!worker.getMainHandItem().isEmpty())worker.getMainHandItem().hurtAndBreak(1,worker,EquipmentSlot.MAINHAND);}}
            if(shared.getInt("Age")%80==0){
                int growth=Math.min(9,shared.getInt("Age")/160+1);shared.putInt("Growth",growth);
                for(int x=-growth;x<=growth;x++)for(int y=-5;y<=Math.min(-1,-5+growth);y++)for(int z=-20-growth;z<=-20+growth;z++){
                    if(Math.abs(x)>6||z<-28||z>-10||x==0&&y==-4&&z==-20)continue;
                    var at=b.offset(x,y,z);if(worker!=null&&shared.getInt("Age")<800&&worker.getBoundingBox().inflate(.15).intersects(new AABB(at)))continue;if(p.serverLevel().getBlockState(at).isAir())p.serverLevel().setBlock(at,UncannyPropBlock.of(FLESH,Direction.SOUTH),F);
                }
                shared.putInt("Pulse",shared.getInt("Pulse")+1);
            }
            world(p.serverLevel(),b,place,shared);
        }
        own.putInt("GrowthCue",shared.getInt("Pulse"));
        if(own.getInt("LastGrowth")!=shared.getInt("Pulse")){own.putInt("LastGrowth",shared.getInt("Pulse"));heard(p,SoundEvents.WARDEN_HEARTBEAT,Vec3.atCenterOf(b.offset(UncannyRooms.MASS)),.65F,.55F);cue(p,"A new fold presses against the cellar steps.");}
        if(own.getInt("Peeled")>=6&&!own.getBoolean("KeyReleased")&&p.serverLevel().isEmptyBlock(b.offset(0,-4,-19))&&p.serverLevel().isEmptyBlock(b.offset(UncannyRooms.KEY_CAVITY))){
            if(!own.contains("OriginalKey")){var key=new ItemStack(LiteraryRegistry.KEY.get());CustomData.update(DataComponents.CUSTOM_DATA,key,t->{t.putUUID("UncannyKeyOwner",p.getUUID());t.putUUID("UncannyKey",UUID.randomUUID());});own.put("OriginalKey",key.save(p.registryAccess()));}
            var key=ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalKey"));var e=new ItemEntity(p.serverLevel(),b.getX()+.5,b.getY()-3.8,b.getZ()-19.5,key);e.setTarget(p.getUUID());
            if(p.serverLevel().addFreshEntity(e)){own.remove("OriginalKey");own.putBoolean("KeyReleased",true);own.putUUID("KeyEntity",e.getUUID());}
        }
        boolean key=false;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);var custom=s.get(DataComponents.CUSTOM_DATA);if(custom!=null&&custom.copyTag().hasUUID("UncannyKeyOwner")&&custom.copyTag().getUUID("UncannyKeyOwner").equals(p.getUUID())){key=true;break;}}
        if(key&&own.getInt("Peeled")>=6&&p.getY()>=b.getY()&&p.getZ()>b.getZ()-7){own.putBoolean("Helped",true);ready(p,place,own,"brought_the_cellar_key_up");}
    }
    private static void foyer(ServerPlayer p,BlockPos b,CompoundTag own){
        var place=LabyrinthPlace.MOTION_LIGHT;int age=own.getInt("UncannyAge");if(age==0)return;
        var shared=world(p.serverLevel(),b,place);long now=p.serverLevel().getGameTime();if(shared.getLong("Tick")!=now){shared.putLong("Tick",now);shared.putInt("Age",shared.getInt("Age")+5);world(p.serverLevel(),b,place,shared);}
        boolean dark=shared.getInt("Age")%240>=100;var lamp=b.offset(UncannyRooms.FLOOD);var state=p.serverLevel().getBlockState(lamp);
        if(state.is(UncannyRegistry.PROP.get())&&state.getValue(UncannyPropBlock.STAGE)!=(dark?1:0))p.serverLevel().setBlock(lamp,state.setValue(UncannyPropBlock.STAGE,dark?1:0),F);
        if(own.getBoolean("LastDark")!=dark){own.putBoolean("LastDark",dark);if(dark)own.putInt("DarkSeen",own.getInt("DarkSeen")+1);heard(p,SoundEvents.IRON_TRAPDOOR_CLOSE,Vec3.atCenterOf(lamp),.65F,.8F);cue(p,dark?"Floodlight OFF / Stillness outside":"Floodlight ON / Wind in the courtyard");}
        if(age%100==0){heard(p,SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),Vec3.atCenterOf(b.offset(UncannyRooms.INTERCOM)),.8F,.65F);cue(p,"Buzzer vibrating");}
        var visitor=actor(p,own,"CastVisitor");if(visitor!=null){visitor.appearance(LiteraryActor.UNCANNY_VISITOR,dark?1:0);if(dark)visitor.setDeltaMovement(Vec3.ZERO);else SceneFootpaths.step(visitor,Vec3.atBottomCenterOf(b.offset(0,0,-17)),.65);}
        if(dark&&!own.getBoolean("FalseDoors")){
            boolean all=true;for(var rel:UncannyRooms.FALSE_DOORS){var at=b.offset(rel);if(LiteraryVignettes.watched(p.serverLevel(),at.getCenter())){all=false;continue;}var s=p.serverLevel().getBlockState(at);if(s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==FALSE_DOOR)continue;if(s.is(Blocks.SMOOTH_SANDSTONE)||s.isAir()){p.serverLevel().setBlock(at,UncannyPropBlock.of(FALSE_DOOR,rel.getX()<0?Direction.EAST:Direction.SOUTH),F);p.serverLevel().setBlock(at.above(),UncannyPropBlock.of(FALSE_DOOR,rel.getX()<0?Direction.EAST:Direction.SOUTH).setValue(UncannyPropBlock.STAGE,1),F);}}
            own.putBoolean("FalseDoors",all);
        }
        for(var rel:UncannyRooms.FALSE_DOORS)if(p.distanceToSqr(b.offset(rel).getCenter())<1.5&&p.serverLevel().getBlockState(b.offset(rel)).is(UncannyRegistry.PROP.get())&&p.serverLevel().getBlockState(b.offset(rel)).getValue(UncannyPropBlock.STAGE)>=2){
            own.putInt("Loops",own.getInt("Loops")+1);HousePackets.send(p,new HouseFadePayload(5,5,10));var safe=Vec3.atBottomCenterOf(b.offset(0,0,-6));p.teleportTo(p.serverLevel(),safe.x,safe.y,safe.z,180,0);p.hasChangedDimension();break;
        }
        if(p.getX()>b.getX()+4.6&&p.getZ()<b.getZ()-3&&p.getZ()>b.getZ()-8&&own.getInt("DarkSeen")>=2&&own.getBoolean("HingeSeen"))ready(p,place,own,"opened_the_hinged_service_door");
    }
    public static String sign(ServerPlayer p,BlockPos b,CompoundTag own){
        var place=LiteraryVignettes.current(p);if(place==LabyrinthPlace.TUNNEL_PERIPHERY){if(own.getInt("UncannyAge")<120)return "PEDESTRIAN UNDERPASS";int steps=Math.max(0,(int)Math.ceil(-66-(p.getZ()-b.getZ()))*-1);return green(own)?"WALK / ||":"RED / "+steps+" STEPS";}
        if(place==LabyrinthPlace.MISSING_SLEEP)return own.getInt("Sleeps")>=2?"CAM 02 / Subject whistling":"CAM 01 / Bed empty / 00:00 → 06:00";
        return "";
    }
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,LabyrinthPlace place,CompoundTag own){
        if(place==LabyrinthPlace.PAINTED_INTERIOR&&p.serverLevel().getBlockState(b.offset(rel)).is(UncannyRegistry.PROP.get())&&p.serverLevel().getBlockState(b.offset(rel)).getValue(UncannyPropBlock.KIND)==PAINTED_BOARD){own.putBoolean("BoardOpened",true);p.serverLevel().setBlock(b.offset(rel),Blocks.AIR.defaultBlockState(),F);cue(p,"Cold earth presses against the back of the paint.");return true;}
        if(place==LabyrinthPlace.MISSING_SLEEP){
            if(rel.equals(UncannyRooms.MONITOR_AT)){UncannySenses.send(p,true);return true;}
            if(p.serverLevel().getBlockState(b.offset(rel)).getBlock() instanceof BedBlock){
                if(!own.getBoolean("Read_Source")){cue(p,"The notebook is taped beside the monitor.");return true;}
                int sleeps=Math.min(3,own.getInt("Sleeps")+1);own.putInt("Sleeps",sleeps);own.putInt("NightClock",own.getInt("NightClock")+7200);if(sleeps==1)own.putInt("FirstSleepAge",own.getInt("UncannyAge"));if(sleeps==2)own.putInt("SecondSleepAge",own.getInt("UncannyAge"));
                HousePackets.send(p,new HouseFadePayload(8,8,16));var target=b.offset(sleeps%2==1?-7:1,5,sleeps%2==1?-13:-20);p.teleportTo(p.serverLevel(),target.getX()+.5,target.getY(),target.getZ()+.5,180,0);p.hasChangedDimension();cue(p,"06:00 / The bed was empty.");return true;
            }
            if((rel.equals(UncannyRooms.PARENTS_DOOR)||rel.equals(UncannyRooms.PARENTS_DOOR.above()))&&own.getInt("Sleeps")>=2){
                if(own.getBoolean("BlockedDoor"))return true;
                if(own.getBoolean("MarionetteSeen")&&own.getBoolean("OwnFaceSeen")&&own.getInt("MonitorTicks")>=40&&own.getInt("FeedsSeen")==15&&p.serverLevel().getBlockState(b.offset(UncannyRooms.PARENTS_DOOR)).getValue(DoorBlock.OPEN)){setDoor(p.serverLevel(),b.offset(UncannyRooms.PARENTS_DOOR),false);own.putBoolean("BlockedDoor",true);ready(p,place,own,"closed_the_door_on_the_other_feed");}
                else if(own.getBoolean("MarionetteSeen")&&own.getBoolean("OwnFaceSeen")&&own.getInt("FeedsSeen")==15){setDoor(p.serverLevel(),b.offset(UncannyRooms.PARENTS_DOOR),true);cue(p,"Your hand raises the latch.");}
                else cue(p,"On the screen, your hand reaches for this handle.");
                return true;
            }
        }
        if(place==LabyrinthPlace.LAYERED_MAN){
            if(rel.equals(UncannyRooms.TOOL)&&!own.getBoolean("CleaverTaken")){own.putBoolean("CleaverTaken",true);LiteraryVignettes.reward(p,place,own,"Cleaver",new ItemStack(UncannyRegistry.CLEAVER.get()));return true;}
            var s=p.serverLevel().getBlockState(b.offset(rel));if(s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==FLESH)return peel(p,b,rel,own);
            if((rel.equals(UncannyRooms.CELLAR_DOOR)||rel.equals(UncannyRooms.CELLAR_DOOR.above()))&&p.getY()>=b.getY()&&world(p.serverLevel(),b,place).getInt("Age")>=800&&!own.getBoolean("Helped")){setDoor(p.serverLevel(),b.offset(UncannyRooms.CELLAR_DOOR),false);ready(p,place,own,"closed_the_cellar_after_the_blade_broke");return true;}
        }
        if(place==LabyrinthPlace.MOTION_LIGHT){
            if(rel.equals(UncannyRooms.INTERCOM)){caught(p,b,place,own,"pressed_the_buzzer_release");return true;}
            if(rel.equals(UncannyRooms.HINGED)||rel.equals(UncannyRooms.HINGED.above())){own.putBoolean("HingeSeen",true);setDoor(p.serverLevel(),b.offset(UncannyRooms.HINGED),true);return true;}
            if(UncannyRooms.FALSE_DOORS.contains(rel)||UncannyRooms.FALSE_DOORS.stream().anyMatch(r->r.above().equals(rel))){own.putBoolean("FalseExamined",true);var lower=UncannyRooms.FALSE_DOORS.stream().filter(r->r.equals(rel)||r.above().equals(rel)).findFirst().orElse(rel);for(int y=0;y<2;y++){var at=b.offset(lower.above(y));var state=p.serverLevel().getBlockState(at);if(state.is(UncannyRegistry.PROP.get()))p.serverLevel().setBlock(at,state.setValue(UncannyPropBlock.STAGE,2+y),F);}cue(p,"The frame has no hinge pins.");return true;}
        }
        return false;
    }
    public static boolean peel(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var held=p.getMainHandItem();if(!held.is(UncannyRegistry.CLEAVER.get())&&!(held.getItem() instanceof AxeItem)){cue(p,"The cleaver is on the workbench.");return true;}
        var at=b.offset(rel);var s=p.serverLevel().getBlockState(at);if(!s.is(UncannyRegistry.PROP.get())||s.getValue(UncannyPropBlock.KIND)!=FLESH)return false;
        if(p.distanceToSqr(at.getCenter())>36)return false;long now=p.serverLevel().getGameTime();if(own.contains("LastPeelAt")&&now-own.getLong("LastPeelAt")<3)return true;own.putLong("LastPeelAt",now);p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),F);own.putInt("Peeled",own.getInt("Peeled")+1);
        held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);heard(p,SoundEvents.HONEY_BLOCK_BREAK,at.getCenter(),.7F,.7F);return true;
    }
    public static void talk(ServerPlayer p,LiteraryActor a,LabyrinthPlace place,CompoundTag own){
        if(place==LabyrinthPlace.SECOND_WAITRESS&&a.role()==LiteraryActor.UNCANNY_WAITRESS){
            if(own.getBoolean("MealServed")){own.putBoolean("Refused",true);p.displayClientMessage(Component.literal("“Leave it on the table. Staff door’s beside the till.”"),false);}
            else p.displayClientMessage(Component.literal("“Don't eat what she brings you, and don't let her clear your table.”"),false);
        }
        if(place==LabyrinthPlace.LAYERED_MAN&&a.role()==LiteraryActor.UNCANNY_PEELER)p.displayClientMessage(Component.literal(a.phase()<1?"“My hands won’t last. The cleaver’s on the bench.”":"“Don’t stop at the steps.”"),false);
    }
    public static boolean locked(ServerPlayer p){if(!LiteraryVignettes.inside(p,LabyrinthPlace.MISSING_SLEEP))return false;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.MISSING_SLEEP);return own.getInt("Sleeps")>0&&!own.getBoolean("BlockedDoor")&&!failed(own)&&!own.getBoolean("Completed");}
    public static void finishReward(ServerPlayer p,LabyrinthPlace place,CompoundTag own){
        if(place==LabyrinthPlace.PAINTED_INTERIOR)LiteraryVignettes.reward(p,place,own,"BoneRing",new ItemStack(UncannyRegistry.BONE_RING.get()));
        if(place==LabyrinthPlace.MISSING_SLEEP)LiteraryVignettes.reward(p,place,own,"SmokyMirror",new ItemStack(UncannyRegistry.SMOKY_MIRROR.get()));
    }
    public static void setDoor(ServerLevel l,BlockPos at,boolean open){for(int y=0;y<2;y++){var s=l.getBlockState(at.above(y));if(s.getBlock() instanceof DoorBlock)l.setBlock(at.above(y),s.setValue(DoorBlock.OPEN,open),F);}}
    private static void nativeStep(LiteraryActor a,Vec3 target,double step){var delta=target.subtract(a.position());if(delta.lengthSqr()>.001)a.move(MoverType.SELF,delta.length()<=step?delta:delta.normalize().scale(step));a.setDeltaMovement(Vec3.ZERO);}
}
