package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** These lives can be pieced together in any order. Nothing is a solution or a compulsory account. */
public final class DomesticStories {
    public record Piece(String id,BlockPos at,DomesticRecordBlock.Kind kind,Direction facing,String title,String author,List<String> pages){
        public BlockState state(){return DomesticRecordBlock.of(kind,facing);}
        public ItemStack book(){return HouseWriting.book(title,author,HouseWriting.WritingStyle.PLAIN,pages);}
    }
    private static Piece p(String id,int x,int y,int z,DomesticRecordBlock.Kind k,Direction f,String title,String author,String...pages){return new Piece(id,new BlockPos(x,y,z),k,f,title,author,List.of(pages));}
    public static final Map<String,List<Piece>> HOMES=Map.of(
        "family",List.of(
            p("rota",-11,1,-22,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"On the back of a receipt","Ruth","Tues\nCar's at the bakery. Keys in the blue mug.\n\nLeah can't have egg. PLEASE check the sandwiches. Trip form's underneath, they need it tomorrow.","Signed it.\nGot the shoes. Ben says they're tight but he's wearing two pairs of socks??\n\nI'll do Thursday. Leave your cup by the kettle, I'll make us one when I'm in."),
            p("drawing",-13,1,-17,DomesticRecordBlock.Kind.FAMILY_DRAWING,Direction.EAST,"The house with all the windows","Leah","A child's drawing. Five people fill the windows of a narrow house. The person downstairs has been rubbed out and redrawn upstairs with the others.\n\nOn the back: EVERYONE HOME. Leah, age 7."),
            p("packed",-18,1,-15,DomesticRecordBlock.Kind.NOTE,Direction.SOUTH,"Clothes for Monday","Ruth","Orange one's Leah's. Blue is Ben's. Nothing in the bottom bag fits anybody.\n\nDon't let him paint the kitchen door yet. I haven't copied the pencil marks. I know it's only a door just leave it till Monday.")),
        "students",List.of(
            p("rent",10,1,-10,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"Rent, gas, washing-up","Three hands","Mara: GAS. 17 EACH. Envelope on fridge.\nJules: 17 or 12?\nM: SEVENTEEN\nEli: mine's under the takeaway menu. Don't bin it.","I've put next month's in too. You don't need anybody else till June.\n\nSorry. I was going to tell you both last night and couldn't. Then you were asleep. Key's through the letterbox."),
            p("sleeve",12,1,-17,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"Inside the record sleeve","Eli","Mara,\nIt's yours. Stop giving it back.\n\nSide B jumps, just before the last one. You know where.\n\nE."),
            p("address",15,1,-8,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"Forwarding address","Mara","Jules -\nDo you have his new address? The one he left's scribbled out. Can't read any of it.\n\nI've boxed the books. They're on his desk. Please move your wet stuff.")),
        "drinker",List.of(
            p("photo",-7,1,-25,DomesticRecordBlock.Kind.PHOTO,Direction.NORTH,"A photograph behind glass","On the back","Kit's first ride without the little wheels. Martin is running beside the bicycle. Elaine has caught both of them laughing.\n\nJune, before the move."),
            p("award",-3,2,-35,DomesticRecordBlock.Kind.AWARD,Direction.NORTH,"County junior league, 1998","The engraved plate","To Martin Hale\nVolunteer Coach\nFor every Saturday, rain or shine.\n\nA folded team list is tucked under the base. Beside each name is a parent's telephone number. Martin has written: GET EVERYONE HOME."),
            p("drawing",-7,1,-31,DomesticRecordBlock.Kind.DRAWING,Direction.EAST,"Me and Dad","Kit","Two stick figures stand beside a blue bicycle. The taller one's hand is enormous, covering the smaller hand.\n\nME AND DAD. Underneath, in an adult hand: Best afternoon in years. Keep this one."),
            p("play",-2,1,-28,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"School hall, Thursday, six o'clock","Elaine","School hall. Thursday, 6pm.\nKit's a tree. She's been making the bloody thing all week so yes you do need to come.\n\nBlue shirt's clean. Back of the chair.","leave work 4.30\nalarm 5\n\nDon't stop for one."),
            p("reply",-4,1,-35,DomesticRecordBlock.Kind.FAMILY_DRAWING,Direction.SOUTH,"A folded sheet","Kit","It's ok Dad I said you had work.\n\nMum said bring the costume next time so you can see.\n\nDon't shout at Mum I asked her to ring you."),
            p("letter",-5,1,-25,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"No envelope","Martin","I was awake Elaine. I heard it. I turned it off.\n\nI keep putting work in this letter. It wasn't work. I sat there. She was waiting for me and I just sat there.","I'm not asking you to ring her for me. I was going to. I wrote that bit first.\n\nI'll ring at six. If she's busy she's busy. That's fine.")),
        "grandmother",List.of(
            p("recipe",-11,1,-2,DomesticRecordBlock.Kind.RECIPE,Direction.NORTH,"Apple cake","Ada","Apple cake\n4 oz butter\n4 sugar\n2 eggs\n2 apples, small pieces\nFlour - usual cupful\n\nOven 4. Check before the news.\n\nFor 6 (crossed out)\n4 (crossed out)\n2","Sunday - make the full one. Tom's bringing everybody now.\n\nFoil in bottom drawer. Wrap half for his train."),
            p("mending",-16,1,2,DomesticRecordBlock.Kind.NOTE,Direction.NORTH,"Mending to finish","Ada","Tom - coat pocket, the INSIDE one\nRuth blue skirt hem\nRadio chair (arm's loose)\n\nDON'T pack scissors yet. Tape them to the sewing tin when I'm done."),
            p("card",-18,1,-7,DomesticRecordBlock.Kind.COUPLE,Direction.SOUTH,"A card kept with the photograph","Tom","Mum,\nMeasured again. Chairs will go, both of them. Radio too. Stop trying to give it to next door.\n\nI'll come Saturday with the big car. Don't lift anything before I get there.\nTom")));
    private DomesticStories(){}
    public static List<Piece> pieces(String home){return HOMES.getOrDefault(home,List.of());}
    public static boolean revise(ServerLevel l,BlockPos at,String home,String id){
        if(!(l.getBlockEntity(at) instanceof DomesticRecordBlockEntity be))return false;
        var before=DomesticStoriesBefore142.pieces(home).stream().filter(p->p.id().equals(id)).findFirst();
        var after=pieces(home).stream().filter(p->p.id().equals(id)).findFirst();
        return before.isPresent()&&after.isPresent()&&be.revise(before.get().book(),after.get().book());
    }
    public static void plan(Map<BlockPos,BlockState> plan,String home){for(var p:pieces(home))plan.put(p.at,p.state());}
    public static void seed(ServerLevel l,BlockPos base,DomesticLives.Home h){for(var p:pieces(h.id())){var at=base.offset(p.at);if(l.getBlockState(at).getBlock()==DomesticRegistry.RECORD.get()&&l.getBlockEntity(at) instanceof DomesticRecordBlockEntity be){DomesticRecordSurfaces.settle(l,at);be.seed(p.book());}}}
}
