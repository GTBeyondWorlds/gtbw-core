package com.gtbeyondworlds.gtbwcore.api.multiblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The shape of a multiblock structure, parsed from layer strings and anchored
 * at its controller block.
 *
 * <p>Layout convention (see {@code MultiblockLayoutTest}, which pins it):
 * layers are added bottom-up; within a layer, row strings run front-to-back
 * (row 0 contains the machine's front face); within a row, characters read
 * left-to-right as seen by a player standing in front of the machine. The
 * controller character must appear exactly once and becomes the origin.
 * A space means "must be air" — hollow interiors are enforced, not ignored.
 *
 * <p>This class is pure geometry. Binding characters to actual blocks happens
 * in {@code MultiblockPattern}.
 */
public final class MultiblockLayout {
    private final char controllerKey;
    private final Map<RelativePos, Character> cells;

    private MultiblockLayout(char controllerKey, Map<RelativePos, Character> cells) {
        this.controllerKey = controllerKey;
        this.cells = Map.copyOf(cells);
    }

    public static Builder builder(char controllerKey) {
        return new Builder(controllerKey);
    }

    /** The character that marked the controller position. */
    public char controllerKey() {
        return this.controllerKey;
    }

    /**
     * Every cell of the structure except the controller itself, keyed by
     * position relative to the controller. Air cells carry the space character.
     */
    public Map<RelativePos, Character> cells() {
        return this.cells;
    }

    /** The distinct non-air, non-controller characters used by this layout. */
    public Set<Character> blockKeys() {
        Set<Character> keys = new HashSet<>();
        for (char key : this.cells.values()) {
            if (key != ' ') {
                keys.add(key);
            }
        }
        return keys;
    }

    public static final class Builder {
        private final char controllerKey;
        private final List<String[]> layers = new ArrayList<>();

        private Builder(char controllerKey) {
            this.controllerKey = controllerKey;
        }

        /** Adds the next layer up; rows run front-to-back. */
        public Builder layer(String... rowsFrontToBack) {
            this.layers.add(rowsFrontToBack.clone());
            return this;
        }

        public MultiblockLayout build() {
            if (this.layers.isEmpty() || this.layers.get(0).length == 0) {
                throw new IllegalArgumentException("Multiblock layout needs at least one layer with one row");
            }
            int depth = this.layers.get(0).length;
            int width = this.layers.get(0)[0].length();
            if (width == 0) {
                throw new IllegalArgumentException("Multiblock layout rows must not be empty");
            }

            // First pass: validate the box shape and locate the controller.
            int controllerLayer = -1;
            int controllerRow = -1;
            int controllerIndex = -1;
            for (int layer = 0; layer < this.layers.size(); layer++) {
                String[] rows = this.layers.get(layer);
                if (rows.length != depth) {
                    throw new IllegalArgumentException(
                            "Layer " + layer + " has " + rows.length + " rows, expected " + depth);
                }
                for (int row = 0; row < rows.length; row++) {
                    String cells = rows[row];
                    if (cells.length() != width) {
                        throw new IllegalArgumentException(
                                "Layer " + layer + " row " + row + " has width " + cells.length()
                                        + ", expected " + width);
                    }
                    for (int i = 0; i < cells.length(); i++) {
                        if (cells.charAt(i) == this.controllerKey) {
                            if (controllerLayer != -1) {
                                throw new IllegalArgumentException(
                                        "Controller character '" + this.controllerKey + "' appears more than once");
                            }
                            controllerLayer = layer;
                            controllerRow = row;
                            controllerIndex = i;
                        }
                    }
                }
            }
            if (controllerLayer == -1) {
                throw new IllegalArgumentException(
                        "Controller character '" + this.controllerKey + "' is missing from the layout");
            }

            // Second pass: emit cells relative to the controller. String index 0 is
            // the viewer's left, which is the structure's +right side.
            Map<RelativePos, Character> cells = new HashMap<>();
            for (int layer = 0; layer < this.layers.size(); layer++) {
                String[] rows = this.layers.get(layer);
                for (int row = 0; row < rows.length; row++) {
                    for (int i = 0; i < width; i++) {
                        if (layer == controllerLayer && row == controllerRow && i == controllerIndex) {
                            continue;
                        }
                        RelativePos pos = new RelativePos(
                                controllerIndex - i, layer - controllerLayer, row - controllerRow);
                        cells.put(pos, rows[row].charAt(i));
                    }
                }
            }
            return new MultiblockLayout(this.controllerKey, cells);
        }
    }
}
