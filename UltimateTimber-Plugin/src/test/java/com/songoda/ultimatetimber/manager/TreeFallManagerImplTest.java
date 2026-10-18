package com.songoda.ultimatetimber.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TreeFallManagerImplTest {

    private final TreeFallManagerImpl manager = new TreeFallManagerImpl();

    @Test
    void unbreakableToolCannotBreakEvenWithExistingDamage() {
        assertFalse(this.manager.wouldToolBreak(tool(true, 9, 10), (short) 150));
    }

    @Test
    void unbreakableToolDoesNotReceiveDamage() throws ReflectiveOperationException {
        assertUnbreakableToolIsUntouched(tool(true, 0, 10), (short) 1);
    }

    @Test
    void unbreakableToolIsNotDestroyedAtDamageLimit() throws ReflectiveOperationException {
        assertUnbreakableToolIsUntouched(tool(true, 9, 10), (short) 150);
    }

    @Test
    void breakableToolStillReportsBreakageAtDamageLimit() {
        assertTrue(this.manager.wouldToolBreak(tool(false, 9, 10), (short) 1));
    }

    @Test
    void breakableToolBelowDamageLimitDoesNotBreak() {
        assertFalse(this.manager.wouldToolBreak(tool(false, 0, 10), (short) 1));
    }

    @Test
    void missingToolDoesNotBreak() {
        assertFalse(this.manager.wouldToolBreak(null, (short) 150));
    }

    @Test
    void airDoesNotBreak() {
        ItemStack tool = mock(ItemStack.class);
        Material material = mock(Material.class);
        when(tool.getType()).thenReturn(material);
        when(material.isAir()).thenReturn(true);
        assertFalse(this.manager.wouldToolBreak(tool, (short) 150));
    }

    private void assertUnbreakableToolIsUntouched(ItemStack tool, short damage) throws ReflectiveOperationException {
        Player player = mock(Player.class);
        Method applyToolDamage = TreeFallManagerImpl.class.getDeclaredMethod(
                "applyToolDamage", Player.class, ItemStack.class, short.class);
        applyToolDamage.setAccessible(true);
        applyToolDamage.invoke(this.manager, player, tool, damage);
        verifyNoInteractions(player);
        // Returning before even reading damage also avoids enchantment lookup and metadata writes.
        verify((Damageable) tool.getItemMeta(), never()).getDamage();
        verify((Damageable) tool.getItemMeta(), never()).hasMaxDamage();
    }

    private static ItemStack tool(boolean unbreakable, int damage, int maxDamage) {
        ItemStack tool = mock(ItemStack.class);
        // Material.isAir() uses Paper's server registry; mock it for a server-free unit test.
        Material material = mock(Material.class);
        Damageable meta = mock(Damageable.class);
        when(tool.getType()).thenReturn(material);
        when(tool.getItemMeta()).thenReturn(meta);
        when(meta.isUnbreakable()).thenReturn(unbreakable);
        when(meta.hasMaxDamage()).thenReturn(true);
        when(meta.getMaxDamage()).thenReturn(maxDamage);
        when(meta.getDamage()).thenReturn(damage);
        return tool;
    }
}
