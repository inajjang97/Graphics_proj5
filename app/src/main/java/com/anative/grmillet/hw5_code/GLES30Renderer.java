package com.anative.grmillet.hw5_code;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

public class GLES30Renderer implements GLSurfaceView.Renderer {

    private Context mContext;


    Camera mCamera;
    private Mario mMario;
    private Ina mIna;
    private Ironman mIronman;
    private Chair mChair;

    public float ratio = 1.0f;
    public int headLightFlag = 1;
    public int lampLightFlag = 1;
    public int pointLightFlag = 1;
    public int blindEffectFlag = 1;
    public int spotLightFlag = 1;
    public int textureFlag = 1;

    public float[] mMVPMatrix = new float[16];
    public float[] mProjectionMatrix = new float[16];
    public float[] mModelViewMatrix = new float[16];
    public float[] mModelMatrix = new float[16];
    public float[] mModelMatrix_iron = new float[16];
    public float[] mModelMatrix_iron2= new float[16];
    public float[] mViewMatrix = new float[16];
    public float[] mModelViewInvTrans = new float[16];

    final static int TEXTURE_ID_MARIO = 0;
    final static int TEXTURE_ID_INA = 1;
    final static int TEXTURE_ID_IRONMAN = 2;
    final static int TEXTURE_ID_CHAIR = 3;

    private ShadingProgram mShadingProgram;

    public GLES30Renderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES30.glClearColor(0.0f, 0.0f, 0.8f, 1.0f);

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // 초기 뷰 매트릭스를 설정.
        mCamera = new Camera();

        //vertex 정보를 할당할 때 사용할 변수.
        int nBytesPerVertex = 8 * 4;        // 3 for vertex, 3 for normal, 2 for texcoord, 4 is sizeof(float)
        int nBytesPerTriangles = nBytesPerVertex * 3;

        /*
            우리가 만든 ShadingProgram을 실제로 생성하는 부분
         */
        mShadingProgram = new ShadingProgram(
            AssetReader.readFromFile("vertexshader.vert" , mContext),
            AssetReader.readFromFile("fragmentshader.frag" , mContext));
        mShadingProgram.prepare();
        mShadingProgram.initLightsAndMaterial();
        mShadingProgram.initFlags();
        mShadingProgram.set_up_scene_lights(mViewMatrix,blindEffectFlag);

        /*
                우리가 만든 Object들을 로드.
         */
        mMario = new Mario();
        mMario.addGeometry(AssetReader.readGeometry("Mario_Triangle.geom", nBytesPerTriangles, mContext));
        mMario.prepare();
        mMario.setTexture(AssetReader.getBitmapFromFile("mario.jpg", mContext), TEXTURE_ID_MARIO);

        mIna = new Ina();
        mIna.addGeometry(AssetReader.readGeometry("Godzilla.geom", nBytesPerTriangles, mContext));
        mIna.prepare();
        mIna.setTexture(AssetReader.getBitmapFromFile("Ina.jpg", mContext), TEXTURE_ID_INA);


        mIronman = new Ironman();
        mIronman.addGeometry(AssetReader.readGeometry("IronMan.geom", nBytesPerTriangles, mContext));
        mIronman.prepare();
        mIronman.setTexture(AssetReader.getBitmapFromFile("Ina2.jpg", mContext), TEXTURE_ID_IRONMAN);


