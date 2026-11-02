package io.github.knaitoe.theoldesthouse.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * 0.4.271, the reader's comfort settings, read one way everywhere (the owner: "Make that behavior consistent ... A player selecting
 * zero should get predictable results for cosmetic effects"). Vanilla's two sliders under Accessibility decide:
 * <ul>
 * <li><b>Distortion Effects</b> ({@link #effects}): every shake, wobble, tilt and tumble of the camera, every flash, and the static
 * and grain laid over sight scale with it, from nothing at 0 to their full strength at 1. Nothing is clamped to a floor.</li>
 * <li><b>FOV Effects</b> ({@link #fov}): every narrowing or widening of sight that is a feeling rather than a lens scales with it.</li>
 * </ul>
 * What a scene needs the reader to see or not see is not cosmetic and does not scale: the dark of the covered well, a blindfold, a
 * fade between places, a viewfinder or a lens, a camera held still for a take, the words of a card.
 */
public final class Comfort {
    private Comfort(){}
    /** Distortion Effects, 0 to 1 (1 before the options are read). */
    public static float effects(){var mc=Minecraft.getInstance();return mc==null||mc.options==null?1F:Mth.clamp(mc.options.screenEffectScale().get().floatValue(),0F,1F);}
    /** FOV Effects, 0 to 1 (1 before the options are read). */
    public static float fov(){var mc=Minecraft.getInstance();return mc==null||mc.options==null?1F:Mth.clamp(mc.options.fovEffectScale().get().floatValue(),0F,1F);}
    /** A field of view taken toward {@code factor} of itself, as far as FOV Effects allows: at 0 it is left as it was. */
    public static double narrow(double fov,double factor){return fov*(1-(1-factor)*fov());}
}
