package id.hanif.legendary.item;

import id.hanif.legendary.HLegendaryWeapons;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item INFERNO_BLADE = Registry.register(
        Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "inferno_blade"),
        new SwordItem(ToolMaterials.DIAMOND, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 8, -2.2f))
            .maxCount(1).fireproof())
    );
    public static final Item FROSTBITE = Registry.register(
        Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "frostbite"),
        new SwordItem(ToolMaterials.DIAMOND, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 9, -2.4f))
            .maxCount(1))
    );
    public static final Item THUNDER_HAMMER = Registry.register(
        Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "thunder_hammer"),
        new SwordItem(ToolMaterials.DIAMOND, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 10, -3.0f))
            .maxCount(1).fireproof())
    );
    public static final Item VOID_SCYTHE = Registry.register(
        Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "void_scythe"),
        new SwordItem(ToolMaterials.DIAMOND, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 11, -2.8f))
            .maxCount(1).fireproof())
    );
    public static final Item DRAGON_SLAYER = Registry.register(
        Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "dragon_slayer"),
        new SwordItem(ToolMaterials.NETHERITE, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 12, -2.6f))
            .maxCount(1).fireproof())
    );

    public static final Item WIND_KATANA = Registry.register(
        Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "wind_katana"),
        new SwordItem(ToolMaterials.DIAMOND, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 10, -2.4f))
            .maxCount(1).fireproof())
    );

    public static final Item TITAN_AXE = Registry.register(
        Registries.ITEM, Identifier.of(HLegendaryWeapons.MOD_ID, "titan_axe"),
        new net.minecraft.item.AxeItem(ToolMaterials.DIAMOND, 8.0F, -3.1F, new Item.Settings().maxCount(1))
    );
    private ModItems() {}

    public static void register() {
        HLegendaryWeapons.LOGGER.info("Registered seven H Legendary Weapons, including Titan Axe");
    }
}
