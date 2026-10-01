package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

/** Native readable books produced by normal furnace recipes, with original student writing. */
public final class DrownedEssayItem extends WrittenBookItem {
    private final int essay;
    public DrownedEssayItem(Properties properties, int essay) {
        super(properties.component(DataComponents.WRITTEN_BOOK_CONTENT, content(essay, List.of())));
        this.essay = essay;
    }

    public static WrittenBookContent content(int essay, List<String> boys) {
        List<Component> pages = new ArrayList<>();
        String title = switch (essay) {
            case 0 -> "Safe ground";
            case 1 -> "The second visit";
            default -> "An audience";
        };
        String[] text = switch (essay) {
            case 0 -> new String[]{
                "FILM STUDIES\n\nSafe ground\n\nThe monster has a rule. A rule is useful only if you notice it before you need it.",
                "At this lake she cannot enter water. She cannot step onto grass.\n\nThe dirt between those places is hers.\n\nLook at what you are standing on.",
                "Water buys time. It does not give you breath.\n\nA door can hold a little air. Bubbles rise from soul sand. A shell and a potion buy more.\n\nShe waits for you to need the shore."
            };
            case 1 -> new String[]{
                "FILM STUDIES\n\nThe second visit\n\nIn a sequel, the place remembers what happened. You are the one who expects it to start over.",
                "A house can remember the doors you opened. Returning to a familiar room does not make it safe.\n\nKeep something that tells you where you have been.",
                "The school kept the church key after the lake rose.\n\nDry all three essays. Leave. Come back.\n\nThe last desk will have something the first visit did not."
            };
            default -> new String[]{
                "FILM STUDIES\n\nAn audience\n\nA song behind a wall is still a song. Sometimes the wall is the only reason you can bear to listen.",
                "The preacher is still below. As long as he stays, the water over the church stays consecrated.\n\nThe door takes a key. Inside, there is only one thing left to open: the roof hatch.",
                "A slasher waits for you to hurry, split up, or mistake quiet for the end.\n\nGrass is a place to stop and think.\n\nIf you let the hymn out, remember who has been listening."
            };
        };
        for (String page : text) pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN, page));
        for (int first = 0; first < boys.size() && pages.size() < 100; first += 6)
            pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,
                    "THE SHALLOWS\n\nThe boys who were there:\n\n" + String.join("\n", boys.subList(first, Math.min(first + 6, boys.size())))
                            + "\n\nThe shore remembers who threw her."));
        return new WrittenBookContent(Filterable.passThrough(title), "An Indian Lake student", 0,
                pages.stream().map(Filterable::passThrough).toList(), true);
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer reader) {
            LabyrinthData data = LabyrinthData.get(reader.server);
            List<String> names = new ArrayList<>();
            if (IndianLakeProgress.hasThrown(data, reader.getUUID())) {
                var boys = data.state("indian_lake").getCompound("Boys");
                for (String id : boys.getAllKeys().stream().sorted().toList()) names.add(boys.getString(id));
            }
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, content(essay, names));
            // Publish the changed held stack before the native open-book packet reads its pages.
            reader.inventoryMenu.broadcastChanges();
        }
        return super.use(level, player, hand);
    }
}
