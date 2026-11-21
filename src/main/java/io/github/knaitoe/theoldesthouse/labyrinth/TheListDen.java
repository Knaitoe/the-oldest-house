package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.network.TheListNightPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.720, The list part 5a (docs/THE_LIST_0_4_720.md): night two, as far as the first of Amber's tests. The one who went in on night
 * one comes back to a town whose night one is over, and night two begins round them at ten in their own kitchen.
 * <ol>
 * <li><b>Home</b> ({@link TheListNight#HOME2}): Amber's page said her back door, after ten. Amber is in her den in front of the television.</li>
 * <li><b>The den</b> ({@link TheListNight#DEN}): a hand on her back door and she has it open. Talked to, she humors the one who went in:
 * the tapes have rules, there are always rules; she hands over her notebook with the ones from the tapes already in it, and names the
 * first test.</li>
 * <li><b>To the field</b> ({@link TheListNight#TO_FIELD}): she goes out her front door and down Seventh to the school's field, and waits at
 * the south end zone.</li>
 * <li><b>The field</b> ({@link TheListNight#FIELD}): with the one who went in beside her she counts three and runs the length of the field.
 * If they are still with her at the far end she writes the first rule down; if not, she walks back to try again.</li>
 * <li><b>Ran</b> ({@link TheListNight#RAN}): the rule is written, and the rest of the tests follow (0.4.722, part 5b), each in turn
 * by the rules written so far:
 * <ul>
 * <li><b>The door</b>: she goes in through the field house's back doors and bolts them behind her once the one who went in is outside.
 * The bolted steel takes five blows (TheListBody.strike); when one swings in, the second rule is written.</li>
 * <li><b>The heart</b>: she sends them back out, goes into the coach's office or the showers, both behind the block wall, and bangs a
 * locker. The first of the two rooms they come into is their guess: the right one writes that he knows whose heart it is; the wrong
 * one writes that he cannot tell, which is not so.</li>
 * <li><b>The tower</b>: she walks out to the water tower. Up the ladder to the catwalk and off it: the body lands, goes down and gets up,
 * and she writes that it stays down longer the higher it falls, which is not so either. Climbing back down the ladder does not count.</li>
 * </ul>
 * After the last, the first name goes out (TheListNights.ran).</li>
 * </ol>
 * Amber is one person in the town, heard and seen by everyone in it; the den's talk, the notebook and the tests are the one who went in's.
 * Where she is walking or running is kept per town and never saved: a town picked up again mid-walk finds her at the end zone.
 */
public final class TheListDen{
    private TheListDen(){}
    /** Where Amber is in her den on night two, facing the television. */
    public static final BlockPos DEN_AT=new BlockPos(-8,0,-81);
    /** The field's south end zone, where she waits, and its north end, where she runs to. */
    public static final Vec3 END_S=new Vec3(TheListSite.FIELD_X-.5,0,TheListSite.FIELD_S-3.5),END_N=new Vec3(TheListSite.FIELD_X-.5,0,TheListSite.FIELD_N+3.5);
    /** Her way out of the den, out the front door, down Seventh, through the field's gate and down to the south end zone. The front door is
     * opened as she comes to it and shut behind her once she is in the street. */
    static final Vec3[] TO_FIELD_WAY={new Vec3(-4.5,0,-80.5),new Vec3(-1.5,0,-80.5),new Vec3(.5,0,-80.5),new Vec3(.5,0,-79.5),new Vec3(.5,0,-76.5),
        new Vec3(.5,0,-71.5),new Vec3(-59.5,0,-71.5),new Vec3(-95.5,0,-71.5),new Vec3(-111.5,0,-71.5),END_S};
    static final int DOOR_OPEN_AT=4,DOOR_SHUT_AT=6;
    /** How fast she walks, and runs; how near the one who went in must be to start the run, and to have kept up at its end. */
    static final double WALK=.75,RUN=1.0,START=6,KEPT=8;
    /** Ticks she stands still on her way before she is set on at the next step; ticks between "one", "two" and "three". */
    static final int STUCK=100,COUNT=20;

    /** Per town: where Amber is on her way, how long she has stood still, the count before the run. Never saved. */
    static final class Den{int step;int still;@Nullable Vec3 last;int count=-1;boolean running,back;
        /** 0.4.722: which test she is on, how far along its way, and what it has seen. */
        int test=-1,way,room=-1,stage;boolean there,climbed,fell;@Nullable Vec3[] path;final java.util.Set<BlockPos> bolted=new java.util.HashSet<>();}
    static final Map<Long,Den> DENS=new HashMap<>();
    public static void clearAll(){DENS.clear();}

    /** Whether night two's den has Amber herself: from the walk to the field on, wherever she is. */
    public static boolean holds(net.minecraft.server.MinecraftServer s,BlockPos b,int who){
        if(who!=TheListPeople.AMBER)return false;int phase=TheListNight.phase(s,b);
        return phase>=TheListNight.TO_FIELD&&phase<=TheListNight.RAN;
    }

    // ------------------------------------------------------------------------------------------------ beginning
    /** Whether this reader is owed night two in a town whose night one is over: the one who went in, who has had night one only. */
    static boolean owed(ServerPlayer p,TheListNight.Night n,CompoundTag own){
        return n.phase==TheListNight.DONE&&p.getUUID().equals(n.slasher)&&LiteraryVignettes.participant(p)&&own.getInt("Night")==1;
    }
    /** Night two begun round the one who went in: ten o'clock in their kitchen, Amber in her den. */
    static void begin(ServerPlayer p,TheListNight.Night n){
        var l=p.serverLevel();var b=n.base;
        n.phase=TheListNight.HOME2;n.since=0;n.time=TheListNight.TEN;
        DENS.remove(b.asLong());
        TheListNight.save(p.server,n);
        TheListBody.give(p);
        TheListPeople.ensure(l,b);
        TheList.cue(p,TheListTexts.HOME2);
        TheListNight.send(l,n,true);
    }
    /** For an operator or a check: night two begun round this reader, night one put behind them first. False if they cannot be. */
    public static boolean begin(ServerPlayer p,BlockPos b){
        if(!LiteraryVignettes.participant(p))return false;
        var n=TheListNight.night(p.server,b);
        if(n==null||n.phase==TheListNight.NONE){if(!TheListNight.begin(p,b))return false;n=TheListNight.night(p.server,b);}
        if(n==null)return false;
        if(n.phase<TheListNight.DONE)TheListNight.jump(p.serverLevel(),b,TheListNight.DONE);
        n.slasher=p.getUUID();
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),TheList.PLACE);
        own.putInt("Night",1);LiteraryVignettes.save(d,p.getUUID(),TheList.PLACE,own);
        begin(p,n);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the back door
    /** A hand on Amber's back door on night two: she has it open for the one who went in, and the den begins. True if it was the den's. */
    static boolean door(ServerPlayer p,BlockPos b,BlockPos lower,CompoundTag own){
        if(!lower.equals(TheListSite.AMBER_BACK))return false;
        var n=TheListNight.night(p.server,b);if(n==null||n.phase<TheListNight.HOME2)return false;
        if(!LiteraryVignettes.participant(p))return false;
        var l=p.serverLevel();
        if(n.phase==TheListNight.HOME2){
            if(!p.getUUID().equals(n.slasher)){TheList.cue(p,TheListTexts.AMBER_DOOR);return true;}
            open(l,b,TheListSite.AMBER_BACK,true);
            TheList.cue(p,TheListTexts.BACK_DOOR_OPENS);
            n.phase=TheListNight.DEN;n.since=0;TheListNight.save(p.server,n);
            var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
            if(amber!=null){amber.sayTo(p,TheListTexts.NAMES[TheListPeople.AMBER],TheListTexts.DEN[0]);amber.speaks(TheListTexts.DEN[0]);}
            if(own.getInt("Den2")<1)own.putInt("Den2",1);
            return true;
        }
        // From the den on the back door is open, or opens, to anyone.
        open(l,b,TheListSite.AMBER_BACK,!isOpen(l,b,TheListSite.AMBER_BACK));
        return true;
    }
    static boolean isOpen(ServerLevel l,BlockPos b,BlockPos lower){var st=l.getBlockState(b.offset(lower));return st.getBlock() instanceof DoorBlock&&st.getValue(DoorBlock.OPEN);}
    static void open(ServerLevel l,BlockPos b,BlockPos lower,boolean open){
        var at=b.offset(lower);if(!l.isLoaded(at))return;var st=l.getBlockState(at);
        if(st.getBlock() instanceof DoorBlock d&&st.getValue(DoorBlock.OPEN)!=open)d.setOpen(null,l,st,at,open);
    }

    // ------------------------------------------------------------------------------------------------ Amber, talked to
    /**
     * A word with Amber on night two. The one who went in hears the den through, a line a word: the tapes, the rules, the notebook, the
     * first test; on the way and at the field she says where she is going and what she is waiting for. Anyone else hears her keep it down.
     * True if night two took it.
     */
    static boolean talk(ServerPlayer p,LiteraryActor a,TheListNight.Night n,CompoundTag own){
        if(n.phase<TheListNight.HOME2||TheListPeople.who(a)!=TheListPeople.AMBER)return false;
        var name=TheListTexts.NAMES[TheListPeople.AMBER];
        if(!p.getUUID().equals(n.slasher)){say(a,p,name,TheListTexts.DEN_OTHER);return true;}
        var l=p.serverLevel();var b=n.base;
        switch(n.phase){
            case TheListNight.HOME2,TheListNight.DEN->{
                if(n.phase==TheListNight.HOME2){n.phase=TheListNight.DEN;n.since=0;TheListNight.save(p.server,n);}
                int at=Math.max(1,own.getInt("Den2"));
                if(at>=TheListTexts.DEN.length){say(a,p,name,TheListTexts.DEN[TheListTexts.DEN.length-1]);go(l,n);return true;}
                say(a,p,name,TheListTexts.DEN[at]);
                if(at==NOTEBOOK_AT)notebook(p,own);
                own.putInt("Den2",at+1);
                if(at+1>=TheListTexts.DEN.length)go(l,n);
            }
            case TheListNight.TO_FIELD->say(a,p,name,TheListTexts.ON_THE_WAY);
            case TheListNight.FIELD->say(a,p,name,TheListTexts.AT_THE_END);
            case TheListNight.RAN->say(a,p,name,switch(test(own.getInt("Rules"))){case DOOR->TheListTexts.NEXT_DOOR;case HEART->TheListTexts.HEART_TALK;
                case TOWER->TheListTexts.TOWER_TALK;default->TheListTexts.TESTS_DONE;});
            default->{return false;}
        }
        return true;
    }
    /** Which of her den lines hands the notebook over. */
    static final int NOTEBOOK_AT=2;
    static void say(LiteraryActor a,ServerPlayer p,String name,String line){a.sayTo(p,name,line);a.speaks(line);}

    /** She sets off for the field. */
    static void go(ServerLevel l,TheListNight.Night n){
        n.phase=TheListNight.TO_FIELD;n.since=0;TheListNight.save(l.getServer(),n);
        var d=new Den();DENS.put(n.base.asLong(),d);
        var amber=TheListPeople.find(l,n.base,TheListPeople.AMBER);if(amber!=null)walk(amber,n.base,TO_FIELD_WAY[0],WALK);
    }
    static void walk(LiteraryActor a,BlockPos b,Vec3 rel,double speed){BorrascaCast.walk(a,Vec3.atLowerCornerOf(b).add(rel),speed);}

    // ------------------------------------------------------------------------------------------------ the notebook
    /** Amber's notebook, the one who went in's own: once, and only to them. The rules written so far, kept in their record, are its pages. */
    static void notebook(ServerPlayer p,CompoundTag own){
        if(own.getBoolean("Notebook"))return;
        own.putBoolean("Notebook",true);
        var book=book(p,own.getInt("Rules"));
        if(!p.getInventory().add(book))p.drop(book,false);
        TheList.cue(p,TheListTexts.NOTEBOOK_GIVEN);
    }
    public static final String OWNER="ListNotebook";
    static ItemStack book(ServerPlayer p,int rules){
        var pages=new ArrayList<String>();pages.add(TheListTexts.RULES_TAPES);
        // 0.4.722: two rules to a page, so a page never runs past what a book shows.
        var tested=new StringBuilder(TheListTexts.RULES_TESTED);int on=0;
        for(int i=0;i<TheListTexts.RULES.length;i++){
            if((rules&1<<i)==0)continue;
            if(on==2){pages.add(tested.toString());tested=new StringBuilder();on=0;}
            if(tested.length()>0)tested.append("\n\n");
            tested.append(TheListTexts.RULES[i]);on++;
        }
        if(rules!=0)pages.add(tested.toString());
        var book=HouseWriting.book(TheListTexts.NOTEBOOK_TITLE,TheListTexts.NAMES[TheListPeople.AMBER],HouseWriting.WritingStyle.PLAIN,pages);
        var tag=new CompoundTag();tag.putUUID(OWNER,p.getUUID());
        book.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        return book;
    }
    /** Whether a stack is a notebook of Amber's, and whose. */
    public static boolean isNotebook(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().hasUUID(OWNER);}
    /** Whether a reader holds a notebook of Amber's in either hand: holding it resists the pull (TheListBody.pull). */
    public static boolean holding(ServerPlayer p){return isNotebook(p.getItemInHand(InteractionHand.MAIN_HAND))||isNotebook(p.getItemInHand(InteractionHand.OFF_HAND));}
    /** A rule written: kept in the reader's record, and their notebook (wherever it is on them) rewritten with it. */
    static void write(ServerPlayer p,int rule){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),TheList.PLACE);
        int rules=own.getInt("Rules")|1<<rule;own.putInt("Rules",rules);LiteraryVignettes.save(d,p.getUUID(),TheList.PLACE,own);
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){
            var s=inv.getItem(i);if(!isNotebook(s))continue;
            var t=s.get(DataComponents.CUSTOM_DATA).copyTag();if(!t.getUUID(OWNER).equals(p.getUUID()))continue;
            inv.setItem(i,book(p,rules));
        }
        TheList.cue(p,TheListTexts.RULE_WRITTEN);
    }
    /** The rules written for this reader so far, as bits of TheListTexts.RULES. */
    public static int rules(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),TheList.PLACE).getInt("Rules");}

    // ------------------------------------------------------------------------------------------------ each tick
    /** One tick of night two in a town. True if the night changed and wants saving and sending. */
    static boolean tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        n.time=Math.min(TheListNight.LIGHT,n.time+TheListNight.RATE);
        var b=n.base;
        var amber=TheListPeople.find(l,b,TheListPeople.AMBER);
        switch(n.phase){
            case TheListNight.TO_FIELD->{if(amber!=null)return toField(l,n,amber);}
            case TheListNight.FIELD->{if(amber!=null)return field(l,n,amber,TheListNight.slasher(l,n));}
            case TheListNight.RAN->{if(amber!=null)return tests(l,n,amber,TheListNight.slasher(l,n));}
            default->{}
        }
        return false;
    }
    static Den den(BlockPos b){return DENS.computeIfAbsent(b.asLong(),k->{var d=new Den();d.step=TO_FIELD_WAY.length-1;return d;});}
    /** On her way: a step at a time, the front door opened and shut behind her; set on at the next step if she stands still too long. */
    static boolean toField(ServerLevel l,TheListNight.Night n,LiteraryActor a){
        var b=n.base;var d=den(b);
        var goal=Vec3.atLowerCornerOf(b).add(TO_FIELD_WAY[d.step]);
        if(a.position().distanceToSqr(goal)>.36){
            moving(a,d,goal);
            if(!a.getPersistentData().contains("BorrascaSpeed"))walk(a,b,TO_FIELD_WAY[d.step],WALK);
            return false;
        }
        d.still=0;
        if(d.step==DOOR_OPEN_AT-1)open(l,b,TheListSite.AMBER_FRONT,true);
        if(d.step==DOOR_SHUT_AT)open(l,b,TheListSite.AMBER_FRONT,false);
        if(++d.step<TO_FIELD_WAY.length){walk(a,b,TO_FIELD_WAY[d.step],WALK);return false;}
        // At the end zone: she turns up the field and waits.
        BorrascaCast.stop(a);face(a,Vec3.atLowerCornerOf(b).add(END_N));
        n.phase=TheListNight.FIELD;n.since=0;d.count=-1;d.running=false;d.back=false;
        return true;
    }
    /** Stood still on her way too long (something in the road): set on where the next step is. */
    static void moving(LiteraryActor a,Den d,Vec3 goal){
        if(d.last!=null&&a.position().distanceToSqr(d.last)<.0004){if(++d.still>=STUCK){a.moveTo(goal.x,goal.y,goal.z,a.getYRot(),0);d.still=0;}}
        else d.still=0;
        d.last=a.position();
    }
    static void face(LiteraryActor a,Vec3 at){var to=at.subtract(a.position());float yaw=(float)Math.toDegrees(Math.atan2(-to.x,to.z));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    /**
     * At the field: waiting at the south end zone until the one who went in is beside her, counting three, running the field's length;
     * at the north end, whether they kept up. Walking back to try again, she is set on the way at a walk.
     */
    static boolean field(ServerLevel l,TheListNight.Night n,LiteraryActor a,@Nullable ServerPlayer p){
        var b=n.base;var d=den(b);var name=TheListTexts.NAMES[TheListPeople.AMBER];
        Vec3 south=Vec3.atLowerCornerOf(b).add(END_S),north=Vec3.atLowerCornerOf(b).add(END_N);
        if(d.back){
            if(a.position().distanceToSqr(south)>.36){moving(a,d,south);if(!a.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(a,south,WALK);return false;}
            BorrascaCast.stop(a);face(a,north);d.back=false;d.count=-1;return false;
        }
        if(d.running){
            if(a.position().distanceToSqr(north)>.36){moving(a,d,north);if(!a.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(a,north,RUN);return false;}
            BorrascaCast.stop(a);d.running=false;
            boolean kept=p!=null&&p.position().distanceTo(a.position())<=KEPT;
            if(kept){
                a.say(name,TheListTexts.RAN_KEPT);a.speaks(TheListTexts.RAN_KEPT);
                write(p,0);
                n.phase=TheListNight.RAN;n.since=0;
                return true;
            }
            a.say(name,TheListTexts.RAN_LOST);a.speaks(TheListTexts.RAN_LOST);
            d.back=true;BorrascaCast.walk(a,south,WALK);
            return false;
        }
        if(d.count<0){
            if(p==null||p.position().distanceTo(a.position())>START)return false;
            d.count=0;a.say(name,TheListTexts.COUNT[0]);a.speaks(TheListTexts.COUNT[0]);return false;
        }
        if(++d.count%COUNT!=0)return false;
        int said=d.count/COUNT;
        if(said<TheListTexts.COUNT.length){a.say(name,TheListTexts.COUNT[said]);a.speaks(TheListTexts.COUNT[said]);}
        if(said>=TheListTexts.COUNT.length-1){
            d.running=true;d.last=null;d.still=0;
            l.playSound(null,a.blockPosition(),SoundEvents.PLAYER_SMALL_FALL,SoundSource.NEUTRAL,.4F,1.4F);
            BorrascaCast.walk(a,north,RUN);
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------ 0.4.722, the rest of the tests
    /** The tests after the field, by the rules they write: RULES[1] the door, RULES[2] or [3] the heart (right or wrong), RULES[4] the tower. */
    public static final int DOOR=1,HEART=2,TOWER=3,DONE=0;
    public static final int RULE_DOOR=1,RULE_HEART=2,RULE_NO_HEART=3,RULE_TOWER=4;
    /** Which test is next for the rules written so far. */
    public static int test(int rules){
        if((rules&1<<RULE_DOOR)==0)return DOOR;
        if((rules&(1<<RULE_HEART|1<<RULE_NO_HEART))==0)return HEART;
        if((rules&1<<RULE_TOWER)==0)return TOWER;
        return DONE;
    }
    /** Whether the one who went in has had every test: the first name can go out. */
    static boolean tested(ServerLevel l,TheListNight.Night n){
        if(n.slasher==null)return false;
        return test(LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),n.slasher,TheList.PLACE).getInt("Rules"))==DONE;
    }
    static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    /** The field house's two back doors, where they stand in the town. */
    public static final BlockPos BACK_L=new BlockPos(TheListSite.FIELD_X-1,0,TheListSite.FIELD_HOUSE_S),BACK_R=new BlockPos(TheListSite.FIELD_X,0,TheListSite.FIELD_HOUSE_S);
    /** From the north end zone through the gap in the fence to the field house's back doors, and in; the doors opened at DOOR_AT. */
    static final Vec3[] DOOR_WAY={v(-124.5,-90.5),v(-124.5,-97.5),v(-124.5,-98.6),v(-124.5,-101.5),v(-124.5,-102.5)};
    /** Where she stands behind the bolted doors. */
    public static final Vec3 BEHIND=v(-124.5,-102.5);
    /** Her way from behind the doors into each room, and where she stands there: the coach's office, the showers. */
    static final Vec3[][] ROOM_WAY={{v(-121.5,-102.5),v(-121.5,-105.5),v(-118.5,-105.5)},{v(-121.5,-102.5),v(-121.5,-101.5),v(-117.5,-101.5)}};
    /** Back out of each room to behind the doors. */
    static final Vec3[][] ROOM_OUT={{v(-121.5,-105.5),v(-121.5,-101.5)},{v(-121.5,-101.5)}};
    /** Out of the field house, across the field to its gate, east along Seventh and over the highway and the tracks to the tower's foot. */
    static final Vec3[] TOWER_WAY={v(-124.5,-101.5),v(-124.5,-98.6),v(-124.5,-96.5),v(-98.5,-71.5),v(-94.5,-71.5),v(-60.5,-71.5),v(54.5,-71.5),
        v(60.5,-74.5),v(61.5,-76.5)};
    /** Where she stands under the tower. */
    public static final Vec3 UNDER=v(61.5,-76.5);
    /** Ticks before she bangs the locker once she is in her room. */
    static final int KNOCK_AFTER=40;
    /** Ticks after a rule is written before she starts the next test. */
    static final int ASK_AFTER=40;

    /** Which room of the field house's far side a point in the town is in: 0 the coach's office, 1 the showers, -1 neither. */
    public static int room(Vec3 r){
        if(r.x<=TheListSite.FIELD_X+3.5||r.x>=TheListSite.FIELD_X+12||r.y<-.5||r.y>3)return -1;
        if(r.z>TheListSite.MAIN_FRONT_S+1&&r.z<TheListSite.FIELD_HOUSE_S-3)return 0;
        if(r.z>TheListSite.FIELD_HOUSE_S-3+1&&r.z<TheListSite.FIELD_HOUSE_S)return 1;
        return -1;
    }
    /** Whether a door of the field house is one she has bolted behind her: it opens to nobody's hand, and the body can strike it. */
    public static boolean bolted(BlockPos b,BlockPos lower){var d=DENS.get(b.asLong());return d!=null&&d.bolted.contains(lower);}
    /** Whether the town's slasher, at a point relative to the town, is up on the tower's catwalk. */
    static boolean onCatwalk(Vec3 r){return r.y>=TheListSite.CATWALK_Y-.1&&Math.abs(r.x-TheListSite.TOWER_X-.5)<4.5&&Math.abs(r.z-TheListSite.TOWER_Z-.5)<4.5;}

    /** One tick of the tests after the field. True if the night changed. */
    static boolean tests(ServerLevel l,TheListNight.Night n,LiteraryActor a,@Nullable ServerPlayer p){
        if(p==null)return false;
        var b=n.base;var d=den(b);var name=TheListTexts.NAMES[TheListPeople.AMBER];
        int test=test(rules(p));
        if(test!=d.test){
            if(n.since<ASK_AFTER)return false;
            d.test=test;d.way=0;d.there=false;d.stage=0;d.room=-1;d.climbed=d.fell=false;d.still=0;d.last=null;d.path=null;
            if(test==TOWER){int in=room(a.position().subtract(Vec3.atLowerCornerOf(b)));
                d.path=in<0?TOWER_WAY:java.util.stream.Stream.concat(java.util.Arrays.stream(ROOM_OUT[in]),java.util.Arrays.stream(TOWER_WAY)).toArray(Vec3[]::new);}
            switch(test){
                case DOOR->say(a,name,TheListTexts.DOOR_ASK);
                case HEART->say(a,name,TheListTexts.HEART_ASK);
                case TOWER->say(a,name,TheListTexts.TOWER_ASK);
                default->{}
            }
        }
        var r=p.position().subtract(Vec3.atLowerCornerOf(b));
        switch(test){
            case DOOR->{
                if(!d.there){if(along(l,n,a,d,DOOR_WAY))d.there=true;return false;}
                if(d.stage==0){
                    // She waits for them to be outside before she shuts the doors on them.
                    if(TheListSite.inFieldHouse(r)){if(d.still++%200==0)say(a,name,TheListTexts.DOOR_OUT);return false;}
                    open(l,b,BACK_L,false);open(l,b,BACK_R,false);d.bolted.add(BACK_L);d.bolted.add(BACK_R);d.stage=1;
                    l.playSound(null,b.getX()+BACK_R.getX()+.5,b.getY()+1,b.getZ()+BACK_R.getZ()+.5,SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,1.2F,.8F);
                    TheList.cue(p,TheListTexts.DOOR_BOLTED);
                    return false;
                }
                d.bolted.removeIf(at->isOpen(l,b,at));
                if(d.bolted.size()==2)return false;
                d.bolted.clear();
                say(a,name,TheListTexts.DOOR_DONE);write(p,RULE_DOOR);n.since=0;
                return true;
            }
            case HEART->{
                if(d.stage==0){
                    // Back out first; then she goes in to one of the two rooms.
                    if(TheListSite.inFieldHouse(r)){if(d.still++%200==0)say(a,name,TheListTexts.HEART_OUT);return false;}
                    d.room=l.getRandom().nextInt(2);d.stage=1;d.way=0;d.still=0;d.last=null;
                    return false;
                }
                if(d.stage==1){if(along(l,n,a,d,ROOM_WAY[d.room])){d.stage=2;d.still=0;}return false;}
                if(d.stage==2){
                    if(++d.still<KNOCK_AFTER)return false;
                    l.playSound(null,a.getX(),a.getY()+1,a.getZ(),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,1.4F,.7F);
                    TheList.cue(p,TheListTexts.HEART_KNOCK);d.stage=3;return false;
                }
                int in=room(r);if(in<0)return false;
                boolean right=in==d.room;
                say(a,name,right?TheListTexts.HEART_RIGHT:TheListTexts.HEART_WRONG);
                write(p,right?RULE_HEART:RULE_NO_HEART);n.since=0;
                return true;
            }
            case TOWER->{
                if(!d.there){if(along(l,n,a,d,d.path==null?TOWER_WAY:d.path)){d.there=true;face(a,Vec3.atLowerCornerOf(b).add(TheListSite.TOWER_X+.5,10,TheListSite.TOWER_Z+.5));}return false;}
                boolean down=TheListBody.down(p)>0;
                if(!d.climbed){if(onCatwalk(r)){d.climbed=true;say(a,name,TheListTexts.TOWER_UP);}return false;}
                if(!d.fell){
                    if(down){d.fell=true;return false;}
                    // Off the catwalk and dropping (not on the ladder); landed, it puts the body down whatever it had left.
                    if(!p.onGround()&&!p.onClimbable()&&r.y<TheListSite.CATWALK_Y-1)d.stage=1;
                    if(p.onGround()&&d.stage==1){TheListBody.fall(p);d.fell=true;return false;}
                    if(p.onGround()&&r.y<2){d.climbed=false;say(a,name,TheListTexts.TOWER_LADDER);}
                    return false;
                }
                if(down)return false;
                say(a,name,TheListTexts.TOWER_DONE);write(p,RULE_TOWER);n.since=0;
                return true;
            }
            default->{return false;}
        }
    }
    static void say(LiteraryActor a,String name,String line){a.say(name,line);a.speaks(line);}
    /** Along one of the tests' ways a step at a time, the field house's back doors opened as she comes to them. True once at its end. */
    static boolean along(ServerLevel l,TheListNight.Night n,LiteraryActor a,Den d,Vec3[] way){
        var b=n.base;if(d.way>=way.length)return true;
        var goal=Vec3.atLowerCornerOf(b).add(way[d.way]);
        if(a.position().distanceToSqr(goal)>.36){
            moving(a,d,goal);
            if(!a.getPersistentData().contains("BorrascaSpeed"))walk(a,b,way[d.way],WALK);
            return false;
        }
        d.still=0;
        // At the back doors, from either side, she opens them.
        var at=way[d.way];if(Math.abs(at.x-BEHIND.x)<.1&&at.z>TheListSite.FIELD_HOUSE_S-2.5&&at.z<TheListSite.FIELD_HOUSE_S+3){open(l,b,BACK_L,true);open(l,b,BACK_R,true);}
        if(++d.way<way.length){walk(a,b,way[d.way],WALK);return false;}
        BorrascaCast.stop(a);d.still=0;d.last=null;
        return true;
    }
    /** For a check: Amber set down at the end of the way to this test, as if she had walked it. */
    public static void atTest(ServerLevel l,BlockPos b,int test){
        var a=TheListPeople.find(l,b,TheListPeople.AMBER);if(a==null)return;
        var d=den(b);var way=test==DOOR?DOOR_WAY:test==TOWER?TOWER_WAY:null;if(way==null)return;
        var at=Vec3.atLowerCornerOf(b).add(way[way.length-1]);BorrascaCast.stop(a);a.moveTo(at.x,at.y,at.z,0,0);
        if(way==DOOR_WAY){open(l,b,BACK_L,true);open(l,b,BACK_R,true);}
        d.test=test;d.way=way.length;d.path=way;d.there=false;d.stage=0;d.still=0;d.last=null;d.climbed=d.fell=false;
    }
    /** For a check: night two brought to the tests after the field, the first rule written, the notebook given, Amber at the north end. */
    public static void toTests(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var n=TheListNight.night(p.server,b);if(n==null)return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),TheList.PLACE);
        notebook(p,own);own.putInt("Den2",TheListTexts.DEN.length);LiteraryVignettes.save(d,p.getUUID(),TheList.PLACE,own);
        write(p,0);
        var a=TheListPeople.find(l,b,TheListPeople.AMBER);
        if(a!=null){var at=Vec3.atLowerCornerOf(b).add(END_N);BorrascaCast.stop(a);a.moveTo(at.x,at.y,at.z,0,0);}
        var den=new Den();den.step=TO_FIELD_WAY.length;DENS.put(b.asLong(),den);
        n.phase=TheListNight.RAN;n.since=ASK_AFTER;TheListNight.save(p.server,n);
    }
    /** Which room she went into for the heart test, -1 before she has gone (for a check). */
    public static int heartRoom(BlockPos b){var d=DENS.get(b.asLong());return d==null||d.test!=HEART||d.stage<1?-1:d.room;}
    /** Whether she has banged the locker (for a check). */
    public static boolean knocked(BlockPos b){var d=DENS.get(b.asLong());return d!=null&&d.test==HEART&&d.stage>=3;}
    /** Whether the slasher has been up on the catwalk this test, and has come off it (for a check). */
    public static boolean climbed(BlockPos b){var d=DENS.get(b.asLong());return d!=null&&d.test==TOWER&&d.climbed;}

    /** For a check: Amber set down at the south end zone with the walk there done, as if she had walked it. */
    public static void atField(ServerLevel l,BlockPos b){
        var n=TheListNight.night(l.getServer(),b);if(n==null)return;
        var a=TheListPeople.find(l,b,TheListPeople.AMBER);
        if(a==null){
            for(var x:l.getEntitiesOfClass(LiteraryActor.class,TheListBody.bounds(b),y->TheListPeople.is(y)&&TheListPeople.who(y)==TheListPeople.AMBER))a=x;
        }
        if(a==null)return;
        var at=Vec3.atLowerCornerOf(b).add(END_S);BorrascaCast.stop(a);a.setNoGravity(true);a.moveTo(at.x,at.y,at.z,0,0);
        open(l,b,TheListSite.AMBER_FRONT,false);
        var d=new Den();d.step=TO_FIELD_WAY.length;DENS.put(b.asLong(),d);
        face(a,Vec3.atLowerCornerOf(b).add(END_N));
        n.phase=TheListNight.FIELD;n.since=0;TheListNight.save(l.getServer(),n);
    }
    /** The town's night two put away: Amber's doors shut, her walk forgotten. */
    static void reset(ServerLevel l,BlockPos b){
        DENS.remove(b.asLong());
        open(l,b,BACK_L,false);open(l,b,BACK_R,false);
        open(l,b,TheListSite.AMBER_BACK,false);open(l,b,TheListSite.AMBER_FRONT,false);
    }
    /** Whether Amber is running now (for a check). */
    public static boolean running(BlockPos b){var d=DENS.get(b.asLong());return d!=null&&d.running;}
    /** Sent to a reader coming into a town whose night two is on: its hour. */
    static void hour(ServerPlayer p,TheListNight.Night n){
        PacketDistributor.sendToPlayer(p,new TheListNightPayload((long)n.time,(float)(n.time<TheListNight.LIGHT?TheListNight.RATE:0),0));
    }
}
