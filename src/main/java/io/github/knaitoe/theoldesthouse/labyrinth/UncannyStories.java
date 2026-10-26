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
    public static boolean green(CompoundTag own){return own.getBoolean("TunnelPassed130")||own.getInt("UncannyAge")%240>=140;}
    public static boolean failed(CompoundTag own){return own.getBoolean("UncannyFailed");}
    static CompoundTag world(ServerLevel l,BlockPos b,LabyrinthPlace place){return LabyrinthData.get(l.getServer()).stateEntry(WORLD,place.id()+":"+b.asLong());}
    static void world(ServerLevel l,BlockPos b,LabyrinthPlace place,CompoundTag tag){LabyrinthData.get(l.getServer()).setStateEntry(WORLD,place.id()+":"+b.asLong(),tag);}
    public static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag own,String key){return own.hasUUID(key)&&p.serverLevel().getEntity(own.getUUID(key)) instanceof LiteraryActor a?a:null;}
    static boolean actorsLoaded(ServerLevel l,BlockPos b,LabyrinthPlace place){var r=place.room();for(int x=(b.getX()+r.minX())>>4;x<=(b.getX()+r.maxX())>>4;x++)for(int z=(b.getZ()+r.minZ())>>4;z<=(b.getZ()+r.maxZ())>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;return true;}
    private static @Nullable LiteraryActor create(ServerLevel l,BlockPos b,LabyrinthPlace place,CompoundTag tag,String key,int role,BlockPos rel,@Nullable UUID reader){
        UUID restore=null;
        if(tag.hasUUID(key)){
            if(l.getEntity(tag.getUUID(key)) instanceof LiteraryActor a){tag.remove("Missing_"+key);return a;}
            // Itemless figures can recover a genuinely missing saved body, retaining its UUID. Never guess across unloaded sections.
            if(role!=LiteraryActor.UNCANNY_GIANT&&role!=LiteraryActor.UNCANNY_VISITOR||!actorsLoaded(l,b,place))return null;
            if(!tag.contains("Missing_"+key)){tag.putLong("Missing_"+key,l.getGameTime());return null;}
            if(l.getGameTime()-tag.getLong("Missing_"+key)<80)return null;restore=tag.getUUID(key);
        }
        var at=b.offset(rel);if(!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;if(restore!=null)a.setUUID(restore);
        a.bind(place,reader);a.appearance(role,0);a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);a.setDeltaMovement(Vec3.ZERO);
        if(role==LiteraryActor.UNCANNY_WAITRESS)DinerWalk.register(a,b,true);
        if(restore!=null&&role==LiteraryActor.UNCANNY_VISITOR&&tag.contains("FoyerPose131")){var pose=tag.getCompound("FoyerPose131");a.moveTo(b.getX()+pose.getDouble("X"),b.getY()+pose.getDouble("Y"),b.getZ()+pose.getDouble("Z"),pose.getFloat("Yaw"),0);}
        if(role==LiteraryActor.UNCANNY_FALSE_WAITRESS){a.noPhysics=true;a.setNoGravity(true);}
        if(!l.addFreshEntity(a))return null;tag.putUUID(key,a.getUUID());tag.remove("Missing_"+key);return a;
    }
    public static void prepare(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var shared=world(l,b,place);
        if(place==LabyrinthPlace.SECOND_WAITRESS)create(l,b,place,shared,"Waitress",LiteraryActor.UNCANNY_WAITRESS,new BlockPos(-8,0,-9),null);
        if(place==LabyrinthPlace.LAYERED_MAN){var worker=create(l,b,place,shared,"Peeler",LiteraryActor.UNCANNY_PEELER,new BlockPos(-3,-5,-19),null);if(worker!=null&&!shared.getBoolean("Rescued")&&!shared.hasUUID("EscortOwner")&&!shared.getBoolean("ToolGiven")){var blade=new ItemStack(Items.IRON_AXE);blade.setDamageValue(blade.getMaxDamage()-9);worker.setItemSlot(EquipmentSlot.MAINHAND,blade);shared.putBoolean("ToolGiven",true);}}
        world(l,b,place,shared);
    }
    public static void arrive(ServerPlayer p,LabyrinthPlace place,CompoundTag own){
        var b=LiteraryVignettes.base(p,place);if(b==null)return;if(place==LabyrinthPlace.LAYERED_MAN)UncannyCellar.arrive(p,b);prepare(p.serverLevel(),b,place);
        if(!own.contains("UncannyAge")){own.putInt("UncannyAge",0);own.putInt("NightClock",0);}
        if(place==LabyrinthPlace.PAINTED_INTERIOR)for(int i=0;i<4;i++)create(p.serverLevel(),b,place,own,"CastP"+i,LiteraryActor.UNCANNY_PROPPED,new BlockPos(i%2==0?-6:6,0,i<2?-18:-25),p.getUUID());
        if(place==LabyrinthPlace.SECOND_WAITRESS){
            create(p.serverLevel(),b,place,own,"CastFake",LiteraryActor.UNCANNY_FALSE_WAITRESS,new BlockPos(-8,0,-27),p.getUUID());
            if(!own.contains("OriginalMeal")&&!own.getBoolean("MealReleased")){
                var s=new ItemStack(UncannyRegistry.MEAL.get());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID("UncannyMealOwner",p.getUUID());t.putUUID("UncannyMeal",UUID.randomUUID());});own.put("OriginalMeal",s.save(p.registryAccess()));
            }
        }
        if(place==LabyrinthPlace.TUNNEL_PERIPHERY)create(p.serverLevel(),b,place,own,"CastGiant",LiteraryActor.UNCANNY_GIANT,new BlockPos(0,0,-71),p.getUUID());
        if(place==LabyrinthPlace.MISSING_SLEEP){var doll=create(p.serverLevel(),b,place,own,"CastDoll",LiteraryActor.UNCANNY_MARIONETTE,new BlockPos(1,5,-27),p.getUUID());if(doll!=null&&!own.getBoolean("DollBatGiven")){doll.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(UncannyRegistry.BAT.get()));own.putBoolean("DollBatGiven",true);}}
        if(place==LabyrinthPlace.MOTION_LIGHT)create(p.serverLevel(),b,place,own,"CastVisitor",LiteraryActor.UNCANNY_VISITOR,new BlockPos(0,0,-18),p.getUUID());
        if(place==LabyrinthPlace.LAYERED_MAN&&!own.getBoolean("HelpCalled127")&&!own.getBoolean("Completed")&&!failed(own)){
            var shared=world(p.serverLevel(),b,place);
            if(!shared.getBoolean("Rescued")&&shared.hasUUID("Peeler")&&p.serverLevel().getEntity(shared.getUUID("Peeler")) instanceof LiteraryActor worker){
                own.putBoolean("HelpCalled127",true);p.displayClientMessage(Component.literal("“Hey. Down here. Please. I need another pair of hands.”"),false);
                heard(p,NovelRegistry.WELL_BREATH.get(),worker.getEyePosition(),.45F,.85F);
            }
        }
        if(place==LabyrinthPlace.SECOND_WAITRESS)UncannyDiner.greet(p,b,own);
        own.putBoolean("UncannyArrived",true);
    }
    public static void reader(ServerPlayer p,BlockPos b,LabyrinthPlace place,CompoundTag own){
        if(!LiteraryVignettes.participant(p)||!LiteraryVignettes.inside(p,place))return;
        arrive(p,place,own);observe(p,b,place,own);UncannyReading.resolve(p,place,own);
        if(place==LabyrinthPlace.MOTION_LIGHT)FoyerMotion.dark(p.serverLevel(),b);
        if(place==LabyrinthPlace.LAYERED_MAN&&!failed(own)&&p.containerMenu==p.inventoryMenu)UncannyCellar.physical(p,b,own);
        if(place==LabyrinthPlace.SECOND_WAITRESS&&p.containerMenu==p.inventoryMenu)DinerPlate.migrate(p,b,own);
        if(place==LabyrinthPlace.TUNNEL_PERIPHERY&&own.getBoolean("Completed")&&!own.getBoolean("TunnelExit130")&&p.getZ()<b.getZ()-70.5){own.putBoolean("TunnelExit130",true);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),place,own);LabyrinthDoors.goToJunction(p);return;}
        if(own.getBoolean("Completed")||failed(own)||p.containerMenu!=p.inventoryMenu)return;
        int age=Math.min(72000,own.getInt("UncannyAge")+5);own.putInt("UncannyAge",age);
        switch(place){
            case PAINTED_INTERIOR->boarded(p,b,own);case SECOND_WAITRESS->diner(p,b,own);case TUNNEL_PERIPHERY->tunnel(p,b,own);
            case MISSING_SLEEP->bedroom(p,b,own);case LAYERED_MAN->cellar(p,b,own);case MOTION_LIGHT->foyer(p,b,own);default->{}
        }
    }
    static void ready(ServerPlayer p,LabyrinthPlace place,CompoundTag own,String outcome){if(LiteraryVignettes.participant(p)&&!failed(own)){LiteraryVignettes.ready(p,place,own,outcome);UncannyReading.resolve(p,place,own);}}
    private static boolean seen(ServerPlayer p,LiteraryActor a){return a!=null&&HouseWatchers.sees(p,a.getEyePosition());}
    private static void observe(ServerPlayer p,BlockPos b,LabyrinthPlace place,CompoundTag own){
        if(place==LabyrinthPlace.SECOND_WAITRESS){var a=actor(p,own,"CastFake");if(a!=null&&a.phase()>=1&&seen(p,a))own.putBoolean("SawSecond",true);}
        if(place==LabyrinthPlace.TUNNEL_PERIPHERY&&green(own)){var a=actor(p,own,"CastGiant");if(a!=null&&a.phase()>0&&seen(p,a))own.putBoolean("StoopingSeen",true);}
        if(place==LabyrinthPlace.MOTION_LIGHT){var a=actor(p,own,"CastVisitor");if(a!=null&&a.phase()>=1&&seen(p,a))own.putBoolean("VisitorSeen",true);}
        if(place==LabyrinthPlace.LAYERED_MAN&&p.getY()<b.getY()-1){own.putBoolean("CellarEntered",true);var shared=world(p.serverLevel(),b,place);if(shared.hasUUID("Peeler")&&p.serverLevel().getEntity(shared.getUUID("Peeler")) instanceof LiteraryActor a&&seen(p,a)){own.putBoolean("WorkerSeen",true);if(shared.getInt("Age")>=400)own.putBoolean("BladeSeen",true);}}
    }
    static void heard(ServerPlayer p,SoundEvent sound,Vec3 at,float volume,float pitch){
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.BLOCKS,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    private static void cue(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text).withStyle(net.minecraft.ChatFormatting.GRAY),true);}
    /** Ambient sounds leave real gaps; their schedule is personal and follows the occupied story clock. */
    private static boolean ambient(ServerPlayer p,CompoundTag own,String key,int age,int minimum,int variation){
        if(!own.contains(key)){own.putInt(key,age+minimum+p.getRandom().nextInt(variation+1));return false;}
        if(age<own.getInt(key))return false;
        own.putInt(key,age+minimum+p.getRandom().nextInt(variation+1));return true;
    }
    static void caught(ServerPlayer p,BlockPos b,LabyrinthPlace place,CompoundTag own,String outcome){
        if(failed(own)||own.getBoolean("Completed"))return;own.putBoolean("UncannyFailed",true);own.putString("Failure",outcome);own.putBoolean("Ready",false);
        HousePackets.send(p,new HouseFadePayload(10,8,16));var safe=Vec3.atBottomCenterOf(b.offset(0,0,-3));
        p.teleportTo(p.serverLevel(),safe.x,safe.y,safe.z,0,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);
        cue(p,switch(place){case SECOND_WAITRESS->"The place setting in front of you is empty.";case TUNNEL_PERIPHERY->"A hand on the back of your neck.";case MISSING_SLEEP->"The monitor is still showing the empty bed.";case MOTION_LIGHT->"The buzzer stops.";case LAYERED_MAN->"Warm tallow closes over your boots.";default->"You are facing the painted sun again.";});
    }
    private static void boarded(ServerPlayer p,BlockPos b,CompoundTag own){
        int age=own.getInt("UncannyAge");if(age==0)return;int nearest=0;double proximity=100;
        for(int i=0;i<4;i++){
            var a=actor(p,own,"CastP"+i);if(a==null)continue;a.appearance(LiteraryActor.UNCANNY_PROPPED,i+1);
            if(a.getBoundingBox().inflate(.12,.03,.12).intersects(p.getBoundingBox())&&UncannySenses.clear(p,a.getEyePosition())){caught(p,b,LabyrinthPlace.PAINTED_INTERIOR,own,"touched_by_the_family");return;}
            double angle=UncannySenses.angle(p,a.getEyePosition()),half=UncannySenses.halfFov(p);
            if(angle>half*.52&&angle<half*1.04&&UncannySenses.clear(p,a.getEyePosition())){
                double d=a.distanceToSqr(p);if(d<proximity){proximity=d;var look=UncannySenses.look(p);var delta=a.position().subtract(p.position());nearest=look.x*delta.z-look.z*delta.x>0?1:-1;}
                own.putInt("EdgeSeen",own.getInt("EdgeSeen")|1<<i);
            }else if(age%30==0&&!UncannySenses.mirror(p)){
                double yaw=Math.atan2(UncannySenses.look(p).z,UncannySenses.look(p).x)+(i%2==0?-1:1)*half*.91;
                // "Until you back into a corner": there the edges of the view have nowhere left to push them.
                double radius=corner(p)?1:Math.max(1.15,4-age/650.);var target=p.position().add(Math.cos(yaw)*radius,0,Math.sin(yaw)*radius);
                // The original crosses physical floor space while hidden by the direct-gaze rule.
                if(target.x>b.getX()-7&&target.x<b.getX()+7&&target.z<b.getZ()-15&&target.z>b.getZ()-27)SceneFootpaths.step(a,target,.32);
            }
        }
        own.putInt("Edge",nearest);
        if(nearest!=0&&ambient(p,own,"NextWoodSound",age,160,120)){var right=new Vec3(-p.getLookAngle().z,0,p.getLookAngle().x).scale(nearest*2.5);heard(p,SoundEvents.WOOD_STEP,p.position().add(right),.25F,.8F);}
        if((own.getInt("EdgeSeen")!=0||own.getBoolean("BoardOpened"))&&p.getX()>b.getX()+8.5&&p.getZ()<b.getZ()-16)ready(p,LabyrinthPlace.PAINTED_INTERIOR,own,"left_by_the_unpainted_door");
        if(proximity<2.25&&corner(p)&&!own.getBoolean("Ready"))caught(p,b,LabyrinthPlace.PAINTED_INTERIOR,own,"backed_into_the_corners");
    }
    private static boolean corner(ServerPlayer p){
        // A real corner: a wall on one side and another at right angles to it. A doorway's two jambs are not one.
        var at=p.blockPosition();boolean z=false,x=false;
        for(var d:Direction.Plane.HORIZONTAL)if(!p.serverLevel().getBlockState(at.relative(d)).getCollisionShape(p.serverLevel(),at.relative(d)).isEmpty()){if(d.getAxis()==Direction.Axis.Z)z=true;else x=true;}
        return z&&x;
    }
    private static void diner(ServerPlayer p,BlockPos b,CompoundTag own){
        int age=own.getInt("UncannyAge");var fake=actor(p,own,"CastFake");if(fake==null)return;
        if(own.getBoolean("MealEaten127")){
            fake.appearance(LiteraryActor.UNCANNY_FALSE_WAITRESS,3);SceneFootpaths.step(fake,p.position(),.18);
            if(age-own.getInt("MealEatenAt127")>=35||fake.distanceToSqr(p)<1.1)caught(p,b,LabyrinthPlace.SECOND_WAITRESS,own,"ate_what_she_brought");return;
        }
        if(!UncannyDiner.host(p,b,own)){fake.appearance(LiteraryActor.UNCANNY_FALSE_WAITRESS,0);return;}
        int service=Math.max(0,age-own.getInt("DinerHostedAt127"));
        var serving=Vec3.atBottomCenterOf(b.offset(3,0,-19));
        if(!own.getBoolean("SecondPlaced131")&&!own.getBoolean("MealReleased")&&fake.phase()<2){
            var box=fake.getBoundingBox().move(serving.subtract(fake.position()));
            if(p.serverLevel().getBlockCollisions(fake,box).iterator().hasNext()||!p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,box,e->e!=fake&&e.isAlive()&&!(e instanceof LiteraryActor other&&other.owner().isPresent()&&!other.owner().get().equals(p.getUUID()))).isEmpty())return;
            if(fake.phase()>0){fake.appearance(LiteraryActor.UNCANNY_FALSE_WAITRESS,0);own.putInt("SecondManifestAge131",age);}
            fake.moveTo(serving);fake.setDeltaMovement(Vec3.ZERO);own.putBoolean("SecondPlaced131",true);
        }
        int manifestation=Math.max(0,age-own.getInt(own.contains("SecondManifestAge131")?"SecondManifestAge131":"DinerHostedAt127"));
        int stage=service>=1000?3:manifestation<6?0:manifestation<30?1:2;own.putInt("UncannyStage",stage);fake.appearance(LiteraryActor.UNCANNY_FALSE_WAITRESS,Math.min(stage,2));fake.noPhysics=stage<2;fake.setNoGravity(stage<2);
        if(age>0&&ambient(p,own,"NextRattleSound",age,180,160))heard(p,SoundEvents.CHAIN_STEP,Vec3.atCenterOf(b.offset(UncannyRooms.DINER_TABLE)),.18F,1.65F);
        if(age>0&&age%60==0)p.serverLevel().sendParticles(p,ParticleTypes.ASH,true,b.getX()+8,b.getY()+1.1,b.getZ()-19,9,.35,.2,.4,.005);
        if(stage==0&&age>0&&age%100==0){var wind=p.position().subtract(Vec3.atCenterOf(b.offset(8,1,-19))).normalize().scale(.045);p.push(wind.x,0,wind.z);p.hurtMarked=true;}
        if(stage>=2&&!own.getBoolean("MealReleased")){
            var target=Vec3.atBottomCenterOf(b.offset(3,0,-19));SceneFootpaths.step(fake,target,.2);
            if(fake.position().distanceToSqr(target)<.4&&own.contains("OriginalMeal")){
                var meal=ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalMeal"));
                fake.setItemSlot(EquipmentSlot.MAINHAND,meal);own.remove("OriginalMeal");own.putBoolean("MealReleased",true);
            }
        }
        if(own.getBoolean("MealReleased")&&!own.getBoolean("MealServed")&&!fake.getMainHandItem().isEmpty()){
            fake.gesture(1);var plate=fake.getMainHandItem();var at=DinerPlate.serve(p,b,plate);
            if(at!=null){fake.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);own.putLong("PlatePos131",at.asLong());own.putBoolean("MealServed",true);ask(p,fake,own);}
        }
        boolean sitting=own.getBoolean("MealServed")&&UncannyDiner.pressure(p,fake,b,own);
        if(stage>=3&&own.getBoolean("MealServed")&&!sitting&&!own.getBoolean("Refused")&&!own.getBoolean("Ready")){
            if(!own.contains("LeftBoothAt130"))own.putInt("LeftBoothAt130",age);
            if(age-own.getInt("LeftBoothAt130")>=80){SceneFootpaths.step(fake,p.position(),.24);if(fake.distanceToSqr(p)<1.1)caught(p,b,LabyrinthPlace.SECOND_WAITRESS,own,"let_her_clear_the_table");}
        }
        if(fake.phase()>=2&&!own.getBoolean("CounterWarned")&&p.getX()<b.getX()-6&&p.getZ()<b.getZ()-5&&p.getZ()>b.getZ()-12){own.putBoolean("CounterWarned",true);cue(p,"Leave what she brings. Don't answer her.");}
        boolean front=p.getZ()>b.getZ()-2&&Math.abs(p.getX()-b.getX())<2,staff=p.getX()<b.getX()-10.4&&p.getZ()>b.getZ()-9&&p.getZ()<b.getZ()-2;
        if(own.getBoolean("SawSecond")&&!own.getBoolean("MealServed")&&front)ready(p,LabyrinthPlace.SECOND_WAITRESS,own,"left_before_the_plate");
        if(own.getBoolean("MealServed")&&(front||staff)){own.putBoolean("Refused",true);ready(p,LabyrinthPlace.SECOND_WAITRESS,own,staff?"left_by_the_staff_door":"left_the_plate_and_walked_out");}
    }
    /** "Don't answer her when she asks how your meal is." */
    private static void ask(ServerPlayer p,LiteraryActor fake,CompoundTag own){own.putBoolean("AskedMeal",true);fake.say("How's your meal?");p.displayClientMessage(Component.literal("“How’s your meal?”"),false);}
    public static void ate(ServerPlayer p,ItemStack s){
        var c=s.get(DataComponents.CUSTOM_DATA);if(c==null||!c.copyTag().hasUUID("UncannyMealOwner")||!c.copyTag().getUUID("UncannyMealOwner").equals(p.getUUID())||!LiteraryVignettes.inside(p,LabyrinthPlace.SECOND_WAITRESS))return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.SECOND_WAITRESS);
        if(failed(own)||own.getBoolean("Completed")||own.getBoolean("MealEaten127"))return;
        own.putBoolean("MealEaten127",true);own.putInt("MealEatenAt127",own.getInt("UncannyAge"));
        var fake=actor(p,own,"CastFake");if(fake!=null)fake.appearance(LiteraryActor.UNCANNY_FALSE_WAITRESS,3);
        LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.SECOND_WAITRESS,own);
    }
    private static void tunnel(ServerPlayer p,BlockPos b,CompoundTag own){
        int age=own.getInt("UncannyAge");var giant=actor(p,own,"CastGiant");if(age==0)return;
        double moved=own.contains("LastX")?Math.hypot(p.getX()-own.getDouble("LastX"),p.getZ()-own.getDouble("LastZ")):0;
        if(green(own)&&own.contains("LastZ")&&p.getZ()<own.getDouble("LastZ")-.08)own.putInt("GreenSteps",own.getInt("GreenSteps")+1);
        own.putDouble("LastX",p.getX());own.putDouble("LastZ",p.getZ());
        // Moving through real green signals is a route, even if the reader never
        // gives the dormant woman an unseen opportunity to appear behind them.
        if(p.getZ()<b.getZ()-66&&(own.getInt("GreenSteps")>=5||p.getZ()<b.getZ()-70.5&&own.getInt("UncannyAge")>=160)){
            own.putBoolean("TunnelPassed130",true);if(giant!=null){giant.appearance(LiteraryActor.UNCANNY_GIANT,0);giant.setDeltaMovement(Vec3.ZERO);}ready(p,LabyrinthPlace.TUNNEL_PERIPHERY,own,"passed_the_last_green_signal");return;
        }
        if(giant==null)return;
        if(!own.getBoolean("TunnelStarted127")){
            if(giant.phase()>0&&own.contains("LastZ"))own.putBoolean("TunnelStarted127",true);
            else{
                giant.appearance(LiteraryActor.UNCANNY_GIANT,0);
                if(age<160||p.getZ()>b.getZ()-12)return;
                Vec3 spot=null;
                // The tall body needs a genuinely clear bay between the ceiling fittings.
                // Try behind the reader; never make an appearance in their current view.
                for(int offset=0;offset<=6;offset+=2){
                    var candidate=new Vec3(b.getX()+.5,b.getY(),Math.min(b.getZ()-8.5,p.getZ()+10-offset));
                    if(UncannySenses.angle(p,candidate.add(0,2.1,0))<UncannySenses.halfFov(p)&&UncannySenses.clear(p,candidate.add(0,2.1,0)))continue;
                    if(p.serverLevel().noCollision(giant,giant.getBoundingBox().move(candidate.subtract(giant.position())))){spot=candidate;break;}
                }
                if(spot==null)return;
                giant.moveTo(spot);giant.setDeltaMovement(Vec3.ZERO);own.putBoolean("TunnelStarted127",true);
            }
        }
        int stage=green(own)?1:2;
        if(own.getInt("UncannyStage")!=stage)heard(p,green(own)?SoundEvents.NOTE_BLOCK_HAT.value():SoundEvents.NOTE_BLOCK_BASS.value(),p.position().add(0,2,-4),.2F,green(own)?1.7F:.6F);
        giant.appearance(LiteraryActor.UNCANNY_GIANT,stage);own.putInt("UncannyStage",stage);
        if(green(own)){giant.setDeltaMovement(Vec3.ZERO);}
        else{
            TunnelStride.seek(giant,p.position().add(0,0,1));
            if(giant.distanceToSqr(p)<1.7&&age>120&&!own.getBoolean("Ready"))caught(p,b,LabyrinthPlace.TUNNEL_PERIPHERY,own,"paced_to_the_last_red_light");
        }
    }
    private static void bedroom(ServerPlayer p,BlockPos b,CompoundTag own){
        int sleeps=own.getInt("Sleeps");if(sleeps==1&&own.getInt("UncannyAge")>own.getInt("FirstSleepAge")+1800){own.putInt("Sleeps",0);own.remove("FirstSleepAge");return;}var doll=actor(p,own,"CastDoll");if(doll==null)return;
        doll.appearance(LiteraryActor.UNCANNY_MARIONETTE,sleeps>=2?1:0);own.putInt("UncannyStage",sleeps>=2?2:sleeps);
        var lens=p.getPersistentData().getCompound(UncannySenses.LENS);
        if(UncannySenses.fresh(p)&&lens.getInt("Monitor")>=0&&p.distanceToSqr(b.offset(UncannyRooms.MONITOR_AT).getCenter())<25&&UncannySenses.clear(p,Vec3.atLowerCornerOf(b.offset(UncannyRooms.MONITOR_AT)).add(.5,.6,.93))){
            int channel=lens.getInt("Monitor");own.putInt("FeedsSeen",own.getInt("FeedsSeen")|1<<channel);own.putInt("MonitorTicks",own.getInt("MonitorTicks")+5);
            if(sleeps>=1&&channel==0)own.putBoolean("EmptyBedSeen",true);
            if(sleeps>=2&&channel==1&&doll.getZ()<b.getZ()-15){own.putBoolean("OwnFaceSeen",true);if(own.getString("FirstView").isEmpty())own.putString("FirstView","camera");}
        }
        if(sleeps>=2){
            var delta=doll.getEyePosition().subtract(UncannySenses.eye(p));if(delta.lengthSqr()<100&&UncannySenses.look(p).dot(delta.normalize())>.8&&UncannySenses.clear(p,doll.getEyePosition())){own.putBoolean("MarionetteSeen",true);if(own.getString("FirstView").isEmpty())own.putString("FirstView","hall");}
            if(own.getBoolean("BlockedDoor")&&(own.getBoolean("MarionetteSeen")||own.getBoolean("OwnFaceSeen")))ready(p,LabyrinthPlace.MISSING_SLEEP,own,"closed_the_door_on_the_other_feed");
            if(!own.getBoolean("BlockedDoor")){
                var target=doll.getZ()<b.getZ()-15.5?new Vec3(b.getX()+1.5,b.getY()+5,b.getZ()-15.5):new Vec3(b.getX()+4,b.getY()+5,b.getZ()-15.5);
                SceneFootpaths.step(doll,target,.07);if(doll.getX()>b.getX()+3.1){doll.gesture(2);if(own.getInt("UncannyAge")>own.getInt("SecondSleepAge")+300)caught(p,b,LabyrinthPlace.MISSING_SLEEP,own,"watched_the_hand_reach_the_door");}
            }
            if(own.getInt("UncannyAge")>own.getInt("SecondSleepAge")+2400&&!own.getBoolean("BlockedDoor"))caught(p,b,LabyrinthPlace.MISSING_SLEEP,own,"waited_out_the_empty_bed");
            if(ambient(p,own,"NextHallSound",own.getInt("UncannyAge"),200,160))heard(p,SoundEvents.FOX_SCREECH,doll.getEyePosition(),.06F,.55F);
        }
    }
    private static void cellar(ServerPlayer p,BlockPos b,CompoundTag own){UncannyCellar.tick(p,b,own);}
    private static void foyer(ServerPlayer p,BlockPos b,CompoundTag own){
        var place=LabyrinthPlace.MOTION_LIGHT;int age=own.getInt("UncannyAge");if(age==0)return;
        var shared=world(p.serverLevel(),b,place);long now=p.serverLevel().getGameTime();if(shared.getLong("Tick")!=now){shared.putLong("Tick",now);shared.putInt("Age",shared.getInt("Age")+5);world(p.serverLevel(),b,place,shared);}
        boolean dark=FoyerMotion.dark(p.serverLevel(),b);var lamp=b.offset(UncannyRooms.FLOOD);
        if(own.getBoolean("LastDark")!=dark){own.putBoolean("LastDark",dark);if(dark){own.putInt("DarkSeen",own.getInt("DarkSeen")+1);own.putInt("FoyerStill131",own.getInt("FoyerStill131")+1);}heard(p,SoundEvents.IRON_TRAPDOOR_CLOSE,Vec3.atCenterOf(lamp),.25F,.8F);}
        if(ambient(p,own,"NextBuzzerSound",age,180,140))heard(p,SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),Vec3.atCenterOf(b.offset(UncannyRooms.INTERCOM)),.18F,.65F);
        FoyerPresence.tick(p,b,own,dark);
        if(dark){
            boolean all=true;for(var rel:UncannyRooms.FALSE_DOORS){var at=b.offset(rel);if(LiteraryVignettes.watched(p.serverLevel(),at.getCenter())){all=false;continue;}var s=p.serverLevel().getBlockState(at);if(s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==FALSE_DOOR)continue;if(s.is(Blocks.SMOOTH_SANDSTONE)||s.is(UncannyRegistry.material("lobby_wall"))||s.isAir()){p.serverLevel().setBlock(at,UncannyPropBlock.of(FALSE_DOOR,rel.getX()<0?Direction.EAST:Direction.SOUTH),F);p.serverLevel().setBlock(at.above(),UncannyPropBlock.of(FALSE_DOOR,rel.getX()<0?Direction.EAST:Direction.SOUTH).setValue(UncannyPropBlock.STAGE,1),F);}}
            own.putBoolean("FalseDoors",all);
        }
        {
            // "Don't back up without looking behind you first." In the dark, a frame the reader is not facing clicks open;
            // it shuts again with the light. A hand can examine it, but cannot open it.
            int auto=own.getInt("FalseAuto");
            for(int i=0;i<UncannyRooms.FALSE_DOORS.size();i++){
                var at=b.offset(UncannyRooms.FALSE_DOORS.get(i));var lower=p.serverLevel().getBlockState(at);
                if(!lower.is(UncannyRegistry.PROP.get())||lower.getValue(UncannyPropBlock.KIND)!=FALSE_DOOR)continue;int bit=1<<i;
                boolean behind=UncannySenses.angle(p,at.getCenter())>UncannySenses.halfFov(p);
                if(dark&&behind&&(auto&bit)==0&&lower.getValue(UncannyPropBlock.STAGE)<2){falseDoor(p.serverLevel(),at,true);auto|=bit;heard(p,SoundEvents.WOODEN_TRAPDOOR_OPEN,at.getCenter(),.2F,.6F);}
                else if(!dark&&(auto&bit)!=0){falseDoor(p.serverLevel(),at,false);auto&=~bit;}
            }
            own.putInt("FalseAuto",auto);
        }
        if(p.getX()>b.getX()+4.6&&p.getZ()<b.getZ()-3&&p.getZ()>b.getZ()-8&&own.getBoolean("HingeSeen")&&(own.getBoolean("VisitorSeen")||own.getBoolean("FalseExamined")))ready(p,place,own,"opened_the_hinged_service_door");
    }
    private static void falseDoor(ServerLevel l,BlockPos at,boolean open){for(int y=0;y<2;y++){var s=l.getBlockState(at.above(y));if(s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==FALSE_DOOR)l.setBlock(at.above(y),s.setValue(UncannyPropBlock.STAGE,(open?2:0)+y),F);}}
    public static String sign(ServerPlayer p,BlockPos b,CompoundTag own){
        var place=LiteraryVignettes.current(p);if(place==LabyrinthPlace.TUNNEL_PERIPHERY){
            // "They stopped flashing detour warnings ten minutes ago. Now they just flicker on and off, counting down how many steps I have left."
            // A blank (a space, not an empty line, which the sign renderer reads as the diner's own lettering) is the sign switched off.
            int age=own.getInt("UncannyAge");if(age<120)return "DETOUR / USE OTHER CROSSING";if(age/10%4==3)return " ";
            var giant=actor(p,own,"CastGiant");if(giant==null)return " ";
            return Math.max(0,(int)Math.ceil((Math.sqrt(giant.distanceToSqr(p))-1.3)/.8))+" STEPS";
        }
        if(place==LabyrinthPlace.MISSING_SLEEP)return own.getInt("Sleeps")>=2?"CAM 02 / Subject whistling":"CAM 01 / Bed empty / 00:00 → 06:00";
        return "";
    }
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,LabyrinthPlace place,CompoundTag own){
        if(place==LabyrinthPlace.SECOND_WAITRESS&&DinerPlate.at(p.serverLevel(),b.offset(rel))!=null){boolean used=DinerPlate.use(p,b.offset(rel));if(used)own.merge(LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),place));return used;}
        if(place==LabyrinthPlace.PAINTED_INTERIOR&&p.serverLevel().getBlockState(b.offset(rel)).is(UncannyRegistry.PROP.get())){
            var kind=p.serverLevel().getBlockState(b.offset(rel)).getValue(UncannyPropBlock.KIND);
            if(kind==PAINTED_BOARD||kind==SUN_BOARD){own.putBoolean("BoardOpened",true);
                if(kind==SUN_BOARD){for(int y=0;y<=1;y++){var at=b.offset(0,y,-15);var s=p.serverLevel().getBlockState(at);if(s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==SUN_BOARD)p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),F);}cue(p,"The painted sun comes away from the frame.");}
                else{p.serverLevel().setBlock(b.offset(rel),Blocks.AIR.defaultBlockState(),F);cue(p,"Cold earth presses against the back of the paint.");}
                heard(p,SoundEvents.WOOD_BREAK,b.offset(rel).getCenter(),.5F,.8F);return true;
            }
        }
        if(place==LabyrinthPlace.MISSING_SLEEP){
            if(rel.equals(UncannyRooms.MONITOR_AT)){UncannySenses.send(p,true);return true;}
            if(p.serverLevel().getBlockState(b.offset(rel)).getBlock() instanceof BedBlock){
                int sleeps=Math.min(3,own.getInt("Sleeps")+1);own.putInt("Sleeps",sleeps);own.putInt("NightClock",own.getInt("NightClock")+7200);if(sleeps==1)own.putInt("FirstSleepAge",own.getInt("UncannyAge"));if(sleeps==2)own.putInt("SecondSleepAge",own.getInt("UncannyAge"));
                HousePackets.send(p,new HouseFadePayload(8,8,16));var target=b.offset(sleeps%2==1?-7:1,5,sleeps%2==1?-13:-20);p.teleportTo(p.serverLevel(),target.getX()+.5,target.getY(),target.getZ()+.5,180,0);p.hasChangedDimension();cue(p,sleeps==1?"06:00. Dried mud between your toes.":"06:00. On the monitor, the mattress is empty.");return true;
            }
            if((rel.equals(UncannyRooms.PARENTS_DOOR)||rel.equals(UncannyRooms.PARENTS_DOOR.above()))&&own.getInt("Sleeps")>=2){
                var door=p.serverLevel().getBlockState(b.offset(UncannyRooms.PARENTS_DOOR));boolean close=door.getValue(DoorBlock.OPEN);
                setDoor(p.serverLevel(),b.offset(UncannyRooms.PARENTS_DOOR),!close);own.putBoolean("BlockedDoor",close);
                if(close&&(own.getBoolean("MarionetteSeen")||own.getBoolean("OwnFaceSeen")))ready(p,place,own,"closed_the_door_on_the_other_feed");
                return true;
            }
        }
        if(place==LabyrinthPlace.LAYERED_MAN){
            if(rel.equals(UncannyRooms.TOOL)&&!own.getBoolean("CleaverTaken")){own.putBoolean("CleaverTaken",true);LiteraryVignettes.reward(p,place,own,"Cleaver",new ItemStack(UncannyRegistry.CLEAVER.get()));CellarDialogue.actions(p,b,own,world(p.serverLevel(),b,place));return true;}
            var s=p.serverLevel().getBlockState(b.offset(rel));if(s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==FLESH)return peel(p,b,rel,own);
            if(s.is(UncannyRegistry.FLESH.get()))return peel(p,b,rel,own);
            if((rel.equals(UncannyRooms.CELLAR_DOOR)||rel.equals(UncannyRooms.CELLAR_DOOR.above()))&&p.getY()>=b.getY()&&world(p.serverLevel(),b,place).getInt("Age")>=800&&!own.getBoolean("Helped")&&own.getBoolean("CellarEntered")&&(own.getBoolean("WorkerSeen")||own.getInt("Peeled")>0)&&s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN)){setDoor(p.serverLevel(),b.offset(UncannyRooms.CELLAR_DOOR),false);own.putBoolean("SealedCellar",true);var shared=world(p.serverLevel(),b,place);own.putBoolean("WorkerAlreadySafe",shared.getBoolean("Rescued"));if(shared.hasUUID("EscortOwner")&&shared.getUUID("EscortOwner").equals(p.getUUID())){shared.remove("EscortOwner");world(p.serverLevel(),b,place,shared);}ready(p,place,own,"closed_the_cellar_after_the_blade_broke");return true;}
        }
        if(place==LabyrinthPlace.MOTION_LIGHT){
            if(rel.equals(FoyerMotion.PULL))return FoyerMotion.pull(p,b,own);
            if(rel.equals(UncannyRooms.INTERCOM)){if(!own.getBoolean("Read_Source")&&!own.getBoolean("BuzzerWarned")){own.putBoolean("BuzzerWarned",true);cue(p,"Black marker beside the panel: do not touch the brass button.");return true;}caught(p,b,place,own,"pressed_the_buzzer_release");return true;}
            if(rel.equals(UncannyRooms.HINGED)||rel.equals(UncannyRooms.HINGED.above())){own.putBoolean("HingeSeen",true);setDoor(p.serverLevel(),b.offset(UncannyRooms.HINGED),true);return true;}
            if((UncannyRooms.FALSE_DOORS.contains(rel)||UncannyRooms.FALSE_DOORS.stream().anyMatch(r->r.above().equals(rel)))&&p.serverLevel().getBlockState(b.offset(rel)).is(UncannyRegistry.PROP.get())&&p.serverLevel().getBlockState(b.offset(rel)).getValue(UncannyPropBlock.KIND)==FALSE_DOOR){own.putBoolean("FalseExamined",true);cue(p,"The frame has no hinge pins.");return true;}
        }
        return false;
    }
    public static boolean peel(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var held=p.getMainHandItem();if(!held.is(UncannyRegistry.CLEAVER.get())&&!(held.getItem() instanceof AxeItem)){cue(p,"The cleaver is on the workbench.");return true;}
        var at=b.offset(rel);var s=p.serverLevel().getBlockState(at);if(!UncannyFleshBlock.is(s))return false;
        if(p.distanceToSqr(at.getCenter())>36)return false;long now=p.serverLevel().getGameTime();if(own.contains("LastPeelAt")&&now-own.getLong("LastPeelAt")<8)return true;
        if(!p.serverLevel().destroyBlock(at,false,p))return false;own.putLong("LastPeelAt",now);recordCut(p,b,rel,own);
        if(!held.is(UncannyRegistry.CLEAVER.get()))held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);return true;
    }
    /** Shared by native completed mining and the deliberate right-click cut. */
    public static void recordCut(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        own.putInt("Peeled",own.getInt("Peeled")+1);var shared=world(p.serverLevel(),b,LabyrinthPlace.LAYERED_MAN);
        long now=p.serverLevel().getGameTime();shared.putLong("LastPeel",now);shared.putLong("LastPlayerPeel",now);
        UncannyCellar.cut(p.serverLevel(),b,rel,shared);own.putInt("Engulfment",Math.max(0,own.getInt("Engulfment")-6));
        CellarDialogue.cut(p,b,rel,own,shared);world(p.serverLevel(),b,LabyrinthPlace.LAYERED_MAN,shared);
    }
    public static void talk(ServerPlayer p,LiteraryActor a,LabyrinthPlace place,CompoundTag own){
        if(!LiteraryVignettes.participant(p)||!LiteraryVignettes.inside(p,place))return;
        if(place==LabyrinthPlace.SECOND_WAITRESS&&a.role()==LiteraryActor.UNCANNY_WAITRESS)UncannyDiner.talk(p,a,LiteraryVignettes.base(p,place),own);
        if(place==LabyrinthPlace.SECOND_WAITRESS&&a.role()==LiteraryActor.UNCANNY_FALSE_WAITRESS&&own.getBoolean("AskedMeal")&&!own.getBoolean("Ready")){var b=LiteraryVignettes.base(p,place);if(b!=null)caught(p,b,place,own,"answered_her");}
        if(place==LabyrinthPlace.LAYERED_MAN&&a.role()==LiteraryActor.UNCANNY_PEELER&&!UncannyCellar.askToLeave(p,LiteraryVignettes.base(p,place),own))CellarDialogue.worker(p,own);
    }
    public static boolean locked(ServerPlayer p){if(!LiteraryVignettes.inside(p,LabyrinthPlace.MISSING_SLEEP))return false;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.MISSING_SLEEP);return own.getInt("Sleeps")>0&&!own.getBoolean("BlockedDoor")&&!failed(own)&&!own.getBoolean("Completed");}
    public static void finishReward(ServerPlayer p,LabyrinthPlace place,CompoundTag own){
        if(place==LabyrinthPlace.PAINTED_INTERIOR)LiteraryVignettes.reward(p,place,own,"BoneRing",new ItemStack(UncannyRegistry.BONE_RING.get()));
        if(place==LabyrinthPlace.MISSING_SLEEP)LiteraryVignettes.reward(p,place,own,"SmokyMirror",new ItemStack(UncannyRegistry.SMOKY_MIRROR.get()));
    }
    public static void setDoor(ServerLevel l,BlockPos at,boolean open){for(int y=0;y<2;y++){var s=l.getBlockState(at.above(y));if(s.getBlock() instanceof DoorBlock)l.setBlock(at.above(y),s.setValue(DoorBlock.OPEN,open),F);}}
    private static void nativeStep(LiteraryActor a,Vec3 target,double step){var delta=target.subtract(a.position());if(delta.lengthSqr()>.001)a.move(MoverType.SELF,delta.length()<=step?delta:delta.normalize().scale(step));a.setDeltaMovement(Vec3.ZERO);}
}
