//Shereece A. A. Victor 922-05-0440 CS465 Project Fri/15th/12/2023
#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
out vec3 vNormal;
out vec3 vVertPos;
out vec4 ambDif;
out vec4 spec;

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

	spec = vec4((light.specular.xyz + 0 + 0), 1.0);

	// send the color output to the fragment shader
	ambDif = vec4((ambient + diffuse + 0), 1.0);

	// send the position to the fragment shader, as before
	//gl_Position = p_matrix * v_matrix * m_matrix * vec4(vertPos,1.0);

	vVertPos = (mv_matrix * vec4(position,1.0)).xyz;
	vNormal = (norm_matrix * vec4(normal,1.0)).xyz;
	gl_Position = p_matrix * mv_matrix * vec4(position,1.0);
}
