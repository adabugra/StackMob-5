package uk.antiperson.stackmob.packets;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.hook.hooks.PacketEventsHook;

public class PacketEventsFakeArmorStand implements FakeArmorStand {

    private final StackMob sm;
    private PacketEventsHook plh;
    private int entityId;
    private final Player player;
    public PacketEventsFakeArmorStand(StackMob sm, Player player) {
        this.sm = sm;
        this.player = player;
    }

    @Override
    public void spawnFakeArmorStand(Entity owner, Location location, Component name, double offset) {
        if (plh == null) {
            plh = sm.getHookManager().getPacketEventsHook();
        }
        entityId = plh.spawnFakeArmorStand(player, owner, adjustLocation(owner, offset), name);
    }

    @Override
    public void updateName(Component newName) {
        if (plh == null) {
            plh = sm.getHookManager().getPacketEventsHook();
        }
        plh.updateTag(player, entityId, newName);
    }

    @Override
    public void removeFakeArmorStand() {
        if (plh == null) {
            plh = sm.getHookManager().getPacketEventsHook();
        }
        plh.removeFakeArmorStand(player, entityId);
    }
}
