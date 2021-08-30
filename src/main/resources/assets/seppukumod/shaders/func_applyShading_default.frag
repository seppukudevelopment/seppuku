#version 120

varying vec3 normal;

// applies diffuse shading with ambient light to a fragment
// its not perfect; for example, some faces are slightly darker than when not using the shader and its especially noticable with dropped block items
void applyShading(inout vec4 color) {
    // combine all light intensities for diffuse lighting
    float intensity0 = max(dot(normalize(vec3(gl_LightSource[0].position)), normal), 0.0f);
    float intensity1 = max(dot(normalize(vec3(gl_LightSource[1].position)), normal), 0.0f);
    float intensity = min(intensity0 + intensity1, 1.0f);
    // apply diffuse + ambient light. using hardcoded values from net.minecraft.client.renderer.RenderHelper
    color *= intensity * vec4(0.6f, 0.6f, 0.6f, 0.0f) + vec4(0.4f, 0.4f, 0.4f, 1.0f);
}