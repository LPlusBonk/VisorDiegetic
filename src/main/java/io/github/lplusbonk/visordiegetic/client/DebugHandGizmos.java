package io.github.lplusbonk.visordiegetic.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.player.VRPose;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;

@RegisterVisorTask
public class DebugHandGizmos extends VisorTask {

    public DebugHandGizmos(@NotNull VisorAddon owner) {
        super(owner);
        // TODO Auto-generated constructor stub
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull String getId() {
        return null;
    }

    @Override
    public boolean isActive(@Nullable LocalPlayer arg0) {
        return true;
    }

    @Override
    protected void onClear(@Nullable LocalPlayer arg0) {
    }

    @Override
    protected void onRun(@Nullable LocalPlayer arg0) {
        VRPlayerPoseClient pose = VisorAPI.client().getVRLocalPlayer().getPoseData();
        VRPose handMain = pose.getGripHand(HandType.MAIN);
        VRPose handOff = pose.getGripHand(HandType.MAIN);
        Vec3 posMain = handMain.getPositionVec3();
        Vec3 posOff = handOff.getPositionVec3();
        Gizmos.point(posMain, 0xFFFF0000, 8f).setAlwaysOnTop();
        Gizmos.point(posOff, 0xFFFF0000, 8f).setAlwaysOnTop();
    }

}
