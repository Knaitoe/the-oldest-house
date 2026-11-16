package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.632, A Little Longer, the twelfth slice (docs/A_LITTLE_LONGER_0_4_632.md): personhood. The proposal: "SELF affects established
 * address... HAIR can support one practical callback, such as offering a cloth... HOME can change the first question about staying or
 * leaving. Rowan still answers as Rowan... Unknown, uncertain, tentative, and opted-out states have neutral writing."
 *
 * <p>Only settled reads count (HouseRead, with the operator's switches). Every other state gets the line as it was written before this
 * slice, word for word. Who Rowan is drawn as was already the settled DRAWN_TO read (LongerRowan.castFor); AGE has one adult cast per body
 * and changes nothing yet.
 */
public final class LongerSelf {
    private LongerSelf(){}
    /** The reader's record: Rowan offered the cloth after the roof. */
    public static final String CLOTH="Cloth0632";

    /** Rowan's first request at the bench, with an address only for a settled SELF read. */
    public static String askHold(ServerPlayer p){
        return HouseRead.address(p,LongerTexts.ASK_HOLD,LongerTexts.ASK_HOLD_MASC,LongerTexts.ASK_HOLD_FEM,LongerTexts.ASK_HOLD_ANDRO);
    }
    /** Rowan's first question about staying, by a settled HOME read. */
    public static String stayFor(ServerPlayer p){
        return switch(HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,true)){
            case "HAVEN"->LongerTexts.STAY_HAVEN;case "DANGER"->LongerTexts.STAY_DANGER;case "RUNAWAY"->LongerTexts.STAY_RUNAWAY;default->LongerTexts.STAY_FOR;};
    }
    /** The one practical callback: after the roof's rain, a dry tea towel, for a settled HAIR read only. Null when there is none to offer. */
    public static String cloth(ServerPlayer p,CompoundTag own){
        if(own.getBoolean(CLOTH))return null;
        var hair=HouseRead.hair(p,true);if(hair.isEmpty())return null;
        own.putBoolean(CLOTH,true);
        return hair.get().equals("WRAPPED")?LongerTexts.CLOTH_WRAPPED:LongerTexts.CLOTH;
    }
}
