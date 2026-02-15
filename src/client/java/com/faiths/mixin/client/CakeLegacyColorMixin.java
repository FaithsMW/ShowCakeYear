package com.faiths.mixin.client;

import com.faiths.client.CakeItemHelper;
import com.faiths.client.CakeTextureOverrides;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mixin(ItemStack.class)
public class CakeLegacyColorMixin {
    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void applyLegacyCakeTooltipColors(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!CakeTextureOverrides.isLegacyPinkEnabled() || !CakeItemHelper.isSkyblockCake(stack)) {
            return;
        }

        List<Component> originalLines = cir.getReturnValue();
        if (originalLines == null || originalLines.isEmpty()) {
            return;
        }

        List<Component> updatedLines = new ArrayList<>(originalLines.size());
        boolean changed = false;

        for (int i = 0; i < originalLines.size(); i++) {
            Component line = originalLines.get(i);
            String raw = line.getString();

            if (i == 0) {
                updatedLines.add(Component.literal(raw).withStyle(ChatFormatting.LIGHT_PURPLE));
                changed = true;
                continue;
            }

            if (raw != null && raw.toUpperCase(Locale.ROOT).contains("SPECIAL")) {
                updatedLines.add(Component.literal(raw).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
                changed = true;
            } else {
                updatedLines.add(line);
            }
        }

        if (changed) {
            cir.setReturnValue(updatedLines);
        }
    }
}
