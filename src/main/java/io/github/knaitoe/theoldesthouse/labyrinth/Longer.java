package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 0.4.621, A Little Longer, the first slice (docs/A_LITTLE_LONGER_0_4_621.md), from the owner's proposal
 * (docs/A_LITTLE_LONGER_PROPOSAL.md) by the plan (docs/A_LITTLE_LONGER_PLAN.md). The place alone: Rowan's shop, the flat above it,
 * the roof landing and the House's corridor, every door and fitting answering a hand. Nobody is in it yet, and the House does not deal
 * it until its evening can be played.
 */
public final class Longer {
    private Longer(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Not dealt in this slice. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}
    /** 0.4.622: Rowan is decided for this reader and made in their chair (LongerRowan). */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){LongerRowan.arrive(p,b,own);}

    /** A hand on something of the shop's or the flat's. True if it was theirs to answer. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var at=b.offset(rel);var st=l.getBlockState(at);
        // The shop's front door: locked, the street through its glass.
        if(rel.getX()==LongerSite.FRONT_DOOR.getX()&&rel.getZ()==LongerSite.FRONT_DOOR.getZ()&&rel.getY()>=0&&rel.getY()<=1&&st.getBlock() instanceof net.minecraft.world.level.block.DoorBlock){
            l.playSound(null,at,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.3F,1.6F);cue(p,LongerTexts.FRONT_DOOR);return true;}
        if(!st.is(LongerRegistry.FITTING.get()))return false;
        var kind=st.getValue(LongerBlock.KIND);int stage=st.getValue(LongerBlock.STAGE);
        switch(kind){
            // The work order on its spike and the table are the first paper's and the account's: the House's own reading answers them.
            case ORDER,TABLE->{return false;}
            case LAMP->{if(LongerBench.lamp(p,b,at,st,own))return true;boolean on=stage==0;l.setBlock(at,st.setValue(LongerBlock.STAGE,on?1:0),3);l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.4F,on?1.4F:1.1F);cue(p,on?LongerTexts.LAMP_ON:LongerTexts.LAMP_OFF);}
            case RADIO->{
                if(stage==2){cue(p,LongerTexts.BENCH_RADIO);return true;}
                boolean on=stage==0;l.setBlock(at,st.setValue(LongerBlock.STAGE,on?1:0),3);l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.4F,.8F);
                LongerRadio.switched(l,b);cue(p,on?LongerTexts.FLAT_RADIO_ON:LongerTexts.FLAT_RADIO_OFF);}
            case TOASTER->{if(LongerKettle.toaster(p,b,at,st,own))return true;boolean down=stage==0;l.setBlock(at,st.setValue(LongerBlock.STAGE,down?1:0),3);l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.5F,down?.7F:1.6F);cue(p,down?LongerTexts.TOASTER_DOWN:LongerTexts.TOASTER_UP);}
            case WINDOW->cue(p,LongerSite.inFlat(p.position().subtract(b.getX(),b.getY(),b.getZ()))?LongerTexts.FLAT_WINDOW:LongerTexts.WINDOW);
            case WORKBENCH,TRAYS->cue(p,LongerTexts.TRAYS);
            case CHAIR->cue(p,LongerTexts.CHAIR);
            case SHELF->cue(p,LongerTexts.SHELF);
            case COUNTER->cue(p,LongerTexts.COUNTER);
            case KETTLE->cue(p,LongerTexts.KETTLE);
            case CUPS->{if(!LongerKettle.cups(p,b,at,st,own))cue(p,LongerTexts.CUPS);}
            case ARMCHAIR->cue(p,LongerTexts.ARMCHAIR);
            case KITCHEN_CHAIR->cue(p,LongerTexts.KITCHEN_CHAIR);
            case HOOK->cue(p,LongerTexts.HOOK);
            case WORKTOP->cue(p,stage==1?LongerTexts.SINK:LongerTexts.WORKTOP);
            case BASIN->cue(p,LongerTexts.BASIN);
            case LAVATORY->cue(p,LongerTexts.LAVATORY);
            case BENCH->cue(p,LongerTexts.BENCH);
            case TANK->cue(p,LongerTexts.TANK);
            case AWNING->cue(p,LongerTexts.AWNING);
        }
        return true;
    }
    /** A reader sits in one of the place's seats: the seat says what it is, as a hand on anything else here would. */
    public static void sat(net.minecraft.world.entity.player.Player player,net.minecraft.world.level.block.state.BlockState st){
        if(player instanceof ServerPlayer p&&LongerBlock.seat(st)){var line=line(st.getValue(LongerBlock.KIND));if(line!=null)cue(p,line);}
    }
    /** For a case: what a hand on this kind says, as the house would answer it. */
    public static String line(LongerBlock.Kind kind){
        return switch(kind){case CHAIR->LongerTexts.CHAIR;case SHELF->LongerTexts.SHELF;case COUNTER->LongerTexts.COUNTER;case KETTLE->LongerTexts.KETTLE;case CUPS->LongerTexts.CUPS;
            case ARMCHAIR->LongerTexts.ARMCHAIR;case KITCHEN_CHAIR->LongerTexts.KITCHEN_CHAIR;case HOOK->LongerTexts.HOOK;case BASIN->LongerTexts.BASIN;case LAVATORY->LongerTexts.LAVATORY;
            case BENCH->LongerTexts.BENCH;case TANK->LongerTexts.TANK;case AWNING->LongerTexts.AWNING;case WORKBENCH,TRAYS->LongerTexts.TRAYS;case WORKTOP->LongerTexts.WORKTOP;
            default->null;};
    }
}
