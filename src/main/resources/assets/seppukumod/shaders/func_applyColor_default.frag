#version 120

// applies vertex color to a fragment
void applyColor(inout vec4 color) {
    color *= gl_Color;
}