package lol.lwes.voil.util.media;

import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lol.lwes.media.MediaManager;
import lol.lwes.media.MediaProvider;
import lol.lwes.media.Native;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.PlayerTargetUtil;

public class MediaController {
    private GlTexture coverTexture;
    private GlTexture previousCoverTexture;
    private boolean needsRefresh;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean fetching = new AtomicBoolean(false);
    private static final int SPECTRUM_BANDS = 25;

    public String getTitle() {
        return MediaManager.getProvider().getTitle();
    }

    public boolean isPlaying() {
        return !this.isIdle() && MediaManager.getProvider().isPlaying();
    }

    public boolean isIdle() {
        return !this.needsRefresh || this.coverTexture == null;
    }

    public GlTexture getCover() {
        return this.coverTexture;
    }

    public long getPosition() {
        return MediaManager.getProvider().getPositionMs() / 1000L;
    }

    public String getArtist() {
        return MediaManager.getProvider().getArtist();
    }

    public void previous() {
        if (this.needsRefresh) {
            this.executor.submit(() -> MediaManager.getProvider().previous());
        }
    }

    public GlTexture getPreviousCover() {
        return this.previousCoverTexture;
    }

    public boolean update() {
        if (!this.fetching.compareAndSet(false, true)) {
            return true;
        }

        this.executor.submit(() -> {
            try {
                MediaProvider mediaprovider = MediaManager.getProvider();
                Native.initSpectrum(SPECTRUM_BANDS);
                mediaprovider.update();
                String s = mediaprovider.getTitle();
                if (s == null || s.isEmpty()) {
                    this.needsRefresh = false;
                    if (this.coverTexture != null) {
                        VoilClient.mc.execute(() -> {
                            if (this.coverTexture != null) {
                                this.coverTexture.close();
                                this.coverTexture = null;
                            }
                        });
                    }

                    return;
                }

                this.needsRefresh = true;
                if (mediaprovider.isCoverChanged()) {
                    BufferedImage bufferedimage = mediaprovider.getCoverImage();
                    VoilClient.mc.execute(() -> {
                        if (this.previousCoverTexture != null) {
                            this.previousCoverTexture.close();
                        }

                        this.previousCoverTexture = this.coverTexture;
                        this.coverTexture = bufferedimage != null ? PlayerTargetUtil.createTextureFromImage(bufferedimage) : null;
                    });
                }
            } finally {
                this.fetching.set(false);
            }
        });
        return true;
    }

    public void seek(long i) {
        if (this.needsRefresh) {
            this.executor.submit(() -> {
                MediaProvider mediaprovider = MediaManager.getProvider();
                long k = i * 1000L;
                return mediaprovider.seek(k);
            });
        }
    }

    public void next() {
        if (this.needsRefresh) {
            this.executor.submit(() -> MediaManager.getProvider().next());
        }
    }

    public long getDuration() {
        return MediaManager.getProvider().getDurationMs() / 1000L;
    }

    public void playPause() {
        if (this.needsRefresh) {
            this.executor.submit(() -> MediaManager.getProvider().togglePlayPause());
        }
    }
}
