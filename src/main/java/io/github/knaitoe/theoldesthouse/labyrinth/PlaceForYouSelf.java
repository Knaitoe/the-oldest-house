package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.698, A Place for You, slice 14 (docs/A_PLACE_FOR_YOU_0_4_698.md): recognition and the right to refuse it, and what the reader
 * keeps. The last part of item 9 of the plan.
 *
 * The coat hung on the end hook for the reader follows the House's settled read of how they present (SELF): a short jacket, a long
 * belted coat or a knitted one; none of theirs for a reader not read or UNSEEN. For an UNSEEN reader the hall glass is covered before
 * they come in and the face in their picture is rubbed out, and one person knows them anyway, by something they do: the routine they
 * chose, the way they fold a pillowcase, or standing behind their chair until everyone else has sat. Any reader can cover or uncover
 * the hall glass. Once the account has been read, the place card at the reader's place can be taken: the keepsake.
 */
public final class PlaceForYouSelf {
    private PlaceForYouSelf(){}

    /** The settled read of how the reader presents, or empty when the House was asked not to read them or never settled it. */
    public static String self(CompoundTag own){var snap=own.getCompound("Snap");return snap.getBoolean("Read")?snap.getString("Self"):"";}
    public static boolean unseen(CompoundTag own){return self(own).equals("UNSEEN");}
    /** The hooks' stage: 0 the household's two coats; 1 the reader's short jacket, 2 a long belted coat, 3 a knitted coat on the end hook in place of the household's second. */
    public static int coat(CompoundTag snap){if(!snap.getBoolean("Read"))return 0;return switch(snap.getString("Self")){case "MASC"->1;case "FEM"->2;case "ANDROGYNOUS"->3;default->0;};}

    // ------------------------------------------------------------------------------------------------ the hooks and the glass
    static boolean hooks(ServerPlayer p,CompoundTag own){
        PlaceForYou.cue(p,switch(coat(own.getCompound("Snap"))){
            case 1->"One of their coats on the first hook, and on the end one, a short jacket. They've hung it where you'd reach for it.";
            case 2->"One of their coats on the first hook, and on the end one, a long coat with a belt. They've hung it where you'd reach for it.";
            case 3->"One of their coats on the first hook, and on the end one, a knitted coat. They've hung it where you'd reach for it.";
            default->"Two coats on the hooks. Neither of them is yours.";});
        return true;
    }
    /** Any reader can cover the hall glass, or uncover it. Mara, if she said she'd covered it for them, lets it be. */
    static boolean glass(ServerPlayer p,PlaceForYou.House h,int stage){
        var l=p.serverLevel();var b=h.base;int next=stage==0?2:0;String set=stage==0?"covered":stage==1?"turned_back":"uncovered";
        PlaceForYouSite.glass(l,b,next);l.playSound(null,b.offset(PlaceForYouSite.GLASS),SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.5F,1.2F);
        var own=PlaceForYou.own(p);boolean first=!own.contains("GlassSet");PlaceForYou.edit(p,o->o.putString("GlassSet",set));
        PlaceForYou.cue(p,switch(set){case "covered"->"You hang the cloth over the hall glass.";case "turned_back"->"You turn the hall glass back round to face the hall.";default->"You take the cloth off the hall glass.";});
        // Her offer at the door was "Unless you'd rather I didn't."
        if(first&&next==0&&own.getString("Detail").equals("glass")){var m=PlaceForYou.actor(l,h,"Mara");if(m!=null&&p.distanceToSqr(m)<144)PlaceForYou.above(h,m,"All right. I won't put it back.");}
        return true;
    }

