package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Nell's games on the walk from the town to the Triple Tree. */
final class BorrascaPlay {
    static final int COMPASS=10;
    static final BlockPos HIDING_TREE=new BlockPos(3,0,-33);
    private static final BlockPos TAG_END=new BlockPos(1,0,-28);
    private static final BlockPos[] HIDE_ROUTE={new BlockPos(1,0,-34),new BlockPos(3,0,-35)};
    private BorrascaPlay(){}

    static boolean tick(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor nell){
        if(own.getInt("ChildStep")>0||own.getBoolean("PlayDone"))return false;
        if(!own.getBoolean("MetFriend"))return false;
        int beat=own.getInt("PlayBeat"), ticks=own.getInt("PlayTicks")+5;
        if(ticks>=1000){done(p,own,nell);return false;}
        own.putInt("PlayTicks",ticks);
        if(beat==0){
            if(ticks==5){
                nell.gesture(COMPASS);
                BorrascaStory.say(p,nell,"I'll keep north. You get a head start. Just kidding. You're it!");
            }
            if(ticks<75)return true;
            nell.gesture(0);
            if(!walk(nell,b.offset(TAG_END)))return true;
            BorrascaCast.stop(nell);
            if(p.distanceToSqr(nell)>4)return true;
            own.putBoolean("TaggedNell",true);own.putInt("PlayBeat",1);
            nell.gesture(COMPASS);
            BorrascaStory.say(p,nell,"You got me. Now close your eyes. I'll keep the compass so I don't get properly lost.");
            own.putInt("HideAt",ticks+65);
            return true;
        }
        if(ticks<own.getInt("HideAt"))return true;
        nell.gesture(0);
        int leg=own.getInt("HideLeg");
        if(leg<HIDE_ROUTE.length){
            if(walk(nell,b.offset(HIDE_ROUTE[leg])))own.putInt("HideLeg",leg+1);
            return true;
        }
        BorrascaCast.stop(nell);
        if(p.distanceToSqr(nell)<6.25&&p.hasLineOfSight(nell)){
            own.putBoolean("FoundNell",true);
            done(p,own,nell);
            return false;
        }
        if(!own.getBoolean("HideHint")&&p.distanceToSqr(nell)<49){
            own.putBoolean("HideHint",true);
            BorrascaStory.say(p,nell,"The needle's pointing right at you. I think it likes you.");
        }
        return true;
    }

    private static boolean walk(LiteraryActor a,BlockPos at){
        var point=Vec3.atBottomCenterOf(at);
        if(a.distanceToSqr(point)<.18)return true;
        BorrascaCast.walk(a,point,.95);
        return false;
    }

    private static void done(ServerPlayer p,CompoundTag own,LiteraryActor nell){
        own.putBoolean("PlayDone",true);
        nell.gesture(0);
        BorrascaStory.say(p,nell,own.getBoolean("FoundNell")
                ?"Knew you'd find me. Come on. I'll show you my hiding place that actually works."
                :"Come on. I'll show you where the three trees meet.");
    }
}
