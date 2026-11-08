package id.hanif.legendary.client;

import id.hanif.legendary.item.ModItems;
import id.hanif.legendary.network.SkillPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HLegendaryWeaponsClient implements ClientModInitializer {
    // The seven configurable keys remain available to the other weapons.
    private static final KeyBinding[] KEYS = new KeyBinding[7];
    private static final int[] DEFAULT_KEYS = {
        GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C,
        GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_N, GLFW.GLFW_KEY_M
    };

    private static boolean wasShiftDown = false;
    private static long lastShiftTap = 0L;
    private static final long DOUBLE_SHIFT_WINDOW_MS = 350L;

    @Override
    public void onInitializeClient() {
        for (int i = 0; i < KEYS.length; i++) {
            KEYS[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hlegendary.skill_" + (i + 1), InputUtil.Type.KEYSYM,
                DEFAULT_KEYS[i], "key.categories.hlegendary.skills"
            ));
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            boolean doombringer = client.player.getMainHandStack().isOf(ModItems.DOOMBRINGER_AXE);
            if (doombringer) {
                long window = client.getWindow().getHandle();
                boolean shiftDown = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                    || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
                long now = System.currentTimeMillis();

                // Double-tap either Shift key to activate skill 3.
                if (shiftDown && !wasShiftDown) {
                    if (now - lastShiftTap <= DOUBLE_SHIFT_WINDOW_MS) {
                        ClientPlayNetworking.send(new SkillPayload(2));
                        lastShiftTap = 0L;
                    } else {
                        lastShiftTap = now;
                    }
                }
                wasShiftDown = shiftDown;

                // Shift + right click = skill 1; ordinary right click = skill 4.
                while (client.options.useKey.wasPressed()) {
                    ClientPlayNetworking.send(new SkillPayload(shiftDown ? 0 : 3));
                }

                // Shift + left click = skill 2. Ordinary left click remains the basic attack.
                while (client.options.attackKey.wasPressed()) {
                    if (shiftDown) ClientPlayNetworking.send(new SkillPayload(1));
                }
                return;
            }

            wasShiftDown = false;
            lastShiftTap = 0L;

            boolean legendaryWeapon = client.player.getMainHandStack().isOf(ModItems.INFERNO_BLADE)
                || client.player.getMainHandStack().isOf(ModItems.FROSTBITE)
                || client.player.getMainHandStack().isOf(ModItems.THUNDER_HAMMER)
                || client.player.getMainHandStack().isOf(ModItems.VOID_SCYTHE)
                || client.player.getMainHandStack().isOf(ModItems.DRAGON_SLAYER)
                || client.player.getMainHandStack().isOf(ModItems.WIND_KATANA)
                || client.player.getMainHandStack().isOf(ModItems.TITAN_AXE)
                || client.player.getMainHandStack().isOf(ModItems.CELESTIAL_AXE)
                || client.player.getMainHandStack().isOf(ModItems.SOLARIA)
                || client.player.getMainHandStack().isOf(ModItems.CHRONOS)
                || client.player.getMainHandStack().isOf(ModItems.ZAHR_HANI);
            if (!legendaryWeapon) return;

            for (int i = 0; i < KEYS.length; i++) {
                while (KEYS[i].wasPressed()) ClientPlayNetworking.send(new SkillPayload(i));
            }
        });
    }
}
