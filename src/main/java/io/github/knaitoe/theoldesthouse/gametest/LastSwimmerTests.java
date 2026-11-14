package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.227, the last swimmer, played with a real reader: the pool built whole and closing; the cover hauled across a lane at a time
 * from the shallow end, the swimmer a lane further over each time the cover reaches theirs, every length the same length; the cover
 * closed and the splashing stopped; the steps pressing the cover up from underneath; the attendant holding the fire door; and walking
 * out through it, which is the ending.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class LastSwimmerTests {
    private LastSwimmerTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.LAST_SWIMMER;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);LastSwimmer.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}LastSwimmer.clearAll();}
    private static void at(LiteraryTests.Fixture f,ServerPlayer p,double x,double z,float yaw){var b=f.base(P);p.teleportTo(f.level(P),b.getX()+x,b.getY(),b.getZ()+z,yaw,0);}
    private static int swimmers(ServerLevel l,BlockPos b){return l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-16,-8,-46)).minmax(new AABB(b.offset(16,11,2))),a->a.role()==LiteraryActor.STRANGER&&a.phase()==LastSwimmer.SWIMMER).size();}
    private static void haul(LiteraryTests.Fixture f,ServerPlayer p){var b=f.base(P);var own=f.own(p,P);LastSwimmer.click(p,b,SwimmerSite.REEL_SOUTH,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);}

    @GameTest(template="empty",batch="swim_close",timeoutTicks=2400)
    public static void theCoverGoesOnOverTheLastSwimmerAndTheAttendantHoldsTheDoor(GameTestHelper h){
        var f=fixture(h);var p=f.player("swim_reader",P);var b=f.base(P);var l=f.level(P);
        var steps=new java.util.ArrayList<BlockPos>();var stages=new java.util.ArrayList<Integer>();var both=new int[1];
        var seq=h.startSequence().thenExecute(()->f.chunks.hold(l,new AABB(b.getX()-16,b.getY()-8,b.getZ()-47,b.getX()+16,b.getY()+11,b.getZ()+2)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the pool is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored(),"a literary pool indoors, one visit, built whole");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, and the quota derives from the pool");
                h.assertTrue(SwimmerBlock.is(l.getBlockState(b.offset(SwimmerSite.CHECKLIST)),SwimmerBlock.Kind.CLIPBOARD)&&SwimmerBlock.is(l.getBlockState(b.offset(SwimmerSite.LOG)),SwimmerBlock.Kind.CLIPBOARD),"the checklist and the closing log on the counter");
                h.assertTrue(SwimmerBlock.is(l.getBlockState(b.offset(0,SwimmerSite.SURFACE,-20)),SwimmerBlock.Kind.ROPE)&&l.getBlockState(b.offset(-8,SwimmerSite.SURFACE,-20)).is(Blocks.WATER)&&SwimmerBlock.is(l.getBlockState(b.offset(SwimmerSite.REEL_X,0,-20)),SwimmerBlock.Kind.REEL),"the water, the lane ropes, the cover on its reel");
                h.assertTrue(l.getBlockState(b.offset(-7,-4,-34)).is(Blocks.WATER)&&l.getBlockState(b.offset(-7,-5,-34)).is(SwimmerRegistry.material("pool_tile"))&&l.getBlockState(b.offset(-7,-3,-15)).is(SwimmerRegistry.material("pool_tile"))&&l.getBlockState(b.offset(-8,-4,-25)).is(SwimmerRegistry.material("pool_line")),"deep at the north end, shallow at the south, a lane line down each lane: "+l.getBlockState(b.offset(-7,-4,-34))+" "+l.getBlockState(b.offset(-7,-5,-34))+" "+l.getBlockState(b.offset(-7,-3,-15))+" "+l.getBlockState(b.offset(-8,-4,-25)));
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("NEVER put the cover on with anyone in the water")&&all.contains("Hold it for whoever helped you"),"the checklist, and what is written under it: "+all);
                // 0.4.581 (the owner: the pool is too dark to see the impressions): the hall's lamps in rows of four, each throwing its
                // light down; and the pool's own lamps under the water, with their light through it.
                var lamps=SwimmerSite.LAMPS;h.assertTrue(lamps.size()==24&&lamps.size()%SwimmerSite.BANK==0,"twenty-four lamps, four to a bank: "+lamps.size());
                for(int i=0;i<lamps.size();i++){var lamp=lamps.get(i);var s=l.getBlockState(b.offset(lamp));
                    h.assertTrue(lamp.getZ()==lamps.get(i-i%SwimmerSite.BANK).getZ()&&(i<SwimmerSite.BANK||lamp.getZ()>lamps.get(i-SwimmerSite.BANK).getZ()),"a bank is a row across the hall, and the banks run from the far end: "+i+" "+lamp);
                    h.assertTrue(SwimmerBlock.is(s,SwimmerBlock.Kind.LAMP)&&s.getValue(SwimmerBlock.STAGE)==0&&l.getBlockState(b.offset(SwimmerSite.thrown(lamp))).is(Blocks.LIGHT),"every lamp lit and throwing its light down: "+i+" "+s+" "+l.getBlockState(b.offset(SwimmerSite.thrown(lamp))));}
                var kept=lamps.subList(lamps.size()-2,lamps.size());
                for(var lamp:kept)h.assertTrue(Math.abs(lamp.getZ()-SwimmerSite.EXIT_Z)<=2&&lamp.getX()<0==SwimmerSite.EXIT_X<0,"the last two are the two by the fire door: "+lamp);
                h.assertTrue(SwimmerSite.POOL_LAMPS.size()==12&&SwimmerSite.POOL_GLOW.size()==SwimmerSite.COLUMNS*(SwimmerSite.POOL_SOUTH-SwimmerSite.POOL_NORTH+1),"the pool's own lamps: "+SwimmerSite.POOL_LAMPS.size()+" "+SwimmerSite.POOL_GLOW.size());
                for(var at:SwimmerSite.POOL_LAMPS){var s=l.getBlockState(b.offset(at));var wet=b.offset(at.getX()<0?at.east():at.west());
                    h.assertTrue(s.is(SwimmerRegistry.material("pool_lamp"))&&s.getLightEmission(l,b.offset(at))==SwimmerRegistry.POOL_LAMP_LIGHT&&l.getFluidState(wet).is(FluidTags.WATER)&&at.getY()<SwimmerSite.SURFACE,"a lamp in the pool's wall, under the water: "+at+" "+s);}
                for(var at:SwimmerSite.POOL_GLOW){var s=l.getBlockState(b.offset(at));
                    h.assertTrue(s.is(Blocks.LIGHT)&&s.getValue(LightBlock.LEVEL)==SwimmerSite.GLOW&&s.getFluidState().is(FluidTags.WATER)&&SwimmerSite.inPool(at)&&at.getY()==SwimmerSite.SURFACE-1,"its light through the water, under every cell of the surface: "+at+" "+s);}
                at(f,p,.5,-10.5,180);})
            // The hall is lit to its far end and over the middle of the water. It was dark in both places (a lamp eight blocks up, and none over the middle lanes).
            .thenWaitUntil(()->{int mid=l.getBrightness(LightLayer.BLOCK,b.offset(0,0,-25)),far=l.getBrightness(LightLayer.BLOCK,b.offset(0,0,-43)),deck=l.getBrightness(LightLayer.BLOCK,b.offset(-12,0,-22));
                h.assertTrue(mid>=8&&far>=8&&deck>=10,"lit over the middle of the pool, at the far end and along the deck: "+mid+" "+far+" "+deck);})
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.tick(b)>5&&swimmers(l,b)==1,"the pool is closing, and someone is still swimming: tick "+LastSwimmer.tick(b)+", swimmers "+swimmers(l,b)+", current "+LiteraryVignettes.current(p)+", base "+LiteraryVignettes.base(p,P)+" vs "+b+", at "+p.position()+", completed "+f.own(p,P).getBoolean("Completed")))
            .thenExecute(()->{h.assertTrue(LastSwimmer.lane(b)==3&&LastSwimmer.LENGTH_TICKS==300,"in lane four, fifteen seconds a length");
                var own=f.own(p,P);at(f,p,.5,-30.5,180);h.assertTrue(LastSwimmer.click(p,b,SwimmerSite.REEL_SOUTH,own)&&LastSwimmer.hauls(b)==0,"the cover is hauled from the shallow end only");at(f,p,.5,-10.5,180);});
        // Nine hauls take the cover over lanes six, five and four's ropes and over lane four: at the next turn they are a lane over.
        for(int i=0;i<9;i++)seq=seq.thenIdle(LastSwimmer.HAUL_GAP+1).thenExecute(()->haul(f,p));
        seq=seq.thenExecute(()->{h.assertTrue(LastSwimmer.hauls(b)==9&&SwimmerBlock.is(l.getBlockState(b.offset(0,SwimmerSite.SURFACE,-20)),SwimmerBlock.Kind.COVER)&&SwimmerBlock.is(l.getBlockState(b.offset(0,SwimmerSite.SURFACE,-20)),SwimmerBlock.Kind.COVER)&&l.getBlockState(b.offset(0,SwimmerSite.SURFACE,-20)).getValue(SwimmerBlock.STAGE)==2&&l.getBlockState(b.offset(-1,SwimmerSite.SURFACE,-20)).is(Blocks.WATER),"nine columns covered, the leading edge on the ninth: "+LastSwimmer.hauls(b));
                // 0.4.403: turned to while the cover goes across, the attendant has a word, and a different one the next time.
                var attendant=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b).inflate(60),a->a.role()==LiteraryActor.STRANGER&&a.phase()==LastSwimmer.ATTENDANT).stream().findFirst().orElse(null);
                var talk=new net.minecraft.nbt.CompoundTag();h.assertTrue(attendant!=null,"the attendant is out on the deck");LastSwimmer.talk(p,attendant,talk);LastSwimmer.talk(p,attendant,talk);
                h.assertTrue(talk.getInt("Talk227")==2&&!SwimmerTexts.attendant(0).equals(SwimmerTexts.attendant(1)),"the attendant answers each time, not with the same words");
                long t=LastSwimmer.tick(b);LastSwimmer.hurry(b,(int)(LastSwimmer.LENGTH_TICKS-t%LastSwimmer.LENGTH_TICKS)-1);})
            .thenIdle(3)
            .thenExecute(()->h.assertTrue(LastSwimmer.lane(b)==2,"at the next turn, the swimmer is a lane further over: "+LastSwimmer.lane(b)));
        for(int i=0;i<8;i++)seq=seq.thenIdle(LastSwimmer.HAUL_GAP+1).thenExecute(()->haul(f,p));
        seq.thenExecute(()->{h.assertTrue(LastSwimmer.closed(b)&&swimmers(l,b)==0,"the cover is on, and the splashing stops");
                for(int x=SwimmerSite.POOL_X0;x<=SwimmerSite.POOL_X1;x++)h.assertTrue(SwimmerBlock.is(l.getBlockState(b.offset(x,SwimmerSite.SURFACE,-25)),SwimmerBlock.Kind.COVER),"every column covered: "+x);
                h.assertTrue(f.own(p,P).getBoolean("Covered227")&&!f.own(p,P).getBoolean("Ready"),"not yet the ending");
                LastSwimmer.hurry(b,LastSwimmer.STEPS_AFTER);})
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.stepping(b)&&LastSwimmer.pressed(b)!=null,"something crosses the cover"))
            .thenExecute(()->{var at=b.offset(LastSwimmer.pressed(b));h.assertTrue(SwimmerBlock.pressed(l.getBlockState(at)),"the cover rises under the step: "+l.getBlockState(at));
                var own=f.own(p,P);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("held_the_door"),"ready, held_the_door: "+own);
                var lamp=b.offset(SwimmerSite.LAMPS.get(0));h.assertTrue(l.getBlockState(lamp).getValue(SwimmerBlock.STAGE)==1,"the far lamps are off");
                // 0.4.581: the lamps go a bank at a time, and by the time anything walks all but the last row are off; a dark lamp throws nothing.
                int out=LastSwimmer.lightsOut(b);h.assertTrue(out>=20&&out%SwimmerSite.BANK<=2&&out<=SwimmerSite.LAMPS.size()-2,"the lamps are off a bank at a time: "+out);
                for(int i=0;i<SwimmerSite.LAMPS.size();i++){var under=l.getBlockState(b.offset(SwimmerSite.thrown(SwimmerSite.LAMPS.get(i))));
                    h.assertTrue(under.is(Blocks.LIGHT)==(i>=out),"a dark lamp throws no light, and a lit one still does: "+i+" of "+out+" "+under);}
                steps.clear();stages.clear();})
            // 0.4.581: each step is a foot, the left and the right by turns, pointing the way it walks, and the one before stays down
            // until this one has landed. Never in the column under the cover's bar.
            .thenExecuteFor(LastSwimmer.STEP_EVERY*5+2,()->{var at=LastSwimmer.pressed(b);if(at==null||!steps.isEmpty()&&steps.getLast().equals(at))return;
                steps.add(at);stages.add(l.getBlockState(b.offset(at)).getValue(SwimmerBlock.STAGE));
                if(steps.size()>1){var before=LastSwimmer.lifted(b);if(before!=null&&before.equals(steps.get(steps.size()-2))&&SwimmerBlock.pressed(l.getBlockState(b.offset(before))))both[0]++;}})
            .thenExecute(()->{h.assertTrue(steps.size()>=4,"it walks on: "+steps);
                Boolean last=null;
                for(int i=0;i<steps.size();i++){var at=steps.get(i);int stage=stages.get(i);
                    h.assertTrue(at.getX()>SwimmerSite.POOL_X0&&at.getX()<=SwimmerSite.POOL_X1&&at.getZ()>=SwimmerSite.POOL_NORTH&&at.getZ()<=SwimmerSite.POOL_SOUTH&&at.getY()==SwimmerSite.SURFACE,"on the cover, and never under its bar: "+at);
                    if(i==0){h.assertTrue(stage==1||stage>=3&&stage<=9,"a foot: "+stage);continue;}
                    var from=steps.get(i-1);var way=Direction.getNearest(at.getX()-from.getX(),0,at.getZ()-from.getZ());
                    h.assertTrue(at.distManhattan(from)==1,"one step at a time: "+from+" to "+at);
                    boolean left=stage==SwimmerBlock.foot(way,false),right=stage==SwimmerBlock.foot(way,true);
                    h.assertTrue(left!=right,"a foot pointing the way it walks ("+way+"): stage "+stage+" at "+at);
                    h.assertTrue(last==null||last!=right,"the left and the right by turns: step "+i+" "+stages);last=right;}
                h.assertTrue(both[0]>=steps.size()-2,"the foot before stays down until the next has landed: "+both[0]+" of "+(steps.size()-1));
                h.assertTrue(SwimmerBlock.foot(Direction.NORTH,false)==1&&SwimmerBlock.foot(Direction.WEST,true)==9&&SwimmerBlock.foot(Direction.SOUTH,false)!=SwimmerBlock.foot(Direction.SOUTH,true),"eight feet, a stage each");
                // With the hall dark, the covered pool is still lit from underneath: the cover, and the air over it where a foot stands.
                int out=LastSwimmer.lightsOut(b);h.assertTrue(out==SwimmerSite.LAMPS.size()-2,"every lamp off but the two by the fire door: "+out);
                for(int i=0;i<SwimmerSite.LAMPS.size();i++){var lamp=SwimmerSite.LAMPS.get(i);
                    h.assertTrue(l.getBlockState(b.offset(lamp)).getValue(SwimmerBlock.STAGE)==(i<out?1:0)&&l.getBlockState(b.offset(SwimmerSite.thrown(lamp))).is(Blocks.LIGHT)==(i>=out),"each lamp off with its light, but the last two: "+i+" "+l.getBlockState(b.offset(lamp))+" "+l.getBlockState(b.offset(SwimmerSite.thrown(lamp))));}
                int strap=l.getBrightness(LightLayer.BLOCK,b.offset(9,0,-12)),door=l.getBrightness(LightLayer.BLOCK,b.offset(-11,0,-12));
                h.assertTrue(strap<=7&&door>=12,"dark at the strap, where the last bank was; lit by the fire door: "+strap+" "+door);
                int low=15,over=15;for(int x=SwimmerSite.POOL_X0;x<=SwimmerSite.POOL_X1;x++)for(int z=SwimmerSite.POOL_NORTH;z<=SwimmerSite.POOL_SOUTH;z++){low=Math.min(low,l.getBrightness(LightLayer.BLOCK,b.offset(x,SwimmerSite.SURFACE,z)));over=Math.min(over,l.getBrightness(LightLayer.BLOCK,b.offset(x,SwimmerSite.SURFACE+1,z)));}
                int corner=l.getBrightness(LightLayer.BLOCK,b.offset(-13,0,-43));
                int high=0;for(int x=SwimmerSite.POOL_X0+2;x<=SwimmerSite.POOL_X1-2;x++)for(int z=SwimmerSite.POOL_NORTH;z<=SwimmerSite.POOL_SOUTH-6;z++)high=Math.max(high,l.getBrightness(LightLayer.BLOCK,b.offset(x,SwimmerSite.SURFACE,z)));
                h.assertTrue(low>=7&&over>=6&&corner<=2,"the cover is lit from underneath in a dark hall: cover "+low+", over it "+over+", the far corner "+corner);
                h.assertTrue(high==low,"and evenly, away from the walls and the two lamps left on: "+low+" to "+high);})
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.held(b),"the attendant reaches the fire door and holds it"))
            .thenExecute(()->{var top=l.getBlockState(b.offset(SwimmerSite.EXIT_X,0,SwimmerSite.EXIT_Z));h.assertTrue(top.getBlock() instanceof net.minecraft.world.level.block.DoorBlock&&top.getValue(net.minecraft.world.level.block.DoorBlock.OPEN),"the fire door stands open: "+top);
                at(f,p,SwimmerSite.EXIT_X+1.5,SwimmerSite.EXIT_Z+.5,90);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"walking out is the ending"))
            .thenExecute(()->{boolean log=false;for(var s:p.getInventory().items)if(s.is(Items.WRITTEN_BOOK)){var c=s.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null&&String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList()).contains("IN AT CLOSE"))log=true;}
                h.assertTrue(log,"the closing log is in the reader's hand");h.assertTrue(f.data().isCompleted(P.id()),"the pool is finished");})
            .thenSucceed();
    }
    @AfterBatch(batch="swim_close") public static void closeDone(ServerLevel l){close();}

    /**
     * 0.4.233: the closing played against "Falling Out the Sky" on the radio (no lyric anywhere): the radio's clock through the song's
     * parts; in the second spoken part the lights waver; when the last verse begins the swimmer holds a breath and nothing is heard;
     * stars on the water go out as the cover reaches them; the crank only turns one way; and under the water at the deep end, voices
     * the reader can't make out, remembered in the account.
     */
    @GameTest(template="empty",batch="swim_radio",timeoutTicks=1200)
    public static void theClosingIsPlayedAgainstTheSongOnTheRadio(GameTestHelper h){
        var f=fixture(h);var p=f.player("swim_listener",P);var b=f.base(P);var l=f.level(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,new AABB(b.getX()-16,b.getY()-8,b.getZ()-47,b.getX()+16,b.getY()+11,b.getZ()+2)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the pool is resident"))
            .thenExecute(()->{
                h.assertTrue(LastSwimmer.part(0)==0&&LastSwimmer.part(LastSwimmer.SONG[LastSwimmer.SECOND_TALK])==LastSwimmer.SECOND_TALK&&LastSwimmer.part(LastSwimmer.SONG[LastSwimmer.LAST_VERSE]+5)==LastSwimmer.LAST_VERSE&&LastSwimmer.part(4400)==5&&LastSwimmer.SONG[5]==4400,"the radio's clock runs through the song's five parts, 3:40 in all");
                h.assertTrue(LastSwimmer.stars(0)==LastSwimmer.STARS.length&&LastSwimmer.stars(6)>0&&LastSwimmer.stars(6)<LastSwimmer.STARS.length&&LastSwimmer.stars(12)==0,"the stars on the water go out as the cover reaches them");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("since last summer"),"the note under the card says since when: "+all);
                at(f,p,.5,-10.5,180);})
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.tick(b)>5&&swimmers(l,b)==1,"the pool is closing: tick "+LastSwimmer.tick(b)+", swimmers "+swimmers(l,b)+", current "+LiteraryVignettes.current(p)+", at "+p.position()))
            .thenExecute(()->{
                var own=f.own(p,P);at(f,p,.5,-11.5,180);p.setShiftKeyDown(true);
                h.assertTrue(LastSwimmer.click(p,b,SwimmerSite.REEL_SOUTH,own)&&LastSwimmer.hauls(b)==0,"the crank won't turn back");p.setShiftKeyDown(false);
                LastSwimmer.hurry(b,LastSwimmer.SONG[LastSwimmer.SECOND_TALK]-(int)(LastSwimmer.tick(b)-1));})
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.part(b)==LastSwimmer.SECOND_TALK,"the radio reaches its second spoken part: "+LastSwimmer.part(b)))
            .thenExecute(()->LastSwimmer.hurry(b,LastSwimmer.SONG[LastSwimmer.LAST_VERSE]-(int)(LastSwimmer.tick(b)-1)))
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.holding(b),"the last verse: the splashing stops"))
            .thenExecute(()->{
                h.assertTrue(f.own(p,P).getBoolean("Breath227"),"the reader in the hall heard it stop");
                var all=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-16,-8,-46)).minmax(new AABB(b.offset(16,11,2))),a->a.role()==LiteraryActor.STRANGER&&a.phase()==LastSwimmer.SWIMMER);
                h.assertTrue(all.size()==1&&all.get(0).isInvisible(),"and nothing comes up: "+all.stream().map(a->a.getUUID().toString().substring(0,8)+(a.isInvisible()?" unseen":" SEEN")+" at "+a.position().subtract(Vec3.atLowerCornerOf(b))).toList()+", tick "+LastSwimmer.tick(b));
                // Under the water at the deep end.
                at(f,p,-6.5,-33.5,0);p.teleportTo(l,p.getX(),b.getY()-4,p.getZ(),0,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Voices227"),"under the water at the deep end, voices: eyes in "+l.getBlockState(BlockPos.containing(p.getEyePosition()))))
            .thenExecute(()->h.assertTrue(SwimmerTexts.afterwards(p.getGameProfile().getName(),f.own(p,P)).contains("voices down there"),"the account remembers the voices"))
            .thenSucceed();
    }
    @AfterBatch(batch="swim_radio") public static void radioDone(ServerLevel l){close();}

    /**
     * 0.4.238: a reader who hauls the cover across as asked, without waiting on the radio, still sees the lights waver once a third of
     * the pool is covered and hears the splashing stop at two thirds, while a lane is open; and a reader who has finished and lingers
     * by the fire door keeps the pool as it is until they have gone.
     */
    @GameTest(template="empty",batch="swim_prompt",timeoutTicks=2400)
    public static void aPromptCoverStillMeetsTheBeatsAndALingeringReaderKeepsThePool(GameTestHelper h){
        var f=fixture(h);var p=f.player("swim_prompt",P);var b=f.base(P);var l=f.level(P);
        var seq=h.startSequence().thenExecute(()->f.chunks.hold(l,new AABB(b.getX()-16,b.getY()-8,b.getZ()-47,b.getX()+16,b.getY()+11,b.getZ()+2)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the pool is resident"))
            .thenExecute(()->at(f,p,.5,-10.5,180))
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.tick(b)>5&&swimmers(l,b)==1,"the pool is closing: tick "+LastSwimmer.tick(b)+", swimmers "+swimmers(l,b)+", at "+p.position()))
            .thenExecute(()->{var heard=LastSwimmer.heard(b);h.assertTrue(heard!=null&&heard.carry()==DoorSounds.Carry.POOL,"through the door, someone swimming: "+heard);});
        for(int i=0;i<LastSwimmer.WAVER_HAULS;i++)seq=seq.thenIdle(LastSwimmer.HAUL_GAP+1).thenExecute(()->haul(f,p));
        seq=seq.thenIdle(2).thenExecute(()->{
                h.assertTrue(LastSwimmer.hauls(b)==LastSwimmer.WAVER_HAULS&&LastSwimmer.part(b)<LastSwimmer.SECOND_TALK,"a third covered, long before the radio's second spoken part: hauls "+LastSwimmer.hauls(b)+", part "+LastSwimmer.part(b));
                h.assertTrue(LastSwimmer.waved(b)&&LastSwimmer.wavering(b)&&!LastSwimmer.holding(b),"the lights waver with the cover, not the song");});
        for(int i=LastSwimmer.WAVER_HAULS;i<LastSwimmer.BREATH_HAULS;i++)seq=seq.thenIdle(LastSwimmer.HAUL_GAP+1).thenExecute(()->haul(f,p));
        seq=seq.thenIdle(2).thenExecute(()->{
                h.assertTrue(LastSwimmer.hauls(b)==LastSwimmer.BREATH_HAULS&&LastSwimmer.part(b)<LastSwimmer.LAST_VERSE&&!LastSwimmer.closed(b),"two thirds covered, a lane still open, before the last verse: part "+LastSwimmer.part(b));
                h.assertTrue(LastSwimmer.holding(b)&&f.own(p,P).getBoolean("Breath227")&&!LastSwimmer.wavering(b),"the splashing stops with the cover, and the lights steady");});
        for(int i=LastSwimmer.BREATH_HAULS;i<SwimmerSite.COLUMNS;i++)seq=seq.thenIdle(LastSwimmer.HAUL_GAP+1).thenExecute(()->haul(f,p));
        seq.thenExecute(()->{h.assertTrue(LastSwimmer.closed(b),"the cover is on");
                var heard=LastSwimmer.heard(b);h.assertTrue(heard!=null&&heard.carry()==DoorSounds.Carry.EMPTY,"through the door, nothing moving now: "+heard);
                LastSwimmer.hurry(b,LastSwimmer.STEPS_AFTER);})
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.held(b),"the attendant holds the fire door"))
            .thenExecute(()->at(f,p,SwimmerSite.EXIT_X+1.5,SwimmerSite.EXIT_Z+.5,90))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"walking out is the ending"))
            // Finished, the reader stays by the door reading the account: the pool is not put back around them.
            .thenIdle(LastSwimmer.EMPTY_RESET+20)
            .thenExecute(()->{
                var door=l.getBlockState(b.offset(SwimmerSite.EXIT_X,0,SwimmerSite.EXIT_Z));
                h.assertTrue(LastSwimmer.tick(b)>=0&&LastSwimmer.held(b)&&SwimmerBlock.is(l.getBlockState(b.offset(0,SwimmerSite.SURFACE,-25)),SwimmerBlock.Kind.COVER)&&door.getValue(net.minecraft.world.level.block.DoorBlock.OPEN),"a finished reader lingering by the door keeps the pool as it is: tick "+LastSwimmer.tick(b)+", door "+door);
                // Gone from the hall, the pool is put back.
                at(f,p,.5,4.5,0);})
            .thenIdle(LastSwimmer.EMPTY_RESET+20)
            .thenExecute(()->h.assertTrue(LastSwimmer.tick(b)==-1&&!SwimmerBlock.is(l.getBlockState(b.offset(0,SwimmerSite.SURFACE,-25)),SwimmerBlock.Kind.COVER),"with nobody left in the hall, the pool is put back: tick "+LastSwimmer.tick(b)))
            .thenSucceed();
    }
    @AfterBatch(batch="swim_prompt") public static void promptDone(ServerLevel l){close();}

    /**
     * 0.4.244, aftermath (docs/AFTERMATH_0_4_244.md): a reader who closed the pool comes back to it as they left it. The cover on from
     * wall to wall, the lamps off but the two by the fire door, nobody swimming and nobody to help; nothing to do, nothing to earn. The
     * dealer offers it back at most twice and never within the reader's last eight crossings.
     */
    @GameTest(template="empty",batch="swim_after",timeoutTicks=600)
    public static void aFinishedPoolComesBackCoveredAndClosed(GameTestHelper h){
        var f=fixture(h);var p=f.player("swim_after",P);var b=f.base(P);var l=f.level(P);
        // Finished before the pool's clock first sees them: they closed it on an earlier visit.
        var own=f.own(p,P);own.putBoolean("Completed",true);own.putBoolean("Ready",true);own.putString("Outcome","held_the_door");LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
        WitnessAccount.resolve(f.data(),p.getUUID(),WitnessAccount.Story.of(P.id()),"held_the_door");int credit=WitnessAccount.count(f.data(),p.getUUID());
        var stranger=java.util.UUID.randomUUID();
        h.startSequence().thenExecute(()->f.chunks.hold(l,new AABB(b.getX()-16,b.getY()-8,b.getZ()-47,b.getX()+16,b.getY()+11,b.getZ()+2)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the pool is resident"))
            .thenExecute(()->at(f,p,.5,-10.5,180))
            .thenWaitUntil(()->h.assertTrue(LastSwimmer.after(b),"the pool stands as the reader left it"))
            .thenExecute(()->{
                for(int x=SwimmerSite.POOL_X0;x<=SwimmerSite.POOL_X1;x++)h.assertTrue(SwimmerBlock.is(l.getBlockState(b.offset(x,SwimmerSite.SURFACE,-25)),SwimmerBlock.Kind.COVER),"the cover on from wall to wall: "+x);
                var lamps=SwimmerSite.LAMPS;
                for(int i=0;i<lamps.size();i++)h.assertTrue(l.getBlockState(b.offset(lamps.get(i))).getValue(SwimmerBlock.STAGE)==(i<lamps.size()-2?1:0),"every lamp off but the two by the fire door: "+i);
                // 0.4.581: a dark lamp throws nothing, and the pool's own lamps are off: the hall is dark but for those two, as the reader is told.
                for(int i=0;i<lamps.size();i++)h.assertTrue(l.getBlockState(b.offset(SwimmerSite.thrown(lamps.get(i)))).is(Blocks.LIGHT)==(i>=lamps.size()-2),"a dark lamp throws no light: "+i);
                for(var at:SwimmerSite.POOL_LAMPS)h.assertTrue(l.getBlockState(b.offset(at)).is(SwimmerRegistry.material("pool_lamp_off")),"the pool's lamps are off: "+at+" "+l.getBlockState(b.offset(at)));
                for(var at:SwimmerSite.POOL_GLOW)h.assertTrue(l.getBlockState(b.offset(at)).is(Blocks.WATER),"and nothing lights the water: "+at+" "+l.getBlockState(b.offset(at)));
                h.assertTrue(SwimmerTexts.AFTERMATH.startsWith("The hall is dark but for the two lamps by the fire door."),"what the reader is told of it");
                h.assertTrue(swimmers(l,b)==0&&l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-16,-8,-46)).minmax(new AABB(b.offset(16,11,2))),a->a.phase()==LastSwimmer.ATTENDANT).isEmpty(),"nobody swimming, and nobody to help");
                h.assertTrue(LastSwimmer.tick(b)==-1&&LastSwimmer.hauls(b)==0,"no closing under way");
                var mine=f.own(p,P);h.assertTrue(mine.getBoolean("Completed")&&"held_the_door".equals(mine.getString("Outcome"))&&WitnessAccount.count(f.data(),p.getUUID())==credit,"nothing new is earned: "+mine);
                h.assertTrue(Aftermath.returns(f.data(),p.getUUID(),P)==1,"the return is counted");
                h.assertTrue(LastSwimmer.click(p,b,new BlockPos(0,SwimmerSite.SURFACE,-25),f.own(p,P))&&LastSwimmer.hauls(b)==0,"the cover is taut, and nothing hauls it");
                var through=LastSwimmer.heard(b,f.data(),p.getUUID());var other=LastSwimmer.heard(b,f.data(),stranger);
                h.assertTrue(through!=null&&through.carry()==DoorSounds.Carry.EMPTY&&other!=null&&other.carry()==DoorSounds.Carry.POOL,"through its door: nothing moving for the reader who closed it, someone swimming for anyone else: "+through+" "+other);})
            .thenIdle(40)
            .thenExecute(()->{
                int far=l.getBrightness(LightLayer.BLOCK,b.offset(0,SwimmerSite.SURFACE,-30)),near=l.getBrightness(LightLayer.BLOCK,b.offset(-4,0,-12)),deep=l.getBrightness(LightLayer.BLOCK,b.offset(12,0,-40));
                h.assertTrue(far==0&&deep==0&&near>=12,"dark but for the two lamps by the fire door: the cover's far end "+far+", the far deck "+deep+", under a lamp "+near);
                h.assertTrue(Aftermath.returns(f.data(),p.getUUID(),P)==1,"one visit counts once");
                // Offered back while the reader has returns left and has not been there in their last eight crossings.
                h.assertTrue(Aftermath.canDeal(f.data(),p.getUUID(),P)&&!Aftermath.canDeal(f.data(),stranger,P),"a finished reader can be offered it back; a reader who never finished it is not");
                var mine=f.own(p,P);mine.putInt(Aftermath.COUNT,Aftermath.RETURNS);LiteraryVignettes.save(f.data(),p.getUUID(),P,mine);
                h.assertTrue(!Aftermath.canDeal(f.data(),p.getUUID(),P),"never more than twice");
                mine.putInt(Aftermath.COUNT,1);LiteraryVignettes.save(f.data(),p.getUUID(),P,mine);f.data().visit(p.getUUID(),P);
                h.assertTrue(!Aftermath.canDeal(f.data(),p.getUUID(),P),"never within the last eight crossings");})
            .thenSucceed();
    }
    @AfterBatch(batch="swim_after") public static void afterDone(ServerLevel l){close();}
}
