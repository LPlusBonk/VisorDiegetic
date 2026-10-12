package io.github.lplusbonk.visordiegetic.client;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;

public class GripListener implements VREventListener {

    @VREventHandler
    public void onActionButton(ActionButtonVREvent event) {
        String id = event.getActionButton().getId();
        HandType hand;
        if (id.equals("hotbar_main")) {
            hand = HandType.MAIN;
        } else if (id.equals("hotbar_offhand")) {
            hand = HandType.OFFHAND;
        } else {
            return;
        }

        if (event.isPressEvent()) {
            VisorAPI.client().getInputManager().triggerHapticPulse(hand, 0.1f);
        }

        event.setCanceled(true);
    }
}
