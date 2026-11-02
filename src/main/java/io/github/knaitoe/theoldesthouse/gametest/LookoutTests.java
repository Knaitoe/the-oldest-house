package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.202, the Whistlers in the Pines: the fire lookout's four nights, driven through the tower's own controller with a real
 * reader in the cab. The rule (a look toward the whistling wall while its blind is up raises the tier; looking down is safe)
 * and each night's own task are played through.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class LookoutTests {
    private LookoutTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.WHISTLERS;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);return active;}
    private static void close(){if(active!=null){active.close();active=null;}}

    /** A reader standing in the cab beside the fire finder, eyes on the boards. */
    private static ServerPlayer reader(LiteraryTests.Fixture f,String name,int visit){
        var p=f.player(name,P);var own=f.own(p,P);own.putInt("Visit",visit);own.putInt("Beat",visit-1);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
        var b=f.base(P);var at=b.offset(LookoutTower.FINDER);p.moveTo(at.getX()+1.7,at.getY(),at.getZ()+.5,0,60);p.setDeltaMovement(Vec3.ZERO);
        return p;
    }
    /** A right click on a cell of the tower, from where the reader stands. */
    private static void use(ServerPlayer p,BlockPos rel){var at=LiteraryVignettes.base(p,P).offset(rel);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false)));}
    private static CompoundTag tower(LiteraryTests.Fixture f){return LookoutNight.tower(f.in.getServer(),f.base(P));}
    private static void tower(LiteraryTests.Fixture f,CompoundTag t){f.data().setStateEntry(LookoutNight.STATE,Long.toString(f.base(P).asLong()),t);}
    /** Takes what is in an inventory slot into the main hand (it may be there already: a picked-up thing lands in the selected slot). */
    private static void take(ServerPlayer p,int slot){var inv=p.getInventory();if(slot==inv.selected)return;var stack=inv.getItem(slot).copy();inv.setItem(slot,net.minecraft.world.item.ItemStack.EMPTY);p.setItemInHand(InteractionHand.MAIN_HAND,stack);}
    private static void hold(ServerPlayer p,float bearing){p.setYRot(bearing-180);p.setYHeadRot(bearing-180);p.setXRot(60);}

    @GameTest(template="empty",batch="lookout_first",timeoutTicks=2000)
    public static void theFirstNightChecksInLogsThreeFlaresByBearingAndLooksDownThroughTheWhistles(GameTestHelper h){
        var f=fixture(h);var p=reader(f,"lookout_first",1);
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(tower(f).getInt("Night")==1&&tower(f).getBoolean("Started")&&tower(f).getInt("Step")==1,"the reader's first night begins in the cab, and the welcome comes off the tape"))
            .thenExecute(()->{use(p,LookoutTower.RADIO);h.assertTrue(tower(f).getBoolean("CheckedIn"),"the handset checks tower four in");use(p,LookoutTower.FINDER);h.assertTrue(f.own(p,P).getBoolean("Alidade202"),"the reader takes the alidade");})
            .thenWaitUntil(()->{var t=tower(f);int flare=t.getInt("Flare");if(flare>=0)hold(p,LookoutNight.FLARES[flare]);else p.setXRot(60);
                h.assertTrue(f.own(p,P).getList("Flares202",3).size()==3&&f.own(p,P).getBoolean("Logged202"),"three flares go up in turn and each is logged at its bearing");})
            .thenExecute(()->{var own=f.own(p,P);var t=tower(f);
                for(int i=0;i<3;i++)h.assertTrue(Math.abs(own.getList("Flares202",3).getInt(i)-LookoutNight.FLARES[i])<=2,"flare "+i+" is logged at its own bearing");
                h.assertTrue(own.getInt("Looked202")==0&&t.getInt("Tier")==0,"eyes on the boards through both whistles: nothing came any nearer");
                h.assertTrue(own.getList("Tape202",8).size()>=5,"the tape kept the welcome, the check-in and each flare on the reader's record");})
            .thenSucceed();
    }
    @AfterBatch(batch="lookout_first") public static void firstDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="lookout_look",timeoutTicks=600)
    public static void eachLookTowardTheWhistlingWallBringsItNearerAndTheFourthBreaksTheGlass(GameTestHelper h){
        var f=fixture(h);var p=reader(f,"lookout_look",1);var b=f.base(P);
        Runnable whistle=()->{var t=tower(f);t.putInt("Wall",0);t.putInt("LastWall",0);t.putInt("WhistleEnd",t.getInt("Clock")+400);t.putInt("WhistleNext",t.getInt("Clock"));t.putBoolean("Failed",false);tower(f,t);p.setYRot(180);p.setYHeadRot(180);p.setXRot(0);};
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(tower(f).getBoolean("Started"),"the night begins with the reader in the cab"))
            .thenExecute(()->{var t=tower(f);t.putInt("Wall",0);t.putInt("WhistleEnd",t.getInt("Clock")+400);tower(f,t);p.setXRot(60);})
            .thenIdle(20)
            .thenExecute(()->h.assertTrue(tower(f).getInt("Tier")==0,"looking down at the boards while it whistles from the north is safe"))
            .thenExecute(whistle).thenWaitUntil(()->h.assertTrue(tower(f).getInt("Tier")==1,"a look north while the north blind is up brings it to the shed roof"))
            .thenExecute(whistle).thenWaitUntil(()->h.assertTrue(tower(f).getInt("Tier")==2,"a second look brings it to the frame under the cab"))
            .thenExecute(whistle).thenWaitUntil(()->h.assertTrue(tower(f).getInt("Tier")==3,"a third brings it to the glass"))
            .thenExecute(()->{use(p,LookoutTower.cord(0,LookoutTower.CZ));h.assertTrue(LookoutTower.wall(f.out,b,0,LookoutTower.CZ)==LookoutBlock.Kind.SHUTTER,"the north cord brings the blind down");})
            .thenExecute(whistle).thenIdle(20)
            .thenExecute(()->h.assertTrue(tower(f).getInt("Tier")==3&&!tower(f).getBoolean("Over"),"with the blind down a look at the north wall sees canvas, and nothing comes"))
            .thenExecute(()->{use(p,LookoutTower.cord(0,LookoutTower.CZ));whistle.run();})
            .thenWaitUntil(()->h.assertTrue(tower(f).getBoolean("Over")&&LookoutTower.wall(f.out,b,0,LookoutTower.CZ)==LookoutBlock.Kind.BROKEN,"the fourth look, at the glass, breaks it and ends the night"))
            .thenExecute(()->{var own=f.own(p,P);h.assertTrue(own.getInt("Broke202")==1&&own.getInt("Looked202")==4,"the reader's record keeps every look and the broken glass");
                h.assertTrue(p.getY()-b.getY()<2,"the reader comes to at the foot of the tower");})
            .thenSucceed();
    }
    @AfterBatch(batch="lookout_look") public static void lookDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="lookout_crank",timeoutTicks=900)
    public static void theSecondNightTheCrankAndFourTurnsStartTheDieselAndTheCabIsWarm(GameTestHelper h){
        var f=fixture(h);var p=reader(f,"lookout_crank",2);
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(tower(f).getInt("Night")==2&&tower(f).getBoolean("Started")&&!tower(f).getBoolean("Power"),"the second night starts in a dark, cold cab"))
            .thenExecute(()->{use(p,LookoutTower.CRANK_HOOK);h.assertTrue(tower(f).getBoolean("Crank"),"the crank comes down from its hook");
                int slot=-1;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(LookoutRegistry.CRANK_ITEM.get()))slot=i;h.assertTrue(slot>=0,"the reader carries the crank");
                take(p,slot);
                // Down the stair to the shed beside the generator, then back up to the cab once it runs.
                var b=f.base(P);var g=b.offset(LookoutTower.GENERATOR);p.moveTo(g.getX()+2.5,g.getY(),g.getZ()+.5,90,30);p.setDeltaMovement(Vec3.ZERO);
                use(p,LookoutTower.GENERATOR);h.assertTrue(tower(f).getBoolean("Primed")&&!tower(f).getBoolean("Power"),"the primer first");
                for(int i=0;i<LookoutNight.TURNS;i++)use(p,LookoutTower.GENERATOR);
                h.assertTrue(tower(f).getBoolean("Power")&&f.own(p,P).getBoolean("Cranked202"),"four turns of the crank close together and the diesel catches");
                var at=b.offset(LookoutTower.FINDER);p.moveTo(at.getX()+1.7,at.getY(),at.getZ()+.5,0,60);p.setDeltaMovement(Vec3.ZERO);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Powered202"),"back in a warm cab the night is done"))
            .thenExecute(()->h.assertTrue(f.own(p,P).getInt("Looked202")==0,"eyes down whenever it whistled at the dark glass"))
            .thenSucceed();
    }
    @AfterBatch(batch="lookout_crank") public static void crankDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="lookout_tarps",timeoutTicks=2600)
    public static void theThirdNightTornBlindsTwoTarpsSixWhistlesAndThreeKnocksOnTheHatch(GameTestHelper h){
        var f=fixture(h);var p=reader(f,"lookout_tarps",3);var b=f.base(P);
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(tower(f).getInt("Night")==3&&tower(f).getBoolean("Started"),"the third night starts with the reader in the cab"))
            .thenExecute(()->{for(int w:new int[]{0,1,3})h.assertTrue(LookoutTower.wall(f.out,b,w,LookoutTower.CZ)==LookoutBlock.Kind.TORN,"the gale has torn the blind on wall "+w);
                h.assertTrue(LookoutTower.wall(f.out,b,2,LookoutTower.CZ)==LookoutBlock.Kind.PANE,"the south blind held");
                use(p,LookoutTower.TARPS.get(0));h.assertTrue(p.getInventory().countItem(LookoutRegistry.TARP_ITEM.get())==1,"a folded tarp picked up");
                int slot=-1;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(LookoutRegistry.TARP_ITEM.get()))slot=i;
                take(p,slot);
                use(p,LookoutTower.cord(0,LookoutTower.CZ));h.assertTrue(LookoutTower.wall(f.out,b,0,LookoutTower.CZ)==LookoutBlock.Kind.TARP,"the tarp is lashed over the north frame");
                h.assertTrue(f.own(p,P).getInt("Watched202")>=1,"the tape prints what the reader did");p.setXRot(60);})
            .thenWaitUntil(()->{p.setXRot(60);h.assertTrue(f.own(p,P).getBoolean("Tarps202"),"six whistles round the walls with eyes down, three knocks on the hatch, and dawn");})
            .thenExecute(()->h.assertTrue(f.own(p,P).getInt("Looked202")==0&&!tower(f).getBoolean("Over"),"nothing came near and no glass went"))
            .thenSucceed();
    }
    @AfterBatch(batch="lookout_tarps") public static void tarpsDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="lookout_north",timeoutTicks=600)
    public static void theFourthNightLockingTheNorthTowerOpensTheHatchToTheFireLog(GameTestHelper h){
        var f=fixture(h);var p=reader(f,"lookout_north",4);var b=f.base(P);
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(tower(f).getInt("Night")==4&&p.getZ()-b.getZ()<LookoutTower.VZ+60,"on the fourth night the reader comes to in tower four over nothing"))
            .thenExecute(()->{var at=b.offset(LookoutTower.VOID_FINDER);p.moveTo(at.getX()+1.7,at.getY(),at.getZ()+.5,180,60);p.setDeltaMovement(Vec3.ZERO);
                use(p,LookoutTower.VOID_FINDER);h.assertTrue(f.own(p,P).getBoolean("Alidade202"),"the reader takes the alidade in the forest's tower four");hold(p,0);})
            .thenWaitUntil(()->{hold(p,0);h.assertTrue(tower(f).getBoolean("Locked"),"bearing 000, the north tower, held steady: the hatch opens");})
            .thenExecute(()->{for(var c:LookoutTower.TRAPDOORS)h.assertTrue(f.out.getBlockState(b.offset(c.getX(),c.getY(),LookoutTower.VZ+(c.getZ()-LookoutTower.CZ))).isAir(),"the hatch is open");
                var c=LookoutTower.TRAPDOORS.get(0);p.moveTo(b.getX()+c.getX()+.5,b.getY()+LookoutTower.FLOOR-1,b.getZ()+LookoutTower.VZ+(c.getZ()-LookoutTower.CZ)+.5);p.setDeltaMovement(Vec3.ZERO);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("North202")&&f.own(p,P).getBoolean("Ready"),"down through the hatch into tower four's own cab, with the fire log waiting"))
            .thenExecute(()->h.assertTrue(LookoutTower.inCab(p.position().subtract(Vec3.atLowerCornerOf(b)),LookoutTower.CZ),"the reader is back in the real cab"))
            .thenSucceed();
    }
    @AfterBatch(batch="lookout_north") public static void northDone(ServerLevel l){close();}

    /**
     * Owner, November 2: "players still aren't able to enter; the platform is too low". A reader walks from the foot of the stair
     * up every tread, round the four landings and in through the opening onto the cab floor, with ordinary steps, gravity and
     * collision (no jumping, no teleport), and ends standing on the boards inside the cab.
     */
    @GameTest(template="empty",batch="lookout_climb",timeoutTicks=2400)
    public static void aReaderWalksUpTheStairIntoTheCab(GameTestHelper h){
        var f=fixture(h);var p=f.player("lookout_climb",P);var b=f.base(P);var l=f.out;
        var route=new java.util.ArrayList<Vec3>();for(var t:LookoutTower.STAIR)route.add(new Vec3(b.getX()+t.x()+.5,0,b.getZ()+t.z()+.5));
        var top=LookoutTower.STAIR.get(LookoutTower.STAIR.size()-1);
        route.add(new Vec3(b.getX()+top.x()+.5,0,b.getZ()+top.z()-1.5));route.add(new Vec3(b.getX()+LookoutTower.CX+2.5,0,b.getZ()+LookoutTower.CZ+1.5));
        int[] at={1,0,0};
        h.startSequence().thenExecute(()->f.chunks.hold(l,new AABB(b.getX()-12,b.getY()-4,b.getZ()+LookoutTower.CZ-12,b.getX()+12,b.getY()+30,b.getZ()+LookoutTower.CZ+12)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the tower is resident"))
            .thenExecute(()->{
                // Every banister and rail joins whatever stands beside it: another post or a solid face. Owner: "banisters are still disconnected".
                var loose=new java.util.ArrayList<String>();
                for(var cell:BlockPos.betweenClosed(b.offset(LookoutTower.CX-6,0,LookoutTower.CZ-6),b.offset(LookoutTower.CX+6,LookoutTower.FLOOR+2,LookoutTower.CZ+6))){var s=l.getBlockState(cell);if(!(s.getBlock() instanceof net.minecraft.world.level.block.FenceBlock))continue;
                    for(var d:Direction.Plane.HORIZONTAL){var n=cell.relative(d);var ns=l.getBlockState(n);boolean should=ns.getBlock() instanceof net.minecraft.world.level.block.FenceBlock||ns.isFaceSturdy(l,n,d.getOpposite());
                        var side=switch(d){case NORTH->net.minecraft.world.level.block.CrossCollisionBlock.NORTH;case SOUTH->net.minecraft.world.level.block.CrossCollisionBlock.SOUTH;case EAST->net.minecraft.world.level.block.CrossCollisionBlock.EAST;default->net.minecraft.world.level.block.CrossCollisionBlock.WEST;};
                        if(should&&!s.getValue(side))loose.add(cell.subtract(b).toShortString()+" "+d);}
                    // A rail that meets another only corner to corner reads as broken: some post must join them along a side.
                    for(int dx:new int[]{-1,1})for(int dz:new int[]{-1,1}){if(!(l.getBlockState(cell.offset(dx,0,dz)).getBlock() instanceof net.minecraft.world.level.block.FenceBlock))continue;
                        var a1=l.getBlockState(cell.offset(dx,0,0));var a2=l.getBlockState(cell.offset(0,0,dz));
                        if(!(a1.getBlock() instanceof net.minecraft.world.level.block.FenceBlock)&&!(a2.getBlock() instanceof net.minecraft.world.level.block.FenceBlock)&&!a1.isSolid()&&!a2.isSolid())loose.add(cell.subtract(b).toShortString()+" diagonal "+dx+","+dz);}}
                h.assertTrue(loose.isEmpty(),"every banister joins its neighbours; loose: "+loose);
                var foot=LookoutTower.STAIR.get(0);p.stopRiding();p.setNoGravity(false);p.teleportTo(l,b.getX()+foot.x()+.5,b.getY(),b.getZ()+foot.z()+.5,0,0);p.setDeltaMovement(Vec3.ZERO);})
            .thenWaitUntil(()->{
                var target=route.get(Math.min(at[0],route.size()-1));var d=target.subtract(p.position()).multiply(1,0,1);
                if(d.lengthSqr()<.02&&at[0]<route.size()){at[0]++;at[1]=0;}else at[1]++;
                if(at[0]<route.size()){var aim=route.get(at[0]).subtract(p.position()).multiply(1,0,1);var step=aim.lengthSqr()<.0256?aim:aim.normalize().scale(.16);
                    double vertical=p.onGround()?0:(p.getDeltaMovement().y-.08)*.98;var v=new Vec3(step.x,vertical,step.z);p.setDeltaMovement(v);p.move(MoverType.SELF,v);if(p.verticalCollision)p.setDeltaMovement(step.x,0,step.z);}
                var rel=p.position().subtract(b.getX(),b.getY(),b.getZ());
                h.assertTrue(at[1]<120,"stuck on the way up at step "+at[0]+" of "+route.size()+" at "+rel+(at[0]<LookoutTower.STAIR.size()?" (tread "+LookoutTower.STAIR.get(at[0])+")":" (the cab floor)"));
                h.assertTrue(at[0]>=route.size()&&LookoutTower.inCab(rel,LookoutTower.CZ)&&Math.abs(rel.y-(LookoutTower.FLOOR+1))<.01,"walking up: step "+at[0]+" of "+route.size()+" at "+rel);})
            .thenSucceed();
    }
    @AfterBatch(batch="lookout_climb") public static void climbDone(ServerLevel l){close();}
}
