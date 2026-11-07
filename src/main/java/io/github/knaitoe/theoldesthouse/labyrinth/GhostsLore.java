package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.GhostsLorePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Optional things the family left behind. Reading them never calls or completes a take. */
public final class GhostsLore {
    public static final int DIARY=0,BANK=1,LAPTOP=2,DRAWING=3,PAD=4,CANISTERS=5;
    public static final BlockPos DIARY_AT=new BlockPos(-10,5,-4),BANK_AT=new BlockPos(-7,1,-16),
            LAPTOP_AT=GhostsSetRooms.EXORCISM_DESK.above(),EFFIGY_AT=new BlockPos(5,-4,-24),DRAWING_AT=new BlockPos(4,5,-17),
            // 0.4.325: Merry's pad on her nightstand, the canisters on the counter by the sink, and where she sets the empty jar down.
            PAD_AT=new BlockPos(10,6,-25),CANISTERS_AT=new BlockPos(-8,1,-25),JAR_AT=EFFIGY_AT.south();
    /** 0.4.325 (owner: "maybe have the shrine appear on day 5"): from the fifth day, before the rite. */
    public static final int SHRINE_DAY=5;
    private GhostsLore(){}
    public static void build(ServerLevel level,BlockPos base){
        level.setBlock(base.offset(BANK_AT),GhostsSetBlock.of(GhostsSetBlock.Kind.BANK_SLIPS,Direction.SOUTH),2);
        level.setBlock(base.offset(LAPTOP_AT),GhostsSetBlock.of(GhostsSetBlock.Kind.LAPTOP_CLOSED,Direction.SOUTH),2);
        level.setBlock(base.offset(EFFIGY_AT),GhostsSetBlock.of(GhostsSetBlock.Kind.STORED_CROSS,Direction.SOUTH),2);
        level.setBlock(base.offset(PAD_AT),GhostsSetBlock.of(GhostsSetBlock.Kind.PLAN_PAD,Direction.SOUTH),2);
        level.setBlock(base.offset(CANISTERS_AT),GhostsSetBlock.of(GhostsSetBlock.Kind.CANISTERS,Direction.SOUTH),2);
    }
    /** Each reader's film day supplies the appearance; the shared, non-colliding anchor never moves. */
    public static GhostsSetBlock.Kind laptop(int day){return day==6?null:day==7?GhostsSetBlock.Kind.LAPTOP_STUDY:GhostsSetBlock.Kind.LAPTOP_CLOSED;}
    /** 0.4.325: Merry's pad is by her bed only on the last day; the canisters stand open, a spoon across them, only on the last day. */
    public static GhostsSetBlock.Kind pad(int day){return day==7?GhostsSetBlock.Kind.PLAN_PAD:null;}
    public static GhostsSetBlock.Kind canisters(int day){return day==7?GhostsSetBlock.Kind.CANISTERS_OPEN:GhostsSetBlock.Kind.CANISTERS;}
    /** 0.4.325: the camera's shrine has the small jar on its bench until the last day, when the bench is bare. */
    public static GhostsSetBlock.Kind shrine(int day){return day>=7?GhostsSetBlock.Kind.BASEMENT_EFFIGY:GhostsSetBlock.Kind.BASEMENT_EFFIGY_JAR;}
    public static boolean click(ServerPlayer p,BlockPos base,BlockPos rel,CompoundTag own){
        if(!LiteraryVignettes.inside(p,GhostsSet.PLACE)||p.isSpectator()||p.distanceToSqr(base.offset(rel).getCenter())>36)return false;
        var state=p.serverLevel().getBlockState(base.offset(rel));int kind=-1,day=GhostsSet.day(own);
        if(rel.equals(DIARY_AT)&&BarrettHomeBlock.is(state,BarrettHomeBlock.Kind.NIGHTSTAND_SARAH))kind=DIARY;
        if(rel.equals(DRAWING_AT)&&BarrettHomeBlock.is(state,BarrettHomeBlock.Kind.BOOKSHELF))kind=DRAWING;
        if(state.is(LiteraryRegistry.GHOST_SET.get())){
            var fitting=state.getValue(GhostsSetBlock.KIND);
            if(rel.equals(BANK_AT)&&fitting==GhostsSetBlock.Kind.BANK_SLIPS)kind=BANK;
            if(rel.equals(LAPTOP_AT)&&fitting==GhostsSetBlock.Kind.LAPTOP_CLOSED&&day==7)kind=LAPTOP;
            if(rel.equals(PAD_AT)&&fitting==GhostsSetBlock.Kind.PLAN_PAD&&day==7)kind=PAD;
            if(rel.equals(CANISTERS_AT)&&fitting==GhostsSetBlock.Kind.CANISTERS&&day==7)kind=CANISTERS;
        }
        if(kind<0)return false;
        if(kind==DIARY)own.putInt("LoreDiaryDay",Math.max(day,own.getInt("LoreDiaryDay")));
        else if(kind==DRAWING)own.putInt("LoreDrawingDay",Math.max(day,own.getInt("LoreDrawingDay")));
        else own.putBoolean(kind==BANK?"LoreBank":kind==LAPTOP?"LoreLaptop":kind==PAD?"LorePad":"LoreCanisters",true);
        HousePackets.send(p,new GhostsLorePayload(kind,day));return true;
    }
    public static boolean lensImage(int day,boolean ownCamera,boolean basement){return day>=SHRINE_DAY&&ownCamera&&basement;}
    public static boolean inBasement(Vec3 at,BlockPos base){return new AABB(base.getX()+3,base.getY()-4.5,base.getZ()-26,base.getX()+12,base.getY()-.5,base.getZ()-13).contains(at);}
    public static boolean throughLens(ServerPlayer p,BlockPos base,CompoundTag own){
        var cross=p.serverLevel().getBlockState(base.offset(EFFIGY_AT));
        return cross.is(LiteraryRegistry.GHOST_SET.get())&&cross.getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.STORED_CROSS&&lensImage(GhostsSet.day(own),GhostsSet.recording(p),inBasement(p.position(),base));
    }
    public static void reader(ServerPlayer p,BlockPos base,CompoundTag own){
        GhostsSet.capture(p,own,"SarahDiary",Vec3.atLowerCornerOf(base.offset(DIARY_AT)).add(.5,.9,.5));
        var bank=p.serverLevel().getBlockState(base.offset(BANK_AT));
        if(bank.is(LiteraryRegistry.GHOST_SET.get())&&bank.getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.BANK_SLIPS)
            GhostsSet.capture(p,own,"BankSlips",Vec3.atLowerCornerOf(base.offset(BANK_AT)).add(.5,.1,.5));
        if(GhostsSet.day(own)>=SHRINE_DAY&&throughLens(p,base,own))GhostsSet.capture(p,own,"BasementEffigy",Vec3.atLowerCornerOf(base.offset(EFFIGY_AT)).add(.5,1.4,.5));
        if(GhostsSet.day(own)==7){
            var laptop=p.serverLevel().getBlockState(base.offset(LAPTOP_AT));
            if(laptop.is(LiteraryRegistry.GHOST_SET.get())&&laptop.getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.LAPTOP_CLOSED)
                GhostsSet.capture(p,own,"LaptopStudy",Vec3.atLowerCornerOf(base.offset(LAPTOP_AT)).add(.5,.32,.55));
            if(fitting(p,base,PAD_AT,GhostsSetBlock.Kind.PLAN_PAD))GhostsSet.capture(p,own,"PlanPad",Vec3.atLowerCornerOf(base.offset(PAD_AT)).add(.5,.05,.6));
            if(fitting(p,base,CANISTERS_AT,GhostsSetBlock.Kind.CANISTERS))GhostsSet.capture(p,own,"OpenCanisters",Vec3.atLowerCornerOf(base.offset(CANISTERS_AT)).add(.5,.3,.5));
            if(own.getBoolean(GhostsSet.MERRY_JAR))GhostsSet.capture(p,own,"EmptyJar",Vec3.atLowerCornerOf(base.offset(JAR_AT)).add(.5,.15,.28));
        }
    }
    private static boolean fitting(ServerPlayer p,BlockPos base,BlockPos rel,GhostsSetBlock.Kind kind){var s=p.serverLevel().getBlockState(base.offset(rel));return s.is(LiteraryRegistry.GHOST_SET.get())&&s.getValue(GhostsSetBlock.KIND)==kind;}
    public static List<String> pages(int kind,int day){
        // 0.4.325: Merry's pad. The signs she made for the last day, and under them, in the same printing, what her sister told her
        // they were going to do. Adapted from the plan Merry describes in the novel; the words are not quotations.
        if(kind==PAD)return List.of(
                "A pocket pad with a glittery cover. One of Marjorie's pens is clipped to it.\n\nThe first page, in Merry's capitals:\n\nWHAT DO WE DO?",
                "Signs, each written large and torn off. The torn stubs are still in the spiral.\n\nWHAT'S FOR DINNER?\nSPAGHETTI\nYES\nNO\nOKAY\nIS THAT IT?",
                "Smaller, crowded at the bottom of a page:\n\nMOM MAKES SPAGHETTI\nSAUCE NOT US\nM SAYS HER TUMMY HURTS\nKNOCK OUT (her word)\nRUN AWAY\nJAR TO POLICE\nDAD GETS HELP\nHE COMES BACK");
        if(kind==CANISTERS)return List.of(
                "The sugar and flour canisters stand open on the counter by the sink. Both lids are off.\n\nA teaspoon lies across the counter between them. There is white dust on the counter and on the spoon, and a little on the floor.\n\nNobody has been baking.");
        if(kind==DRAWING)return List.of(day>=4?"The folded sister-cats picture. The larger cat wears a grey hoodie; Merry has given the smaller cat glasses. Green vines have been drawn across the house and around both sisters.":"A folded picture tucked inside Merry's book. Two sister cats stand together. The larger wears a grey hoodie; the smaller has glasses. Their paws touch in the middle of the page.");
        if(kind==BANK)return List.of(
                "ACCOUNT ACTIVITY\n\nSalary credit: S. Barrett\nMortgage debit: RETURNED\n\nUnpaid item notice enclosed.\n\nThe notice has been folded around a payment slip from the production company.",
                "PAYMENT ADVICE\n\nSixth Finger Productions\nRelease on signing.\n\nThe mortgage due date is written on the reverse. A paper clip holds the two slips together.");
        if(kind==LAPTOP)return List.of(
                "A DECADE OF CASES\n\nFour recurring patterns in men who killed their families:\n\nLosing work or the role of provider.\nBlaming a partner for turning the children against him.\nJudging the family against rigid beliefs or expectations.\nBelieving that killing the family would protect them.",
                "SAVED ARTICLES\n\nA man is laid off. He kills his wife and children, then burns their house.\n\nAfter a divorce, a father and his two children die in a parked car.\n\nFurther reports about fathers and children, saved in the same bookmarks folder.",
                "BOOKMARKS\n\nBBC News\nHoward Journal of Criminal Justice\nFamily killings: study summary\n\nThe same paragraph is selected in the study. No answer has been typed beneath it.",
                "IMAGES\n\nThe search box has been cleared. The results are still there: rows of small jars and plastic bags, all of white powder, some with a spoon beside them for scale.\n\nOne picture has been opened large. A glass jar with a metal cap.");
        var all=List.of(
                "Marjorie missed school again. I called Dr. Hamilton while John was out. When he came home, we started arguing before I could finish telling him what she'd said.\n\nI need to call again tomorrow.",
                "They wanted another interview. I said the girls had homework. Barry asked which room he could set up in.\n\nI asked Ken if I could have the kitchen back for an hour. He told me to ask Barry.",
                "Work called it a leave of absence. They said I could come back when things settled down.\n\nI didn't ask what happens if they don't. I haven't told the girls.",
                "Called Mom after the girls were in bed. I said we might come to California.\n\nShe asked for how long. I couldn't tell her.\n\nI should have asked if she could come here.",
                "Folded contact slip\n\nA phone number and a California address, copied onto a scrap of paper.\n\nBelow, in a different pen:\nCall after six.\nAsk whether we can stay.");
        int count=1+(day>=3?1:0)+(day>=5?1:0)+(day>=6?2:0);return all.subList(0,count);
    }
    /** 0.4.325: the shrine is on tape if the camera held it for a second on any day it could be seen. */
    public static boolean effigyRecorded(CompoundTag own){int frames=0;for(int d=SHRINE_DAY;d<=7;d++)frames+=own.getCompound("FilmLog"+d).getInt("Frames_BasementEffigy");return frames>=20;}
    /** Whether the camera kept the jar on the shrine's bench (only before the last day). */
    public static boolean jarRecorded(CompoundTag own){int frames=0;for(int d=SHRINE_DAY;d<7;d++)frames+=own.getCompound("FilmLog"+d).getInt("Frames_BasementEffigy");return frames>=20;}
    public static int found(CompoundTag own){return (own.getInt("LoreDiaryDay")>0?1:0)+(own.getBoolean("LoreBank")?1:0)+(own.getBoolean("LoreLaptop")?1:0)+(own.getInt("LoreDrawingDay")>0?1:0)+(effigyRecorded(own)?1:0)+(own.getBoolean("LorePad")?1:0)+(own.getBoolean("LoreCanisters")?1:0);}
    public static String fragment(CompoundTag own){
        if(own.getBoolean("LorePad"))return "the plan written small at the bottom of Merry's pad";
        if(effigyRecorded(own))return jarRecorded(own)?"a jar of white powder on a basement shrine visible only through your camera":"a basement shrine visible only through your camera";
        if(own.getBoolean("LoreCanisters"))return "the sugar and flour left open beside a spoon";
        if(own.getBoolean("LoreLaptop"))return "the study Marjorie had saved about fathers who kill their families";
        if(own.getInt("LoreDiaryDay")>=6)return "Sarah considering a visit to California";
        if(own.getInt("LoreDiaryDay")>0)return "Sarah's private account of the girls' bad nights";
        if(own.getInt("LoreDrawingDay")>0)return own.getInt("LoreDrawingDay")>=4?"vines drawn across the sisters' picture":"the sister-cats picture inside Merry's book";
        return "a returned mortgage payment beside the production paperwork";
    }
    public static String account(CompoundTag own){
        var text=new StringBuilder();
        if(own.getBoolean("LoreBank"))text.append("The kitchen papers put a returned mortgage payment beside the production company's payment advice.\n\n");
        if(own.getInt("LoreDiaryDay")>0)text.append(own.getInt("LoreDiaryDay")>=6?"Sarah had written about leave from work and a possible visit to her parents in California.\n\n":"Sarah kept a private record of the girls' bad nights and her calls for help.\n\n");
        if(own.getBoolean("LoreLaptop"))text.append("Marjorie had selected a paragraph in a study of fathers who killed their families. Several news reports were bookmarked.\n\n");
        if(own.getInt("LoreDrawingDay")>0)text.append(own.getInt("LoreDrawingDay")>=4?"The sister-cats picture in Merry's book had vines drawn across it.\n\n":"Merry had kept a folded picture of two sister cats together in her book.\n\n");
        if(effigyRecorded(own))text.append(jarRecorded(own)?"My camera kept a shrine in the basement: cloths, a bench, the cross, and on the bench a small glass jar of white powder. Those things were not arranged there in the ordinary room.\n\n":"My camera kept a shrine in the basement: cloths, a bench and the cross. Those things were not arranged there in the ordinary room.\n\n");
        if(own.getBoolean("LorePad"))text.append("Merry's pad held the signs she made for the last day, and under them, in the same capitals, the plan: the sauce, the police, their father getting help and coming back.\n\n");
        if(own.getBoolean("LoreCanisters"))text.append("On the last day the sugar and the flour stood open on the counter, with a spoon and white dust between them. Nobody had been baking.\n\n");
        if(own.getBoolean(GhostsSet.MERRY_JAR))text.append("Afterwards Merry went down to the basement and set an empty jar on the floor in front of the cross.\n\n");
        return text.toString();
    }
    public static String footage(String subject,boolean raw,int day){return switch(subject){
        case "SarahDiary"->raw?"Sarah's closed notebook beside her bed. The writing inside is not visible in this shot.":"The diary is absent from the cut.";
        case "BankSlips"->raw?"The mortgage notice and the production payment are clipped together on the kitchen counter.":"The kitchen's financial papers are left out.";
        case "LaptopStudy"->raw?"The laptop is open to a study summary. A large 4 is visible above the paragraphs.":"The assembly does not show the saved pages on the laptop.";
        case "BasementEffigy"->raw?(day<7?"The viewfinder shows cloths, a bench and the cross in the basement, and a small capped jar of white powder on the bench. Without the camera, there is only the stored cross and rag.":"The viewfinder shows cloths, a bench and the cross in the basement. The bench is bare. Without the camera, there is only the stored cross and rag."):"The assembly leaves out the basement image.";
        case "PlanPad"->raw?"Merry's pad by her bed, a pen clipped to it. The pages are not readable in this shot.":"The pad is not in the cut.";
        case "OpenCanisters"->raw?"The sugar and flour canisters stand open on the kitchen counter, a spoon and white dust between them.":"The kitchen counter is not in the cut.";
        case "EmptyJar"->raw?"An empty glass jar on the basement floor in front of the cross.":"The basement floor is not in the cut.";
        default->"";
    };}
}
