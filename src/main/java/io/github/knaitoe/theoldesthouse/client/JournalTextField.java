package io.github.knaitoe.theoldesthouse.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultilineTextField;

/** Vanilla's multi-line text engine (wrapping, cursor, selection, clipboard), read out so the journal can draw it in graphite on paper. */
final class JournalTextField extends MultilineTextField {
    JournalTextField(Font font,int width){super(font,width);}
    /** The start and end of each wrapped line. */
    List<int[]> lines(){var out=new ArrayList<int[]>();for(var line:iterateLines())out.add(new int[]{line.beginIndex(),line.endIndex()});return out;}
    int[] selection(){var s=getSelected();return new int[]{s.beginIndex(),s.endIndex()};}
    int[] line(int i){var l=getLineView(i);return new int[]{l.beginIndex(),l.endIndex()};}
}
