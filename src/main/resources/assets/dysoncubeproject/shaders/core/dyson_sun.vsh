#version 150 compatibility

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vColor;
out vec3 vWorldPos;

void main() {
    vec3 Position = gl_Vertex.xyz;
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    vWorldPos = viewPos.xyz;
    gl_Position = ProjMat * viewPos;
    vColor = gl_Color;
}
