// FastplaceAccessor.java
package me.snow.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface FastplaceAccessor {
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int cooldown);

    @Accessor("itemUseCooldown")
    int getItemUseCooldown();
}