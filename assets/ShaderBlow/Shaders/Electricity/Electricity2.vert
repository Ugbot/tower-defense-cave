// author: cvlad

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrixInverse;
attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;

varying vec2 uv;
varying float ypos;
varying float viewAngle;

uniform float m_width;

#ifdef FOG
    varying float fog_z;
#endif


void main() { 
    vec4 vertex = vec4(inPosition, 1.0) + vec4(inNormal * m_width, 0.0);
    gl_Position = g_WorldViewProjectionMatrix * vertex;
    uv = inTexCoord;
    vec4 objectSpaceCamPos = g_WorldViewMatrixInverse * vec4(0.0, 0.0, 0.0, 1.0);
    viewAngle = 1.0 - abs(dot(inNormal, normalize(objectSpaceCamPos.xyz - inPosition)));
    ypos = inPosition.y;

    #ifdef FOG
        fog_z = gl_Position.z;
    #endif

}