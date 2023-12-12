#version 430

in vec4 varyingColor;
in vec3 vNormal;
in vec3 vVertPos;
out vec4 fragColor;

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
layout (binding = 0) uniform samplerCube t;

void main(void)
{
	vec3 r = -reflect(normalize(-vVertPos), normalize(vNormal));
	fragColor = 0.90* texture(t,r) + 0.05*varyingColor;
}