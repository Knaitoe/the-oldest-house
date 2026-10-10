package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.nbt.CompoundTag;

/** Individual fear belongs to the saved cousins; their words wait while the outside voice speaks. */
public final class GoatmanFear {
    public static final int BRACE=1,WATCH=2,PACE=3,COMFORT=4,TREMBLE=5,EARS=6,STARTLE=7;
    public static final int LINE_TICKS=100;
    private static final String[][] WORDS={
        {"At the latch: Don't touch it. Just keep your hand there.","At the latch: It's pushing back. Can somebody help me?","At the latch: No. No, we're not opening it."},
        {"By the window: Something went past the glass.","By the window: It was here. Now it's on the other side.","By the window: Don't make me look again."},
        {"In the aisle: Move away from the windows. Come over here.","In the aisle: I can't keep standing still.","In the aisle: Which side is it on now?"},
        {"A low voice: Look at me. Breathe with me.","A low voice: I'm here. Keep looking at me.","A low voice: Don't listen to it. Listen to me."},
        {"At the table: That's his voice. We heard him get back.","At the table: Why does it keep saying it like that?","At the table: I can't stop shaking."},
        {"From the back: Stop saying my name.","From the back: I can hear it even with my hands over my ears.","From the back: Please make it stop."},
        {"Near the door: That isn't how we knock.","Near the door: Did everybody hear that?","Near the door: Don't answer. Please don't answer."}
    };
    private GoatmanFear(){}
    public static int role(int index,CompoundTag run){
        if(index<0||index==run.getInt("Wrong"))return 0;
        int rank=index<run.getInt("Wrong")?index:index-1;
        return 1+Math.floorMod(rank,7);
    }
    public static String destination(int index,CompoundTag run){
        return switch(role(index,run)){
            case BRACE->"fearDoor";case WATCH->"fearWindow";
            case PACE->run.getInt("Clock")%480<240?"fearPaceA":"fearPaceB";
            case COMFORT->"fearComfort";case TREMBLE->GoatmanVignette.seatName(index,run);
            case EARS->"fearEars";default->"fearStartle";
        };
    }
    public static void pose(GoatmanChild child,CompoundTag run){
        int role=role(child.getPersistentData().getInt(GoatmanVignette.INDEX),run);
        child.fear(role);child.cower(role==EARS);child.heave(false);
    }
    public static String line(int id){
        if(id<=0||id>WORDS.length*3)return "";
        return WORDS[(id-1)/3][(id-1)%3];
    }
    public static void tick(CompoundTag run,List<GoatmanChild> children,int clock){
        if(clock>=run.getInt("FearNext0472")&&run.getInt("FearPending0472")==0){
            int cursor=run.getInt("FearCursor0472");
            for(int attempt=0;attempt<7;attempt++){
                int role=1+Math.floorMod(cursor++,7);
                if(children.stream().noneMatch(c->role(c.getPersistentData().getInt(GoatmanVignette.INDEX),run)==role))continue;
                int variation=Math.min(2,clock/700);run.putInt("FearPending0472",(role-1)*3+variation+1);break;
            }
            run.putInt("FearCursor0472",cursor);run.putInt("FearNext0472",clock+160);
        }
        // A queued response survives a demand and a reload. It never competes with the thing's words.
        if(GoatmanVignette.speaking(run)){
            if(run.getInt("FearLine0472")>0&&clock-run.getInt("FearAt0472")<LINE_TICKS){
                if(run.getInt("FearPending0472")==0)run.putInt("FearPending0472",run.getInt("FearLine0472"));
                run.remove("FearLine0472");
            }
            return;
        }
        if(clock-run.getInt("FearAt0472")<LINE_TICKS&&run.getInt("FearLine0472")!=0)return;
        int pending=run.getInt("FearPending0472");if(pending==0)return;
        run.putInt("FearLine0472",pending);run.putInt("FearAt0472",clock);run.remove("FearPending0472");
    }
}
