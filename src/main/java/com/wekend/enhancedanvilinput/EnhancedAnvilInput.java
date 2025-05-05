package com.wekend.enhancedanvilinput;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

@Mod(modid = EnhancedAnvilInput.MODID, version = EnhancedAnvilInput.VERSION)
public class EnhancedAnvilInput
{
    public static final String MODID = "enhancedanvilinput";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private Field nameFieldReflection = null;
    private boolean reflectionFailed = false; // Flag if reflection setup fails

    public EnhancedAnvilInput() {
        MinecraftForge.EVENT_BUS.register(this);
        initializeReflection();
    }

    private void initializeReflection() {
        try {
            nameFieldReflection = ReflectionHelper.findField(GuiRepair.class, "field_147091_w", "nameField");
            nameFieldReflection.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during reflection setup. Auto-focus feature disabled.");
            e.printStackTrace();
            reflectionFailed = true;
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (reflectionFailed || !(event.gui instanceof GuiRepair)) return;

        GuiRepair gui = (GuiRepair) event.gui;

        if (!gui.inventorySlots.getSlot(0).getHasStack()) return;

        try {
            GuiTextField textField = (GuiTextField) nameFieldReflection.get(gui);

            if (textField != null && !textField.isFocused()) {
                textField.setFocused(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiRepair)) return;
        if (!Keyboard.getEventKeyState()) return;

        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            GuiRepair gui = (GuiRepair) mc.currentScreen;
            Container container = gui.inventorySlots;

            Slot outputSlot = container.getSlot(2);
            if (outputSlot != null && outputSlot.getHasStack()) {
                mc.playerController.windowClick(container.windowId, 2, 0, 0, mc.thePlayer);
            }
        }
    }
}
