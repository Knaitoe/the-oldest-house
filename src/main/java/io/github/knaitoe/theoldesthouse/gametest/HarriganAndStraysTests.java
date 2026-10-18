package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HarriganAndStraysTests {
    private static final List<Item> MEATS = List.of(Items.BEEF,Items.COOKED_BEEF,Items.PORKCHOP,Items.COOKED_PORKCHOP,
            Items.CHICKEN,Items.COOKED_CHICKEN,Items.MUTTON,Items.COOKED_MUTTON,Items.RABBIT,Items.COOKED_RABBIT,
            Items.ROTTEN_FLESH,Items.COD,Items.COOKED_COD,Items.SALMON,Items.COOKED_SALMON,Items.TROPICAL_FISH,Items.PUFFERFISH);
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerLevel level;
        final HouseSavedData oldHouse;
        final LabyrinthData oldData;
        final BlockPos origin, base;
        final List<ServerPlayer> players = new ArrayList<>();
        final Set<ChunkPos> chunks = new HashSet<>();
        final List<Entity> animals = new ArrayList<>();
        Fixture(GameTestHelper h, int coordinate) {
            helper=h;level=HouseTestLevel.get(h.getLevel().getServer());origin=new BlockPos(coordinate,80,coordinate);
            var server=level.getServer();oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            base=LabyrinthPlaces.base(origin,LabyrinthPlace.HARRIGAN);
            for(int x=(base.getX()-9)>>4;x<=(base.getX()+9)>>4;x++)for(int z=(base.getZ()-26)>>4;z<=(base.getZ()+3)>>4;z++) {
                var chunk=new ChunkPos(x,z);chunks.add(chunk);level.getChunkSource().addRegionTicket(TicketType.PORTAL,chunk,3,base);level.getChunk(x,z);
            }
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)level.setBlock(base.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
        }
        ServerPlayer player(BlockPos at) {
            var player=helper.makeMockServerPlayerInLevel();player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            player.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);player.hasChangedDimension();players.add(player);return player;
        }
        TamableAnimal stray(boolean cat, ServerPlayer player) {
            var animal=LabyrinthEncounters.spawnStray(level,player.position().add(1,0,0),cat);
            helper.assertTrue(animal!=null,"the actual encounter factory spawns a native stray");animal.setNoAi(true);animals.add(animal);return animal;
        }
        List<LiteraryActor> bodies() {
            return level.getEntitiesOfClass(LiteraryActor.class,bounds(),e->e.role()==LiteraryActor.HARRIGAN);
        }
        AABB bounds() {
            return new AABB(base.getX()-8,base.getY()-2,base.getZ()-25,base.getX()+9,base.getY()+7,base.getZ()+3);
        }
        public void close() {
            for(var animal:animals)animal.discard();
            for(var entity:level.getEntitiesOfClass(Entity.class,bounds(),e->e instanceof LiteraryActor||e instanceof ItemFrame||e instanceof SeatEntity))entity.discard();
            for(var player:players)if(level.getServer().getPlayerList().getPlayers().contains(player))level.getServer().getPlayerList().remove(player);else player.discard();
            for(var chunk:chunks)level.getChunkSource().removeRegionTicket(TicketType.PORTAL,chunk,3,base);
            level.getServer().overworld().getDataStorage().set("the_oldest_house",oldHouse);
            level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);
        }
    }
    private static Fixture pose, meats, guarded, hands;
    @AfterBatch(batch="harrigan_pose") public static void cleanPose(ServerLevel level){if(pose!=null){pose.close();pose=null;}}
    @AfterBatch(batch="stray_meats") public static void cleanMeats(ServerLevel level){if(meats!=null){meats.close();meats=null;}}
    @AfterBatch(batch="stray_guards") public static void cleanGuards(ServerLevel level){if(guarded!=null){guarded.close();guarded=null;}}
    @AfterBatch(batch="stray_hands") public static void cleanHands(ServerLevel level){if(hands!=null){hands.close();hands=null;}}

    private static void seated(GameTestHelper h, LiteraryActor body, BlockPos chair) {
        h.assertTrue(body.isNoGravity()&&body.role()==LiteraryActor.HARRIGAN&&body.phase()!=HarriganVignette.CASKET_PHASE,
                "a real seated scene actor whom gravity cannot stand back up");
        h.assertTrue(body.getY()<chair.getY()&&body.getY()+13.0/16.0>=chair.getY()+.5
                &&body.getY()+13.0/16.0<chair.getY()+.65&&body.getZ()<chair.getZ()+.5,
                "the rendered hip rests on the lower stair seat, ahead of its back");
        h.assertTrue(body.getYRot()==180,"Harrigan faces away from the entrance");
    }
    /** 0.4.105: one scene actor through all three visits; nothing about him can be taken apart. */
    @GameTest(template="empty",batch="harrigan_pose",timeoutTicks=450)
    public static void harriganIsOneRealActorAliveThenDeadInHisChairThenInHisCasket(GameTestHelper h) {
        pose=new Fixture(h,25400);Fixture f=pose;
        var data=LabyrinthData.get(f.level.getServer());CompoundTag state=data.state(HarriganVignette.ID);
        state.putInt("Visit",1);data.setState(HarriganVignette.ID,state);
        HarriganVignette.build(f.level.getServer(),f.level,f.base);
        var owner=f.player(f.base.offset(HarriganVignette.READER_CHAIR).above());
        UUID witness=UUID.randomUUID();WitnessAccount.resolve(data,witness,WitnessAccount.Story.HARRIGAN,"kept_phone");
        boolean[] started={false};
        h.onEachTick(()->{
            if(started[0])return;
            if(f.bodies().isEmpty())HarriganVignette.onArrive(owner,LabyrinthPlace.HARRIGAN);
            if(f.bodies().isEmpty())return;
            started[0]=true;
            h.assertTrue(f.bodies().size()==1,"one native study actor is loaded; count="+f.bodies().size());var body=f.bodies().getFirst();seated(h,body,f.base.offset(HarriganVignette.CHAIR));
            h.assertTrue(body.phase()==HarriganVignette.ALIVE_PHASE,"on the first visit he is alive");
            UUID original=body.getUUID();
            h.assertTrue(!body.hurt(owner.damageSources().playerAttack(owner),100)&&body.isAlive(),"a blow neither hurts nor removes him");
            body.interact(owner,InteractionHand.MAIN_HAND);
            h.assertTrue(java.util.Arrays.stream(EquipmentSlot.values()).allMatch(slot->body.getItemBySlot(slot).isEmpty())&&owner.getInventory().isEmpty()&&body.isAlive(),"there is nothing on him to strip, and interacting takes nothing");
            CompoundTag saved=new CompoundTag();body.saveWithoutId(saved);
            var loaded=LiteraryRegistry.ACTOR.get().create(f.level);loaded.load(saved);
            h.assertTrue(loaded.getUUID().equals(original)&&loaded.role()==LiteraryActor.HARRIGAN&&loaded.phase()==HarriganVignette.ALIVE_PHASE,"native entity NBT keeps his identity and state");loaded.discard();
            owner.moveTo(Vec3.atBottomCenterOf(f.base.offset(HarriganVignette.CHAIR).north()));
            h.assertTrue(!HouseSitting.sit(owner,f.base.offset(HarriganVignette.CHAIR)),"the player cannot occupy Harrigan's chair through his body");
            owner.moveTo(Vec3.atBottomCenterOf(f.base.offset(HarriganVignette.READER_CHAIR).above()));
            h.assertTrue(HouseSitting.sit(owner,f.base.offset(HarriganVignette.READER_CHAIR)),"the separate reading chair still seats its reader");owner.stopRiding();
            CompoundTag next=data.state(HarriganVignette.ID);next.putBoolean("BeatDone",true);data.setState(HarriganVignette.ID,next);HarriganVignette.onArrive(owner,LabyrinthPlace.HARRIGAN);
            h.runAfterDelay(5,()->{
                h.assertTrue(data.state(HarriganVignette.ID).getInt("Visit")==2&&f.bodies().size()==1&&f.bodies().getFirst().getUUID().equals(original),"the second visit has the same body");
                var dead=f.bodies().getFirst();seated(h,dead,f.base.offset(HarriganVignette.CHAIR));
                h.assertTrue(dead.phase()==HarriganVignette.DEAD_PHASE&&!WitnessAccount.has(data,owner.getUUID(),WitnessAccount.Story.HARRIGAN),"he is dead in his chair, and that awards nothing");
                h.assertTrue(!f.level.getBlockState(f.base.offset(HarriganVignette.FUNERAL_DOOR)).isAir(),"on the second visit there is no way through to the funeral");
                owner.moveTo(Vec3.atBottomCenterOf(f.base.offset(HarriganVignette.SIDE_TABLE).west()));
                var phone=f.level.getEntitiesOfClass(ItemFrame.class,f.bounds(),frame->frame.getItem().is(LabyrinthRegistry.HARRIGANS_PHONE.get())).getFirst();
                NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,phone));
                h.assertTrue(owner.getInventory().countItem(LabyrinthRegistry.HARRIGANS_PHONE.get())==1&&data.state(HarriganVignette.ID).getBoolean("BeatDone"),"taking his phone is the second visit's beat");
                HarriganVignette.onDepart(owner);
                h.assertTrue(!WitnessAccount.has(data,owner.getUUID(),WitnessAccount.Story.HARRIGAN),"carrying it out of the study only takes it toward the funeral");
                owner.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-3)));HarriganVignette.onArrive(owner,LabyrinthPlace.HARRIGAN);
                h.runAfterDelay(5,()->{
                    var laid=f.bodies();
                    h.assertTrue(data.state(HarriganVignette.ID).getInt("Visit")==3&&laid.size()==1&&laid.getFirst().getUUID().equals(original)&&laid.getFirst().phase()==HarriganVignette.CASKET_PHASE
                            &&Math.abs(laid.getFirst().getZ()-(f.base.getZ()+HarriganVignette.CASKET.getZ()+.5))<.01,"the third visit lays the same body in his casket");
                    h.assertTrue(f.level.getBlockState(f.base.offset(HarriganVignette.FUNERAL_DOOR).above()).isAir()&&f.level.getBlockState(f.base.offset(-4,2,-13)).isAir()&&!(f.level.getBlockState(f.base.offset(HarriganVignette.CHAIR)).getBlock() instanceof net.minecraft.world.level.block.StairBlock)                            &&!HarriganVignette.occupiesChair(f.level,f.base.offset(HarriganVignette.CHAIR))&&!f.level.getBlockState(f.base.offset(HarriganVignette.LECTERN)).is(net.minecraft.world.level.block.Blocks.LECTERN),"the door opens on a funeral home: his study, its chair and its reading lectern are gone");
                    var lectern=(net.minecraft.world.level.block.entity.LecternBlockEntity)f.level.getBlockEntity(f.base.offset(HarriganVignette.GUEST_BOOK));
                    h.assertTrue(lectern.getBook().get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).title().raw().equals("In memory"),"the foyer's stand holds the guest book");
                    var sign=(net.minecraft.world.level.block.entity.SignBlockEntity)f.level.getBlockEntity(f.base.offset(HarriganVignette.HOME_SIGN));
                    h.assertTrue(sign.getFrontText().getMessage(0,false).getString().equals("PEABODY & SONS"),"the funeral home has its own sign");
                    h.assertTrue(f.level.getBlockState(f.base.offset(HarriganVignette.CASKET)).is(LabyrinthRegistry.HARRIGAN_FINISH.get())
                            &&f.level.getBlockState(f.base.offset(4,0,-8)).getBlock() instanceof net.minecraft.world.level.block.StairBlock
                            &&f.level.getBlockState(f.base.offset(0,0,-10)).is(net.minecraft.world.level.block.Blocks.RED_CARPET),"his casket stands at the front of a chapel of pews either side of an aisle");
                    h.assertTrue(f.level.getEntitiesOfClass(LiteraryActor.class,f.bounds(),a->a.role()==LiteraryActor.HARRIGAN_MOURNER).size()==3,"three mourners stand at the funeral");
                    h.assertTrue(WitnessAccount.has(data,witness,WitnessAccount.Story.HARRIGAN),"another reader's evidence is untouched");h.succeed();
                });
            });
        });
    }
    @GameTest(template="empty",batch="stray_meats")
    public static void everyVanillaMeatAdoptsNativeCatsAndWolvesThroughTheRegisteredInteraction(GameTestHelper h) {
        meats=new Fixture(h,25700);Fixture f=meats;var owner=f.player(f.base);
        h.assertTrue(!owner.getAbilities().instabuild,"the fixture really consumes survival inventory");
        for(boolean cat:new boolean[]{true,false})for(Item meat:MEATS) {
            var pet=f.stray(cat,owner);UUID id=pet.getUUID();owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(meat,2));
            var event=new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,pet);NeoForge.EVENT_BUS.post(event);
            h.assertTrue(event.isCanceled()&&event.getCancellationResult()==InteractionResult.SUCCESS&&owner.getMainHandItem().getCount()==1,"feeding "+meat+" consumes exactly one item through the registered route");
            h.assertTrue(pet.isTame()&&owner.getUUID().equals(pet.getOwnerUUID())&&pet.getUUID().equals(id)
                    &&CompanionOrders.managed(pet)&&CompanionOrders.order(pet)==CompanionOrders.Order.FOLLOW&&!pet.isOrderedToSit(),"the original cat or wolf becomes the explorer's following companion");
            h.assertTrue(CompanionOrders.canCommand(owner,pet)&&CompanionOrders.issue(pet,owner,CompanionOrders.Order.STAY),"native ownership immediately permits the existing wheel commands");
            CompoundTag saved=new CompoundTag();pet.saveWithoutId(saved);var loaded=(TamableAnimal)pet.getType().create(f.level);loaded.load(saved);
            h.assertTrue(loaded.getUUID().equals(id)&&loaded.isTame()&&owner.getUUID().equals(loaded.getOwnerUUID())
                    &&loaded.isOrderedToSit()&&CompanionOrders.order(loaded)==CompanionOrders.Order.STAY,"native save/reload retains identity, taming and the selected order");pet.discard();
        }
        h.succeed();
    }
    @GameTest(template="empty",batch="stray_guards")
    public static void meatCannotStealPetsTameProjectionsOrReplaceOrdinaryVanillaInteractions(GameTestHelper h) {
        guarded=new Fixture(h,26000);Fixture f=guarded;var owner=f.player(f.base);var pet=f.stray(false,owner);
        owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BEEF,2));
        pet.getPersistentData().remove(LabyrinthEncounters.STRAY);h.assertTrue(!LabyrinthEncounters.feedStray(owner,pet,InteractionHand.MAIN_HAND),"ordinary vanilla animals keep their native taming rules");pet.getPersistentData().putBoolean(LabyrinthEncounters.STRAY,true);
        pet.addTag(MotherOfStrays.PET);h.assertTrue(!LabyrinthEncounters.feedStray(owner,pet,InteractionHand.MAIN_HAND),"a captive display pet cannot be adopted by feeding");pet.removeTag(MotherOfStrays.PET);
        pet.addTag(HouseExteriorEntityMirror.PROJECTION_TAG);h.assertTrue(!LabyrinthEncounters.feedStray(owner,pet,InteractionHand.MAIN_HAND),"a mirror projection cannot become a real pet");pet.removeTag(HouseExteriorEntityMirror.PROJECTION_TAG);
        UUID other=UUID.randomUUID();pet.setTame(true,true);pet.setOwnerUUID(other);h.assertTrue(!LabyrinthEncounters.feedStray(owner,pet,InteractionHand.MAIN_HAND)&&other.equals(pet.getOwnerUUID()),"someone else's animal retains its native owner");pet.setTame(false,true);
        h.assertTrue(!LabyrinthEncounters.feedStray(owner,pet,InteractionHand.MAIN_HAND),"an untamed flag cannot override another stored owner");pet.setOwnerUUID(null);
        pet.moveTo(owner.position().add(6,0,0));h.assertTrue(!LabyrinthEncounters.feedStray(owner,pet,InteractionHand.MAIN_HAND),"feeding requires normal nearby reach");pet.moveTo(owner.position().add(1,0,0));
        owner.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);h.assertTrue(!LabyrinthEncounters.feedStray(owner,pet,InteractionHand.MAIN_HAND),"spectators cannot adopt");owner.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        h.assertTrue(owner.getMainHandItem().getCount()==2&&!pet.isTame(),"refused feedings consume nothing");
        for(Item food:List.of(Items.APPLE,Items.BREAD,Items.BONE)) {
            owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(food,2));var event=new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,pet);NeoForge.EVENT_BUS.post(event);
            h.assertTrue(!event.isCanceled()&&!pet.isTame()&&owner.getMainHandItem().getCount()==2,"non-meat and native bone interactions pass through untouched");
        }
        var outside=h.makeMockServerPlayerInLevel();f.players.add(outside);outside.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);outside.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BEEF,2));
        var vanilla=LabyrinthEncounters.spawnStray(h.getLevel(),outside.position().add(1,0,0),false);f.animals.add(vanilla);
        h.assertTrue(!LabyrinthEncounters.feedStray(outside,vanilla,InteractionHand.MAIN_HAND)&&outside.getMainHandItem().getCount()==2,"even a stray marker does not change animals outside the House");h.succeed();
    }
    @GameTest(template="empty",batch="stray_hands")
    public static void offHandMeatWorksAndCreativeAdoptionDoesNotConsumeTheItem(GameTestHelper h) {
        hands=new Fixture(h,26300);Fixture f=hands;var owner=f.player(f.base);var cat=f.stray(true,owner);
        owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));owner.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.COOKED_MUTTON,2));
        var event=new PlayerInteractEvent.EntityInteract(owner,InteractionHand.OFF_HAND,cat);NeoForge.EVENT_BUS.post(event);
        h.assertTrue(event.isCanceled()&&cat.isTame()&&owner.getOffhandItem().getCount()==1&&owner.getMainHandItem().is(Items.STICK),"the feeding hand is consumed without touching the other hand");
        owner.gameMode.changeGameModeForPlayer(GameType.CREATIVE);var wolf=f.stray(false,owner);owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.CHICKEN,2));
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,wolf));
        h.assertTrue(wolf.isTame()&&owner.getMainHandItem().getCount()==2,"creative feeds still adopt while retaining the held meat");h.succeed();
    }
}
