package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.211, the stillness, played with real readers: dealt only in the dark band; past halfway it comes in from the far door and
 * goes to what it hears; a still reader stood over, breathed on and pushed, and then left alone to reach the far door; moving
 * under it a goring that a raised shield takes once; and a noise drawing it off a friend.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class StillnessTests {
    private StillnessTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.STILLNESS;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Stillness.clearAll();Stillness.TEST_OVER=130;return active;}
    private static void close(){if(active!=null){active.close();active=null;}Stillness.clearAll();}
    private static net.minecraft.nbt.CompoundTag set(LiteraryTests.Fixture f){return Stillness.set(f.in.getServer());}
    private static int mode(LiteraryTests.Fixture f){return Stillness.mode(set(f));}
    private static void hold(LiteraryTests.Fixture f){var b=f.base(P);f.chunks.hold(f.in,new AABB(b.getX()-8,b.getY()-2,b.getZ()-52,b.getX()+9,b.getY()+6,b.getZ()+3));}
    private static void place(LiteraryTests.Fixture f,ServerPlayer p,double x,double z){var b=f.base(P);p.teleportTo(f.in,b.getX()+x,b.getY(),b.getZ()+z,180,0);}
    private static Vec3 rel(LiteraryTests.Fixture f,ServerPlayer p){return p.position().subtract(Vec3.atLowerCornerOf(f.base(P)));}
    /** Stands the thing a pace from a reader, as if it had just found them by smell. */
    private static void by(LiteraryTests.Fixture f,ServerPlayer p){var r=rel(f,p);Stillness.edit(p.server,t->{t.putDouble("X",r.x);t.putDouble("Z",r.z-1.2);t.remove("HT");});}
    private static boolean target(LiteraryTests.Fixture f,ServerPlayer p){return mode(f)==Stillness.OVER&&set(f).getString("Target").equals(p.getStringUUID());}
    private static boolean printNear(LiteraryTests.Fixture f,ServerPlayer p){var b=f.base(P);var c=p.blockPosition();for(var at:BlockPos.betweenClosed(c.offset(-2,0,-3),c.offset(2,0,2)))if(HorrorBlock.is(f.in.getBlockState(at),HorrorBlock.Kind.HOOF))return true;return false;}

    @GameTest(template="empty",batch="still_deal",timeoutTicks=400)
    public static void dealtOnlyInTheDarkBandWithThePageThePillarsAndTheFarDoor(GameTestHelper h){
        var f=fixture(h);var p=f.player("still_deal",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&DarkHalls.horror(P)&&P.kind()==LabyrinthPlace.Kind.ONE_SHOT,"an interior horror of the dark halls, in one visit");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("survival")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a survival story, and the Witness pool grows by one");
                h.assertTrue(LiteraryRooms.source(P).equals(StillnessSite.LOG)&&LiteraryRooms.ending(P).equals(StillnessSite.TALLY),"the surveyor's page is the source and the count in the dust the account");
                var d=new LabyrinthData();var id=UUID.randomUUID();
                for(int depth=1;depth<=17;depth++){d.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(depth,64,0),0F,true));h.assertTrue(LiteraryVignettes.canDeal(d,id,P)==(depth>=DarkHalls.DARK_DEPTH),"dealt only from depth sixteen (depth "+depth+")");}
                h.assertTrue(f.in.getBlockState(b.offset(-5,1,-24)).isAir()&&f.in.getBlockState(b.offset(5,2,-24)).isAir()&&!f.in.getBlockState(b.offset(0,3,-24)).isAir()&&!f.in.getBlockState(b.offset(6,1,-24)).isAir(),"eleven wide and three high");
                h.assertTrue(!f.in.getBlockState(b.offset(3,1,-6)).isAir()&&!f.in.getBlockState(b.offset(-3,2,-42)).isAir()&&f.in.getBlockState(b.offset(0,1,-6)).isAir(),"square pillars down both sides");
                h.assertTrue(HorrorBlock.is(f.in.getBlockState(b.offset(StillnessSite.LOG)),HorrorBlock.Kind.LOG)&&HorrorBlock.is(f.in.getBlockState(b.offset(StillnessSite.TALLY)),HorrorBlock.Kind.TALLY),"the page by the way in and the scratches before the far door");
                var text=StillnessTexts.source().get(DataComponents.WRITTEN_BOOK_CONTENT);
                var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("It doesn't see. It listens")&&all.contains("Don't."),"the surveyor's page tells a reader what it learned: "+all);
                h.assertTrue(Stillness.mode(set(f))==Stillness.WAITING&&!Stillness.locked(p),"nothing in the hall yet");
            }).thenSucceed();
    }
    @AfterBatch(batch="still_deal") public static void dealDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="still_hunt",timeoutTicks=600)
    public static void pastHalfwayItComesInFromTheFarDoorAndGoesToWhatItHears(GameTestHelper h){
        var f=fixture(h);var p=f.player("still_hunt",P);int[] steps={0};double[] start={0};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->place(f,p,.5,-20.5))
            .thenIdle(5)
            .thenExecute(()->h.assertTrue(mode(f)==Stillness.WAITING,"short of halfway, nothing comes"))
            .thenExecute(()->place(f,p,.5,-26.5))
            .thenWaitUntil(()->h.assertTrue(mode(f)==Stillness.HUNTING,"past halfway it comes in from the far door: "+mode(f)))
            .thenExecute(()->{h.assertTrue(Stillness.locked(p),"while it is in the hall, neither door lets anyone out");
                h.assertTrue(Stillness.at(set(f)).z<-44,"it starts at the far door: "+Stillness.at(set(f)));
                Stillness.edit(p.server,t->{t.putDouble("X",.5);t.putDouble("Z",-34.5);t.remove("HT");});start[0]=-34.5;})
            .thenIdle(3)
            .thenExecute(()->h.assertTrue(Stillness.at(set(f)).z<start[0]+.6,"a still reader is only smelled, slowly: "+Stillness.at(set(f))))
            // walking, a few ticks: heard across the gap
            .thenWaitUntil(()->{if(steps[0]<4){steps[0]++;var r=rel(f,p);place(f,p,r.x+(steps[0]%2==0?.2:-.2),r.z);}
                h.assertTrue(set(f).contains("HT"),"walking is heard seven blocks off");})
            .thenIdle(6)
            .thenExecute(()->h.assertTrue(Stillness.at(set(f)).z>start[0]+1.2,"and it goes to what it heard, fast: "+Stillness.at(set(f))))
            .thenWaitUntil(()->h.assertTrue(target(f,p),"it reaches the reader and stands over them"))
            .thenSucceed();
    }
    @AfterBatch(batch="still_hunt") public static void huntDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="still_over",timeoutTicks=900)
    public static void stillUnderItTheBreathsAreCountedAndItMovesOn(GameTestHelper h){
        var f=fixture(h);var p=f.player("still_over",P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{f.source(p,P);place(f,p,.5,-26.5);})
            .thenWaitUntil(()->h.assertTrue(mode(f)==Stillness.HUNTING,"it comes in"))
            .thenExecute(()->by(f,p))
            .thenWaitUntil(()->h.assertTrue(target(f,p),"it finds the still reader and stands over them"))
            .thenExecute(()->h.assertTrue(printNear(f,p),"its prints are pressed into the dust in front of them"))
            .thenWaitUntil(()->h.assertTrue(mode(f)==Stillness.LEAVING||mode(f)==Stillness.GONE,"standing still, it moves on: "+mode(f)))
            .thenExecute(()->{var own=f.own(p,P);
                h.assertTrue(own.getBoolean("Stood211")&&own.getInt("Breaths211")>=2&&own.getInt("Pushes211")>=1,"its breaths were counted and it pushed them: "+own);
                h.assertTrue(p.isAlive()&&own.getInt("Gored211")==0,"and nothing happened to a reader who did not move");
                Stillness.edit(p.server,t->{t.putDouble("X",StillnessSite.LAIR.x);t.putDouble("Z",StillnessSite.LAIR.z+.2);});})
            .thenWaitUntil(()->h.assertTrue(mode(f)==Stillness.GONE&&!Stillness.locked(p),"the hoofs go through the far door, and the doors let them out"))
            .thenExecute(()->place(f,p,.5,-45.5))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready"),"at the far door the count is theirs"))
            .thenExecute(()->{var own=f.own(p,P);h.assertTrue(own.getString("Outcome").equals("let_it_breathe"),"the outcome: "+own);
                var text=StillnessTexts.accountText(p.getGameProfile().getName(),own);
                h.assertTrue(text.contains(StillnessTexts.capital(Stillness.words(own.getInt("Breaths211"))))&&text.contains("rows of five"),"the account is their own count: "+text);
                h.assertTrue(!text.matches("(?s).*\\b(he|she|his|her|him|hers)\\b.*"),"and never gives the reader a gender");})
            .thenSucceed();
    }
    @AfterBatch(batch="still_over") public static void overDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="still_gore",timeoutTicks=900)
    public static void movingUnderItIsAGoringAndARaisedShieldTakesItOnce(GameTestHelper h){
        var f=fixture(h);var a=f.player("still_shield",P);var c=f.player("still_bare",P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{a.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));place(f,a,-2.5,-26.5);place(f,c,3.5,-12.5);})
            .thenWaitUntil(()->h.assertTrue(mode(f)==Stillness.HUNTING,"it comes in"))
            .thenExecute(()->by(f,a))
            .thenWaitUntil(()->h.assertTrue(target(f,a),"it stands over the first"))
            .thenIdle(Stillness.REACH_GRACE+4)
            .thenExecute(()->{a.startUsingItem(InteractionHand.OFF_HAND);a.invulnerableTime=0;var r=rel(f,a);place(f,a,r.x-1,r.z);})
            .thenWaitUntil(()->h.assertTrue(f.own(a,P).getInt("Gored211")==1,"moving under it is a goring"))
            .thenExecute(()->{h.assertTrue(a.isAlive()&&a.getHealth()<=20-Stillness.BLOCKED+.01F&&a.getHealth()>=20-Stillness.BLOCKED-.01F,"the raised shield takes it, and two hearts get through: "+a.getHealth());
                var shield=a.getOffhandItem();var data=shield.get(DataComponents.CUSTOM_DATA);
                h.assertTrue(shield.is(Items.SHIELD)&&data!=null&&data.copyTag().getInt("MinotaurBlocks")==1,"and the shield is cracked: "+data);
                a.teleportTo(f.in,a.getX()+200,a.getY(),a.getZ(),0,0);})
            .thenIdle(2)
            .thenExecute(()->by(f,c))
            .thenWaitUntil(()->h.assertTrue(target(f,c),"it stands over the second"))
            .thenIdle(Stillness.REACH_GRACE+4)
            .thenExecute(()->{c.invulnerableTime=0;var r=rel(f,c);place(f,c,r.x,r.z+1);})
            .thenWaitUntil(()->h.assertTrue(c.isDeadOrDying(),"with nothing raised, the goring is a death"))
            .thenSucceed();
    }
    @AfterBatch(batch="still_gore") public static void goreDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="still_draw",timeoutTicks=900)
    public static void aNoiseDrawsItOffAFriendWhoMustThenStandStill(GameTestHelper h){
        var f=fixture(h);var a=f.player("still_held",P);var c=f.player("still_draws",P);int[] steps={0};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{place(f,a,-2.5,-26.5);place(f,c,3.5,-31.5);})
            .thenWaitUntil(()->h.assertTrue(mode(f)==Stillness.HUNTING,"it comes in"))
            .thenExecute(()->by(f,a))
            .thenWaitUntil(()->h.assertTrue(target(f,a),"it stands over the first"))
            .thenIdle(Stillness.REACH_GRACE+30)
            .thenWaitUntil(()->{if(steps[0]<4){steps[0]++;var r=rel(f,c);place(f,c,r.x+(steps[0]%2==0?.2:-.2),r.z);}
                h.assertTrue(mode(f)==Stillness.HUNTING&&set(f).getString("HBy").equals(c.getStringUUID()),"a friend's noise draws it off");})
            .thenExecute(()->{var held=f.own(a,P);var drew=f.own(c,P);
                h.assertTrue(held.getBoolean("Stood211")&&held.getString("Drawn211").equals(c.getGameProfile().getName()),"the one it left is spared, and knows who drew it off: "+held);
                h.assertTrue(drew.getString("Drew211").equals(a.getGameProfile().getName()),"and the one who drew it knows whom they drew it off");
                h.assertTrue(a.isAlive()&&held.getInt("Gored211")==0,"nobody was hurt");})
            .thenWaitUntil(()->h.assertTrue(target(f,c),"and it comes for the one who made the noise, who has to stand still"))
            .thenSucceed();
    }
    @AfterBatch(batch="still_draw") public static void drawDone(ServerLevel l){close();}
}
