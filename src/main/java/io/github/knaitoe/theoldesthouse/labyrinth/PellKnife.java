package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PellCutPayload;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.534, the knife (docs/PELL_HOUSE_KNIFE_0_4_534.md), from the proposal's "2. A week in": "The fight: at night the older sibling
 * finds Callum in the kitchen with the knife from the empty slot, and they fight him for it. The cut: a fist lunges, then a sharp cut
 * to black. After the cut: static fills the reader's own view while their breathing, heavy at first, settles as the static clears.
 * Afterwards: the knife is gone, there's a towel on the floor, and June is in the doorway. The next morning, Gail has June's version:
 * the older sibling went for Callum. Never shown: what happened in the black, and no wound on anyone."
 *
 * <ul>
 * <li><b>One o'clock.</b> A week in, once June is back in her room from the line, at one, the first moment nobody sees him or the
 * kitchen, Callum is at the kitchen sink with the knife from his drawer, the light over the sink on and the sliding door standing
 * open. Whoever is asleep in the garage wakes. The hour holds while he is there.</li>
 * <li><b>In the kitchen.</b> A reader who comes in sees him at the sink with his back to the room and the knife in his hand. He
 * looks round at whoever is nearest. A hand on him takes his wrist; a reader who stands close to him a while, he turns on.</li>
 * <li><b>The struggle and the cut.</b> They struggle for it; then his fist comes at the reader's face, and everything goes black, for
 * everyone in the kitchen and anyone who could see in. In the black he is back on his mattress, the knife is gone, a tea towel lies
 * folded on the kitchen floor, and June is in the kitchen's doorway. The black gives way to static that clears, under the reader's
 * own breathing, heavy at first and settling.</li>
 * <li><b>After.</b> June: "Go on back to bed, love." Then she turns the light off, locks the sliding door once nobody is inside
 * ("Out you go, love. I'm locking up."), and goes back to her room. The night goes on.</li>
 * <li><b>Nobody comes.</b> If nobody has come in a minute, or everyone has gone back to sleep, he puts the knife back in his drawer
 * and is out on the line again, the first moment nobody sees, the light off and the door shut and locked as June left it. It is as if
 * it never was.</li>
 * </ul>
 *
 * <p>Whoever was in the kitchen when it went black is who June found there: Gail hears that one of them went for Callum, and each of
 * them goes on Gail's file ("GailMarks"), whatever they say (PellCall). Nobody is hurt.
 */
public final class PellKnife {
    private PellKnife(){}
    /** One in the morning. */
    public static final long ONE=19000;
    /** Where it has got to (Household.knife): not yet; Callum at the sink with it; the struggle; the lunge; the black; after, June in
     *  the doorway; June locking up and going back to her room; over; and over without anybody having come. */
    public static final int NOT_YET=0,AT_SINK=1,STRUGGLE=2,LUNGE=3,BLACK=4,AFTER=5,LOCKING=6,OVER=7,QUIET=8;
    /** How long he stands at the sink for somebody to come; how long a reader may stand close to him before he turns on them; how long
     *  the struggle and the lunge last; how long the black holds and the static takes to clear after it; when June speaks after it. */
    public static final int WAITS=1200,COMES_AT=50,STRUGGLE_FOR=50,LUNGE_FOR=6,BLACK_FOR=60,CLEAR_FOR=120,SPEAKS=40;
    /** How close is close, and how near Callum a reader is still in it with him outside the kitchen. */
    public static final double CLOSE=1.8,WITH=4;
    /** The breaths a reader in the black hears as the static clears: when, from the black's end, how loud and how fast. */
    static final int[] BREATHS={0,26,56,92};
    static final float[] BREATH_VOLUME={1F,.8F,.55F,.35F},BREATH_PITCH={1.25F,1.12F,1F,.92F};
    /** Where Callum stands, at the sink where June washes up, the knife block at his left, facing the window onto the pool. */
    public static final PellPeople.Spot SINK=new PellPeople.Spot(-9.5,-69.6,180);
    /** Where June stands after, in the kitchen's arch from the hall, facing in. */
    public static final PellPeople.Spot ARCH=new PellPeople.Spot(-9.5,-66.6,180);
    /** The light over the sink, and how bright. */
    public static final BlockPos LIGHT=new BlockPos(-10,2,-71);
    static final int BRIGHT=12;
    /** Where the tea towel may lie after: the first of these nobody is standing in. */
    public static final List<BlockPos> TOWELS=List.of(new BlockPos(-10,0,-70),new BlockPos(-9,0,-70),new BlockPos(-8,0,-70),new BlockPos(-9,0,-69));
    /** Callum's drawer: with the knife in it (a week in), and with the space where it lay. */
    static final int DRAWER_KNIFE=1,DRAWER_SPACE=2;
    /** June's way from the arch to the sliding door, and from it back to her room. */
    static final Vec3[] TO_SLIDER={PellMorning.v(-9.5,-65.6),PellMorning.v(0,-65.6),PellMorning.v(0,-68.5),PellMorning.v(.5,-70.6)},
        SLIDER_TO_ROOM={PellMorning.v(0,-68.5),PellMorning.v(0,-65.6),PellMorning.v(8.5,-65.6),PellMorning.v(8.5,-63.4),PellMorning.v(PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z())};
    private static final int C=PellPeople.CALLUM,J=PellPeople.JUNE;

