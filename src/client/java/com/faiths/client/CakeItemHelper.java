package com.faiths.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CakeItemHelper {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

    private CakeItemHelper() {
    }

    public static boolean isSkyblockCake(ItemStack stack) {
        return extractYear(stack) != null;
    }

    public static String extractYear(ItemStack stack) {
        try {
            if (!stack.is(Items.CAKE)) {
                return null;
            }

            Component customName = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
            String nameString = customName != null ? customName.getString() : "";

            if (!nameString.toLowerCase().contains("cake")) {
            }

            Matcher matcher = NUMBER_PATTERN.matcher(nameString);
            if (matcher.find()) {
                return matcher.group(1);
            }

            var loreComponent = stack.get(net.minecraft.core.component.DataComponents.LORE);
            if (loreComponent != null) {
                for (Component line : loreComponent.lines()) {
                    String lineString = line.getString();
                    matcher = NUMBER_PATTERN.matcher(lineString);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
