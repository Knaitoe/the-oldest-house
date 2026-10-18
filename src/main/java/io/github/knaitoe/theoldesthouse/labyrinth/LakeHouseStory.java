package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Saved private beats and original artifacts; the building, its doors and water remain shared. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHouseStory {
    public static final int STAY_TICKS=160,SAUNA_TICKS=60;
    private record Lift(ServerPlayer reader,BlockPos at,BlockState original,LakeHouseDress display,long until){}
    private static final Map<UUID,Lift> LIFTS=new HashMap<>();
    private static final Set<UUID> VISION=new HashSet<>();
    private static final Map<UUID,BlockPos> VIEWS=new HashMap<>();
    private record Touch(ServerPlayer reader,net.minecraft.server.level.ServerLevel level,BlockPos at,LakeHouseDress cue,long until){}
    private static final Map<UUID,Touch> TOUCHES=new HashMap<>();
    private LakeHouseStory(){}
    private static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LakeHouse.PLACE);}
    private static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),LakeHouse.PLACE,own);}
    public static boolean lit(ServerPlayer p){return own(p).getBoolean("Lit");}
    public static void arrive(ServerPlayer p,CompoundTag own){
        if(!own.getBoolean("LakeFreshVisit"))return;own.putBoolean("LakeDeparted",false);
        own.putBoolean("SoloGone",false);own.putBoolean("TessGreeting",false);own.putInt("TessLeaving",0);own.putInt("SaunaWait",0);own.putInt("SeatWait",0);own.putBoolean("InsideLit0505",false);
        if(own.getInt("Visit")>=3)own.putBoolean("FlashlightOut",false);
    }
    public static void depart(ServerPlayer p){var own=own(p);own.putBoolean("LakeDeparted",true);own.putBoolean("Here",false);save(p,own);releaseLift(p.getUUID());releaseVision(p);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void died(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!LakeHouse.inside(p))return;var own=own(p);own.putInt("Finish",0);if(own.getBoolean("Ready"))own.putBoolean("LakeDeparted",true);save(p,own);releaseLift(p.getUUID());releaseVision(p);}
    private static boolean house(Vec3 r){return Math.abs(r.x-.5)<9&&r.z<-161&&r.z>-179&&r.y>-43&&r.y<-14;}
    /** Inside the house's walls, basement and sauna included; relative to the base. */
    public static boolean withinWalls(Vec3 r){return house(r);}
    private static void view(ServerPlayer p,BlockPos at,BlockState state){p.connection.send(new ClientboundBlockUpdatePacket(at,state));}
    private static void lightView(ServerPlayer p,BlockPos b,CompoundTag own,boolean inside){
        boolean bright=own.getBoolean("Lit")&&inside;
        if(bright){
            if(!p.hasEffect(MobEffects.NIGHT_VISION)){p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,220,0,false,false,false));VISION.add(p.getUUID());}
            else if(VISION.contains(p.getUUID())&&p.getEffect(MobEffects.NIGHT_VISION).getDuration()<180)p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,220,0,false,false,false));
        }else releaseVision(p);
        if(own.getInt("T")%40!=5&&VIEWS.containsKey(p.getUUID()))return;
        VIEWS.put(p.getUUID(),b);
        for(var rel:LakeHouseInterior.LAMPS){var at=b.offset(rel);var state=p.serverLevel().getBlockState(at);if(state.is(LakeHouseRegistry.FITTING.get()))view(p,at,state.setValue(LakeHouseFittingBlock.LIT,own.getBoolean("Lit")));}
        var at=b.offset(LakeHouseInterior.PAINTING_AT);var state=p.serverLevel().getBlockState(at);
        if(state.is(LakeHouseRegistry.FITTING.get()))view(p,at,state.setValue(LakeHouseFittingBlock.LIT,own.getBoolean("PaintingFace")));
    }
    private static void releaseVision(ServerPlayer p){
        if(!VISION.remove(p.getUUID()))return;var effect=p.getEffect(MobEffects.NIGHT_VISION);
        if(effect!=null&&!effect.showIcon()&&effect.getAmplifier()==0&&effect.getDuration()<=220)p.removeEffect(MobEffects.NIGHT_VISION);
    }
    private static @Nullable LakeHouseDress display(ServerPlayer p,BlockPos at,BlockState state){
        var d=LakeHouseRegistry.DRESS.get().create(p.serverLevel());if(d==null)return null;d.reader(p.getUUID());d.moveTo(at.getX(),at.getY(),at.getZ());d.shape(state,0,0);
        return p.serverLevel().addFreshEntity(d)?d:null;
    }
    private static void dresses(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getInt("Visit")<3||LakeHouse.empty(p))return;
        for(int i=0;i<3;i++){
            String key="Dress"+i;var have=own.hasUUID(key)?p.serverLevel().getEntity(own.getUUID(key)):null;
            if(!(have instanceof LakeHouseDress d)||have.isRemoved()){
                if(own.hasUUID(key)||!p.serverLevel().areEntitiesLoaded(new net.minecraft.world.level.ChunkPos(b.offset(-7+i*2,-18,-168)).toLong()))continue;
                var d=display(p,b.offset(-7+i*2,-18,-168),LakeHouseFittingBlock.of(LakeHouseFittingBlock.Kind.DRESS,Direction.SOUTH));if(d!=null)own.putUUID(key,d.getUUID());
            }else if(own.getInt("T")%10==5){d.shape(d.paintedState(),(float)Math.sin((own.getInt("T")+i*37)*.025)*.12F,(float)Math.sin((own.getInt("T")+i*19)*.012)*.07F);}
        }
        if(own.getBoolean("SawPool")&&!own.getBoolean("Lit")&&own.hasUUID("Dress2")&&p.serverLevel().getEntity(own.getUUID("Dress2")) instanceof LakeHouseDress black&&p.position().distanceToSqr(b.offset(LakeHouseInterior.POOL).getCenter())<64){
            black.moveTo(b.getX()+4,b.getY()-38.5,b.getZ()-175);black.shape(black.paintedState(),(float)Math.sin(own.getInt("T")*.018)*.1F,0);
        }
        // 0.4.105: once every lamp is on, the black dress is the figure's. It comes down over the wax while the reader climbs.
        if(own.getBoolean("Lit")&&own.hasUUID("Dress2")&&p.serverLevel().getEntity(own.getUUID("Dress2")) instanceof LakeHouseDress worn){
            float settle=Math.min(1F,own.getInt("T")/120F);var at=b.offset(LakeHouseInterior.FIGURE);
            worn.moveTo(at.getX(),at.getY()+1.45-settle,at.getZ());
            if(own.getInt("T")%10==5)worn.shape(worn.paintedState(),0,(1-settle)*.08F*(float)Math.sin(own.getInt("T")*.05));
        }
    }
    private static void figure(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("Lit")||LakeHouse.empty(p))return;
        var a=own.hasUUID("Figure")?p.serverLevel().getEntity(own.getUUID("Figure")):null;
        if(a instanceof LiteraryActor)return;
        if(own.hasUUID("Figure")||!p.serverLevel().areEntitiesLoaded(new net.minecraft.world.level.ChunkPos(b.offset(LakeHouseInterior.FIGURE)).toLong()))return;
        var made=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(made==null)return;made.appearance(LiteraryActor.LAKE_WAX,0);made.bind(LakeHouse.PLACE,p.getUUID());made.setNoGravity(true);
        var at=b.offset(LakeHouseInterior.FIGURE);made.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,135,0);if(p.serverLevel().addFreshEntity(made))own.putUUID("Figure",made.getUUID());
    }
    /** Returns true when this reader's terminal scene takes over the ordinary gear/visit controller. */
    public static boolean reader(ServerPlayer p,BlockPos b,CompoundTag own){
        HousePackets.send(p,new LakeHouseViewPayload(1|(own.getBoolean("Lit")?2:0)|(LakeHouse.STAYED.equals(own.getString("Outcome"))?4:0)));
        LakeHouseTess.reader(p,b,own);
        if(own.getInt("Finish")>0){finishTick(p,b,own);return true;}
        if(LakeHouse.empty(p))return true;
        var r=p.position().subtract(Vec3.atLowerCornerOf(b));boolean inside=house(r);
        lightView(p,b,own,inside);dresses(p,b,own);figure(p,b,own);
        if(own.getBoolean("Ready"))return false;
        int visit=own.getInt("Visit");
        cues(p,b,own,visit);
        if(visit==2&&r.x>2&&r.z<-162&&r.z>-170&&inside&&!own.getBoolean("FlashlightFailed")){
            own.putBoolean("FlashlightFailed",true);own.putBoolean("FlashlightOut",true);p.displayClientMessage(Component.literal("The flashlight clicks. Nothing comes back on."),true);
        }
        if(visit>=3&&r.y>-20&&r.x<-2&&r.z>-170&&inside&&HouseWatchers.sees(p,b.offset(LakeHouseInterior.DRESSING).getCenter()))own.putBoolean("SawDresses",true);
        if(visit>=3&&r.y>-20&&r.x>2&&inside)own.putBoolean("SawBedrooms",true);
        if(visit>=3&&r.y<-37&&p.distanceToSqr(b.offset(LakeHouseInterior.POOL).getCenter())<64){
            var flow=p.getDeltaMovement();p.setDeltaMovement(flow.add(0,-.018,0));p.hurtMarked=true;
            if(!own.getBoolean("SawPool")){own.putBoolean("SawPool",true);own.putBoolean("HomePending",true);p.displayClientMessage(Component.literal("A black dress is coming down through the water. Tess points up.").withStyle(ChatFormatting.ITALIC),true);}
        }
        if(visit==3&&own.getBoolean("SawDresses")&&own.getBoolean("SawBedrooms")&&own.getBoolean("SawPool")&&r.y>-.3&&!p.isEyeInFluid(FluidTags.WATER)){
            own.putInt("Beat",3);if(!own.getBoolean("ThirdBeatSaid")){own.putBoolean("ThirdBeatSaid",true);p.displayClientMessage(Component.literal("You climb onto the raft. Water keeps dripping from the tanks."),false);}
        }
        if(own.getBoolean("RuleBroken")&&inside&&own.getInt("T")%100==5)ThreeFingers.hear(p,SoundEvents.DEEPSLATE_STEP,b.offset(0,-19,-165-(own.getInt("T")/100%8)),.6F,.75F);
        if(inside&&own.getInt("T")%20==5){
            var at=p.getEyePosition().add(p.getLookAngle().scale(.4));p.serverLevel().sendParticles(p,LakeHouseRegistry.STILL_BUBBLE.get(),true,at.x,at.y,at.z,2,.1,.12,.1,0);
            if(r.x>2&&r.z>-170&&HouseWatchers.sees(p,b.offset(LakeHouseInterior.PAINTING_AT).getCenter()))own.putBoolean("PaintingFace",true);
        }
        if(visit>=4&&own.getBoolean("SawPool")&&r.y<-37&&r.x>2&&r.z>-170&&r.z<-162){
            own.putInt("SaunaWait",own.getInt("SaunaWait")+5);
            var at=b.offset(LakeHouseInterior.SAUNA_EXIT);var s=p.serverLevel().getBlockState(at);if(s.is(LakeHouseRegistry.FITTING.get()))view(p,at,s.setValue(LakeHouseFittingBlock.LIT,false));
            p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.CLOUD,true,p.getX(),p.getEyeY(),p.getZ(),2,.7,.2,.7,.005);
            if(own.getInt("SaunaWait")>=SAUNA_TICKS&&!own.getBoolean("Lit")){own.putBoolean("Lit",true);own.putBoolean("HelloStage",true);own.putBoolean("FlashlightOut",false);own.putInt("T",1);p.displayClientMessage(Component.literal("Footsteps climb the stairs. Every lamp comes on, and the water in the house is suddenly easy to breathe."),false);}
        }
        if(own.getBoolean("Lit")){
            // 0.4.105: only leaving counts. A reader swimming back in through the half door (after a blackout, say) has
            // not left; the door counts once they have been inside the lit house and come out through it.
            if(own.getBoolean("HelloStage")&&inside)own.putBoolean("InsideLit0505",true);
            if(own.getBoolean("HelloStage")&&own.getBoolean("InsideLit0505")&&Math.abs(r.x)<2&&r.z>-160&&r.z<-156&&r.y>-26&&r.y<-21){complete(p,own,LakeHouse.LEFT_THE_LIGHTS_ON);return true;}
            if(p.getVehicle() instanceof LakeHouseSeat&&p.getVehicle().blockPosition().equals(b.offset(LakeHouseInterior.TABLE_PLACE))){own.putInt("SeatWait",own.getInt("SeatWait")+5);if(own.getInt("SeatWait")>=STAY_TICKS){complete(p,own,LakeHouse.STAYED);return true;}}
            else own.putInt("SeatWait",0);
        }
        return false;
    }
    /**
     * 0.4.105: the way down is shown, not stated. Until someone opens it, bubbles leak round the study wardrobe for a
     * reader on the tanks visit; on the last visit, steam from the sauna drifts up the stair behind it. Both are the
     * reader's own, so a reader without Tess sees them too.
     */
    private static void cues(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        if(own.getInt("T")%10!=5)return;
        var closet=b.offset(LakeHouseInterior.CLOSET);var face=new Vec3(closet.getX()+.5,closet.getY()+.9,closet.getZ()+1.05);
        if(visit>=3&&!own.getBoolean("SawPool")&&p.serverLevel().getBlockState(closet).is(LakeHouseRegistry.FITTING.get())){
            p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.BUBBLE,true,face.x,face.y,face.z,6,.45,.8,.05,.02);
            if(own.getInt("T")%60==5&&p.distanceToSqr(face)<144)ThreeFingers.hear(p,SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,closet,.5F,.8F);
            if(!own.getBoolean("ClosetHint0505")&&p.distanceToSqr(face)<64&&HouseWatchers.sees(p,closet.getCenter())){own.putBoolean("ClosetHint0505",true);p.displayClientMessage(Component.literal("Bubbles are leaking out round the edges of the wardrobe."),true);}
        }
        if(visit>=4&&own.getBoolean("SawPool")&&!own.getBoolean("Lit")){
            var low=b.offset(-6,-31,-174).getCenter();
            p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.CLOUD,true,face.x,face.y,face.z,3,.4,.6,.4,.01);
            p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.CLOUD,true,low.x,low.y,low.z,3,1,1.5,1,.01);
            if(!own.getBoolean("WarmHint0505")&&p.distanceToSqr(face)<64){own.putBoolean("WarmHint0505",true);p.displayClientMessage(Component.literal("Warm water is coming up the stair behind the wardrobe."),true);}
        }
    }
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var at=b.offset(rel);var state=p.serverLevel().getBlockState(at);if(!state.is(LakeHouseRegistry.FITTING.get())||LakeHouse.empty(p))return false;
        var kind=state.getValue(LakeHouseFittingBlock.KIND);
        if(kind==LakeHouseFittingBlock.Kind.CHAIR){
            if(p.getVehicle() instanceof LakeHouseSeat&&p.getVehicle().blockPosition().equals(b.offset(LakeHouseInterior.TABLE_PLACE))){p.stopRiding();return true;}
            if(!p.serverLevel().getEntitiesOfClass(LakeHouseSeat.class,new net.minecraft.world.phys.AABB(at).inflate(.2),q->!q.getPassengers().isEmpty()).isEmpty()){p.displayClientMessage(Component.literal("Someone is already sitting there."),true);return true;}
            var seat=LakeHouseRegistry.SEAT.get().create(p.serverLevel());if(seat!=null){seat.moveTo(at.getX()+.5,at.getY()+.1,at.getZ()+.5);if(p.serverLevel().addFreshEntity(seat))p.startRiding(seat);}
            p.displayClientMessage(Component.literal("The chair is dry. Air catches in your lungs."),true);return true;
        }
        if(kind==LakeHouseFittingBlock.Kind.WARDROBE&&rel.equals(LakeHouseInterior.CLOSET)){
            p.serverLevel().setBlock(at,Blocks.WATER.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_SUPPRESS_DROPS);own.putBoolean("ClosetOpened",true);p.displayClientMessage(Component.literal("The wardrobe swings away. Marks run down the stair behind it."),true);return true;
        }
        String line=switch(kind){
            case COATRACK->"The hooks turn. Nothing hanging here ever dries.";case KEY_BOWL->"The keys slide together with a small, ordinary sound.";
            case BULB,LAMP->own.getBoolean("Lit")?"The switch clicks. The light stays on.":"The switch clicks. The glass stays dark.";
            case ASH_TRAY->"Ash lies loose in the tray. None of it lifts into the water.";case BOOKCASE->"The covers open. The pages stay flat, even underwater.";
            case POOL_TABLE->"You nudge the cue ball. It rolls to the rail and stops.";case PAINTING->"The still life has turned toward you.";
            case BEAVER->"A porcelain beaver. A little pepper falls and lies on the counter.";case ISLAND->"The drawer opens without swelling. There are spoons inside.";
            case WARDROBE->"A dress turns, slowly, without a current.";case BED->"The quilt settles where you touched it. You don't lie down.";
            case SAUNA_BENCH,SAUNA_DOOR->"Warm wood. Steam, inside the water.";case HALF_DOOR->"Only the left half remains. It swings without a sound.";
            case CANDLE,HEARTH->"The flame keeps burning under the water.";case TABLE->"One more place is set than the people who came.";
            default->"It stays exactly where somebody left it.";};
        if(kind==LakeHouseFittingBlock.Kind.PAINTING)own.putBoolean("PaintingFace",true);
        if(java.util.EnumSet.of(LakeHouseFittingBlock.Kind.COATRACK,LakeHouseFittingBlock.Kind.KEY_BOWL,LakeHouseFittingBlock.Kind.BOOKCASE,LakeHouseFittingBlock.Kind.POOL_TABLE,LakeHouseFittingBlock.Kind.ISLAND,LakeHouseFittingBlock.Kind.BEAVER,LakeHouseFittingBlock.Kind.WARDROBE,LakeHouseFittingBlock.Kind.BED,LakeHouseFittingBlock.Kind.HALF_DOOR,LakeHouseFittingBlock.Kind.SAUNA_DOOR).contains(kind)){
            releaseTouch(p.getUUID());view(p,at,state.setValue(LakeHouseFittingBlock.LIT,true));LakeHouseDress cue=null;
            if(kind==LakeHouseFittingBlock.Kind.POOL_TABLE){cue=display(p,at,Blocks.SNOW_BLOCK.defaultBlockState());if(cue!=null)cue.small(Blocks.SNOW_BLOCK.defaultBlockState(),.375F,.8125F,.6875F,.0625F);}
            TOUCHES.put(p.getUUID(),new Touch(p,p.serverLevel(),at.immutable(),cue,p.level().getGameTime()+40));
            if(kind==LakeHouseFittingBlock.Kind.BEAVER)p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.ASH,true,at.getX()+.5,at.getY()+.1,at.getZ()+.2,3,.06,0,.08,0);
        }
        p.displayClientMessage(Component.literal(line),true);ThreeFingers.hear(p,kind==LakeHouseFittingBlock.Kind.POOL_TABLE?SoundEvents.WOOD_HIT:SoundEvents.WOODEN_BUTTON_CLICK_ON,at,.4F,.8F);return true;
    }
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(a.role()==LiteraryActor.LAKE_TESS)LakeHouseTess.talk(p,a,own);
        else if(a.role()==LiteraryActor.LAKE_WAX&&own.getBoolean("HelloStage")&&own.getBoolean("Lit")&&!own.getBoolean("Ready"))complete(p,own,LakeHouse.SAID_HELLO);
    }
    public static void complete(ServerPlayer p,CompoundTag own,String ending){
        if(own.getBoolean("Ready"))return;own.putBoolean("Ready",true);own.putString("Outcome",ending);own.putInt("Beat",own.getInt("Visit"));own.putInt("EndVisit",own.getInt("Visit"));
        own.putInt("Finish",100);own.putBoolean("Lit",ending.equals(LakeHouse.LEFT_THE_LIGHTS_ON));
        HousePackets.send(p,new HouseFadePayload(18,50,32));releaseVision(p);p.stopRiding();
        var artifact=ending.equals(LakeHouse.SAID_HELLO)?LakeHouseRegistry.FOUND_FLASHLIGHT.get():ending.equals(LakeHouse.LEFT_THE_LIGHTS_ON)?LakeHouseRegistry.HOUSE_LIGHT.get():LakeHouseRegistry.GOLD_HELMET.get();
        LiteraryVignettes.reward(p,LakeHouse.PLACE,own,"Artifact",new ItemStack(artifact));save(p,own);
    }
    private static void finishTick(ServerPlayer p,BlockPos b,CompoundTag own){
        int left=own.getInt("Finish");
        if(left==65){
            var target=b;boolean empty=LakeHouse.SAID_HELLO.equals(own.getString("Outcome"));
            if(empty){target=LakeHouse.emptyBase(p.server);if(target==null)return;IndianLakeRooms.keepLoaded(p.serverLevel(),target,LabyrinthPlace.LAKE_HOUSE_EMPTY);
                if(!LabyrinthBuilder.isPlaceReady(LabyrinthData.get(p.server),LabyrinthPlace.LAKE_HOUSE_EMPTY)||!p.serverLevel().hasChunkAt(target))return;}
            var at=empty?Vec3.atBottomCenterOf(target.offset(LakeHouse.DOCK_END.above())):Vec3.atBottomCenterOf(target.offset(LakeHouse.RAFT.offset(1,1,0)));
            p.teleportTo(p.serverLevel(),at.x,at.y,at.z,180,10);p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();LakeHouseDive.setAir(p,p.getMaxAirSupply());
            dismiss(p,own);own.putBoolean("FinishMoved",true);
            p.displayClientMessage(Component.literal(empty?"You come up into an empty lake. There is no roof beneath the water.":LakeHouse.STAYED.equals(own.getString("Outcome"))?"You wake on the raft. It feels like dawn. Your place is still set below.":"From the raft you can still see it, lit all the way down."),false);
        }
        own.putInt("Finish",Math.max(0,left-5));
    }
    private static void dismiss(ServerPlayer p,CompoundTag own){
        for(String key:List.of("Figure","Dress0","Dress1","Dress2")){if(own.hasUUID(key)){var e=p.serverLevel().getEntity(own.getUUID(key));if(e!=null)e.discard();own.remove(key);}}
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public static void mining(PlayerInteractEvent.LeftClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator()||!p.isAlive()||e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!LakeHouse.inside(p)||LakeHouse.empty(p))return;
        var b=LakeHouse.readerBase(p);if(b==null||!house(Vec3.atCenterOf(e.getPos()).subtract(Vec3.atLowerCornerOf(b)))||p.distanceToSqr(e.getPos().getCenter())>36)return;
        e.setCanceled(true);var own=own(p);long now=p.level().getGameTime();if(own.contains("LastAsk")&&now-own.getLong("LastAsk")<20)return;
        own.putLong("LastAsk",now);own.putInt("RuleAttempts",own.getInt("RuleAttempts")+1);
        if(own.getInt("RuleAttempts")==1){p.displayClientMessage(Component.literal("Don't ask how."),false);LakeHouseTess.line(p,LakeHouseTess.actor(p,own),"Don't ask how.");}
        else{own.putBoolean("RuleBroken",true);p.displayClientMessage(Component.literal("Someone starts walking upstairs."),false);LakeHouseTess.line(p,LakeHouseTess.actor(p,own),"Couldn't it just be ours?");}
        releaseLift(p.getUUID());var state=p.serverLevel().getBlockState(e.getPos());var d=display(p,e.getPos(),state);
        if(d!=null){view(p,e.getPos(),Blocks.WATER.defaultBlockState());LIFTS.put(p.getUUID(),new Lift(p,e.getPos().immutable(),state,d,now+40));}save(p,own);
    }
    private static void releaseLift(UUID id){var lift=LIFTS.remove(id);if(lift==null)return;lift.display().discard();if(lift.reader().connection!=null&&lift.reader().serverLevel().hasChunkAt(lift.at()))view(lift.reader(),lift.at(),lift.reader().serverLevel().getBlockState(lift.at()));}
    private static void releaseTouch(UUID id){var a=TOUCHES.remove(id);if(a==null)return;if(a.cue()!=null)a.cue().discard();if(a.reader().serverLevel()==a.level()&&a.level().hasChunkAt(a.at()))view(a.reader(),a.at(),a.level().getBlockState(a.at()));}
    /** A real morning wake at home, once. Footsteps and dripping are sent only to the reader; no home blocks change. */
    @SubscribeEvent public static void waking(PlayerWakeUpEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator()||p.level().dimension()!=net.minecraft.world.level.Level.OVERWORLD||!HouseDays.isMorningWake(p))return;
        var own=own(p);if(!own.getBoolean("HomePending")||own.getBoolean("HomeSeen"))return;
        own.putBoolean("HomeSeen",true);own.putBoolean("HomePending",false);own.putInt("HomeSteps",120);save(p,own);
        p.displayClientMessage(Component.literal("Water drips from the ceiling. Wet footprints stop beside the bed."),false);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        long now=e.getServer().overworld().getGameTime();
        for(var entry:new ArrayList<>(TOUCHES.entrySet())){var a=entry.getValue();if(!a.reader().isAlive()||a.reader().serverLevel()!=a.level()||!LakeHouse.inside(a.reader())||a.level().getGameTime()>=a.until()){releaseTouch(entry.getKey());continue;}if(a.cue()!=null){float t=1-(a.until()-a.level().getGameTime())/40F;a.cue().small(Blocks.SNOW_BLOCK.defaultBlockState(),.375F,.8125F,.6875F+Math.min(1,t*2)*.25F,.0625F);}}
        for(var entry:new ArrayList<>(LIFTS.entrySet())){var lift=entry.getValue();var p=lift.reader();if(p.level().getGameTime()>=lift.until()||!p.isAlive()||!LakeHouse.inside(p)){releaseLift(entry.getKey());continue;}
            float t=(40-(lift.until()-p.level().getGameTime()))/40F;lift.display().shape(lift.original(),(float)Math.sin(t*Math.PI)*.45F,(float)Math.sin(t*Math.PI*2)*.32F);}
        for(var p:e.getServer().getPlayerList().getPlayers()){
            if(!LakeHouse.inside(p)){releaseVision(p);var b=VIEWS.remove(p.getUUID());if(b!=null&&p.serverLevel().dimension()==HouseDimensions.OUTSIDE)for(var rel:LakeHouseInterior.LAMPS){var at=b.offset(rel);if(p.serverLevel().hasChunkAt(at))view(p,at,p.serverLevel().getBlockState(at));}}
            if(now%5!=0)continue;var own=own(p);int t=own.getInt("HomeSteps");if(t>0&&p.level().dimension()==net.minecraft.world.level.Level.OVERWORLD){
                var bed=p.getRespawnPosition()==null?p.blockPosition():p.getRespawnPosition();var door=bed;
                double nearest=Double.MAX_VALUE;
                for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++){var at=bed.offset(x,0,z);if(p.serverLevel().hasChunkAt(at)&&p.serverLevel().getBlockState(at).getBlock() instanceof DoorBlock&&at.distSqr(bed)<nearest){door=at;nearest=at.distSqr(bed);}}
                double progress=1-t/120.0;var at=Vec3.atCenterOf(door).lerp(Vec3.atCenterOf(bed),progress);
                p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.DRIPPING_WATER,true,at.x,bed.getY()+.12,at.z,4,.12,.01,.2,0);
                if(t%20==0){var direction=Vec3.atCenterOf(bed).subtract(Vec3.atCenterOf(door));double yaw=Math.atan2(direction.z,direction.x);var floor=BlockPos.containing(at.x,bed.getY(),at.z);if(p.serverLevel().hasChunkAt(floor)&&p.serverLevel().getBlockState(floor).getCollisionShape(p.serverLevel(),floor).isEmpty())p.serverLevel().sendParticles(p,LakeHouseRegistry.WET_FOOTPRINT.get(),true,at.x,bed.getY()+.02,at.z,0,yaw,0,0,1);}
                p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.DRIPPING_WATER,true,bed.getX()+.5,bed.getY()+2.4,bed.getZ()+.5,3,.7,.05,.7,0);
                if(t%20==0)ThreeFingers.hear(p,SoundEvents.WET_GRASS_STEP,BlockPos.containing(at),.45F,.8F);own.putInt("HomeSteps",Math.max(0,t-5));save(p,own);
            }
        }
        var l=e.getServer().getLevel(HouseDimensions.OUTSIDE);var b=LakeHouse.base(e.getServer());if(l==null||b==null||!l.hasChunkAt(b.offset(LakeHouse.HOUSE)))return;
        // Vanilla normally makes dropped items bob upward. Here the exact native originals sink and lie still.
        for(var fish:l.getEntitiesOfClass(net.minecraft.world.entity.animal.AbstractFish.class,LakeHouse.box(b),q->house(q.position().subtract(Vec3.atLowerCornerOf(b)))))fish.moveTo(b.getX()+.5,fish.getY(),b.getZ()-158.5);
        for(var item:l.getEntitiesOfClass(ItemEntity.class,LakeHouse.box(b),q->house(q.position().subtract(Vec3.atLowerCornerOf(b))))){var v=item.getDeltaMovement();item.setDeltaMovement(item.onGround()?Vec3.ZERO:new Vec3(v.x*.8,-.065,v.z*.8));}
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){releaseLift(e.getEntity().getUUID());releaseTouch(e.getEntity().getUUID());if(e.getEntity() instanceof ServerPlayer p)releaseVision(p);VIEWS.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var id:new ArrayList<>(LIFTS.keySet()))releaseLift(id);for(var id:new ArrayList<>(TOUCHES.keySet()))releaseTouch(id);for(var p:e.getServer().getPlayerList().getPlayers())releaseVision(p);VISION.clear();VIEWS.clear();}
}
