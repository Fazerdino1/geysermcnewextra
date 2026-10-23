package fr.crossplaymc.geysermcnewextra.spigot.modules.armorstands;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

/**
 * The poses a Bedrock Edition player cycles through by sneaking and using an armor stand, in Bedrock's order.
 * Angles are in degrees (x, y, z) for head, body, left arm, right arm, left leg and right leg, set by eye
 * to look like Bedrock's poses.
 */
public enum ArmorStandPose {
    DEFAULT(a(0, 0, 0), a(0, 0, 0), a(-10, 0, -10), a(-15, 0, 10), a(-1, 0, -1), a(1, 0, 1)),
    NONE(a(0, 0, 0), a(0, 0, 0), a(0, 0, 0), a(0, 0, 0), a(0, 0, 0), a(0, 0, 0)),
    SOLEMN(a(15, 0, 0), a(0, 0, 2), a(-30, 15, 15), a(-60, -20, -10), a(-1, 0, -1), a(1, 0, 1)),
    ATHENA(a(-5, 0, 0), a(0, 0, 2), a(10, 0, -5), a(-60, 20, -10), a(-3, -3, -3), a(3, 3, 3)),
    BRANDISH(a(-15, 0, 0), a(0, 0, -2), a(20, 0, -10), a(-110, 50, 0), a(5, -3, -3), a(-5, 3, 3)),
    HONOR(a(-15, 0, 0), a(0, 0, 0), a(-110, 35, 0), a(-110, -35, 0), a(5, -3, -3), a(-5, 3, 3)),
    ENTERTAIN(a(-15, 0, 0), a(0, 0, 0), a(-110, -35, 0), a(-110, 35, 0), a(5, -3, -3), a(-5, 3, 3)),
    SALUTE(a(0, 0, 0), a(0, 0, 0), a(10, 0, -5), a(-70, -40, 0), a(-1, 0, -1), a(1, 0, 1)),
    RIPOSTE(a(16, 20, 0), a(0, 0, 0), a(4, 8, -123), a(-70, -10, 30), a(-10, 0, 0), a(10, 0, 0)),
    ZOMBIE(a(-10, 0, -5), a(0, 0, 0), a(-105, 0, 0), a(-100, 0, 0), a(7, 0, 0), a(-46, 0, 0)),
    CANCAN_A(a(-5, 18, 0), a(0, 22, 0), a(8, 0, -114), a(0, 84, 111), a(-111, 55, 0), a(0, 23, -13)),
    CANCAN_B(a(-10, -20, 0), a(0, -18, 0), a(0, 0, -112), a(8, 90, 111), a(0, 0, 13), a(-119, -42, 0)),
    HERO(a(-4, 67, 0), a(0, 8, 0), a(16, 32, -8), a(-99, 63, 0), a(0, -75, -8), a(4, 63, 8));

    private final EulerAngle head;
    private final EulerAngle body;
    private final EulerAngle leftArm;
    private final EulerAngle rightArm;
    private final EulerAngle leftLeg;
    private final EulerAngle rightLeg;

    ArmorStandPose(EulerAngle head, EulerAngle body, EulerAngle leftArm, EulerAngle rightArm, EulerAngle leftLeg, EulerAngle rightLeg) {
        this.head = head;
        this.body = body;
        this.leftArm = leftArm;
        this.rightArm = rightArm;
        this.leftLeg = leftLeg;
        this.rightLeg = rightLeg;
    }

    private static EulerAngle a(double x, double y, double z) {
        return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    /**
     * @return the pose after {@code index}, wrapping around; an unknown index starts again from the first pose
     */
    public static ArmorStandPose after(int index) {
        ArmorStandPose[] poses = values();
        if (index < 0 || index >= poses.length) {
            return poses[0];
        }
        return poses[(index + 1) % poses.length];
    }

    public void applyTo(ArmorStand stand) {
        stand.setHeadPose(head);
        stand.setBodyPose(body);
        stand.setLeftArmPose(leftArm);
        stand.setRightArmPose(rightArm);
        stand.setLeftLegPose(leftLeg);
        stand.setRightLegPose(rightLeg);
    }
}
