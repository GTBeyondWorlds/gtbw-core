package com.gtbeyondworlds.gtbwcore.api.multiblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.gtbeyondworlds.gtbwcore.content.machine.blastfurnace.BrickedBlastFurnaceLayout;
import com.gtbeyondworlds.gtbwcore.content.machine.cokeoven.CokeOvenLayout;

class MultiblockLayoutTest {

    @Test
    void cokeOvenLayoutHas25BricksAndOneAirCell() {
        Map<RelativePos, Character> cells = CokeOvenLayout.LAYOUT.cells();
        assertEquals(26, cells.size()); // 27 cells minus the controller
        assertEquals(25, cells.values().stream().filter(c -> c == 'B').count());
        assertEquals(1, cells.values().stream().filter(c -> c == ' ').count());
        // The hollow center sits directly behind the controller.
        assertEquals(' ', cells.get(new RelativePos(0, 0, 1)));
    }

    @Test
    void blastFurnaceLayoutHas40BricksAndHollowColumn() {
        Map<RelativePos, Character> cells = BrickedBlastFurnaceLayout.LAYOUT.cells();
        assertEquals(44, cells.size()); // 45 cells minus the controller
        assertEquals(40, cells.values().stream().filter(c -> c == 'B').count());
        assertEquals(4, cells.values().stream().filter(c -> c == ' ').count());
        assertEquals(' ', cells.get(new RelativePos(0, 0, 1)));
        assertEquals(' ', cells.get(new RelativePos(0, 1, 1)));
        assertEquals(' ', cells.get(new RelativePos(0, 2, 1)));
        assertEquals(' ', cells.get(new RelativePos(0, 3, 1)));
        // Base directly below the hollow column is solid.
        assertEquals('B', cells.get(new RelativePos(0, -1, 1)));
    }

    @Test
    void layoutsStayWithinControllerScanRadius() {
        // MultiblockControllerBlock.PART_SCAN_RADIUS is 3: parts further from
        // the controller than that would never notify it, so structures would
        // stop forming/un-forming promptly. Bump the radius if this fails.
        for (MultiblockLayout layout : java.util.List.of(CokeOvenLayout.LAYOUT, BrickedBlastFurnaceLayout.LAYOUT)) {
            for (RelativePos pos : layout.cells().keySet()) {
                int distance = Math.max(Math.abs(pos.right()), Math.max(Math.abs(pos.up()), Math.abs(pos.back())));
                assertTrue(distance <= 3, "cell " + pos + " is outside the controller scan radius");
            }
        }
    }

    @Test
    void firstCharacterOfARowIsTheViewersLeft() {
        // Standing in front of the machine, "ACB" reads A on the viewer's left,
        // which is the structure's +right side.
        MultiblockLayout layout = MultiblockLayout.builder('C').layer("ACB").build();
        assertEquals('A', layout.cells().get(new RelativePos(1, 0, 0)));
        assertEquals('B', layout.cells().get(new RelativePos(-1, 0, 0)));
    }

    @Test
    void rowsRunFrontToBackAndLayersBottomUp() {
        MultiblockLayout layout = MultiblockLayout.builder('C')
                .layer("C", "F")
                .layer("U", "K")
                .build();
        assertEquals('F', layout.cells().get(new RelativePos(0, 0, 1)));
        assertEquals('U', layout.cells().get(new RelativePos(0, 1, 0)));
        assertEquals('K', layout.cells().get(new RelativePos(0, 1, 1)));
    }

    @Test
    void missingControllerIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> MultiblockLayout.builder('C').layer("BBB").build());
    }

    @Test
    void duplicateControllerIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> MultiblockLayout.builder('C').layer("CBC").build());
    }

    @Test
    void raggedRowsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> MultiblockLayout.builder('C').layer("CB", "B").build());
    }

    @Test
    void mismatchedLayerShapesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> MultiblockLayout.builder('C').layer("CB").layer("B", "B").build());
    }

    @Test
    void emptyLayoutIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> MultiblockLayout.builder('C').build());
    }
}
