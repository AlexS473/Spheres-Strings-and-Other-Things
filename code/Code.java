//922-05-0440 CS465 Project Fri/15th/12/2023
package code;

import java.nio.*;
import java.lang.Math;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import org.joml.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Code extends JFrame implements GLEventListener, KeyListener
{	private GLCanvas myCanvas;
	float rot1, rot2, rot4, rot5;
	float rot11, rot12, rot21, rot22, rot41, rot42, rot51, rot52;
	private double startTime = 0.0;
	private int renderingProgramShadow,renderingProgramRefl, renderingProgramCubeMap, renderingProgramTex;
	private int vao[] = new int[1];
	private int vbo[] = new int[13];
	private int skyboxTexture;
	private Sphere sphere = new Sphere(96);
	private int numSphereVerts;
	private float cameraX, cameraY, cameraZ;
	private Vector3f initialLightLoc = new Vector3f(5, 5.0f, 1.0f);
	private float rotx, roty = 0;
	
	// allocate variables for display() function
	private Shapes shapes = new Shapes();
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(10);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private Matrix4f invTrMat = new Matrix4f();	// invert-transpose for normals
	private int vLoc, mvLoc, pLoc, nLoc, sLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc;
	private float aspect;
	private double elapsedTime;
	private double tf;
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];
	private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

	double angle = 0;

	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();

	// white light properties
	float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
	float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	private int snowTexture;
	private int numObjVertices;
	private ImportedModel iceburg;
	private ImportedModel frame;
	private ImportedModel string;
	boolean firstPass;

	public Code()
	{	setTitle("Spheres-Strings and Other Things");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);
		myCanvas.setFocusable(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	public boolean[] raise = {false,false,false,false,false,false};
	public boolean[] start = {false,false,false,false,false,false};
	public int[] startList = new int[6];

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		elapsedTime = System.currentTimeMillis() - startTime;
		tf = elapsedTime/1000.0;

		vMat.identity().setTranslation(-cameraX, -cameraY, -cameraZ);
		vMat.rotateXYZ(rotx, roty,0);

		currentLightPos.set(initialLightLoc);
		currentLightPos.rotateY((float)Math.toRadians(angle));

		drawSkyBox(gl);

		passOne(gl);

		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued

		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);

		gl.glDrawBuffer(GL_FRONT);

		drawScene(gl, 2);

	}

	private void passOne(GL4 gl) {
		//shadow stuff
		lightVmat.identity().setLookAt(currentLightPos, origin, up);	// vector from light to origin
		lightPmat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);

		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_POLYGON_OFFSET_FILL);	//  for reducing
		gl.glPolygonOffset(3.0f, 5.0f);		//  shadow artifacts

		gl.glUseProgram(renderingProgramShadow);
		drawScene(gl, 1);

	}

	private void drawScene(GL4 gl, int pass){
		mvStack.pushMatrix();
		mvStack.mul(vMat);

		mvStack.pushMatrix();
		mvStack.translate(0f,-7.25f,1);
		drawFrame(gl, pass);
		mvStack.popMatrix();

		mvStack.pushMatrix();
		mvStack.translate(0f,-11f,0);
		mvStack.rotateY((float) Math.toRadians(90));
		drawIceberg(gl,pass);
		mvStack.popMatrix();

		//Sphere 1  String Front
		mvStack.pushMatrix();
		mvStack.translate(-3f,-5.75f, 0);

		double maxAngle = -.65;
		if (raise[1]){
			rot11 = (float)Math.max(Math.cos(3 * tf) * maxAngle, 0);
		}
		else if(start[1]){
			rot11 = (float) Math.max( (Math.cos(4 * tf) * maxAngle), 0);
		}
		mvStack.rotateZ(-rot11);
		mvStack.translate(0f,-1.75f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 1  String Back
		mvStack.pushMatrix();
		mvStack.translate(-3f,-5.75f, 0);
		mvStack.rotateY((float) Math.toRadians(180));
		if (raise[1]){
			rot12 = (float)Math.max(Math.cos(3 * tf) * maxAngle, 0);
		}
		else if(start[1]){
			rot12 = (float) Math.max( (Math.cos(4 * tf) * maxAngle), 0);
		}
		mvStack.rotateZ(rot12);
		mvStack.translate(0f,-1.75f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 1
		mvStack.pushMatrix();
		mvStack.translate(-3f,-7.5f,0);
		maxAngle = -1.1;
		if (raise[1]){
			rot1 = (float)Math.max((Math.cos(3 * tf) * maxAngle),0);
			if (rot1 >= 1.098576){
				raise[1] = false;
				startList[1]=1;
				startList[5]=1;
			}
		}
		else if(start[1]){
			rot1 = (float) Math.max( (Math.cos(4 * tf) * maxAngle), 0);
		}
		mvStack.rotateZ(-rot1);
		mvStack.translate(0f,-2,0);
		mvStack.scale(.75f, .75f, .75f);
		drawSphere(gl,pass);
		mvStack.popMatrix();

		//Sphere 2  String Front
		mvStack.pushMatrix();
		mvStack.translate(-1.5f,-5.75f, 0);

		double maxAngle2 = -.6;
		if (raise[2]){
			rot21 = (float)Math.max(Math.cos(3 * tf) * maxAngle2, 0);
		}
		else if(start[2]){
			rot21 = (float) Math.max( (Math.cos(4 * tf) * maxAngle2), 0);
		}
		mvStack.rotateZ(-rot21);
		mvStack.translate(0f,-1.75f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 2  String Back
		mvStack.pushMatrix();
		mvStack.translate(-1.5f,-5.75f, 0);
		mvStack.rotateY((float) Math.toRadians(180));
		if (raise[2]){
			rot22 = (float)Math.max(Math.cos(3 * tf) * maxAngle2, 0);
		}
		else if(start[2]){
			rot22 = (float) Math.max( (Math.cos(4 * tf) * maxAngle2), 0);
		}
		mvStack.rotateZ(rot22);
		mvStack.translate(0f,-1.75f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 2
		mvStack.pushMatrix();
		mvStack.translate(-1.5f,-7.5f,0);
		maxAngle2 = -1;
		if (raise[2]){
			rot2 = (float)Math.max((Math.cos(3 * tf) * maxAngle2),0);
			if (rot2 >= 0.999836){
				raise[2] = false;
				startList[2]=1;
				startList[4]=1;
			}
		}
		else if(start[2]){
			rot2 = (float) Math.max( (Math.cos(4 * tf) * maxAngle2), 0);

		}
		mvStack.rotateZ(-rot2);
		mvStack.translate(0f,-2,0);
		mvStack.scale(.75f, .75f, .75f);
		drawSphere(gl, pass);
		mvStack.popMatrix();

		//Sphere 3  String Front
		mvStack.pushMatrix();
		mvStack.translate(0f,-7.5f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 3  String Back
		mvStack.pushMatrix();
		mvStack.translate(0,-7.5f, -1f);
		mvStack.rotateY((float) Math.toRadians(180));
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 3
		mvStack.pushMatrix();
		mvStack.translate(0f,-9.5f,0);
		mvStack.scale(.75f, .75f, .75f);
		drawSphere(gl, pass);
		mvStack.popMatrix();

		//Sphere 4  String Front
		mvStack.pushMatrix();
		mvStack.translate(1.5f,-5.75f, 0);

		double maxAngle4 = .6;
		if (raise[4]){
			rot41 = (float)Math.max(Math.cos(3 * tf) * maxAngle4, 0);
		}
		else if(start[4]){
			rot41 = (float) Math.max( (Math.cos(4 * tf) * maxAngle4), 0);
		}
		mvStack.rotateZ(rot41);
		mvStack.translate(0f,-1.75f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 4  String Back
		mvStack.pushMatrix();
		mvStack.translate(1.5f,-5.75f, 0);
		mvStack.rotateY((float) Math.toRadians(180));
		if (raise[4]){
			rot42 = (float)Math.max(Math.cos(3 * tf) * maxAngle4, 0);
		}
		else if(start[4]){
			rot42 = (float) Math.max( (Math.cos(4 * tf) * maxAngle4), 0);
		}
		mvStack.rotateZ(-rot42);
		mvStack.translate(0f,-1.75f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 4
		mvStack.pushMatrix();
		mvStack.translate(1.5f,-7.5f,0);
		maxAngle4 = 1;
		if (raise[4]){
			rot4 = (float)Math.max((Math.cos(3 * tf) * maxAngle4),0);
			if (rot4 >= 0.999){
				raise[4] = false;
				startList[2]=1;
				startList[4]=1;
			}
		}
		else if(start[4]){
			rot4 = (float) Math.max( (Math.cos(4 * tf) * maxAngle4), 0);
		}
		mvStack.rotateZ(rot4);
		mvStack.translate(0f,-2,0);
		mvStack.scale(.75f, .75f, .75f);
		drawSphere(gl, pass);
		mvStack.popMatrix();

		//Sphere 5  String Front
		mvStack.pushMatrix();
		mvStack.translate(3f,-5.75f, 0);

		double maxAngle5 = .65;
		if (raise[5]){
			rot51 = (float)Math.max(Math.cos(3 * tf) * maxAngle5, 0);
		}
		else if(start[5]){
			rot51 = (float) Math.max( (Math.cos(4 * tf) * maxAngle5), 0);
		}
		mvStack.rotateZ(rot51);
		mvStack.translate(0f,-1.75f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 5  String Back
		mvStack.pushMatrix();
		mvStack.translate(3f,-5.75f, 0);
		mvStack.rotateY((float) Math.toRadians(180));
		if (raise[5]){
			rot52 = (float)Math.max(Math.cos(3 * tf) * maxAngle5, 0);
		}
		else if(start[5]){
			rot52 = (float) Math.max( (Math.cos(4 * tf) * maxAngle5), 0);
		}
		mvStack.rotateZ(-rot52);
		mvStack.translate(0f,-1.75f,1f);
		drawString(gl, pass);
		mvStack.popMatrix();

		//Sphere 5
		mvStack.pushMatrix();
		mvStack.translate(3f,-7.5f,0);
		maxAngle5 = 1.1;
		if (raise[5]){
			rot5 = (float)Math.max((Math.cos(3 * tf) * maxAngle5),0);
			if (rot5 >= 1.098576){
				raise[5] = false;
				startList[5]=1;
				startList[1]=1;
			}
		}
		else if(start[5]){
			rot5 = (float) Math.max( (Math.cos(4 * tf) * maxAngle5), 0);
		}
		mvStack.rotateZ(rot5);
		mvStack.translate(0f,-2,0);
		mvStack.scale(.75f, .75f, .75f);
		drawSphere(gl, pass);
		mvStack.popMatrix();

		mvStack.pushMatrix();
		mvStack.translate(0f,-7.25f,5);
		drawFrame(gl, pass);
		mvStack.popMatrix();

		mvStack.popMatrix();
	}

	private void installLights(GL4 gl, int renderingProgram)
	{
		gl.glUseProgram(renderingProgram);

		lightPos[0]=currentLightPos.x();
		lightPos[1]=currentLightPos.y();
		lightPos[2]=currentLightPos.z();

		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");

		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		startTime = System.currentTimeMillis();

		iceburg = new ImportedModel("iceblock.obj");
		frame = new ImportedModel("frame_side.obj");
		string = new ImportedModel("string.obj");

		renderingProgramShadow = Utils.createShaderProgram("code/vertShader_shadow.glsl", "code/fragShader_shadow.glsl");
		renderingProgramRefl = Utils.createShaderProgram("code/vertShader_refl.glsl", "code/fragShader_refl.glsl");
		renderingProgramCubeMap = Utils.createShaderProgram("code/vertShader_CubeMap.glsl", "code/fragShader_CubeMap.glsl");
		renderingProgramTex = Utils.createShaderProgram("code/vertShader_tex.glsl", "code/fragShader_tex.glsl");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		
		setupVertices();
		setupShadowBuffers();

		b.set(
				0.5f, 0.0f, 0.0f, 0.0f,
				0.0f, 0.5f, 0.0f, 0.0f,
				0.0f, 0.0f, 0.5f, 0.0f,
				0.5f, 0.5f, 0.5f, 1.0f);

		cameraX = 0.0f; cameraY = -5.0f; cameraZ = 15.0f;
		
		skyboxTexture = Utils.loadCubeMap("cubeMap");
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

		snowTexture = Utils.loadTexture("snow.jpg");
		firstPass = true;
	}

	private void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();

		gl.glGenFramebuffers(1, shadowBuffer, 0);

		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
				scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	private void setupVertices()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
	
		setUpCube(gl);
		setUpSphere(gl);
		setupIceberg(gl);
		setupFrames(gl);
		setupStrings(gl);
	}

	public void setUpCube(GL4 gl){
		float[] cubeVertexPositions = shapes.getCubeVertexPositions();

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cvertBuf = Buffers.newDirectFloatBuffer(cubeVertexPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cvertBuf.limit()* 4L, cvertBuf, GL_STATIC_DRAW);
	}

	public void drawSkyBox(GL4 gl){

		gl.glUseProgram(renderingProgramCubeMap);

		vLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "p_matrix");

		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
	}

	public void setUpSphere(GL4 gl){

		numSphereVerts = sphere.getIndices().length;

		Vector3f[] vert = sphere.getVertices();
		Vector3f[] norm = sphere.getNormals();
		int[] indices = sphere.getIndices();

		float[] pvalues = new float[indices.length*3];
		float[] nvalues = new float[indices.length*3];

		for (int i=0; i<indices.length; i++)
		{	pvalues[i*3] = (vert[indices[i]]).x;
			pvalues[i*3+1] = (vert[indices[i]]).y;
			pvalues[i*3+2] = (vert[indices[i]]).z;
			nvalues[i*3] = (norm[indices[i]]).x;
			nvalues[i*3+1]= (norm[indices[i]]).y;
			nvalues[i*3+2]=(norm[indices[i]]).z;
		}

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()* 4L, vertBuf, GL_STATIC_DRAW);


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()* 4L,norBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
		IntBuffer idxBuf = Buffers.newDirectIntBuffer(indices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit()* 4L, idxBuf, GL_STATIC_DRAW);
	}

	public void shadowPass(GL4 gl, int vbo_index){

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mvStack);
		sLoc = gl.glGetUniformLocation(renderingProgramShadow, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vbo_index]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		if (firstPass) {
			gl.glClear(GL_DEPTH_BUFFER_BIT);
			firstPass = false;
		}
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

	}

	public void drawSphere(GL4 gl, int pass){

		if (pass == 1){
			shadowPass(gl, 1);
		}
		else {
			gl.glUseProgram(renderingProgramRefl);

			installLights(gl,renderingProgramRefl);

			mvLoc = gl.glGetUniformLocation(renderingProgramRefl, "mv_matrix");
			pLoc = gl.glGetUniformLocation(renderingProgramRefl, "p_matrix");
			nLoc = gl.glGetUniformLocation(renderingProgramRefl, "norm_matrix");

			mMat.identity();
			mMat.mul(mvStack);
			mMat.invert(invTrMat);
			invTrMat.transpose(invTrMat);

			gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
			gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
			gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
			gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);

			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

			gl.glClear(GL_DEPTH_BUFFER_BIT);
			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(GL_CCW);
			gl.glDepthFunc(GL_LEQUAL);
		}
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);

	}

	private void setupIceberg(GL4 gl)
	{
		numObjVertices = iceburg.getNumVertices();
		Vector3f[] vertices = iceburg.getVertices();
		Vector2f[] texCoords = iceburg.getTexCoords();
		Vector3f[] normals = iceburg.getNormals();

		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];

		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (vertices[i]).x();
			pvalues[i*3+1] = (vertices[i]).y();
			pvalues[i*3+2] = (vertices[i]).z();
			tvalues[i*2]   = (texCoords[i]).x();
			tvalues[i*2+1] = (texCoords[i]).y();
			nvalues[i*3]   = (normals[i]).x();
			nvalues[i*3+1] = (normals[i]).y();
			nvalues[i*3+2] = (normals[i]).z();
		}

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()* 4L, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()* 4L, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()* 4L,norBuf, GL_STATIC_DRAW);
	}

	public void drawIceberg(GL4 gl, int pass){
		if (pass==1){
			//shadowPass(gl,4);
		}
		else {
			gl.glUseProgram(renderingProgramTex);
			installLights(gl, renderingProgramTex);

			mvLoc = gl.glGetUniformLocation(renderingProgramTex, "mv_matrix");
			pLoc = gl.glGetUniformLocation(renderingProgramTex, "p_matrix");
			nLoc = gl.glGetUniformLocation(renderingProgramTex, "norm_matrix");
			sLoc = gl.glGetUniformLocation(renderingProgramTex, "shadowMVP");

			mMat.identity();
			mMat.mul(mvStack);
			mvMat.identity();
			mvMat.mul(vMat);
			mvMat.mul(mMat);

			mMat.invert(invTrMat);
			invTrMat.transpose(invTrMat);

			shadowMVP2.identity();
			shadowMVP2.mul(b);
			shadowMVP2.mul(lightPmat);
			shadowMVP2.mul(lightVmat);
			shadowMVP2.mul(mvStack);


			gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
			gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
			gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
			gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
			gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2);

			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, snowTexture);

			gl.glActiveTexture(GL_TEXTURE1);
			gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);

			gl.glEnable(GL_DEPTH_TEST);
			gl.glClear(GL_DEPTH_BUFFER_BIT);
			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(GL_CCW);
		}
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, iceburg.getNumVertices());

	}

	private void setupFrames(GL4 gl)
	{
		numObjVertices = frame.getNumVertices();
		Vector3f[] vertices = frame.getVertices();
		Vector2f[] texCoords = frame.getTexCoords();
		Vector3f[] normals = frame.getNormals();

		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];

		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   =  (vertices[i]).x();
			pvalues[i*3+1] = (vertices[i]).y();
			pvalues[i*3+2] = (vertices[i]).z();
			tvalues[i*2]   = (texCoords[i]).x();
			tvalues[i*2+1] = (texCoords[i]).y();
			nvalues[i*3]   = (normals[i]).x();
			nvalues[i*3+1] = (normals[i]).y();
			nvalues[i*3+2] = (normals[i]).z();
		}

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()* 4L, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()* 4L, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()* 4L,norBuf, GL_STATIC_DRAW);
	}

	public void drawFrame(GL4 gl, int pass){
		if(pass == 1){
			shadowPass(gl,7);
		}
		else {
			gl.glUseProgram(renderingProgramTex);
			installLights(gl, renderingProgramTex);

			mvLoc = gl.glGetUniformLocation(renderingProgramTex, "mv_matrix");
			pLoc = gl.glGetUniformLocation(renderingProgramTex, "p_matrix");
			nLoc = gl.glGetUniformLocation(renderingProgramTex, "norm_matrix");

			mMat.identity();
			mMat.mul(mvStack);
			mvMat.identity();
			mvMat.mul(vMat);
			mvMat.mul(mMat);

			mMat.invert(invTrMat);
			invTrMat.transpose(invTrMat);

			gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
			gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
			gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
			gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2);

			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, snowTexture);

			gl.glEnable(GL_DEPTH_TEST);
		}
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, frame.getNumVertices());
	}

	private void setupStrings(GL4 gl)
	{
		numObjVertices = string.getNumVertices();
		Vector3f[] vertices = string.getVertices();
		Vector2f[] texCoords = string.getTexCoords();
		Vector3f[] normals = string.getNormals();

		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];

		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   =  (vertices[i]).x();
			pvalues[i*3+1] = (vertices[i]).y();
			pvalues[i*3+2] = (vertices[i]).z();
			tvalues[i*2]   = (texCoords[i]).x();
			tvalues[i*2+1] = (texCoords[i]).y();
			nvalues[i*3]   = (normals[i]).x();
			nvalues[i*3+1] = (normals[i]).y();
			nvalues[i*3+2] = (normals[i]).z();
		}

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()* 4L, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()* 4L, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()* 4L,norBuf, GL_STATIC_DRAW);
	}

	public void drawString(GL4 gl, int pass){
		if (pass == 1) {
			shadowPass(gl, 10);
		}
		else {
			gl.glUseProgram(renderingProgramTex);
			installLights(gl, renderingProgramTex);

			mvLoc = gl.glGetUniformLocation(renderingProgramTex, "mv_matrix");
			pLoc = gl.glGetUniformLocation(renderingProgramTex, "p_matrix");
			nLoc = gl.glGetUniformLocation(renderingProgramTex, "norm_matrix");

			mMat.identity();
			mMat.mul(mvStack);
			mvMat.identity();
			mvMat.mul(vMat);
			mvMat.mul(mMat);

			mMat.invert(invTrMat);
			invTrMat.transpose(invTrMat);

			gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
			gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
			gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
			gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2);

			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, snowTexture);

			gl.glEnable(GL_DEPTH_TEST);
		}
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, string.getNumVertices());
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode()==KeyEvent.VK_RIGHT){
			roty-= 0.1f;
		}
		if (e.getKeyCode()==KeyEvent.VK_LEFT){
			roty+= 0.1f;
		}
		if (e.getKeyCode()==KeyEvent.VK_A){
			angle+= 15;
		}
		if (e.getKeyCode()==KeyEvent.VK_D){
			angle-= 15;
		}
		if (e.getKeyCode()==KeyEvent.VK_UP){
			rotx-= 0.1f;
		}
		if (e.getKeyCode()==KeyEvent.VK_DOWN){
			rotx+= 0.1f;
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			for (int i = 0; i<6; i++){
				start[i] = startList[i] == 1;
				startList[i]=0;
			}
		}
		if (e.getKeyCode()==KeyEvent.VK_NUMPAD1){
			raise[1] = true;
		}

		if (e.getKeyCode()==KeyEvent.VK_NUMPAD2){
			raise[1] = true;
			raise[2]=true;
		}

		if(e.getKeyCode()==KeyEvent.VK_NUMPAD4){
			raise[4]=true;
			raise[5]=true;
		}

		if (e.getKeyCode()==KeyEvent.VK_NUMPAD5){
			raise[5]=true;
		}

		// Key Z Zoom In
		if (e.getKeyCode()==KeyEvent.VK_Z){
			cameraZ = Math.max(0.1f, cameraZ - 0.1f);
		}
		// Key X Zoom Out
		if (e.getKeyCode()==KeyEvent.VK_X){
			cameraZ = cameraZ + 0.1f;
		}
	}


	public void keyReleased(KeyEvent e) {
		//System.out.println("keyReleased");
	}

	public void keyTyped(KeyEvent e) {
		// System.out.println("keyTyped");
	}

	public static void main(String[] args) { new Code(); }
	public void dispose(GLAutoDrawable drawable) {}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupShadowBuffers();
	}
}