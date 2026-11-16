package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPerson.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonSite.*;

/**
 * 0.4.637, Second Person: the aftermath (docs/SECOND_PERSON_0_4_637.md; the proposal's "Aftermath"). A reader who has finished the year
 * can be dealt the flat again ({@link Aftermath}), and finds it as their ending left it, with nothing to do and nothing to earn:
 * <ul>
 * <li>the door on the latch: their life going on in the armchair, not looking up; or, if they went, an empty flat and a card on the mat
 *     with a forwarding address;</li>
 * <li>their name on the post: night, and the resident at the window, looking down at the street;</li>
 * <li>nobody home: dust, and their things where they were last put;</li>
 * <li>two cups: the evening, the resident at the kitchen table, and the second cup out.</li>
 * </ul>
 * The flat is the reader's own copy, so nothing here needs solitude. Nothing in it answers a hand, except that in the empty flat of
 * "nobody home" the reader can still move things, as they always could.
 */
public final class SecondPersonAfter {
    private SecondPersonAfter(){}
    public static final String LATCH="the_door_on_the_latch",POST="their_name_on_the_post",NOBODY="nobody_home",CUPS="two_cups";
    /** Set on each arrival: whether this visit is the aftermath (the year finished on an earlier visit). */
    public static final String KEY="After637";
    /** The forwarding card, on the mat where the post comes: the application's fourth face. */
    public static final int FORWARDING=3;

    public static final String LIFE="They are in the armchair, as they are most evenings now. They do not look up.";
    public static final String MOVED="The flat is empty. On the mat there is a card: please forward, and an address a long way from here.";
    public static final String WINDOW="Somebody is standing at the window of the flat, looking down at the street.";
    public static final String DUST="Dust on everything.";
    public static final String SECOND_CUP="Two cups on the kitchen table. The second one is out, the way it always is now.";

    public static boolean after(CompoundTag own){return own.getBoolean(KEY);}
    public static String outcome(CompoundTag own){return own.getString("Outcome");}
    static boolean stayed(CompoundTag own){return own.getString("Decision").equals("stay");}
    /** A new visit: the aftermath if the reader finished the year before it began. */
    static void arrive(CompoundTag own){own.putBoolean(KEY,Aftermath.finished(own));own.putInt("AfterT",0);}

    public static long hour(CompoundTag own){return switch(outcome(own)){
        case LATCH->stayed(own)?13400:6000;
        case POST->18000;
        case NOBODY->4500;
        default->13200;};}

    static Mark residentMark(CompoundTag own){return switch(outcome(own)){
        case LATCH->stayed(own)?seat(ARMCHAIR,WEST):Mark.gone();
        case POST->beside("window");
        case CUPS->seat(RESIDENT_CHAIR,EAST);
        default->Mark.gone();};}

    /** The quiet line for this ending, from the record only. */
    public static String line(CompoundTag own){return switch(outcome(own)){
        case LATCH->stayed(own)?LIFE:MOVED;
        case POST->WINDOW;
        case NOBODY->{String where=own.getString("RoutineWhere");
            yield where.isEmpty()?DUST+" Their things are where they were left.":DUST+" Their "+SecondPersonStory.routine(own)+" is "+where+".";}
        default->SECOND_CUP;};}

    /** Nothing in the flat answers a hand, except in the empty flat of nobody home, where things can still be moved. */
    public static boolean inert(CompoundTag own){return after(own)&&!outcome(own).equals(NOBODY);}

    static void visit(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=f.base;int t=own.getInt("AfterT")+1;own.putInt("AfterT",t);
        var resident=stage(l,f,p,f.resident,residentMark(own),at(5.5,UP,-24.5),LiteraryActor.SP_RESIDENT,0,id->f.resident=id);
        stage(l,f,p,f.iris,Mark.gone(),null,LiteraryActor.STRANGER,0,id->f.iris=id);
        stage(l,f,p,f.sam,Mark.gone(),null,LiteraryActor.STRANGER,0,id->f.sam=id);
        stage(l,f,p,f.agent,Mark.gone(),null,LiteraryActor.STRANGER,0,id->f.agent=id);
        if(resident!=null&&!resident.getMainHandItem().isEmpty())resident.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,net.minecraft.world.item.ItemStack.EMPTY);
        String o=outcome(own);
        if(t==1){
            benchBook(l,b,false);open(l,b,FLAT_DOOR,true);
            lamp(l,b,o.equals(CUPS)||o.equals(LATCH)&&stayed(own));
            if(o.equals(CUPS))SecondPersonSite.set(l,b,SecondPersonCups.CUPS_AT,SecondPersonBlock.of(SecondPersonBlock.Kind.CUPS,Direction.NORTH,0));
            if(o.equals(LATCH)&&!stayed(own))SecondPersonSite.set(l,b,MAT,SecondPersonBlock.of(SecondPersonBlock.Kind.APPLICATION,Direction.SOUTH,FORWARDING));
        }
        if(t==40&&Aftermath.count(own))cue(p,line(own));
        // The empty flat's dust, seen only by this reader, in the light from the windows.
        if(o.equals(NOBODY)&&t%8==0&&inFlat(r))l.sendParticles(p,ParticleTypes.WHITE_ASH,true,p.getX(),p.getY()+1.2,p.getZ(),3,2.5,1,2.5,.002);
    }
}