    /** The kitchen's floor, from the house's base. */
    public static boolean inKitchen(Vec3 r){return r.x>=-13&&r.x<-6&&r.z>=-71&&r.z<-67&&r.y>-.5&&r.y<3;}
    /** For tests: where the knife has got to at this house. */
    public static int stage(BlockPos b){return PellPeople.household(b).knife;}
    /** For tests: who June found in the kitchen. */
    public static java.util.Set<UUID> found(BlockPos b){return java.util.Collections.unmodifiableSet(PellPeople.household(b).found);}
    /** Whether the knife has the house and the hour holds: from him at the sink until June is back in her room, unless everyone is asleep. */
    static boolean holds(PellPeople.Household h){return h.visit>=2&&h.knife>=AT_SINK&&h.knife<OVER&&!h.hurried;}
    /** Whether June is up after it, on her way to lock up. */
    public static boolean after(BlockPos b){var h=PellPeople.household(b);return h.knife==AFTER||h.knife==LOCKING;}

    /** For tests: the night as it is just before one, a week in: the small hours, the house as a week has it. */
    public static void beforeOne(ServerLevel l,BlockPos b){
        PellMorning.smallHours(l,b);var h=PellPeople.household(b);h.time=ONE-1;h.knife=NOT_YET;
    }
    /** For tests: a night a week in on which nobody came to the kitchen, as though it was over before it began. */
    public static void skip(BlockPos b){PellPeople.household(b).knife=QUIET;}
    /**
     * For tests: the night as the knife leaves it, these readers found in the kitchen by June: each kept that they were in the black
     * and is on Gail's file; Callum asleep on his mattress with nothing in his hand, his drawer without its knife; the tea towel on
     * the kitchen floor; the light off and the sliding door locked; June in her room.
     */
    public static void foundThere(ServerLevel l,BlockPos b,ServerPlayer... readers){
        var h=PellPeople.household(b);h.knife=OVER;h.found.clear();
        for(var p:readers){
            h.found.add(p.getUUID());var own=PellHouse.own(p);own.putBoolean("InTheBlack",true);
            if(!own.getBoolean("MarkKnife")){own.putBoolean("MarkKnife",true);own.putInt("GailMarks",own.getInt("GailMarks")+1);}
            PellHouse.save(p,own);
        }
        var c=PellPeople.find(l,b,C);
        if(c!=null){
            c.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);c.setXRot(0);c.setYRot(0);c.setYBodyRot(0);c.setYHeadRot(0);
            h.callumOut=false;h.copied=0;h.copyOf=null;PellVisit.lie(l,b,h,C,c,PellVisit.CALLUM_SLEEPS);
        }
        drawer(l,b,DRAWER_SPACE);light(l,b,false);towel(l,b,null,true);
    }

    /** The knife undone, for the visit beginning again: the light off, no towel on the kitchen floor, nothing in his hand. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        h.knife=NOT_YET;h.knifeAt=0;h.knifeSaid=0;h.fighter=null;h.closeSince=0;h.found.clear();
        light(l,b,false);towel(l,b,null,false);
        var c=PellPeople.find(l,b,C);if(c!=null&&c.getMainHandItem().is(PellRegistry.KNIFE.get()))c.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
    }
    /** At six, June on her way to lay breakfast: the towel picked up and the light off. */
    static void tidy(ServerLevel l,BlockPos b){light(l,b,false);towel(l,b,null,false);}

    // ------------------------------------------------------------------------------------------------ the night
    /**
     * Every tick of the small hours a week in (PellMorning.tick asks first). True while the knife has the house: the swim and the rest
     * of the night wait for it.
     */
    static boolean tick(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.visit<2||h.knife>=OVER)return false;
        switch(h.knife){
            case NOT_YET->{if(h.time>=ONE&&h.time<PellMorning.DAWN)begin(l,b,h,here,now);}
            case AT_SINK->atSink(l,b,h,here,now);
            case STRUGGLE->struggle(l,b,h,here,now);
            case LUNGE->{if(now-h.knifeAt>=LUNGE_FOR)cut(l,b,h,june,here,now);}
            case BLACK->{if(now-h.knifeAt>=BLACK_FOR){h.knife=AFTER;h.knifeAt=now;h.knifeSaid=0;after(l,b,h,june,here,now);}}
            case AFTER->after(l,b,h,june,here,now);
            case LOCKING->locking(l,b,h,june,here,now);
            default->{}
        }
        seen(l,b,h,here,now);
        return h.knife>=AT_SINK&&h.knife<OVER;
    }
    /** One o'clock: the first moment nobody sees him or the kitchen, he is at the sink with the knife from his drawer. */
    private static void begin(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        var c=PellPeople.find(l,b,C);if(c==null)return;
        // Nobody sees him, the sink, his drawer or the sliding door.
        if(PellNight.seen(l,c.getEyePosition())||PellNight.seen(l,PellPeople.world(b,SINK).add(0,1,0))||PellNight.seen(l,Vec3.atCenterOf(b.offset(PellWeek.CUTLERY)))
            ||PellNight.seen(l,Vec3.atCenterOf(b.offset(PellSite.SLIDER))))return;
        h.callumOut=false;h.copied=0;h.copyOf=null;h.asleep[C]=false;c.setXRot(0);c.gesture(0);
        PellVisit.stand(l,b,h,C,SINK);
        c.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.KNIFE.get()));
        drawer(l,b,DRAWER_SPACE);light(l,b,true);
        // The sliding door stands open, unlocked.
        PellVisit.unlock(b,PellSite.SLIDER);if(!PellSite.open(l,b,PellSite.SLIDER))PellHouse.toggle(l,b,PellSite.SLIDER);
        h.knife=AT_SINK;h.knifeAt=now;h.knifeSaid=0;h.fighter=null;h.closeSince=0;h.found.clear();
        PellMorning.wakeAll(l,b,h,here,PellTexts.KNIFE_WOKEN,"WokenByTheKitchen");
        sound(l,b,PellPeople.world(b,SINK),SoundEvents.STONE_BUTTON_CLICK_ON,.4F,1.4F);
    }
    /**
     * At the sink. A reader who comes into the kitchen sees him; he looks round at whoever is nearest, and turns on a reader who stands
     * close to him a while. If nobody has come in a minute, or everyone is asleep, he goes back out as if he had never been in.
     */
    private static void atSink(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        var c=PellPeople.find(l,b,C);if(c==null)return;
        ServerPlayer nearest=null;double best=Double.MAX_VALUE;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.isSleeping()||!inKitchen(rel(b,p.position())))continue;
            var own=PellHouse.own(p);
            if(!own.getBoolean("SawKnifeAtSink")){own.putBoolean("SawKnifeAtSink",true);own.putBoolean("SawKnife",true);PellHouse.save(p,own);PellHouse.cue(p,PellTexts.KNIFE_SEEN);}
            double d=p.distanceToSqr(c);if(d<best){best=d;nearest=p;}
        }
        if(nearest!=null&&now-h.knifeAt>=20){
            face(c,nearest);h.rest[C]=c.getYRot();
            if(best<CLOSE*CLOSE){if(h.closeSince==0)h.closeSince=now;else if(now-h.closeSince>=COMES_AT){fight(l,b,h,nearest,null,now);return;}}
            else h.closeSince=0;
        }else{h.closeSince=0;h.rest[C]=SINK.look();}
        if(h.hurried||now-h.knifeAt>=WAITS&&!PellVisit.anyoneInside(b,here))quietly(l,b,h);
    }
    /** Nobody came: the knife back in his drawer and him out on the line again, the first moment nobody sees; the light off; the
     *  sliding door shut and locked as June left it. */
    private static void quietly(ServerLevel l,BlockPos b,PellPeople.Household h){
        var c=PellPeople.find(l,b,C);if(c==null)return;
        var line=PellPeople.world(b,PellNight.CALLUM_LINE);
        if(PellNight.seen(l,c.getEyePosition())||PellNight.seen(l,line.add(0,1,0))||PellNight.seen(l,Vec3.atCenterOf(b.offset(PellWeek.CUTLERY)))
            ||PellNight.seen(l,Vec3.atCenterOf(b.offset(PellSite.SLIDER))))return;
        c.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);drawer(l,b,DRAWER_KNIFE);light(l,b,false);
        float body=PellNight.CALLUM_LINE.look();
        c.moveTo(line.x,line.y,line.z,body,0);c.setYHeadRot(body);c.setYBodyRot(body);c.gesture(0);
        h.way[C].clear();h.rest[C]=body;h.callumOut=true;h.copied=0;h.copyOf=null;
        if(PellSite.open(l,b,PellSite.SLIDER))PellHouse.toggle(l,b,PellSite.SLIDER);
        h.locked.add(PellSite.SLIDER);
        h.knife=QUIET;
    }
    /** The struggle begins: a reader has hold of his wrist, or stood too close and he has turned on them. */
    private static void fight(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,@Nullable CompoundTag own,long now){
        var c=PellPeople.find(l,b,C);if(c==null)return;
        h.knife=STRUGGLE;h.knifeAt=now;h.fighter=p.getUUID();
        c.gesture(PellPeople.STRUGGLE);face(c,p);h.rest[C]=c.getYRot();
        if(own!=null)own.putBoolean("FoughtCallum",true);
        else{var mine=PellHouse.own(p);mine.putBoolean("FoughtCallum",true);PellHouse.save(p,mine);}
        scuffle(l,b,c,now);
    }
    /** The struggle: him turned to whoever has hold of him, the two of them pulling; then the lunge. */
    private static void struggle(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        var c=PellPeople.find(l,b,C);if(c==null)return;
        var p=h.fighter==null?null:l.getServer().getPlayerList().getPlayer(h.fighter);
        if(p!=null){face(c,p);h.rest[C]=c.getYRot();}
        if((now-h.knifeAt)%8==0)scuffle(l,b,c,now);
        if(now-h.knifeAt<STRUGGLE_FOR)return;
        // His fist comes at their face.
        h.knife=LUNGE;h.knifeAt=now;c.gesture(PellPeople.LUNGE);
        sound(l,b,c.position(),SoundEvents.PLAYER_ATTACK_SWEEP,.7F,1.4F);
    }
    /**
     * The cut to black, for everyone in the kitchen, near enough to be in it with him, or able to see in. Those in the kitchen or with
     * him are who June finds there, and go on Gail's file. In the black he is back on his mattress; the knife is gone; a tea towel lies
     * on the kitchen floor; June is in the doorway.
     */
    private static void cut(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        var c=PellPeople.find(l,b,C);if(c==null)return;
        var at=c.position().add(0,1,0);
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            boolean with=inKitchen(rel(b,p.position()))||p.distanceToSqr(c)<WITH*WITH;
            if(!with&&(p.isSleeping()||!PellNight.sees(p,at,PellNight.SEE)))continue;
            HousePackets.send(p,new PellCutPayload(BLACK_FOR,CLEAR_FOR));
            var own=PellHouse.own(p);
            if(with){
                h.found.add(p.getUUID());own.putBoolean("InTheBlack",true);
                // On Gail's file, once.
                if(!own.getBoolean("MarkKnife")){own.putBoolean("MarkKnife",true);own.putInt("GailMarks",own.getInt("GailMarks")+1);}
            }else own.putBoolean("SawTheBlack",true);
            PellHouse.save(p,own);
        }
        c.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);c.setXRot(0);c.setYRot(0);c.setYBodyRot(0);c.setYHeadRot(0);
        PellVisit.lie(l,b,h,C,c,PellVisit.CALLUM_SLEEPS);
        towel(l,b,here,true);
        if(!PellSite.open(l,b,PellSite.JUNE_DOOR))PellHouse.toggle(l,b,PellSite.JUNE_DOOR);
        june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);june.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);june.gesture(0);
        PellVisit.stand(l,b,h,J,ARCH);
        h.knife=BLACK;h.knifeAt=now;
    }
    /** After the black: each reader who was in it breathing, heavy and settling; then June, from the doorway, sends them back to bed;
     *  once the static has cleared she goes to lock the sliding door. */
    private static void after(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        long t=now-h.knifeAt;
        for(int i=0;i<BREATHS.length;i++)if(t==BREATHS[i])for(var id:h.found){
            var p=l.getServer().getPlayerList().getPlayer(id);
            if(p!=null&&p.isAlive())p.playNotifySound(NovelRegistry.WELL_BREATH.get(),SoundSource.PLAYERS,BREATH_VOLUME[i],BREATH_PITCH[i]);
        }
        if(t<SPEAKS||!PellVisit.ready(h,now,june))return;
        if(h.knifeSaid==0){
            ServerPlayer to=null;double best=Double.MAX_VALUE;
            for(var p:here){if(!p.isAlive()||p.isSpectator())continue;double d=p.distanceToSqr(june);if(d<best){best=d;to=p;}}
            if(to!=null){face(june,to);h.rest[J]=june.getYRot();}
            PellVisit.say(h,june,J,PellTexts.JUNE_KNIFE_BED,now);PellVisit.present(june,"HeardJuneAfterKnife",here);
            h.knifeSaid=1;h.nudgedAt=now;return;
        }
        if(t<CLEAR_FOR)return;
        // The light off as she leaves the kitchen, and to the sliding door.
        light(l,b,false);
        h.knife=LOCKING;h.knifeAt=now;h.knifeWay=0;PellVisit.go(h,J,-1,180,TO_SLIDER);
    }
    /** At the sliding door: shut and locked once nobody is inside the house (she asks them out); then back to her room, her door shut. */
    private static void locking(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(!h.way[J].isEmpty())return;
        switch(h.knifeWay){
            case 0->{
                h.rest[J]=180;
                if(!PellNight.shut(l,b,h,june,PellSite.SLIDER,here,now,PellTexts.JUNE_KNIFE_OUT))return;
                PellVisit.go(h,J,-1,PellVisit.HER_ROOM.look(),SLIDER_TO_ROOM);h.knifeWay=1;
            }
            default->{
                if(PellSite.open(l,b,PellSite.JUNE_DOOR)){if(!PellVisit.clearOf(b,PellSite.JUNE_DOOR,here))return;PellHouse.toggle(l,b,PellSite.JUNE_DOOR);}
                h.knife=OVER;
            }
        }
    }
    /** A reader awake who sees Callum with the knife, in the kitchen or through its window, keeps it. */
    private static void seen(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(h.knife<AT_SINK||h.knife>LUNGE||now%5!=0)return;
        var c=PellPeople.find(l,b,C);if(c==null)return;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.isSleeping()||!PellNight.sees(p,c.position().add(0,1,0),PellNight.SEE))continue;
            var own=PellHouse.own(p);if(!own.getBoolean("SawKnife")){own.putBoolean("SawKnife",true);PellHouse.save(p,own);}
        }
    }

    // ------------------------------------------------------------------------------------------------ hands
    /** A hand on Callum while he has the knife: the struggle begins, or goes on. True if the knife answered the hand. */
    static boolean hand(ServerPlayer p,BlockPos b,PellPeople.Household h,CompoundTag own){
        var l=p.serverLevel();
        switch(h.knife){
            case AT_SINK->{fight(l,b,h,p,own,l.getGameTime());PellHouse.cue(p,PellTexts.KNIFE_HOLD);return true;}
            case STRUGGLE,LUNGE->{var c=PellPeople.find(l,b,C);if(c!=null)scuffle(l,b,c,l.getGameTime());PellHouse.cue(p,PellTexts.KNIFE_WRENCH);return true;}
            default->{return false;}
        }
    }

    // ------------------------------------------------------------------------------------------------ the kitchen
    /** The light over the sink, on or off. */
    static void light(ServerLevel l,BlockPos b,boolean on){
        var at=b.offset(LIGHT);var s=l.getBlockState(at);
        if(on&&s.isAir())l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,BRIGHT),3);
        else if(!on&&s.is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
    }
    /** For tests: whether the light over the sink is on. */
    public static boolean lit(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(LIGHT)).is(Blocks.LIGHT);}
    /** The tea towel on the kitchen floor: laid in the first of its cells nobody stands in, or taken up from wherever it lies. */
    static void towel(ServerLevel l,BlockPos b,@Nullable List<ServerPlayer> here,boolean on){
        if(!on){
            for(var cell:TOWELS){var at=b.offset(cell);if(PellThing.is(l.getBlockState(at),PellThing.Kind.TOWEL))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);}
            return;
        }
        for(var cell:TOWELS){
            var at=b.offset(cell);if(!l.getBlockState(at).isAir())continue;
            boolean stood=false;
            if(here!=null)for(var p:here)if(p.isAlive()&&p.getBoundingBox().intersects(new net.minecraft.world.phys.AABB(at)))stood=true;
            if(stood)continue;
            l.setBlock(at,PellThing.of(PellThing.Kind.TOWEL,Direction.NORTH,1),3);return;
        }
    }
    /** For tests: where the tea towel lies on the kitchen floor, if it does. */
    public static @Nullable BlockPos towelAt(ServerLevel l,BlockPos b){
        for(var cell:TOWELS)if(PellThing.is(l.getBlockState(b.offset(cell)),PellThing.Kind.TOWEL))return cell;
        return null;
    }
    /** Callum's drawer: the knife in it, or the space where it lay. */
    static void drawer(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(PellWeek.CUTLERY);var s=l.getBlockState(at);
        if(PellThing.is(s,PellThing.Kind.CUTLERY)&&s.getValue(PellThing.STAGE)!=stage)l.setBlock(at,s.setValue(PellThing.STAGE,stage),3);
    }

    // ------------------------------------------------------------------------------------------------ helpers
    private static Vec3 rel(BlockPos b,Vec3 at){return at.subtract(b.getX(),b.getY(),b.getZ());}
    /** One of them turned to face a reader. */
    private static void face(LiteraryActor a,ServerPlayer p){
        var d=p.position().subtract(a.position());
        float yaw=(float)(Math.atan2(-d.x,d.z)*180/Math.PI);
        a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
    }
    /** The two of them pulling at it: a scuffle of feet and cloth, a chair knocked. */
    private static void scuffle(ServerLevel l,BlockPos b,LiteraryActor c,long now){
        SoundEvent[] scuffles={SoundEvents.WOOL_STEP,SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundEvents.WOOD_HIT,SoundEvents.PLAYER_ATTACK_NODAMAGE};
        sound(l,b,c.position(),scuffles[(int)(now/8%scuffles.length)],.6F,.9F+(now%5)*.05F);
    }
    private static void sound(ServerLevel l,BlockPos b,Vec3 at,SoundEvent s,float volume,float pitch){l.playSound(null,at.x,at.y+1,at.z,s,SoundSource.NEUTRAL,volume,pitch);}
}
