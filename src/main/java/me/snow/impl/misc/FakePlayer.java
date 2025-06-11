package me.snow.impl.misc;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import me.snow.impl.Module;
import me.snow.impl.Category;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;

import java.util.UUID;

public class FakePlayer extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private static OtherClientPlayerEntity fakePlayer = null;

    public FakePlayer() {
        super("FakePlayer", "summons fake player", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.world != null && mc.player != null && fakePlayer == null) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "Target");
            fakePlayer = new OtherClientPlayerEntity(mc.world, profile) {


                @Override
                public boolean canHit() { return true; }

                @Override
                public boolean canTakeDamage() { return true; }



                @Override
                public boolean isPushable() { return true; }

                @Override
                public boolean canBeHitByProjectile() { return true; }

                @Override
                public boolean isAttackable() { return true; }


            };

            fakePlayer.setHealth(20f);
            fakePlayer.setPosition(mc.player.getX() + 2, mc.player.getY(), mc.player.getZ());
            fakePlayer.setInvulnerable(false);
            fakePlayer.setInvisible(false);
            fakePlayer.calculateDimensions();
            fakePlayer.setBoundingBox(fakePlayer.getType().getDimensions().getBoxAt(fakePlayer.getPos()));
            mc.world.addEntity(fakePlayer);
        }
    }

    @Override
    public void onDisable() {
        if (fakePlayer != null && mc.world != null) {
            mc.world.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
            fakePlayer = null;
        }
    }

    @Override
    public void onTick() {
        // Keep the fake player synchronized if needed
        if (fakePlayer != null && mc.world != null && this.isEnabled()) {
            // Add any continuous updates for the fake player here if needed
        }
    }
}