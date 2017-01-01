package com.timerecord;

/**
 * 图像处理类，负责链接IPC，获取、处理、存储图像。
 * @author Hugh
 * @Time 2016/12/12
 */

import android.app.Activity;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.view.SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;


/**
 *图像处理类
 * @author Hugh
 * @Time 2016/12/30
 */
public class ImagePro implements SurfaceHolder.Callback {
    static int JPEGCOMPRESS = 80;
    private Camera mCamera;// Camera对象
    Context m_context;
    private SurfaceView m_SurfaceView;// 显示图像的surfaceView
    private SurfaceHolder m_holder;// SurfaceView的控制器
   // private String strCaptureFilePath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";// 保存图像的路径
    private String m_sCaptureFilePath ;             // 保存图像的路径
    private AutoFocusCallback m_AutoFocusCallback;  // AutoFocusCallback自动对焦的回调对象
    WindowManager m_WindowManager;

    public ImagePro(Context context)
    {
        m_context = context;
        m_sCaptureFilePath = m_context.getExternalFilesDir(DIRECTORY_PICTURES).toString();
        m_AutoFocusCallback  = new AutoFocusCallback();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceholder)
    {
        L.d("surfaceCreated in");
       // mCamera.startPreview();//开始预览，这步操作很重要
        try
        {

        mCamera.setPreviewDisplay(m_holder);//设置显示面板控制器
        previewCallBack pre = new previewCallBack();//建立预览回调对象
        mCamera.setPreviewCallback(pre); //设置预览回调对象
        mCamera.getParameters().setPreviewFormat(ImageFormat.JPEG);
        mCamera.startPreview();//开始预览，这步操作很重要

        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }



    // 销毁面板时的方法
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceholder) {

    }
    // 在面板改变的时候调用的方法
    @Override
    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {
    }

    private int InitCamera()
    {
        // mSurfaceCallback = new SurfaceHoler.callBack(){    public void surfaceCreated(SurfaceHoler holder){       initCamera();     }};

        m_SurfaceView = new SurfaceView(m_context);
        m_holder = m_SurfaceView.getHolder();
        // m_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//获取相机缓存数据
        m_holder.addCallback(this);//mSurfaceCallback
        m_WindowManager = (WindowManager) m_context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.UNKNOWN);
        m_WindowManager.addView(m_SurfaceView, params);

        int rt = 0;
        mCamera = null;
        if (checkCameraHardware(m_context)) {
            L.e("ImagePro", "camera dos'nt exist");// 验证摄像头是否存在
            return -1;
        }

        try {
            // mCamera = Camera.open(0);//打开相机；在低版本里，只有open（）方法；高级版本加入此方法的意义是具有打开多个
            mCamera = Camera.open(1);//打开相机；在低版本里，只有open（）方法；高级版本加入此方法的意义是具有打开多个
            //摄像机的能力，其中输入参数为摄像机的编号
            //在manifest中设定的最小版本会影响这里方法的调用，如果最小版本设定有误（版本过低），在ide里将不允许调用有参的
            //open方法;
            //如果模拟器版本较高的话，无参的open方法将会获得null值!所以尽量使用通用版本的模拟器和API；
        } catch (Exception e) {
            L.e("ImagePro", "camera busy or need to get camera permission");
            rt = -1;
            e.printStackTrace();
        }
        if (mCamera == null) {
            L.e("ImagePro", "mCamera was null, Camera.Open failed");
            rt = -1;
        }
        if (rt != 0) {
            return rt;
        }


        try {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> lst = parameters.getSupportedPictureSizes();
            for (int i = 0; i < lst.size(); i++) {
                System.out.println(lst.get(i).width + "   " + lst.get(i).height);
            }
            // parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setPictureSize(lst.get(3).width, lst.get(3).height);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rt;
    }


    public int TakePicture()
    {
        if(InitCamera() != 0 )
        {
            return  -1;
        }
        new Thread(new Runnable()
        {
            public void run()
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mCamera.autoFocus(m_AutoFocusCallback);// 调用mCamera的
            }
        }).start();
        return 0;
    }
    private static Bitmap scaleWithWH(Bitmap src, double w, double h) {
        if (w == 0 || h == 0 || src == null) {
            return src;
        } else {
            // 记录src的宽高
            int width = src.getWidth();
            int height = src.getHeight();
            // 创建一个matrix容器
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float scaleWidth = (float) (w / width);
            float scaleHeight = (float) (h / height);
            // 开始缩放
            matrix.postScale(scaleWidth, scaleHeight);
            // 创建缩放后的图片
            return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
        }
    }
