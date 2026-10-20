package io.github.knaitoe.theoldesthouse.house;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import javax.annotation.Nullable;

/** A skin snapshot, not a URL supplied by a client. Kept on the original painting. */
public record PortraitProfile(UUID owner, String name, String texture, String signature) {
    public static PortraitProfile of(GameProfile profile) {
        var properties = profile.getProperties().get("textures");
        var skin = properties.isEmpty() ? null : properties.iterator().next();
        return new PortraitProfile(profile.getId(), profile.getName(), skin == null ? "" : skin.value(),
                skin == null || skin.signature() == null ? "" : skin.signature());
    }

    public String encode() { return owner + "\n" + name + "\n" + texture + "\n" + signature; }

    public static @Nullable PortraitProfile decode(String encoded) {
        if (encoded.length() > 16384) return null;
        var fields = encoded.split("\n", -1);
        if (fields.length != 4 || fields[1].isEmpty() || fields[1].length() > 16) return null;
        try { return new PortraitProfile(UUID.fromString(fields[0]), fields[1], fields[2], fields[3]); }
        catch (IllegalArgumentException exception) { return null; }
    }

    public GameProfile gameProfile() {
        var profile = new GameProfile(owner, name);
        if (!texture.isEmpty()) profile.getProperties().put("textures", signature.isEmpty()
                ? new Property("textures", texture) : new Property("textures", texture, signature));
        return profile;
    }
}
