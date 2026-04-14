#version 150 compatibility

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vColor;
out vec3 vWorldPos;

void main() {
    vec3 Position = gl_Vertex.xyz;
    // Pass through world-space position (before ModelView transform) for procedural effects
    vWorldPos = Position;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vColor = gl_Color;
}
