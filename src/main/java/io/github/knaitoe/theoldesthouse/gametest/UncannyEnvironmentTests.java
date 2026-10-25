package io.github.knaitoe.theoldesthouse.gametest;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Fast native construction and usable-furniture review, with actual floor-specific exports. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_uncanny_environment") @PrefixGameTestTemplate(false)
public final class UncannyEnvironmentTests {
    private static LiteraryTests.Fixture fixture;
    @GameTest(template="empty",batch="uncanny_environment",timeoutTicks=600)
    public static void fittedRoomsKeepTheirServingAislesOriginalsAndNativeSeats(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();
        for(var place:UncannyRooms.PLACES){f.build(place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));}
        var p=f.player("environment_reader",LabyrinthPlace.SECOND_WAITRESS);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native entity sections are loaded")).thenExecute(()->{
            var place=LabyrinthPlace.SECOND_WAITRESS;var l=f.level(place);var b=f.base(place);
            for(int z=-4;z>=-24;z--)body(h,l,b,0,0,z,"the entrance and main serving aisle");
            for(int x=-5;x<=3;x++)body(h,l,b,x,0,-20,"the original waitress's solid serving route");
            body(h,l,b,-8,0,-25,"the working kitchen door");
            var sleep=f.base(LabyrinthPlace.MISSING_SLEEP);var home=f.level(LabyrinthPlace.MISSING_SLEEP);
            for(boolean peer:new boolean[]{false,true})for(int i=0;i<=20;i++){
                double t=i/20.,x=(peer?-7.3:-6.5)+t,z=-10.5-10*t;
                var feet=new Vec3(sleep.getX()+x,sleep.getY()+5,sleep.getZ()+z);
                h.assertTrue(home.noCollision(null,new AABB(feet.x-.3,feet.y+.01,feet.z-.3,feet.x+.3,feet.y+1.8,feet.z+.3)),"both notebook approaches clear a real standing body at "+feet);
            }
            f.at(p,place,4,0,-17);h.assertTrue(HouseSitting.sit(p,b.offset(5,0,-18))&&p.getVehicle() instanceof SeatEntity,"a real reader sits on the actual fourth booth");p.stopRiding();
            var cabinet=b.offset(-9,0,-13);p.moveTo(cabinet.getX()+2, cabinet.getY(),cabinet.getZ()+.5);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            var before=f.own(p,place).copy();var count=p.getInventory().countItem(UncannyRegistry.MEAL.get());
            p.gameMode.useItemOn(p,l,p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(cabinet.getCenter(),Direction.EAST,cabinet,false));
            h.assertTrue(l.getBlockState(cabinet).getValue(UncannyFurnitureBlock.OPEN),"the fitted cabinet opens through the real native use path");
            h.assertTrue(f.own(p,place).equals(before)&&p.getInventory().countItem(UncannyRegistry.MEAL.get())==count,"opening decor does not create a serving or advance the account");
            for(var scene:UncannyRooms.PLACES){var state=f.level(scene).getBlockState(f.base(scene).offset(UncannyRooms.source(scene)));h.assertTrue(state.is(UncannyRegistry.PROP.get()),"the authored source survives dressing: "+scene);}
            try{
                export(f,LabyrinthPlace.SECOND_WAITRESS,"second_waitress_interior",-10,10,-32,-3,-1,4,true,0,0);
                export(f,LabyrinthPlace.SECOND_WAITRESS,"second_waitress_exterior",-16,26,-43,0,-1,7,false,0,0);
                export(f,LabyrinthPlace.SECOND_WAITRESS,"second_waitress_kitchen",-10,10,-32,-25,-1,4,true,0,0);
                export(f,LabyrinthPlace.PAINTED_INTERIOR,"painted_interior",-8,8,-28,-7,-1,4,true,0,-15);
                export(f,LabyrinthPlace.TUNNEL_PERIPHERY,"tunnel_periphery",-3,3,-40,-1,-1,2,true,0,0);
                export(f,LabyrinthPlace.MISSING_SLEEP,"missing_sleep_ground",-9,9,-29,-1,-1,3,true,0,-12);
                export(f,LabyrinthPlace.MISSING_SLEEP,"missing_sleep_upper",-9,9,-29,-1,4,9,true,-3,0);
                export(f,LabyrinthPlace.LAYERED_MAN,"layered_man_cellar",-7,7,-29,-9,-6,-2,true,0,0);
                export(f,LabyrinthPlace.LAYERED_MAN,"layered_man_workshop",-7,7,-30,-1,-1,3,true,0,0);
                export(f,LabyrinthPlace.MOTION_LIGHT,"motion_light",-7,8,-25,-1,-1,6,true,0,0);
            }catch(Exception e){throw new IllegalStateException("Native environment exports failed",e);}
        }).thenSucceed();
    }
    private static void body(GameTestHelper h,ServerLevel l,BlockPos b,int x,int y,int z,String route){
        double xx=b.getX()+x,yy=b.getY()+y,zz=b.getZ()+z;
        h.assertTrue(l.noCollision(null,new AABB(xx+.2,yy+.01,zz+.2,xx+.8,yy+1.8,zz+.8)),route+" clears an actual player body at "+x+","+y+","+z);
        h.assertTrue(!l.noCollision(null,new AABB(xx+.2,yy-.04,zz+.2,xx+.8,yy-.01,zz+.8)),route+" supports the body");
    }
    @GameTest(template="empty",batch="uncanny_environment_refit",timeoutTicks=1200)
    public static void oldRoomsRefitOnlyUnwatchedAndKeepTheActualServedMealAndPeelHoles(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();
        var place=LabyrinthPlace.SECOND_WAITRESS;var p=f.player("refit_reader",place);var l=f.level(place);var b=f.base(place);
        UncannyRooms0121.build(l,b,place);f.build(LabyrinthPlace.LAYERED_MAN);UncannyRooms0121.build(f.level(LabyrinthPlace.LAYERED_MAN),f.base(LabyrinthPlace.LAYERED_MAN),LabyrinthPlace.LAYERED_MAN);
        f.chunks.hold(l,IndianLakeRooms.bounds(b,place));f.chunks.hold(f.level(LabyrinthPlace.LAYERED_MAN),IndianLakeRooms.bounds(f.base(LabyrinthPlace.LAYERED_MAN),LabyrinthPlace.LAYERED_MAN));
        var yard=f.base(LabyrinthPlace.MOTION_LIGHT);var yardLevel=f.level(LabyrinthPlace.MOTION_LIGHT);
        UncannyRooms0121.build(yardLevel,yard,LabyrinthPlace.MOTION_LIGHT);
        for(var at:BlockPos.betweenClosed(yard.offset(-7,-3,-25),yard.offset(7,-3,-15)))yardLevel.setBlock(at,Blocks.STONE.defaultBlockState(),3);
        f.chunks.hold(yardLevel,IndianLakeRooms.bounds(yard,LabyrinthPlace.MOTION_LIGHT));
        LiteraryActor[] settledVisitor={null};
        var kept=new CompoundTag();var meal=new ItemEntity[1];var source=new net.minecraft.world.level.block.entity.BlockEntity[1];
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both original room slots are loaded")).thenExecute(()->{
            f.source(p,place);var own=f.own(p,place);own.putInt("UncannyAge",300);
            for(int i=0;i<18;i++)UncannyStories.reader(p,b,place,own);
            own.putInt("UncannyAge",600);for(int i=0;i<90&&!own.getBoolean("MealServed");i++)UncannyStories.reader(p,b,place,own);
            LiteraryVignettes.save(f.data(),p.getUUID(),place,own);h.assertTrue(own.getBoolean("MealServed"),"the legacy room served its actual original meal before refitting");
            meal[0]=(ItemEntity)l.getEntity(own.getUUID("PlateEntity"));h.assertTrue(meal[0]!=null,"the original exists as a native item entity");kept.putUUID("Meal",meal[0].getUUID());kept.put("Stack",meal[0].getItem().save(p.registryAccess()));kept.putUUID("Actor",own.getUUID("CastFake"));kept.put("Source",own.getCompound("Original_Source").copy());
            source[0]=l.getBlockEntity(b.offset(UncannyRooms.source(place)));
            h.assertTrue(!UncannyEnvironmentRepairs.step(l,f.origin,place)&&!l.getBlockState(b.offset(-2,-1,-16)).is(UncannyRegistry.material("diner_tile")),"a reader in the room prevents every refit edit");
            l.setBlock(b.offset(-1,-1,-16),Blocks.GOLD_BLOCK.defaultBlockState(),3);
            // Scene bounds include eighteen blocks of approach; leave that and the eight-block watch margin.
            f.at(p,place,0,0,40);
            var cb=f.base(LabyrinthPlace.LAYERED_MAN);f.level(LabyrinthPlace.LAYERED_MAN).setBlock(cb.offset(-1,-3,-19),Blocks.AIR.defaultBlockState(),3);
            var world=f.data().stateEntry(UncannyStories.WORLD,LabyrinthPlace.LAYERED_MAN.id()+":"+cb.asLong());world.putInt("Layers",2);world.putInt("Pulse",7);f.data().setStateEntry(UncannyStories.WORLD,LabyrinthPlace.LAYERED_MAN.id()+":"+cb.asLong(),world);
        }).thenWaitUntil(()->h.assertTrue(UncannyEnvironmentRepairs.step(l,f.origin,place),"the bounded native diner refit completed"))
        .thenWaitUntil(()->h.assertTrue(UncannyEnvironmentRepairs.step(f.level(LabyrinthPlace.LAYERED_MAN),f.origin,LabyrinthPlace.LAYERED_MAN),"the bounded native cellar refit completed"))
        .thenWaitUntil(()->h.assertTrue(yardLevel.getBlockState(yard.offset(0,-1,-20)).isAir()&&yardLevel.getBlockState(yard.offset(0,-2,-20)).is(Blocks.GRAVEL),"the legacy unsupported gravel actually settles into the cleared row"))
        .thenExecute(()->{settledVisitor[0]=LiteraryRegistry.ACTOR.get().create(yardLevel);h.assertTrue(settledVisitor[0]!=null,"the original visitor is created for the settled-floor fixture");settledVisitor[0].bind(LabyrinthPlace.MOTION_LIGHT,p.getUUID());settledVisitor[0].appearance(LiteraryActor.UNCANNY_VISITOR,1);settledVisitor[0].moveTo(yard.getX()+.5,yard.getY()-1,yard.getZ()-19.5,180,0);yardLevel.addFreshEntity(settledVisitor[0]);})
        .thenWaitUntil(()->h.assertTrue(UncannyEnvironmentRepairs.step(yardLevel,f.origin,LabyrinthPlace.MOTION_LIGHT),"the bounded native courtyard repair completed"))
        .thenExecute(()->{
            h.assertTrue(yardLevel.getEntity(settledVisitor[0].getUUID())==settledVisitor[0]&&Math.abs(settledVisitor[0].getY()-yard.getY())<.1,"the same settled visitor is lifted onto its restored native floor");
            for(int x=-6;x<=6;x++)for(int z=-24;z<=-16;z++)h.assertTrue(yardLevel.getBlockState(yard.offset(x,-1,z)).is(Blocks.GRAVEL)&&yardLevel.getBlockState(yard.offset(x,-2,z)).is(Blocks.STONE),"settled legacy gravel returns to a fully supported yard without changing its surface");
            h.assertTrue(l.getBlockState(b.offset(-2,-1,-16)).is(UncannyRegistry.material("diner_tile"))&&l.getBlockState(b.offset(-1,-1,-16)).is(Blocks.GOLD_BLOCK),"authored tile changes while a player's edit remains");
            var own=f.own(p,place);h.assertTrue(own.getBoolean("Read_Source")&&own.getBoolean("MealServed")&&!own.contains("OriginalMeal")&&own.getCompound("Original_Source").equals(kept.getCompound("Source")),"reading and exhausted original custody survive");
            h.assertTrue(l.getBlockEntity(b.offset(UncannyRooms.source(place)))==source[0]&&UncannyStories.actor(p,own,"CastFake").getUUID().equals(kept.getUUID("Actor")),"the actual source block entity and actor survive, not replacements");
            h.assertTrue(l.getEntity(kept.getUUID("Meal"))==meal[0]&&meal[0].getItem().save(p.registryAccess()).equals(kept.get("Stack")),"the same served item and components remain");
            var cb=f.base(LabyrinthPlace.LAYERED_MAN);var world=f.data().stateEntry(UncannyStories.WORLD,LabyrinthPlace.LAYERED_MAN.id()+":"+cb.asLong());
            h.assertTrue(f.level(LabyrinthPlace.LAYERED_MAN).getBlockState(cb.offset(-1,-3,-19)).isAir()&&world.getInt("Layers")==2&&world.getInt("Pulse")==7,"peeled holes and shared work progress are not restored or restarted");
            var cabinet=b.offset(-9,0,-13);l.setBlock(cabinet,l.getBlockState(cabinet).setValue(UncannyFurnitureBlock.OPEN,true),3);f.reload();
            h.assertTrue(UncannyEnvironmentRepairs.step(l,f.origin,place)&&l.getBlockState(cabinet).getValue(UncannyFurnitureBlock.OPEN)&&l.getEntity(kept.getUUID("Meal"))==meal[0],"the saved checkpoint never repeats, shuts furniture or replaces the original after reload");
        }).thenSucceed();
    }
    private static void export(LiteraryTests.Fixture f,LabyrinthPlace place,String name,int x0,int x1,int z0,int z1,int y0,int y1,boolean cut,int sliceX,int sliceZ)throws Exception{
        var l=f.level(place);var b=f.base(place);var file=new JsonObject();file.addProperty("name",name);
        var palette=new JsonArray();var blocks=new JsonArray();Map<BlockState,Integer> states=new LinkedHashMap<>();
        for(var rel:BlockPos.betweenClosed(x0,y0,z0,x1,y1,z1)){
            var s=l.getBlockState(b.offset(rel));if(s.isAir()||s.is(Blocks.BARRIER)||s.is(Blocks.LIGHT))continue;
            int x=rel.getX(),y=rel.getY(),z=rel.getZ();
            if(cut&&y>y0&&(x==x1||z==z1))continue;
            boolean finish=s.getBlock().getDescriptionId().startsWith("block.the_oldest_house.uncanny_")&&!s.is(UncannyRegistry.FURNITURE.get())&&!s.is(UncannyRegistry.PROP.get());
            if(cut&&finish&&((sliceX!=0&&x==sliceX&&y>=7)||(sliceZ!=0&&z==sliceZ&&y>=2)))continue;
            if(place==LabyrinthPlace.MISSING_SLEEP&&x==4&&y>=7&&finish)continue;
            if(place==LabyrinthPlace.MOTION_LIGHT&&y==5&&z>=-14&&s.is(Blocks.BIRCH_PLANKS))continue;
            int index=states.computeIfAbsent(s,k->{palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,k).getOrThrow());return states.size();});
            var row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(index);blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);var folder=Path.of("../build/uncanny-environment-proof");Files.createDirectories(folder);Files.writeString(folder.resolve(name+".json"),new Gson().toJson(file));
    }
    @AfterBatch(batch="uncanny_environment") public static void close(ServerLevel l){if(fixture!=null){fixture.close();fixture=null;}}
    @AfterBatch(batch="uncanny_environment_refit") public static void closeRefit(ServerLevel l){close(l);}
}
