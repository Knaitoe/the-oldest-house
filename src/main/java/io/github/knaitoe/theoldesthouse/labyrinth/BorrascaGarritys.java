package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.network.BorrascaLetterPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * 0.4.459, Borrasca (docs/BORRASCA_LIFE_PROPOSAL.md part 3, docs/BORRASCA_LETTER_0_4_459.md). The owner, 10 November: "Carry a
 * letter for Dorsey. He already mentions the Garritys' mailbox going untouched. On the second visit, he could hand you a letter to
 * leave under their porch door. Years later, the box is still on its post, with the family name barely legible under cloudy tape.
 * You remember where it is because you've been to that porch."
 *
 * <p>The friend's family has a house, the fifth on the street, across from the church. Its front door is its own and does not
 * open; its curtains are drawn. Every gate on the street has a mailbox on a post now, and theirs is the one that changes: as any
 * other on the first visit, its door hanging open on a week of mail on the second, rusted under a clouded label years on.
 *
 * <p>On the second visit, before the friend is taken, Mr. Dorsey says he has a letter for them, and asked again he hands it over
 * ({@link #KEY}, the reader's own as the comic is). Used, it is an envelope in the hand, which turns over (client.
 * BorrascaLetterScreen): who it is to, who it is from, and a flap that is sealed. It is never opened. A hand on their door with the
 * letter in it pushes it under: a corner shows at the door's foot from then on. A reader who comes back grown finds it where they
 * left it, and can draw it out; one who never took it to the door still has it, and can. Nothing in the vignette needs any of it.
 */
public final class BorrascaGarritys {
    private BorrascaGarritys(){}
    public static final String KEY=BorrascaBelongings.MAIL;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The house: its west and east walls, its north and south ends, and the row its door and path are on. */
    public static final int X0=-23,X1=-15,NORTH=-67,SOUTH=-59,MIDDLE=-63;
    /** The lower half of the front door, in the east wall; the porch board before it, where the envelope's corner shows. */
    public static final BlockPos DOOR=new BlockPos(X1,0,MIDDLE),PORCH=new BlockPos(X1+1,0,MIDDLE);
    /** Their mailbox, over its post on the fence line at the gate. */
    public static final BlockPos BOX=new BlockPos(-5,1,MIDDLE-1);
    /** The curtains: the cell inside each window, and the way each faces into the room. Each is two cells high. */
    private static final int[][] CURTAINS={{X1-1,MIDDLE-2,4},{X1-1,MIDDLE+2,4},{X0+1,MIDDLE-2,5},{X0+1,MIDDLE+2,5},{(X0+X1)/2,SOUTH-1,2},{(X0+X1)/2,NORTH+1,3}};

    public static final String DORSEY_HAS="Got a letter here for the Garritys. Nobody's emptied their box all week.";
    public static final String DORSEY_GIVES="Their box won't take another. You know their girl, don't you? It's the house across from the church. Push it under the porch door for me.";
    public static final String DORSEY_REMINDS="Under the porch door, mind. Not in the box.";
    public static final String DORSEY_LEFT="Under the door? Good. That's one I don't carry back.";
    public static final String DORSEY_MADDOX="The Maddox boy's mother asks me every day if I've seen him. Like I'd know. I just carry the mail.";
    public static final String DORSEY_STILL="That letter's still theirs. Push it under the door, and then go home.";
    public static final String DORSEY_GROWN="That's one of mine. I gave that to a kid, the week the Garrity girl went. ...Keep it. There's nobody there to want it.";
    public static final String KELLY_DOOR="That's my house. Mom's waxing the floors. Nobody goes in till they're dry.";
    public static final String KNOCK_FIRST="You knock. A radio is playing inside. A woman calls that Kelly is out, and to try the trail.";
    public static final String KNOCK_SECOND="You knock. A floorboard shifts inside. Nobody comes.";
    public static final String KNOCK_LATER="You knock. Nothing in there moves. The paint has come off the door in long curls.";
    public static final String PUSHED="You push the letter under the door. It goes in as far as its corner.";
    public static final String PUSHED_LATE="You push the letter under the door, years late. It goes in as far as its corner.";
    public static final String PUT_BACK="You push the letter back under the door. It goes in as far as its corner.";
    public static final String IN_PACK="You knock. Nobody comes. The letter is still in your pack.";
    public static final String CORNER="A corner of the envelope shows under the door. Nobody has taken it in.";
    public static final String CORNER_LATER="The corner of an envelope shows under the door, gone yellow. It has not been moved. It is loose.";
    public static final String CORNER_LOOSE="A corner of the envelope shows under the door. It is loose.";
    public static final String DRAWN_OUT="It comes out from under the door whole, still sealed. Nobody ever took it in.";
    public static final String DRAWN_AGAIN="It comes back out from under the door.";
    public static final String HANDS_FULL="Your hands are too full to take it.";
    public static final String BOX_FULL="The Garritys' mailbox. Its door hangs open on a week of mail.";
    public static final String BOX_LATER="A mailbox still on its post, rusted, its flag gone. The tape over the label has clouded. Under it, barely, GARRITY.";

    // ---------------------------------------------------------------------------------------------------------------- the lot
    /** The mailbox at one of the four households' gates: on the fence line, a cell north of the path. */
    public static BlockPos box(int house){return new BlockPos(BorrascaHomes.WEST[house]?-5:5,1,BorrascaHomes.NORTH[house]+3);}
    private static @Nullable String family(BlockPos rel){
        if(rel.equals(BOX))return "GARRITY";
        String[] names={"PRUITT","HOLLIS","WEBB","CORLEY"};
        for(int h=0;h<BorrascaHomes.HOUSES;h++)if(rel.equals(box(h)))return names[h];
        return null;
    }
    private static BlockState curtain(int facing){return BorrascaHomeBlock.of(BorrascaHomeBlock.Kind.CURTAIN,Direction.from3DDataValue(facing),0);}
    /** Inside the Garritys' walls, at the time the house is built: their door, their curtains, and a room nobody is let into. */
    public static void furnish(ServerLevel l,BlockPos b){
        BuildBlocks.set(l,b.offset(DOOR),BorrascaHomeBlock.of(BorrascaHomeBlock.Kind.DOOR,Direction.EAST,0),F);
        BuildBlocks.set(l,b.offset(DOOR.above()),BorrascaHomeBlock.of(BorrascaHomeBlock.Kind.DOOR_TOP,Direction.EAST,0),F);
        for(var c:CURTAINS)for(int y=1;y<=2;y++)BuildBlocks.set(l,b.offset(c[0],y,c[1]),curtain(c[2]),F);
        bed(l,b,X0+1,NORTH+1,Blocks.CYAN_BED);bed(l,b,X0+1,SOUTH-2,Blocks.LIGHT_BLUE_BED);
        NovelRooms.furniture(l,b.offset(X0+3,0,NORTH+1),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
        NovelRooms.furniture(l,b.offset(X1-3,0,SOUTH-3),HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.EAST);
        NovelRooms.furniture(l,b.offset(X1-4,0,SOUTH-3),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);
        NovelRooms.furniture(l,b.offset(X1-2,0,SOUTH-3),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        NovelRooms.furniture(l,b.offset(X1-3,0,SOUTH-2),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        for(int x=X0+3;x<=X0+5;x++)for(int z=MIDDLE-1;z<=MIDDLE;z++)BuildBlocks.set(l,b.offset(x,0,z),Blocks.LIGHT_GRAY_CARPET.defaultBlockState(),F);
    }
    private static void bed(ServerLevel l,BlockPos b,int x,int head,Block bed){
        BuildBlocks.set(l,b.offset(x,0,head+1),bed.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT),F);
        BuildBlocks.set(l,b.offset(x,0,head),bed.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD),F);
    }

    /**
     * The house in the reader's own year: the box as it is on this visit, the door and the curtains new or years on, and the
     * envelope's corner at the door's foot for as long as the letter lies there.
     */
    public static void keep(ServerLevel l,BlockPos b,CompoundTag own,int visit){
        var box=b.offset(BOX);if(!l.hasChunkAt(box)||!l.hasChunkAt(b.offset(DOOR))||!l.hasChunkAt(b.offset(X0,0,NORTH)))return;
        int stage=visit>=3?1:0;
        var now=l.getBlockState(box);
        if(now.is(BorrascaRegistry.HOME.get())){
            var want=visit==2?BorrascaHomeBlock.of(BorrascaHomeBlock.Kind.BOX_FULL,Direction.EAST,0):BorrascaHomeBlock.of(BorrascaHomeBlock.Kind.BOX,Direction.EAST,stage);
            if(!now.equals(want))l.setBlock(box,want,3);
        }
        year(l,b.offset(DOOR),stage);year(l,b.offset(DOOR.above()),stage);
        for(var c:CURTAINS)for(int y=1;y<=2;y++)year(l,b.offset(c[0],y,c[1]),stage);
        var porch=b.offset(PORCH);var there=l.getBlockState(porch);boolean lies=BorrascaHomeBlock.is(there,BorrascaHomeBlock.Kind.LETTER);
        if(lying(own)){
            // The envelope is yellow only if it has lain there since the childhood visit.
            var want=BorrascaHomeBlock.of(BorrascaHomeBlock.Kind.LETTER,Direction.WEST,stage==1&&own.getInt("MailFirstVisit")==2?1:0);
            if(!there.equals(want)&&(lies||there.isAir()))l.setBlock(porch,want,3);
        }else if(lies)l.setBlock(porch,Blocks.AIR.defaultBlockState(),3);
    }
    private static void year(ServerLevel l,BlockPos at,int stage){
        var s=l.getBlockState(at);if(s.is(BorrascaRegistry.HOME.get())&&s.getValue(BorrascaHomeBlock.STAGE)!=stage)l.setBlock(at,s.setValue(BorrascaHomeBlock.STAGE,stage),3);
    }
    /** Whether the letter is under their door now. */
    public static boolean lying(CompoundTag own){return own.getBoolean("MailLeft")&&!own.getBoolean("MailBack");}

    // ---------------------------------------------------------------------------------------------------------------- hands
    /** A hand on their door, or on any mailbox on the street. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        int visit=own.getInt("Visit");
        if(rel.equals(DOOR)||rel.equals(DOOR.above())){door(p,b,own);return true;}
        var s=p.serverLevel().getBlockState(b.offset(rel));
        if(!BorrascaHomeBlock.is(s,BorrascaHomeBlock.Kind.BOX)&&!BorrascaHomeBlock.is(s,BorrascaHomeBlock.Kind.BOX_FULL))return false;
        String name=family(rel);if(name==null)return false;
        BorrascaStory.cue(p,look(rel.equals(BOX),name,visit));return true;
    }
    /** What a mailbox is to look at: any of the street's, or the Garritys' on each of the reader's visits. */
    public static String look(boolean theirs,String name,int visit){
        if(theirs&&visit==2)return BOX_FULL;
        if(theirs&&visit>=3)return BOX_LATER;
        return "A mailbox on its post, the flag down. "+name+(visit>=3?", on a label under tape. The tape is new.":", on a label under clear tape.");
    }
    private static boolean inHand(ServerPlayer p,CompoundTag own){return BorrascaBelongings.valid(p,own,KEY,p.getMainHandItem())||BorrascaBelongings.valid(p,own,KEY,p.getOffhandItem());}
    /**
     * A hand on their door. With the letter in it, the letter goes under. With the letter lying there, its corner is what is seen,
     * and a grown reader's second hand draws it out. Otherwise it is a knock, and what a knock gets on that visit. Returns what
     * was shown or said.
     */
    public static String door(ServerPlayer p,BlockPos b,CompoundTag own){
        int visit=own.getInt("Visit");var at=b.offset(DOOR);
        if(inHand(p,own)&&!lying(own)){
            // Under the door it goes. It is the door's from now on, so it can be given again when a grown reader draws it out.
            if(BorrascaBelongings.remove(p,own,KEY).isEmpty())return shown(p,KNOCK_SECOND);
            own.putBoolean("MailLeft",true);own.putBoolean("MailBack",false);own.remove(KEY+"Issued");
            if(!own.contains("MailFirstVisit"))own.putInt("MailFirstVisit",visit);
            keep(p.serverLevel(),b,own,visit);BorrascaStory.heard(p,SoundEvents.BOOK_PAGE_TURN,at,.7F);
            return shown(p,visit<3?PUSHED:own.getBoolean("MailDrawn")?PUT_BACK:PUSHED_LATE);
        }
        if(lying(own)){
            if(visit<3)return shown(p,CORNER);
            // Years on it is loose under the door. The first hand finds it; the next draws it out.
            boolean old=own.getInt("MailFirstVisit")==2;
            if(!own.getBoolean("MailFound")){own.putBoolean("MailFound",true);return shown(p,old?CORNER_LATER:CORNER_LOOSE);}
            // One that has lain there since the childhood visit is the yellowed one. One pushed under late comes out as it went in.
            if(old){var s=BorrascaBelongings.original(p,own,KEY);s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(1));own.put(KEY+"Original",s.save(p.registryAccess()));}
            if(!BorrascaBelongings.takeAt(p,own,KEY,b.offset(PORCH)))return shown(p,HANDS_FULL);
            boolean again=!old||own.getBoolean("MailDrawn");
            own.putBoolean("MailBack",true);own.putBoolean("MailDrawn",true);keep(p.serverLevel(),b,own,visit);BorrascaStory.heard(p,SoundEvents.BOOK_PAGE_TURN,at,.7F);
            return shown(p,again?DRAWN_AGAIN:DRAWN_OUT);
        }
        if(BorrascaBelongings.has(p,own,KEY))return shown(p,IN_PACK);
        if(visit==1){
            var friend=BorrascaStory.friend(p,own);
            if(friend!=null&&friend.distanceToSqr(p)<144){BorrascaStory.say(p,friend,KELLY_DOOR);return KELLY_DOOR;}
        }
        return shown(p,visit==1?KNOCK_FIRST:visit==2?KNOCK_SECOND:KNOCK_LATER);
    }
    private static String shown(ServerPlayer p,String line){BorrascaStory.cue(p,line);return line;}

    // ---------------------------------------------------------------------------------------------------------------- Mr. Dorsey
    /**
     * Mr. Dorsey on the second visit, before the friend is taken: he has a letter for the Garritys; asked again he hands it over;
     * after that he has his other line, and one about where the letter goes or has gone.
     */
    public static String dorsey(ServerPlayer p,CompoundTag own,LiteraryActor dorsey){
        int n=own.getInt("DorseyMail");own.putInt("DorseyMail",n+1);
        if(n==0)return DORSEY_HAS;
        if(!own.getBoolean("MailGiven")){
            if(!BorrascaBelongings.takeAt(p,own,KEY,dorsey.blockPosition()))return DORSEY_HAS;
            own.putBoolean("MailGiven",true);return DORSEY_GIVES;
        }
        return n%2==0?DORSEY_MADDOX:own.getBoolean("MailLeft")?DORSEY_LEFT:DORSEY_REMINDS;
    }
    /** After she is taken: his own lines, but a letter he handed over and that is still in the reader's pack is still the Garritys'. */
    static String[] dorseyAfter(CompoundTag own,String[] lines){
        if(!own.getBoolean("MailGiven")||own.getBoolean("MailLeft"))return lines;
        var out=lines.clone();out[out.length-1]=DORSEY_STILL;return out;
    }
    /** Years on: what he says, once, to a grown reader who has the letter with them. Null when there is nothing. */
    static @Nullable String dorseyGrown(ServerPlayer p,CompoundTag own){
        if(own.getBoolean("DorseySawMail")||!own.getBoolean("MailGiven")||!BorrascaBelongings.has(p,own,KEY))return null;
        own.putBoolean("DorseySawMail",true);return DORSEY_GROWN;
    }

    // ---------------------------------------------------------------------------------------------------------------- the envelope
    /** The letter in the hand: an envelope, which turns over. */
    public static void look(ServerPlayer p,ItemStack s,CompoundTag own){HousePackets.send(p,new BorrascaLetterPayload(s.has(DataComponents.CUSTOM_MODEL_DATA)));}
    /** From the reader's screen: a side of the envelope they have had before them for a moment. */
    public static void seen(ServerPlayer p,boolean back){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.BORRASCA);
        if(!BorrascaBelongings.has(p,own,KEY))return;
        own.putInt("MailSeen",own.getInt("MailSeen")|(back?2:1));LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.BORRASCA,own);
    }

    // ---------------------------------------------------------------------------------------------------------------- the account
    /** The account: the letter, in the week it was given. */
    public static String carried(CompoundTag own){
        if(!own.getBoolean("MailGiven"))return "";
        String from=(own.getInt("MailSeen")&1)!=0?" The company's name was printed in its corner.":"";
        return " The second time, Mr. Dorsey gave me a letter for her family."+from+(own.getInt("MailFirstVisit")==2?" I pushed it under their porch door.":" I did not take it to their door.");
    }
    /** The account: the letter, years on. */
    public static String found(CompoundTag own){
        if(!own.getBoolean("MailGiven"))return "";
        if(!own.getBoolean("MailLeft"))return "";
        if(own.getInt("MailFirstVisit")>=3)return " I pushed the Garritys' letter under their door years late."+(own.getBoolean("MailBack")?" Then I drew it out again.":"");
        String still=" The letter was still under the Garritys' door. Nobody had taken it in.";
        return own.getBoolean("MailDrawn")?still+(own.getBoolean("MailBack")?" I have it now.":" I drew it out, and then I put it back."):own.getBoolean("MailFound")?still:"";
    }
}
