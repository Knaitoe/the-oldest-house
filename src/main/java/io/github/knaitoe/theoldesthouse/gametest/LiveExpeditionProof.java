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
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Opt-in dedicated-server proof with actual socket-connected clients. Inert in every ordinary game. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LiveExpeditionProof {
    public static boolean enabled(){return Boolean.getBoolean("the_oldest_house.liveProof");}
    public static Path folder(){return Path.of(System.getProperty("the_oldest_house.liveProofDir","build/live-proof"));}
    private static final BlockPos ORIGIN=new BlockPos(6000,80,6000);
    private static final Set<String> ACKS=new HashSet<>();
    private static final Set<String> PORTHOLE_CRAWLERS=new HashSet<>();
    private static int phase,changed;private static boolean restarted;private static UUID secondId;
    private static CompoundTag secondStory;private static Map<String,String> firstMap;
    private static final Map<UUID,Vec3> leakReturns=new HashMap<>();private static boolean leakRestarted,firstReturned;
    private static LakeWitchEntity stacy;private static LiteraryActor slasher;private static UUID stacyId,slasherId;private static int crackStarted=-1;
    private static Vec3 lastHunterPosition;private static LiteraryActor privateA,privateB;private static UUID privateAId,privateBId;
    private static final NativeTestChunks huntChunks=new NativeTestChunks();
    private LiveExpeditionProof() {}
    private static ServerPlayer player(MinecraftServer s,String role){return s.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().equals("OTHProof"+role)).findFirst().orElse(null);}
    private static String role(ServerPlayer p){return p.getGameProfile().getName().equals("OTHProofA")?"A":p.getGameProfile().getName().equals("OTHProofB")?"B":"";}
    private static void require(boolean okay,String message){if(!okay)throw new IllegalStateException("LIVE EXPEDITION: "+message);}
    @SubscribeEvent public static void commands(RegisterCommandsEvent e) {
        if(!enabled())return;
        e.getDispatcher().register(Commands.literal("othproof").then(Commands.argument("step",IntegerArgumentType.integer(1,24)).executes(c->{
            var p=c.getSource().getPlayerOrException();if(!role(p).isEmpty()&&IntegerArgumentType.getInteger(c,"step")==phase)ACKS.add(role(p));return 1;
        })));
    }
    private static Map<String,String> doors(LabyrinthData d,ServerPlayer p,LabyrinthPlace place) {
        var out=new TreeMap<String,String>();for(var spec:place.doors())if(LabyrinthData.DEALT.equals(spec.destination())) {
            var door=d.door(place.doorId(spec));out.put(door.id,d.deal(p.getUUID(),door).place());
        }return out;
    }
    private static void marker(ServerPlayer p,int step,BlockPos target) {
        var tag=new CompoundTag();tag.putInt("Step",step);tag.putLong("Target",target.asLong());
        if(step==6||step==8)tag.putString("Expected",BurnEmbers.excerpt(StaircaseStory.record(p),0,target).text());
        var stack=new ItemStack(Items.STICK);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));p.getInventory().setItem(8,stack);p.inventoryMenu.broadcastChanges();
    }
    private static void step(MinecraftServer s,int next,BlockPos target) {
        phase=next;changed=s.getTickCount();ACKS.clear();for(var p:s.getPlayerList().getPlayers())if(!role(p).isEmpty())marker(p,next,target);
        TheOldestHouse.LOGGER.info("LIVE EXPEDITION phase {}",next);
    }
    private static void at(ServerPlayer p,BlockPos pos,float yaw,double side) {
        var l=p.server.getLevel(HouseDimensions.INTERIOR);p.setGameMode(GameType.SURVIVAL);
        p.teleportTo(l,pos.getX()+.5+side,pos.getY(),pos.getZ()+.5,yaw,0);p.hasChangedDimension();
        // Fixture staging occurs in ServerTick.Post, after connection.tick.
        // Give its absolute teleport the same fresh movement baseline as a
        // real House crossing; the next input still uses native packets.
        p.connection.resetPosition();
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
        if(!enabled())return;var s=e.getServer();var a=player(s,"A");var b=player(s,"B");
        // Read on the tick the native return lands, before the reader's restored stride or the test
        // client's still-held walk key carries them on along the tread.
        if(phase==11&&a!=null&&!firstReturned&&!StaircaseLeaks.active(a)){
            require(a.position().distanceToSqr(leakReturns.get(a.getUUID()))<.01,"the first real client walks out to its exact saved tread");firstReturned=true;
        }
        if(phase==0) {
            if(a==null||b==null)return;
            var l=s.getLevel(HouseDimensions.INTERIOR);require(l!=null,"native House dimension loaded");
            var house=new HouseSavedData();house.markSpawned(ORIGIN);house.markInteriorInitialized();s.overworld().getDataStorage().set("the_oldest_house",house);
            var d=new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION,ORIGIN);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
            // Explicit proof fixture setup only; production retains its asynchronous, sliced carving.
            for(var hall:List.of(LabyrinthPlace.ALCOVE_HALL,LabyrinthPlace.OFFSET_HALL)) {
                var base=LabyrinthPlaces.base(ORIGIN,hall);LabyrinthHalls.build(l,base,hall);HouseFurnishings.decorate(l,base,hall);LabyrinthBuilder.registerDoors(d,hall,base);
            }
            var base=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.ALCOVE_HALL);
            for(var p:List.of(a,b)){d.visit(p.getUUID(),LabyrinthPlace.ALCOVE_HALL);LabyrinthDealer.arriveAt(d,p.getUUID(),LabyrinthPlace.ALCOVE_HALL,ORIGIN.asLong());at(p,base.offset(0,0,-4),180,p==a?-.65:.65);}
            firstMap=doors(d,a,LabyrinthPlace.ALCOVE_HALL);require(firstMap.equals(doors(d,b,LabyrinthPlace.ALCOVE_HALL)),"shared native starting routes agree");
            secondId=b.getUUID();step(s,1,base);return;
        }
        if(s.getTickCount()-changed>2400){
            String detail="LIVE EXPEDITION timed out at phase "+phase+": A="+location(a)+", B="+location(b);
            write("failed.txt",detail+"\n");s.halt(false);throw new IllegalStateException(detail);
        }
        var d=LabyrinthData.get(s);var base=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.OFFSET_HALL);
        if((phase==2||phase==4)&&s.getTickCount()%100==0)TheOldestHouse.LOGGER.info("LIVE EXPEDITION positions phase {}: A={}, B={}",phase,location(a),location(b));
        if(phase==1&&ACKS.size()==2) {
            var source=d.door("alcove_hall/far");require(source!=null,"real source door registered");
            for(var p:List.of(a,b)){d.deal(p.getUUID(),source,LabyrinthPlace.OFFSET_HALL.id(),false);at(p,source.lower.south(p==a?2:3),180,0);}
            step(s,2,source.lower);
        }else if(phase==2&&a!=null&&b!=null&&LabyrinthPlaces.placeAt(ORIGIN,a.blockPosition())==LabyrinthPlace.OFFSET_HALL&&LabyrinthPlaces.placeAt(ORIGIN,b.blockPosition())==LabyrinthPlace.OFFSET_HALL
                &&a.getZ()<base.getZ()-2&&b.getZ()<base.getZ()-2) {
            require(d.returnDepth(a.getUUID())==1&&d.returnDepth(b.getUUID())==1,"both actual door packets commit one independent return route");
            require(doors(d,a,LabyrinthPlace.OFFSET_HALL).equals(doors(d,b,LabyrinthPlace.OFFSET_HALL)),"native multiplayer arrivals share new onward halls");
            at(a,base.offset(0,0,-2),0,0);at(b,base,0,0);step(s,3,d.door(LabyrinthPlace.OFFSET_HALL.entryDoorId()).lower);
        }else if(phase==3&&ACKS.size()==2) {
            var door=d.door(LabyrinthPlace.OFFSET_HALL.entryDoorId());require(s.getLevel(door.dimension).getBlockState(door.lower).getValue(DoorBlock.OPEN),"shared entry stays open with a real peer in its approach");
            step(s,4,door.lower);
        }else if(phase==4&&a!=null&&b!=null&&d.returnDepth(a.getUUID())==0&&d.returnDepth(b.getUUID())==0
                &&LabyrinthPlaces.placeAt(ORIGIN,a.blockPosition())==LabyrinthPlace.ALCOVE_HALL&&LabyrinthPlaces.placeAt(ORIGIN,b.blockPosition())==LabyrinthPlace.ALCOVE_HALL) {
            require(LabyrinthPlaces.placeAt(ORIGIN,a.blockPosition())==LabyrinthPlace.ALCOVE_HALL&&LabyrinthPlaces.placeAt(ORIGIN,b.blockPosition())==LabyrinthPlace.ALCOVE_HALL,"both clients really walk back to the source hall without outside ejection");
            var l=s.getLevel(HouseDimensions.INTERIOR);var arch=new CompoundTag();arch.putBoolean("Ready",true);arch.putInt("CarveVersion",FinaleArchitecture.CARVE_VERSION);d.setState("finale_architecture_049",arch);
            for(var block:FinaleArchitecture.entrancePlan(ORIGIN))l.setBlock(block.pos(),block.block(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            var sheet=StaircaseLeaves.positions(ORIGIN).getFirst();var fire=StaircaseFire.braziers(ORIGIN).getFirst();
            for(var center:List.of(sheet,fire))for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=-1;y<=2;y++)
                l.setBlock(center.offset(x,y,z),(y==-1?Blocks.STONE:Blocks.AIR).defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            l.setBlock(sheet.below(),Blocks.STONE.defaultBlockState(),3);l.setBlock(sheet,NoteSurfaceBlock.state(HouseMarginalia.Thread.HOUSEKEEPING,Direction.NORTH),3);
            l.setBlock(fire.below(),Blocks.STONE.defaultBlockState(),3);l.setBlock(fire,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false),3);
            for(var p:List.of(a,b)) {
                FinaleProgress.phase(s,p.getUUID(),FinaleProgress.Phase.STAIRCASE);StaircaseStory.shelf(p);
                for(int i=0;i<8;i++)if(StaircaseStory.isCurrent(p,p.getInventory().getItem(i))){var book=p.getInventory().getItem(i);p.getInventory().setItem(i,ItemStack.EMPTY);p.setItemInHand(InteractionHand.OFF_HAND,book);break;}
                p.getInventory().selected=0;p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(NovelRegistry.LIGHTER.get()));at(p,sheet.south(2),180,p==a?-.65:.65);
            }
            step(s,5,sheet);
        }else if(phase==5&&a!=null&&b!=null&&StaircaseStory.found(StaircaseStory.record(a))==1&&StaircaseStory.found(StaircaseStory.record(b))==1) {
            var fire=StaircaseFire.braziers(ORIGIN).getFirst();secondStory=StaircaseStory.record(b);at(a,fire.east(2),90,0);at(b,fire.south(2),180,0);step(s,6,fire);
        }else if(phase==6&&ACKS.size()==2) {
            require(StaircaseStory.burned(StaircaseStory.record(a))==1&&StaircaseStory.burned(StaircaseStory.record(b))==0,"one real burn advances only its owner's canonical leaf");
            require(secondStory.equals(StaircaseStory.record(b)),"peer smoke and shared flame cannot change another reader's saved original");step(s,7,StaircaseFire.braziers(ORIGIN).getFirst());
        }else if(phase==7&&b==null) {
            phase=8;changed=s.getTickCount();ACKS.clear();write("restart-b.txt","Reconnect the same native client profile.\n");
        }else if(phase==8&&b!=null) {
            if(!restarted) {
                require(b.getUUID().equals(secondId)&&secondStory.equals(StaircaseStory.record(b)),"same real reconnect retains identity, immutable words and unburned personal cursor");
                require(StaircaseStory.isCurrent(b,b.getOffhandItem()),"native logout retained the living original with its components");
                marker(b,8,StaircaseFire.braziers(ORIGIN).getFirst());restarted=true;
            }
            if(ACKS.contains("B")) {
                require(StaircaseStory.burned(StaircaseStory.record(b))==1,"reconnected owner can burn their own leaf at the shared fire");
                var note=FinaleArchitecture.base(ORIGIN).offset(3,FinaleArchitecture.TOP,24);var l=s.getLevel(HouseDimensions.INTERIOR);
                for(int x=-3;x<=3;x++)for(int z=-2;z<=4;z++)for(int y=-1;y<=2;y++)l.setBlock(note.offset(x,y,z),(y==-1?Blocks.STONE:Blocks.AIR).defaultBlockState(),18);
                l.setBlock(note,NoteSurfaceBlock.state(HouseMarginalia.Thread.HOUSEKEEPING,Direction.NORTH),18);
                for(var p:List.of(a,b)){var record=d.stateEntry(StaircaseWriting.ID,p.getUUID().toString());var books=record.getCompound("Books");var indices=record.getCompound("Indices");var key=Long.toString(note.asLong());books.put(key,StaircaseNotes.specimen(9).save(p.registryAccess()));indices.putInt(key,9);record.put("Books",books);record.put("Indices",indices);d.setStateEntry(StaircaseWriting.ID,p.getUUID().toString(),record);at(p,note.south(2),180,p==a?-.65:.65);}
                step(s,9,note);
            }
        }else if(phase==9&&a!=null&&b!=null&&StaircaseLeaks.active(a)&&StaircaseLeaks.active(b)){
            require(!StaircaseLeaks.activeBase(a).equals(StaircaseLeaks.activeBase(b)),"the two socket readers enter independent native pocket rooms");
            for(var p:List.of(a,b)){var back=d.stateEntry(StaircaseLeaks.STATE,p.getUUID().toString()).getCompound("Active").getCompound("Return");leakReturns.put(p.getUUID(),new Vec3(back.getDouble("X"),back.getDouble("Y"),back.getDouble("Z")));}
            step(s,10,StaircaseLeaks.activeBase(a));marker(b,10,StaircaseLeaks.activeBase(b));
        }else if(phase==10&&a!=null&&b!=null&&StaircaseLeaks.progress(a).getBoolean("Waxed")&&StaircaseLeaks.progress(b).getBoolean("Waxed")&&ACKS.size()==2){
            step(s,11,StaircaseLeaks.activeBase(a));marker(b,11,StaircaseLeaks.activeBase(b));
        }else if(phase==11&&b==null&&a!=null&&firstReturned){
            phase=12;changed=s.getTickCount();ACKS.clear();write("restart-b-leak.txt","Reconnect the same profile after leaving during a personal note scene.\n");marker(a,12,a.blockPosition());
        }else if(phase==12&&a!=null&&b!=null&&!StaircaseLeaks.active(b)){
            require(b.getUUID().equals(secondId)&&b.position().distanceToSqr(leakReturns.get(b.getUUID()))<.01,"logout inside a leak restores the actual reconnected socket reader to their own tread");
            require(StaircaseStory.isCurrent(b,b.getOffhandItem())&&StaircaseStory.burned(StaircaseStory.record(b))==1,"scene recovery preserves the living book and personal burned cursor");
            if(!leakRestarted){step(s,13,b.blockPosition());leakRestarted=true;}
        }else if(phase==13&&ACKS.size()==2){
            var l=s.getLevel(HouseDimensions.OUTSIDE);var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.COSTUME_NIGHT);
            holdHunt(l,site);if(!huntChunks.ready())return;huntFloor(l,site);
            for(int x:new int[]{-1,1})for(int y=0;y<=3;y++)l.setBlock(site.offset(x,y,-35),Blocks.STONE.defaultBlockState(),3);
            var lower=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
            l.setBlock(site.offset(0,0,-35),lower,3);l.setBlock(site.offset(0,1,-35),lower.setValue(DoorBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),3);
            for(var p:List.of(a,b))outside(p,site.offset(0,0,-30),p==a?-.4:.4);
            stacy=DrownedTownRegistry.LAKE_WITCH.get().create(l);stacy.moveTo(site.getX()+.5,site.getY(),site.getZ()-41.5);stacy.literaryHunt(site,LabyrinthPlace.COSTUME_NIGHT);stacy.setNoAi(true);l.addFreshEntity(stacy);stacyId=stacy.getUUID();
            step(s,14,site.offset(0,0,-35));
        }else if(phase==14&&a!=null&&b!=null){
            var l=s.getLevel(HouseDimensions.OUTSIDE);var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.COSTUME_NIGHT);var door=site.offset(0,0,-35);
            if(stacy.doorBreakTicks()>0&&crackStarted<0)crackStarted=stacy.tickCount;
            if(crackStarted>=0&&!l.getBlockState(door).isAir())require(stacy.doorBreakTicks()<=stacy.tickCount-crackStarted+1,"two socket readers cannot multiply Stacy's cracking clock");
            if(l.getBlockState(door).isAir()){
                require(crackStarted>=0&&stacy.tickCount-crackStarted>=LakeWitchEntity.DOOR_BREAK_TICKS-1&&l.getBlockState(door.above()).isAir(),"one physical three-second door break opens both native halves");
                require(stacy.getUUID().equals(stacyId)&&l.getEntitiesOfClass(LakeWitchEntity.class,new net.minecraft.world.phys.AABB(Vec3.atLowerCornerOf(site.offset(-4,0,-46)),Vec3.atLowerCornerOf(site.offset(5,5,-27)))).size()==1,"both clients observe the original shared Stacy body");step(s,15,stacy.blockPosition());
            }
        }else if(phase==15&&ACKS.size()==2){
            var l=s.getLevel(HouseDimensions.OUTSIDE);var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.CAMP_BLOOD);
            holdHunt(l,site);if(!huntChunks.ready())return;stacy.discard();huntFloor(l,site);
            for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++){l.setBlock(site.offset(x,y,-44),Blocks.STONE.defaultBlockState(),3);l.setBlock(site.offset(x,y,-43),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3);}
            for(int x=-1;x<=1;x++)for(int z=-38;z<=-34;z++)l.setBlock(site.offset(x,1,z),HouseBlocks.FOREST_COVER.get().defaultBlockState(),3);
            for(var p:List.of(a,b)){outside(p,site.offset(p==a?-1:1,0,-40),0);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.CAMP_BLOOD);own.putBoolean("Here",true);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.CAMP_BLOOD,own);}
            slasher=LiteraryVignettes.actor(a,LabyrinthPlace.CAMP_BLOOD,"Killer",LiteraryActor.KILLER,new BlockPos(0,0,-49),false);require(slasher!=null,"the original native shared slasher exists");slasher.moveTo(site.getX()+.5,site.getY(),site.getZ()-48.5);slasher.setNoGravity(true);slasherId=slasher.getUUID();lastHunterPosition=slasher.position();
            require(LiteraryVignettes.shared(d,LabyrinthPlace.CAMP_BLOOD).getUUID("Killer").equals(slasherId),"both sockets use the registered shared body");step(s,16,site.offset(-1,0,-36));marker(b,16,site.offset(1,0,-36));
        }else if(phase==16&&a!=null&&b!=null&&ACKS.size()==2){
            var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.CAMP_BLOOD);for(var p:List.of(a,b))require(p.isShiftKeyDown()&&p.getPose()==net.minecraft.world.entity.Pose.CROUCHING&&CarcassHunt.concealed(p,site)&&p.serverLevel().noCollision(p,p.getBoundingBox())&&!CarcassHunt.sees(slasher,p),"both real clients physically crouch under native leaves and are concealed");
            var registered=LiteraryVignettes.shared(d,LabyrinthPlace.CAMP_BLOOD).getUUID("Killer");
            require(registered.equals(slasherId)&&a.serverLevel().getEntity(registered)==slasher,"native entity sections retain the registered shared slasher: original="+slasherId+", registered="+registered+", loaded="+a.serverLevel().getEntity(registered)+", body="+slasher.position());
            for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)a.serverLevel().setBlock(site.offset(x,y,-44),Blocks.AIR.defaultBlockState(),3);
            CarcassHunt.noise(a,a.blockPosition());require(CarcassHunt.tracks(slasher,a.getUUID()),"actual native noise must start the shared search before physical leaf pursuit: phase="+FinaleProgress.phase(s,a.getUUID())+", inside="+LiteraryVignettes.inside(a,LabyrinthPlace.CAMP_BLOOD)+", own="+LiteraryVignettes.personal(d,a.getUUID(),LabyrinthPlace.CAMP_BLOOD));step(s,17,site.offset(0,0,-43));
        }else if(phase==17&&a!=null&&b!=null){
            if(s.getTickCount()%100==0)TheOldestHouse.LOGGER.info("LIVE HUNT actor={} age={} state={} grief={} Aphase={} Bphase={} Ainside={} Binside={} AHere={} BHere={}",slasher.position(),slasher.tickCount,slasher.getPersistentData(),a.serverLevel().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING),FinaleProgress.phase(s,a.getUUID()),FinaleProgress.phase(s,b.getUUID()),LiteraryVignettes.inside(a,LabyrinthPlace.CAMP_BLOOD),LiteraryVignettes.inside(b,LabyrinthPlace.CAMP_BLOOD),LiteraryVignettes.personal(d,a.getUUID(),LabyrinthPlace.CAMP_BLOOD).getBoolean("Here"),LiteraryVignettes.personal(d,b.getUUID(),LabyrinthPlace.CAMP_BLOOD).getBoolean("Here"));
            require(slasher.getUUID().equals(slasherId)&&slasher.position().distanceTo(lastHunterPosition)<=.24,"one native slasher advances once per physical tick without teleporting");lastHunterPosition=slasher.position();
            var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.CAMP_BLOOD);if(slasher.getZ()>site.getZ()-42.5){
                boolean torn=false;for(int x=-1;x<=1;x++)torn|=a.serverLevel().getBlockState(site.offset(x,0,-43)).isAir()&&a.serverLevel().getBlockState(site.offset(x,1,-43)).isAir();
                require(torn,"the actual slasher physically tears a passage through the leaf barrier");
                require(WitnessAccount.count(d,a.getUUID())==0&&WitnessAccount.count(d,b.getUUID())==0,"shared pursuit grants no ending credit");
                step(s,18,slasher.blockPosition());
            }
        }else if(phase==18&&a!=null&&b!=null&&ACKS.size()==2){
            var l=s.getLevel(HouseDimensions.OUTSIDE);var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.ELK_CARCASSES);
            holdHunt(l,site);if(!huntChunks.ready())return;slasher.discard();huntFloor(l,site);
            for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)l.setBlock(site.offset(x,y,-43),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3);
            for(var p:List.of(a,b)){var camp=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.CAMP_BLOOD);camp.putBoolean("Here",false);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.CAMP_BLOOD,camp);
                outside(p,site.offset(p==a?-1:1,0,-31),0);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.ELK_CARCASSES);own.putBoolean("Here",true);own.putBoolean("Interrupted",true);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.ELK_CARCASSES,own);}
            privateA=ElkHunt.killer(a,site);privateB=ElkHunt.killer(b,site);require(privateA!=null&&privateB!=null&&privateA!=privateB,"two sockets receive two distinct private elk killers");
            privateA.moveTo(site.getX()-.5,site.getY(),site.getZ()-38.5);privateB.moveTo(site.getX()+1.3,site.getY(),site.getZ()-39.5);privateA.setNoGravity(true);privateB.setNoGravity(true);privateAId=privateA.getUUID();privateBId=privateB.getUUID();
            step(s,19,privateA.blockPosition());marker(b,19,privateB.blockPosition());
        }else if(phase==19&&a!=null&&b!=null&&ACKS.size()==2){
            require(privateA.owner().orElseThrow().equals(a.getUUID())&&privateB.owner().orElseThrow().equals(b.getUUID()),"native private actor ownership matches each socket");
            var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.ELK_CARCASSES);
            for(var p:List.of(a,b)){var cover=site.offset(p==a?0:1,0,-41);outside(p,cover,0);p.setYRot(180);p.serverLevel().setBlock(cover.above(),HouseBlocks.FOREST_COVER.get().defaultBlockState(),3);}
            privateA.moveTo(site.getX()+.5,site.getY(),site.getZ()-48.5);KillerNavigation.request(privateA,Vec3.atBottomCenterOf(site.offset(0,0,-35)),1.0);lastHunterPosition=privateA.position();step(s,22,site.offset(0,0,-43));
        }else if(phase==22&&a!=null&&b!=null&&ACKS.size()==2&&s.getTickCount()-changed>140){
            var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.ELK_CARCASSES);
            require(CarcassHunt.concealed(a,site)&&CarcassHunt.concealed(b,site),"both real socket players actively hide beneath the same cover");
            require(a.serverLevel().getBlockState(site.offset(0,0,-43)).is(Blocks.SPRUCE_LEAVES)&&a.serverLevel().getBlockState(site.offset(0,1,-43)).is(Blocks.SPRUCE_LEAVES),"a private invisible hunter cannot destroy its peer's occupied foliage");
            for(var p:List.of(a,b))outside(p,site.offset(p==a?-1:1,0,-31),0);
            KillerNavigation.request(privateA,Vec3.atBottomCenterOf(site.offset(0,0,-35)),1.0);lastHunterPosition=privateA.position();step(s,20,site.offset(0,0,-43));
        }else if(phase==20&&a!=null&&b!=null){
            var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.ELK_CARCASSES);
            require(privateA.position().distanceTo(lastHunterPosition)<.45,"the private hunter has one physical driver and no teleport recovery");lastHunterPosition=privateA.position();
            if(privateA.getZ()>site.getZ()-42.5){
                require(privateA.getUUID().equals(privateAId)&&privateB.getUUID().equals(privateBId)&&privateB.position().distanceToSqr(new Vec3(site.getX()+1.3,site.getY(),site.getZ()-39.5))<.01,"the other socket's original killer remains independently waiting");
                require(!privateA.getPersistentData().contains("CarcassSearch0455")&&!privateB.getPersistentData().contains("CarcassSearch0455"),"private actors never run the shared Camp Blood controller");
                require(a.serverLevel().getBlockState(site.offset(0,0,-43)).isAir()&&a.serverLevel().getBlockState(site.offset(0,1,-43)).isAir()&&a.serverLevel().getBlockState(site.offset(1,0,-43)).is(Blocks.SPRUCE_LEAVES),"private hunter clears only the native physical leaf passage");
                require(WitnessAccount.count(d,a.getUUID())==0&&WitnessAccount.count(d,b.getUUID())==0,"neither socket gains a personal ending from another hunter's world changes");
                step(s,21,privateA.blockPosition());
            }
        }else if(phase==21&&a!=null&&b!=null&&ACKS.size()==2){
            var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.ELK_CARCASSES);portholeFrame(a.serverLevel(),site,-1);portholeFrame(a.serverLevel(),site,1);
            outside(a,site.offset(-8,1,-3),0);outside(b,site.offset(7,1,-3),0);PORTHOLE_CRAWLERS.clear();
            step(s,23,site.offset(-9,1,-3));marker(b,23,site.offset(9,1,-3));
        }else if(phase==23&&a!=null&&b!=null){
            for(var p:List.of(a,b))if(p.getForcedPose()==net.minecraft.world.entity.Pose.SWIMMING&&p.getBbHeight()<.7)PORTHOLE_CRAWLERS.add(role(p));
            var site=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.ELK_CARCASSES);
            if(ACKS.size()==2&&a.getX()<site.getX()-9.8&&b.getX()>site.getX()+10.3){
                require(PORTHOLE_CRAWLERS.size()==2,"both socket players use their real native crawl collision bodies");
                require(a.serverLevel().getBlockState(site.offset(-9,1,-3)).isAir()&&a.serverLevel().getBlockState(site.offset(9,1,-3)).isAir(),"both native client mining actions break their actual portholes");
                require(a.serverLevel().getBlockState(site.offset(-9,2,-3)).is(LiteraryRegistry.YACHT_HULL.get())&&a.serverLevel().getBlockState(site.offset(9,2,-3)).is(LiteraryRegistry.YACHT_HULL.get()),"the surrounding yacht hull remains intact");
                require(WitnessAccount.count(d,a.getUUID())==0&&WitnessAccount.count(d,b.getUUID())==0,"physical porthole escape gives neither socket personal ending credit");
                step(s,24,site.offset(0,1,-3));write("passed.txt","Two actual NeoForge socket clients: all original shared doors, personal leaves/burns/note scenes and two same-profile reconnects; one Stacy body and one three-second wooden-door break; both readers walk crouched under native leaf cover; one shared Camp Blood hunter and two private elk killers use physical movement and leaf breaking; native client renderers exclude each peer's private killer; two actively hiding peers keep their cover from invisible hunters; native Stacy look tracking; both real clients mine portholes and crawl physically through while holding crouch, with matching native collision bodies and protected hull; no doubled clocks or transferred ending credit.\n");TheOldestHouse.LOGGER.info("LIVE EXPEDITION CHECK PASSED: shared and private hunts, native ownership rendering, porthole mining/crawling and two reconnects");
            }
        }else if(phase==24&&s.getTickCount()-changed>100){huntChunks.close();s.halt(false);}
    }

    private static void holdHunt(net.minecraft.server.level.ServerLevel l,BlockPos site){
        huntChunks.hold(l,new net.minecraft.world.phys.AABB(Vec3.atLowerCornerOf(site.offset(-13,-5,-55)),Vec3.atLowerCornerOf(site.offset(14,6,4))));
    }
    private static void portholeFrame(net.minecraft.server.level.ServerLevel l,BlockPos site,int side){
        int window=side*9;
        for(int x=side<0?-12:6;x<=(side<0?-6:12);x++)for(int z=-4;z<=-2;z++)for(int y=-4;y<=3;y++){
            boolean outer=side<0?x<-9:x>9;
            var block=x==window?LiteraryRegistry.YACHT_HULL.get():y==-4||(!outer&&y==0)?Blocks.PODZOL:outer&&y<=0?Blocks.WATER:Blocks.AIR;
            l.setBlock(site.offset(x,y,z),block.defaultBlockState(),3);
        }
        l.setBlock(site.offset(window,1,-3),LiteraryRegistry.YACHT_PORTHOLE.get().defaultBlockState(),3);
    }
    private static void huntFloor(net.minecraft.server.level.ServerLevel l,BlockPos site){
        for(int x=-2;x<=2;x++)for(int z=-51;z<=-27;z++)for(int y=-1;y<=3;y++)l.setBlock(site.offset(x,y,z),(y==-1?Blocks.PODZOL:x==-2||x==2||z==-51||z==-27?Blocks.STONE:Blocks.AIR).defaultBlockState(),3);
    }
    private static void outside(ServerPlayer p,BlockPos pos,double side){
        p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(true);p.setNoGravity(false);p.getFoodData().setFoodLevel(6);p.teleportTo(p.server.getLevel(HouseDimensions.OUTSIDE),pos.getX()+.5+side,pos.getY(),pos.getZ()+.5,0,0);p.hasChangedDimension();p.connection.resetPosition();
    }
    private static void write(String name,String text) {
        try{Files.createDirectories(folder());Files.writeString(folder().resolve(name),text);}catch(Exception e){throw new IllegalStateException(e);}
    }
    private static String location(ServerPlayer p){return p==null?"offline":p.level().dimension().location()+" "+p.position()+" returns="+LabyrinthData.get(p.server).returnDepth(p.getUUID())+" onGround="+p.onGround()+" below="+p.serverLevel().getBlockState(p.blockPosition().below());}
}
