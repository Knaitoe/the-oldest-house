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
 * will not keep her. {@code acknowledged}: she and her recipient have
 * greeted each other, so she may lead them to the manor.
 */
public record HillaryTag(UUID recipient, BlockPos home, boolean acknowledged) {
    public static final HillaryTag NONE = new HillaryTag(Util.NIL_UUID, BlockPos.ZERO, false);

    public static final Codec<HillaryTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("recipient").forGetter(HillaryTag::recipient),
            BlockPos.CODEC.fieldOf("home").forGetter(HillaryTag::home),
            Codec.BOOL.optionalFieldOf("acknowledged", false).forGetter(HillaryTag::acknowledged)
    ).apply(instance, HillaryTag::new));

    public HillaryTag(UUID recipient, BlockPos home) {
        this(recipient, home, false);
    }

    public boolean isSet() {
        return !recipient.equals(Util.NIL_UUID);
    }

    public HillaryTag withHome(BlockPos newHome) {
        return new HillaryTag(recipient, newHome.immutable(), acknowledged);
    }

    public HillaryTag withAcknowledged() {
        return new HillaryTag(recipient, home, true);
    }
}
