package io.github.knaitoe.theoldesthouse.labyrinth;

import static io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace.*;

import java.util.*;
import javax.annotation.Nullable;

/**
 * 0.4.162 (owner: "the varying perspectives for multiplayer"): every shared scene's account, drafted for being there with
 * others. Where one reader's act changes the room for everyone in it (the floor's board, Mother letting go, the séance's
 * sitting, the vault's lid), the others there watched it, and keep a watcher's account and the Witness credit. Everywhere
 * else each reader still comes through for themselves, as in the child's room, and their own account closes on who of
 * their party had gone ahead of them and who was still in it. Readers are named by their player names and never he or she.
 */
final class CoopAccounts {
    private CoopAccounts(){}

    /** "Ana", "Ana and Ben", "Ana, Ben and Cy". */
    static String names(List<String> names){
        if(names.size()<2)return names.isEmpty()?"":names.getFirst();
        return String.join(", ",names.subList(0,names.size()-1))+" and "+names.getLast();
    }
    /** A sentence about some of the party: %s is their names, and {was} and {has} agree with how many there were. */
    static String fill(String sentence,List<String> names){
        if(names.isEmpty())return "";boolean one=names.size()==1;
        return sentence.replace("{was}",one?"was":"were").replace("{has}",one?"has":"have").formatted(names(names));
    }
    /** One sentence for those of the party who had come through first, one for those still in it. */
    private static SharedVignettes.Together party(String ahead,String behind){
        return (outcome,a,b)->(fill(ahead,a)+" "+fill(behind,b)).trim();
    }
    /** One sentence for everyone who was there, where nobody can be said to have come through first. */
    private static SharedVignettes.Together with(String sentence){
        return (outcome,a,b)->{var all=new ArrayList<>(a);all.addAll(b);return fill(sentence,all);};
    }
    private static void together(LabyrinthPlace place,SharedVignettes.Together scene){SharedVignettes.together(place,scene);}

    static void register(){
        // The scenes everyone comes through for themselves.
        together(MODEL_HOME,with("%s {was} at the window with me when the glass gave."));
        together(PRESERVED_CAVE,party("%s had already reached the canoe and gone back up to the air.",
            "When I turned back, %s {was} still walking between the rows, and none of the faces turned to follow."));
        together(TED_CAVER,party("%s had come back through the squeeze before me; the rubble was still settling from it.",
            "When I climbed past the rope, %s {was} still somewhere below me in the passage."));
        together(ZAMPANO_COURTYARD,party("%s had read the survey before me and left it squared on the table, as if it had never been touched.",
            "%s {was} still reading by the sealed windows when I put the survey down."));
        together(BARN_WELL,party("%s had already climbed out into the yard. The hands that lifted the cover might have been theirs.",
            "%s {was} still waiting in the dark below the initials when I reached the yard."));
        together(PLAIN,party("%s had already held the frame and heard the shutter. We never compared the photographs.",
            "%s {was} still at the spyglass when I came away with mine."));
        together(HOSPITAL,party("%s had pressed the call button before me. Nobody came for any of us.",
            "%s {was} still sitting by the incubator when the alarms stopped."));
        together(HOLLOWAY_CAMP,party("%s had gone through the service latch first. A shield had been left for me all the same.",
            "%s {was} still among the pillars when I pulled the latch, keeping to the light."));
        together(WALLPAPER_NURSERY,party("%s had found the four strips before me and left them hanging loose from the seams.",
            "%s {was} still at the walls, following the shape from seam to seam, when I read the last page."));
        together(HOTEL,party("%s had left the hotel before me. Their room was already made up for someone else.",
            "%s {was} still a guest when I left. The porter kept their keys on the hook beside mine."));
        together(HILL_NURSERY,party("%s had knocked on the cellar wall before me, and the cellar had answered them too.",
            "%s {was} still at the cellar wall, knocking, when I went up the stairs."));
        together(MINIATURES,party("%s had seen the little rooms before me.",
            "%s {was} still bent over the tables when I left. I did not look to see whose figure stood behind mine."));
        together(MASQUE,party("%s had reached the clock before me; for them the chimes had already stopped.",
            "%s {was} still somewhere in the colored rooms when the clock stopped for me. I could hear the music going on for them."));
        together(WINCHESTER,party("%s had climbed the stair before me.",
            "%s {was} still on the lower flights when I read the last name in the ledger."));
        together(CRIMSON_HALL,party("%s had played the voices before me; I had heard the last of them through the wall before I touched a cylinder.",
            "%s {was} still at the cylinders when I cleared the snow from the floor."));
        together(BLY_ROUTE,party("%s had gone into hiding before me. I did not open a cupboard to see.",
            "When I came out, %s {was} still shut in somewhere along the wet boards."));
        together(ELK_LOT,party("%s had already woken on the porch.",
            "%s {was} still out in the field when it let me go."));
        together(ELK_FAN,party("%s had been thrown out from under the fan before me.",
            "%s {was} still standing under the blades when they threw me out."));
        together(MAPPING_INTERIOR,party("%s had come back to the boy before me.",
            "%s {was} still in the crawl beyond the roof when I went back to him."));
        together(ELK_CARCASSES,party("%s had taken the service path before me.",
            "%s {was} still under the hides when the boots went by and I left."));
        together(COSTUME_NIGHT,party("%s had reached the park grass before me.",
            "%s {was} still on the sand among the costumes when I reached the grass."));
        together(MOVIE_NIGHT,party("%s had reached the pier before me.",
            "%s {was} still out on the dark water between the bursts when I tied up at the pier."));
        together(WINTER_LAKE,party("%s had been out to the hole before me.",
            "%s {was} still out on the ice when I reached the bank."));
        together(CAMP_BLOOD,party("%s had got past the last cabin before me.",
            "%s {was} still among the cabins when I reached the service road."));
        together(DEVILS_ROCK,party("%s had read the pages before me.",
            "%s {was} still reading when I put down the eighth page."));
        together(GHOSTS_SET,party("%s had finished filming before me. Their footage kept different moments of the same days.",
            "%s {was} still behind a camera of their own when I left."));
        together(END_WORLD_CABIN,party("%s had answered the visitors before me.",
            "%s {was} still waiting for the visitors when I gave my answer."));
        together(PAINTED_INTERIOR,party("%s had already left by the unpainted door.",
            "%s {was} still in the painted room when I left. I did not turn around to look at the corners."));
        together(TUNNEL_PERIPHERY,party("%s had reached the rising ceiling before me.",
            "%s {was} still under the green lights behind me."));
        together(LAYERED_MAN,party("%s had reached the top step before me.",
            "%s {was} still in the cellar when I reached the top step."));
        together(MOTION_LIGHT,party("%s had gone out through the service door before me.",
            "%s {was} still in the courtyard when I opened the service door."));

        // The scenes where one reader's act changes the room for everyone in it.
        SharedVignettes.draft(FLOORBOARDS,FLOOR);
        SharedVignettes.draft(SEANCE,SITTING);
        SharedVignettes.draft(USHER,VAULT);
        SharedVignettes.draft(MOTHER_DEN,KEEPER);
    }

