package id.hanif.legendary.item;

import id.hanif.legendary.HLegendaryWeapons;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item INFERNO_BLADE = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "inferno_blade"), new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 8, -2.2f)).maxCount(1).fireproof()));
    public static final Item FROSTBITE = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "frostbite"), new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 9, -2.4f)).maxCount(1)));
    public static final Item THUNDER_HAMMER = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "thunder_hammer"), new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 10, -3.0f)).maxCount(1).fireproof()));
    public static final Item VOID_SCYTHE = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "void_scythe"), new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 11, -2.8f)).maxCount(1).fireproof()));
    public static final Item DRAGON_SLAYER = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "dragon_slayer"), new SwordItem(ToolMaterials.NETHERITE, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 12, -2.6f)).maxCount(1).fireproof()));
    public static final Item WIND_KATANA = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "wind_katana"), new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 10, -2.4f)).maxCount(1).fireproof()));
    public static final Item TITAN_AXE = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "titan_axe"), new net.minecraft.item.AxeItem(ToolMaterials.DIAMOND, new Item.Settings().attributeModifiers(net.minecraft.item.AxeItem.createAttributeModifiers(ToolMaterials.DIAMOND, 8.0F, -3.1F)).maxCount(1)));
    public static final Item CELESTIAL_AXE = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "celestial_axe"), new net.minecraft.item.AxeItem(ToolMaterials.DIAMOND, new Item.Settings().attributeModifiers(net.minecraft.item.AxeItem.createAttributeModifiers(ToolMaterials.DIAMOND, 9.0F, -3.0F)).maxCount(1).fireproof()));
    public static final Item SOLARIA = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "solaria"), new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 11, -2.5f)).maxCount(1).fireproof()));
    public static final Item CHRONOS = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "chronos"), new SwordItem(ToolMaterials.NETHERITE, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 12, -2.7f)).maxCount(1).fireproof()));
    public static final Item ZAHR_HANI = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "zahr_hani"), new SwordItem(ToolMaterials.NETHERITE, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 14, -2.8f)).maxCount(1).fireproof()));
    public static final Item DOOMBRINGER_AXE = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "doombringer_axe"), new net.minecraft.item.AxeItem(ToolMaterials.NETHERITE, new Item.Settings().attributeModifiers(net.minecraft.item.AxeItem.createAttributeModifiers(ToolMaterials.NETHERITE, 13.0F, -3.2F)).maxCount(1).fireproof()));
    public static final Item GALAXY_SWORD = Registry.register(Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "galaxy_sword"), new SwordItem(ToolMaterials.NETHERITE, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 13, -2.4f)).maxCount(1).fireproof()));

    private ModItems() {}

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(INFERNO_BLADE); entries.add(FROSTBITE); entries.add(THUNDER_HAMMER); entries.add(VOID_SCYTHE);
            entries.add(DRAGON_SLAYER); entries.add(WIND_KATANA); entries.add(TITAN_AXE); entries.add(CELESTIAL_AXE);
            entries.add(SOLARIA); entries.add(CHRONOS); entries.add(ZAHR_HANI); entries.add(DOOMBRINGER_AXE); entries.add(GALAXY_SWORD);
        });
        HLegendaryWeapons.LOGGER.info("Registered thirteen H Legendary Weapons, including Galaxy Sword");
    }
}
