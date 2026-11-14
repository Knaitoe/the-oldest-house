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

    /**
     * 0.4.541: the house can be finished, so it is dealt; once to each reader. "Every ending is permanent. There is no second attempt,
     * and the vignette is never dealt again to a reader whose ending has landed": not to somebody whose account has been read
     * ("Completed"), nor to somebody whose morning came after Tilly answered ("Ended", PellDawn).
     */
    public static boolean dealable(LabyrinthData d,UUID reader){var o=LiteraryVignettes.personal(d,reader,PLACE);return !o.getBoolean("Completed")&&!o.getBoolean("Ended");}
    /** 0.4.524: the people are at the house, where the first visit puts them; 0.4.542, unless it stands, or is about to stand, as its
     *  aftermath for everybody at it (PellAftermath), with nobody in it. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var here=readers(l,b);if(!here.contains(p))here.add(p);
        if(PellAftermath.holds(l,b,here))PellPeople.household(b);else PellPeople.ensure(l,b);
    }
    /** 0.4.547: in a reader's own record, who else they have been at the house with, by name, in the order they met: their own pair, in a
     *  house of their own (SceneInstances), for their account (PellTexts.others). */
    public static final String WITH="With0547";
    /** Every reader tick. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("WentIn")&&PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ()))){own.putBoolean("WentIn",true);save(p,own);}
        // The people are made here if they are missing; they look about on their own tick (PellPeople.serverTick). 0.4.542: not while
        // the house stands as its aftermath, whose tick runs there too.
        var here=readers(p.serverLevel(),b);
        if(PellAftermath.holds(p.serverLevel(),b,here))PellPeople.household(b);else{PellPeople.ensure(p.serverLevel(),b);met(p,own,here);}
        // 0.4.537: an entry due in their journal goes in, in their own hand, the first moment it can.
        PellEntries.reader(p,own);
        leaving(p,b,own);
    }
    /**
     * 0.4.566 (owner, playing the house: "the player should also be sort of prompted on when to leave, so they're not trying to wait
     * for another day to pass without realizing they should leave and come back"). A visit is a reader's once Gail has rung and June
     * has hung up (PellWeek.done), or once the last night has ended; after that the morning waits for nothing, and the next visit
     * comes only by going out through the House's door and coming back. So, once a reader has had all of a visit: a while later June
     * sends whoever is near her out into the day, and a while after that the House says plainly, once, that this day is done and the
     * house will have moved on when they come back; once more, later, in passing, if they are still here.
     */
    static void leaving(ServerPlayer p,BlockPos b,CompoundTag own){
        int visit=own.getInt("Visit");if(visit<=0||own.getInt("Beat")<visit||p.isSpectator())return;
        long now=p.serverLevel().getGameTime();
        if(own.getInt(DONE_VISIT)!=visit){own.putInt(DONE_VISIT,visit);own.putLong(DONE_AT,now);own.putInt(TOLD,0);save(p,own);return;}
        long since=now-own.getLong(DONE_AT);int told=own.getInt(TOLD);boolean ended=own.getBoolean("Ended");
        if(told==0&&since>=SEND_OUT){
            var h=PellPeople.household(b);var june=PellPeople.find(p.serverLevel(),b,PellPeople.JUNE);
            boolean hers=!ended&&june!=null&&june.isAlive()&&h.sentOut!=visit&&june.distanceToSqr(p)<16*16;
            // In the middle of something (a line still in the air, or her own business), she says it when she is done, if before the House would.
            if(hers&&!PellVisit.ready(h,now,june)&&since<HINT)return;
            if(hers){h.sentOut=visit;PellVisit.say(h,june,PellPeople.JUNE,PellTexts.sendOut(visit),now);}
            own.putInt(TOLD,1);save(p,own);
        }else if(told==1&&since>=HINT){
            p.displayClientMessage(Component.literal(PellTexts.leaveHint(visit,ended)).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);own.putInt(TOLD,2);save(p,own);
        }else if(told==2&&since>=AGAIN){
            p.displayClientMessage(Component.literal(PellTexts.leaveHint(visit,ended)).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);own.putInt(TOLD,3);save(p,own);
        }
    }
    /** 0.4.566: in a reader's record, the visit they were last seen to have had all of, when, and how far the telling has got. */
    static final String DONE_VISIT="DoneVisit0566",DONE_AT="DoneAt0566",TOLD="ToldToGo0566";
    /** 0.4.566: how long after a visit is had June sends the readers out; the House says so; and says so again. */
    public static final int SEND_OUT=300,HINT=600,AGAIN=3000;
    /** 0.4.547: whoever else is at this reader's house with them, both with the house still to finish, is kept in their record (WITH). */
    static void met(ServerPlayer p,CompoundTag own,java.util.List<ServerPlayer> here){
        if(own.getBoolean("Ended"))return;var with=own.getList(WITH,net.minecraft.nbt.Tag.TAG_STRING);boolean changed=false;
        for(var q:here){
            if(q==p||!q.isAlive()||q.isSpectator()||own(q).getBoolean("Ended"))continue;var name=q.getGameProfile().getName();boolean known=false;
            for(int i=0;i<with.size()&&!known;i++)known=with.getString(i).equals(name);
            if(!known){with.add(net.minecraft.nbt.StringTag.valueOf(name));changed=true;}
        }
        if(changed){own.put(WITH,with);save(p,own);}
    }
    /** Everyone at this house now. */
    static java.util.List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new java.util.ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    public static void clearAll(){PellPeople.clearAll();PellAfterlife.clearAll();PellPages.clearAll();}

    /** A hand on something of the house's. True if it was the house's to answer. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        // 0.4.542: the house as its aftermath answers a hand its own way (PellAftermath).
        if(PellAftermath.click(p,b,rel,own))return true;
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(PellThing.is(st)){thing(p,l,b,b.offset(rel),st,own);return true;}
        // 0.4.571: the dog's bowl, and a hand on the ground by the kennel: it goes down where it stood (PellJobs.ground).
        if(PellJobs.ground(p,b,rel,own))return true;
        // 0.4.523: a hand on the line, wherever it is: what it is painted on and how it is now.
        // 0.4.531: after first light, paint put down in the night is dry.
        // 0.4.538: with the bucket in hand, the brush goes to work on it (PellScrubItem) instead.
        if(PellLine.is(st)){if(PellScrub.holding(p))return false;cue(p,PellTexts.line(st.getValue(PellLine.SURFACE),st.getValue(PellLine.SHAPE)==PellLine.Shape.THIN,st.getValue(PellLine.STAGE),PellMorning.dawned(b)));return true;}
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
        // 0.4.536: a week in, its padlock hangs open until June's rounds, and a hand opens and shuts it (PellTapes).
        if(door.equals(PellSite.SHED_DOOR)){if(!PellTapes.shedHand(p,l,b))cue(p,PellSite.open(l,b,door)?PellTexts.SHED_OPEN:PellTexts.PADLOCKED);return true;}
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
        // 0.4.571: what a reader has put a hand on is theirs to tell Tilly about (PellCompany).
        PellCompany.saw(own,kind,stage);
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
            // 0.4.571: with plates in hand while the table may be laid, one is laid at the nearest bare place (PellJobs).
            case TABLE->{if(!PellJobs.lay(p,b,own)&&!PellVisit.table(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.571, the day's jobs (PellJobs): the chart says what is on it now; the cupboards give up the plates; the dog's bowl is
            // tipped out and taken, filled at a tap, and set down again by the kennel.
            case CHORE_CHART->PellJobs.chart(p,b,st,own);
            case CUPBOARD_UPPER->{if(!PellJobs.cupboard(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            case BOWL->{if(!PellJobs.bowl(p,b,at,st,own))cue(p,PellTexts.thing(kind,stage));}
            case KENNEL->{if(!PellJobs.kennel(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            case SINK,TROUGH->{if(!PellJobs.tap(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.571: the book that is out, read to Tilly (PellCompany).
            case BOOKSHELF->{if(!PellCompany.book(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.527: while June's tape plays, what is on it.
            case TV_CABINET->{if(!PellVisit.tvHand(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.531: from when June sends them out until breakfast is called, the reader lies down on it.
            case CAMP_BED->{if(!PellMorning.lieDown(p,b,at,st,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.545: the cane on the hook by the front door, taken, makes its taker Tilly (PellTilly.hook); Nessa's bed is theirs to
            // sit on the edge of at bedtime and to lie down in at night (PellTilly.bed).
            case COAT_HOOK->{if(PellTilly.hook(p,b,at,st,own))save(p,own);else cue(p,PellTexts.thing(kind,stage));}
            case BED->{if(!PellTilly.bed(p,b,at,st,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.532: in the morning the telephone rings, and it is Gail.
            case PHONE->{if(!PellCall.hand(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.536: a week in, the shed's tapes and the deck under its television play (PellTapes).
            case TAPES->{if(!PellTapes.shelf(p,b))cue(p,PellTexts.thing(kind,stage));}
            case SHED_TV->{if(!PellTapes.tv(p,b))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.538: the bucket is taken, to scrub the line with (PellScrub); on the last night the fridge has a plate in it.
            case BUCKET->PellScrub.take(p,b,at,own);
            case FRIDGE->{if(!PellScrub.fridge(p,b,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.540: the lanyard in June's robe on the chair, the last night; the freezer it opens, and Nessa in it (PellFreezer).
            case POOL_CHAIR->{if(!PellFreezer.chair(p,b,st,own))cue(p,PellTexts.thing(kind,stage));}
            case FREEZER,FREEZER_OPEN->{if(!PellFreezer.hand(p,b,st,own))cue(p,PellTexts.thing(kind,stage));}
            // 0.4.548: the last page off the shed's wall and the note off the freezer's switch, to show Gail (PellMark).
            case TRANSCRIPT->{if(!PellMark.page(p,b,at,st,own))cue(p,PellTexts.thing(kind,stage));}
            case POWER->{if(!PellMark.note(p,b,at,st,own))cue(p,PellTexts.thing(kind,stage));}
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
