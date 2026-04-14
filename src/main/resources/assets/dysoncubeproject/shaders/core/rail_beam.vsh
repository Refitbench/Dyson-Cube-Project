#version 150 compatibility

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float uTime;
uniform float uIntensity;

out vec4 vColor;
out float vX;
out vec2 vRC;// radial coords (y,z) for beam falloff

void main(){
    vec3 Position = gl_Vertex.xyz;
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    vColor = gl_Color;

    // Pass along longitudinal and radial info in model space (already oriented by pose)
    vX = Position.x;
    vRC = Position.yz;
}
