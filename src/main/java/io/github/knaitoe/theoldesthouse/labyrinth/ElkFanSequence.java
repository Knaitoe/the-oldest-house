package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.*;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Finite real bulb repair and a five-second present platform ending, using the actual saved door return. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ElkFanSequence {
    private static final LabyrinthPlace P=LabyrinthPlace.ELK_FAN;
    private static final String DRESSING="fan_dressing_0488";
    private ElkFanSequence() {}
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(own.getBoolean("BladeStrike")&&rel.getY()==-1&&Math.abs(rel.getX())<=2&&rel.getZ()>=-20&&rel.getZ()<=-15){own.putBoolean("TapeSeen",true);p.displayClientMessage(Component.literal(own.getInt("Visit")>=3?"The tape has gone. Dust keeps the muzzle, antlers and splayed legs.":"The tape bends around a muzzle, split antlers and four splayed legs."),false);return true;}
        if(rel.equals(SceneHuntReview.TOOLS)){
            LiteraryVignettes.reward(p,P,own,"Lightbulb",new ItemStack(LiteraryRegistry.LIGHTBULB.get()));
            p.displayClientMessage(Component.literal(own.getBoolean("Repaired")?"The empty bulb carton is still in the tool tray.":"A replacement bulb lies beside the tools."),true);return true;
        }
        if(!rel.equals(LiteraryRooms.FAN)||p.getY()<b.getY()+4.5||!(own.getBoolean("BuiltReach")||ElkFanRoom.onLadder(p,b)))return false;
        if(own.getBoolean("Repaired")){p.displayClientMessage(Component.literal("The replacement bulb is secure."),true);return true;}
        var bulb=p.getMainHandItem();var custom=bulb.get(DataComponents.CUSTOM_DATA);var mark=custom==null?new CompoundTag():custom.copyTag();
        if(!bulb.is(LiteraryRegistry.LIGHTBULB.get())||!mark.hasUUID(LiteraryVignettes.OWNER)||!mark.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID())||!mark.getString(LiteraryVignettes.PLACE).equals(P.id())||!mark.getString(LiteraryVignettes.KEY).equals("Lightbulb")){
            p.displayClientMessage(Component.literal("The bulb socket is empty. There was a carton by the tools."),true);return true;
        }
        var at=b.offset(LiteraryRooms.FAN);var state=p.serverLevel().getBlockState(at);if(!state.is(LiteraryRegistry.PROP.get())||state.getValue(LiteraryPropBlock.KIND)!=LiteraryPropBlock.Kind.FAN)return false;
        bulb.shrink(1);own.putBoolean("BuiltReach",true);own.putBoolean("Repaired",true);
        var data=LabyrinthData.get(p.server);var shared=LiteraryVignettes.shared(data,P);shared.putBoolean("BulbFixed0488",true);LiteraryVignettes.shared(data,P,shared);
        p.serverLevel().setBlock(at,state.setValue(LiteraryPropBlock.STAGE,2),3);p.displayClientMessage(Component.literal("The bulb catches. The light steadies."),true);return true;
    }
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var data=LabyrinthData.get(p.server);var world=LiteraryVignettes.shared(data,P);var at=b.offset(LiteraryRooms.FAN);var state=p.serverLevel().getBlockState(at);
        if(state.is(LiteraryRegistry.PROP.get())&&state.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.FAN){
            int stage=world.getBoolean("BulbFixed0488")||own.getBoolean("Repaired")?2:(p.level().getGameTime()%96<9||p.level().getGameTime()%96>=27&&p.level().getGameTime()%96<37?1:0);
            if(state.getValue(LiteraryPropBlock.STAGE)!=stage)p.serverLevel().setBlock(at,state.setValue(LiteraryPropBlock.STAGE,stage),3);
        }
        var display=new CompoundTag();display.putUUID("Reader",p.getUUID());display.putBoolean("Scar",LiteraryVignettes.personal(data,p.getUUID(),LabyrinthPlace.ELK_LOT).getBoolean("ElkWounded"));
        display.putInt("FanOutline",own.getBoolean("BladeStrike")?own.getInt("Visit")>=3?2:1:0);display.putLong("FanBase",b.asLong());HousePackets.send(p,new LiteraryViewPayload(-1,0,0,display));
        boolean platform=ElkFanRoom.onLadder(p,b)||own.getBoolean("BuiltReach")&&p.getY()>=b.getY()+4.5&&p.getY()<b.getY()+6.5&&Math.abs(p.getX()-b.getX()-.5)<1.3&&Math.abs(p.getZ()-b.getZ()+16.5)<1.3;
        if(!own.getBoolean("BladeStrike")){
            if(!platform){own.putInt("FanTicks",0);return;}
            own.putBoolean("BuiltReach",true);own.putInt("FanTicks",own.getInt("FanTicks")+5);
            if(HouseWatchers.sees(p,b.offset(0,0,-17).getCenter()))own.putBoolean("ElkThroughFan",true);
            if(own.getInt("FanTicks")>=100){p.setDeltaMovement(.85,.15,-.4);p.hurt(p.damageSources().generic(),Math.min(3,Math.max(0,p.getHealth()-1)));own.putBoolean("BladeStrike",true);own.putBoolean("FanReturn0488",true);own.putInt("Beat",own.getInt("Visit"));LiteraryVignettes.finishFan(p,own);p.displayClientMessage(Component.literal("The blade catches. The room goes dark."),false);}
        }
        // Keep unfinished older multi-visit accounts playable, without overwriting completed records.
        if(own.getBoolean("BladeStrike")&&own.getBoolean("ElkThroughFan")&&own.getInt("Visit")>=2&&HouseWatchers.sees(p,b.offset(0,0,-17).getCenter())){own.putBoolean("TapeSeen",true);if(!own.getBoolean("Ready"))LiteraryVignettes.ready(p,P,own,"returned_to_the_deer_outline");}
    }
    public static void markDressed(net.minecraft.server.level.ServerLevel l,BlockPos b){var data=LabyrinthData.get(l.getServer());var state=data.state(DRESSING);state.putBoolean(Long.toString(b.asLong()),true);data.setState(DRESSING,state);}
    private static void dress(net.minecraft.server.MinecraftServer server){
        var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;var data=LabyrinthData.get(server);if(!LabyrinthBuilder.isPlaceReady(data,P))return;var l=server.getLevel(HouseDimensions.INTERIOR);var b=LabyrinthPlaces.base(origin,P);if(l==null||b==null||data.state(DRESSING).getBoolean(Long.toString(b.asLong())))return;
        var area=new AABB(Vec3.atLowerCornerOf(b.offset(-11,-1,-28)),Vec3.atLowerCornerOf(b.offset(12,10,1)));
        if(!ScenePolish.loaded(l,area,b)||l.players().stream().anyMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox()))||!l.getEntitiesOfClass(Entity.class,area,e->e.isAlive()).isEmpty())return;
        // Only the previous exact floor-border blocks are removed; loose originals, containers and edits block or survive this pass.
        for(int x=-3;x<=3;x++)for(int z=-20;z<=-14;z++)if(Math.abs(x)==3||z==-20||z==-14){var at=b.offset(x,-1,z);var old=l.getBlockState(at);if(l.getBlockEntity(at)==null&&(old.is(LiteraryRegistry.TAPE.get())||old.is(LiteraryRegistry.PEELED_TAPE.get())))l.setBlock(at,Blocks.GRAY_WOOL.defaultBlockState(),3);}
        markDressed(l,b);
    }
    @SubscribeEvent public static void finish(ServerTickEvent.Post e){for(var p:e.getServer().getPlayerList().getPlayers()){
        if(!LiteraryVignettes.inside(p,P)||LabyrinthDoors.isBusy(p))continue;var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),P);if(!own.getBoolean("FanReturn0488"))continue;
        if(LabyrinthDoors.finishTakeAndReturn(p,P)){own=LiteraryVignettes.personal(data,p.getUUID(),P);own.remove("FanReturn0488");LiteraryVignettes.save(data,p.getUUID(),P,own);}
        else if(data.peekReturn(p.getUUID())==null){p.teleportTo(p.serverLevel(),LiteraryVignettes.base(p,P).getX()+.5,LiteraryVignettes.base(p,P).getY(),LiteraryVignettes.base(p,P).getZ()+2.5,p.getYRot(),p.getXRot());own.remove("FanReturn0488");LiteraryVignettes.save(data,p.getUUID(),P,own);}
    }if(e.getServer().getTickCount()%40==0)dress(e.getServer());}
}
