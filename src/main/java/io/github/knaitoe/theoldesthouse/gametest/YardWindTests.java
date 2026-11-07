package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.345, the Windbreak (YardWind), with readers in the field: the gust shown across the grass before it hits, then pushing the reader
 * out in the open while the poplars, the stone wall and a rut crouched in keep theirs; the loose sheet torn off on the third gust and
 * across the field into whoever is in its row, never past three hearts, landing by the wire; the lean-to's back boards stripped on the
 * fourth and fifth until it shelters nobody; and the field as it was again.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardWindTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_wind") public static void windDone(ServerLevel l){close();}
    @AfterBatch(batch="yard_wind_sheet") public static void sheetDone(ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);var a=LabyrinthPlaces.slotBounds(active.origin,YardWind.PLACE);active.chunks.hold(active.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,90,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();}
    private static void clock(LiteraryTests.Fixture f,ServerLevel l,long c){var s=YardWind.state(l);s.putLong("Clock",c);f.data().setStateEntry(YardWind.STATE,YardWind.PLACE.id(),s);}

    @GameTest(template="empty",batch="yard_wind",timeoutTicks=1600)
    public static void theGustShowsOnTheGrassThenPushesTheReaderInTheOpenAndNotThoseInShelter(GameTestHelper h){
        var f=fixture(h);var l=f.out;var b=f.base(YardWind.PLACE);var open=player(h,f,"wind_open");var poplar=player(h,f,"wind_poplar");var wall=player(h,f,"wind_wall");var rut=player(h,f,"wind_rut");
        h.onEachTick(()->{rut.setShiftKeyDown(true);rut.setPose(Pose.CROUCHING);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the field is resident"))
            .thenExecute(()->{
                YardWind.build(l,b);LabyrinthBuilder.registerDoors(f.data(),YardWind.PLACE,b);
                at(open,l,b,.5,0,-20.5);at(poplar,l,b,-6.5,0,-9.5);at(wall,l,b,-6.5,0,-31.5);at(rut,l,b,4.5,-1,-20.5);
                h.assertTrue(YardWind.sheltered(l,b,poplar)&&YardWind.sheltered(l,b,wall)&&!YardWind.sheltered(l,b,open),"the poplars and the stone wall shelter their lee; the middle of the field is open");
                clock(f,l,YardWind.RIPPLE-1);
            })
            .thenWaitUntil(()->h.assertTrue(YardWind.phase(YardWind.state(l).getLong("Clock"))==1,"the gust is coming"))
            .thenExecute(()->{h.assertTrue(open.getDeltaMovement().x==0,"the grass shows it before it hits: nobody is pushed yet");
                h.assertTrue(TruantFittingBlock.stage(l.getBlockState(b.offset(YardWind.SHELTER_X+1,6,-9)))==1,"the poplars' boughs lean with it");
                h.assertTrue(YardWind.sheltered(l,b,rut),"crouched in a rut is out of the wind");clock(f,l,YardWind.GUST-1);})
            .thenIdle(6)
            .thenExecute(()->{
                h.assertTrue(open.getDeltaMovement().x>.2,"in the gust the reader out in the open is pushed east: "+open.getDeltaMovement());
                for(var p:List.of(poplar,wall,rut))h.assertTrue(p.getDeltaMovement().x==0,"in shelter nothing moves "+p.getGameProfile().getName());
                h.assertTrue(YardWind.state(l).getInt("Gusts")==1,"one gust counted");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="yard_wind_sheet",timeoutTicks=1600)
    public static void theLooseSheetTearsAcrossTheFieldAndTheLeanToComesApart(GameTestHelper h){
        var f=fixture(h);var l=f.out;var b=f.base(YardWind.PLACE);var row=player(h,f,"sheet_row");var low=player(h,f,"sheet_low");var under=player(h,f,"sheet_under");float[] before={0,0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the field is resident"))
            .thenExecute(()->{
                YardWind.build(l,b);at(row,l,b,3.5,0,-19.5);at(low,l,b,6.5,0,-19.5);at(under,l,b,-6.5,0,-20.5);low.setHealth(8);before[0]=row.getHealth();before[1]=low.getHealth();
                h.assertTrue(YardWind.sheltered(l,b,under),"under the lean-to, out of the wind");
            })
            // Past the readers' moment of grace after joining, so what reaches them is felt.
            .thenIdle(70)
            .thenExecute(()->{var s=YardWind.state(l);s.putInt("Gusts",2);f.data().setStateEntry(YardWind.STATE,YardWind.PLACE.id(),s);clock(f,l,YardWind.GUST-1);})
            .thenWaitUntil(()->h.assertTrue(YardWind.state(l).getBoolean("Torn"),"the third gust tears the sheet off"))
            .thenExecute(()->h.assertTrue(l.getBlockState(b.offset(YardWind.LOOSE)).isAir()&&YardWind.state(l).contains("Flight"),"it is gone from the roof, in the air"))
            .thenWaitUntil(()->h.assertTrue(!YardWind.state(l).contains("Flight"),"it crosses the field into the wire"))
            .thenExecute(()->{
                h.assertTrue(row.getHealth()==before[0]-YardWind.SHEET_DAMAGE,"it meets the reader in its row at head height: "+row.getHealth());
                h.assertTrue(low.getHealth()==YardWind.FLOOR,"and never takes anyone past three hearts: "+low.getHealth());
                h.assertTrue(under.getHealth()==under.getMaxHealth(),"the reader under the lean-to, behind where it came from, is untouched");
                h.assertTrue(YardBlock.is(l.getBlockState(b.offset(YardWind.LANDED)),YardBlock.Kind.ROOF_SHEET),"it lies by the wire where it struck");
                var s=YardWind.state(l);s.putInt("Gusts",3);f.data().setStateEntry(YardWind.STATE,YardWind.PLACE.id(),s);clock(f,l,YardWind.GUST-1);
            })
            .thenIdle(3)
            .thenExecute(()->{h.assertTrue(l.getBlockState(b.offset(YardWind.SHELTER_X,2,-20)).isAir()&&l.getBlockState(b.offset(YardWind.SHELTER_X,1,-20)).is(Blocks.SPRUCE_PLANKS),"the fourth gust takes the top boards");
                clock(f,l,YardWind.GUST-1);})
            .thenIdle(3)
            .thenExecute(()->{
                h.assertTrue(YardWind.state(l).getBoolean("Stripped")&&l.getBlockState(b.offset(YardWind.SHELTER_X,1,-20)).isAir()&&l.getBlockState(b.offset(YardWind.SHELTER_X,0,-20)).is(Blocks.SPRUCE_PLANKS),"the fifth takes the rest down to the bottom board");
                h.assertTrue(!YardWind.sheltered(l,b,under),"the lean-to shelters nobody now");
                YardWind.reset(l,b,YardWind.state(l));
                h.assertTrue(l.getBlockState(b.offset(YardWind.SHELTER_X,2,-20)).is(Blocks.SPRUCE_PLANKS)&&YardBlock.is(l.getBlockState(b.offset(YardWind.LOOSE)),YardBlock.Kind.ROOF_SHEET)&&l.getBlockState(b.offset(YardWind.LANDED)).isAir()&&YardWind.state(l).getInt("Gusts")==0,"the field as it was, for the next crossing");
            }).thenSucceed();
    }
}
