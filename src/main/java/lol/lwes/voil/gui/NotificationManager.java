package lol.lwes.voil.gui;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.Render2DEvent;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Notifications;
import lol.lwes.voil.util.ScreenCorner;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.UiEasings;

public class NotificationManager {
    private final Deque notifications = new ArrayDeque(10);

    private void renderDefaultNotification(Notification notification, float f, float f1, boolean flag) {
        Color color = flag ? new Color(136, 35, 3) : new Color(31, 100, 26);
        float f3 = notification.posX;
        float f4 = notification.posY;
        float f5 = f1 - 10.0F;
        Color color1 = new Color(23, 23, 23);
        Render2DUtil.drawGlow(f3, f4, f, f5, 1.0F, 1.0F, 6.0F, color1);
        float f2 = notification.posY;
        Render2DUtil.drawRoundedRect(notification.posX, f2, f, f1 - 10.0F, 3.0F, 1.0F, color);
        if (flag) {
            Color color2 = new Color(200, 50, 50);
            double d0 = notification.posX + 14.0F;
            Render2DUtil.drawPlus(color2, d0, notification.posY + 20.0F, 18.0F, 3.0F, 195.0F);
        } else {
            Render2DUtil.drawAxes(new Color(5, 162, 63), notification.posX + 14.0F, notification.posY + 18.0F, 10.0F, 18.0F, 3.0F, -45.0F);
        }

        VoilClient.fonts.getSelectedFont().renderWithShadow(notification.title.toString(), (int)(notification.posX + 30.0F), (int)notification.posY, 10.0F, -1);
        VoilClient.fonts
            .getSelectedFont()
            .renderWithShadow(notification.message.toString(), (int)(notification.posX + 30.0F), (int)(notification.posY + 20.0F), 10.0F, -1);
    }

    private void renderVoilNotification(Notification notification, float f, float f1, float f2, boolean flag) {
        float f3 = f * f2;
        float f10 = notification.posX - 5.0F;
        float f11 = notification.posY - 5.0F;
        float f5 = f1 + 2.0F;
        float f6 = f;
        float f7 = f11;
        float f8 = f10;
        Render2DUtil.drawBlurRect(f8, f7, f6, f5, 8.0F, 10.0F);
        float f4 = notification.posY + f1 - 3.0F - 5.0F;
        f10 = notification.posX - 2.0F;
        float f12 = f3 - 8.0F;
        Color color1 = GuiTheme.accentColor2;
        Render2DUtil.drawRoundedRect(f10, f4, f12, 3.0F, 1.5F, 4.0F, ColorUtil.withAlpha(color1, 128));
        Render2DUtil.drawRoundedRect(notification.posX - 2.5F, f4, f3 - 8.0F, 3.0F, 1.5F, 1.0F, GuiTheme.accentColor2);
        f10 = notification.posX - 5.0F;
        float f9 = notification.posY - 5.0F;
        Render2DUtil.drawOutline(f10, f9, f, f1 + 2.0F, 8.0F, 0.2F, 0.2F, new Color(56, 53, 68));
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE05D(), notification.posX + 3.0F, notification.posY + 1.0F, 24.0F, -1);
        VoilClient.fonts.getGreyscale().render(String.valueOf(notification.title), notification.posX + 30.0F, notification.posY - 2.0F, Color.white.getRGB());
        int i = (int)(notification.posY + f1 / 2.0F - VoilClient.fonts.getGreyscale().getLineHeight() / 2.0F);
        String s = notification.message.toString().toLowerCase();
        int j = s.indexOf(" on");
        int k = s.indexOf(" off");
        int l = -1;
        if (j == -1 || k != -1 && j >= k) {
            if (k != -1) {
                l = k;
            }
        } else {
            l = j;
        }

        String s1;
        String s2;
        if (l != -1) {
            s1 = notification.message.toString().substring(0, l);
            s2 = notification.message.toString().substring(l + 1);
        } else {
            s1 = notification.message.toString();
            s2 = "";
        }

