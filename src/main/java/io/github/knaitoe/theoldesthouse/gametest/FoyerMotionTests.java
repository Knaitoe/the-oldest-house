package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Native chain use, physical settling, personal original visitors and old-scene custody. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_uncanny") @PrefixGameTestTemplate(false)
public final class FoyerMotionTests {
    private static LiteraryTests.Fixture fixture;
    private static final LabyrinthPlace PLACE=LabyrinthPlace.MOTION_LIGHT;
    private static void close(){if(fixture!=null){fixture.close();fixture=null;}}
    private static void pull(LiteraryTests.Fixture f,ServerPlayer p){
        var b=f.base(PLACE);f.at(p,PLACE,2.1,0,-11.8);var at=b.offset(FoyerMotion.PULL);
        p.gameMode.useItemOn(p,f.level(PLACE),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));
    }
    private static boolean dark(LiteraryTests.Fixture f){return f.level(PLACE).getBlockState(f.base(PLACE).offset(UncannyRooms.FLOOD)).getValue(UncannyPropBlock.STAGE)==1;}

    @GameTest(template="empty",batch="foyer_motion_native",timeoutTicks=2000)
    public static void nativePullAndActualSettlingControlTheLampAndRevealTheSameVisitorGradually(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var p=f.player("chain_reader",PLACE);var peer=f.player("chain_peer",PLACE);var l=f.level(PLACE);var b=f.base(PLACE);
        f.chunks.hold(l,IndianLakeRooms.bounds(b,PLACE));UUID[] original={null};double[] firstZ={0},angle={0};int[] firstStill={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual yard and motion fitting are resident"))
        .thenWaitUntil(()->{var a=UncannyStories.actor(p,f.own(p,PLACE),"CastVisitor");h.assertTrue(dark(f)&&f.own(p,PLACE).getInt("FoyerStill131")==1&&a!=null&&a.phase()==1,"the first gust decays and the sounds precede the original visitor");})
        .thenExecute(()->{
            var a=UncannyStories.actor(p,f.own(p,PLACE),"CastVisitor");h.assertTrue(a!=null&&a.phase()==1,"first stillness reveals the original quiet visitor");original[0]=a.getUUID();firstZ[0]=a.getZ();firstStill[0]=f.own(p,PLACE).getInt("FoyerStill131");
            f.at(p,PLACE,-1.5,0,-11.5);f.at(peer,PLACE,1.5,0,-11.5);
        }).thenIdle(40).thenExecute(()->{
            h.assertTrue(dark(f)&&f.own(p,PLACE).getInt("FoyerStill131")==firstStill[0],"walking during the actual wind lull cannot renew the physical sensor");
            var a=UncannyStories.actor(p,f.own(p,PLACE),"CastVisitor");h.assertTrue(a.getZ()==firstZ[0],"the visitor remains physically still in darkness");
            pull(f,p);var motion=FoyerMotion.fitting(l,b);angle[0]=motion.swing.sample(l.getGameTime()).angle();
            h.assertTrue(!dark(f)&&motion.swing.pulls()==1&&f.own(p,PLACE).getInt("FoyerPulls131")==1&&f.own(peer,PLACE).getInt("FoyerPulls131")==0,"actual native chain use starts shared light and records only its reader's pull");
        }).thenIdle(15).thenExecute(()->h.assertTrue(Math.abs(FoyerMotion.fitting(l,b).swing.sample(l.getGameTime()).angle()-angle[0])>.01,"the real fitting moves after the reader stops moving"))
        .thenWaitUntil(()->{var a=UncannyStories.actor(p,f.own(p,PLACE),"CastVisitor");h.assertTrue(dark(f)&&f.own(p,PLACE).getInt("FoyerStill131")>=2&&a!=null&&a.phase()==2,"actual force and settling reveal the original closer to the glass");})
        .thenExecute(()->{
            var a=UncannyStories.actor(p,f.own(p,PLACE),"CastVisitor");h.assertTrue(a.getUUID().equals(original[0])&&a.phase()==2&&a.getZ()>firstZ[0]+1,"second stillness reveals the same visitor closer to the glass");
            pull(f,p);
        }).thenWaitUntil(()->{var a=UncannyStories.actor(p,f.own(p,PLACE),"CastVisitor");h.assertTrue(dark(f)&&f.own(p,PLACE).getInt("FoyerStill131")>=3&&a!=null&&a.phase()==3,"wind and the optional second pull produce the final stillness");})
        .thenExecute(()->{
            var a=UncannyStories.actor(p,f.own(p,PLACE),"CastVisitor");var other=UncannyStories.actor(peer,f.own(peer,PLACE),"CastVisitor");
            h.assertTrue(a.getUUID().equals(original[0])&&a.phase()==3&&a.getZ()>b.getZ()-15.2&&l.noCollision(a,a.getBoundingBox()),"the original holds one hand toward the glass from a real collision-free footing");
            h.assertTrue(other!=null&&!other.getUUID().equals(a.getUUID())&&other.owner().orElseThrow().equals(peer.getUUID()),"the physical sensor never merges private visitors");
            h.assertTrue(!f.own(p,PLACE).getBoolean("Ready")&&WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"operating a fitting creates no source, item, ending or peer credit");
            h.succeed();
        });
    }
    @AfterBatch(batch="foyer_motion_native") public static void nativeDone(ServerLevel l){close();}

}
