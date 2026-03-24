package com.ee.Common;

import org.joml.*;
import java.lang.Math;

public class Util {
    public static float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    public static Vector3i vec3fToVec3i(Vector3f vec) {
        return new Vector3i((int) Math.floor(vec.x), (int) Math.floor(vec.y), (int) Math.floor(vec.z));
    }
}
