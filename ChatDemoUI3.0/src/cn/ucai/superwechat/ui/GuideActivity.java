package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.MFGT;

public class GuideActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.iv_guide_register, R.id.iv_guide_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_guide_register:
                MFGT.gotoRegister(this);
                break;
            case R.id.iv_guide_login:
                MFGT.gotoLogin(this);
                break;
        }
    }
}