    /** The loose board: whoever takes it up stops the sound for everyone in the room. */
    static final SharedVignettes.Scene FLOOR=new SharedVignettes.Scene(){
        @Override public @Nullable String line(VignetteRole role,String outcome,String actor){return switch(role){
            case WITNESS->actor+" took the hatchet to the loose board while I stood by the bed. The beating stopped for all of us at once. I read the words underneath over "+actor+"'s shoulder: someone had stayed to care for him.";
            case PARTWAY->"The floor was already beating when I came in, and "+actor+" already had the hatchet. I watched the board come up and the room go quiet. I never learned how long it had been beating before me.";
            case AFTERMATH->"I had been in that room while the floor still beat under it. When I came back, "+actor+" had taken the board up. The room was quiet, and there were words where the sound had been.";
            default->null;};}
    };

    /** The séance: one sitting, whoever's hand is at the cupboard. */
    static final SharedVignettes.Scene SITTING=new SharedVignettes.Scene(){
        @Override public @Nullable String line(VignetteRole role,String outcome,String actor){
            String end=switch(outcome){
                case "drove_the_family_out"->"the family went out through the west door and shut it behind them";
                case "heard_the_medium"->"the medium had said everything she meant to say";
                default->"nothing at the table moved again";};
            return switch(role){
                case WITNESS->"I sat at the table for the sitting. It was "+actor+"'s hand the family could not see. From my chair I could see their faces when the cupboard knocked: they were afraid of the room, not of us. I stayed until "+end+".";
                case PARTWAY->"I came in after the sitting had begun. "+actor+" was already at the cupboard, and the family were already listening. I stayed until "+end+". The album was the first thing I read, not the last.";
                case AFTERMATH->"When I came back the chairs had been pushed in. "+actor+" had finished the sitting without me. The album lay open at its last page, at her name and the children's.";
                default->null;};
        }
    };

    /** The vault: one choice of the lid, and every visit after shows its consequence to whoever comes. */
    static final SharedVignettes.Scene VAULT=new SharedVignettes.Scene(){
        @Override public @Nullable String line(VignetteRole role,String outcome,String actor){
            boolean closed=!outcome.equals("found_her_outside_the_open_coffin");
            return switch(role){
                case WITNESS->closed?"I was in the vault when "+actor+" chose the lid. I never put my own hand on it. On every visit after, the scratching beneath it was louder, and the cracks ran further along the wall."
                    :"I was in the vault when "+actor+" left the coffin open. I chose nothing. On the next visit she was standing outside it, and nobody had moved her.";
                case PARTWAY->closed?"I came to the vault after the lid had been chosen; "+actor+" had chosen it. All I knew of the choice was the scratching, and the cracks along the wall."
                    :"I came to the vault after "+actor+" had left the coffin open. By then she was already standing outside it.";
                case AFTERMATH->closed?"I had been to the vault before the lid was chosen. When I came back the sound had followed the stone, as "+actor+" had seen it do."
                    :"I had been to the vault while she still lay in the coffin. When I came back, "+actor+" had seen it through, and she was standing outside.";
                default->null;};
        }
    };

    /** Mother: whoever gives without asking lets her let go, in front of everyone in her den. */
    static final SharedVignettes.Scene KEEPER=new SharedVignettes.Scene(){
        @Override public @Nullable String line(VignetteRole role,String outcome,String actor){return switch(role){
            case WITNESS->outcome.equals("saved_dog")
                ?"I was in her den when she gave "+actor+" back a dog. It came out alive and went straight to "+actor+"'s side. For a moment, keeping it safe and keeping it forever were different things."
                :"I was in her den when "+actor+" gave her something and asked for nothing back. She looked at her shelves as if she had never seen how full they were. Then she let something go.";
            case PARTWAY->"I came into her den late, when "+actor+" had already been talking with her a long while. I saw only the end: she let something go.";
            case AFTERMATH->"When I came back to her den the shelves were still full, but nobody was keeping them. "+actor+" had been there when she let go.";
            default->null;};}
    };
}