//
//    private Bitmap drawTextToBitmap(Context gContext, int gResId, String gText) {
//        Resources resources = gContext.getResources();
//        float scale = resources.getDisplayMetrics().density;
//        Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);
//        bitmap = scaleWithWH(bitmap, 300*scale, 300*scale);
//        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
//        // set default bitmap config if none
//        if(bitmapConfig == null) {
//            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
//        }
//        // resource bitmaps are imutable,
//        // so we need to convert it to mutable one
//        bitmap = bitmap.copy(bitmapConfig, true);
//        Canvas canvas = new Canvas(bitmap);
//        // new antialised Paint
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        // text color - #3D3D3D
//        paint.setColor(Color.RED);
//        paint.setTextSize((int) (18 * scale));
//        paint.setDither(true); //获取跟清晰的图像采样
//        paint.setFilterBitmap(true);//过滤一些
//        Rect bounds = new Rect();
//        paint.getTextBounds(gText, 0, gText.length(), bounds);
//        int x = 30;
//        int y = 30;
//        canvas.drawText(gText, x * scale, y * scale, paint);
//        return bitmap;
//    }

    private Bitmap drawTextToBitmap(Context gContext, Bitmap bitmap ,int gResId, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        //Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);
       // bitmap = scaleWithWH(bitmap, 300*scale, 300*scale);
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.RED);
        paint.setTextSize((int) (18 * scale));
        paint.setDither(true); //获取跟清晰的图像采样
        paint.setFilterBitmap(true);//过滤一些
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = 30;
        int y = 30;
        canvas.drawText(gText, x * scale, y * scale, paint);
        return bitmap;
    }



    /* 拍照的method */
    private void shot() {
        if (mCamera != null) {
            L.v("============", "shot IN");
            mCamera.takePicture(null, null, jpegCallback);
        }
    }
    /* 自定义class AutoFocusCallback */
    private final class AutoFocusCallback implements
            android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, Camera camera) {
            L.v("============", "onAutoFocus IN");
            /* 对到焦点拍照 */
            if (focused) {
                shot();
            }
        }
    };
    //在takepicture中调用的回调方法之一，接收jpeg格式的图像
    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {

            L.i("============", "jpegCallback IN");
            // Log.w("============", _data[55] + "");
            try {
                /* 取得相片 */
                Bitmap bm = BitmapFactory.decodeByteArray(_data, 0, _data.length);
                /* 创建文件 */
                File myCaptureFile = new File(m_sCaptureFilePath, "1.jpg"); //TODO 图片文件名命名
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(myCaptureFile));
                /* 采用压缩转档方法 */
                //bm.compress(Bitmap.CompressFormat.JPEG, JPEGCOMPRESS, bos);
                Bitmap bmNew = drawTextToBitmap(m_context, bm, 0, "clek2323   中文");
                /* 采用压缩转档方法 */
                bmNew.compress(Bitmap.CompressFormat.JPEG, JPEGCOMPRESS, bos);

                /* 调用flush()方法，更新BufferStream */
                bos.flush();
                /* 结束OutputStream */
                bos.close();
                /* 让相片显示3秒后圳重设相机 */
                // Thread.sleep(2000);
                /* 重新设定Camera */
                L.v("ImagePro", "   bos.close() OUT");
                StopCamera();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // 检测摄像头是否存在的私有方法
    private boolean checkCameraHardware(Context context)
        {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // 摄像头存在
            return false;
        } else {
            // 摄像头不存在
            return true;
        }
    }

    // 每次cam采集到新图像时调用的回调方法，前提是必须开启预览
    class previewCallBack implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
           // System.out.println("previewCallBack in");
            // Log.w("wwwwwwwww", data[5] + "");
            // Log.w("支持格式", mCamera.getParameters().getPreviewFormat()+"");
          //  decodeToBitMap(data, camera);
        }
    }


    private void decodeToBitMap(byte[] data, Camera _camera) {
        Camera.Size size = mCamera.getParameters().getPreviewSize();
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
                    size.height, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, size.width, size.height),
                        80, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(
                        stream.toByteArray(), 0, stream.size());
                //  Log.w("wwwwwwwww", bmp.getWidth() + " " + bmp.getHeight());
//                Log.w("wwwwwwwww",
//                        (bmp.getPixel(100, 100) & 0xff) + "  "
//                                + ((bmp.getPixel(100, 100) >> 8) & 0xff) + "  "
//                                + ((bmp.getPixel(100, 100) >> 16) & 0xff));

                stream.close();
            }
        } catch (Exception ex) {
            L.e("Sys", "Error:" + ex.getMessage());
        }
    }

    /* 停止相机的method */
    private void StopCamera() {
        if (mCamera != null)
        {
            try {
                /* 停止预览 */
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera.release();
            mCamera = null;
            m_WindowManager.removeView(m_SurfaceView);
        }
    }
}
