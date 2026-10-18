package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.NovelSceneClient;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The sky, sun, moon, stars and daylight read the day through dayTime() (by way of getTimeOfDay), which goes straight to
 * the level data and never through getDayTime(). A scene's own clock must reach them too, or Movie Night is lit like
 * whatever hour it is at home. Client only: the level data, packets and saved day are untouched.
 */
@Mixin(ClientLevel.class)
public abstract class HouseSceneSkyMixin {
    public long dayTime(){
        var level=(ClientLevel)(Object)this;
        return NovelSceneClient.sceneTime(level,level.getLevelData().getDayTime());
    }
}
