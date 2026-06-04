package me.incend1um.noinputlagtickrate;

import me.incend1um.noinputlagtickrate.mixin.client.access.MinecraftAccess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

public class NoInputLagTickRateClient implements ClientModInitializer {
    public static DeltaTracker.Timer inputDeltaTracker = new DeltaTracker.Timer(20.0f, 0, NoInputLagTickRateClient::tickTargetMspt);
    
    private static long lastInputTime = 0;

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.START_MAIN.register((worldRenderContext) -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.screen != null) {
                mc.missTime = 1000;
            }

            if (mc.getOverlay() == null && mc.screen == null) {
                MinecraftAccess access = (MinecraftAccess) mc;
                long currentTime = Util.getMillis();

                int ticksDue = inputDeltaTracker.advanceTime(currentTime, true);
                
                if (ticksDue > 0) {
                    if (currentTime != lastInputTime) {
                        access.invokeHandleKeybinds();
                        lastInputTime = currentTime; 
                    }
                }
            }
        });
    }

    static float tickTargetMspt(float defaultValue) {
        return defaultValue;
    }
}
