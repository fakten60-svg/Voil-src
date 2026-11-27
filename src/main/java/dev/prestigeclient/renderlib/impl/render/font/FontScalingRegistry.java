package dev.prestigeclient.renderlib.impl.render.font;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FontScalingRegistry {
    private static final List entries = new ArrayList();
    private static int lastSize = -1;

    public static void register(Font font, Font[] afont) {
        synchronized (entries) {
            entries.add(new WeakReference<>(font));

            for (Font font1 : afont) {
                entries.add(new WeakReference<>(font1));
            }
        }

        resize(lastSize);
    }

    public static void resize(int i) {
        synchronized (entries) {
            Iterator iterator = entries.iterator();

            while (iterator.hasNext()) {
                Font font = (Font)((WeakReference)iterator.next()).get();
                if (font == null) {
                    iterator.remove();
                } else if (font.lastScale != i) {
                    font.setScale(i);
                }
            }
        }

        lastSize = i;
    }
}
