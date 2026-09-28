package io.github.knaitoe.theoldesthouse.opening;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;

/**
 * Marks a wolf as one player's Hillary. {@code home} is the doorstep she was
 * found on; she keeps to it until tamed and is returned to it when the House
 * will not keep her.
 */
public record HillaryTag(UUID recipient, BlockPos home) {
    public static final HillaryTag NONE = new HillaryTag(Util.NIL_UUID, BlockPos.ZERO);

    public static final Codec<HillaryTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("recipient").forGetter(HillaryTag::recipient),
            BlockPos.CODEC.fieldOf("home").forGetter(HillaryTag::home)
    ).apply(instance, HillaryTag::new));

    public boolean isSet() {
        return !recipient.equals(Util.NIL_UUID);
    }
}
