package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/** The house at the bottom of the lake: its own namespace, so the complete native suite counts it and no focused job changes. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_lake_house") @PrefixGameTestTemplate(false)
public final class LakeHouseTests {
    private static final LabyrinthPlace PLACE=LabyrinthPlace.LAKE_HOUSE;
    private LakeHouseTests(){}

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel in,out;final BlockPos origin;final HouseSavedData oldHouse;final LabyrinthData oldData;final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h){this.h=h;in=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);out=HouseTestLevel.get(in.getServer(),HouseDimensions.OUTSIDE);
            oldHouse=HouseSavedData.get(in.getServer());oldData=LabyrinthData.get(in.getServer());origin=new BlockPos(59000,80,59000);
            var house=new HouseSavedData();house.markSpawned(origin);var store=in.getServer().overworld().getDataStorage();store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",new LabyrinthData());
            data().setBuilt(LabyrinthBuilder.VERSION,origin);LiteraryRooms.build(out,base(),PLACE);LabyrinthBuilder.registerDoors(data(),PLACE,base());}
        LabyrinthData data(){return LabyrinthData.get(in.getServer());}
        BlockPos base(){return LabyrinthPlaces.base(origin,PLACE);}
        ServerPlayer player(String name){var p=NativeTestPlayers.survival(h,name);players.add(p);var b=base();p.teleportTo(out,b.getX()+.5,b.getY(),b.getZ()-3,180,0);p.hasChangedDimension();p.setNoGravity(true);LiteraryVignettes.onArrive(p,PLACE);return p;}
        void read(ServerPlayer p,BlockPos rel){var at=base().offset(rel);p.moveTo(at.getX()+(rel.getX()<0?1.5:-.5),at.getY(),at.getZ()+.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,at.getCenter());
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.EAST,at,false)));
            h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"the lake house's paper opens at "+rel);var menu=(LiteraryVignettes.Pages)p.containerMenu;
            h.assertTrue(menu.clickMenuButton(p,100+menu.original().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1),"its last page is read");p.closeContainer();}
        boolean solid(BlockPos rel){return !out.getBlockState(base().offset(rel)).getCollisionShape(out,base().offset(rel)).isEmpty();}
        @Override public void close(){LiteraryVignettes.clearAll();players.forEach(NativeTestPlayers::remove);for(var e:out.getEntitiesOfClass(Entity.class,LakeHouse.box(base()).inflate(4)))if(!(e instanceof ServerPlayer))e.discard();
            var s=in.getServer().overworld().getDataStorage();s.set("the_oldest_house",oldHouse);s.set("the_oldest_house_labyrinth",oldData);}
    }

    @GameTest(template="empty",batch="lake_house_scene",timeoutTicks=200)
    public static void theBoathouseCulvertAndHouseStandWhereTheCanoeCanReachThem(GameTestHelper h){
        var f=new Fixture(h);
        try{
            var b=f.base();
            h.assertTrue(b.getY()==LakeHouse.BASE_Y&&NovelRooms.outside(PLACE)&&LiteraryRooms.isLiterary(PLACE)&&PLACE.kind()==LabyrinthPlace.Kind.MULTI_VISIT,"the lake house is an outdoor multi-visit literary scene at its fixed height");
            h.assertTrue(WitnessAccount.Story.of(PLACE.id())!=null&&WitnessAccount.Story.values().length==45&&WitnessAccount.REQUIRED==34,"one new personal source: forty-five sources, thirty-four required");
            h.assertTrue(!ScenePolish.applies(PLACE)&&!SceneReview.applies(PLACE)&&!SceneCraft.applies(PLACE),"the scenery passes leave the lake house as it is built");
            h.assertTrue(f.out.getBlockEntity(b.offset(LakeHouse.SOURCE)) instanceof LecternBlockEntity lectern&&lectern.hasBook(),"the rental log lies on its lectern in the boathouse");
            h.assertTrue(f.out.getBlockState(b.offset(LakeHouse.ACCOUNT)).is(LiteraryRegistry.PROP.get()),"the account waits on its shelf");
            // The culvert carries water from the clear lake to the third, one block of air over it and a ceiling under the drive.
            for(int z=LakeHouse.CHANNEL_SOUTH;z>=LakeHouse.CHANNEL_NORTH;z--){
                h.assertTrue(f.out.getFluidState(b.offset(0,-1,z)).is(Fluids.WATER)&&f.out.getFluidState(b.offset(1,-1,z)).is(Fluids.WATER),"the channel holds water at z "+z);
                h.assertTrue(f.out.getBlockState(b.offset(0,0,z)).isAir()&&f.out.getBlockState(b.offset(1,0,z)).isAir(),"air over the channel at z "+z);
                // A seated reader in the canoe fits under the culvert's ceiling.
                var at=b.offset(0,0,z);h.assertTrue(f.out.noCollision(null,new AABB(at.getX()+.32,at.getY()-.48,at.getZ(),at.getX()+1.68,at.getY()+.92,at.getZ()+1)),"a canoe and its reader pass at z "+z);
            }
            h.assertTrue(f.solid(new BlockPos(0,1,-112))&&f.out.getFluidState(b.offset(0,-1,-98)).is(Fluids.WATER)&&f.out.getFluidState(b.offset(0,-1,-129)).is(Fluids.WATER),"the drive roofs the culvert and both lakes meet its mouths");
            // No lake water stands against open space beside or under it, so nothing drains into the ground.
            int leaks=0;BlockPos leak=null;
            for(int x=LakeHouse.MIN_X;x<=LakeHouse.MAX_X;x++)for(int z=LakeHouse.MIN_Z;z<=-10;z++)for(int y=-44;y<=-1;y++){
                var at=b.offset(x,y,z);if(!f.out.getFluidState(at).is(Fluids.WATER))continue;
                for(var side:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST,Direction.DOWN})if(f.out.getBlockState(at.relative(side)).isAir()){leaks++;if(leak==null)leak=new BlockPos(x,y,z);}
            }
            h.assertTrue(leaks==0,"lake water is held on every side; "+leaks+" open faces, first at "+leak);
            // The house: brick walls, its roof under the surface, the half door open to the water and the raft over the chimney.
            h.assertTrue(f.out.getBlockState(b.offset(-LakeHouse.HX,-24,-170)).is(Blocks.BRICKS)&&f.out.getFluidState(b.offset(LakeHouse.FRONT_DOOR)).is(Fluids.WATER),"the brick house stands with its front doorway open to the lake");
            h.assertTrue(f.out.getFluidState(b.offset(0,-2,-170)).is(Fluids.WATER)&&f.solid(new BlockPos(0,-3,-170)),"the roof's ridge is two blocks under the surface");
            h.assertTrue(f.solid(LakeHouse.RAFT)&&f.out.getBlockState(b.offset(LakeHouse.CHIMNEY)).is(Blocks.BRICKS),"the raft floats over the chimney it is tied to");
            h.assertTrue(f.out.getBlockState(b.offset(LakeHouse.COMPRESSOR)).is(LakeHouseRegistry.COMPRESSOR.get())&&!LakeHouseDive.running(f.out,b),"the compressor sits on the raft, idle until somebody minds it");
            h.succeed();
        }finally{f.close();}
    }

    @GameTest(template="empty",batch="lake_house_canoe",timeoutTicks=200)
    public static void eachReaderIsLentTheirOwnCanoeAndItIsPutAwayWhenTheyLeave(GameTestHelper h){
        var f=new Fixture(h);
        try{
            var b=f.base();var box=LakeHouse.box(b);
            var p=f.player("lake_canoeist");var peer=f.player("lake_partner");
            var canoes=f.out.getEntitiesOfClass(LakeHouseCanoe.class,box);
            h.assertTrue(canoes.size()==2,"two readers find two canoes by the dock, not "+canoes.size());
            var mine=canoes.stream().filter(c->p.getUUID().equals(c.owner())).findFirst().orElse(null);var theirs=canoes.stream().filter(c->peer.getUUID().equals(c.owner())).findFirst().orElse(null);
            h.assertTrue(mine!=null&&theirs!=null&&mine.distanceTo(theirs)>2,"each canoe is its reader's and they wait apart");
            LakeHouseCanoe.ensure(p);h.assertTrue(f.out.getEntitiesOfClass(LakeHouseCanoe.class,box).size()==2,"a reader with a canoe is not lent another");
            h.assertTrue(!mine.hurt(f.out.damageSources().playerAttack(p),100)&&mine.isAlive()&&mine.getDropItem()==net.minecraft.world.item.Items.AIR,"a lent canoe cannot be broken and drops nothing");
            mine.chip();mine.chip();mine.chip();h.assertTrue(mine.chips()==LakeHouseCanoe.MAX_CHIPS,"the paint wears to the wood and no further");
            h.assertTrue(peer.startRiding(mine)&&p.startRiding(mine)&&mine.getPassengers().size()==2,"two readers share one canoe");
            p.stopRiding();peer.stopRiding();
            // The partner leaves the scene; their empty canoe is put away and the reader's stays.
            peer.teleportTo(f.in,b.getX()+.5,b.getY()+300,b.getZ()+.5,0,0);
            LakeHouseCanoe.tidy(f.out,b);
            h.assertTrue(!theirs.isAlive()&&mine.isAlive(),"only the canoe of the reader who left is put away");
            h.succeed();
        }finally{f.close();}
    }

    /** Puts a reader at a spot relative to the scene, facing north, and refreshes what their eyes are in. */
    private static void at(Fixture f,ServerPlayer p,double x,double y,double z){var b=f.base();p.stopRiding();p.teleportTo(f.out,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.baseTick();}
    private static void breathe(Fixture f,ServerPlayer p,int ticks){for(int i=0;i<ticks;i++){p.baseTick();LakeHouseDive.breathe(p,f.base());}}

    @GameTest(template="empty",batch="lake_house_breath",timeoutTicks=200)
    public static void underTheLakeOnlyTheLentGearGivesAirAndRunningOutEndsOnTheRaft(GameTestHelper h){
        var f=new Fixture(h);
        try{
            var p=f.player("lake_diver");var id=p.getUUID();
            at(f,p,-19.5,-10,-171.5);
            h.assertTrue(p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER),"the reader is under the third lake");
            // A potion of water breathing is the reader's own aid: in the lake house it gives nothing.
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING,600));
            LakeHouseDive.setAir(p,300);breathe(f,p,20);
            h.assertTrue(LakeHouseDive.air(id)==280&&p.getAirSupply()==280,"the reader's own water breathing gives no air; air "+LakeHouseDive.air(id));
            // The lent tanks do, and spend their fill doing it.
            var tanks=LakeHouseGear.lend(p,LakeHouseRegistry.TANKS.get());p.setItemSlot(EquipmentSlot.CHEST,tanks);
            breathe(f,p,20);
            h.assertTrue(LakeHouseDive.air(id)==300&&p.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()==20,"the lent tanks breathe for the reader and their gauge falls");
            // Somebody else's tanks are nothing to this reader.
            var peer=f.player("lake_lender");p.setItemSlot(EquipmentSlot.CHEST,LakeHouseGear.lend(peer,LakeHouseRegistry.TANKS.get()));
            LakeHouseDive.setAir(p,50);breathe(f,p,10);
            h.assertTrue(LakeHouseDive.air(id)==40,"tanks lent to another reader give this one nothing");
            p.setItemSlot(EquipmentSlot.CHEST,net.minecraft.world.item.ItemStack.EMPTY);
            // Out of air: the chest locks, then the lake goes black, then the reader comes to on the raft with half a breath.
            LakeHouseDive.setAir(p,0);breathe(f,p,1);
            h.assertTrue(LakeHouseDive.choking(id)&&p.getHealth()==p.getMaxHealth(),"no air: the reader chokes, unharmed");
            breathe(f,p,LakeHouseDive.CHOKE-1);
            h.assertTrue(LakeHouseDive.blackedOut(id)&&!LakeHouseDive.choking(id),"after the choke, the blackout");
            breathe(f,p,LakeHouseDive.BLACKOUT-LakeHouseDive.WAKE);
            var raft=Vec3.atBottomCenterOf(f.base().offset(LakeHouse.RAFT.offset(1,1,0)));
            h.assertTrue(p.position().distanceTo(raft)<1.5&&p.getAirSupply()==p.getMaxAirSupply()/2&&p.isAlive(),"the reader comes to on the raft with half a breath: "+p.position());
            h.succeed();
        }finally{f.close();}
    }

    @GameTest(template="empty",batch="lake_house_hose",timeoutTicks=200)
    public static void theHelmetBreathesOnlyWhileTheCompressorIsMindedAndTheHoseIsFree(GameTestHelper h){
        var f=new Fixture(h);
        try{
            var b=f.base();var p=f.player("lake_helmet");var id=p.getUUID();
            p.setItemSlot(EquipmentSlot.HEAD,LakeHouseGear.lend(p,LakeHouseRegistry.HELMET.get()));
            at(f,p,-.5,-23,-168.5);
            // Alone, Tess minds the compressor: it runs and the hose breathes.
            LakeHouseDive.compressor(f.out,b,0);
            h.assertTrue(LakeHouseDive.running(f.out,b),"a reader diving alone has the compressor minded for them");
            LakeHouseDive.setAir(p,100);breathe(f,p,10);
            h.assertTrue(LakeHouseDive.air(id)==120,"the hose breathes for the diver; air "+LakeHouseDive.air(id));
            // Two readers and nobody at the compressor: it stops, and so does the air.
            var partner=f.player("lake_minder");LakeHouseDive.compressor(f.out,b,10);
            h.assertTrue(!LakeHouseDive.running(f.out,b),"with two readers, an unminded compressor stops");
            breathe(f,p,10);h.assertTrue(LakeHouseDive.air(id)==110,"no air comes down an idle hose");
            at(f,partner,LakeHouse.RAFT.getX()+1.5,0,LakeHouse.RAFT.getZ()+.5);LakeHouseDive.compressor(f.out,b,20);
            h.assertTrue(LakeHouseDive.running(f.out,b),"a partner on the raft minds the compressor");
            // On the helmet visit the hose catches on the step's rail the first time the diver goes in.
            var own=LiteraryVignettes.personal(f.data(),id,PLACE);own.putInt("Visit",2);
            LakeHouse.reader(p,b,own);
            h.assertTrue(own.getInt("HoseSnag")==1,"the hose catches on the way in");
            breathe(f,p,10);h.assertTrue(LakeHouseDive.air(id)==100,"a caught hose gives no air");
            at(f,p,-.5,-25.8,-159.5);LakeHouse.reader(p,b,own);
            h.assertTrue(own.getInt("HoseSnag")==2,"swimming back to the rail frees the hose");
            at(f,p,-.5,-23,-168.5);LakeHouse.reader(p,b,own);breathe(f,p,10);
            h.assertTrue(own.getInt("HoseSnag")==2&&LakeHouseDive.air(id)==120,"freed, it never catches again and breathes again");
            h.succeed();
        }finally{f.close();}
    }

    @GameTest(template="empty",batch="lake_house_gear",timeoutTicks=200)
    public static void theFlashlightIsARealLightAndTheLentGearStaysBehind(GameTestHelper h){
        var f=new Fixture(h);
        try{
            var b=f.base();var p=f.player("lake_light");var id=p.getUUID();
            var own=LiteraryVignettes.personal(f.data(),id,PLACE);LakeHouse.reader(p,b,own);
            h.assertTrue(LakeHouseGear.has(p,LakeHouseRegistry.FLASHLIGHT.get()),"the flashlight by the boathouse door is lent on arrival");
            for(int i=0;i<p.getInventory().items.size();i++)if(LakeHouseGear.mine(p.getInventory().items.get(i),p,LakeHouseRegistry.FLASHLIGHT.get())){p.getInventory().selected=i<9?i:0;if(i>=9){p.getInventory().items.set(0,p.getInventory().items.get(i));p.getInventory().items.set(i,net.minecraft.world.item.ItemStack.EMPTY);}break;}
            at(f,p,.5,-24,-162.5);LakeHouseDive.light(p);
            var cell=LakeHouseDive.lightAt(id);
            h.assertTrue(cell!=null&&f.out.getBlockState(cell).is(Blocks.LIGHT)&&f.out.getBlockState(cell).getValue(LightBlock.WATERLOGGED),"the beam lands as a real light in the water: "+cell);
            p.getInventory().selected=8;p.getInventory().items.set(8,net.minecraft.world.item.ItemStack.EMPTY);LakeHouseDive.light(p);
            h.assertTrue(LakeHouseDive.lightAt(id)==null&&f.out.getFluidState(cell).isSource()&&f.out.getBlockState(cell).is(Blocks.WATER),"put away, the light goes and the water is as it was");
            // Out of the scene, every lent piece goes back.
            p.setItemSlot(EquipmentSlot.HEAD,LakeHouseGear.lend(p,LakeHouseRegistry.HELMET.get()));
            p.teleportTo(f.in,b.getX()+.5,b.getY()+300,b.getZ()+.5,0,0);p.getInventory().tick();
            h.assertTrue(!LakeHouseGear.has(p,LakeHouseRegistry.FLASHLIGHT.get())&&!LakeHouseGear.has(p,LakeHouseRegistry.HELMET.get()),"outside the lake house the lent gear is gone");
            h.succeed();
        }finally{f.close();}
    }

    @GameTest(template="empty",batch="lake_house_rooms",timeoutTicks=200)
    public static void theHouseHoldsWaterEverywhereButItsChairsAndThirtyStepsGoDownToThePool(GameTestHelper h){
        var f=new Fixture(h);
        try{
            var b=f.base();
            // No pocket of air anywhere inside, under or over the house, except the eight dining chairs.
            var chairs=new java.util.HashSet<BlockPos>();for(int z=-166;z<=-163;z++){chairs.add(new BlockPos(-8,-25,z));chairs.add(new BlockPos(-5,-25,z));}
            for(var c:chairs){var st=f.out.getBlockState(b.offset(c));h.assertTrue(st.getBlock() instanceof StairBlock&&!st.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED),"a dining chair holds no water at "+c);}
            int pockets=0;BlockPos pocket=null;
            for(int x=-LakeHouse.HX;x<=LakeHouse.HX;x++)for(int z=LakeHouse.HZ_BACK;z<=LakeHouse.HZ_FRONT;z++)for(int y=-44;y<=-4;y++){
                var rel=new BlockPos(x,y,z);if(chairs.contains(rel))continue;var st=f.out.getBlockState(b.offset(rel));
                boolean dry=st.isAir()||st.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)&&!st.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
                if(dry){pockets++;if(pocket==null)pocket=rel;}
            }
            h.assertTrue(pockets==0,"the house holds water in every cell but its chairs; "+pockets+" dry, first at "+pocket);
            // The closet's wall looks like the rest of the pink, and thirty steps go down behind it, each with room to stand.
            h.assertTrue(f.out.getBlockState(b.offset(-6,-25,-178)).is(Blocks.PINK_TERRACOTTA)&&f.out.getBlockState(b.offset(-6,-24,-178)).is(Blocks.PINK_TERRACOTTA),"the closet's wall is shut");
            double[] surfaces={-26,-27};
            for(int k=0;k<30;k++){
                double surf=k<2?-26-k:-27-.5*(k-1);int x=k<10?-8:k<20?-6:-4,z=k<10?-177+k:k<20?-168-(k-10):-177+(k-20);
                var at=Vec3.atLowerCornerOf(b).add(x+.2,surf+.01,z+.2);
                h.assertTrue(f.out.noCollision(null,new AABB(at.x,at.y,at.z,at.x+1.6,at.y+1.8,at.z+.6)),"step "+(k+1)+" of thirty has room to stand at "+x+","+surf+","+z);
                h.assertTrue(!f.out.noCollision(null,new AABB(at.x,at.y-.2,at.z,at.x+.6,at.y-.02,at.z+.6)),"step "+(k+1)+" is underfoot");
            }
            int marks=0;for(var e:BlockPos.betweenClosed(b.offset(-9,-44,-180),b.offset(-2,-26,-160)))if(f.out.getBlockEntity(e) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign&&sign.getFrontText().getMessage(2,false).getString().length()>0)marks++;
            h.assertTrue(marks==7,"the stairwell carries its growth chart: "+marks+" marks");
            // The basement: the pool, three feet and five, warm at the bottom, and the sauna nobody looked into.
            h.assertTrue(f.out.getFluidState(b.offset(3,-42,-171)).is(Fluids.WATER)&&f.out.getBlockState(b.offset(3,-43,-171)).is(Blocks.SMOOTH_QUARTZ),"the shallow end is a block deep");
            h.assertTrue(f.out.getFluidState(b.offset(3,-43,-175)).is(Fluids.WATER)&&f.out.getBlockState(b.offset(3,-44,-175)).is(Blocks.MAGMA_BLOCK),"the deep end is two deep, something warm at its floor");
            h.assertTrue(f.out.getBlockState(b.offset(4,-41,-166)).is(Blocks.SPRUCE_PLANKS)&&f.out.getFluidState(b.offset(4,-41,-165)).is(Fluids.WATER),"the sauna has its walls and its door");
            // Every lamp is a bulb set in the ceiling, unlit; the hearth's fire and the still life are shown, and the half door hangs in the doorway.
            h.assertTrue(f.out.getBlockState(b.offset(0,-20,-164)).is(Blocks.REDSTONE_LAMP)&&!f.out.getBlockState(b.offset(0,-20,-164)).getValue(RedstoneLampBlock.LIT),"the foyer's bulb is unlit");
            var shown=f.out.getEntitiesOfClass(Entity.class,LakeHouse.box(b),e->e.getTags().contains(LakeHouseDisplay.TAG));
            h.assertTrue(shown.stream().anyMatch(e->e instanceof net.minecraft.world.entity.decoration.Painting)&&shown.stream().filter(e->e instanceof LakeHouseDisplay).count()==3,"the painting, the door's two halves and the hearth's fire are in place: "+shown.size());
            h.succeed();
        }finally{f.close();}
    }

    @GameTest(template="empty",batch="lake_house_ask",timeoutTicks=200)
    public static void askingHowLiftsThePieceForThatReaderAloneAndTheSecondTimeBreaksTheRule(GameTestHelper h){
        var f=new Fixture(h);asking=f;
        try{
            var b=f.base();var p=f.player("lake_asker");var peer=f.player("lake_witness");var beaver=b.offset(5,-24,-171);
            at(f,p,4.5,-24,-170.5);
            h.assertTrue(f.out.getBlockState(beaver).is(LakeHouseRegistry.BEAVER.get())&&f.out.getBlockState(beaver).getValue(LakeHouseBeaverBlock.WATERLOGGED),"the beaver stands on the island, in the water");
            h.assertTrue(LakeHouseRules.ask(p,b,beaver)==1,"the first time is a warning");
            var own=LiteraryVignettes.personal(f.data(),p.getUUID(),PLACE);
            h.assertTrue(!own.getBoolean("AskedHow")&&f.out.getBlockState(beaver).is(LakeHouseRegistry.BEAVER.get()),"it is not broken and the rule holds");
            var lifted=f.out.getEntitiesOfClass(LakeHouseDisplay.class,new AABB(beaver).inflate(.5),d->p.getUUID().equals(d.owner()));
            h.assertTrue(lifted.size()==1&&lifted.getFirst().broadcastToPlayer(p)&&!lifted.getFirst().broadcastToPlayer(peer),"only the reader who asked sees it lift");
            h.assertTrue(LakeHouseRules.ask(p,b,beaver)==2,"asking again");
            own=LiteraryVignettes.personal(f.data(),p.getUUID(),PLACE);
            h.assertTrue(own.getBoolean("AskedHow")&&f.out.getBlockState(beaver).is(LakeHouseRegistry.BEAVER.get()),"the second time breaks the rule for good, and still nothing breaks");
            h.assertTrue(!LiteraryVignettes.personal(f.data(),peer.getUUID(),PLACE).getBoolean("AskedHow"),"the rule is broken for that reader alone");
            h.runAfterDelay(LakeHouseRules.BACK+2,()->{
                h.assertTrue(f.out.getEntitiesOfClass(LakeHouseDisplay.class,new AABB(beaver).inflate(.5),d->p.getUUID().equals(d.owner())).isEmpty(),"each lifted piece settles back and goes");
                h.succeed();});
        }catch(RuntimeException e){f.close();asking=null;throw e;}
    }
    private static Fixture asking;
    @AfterBatch(batch="lake_house_ask") public static void askDone(ServerLevel l){if(asking!=null){asking.close();asking=null;}}

    @GameTest(template="empty",batch="lake_house_closet",timeoutTicks=200)
    public static void theClosetGivesFromTheTankVisitAndTheChairsGiveAir(GameTestHelper h){
        var f=new Fixture(h);
        try{
            var b=f.base();var p=f.player("lake_closet");var id=p.getUUID();var panel=new BlockPos(-6,-25,-178);
            var own=LiteraryVignettes.personal(f.data(),id,PLACE);own.putInt("Visit",2);
            h.assertTrue(LakeHouse.click(p,b,panel,own)&&f.out.getBlockState(b.offset(panel)).is(Blocks.PINK_TERRACOTTA),"on the helmet visit the wall sounds hollow and does not give");
            own.putInt("Visit",3);
            h.assertTrue(LakeHouse.click(p,b,panel,own)&&f.out.getFluidState(b.offset(panel)).is(Fluids.WATER)&&f.out.getFluidState(b.offset(panel.above())).is(Fluids.WATER),"on the tank visit it gives, and stays open");
            // A dining chair: the reader sits, under water, and breathes.
            at(f,p,-7.5,-25,-164.5);
            h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(-8,-25,-164))&&LakeHouseRules.dryChair(p),"the reader sits in a dining chair");
            p.baseTick();h.assertTrue(p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER),"their head is under water");
            LakeHouseDive.setAir(p,100);breathe(f,p,10);
            h.assertTrue(LakeHouseDive.air(id)==140,"the chair gives them air; air "+LakeHouseDive.air(id));
            p.stopRiding();
            // In the third lake, things sink and lie still.
            var item=new net.minecraft.world.entity.item.ItemEntity(f.out,b.getX()-19.5,b.getY()-10,b.getZ()-171.5,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE));
            f.out.addFreshEntity(item);item.baseTick();
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(item));
            h.assertTrue(item.isInWater()&&item.getDeltaMovement().y<0&&item.getDeltaMovement().x==0,"a thing dropped in the third lake sinks straight down: "+item.getDeltaMovement());
            item.discard();
            h.succeed();
        }finally{f.close();}
    }

    @GameTest(template="empty",batch="lake_house_account",timeoutTicks=200)
    public static void theRentalLogThenTheAccountResolveTheReadersOwnStory(GameTestHelper h){
        var f=new Fixture(h);
        try{
            var p=f.player("lake_reader");
            f.read(p,LakeHouse.SOURCE);
            h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LAKE_HOUSE),"the log alone resolves nothing");
            var own=LiteraryVignettes.personal(f.data(),p.getUUID(),PLACE);own.putBoolean("Ready",true);own.putString("Outcome",LakeHouse.STAYED);LiteraryVignettes.save(f.data(),p.getUUID(),PLACE,own);
            f.read(p,LakeHouse.ACCOUNT);
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LAKE_HOUSE),"the log and then the account resolve the reader's own lake house");
            h.assertTrue(LakeHouse.account(own).startsWith("THE PLACE AT THE TABLE")&&!LakeHouse.account(new net.minecraft.nbt.CompoundTag()).equals(LakeHouse.account(own)),"each ending has its own account");
            h.succeed();
        }finally{f.close();}
    }
}
