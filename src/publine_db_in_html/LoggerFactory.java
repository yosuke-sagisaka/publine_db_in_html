package publine_db_in_html;

import java.io.*;
import java.util.logging.*;
import java.util.*;
import java.text.*;

public class LoggerFactory {
    static FileHandler fileHandler;
    static {
        InputStream is = null;
        try {
            /*
            Properties prop = new Properties();
            is = LoggerFactory.class.getResourceAsStream("fw.properties");
            prop.load(is);
            is.close();
            LogManager defaults = LogManager.getLogManager();
            */

            String str;
            String log_folder = "";
            FileInputStream filestream = new FileInputStream("../config.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(filestream,"Shift_JIS"));
            while((str=br.readLine())!=null) {
                String[] str_split1 = str.split(";");
                String[] str_split2 = str_split1[0].split("=");
                if(str_split2[0].trim().equals("$publine_log")) {
                    log_folder = str_split2[1].trim();
                    log_folder = log_folder.substring(1, log_folder.length()-1);
                }
            }

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String strDate = sdf.format(cal.getTime());
            //File old_mf = new File(in_folder+"old/");

            String pattern = log_folder + strDate + ".log";
            //int limit = 1000000000;
            //int count = 1;
            boolean append = true;
            /*
            String pattern = prop.getProperty("java.util.logging.FileHandler.pattern",
                    defaults.getProperty("java.util.logging.FileHandler.pattern"));
            int limit = Integer.parseInt(prop.getProperty("java.util.logging.FileHandler.limit",
                    defaults.getProperty("java.util.logging.FileHandler.limit")));
            int count = Integer.parseInt(prop.getProperty("java.util.logging.FileHandler.count",
                    defaults.getProperty("java.util.logging.FileHandler.count")));
            boolean append = Boolean.parseBoolean(prop.getProperty("java.util.logging.FileHandler.append",
                    defaults.getProperty("java.util.logging.FileHandler.appdend")));*/

            fileHandler = new FileHandler(pattern, append);
            fileHandler.setFormatter(new java.util.logging.SimpleFormatter());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static Logger getLogger(Level lv,String name) {
        Logger logger = Logger.getLogger(name);

        if(fileHandler!=null) {
                logger.addHandler(fileHandler);
                //logger.log(Level.WARNING,name);
                logger.log(lv,name);
        }
        return logger;
    }
}
