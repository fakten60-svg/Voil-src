package lol.lwes.voil.util.font;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import java.io.IOException;
import java.util.ArrayList;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.util.render.RenderManager;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class FontManager implements SimpleSynchronousResourceReloadListener {
    private MSDFFontAtlas interBold;
    private MSDFFontAtlas inter;
    private MSDFFontAtlas verdana;
    private MSDFFontAtlas poppins;
    private MSDFFontAtlas icons;
    private MSDFFontAtlas greyscale;
    private MSDFFontAtlas quicksand;
    private MSDFFontAtlas sfRegular;
    private MSDFFontAtlas media;
    private EmojiAtlas emojiAtlas;
    private MSDFFontAtlas[] allFonts = new MSDFFontAtlas[0];

    public MSDFFontAtlas getGreyscale() {
        return this.greyscale;
    }

    public MSDFFontAtlas getMedia() {
        return this.media;
    }

    public MSDFFontAtlas getInter() {
        return this.inter;
    }

    public MSDFFontAtlas getVerdana() {
        return this.verdana;
    }

    public MSDFFontAtlas getSfRegular() {
        return this.sfRegular;
    }

    public MSDFFontAtlas getIcons() {
        return this.icons;
    }

    public MSDFFontAtlas getSelectedFont() {
        switch ((String)ClientSettings.fontName.getValue()) {
            case "InterBold":
                return this.interBold;
            case "Inter":
                return this.inter;
            case "Verdana":
                return this.verdana;
            case "Poppins":
                return this.poppins;
            case "Quicksand":
                return this.quicksand;
            case "SfRegular":
                return this.sfRegular;
            default:
                return this.verdana;
        }
    }

    public MSDFFontAtlas getPoppins() {
        return this.poppins;
    }

    public MSDFFontAtlas getInterBold() {
        return this.interBold;
    }

    public MSDFFontAtlas[] getAllFonts() {
        MSDFFontAtlas[] amsdffontatlas = new MSDFFontAtlas[this.allFonts.length];
        System.arraycopy(this.allFonts, 0, amsdffontatlas, 0, amsdffontatlas.length);
        return amsdffontatlas;
    }

    public MSDFFontAtlas getQuicksand() {
        return this.quicksand;
    }

    public EmojiAtlas getEmojiAtlas() {
        return this.emojiAtlas;
    }

    @Override
    public void reload(ResourceManager resourcemanager) {
        try {
            this.interBold = new MSDFFontAtlas("assets/tulip", "interbold");
            this.inter = new MSDFFontAtlas("assets/tulip", "inter");
            this.verdana = new MSDFFontAtlas("assets/tulip", "verdana");
            this.poppins = new MSDFFontAtlas("assets/tulip", "poppins");
            this.icons = new MSDFFontAtlas("assets/tulip", "icons");
            this.greyscale = new MSDFFontAtlas("assets/tulip", "greyscale");
            this.quicksand = new MSDFFontAtlas("assets/tulip", "quicksand");
            this.sfRegular = new MSDFFontAtlas("assets/tulip", "sf-regular");
            this.media = new MSDFFontAtlas("assets/tulip", "media");
            this.allFonts = new MSDFFontAtlas[]{
                this.interBold, this.inter, this.verdana, this.poppins, this.icons, this.greyscale, this.quicksand, this.sfRegular, this.media
            };

            for (MSDFFontAtlas msdffontatlas : this.allFonts) {
                msdffontatlas.fontRenderLayerModifier = (msdffontatlas1, astateapplier) -> {
                    ArrayList<StateApplier> arraylist = new ArrayList<>();

                    for (StateApplier stateapplier : astateapplier) {
                        if (!(stateapplier instanceof StateApplier.TargetFramebufferState)) {
                            arraylist.add(stateapplier);
                        }
                    }

                    arraylist.add(StateApplier.framebufferState(RenderManager.getCurrentFramebuffer()));
                    return arraylist.toArray(new StateApplier[0]);
                };
                msdffontatlas.fontToUIRenderer = msdffontatlas1 -> RenderManager.uiRenderer;
                msdffontatlas.scissorProvider = msdffontatlas1 -> RenderManager.getScissorRect();
                this.emojiAtlas = new EmojiAtlas("assets/tulip", "emoji");
                this.emojiAtlas.rendererProvider = emojiatlas -> RenderManager.uiRenderer;
                this.emojiAtlas.offsetProvider = (emojiatlas, astateapplier) -> {
                    ArrayList<StateApplier> arraylist = new ArrayList<>();

                    for (StateApplier stateapplier : astateapplier) {
                        if (!(stateapplier instanceof StateApplier.TargetFramebufferState)) {
                            arraylist.add(stateapplier);
                        }
                    }

                    arraylist.add(StateApplier.framebufferState(RenderManager.getCurrentFramebuffer()));
                    return arraylist.toArray(new StateApplier[0]);
                };
                this.emojiAtlas.tintProvider = emojiatlas -> RenderManager.getScissorRect();
            }
        } catch (IOException ioexception) {
            throw new RuntimeException("Couldn't load fonts", ioexception);
        }
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("tulip", "reload_fonts");
    }
}
