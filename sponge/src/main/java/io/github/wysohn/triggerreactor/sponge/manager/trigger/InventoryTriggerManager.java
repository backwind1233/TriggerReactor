/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import copy.com.google.gson.JsonDeserializationContext;
import copy.com.google.gson.JsonElement;
import copy.com.google.gson.JsonParseException;
import copy.com.google.gson.JsonSerializationContext;
import copy.com.google.gson.reflect.TypeToken;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.config.serialize.Serializer;
import io.github.wysohn.triggerreactor.core.manager.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeInventory;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeItemStack;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.tools.TextUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryTriggerManager extends AbstractInventoryTriggerManager<ItemStack> {
    public InventoryTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "InventoryTrigger"), ItemStack.class, SpongeItemStack::new);
    }

//    @Override
//    public <T> T getData(File file, String key, T def) throws IOException {
//        if (key.equals(ITEMS)) {
//            int size = getData(file, SIZE, 0);
//            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(file.toPath()).build();
//            ConfigurationNode conf = loader.load();
//
//            Map<Integer, IItemStack> items = new HashMap<>();
//
//            ConfigurationNode node = ConfigurationUtil.getNodeByKeyString(conf, ITEMS);
//            if (node != null)
//                parseItemsList(node, items, size);
//
//            return (T) items;
//        } else {
//            return SpongeConfigurationFileIO.super.getData(file, key, def);
//        }
//    }
//
//    @Override
//    public void setData(File file, String key, Object value) throws IOException {
//        if (key.equals(ITEMS)) {
//            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(file.toPath()).build();
//            ConfigurationNode conf = loader.load();
//
//            IItemStack[] items = (IItemStack[]) value;
//
//            writeItemsList(ConfigurationUtil.getNodeByKeyString(conf, ITEMS), items);
//
//            loader.save(conf);
//        } else {
//            SpongeConfigurationFileIO.super.setData(file, key, value);
//        }
//    }
//
//    private void parseItemsList(ConfigurationNode itemSection, Map<Integer, IItemStack> items, int size) {
//        for (int i = 0; i < size; i++) {
//            ConfigurationNode section = ConfigurationUtil.getNodeByKeyString(itemSection, String.valueOf(i));
//            if (section.isVirtual())
//                continue;
//
//            ItemStackSnapshot IS;
//            try {
//                IS = section.getValue(TypeTokens.ITEM_SNAPSHOT_TOKEN);
//            } catch (ObjectMappingException e) {
//                e.printStackTrace();//temp
//                continue;
//            }
//
//            items.put(i, new SpongeItemStack(IS.createStack()));
//        }
//    }
//
//    private void writeItemsList(ConfigurationNode itemSection, IItemStack[] items) {
//        for (int i = 0; i < items.length; i++) {
//            if (items[i] == null)
//                continue;
//
//            ItemStack item = items[i].get();
//
//            ConfigurationNode section = ConfigurationUtil.getNodeByKeyString(itemSection, String.valueOf(i));
//            try {
//                section.setValue(TypeTokens.ITEM_SNAPSHOT_TOKEN, item.createSnapshot());
//            } catch (ObjectMappingException e) {
//                e.printStackTrace();//temp
//                continue;
//            }
//        }
//    }

    /**
     * @param player
     * @param name
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     */
    public IInventory openGUI(Player player, String name) {
        Sponge.getCauseStackManager().pushCause(player);
        return openGUI(new SpongePlayer(player), name);
    }

    @Listener
    public void onOpen(InteractInventoryEvent.Open e) {
        Inventory inv = e.getTargetInventory();
        if (!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if (carrier == null)
            return;

        if (!this.hasInventoryOpen(new SpongeInventory(inventory, carrier)))
            return;

        InventoryTrigger trigger = getTriggerForOpenInventory(new SpongeInventory(inventory, carrier));

        Map<String, Object> varMap = getSharedVarsForInventory(new SpongeInventory(inventory, carrier));
        varMap.put("player", e.getCause().first(Player.class).orElse(null));
        varMap.put("trigger", "open");

        Inventory grids = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));
        varMap.put("inventory", grids.first());

        trigger.activate(e, varMap);
    }

    @Listener
    public void onClick(ClickInventoryEvent e) {
        Inventory inv = e.getTargetInventory();
        if (!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if (carrier == null)
            return;

        if (!this.hasInventoryOpen(new SpongeInventory(inventory, carrier)))
            return;
        InventoryTrigger trigger = getTriggerForOpenInventory(new SpongeInventory(inventory, carrier));

        // just always cancel if it's GUI
        e.setCancelled(true);

        Player player = e.getCause().first(Player.class).orElse(null);
        if (player == null)
            return;

        int rawSlot = -1;
        SlotTransaction slotTransaction = null;

        List<SlotTransaction> transactions = e.getTransactions();
        if (!transactions.isEmpty()) {
            slotTransaction = e.getTransactions().get(0);
            Slot slot = slotTransaction.getSlot();
            SlotIndex slotIndex = slot.getInventoryProperty(SlotIndex.class).orElse(null);
            rawSlot = slotIndex.getValue();
        }

        Map<String, Object> varMap = getSharedVarsForInventory(new SpongeInventory(inventory, carrier));
        ItemStackSnapshot clickedItemOpt = slotTransaction == null ? ItemStackSnapshot.NONE : slotTransaction.getOriginal();
        varMap.put("item", clickedItemOpt.createStack());
        varMap.put("slot", rawSlot);
        varMap.put("click", e.getClass().getSimpleName());
        varMap.put("trigger", "click");

        Inventory grids = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));
        varMap.put("inventory", grids.first());

        trigger.activate(e, varMap);
    }

    @Listener
    public void onClose(InteractInventoryEvent.Close e, @First Player player) {
        if (player == null)
            return;

        Inventory inv = e.getTargetInventory();
        if (!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if (carrier == null)
            return;

        onInventoryClose(e, new SpongePlayer(player), new SpongeInventory(inv, carrier));
    }

    @Override
    protected void fillInventory(InventoryTrigger trigger, int size, IInventory inventory) {
        Inventory inv = inventory.get();
        GridInventory gridInv = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));

        for (int i = 0; i < size; i++) {
            IItemStack item = trigger.getItems()[i];
            if (item != null) {
                Slot slot = gridInv.getSlot(SlotIndex.of(i)).orElse(null);
                slot.set(getColoredItem(item.get()));
            }
        }
    }

    /**
     * @param item
     * @return copy of colored item
     */
    private ItemStack getColoredItem(ItemStack item) {
        item = item.copy();

        Text displayName = item.get(Keys.DISPLAY_NAME).orElse(null);
        if (displayName != null)
            item.offer(Keys.DISPLAY_NAME, TextUtil.colorStringToText(displayName.toPlain()));

        List<Text> lores = item.get(Keys.ITEM_LORE).orElse(null);
        if (lores != null) {
            for (int i = 0; i < lores.size(); i++) {
                lores.set(i, TextUtil.colorStringToText(lores.get(i).toPlain()));
            }
            item.offer(Keys.ITEM_LORE, lores);
        }

        return item;
    }

    @Override
    protected IInventory createInventory(int size, String name) {
        name = name.replaceAll("_", " ");
        Text text = TextUtil.colorStringToText(name);
        Carrier dummy = new DummyCarrier();
        Inventory inv = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .withCarrier(dummy)
                .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, size / 9))
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(text))
                .build(plugin);
        return new SpongeInventory(inv, dummy);
    }

    private class DummyCarrier implements Carrier {
        private final Object uniqueObject = new Object();

        @Override
        public CarriedInventory<? extends Carrier> getInventory() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int hashCode() {
            return uniqueObject.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DummyCarrier))
                return false;

            return uniqueObject.equals(((DummyCarrier) obj).uniqueObject);
        }
    }

    static {
        GsonConfigSource.registerTypeAdapter(ItemStack.class, new Serializer<ItemStack>() {
            @Override
            public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Map<String, Object> ser = context.deserialize(json, new TypeToken<Map<String, Object>>() {
                }.getType());
                DataContainer container = DataContainer.createNew();
                ser.forEach((s, o) -> container.set(DataQuery.of(".", s), o));
                return Sponge.getDataManager().deserialize(ItemStack.class, container)
                        .orElseThrow(() -> new RuntimeException("Cannot deserialized [" + ser + "] to ItemStack."));
            }

            @Override
            public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
                DataContainer container = src.toContainer();
                Map<String, Object> map = new HashMap<>();
                container.getValues(true).forEach((dataQuery, o) -> map.put(dataQuery.toString(), o));
                return context.serialize(map);
            }
        });
    }
}
