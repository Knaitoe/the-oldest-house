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
    /** 0.4.551: six readers to a house; a seventh is given a house of their own (SceneInstances.PARTIES). */
    public static final int PARTY=6;
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    /** 0.4.618: dealt to anybody who has not read their own account of it. */
    public static boolean dealable(LabyrinthData d,UUID reader){return !LiteraryVignettes.personal(d,reader,PLACE).getBoolean("Completed");}
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){LodgingPeople.ensure(p.serverLevel(),b);LodgingParty.arrive(p,b,own);}
    /** Everybody reading in this house. */
    static java.util.List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new java.util.ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    /** Every reader tick. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        // 0.4.602: the house's people, made if they are not there (after a reload they are taken up again).
        LodgingPeople.ensure(p.serverLevel(),b);
        if(!own.getBoolean("WentUp")&&LodgingSite.upstairs(r)){own.putBoolean("WentUp",true);save(p,own);}
        if(!own.getBoolean("WentDown")&&LodgingSite.inCellar(r)){own.putBoolean("WentDown",true);save(p,own);}
    }
    public static void clearAll(){LodgingPeople.clearAll();LodgingTaken.clearAll();}

    /** A hand on something of the house's. True if it was the house's to answer. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(st.is(LodgingRegistry.WALL.get())){
            var part=st.getValue(LodgingWall.PART);
            // 0.4.612: from inside the wall, a hand on the lath knocks (LodgingTaken).
            if(part!=LodgingWall.Part.HATCH&&part!=LodgingWall.Part.HATCH_OPEN)return LodgingTaken.knock(p,b,rel);
            if(LodgingDaniel.hatch(p,b,rel))return true;
            hatch(l,b.offset(rel),part==LodgingWall.Part.HATCH);
            if(part==LodgingWall.Part.HATCH)LodgingTaken.hatchFromWall(p,b,rel);
            return true;
        }
        // 0.4.550: what is in the rooms says what it is. A chair, an armchair or the settee seats an empty hand before this (HouseSitting).
        if(LodgingThing.is(st)){cue(p,LodgingTexts.thing(st,rel));return true;}
        if(!st.is(LodgingRegistry.FITTING.get()))return false;
        var kind=st.getValue(LodgingBlock.KIND);
        // The terms on the hall table and the bench are the first paper's and the account's: the House's own reading answers them.
        if(kind==LodgingBlock.Kind.HALL_TABLE||kind==LodgingBlock.Kind.BENCH)return false;
        switch(kind){
            case FRAME_SIDE,FRAME_HEAD,FRAME_BACK->{
                // 0.4.605: the straps of Daniel's frame, felt from inside his room.
                // 0.4.608: the key put to them opens them; open, they hang.
                boolean danielsFrame=kind==LodgingBlock.Kind.FRAME_SIDE&&(st.getValue(LodgingBlock.STAGE)&4)!=0||kind==LodgingBlock.Kind.FRAME_BACK&&(st.getValue(LodgingBlock.STAGE)&3)==2;
                if(danielsFrame&&LodgingSite.inDaniels(p.position().subtract(b.getX(),b.getY(),b.getZ()))){
                    // 0.4.615: the catch in the jamb, for a reader who knows the third way and has no key in hand (LodgingSever).
                    boolean key=LodgingKey.is(p.getMainHandItem(),b)||LodgingKey.is(p.getOffhandItem(),b);
                    if(kind==LodgingBlock.Kind.FRAME_SIDE&&!key&&LodgingSever.catch_(p,b))return true;
                    if(!LodgingKey.straps(p,b))LodgingDaniel.straps(p);return true;}
                cue(p,(st.getValue(LodgingBlock.STAGE)&(kind==LodgingBlock.Kind.FRAME_BACK?2:kind==LodgingBlock.Kind.FRAME_SIDE?4:0))!=0
                ||kind==LodgingBlock.Kind.FRAME_HEAD&&st.getValue(LodgingBlock.STAGE)>=3?LodgingTexts.DANIELS_FRAME:LodgingTexts.FRAME);return true;}
            case WINDOW->{cue(p,LodgingTexts.SHUTTERS);return true;}
            case HALL_STAND->{cue(p,LodgingTexts.GLASS);return true;}
            case FIREPLACE->{cue(p,LodgingTexts.GRATE);return true;}
            // 0.4.615: a reader who knows the third way lifts its pawl (LodgingSever).
            case RATCHET->{if(!LodgingSever.ratchet(p,b))cue(p,LodgingTexts.RATCHET);return true;}
            case SHOE,POST->{
                // 0.4.610: the dead post's wedge kicks out, and back in (LodgingCellar).
                if(rel.getX()==LodgingSite.DEAD_POST.getX()&&rel.getZ()==LodgingSite.DEAD_POST.getZ()){LodgingCellar.shoe(p,b);return true;}
                cue(p,LodgingTexts.POST);return true;}
            case NUMBER,TALLY,SILL,COAT_RAIL,LAMP,RAIL,BANISTER,LADDER->{return kind==LodgingBlock.Kind.TALLY?say(p,LodgingTexts.TALLY):true;}
            default->{}
        }
        var door=LodgingSite.doorAt(l,b,rel);if(door==null)return true;
        if(door.equals(LodgingSite.DANIEL_DOOR)&&LodgingDaniel.door(p,b))return true;
        // 0.4.612: the jib door's latch, down on the house's side after a taking (LodgingTaken).
        if(door.equals(LodgingSite.JIB)&&LodgingTaken.jib(p,b))return true;
        var lower=l.getBlockState(b.offset(door));
        if(LodgingBlock.bolted(lower)){
            LodgingSite.leaf(l,b,door,LodgingBlock.Kind.BOLT_DOOR,lower.getValue(LodgingBlock.FACING),false,0);
            l.playSound(null,b.offset(door),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.6F,1.3F);cue(p,LodgingTexts.BOLT_DRAWN);return true;
        }
        toggle(l,b,door);
        // 0.4.608: what Rae left in the cupboard under the stair.
        if(door.equals(LodgingSite.CUPBOARD))LodgingKey.cupboard(p,b);
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
