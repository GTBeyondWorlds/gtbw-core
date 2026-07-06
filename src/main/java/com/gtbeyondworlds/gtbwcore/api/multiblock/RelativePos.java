package com.gtbeyondworlds.gtbwcore.api.multiblock;

/**
 * A block position relative to a multiblock controller, expressed in the
 * controller's local frame:
 *
 * <ul>
 *   <li>{@code right} — along the structure's own right (a player standing in
 *       front of the machine sees +right on their left),</li>
 *   <li>{@code up} — world up,</li>
 *   <li>{@code back} — into the structure, away from the controller's visible
 *       face.</li>
 * </ul>
 *
 * <p>{@link PatternFacing} maps these into world-space offsets. This class is
 * pure — no Minecraft dependencies — so pattern geometry stays unit-testable.
 */
public record RelativePos(int right, int up, int back) {
}
