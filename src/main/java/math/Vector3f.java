package math;

public class Vector3f {
    final float eps = 1e-7f;
    final float x, y, z;

    public Vector3f(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(final Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        Vector3f other = (Vector3f) obj;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
