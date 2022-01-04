package com.hyfun.camera.p2v;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyfun.camera.FunCamera;
import com.hyfun.camera.R;
import com.hyfun.camera.widget.AutoFitTextureView;
import com.hyfun.camera.widget.CaptureButton;
import com.hyfun.camera.widget.CaptureTimeView;

/**
 * Created by HyFun on 2019/10/14.
 * Email: 775183940@qq.com
 * Description: 用于拍摄照片和视频的fragment,默认保存至DCMI文件夹中
 */
@SuppressLint("ValidFragment")
public class CameraCaptureRecordFragment extends BaseFragment implements OnCameraCaptureListener {


    private int mode; // 拍摄模式
    private long duration; // 拍摄时长

    private CameraOrientationListener cameraOrientationListener;


    //视图
    private AutoFitTextureView surfaceView;
    private View viewBack;
    private CaptureButton captureButton;
    private ImageView viewSplashMode, viewSwitch, viewFocusView;
    private TextView viewTextInfo;
    private View viewNavigation;

    private FrameLayout flCapture;
    private ImageView ivCaptureButton;
    private TextView tvCaptureTime;

    private CaptureTimeView captureTimeView;

    private ImageView ivFrame;

    public CameraCaptureRecordFragment(int mode, long duration) {
        this.mode = mode;
        this.duration = duration;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraOrientationListener = new CameraOrientationListener(getContext());
        cameraOrientationListener.enable();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.camera_fragment_capture_record, container, false);
        surfaceView = view.findViewById(R.id.camera_capture_record_surface_view);
        viewBack = view.findViewById(R.id.camera_capture_record_btn_back);
        captureButton = view.findViewById(R.id.camera_capture_record_capture_button);
        viewSplashMode = view.findViewById(R.id.camera_capture_record_iv_splash);
        viewSwitch = view.findViewById(R.id.camera_capture_record_iv_switch);
        viewFocusView = view.findViewById(R.id.camera_capture_record_focus_view);
        viewTextInfo = view.findViewById(R.id.camera_capture_record_tv_info);
        viewNavigation = view.findViewById(R.id.camera_capture_record_view_navigation);

        flCapture = view.findViewById(R.id.flCapture);
        ivCaptureButton = view.findViewById(R.id.ivCaptureButton);
        tvCaptureTime = view.findViewById(R.id.tvCaptureTime);
        captureTimeView = view.findViewById(R.id.captureTimeView);

        ivFrame = view.findViewById(R.id.ivFrame);

        // ——————————————————————————————————————初始化——————————————————————————————————————————
        final Capture capture = new Capture(surfaceView);
        capture.setOnCameraCaptureListener(this);

