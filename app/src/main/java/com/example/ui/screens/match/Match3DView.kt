package com.example.ui.screens.match

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.engine.LiveMatchUiState
import com.example.engine.PitchPlayerNode
import com.example.engine.PlayerVisualPose
import com.example.model.Position
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * GERÇEK 3D MAÇ SAHNESİ (OpenGL ES 2.0)
 *
 * Jetpack Compose 2D Canvas'ın aksine burada gerçek bir 3D dünya kurulur:
 * - perspektif kamera, derinlik testi, ışık-gölge,
 * - 3D saha, kale ve file, tribünler,
 * - prosedürel 3D oyuncu figürleri ve top.
 *
 * Not: Bu bir prototiptir. Hazır 3D model (.glb) olmadığı için oyuncular
 * prosedürel geometriden üretilir; fotoğraftaki fotogerçekçi kalite hedeflenmez.
 */
class Match3DSurfaceView(context: Context) : GLSurfaceView(context) {
    private val sceneRenderer = Match3DRenderer()

    init {
        // Bazı cihazlarda varsayılan EGL config şeffaf/boş bir yüzey üretir.
        // RGBA8888 + derinlik isteyerek sahnenin görünür olmasını garanti et.
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setEGLContextClientVersion(2)
        setRenderer(sceneRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        preserveEGLContextOnPause = true
    }

    fun updateScene(state: LiveMatchUiState, timeSec: Float, homeColor: FloatArray, awayColor: FloatArray) {
        sceneRenderer.update(state, timeSec, homeColor, awayColor)
    }
}

/** Column-major 4x4 matriks yardımcıları (OpenGL standart). */
private object Mat4 {
    fun identity() = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    fun perspective(fovDeg: Float, aspect: Float, near: Float, far: Float): FloatArray {
        val f = 1f / kotlin.math.tan(Math.toRadians(fovDeg.toDouble()).toFloat() / 2f)
        val nf = 1f / (near - far)
        return floatArrayOf(
            f / aspect, 0f, 0f, 0f,
            0f, f, 0f, 0f,
            0f, 0f, (far + near) * nf, -1f,
            0f, 0f, 2f * far * near * nf, 0f
        )
    }

    fun lookAt(eye: FloatArray, center: FloatArray, up: FloatArray): FloatArray {
        val fx = center[0] - eye[0]; val fy = center[1] - eye[1]; val fz = center[2] - eye[2]
        val rl = 1f / kotlin.math.sqrt(fx * fx + fy * fy + fz * fz).coerceAtLeast(0.0001f)
        val nx = fx * rl; val ny = fy * rl; val nz = fz * rl
        val sx = ny * up[2] - nz * up[1]
        val sy = nz * up[0] - nx * up[2]
        val sz = nx * up[1] - ny * up[0]
        val sl = 1f / kotlin.math.sqrt(sx * sx + sy * sy + sz * sz).coerceAtLeast(0.0001f)
        val ux = sx * sl; val uy = sy * sl; val uz = sz * sl
        val vx = uy * nz - uz * ny
        val vy = uz * nx - ux * nz
        val vz = ux * ny - uy * nx
        // Column-major view matrix: 3. kolon = -forward, son satır translation.
        // (ileri yönü '+' koyarsak sahne kameranın arkasında kalır ve ekran siyah olur.)
        return floatArrayOf(
            ux, vx, -nx, 0f,
            uy, vy, -ny, 0f,
            uz, vz, -nz, 0f,
            -(ux * eye[0] + uy * eye[1] + uz * eye[2]),
            -(vx * eye[0] + vy * eye[1] + vz * eye[2]),
            (nx * eye[0] + ny * eye[1] + nz * eye[2]),
            1f
        )
    }

    fun multiply(a: FloatArray, b: FloatArray): FloatArray {
        val out = FloatArray(16)
        for (c in 0..3) {
            for (r in 0..3) {
                var sum = 0f
                for (k in 0..3) sum += a[k * 4 + r] * b[c * 4 + k]
                out[c * 4 + r] = sum
            }
        }
        return out
    }
}

/** Dinamik üçgen geometri biriktirici. */
private class MeshBuilder {
    private val verts = FloatArrayPool()

    fun triangle(
        ax: Float, ay: Float, az: Float,
        bx: Float, by: Float, bz: Float,
        cx: Float, cy: Float, cz: Float,
        color: FloatArray
    ) {
        verts.add(ax, ay, az, color); verts.add(bx, by, bz, color); verts.add(cx, cy, cz, color)
    }

    fun quad(
        ax: Float, ay: Float, az: Float,
        bx: Float, by: Float, bz: Float,
        cx: Float, cy: Float, cz: Float,
        dx: Float, dy: Float, dz: Float,
        color: FloatArray
    ) {
        triangle(ax, ay, az, bx, by, bz, cx, cy, cz, color)
        triangle(ax, ay, az, cx, cy, cz, dx, dy, dz, color)
    }

