#version 430

in vec2 tc;
in vec4 varyingColor;
out vec4 color;

struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;

uniform mat4 mv_matrix;
layout (binding=0) uniform sampler2D s;

void main(void)
{
	color = 0.85*texture(s,tc) + 0.15*varyingColor;

}
