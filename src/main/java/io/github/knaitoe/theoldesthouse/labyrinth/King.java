package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.641, A sort of thing, the first part (docs/SLEEPING_KING_0_4_641.md), from the owner's proposal (docs/SLEEPING_KING_PROPOSAL.md)
 * by the plan (docs/SLEEPING_KING_PLAN.md). The place alone: the drawing-room, the room behind the glass, the board in its three kinds
 * of land, the wood and the King asleep under his tree. The glass lets a reader through by a hand on it, either way. Nothing else
 * happens yet, and the House does not deal it until part 3.
 */
public final class King {
    private King(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    /** The King, on stranger phase 217 (literary_king). */
    public static final int PHASE=217;
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Not dealt until part 3. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==PHASE;}
    static AABB area(BlockPos b){var k=b.offset(KingSite.KING);return new AABB(k).inflate(8);}
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){ensure(p.serverLevel(),b);}

    /** The King under his tree, if his body is there. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),King::is))if(a.isAlive())return a;
        return null;
    }
    /** He is made lying under his tree if he is not there; nothing is made while the wood's entities are unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        var at=b.offset(KingSite.KING);
        if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))return;
        var a=find(l,b);
        if(a==null){
            a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
            a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,PHASE);
            if(!l.addFreshEntity(a))return;
        }
        lie(a,at);
    }
    /** Asleep in a heap at the foot of the tree, his head toward the trunk. */
    static void lie(LiteraryActor a,BlockPos at){
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);a.setYBodyRot(180);a.setYHeadRot(180);
        a.setSleepingPos(at);a.setPose(Pose.SLEEPING);a.gesture(0);a.setDeltaMovement(Vec3.ZERO);
    }

    /** A hand on something of the room's or the country's. True if it was theirs to answer. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var at=b.offset(rel);var st=l.getBlockState(at);
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        if(KingRegistry.ruled(st)){cue(p,KingTexts.RULED);return true;}
        if(!st.is(KingRegistry.FITTING.get()))return false;
        boolean behind=rel.getZ()<KingSite.MIRROR_Z;
        switch(st.getValue(KingBlock.KIND)){
            case MANTEL->cue(p,KingTexts.MANTEL);
            case GLASS->{
                // Through the glass: from the hearth into the same place on the other side of it, facing on.
                var from=BlockPos.containing(r);
                if(Math.abs(rel.getZ()-r.z)>3.2){cue(p,behind?KingTexts.GLASS_BACK:KingTexts.GLASS);return true;}
                double tz=behind?-10.5:-15.5;float yaw=behind?0:180;
                l.playSound(null,at,SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.BLOCKS,.5F,.6F);
                p.teleportTo(l,b.getX()+Math.max(-1,Math.min(1,from.getX()))+.5,b.getY(),b.getZ()+tz,yaw,p.getXRot());
                cue(p,behind?KingTexts.GLASS_BACK:KingTexts.GLASS);
            }
            case RUG->cue(p,KingTexts.RUG);
            case CHESSMEN->cue(p,KingTexts.CHESSMEN);
            case TABLE->cue(p,KingTexts.TABLE);
            // The chess problem on the drawing-room table is the first paper: the House's own reading answers it. Behind the glass it reads.
            case PROBLEM->{if(!behind)return false;cue(p,KingTexts.PROBLEM);}
            case ARMCHAIR->cue(p,KingTexts.ARMCHAIR);
            case WINDOW->cue(p,KingTexts.WINDOW);
        }
        return true;
    }
}
