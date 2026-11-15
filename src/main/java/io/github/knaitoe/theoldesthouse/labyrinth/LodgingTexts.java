package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.549, The Ones Already Inside: what is written. The story is the owner's own (docs/THE_ONES_ALREADY_INSIDE_PROPOSAL.md); every
 * line here is new. Nothing here says what the reader is like, how they feel, or what they are called.
 */
public final class LodgingTexts {
    private LodgingTexts(){}
    public static final String HINT="The terms of the house are on the hall table.";
    public static final String FRAME="A doorcase set into the outside wall. Behind it there is only plaster, a hand's depth back, and the plaster is warm.",
        DANIELS_FRAME="A doorcase in the back wall. The plaster inside it is worn smooth at the height of a pair of shoulders.",
        SHUTTERS="The shutters are screwed shut from this side. The screws are painted over.",
        GLASS="The hall glass. It is a moment late, or the light in here is.",
        GRATE="The grate is laid and has never been lit. The coal is painted.",
        RATCHET="An iron wheel with a pawl in its teeth, and a lever to lift the pawl. A chain goes up from it into the floor above.",
        POST="A post under one of the rooms upstairs, its foot in an iron shoe. A wedge is driven in beside it.",
        DEAD_POST="A post with its own shoe and wedge, standing on the bare floor. Nothing above it. Rope burns go round it at the height of a pair of wrists.",
        TALLY="Marks in fives, scratched with something blunt. Then marks that are not in fives. Then marks that are only lines.",
        BOLT_DRAWN="The bolt is on this side. It slides back easily; it is used.";
    // 0.4.602: the four, in the panel. Their lines are the neutral script's (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, N1 to N3), as it has
    // them; RAE_THANKS, the reply labels R_WRONG_DAN, R_LEAVE_DAN and R_NOTHING, and the grey lines are new here.
    public static final String[] RAE_REQUEST={"Oh, thank God. You're staying here? You must be. Nobody comes in that door who isn't.","I need a favour. It's a small one.",
        "This is Dan. He isn't well. The woman who keeps this house said she'd see him, and I have to go and find her.","Will you stay with him? Only till I'm back. He doesn't want to be on his own with her."};
    public static final String[] DAN_FINE={"I'm fine.","I'm not fine. But I can sit in a hall on my own.","You don't have to. She asks everybody for things."};
    public static final String R_STAY="I'll stay with him.",R_WRONG="What's wrong with him?",R_PLACE="What is this place?",R_VERA="I'd rather see this Vera first.",R_NOT="It isn't my business.";
    public static final String RAE_HAND="His hand, to start with. Show them, Dan.",DAN_SHOWS="Dan takes his hand out of his pocket. The first finger stays bent when the others open.",
        DAN_SPRING="Since the spring. And other things.",RAE_ARRANGEMENT="There was an arrangement here once. Vera promised to put it right if it ever went wrong. It's gone wrong.",
        DAN_PLACE="A boarding house. I stayed here once, a long time ago. I remember the stairs and not much else.",RAE_COME="Then come with me. She'll be upstairs, or in the back.",
        RAE_NO="No. It isn't.",DAN_GOOD="Good for you.",RAE_THANKS="Thank you. I won't be long.",RAE_WATCHES="Rae is watching the stair.";
    public static final String R_WRONG_DAN="What's wrong?",R_LEAVE_DAN="Leave him be.",DAN_SLEEPING="I've stopped sleeping. I don't like what my hands do when I'm not watching them.",
        DAN_SITS="Dan keeps one hand in his coat pocket.",DAN_LOOKS_UP="Dan looks up.";
    public static final String[] VERA_GUESTS={"You'll be the new guests. Good. Your rooms are ready.","Nobody touches anything in this house that isn't theirs. You'll find that easy."};
    // 0.4.604: N1's end and N2 (docs/THE_ONES_ALREADY_INSIDE_PLAN.md). VERA_ROOMS, VERA_YOURS, VERA_EITHER_WAY, VERA_DONT, DAN_NOT_HOW and
    // the four reply labels are the script's; RAE_GOES, the grey lines about the bodies and RAE_BUSY and VERA_BUSY are new here.
    public static final String RAE_GOES="I'll go up and find her. She won't have heard the door.";
    public static final String[] VERA_ROOMS={"You'll be the new guests. Good. Your rooms are ready.","Up the stairs. Your names are on the doors.",
        "Nobody touches anything in this house that isn't theirs. You'll find that easy.","Let's have a look at you. Not here."};
    public static final String R_WHO_THAT="Who is that?",R_WHAT_THIS="What is this?",R_NOT_STAYING="I'm not staying in here.",R_LEAVE_IT="Leave it.";
    public static final String[] VERA_YOURS={"That's yours. It isn't finished.","It wants a week of looking at you. You'd be surprised how little it needs.",
        "When it's ready it can go out in your place for a while, and you can rest. Everybody here was tired once."};
    public static final String VERA_EITHER_WAY="Nobody makes you. The room is yours either way.",VERA_DONT="Don't. It isn't anybody's yet. You'll only spoil the work.",
        DAN_NOT_HOW="That's not how it was. That's not how it was for me.";
    public static final String DOUBLE_OWN="Asleep under the blanket is a body with your face. The skin is too smooth. Its eyes move under the lids.",
        DOUBLE_OWN_AWAKE="Its eyes are open. It is not looking at you.",
        DOUBLE_WAKES="It opens its eyes. It does not look at you. It turns its head on the pillow and looks toward the rest of the house.",
        DOUBLE_STRUCK="Nothing marks where the blow landed. It goes on breathing.",
        RAE_BUSY="Rae keeps close to Dan.",VERA_BUSY="Vera does not stop for it.";
    /** Another reader's body, in their bed. */
    public static String doubleOf(String name,boolean awake){
        String who=name==null||name.isEmpty()?"somebody else's":name+"'s";
        return awake?"Its eyes are open. It has "+who+" face, and it is not looking at anybody.":"Asleep under the blanket, a body with "+who+" face. Its eyes move under the lids.";
    }
    // 0.4.605: N3. DANIEL_SHOUTS is the script's; the rest are new here.
    public static final String DANIEL_SHOUTS="Is that him? Is that him out there?",
        HATCH_LOOK="Through the hatch: a bare room, a cot, a chair turned to the back wall, and a man held standing in a doorcase there.",
        THROUGH_DOOR="Close to the door, low. Somebody on the other side stops moving.",
        STRAPS="Each strap goes through an iron loop and is buckled, and each buckle is locked. There is a keyhole in each.";
    // 0.4.606: N4. DANIEL_CUT, DAN_CUT and VERA_PIN are the script's; the rest are new here.
    public static final String DANIEL_CUT="What did you do? What did they do to him?",DAN_CUT="It's nothing. It's a cut.",
        VERA_PIN="Watch his hand. Not this one. His.",
        JIB_PULLED="At the end of the back passage the papered door shifts in its frame, stops, and shifts again.",
        GLASS_BREAKS="The glass in the hall stand breaks. Dan's hand comes away from it cut across the back.",
        DANIEL_HAND_OPENS="The back of his hand opens across the knuckles, where nothing has touched it.",
        GRIPPED="A hand closes on your arm and does not let go. It walks you back the way it came.",
        GRIP_BROKEN="Its hand comes off you.",
        TAKEN="In the dark behind the paper it lets go of you, and goes on without you.",
        STRUCK_FAILED="It stops where it is put, and comes on again.";
    // 0.4.607: N5 and N6. The accounts, Daniel's answers and Dan's are the script's; the reply labels R_GIVE and R_STILL and the grey
    // lines are new here.
    public static final String[] DANIEL_ACCOUNT={
        "She said a week. I'd been working nights for a year. She said I could sleep a week and something would go out and do my days, and when the week was done it would come back, and I'd go out, and nobody would know.",
        "It didn't come back. I asked her where it was. She said it was getting on well.",
        "The food comes through the hatch. I mended the chair. I mended it again.",
        "I want the key. I want my name. I want whatever's left."};
    public static final String R_DIDNT_KNOW="He didn't know.",R_WAIT="Wait.",R_GIVE="Give it to him.",R_HURT_SELF="If you hurt him, you hurt yourself. I watched it.",
        DANIEL_GOOD="Good.",DANIEL_MAKE="Then he'd better not make me.",
        DANIEL_SILENT="He looks at you, and says nothing to that.",DANIEL_TAKES="He takes it in the hand the strap leaves free, and holds it down against the frame.",
        DANIEL_ARMED="He has it still, held down against the frame.";
    public static final String[] DAN_DOG={
        "There's a woman two doors down from me with a dog that's too old for the stairs. I carry it up for her, morning and night. Six years. Rae can tell you. She thinks I'm the nicest man in the street.",
        "That happened. I don't care what else happened. That happened."};
    public static final String DAN_STAND="When it comes to it, what does that mean? Standing with me.",
        R_STILL="He's still here, in the room at the back. She's still using him.";
    public static final String[] DAN_NO={"No. Vera told me he'd gone home. She said he had his own life and didn't want this one.","Show me."};
    public static final String VERA_WORKS="Vera folds a cloth into a square and sets it on the others.";
    public static final String MAN_IN_FRAME="The man in the frame";
    public static final String[] DANIEL_LISTEN={"You're not her. You're staying here? Listen to me.","There's a key for these. She keeps it, or the woman with her does.",
        "Get me out of this frame before she brings him down. If she brings him down it's finished."};
    public static final String R_DAN_HERE="Dan's here.",R_WHO="Who are you?",R_FREE="I'll get you out.",R_BACK="I'll come back.",R_NOTHING="Say nothing.";
    public static final String DANIEL_VOICE="I know he's here. I'd know that voice anywhere. It's mine.",DANIEL_NAME="Daniel. This was my room for a week.",DANIEL_LOOKS="He looks at the marked wall.",
        DANIEL_COUNTING="I stopped counting.",DANIEL_KEY="Then get the key. Don't let her see you take it.";
    // 0.4.608: N7. RAE_CONFESSION is the script's; the reply labels and every other line are new here.
    public static final String[] RAE_CONFESSION={"I knew. All right? I knew he was alive.",
        "If I'd told Dan he'd never have come. He'd have sat in the flat and come apart. I told myself she could save them both.",
        "She can't. She told me that upstairs. One of them."};
    public static final String KEY_GLANCE="Rae has a key in her hand, long and plain. She looks at it, and puts it away in her coat.",
        RAE_WARY="Rae looks at you, and then at her hands.",
        R_KEY="You have the key.",R_WAY_UP="You knew the way up.",
        R_TO_VERA="Keep it. I'll stay with Dan.",R_GIVE_ME="Give it to me.",R_YOURSELF="Give it to him yourself.",
        RAE_TO_VERA="Then I'll take it up to her.",RAE_GIVES="Don't let her see it.",KEY_GIVEN="Rae puts the key in your hand. It is warm from her coat.",
        RAE_ALL_RIGHT="All right.",RAE_SORRY="I'm sorry.",DANIEL_DONT="Don't.",
        RAE_UNLOCKS="Rae unlocks the straps, one and then the other.",
        KEY_TURNS="The key turns hard in the first lock and easier in the second. The straps fall open.",
        STRAPS_OPEN="The straps hang open from their iron loops.",
        DANIEL_FREE="He stands with his arms down, turning one wrist in the other hand.",
        KEY_FOUND="On the floor of the cupboard, pushed to the back: a key, long and plain.",
        RAE_NOT_YET="Daniel. Please. Not yet. Not with him there.",
        R_RAE_KNEW="Rae knew he was alive.",DAN_SHE_KNEW="She knew?",DAN_YOU_KNEW="You knew.",RAE_SO_YOU_CAME="I did it so you'd come.";
    // 0.4.609: N8. VERA_OFFER and VERA_TRUTH are the script's; the reply labels and every other line are new here.
    public static final String[] VERA_OFFER={"I can mend him. I need help keeping the other one quiet while I do it.",
        "It's a matter of making the arrangement stable. It has been loose for years."};
    public static final String[] VERA_TRUTH={"The other one won't come through it. No.",
        "He has been cared for. Dan has a life to go back to. Surely you can see what would be wasted.",
        "Or you can send Dan back through the frame. The other walks out. It's possible. It's a waste."};
    public static final String VERA_WAITS="Vera waits for an answer.",VERA_HAS_KEY="She has the key from the workroom hook in her hand.",
        R_OTHER_ONE="And the other one?",R_IT_CUT_HIM="It cut him too.",R_HE_TOLD_ME="Daniel told me what you did to him.",
        R_HELP_VERA="I'll help you.",R_SEND_BACK="Send Dan back.",R_REFUSE_VERA="No.",
        VERA_GOOD="Good. When I call for him, bring him to me.",VERA_WASTE="Then you do it. I won't.",VERA_KEEP_OUT="Then keep out of my way.";
    /** What a failed body is doing when a hand is put on it. */
    public static String failed(int who){return switch(who){
        case LodgingPeople.FAILED_POST->"Tied to the post by the wrists. Its fingers go on doing up a button that is not there.";
        case LodgingPeople.FAILED_DRESS->"It keeps one hand flat on the hatch, as somebody waits at a door they have knocked on.";
        default->"Its fingers are hooked round the door's frame. It counts under its breath, loses its place, and starts again.";};}
    // 0.4.550: what is in the rooms.
    public static final String BED="A bed, made and turned down for the night.",
        BED_VERA="A narrow bed, made so tight the blanket does not crease.",
        WASHSTAND="A basin, a jug of water and a glass. The water is fresh.",
        CHAIR="A plain chair.",
        GUEST_CHAIR="A hard chair, set close to the bed's head.",
        DANIEL_CHAIR="A plain chair, turned to face the doorcase in the back wall. The boards under it are worn in arcs where it has been turned on the one spot.",
        ARMCHAIR="An armchair, its arms worn smooth where hands have rested.",
        SETTEE="A settee for two, sunk in the middle.",
        SIDE_TABLE="A small table with a ring on it where a cup has stood.",
        LONG_TABLE="The long table with all its leaves in. In front of each chair the polish is worn where a plate goes.",
        SIDEBOARD="A sideboard. Its drawers hold cutlery laid in rows, every place the same.",
        COT="A narrow cot. The mattress is worn through in one place, and the blanket at its foot is folded as square as it will go.",
        SHELF="A shelf the height of a tray, under the hatch. Tin plates on it, washed and stacked, and a cup with its handle mended.",
        PAIL="A pail with a lid, scrubbed.",
        SCULLERY_PAIL="A pail of clean water.",
        RANGE="The range is in, low. The kettle on it is warm.",
        FLUE="The range's flue. It is warm to the hand.",
        SINK="A stone sink, worn hollow in the middle.",
        DRESSER="A dresser with its plates standing in the rack, all of them alike.",
        TRAY="A tray laid for one: a tin plate, a tin cup, a spoon. Nothing on the plate yet.",
        TABLE="A scrubbed table.",
        PREPARATIONS="Cloths folded in squares, a basin, thread wound on cards, and a roll of oilcloth tied shut. Everything is clean, and nothing has been put away.",
        RECORD="A cabinet of little drawers, each full of cards. Every card has a room number and a date on it in the same hand; most have a second date under the first. None has a name.",
        KEY="A hook by the door with a key on it, long and plain. Its tag is blank.",
        KEY_GONE="An empty hook by the door. The paint is worn under it where something has hung a long time.",
        CUPBOARD="A tall cupboard, locked.",
        WARDROBE="A wardrobe, locked. It smells of camphor.",
        DRESSING="A brush and a dish of pins before the glass. Nothing else.",
        CARD_HOLDER="A brass holder beside the door, for a card. Empty.",
        ROPE="A coil of rope, its end cut, not frayed.",
        CRATE="A crate, nailed shut.",
        COAL="A heap of coal.";
    /** What a thing in the rooms says to a hand, by what it is and where it stands. */
    public static String thing(BlockState s,BlockPos rel){
        var r=Vec3.atCenterOf(rel);int stage=s.getValue(LodgingThing.STAGE);
        return switch(s.getValue(LodgingThing.KIND)){
            case BED->stage>=2?BED_VERA:BED;
            case COT->COT;
            case WASHSTAND->WASHSTAND;
            case CHAIR->LodgingSite.inDaniels(r)?DANIEL_CHAIR:LodgingSite.guestRoom(r)>=0?GUEST_CHAIR:CHAIR;
            case ARMCHAIR->ARMCHAIR;
            case SETTEE->SETTEE;
            case SIDE_TABLE->SIDE_TABLE;
            case LONG_TABLE->LONG_TABLE;
            case SIDEBOARD->SIDEBOARD;
            case WORK_TABLE->PREPARATIONS;
            case KEY_HOOK->stage==0?KEY:KEY_GONE;
            case CARD_INDEX->RECORD;
            case WARDROBE->LodgingSite.inWorkroom(r)?CUPBOARD:WARDROBE;
            case DRESSING_TABLE->DRESSING;
            case CARD_HOLDER->CARD_HOLDER;
            case SINK->SINK;
            case RANGE->stage==0?RANGE:FLUE;
            case DRESSER->DRESSER;
            case SCULLERY_TABLE->(stage&2)!=0?TRAY:TABLE;
            case SHELF->SHELF;
            case PAIL->LodgingSite.inDaniels(r)?PAIL:SCULLERY_PAIL;
            case ROPE->ROPE;
            case CRATE->CRATE;
            case COAL->COAL;
        };
    }
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the terms of the house, printed on a card, with additions in ink. */
    public static ItemStack source(){
        return HouseWriting.book("Terms of the house","V.",pages(
            "TERMS\n\nRooms by the week. A week is seven nights.\n\nGuests are asked to keep to their own rooms after eleven.\n\nThe hatches are for the house's use.",
            "Do not wake the sleeper in your room. It is not yours yet, and it is not used to being looked at.\n\nNothing in this house is to be touched that is not your own. You will find that easy.",
            "Anyone who is tired may rest. Anyone who rests will be looked after, and missed by nobody.\n\nThe house does not keep a register. The house remembers.\n\nV."));
    }
    /**
     * The account. This slice the story cannot be finished, so nothing asks for this; it says only what a reader could have seen of the
     * house, and later slices write the rest from the reader's own record.
     */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("There was a lodging house with its shutters screwed shut, and a card of terms in the hall.");
        if(own.getBoolean("WentUp"))p.add("Upstairs every room had a number, and every outside wall had a doorway in it that went nowhere.");
        if(own.getBoolean("WentDown"))p.add("Under the house the whole of it stood on posts, and the posts stood on a beam, and the beam was held by one wheel.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.ONES_INSIDE),name,pages(String.join("\n\n",p)));
    }
}
