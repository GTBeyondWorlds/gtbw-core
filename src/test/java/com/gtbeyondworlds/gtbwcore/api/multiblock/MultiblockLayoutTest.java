package com.gtbeyondworlds.gtbwcore.api.multiblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

class MultiblockLayoutTest {

    /** 3x3x3 hollow cube, controller centered on the front face, second layer. */
    private static MultiblockLayout cokeOvenLayout() {
        return MultiblockLayout.builder('C')
                .layer("BBB", "BBB", "BBB")
                .layer("BCB", "B B", "BBB")
                .layer("BBB", "BBB", "BBB")
                .build();
    }

    /** 3x3 solid base, 3 hollow ring layers above, controller front-center on layer 2. */
    private static MultiblockLayout blastFurnaceLayout() {
        return MultiblockLayout.builder('C')
                .layer("BBB", "BBB", "BBB")
                .layer("BCB", "B B", "BBB")
                .layer("BBB", "B B", "BBB")
                .layer("BBB", "B B", "BBB")
                .build();
    }

    @Test
    void cokeOvenLayoutHas25BricksAndOneAirCell() {
        Map<RelativePos, Character> cells = cokeOvenLayout().cells();
        assertEquals(26, cells.size()); // 27 cells minus the controller
        assertEquals(25, cells.values().stream().filter(c -> c == 'B').count());
        assertEquals(1, cells.values().stream().filter(c -> c == ' ').count());
        // The hollow center sits directly behind the controller.
        assertEquals(' ', cells.get(new RelativePos(0, 0, 1)));
    }

    @Test
    void blastFurnaceLayoutHas32BricksAndHollowColumn() {
        Map<RelativePos, Character> cells = blastFurnaceLayout().cells();
        assertEquals(35, cells.size()); // 36 cells minus the controller
        assertEquals(32, cells.values().stream().filter(c -> c == 'B').count());
        assertEquals(3, cells.values().stream().filter(c -> c == ' ').count());
        assertEquals(' ', cells.get(new RelativePos(0, 0, 1)));
        assertEquals(' ', cells.get(new RelativePos(0, 1, 1)));
        assertEquals(' ', cells.get(new RelativePos(0, 2, 1)));
        // Base directly below the hollow column is solid.
        assertEquals('B', cells.get(new RelativePos(0, -1, 1)));
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
