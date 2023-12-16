//Shereece A. A. Victor 922-05-0440 CS465 Project Fri/15th/12/2023
#version 430

layout (location=0) in vec3 position;

uniform mat4 shadowMVP;

void main(void)
{
    gl_Position = shadowMVP * vec4(position,1.0);
}
