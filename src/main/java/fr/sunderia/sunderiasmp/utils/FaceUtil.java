package fr.sunderia.sunderiasmp.utils;

import org.bukkit.block.BlockFace;

import java.util.EnumMap;

/**
 * @author <a href="https://github.com/bergerkiller/BKCommonLib/blob/3b44f8bece00450d4f647ab3b9ba2439b73bef2c/src/main/java/com/bergerkiller/bukkit/common/utils/FaceUtil.java#L34">bergerkiller/BKCommonLib</a>
 */
public final class FaceUtil {

    private FaceUtil() {}

    private static final BlockFace[] AXIS = new BlockFace[4];
    private static final BlockFace[] RADIAL = {BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST};
    private static final EnumMap<BlockFace, Integer> notches = new EnumMap<>(BlockFace.class);

    static {
        for (int i = 0; i < RADIAL.length; i++) {
            notches.put(RADIAL[i], i);
        }
        for (int i = 0; i < AXIS.length; i++) {
            AXIS[i] = RADIAL[i << 1];
        }
    }

    /**
     * Gets the Notch integer representation of a BlockFace<br>
     * <b>These are the horizontal faces, which exclude up and down</b>
     *
     * @param face to get
     * @return Notch of the face
     */
    public static int faceToNotch(BlockFace face) {
        Integer notch = notches.get(face);
        return notch == null ? 0 : notch;
    }


    /**
     * Gets the angle from a horizontal Block Face
     *
     * @param face to get the angle for
     * @return face angle
     */
    public static int faceToYaw(final BlockFace face) {
        return wrapAngle(45 * faceToNotch(face));
    }

    public static int wrapAngle(int angle) {
        int wrappedAngle = angle;
        while (wrappedAngle <= -180) {
            wrappedAngle += 360;
        }
        while (wrappedAngle > 180) {
            wrappedAngle -= 360;
        }
        return wrappedAngle;
    }

    public static float wrapAngle(float angle) {
        float wrappedAngle = angle;
        while (wrappedAngle <= -180) {
            wrappedAngle += 360;
        }
        while (wrappedAngle > 180) {
            wrappedAngle -= 360;
        }
        return wrappedAngle;
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle<br>
     * This includes the NORTH_WEST faces
     *
     * @param yaw angle
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw) {
        return yawToFace(yaw, true);
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle
     *
     * @param yaw angle
     * @param useSubCardinalDirections setting, True to allow NORTH_WEST to be returned
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return RADIAL[Math.round(yaw / 45f) & 0x7];
        } else {
            return AXIS[Math.round(yaw / 90f) & 0x3];
        }
    }
}
