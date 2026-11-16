package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPerson.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonSite.*;

/**
 * 0.4.635, Second Person, E3: nobody home (docs/SECOND_PERSON_0_4_635.md; the proposal's ledger row 26). A reader who has frightened
 * the resident far more than they have been gentle finds, at the next cupboard, that the season does not go by: the resident has stopped
 * sleeping, and that night they leave with one bag, the rest of their things where they stand. The book's last page: "They've gone. The
 * flat is yours. Nobody can see you." Nothing moves after that unless the reader moves it.
 */
public final class SecondPersonGone {
    private SecondPersonGone(){}
    /** The season the flat is in once they have gone: compared only by equality, like the others. */
    public static final int GONE=5;
    /** How frightening the year must have been at the least, and by how much worse than gentle. */
    static final int FRIGHT_AT_LEAST=6,FRIGHT_OVER_GENTLE=3;
    /** The night: sitting dressed on the bed, the bag in the hall, the door, the landing. */
    static final int SITTING=160,HALL=140,DOOR=100,LANDING=80;

    public static final String WOKE="You come out of the cupboard into the middle of the night. The season has not gone by. They have stopped sleeping.";
    public static final String SITTING_UP="They are sitting on the edge of the bed, dressed, with one bag.";
    public static final String DONT="Don't. Please don't.",NOTHING_ELSE="I'm not taking anything else. You can have it.",SORRY="I'm sorry. Whoever you are.";
    public static final String STAIRS="The door shuts. Their steps go down the stairs, then the street door, then nothing.";
    public static final String LAST_PAGE="They've gone. The flat is yours. Nobody can see you.";
    public static final String UNLOCKED="They did not lock the door behind them.";

    public static boolean gone(CompoundTag own){return seasonOf(own)==GONE;}
    /** Frightened out: a year far more cold, hidden and watched than kind. */
    public static boolean frightened(CompoundTag own){int f=SecondPersonStory.fright(own),g=SecondPersonStory.gentle(own);return f>=FRIGHT_AT_LEAST&&f>=g+FRIGHT_OVER_GENTLE;}

    /** At the cupboard the season does not go by: they leave instead. */
    static void begin(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own){
        own.putInt("LeftIn",seasonOf(own));own.putInt("Season",GONE);go(own,0);
        lamp(l,f.base,false);open(l,f.base,FLAT_DOOR,false);
        cue(p,WOKE);
        SecondPersonStory.turned(own,"You went into the cupboard, and the season did not go by.");
    }

    static Mark residentMark(CompoundTag own){
        return switch(sub(own)){
            case 0->bedEdge();
            case 1->beside("hall_bag").pose(G_BAG);
            case 2->beside("door").pose(G_BAG);
            case 3->Mark.stand(4.5,UP,-27.5,EAST).pose(G_BAG);
            default->Mark.gone();
        };
    }
    static long hour(CompoundTag own){return 18600;}

    static void night(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a){
        var b=f.base;int sub=sub(own),dt=dt(own);
        switch(sub){
            case 0->{
                if(dt==20)cue(p,SITTING_UP);
                if(dt==90&&inBedroom(r))say(f,a,DONT);
                if(dt>=SITTING)next(own,1);
            }
            case 1->{
                if(dt==30)say(f,a,NOTHING_ELSE);
                if(dt>=HALL&&arrived(a,b,residentMark(own)))next(own,2);
            }
            case 2->{
                if(dt==1){open(l,b,FLAT_DOOR,true);sound(p,SoundEvents.WOODEN_DOOR_OPEN,b,FLAT_DOOR,1,1);}
                if(dt==40)say(f,a,SORRY);
                if(dt>=DOOR)next(own,3);
            }
            case 3->{
                if(dt>=LANDING){open(l,b,FLAT_DOOR,false);sound(p,SoundEvents.WOODEN_DOOR_CLOSE,b,FLAT_DOOR,1,1);cue(p,STAIRS);next(own,4);}
            }
            case 4->{
                if(dt==60){cue(p,LAST_PAGE);SecondPersonStory.turned(own,"They left in the night with one bag.");}
                if(dt>=200){open(l,b,FLAT_DOOR,true);benchBook(l,b,true);
                    LiteraryVignettes.ready(p,SecondPerson.PLACE,own,"nobody_home");cue(p,UNLOCKED);next(own,5);}
            }
            default->{}
        }
    }

    static String page(CompoundTag own){return sub(own)>=4?LAST_PAGE+"\n\n":SITTING_UP+"\n\n";}

    static String season(int s){return switch(s){case SUMMER->"the summer";case AUTUMN->"the autumn";case WINTER->"the winter";default->"the spring";};}
    static String times(int n){return n==1?"once":n==2?"twice":n==3?"three times":"again and again";}

    static void account(StringBuilder b,CompoundTag own){
        b.append("They left in the night, in ").append(season(own.getInt("LeftIn"))).append(", with one bag. The rest of their things are where they stood.");
        String where=own.getString("RoutineWhere");
        if(!where.isEmpty())b.append(" Their ").append(SecondPersonStory.routine(own)).append(" is ").append(where).append(".");
        int hid=SecondPersonStory.list(own,"Hidden").size(),cold=SecondPersonStory.list(own,"Cold").size();
        if(hid>0)b.append(hid==1?" You hid one of their things.":" You hid their things.");
        if(cold>0)b.append(" You made the air go cold round them ").append(times(cold)).append(".");
        if(own.getString("Night").equals("tap"))b.append(" On the first night you ran the tap.");
        if(SecondPersonStory.list(own,"WordsSeen").contains("GO"))b.append(" You wrote GO on the glass.");
        switch(own.getString("Seen")){
            case "gone"->b.append(" When they saw you in the glass, you went.");
            case "touch_afraid"->b.append(" When they saw you in the glass, you put a hand on them, and they backed away to the wall.");
            default->{}
        }
        b.append("\n\nNothing in the flat moved after that, unless you moved it. The flat was yours. Nobody could see you.");
    }
}
