package com.faiths.mixin.client;

import com.faiths.client.CakeItemHelper;
import com.faiths.client.CakeTextureOverrides;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class CakeMixin {
    private static final float YEAR_TEXT_SCALE_X = 0.78f;
    private static final float YEAR_TEXT_SCALE_Y = 1.0f;
    private static final int DEFAULT_TEXT_COLOR = 0xFF000000;
    private static final int OUTLINE_COLOR = 0xFFFFFFFF;
    private static final ResourceLocation DEFAULT_CAKE_TEXTURE = ResourceLocation.fromNamespaceAndPath("showcakeyear", "cakes/default.png");
    private static final ResourceLocation BLACK_CAKE_TEXTURE = ResourceLocation.fromNamespaceAndPath("showcakeyear", "cakes/black.png");
    private static final ResourceLocation BLUE_CAKE_TEXTURE = ResourceLocation.fromNamespaceAndPath("showcakeyear", "cakes/blue.png");

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", 
            at = @At("HEAD"))
    private void renderCakeYear(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }

        String year = CakeItemHelper.extractYear(stack);
        if (year != null) {
            GuiGraphics graphics = (GuiGraphics) (Object) this;
            int cakeYear = parseYear(year);
            ResourceLocation cakeTexture = getCakeTexture(cakeYear);
            graphics.blit(RenderPipelines.GUI_TEXTURED, cakeTexture, x, y, 0.0F, 0.0F, 16, 16, 16, 16);

            int textWidth = font.width(year);
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + 8.0f, y + 4.0f);
            graphics.pose().scale(YEAR_TEXT_SCALE_X, YEAR_TEXT_SCALE_Y);
            drawOutlinedText(graphics, font, year, -textWidth / 2, 0, DEFAULT_TEXT_COLOR, OUTLINE_COLOR);
            graphics.pose().popMatrix();
        }
    }

    private int parseYear(String yearText) {
        try {
            return Integer.parseInt(yearText);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private ResourceLocation getCakeTexture(int year) {
        ResourceLocation overrideTexture = CakeTextureOverrides.getOverrideForYear(year);
        if (overrideTexture != null) {
            return overrideTexture;
        }

        if (year >= 3 && (year - 3) % 6 == 0) {
            return BLACK_CAKE_TEXTURE;
        }

        if (year >= 4 && (year - 4) % 6 == 0) {
            return BLUE_CAKE_TEXTURE;
        }

        return DEFAULT_CAKE_TEXTURE;
    }

    private void drawOutlinedText(GuiGraphics graphics, Font font, String text, int x, int y, int textColor, int outlineColor) {
        graphics.drawString(font, text, x - 1, y, outlineColor, false);
        graphics.drawString(font, text, x + 1, y, outlineColor, false);
        graphics.drawString(font, text, x, y - 1, outlineColor, false);
        graphics.drawString(font, text, x, y + 1, outlineColor, false);
        graphics.drawString(font, text, x, y, textColor, false);
    }

}
