#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 tex_coord;
layout (location = 2) in vec3 normal;
out vec2 tc;
out vec4 varyingColor;

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
	vec4 color;

	// convert vertex position to world space
	vec4 P = m_matrix * vec4(position,1.0);

	// convert normal to world space
	vec3 N = normalize((norm_matrix * vec4(normal,1.0)).xyz);

	// calculate view-space light vector (from point to light)
	vec3 L = normalize(light.position - P.xyz);

	//  view vector is from vertex to camera
	vec3 V = normalize(-v_matrix[3].xyz - P.xyz);

	//  R is reflection of -L around the plane defined by N
	vec3 R = reflect(-L,N);

	// ambient, diffuse, and specular contributions
	vec3 ambient =
	(globalAmbient
	+ light.ambient ).xyz;

	vec3 diffuse =
	light.diffuse.xyz
	* max(dot(N,L), 0.0);

	vec3 specular = light.specular.xyz;

	// send the color output to the fragment shader
	varyingColor = vec4((ambient + diffuse + specular), 1.0);

	gl_Position = p_matrix * mv_matrix * vec4(position,1.0);
	tc = tex_coord;
}