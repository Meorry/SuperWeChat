package cn.ucai.superwechat.utils;

import android.widget.Toast;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;

/**
 * Created by clawpo on 16/9/20.
 */
public class CommonUtils {
    public static void showLongToast(String msg){
        Toast.makeText(SuperWeChatApplication.getInstance(),msg,Toast.LENGTH_LONG).show();
    }
    public static void showShortToast(String msg){
        Toast.makeText(SuperWeChatApplication.getInstance(),msg,Toast.LENGTH_SHORT).show();
    }
    public static void showLongToast(int rId){
        showLongToast(SuperWeChatApplication.getInstance().getString(rId));
    }
    public static void showShortToast(int rId){
        showShortToast(SuperWeChatApplication.getInstance().getString(rId));
    }
    public static void showMsgShortToast(int magId){
        if (magId>0){
            showShortToast(SuperWeChatApplication.getInstance().getResources().getIdentifier(I.MSG_PREFIX_MSG+magId,"string",SuperWeChatApplication.getInstance().getPackageName()));
        }else {
            showShortToast(R.string.msg_1);
        }
    }
}
