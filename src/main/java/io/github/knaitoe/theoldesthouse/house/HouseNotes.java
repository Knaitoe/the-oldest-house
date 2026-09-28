package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.opening.NavidsonLetter;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * Notes the Navidsons leave on the manor's shelves: Will measuring the
 * house, Karen watching him do it. Each measurement is a little different,
 * and none of them agree with the outside.
 */
public final class HouseNotes {
    public record Note(String title, String author, boolean will, String text, Predicate<HouseSavedData> available) {
    }

    private static final String WILL = "Will Navidson";
    private static final String KAREN = "Karen Green";

    public static final List<Note> NOTES = List.of(
            new Note("Measurements", WILL, true,
                    "Measured the house again tonight, outside and in.\n\nOutside, front to back: 32 ft 9 in.\n\n"
                            + "Inside, wall to wall, plus the walls: 32 ft 9 1/4 in.\n\nThree times. Same quarter inch.",
                    data -> true),
            new Note("Notes", KAREN, false,
                    "Will has the tape out again. Now it's 5/8 of an inch.\n\nI told him houses settle.\n\n"
                            + "He said houses don't settle bigger.",
                    data -> true),
            new Note("Measurements", WILL, true,
                    "Borrowed a laser measure so the tape can't sag.\n\nInside is longer by 3/16 now. Less than "
                            + "Tuesday.\n\nThat should make me feel better.",
                    data -> true),
            new Note("Notes", KAREN, false,
                    "The rug in the big bedroom was red. I am sure it was red. I picked it.\n\n"
                            + "Will says it was always brown. He's measuring the rug now.",
                    HouseSavedData::areRugsShifted),
            new Note("Measurements", WILL, true,
                    "The wall between the two bedrooms is four inches thick. I drilled a pilot hole to be sure.\n\n"
                            + "There is a door in it now, and the room behind the door is fourteen feet deep.",
                    HouseSavedData::isRoomRevealed),
            new Note("Notes", KAREN, false,
                    "Counted steps down the hall with Daisy. Thirty-one.\n\nThis morning she counted thirty-two, "
                            + "so we did it again, together, holding hands.\n\nThirty-two.",
                    HouseSavedData::isHallDeepened),
            new Note("Notes", KAREN, false,
                    "I don't want the tape measure in the house anymore.\n\nEvery number it gives is a little "
                            + "different, and every one of them is right.",
                    data -> data.shiftsTriggered() >= 3),
            new Note("Measurements", WILL, true,
                    "There is a door at the end of the hall that I did not build.\n\nI'm going to measure what's behind "
                            + "it. Karen asked me not to.\n\nI'm writing that down so I remember she asked.",
                    HouseSavedData::isImpossibleDoorRevealed)
    );

    private HouseNotes() {
    }

    /** The index of the next note to leave out, or -1 if none is due. */
    public static int next(HouseSavedData data) {
        for (int i = 0; i < NOTES.size(); i++) {
            if (!data.isNoteWritten(i) && NOTES.get(i).available().test(data)) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    public static ItemStack book(int index) {
        if (index < 0 || index >= NOTES.size()) {
            return null;
        }
        Note note = NOTES.get(index);
        Component page = note.will()
                ? Component.literal(note.text()).withStyle(style -> style.withFont(NavidsonLetter.FONT))
                : Component.literal(note.text());
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(note.title()),
                note.author(),
                0,
                List.of(Filterable.passThrough(page)),
                true
        ));
        return book;
    }
}
