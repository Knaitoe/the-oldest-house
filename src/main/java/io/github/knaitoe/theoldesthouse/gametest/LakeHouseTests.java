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
            for(int x=LakeHouse.MIN_X;x<=LakeHouse.MAX_X;x++)for(int z=LakeHouse.MIN_Z;z<=-10;z++)for(int y=-30;y<=-1;y++){
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
