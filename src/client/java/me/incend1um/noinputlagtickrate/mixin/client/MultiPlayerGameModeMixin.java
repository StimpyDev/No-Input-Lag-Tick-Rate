package me.incend1um.noinputlagtickrate.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.incend1um.noinputlagtickrate.mixin.client.access.MinecraftAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private float destroyTicks;
    @Shadow private int destroyDelay;

    @Unique
    private long lastTick = -1;

    @Redirect(method = "continueDestroyBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyDelay:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    void syncDestroyingDelayWithActualTickRate(MultiPlayerGameMode instance, int value) {
        long currentTick = ((MinecraftAccess) this.minecraft).getClientTickCount();
        if (currentTick != lastTick) {
            this.destroyDelay = value;
        }
    }

    @ModifyExpressionValue(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    float syncDestroyingWithActualTickRate(float original) {
        if (this.minecraft.level != null) {
            return original * (50.0f / this.minecraft.level.tickRateManager().millisecondsPerTick());
        }
        return original;
    }

    @Redirect(method = "continueDestroyBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyTicks:F", opcode = Opcodes.PUTFIELD, ordinal = 0))
    void syncSoundWithActualTickRate(MultiPlayerGameMode instance, float value) {
        long currentTick = ((MinecraftAccess) this.minecraft).getClientTickCount();
        if (currentTick != lastTick) {
            this.lastTick = currentTick;
            this.destroyTicks = value;
        }
    }
}
