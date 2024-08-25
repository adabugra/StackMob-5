package uk.antiperson.stackmob.hook.hooks;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.hook.Hook;
import uk.antiperson.stackmob.hook.HookMetadata;
import uk.antiperson.stackmob.utils.Utilities;

import java.util.*;

@HookMetadata(name = "Packetevents", config = "packetevents")
public class PacketEventsHook extends Hook {

    private PlayerManager playerManager;
    private int entityIdCounter;

    public PacketEventsHook(StackMob sm) {
        super(sm);
        this.entityIdCounter = Integer.MAX_VALUE / 2;
    }

    @Override
    public void onEnable() {
        playerManager = PacketEvents.getAPI().getPlayerManager();
    }

    /* https://github.com/Ste3et/FurnitureLib/commit/2f7c9adbe90716811ecc620c021bed0c727b10f0#diff-0f3b41bd8ab636343d5689cbcc1e2d008aa3b65454e5af09cba8059a4ac51bed */
//    private void writeWatchableObjects(WrappedDataWatcher watcher, PacketContainer packetContainer) {
//        if (Utilities.isVersionAtLeast(Utilities.MinecraftVersion.V1_19_4)) {
//            List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
//            watcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
//                WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
//                wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
//            });
//            packetContainer.getDataValueCollectionModifier().write(0, wrappedDataValueList);
//            return;
//        }
//        packetContainer.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
//    }

    public void sendPacket(Player player, Entity entity, boolean visible) {
        int entityId = entity.getEntityId();
        EntityData entityData = new EntityData(3, EntityDataTypes.BOOLEAN, visible);
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, List.of(entityData));
        playerManager.sendPacket(player, metadataPacket);
    }

    public int spawnFakeArmorStand(Player player, Entity owner, Location location, Component name) {
        // spawn packet
        entityIdCounter = SpigotReflectionUtil.generateEntityId();
        UUID uuid = UUID.randomUUID();

        com.github.retrooper.packetevents.protocol.world.Location packetLocation = SpigotConversionUtil.fromBukkitLocation(location);
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                entityIdCounter,
                uuid,
                EntityTypes.ARMOR_STAND,
                packetLocation,
                0, // Head yaw
                0,
                null
        );

        List<EntityData> entityDataList = new ArrayList<>();

        entityDataList.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20));
        entityDataList.add(new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(name)));
        entityDataList.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
        entityDataList.add(new EntityData(5, EntityDataTypes.BOOLEAN, true));
        entityDataList.add(new EntityData(15, EntityDataTypes.BYTE, (byte) 0x10));

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityIdCounter, entityDataList);

        WrapperPlayServerSetPassengers setPassengersPacket = new WrapperPlayServerSetPassengers(owner.getEntityId(), new int[]{entityIdCounter});

        playerManager.sendPacket(player, packet);
        playerManager.sendPacket(player, metadataPacket);
        playerManager.sendPacket(player, setPassengersPacket);


        return entityIdCounter;
    }

    public void updateTag(Player player, int id, Component newName) {

        EntityData entityData = new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(newName));

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(id, List.of(entityData));
        playerManager.sendPacket(player, metadataPacket);
    }

    public void removeFakeArmorStand(Player player, int id) {
        WrapperPlayServerDestroyEntities destroyEntitiesPacket = new WrapperPlayServerDestroyEntities(id);

        playerManager.sendPacket(player, destroyEntitiesPacket);
    }
}
