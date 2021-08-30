#version 120

uniform vec4 entitybrightness;
uniform sampler2D lightmap;

// applies light level from lightmap and entity brightness (primed tnt/creeper glow, mob hurt red, etc...)
void applyLighting(inout vec4 color) {
    color = texture2D(lightmap, gl_TexCoord[1].st) * color * (1 - entitybrightness.a) + entitybrightness;
}