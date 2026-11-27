package lol.lwes.voil.util.font;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import lol.lwes.voil.util.render.EmojiGlyph;

public class EmojiTextRenderer {
    public static float getTextWidth(MSDFFontAtlas msdffontatlas, EmojiAtlas emojiatlas, String s, float f) {
        float f1 = 0.0F;
        int i = 0;
        int j = 0;

        while (i <= s.length()) {
            boolean flag = i == s.length();
            EmojiGlyph emojiglyph = flag ? null : emojiatlas.findGlyphAt(s, i);
            if (emojiglyph == null && !flag) {
                int j1 = Character.charCount(s.codePointAt(i));
                int i1 = i;
                i = i1 + j1;
            } else {
                if (i > j) {
                    f1 += msdffontatlas.getWidth(s.substring(j, i), f);
                }

                if (flag) {
                    break;
                }

                f1 += emojiatlas.getGlyphAdvance(emojiglyph, f);
                int l = emojiglyph.getEmoji().length();
                int k = i;
                i = k + l;
                j = i;
            }
        }

        return f1;
    }

    public static float renderText(MSDFFontAtlas msdffontatlas, EmojiAtlas emojiatlas, String s, float f, float f1, float f2, int i) {
        float f3 = f;
        int l = i >>> 24;
        float f4 = (l & 0xFF) / 255.0F;
        if (f4 == 0.0F) {
            f4 = 1.0F;
        }

        int j = 0;
        int k = 0;

        while (j <= s.length()) {
            boolean flag = j == s.length();
            EmojiGlyph emojiglyph = flag ? null : emojiatlas.findGlyphAt(s, j);
            if (emojiglyph == null && !flag) {
                j += Character.charCount(s.codePointAt(j));
            } else {
                if (j > k) {
                    String s1 = s.substring(k, j);
                    msdffontatlas.render(s1, f3, f1, f2, i);
                    f3 += msdffontatlas.getWidth(s1, f2);
                }

                if (flag) {
                    break;
                }

                f3 += emojiatlas.drawGlyph(emojiglyph, f3, f1, f2, f4);
                int i1 = emojiglyph.getEmoji().length();
                l = j;
                j = l + i1;
                k = j;
            }
        }

        return f3 - f;
    }
}