        captureButton.setMode(mode);
        captureButton.setDuration(duration);
        if (mode == CaptureButton.Mode.MODE_CAPTURE) {
            viewTextInfo.setText("轻触拍照");

            if (FunCamera.MASK_MODE == FunCamera.MASK_MODE_NONE || FunCamera.MASK_MODE == FunCamera.MASK_MODE_ONLY_VIDEO) {
                ivFrame.setVisibility(View.INVISIBLE);
            }
        } else if (mode == CaptureButton.Mode.MODE_RECORD) {
            viewTextInfo.setText("长按摄像");

            if (FunCamera.MASK_MODE == FunCamera.MASK_MODE_NONE || FunCamera.MASK_MODE == FunCamera.MASK_MODE_ONLY_PHOTO) {
                ivFrame.setVisibility(View.INVISIBLE);
            }
        } else if (mode == CaptureButton.Mode.MODE_CAPTURE_RECORD) {
            viewTextInfo.setText("轻触拍照 长按摄像");
        }
        // ——————————————————————————————————————点击事件——————————————————————————————————————————
        // 返回
        viewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 切换闪光灯
        viewSplashMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture.enableFlashLight();
            }
        });

        // 切换摄像头
        viewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture.switchCamera();
            }
        });

        // 点击拍摄
        captureButton.setOnProgressTouchListener(new CaptureButton.OnProgressTouchListener() {
            @Override
            public void onCapture() {
                capture.capturePhoto(cameraOrientationListener.getOrientation());
            }

            @Override
            public void onCaptureRecordStart() {
                capture.captureRecordStart(cameraOrientationListener.getOrientation());
            }

            @Override
            public void onCaptureRecordEnd() {
                capture.captureRecordEnd();
            }

            @Override
            public void onCaptureError(String message) {
                capture.captureRecordFailed();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

        });

        // 拍摄按钮
        flCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == CaptureButton.Mode.MODE_CAPTURE) {
                    capture.capturePhoto(cameraOrientationListener.getOrientation());
                } else {
                    changeRecordStatusAndUI(capture);
                }
            }
        });

        // 对焦
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        float X = event.getRawX();
                        float Y = event.getRawY();
                        Util.log("click    X：" + X + "    Y：" + Y);
                        capture.focus(X, Y);
                        break;
                }
                return true;
            }
        });

        return view;
    }

    /**
     * 是否正在拍摄
     */
    private boolean isCapturing = false;

    /**
     * 上次拍摄按钮点击的时间
     */
    private long lastChangeRecordStatusTime = 0;

    /**
     * 拍摄持续时间
     */
    private long captureDuration = 0;

    /**
     * 修改拍摄状态与UI
     */
    private void changeRecordStatusAndUI(Capture capture) {

        long nowTime = System.currentTimeMillis();
        if (nowTime - lastChangeRecordStatusTime < 1000) {
            return;
        }

        isCapturing = !isCapturing;
        if (isCapturing) {
            capture.captureRecordStart(cameraOrientationListener.getOrientation());
            ivCaptureButton.setImageResource(R.mipmap.icon_end_capture);

            captureTimeView.setVisibility(View.VISIBLE);
            captureTimeView.start();
        } else {
            capture.captureRecordEnd();
            ivCaptureButton.setImageResource(R.mipmap.icon_start_capture);

            captureTimeView.setVisibility(View.INVISIBLE);
        }

//        changeCaptureTime(isCapturing);

        lastChangeRecordStatusTime = System.currentTimeMillis();
    }

    /**
     * 更新拍摄时间
     *
     * @param isCapturing
     */
    private void changeCaptureTime(boolean isCapturing) {
        if (isCapturing) {
            tvCaptureTime.setVisibility(View.VISIBLE);
            captureDuration = 0;
            tvCaptureTime.setText(timeCalculate(captureDuration));
            captureTimeHandler.sendEmptyMessageDelayed(1, 1000);
        } else {
            tvCaptureTime.setVisibility(View.INVISIBLE);
            captureTimeHandler.removeCallbacks(null);
            captureTimeHandler.removeMessages(1);
        }
    }

    private Handler captureTimeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            captureDuration = captureDuration + 1;
            tvCaptureTime.setText(timeCalculate(captureDuration));
            captureTimeHandler.sendEmptyMessageDelayed(1, 1000);
        }
    };


    public String timeCalculate(long ttime) {
        long daysuuu, hoursuuu, minutesuuu, secondsuuu;
        String daysT = "", restT = "";
        daysuuu = (Math.round(ttime) / 86400);
        hoursuuu = (Math.round(ttime) / 3600) - (daysuuu * 24);
        minutesuuu = (Math.round(ttime) / 60) - (daysuuu * 1440) - (hoursuuu * 60);
        secondsuuu = Math.round(ttime) % 60;
        if (daysuuu == 1) daysT = String.format("%d day ", daysuuu);
        if (daysuuu > 1) daysT = String.format("%d days ", daysuuu);
        restT = String.format("%02d:%02d:%02d", hoursuuu, minutesuuu, secondsuuu);
        return daysT + restT;
    }


    @Override
    public void onResume() {
        super.onResume();
        Util.setFullScreen(getActivity());
        // 导航栏区域
        if (Util.isNavigationBarShow(getActivity())) {
            ViewGroup.LayoutParams layoutParams = viewNavigation.getLayoutParams();
            layoutParams.height = Util.getNavigationBarHeight(getActivity());
            viewNavigation.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onDestroy() {
        cameraOrientationListener.disable();
        cameraOrientationListener = null;
        super.onDestroy();
    }

    // —————————————————————————————————VIEW———————————————————————————————————————

    @Override
    public void onToggleSplash(String flashMode) {
        if (flashMode == null) {
            // 说明不支持闪光灯
            viewSplashMode.setVisibility(View.GONE);
            return;
        }
        viewSplashMode.setVisibility(View.VISIBLE);
        if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
            viewSplashMode.setImageResource(R.drawable.camera_ic_capture_flash_off_24dp);
        }

        if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
            viewSplashMode.setImageResource(R.drawable.camera_ic_camera_flash_auto_24dp);
        }

        if (flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
            viewSplashMode.setImageResource(R.drawable.camera_ic_capture_flash_on_24dp);
        }
    }

    @Override
    public void onFocusSuccess(float x, float y) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewFocusView.getLayoutParams();
        layoutParams.leftMargin = (int) x - Util.dip2px(getContext(), 35);
        layoutParams.topMargin = (int) y - Util.dip2px(getContext(), 35);
        viewFocusView.setLayoutParams(layoutParams);
        Util.scale(viewFocusView);
    }

    @Override
    public void onCapturePhoto(String photoPath) {
        // 拍照成功
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.camera_capture_main_framelayout,
                        new CameraCapturePreviewFragment(Util.Const.类型_照片, photoPath),
                        CameraCapturePreviewFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCaptureRecord(String filePath) {
        // 录像成功
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.camera_capture_main_framelayout,
                        new CameraCapturePreviewFragment(Util.Const.类型_视频, filePath),
                        CameraCapturePreviewFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onError(Throwable throwable) {
        Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
