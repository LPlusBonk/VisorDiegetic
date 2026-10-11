package io.github.lplusbonk.visordiegetic;

import net.fabricmc.api.ModInitializer;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import io.github.lplusbonk.visordiegetic.client.DiegeticAddonClient;
import io.github.lplusbonk.visordiegetic.server.DiegeticAddonServer;

public class VisorDiegeticMod implements ModInitializer {
    @Override
    public void onInitialize() {
        VisorAddon addon = ModLoader.get().isDedicatedServer()
                ? new DiegeticAddonServer()
                : new DiegeticAddonClient();
        VisorAPI.registerAddon(addon);
    }
}
