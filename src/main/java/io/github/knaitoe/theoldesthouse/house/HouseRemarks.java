package io.github.knaitoe.theoldesthouse.house;

import java.util.List;

/**
 * 0.4.199: notes slipped under a door to a reader who has eaten in the House, from whoever lives in the
 * other room. Each is written once, in order, after a meal and at least a room later; then they stop.
 * They are scenery, like the rest of the correspondence, and confer nothing.
 */
public final class HouseRemarks {
    public static final String PREFIX="ATE";
    private static final String TITLE="Under the door",AUTHOR="From the other room";
    public static final List<CorrespondenceTexts.Note> NOTES=List.of(
            note(1,"Could you stop chewing so loudly? I could hear you all the way from the other room."),
            note(2,"Was that {food}? You ate it with your mouth open. The walls in here are thinner than they look."),
            note(3,"Somebody has been dropping crumbs in the hall. I swept them up. Next time eat over your hand."),
            note(4,"You eat as if nobody can hear you. Somebody can always hear you in a house like this. Mostly it's us, with a glass against the wall."),
            note(5,"We heard you eating again. We haven't eaten in a long time. Please don't do it so close to the wall."));
    private HouseRemarks(){}
    private static CorrespondenceTexts.Note note(int n,String text){
        return new CorrespondenceTexts.Note(PREFIX+n,TITLE,AUTHOR,HouseWriting.WritingStyle.KAREN,"",0,0,HouseMarginalia.Thread.HOUSEKEEPING,text);
    }
    public static CorrespondenceTexts.Note find(String id){return NOTES.stream().filter(n->n.id().equals(id)).findFirst().orElse(null);}
}
