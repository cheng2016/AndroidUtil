import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class ResourceUtils {
    
	/**
	 * 纯粹反射获取资源id，比如getResource无法获取styleable类型的资源id和数组。采用这种方式获取
	 * @param context
	 * @param name
	 * @param type
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static Object getResourceId(Context context, String name, String type){
		String className = context.getPackageName()+".R";
		try{

			Class cls = Class.forName(className);
			for(Class child : cls.getClasses()){
				String sname = child.getSimpleName();
				if(sname.equals(type)){
					for(Field f : child.getFields()){
						String fname = f.getName();
						if(fname.equals(name)){
							return f.get(null);
						}
					}
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

    /**
     * android 获取Asset中Properties文件配置的键值对
     * @param context
     * @param key
     * @return
     */
    public static String getProperties(Context context,String key){
        String defaultValues = "";
        InputStream inputStream = null;
        try {
            AssetManager assetManager = context.getApplicationContext().getAssets();
            String confFile = "kuda.properties";
            inputStream = assetManager.open(confFile);
            Properties properties = new Properties();
            properties.load(new InputStreamReader(inputStream, "utf-8"));
            String value = properties.getProperty(key,defaultValues);
            inputStream.close();
            return value;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultValues;
    }
    

    /**
     * 读取assets中的文件
     *
     * @param fileName 文件名
     * @return
     */
    public static String getFileFromAssets(Context context, String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder("");
            InputStreamReader in = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            in.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 读取raw中的文件
     *
     * @param context
     * @param resid
     * @return
     */
    public static String getFileFromRaw(Context context, int resid) {
        InputStreamReader in = null;
        BufferedReader br = null;
        try {
            StringBuffer sb = new StringBuffer("");
            in = new InputStreamReader(context.getResources().openRawResource(resid));
            br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (br != null)
                    br.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    public static InputStream getAssert(Context context, String name) {
        try {
            InputStream inputStream = context.getAssets().open(name);
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	
    public static int getLayoutId(Context paramActivity, String id) {
        String packageName = paramActivity.getPackageName();
        return paramActivity.getResources().getIdentifier(id, "layout",
                packageName);
    }

    public static int getId(Context paramActivity, String id) {
        String packageName = paramActivity.getPackageName();
        return paramActivity.getResources()
                .getIdentifier(id, "id", packageName);
    }

    public static int getColorId(Context paramActivity, String id) {
        String packageName = paramActivity.getPackageName();
        return paramActivity.getResources()
                .getIdentifier(id, "color", packageName);
    }

    public static int getDrawableId(Context paramActivity, String id) {
        String packageName = paramActivity.getPackageName();
        return paramActivity.getResources()
                .getIdentifier(id, "drawable", packageName);
    }

    public static int getStyleId(Context paramActivity, String id) {
        String packageName = paramActivity.getPackageName();
        return paramActivity.getResources().getIdentifier(id, "style",
                packageName);
    }

    public static String getStringId(Context paramActivity, String id) {
        String packageName = paramActivity.getPackageName();
        return paramActivity.getResources().getString(paramActivity.getResources().getIdentifier(id, "string",
                packageName));
    }

    public static int getAttrId(Context paramActivity, String id) {
        String packageName = paramActivity.getPackageName();
        return paramActivity.getResources().getIdentifier(id, "attrs", packageName);
    }


    public static int[] getAttrArrayId(Context paramActivity, String id) {
        String packageName = paramActivity.getPackageName();
        int attrs = paramActivity.getResources().getIdentifier(id, "attrs", packageName);
        return new int[]{attrs};
    }

    public static int getStyleableId(Context context, String resourceName) {
        return getIdentifierByType(context, resourceName, "styleable");
    }


    public static int getAnimId(Context context, String resourceName) {
        return getIdentifierByType(context, resourceName, "anim");
    }

    public static int getArrayId(Context context, String resourceName) {
        return getIdentifierByType(context, resourceName, "array");
    }

    public static int getIntegerId(Context context, String resourceName) {
        return getIdentifierByType(context, resourceName, "integer");
    }

    public static int getBoolId(Context context, String resourceName) {
        return getIdentifierByType(context, resourceName, "bool");
    }

    private static int getIdentifierByType(Context context, String resourceName, String defType) {
        return context.getResources().getIdentifier(resourceName,
                defType,
                context.getPackageName());
    }

}
    
