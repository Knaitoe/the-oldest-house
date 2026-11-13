package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.518, the Pell house (docs/PELL_HOUSE_PROPOSAL.md; the owner: "the house itself carries the story"). This slice: the place, and
 * its doors. The house is not dealt until it can be finished.
 *
 * <p>Doors open and shut to a hand. June's door does not. The shed's door and the pool's gate are padlocked. The garage's roller door
 * is never opened, and the sliding door's west half is fixed glass.
 */
public final class PellHouse {
    private PellHouse(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PELL_HOUSE;
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    /** Not until the house can be finished: it is being built in slices. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}
    /** 0.4.524: the people are at the house, where the first visit puts them. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){PellPeople.ensure(p.serverLevel(),b);}
    /** Every reader tick. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("WentIn")&&PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ()))){own.putBoolean("WentIn",true);save(p,own);}
        // The people are made here if they are missing; they look about on their own tick (PellPeople.serverTick).
        PellPeople.ensure(p.serverLevel(),b);
    }
    /** Everyone at this house now. */
    static java.util.List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new java.util.ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    public static void clearAll(){PellPeople.clearAll();}

    /** A hand on something of the house's. True if it was the house's to answer. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(PellThing.is(st)){thing(p,l,b,b.offset(rel),st,own);return true;}
        // 0.4.523: a hand on the line, wherever it is: what it is painted on and how it is now.
        // 0.4.531: after first light, paint put down in the night is dry.
        if(PellLine.is(st)){cue(p,PellTexts.line(st.getValue(PellLine.SURFACE),st.getValue(PellLine.SHAPE)==PellLine.Shape.THIN,st.getValue(PellLine.STAGE),PellMorning.dawned(b)));return true;}
        // 0.4.533: in the pool, the band's missing tile and its odd one, and what is under the drain's grate.
        var detail=PellWeek.detail(st);
        if(detail!=null){cue(p,detail);if(rel.equals(PellSite.DRAIN))own.putBoolean("SawGoggles",true);return true;}
        if(!st.is(PellRegistry.FITTING.get()))return false;
        var kind=st.getValue(PellBlock.KIND);
        if(kind==PellBlock.Kind.ROLLER){cue(p,PellTexts.ROLLER);return true;}
        // The letterbox is the first paper's: the House's own reading answers a hand on it. The mat is the account's once it is ready
        // (the House's reading answers that first); before then a hand lifts its corner, and the line runs on under it.
        if(kind==PellBlock.Kind.LETTERBOX)return false;
        if(kind==PellBlock.Kind.DOORMAT){
            boolean lift=st.getValue(PellBlock.STAGE)==0;
            l.setBlock(b.offset(rel),st.setValue(PellBlock.STAGE,lift?1:0),3);
            l.playSound(null,b.offset(rel),SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.5F,.8F);
            cue(p,lift?PellTexts.MAT_LIFTED:PellTexts.MAT_DOWN);
            return true;
        }
        if(kind==PellBlock.Kind.WINDOW||kind==PellBlock.Kind.FENCE||kind==PellBlock.Kind.POOL_FENCE||kind==PellBlock.Kind.TARP)return true;
        var door=PellSite.doorAt(l,b,rel);if(door==null)return true;
        // A neighbour's door: nobody answers it.
        if(!PellSite.onLot(new net.minecraft.world.phys.Vec3(door.getX()+.5,0,door.getZ()+.5))){cue(p,PellTexts.NOBODY_HOME);return true;}
        if(door.equals(PellSite.JUNE_DOOR)){cue(p,PellTexts.JUNES_DOOR);return true;}
        // 0.4.530: open at night while June has the tin out, its padlock hangs on the hasp; a hand does not shut it.
        if(door.equals(PellSite.SHED_DOOR)){cue(p,PellSite.open(l,b,door)?PellTexts.SHED_OPEN:PellTexts.PADLOCKED);return true;}
        // 0.4.533: while June swims, the padlock hangs open and the gate opens and shuts (PellSwim).
        if(door.equals(PellSite.POOL_GATE)){if(!PellSwim.gateHand(p,l,b))cue(p,PellTexts.POOL_GATE);return true;}
        if(door.equals(PellSite.SLIDER.west())){cue(p,PellTexts.FIXED_GLASS);return true;}
        // 0.4.527: a door June has locked. From inside, its lock turns and it opens; from outside it does not.
        if(PellVisit.locked(b,door)){
            if(!PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ()))){
                // 0.4.530: while June is out with the tin she has locked the back door behind her with her key.
                l.playSound(null,b.offset(door),SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.3F,1.8F);
                cue(p,door.equals(PellSite.BACK_DOOR)&&PellNight.juneOut(l,b)?PellTexts.BACK_LOCKED_OUT:PellTexts.locked(door));return true;
            }
            PellVisit.unlock(b,door);l.playSound(null,b.offset(door),PellRegistry.LOCK.get(),SoundSource.BLOCKS,.6F,1.1F);
        }
        toggle(l,b,door);
        return true;
    }
    /**
     * 0.4.522: a hand on one of the house's things. Each says what it is (PellTexts.thing); the wardrobe opens and shuts, the
     * garage's bulb goes on and off by its cord, and the clock on Nessa's drawers says the time.
     */
    static void thing(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        var kind=st.getValue(PellThing.KIND);int stage=st.getValue(PellThing.STAGE);
        switch(kind){
            case WARDROBE->{
                // Both cells of it, from whichever was touched.
                BlockPos lower=(stage&1)==1?at.below():at;boolean open=(stage&2)==0;
                for(int i=0;i<2;i++){var cell=lower.above(i);var s=l.getBlockState(cell);if(PellThing.is(s,PellThing.Kind.WARDROBE))l.setBlock(cell,s.setValue(PellThing.STAGE,(s.getValue(PellThing.STAGE)&1)|(open?2:0)),3);}
                l.playSound(null,at,open?SoundEvents.WOODEN_DOOR_OPEN:SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.5F,1.2F);
                cue(p,PellTexts.thing(kind,open?2:0));
            }
            case BULB,FLOOR_LAMP->{
                // On and off: the bulb on its pull cord, the lamp at its switch; the line is what it is now.
                l.setBlock(at,st.setValue(PellThing.STAGE,stage==0?1:0),3);
                l.playSound(null,at,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,.4F,1.6F);
                cue(p,PellTexts.thing(kind,stage==0?1:0));
            }
            // 0.4.526: the talking clock says the house's own hour.
            case DRAWERS->cue(p,PellTexts.thing(kind,stage)+" "+PellTexts.clock(PellVisit.hour(b)));
            // 0.4.526: at dinner, what is at the place nearest the hand, or the reader's own dinner, eaten.
            case TABLE->{if(!PellVisit.table(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.527: while June's tape plays, what is on it.
            case TV_CABINET->{if(!PellVisit.tvHand(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.531: from when June sends them out until breakfast is called, the reader lies down on it.
            case CAMP_BED->{if(!PellMorning.lieDown(p,b,at,st,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.532: in the morning the telephone rings, and it is Gail.
            case PHONE->{if(!PellCall.hand(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            default->cue(p,PellTexts.thing(kind,stage));
        }
    }
    /** Opens a shut door or gate and shuts an open one, both cells of it. */
    public static void toggle(ServerLevel l,BlockPos b,BlockPos door){
        var at=b.offset(door);var st=l.getBlockState(at);if(!st.is(PellRegistry.FITTING.get()))return;
        var kind=st.getValue(PellBlock.KIND);boolean open=PellBlock.open(st);Direction f=st.getValue(PellBlock.FACING);
        if(kind.leaf())PellSite.leaf(l,b,door,kind,f,!open);
        else if(kind==PellBlock.Kind.GATE)l.setBlock(at,PellBlock.of(kind,f,open?0:1),3);
        else return;
        l.playSound(null,at,kind==PellBlock.Kind.GATE?(open?SoundEvents.FENCE_GATE_CLOSE:SoundEvents.FENCE_GATE_OPEN):kind==PellBlock.Kind.SHED_DOOR||kind==PellBlock.Kind.SCREEN||kind==PellBlock.Kind.SLIDER
            ?(open?SoundEvents.IRON_DOOR_CLOSE:SoundEvents.IRON_DOOR_OPEN):(open?SoundEvents.WOODEN_DOOR_CLOSE:SoundEvents.WOODEN_DOOR_OPEN),SoundSource.BLOCKS,.7F,1F);
    }
}
