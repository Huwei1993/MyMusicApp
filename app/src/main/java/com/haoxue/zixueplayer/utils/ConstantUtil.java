package com.haoxue.zixueplayer.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 *  常量工具类
 * @author wwj
 *
 */
public class ConstantUtil {


	/**
	 * 获取屏幕大小
	 * @param context
	 * @return
	 */
	public static int[] getScreen(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		return new int[] {(int) (outMetrics.density * outMetrics.widthPixels),
				(int)(outMetrics.density * outMetrics.heightPixels)
		};
	}

}