        Color color = flag ? new Color(200, 50, 50) : new Color(5, 162, 63);
        VoilClient.fonts.getGreyscale().render(s1, notification.posX + 30.0F, i, Color.WHITE.getRGB());
        VoilClient.fonts.getGreyscale().render(s2, notification.posX + 30.0F + VoilClient.fonts.getGreyscale().getWidth(s1) + 5.0F, i, color.getRGB());
    }

    private void renderSkeetNotification(Notification notification, float f, float f1, float f2, boolean flag) {
        Color color = new Color(20, 20, 20);
        Color color1 = new Color(25, 25, 25);
        Color color2 = new Color(58, 58, 58);
        Color color3 = new Color(255, 0, 150);
        Color color4 = new Color(160, 10, 255);
        float f3 = f * f2;
        float f7 = notification.posX - 5.0F;
        float f4 = notification.posY - 5.0F;
        Render2DUtil.drawRoundedRect(f7, f4, f, f1 + 2.0F, 1.0F, 1.0F, color1);
        float f5 = notification.posY;
        Render2DUtil.drawRoundedRect(notification.posX, f5, f - 10.0F, f1 - 5.0F, 0.0F, 1.0F, color);
        float f6 = notification.posY;
        Render2DUtil.drawRoundedRect(notification.posX, f6, f - 10.0F, 5.0F, 0.0F, 1.0F, color2);
        Render2DUtil.drawRectGradient(
            notification.posX,
            notification.posY + 1.0F,
            f3,
            1.0F,
            0.0F,
            10.0F,
            ColorUtil.withAlpha(color3, 100),
            ColorUtil.withAlpha(color3, 100),
            ColorUtil.withAlpha(color4, 100),
            ColorUtil.withAlpha(color4, 100)
        );
        Render2DUtil.drawRectGradient(notification.posX, notification.posY, f3, 5.0F, 0.0F, 1.0F, color3, color3, color4, color4);
        int i = (int)VoilClient.fonts.getSelectedFont().getWidth(notification.message.toString());
        int j = (int)(notification.posX + (f - 10.0F) / 2.0F - i / 2.0F);
        int k = (int)(notification.posY + f1 / 2.0F - VoilClient.fonts.getSelectedFont().getLineHeight() / 2.0F);
        String s = notification.message.toString().toLowerCase();
        int l = s.indexOf(" on");
        int i1 = s.indexOf(" off");
        int j1 = -1;
        if (l == -1 || i1 != -1 && l >= i1) {
            if (i1 != -1) {
                j1 = i1;
            }
        } else {
            j1 = l;
        }

        String s1;
        String s2;
        if (j1 != -1) {
            s1 = notification.message.toString().substring(0, j1);
            String s3 = notification.message.toString().substring(j1 + 1);
            s2 = s3;
        } else {
            s1 = notification.message.toString();
            s2 = "";
        }

        Color color5 = flag ? new Color(200, 50, 50) : new Color(5, 162, 63);
        VoilClient.fonts.getSelectedFont().renderWithShadow(s1, j, k, 10.0F, Color.WHITE.getRGB());
        VoilClient.fonts.getSelectedFont().renderWithShadow(s2, j + VoilClient.fonts.getSelectedFont().getWidth(s1), k, 10.0F, color5.getRGB());
    }

    private float getTargetY(ScreenCorner screencorner, float f, int i) {
        return screencorner != ScreenCorner.BOTTOM_LEFT && screencorner != ScreenCorner.BOTTOM_RIGHT
            ? 10.0F + i * (f + 5.0F)
            : VoilClient.mc.getWindow().getScaledHeight() - f - 10.0F - i * (f + 5.0F);
    }

    private float getStartX(ScreenCorner screencorner, float f) {
        return screencorner != ScreenCorner.TOP_RIGHT && screencorner != ScreenCorner.BOTTOM_RIGHT
            ? 10.0F
            : VoilClient.mc.getWindow().getScaledWidth() - f - 10.0F;
    }

    private void renderBlurNotification(Notification notification, float f, float f1) {
        float f2 = notification.posY;
        float f3 = notification.posX;
        Render2DUtil.drawBlurRect(f3, f2, f, f1, 12.0F, 10.0F);
        VoilClient.fonts
            .getSelectedFont()
            .renderDiagonalGradient(
                notification.title.toString(),
                (int)(notification.posX + 14.0F),
                (int)(notification.posY + 10.0F),
                0.0F,
                new Color(230, 80, 200),
                new Color(120, 21, 178),
                10,
                12.0F
            );
        VoilClient.fonts
            .getSelectedFont()
            .renderDiagonalGradient(
                notification.message.toString(),
                (int)(notification.posX + 14.0F),
                (int)(notification.posY + 30.0F),
                0.0F,
                Color.WHITE,
                Color.white.darker(),
                10,
                12.0F
            );
    }

    public void addNotification(String s, CharSequence charsequence) {
        if (VoilClient.mc.player != null) {
            if (this.notifications.size() >= 10) {
                this.notifications.remove(0);
            }
            ScreenCorner screencorner = switch (Notifications.position.getMode().toUpperCase()) {
                case "TOP_LEFT", "TOPLEFT" -> ScreenCorner.TOP_LEFT;
                case "TOP_RIGHT", "TOPRIGHT" -> ScreenCorner.TOP_RIGHT;
                case "BOTTOM_LEFT", "BOTTOMLEFT" -> ScreenCorner.BOTTOM_LEFT;
                case "BOTTOM_RIGHT", "BOTTOMRIGHT" -> ScreenCorner.BOTTOM_RIGHT;
                default -> ScreenCorner.TOP_LEFT;
            };
            Notification notification = new Notification(s, charsequence, screencorner);
            float f = 150.0F;
            notification.posX = this.getStartX(screencorner, f);
            notification.posY = this.getStartY(screencorner, 50.0F);
            notification.targetY = this.getTargetY(screencorner, 50.0F, this.notifications.size());
            this.notifications.add(notification);
        }
    }

    private float getStartY(ScreenCorner screencorner, float f) {
        return screencorner != ScreenCorner.BOTTOM_LEFT && screencorner != ScreenCorner.BOTTOM_RIGHT ? -f : VoilClient.mc.getWindow().getScaledHeight();
    }

    @EventHandler
    public void onRender(Render2DEvent render2devent) {
        if (VoilClient.mc.player != null && !this.notifications.isEmpty()) {
            if (ModuleManager.INSTANCE.isEnabled(Notifications.class)) {
                Iterator iterator = this.notifications.iterator();

                while (iterator.hasNext()) {
                    Notification notification = (Notification)iterator.next();
                    long i = System.currentTimeMillis() - notification.createdAt;
                    if (i > 3000L) {
                        iterator.remove();
                    } else {
                        float f = Math.min(1.0F, (float)i / 500.0F);
                        float f1 = UiEasings.easeOutExpo(f);
                        notification.posY = notification.posY + (notification.targetY - notification.posY) * f1;
                        if (i > 2500L) {
                            float f2 = (float)(i - 2500L) / 500.0F;
                            float f3 = UiEasings.easeInOutSine(Math.min(1.0F, f2));
                            notification.posY += f3 * 50.0F;
                        }

                        boolean flag = notification.message.toString().contains("off") || notification.message.toString().contains("disabled");
                        switch (Notifications.style.getMode()) {
                            case "Blur" -> this.renderBlurNotification(notification, 150.0F, 50.0F);
                            case "Skeet" -> this.renderSkeetNotification(notification, 150.0F, 50.0F, (float)i / 3000.0F, flag);
                            case "Voil" -> this.renderVoilNotification(notification, 150.0F, 35.0F, (float)i / 3000.0F, flag);
                            default -> this.renderDefaultNotification(notification, 150.0F, 50.0F, flag);
                        }
                    }
                }
            }
        }
    }
}
