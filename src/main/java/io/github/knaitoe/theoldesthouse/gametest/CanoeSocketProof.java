package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;

/** Opt-in connected riders on two authored culverts; no artificial immunity or shortened collision boxes. */
final class CanoeSocketProof {
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static final Map<UUID,UUID> BOATS=new HashMap<>();
    private static final Map<UUID,Float> HEALTH=new HashMap<>();
    private static final Map<UUID,Integer> SIDES=new HashMap<>();
    private static BlockPos lake,empty;private static UUID tess;private static Vec3 previousTess;
    private CanoeSocketProof(){}
    private static void require(boolean yes,String text){if(!yes)throw new IllegalStateException("LIVE CANOE: "+text);}
    static BlockPos base(ServerPlayer p){return p.getGameProfile().getName().endsWith("A")?lake:empty;}
    private static LakeHouseCanoe boat(ServerPlayer p){var e=p.serverLevel().getEntity(BOATS.get(p.getUUID()));require(e instanceof LakeHouseCanoe,"the same lent canoe is loaded");return (LakeHouseCanoe)e;}
    static void begin(ServerPlayer a,ServerPlayer b,BlockPos origin){
        var level=a.server.getLevel(HouseDimensions.OUTSIDE);var data=LabyrinthData.get(a.server);
        lake=LabyrinthPlaces.base(origin,LabyrinthPlace.LAKE_HOUSE);empty=LabyrinthPlaces.base(origin,LabyrinthPlace.LAKE_HOUSE_EMPTY);
        for(var at:List.of(lake,empty))CHUNKS.hold(level,new AABB(Vec3.atLowerCornerOf(at.offset(-12,-44,-184)),Vec3.atLowerCornerOf(at.offset(12,12,8))));
        LiteraryRooms.build(level,lake,LabyrinthPlace.LAKE_HOUSE);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.LAKE_HOUSE,lake);
        LakeHouse.buildEmpty(level,empty);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.LAKE_HOUSE_EMPTY,empty);
        for(var p:List.of(a,b)){
            GoatmanVignette.depart(p);p.stopRiding();CanoeRiderPose.apply(p,false);p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(false);p.setNoGravity(false);p.getFoodData().setFoodLevel(20);p.setHealth(p.getMaxHealth());p.setAirSupply(p.getMaxAirSupply());p.setItemSlot(EquipmentSlot.HEAD,net.minecraft.world.item.ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.CHEST,net.minecraft.world.item.ItemStack.EMPTY);
            var at=base(p);p.teleportTo(level,at.getX()+1,at.getY()+.2,at.getZ()-104.5,180,0);p.hasChangedDimension();p.connection.resetPosition();
            if(p==b){var own=LiteraryVignettes.personal(data,p.getUUID(),LakeHouse.PLACE);own.putInt("Visit",1);own.putInt("EndVisit",1);own.putBoolean("Ready",true);own.putString("Outcome",LakeHouse.SAID_HELLO);own.putInt("Finish",0);LiteraryVignettes.save(data,p.getUUID(),LakeHouse.PLACE,own);}
            LiteraryVignettes.onArrive(p,p==a?LabyrinthPlace.LAKE_HOUSE:LabyrinthPlace.LAKE_HOUSE_EMPTY);LakeHouseCanoe.ensure(p);
            var originals=level.getEntitiesOfClass(LakeHouseCanoe.class,LakeHouse.box(at),c->p.getUUID().equals(c.owner()));require(originals.size()==1,"each reader has exactly one actual lent canoe");var c=originals.getFirst();c.moveTo(at.getX()+1,at.getY()-.35,at.getZ()-104.5,180,0);c.setDeltaMovement(Vec3.ZERO);require(p.startRiding(c),"the actual socket reader boards their original");c.positionRider(p);BOATS.put(p.getUUID(),c.getUUID());HEALTH.put(p.getUUID(),p.getHealth());SIDES.put(p.getUUID(),0);
        }
    }
    static boolean ready(ServerPlayer a,ServerPlayer b){
        if(!CHUNKS.ready())return false;var own=LiteraryVignettes.personal(LabyrinthData.get(a.server),a.getUUID(),LakeHouse.PLACE);var actor=LakeHouseTess.actor(a,own);
        if(actor==null||actor.getVehicle()!=boat(a)||actor.phase()!=1)return false;tess=actor.getUUID();return a.getVehicle()==boat(a)&&b.getVehicle()==boat(b);
    }
    static void observe(ServerPlayer a,ServerPlayer b,int phase,int age){
        if(age<40)return;
        for(var p:List.of(a,b)){
            var c=boat(p);if(phase==48||phase==49){
                require(p.getVehicle()==c&&p.getHealth()==HEALTH.get(p.getUUID())&&!p.isInWall()&&!p.isEyeInFluid(FluidTags.WATER)&&p.getAirSupply()==p.getMaxAirSupply()&&!LakeHouseDive.choking(p.getUUID()),"the connected rider must remain alive, dry-eyed and at full breath, including actual client-owned buoyancy: "+p.getGameProfile().getName()+" y="+p.getY()+" eye="+p.getEyeY()+" boat="+c.getY()+" pose="+p.getPose()+" air="+p.getAirSupply());
                require(!p.serverLevel().getBlockCollisions(p,p.getBoundingBox()).iterator().hasNext(),"the actual full native rider body clears the authored ceiling and walls");
                if(c.underLowRoof())require(p.getForcedPose()==Pose.CROUCHING,"the seated reader ducks only below the actual roof");
                int side=c.pushSide();if(side!=0)SIDES.put(p.getUUID(),SIDES.get(p.getUUID())|(side<0?1:2));
            }
        }
        if(phase==48||phase==49){var actor=a.serverLevel().getEntity(tess);require(actor instanceof LiteraryActor t&&t.getVehicle()==boat(a)&&t.phase()==1&&!t.isEyeInFluid(FluidTags.WATER)&&t.getAirSupply()==t.getMaxAirSupply(),"the same seated Tess has dry eyes and no native drowning bubbles");}
        if(age%40==0)for(var p:List.of(a,b))write(p.getGameProfile().getName()+"-canoe-status.txt","phase="+phase+" age="+age+" body="+p.position()+" eye="+p.getEyeY()+" pose="+p.getPose()+" forced="+p.getForcedPose()+" health="+p.getHealth()+" air="+p.getAirSupply()+" side="+boat(p).pushSide()+"\n");
    }
    static void waited(ServerPlayer a,ServerPlayer b,int age){require(age>=340,"the actual clients wait beyond an ordinary 300-tick drowning window");observe(a,b,48,age);}
    static void pushed(ServerPlayer a,ServerPlayer b){
        for(var p:List.of(a,b)){var c=boat(p);require(SIDES.get(p.getUUID())==3&&c.getZ()<base(p).getZ()+LakeHouse.CHANNEL_NORTH-8&&!c.underLowRoof()&&p.getForcedPose()==null,"both actual hand packets propel the original through the culvert into open water and restore the original body");}
    }
    static void dived(ServerPlayer a,ServerPlayer b){
        for(var p:List.of(a,b))require(!p.isPassenger()&&p.getForcedPose()==null&&p.isEyeInFluid(FluidTags.WATER)&&p.getAirSupply()<p.getMaxAirSupply()-20&&p.getAirSupply()>0&&p.getHealth()==HEALTH.get(p.getUUID()),"native dismount and a real dive restore normal air consumption without a held canoe pose or blanket breath immunity");
    }
    static void swim(ServerPlayer a,ServerPlayer b){
        b.setAirSupply(b.getMaxAirSupply());require(b.startRiding(boat(b)),"the peer waits safely in their same canoe");boat(b).positionRider(b);
        var data=LabyrinthData.get(a.server);var own=LiteraryVignettes.personal(data,a.getUUID(),LakeHouse.PLACE);own.putInt("Visit",3);own.putInt("Beat",2);own.putBoolean("Ready",false);own.putInt("Finish",0);own.putBoolean("SoloGone",false);own.remove("TessLeaving");LiteraryVignettes.save(data,a.getUUID(),LakeHouse.PLACE,own);
        var original=a.serverLevel().getEntity(tess);require(original instanceof LiteraryActor,"the canoe's original Tess is retained for the real stair-route fixture");var actor=(LiteraryActor)original;actor.stopRiding();actor.appearance(LiteraryActor.LAKE_TESS,2);actor.moveTo(lake.getX()+.5,lake.getY()-24,lake.getZ()-159.5,180,0);actor.setDeltaMovement(Vec3.ZERO);previousTess=actor.position();
        // Explicit test setup isolates physical swimming from four separate story visits; the production follower moves every step.
        a.setNoGravity(true);a.setAirSupply(a.getMaxAirSupply());a.setItemSlot(EquipmentSlot.CHEST,LakeHouseGear.lend(a,LakeHouseRegistry.TANKS.get()));a.teleportTo(a.serverLevel(),lake.getX()+7.5,lake.getY()-18.8,lake.getZ()-175.5,180,0);a.connection.resetPosition();
    }
    static void swimming(ServerPlayer a){
        var actor=(LiteraryActor)a.serverLevel().getEntity(tess);require(actor!=null&&!actor.isRemoved()&&actor.phase()==2,"the original swimming guide remains in her physical phase");
        require(!a.serverLevel().getBlockCollisions(actor,actor.getBoundingBox().deflate(.001)).iterator().hasNext()&&actor.position().distanceTo(previousTess)<1,"Tess follows the real door and stairs without crossing a wall or being warped to the reader");previousTess=actor.position();
        if(a.server.getTickCount()%20==0)write("tess-native-route.txt","uuid="+actor.getUUID()+" at="+actor.position().subtract(Vec3.atLowerCornerOf(lake))+" yaw="+actor.getYRot()+" pitch="+actor.getXRot()+" health="+actor.getHealth()+" air="+actor.getAirSupply()+"\n");
    }
    static void finish(ServerPlayer a,ServerPlayer b){
        var actor=(LiteraryActor)a.serverLevel().getEntity(tess);require(actor!=null&&actor.getY()>lake.getY()-20&&actor.distanceToSqr(a)<64,"the same original reaches the real upper room, visibly swimming up the native stair");
        write("canoe-passed.txt","Two actual survival clients waited beyond the ordinary drowning window under the authored roof with full breath and unchanged health. Both sent alternating native hand packets and physically left the culvert in their original lent canoe; dismount restored ordinary underwater air consumption. Tess remained the original dry-eyed seated passenger, then followed the physical foyer/stair route in the real actor renderer. The upper-room position is explicit fixture setup; this does not claim a complete four-visit lake-house playthrough.\n");a.setNoGravity(false);CHUNKS.close();
    }
    private static void write(String name,String content){try{Files.createDirectories(LiveExpeditionProof.folder());Files.writeString(LiveExpeditionProof.folder().resolve(name),content,StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception e){throw new IllegalStateException(e);}}
}
