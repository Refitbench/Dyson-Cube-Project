#version 150 compatibility

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 uCamPos;

out vec4 vColor;
out vec3 vWorldPos;

void main() {
    vec3 Position = gl_Vertex.xyz;
    // Anchor pattern to object/local space so it does not follow camera or player
    vWorldPos = Position;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vColor = gl_Color;
}
