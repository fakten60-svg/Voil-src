package lol.lwes.media;

import java.awt.image.BufferedImage;

public class WindowsMediaProvider extends AbstractMediaProvider {
    private static final String CROP_APP_ID = "spotify";

    public WindowsMediaProvider() {
        if (this.available) {
            Native.initSpectrum(64);
        }
    }

    @Override
    protected BufferedImage processCover(BufferedImage image) {
        String appId = this.getSourceAppId();
        if (appId == null || !appId.toLowerCase().contains(CROP_APP_ID)) {
            return image;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int bottom = height;
        int misses = 0;
        for (int y = height - 1; y >= height / 2; y--) {
            if (isLetterboxRow(image, y, width, 0.5)) {
                bottom = y;
                misses = 0;
            } else if (++misses >= 5) {
                break;
            }
        }
        return bottom < height ? image.getSubimage(0, 0, width, bottom) : image;
    }

    private static boolean isLetterboxRow(BufferedImage image, int y, int width, double threshold) {
        int dark = 0;
        for (int x = 0; x < width; x++) {
            int rgb = image.getRGB(x, y) & 0xFFFFFF;
            int r = rgb >> 16 & 0xFF;
            int g = rgb >> 8 & 0xFF;
            int b = rgb & 0xFF;
            if (r < 16 && g < 16 && b < 16) {
                dark++;
            }
        }
        return dark >= width * threshold;
    }
}
