package org.jerey.activity;

import android.Manifest;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cn.jerey.permissiontools.Callback.PermissionCallbacks;
import com.cn.jerey.permissiontools.PermissionTools;

import org.jerey.camera.CameraInterface;
import org.jerey.camera.CameraInterface.CamOpenOverCallback;
import org.jerey.camera.preview.CameraSurfaceView;
import org.jerey.playcamera.R;
import org.jerey.util.DisplayUtil;

import java.util.List;

public class CameraActivity extends Activity implements CamOpenOverCallback {
	private static final String TAG = "xiamin";
	private CameraSurfaceView surfaceView = null;
	private ImageButton shutterBtn;
	private PermissionTools mPerm;
	float previewRate = -1f;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
		shutterBtn.setOnClickListener(new BtnListeners());
	}

	@Override
	protected void onResume() {
		super.onResume();

		mPerm = new PermissionTools.Builder(this)
				.setRequestCode(111)
				.setOnPermissionCallbacks(new PermissionCallbacks() {
					@Override
					public void onPermissionsGranted(int requestCode, List<String> perms) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								CameraInterface.getInstance().doOpenCamera(CameraActivity.this);
							}
						}).start();
					}

					@Override
					public void onPermissionsDenied(int requestCode, List<String> perms) {
						Toast.makeText(CameraActivity.this,"权限被拒绝",Toast.LENGTH_SHORT).show();
						finish();
					}
				})
				.build();
		mPerm.requestPermissions(Manifest.permission.CAMERA,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	private void initUI(){
		surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
		shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
	}
	private void initViewParams(){
		LayoutParams params = surfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
		surfaceView.setLayoutParams(params);

		//手动设置拍照ImageButton的大小为120dip×120dip,原图片大小是64×64
		LayoutParams p2 = shutterBtn.getLayoutParams();
		p2.width = DisplayUtil.dip2px(this, 80);
		p2.height = DisplayUtil.dip2px(this, 80);;		
		shutterBtn.setLayoutParams(p2);	

	}

	@Override
	public void cameraHasOpened() {
		// TODO Auto-generated method stub
		SurfaceHolder holder = surfaceView.getSurfaceHolder();
		CameraInterface.getInstance().doStartPreview(holder, previewRate);
	}
	private class BtnListeners implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.btn_shutter:
				CameraInterface.getInstance().doTakePicture();
				break;
			default:break;
			}
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		CameraInterface.getInstance().doStopCamera();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		mPerm.onRequestPermissionsResult(requestCode,permissions,grantResults);
	}
}
