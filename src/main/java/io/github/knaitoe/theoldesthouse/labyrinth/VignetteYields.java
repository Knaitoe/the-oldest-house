package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * Every vignette yields one object: the caregiver's note, the crayon
 * drawing. Each carries the vignette it came from, so Hillary can take a
 * scent from it.
 */
public final class VignetteYields {
    private static final String KEY = "the_oldest_house_yield";

    private VignetteYields() {
    }

    public static ItemStack mark(ItemStack stack, String vignette) {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY, vignette);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    /** The vignette an object came from, or null if it is not one of theirs. */
    @Nullable
    public static String of(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains(KEY)) {
                return tag.getString(KEY);
            }
        }
        // Notes found before objects were marked.
        WrittenBookContent book = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (book != null && "Kept by his bed".equals(book.title().raw())) {
            return TellTaleFloorboards.ID;
        }
        return null;
    }
}
