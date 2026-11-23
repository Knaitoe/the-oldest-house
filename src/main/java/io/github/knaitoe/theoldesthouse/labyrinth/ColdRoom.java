package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;

/**
 * 0.4.754, the cold room, the first part (docs/COLD_ROOM_0_4_754.md), from the owner's request of 23 November ("And one based off of the
 * exorcist?"). The place alone: the rented house on the hill, its hall and stair, the kitchen where the mother sits, the landing and the
 * back bedroom, where her daughter lies in the iron bed and the priest stands at the window; the long steps outside. Nothing happens yet.
 * The House does not deal it until part 4, when it has its rite and its ending. Stranger phases 314 (the mother), 315 (the daughter) and
 * 316 (the priest).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ColdRoom {
    private ColdRoom(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.COLD_ROOM;
    public static final int FIRST=314,MOTHER=0,GIRL=1,PRIEST=2,CAST=3;
    private static final String[] SKINS={"cold_mother","cold_girl","cold_priest"};
    /** One house holds up to three readers (docs/WAXWORKS_AND_COLD_ROOM_0_4_749.md). */
    public static final int PARTY=3;
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+CAST;}
    public static String skin(int phase){return SKINS[phase-FIRST];}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    public static int which(LiteraryActor a){return a.phase()-FIRST;}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Not dealt until part 4. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}

    /** A reader in: the three are put where they keep if they are not there, and the house's night sound goes round once. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();ensure(l,b);ColdNight.open(b);
        own.putInt("Visits",own.getInt("Visits")+1);
        l.playSound(null,b.offset(0,1,-4),ColdRegistry.ROOM.get(),SoundSource.AMBIENT,.6F,1F);
    }
    private static BlockPos home(int who){return switch(who){case MOTHER->ColdSite.MOTHER;case GIRL->ColdSite.GIRL;default->ColdSite.PRIEST;};}
    /** The one with this part, if their body is there. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var at=b.offset(home(who));
        // 0.4.756: the priest moves about the bedroom in the rite (the window, the bedside, the chair), so he is looked for in all of it.
        var box=who==PRIEST?new AABB(b.offset(ColdSite.BEDROOM.x0()-1,ColdSite.BEDROOM.y0()-1,ColdSite.BEDROOM.z0()-1)).minmax(new AABB(b.offset(ColdSite.BEDROOM.x1()+1,ColdSite.BEDROOM.y1()+1,ColdSite.BEDROOM.z1()+1))):new AABB(at).inflate(4);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,box,x->is(x)&&x.phase()==FIRST+who))if(a.isAlive())return a;
        return null;
    }
    /** Each of the three is made where they keep if they are not there; nobody is made where the house's entities are not loaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        for(int who=0;who<CAST;who++){
            var at=b.offset(home(who));
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var a=find(l,b,who);
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                place(a,b,who);
                if(!l.addFreshEntity(a))continue;
            }
            place(a,b,who);
        }
    }
    /**
     * Where each keeps: the mother on her chair at the kitchen table, facing it; the daughter on her back in the bed, her head on the
     * pillow (the body laid along the bed by LiteraryRenderers.lieOnColdBed); the priest at the window, looking out.
     */
    static void place(LiteraryActor a,BlockPos b,int who){
        switch(who){
            case MOTHER->{var at=b.offset(ColdSite.MOTHER_CHAIR);float yaw=net.minecraft.core.Direction.EAST.toYRot();a.moveTo(at.getX()+.5,at.getY()+10/16.0,at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.gesture(0);}
            case GIRL->{
                var head=b.offset(ColdSite.BED_HEAD);float yaw=ColdSite.BED_FACING.toYRot();
                a.moveTo(head.getX()+.5,head.getY()+10/16.0,head.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
                a.setSleepingPos(head);a.setPose(Pose.SLEEPING);a.gesture(0);
            }
            default->{
                // 0.4.756: at the window until the rite begins; then kneeling by the bed, facing it; at the eleventh page, found in the chair.
                int mode=ColdNight.priest(b);
                if(mode==ColdRite.KNEELING){var at=b.offset(ColdRite.KNEEL);float yaw=net.minecraft.core.Direction.EAST.toYRot();a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,20);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setPose(Pose.CROUCHING);a.gesture(0);}
                else if(mode==ColdRite.DOWN){var at=b.offset(ColdSite.CHAIR);float yaw=net.minecraft.core.Direction.EAST.toYRot();a.moveTo(at.getX()+.5,at.getY()+10/16.0,at.getZ()+.5,yaw,50);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setXRot(50);a.setPose(Pose.STANDING);a.gesture(0);}
                else{var at=b.offset(ColdSite.PRIEST);float yaw=net.minecraft.core.Direction.NORTH.toYRot();a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setPose(Pose.STANDING);a.gesture(0);}
            }
        }
        a.setDeltaMovement(Vec3.ZERO);
    }
    /** The daughter lies in the bed, which is a bed to the game; nothing gets her up. */
    @SubscribeEvent public static void keepLying(CanContinueSleepingEvent e){
        if(e.getEntity() instanceof LiteraryActor a&&is(a)&&which(a)==GIRL)e.setContinueSleeping(true);
    }
    /** The house a person of the three keeps in: the night's copy nearest them. */
    private static BlockPos b(LiteraryActor a){
        BlockPos best=null;double d=Double.MAX_VALUE;
        for(long s:ColdNight.NIGHTS.keySet()){var at=BlockPos.of(s);double e=a.distanceToSqr(at.getCenter());if(e<d){d=e;best=at;}}
        return best==null?a.blockPosition():best;
    }
    /** A hand on one of the three. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int who=which(a);
        int mode=who==PRIEST?ColdNight.priest(b(a)):ColdRite.AT_WINDOW;
        cue(p,mode==ColdRite.KNEELING?ColdTexts.PRIEST_KNEELING:mode==ColdRite.DOWN?ColdTexts.PRIEST_STILL:ColdTexts.touch(who));
        own.putInt("Touched",own.getInt("Touched")|1<<who);
    }
    /** A hand on something of the house's. True if it was theirs to answer. The letter on the hall table is the House's own reading. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(st.is(net.minecraft.world.level.block.Blocks.GLASS_PANE)&&ColdSite.BEDROOM.contains(Vec3.atCenterOf(rel).add(0,0,1))){cue(p,ColdTexts.WINDOW);return true;}
        if(!ColdBlock.is(st))return false;
        int stage=st.getValue(ColdBlock.STAGE);
        switch(st.getValue(ColdBlock.KIND)){
            case HALL_TABLE->{if(stage==1)return false;cue(p,ColdTexts.HALL_TABLE);}
            case HOOKS->cue(p,ColdTexts.HOOKS);
            case KITCHEN_TABLE->cue(p,ColdTexts.KITCHEN_TABLE);
            case STOVE->cue(p,ColdTexts.STOVE);
            case SINK->cue(p,ColdTexts.SINK);
            case CHAIR->cue(p,rel.equals(ColdSite.CHAIR)?ColdTexts.BEDSIDE_CHAIR:ColdTexts.CHAIR);
            case BED->cue(p,stage>=2?ColdTexts.BED_RISEN:stage%2==0?ColdTexts.PILLOW:ColdTexts.BED);
            case DRESSER->cue(p,stage==0?ColdTexts.DRESSER:ColdTexts.DRESSER_OUT);
            case BOOK->ColdRite.book(p,b,own);
            case CRUCIFIX->cue(p,stage==0?ColdTexts.CRUCIFIX:ColdTexts.CRUCIFIX_TURNED);
            case NIGHTSTAND->{if(stage==0||!ColdRite.flask(p,b,own))cue(p,stage==0?ColdTexts.LAMP_TABLE:ColdTexts.FLASK_TABLE);}
            case RADIATOR->cue(p,ColdTexts.RADIATOR);
            case BULB->cue(p,ColdTexts.BULB);
            case STREETLAMP->cue(p,ColdTexts.STREETLAMP);
        }
        return true;
    }
}
