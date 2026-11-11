package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseConfig;

/**
 * 0.4.473, the owner: "slow dialogue a bit across vignettes, please", and "if you could make a universal handler and then make it
 * configurable via config or settings that would be incredible".
 *
 * <p>How long words stay up is decided here, and nowhere else.
 * <ul>
 * <li><b>A person's line</b> (LiteraryActor.say, drawn as a caption by client.SpeechCaptions) stays up for {@link #ticks}: a
 * moment to find it, and time for each character, never under five seconds and never over twelve at the setting's usual
 * value. The actor takes its own line down when that is over ({@link LiteraryActor#saying}). A scene does not take a line down
 * on a timer of its own: it asks the actor whether it is still saying it.</li>
 * <li><b>A reader's own words</b> over their head (Spoken) stay up as long.</li>
 * <li><b>The action bar</b> (what a reader notices, and a person's line repeated there) stays up for {@link #noticed}: the
 * same, but never under the game's own three seconds (client.mixin.ActionBarWrapMixin).</li>
 * </ul>
 * This is the rule a person's line already had by itself (0.4.398). Before, most scenes cut every line off after three and a
 * half to six seconds however long it was, and the game's action bar gave every line three.
 *
 * <p><b>The setting.</b> {@code readingTimePercent} in the world's settings (HouseConfig; the game's own settings screen for
 * the mod shows it): 100 is the usual time, 150 half as long again, 50 half. It is the world's, so everybody in a world reads
 * at one pace, and a client is sent it with the rest of the world's settings.
 */
public final class Dialogue {
    private Dialogue(){}
    /**
     * At the setting's usual value: the ticks before a reader has found a line; the ticks each character of it takes; the least
     * and the most a person's line stays up; and the least what a reader notices does (the game's own time for the action bar).
     */
    public static final int FIND=40,A_CHARACTER=3,LEAST=100,MOST=240,NOTICED_LEAST=60;
    /** The least anything stays up, whatever the setting. */
    public static final int FLOOR=30;

    /** The setting: how long words stay up, as a percentage of the usual time. */
    public static int percent(){return HouseConfig.setting(HouseConfig.READING_TIME);}
    /** How long a person's line stays up, in ticks, at the world's setting. */
    public static int ticks(String text){return ticks(text,percent());}
    /** How long a person's line stays up, in ticks, at a setting. */
    public static int ticks(String text,int percent){return scaled(Math.min(MOST,Math.max(LEAST,usual(text))),percent);}
    /** How long what a reader notices stays on the action bar, in ticks, at the world's setting. */
    public static int noticed(String text){return noticed(text,percent());}
    public static int noticed(String text,int percent){return scaled(Math.min(MOST,Math.max(NOTICED_LEAST,usual(text))),percent);}

    private static int usual(String text){return FIND+A_CHARACTER*(text==null?0:text.codePointCount(0,text.length()));}
    private static int scaled(int usual,int percent){return Math.max(FLOOR,(int)Math.round(usual*(double)percent/100));}
}
