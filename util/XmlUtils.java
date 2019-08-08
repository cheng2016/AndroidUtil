import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author chengzj
 * @date 2019/8/8
 */
public class XmlUtil {
    public static final String TAG = XmlUtil.class.getSimpleName();

    public static String readLocalXmlConfig(String path) {
        String imei = "";
        final File file = new File(path);
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fis, "UTF-8");
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            String tagName = parser.getName();
                            if (tagName.equals("imei")) {
                                imei = parser.nextText();
                                Logger.i(TAG, "readLocalXmlConfig 读取成功, imei : " + imei);
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                fis.close();
            } catch (Exception e) {
                Logger.e(TAG, "readLocalXmlConfig 读取失败 ", e);
            }
        }
        return imei;
    }

    public static void wirteXmlConfigToSdcard( String path, String imei) {
        File file = new File(path);
//        if (!file.exists()) {
            if (FileUtils.createOrExistsFile(path)) {
                try {
                    XmlSerializer serializer = Xml.newSerializer();
                    FileOutputStream fos = new FileOutputStream(file);
                    serializer.setOutput(fos, "utf-8");
                    // 设置文件头
                    serializer.startDocument("utf-8", true);
                    serializer.startTag(null, "config");
                    serializer.startTag(null, "imei");
                    serializer.text(imei);
                    serializer.endTag(null, "imei");
                    serializer.endTag(null, "config");
                    serializer.endDocument();
                    fos.close();
                    Logger.i(TAG, "wirteXmlConfigToSdcard 写入成功");
                } catch (IOException e) {
                    Logger.e(TAG, "wirteXmlConfigToSdcard 写入失败  ", e);
                }
            }
//        }
    }

    /**
     * 判断权限集合
     * permissions 权限数组
     * return true-表示没有权限  false-表示权限已开启
     */
    public static boolean lacksPermissions(Context mContexts, String... args) {
        for (String permission : args) {
            if (lacksPermission(mContexts, permission)) {
                return true;
            }
        }
        return false;
    } 
                
    /**
     * 判断是否缺少权限
     */
    private static boolean lacksPermission(Context context, String permission) {
        return checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_DENIED;
    }

    /**
     * v4 包的方法，抠出来的
     *
     * @param context
     * @param permission
     * @return
     */
    public static int checkSelfPermission(Context context, String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }
        return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
    }         
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
