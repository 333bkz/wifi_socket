package www.bkz.wifi;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

public class LocationUtil {

    /**
     * 判断定位服务是否开启
     *
     * @return true 表示开启
     */
    public static boolean isLocationEnabled(Context context) {
        //6.0 需要位置权限
        //8.0 需开启位置服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        return true;
    }
}