    /** Şading uygulanmış küp. */
    fun box(cx: Float, cy: Float, cz: Float, sx: Float, sy: Float, sz: Float, base: FloatArray) {
        val x0 = cx - sx; val x1 = cx + sx
        val y0 = cy - sy; val y1 = cy + sy
        val z0 = cz - sz; val z1 = cz + sz
        fun shade(m: Float) = floatArrayOf(base[0] * m, base[1] * m, base[2] * m, base[3])
        val top = shade(1.0f); val front = shade(0.82f); val side = shade(0.66f); val dark = shade(0.5f)
        // üst
        quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, top)
        // ön
        quad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, front)
        // arka
        quad(x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, dark)
        // sağ
        quad(x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, side)
        // sol
        quad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, side)
    }

    /** Düşük poligonlu küre (top / kafa). */
    fun sphere(cx: Float, cy: Float, cz: Float, r: Float, base: FloatArray, seg: Int = 10, rings: Int = 6) {
        for (i in 0 until seg) {
            val a0 = (i.toFloat() / seg) * 2f * Math.PI.toFloat()
            val a1 = ((i + 1).toFloat() / seg) * 2f * Math.PI.toFloat()
            for (j in 0 until rings) {
                val p0 = (j.toFloat() / rings) * Math.PI.toFloat()
                val p1 = ((j + 1).toFloat() / rings) * Math.PI.toFloat()
                fun pt(a: Float, p: Float) = floatArrayOf(
                    cx + r * sin(p) * cos(a),
                    cy + r * cos(p),
                    cz + r * sin(p) * sin(a)
                )
                val p00 = pt(a0, p0); val p10 = pt(a1, p0); val p11 = pt(a1, p1); val p01 = pt(a0, p1)
                val shade = 0.72f + 0.28f * cos(p0)
                val c = floatArrayOf(base[0] * shade, base[1] * shade, base[2] * shade, base[3])
                triangle(p00[0], p00[1], p00[2], p10[0], p10[1], p10[2], p11[0], p11[1], p11[2], c)
                triangle(p00[0], p00[1], p00[2], p11[0], p11[1], p11[2], p01[0], p01[1], p01[2], c)
            }
        }
    }

    /** Yerdeki yumuşak gölge (yatay elips). */
    fun shadow(cx: Float, cz: Float, rx: Float, rz: Float, alpha: Float) {
        val c = floatArrayOf(0f, 0f, 0f, alpha)
        val steps = 12
        for (i in 0 until steps) {
            val a0 = (i.toFloat() / steps) * 2f * Math.PI.toFloat()
            val a1 = ((i + 1).toFloat() / steps) * 2f * Math.PI.toFloat()
            triangle(
                cx, 0.03f, cz,
                cx + cos(a0) * rx, 0.03f, cz + sin(a0) * rz,
                cx + cos(a1) * rx, 0.03f, cz + sin(a1) * rz,
                c
            )
        }
    }

    fun build(): FloatArray = verts.toArray()

    private class FloatArrayPool {
        private var data = FloatArray(4096)
        private var size = 0
        private fun ensure(n: Int) {
            if (size + n > data.size) data = data.copyOf(data.size * 2 + n)
        }
        fun add(x: Float, y: Float, z: Float, c: FloatArray) {
            ensure(7)
            data[size++] = x; data[size++] = y; data[size++] = z
            data[size++] = c[0]; data[size++] = c[1]; data[size++] = c[2]; data[size++] = c[3]
        }
        fun toArray(): FloatArray = data.copyOf(size)
    }
}

private class Match3DRenderer : GLSurfaceView.Renderer {
    private var program = 0
    private var aPos = 0
    private var aColor = 0
    private var uMvp = 0
    private var vbo = 0
    private var vertexCount = 0
    private var aspect = 1f

    @Volatile private var state: LiveMatchUiState? = null
    @Volatile private var timeSec = 0f
    @Volatile private var homeColor = floatArrayOf(0.9f, 0.2f, 0.2f, 1f)
    @Volatile private var awayColor = floatArrayOf(0.2f, 0.4f, 0.9f, 1f)

    fun update(s: LiveMatchUiState, t: Float, home: FloatArray, away: FloatArray) {
        state = s; timeSec = t; homeColor = home; awayColor = away
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.03f, 0.08f, 0.05f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        program = buildProgram(VERTEX, FRAGMENT)
        aPos = GLES20.glGetAttribLocation(program, "aPos")
        aColor = GLES20.glGetAttribLocation(program, "aColor")
        uMvp = GLES20.glGetUniformLocation(program, "uMvp")
        val bufs = IntArray(1); GLES20.glGenBuffers(1, bufs, 0); vbo = bufs[0]
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        aspect = width.toFloat() / height.coerceAtLeast(1)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0.04f, 0.22f, 0.10f, 1f) // koyu yeşil zemin - siyah değil
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val s = state ?: return
        if (program == 0) return
        val mesh = buildScene(s)
        val data = mesh.first
        vertexCount = data.size / 7
        if (vertexCount == 0) return

