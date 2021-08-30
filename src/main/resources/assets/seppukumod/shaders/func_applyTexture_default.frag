#version 120

uniform sampler2D texture;

// applies the main texture to a fragment
void applyTexture(inout vec4 color) {
    color *= texture2D(texture, gl_TexCoord[0].st);
}