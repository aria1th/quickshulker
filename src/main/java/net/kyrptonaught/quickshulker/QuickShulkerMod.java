package net.kyrptonaught.quickshulker;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.kyrptconfig.config.ConfigManager;
import net.kyrptonaught.quickshulker.api.*;
import net.kyrptonaught.quickshulker.config.ConfigOptions;
import net.kyrptonaught.quickshulker.network.OpenShulkerPacket;
import net.kyrptonaught.quickshulker.network.QuickBundlePacket;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.EggItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;


public class QuickShulkerMod implements ModInitializer, RegisterQuickShulker {
    public static final String MOD_ID = "quickshulker";
    public static ConfigManager.SingleConfigManager config = new ConfigManager.SingleConfigManager(MOD_ID, new ConfigOptions());
    public static double lastMouseX, lastMouseY;

    @Override
    public void onInitialize() {
        config.load();
        OpenShulkerPacket.registerReceivePacket();
        QuickBundlePacket.registerReceivePacket();
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient) {
                if (QuickShulkerMod.getConfig().rightClickToOpen) {
                    if (Util.isOpenableItem(stack)) {
                        if (hand == Hand.MAIN_HAND)
                            Util.openItem(player, 0, player.getInventory().selectedSlot);
                        else Util.openItem(player, 0, PlayerInventory.OFF_HAND_SLOT);

                        return TypedActionResult.success(stack);
                    }
                }
            }
            return TypedActionResult.pass(stack);
        });
        FabricLoader.getInstance().getEntrypoints(MOD_ID, RegisterQuickShulker.class).forEach(RegisterQuickShulker::registerProviders);
    }

    public static ConfigOptions getConfig() {
        return (ConfigOptions) config.getConfig();
    }

    @Override
    public void registerProviders() {
        if (getConfig().quickShulkerBox)
            QuickOpenableRegistry.register(ShulkerBoxBlock.class, true, ((player, stack) -> player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                    new ShulkerBoxScreenHandler(i, player.getInventory(), new ItemStackInventory(stack, 27)), stack.hasCustomName() ? stack.getName() : new TranslatableText("container.shulkerBox")))));

        if (getConfig().quickEChest) {
            QuickShulkerData.QuickEnderData enderData = new QuickShulkerData.QuickEnderData(((player, stack) -> player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                    GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, player.getEnderChestInventory()), new TranslatableText("container.enderchest")))), true, true);
            QuickOpenableRegistry.register(EnderChestBlock.class, enderData);
        }
        if (getConfig().quickCraftingTables)
            new QuickOpenableRegistry.Builder().setItem(CraftingTableBlock.class).ignoreSingleStackCheck(true)
                    .setOpenAction(((player, stack) -> player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                            new CraftingScreenHandler(i, playerInventory, ScreenHandlerContext.create(player.getEntityWorld(), player.getBlockPos())), new TranslatableText("container.crafting")))))
                    .register();

        if (getConfig().quickStonecutter)
            new QuickOpenableRegistry.Builder().setItem(StonecutterBlock.class).ignoreSingleStackCheck(true)
                    .setOpenAction(((player, stack) -> player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                            new StonecutterScreenHandler(i, playerInventory, ScreenHandlerContext.create(player.getEntityWorld(), player.getBlockPos())), new TranslatableText("container.stonecutter")))))
                    .register();
    }
}
