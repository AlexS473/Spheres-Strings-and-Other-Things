//Shereece A. A. Victor 922-05-0440 CS465 Project Fri/15th/12/2023
#version 430

in vec3 tc;
out vec4 fragColor;

uniform mat4 v_matrix;
uniform mat4 p_matrix;
layout (binding = 0) uniform samplerCube samp;

void main(void)
{
	fragColor = texture(samp,tc);
}
