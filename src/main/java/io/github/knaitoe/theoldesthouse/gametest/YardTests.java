package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.309, the House's yard, first connector (docs/YARD_TURNAROUND_0_4_309.md): the gravel turn-around as a reader meets it. The porch
 * bulb in sight from the middle of the apron; the wagon's headlights really lighting the trees ahead of it; the squeeze between the
 * wagon and the garage door the only way north, clear all the way to the gate in the back fence; the road east ending at the swollen
 * creek with nothing past it; the engine heard on the apron; and a passing car's light crossing the crowns and leaving nothing behind.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardTests {
    private YardTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_turnaround") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}YardRooms.clearAll();}
    private static boolean fits(ServerLevel l,BlockPos b,double x,double z){var at=Vec3.atLowerCornerOf(b).add(x,0,z);return l.noCollision(null,new AABB(at.x-.3,at.y+.01,at.z-.3,at.x+.3,at.y+1.8,at.z+.3));}

    @GameTest(template="empty",batch="yard_turnaround",timeoutTicks=1200)
    public static void theTurnaroundIsReadableAndItsOnlyWayOnIsTheSqueeze(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=YardRooms.TURNAROUND;var b=f.base(place);var l=f.out;
        var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(l,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));
        var p=NativeTestPlayers.survival(h,"yard_reader");f.players.add(p);
        int[] seen={-1000};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the yard is resident"))
            .thenExecute(()->{
                h.assertTrue(place.isGray()&&NovelRooms.outside(place)&&!place.isVignette(),"an outdoor connector, not a story");
                YardRooms.clearAll();YardRooms.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(f.data().door(place.entryDoorId()).dimension.equals(io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE),"its doors open outside");
                // The porch bulb, lit, and in sight from the middle of the apron.
                var bulb=b.offset(YardRooms.BULB_AT);
                h.assertTrue(YardBlock.is(l.getBlockState(bulb),YardBlock.Kind.BULB),"a bare bulb by the back door");
                var eye=Vec3.atBottomCenterOf(b.offset(YardRooms.APRON)).add(0,1.62,0);
                var hit=l.clip(new ClipContext(eye,Vec3.atCenterOf(bulb),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
                h.assertTrue(hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceTo(Vec3.atCenterOf(bulb))<.9,"the porch light is in sight from the apron: hit "+hit.getLocation().subtract(Vec3.atLowerCornerOf(b)));
                // The wagon, its headlights into the trees.
                h.assertTrue(YardBlock.is(l.getBlockState(b.offset(YardRooms.NOSE_AT)),YardBlock.Kind.NOSE)&&YardBlock.is(l.getBlockState(b.offset(YardRooms.TAIL_AT)),YardBlock.Kind.TAIL),"a station wagon, nose to the trees");
                // The squeeze: one wide between the garage door and the wagon, clear on to the gate; nothing else goes north.
                for(int z=-15;z>=-22;z--){
                    h.assertTrue(fits(l,b,YardRooms.SQUEEZE_X+.5,z+.5),"a reader fits the squeeze at z "+z);
                    h.assertTrue(!l.getBlockState(b.offset(YardRooms.SQUEEZE_X-1,0,z)).isAir()&&(z<-21||!l.getBlockState(b.offset(YardRooms.SQUEEZE_X+1,0,z)).isAir()||z==-15),"walled by the garage and the wagon at z "+z);
                }
                h.assertTrue(l.getBlockState(b.offset(YardRooms.SQUEEZE_X-1,0,-19)).is(PuzzleRegistry.yardMaterial("garage_door")),"the garage door is one side of it");
                for(int z=-23;z>YardRooms.FAR_GATE.getZ();z--)h.assertTrue(fits(l,b,YardRooms.SQUEEZE_X+.5,z+.5),"the path through the tree line is clear at z "+z);
                h.assertTrue(l.getBlockState(b.offset(YardRooms.FAR_GATE)).getBlock() instanceof DoorBlock,"to the gate in the back fence");
                for(int x=-19;x<=47;x++)if(x!=YardRooms.SQUEEZE_X)h.assertTrue(!fits(l,b,x+.5,-23.5),"no other way north through the tree line at x "+x);
                // The road east: a lane between the cedars to the creek, and only cedars past it.
                for(int x=YardRooms.APRON_R+1;x<=YardRooms.ROAD_END;x++)h.assertTrue(fits(l,b,x+.5,YardRooms.ROAD_Z+.5),"the road out is open at x "+x);
                h.assertTrue(l.getBlockState(b.offset(43,-1,YardRooms.ROAD_Z)).is(Blocks.WATER)&&l.getBlockState(b.offset(43,-3,YardRooms.ROAD_Z)).is(Blocks.WATER),"it ends at a swollen creek");
                for(int x=46;x<=48;x++)h.assertTrue(!fits(l,b,x+.5,YardRooms.ROAD_Z+.5),"nothing past the creek but cedars at x "+x);
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-13.5,180,0);p.setNoGravity(true);})
            .thenWaitUntil(()->h.assertTrue(l.getBrightness(LightLayer.BLOCK,b.offset(YardRooms.BULB_AT).below())>=12,"the bulb is lit"))
            .thenWaitUntil(()->h.assertTrue(l.getBrightness(LightLayer.BLOCK,b.offset(-6,2,-25))>=8&&l.getBrightness(LightLayer.BLOCK,b.offset(-6,2,-28))<l.getBrightness(LightLayer.BLOCK,b.offset(-6,2,-24)),
                "the woods ahead are lit, the light thinning with distance: "+l.getBrightness(LightLayer.BLOCK,b.offset(-6,2,-24))+" "+l.getBrightness(LightLayer.BLOCK,b.offset(-6,2,-25))+" "+l.getBrightness(LightLayer.BLOCK,b.offset(-6,2,-28))))
            .thenWaitUntil(()->h.assertTrue(NativeTestPlayers.sounds(p).stream().anyMatch(s->s.getSound().value()==PuzzleRegistry.YARD_IDLE.get()),"the engine idles, heard on the apron"))
            .thenExecute(()->YardRooms.nextCar=l.getGameTime())
            .thenWaitUntil(()->{
                h.assertTrue(YardRooms.sweep!=-1000&&YardRooms.sweep>0&&l.getBlockState(b.offset(YardRooms.sweep,YardRooms.SWEEP_Y,YardRooms.SWEEP_Z)).is(Blocks.LIGHT),"a car passes beyond the trees, its light on the crowns");
                seen[0]=YardRooms.sweep;})
            .thenWaitUntil(()->h.assertTrue(YardRooms.sweep==-1000,"and passes"))
            .thenExecute(()->{
                for(int x=-20;x<=48;x++)h.assertTrue(!l.getBlockState(b.offset(x,YardRooms.SWEEP_Y,YardRooms.SWEEP_Z)).is(Blocks.LIGHT),"its light leaves nothing behind at x "+x);
                h.assertTrue(NativeTestPlayers.sounds(p).stream().anyMatch(s->s.getSound().value()==PuzzleRegistry.YARD_TRAFFIC.get()),"heard going by");})
            .thenSucceed();
    }
}