    // ------------------------------------------------------------------------------------------------ known anyway
    static final String BY_ROUTINE="That's how you always did it. Same order every time.",
        BY_FOLD="Thirds, then the corners in. You always folded them like that.",
        BY_CHAIR="You used to stand like that, behind your chair, till everyone else had sat down.";
    /** The first thing an UNSEEN reader does that someone knows them by; once. */
    static void known(ServerPlayer p,PlaceForYou.House h,String who,String how,String line){
        var own=PlaceForYou.own(p);if(!unseen(own)||!own.getString("KnownBy").isEmpty())return;
        var a=PlaceForYou.actor(p.serverLevel(),h,who);if(a==null)return;
        PlaceForYou.above(h,a,line);PlaceForYou.edit(p,o->{o.putString("KnownBy",who);o.putString("KnownHow",how);});
    }
    /** With the routine done in company or with help: the one with the reader knows the order they do it in. */
    static void routine(ServerPlayer p,PlaceForYou.House h,CompoundTag own){
        if(own.getString("Care").equals("private"))return;String helper=own.getString("Helper");known(p,h,helper.isEmpty()?"Mara":helper,"routine",BY_ROUTINE);
    }
    /** Folding the pillowcase with Mara upstairs. */
    static void fold(ServerPlayer p,PlaceForYou.House h){known(p,h,"Mara","fold",BY_FOLD);}
    private static final Map<UUID,Integer> BEHIND=new HashMap<>();
    /** Standing behind their own chair, looking at it, for two seconds, between supper and dinner, with their chair at the table. */
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        // After the account, the card is still at the reader's place; said once, near the table.
        if(own.getBoolean("Completed")&&!own.getBoolean("CardTaken")&&!own.getBoolean("CardCued")&&r.distanceToSqr(6.5,1,-15.5)<9&&PlaceForYouSite.inKitchen(r)){
            PlaceForYou.edit(p,o->o.putBoolean("CardCued",true));PlaceForYou.cue(p,"Your name is still on its card at your place.");}
        if(!unseen(own)||!own.getString("KnownBy").isEmpty()){BEHIND.remove(p.getUUID());return;}
        int ch=own.getInt("Chapter");
        boolean behind=ch>=PlaceForYou.SUPPER&&ch<=PlaceForYou.DINNER&&PlaceForYouSite.readerChair(l,h.base)&&Math.abs(r.x-8.5)<.75&&Math.abs(r.z+15.5)<.75&&r.y<1
            &&PlaceForYou.looking(p,h.base,PlaceForYouSite.READER_CHAIR,4);
        if(!behind){BEHIND.remove(p.getUUID());return;}
        int n=BEHIND.merge(p.getUUID(),1,Integer::sum);
        if(n>=40){BEHIND.remove(p.getUUID());known(p,h,PlaceForYou.actor(l,h,"Ren")!=null?"Ren":"Mara","chair",BY_CHAIR);}
    }

    // ------------------------------------------------------------------------------------------------ the place card
    /** Once the account has been read, the card at the reader's place is theirs to take. */
    static boolean card(ServerPlayer p,PlaceForYou.House h){
        var own=PlaceForYou.own(p);if(own.getBoolean("CardTaken")){PlaceForYou.cue(p,"You have the card.");return true;}
        var l=p.serverLevel();l.setBlock(h.base.offset(PlaceForYouSite.CARD),Blocks.AIR.defaultBlockState(),3);
        l.playSound(null,h.base.offset(PlaceForYouSite.CARD),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.6F,1.3F);
        var s=new ItemStack(Items.PAPER);s.set(DataComponents.ITEM_NAME,Component.literal("Place card"));
        LiteraryVignettes.give(p,LiteraryVignettes.mark(p,PlaceForYou.PLACE,"Card",s));
        PlaceForYou.edit(p,o->o.putBoolean("CardTaken",true));PlaceForYou.cue(p,"You fold your place card flat and keep it.");
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the account and the Witness line
    /** The coat, the glass and the picture, and who knew the reader without a face; empty when there is nothing to say. */
    public static String account(CompoundTag own){
        var t=new StringBuilder();
        switch(coat(own.getCompound("Snap"))){
            case 1->t.append("A short jacket hung on the end hook for me.");
            case 2->t.append("A long coat with a belt hung on the end hook for me.");
            case 3->t.append("A knitted coat hung on the end hook for me.");
            default->{}
        }
        if(unseen(own)){
            sep(t).append("The hall glass was covered before I came in, and in my picture on their fridge the face was rubbed out, as if by a thumb.");
            String how=own.getString("KnownHow"),who=own.getString("KnownBy");
            if(!how.isEmpty())t.append(" ").append(who).append(switch(how){
                case "routine"->" knew me anyway, by the order I did my "+PlaceForYouTexts.routine(own.getCompound("Snap").getInt("Routine"))+" in.";
                case "fold"->" knew me anyway, by how I folded a pillowcase.";
                default->" knew me anyway, by how I stood behind my chair until everyone else had sat down.";});
        }
        switch(own.getString("GlassSet")){
            case "covered"->sep(t).append("I hung a cloth over the hall glass.");
            case "uncovered"->sep(t).append("I took the cloth off the hall glass.");
            case "turned_back"->sep(t).append("I turned the hall glass back round to face the hall.");
            default->{}
        }
        return t.toString();
    }
    private static StringBuilder sep(StringBuilder t){if(t.length()>0)t.append(" ");return t;}
    /** The Witness line: for an UNSEEN reader someone knew, a sentence after the rest. */
    public static String witness(CompoundTag own,String text){
        if(!unseen(own)||own.getString("KnownBy").isEmpty())return text;
        return text+" Nobody there knew my face. "+own.getString("KnownBy")+" knew me anyway.";
    }
    /** How the reader left the house, for the House of Leaves; empty until the account has been read. */
    public static String leftIt(CompoundTag own){
        if(!own.getBoolean("Completed"))return "";
        return switch(own.getString("Outcome")){
            case "opened_the_house"->"with its door propped";
            case "kept_a_place"->"with your place laid";
            case "invited_the_past"->"to the man at the door";
            case "took_the_road"->"by the road";
            default->"";
        };
    }
}
