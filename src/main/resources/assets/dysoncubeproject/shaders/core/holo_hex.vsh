#version 150 compatibility

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 uCamPos;
uniform float uIsSkyPass;// 1.0 when rendering the sky overlay, 0.0 for tile use

out vec4 vColor;
out vec3 vWorldPos;

void main() {
    vec3 Position = gl_Vertex.xyz;
    // Keep procedural UV anchor in local model space to avoid camera-relative drift.
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    vWorldPos = Position;
    gl_Position = ProjMat * viewPos;
    // Sky trick: when rendering the sky overlay the quad sits 310 units away which
    // can exceed the terrain far-clip plane at lower render distances. Clamping
    // NDC z to just inside 1.0 keeps all vertices visible regardless of far-plane.
    // Not applied for tile rendering (uIsSkyPass == 0) where normal depth is needed.
    if (uIsSkyPass > 0.5) {
        gl_Position.z = gl_Position.w * 0.9999;
    }
    vColor = gl_Color;
}
