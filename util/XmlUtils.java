import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.util.Xml;

import com.vtech.app.App;

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
public class XmlUtils {
    public static final String TAG = XmlUtils.class.getSimpleName();

    private static String defaultPath;

    static {
        if (isSDCardOK()) {
            defaultPath = Environment.getExternalStorageDirectory() + "/wyd/config/";
        } else {
            defaultPath = App.getInstance().getCacheDir().getAbsolutePath() + "/wyd/config/";
        }
        defaultPath += "config.xml";
    }

    public static String readLocalXmlConfig() {
        return readLocalXmlConfig(defaultPath);
    }

    public static String readLocalXmlConfig(String path) {
        long start = System.currentTimeMillis();
        String token = "";
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
                            if (tagName.equals("token")) {
                                token = parser.nextText();
                                Logger.i(TAG, "readLocalXmlConfig 读取成功, token : " + token);
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
        long end = System.currentTimeMillis();
        Logger.e(TAG, "readLocalXmlConfig time :" + (end - start));
        return token;
    }


    public static void wirteXmlConfig(final String token) {
        wirteXmlConfigToSdcard(defaultPath, token);
    }

    public static void wirteXmlConfigToSdcard(String path, String token) {
        long start = System.currentTimeMillis();
        File file = new File(path);
        if (createOrExistsFile(path)) {
            try {
                XmlSerializer serializer = Xml.newSerializer();
                FileOutputStream fos = new FileOutputStream(file);
                serializer.setOutput(fos, "utf-8");
                // 设置文件头
                serializer.startDocument("utf-8", true);
                serializer.startTag(null, "config");
                serializer.startTag(null, "token");
                serializer.text(token);
                serializer.endTag(null, "token");
                serializer.endTag(null, "config");
                serializer.endDocument();
                fos.close();
                Logger.i(TAG, "wirteXmlConfigToSdcard 写入成功 ");
            } catch (IOException e) {
                Logger.e(TAG, "wirteXmlConfigToSdcard 写入失败  ", e);
            }
        }
        long end = System.currentTimeMillis();
        Logger.e(TAG, "wirteXmlConfigToSdcard time :" + (end - start));
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

    /**
     * 没有则创建，有则创建新文件
     * @param fullPath
     * @return
     */
    private static boolean createOrExistsFile(String fullPath) {
        File file = new File(fullPath);
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 是否存在sd卡
     * @return
     */
    public static boolean isSDCardOK() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            return false;
        } else {
            return true;
        }
    }
    
    
    
  // 存储xml中的键值对
    private static HashMap<String, String> confMap;

    // 读取配置文件并读入至hashmap中
    private static void initReadConfig(Context context, String xmlFileName) {
        try {
            //创建Android的factory实例
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //创建builder实例
            DocumentBuilder builder = factory.newDocumentBuilder();
            //将字节输入流解析为Document对象
            Document document = builder.parse(context.getAssets().open(TextUtils.isEmpty(xmlFileName) ? "topon.xml" : xmlFileName));
            //取得文档的根节点元素及其内容,即:<languages>内容</languages>
            Element root = document.getDocumentElement();
            //根据标签名取得相应的元素节点及其内容，由于标签可能不止一个，返回一个节点列表对象
            NodeList nodeList = root.getElementsByTagName("config");
            int confNum = nodeList.getLength();
            confMap = new HashMap<>();
            for (int i = 0; i < confNum; i++) {
                Element ele = (Element) nodeList.item(i);
                if (!ele.getAttribute("key").equals("")) {
                    confMap.put(ele.getAttribute("key"),
                            ele.getAttribute("value"));
                }
                Log.i(TAG, " key : " + ele.getAttribute("key") + " , value : " + ele.getAttribute("value"));
            }
        } catch (Exception e) {
            System.out.println("init topon.xml failure!");
        }
    }

    // 通过类中的该方法获取hashmap中的值
    public static String getConfigValue(String key, String defaultValue) {
        Log.i(TAG, "getConfigValue key : " + key + " , defaultValue : " + defaultValue);
        if (confMap == null) {
            return defaultValue;
        } else {
            String result = confMap.get(key);
            if (result != null && !result.equals("")) {
                return result;
            }
        }
        return defaultValue;
    }
}
