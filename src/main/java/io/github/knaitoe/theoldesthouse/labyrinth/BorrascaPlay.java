package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Nell's games on the walk from the town to the Triple Tree. */
final class BorrascaPlay {
    static final int COMPASS=10;
    static final BlockPos EAST_TREE=new BlockPos(3,0,-33),WEST_TREE=new BlockPos(-1,0,-33);
    private static final BlockPos TAG_END=new BlockPos(1,0,-28);
    private static final int EAST=3,WEST=-1;
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
                BorrascaStory.say(p,nell,"My compass says that bend is north. See if you can catch me there. You're it!");
            }
            if(ticks<100)return true;
            nell.gesture(0);
            if(!walk(nell,BorrascaRooms.at(b,TAG_END)))return true;
            BorrascaCast.stop(nell);
            if(p.distanceToSqr(nell)>4)return true;
            own.putBoolean("TaggedNell",true);own.putInt("PlayBeat",1);
            nell.gesture(COMPASS);
            BorrascaStory.say(p,nell,"You got me. Now turn around and count. If you get lost looking for me, I'll call you back north.");
            own.putInt("HideAt",ticks+100);
            return true;
        }
        int hideAt=own.getInt("HideAt");
        if(ticks<hideAt)return true;
        if(!own.contains("HideSide")){
            // Give a reader who turns to count a head start on a clean hide.
            if(ticks<hideAt+80&&p.getLookAngle().z<-.25)return true;
            own.putInt("HideSide",p.getX()>b.getX()+1.5?WEST:EAST);
        }
        nell.gesture(0);
        int leg=own.getInt("HideLeg"),side=own.getInt("HideSide");
        if(leg<2){
            var at=leg==0?new BlockPos(1,0,-35):new BlockPos(side,0,-35);
            if(walk(nell,BorrascaRooms.at(b,at)))own.putInt("HideLeg",leg+1);
            return true;
        }
        BorrascaCast.stop(nell);
        if(p.distanceToSqr(nell)<4&&p.hasLineOfSight(nell)){
            own.putBoolean("FoundNell",true);
            done(p,own,nell);
            return false;
        }
        if(!own.getBoolean("HideHint")&&p.distanceToSqr(nell)<49){
            own.putBoolean("HideHint",true);
            BorrascaStory.say(p,nell,side==EAST?"A little east. That's the side the sun comes up on."
                    :"A little west. That's the side where the sun goes down.");
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
                ?"I knew you'd find me. There's a better place up the trail. Come on, I'll show you."
                :"Come on. I'll show you where the three trees meet.");
    }
}
