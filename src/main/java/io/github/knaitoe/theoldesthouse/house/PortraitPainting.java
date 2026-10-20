package io.github.knaitoe.theoldesthouse.house;

/** Native painting data, saved and tracked by Minecraft's existing entity machinery. */
public interface PortraitPainting {
    String housePortrait();
    void housePortrait(String profile);
}
