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
}
