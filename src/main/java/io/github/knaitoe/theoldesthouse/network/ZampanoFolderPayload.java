package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Opens (or refreshes) Zampanò's pages on the reader's screen: a folder from one of the courtyard drawers, or, with folder
 * -1, the one loose leaf the reader holds. Pulled is the reader's own mask of leaves already taken from the drawers. The
 * words themselves are ZampanoLeaves, which both sides have.
 */
public record ZampanoFolderPayload(int folder,int leaf,int pulled) implements CustomPacketPayload {
    public static final Type<ZampanoFolderPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"zampano_folder"));
    public static final StreamCodec<ByteBuf,ZampanoFolderPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,ZampanoFolderPayload::folder,ByteBufCodecs.VAR_INT,ZampanoFolderPayload::leaf,ByteBufCodecs.VAR_INT,ZampanoFolderPayload::pulled,ZampanoFolderPayload::new);
    @Override public Type<ZampanoFolderPayload> type(){return TYPE;}
}
