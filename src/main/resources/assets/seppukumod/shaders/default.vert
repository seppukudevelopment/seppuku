#version 120

varying vec3 normal;

// a vertex shader that tries to imitate the way minecraft renders objects
void main() {
    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_TextureMatrix[1] * gl_MultiTexCoord1;
    gl_TexCoord[2] = gl_TextureMatrix[2] * gl_MultiTexCoord2;
    gl_FrontColor = gl_Color;
    gl_FrontSecondaryColor = gl_SecondaryColor;
    gl_BackColor = gl_Color;
    gl_BackSecondaryColor = gl_SecondaryColor;
    gl_Position = ftransform();
    normal = normalize(gl_NormalMatrix * gl_Normal);
}
