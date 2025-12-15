package com.barium.client.util;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Portado de me.jellysquid.mods.sodium.common.util.MatrixHelper
 * Otimiza rotações de matrizes evitando alocações de Quaternions e cálculos repetitivos.
 */
public class FastMatrixUtil {

    /**
     * Rotaciona as matrizes de posição e normal na ordem ZYX.
     * Funcionalmente idêntico a rotacionar o MatrixStack com um quaternion, mas significativamente mais rápido.
     */
    public static void rotateZYX(MatrixStack.Entry matrices, float angleZ, float angleY, float angleX) {
        float sinX = Math.sin(angleX);
        float cosX = Math.cosFromSin(sinX, angleX);
        float sinInvX = -sinX;

        float sinY = Math.sin(angleY);
        float cosY = Math.cosFromSin(sinY, angleY);
        float sinInvY = -sinY;

        float sinZ = Math.sin(angleZ);
        float cosZ = Math.cosFromSin(sinZ, angleZ);
        float sinInvZ = -sinZ;

        applySinCosMat4(matrices.getPositionMatrix(), sinX, sinY, sinZ, cosX, cosY, cosZ, sinInvX, sinInvY, sinInvZ);
        applySinCosMat3(matrices.getNormalMatrix(), sinX, sinY, sinZ, cosX, cosY, cosZ, sinInvX, sinInvY, sinInvZ);
    }

    private static void applySinCosMat4(Matrix4f mat, float sinX, float sinY, float sinZ, float cosX, float cosY, float cosZ, float sinInvX, float sinInvY, float sinInvZ) {
        float nm00 = (mat.m00() * cosZ) + (mat.m10() * sinZ);
        float nm01 = (mat.m01() * cosZ) + (mat.m11() * sinZ);
        float nm02 = (mat.m02() * cosZ) + (mat.m12() * sinZ);
        float nm03 = (mat.m03() * cosZ) + (mat.m13() * sinZ);

        float nm10 = (mat.m00() * sinInvZ) + (mat.m10() * cosZ);
        float nm11 = (mat.m01() * sinInvZ) + (mat.m11() * cosZ);
        float nm12 = (mat.m02() * sinInvZ) + (mat.m12() * cosZ);
        float nm13 = (mat.m03() * sinInvZ) + (mat.m13() * cosZ);

        float nm20 = (nm00 * sinY) + (mat.m20() * cosY);
        float nm21 = (nm01 * sinY) + (mat.m21() * cosY);
        float nm22 = (nm02 * sinY) + (mat.m22() * cosY);
        float nm23 = (nm03 * sinY) + (mat.m23() * cosY);

        mat.set(
                (nm00 * cosY) + (mat.m20() * sinInvY),
                (nm01 * cosY) + (mat.m21() * sinInvY),
                (nm02 * cosY) + (mat.m22() * sinInvY),
                (nm03 * cosY) + (mat.m23() * sinInvY),

                (nm10 * cosX) + (nm20 * sinX),
                (nm11 * cosX) + (nm21 * sinX),
                (nm12 * cosX) + (nm22 * sinX),
                (nm13 * cosX) + (nm23 * sinX),

                (nm10 * sinInvX) + (nm20 * cosX),
                (nm11 * sinInvX) + (nm21 * cosX),
                (nm12 * sinInvX) + (nm22 * cosX),
                (nm13 * sinInvX) + (nm23 * cosX),

                mat.m30(),
                mat.m31(),
                mat.m32(),
                mat.m33()
        );
    }

    private static void applySinCosMat3(Matrix3f mat, float sinX, float sinY, float sinZ, float cosX, float cosY, float cosZ, float sinInvX, float sinInvY, float sinInvZ) {
        float nm00 = mat.m00 * cosZ + mat.m10 * sinZ;
        float nm01 = mat.m01 * cosZ + mat.m11 * sinZ;
        float nm02 = mat.m02 * cosZ + mat.m12 * sinZ;

        float nm10 = mat.m00 * sinInvZ + mat.m10 * cosZ;
        float nm11 = mat.m01 * sinInvZ + mat.m11 * cosZ;
        float nm12 = mat.m02 * sinInvZ + mat.m12 * cosZ;

        float nm20 = nm00 * sinY + mat.m20 * cosY;
        float nm21 = nm01 * sinY + mat.m21 * cosY;
        float nm22 = nm02 * sinY + mat.m22 * cosY;

        mat.set(nm00 * cosY + mat.m20 * sinInvY,
                nm01 * cosY + mat.m21 * sinInvY,
                nm02 * cosY + mat.m22 * sinInvY,

                nm10 * cosX + nm20 * sinX,
                nm11 * cosX + nm21 * sinX,
                nm12 * cosX + nm22 * sinX,

                nm10 * sinInvX + nm20 * cosX,
                nm11 * sinInvX + nm21 * cosX,
                nm12 * sinInvX + nm22 * cosX);
    }
}