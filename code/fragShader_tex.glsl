//922-05-0440 CS465 Project Fri/15th/12/2023
#version 430

in vec2 tc;
in vec4 varyingColor;
in vec4 shadow_coord;
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
layout (binding=1) uniform sampler2DShadow shadowTex;

void main(void)
{
	float notInShadow = textureProj(shadowTex, shadow_coord);

	color = texture(s,tc);

	if (notInShadow == 1.0)
	{
		color = 0.85*texture(s,tc) + 0.15*varyingColor;
	}

}
