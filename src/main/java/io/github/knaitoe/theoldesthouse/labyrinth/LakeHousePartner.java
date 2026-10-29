package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * Who the reader goes down with (owner, October 28; docs/LAKE_HOUSE_PARTNER_0_4_152.md). The House's read of who the
 * reader is drawn to chooses: a young man in Tess's place for a reader drawn to men, Tess as a friend rather than a date
 * for a reader drawn to no one, and Tess on a first date for everyone else. Only a settled read changes it, and it is
 * chosen once, on the reader's first visit, so the partner never changes partway through the story.
 */
public final class LakeHousePartner {
    /** The reader's partner, saved with their lake house record: "tess", "theo" or "friend". */
    public static final String KEY="Partner0152";
    /** The reader has met their partner on the dock: the account and the Witness line may name them. */
    public static final String MET="MetPartner0152";
    public enum Partner {
        TESS("Tess",LiteraryActor.LAKE_TESS,true),
        THEO("Theo",LiteraryActor.LAKE_THEO,true),
        FRIEND("Tess",LiteraryActor.LAKE_TESS,false);
        public final String name;public final int role;public final boolean date;
        Partner(String name,int role,boolean date){this.name=name;this.role=role;this.date=date;}
    }
    private LakeHousePartner(){}

    public static Partner of(CompoundTag own){
        return switch(own.getString(KEY)){case "theo"->Partner.THEO;case "friend"->Partner.FRIEND;default->Partner.TESS;};
    }
    /** Chosen on a fresh first visit from a settled read; a reader who had already met Tess keeps her. */
    public static Partner choose(ServerPlayer p,CompoundTag own){
        if(own.contains(KEY))return of(own);
        boolean fresh=own.getInt("Visit")<=1&&!own.getBoolean("Ready")&&!own.getBoolean("TessGreeting");
        own.putString(KEY,!fresh?"tess":switch(HouseRead.drawnTo(p)){case "MEN"->"theo";case "NO_ONE"->"friend";default->"tess";});
        return of(own);
    }

    /** The first visit's greeting: a first date's nerves, or two friends with a slow weekend, then the rule. */
    public static String firstGreeting(Partner partner){
        return (partner.date?"I didn't think a first date would come with a culvert. ":"Some way to spend a Saturday. ")
            +"We can go through that tunnel. But if there's something down there, let's not ask how.";
    }
    /** Underwater the partner cannot speak. */
    public static String mouths(Partner partner){return partner.name+" mouths something, then points upward.";}
    public static String pointsUp(Partner partner){return partner.name+" points up.";}

    /** The account opens with who the reader went down with, once they have met. Older accounts have no opening. */
    public static String accountOpening(CompoundTag own){
        if(!own.contains(KEY)||!own.getBoolean(MET))return "";
        var partner=of(own);
        return partner.date?partner.name+" and I took the green canoe out on our first date.\n\n"
            :partner.name+" and I took the green canoe out one weekend, the way friends do.\n\n";
    }
    /** The Witness line names the partner the same way; a reader who never met them keeps the line as it was. */
    public static String witness(CompoundTag own,String text){
        if(own.getBoolean(LakeHousePairs.SIDE))return partnerWitness(own);
        if(!own.contains(KEY)||!own.getBoolean(MET))return text;
        var partner=of(own);
        return (partner.date?partner.name+" and I went out on the lake on our first date and found a house on the bottom of it."
                :partner.name+" and I went out on the lake one weekend, the way friends do, and found a house on the bottom of it.")
            +" Every time we went down it let us a little further in. We kept to one rule and didn't ask how. In the end I went back to meet whoever lived there.";
    }

    // ------------------------------------------------------------------------------------------------ from Tess's side
    /*
     * 0.4.156 (docs/LAKE_HOUSE_PAIRS_0_4_156.md): a second reader goes down in Tess's place. Their account and Witness line
     * are told from her side: the diver by name, the date or the weekend as the diver's partner was, and the ending as Tess
     * says goodbye to a reader alone.
     */
    private static String diver(CompoundTag own){var name=own.getString(LakeHousePairs.DIVER_NAME);return name.isEmpty()?"The one I went down with":name;}
    private static boolean date(CompoundTag own){return !"friend".equals(own.getString(LakeHousePairs.AS));}
    public static String partnerAccount(CompoundTag own){
        String diver=diver(own);
        String opening=(date(own)?diver+" and I took the green canoe out on our first date.":diver+" and I took the green canoe out one weekend, the way friends do.")
            +" There was a house on the bottom of the third lake, and every time we went down it let us a little further in. We kept to one rule and didn't ask how.";
        String ending=switch(own.getString("Outcome")){
            case LakeHouse.LEFT_THE_LIGHTS_ON->"THE LIGHTS\n\nEvery light in the house came on at once, under the raft. "+diver+" came out through the half door and didn't look back. It's still lit. I'm not going back down. I don't think "+diver+" should either.";
            case LakeHouse.STAYED->"THE RAFT\n\n"+diver+" didn't come up. I sat on the raft until it got light, and then "+diver+" was lying beside me, wet through. They tell me it was days. I can't do that again.";
            default->"THE EMPTY LAKE\n\n"+diver+" went back down to say hello to whoever lived there, and then there was nothing under us but water. I came out twice more on my own and sat in the canoe and looked down. There's nothing there. I have to stop coming.";
        };
        return opening+"\n\n"+ending+(own.getBoolean("RuleBroken")?"\n\nWe tested the rule. Nothing came loose, but someone walked upstairs afterward. We had asked how.":"");
    }
    public static String partnerWitness(CompoundTag own){
        String diver=diver(own);
        return (date(own)?diver+" and I went out on the lake on our first date and found a house on the bottom of it.":diver+" and I went out on the lake one weekend, the way friends do, and found a house on the bottom of it.")
            +" Every time we went down it let us a little further in. We kept to one rule and didn't ask how. In the end "+diver+" went back to meet whoever lived there, and I waited on the raft.";
    }
}
