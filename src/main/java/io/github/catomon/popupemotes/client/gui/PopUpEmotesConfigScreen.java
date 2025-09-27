package io.github.catomon.popupemotes.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.catomon.popupemotes.client.ClientEmotePacksManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PopUpEmotesConfigScreen extends Screen {

    public PopUpEmotesConfigScreen() {
        super(Component.translatable("pop_up_emotes.configuration.title"));
    }

    private ResourceLocation[] emoteTextures = new ResourceLocation[8];

    private float reloadTimeout = 0f;

    private void loadCustomEmoteTextures() {
        Map<Integer, byte[]> emotePack = ClientEmotePacksManager.getLocalEmotePack();

        if (emotePack == null) {
            emotePack = new HashMap<>();
        }

        Minecraft mc = Minecraft.getInstance();

        for (int i = 0; i < emoteTextures.length; i++) {
            byte[] bytes = emotePack.get(i);
            if (bytes != null) {
                try {
                    NativeImage img = NativeImage.read(new ByteArrayInputStream(bytes));
                    DynamicTexture texture = new DynamicTexture(img);
                    ResourceLocation location = mc.getTextureManager().register("emote_" + i, texture);
                    emoteTextures[i] = location;
                } catch (Exception e) {
                    emoteTextures[i] = null;
                    e.printStackTrace();
                }
            } else {
                emoteTextures[i] = null;
            }
        }
    }

    private Button reloadButton = null;

    @Override
    protected void init() {
        boolean inGame = this.minecraft != null && this.minecraft.level != null;

        reloadButton = Button.builder(Component.translatable("pop_up_emotes.config.reload"), button -> {
            ClientEmotePacksManager.recreateCache();
            loadCustomEmoteTextures();
            button.active = false;
            reloadTimeout = 20f;
        }).bounds(this.width / 2 - 100, this.height / 2, 200, 20).build();

        reloadButton.active = !inGame;

        this.addRenderableWidget(reloadButton);

        Button openFolderButton = Button.builder(Component.translatable("pop_up_emotes.config.open_emotes_folder"), button -> {
            try {
                File folder = ClientEmotePacksManager.getEmotePackFolder().toFile();
                folder.mkdirs();
                if (folder.exists()) {
                    if (Desktop.isDesktopSupported() && !GraphicsEnvironment.isHeadless()) {
                        Desktop.getDesktop().open(folder);
                    } else {
                        String os = System.getProperty("os.name").toLowerCase();
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec("explorer " + folder.getAbsolutePath());
                        } else if (os.contains("mac")) {
                            Runtime.getRuntime().exec(new String[]{"open", folder.getAbsolutePath()});
                        } else if (os.contains("nix") || os.contains("nux")) {
                            Runtime.getRuntime().exec(new String[]{"xdg-open", folder.getAbsolutePath()});
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).bounds(this.width / 2 - 100, this.height / 2 + 24, 200, 20).build();

        this.addRenderableWidget(openFolderButton);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> {
            onClose();
        }).bounds(this.width / 2 - 100, this.height / 2 + 50, 200, 20).build());

        if (!inGame)
            loadCustomEmoteTextures();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        drawTitle(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);

        if (reloadTimeout > -1000) {
            reloadTimeout -= partialTicks;
            if (reloadTimeout <= 0) {
                reloadButton.active = true;
                reloadTimeout = -1000f;
            }
        }

        drawReloadWarning(graphics);

        int xStart = this.width / 2 - 88;
        int yStart = this.height / 2 + 90;
        int size = 22;

        Minecraft mc = Minecraft.getInstance();

        boolean anyEmotes = false;
        for (int i = 0; i < emoteTextures.length; i++) {
            ResourceLocation tex = emoteTextures[i];
            if (tex != null) {
                anyEmotes = true;
                mc.getTextureManager().bindForSetup(tex);
                graphics.blit(tex, xStart + i * (size + 2), yStart, 0, 0, size, size, size, size);
            }
        }

        if (!anyEmotes) {
            String noEmotesMsg = Component.translatable("pop_up_emotes.config.no_emotes_found").getString();
            int msgX = this.width / 2 - this.font.width(noEmotesMsg) / 2;
            int msgY = yStart + size / 2 - this.font.lineHeight / 2;
            graphics.drawString(this.font, noEmotesMsg, msgX, msgY, 0xFFFFFF, false);
        }
    }

    private void drawReloadWarning(GuiGraphics graphics) {
        if (this.minecraft != null && this.minecraft.level != null) {
            String message = Component.translatable("pop_up_emotes.config.reload_disabled").getString();
            int msgX = this.width / 2 - this.font.width(message) / 2;
            int msgY = this.height / 2 - 20;
            graphics.drawString(this.font, message, msgX, msgY, 0xFFFF5555, false);
        }
    }

    private void drawTitle(GuiGraphics graphics) {
        int x = (this.width - this.font.width(this.title)) / 2;
        int y = 10;
        graphics.drawString(this.font, this.title, x, y, 0xFFFFFF, false);
    }
}