package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Exact pre-revision stock, used only to recognize untouched, uncollected authored notes. */
public final class DomesticStoriesBefore142 {
    public record Piece(String id,BlockPos at,DomesticRecordBlock.Kind kind,Direction facing,String title,String author,List<String> pages){
        public BlockState state(){return DomesticRecordBlock.of(kind,facing);}
        public ItemStack book(){return HouseWriting.book(title,author,HouseWriting.WritingStyle.PLAIN,pages);}
    }
    private static Piece p(String id,int x,int y,int z,DomesticRecordBlock.Kind k,Direction f,String title,String author,String...pages){return new Piece(id,new BlockPos(x,y,z),k,f,title,author,List.of(pages));}
    public static final Map<String,List<Piece>> HOMES=Map.of(
        "family",List.of(
            p("rota",-11,1,-22,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"On the back of a receipt","Ruth","Tuesday: I'll leave the car at the bakery. Your keys are in the blue cup.\n\nThree lunches, no egg in Leah's. The permission slip is underneath this. Please sign it this time.","In another hand:\nSigned. Picked up the shoes too. I'll take Thursday if you can get Sunday off.\n\nDon't wash the cups yet. I want to sit down with you when I get in."),
            p("drawing",-13,1,-17,DomesticRecordBlock.Kind.FAMILY_DRAWING,Direction.EAST,"The house with all the windows","Leah","A child's drawing. Five people fill the windows of a narrow house. The person downstairs has been rubbed out and redrawn upstairs with the others.\n\nOn the back: EVERYONE HOME. Leah, age 7."),
            p("packed",-18,1,-15,DomesticRecordBlock.Kind.NOTE,Direction.SOUTH,"Clothes for Monday","Ruth","These are the ones that still fit. Orange jumper for Leah; the blue one is Ben's.\n\nPlease leave the pencil marks on the doorframe. The landlord said he'd paint over them, but I want to measure everybody once more before we go.")),
        "students",List.of(
            p("rent",10,1,-10,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"Rent, gas, washing-up","Three hands","MARA: I paid the gas. Someone owes me seventeen.\n\nJULES: Twelve. You cooked for the whole street.\n\nELI: Seventeen. I ate the difference.","Below, in small writing:\nI've left my share in the envelope. Don't cover next month for me. The room is empty now.\n\nI couldn't wake you both to say it."),
            p("sleeve",12,1,-17,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"Inside the record sleeve","Eli","Side B sticks. Lift the arm just before the last song.\n\nMara, this is yours. You always said it was mine because I bought it, but you were the one who played it every Sunday."),
            p("address",15,1,-8,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"Forwarding address","Mara","Jules,\nHe left a forwarding address and then crossed it out. I put the books in a box anyway.\n\nPlease don't turn that desk into a drying rack yet.")),
        "drinker",List.of(
            p("photo",-7,1,-25,DomesticRecordBlock.Kind.PHOTO,Direction.NORTH,"A photograph behind glass","On the back","Kit's first ride without the little wheels. Martin is running beside the bicycle. Elaine has caught both of them laughing.\n\nJune, before the move."),
            p("award",-3,2,-35,DomesticRecordBlock.Kind.AWARD,Direction.NORTH,"County junior league, 1998","The engraved plate","To Martin Hale\nVolunteer Coach\nFor every Saturday, rain or shine.\n\nA folded team list is tucked under the base. Beside each name is a parent's telephone number. Martin has written: GET EVERYONE HOME."),
            p("drawing",-7,1,-31,DomesticRecordBlock.Kind.DRAWING,Direction.EAST,"Me and Dad","Kit","Two stick figures stand beside a blue bicycle. The taller one's hand is enormous, covering the smaller hand.\n\nME AND DAD. Underneath, in an adult hand: Best afternoon in years. Keep this one."),
            p("play",-2,1,-28,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"School hall, Thursday, six o'clock","Elaine","Kit is the tree. No lines, but she wants you to see the costume.\n\nWear the blue shirt. You said you'd come straight from work.","On the reverse:\nShirt washed. Alarm set for five.\nDon't stop on the way."),
            p("reply",-4,1,-35,DomesticRecordBlock.Kind.FAMILY_DRAWING,Direction.SOUTH,"A folded sheet","Kit","It's all right, Dad.\nI told Miss Bell you had work.\n\nMum says I can show you the costume next time. Please don't be cross with her."),
            p("letter",-5,1,-25,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"No envelope","Martin","Elaine,\nI keep starting this with what happened at work. That isn't why I missed it.\n\nI heard the alarm. I turned it off. I remember doing that much.","I want to ask you to tell Kit I tried, but that would be asking you to do it again.\n\nThe rest of the sheet is blank.")),
        "grandmother",List.of(
            p("recipe",-11,1,-2,DomesticRecordBlock.Kind.RECIPE,Direction.NORTH,"Apple cake","Ada","Two apples, four ounces butter, four sugar, two eggs.\n\nEnough for six.\n\nSix has been crossed out. Then four. A small 2 is written beside the last line.","In fresh pencil:\nMake the full one on Sunday. They are coming after all.\n\nLeave half for Tom to take back on the train."),
            p("mending",-16,1,2,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"Mending to finish","Ada","Tom's coat: the inside pocket, not the outside one.\nRuth's blue hem.\nThe chair by the radio.\n\nDon't pack the good scissors. I still need them this week."),
            p("card",-18,1,-7,DomesticRecordBlock.Kind.COUPLE,Direction.SOUTH,"A card kept with the photograph","Tom","Mum,\nWe've measured. Both chairs will fit. So will the little radio.\n\nYou can keep the room just how you want it. We only need you to let us carry the boxes.")));
    private DomesticStoriesBefore142(){}
    public static List<Piece> pieces(String home){return HOMES.getOrDefault(home,List.of());}
}
