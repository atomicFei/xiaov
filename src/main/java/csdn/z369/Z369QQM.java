package csdn.z369;


import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhf on 2018/6/4.
 */
public class Z369QQM {
    private static final Logger LOGGER = Logger.getLogger(QQService.class);



        /**
         * @param args
         */
        public static void main(String[] args) {
            mainDo(args);

        }

    public static void mainDo(String[] args) {
        System.out.println();
        System.out.println("ver:2017.01.11.114600");
        LOGGER.info("主程序开始！！");

        URL CurClassUrl = Z369QQM.class.getResource("/");
        String CurClassPath="";
        try {
            CurClassPath = URLDecoder.decode(CurClassUrl.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String aa[] = CurClassPath.split("/");
        String CurPath = "";
        if(CurClassPath.contains("bin")){ //在eclispe中的执行情况
            CurPath=Z369QQM.class.getResource("/").getFile().toString(); //获取当前编译后的根目录
        }else{ //在CertTest中的执行情况
            for(int i = 1; i < aa.length; i++){
                CurPath += aa[i] + "\\";
            }
        }

        String fileName="config.properties";
        //如果没有传入配置文件名称，采用默认的
        if(args==null||args.length==0||args[0]==null||"".equals(args[0])){
            System.out.println("采用默认的配置文件名[config.properties]");
        }else{
            fileName=args[0];
        }
        LOGGER.info("当前配置文件=>"+CurPath+fileName);
        File proFile = new File(CurPath + fileName);
        Properties prop = new Properties();

        if (proFile!=null&&proFile.exists() && !proFile.isDirectory()) {
            try {
                FileInputStream in = new FileInputStream(proFile);
                byte[] b = new byte[3];
                in.read(b);
                String code = "GBK";
                if (b[0] == -17 && b[1] == -69 && b[2] == -65){
                    code = "UTF-8";
                }
                InputStreamReader inputStreamReader = new InputStreamReader(in,code);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                prop.load(bufferedReader);
                String only_focus_person_name = prop.getProperty("onlyFocusMode");
                Config.msg2SendGroupName =  prop.getProperty("focusMsg2SendGroupName",null);
                Integer focusFriend =  Integer.parseInt(prop.getProperty("focusFriend",  "0"));
                Integer focusGroup =  Integer.parseInt( prop.getProperty("focusGroup",  "0"));
                Integer focusDis =  Integer.parseInt( prop.getProperty("focusDis",  "0"));
                if(focusFriend.intValue()==1)  {
                    Config.focusFriend = true;
                    LOGGER.info(" 显示朋友的信息 ");
                }
                if(focusGroup.intValue()==1) {
                    Config.focusGroup = true;
                    LOGGER.info(" 显示群的信息 ");

                }
                if(focusDis.intValue()==1) {
                    Config.focusDis = true;
                    LOGGER.info(" 显示组的信息 ");
                }
                boolean onlyFPN = false;
                if(only_focus_person_name!=null && !"".equals(only_focus_person_name)&&"1".equals(only_focus_person_name.replace(" ",""))){
                    onlyFPN = true;
                }
                LOGGER.info("是否只显示关心的朋友的信息: [" + (onlyFPN?"是":"否" )+ "]");
                if(onlyFPN){
                    List<String> likeList = new ArrayList<String>();
                    LOGGER.info("关心的朋友的QQ昵称/uin(不是QQ号): [");
                    for (Object o : prop.keySet()) {
                        String key = (String)o;
                        if(key.startsWith("focus.person")){
                           if(prop.get(o)!=null) {
                               LOGGER.info("             ---"+key+"----->"+prop.get(o));
                               likeList.add((String) prop.get(o));
                           }
                        }
                    }
                    Config.focusNamesLike =  likeList.toArray( Config.focusNamesLike);
                    LOGGER.info("]");
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("读取配置文件失败");
             }

        }else{
            LOGGER.warn("没有读取到配置文件！！采用默认信息处理机制！");
        }

        QQService qqService =  new QQService();
        qqService.initQQClient();
    }
}
