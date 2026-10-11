package io.github.lplusbonk.visordiegetic.client;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import io.github.lplusbonk.visordiegetic.VisorDiegetic;

public class DiegeticAddonClient implements VisorAddon {
    @Override
    public void onAddonLoad() {
        VisorDiegetic.LOGGER.info("{} client addon loaded", VisorDiegetic.MOD_NAME);
    }

    @Override
    public @Nullable String getAddonPackagePath() {
        return "io.github.lplusbonk.visordiegetic.client";
    }

    @Override
    public @NotNull String getAddonId() {
        return VisorDiegetic.MOD_ID;
    }

    @Override
    public @NotNull Component getAddonName() {
        return Component.literal(VisorDiegetic.MOD_NAME);
    }

    @Override
    public String getModId() {
        return VisorDiegetic.MOD_ID;
    }
}
