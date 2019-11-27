package com.hyfun.camera.p2v;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

class Util {

    private static final String TAG = "FunCamera::";

    public static final void log(String message) {
        Log.d(TAG, message);
    }

    public static final void e(Throwable e) {
        Log.e(TAG, e.getMessage(), e);
    }


    interface Const {
        int 类型_照片 = 0;
        int 类型_视频 = 1;
    }


    /**
     * 设置内容全屏,即内容延伸至状态栏底部,状态栏文字还在
     *
     * @param activity
     */
    public static void setFullScreen(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();

            // 适配刘海屏幕
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }

            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            //Activity全屏显示，但导航栏不会被隐藏覆盖，导航栏依然可见，Activity底部布局部分会被导航栏遮住。
            uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

            // 隐藏状态栏
            uiFlags |= View.INVISIBLE;
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


            window.getDecorView().setSystemUiVisibility(uiFlags);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置透明状态栏,这样才能让 ContentView 向上
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 生成文件名称
     *
     * @return
     */
    public static String randomName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return dateFormat.format(new Date());
    }

    /**
     * 通知系统相册更新了
     */
    public static final void notifyAlbumDataChanged(Context context, File file) {
        //通知相册更新
        MediaStore.Images.Media.insertImage(context.getApplicationContext().getContentResolver(), BitmapFactory.decodeFile(file.getAbsolutePath()), file.getName(), null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.getApplicationContext().sendBroadcast(intent);
    }


    /**
     * 解决录像时清晰度问题
     * <p>
     * 视频清晰度顺序 High 1080 720 480 cif qvga gcif 详情请查看 CamcorderProfile.java
     * 在12秒mp4格式视频大小维持在1M左右时,以下四个选择效果最佳
     * <p>
     * 不同的CamcorderProfile.QUALITY_ 代表每帧画面的清晰度,
     * 变换 profile.videoBitRate 可减少每秒钟帧数
     *
     * @param cameraID 前摄 Camera.CameraInfo.CAMERA_FACING_FRONT /后摄 Camera.CameraInfo.CAMERA_FACING_BACK
     * @return
     */
    public static CamcorderProfile getBestCamcorderProfile(int cameraID) {
        CamcorderProfile profile = CamcorderProfile.get(cameraID, CamcorderProfile.QUALITY_LOW);
        if (CamcorderProfile.hasProfile(cameraID, CamcorderProfile.QUALITY_720P)) {
            //对比上面480 这个选择 动作大时马赛克!!
            profile = CamcorderProfile.get(cameraID, CamcorderProfile.QUALITY_720P);
            profile.videoBitRate = profile.videoBitRate / 10;
            return profile;
        }
        if (CamcorderProfile.hasProfile(cameraID, CamcorderProfile.QUALITY_480P)) {
            //对比下面720 这个选择 每帧不是很清晰
            profile = CamcorderProfile.get(cameraID, CamcorderProfile.QUALITY_480P);
            profile.videoBitRate = profile.videoBitRate / 2;
            return profile;
        }
        if (CamcorderProfile.hasProfile(cameraID, CamcorderProfile.QUALITY_CIF)) {
            profile = CamcorderProfile.get(cameraID, CamcorderProfile.QUALITY_CIF);
            return profile;
        }
        if (CamcorderProfile.hasProfile(cameraID, CamcorderProfile.QUALITY_QVGA)) {
            profile = CamcorderProfile.get(cameraID, CamcorderProfile.QUALITY_QVGA);
            return profile;
        }
        return profile;
    }
}