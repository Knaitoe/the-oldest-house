#version 150

// The reflected picture is drawn with the ordinary camera, so each pixel of the glass shows the same pixel of it,
// a little darkened and cooled, as old silvered glass is.
uniform sampler2D Sampler0;
uniform vec2 ScreenSize;
uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    vec3 seen = texture(Sampler0, gl_FragCoord.xy / ScreenSize).rgb;
    fragColor = vec4(seen * vec3(0.90, 0.92, 0.93) + vec3(0.012, 0.016, 0.02), 1.0) * ColorModulator;
}