        GLES20.glUseProgram(program)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        val fb: FloatBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(data); position(0) }
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, data.size * 4, fb, GLES20.GL_DYNAMIC_DRAW)

        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 28, 0)
        GLES20.glEnableVertexAttribArray(aColor)
        GLES20.glVertexAttribPointer(aColor, 4, GLES20.GL_FLOAT, false, 28, 12)

        val attackingRight = s.userIsHome
        val eye = floatArrayOf(if (attackingRight) -74f else 74f, 32f, 0f)
        val center = floatArrayOf(if (attackingRight) 16f else -16f, 2f, 0f)
        val view = Mat4.lookAt(eye, center, floatArrayOf(0f, 1f, 0f))
        val proj = Mat4.perspective(52f, aspect.coerceIn(0.5f, 3f), 0.5f, 400f)
        val mvp = Mat4.multiply(proj, view)
        GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }

    /** Sahneyi her karede LiveMatchUiState'ten yeniden kurar. */
    private fun buildScene(s: LiveMatchUiState): Pair<FloatArray, Int> {
        val m = MeshBuilder()
        val attackingRight = s.userIsHome

        // ---- Zemin: 20 x 14 çim karesi ----
        val cols = 20; val rows = 14
        for (c in 0 until cols) {
            for (r in 0 until rows) {
                val x0 = -50f + c * (100f / cols); val x1 = -50f + (c + 1) * (100f / cols)
                val z0 = -32f + r * (64f / rows); val z1 = -32f + (r + 1) * (64f / rows)
                val green = if ((c + r) % 2 == 0) floatArrayOf(0.16f, 0.55f, 0.22f, 1f)
                else floatArrayOf(0.13f, 0.48f, 0.19f, 1f)
                m.quad(x0, 0f, z0, x1, 0f, z0, x1, 0f, z1, x0, 0f, z1, green)
            }
        }

        // ---- Saha çizgileri ----
        val white = floatArrayOf(0.92f, 0.95f, 0.92f, 1f)
        fun line(x0: Float, z0: Float, x1: Float, z1: Float, w: Float = 0.35f) {
            m.quad(x0, 0.05f, z0 - w, x1, 0.05f, z1 - w, x1, 0.05f, z1 + w, x0, 0.05f, z0 + w, white)
        }
        line(-50f, -32f, 50f, -32f); line(-50f, 32f, 50f, 32f)
        line(-50f, -32f, -50f, 32f); line(50f, -32f, 50f, 32f)
        line(0f, -32f, 0f, 32f)
        val boxEdge = if (attackingRight) 33f else -33f
        val goalSide = if (attackingRight) 50f else -50f
        line(goalSide, -20f, boxEdge, -20f); line(goalSide, 20f, boxEdge, 20f)
        line(boxEdge, -20f, boxEdge, 20f)

        // ---- Kale (3D direkler + file) ----
        val gx = if (attackingRight) 50f else -50f
        val postZ = 5.7f
        val crossY = 3.9f
        val post = floatArrayOf(0.95f, 0.97f, 1f, 1f)
        m.box(gx, crossY / 2f, -postZ, 0.28f, crossY / 2f, 0.28f, post)
        m.box(gx, crossY / 2f, postZ, 0.28f, crossY / 2f, 0.28f, post)
        m.box(gx, crossY, 0f, 0.28f, 0.28f, postZ, post)
        val netColor = floatArrayOf(0.8f, 0.9f, 0.85f, 0.35f)
        val netBack = gx + if (attackingRight) 3f else -3f
        for (i in 0..10) {
            val z = -postZ + i * (postZ * 2f / 10f)
            m.quad(gx, 0f, z, netBack, 0f, z, netBack, crossY, z, gx, crossY, z, netColor)
        }
        for (i in 0..5) {
            val y = i * (crossY / 5f)
            m.quad(gx, y, -postZ, netBack, y, -postZ, netBack, y, postZ, gx, y, postZ, netColor)
        }

        // ---- Tribünler (basit kutular + renkli kalabalık noktaları) ----
        val concrete = floatArrayOf(0.18f, 0.21f, 0.26f, 1f)
        m.box(gx + if (attackingRight) 14f else -14f, 5f, 0f, 10f, 5f, 40f, concrete)
        m.box(0f, 4f, 42f, 55f, 4f, 8f, concrete)
        m.box(0f, 4f, -42f, 55f, 4f, 8f, concrete)
        for (i in 0 until 120) {
            val rnd = ((i * 73) % 97).toFloat() / 97f
            val rnd2 = ((i * 41 + 13) % 89).toFloat() / 89f
            val cz = -38f + rnd * 76f
            val cy = 2f + rnd2 * 6f
            val c = if (i % 3 == 0) homeColor else if (i % 3 == 1) awayColor
            else floatArrayOf(0.8f, 0.78f, 0.7f, 1f)
            m.box(gx + if (attackingRight) 10f else -10f, cy, cz, 0.35f, 0.35f, 0.35f, c)
        }

        // ---- Oyuncular ----
        fun toWorldX(px: Float) = px - 50f
        fun toWorldZ(py: Float) = (py - 50f) * 0.64f
        fun drawPlayer(p: PitchPlayerNode) {
            val wx = toWorldX(p.x)
            val wz = toWorldZ(p.y)
            val bob = if (p.isMakingRun) sin(timeSec * 9f + p.number) * 0.18f else 0f
            val dive = p.isDivingOrSliding
            val team = if (p.isHome) homeColor else awayColor
            val skin = floatArrayOf(0.86f, 0.68f, 0.52f, 1f)
            val shorts = floatArrayOf(0.95f, 0.95f, 0.95f, 1f)
            m.shadow(wx, wz, 1.1f, 0.7f, 0.35f)
            if (dive) {
                m.box(wx, 0.9f + bob, wz, 2.0f, 0.45f, 0.6f, team)
                m.sphere(wx + (if (p.diveDirectionY < 0f) 1.8f else -1.8f), 1.1f, wz, 0.35f, skin)
            } else {
                m.box(wx - 0.32f, 0.55f + bob, wz, 0.22f, 0.55f, 0.28f, shorts)
                m.box(wx + 0.32f, 0.55f + bob, wz, 0.22f, 0.55f, 0.28f, shorts)
                m.box(wx, 1.65f + bob, wz, 0.55f, 0.75f, 0.38f, team)
                m.sphere(wx, 2.65f + bob, wz, 0.38f, skin)
            }
            if (p.isUser) {
                val ring = floatArrayOf(0.2f, 0.95f, 0.55f, 1f)
                for (i in 0 until 16) {
                    val a0 = i * (2f * Math.PI.toFloat() / 16f)
                    val a1 = (i + 1) * (2f * Math.PI.toFloat() / 16f)
                    m.triangle(wx, 0.08f, wz,
                        wx + cos(a0) * 1.3f, 0.08f, wz + sin(a0) * 1.3f,
                        wx + cos(a1) * 1.3f, 0.08f, wz + sin(a1) * 1.3f, ring)
                }
            }
        }
        s.players.forEach { drawPlayer(it) }

        // ---- Top ----
        val bx = toWorldX(s.ball.x)
        val bz = toWorldZ(s.ball.y)
        val by = 0.45f + (s.ball.z / 24f) * 0.06f
        m.shadow(bx, bz, 0.5f, 0.5f, 0.4f)
        m.sphere(bx, by, bz, 0.45f, floatArrayOf(1f, 1f, 1f, 1f), 12, 8)

        return m.build() to vertexCount
    }

    companion object {
        private const val VERTEX = """
            attribute vec3 aPos;
            attribute vec4 aColor;
            uniform mat4 uMvp;
            varying vec4 vColor;
            void main() {
                gl_Position = uMvp * vec4(aPos, 1.0);
                vColor = aColor;
            }
        """
        private const val FRAGMENT = """
            precision mediump float;
            varying vec4 vColor;
            void main() { gl_FragColor = vColor; }
        """

        private fun buildProgram(vs: String, fs: String): Int {
            fun compile(type: Int, src: String): Int {
                val sh = GLES20.glCreateShader(type)
                GLES20.glShaderSource(sh, src)
                GLES20.glCompileShader(sh)
                val status = IntArray(1)
                GLES20.glGetShaderiv(sh, GLES20.GL_COMPILE_STATUS, status, 0)
                if (status[0] == 0) {
                    android.util.Log.e("Match3D", "Shader compile error: ${GLES20.glGetShaderInfoLog(sh)}")
                }
                return sh
            }
            val p = GLES20.glCreateProgram()
            GLES20.glAttachShader(p, compile(GLES20.GL_VERTEX_SHADER, vs))
            GLES20.glAttachShader(p, compile(GLES20.GL_FRAGMENT_SHADER, fs))
            GLES20.glLinkProgram(p)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                android.util.Log.e("Match3D", "Program link error: ${GLES20.glGetProgramInfoLog(p)}")
                return 0
            }
            return p
        }
    }
}