        mChair = new Chair();
        mChair.addGeometry(AssetReader.readGeometry("new_chair_vnt.geom", nBytesPerTriangles, mContext));
        mChair.prepare();
        mChair.setTexture(AssetReader.getBitmapFromFile("grass_tex.jpg", mContext), TEXTURE_ID_CHAIR);

    }

    @Override
    public void onDrawFrame(GL10 gl){ // 그리기 함수 ( = display )
        int pid;
        int timestamp = getTimeStamp();

        /*
             실시간으로 바뀌는 ViewMatrix의 정보를 가져온다.
             MVP 중 V 매트릭스.
         */
        mViewMatrix = mCamera.GetViewMatrix();
        /*
             fovy 변화를 감지하기 위해 PerspectiveMatrix의 정보를 가져온다.
             MVP 중 P
             mat, offset, fovy, ratio, near, far
         */
        Matrix.perspectiveM(mProjectionMatrix, 0, mCamera.getFovy(), ratio, 0.1f, 2000.0f);

        /*
              행렬 계산을 위해 이제 M만 계산하면 된다.
         */

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        mShadingProgram.set_up_scene_lights(mViewMatrix,blindEffectFlag);

        mShadingProgram.set_lights0(pointLightFlag,blindEffectFlag);
        mShadingProgram.set_lights1(headLightFlag,blindEffectFlag);
        mShadingProgram.set_lights2(lampLightFlag,blindEffectFlag);
        mShadingProgram.set_lights3(spotLightFlag,blindEffectFlag);

        /*
         그리기 영역.
         */
        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1f, 0f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 1f, 0f);
        Matrix.scaleM(mModelMatrix, 0, 1.0f, 1.0f, 1.0f);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 0.0f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mMario.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_MARIO);

        mShadingProgram.setUpMaterialMario();
        mMario.draw();

        //ina
        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.
        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.translateM(mModelMatrix, 0, 5.0f, 0.0f, -10.0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 1f, 0f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 1f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 0f, 1f);
        Matrix.scaleM(mModelMatrix, 0, 0.05f, 0.05f, 0.05f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mIna.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_INA);

        mShadingProgram.setUpMaterialIna();
        mIna.draw();

        int time=getTimeStamp();

        //ironman
        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.
        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.translateM(mModelMatrix, 0, -10.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 1f, 0f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 1f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 0f, 1f);
        Matrix.scaleM(mModelMatrix, 0, 4.0f, 4.0f, 4.0f);


        Matrix.rotateM(mModelMatrix, 0, time, 0f, 1f, 0f);
        Matrix.translateM(mModelMatrix, 0, 10.0f, 0.0f, 0.0f);
       Matrix.rotateM(mModelMatrix, 0, time*10, 0f, 1f, 0f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mIronman.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_IRONMAN);

        mShadingProgram.setUpMaterialIronman();
        mIronman.draw();
        mShadingProgram.set_up_scene_lights2(mModelViewMatrix, blindEffectFlag);


        //ironman2
        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.
        Matrix.setIdentityM(mModelMatrix_iron, 0);
        Matrix.multiplyMM(mModelMatrix_iron,0,mModelMatrix_iron,0,mModelMatrix,0);


        Matrix.rotateM(mModelMatrix_iron, 0, time*10, 0f, 1f, 0f);
        Matrix.translateM(mModelMatrix_iron, 0, -3.0f, 0.0f, 0.0f);
        Matrix.scaleM(mModelMatrix_iron, 0, 0.5f, 0.5f, 0.5f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix_iron, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mIronman.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_IRONMAN);

        mShadingProgram.setUpMaterialIronman();
        mIronman.draw();

        //ironman3
        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.
        Matrix.setIdentityM(mModelMatrix_iron2, 0);
        Matrix.multiplyMM(mModelMatrix_iron2,0,mModelMatrix_iron2,0,mModelMatrix,0);


        Matrix.translateM(mModelMatrix_iron2, 0, 0.0f, 2.0f, 0.0f);
        Matrix.rotateM(mModelMatrix_iron2, 0, time*10, 1f, 0f, 0f);
        Matrix.translateM(mModelMatrix_iron2, 0, 0.0f, 3.0f, 0.0f);
        Matrix.scaleM(mModelMatrix_iron2, 0, 0.5f, 0.5f, 0.5f);


        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix_iron2, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mIronman.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_IRONMAN);

        mShadingProgram.setUpMaterialIronman();
        mIronman.draw();

        //Chair
        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 10.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1f, 0f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 1f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 0f, 1f);
        Matrix.scaleM(mModelMatrix, 0, 0.5f, 0.5f, 0.5f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mChair.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_CHAIR);

        mShadingProgram.setUpMaterialChair();
        mChair.draw();

        //Chair
        mShadingProgram.use(); // 이 프로그램을 사용해 그림을 그릴 것입니다.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -5.0f, 300.0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 1f, 0f);
        Matrix.rotateM(mModelMatrix, 0, 180.0f, 0f, 0f, 1f);
        Matrix.scaleM(mModelMatrix, 0, 30f, 0.2f, 30.0f);

        Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0);
        Matrix.transposeM(mModelViewInvTrans, 0, mModelViewMatrix, 0);
        Matrix.invertM(mModelViewInvTrans, 0, mModelViewInvTrans, 0);

        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewProjectionMatrix, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrix, 1, false, mModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mShadingProgram.locModelViewMatrixInvTrans, 1, false, mModelViewInvTrans, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mChair.mTexId[0]);
        GLES30.glUniform1i(mShadingProgram.locTexture, TEXTURE_ID_CHAIR);

        mShadingProgram.setUpMaterialChair();
        mChair.draw();


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){
        GLES30.glViewport(0, 0, width, height);

        ratio = (float)width / height;

        Matrix.perspectiveM(mProjectionMatrix, 0, mCamera.getFovy(), ratio, 0.1f, 2000.0f);
    }

    static int prevTimeStamp = 0;
    static int currTimeStamp = 0;
    static int totalTimeStamp = 0;

    private int getTimeStamp(){
        Long tsLong = System.currentTimeMillis() / 100;

        currTimeStamp = tsLong.intValue();
        if(prevTimeStamp != 0){
            totalTimeStamp += (currTimeStamp - prevTimeStamp);
        }
        prevTimeStamp = currTimeStamp;

        return totalTimeStamp;
    }

    public void setLight1(){
        mShadingProgram.light[1].light_on = 1 - mShadingProgram.light[1].light_on;
    }

}