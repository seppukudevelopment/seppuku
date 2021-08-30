#version 120

uniform vec4 entitybrightness;

// like applyLighting, but only applies entity brightness
void applyEntityBrightness(inout vec4 color) {
    color = color * (1 - entitybrightness.a) + entitybrightness;
}