package io.github.lplusbonk.visordiegetic.server;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import io.github.lplusbonk.visordiegetic.VisorDiegetic;

public class DiegeticAddonServer implements VisorAddon {
    @Override
    public void onAddonLoad() {
        VisorDiegetic.LOGGER.info("{} server addon loaded", VisorDiegetic.MOD_NAME);
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
