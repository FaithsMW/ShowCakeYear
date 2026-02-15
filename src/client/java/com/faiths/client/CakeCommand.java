package com.faiths.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class CakeCommand {
    private CakeCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("cakes")
                        .executes(context -> sendUsage(context.getSource()))
                        .then(literal("legacy")
                                .executes(context -> toggleLegacy(context.getSource())))
                        .then(argument("year", IntegerArgumentType.integer(1))
                                .then(argument("color", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (String color : CakeTextureOverrides.getAvailableCustomColors()) {
                                                builder.suggest(color);
                                            }
                                            builder.suggest("reset");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> handleChange(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "year"),
                                                StringArgumentType.getString(context, "color")
                                        )))
                                .executes(context -> showCurrentOverride(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "year")
                                )))
        ));
    }

    private static int handleChange(FabricClientCommandSource source, int year, String color) {
        if ("reset".equalsIgnoreCase(color)) {
            boolean wasOverridden = CakeTextureOverrides.resetOverride(year);
            if (wasOverridden) {
                source.sendFeedback(Component.literal("[Cakes] Year " + year + " reset to default behavior.").withStyle(ChatFormatting.GREEN));
            } else {
                source.sendFeedback(Component.literal("[Cakes] Year " + year + " already uses default behavior.").withStyle(ChatFormatting.YELLOW));
            }
            return 1;
        }

        CakeTextureOverrides.OverrideResult result = CakeTextureOverrides.setOverride(year, color);
        if (result == CakeTextureOverrides.OverrideResult.SUCCESS) {
            source.sendFeedback(Component.literal("[Cakes] Year " + year + " now uses '" + color.toLowerCase() + "' texture.").withStyle(ChatFormatting.GREEN));
            return 1;
        }

        if (result == CakeTextureOverrides.OverrideResult.RESERVED_COLOR) {
            source.sendFeedback(Component.literal("[Cakes] Color '" + color + "' is reserved. Reserved: " + CakeTextureOverrides.getReservedColorsDisplay() + ".").withStyle(ChatFormatting.RED));
            return 0;
        }

        List<String> availableColors = CakeTextureOverrides.getAvailableCustomColors();
        if (availableColors.isEmpty()) {
            source.sendFeedback(Component.literal("[Cakes] Texture '" + color + "' was not found. No custom colors are currently available in assets/showcakeyear/cakes/.").withStyle(ChatFormatting.RED));
        } else {
            source.sendFeedback(Component.literal("[Cakes] Texture '" + color + "' was not found. Available custom colors: " + String.join(", ", availableColors) + ".").withStyle(ChatFormatting.RED));
        }
        return 0;
    }

    private static int showCurrentOverride(FabricClientCommandSource source, int year) {
        if (CakeTextureOverrides.getOverrideForYear(year) == null) {
            source.sendFeedback(Component.literal("[Cakes] Year " + year + " uses default behavior. Set with /cakes " + year + " <color>.").withStyle(ChatFormatting.YELLOW));
        } else {
            source.sendFeedback(Component.literal("[Cakes] Year " + year + " has a custom texture override. Use /cakes " + year + " reset to remove it.").withStyle(ChatFormatting.GREEN));
        }
        return 1;
    }

    private static int sendUsage(FabricClientCommandSource source) {
        List<String> availableColors = CakeTextureOverrides.getAvailableCustomColors();
        source.sendFeedback(Component.literal("=== ShowCakeYear Command Tutorial ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        source.sendFeedback(Component.literal("/cakes legacy          -> toggle legacy pink name/lore color").withStyle(ChatFormatting.LIGHT_PURPLE));
        source.sendFeedback(Component.literal("/cakes <year> <color>  -> set custom texture for a year").withStyle(ChatFormatting.AQUA));
        source.sendFeedback(Component.literal("/cakes <year> reset    -> remove custom texture for a year").withStyle(ChatFormatting.AQUA));
        source.sendFeedback(Component.literal("/cakes <year>          -> show whether that year has an override").withStyle(ChatFormatting.AQUA));
        source.sendFeedback(Component.literal("Example: /cakes 67 purple").withStyle(ChatFormatting.LIGHT_PURPLE));
        source.sendFeedback(Component.literal("Example: /cakes 67 reset").withStyle(ChatFormatting.YELLOW));
        source.sendFeedback(Component.literal("Reserved colors (cannot be set manually): " + CakeTextureOverrides.getReservedColorsDisplay()).withStyle(ChatFormatting.RED));
        if (availableColors.isEmpty()) {
            source.sendFeedback(Component.literal("No custom colors found yet in assets/showcakeyear/cakes/. Add files like purple.png.").withStyle(ChatFormatting.YELLOW));
        } else {
            source.sendFeedback(Component.literal("Available custom colors: " + String.join(", ", availableColors)).withStyle(ChatFormatting.GREEN));
        }
        return 1;
    }

    private static int toggleLegacy(FabricClientCommandSource source) {
        boolean enabled = CakeTextureOverrides.toggleLegacyPinkEnabled();
        if (enabled) {
            source.sendFeedback(Component.literal("[Cakes] Legacy pink mode enabled. Cake name and SPECIAL lore are now pink.").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            source.sendFeedback(Component.literal("[Cakes] Legacy pink mode disabled.").withStyle(ChatFormatting.YELLOW));
        }
        return 1;
    }
}
