package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

/** The cousins first question a real disturbance, then become afraid. Their words yield to the outside voice. */
public final class GoatmanFear {
    public static final int BRACE=1,WATCH=2,PACE=3,COMFORT=4,TREMBLE=5,EARS=6,STARTLE=7;
    public static final int CONFUSION=1,FEAR=2,TERROR=3,LINE_TICKS=100;
    private static final String[][] WORDS={
        {"Near the door: Was somebody going back outside?","At the latch: It's pushing back. Can somebody help me?","At the latch: No. No, we're not opening it."},
        {"By the window: Who was that going past?","By the window: It was here. Now it's on the other side.","By the window: Don't make me look again."},
        {"In the aisle: Wait. Is everyone here?","In the aisle: I can't keep standing still.","In the aisle: Which side is it on now?"},
        {"A low voice: Did you see who went out?","A low voice: I'm here. Keep looking at me.","A low voice: Don't listen to it. Listen to me."},
        {"At the table: Didn't he already get back?","At the table: Why does it keep saying it like that?","At the table: I can't stop shaking."},
        {"From the back: Is somebody playing a joke?","From the back: It keeps moving. How is it moving that fast?","From the back: I can hear it even with my hands over my ears. Please make it stop."},
        {"Near the door: Did somebody knock?","Near the door: That isn't how we knock.","Near the door: Don't answer. Please don't answer."}
    };
    private static final String[] REACTIONS={
        "By the window: Who just ran out, then?",
        "At the table: You were just here. I saw you beside me.",
        "Near the door: If he's out there, who came back in?",
        "In the aisle: Why has everything gone quiet?",
        "A cousin: You just came back. Then who was that at the glass?",
        "At the table: Wasn't he sitting with us a second ago?"
    };
    private GoatmanFear(){}
    public static void begin(CompoundTag run){
        if(run.getBoolean("HorrorStarted0472"))return;
        run.putBoolean("HorrorStarted0472",true);run.putInt("FearAge0472",0);run.putInt("FearNext0472",0);
        run.remove("FearLine0472");run.remove("FearPending0472");run.remove("FearQueue0472");
    }
    public static void departed(CompoundTag run){
        if(run.getBoolean("FearDeparture0472"))return;
        begin(run);run.putBoolean("FearDeparture0472",true);queue(run,22);queue(run,23);queue(run,24);
    }
    public static void returned(CompoundTag run){
        if(!run.getBoolean("HorrorStarted0472")||run.getBoolean("FearReturn0472"))return;
        run.putBoolean("FearReturn0472",true);queue(run,26);
    }
    public static void quiet(CompoundTag run){begin(run);queue(run,25);}
    public static void queue(CompoundTag run,int line){
        int[] old=run.getIntArray("FearQueue0472"),next=Arrays.copyOf(old,old.length+1);next[old.length]=line;run.putIntArray("FearQueue0472",next);
    }
    public static int stage(CompoundTag run){
        if(!run.getBoolean("HorrorStarted0472"))return 0;
        if(run.getInt("DemandStage0465")>=4||run.getInt("FearAge0472")>=1600)return TERROR;
        return run.getInt("DemandStage0465")>0||run.getInt("FearAge0472")>=360?FEAR:CONFUSION;
    }
    public static int role(int index,CompoundTag run){
        if(index<0||index==run.getInt("Wrong"))return 0;
        int rank=index<run.getInt("Wrong")?index:index-1;return 1+Math.floorMod(rank,7);
    }
    public static String destination(int index,CompoundTag run){
        if(stage(run)<FEAR)return GoatmanVignette.seatName(index,run);
        return switch(role(index,run)){
            case BRACE->"fearDoor";case WATCH->"fearWindow";
            case PACE->run.getInt("FearAge0472")%480<240?"fearPaceA":"fearPaceB";
            case COMFORT->"fearComfort";case TREMBLE->GoatmanVignette.seatName(index,run);
            case EARS->"fearEars";default->"fearStartle";
        };
    }
    public static void pose(GoatmanChild child,CompoundTag run){
        int stage=stage(run),role=role(child.getPersistentData().getInt(GoatmanVignette.INDEX),run);
        child.fear(stage==0?0:role);child.fearStage(role==0?0:stage);child.cower(stage==TERROR&&role==EARS);child.heave(false);
    }
    public static String line(int id){
        if(id>0&&id<=WORDS.length*3)return WORDS[(id-1)/3][(id-1)%3];
        return id>21&&id<=21+REACTIONS.length?REACTIONS[id-22]:"";
    }
    public static int remaining(CompoundTag run){
        if(stage(run)==0||run.getInt("FearLine0472")==0)return 0;
        int since=run.getInt("FearAge0472")-run.getInt("FearAtAge0472");return since>=0?Math.max(0,LINE_TICKS-since):0;
    }
    public static void tick(CompoundTag run,List<GoatmanChild> children,int clock){
        if(stage(run)==0)return;
        int age=run.getInt("FearAge0472")+1;run.putInt("FearAge0472",age);
        // An occupied age survives the gathering-to-night clock reset and saved-state reload.
        if(GoatmanVignette.speaking(run)){
            if(remaining(run)>0){
                int line=run.getInt("FearLine0472");
                if(run.getInt("FearPending0472")==0)run.putInt("FearPending0472",line);else queue(run,line);
                run.remove("FearLine0472");
            }
            return;
        }
        if(remaining(run)>0)return;
        int pending=run.getInt("FearPending0472");
        if(pending==0){
            int[] queue=run.getIntArray("FearQueue0472");
            if(queue.length>0){pending=queue[0];run.putIntArray("FearQueue0472",Arrays.copyOfRange(queue,1,queue.length));}
            else if(age>=run.getInt("FearNext0472")){
                int cursor=run.getInt("FearCursor0472");
                for(int attempt=0;attempt<7;attempt++){
                    int role=1+Math.floorMod(cursor++,7);
                    if(children.stream().noneMatch(c->role(c.getPersistentData().getInt(GoatmanVignette.INDEX),run)==role))continue;
                    pending=(role-1)*3+stage(run);break;
                }
                run.putInt("FearCursor0472",cursor);
            }
        }
        if(pending==0)return;
        run.putInt("FearLine0472",pending);run.putInt("FearAt0472",clock);run.putInt("FearAtAge0472",age);
        run.putInt("FearNext0472",age+160);run.remove("FearPending0472");
    }
}
