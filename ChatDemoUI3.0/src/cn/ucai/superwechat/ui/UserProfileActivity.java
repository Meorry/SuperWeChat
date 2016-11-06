package cn.ucai.superwechat.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

public class UserProfileActivity extends BaseActivity implements OnClickListener{
	private static final String TAG=UserProfileActivity.class.getSimpleName();

	private static final int REQUESTCODE_PICK = 1;
	private static final int REQUESTCODE_CUTTING = 2;
	@BindView(R.id.iv_userinfo_avatar)
	ImageView mIvUserinfoAvatar;
	@BindView(R.id.tv_userinfo_nick)
	TextView mTvUserinfoNick;
	@BindView(R.id.tv_userinfo_name)
	TextView mTvUserinfoName;
	@BindView(R.id.img_back)
	ImageView mImgBack;
	@BindView(R.id.txt_title)
	TextView mTxtTitle;
	private ProgressDialog dialog;
	private RelativeLayout rlNickName;

	User user = null;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.em_activity_user_profile);
		ButterKnife.bind(this);
		initView();
		initListener();
		user = EaseUserUtils.getCurrentAppUserInfo();
	}

	private void initView() {
		mImgBack.setVisibility(View.VISIBLE);
		mTxtTitle.setVisibility(View.VISIBLE);
		mTxtTitle.setText(getString(R.string.title_user_profile));
	}

	private void initListener() {
		EaseUserUtils.setCuentAppUserAvatar(this,mIvUserinfoAvatar);
		EaseUserUtils.setCuentAppUserNick(mTvUserinfoNick);
		EaseUserUtils.setCuentAppUserName(mTvUserinfoName);
	}

	public void asyncFetchUserInfo(String username) {
		SuperWeChatHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

			@Override
			public void onSuccess(EaseUser user) {
				if (user != null) {
					SuperWeChatHelper.getInstance().saveContact(user);
					if (isFinishing()) {
						return;
					}
					mTvUserinfoNick.setText(user.getNick());
					if (!TextUtils.isEmpty(user.getAvatar())) {
						Glide.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.default_hd_avatar).into(mIvUserinfoAvatar);
					} else {
						Glide.with(UserProfileActivity.this).load(R.drawable.default_hd_avatar).into(mIvUserinfoAvatar);
					}
				}
			}

			@Override
			public void onError(int error, String errorMsg) {
			}
		});
	}


	private void uploadHeadPhoto() {
		Builder builder = new Builder(this);
		builder.setTitle(R.string.dl_title_upload_photo);
		builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
							case 0:
								Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
										Toast.LENGTH_SHORT).show();
								break;
							case 1:
								Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
								pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
								startActivityForResult(pickIntent, REQUESTCODE_PICK);
								break;
							default:
								break;
						}
					}
				});
		builder.create().show();
	}


	private void updateRemoteNick(final String nickName) {
		dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean updatenick = SuperWeChatHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(nickName);
				if (UserProfileActivity.this.isFinishing()) {
					return;
				}
				if (!updatenick) {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
									.show();
							dialog.dismiss();
						}
					});
				} else {
					updateAppNick(nickName);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dialog.dismiss();
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_success), Toast.LENGTH_SHORT)
									.show();
							mTvUserinfoNick.setText(nickName);
						}
					});
				}
			}
		}).start();
	}

	private void updateAppNick(String nickName) {
		NetDao.updateNick(this, user.getMUserName(), nickName, new OkHttpUtils.OnCompleteListener<String>() {
			@Override
			public void onSuccess(String s) {
				if (s!=null){
					Result result= ResultUtils.getResultFromJson(s,User.class);
					L.e(TAG,"result"+result);
					if (result!=null&&result.isRetMsg()){
						User u = (User) result.getRetData();
						updateLocatUser(u);
					}else {
						Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
								.show();
						dialog.dismiss();
					}
				}else {
					Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
							.show();
					dialog.dismiss();
				}
			}

			@Override
			public void onError(String error) {
				L.e(TAG,"error"+error);
				Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
						.show();
				dialog.dismiss();
			}
		});
	}

	private void updateLocatUser(User u) {
		user = u;
		SuperWeChatHelper.getInstance().saveAppContact(u);
		EaseUserUtils.setCuentAppUserNick(mTvUserinfoNick);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUESTCODE_PICK:
				if (data == null || data.getData() == null) {
					return;
				}
				startPhotoZoom(data.getData());
				break;
			case REQUESTCODE_CUTTING:
				if (data != null) {
//					uodateAppUserAvatar(data);
//                    setPicToView(data);
				}
				break;
			default:
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", true);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		startActivityForResult(intent, REQUESTCODE_CUTTING);
	}

	/**
	 * save the picture data
	 *
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(getResources(), photo);
			mIvUserinfoAvatar.setImageDrawable(drawable);
			uploadUserAvatar(Bitmap2Bytes(photo));
		}

	}

	private void uploadUserAvatar(final byte[] data) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				final String avatarUrl = SuperWeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						if (avatarUrl != null) {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
									Toast.LENGTH_SHORT).show();
						}

					}
				});

			}
		}).start();

	}


	public byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	@OnClick({R.id.layout_userinfo_avatar,R.id.img_back, R.id.layout_userinfo_nick, R.id.layout_userinfo_name})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.layout_userinfo_avatar:
//                L.e(TAG,"user.getAvatar="+user.getAvatar());
//                String imagePath = EaseImageUtils.getImagePath(user.getMUserName());
//                L.e(TAG,"imagePath="+imagePath);
				uploadHeadPhoto();
				break;
			case R.id.img_back:
				MFGT.finish(this);
				break;
			case R.id.layout_userinfo_nick:
				final EditText editText = new EditText(this);
				editText.setText(user.getMUserNick());
				new Builder(this).setTitle(R.string.setting_nickname).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
						.setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								String nickString = editText.getText().toString().trim();
								if (TextUtils.isEmpty(nickString)) {
									Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
									return;
								}
								if (nickString.equals(user.getMUserNick())){
									CommonUtils.showLongToast(getString(R.string.toast_nick_not_modify));
									return;
								}
								updateRemoteNick(nickString);
							}
						}).setNegativeButton(R.string.dl_cancel, null).show();
				break;
			case R.id.layout_userinfo_name:
				CommonUtils.showLongToast(R.string.User_name_cannot_be_modify);
				break;

		}
	}
	public File saveBitmapFile (Intent picdata){
		Bundle extra= picdata.getExtras();
		if (extra != null){
			Bitmap bitmap = extra.getParcelable("data");
			String imagePath = EaseImageUtils.getImagePath(user.getMUserName()+ I.AVATAR_SUFFIX_JPG);
			File file = new File(imagePath);
			L.e("file path="+file.getAbsolutePath());
			try {
				BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
				bos.flush();
				bos.close();
			}catch (IOException e){
				e.printStackTrace();
			}
			return file;
		}
		return null;
	}

}
