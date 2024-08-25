package uk.antiperson.stackmob.packets;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.entity.StackEntity;
import uk.antiperson.stackmob.hook.hooks.PacketEventsHook;

public class TagHandler {

    private boolean tagVisible;
    private final StackEntity stackEntity;
    private final StackMob sm;
    private final Player player;
    private FakeArmorStand fakeArmorStand;
    private Component lastTag;

    public TagHandler(StackMob sm, Player player, StackEntity stackEntity) {
        this.sm = sm;
        this.stackEntity = stackEntity;
        this.player = player;
    }

    public void init() {
        // force packetevents for 1.20.2
        this.fakeArmorStand = new PacketEventsFakeArmorStand(sm, player);
    }

    public void newlyInRange() {
        tagVisible = true;
        if (stackEntity.getEntityConfig().isUseArmorStand()) {
            fakeArmorStand.spawnFakeArmorStand(stackEntity.getEntity(), stackEntity.getEntity().getLocation(), stackEntity.getTag().getDisplayName(), stackEntity.getEntityConfig().getArmorstandOffset());
            return;
        }
        sendPacket(stackEntity.getEntity(), player, true);
    }

    public void playerInRange() {
        if (stackEntity.getEntityConfig().isTagNearbyRayTrace() && !stackEntity.rayTracePlayer(player)) {
            if (tagVisible) {
                playerOutRange();
            }
            return;
        }
        if (!tagVisible) {
            newlyInRange();
        }
    }

    public void updateTag() {
        if (!stackEntity.getEntityConfig().isUseArmorStand()) {
            return;
        }
        if (stackEntity.getTag().getDisplayName().equals(lastTag)) {
            return;
        }
        fakeArmorStand.updateName(stackEntity.getTag().getDisplayName());
        lastTag = stackEntity.getTag().getDisplayName();
    }

    public void playerOutRange() {
        sendPacket(stackEntity.getEntity(), player, false);
        tagVisible = false;
        if (stackEntity.getEntityConfig().isUseArmorStand()) {
            fakeArmorStand.removeFakeArmorStand();
        }
    }

    private void sendPacket(Entity entity, Player player, boolean tagVisible) {
        PacketEventsHook packetEventsHook = sm.getHookManager().getPacketEventsHook();
        if (packetEventsHook == null) {
            return;
        }
        packetEventsHook.sendPacket(player, entity, tagVisible);
    }

    public boolean isTagVisible() {
        return tagVisible;
    }
}
