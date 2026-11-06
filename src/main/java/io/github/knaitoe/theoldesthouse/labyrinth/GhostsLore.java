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
    public static final int DIARY=0,BANK=1,LAPTOP=2,DRAWING=3;
    public static final BlockPos DIARY_AT=new BlockPos(-10,5,-4),BANK_AT=new BlockPos(-7,1,-16),
            LAPTOP_AT=GhostsSetRooms.EXORCISM_DESK.above(),EFFIGY_AT=new BlockPos(5,-4,-24),DRAWING_AT=new BlockPos(4,5,-17);
    private GhostsLore(){}
    public static void build(ServerLevel level,BlockPos base){
        level.setBlock(base.offset(BANK_AT),GhostsSetBlock.of(GhostsSetBlock.Kind.BANK_SLIPS,Direction.SOUTH),2);
        level.setBlock(base.offset(LAPTOP_AT),GhostsSetBlock.of(GhostsSetBlock.Kind.LAPTOP_CLOSED,Direction.SOUTH),2);
        level.setBlock(base.offset(EFFIGY_AT),GhostsSetBlock.of(GhostsSetBlock.Kind.STORED_CROSS,Direction.SOUTH),2);
    }
    /** Each reader's film day supplies the appearance; the shared, non-colliding anchor never moves. */
    public static GhostsSetBlock.Kind laptop(int day){return day==6?null:day==7?GhostsSetBlock.Kind.LAPTOP_STUDY:GhostsSetBlock.Kind.LAPTOP_CLOSED;}
    public static boolean click(ServerPlayer p,BlockPos base,BlockPos rel,CompoundTag own){
        if(!LiteraryVignettes.inside(p,GhostsSet.PLACE)||p.isSpectator()||p.distanceToSqr(base.offset(rel).getCenter())>36)return false;
        var state=p.serverLevel().getBlockState(base.offset(rel));int kind=-1,day=GhostsSet.day(own);
        if(rel.equals(DIARY_AT)&&BarrettHomeBlock.is(state,BarrettHomeBlock.Kind.NIGHTSTAND_SARAH))kind=DIARY;
        if(rel.equals(DRAWING_AT)&&BarrettHomeBlock.is(state,BarrettHomeBlock.Kind.BOOKSHELF))kind=DRAWING;
        if(state.is(LiteraryRegistry.GHOST_SET.get())){
            var fitting=state.getValue(GhostsSetBlock.KIND);
            if(rel.equals(BANK_AT)&&fitting==GhostsSetBlock.Kind.BANK_SLIPS)kind=BANK;
            if(rel.equals(LAPTOP_AT)&&fitting==GhostsSetBlock.Kind.LAPTOP_CLOSED&&day==7)kind=LAPTOP;
        }
        if(kind<0)return false;
        if(kind==DIARY)own.putInt("LoreDiaryDay",Math.max(day,own.getInt("LoreDiaryDay")));
        else if(kind==DRAWING)own.putInt("LoreDrawingDay",Math.max(day,own.getInt("LoreDrawingDay")));
        else own.putBoolean(kind==BANK?"LoreBank":"LoreLaptop",true);
        HousePackets.send(p,new GhostsLorePayload(kind,day));return true;
    }
    public static boolean lensImage(int day,boolean ownCamera,boolean basement){return day==7&&ownCamera&&basement;}
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
        if(GhostsSet.day(own)==7){
            var laptop=p.serverLevel().getBlockState(base.offset(LAPTOP_AT));
            if(laptop.is(LiteraryRegistry.GHOST_SET.get())&&laptop.getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.LAPTOP_CLOSED)
                GhostsSet.capture(p,own,"LaptopStudy",Vec3.atLowerCornerOf(base.offset(LAPTOP_AT)).add(.5,.32,.55));
            if(throughLens(p,base,own))GhostsSet.capture(p,own,"BasementEffigy",Vec3.atLowerCornerOf(base.offset(EFFIGY_AT)).add(.5,1.4,.5));
        }
    }
    public static List<String> pages(int kind,int day){
        if(kind==DRAWING)return List.of(day>=4?"The folded sister-cats picture. The larger cat wears a grey hoodie; Merry has given the smaller cat glasses. Green vines have been drawn across the house and around both sisters.":"A folded picture tucked inside Merry's book. Two sister cats stand together. The larger wears a grey hoodie; the smaller has glasses. Their paws touch in the middle of the page.");
        if(kind==BANK)return List.of(
                "ACCOUNT ACTIVITY\n\nSalary credit: S. Barrett\nMortgage debit: RETURNED\n\nUnpaid item notice enclosed.\n\nThe notice has been folded around a payment slip from the production company.",
                "PAYMENT ADVICE\n\nSixth Finger Productions\nRelease on signing.\n\nThe mortgage due date is written on the reverse. A paper clip holds the two slips together.");
        if(kind==LAPTOP)return List.of(
                "A DECADE OF CASES\n\nFour recurring patterns in men who killed their families:\n\nLosing work or the role of provider.\nBlaming a partner for turning the children against him.\nJudging the family against rigid beliefs or expectations.\nBelieving that killing the family would protect them.",
                "SAVED ARTICLES\n\nA man is laid off. He kills his wife and children, then burns their house.\n\nAfter a divorce, a father and his two children die in a parked car.\n\nFurther reports about fathers and children, saved in the same bookmarks folder.",
                "BOOKMARKS\n\nBBC News\nHoward Journal of Criminal Justice\nFamily killings: study summary\n\nThe same paragraph is selected in the study. No answer has been typed beneath it.");
        var all=List.of(
                "Marjorie missed school again. I called Dr. Hamilton while John was out. When he came home, we started arguing before I could finish telling him what she'd said.\n\nI need to call again tomorrow.",
                "They wanted another interview. I said the girls had homework. Barry asked which room he could set up in.\n\nI asked Ken if I could have the kitchen back for an hour. He told me to ask Barry.",
                "Work called it a leave of absence. They said I could come back when things settled down.\n\nI didn't ask what happens if they don't. I haven't told the girls.",
                "Called Mom after the girls were in bed. I said we might come to California.\n\nShe asked for how long. I couldn't tell her.\n\nI should have asked if she could come here.",
                "Folded contact slip\n\nA phone number and a California address, copied onto a scrap of paper.\n\nBelow, in a different pen:\nCall after six.\nAsk whether we can stay.");
        int count=1+(day>=3?1:0)+(day>=5?1:0)+(day>=6?2:0);return all.subList(0,count);
    }
    public static boolean effigyRecorded(CompoundTag own){return own.getCompound("FilmLog7").getInt("Frames_BasementEffigy")>=20;}
    public static int found(CompoundTag own){return (own.getInt("LoreDiaryDay")>0?1:0)+(own.getBoolean("LoreBank")?1:0)+(own.getBoolean("LoreLaptop")?1:0)+(own.getInt("LoreDrawingDay")>0?1:0)+(effigyRecorded(own)?1:0);}
    public static String fragment(CompoundTag own){
        if(effigyRecorded(own))return "a basement shrine visible only through your camera";
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
        if(effigyRecorded(own))text.append("My camera kept a shrine in the basement: cloths, a bench and the cross. Those things were not arranged there in the ordinary room.\n\n");
        return text.toString();
    }
    public static String footage(String subject,boolean raw,int day){return switch(subject){
        case "SarahDiary"->raw?"Sarah's closed notebook beside her bed. The writing inside is not visible in this shot.":"The diary is absent from the cut.";
        case "BankSlips"->raw?"The mortgage notice and the production payment are clipped together on the kitchen counter.":"The kitchen's financial papers are left out.";
        case "LaptopStudy"->raw?"The laptop is open to a study summary. A large 4 is visible above the paragraphs.":"The assembly does not show the saved pages on the laptop.";
        case "BasementEffigy"->raw?"The viewfinder shows cloths, a bench and the cross in the basement. Without the camera, there is only the stored cross and rag.":"The assembly leaves out the basement image.";
        default->"";
    };}
}
