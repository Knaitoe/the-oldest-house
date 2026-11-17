package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.645, A sort of thing, part 5 (docs/SLEEPING_KING_0_4_645.md): the Caterpillar on the mushroom, at the far edge of the fifth
 * square, where the fill gives out. It sits on the cap, looks down at whoever is nearest and asks who they are. The answers the panel
 * offers are what that reader is at the moment; on the out-breath words drop out of them. Its look is notice (KingBreath), so a reader
 * at the mushroom is never less than a sort of thing and can always break a piece off.
 */
public final class KingCaterpillar {
    private KingCaterpillar(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    public static final int PHASE=222;
    /** Sitting with its arms folded (0), and speaking, one hand up (1). */
    public static final int SITTING=0,SPEAKING=1;
    static final String TALK="king_caterpillar",ASKED="KingAsked";
    /** How far it looks for somebody to look at. */
    static final double LOOKS=12;

    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==PHASE;}
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(KingSite.MUSHROOM)).inflate(16,8,16),KingCaterpillar::is))if(a.isAlive())return a;
        return null;
    }
    static void ensure(ServerLevel l,BlockPos b){
        var at=b.offset(KingSite.MUSHROOM).above();
        if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at))||find(l,b)!=null)return;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,PHASE);
        a.moveTo(at.getX()+.5,at.getY()+KingBlock.CAP_TOP,at.getZ()+.5,90,0);a.setYBodyRot(90);a.setYHeadRot(90);
        l.addFreshEntity(a);
    }

    // ------------------------------------------------------------------------------------------------ while anybody is there
    static void tick(ServerLevel l,BlockPos b){
        ensure(l,b);var a=find(l,b);if(a==null)return;
        var at=b.offset(KingSite.MUSHROOM).above();
        // It stays on the cap, whatever pushes it.
        if(a.position().distanceToSqr(at.getX()+.5,at.getY()+KingBlock.CAP_TOP,at.getZ()+.5)>.04)a.moveTo(at.getX()+.5,at.getY()+KingBlock.CAP_TOP,at.getZ()+.5,a.getYRot(),a.getXRot());
        a.gesture(a.speaking()>0?SPEAKING:SITTING);
        // It looks down at the nearest reader, head and eyes both, so its look is notice.
        var near=KingSleep.nearest(l,b,a.position());
        if(near!=null&&near.distanceTo(a)<LOOKS){
            var eye=a.getEyePosition();var to=near.getBoundingBox().getCenter().subtract(eye);
            float yaw=(float)(Math.toDegrees(Math.atan2(to.z,to.x))-90),pitch=(float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z)));
            a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setXRot(Mth.clamp(pitch,-60,60));
        }
    }

    // ------------------------------------------------------------------------------------------------ "Who are you?"
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        Talk.begin(p,TALK,KingTexts.CATERPILLAR_NAME);
        say(p,b,KingTexts.CAT_WHO);
        boolean out=!KingBreath.in(b);int state=KingBreath.of(p);
        Talk.ask(p,Talk.reply(answer(state,out),KingCaterpillar::you),Talk.reply(drop(KingTexts.R_NOT_MYSELF,out),KingCaterpillar::you));
    }
    /** "You!" and then what the mushroom does, and what the bridge will bear. */
    public static void you(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        say(p,b,KingTexts.CAT_YOU);say(p,b,KingTexts.CAT_SIDES);say(p,b,KingTexts.CAT_BRIDGE);
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean(ASKED,true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        Talk.end(p);
    }
    /** What a reader can say they are at the moment, with words gone from it on the out-breath. */
    public static String answer(int state,boolean out){
        String line=switch(state){
            case KingBreath.TOO_MUCH->KingTexts.R_TOO_MUCH;
            case KingBreath.THING->KingTexts.R_A_THING;
            case KingBreath.SORT->KingTexts.R_A_SORT;
            default->KingTexts.R_HARDLY;
        };
        return drop(line,out);
    }
    /** On the out-breath every other word drops out of a line; the first stays, so it is still somebody's answer. */
    public static String drop(String line,boolean out){
        if(!out)return line;
        var words=line.split(" ");var kept=new StringBuilder();
        for(int i=0;i<words.length;i++){if(kept.length()>0)kept.append(' ');kept.append(i%2==0?words[i]:"...");}
        return kept.toString();
    }
    static void say(ServerPlayer p,BlockPos b,String line){
        Talk.line(p,KingTexts.CATERPILLAR_NAME,line);
        var a=find(p.serverLevel(),b);if(a!=null)King.aloud(a,KingTexts.CATERPILLAR_NAME,line);
    }
}
