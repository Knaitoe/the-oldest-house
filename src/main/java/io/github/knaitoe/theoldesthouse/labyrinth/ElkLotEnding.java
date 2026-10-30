package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.phys.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Two physical approaches, two witnessed captures, one personal source. Nothing advances off-line. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ElkLotEnding {
    public static final String MEN="taken_by_the_bar_men",HERD="taken_by_the_elk_herd";
    public static final int CAPTURE_TICKS=80;
    private static final ResourceLocation HOLD=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"elk_capture");
    private static final Map<UUID,ServerPlayer> HELD=new HashMap<>();
    private ElkLotEnding(){}
    public static boolean held(ServerPlayer p){return LiteraryVignettes.inside(p,ElkLot.PLACE)&&LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),ElkLot.PLACE).getInt("CaptureTicks0492")>0;}
    public static boolean choseMen(ServerPlayer p){var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),ElkLot.PLACE);return LiteraryVignettes.inside(p,ElkLot.PLACE)&&!own.getBoolean("Ready")&&!own.getBoolean("Completed")&&MEN.equals(own.getString("EndingSide0492"));}
    /** The shared original men follow one living reader at a time; peers never drive or inherit that capture. */
    public static ServerPlayer menTarget(net.minecraft.server.MinecraftServer s){return ElkLotNight.here(s).stream().filter(ElkLotEnding::choseMen).sorted(Comparator.<ServerPlayer>comparingInt(p->held(p)?0:1).thenComparing(p->p.getUUID().toString())).findFirst().orElse(null);}
    public static void release(ServerPlayer p){var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null)speed.removeModifier(HOLD);HELD.remove(p.getUUID());}
    public static void clearAll(){for(var p:new ArrayList<>(HELD.values()))release(p);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)release(p);}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)release(p);}
    @SubscribeEvent public static void died(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)release(p);}
    @SubscribeEvent public static void playerTick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&HELD.containsKey(p.getUUID())&&(!p.isAlive()||!LiteraryVignettes.inside(p,ElkLot.PLACE)))release(p);}
    public static boolean step(LivingEntity body,Vec3 target,double speed){
        return SceneFootpaths.step(body,target,speed);
    }
    private static LiteraryElk herd(ServerPlayer p,BlockPos b,int i){
        var d=LabyrinthData.get(p.server);var w=LiteraryVignettes.shared(d,ElkLot.PLACE);String key="EndingHerd0492_"+i+"_"+p.getUUID();
        if(w.hasUUID(key))return p.serverLevel().getEntity(w.getUUID(key)) instanceof LiteraryElk elk?elk:null;
        var at=new Vec3(b.getX()+new double[]{-4,5,10}[i],b.getY(),b.getZ()-64-i*3);
        var cell=BlockPos.containing(at);if(!p.serverLevel().hasChunkAt(cell)||!p.serverLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(cell.getX()>>4,cell.getZ()>>4)))return null;
        var elk=LiteraryRegistry.ELK.get().create(p.serverLevel());if(elk==null)return null;elk.owner(p.getUUID());elk.setInvulnerable(true);elk.setNoGravity(true);elk.moveTo(at);
        if(!p.serverLevel().addFreshEntity(elk))return null;w.putUUID(key,elk.getUUID());LiteraryVignettes.shared(d,ElkLot.PLACE,w);return elk;
    }
    private static List<LiteraryActor> men(ServerPlayer p){var w=LiteraryVignettes.shared(LabyrinthData.get(p.server),ElkLot.PLACE);var out=new ArrayList<LiteraryActor>();for(var person:ElkLotNight.MEN)if(w.hasUUID(person.key())&&p.serverLevel().getEntity(w.getUUID(person.key())) instanceof LiteraryActor a)out.add(a);return out;}
    private static boolean touches(ServerPlayer p,LivingEntity body){return body.isAlive()&&body.getBoundingBox().inflate(.7,.2,.7).intersects(p.getBoundingBox())&&p.serverLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),body.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;}
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own,CompoundTag night){
        if(!LiteraryVignettes.inside(p,ElkLot.PLACE)){release(p);return;}
        if(own.getBoolean("Ready")||own.getBoolean("Completed")||ElkFollows.overTonight(own)){release(p);hide(p);return;}
        boolean handContact=ElkLotNight.stage(night)>=3&&men(p).stream().anyMatch(a->touches(p,a));
        if(!own.getBoolean("ElkSeen")&&!handContact&&own.getInt("CaptureTicks0492")==0)return;
        int field=own.getInt("FieldTicks");boolean pressure=ElkLotNight.stage(night)>=3||field>=120;
        if(!pressure)return;
        if(!own.getBoolean("Horrors0492")){own.putBoolean("Horrors0492",true);p.displayClientMessage(Component.literal("The men have stopped asking. Their lights find the road behind you. Beyond the broken fence, the eyes are moving closer. The bar door is still open."),false);}
        String side=own.getString("EndingSide0492");
        if(own.getInt("CaptureTicks0492")==0){if(MEN.equals(side)&&!ElkLotNight.inBar(p.position(),b)){side="";own.remove("EndingSide0492");}else if(HERD.equals(side)&&ElkLotNight.inBar(p.position(),b)){side="";own.remove("EndingSide0492");}}
        // Crossing either threshold is the choice. Merely arriving or watching a peer never commits it.
        if(side.isEmpty()){
            if(handContact){side=MEN;own.putString("EndingSide0492",side);}
            else if(ElkLotNight.inBar(p.position(),b)&&ElkLotNight.stage(night)>=3){side=MEN;own.putString("EndingSide0492",side);p.displayClientMessage(Component.literal("You go back to the lights. The men turn with you. Their hands are out."),false);}
            else if(p.getZ()<b.getZ()-54){side=HERD;own.putString("EndingSide0492",side);p.displayClientMessage(Component.literal("You leave the voices behind. The stalks part without wind. There are bodies beneath the eyes."),false);}
            // Contact chooses too: a man's hand outside, or a shoulder of the herd by the gap.
            else if(ElkLotNight.stage(night)>=3&&men(p).stream().anyMatch(a->touches(p,a))){side=MEN;own.putString("EndingSide0492",side);p.displayClientMessage(Component.literal("A hand closes on your arm. Their lights are in your eyes."),false);}
            else if(herdTouches(p)){side=HERD;own.putString("EndingSide0492",side);p.displayClientMessage(Component.literal("Something warm presses against your side. The eyes are at your height now."),false);}
        }
        var bodies=new ArrayList<LivingEntity>();
        if(MEN.equals(side)){
            LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),ElkLot.PLACE,own);
            if(menTarget(p.server)!=p)return;
            var men=men(p);for(int i=0;i<men.size();i++){var a=men.get(i);a.gesture(4);a.setNoGravity(true);double angle=i*Math.PI*2/3;int leg=a.getPersistentData().getInt("ReturnToBar0492");double[][] route={{-12.5,ElkLot.SIDE_DOOR.getZ()+.5},{-14.5,ElkLot.SIDE_DOOR.getZ()+.5},{-16.5,-22.5}};
                var doorway=b.offset(ElkLot.SIDE_DOOR);var state=p.serverLevel().getBlockState(doorway);
                if(a.distanceToSqr(Vec3.atCenterOf(doorway))<6.25&&state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door&&!state.getValue(net.minecraft.world.level.block.DoorBlock.OPEN))door.setOpen(a,p.serverLevel(),state,doorway,true);
                // Outside, they come to the reader where the reader stands; only a reader in the bar is walked back to.
                if(leg==0&&ElkLotNight.inBar(a.position(),b)||!ElkLotNight.inBar(p.position(),b))leg=route.length;
                if(leg<route.length){var goal=new Vec3(b.getX()+route[leg][0],b.getY(),b.getZ()+route[leg][1]);if(step(a,goal,.095))a.getPersistentData().putInt("ReturnToBar0492",leg+1);}
                else step(a,p.position().add(Math.cos(angle)*.85,0,Math.sin(angle)*.85),.095);bodies.add(a);}
        }else{
            for(int i=0;i<3;i++){var elk=herd(p,b,i);if(elk==null)continue;elk.setInvisible(false);double angle=i*Math.PI*2/3;var centre=HERD.equals(side)?p.position():new Vec3(b.getX()+3.5,b.getY(),b.getZ()-52);
                var goal=centre.add(Math.cos(angle)*(HERD.equals(side)?1.15:5),0,Math.sin(angle)*(HERD.equals(side)?1.15:5));step(elk,goal,HERD.equals(side)?.12:.045);bodies.add(elk);}
        }
        if(side.isEmpty())return;
        int ticks=own.getInt("CaptureTicks0492");
        if(ticks==0){if(bodies.stream().noneMatch(a->touches(p,a)))return;own.putBoolean("CaptureWitness0492",true);own.putDouble("CaptureX0492",p.getX());own.putDouble("CaptureZ0492",p.getZ());}
        if(bodies.stream().noneMatch(a->a.distanceToSqr(p)<36)){release(p);own.putInt("CaptureTicks0492",0);return;}
        boolean resume=!HELD.containsKey(p.getUUID());ticks+=5;own.putInt("CaptureTicks0492",ticks);HELD.put(p.getUUID(),p);var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null&&!speed.hasModifier(HOLD))speed.addTransientModifier(new AttributeModifier(HOLD,-.9,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        p.stopRiding();p.stopFallFlying();
        if(ticks%15==0)LiteraryVignettes.sound(p,HERD.equals(side)?LiteraryRegistry.HOOVES.get():LiteraryRegistry.LOT_POOL.get(),p.blockPosition(),HERD.equals(side)?1.3F:.65F,HERD.equals(side)?.65F:.5F);
        if(ticks==5)p.displayClientMessage(Component.literal(MEN.equals(side)?"Another hand catches your other arm. The music keeps playing inside.":"A shoulder knocks yours. A hoof strikes beside your foot. The circle has no gap."),false);
        if(ticks==35||resume&&ticks>35)HousePackets.send(p,new HouseFadePayload(20,40,30));
        if(ticks<CAPTURE_TICKS)return;
        var porch=b.offset(ElkLot.PORCH);if(!p.serverLevel().hasChunkAt(porch)||!p.serverLevel().noCollision(p,p.getBoundingBox().move(Vec3.atBottomCenterOf(porch).subtract(p.position()))))return;
        p.teleportTo(p.serverLevel(),porch.getX()+.5,porch.getY(),porch.getZ()+.5,180,0);release(p);own.putInt("CaptureTicks0492",0);hide(p);io.github.knaitoe.theoldesthouse.journal.CoAuthor.consider(p,io.github.knaitoe.theoldesthouse.journal.CoAuthor.Trigger.BLACKOUT);
        // 0.4.187: a reader who struck her tonight wakes on the porch to nothing; she follows them home (ElkFollows).
        if(ElkFollows.struckTonight(own)){ElkFollows.wokeStruck(p,own);return;}
        ElkFollows.passed(p,own);LiteraryVignettes.ready(p,ElkLot.PLACE,own,side);
        p.displayClientMessage(Component.literal("On the noticeboard, the clipping has been turned over. It gives your name."),false);
    }
    private static boolean herdTouches(ServerPlayer p){var w=LiteraryVignettes.shared(LabyrinthData.get(p.server),ElkLot.PLACE);
        for(int i=0;i<3;i++){String key="EndingHerd0492_"+i+"_"+p.getUUID();if(w.hasUUID(key)&&p.serverLevel().getEntity(w.getUUID(key)) instanceof LiteraryElk elk&&!elk.isInvisible()&&touches(p,elk))return true;}return false;}
    private static void hide(ServerPlayer p){
        var patrons=p.serverLevel().getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(LiteraryVignettes.base(p,ElkLot.PLACE),ElkLot.PLACE),a->a.role()==LiteraryActor.STRANGER&&a.phase()>=5&&a.phase()<=8&&a.scene().equals(ElkLot.PLACE.id()));
        if(!patrons.isEmpty())p.connection.send(new net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket(patrons.stream().mapToInt(Entity::getId).toArray()));
var w=LiteraryVignettes.shared(LabyrinthData.get(p.server),ElkLot.PLACE);for(int i=0;i<3;i++){String key="EndingHerd0492_"+i+"_"+p.getUUID();if(w.hasUUID(key)&&p.serverLevel().getEntity(w.getUUID(key)) instanceof LiteraryElk elk)elk.setInvisible(true);}for(int side:new int[]{-1,1}){String key="Herd"+side+"_"+p.getUUID();if(w.hasUUID(key)&&p.serverLevel().getEntity(w.getUUID(key)) instanceof LiteraryActor eye)eye.setInvisible(true);}if(menTarget(p.server)==null)for(var a:men(p))if(a.gesture()==4){a.gesture(0);a.getPersistentData().remove("ReturnToBar0492");}}
    public static String account(String name,CompoundTag own){return switch(own.getString("Outcome")){
        case MEN->"LAST SEEN AT THE LAST STOP\n\nThe paper gave my name as "+name+". The men said they had taken me inside to calm me down. Marla said she had gone to wash the glasses. There was no photograph from inside the bar.\n\nI chose the hands I could recognise. In the doorway, one held each arm. The third took the light away. The jukebox never stopped."+ElkLotNight.tab(own);
        case HERD->"FOUND BEYOND THE BROKEN FENCE\n\nThe clipping named "+name+". It called the marks in the field an animal attack. Nobody could explain why there were so many tracks, or why none led back to the trees.\n\nI chose the things that had never pretended to be kind. The eyes came down to my height. Behind them, warm bodies pressed the stalks flat. The last human voice was still telling me to come back."+ElkLotNight.tab(own);
        default->"FIELD DEATH\n\nThe name in the clipping was "+name+". I remembered the lights in the parking lot and the eyes beyond it. The report gave the field an ending that I had not stayed to see."+ElkLotNight.tab(own);
    };}
}
