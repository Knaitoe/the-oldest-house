package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Opt-in actual-client rendering/input/reconnect fixtures. No ordinary game reads these markers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class UncannySocketProof {
    public static boolean enabled(){return Boolean.getBoolean("the_oldest_house.uncannySocketSmoke");}
    public static Path folder(){return Path.of(System.getProperty("the_oldest_house.liveProofDir","build/uncanny-live-proof"));}
    private static final BlockPos ORIGIN=new BlockPos(6000,80,6000);
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static final Set<LabyrinthPlace> BUILT=new HashSet<>();
    private static final Set<UUID> ACKS=new HashSet<>();
    private static final Map<UUID,UUID> DOLLS=new HashMap<>(),FAKES=new HashMap<>();
    private static final Map<UUID,CompoundTag> NIGHTS=new HashMap<>();
    private static int phase,changed;private static boolean offline;
    private UncannySocketProof(){}
    private static void require(boolean okay,String message){if(!okay)throw new IllegalStateException("UNCANNY SOCKET: "+message);}
    private static ServerPlayer player(MinecraftServer s,String role){return s.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().equals("OTHProof"+role)).findFirst().orElse(null);}
    private static CompoundTag own(ServerPlayer p,LabyrinthPlace place){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),place);}
    private static void save(ServerPlayer p,LabyrinthPlace place,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),place,own);}
    private static BlockPos base(LabyrinthPlace place){return LabyrinthPlaces.base(ORIGIN,place);}
    private static void write(String name,String message){try{Files.createDirectories(folder());Files.writeString(folder().resolve(name),message);}catch(Exception ex){throw new IllegalStateException(ex);}}
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){if(!enabled())return;e.getDispatcher().register(Commands.literal("uncannyproof").then(Commands.argument("phase",IntegerArgumentType.integer(1,17)).executes(c->{var p=c.getSource().getPlayerOrException();if(p.getGameProfile().getName().startsWith("OTHProof")&&IntegerArgumentType.getInteger(c,"phase")==phase)ACKS.add(p.getUUID());return 1;})));}
    private static void marker(ServerPlayer p,int step){
        var tag=new CompoundTag();tag.putInt("UncannyStep",step);tag.putInt("ServerAge",p.server.getTickCount()-changed);
        tag.putBoolean("PaperRead",step==5?own(p,LabyrinthPlace.MISSING_SLEEP).getBoolean("Read_Source"):step==13&&WitnessAccount.has(LabyrinthData.get(p.server),p.getUUID(),WitnessAccount.Story.MISSING_SLEEP));
        tag.putBoolean("DoorBlocked",step==12&&own(p,LabyrinthPlace.MISSING_SLEEP).getBoolean("BlockedDoor")&&own(p,LabyrinthPlace.MISSING_SLEEP).getBoolean("Ready"));
        var stick=new ItemStack(Items.STICK);stick.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));p.getInventory().setItem(8,stick);p.inventoryMenu.broadcastChanges();
    }
    private static void step(MinecraftServer s,int next){phase=next;changed=s.getTickCount();ACKS.clear();for(var p:s.getPlayerList().getPlayers())marker(p,next);TheOldestHouse.LOGGER.info("UNCANNY SOCKET phase {}",next);}
    private static void build(MinecraftServer s,LabyrinthPlace place){
        var l=s.getLevel(NovelRooms.dimension(place));CHUNKS.hold(l,IndianLakeRooms.bounds(base(place),place));
        if(BUILT.add(place)){LiteraryRooms.build(l,base(place),place);LabyrinthBuilder.registerDoors(LabyrinthData.get(s),place,base(place));}
    }
    private static void at(ServerPlayer p,LabyrinthPlace place,double x,double y,double z){
        build(p.server,place);p.closeContainer();p.stopUsingItem();p.stopRiding();p.setGameMode(GameType.SURVIVAL);p.setHealth(20);p.setInvulnerable(false);p.setNoGravity(false);p.getFoodData().setFoodLevel(20);
        var b=base(place);p.teleportTo(p.server.getLevel(NovelRooms.dimension(place)),b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.hasChangedDimension();p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);LiteraryVignettes.onArrive(p,place);
    }
    private static void sourceFixture(ServerPlayer p,LabyrinthPlace place,int age){
        var own=own(p,place);UncannyStories.arrive(p,place,own);own.putBoolean("Read_Source",true);own.putInt("UncannyAge",age);save(p,place,own);
    }
    private static boolean both(ServerPlayer a,ServerPlayer b){return a!=null&&b!=null&&ACKS.contains(a.getUUID())&&ACKS.contains(b.getUUID());}
    /** A stalled native monitor must report what each genuine socket client actually sent. */
    private static String monitorState(ServerPlayer p){
        if(p==null)return "offline";
        var own=own(p,LabyrinthPlace.MISSING_SLEEP);
        var lens=p.getPersistentData().getCompound(UncannySenses.LENS);
        return "pos="+p.position()+" feeds=0x"+Integer.toHexString(own.getInt("FeedsSeen"))
            +" face="+own.getBoolean("OwnFaceSeen")+" monitorTicks="+own.getInt("MonitorTicks")
            +" marionette="+own.getBoolean("MarionetteSeen")+" lensChannel="+lens.getInt("Monitor")
            +" lensFresh="+UncannySenses.fresh(p)+" health="+p.getHealth()+" ring="+UncannySenses.ring(p)
            +" mirror="+UncannySenses.mirror(p)+" heldMirror="+UncannySenses.heldMirror(p)+" ack="+ACKS.contains(p.getUUID());
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(!enabled())return;var s=e.getServer();var a=player(s,"A");var b=player(s,"B");
        try{
            if(phase==0){
                if(a==null||b==null)return;
                // This fixture builds six vignettes, not the manor at its otherwise empty origin.
                // Match the full expedition host: do not start an unrelated eighty-chunk terrain copy.
                var house=new HouseSavedData();house.markSpawned(ORIGIN);house.markInteriorInitialized();s.overworld().getDataStorage().set("the_oldest_house",house);
                var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,ORIGIN);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
                for(var p:List.of(a,b)){at(p,LabyrinthPlace.PAINTED_INTERIOR,.5,0,-20.5);sourceFixture(p,LabyrinthPlace.PAINTED_INTERIOR,120);}
                step(s,1);return;
            }
            if(s.getTickCount()-changed>2400)throw new IllegalStateException("phase timed out "+phase+"; A=["+monitorState(a)+"] B=["+monitorState(b)+"]");
            if(phase==10){
                if(b==null){offline=true;return;}
                if(offline){
                    var saved=NIGHTS.get(b.getUUID());require(saved!=null,"same reconnect profile");var own=own(b,LabyrinthPlace.MISSING_SLEEP);
                    require(own.getInt("Sleeps")==saved.getInt("Sleeps")&&own.getInt("NightClock")==saved.getInt("NightClock")&&DOLLS.get(b.getUUID()).equals(own.getUUID("CastDoll")),"same saved night and original doll after native reconnect");
                    for(var p:List.of(a,b))at(p,LabyrinthPlace.MISSING_SLEEP,-5.5,5,-19.5);step(s,11);
                }return;
            }
            if(phase==17){if(s.getTickCount()-changed>100){CHUNKS.close();s.halt(false);}return;}
            if(a==null||b==null)return;
            if(s.getTickCount()%20==0){marker(a,phase);marker(b,phase);}
            if(!CHUNKS.ready()||!both(a,b))return;
            if(phase==1){for(var p:List.of(a,b)){at(p,LabyrinthPlace.SECOND_WAITRESS,.5,0,-17.5);sourceFixture(p,LabyrinthPlace.SECOND_WAITRESS,270);}step(s,2);}
            else if(phase==2){for(var p:List.of(a,b)){var own=own(p,LabyrinthPlace.SECOND_WAITRESS);FAKES.put(p.getUUID(),own.getUUID("CastFake"));own.putInt("UncannyAge",620);save(p,LabyrinthPlace.SECOND_WAITRESS,own);}step(s,3);}
            else if(phase==3){for(var p:List.of(a,b)){require(FAKES.get(p.getUUID()).equals(own(p,LabyrinthPlace.SECOND_WAITRESS).getUUID("CastFake")),"the translucent and solid waitress are the same original");at(p,LabyrinthPlace.TUNNEL_PERIPHERY,.5,0,-10.5);sourceFixture(p,LabyrinthPlace.TUNNEL_PERIPHERY,170);}step(s,4);}
            else if(phase==4){for(var p:List.of(a,b))at(p,LabyrinthPlace.MISSING_SLEEP,.5,0,.5);step(s,5);}
            else if(phase==5){
                for(var p:List.of(a,b)){require(own(p,LabyrinthPlace.MISSING_SLEEP).getBoolean("Read_Source"),"native final source page");require(p.onGround()&&!p.isNoGravity()&&Math.abs(p.getY()-base(LabyrinthPlace.MISSING_SLEEP).getY()-5)<.3,"native doorway and stair walking reaches the notebook floor");DOLLS.put(p.getUUID(),own(p,LabyrinthPlace.MISSING_SLEEP).getUUID("CastDoll"));at(p,LabyrinthPlace.MISSING_SLEEP,-5.5,5,-23.5);}step(s,6);
            }
            else if(phase==6){for(var p:List.of(a,b)){require(own(p,LabyrinthPlace.MISSING_SLEEP).getInt("Sleeps")==1,"first native bed use");at(p,LabyrinthPlace.MISSING_SLEEP,-5.5,5,-23.5);}step(s,7);}
            else if(phase==7){
                for(var p:List.of(a,b)){var own=own(p,LabyrinthPlace.MISSING_SLEEP);require(own.getInt("Sleeps")==2,"second native bed use");
                    // Explicit timeout fixture lets slow software-rendered clients inspect four real feeds.
                    own.putInt("SecondSleepAge",own.getInt("UncannyAge")+3600);save(p,LabyrinthPlace.MISSING_SLEEP,own);at(p,LabyrinthPlace.MISSING_SLEEP,1.5,5,-20.5);
                }step(s,8);
            }
            else if(phase==8){for(var p:List.of(a,b)){require(own(p,LabyrinthPlace.MISSING_SLEEP).getBoolean("MarionetteSeen"),"the reader personally saw the direct form");at(p,LabyrinthPlace.MISSING_SLEEP,-5.5,5,-19.5);}step(s,9);}
            else if(phase==9){
                for(var p:List.of(a,b)){var own=own(p,LabyrinthPlace.MISSING_SLEEP);require(own.getInt("FeedsSeen")==15&&own.getBoolean("OwnFaceSeen")&&own.getInt("MonitorTicks")>=40,"all four captured feeds and actual own face");NIGHTS.put(p.getUUID(),own.copy());}
                s.saveEverything(true,true,true);write("restart-b.txt","Native same-profile reconnect now. Original doll and night are retained.\n");step(s,10);
            }
            else if(phase==11){for(var p:List.of(a,b)){require(DOLLS.get(p.getUUID()).equals(own(p,LabyrinthPlace.MISSING_SLEEP).getUUID("CastDoll")),"reconnect never fabricates a replacement doll");at(p,LabyrinthPlace.MISSING_SLEEP,2.5,5,-15.5);}step(s,12);}
            else if(phase==12){for(var p:List.of(a,b)){require(own(p,LabyrinthPlace.MISSING_SLEEP).getBoolean("BlockedDoor")&&own(p,LabyrinthPlace.MISSING_SLEEP).getBoolean("Ready"),"each reader closes the actual door after their own evidence");at(p,LabyrinthPlace.MISSING_SLEEP,3.5,0,-2.5);}step(s,13);}
            else if(phase==13){for(var p:List.of(a,b)){require(WitnessAccount.has(LabyrinthData.get(s),p.getUUID(),WitnessAccount.Story.MISSING_SLEEP),"native final account earns the personal memory");require(p.getInventory().countItem(UncannyRegistry.SMOKY_MIRROR.get())==1,"exactly one finite mirror");at(p,LabyrinthPlace.LAYERED_MAN,3.5,-5,-15.5);sourceFixture(p,LabyrinthPlace.LAYERED_MAN,50);}step(s,14);}
            else if(phase==14){for(var p:List.of(a,b)){at(p,LabyrinthPlace.MOTION_LIGHT,.5,0,-10.5);sourceFixture(p,LabyrinthPlace.MOTION_LIGHT,5);}step(s,15);}
            else if(phase==15){for(var p:List.of(a,b)){require(own(p,LabyrinthPlace.MOTION_LIGHT).getInt("DarkSeen")>=2,"actual native floodlight cycles");at(p,LabyrinthPlace.MISSING_SLEEP,1.5,5,-20.5);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(UncannyRegistry.BONE_RING.get()));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(UncannyRegistry.SMOKY_MIRROR.get()));p.invulnerableTime=0;
                    // Keep natural regeneration from healing the real two-point payment before the client captures it.
                    p.getFoodData().setFoodLevel(17);
                }step(s,16);}
            else if(phase==16){for(var p:List.of(a,b)){require(p.getHealth()<=18&&UncannySenses.mirror(p)&&UncannySenses.ring(p),"native use spends actual health and sends personal true view");require(WitnessAccount.count(LabyrinthData.get(s),p.getUUID())==1,"visual fixture staging grants no other Witness");}
                write("passed.txt","Two actual socket clients rendered all six production-built spaces and original cast. Board/diner/giant/basement/foyer source and positions were explicitly staged visual fixtures; those stages earned no Witness. Each bedroom source, two bed actions, four native CCTV channels, naked-eye sight, personal door close and final account used actual client packets. Hallway head pixels were required on both clients, including same-profile reconnect, retaining the exact saved night and original doll UUID. Mirror use paid actual health and ring sight retained private ownership. Earlier full expedition/reconnects remain separately required.\n");step(s,17);
            }
        }catch(Throwable ex){write("failed.txt",ex.toString()+"\n");s.halt(false);throw new IllegalStateException(ex);}
    }
}
