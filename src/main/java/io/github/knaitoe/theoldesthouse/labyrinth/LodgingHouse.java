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

/**
 * 0.4.549, The Ones Already Inside (docs/THE_ONES_ALREADY_INSIDE_PLAN.md). The lodging house, its doors and hatches, and from 0.4.550
 * what is in its rooms. The house is not dealt until its story can be finished.
 *
 * <p>Doors open and shut to a hand, both cells. Daniel's has its bolt on the passage side: a first hand draws the bolt and the next
 * opens it, and shut again it stays unbolted until somebody shoots the bolt. The jib door is flush with the wall until it is opened.
 * Hatches slide open and shut from either side. Frames, windows and the rest answer with a line.
 */
public final class LodgingHouse {
    private LodgingHouse(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.ONES_INSIDE;
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    /** Not until the story can be finished: it is being built in slices. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){}
    /** Every reader tick. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        if(!own.getBoolean("WentUp")&&LodgingSite.upstairs(r)){own.putBoolean("WentUp",true);save(p,own);}
        if(!own.getBoolean("WentDown")&&LodgingSite.inCellar(r)){own.putBoolean("WentDown",true);save(p,own);}
    }
    public static void clearAll(){}

    /** A hand on something of the house's. True if it was the house's to answer. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(st.is(LodgingRegistry.WALL.get())){
            var part=st.getValue(LodgingWall.PART);
            if(part!=LodgingWall.Part.HATCH&&part!=LodgingWall.Part.HATCH_OPEN)return false;
            hatch(l,b.offset(rel),part==LodgingWall.Part.HATCH);return true;
        }
        // 0.4.550: what is in the rooms says what it is. A chair, an armchair or the settee seats an empty hand before this (HouseSitting).
        if(LodgingThing.is(st)){cue(p,LodgingTexts.thing(st,rel));return true;}
        if(!st.is(LodgingRegistry.FITTING.get()))return false;
        var kind=st.getValue(LodgingBlock.KIND);
        // The terms on the hall table and the bench are the first paper's and the account's: the House's own reading answers them.
        if(kind==LodgingBlock.Kind.HALL_TABLE||kind==LodgingBlock.Kind.BENCH)return false;
        switch(kind){
            case FRAME_SIDE,FRAME_HEAD,FRAME_BACK->{cue(p,(st.getValue(LodgingBlock.STAGE)&(kind==LodgingBlock.Kind.FRAME_BACK?2:kind==LodgingBlock.Kind.FRAME_SIDE?4:0))!=0
                ||kind==LodgingBlock.Kind.FRAME_HEAD&&st.getValue(LodgingBlock.STAGE)>=3?LodgingTexts.DANIELS_FRAME:LodgingTexts.FRAME);return true;}
            case WINDOW->{cue(p,LodgingTexts.SHUTTERS);return true;}
            case HALL_STAND->{cue(p,LodgingTexts.GLASS);return true;}
            case FIREPLACE->{cue(p,LodgingTexts.GRATE);return true;}
            case RATCHET->{cue(p,LodgingTexts.RATCHET);return true;}
            case SHOE,POST->{cue(p,rel.getX()==LodgingSite.DEAD_POST.getX()&&rel.getZ()==LodgingSite.DEAD_POST.getZ()?LodgingTexts.DEAD_POST:LodgingTexts.POST);return true;}
            case NUMBER,TALLY,SILL,COAT_RAIL,LAMP,RAIL,BANISTER,LADDER->{return kind==LodgingBlock.Kind.TALLY?say(p,LodgingTexts.TALLY):true;}
            default->{}
        }
        var door=LodgingSite.doorAt(l,b,rel);if(door==null)return true;
        var lower=l.getBlockState(b.offset(door));
        if(LodgingBlock.bolted(lower)){
            LodgingSite.leaf(l,b,door,LodgingBlock.Kind.BOLT_DOOR,lower.getValue(LodgingBlock.FACING),false,0);
            l.playSound(null,b.offset(door),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.6F,1.3F);cue(p,LodgingTexts.BOLT_DRAWN);return true;
        }
        toggle(l,b,door);
        return true;
    }
    private static boolean say(ServerPlayer p,String line){cue(p,line);return true;}
    /** Opens a shut door and shuts an open one, both cells of it. Daniel's keeps its bolt drawn. */
    public static void toggle(ServerLevel l,BlockPos b,BlockPos door){
        var at=b.offset(door);var st=l.getBlockState(at);if(!st.is(LodgingRegistry.FITTING.get())||!st.getValue(LodgingBlock.KIND).leaf())return;
        var kind=st.getValue(LodgingBlock.KIND);boolean open=LodgingBlock.open(st);Direction f=st.getValue(LodgingBlock.FACING);
        LodgingSite.leaf(l,b,door,kind,f,!open,0);
        l.playSound(null,at,kind==LodgingBlock.Kind.SERVICE?(open?SoundEvents.WOODEN_TRAPDOOR_CLOSE:SoundEvents.WOODEN_TRAPDOOR_OPEN):(open?SoundEvents.WOODEN_DOOR_CLOSE:SoundEvents.WOODEN_DOOR_OPEN),
            SoundSource.BLOCKS,.7F,kind==LodgingBlock.Kind.CUPBOARD?1.2F:1F);
    }
    /** Shoots Daniel's bolt, if his door is shut. */
    public static boolean bolt(ServerLevel l,BlockPos b){
        var st=l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR));if(LodgingBlock.open(st)||LodgingBlock.bolted(st))return false;
        LodgingSite.leaf(l,b,LodgingSite.DANIEL_DOOR,LodgingBlock.Kind.BOLT_DOOR,st.getValue(LodgingBlock.FACING),false,4);return true;
    }
    /** Slides a hatch open or shut. */
    public static void hatch(ServerLevel l,BlockPos at,boolean open){
        var st=l.getBlockState(at);if(!st.is(LodgingRegistry.WALL.get()))return;
        l.setBlock(at,st.setValue(LodgingWall.PART,open?LodgingWall.Part.HATCH_OPEN:LodgingWall.Part.HATCH),3);
        l.playSound(null,at,open?SoundEvents.WOODEN_TRAPDOOR_OPEN:SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.5F,1.4F);
    }
    public static boolean hatchOpen(ServerLevel l,BlockPos b,BlockPos rel){var st=l.getBlockState(b.offset(rel));return st.is(LodgingRegistry.WALL.get())&&st.getValue(LodgingWall.PART)==LodgingWall.Part.HATCH_OPEN;}
}
