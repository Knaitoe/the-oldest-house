package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;

/** Original household serials and poems. Personal references are snapshots of native play only. */
public final class MarginaliaTexts {
    private MarginaliaTexts() {}
    private static ItemStack plain(String title,String author,String... pages) {
        return HouseWriting.book(title,author,HouseWriting.WritingStyle.PLAIN,List.of(pages));
    }
    private static ItemStack hand(String title,String author,String... pages) {
        return HouseWriting.book(title,author,HouseWriting.WritingStyle.KAREN,List.of(pages));
    }
    public static ItemStack book(ServerPlayer reader, HouseMarginalia.Thread thread, int chapter) {
        String name=reader.getGameProfile().getName();
        return switch(thread) {
            case HOUSEKEEPING -> switch(chapter) {
                case 0 -> hand("Things to do","M.",
                        "Wash the blue cup.\n\nMend the chair.\n\nPut the winter sheets away.\n\nLeave room at the bottom for tomorrow.");
                case 1 -> hand("Things left undone","M.",
                        "The cup is clean. The chair is mended. The sheets are folded.\n\nSomeone added another task.\n\nYou are useless.");
                case 2 -> plain("In the same hand","",
                        "The words are on the shopping list now. Between bread and soap.\n\nYou are useless.",
                        "On the back of the receipt. On a page torn from a calendar.\n\nYou are useless.",
                        name+",\n\nI tried writing something else. My hand waited until I was finished.\n\nYou are useless.");
                default -> scratched(name);
            };
            case CALLS -> switch(chapter) {
                case 0 -> hand("Back before supper","E.",
                        "Gone for milk. The kettle is filled.\n\nIf I am late, eat without me.\n\nThe spare key is still where we agreed.");
                case 1 -> hand("Under the kettle","E.",
                        "I did not go for milk.\n\nThere is a staircase where the pantry was. I can hear our radio at the bottom.",
                        "I have taken the key. I have left the kettle.\n\nDo not come for me.");
                case 2 -> plain("The second letter","E.",
                        "I found our kitchen down here. The blue cup was in the sink. Someone had set two places.",
                        "A chair scraped the floor while I stood in the doorway.\n\nIt was the empty chair.",
                        "Do not come for me.\n\nIf you find this under the kettle, I have not returned.\n\nNeither has the kitchen.");
                default -> plain("A letter with your name","E.",
                        name+",\n\nYou were not the person I meant. I left that letter before I knew your name.",
                        "I am somewhere small. The door fits its frame. I am beginning to sleep.\n\nI heard you passing it.",
                        "Do not come for me.\n\nI used to mean: it is dangerous here.\n\nNow I mean: please leave me one room you have not opened.");
            };
            case ROOM -> switch(chapter) {
                case 0 -> plain("Linen inventory","Household copy",
                        "ONE ROOM\n\nTwo pillows.\nOne lamp.\nOne spare blanket.\n\nThe second pillow is for anyone who needs it.\n\nNothing else is missing.");
                case 1 -> plain("The amended inventory","Household copy",
                        "The room has been prepared for the visitor.\n\n"+carried(reader)+"\n\nThere was no space for this on the old form.",
                        "One lamp.\nOne blanket.\nOne chair, facing the door.\n\nThe second pillow has been put away.");
                case 2 -> plain("A room for you","Household copy",
                        "OCCUPANT: "+name+"\n\n"+observed(reader)+"\n\nThe room remains available.",
                        "The bed is made. It does not look slept in.\n\nUnder the pillow is a dent shaped like the back of your head.");
                default -> plain("No forwarding address","",
                        name+",\n\nYou keep finding places that need you.\n\nThis one has managed without you. It has even learned your name.",
                        "One blanket, folded.\nOne lamp, unlit.\nOne door, unopened.\n\nNo missing occupant.\n\nDo not correct the inventory.");
            };
            case POEMS -> switch(chapter) {
                case 0 -> plain("A verse on the grocery list","",
                        "SMALL THINGS\n\nThe spoon beside\nthe cooling cup.\nThe hem I meant\nto take back up.\n\nA chair pushed in.\nA folded sheet.\nThe little work\nthat makes us meet.");
                case 1 -> plain("A poem in the drawer","",
                        "ADDRESS\n\nI wrote the number\non the door.\nThe rain rubbed out\nthe name before\nthe morning came.\n\nThe number stayed.\nA house can keep\na thing mislaid.",
                        "I say my name\nto keep it near.\n\nThe room says nothing.\n\nI stay here.");
                case 2 -> plain("Instructions for a guest","",
                        "Leave your coat\nwhere coats belong.\nSet down the cup.\nForgive the song.\n\nThe song was playing\nwhen we left.\nThe room has learned\nto hold its breath.",
                        name+",\n\nWe set a place.\nWe left a light.\nWe practiced\nsaying your name right.\n\nWe had so long.\nWe had all night.");
                case 3 -> plain("The landing","",
                        "There was a stair\nthat went upstairs.\nI know because\nI left things there.\n\nA slipper.\nDust.\nA ring of keys.\n\nA life was made\nof things like these.",
                        "I left them there.\nThey are there still.\nThe stair goes down.\n\nI think it will\ngo farther\nif I say their names.\n\nI say them.\nNothing stays the same.");
                default -> erasure(name);
            };
        };
    }
    private static ItemStack scratched(String name) {
        List<Component> pages=new ArrayList<>();
        String refrain="YOU ARE USELESS\n";
        for(int page=0;page<8;page++) {
            String tail=switch(page) {
                case 0 -> "THE CUP IS CLEAN.";
                case 1 -> "THE CHAIR IS MENDED.";
                case 2 -> "THE SHEETS ARE FOLDED.";
                case 3 -> "I TURNED THE PAGE.";
                case 4 -> "I TURNED IT AGAIN.";
                case 5 -> name.toUpperCase(Locale.ROOT)+".";
                case 6 -> "I LEFT NO BLANK PAGE.";
                default -> "THERE IS NO OTHER HAND.";
            };
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.CLAW,refrain.repeat(7)+"\n"+tail));
        }
        return HouseWriting.book("No blank pages","",pages);
    }
    private static ItemStack erasure(String name) {
        Component missing=HouseWriting.page(HouseWriting.WritingStyle.PLAIN,"I kept a room\ninside my name.\nI kept a name\ninside the room.")
                .copy().withStyle(style->style.withStrikethrough(true));
        return HouseWriting.book("A poem with corrections","",List.of(
                Component.empty().append(missing).append(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,"\n\nThe room remained.")),
                HouseWriting.page(HouseWriting.WritingStyle.PLAIN,"\n\n\n\n\n"+name+"\n\n\n\n\nThere.\nIt fits.")));
    }
    private static String carried(ServerPlayer reader) {
        for(ItemStack stack:reader.getInventory().items) if(!stack.isEmpty()&&stack.has(DataComponents.CUSTOM_NAME))
            return "An object labeled '"+shorten(stack.getHoverName().getString(),28)+"'. You brought it in yourself.";
        if(!reader.getOffhandItem().isEmpty()) return "In your other hand: "+reader.getOffhandItem().getCount()+" "+shorten(reader.getOffhandItem().getHoverName().getString(),24)+". No one offered to take them.";
        for(ItemStack stack:reader.getInventory().items) if(!stack.isEmpty())
            return "An entry for what you brought: "+shorten(stack.getHoverName().getString(),28)+". It was absent from yesterday's list.";
        return "No luggage. Both hands empty.\n\nThe form calls this 'ready'.";
    }
    private static String observed(ServerPlayer reader) {
        var pets=reader.serverLevel().getEntitiesOfClass(TamableAnimal.class,reader.getBoundingBox().inflate(16),
                pet->pet.isAlive()&&reader.getUUID().equals(pet.getOwnerUUID())&&pet.hasCustomName());
        if(!pets.isEmpty()) return "Another name on the form: "+shorten(pets.getFirst().getName().getString(),24)+".\n\nNo bed has been made for them.";
        LabyrinthData data=LabyrinthData.get(reader.server); int resolved=WitnessAccount.count(data,reader.getUUID());
        if(resolved>0) return "You have set down "+resolved+" part"+(resolved==1?"":"s")+" of your account.\n\nThe writer has left you no room to add another.";
        int visited=data.visited(reader.getUUID()).size();
        if(visited>0) return "You have entered "+visited+" different place"+(visited==1?"":"s")+".\n\nNot one of them was expecting a guest.";
        return reader.isPassenger() ? "You found somewhere to sit.\n\nThe chair was left where you could see it."
                : "You are reading this standing up.\n\nThe chair has been left where you can see it.";
    }
    private static String shorten(String text,int limit) { String clean=text.replaceAll("[\\p{Cntrl}]"," "); return clean.length()<=limit?clean:clean.substring(0,limit-3)+"..."; }
    public static ItemStack interlude(long site,HouseMarginalia.Thread thread) {
        String[] scraps={
                "The left drawer sticks. Lift it a little before pulling.\n\nI keep forgetting you don't know that yet.",
                "The radiator clicks three times when it cools.\n\nIt always did. I used to complain about it.",
                "One blue sock. One grey sock.\n\nI put them together because I was tired. Please don't make a thing of it.",
                "There is soup in the small pot.\n\nThere are two bowls. You can use either. I washed them both.",
                "FOR THE CHAIR\n\nA little cloth.\nA little thread.\nA place to put\nthe things unsaid.\n\nA stitch can hold\na small thing fast.\nI meant this little\nwork to last.",
                "If the sitting-room lamp goes out, the spare bulb is in the drawer.\n\nIf the drawer has gone, leave it until morning.",
                "I did not label the photographs.\n\nI thought we would always know who they were.",
                "Four plates, not six. We gave the others away.\n\nI am writing it here so we stop setting them out."
        };
        return plain("A household scrap","",scraps[Math.floorMod(Long.hashCode(site)+thread.ordinal()*3,scraps.length)]);
    }
}
