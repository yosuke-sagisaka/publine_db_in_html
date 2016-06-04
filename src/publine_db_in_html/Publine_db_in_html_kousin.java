package publine_db_in_html;

//通常のインポート
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.logging.*;
import java.util.Calendar;
//import java.nio.channels.*;
//JDBCのインポート
import java.sql.*;



public class Publine_db_in_html_kousin extends LoggerFactory {
    private static String t_start = "BEGIN Transaction GO ";
    private static String t_end = " COMMIT Transaction GO";

    private static String site_cd = "01";
    private static String grp_cd = "030820";
    private static String sql_sum = "";
    private static String sql_single = "";

    public static void main(String[] args) {
        getLogger(Level.INFO,"PubLineデータ DB入力開始");
        //読み込みCSVフォルダを設定
        String serverIP="", port="", dbname="", username="", passwd="";
        String publine_folder="";

        Connection con = null;
        PreparedStatement pstmt_sum = null;
        PreparedStatement pstmt_single = null;
        ResultSet rset = null;
        
        int pstmt_no=0;
        
        String get_date="";
        for(int t=0;t<args.length;t++) {
            if("-get_date".equals(args[t])) {
                get_date = args[++t].trim();
                //取得日時指定
            }
        }

        try {
            FileInputStream filestream = new FileInputStream("../config.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(filestream,"Shift_JIS"));

            String str;
            while((str=br.readLine())!=null) {
                String[] str_split1 = str.split(";");
                String[] str_split2 = str_split1[0].split("=");

                if(str_split2[0].trim().equals("$serverIP")) {
                    serverIP = str_split2[1].trim();
                    serverIP = serverIP.substring(1,serverIP.length()-1);
                }
                else if(str_split2[0].trim().equals("$port")) {
                    port = str_split2[1].trim();
                    port = port.substring(1, port.length()-1);
                }
                else if(str_split2[0].trim().equals("$dbname")) {
                    dbname = str_split2[1].trim();
                    dbname = dbname.substring(1, dbname.length()-1);
                }
                else if(str_split2[0].trim().equals("$username")) {
                    username = str_split2[1].trim();
                    username = username.substring(1, username.length()-1);
                }
                else if(str_split2[0].trim().equals("$passwd")) {
                    passwd = str_split2[1].trim();
                    passwd = passwd.substring(1, passwd.length()-1);
                }
                else if(str_split2[0].trim().equals("$publine_folder")) {
                    publine_folder = str_split2[1].trim();
                    publine_folder = publine_folder.substring(1,publine_folder.length()-1);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        //SQLServer2008R2の接続設定
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:sqlserver://"+serverIP+":"+port+";");
        sb.append("databaseName="+dbname+";");
        sb.append("user="+username+";password="+passwd);

        //SQL Serverのドライバを取得
        try {
            getLogger(Level.INFO,"DB接続開始");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //DBへのコネクション開始
            con = DriverManager.getConnection(sb.toString());
            con.setAutoCommit(true);
            getLogger(Level.INFO,"DB接続完了");
        } catch(Exception e) {
            getLogger(Level.WARNING,e.getMessage());
            //e.printStackTrace();
        }
        
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String strDate = get_date.replaceAll("-", "");
        if(strDate.equals("")) {
            strDate = sdf.format(cal.getTime());
        }
        
        File mf = new File(publine_folder+"pos\\old\\"+strDate);
        if(!mf.exists()) mf.mkdir();
        
        //書誌CSVデータを移動
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        String strDate2 = sdf2.format(cal.getTime());
        File s_file = new File(publine_folder+"syoseki_"+strDate2+".csv");
        File s_rename = new File(publine_folder+"pos\\old\\"+strDate+"\\syoseki_"+strDate2+".csv");
        if(s_file.exists()) s_file.renameTo(s_rename);       
        
        int i,j,k;
        File pub_dir = new File(publine_folder+"pos\\");
        File[] pub_file = pub_dir.listFiles();
        for(i=0;i<pub_file.length;i++) {
            if(i==600) System.exit(0);
            if(pub_file[i].getName().indexOf("978")!=-1) {
                int point = pub_file[i].getName().lastIndexOf(".");
                String isbn_cd = pub_file[i].getName().substring(0,point);
                
                if(pub_file[i].isFile()) {
                    try {
                        getLogger(Level.INFO,pub_file[i]+"　：ファイル処理開始");
                        sql_single = "";sql_sum = "";
                        FileInputStream filestream = new FileInputStream(pub_file[i]);
                        BufferedReader br = new BufferedReader(new InputStreamReader(filestream, "Shift-JIS"));
                        String strLine = new String();
                        strLine="";
                        int no=1;
                        while((strLine=br.readLine())!=null) {
                            if(strLine.indexOf("<TR class=even>")!=-1) {
                                String tempo="",nyuko_s="",sale_t="",sale_s="",siire_s="";
                                String syuko_s="",henpin_s="",zaiko="",syoka_r="",henpin_r="";
                                while((strLine=br.readLine())!=null) {
                                    if(strLine.indexOf("合計")!=-1) {
                                        strLine = strLine.replaceAll("<TD style=\"WIDTH: 30px\" id=total><B>", "");
                                        strLine = strLine.replaceAll("</B></TD>", "");
                                        tempo = strLine.replaceAll("　", "");//店舗
                                    }
                                    if(strLine.indexOf("id=purchaseTotal")!=-1) {//仕入累計
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        siire_s = strLine.replaceAll("　", "");//仕入累計
                                    }
                                    else if(strLine.indexOf("id=wareaTotal")!=-1) {//入庫売上
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        nyuko_s = strLine.replaceAll("　", "");//入庫累計
                                    }
                                    else if(strLine.indexOf("id=sales")!=-1) {//本日売上
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        sale_t = strLine.replaceAll("　", "");//本日売上
                                    }
                                    else if(strLine.indexOf("id=saleTotal")!=-1) {//売上累計
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        sale_s = strLine.replaceAll("　", "");//売上累計
                                    }
                                    else if(strLine.indexOf("id=issueTotal")!=-1) {//出庫累計
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        syuko_s = strLine.replaceAll("　", "");//出庫累計
                                    }
                                    else if(strLine.indexOf("id=returnSum")!=-1) {//返品累計
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        henpin_s = strLine.replaceAll("　", "");//返品累計
                                    }
                                    else if(strLine.indexOf("id=stock")!=-1) {//在庫累計
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        zaiko = strLine.replaceAll("　", "");//在庫累計
                                    }
                                    else if(strLine.indexOf("id=digestionRate")!=-1) {//消化率
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        syoka_r = strLine.replaceAll("　", "");//消化率
                                    }
                                    else if(strLine.indexOf("id=returnRate")!=-1) {//返品率
                                        strLine=br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell><B>", "");
                                        strLine = strLine.replaceAll("</B></DIV></TD>", "");
                                        henpin_r = strLine.replaceAll("　", "");//返品率
                                    }
                                    else if(strLine.indexOf("</TR>")!=-1) {
                                        break;
                                    }
                                }
                                if(!tempo.equals("")) {
                                    //累計販売情報
                                    //ISBN,店舗,仕入累計,入庫累計,出庫累計,返品累計,消化率,在庫数量,売上累計
                                    input_sum(isbn_cd,tempo,siire_s,nyuko_s,syuko_s,henpin_s,syoka_r,zaiko,sale_s);
                                    //販売情報（当日）
                                    //ISBN,店舗,仕入累計,入庫累計,出庫累計,本日売上,書店在庫数,                 
                                    input_single(isbn_cd,tempo,siire_s,nyuko_s,syuko_s,sale_t,zaiko,henpin_s,get_date);
                                }
                            }
                            
                            
                            if(strLine.indexOf("<DIV class=rightCell>"+no+"</DIV></TD>")!=-1) {
                                no++;
                                int num=0;
                                getLogger(Level.INFO, pub_file[i]+"　：累計販売用、当日販売情報用SQL作成");
                                String tempo="",nyuko_s="",sale_t="",sale_s="",siire_s="";
                                String syuko_s="",henpin_s="",zaiko="",syoka_r="",henpin_r="";
                                while((strLine=br.readLine())!=null) {  
                                    strLine = strLine.trim();
                                    if(strLine.indexOf("<TR")!=-1) {
                                        break;
                                    }
                                    
                                    if(strLine.equals("<TD>")) {
                                        strLine = br.readLine();
                                        strLine = strLine.replaceAll("<DIV class=rightCell>", "");
                                        strLine = strLine.replaceAll("</DIV>", "");
                                        strLine = strLine.replaceAll("<B>", "");
                                        strLine = strLine.replaceAll("</B>", "");
                                        strLine = strLine.replaceAll("</TD>", "");
                                        strLine = strLine.replaceAll("<FONT color=#cc0033>", "");
                                        strLine = strLine.replaceAll("</FONT>", "");
                                        
                                    }
                                    else if(strLine.equals("<TD></TD>")) {
                                        strLine = strLine.replaceAll("<TD></TD>", "");
                                    }
                                    
                                    else {
                                        strLine = strLine.replaceAll("<TD>", "");
                                        strLine = strLine.replaceAll("</TD>", "");
                                    }
                                    strLine = strLine.trim();
                                    strLine = strLine.replaceAll(" ", "");//店舗

                                    
                                    //0:地域、1:店舗、2:仕入累計、3:入庫累計、4:本日売上
                                    //5:売上累計、6:出庫累計、7:返品累計、8:在庫数量、9:消化率、
                                    //10:返品率、11:棚名称１、12:棚名称２、13:棚名称３
                                    
                                    if(num==1) tempo = strLine.replaceAll("　", "");//店舗
                                    if(num==2) siire_s = strLine.replaceAll("　", "");//仕入累計
                                    if(num==3) nyuko_s = strLine.replaceAll("　", "");//入庫累計
                                    if(num==4) sale_t = strLine.replaceAll("　", "");//本日売上
                                    if(num==5) sale_s = strLine.replaceAll("　", "");//売上累計
                                    if(num==6) syuko_s = strLine.replaceAll("　", "");//出庫累計
                                    if(num==7) henpin_s = strLine.replaceAll("　", "");//返品累計
                                    if(num==8) zaiko = strLine.replaceAll("　", "");//在庫数量
                                    if(num==9) syoka_r = strLine.replaceAll("　", "");//消化率
                                    if(num==10) henpin_r = strLine.replaceAll("　", "");//返品率
                                    num++;
                                    
                                    if(strLine.indexOf("/TR>")!=-1) {
                                        break;
                                    }
                                    
                                }    
                                    
                                if(!tempo.equals("")) {
                                    //累計販売情報
                                    //ISBN,店舗,仕入累計,入庫累計,出庫累計,返品累計,消化率,在庫数量,売上累計
                                    input_sum(isbn_cd,tempo,siire_s,nyuko_s,syuko_s,henpin_s,syoka_r,zaiko,sale_s);
                                    //販売情報（当日）
                                    //ISBN,店舗,仕入累計,入庫累計,出庫累計,本日売上,書店在庫数,                 
                                    input_single(isbn_cd,tempo,siire_s,nyuko_s,syuko_s,sale_t,zaiko,henpin_s,get_date);
                                }
                            }
                        }
                        
                        String sql="";//INSERT実行用SQL
                        int line=0;

                        //販売情報（当日）
                        sql_single = t_start + sql_single + t_end +";";

                        getLogger(Level.INFO, "当日販売 登録ＳＱＬ\r\n"+sql_single);
                        //System.out.println(sql_single);
                        //累計販売情報
                        sql_sum = t_start + sql_sum + t_end +";";

                        getLogger(Level.INFO, "累計販売 登録ＳＱＬ\r\n"+sql_sum);
                        //ステートメントを作成し、SQLを実行
                        int gyo_single=0,gyo_sum = 0;

                        pstmt_single = null;
                        pstmt_sum = null;

                        try {
                            pstmt_single = con.prepareStatement(sql_single);
                            if(pstmt_single!=null) {
                                gyo_single = pstmt_single.executeUpdate();
                                sql_single = null;
                            }
                        }
                        catch(Exception e) {
                            //getLogger(Level.WARNING,e.getMessage());
                        }
                        
                        //getLogger(Level.INFO, "当日販売 ＤＢ入力実行\r\n"+sql_single);
                        try {
                            pstmt_sum = con.prepareStatement(sql_sum);
                            if(pstmt_sum!=null) {
                                gyo_sum = pstmt_sum.executeUpdate();
                                sql_sum = null;
                            }
                        }
                        catch(Exception e) {
                            //getLogger(Level.WARNING,e.getMessage());
                        }
                        
                        filestream.close();
                            
                        File rename = new File(publine_folder+"pos\\old\\"+strDate+"\\"+pub_file[i].getName());
                        boolean tt = pub_file[i].renameTo(rename);
                        
                        pstmt_no++;
                        if(pstmt_no%10==0) {
                            //SQL Serverのドライバを取得
                            try {
                                con.commit();
                                con.close();
                                
                                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                                con = DriverManager.getConnection(sb.toString());
                                con.setAutoCommit(true);
                            } catch(Exception e) {
                                getLogger(Level.WARNING,e.getMessage());
                            }
                        }
                        
                    } catch(Exception e) {
                        //getLogger(Level.WARNING,e.getMessage());
                        //e.printStackTrace();
                        
                    }
                    //System.out.println(pub_file[i]+"終了");
                }       
            }
        }
        //DBへのコミットをクローズ
        try {
            //クローズ前にPubLineが更新されたことを通知
            //getLogger(Level.INFO, "PubLineテーブルを更新したことを通知開始");
            String sql_kousin = "UPDATE dbo.site_access SET site_cd='01' WHERE site_cd='01';";
            PreparedStatement pstmt_kousin = null;
            ResultSet rset_kousin = null;
            int gyo_kousin=0;
            pstmt_kousin = con.prepareStatement(sql_kousin);
            if(pstmt_kousin!=null) {
                gyo_kousin = pstmt_kousin.executeUpdate();
            }
            if(gyo_kousin>0) {
                //getLogger(Level.INFO, "PubLineテーブルを更新したことを通知終了");
            }
            
            con.commit();
            con.close();
            //getLogger(Level.INFO,"DB切断");
        } catch(Exception e) {
            //getLogger(Level.WARNING,e.getMessage());
            //e.printStackTrace();
        }

        System.exit(1);
    }
    

    public static boolean input_single(String isbn_cd,String tempo,String siire_s,String nyuko_s,
            String syuko_s,String sale_s,String zaiko,String henpin_s,String get_date) {
        //ISBN,店舗,仕入累計,入庫累計,出庫累計,本日売上,書店在庫数,
        tempo=tempo.trim();
        
        siire_s=siire_s.trim();if(siire_s.equals("")) siire_s="0";
        nyuko_s=nyuko_s.trim();if(nyuko_s.equals("")) nyuko_s="0";
        syuko_s=syuko_s.trim();if(syuko_s.equals("")) syuko_s="0";
        sale_s=sale_s.trim();if(sale_s.equals("")) sale_s="0";
        zaiko=zaiko.trim();if(zaiko.equals("")) zaiko="0";
        
        //総仕入数を出力⇒仕入数+入庫数-出庫数
        int purchase_n = Integer.valueOf(siire_s) + Integer.valueOf(nyuko_s) - Integer.valueOf(syuko_s);

        sql_single += "INSERT INTO BLM_DB.dbo.sales ";
        sql_single += "(isbn_cd,grp_cd,purchase_num,sale_num,stock_num,return_num,get_date,site_cd,kyoyu_sy_cd) ";
        sql_single += "VALUES ('"+isbn_cd+"','"+grp_cd+"',";

        //日々の仕入(purchase_num)を計算-----------------------------------------------------------------------ここから
        sql_single += "(SELECT "+purchase_n+"-";
        sql_single += "CASE WHEN ";
        sql_single += "(SELECT TOP 1 purchase_num FROM BLM_DB.dbo.sales_sum ";
        sql_single += "WHERE isbn_cd='"+isbn_cd+"' AND site_cd='01' ";
        sql_single += "AND kyoyu_sy_cd=";
        if(tempo.indexOf("合計")!=-1) {
            sql_single += "'000000') ";
        }
        else {
            sql_single += "(SELECT TOP 1 kyoyu_sy_cd FROM BLM_DB.master.kyoyu_sy ";
            sql_single += "WHERE grp_cd='"+grp_cd+"' AND syoten_nm_kanji like '%"+tempo+"%' AND heiten_ym=''))";
        }

        sql_single += "IS NULL THEN 0 ";
        sql_single += "ELSE ";

        sql_single += "(SELECT TOP 1 purchase_num FROM BLM_DB.dbo.sales_sum ";
        sql_single += "WHERE isbn_cd='"+isbn_cd+"' AND site_cd='01' ";
        sql_single += "AND kyoyu_sy_cd=";
        if(tempo.indexOf("合計")!=-1) {
            sql_single += "'000000') ";
        }
        else {
            sql_single += "(SELECT TOP 1 kyoyu_sy_cd FROM BLM_DB.master.kyoyu_sy ";
            sql_single += "WHERE grp_cd='"+grp_cd+"' AND syoten_nm_kanji like '%"+tempo+"%' AND heiten_ym=''))";
        }
        sql_single += "END), ";
        //--------------------------------------------------------------------------------------------------ここまで
        
        sql_single += "'"+sale_s+"','"+zaiko +"',";
        
        
        //日々の返品数(return_num)を計算-----------------------------------------------------------------------ここから
        sql_single += "(SELECT "+henpin_s+"-";
        sql_single += "CASE WHEN ";
        sql_single += "(SELECT TOP 1 return_num FROM BLM_DB.dbo.sales_sum ";
        sql_single += "WHERE isbn_cd='"+isbn_cd+"' AND site_cd='01' ";
        sql_single += "AND kyoyu_sy_cd=";
        if(tempo.indexOf("合計")!=-1) {
            sql_single += "'000000') ";
        }
        else {
            sql_single += "(SELECT TOP 1 kyoyu_sy_cd FROM BLM_DB.master.kyoyu_sy ";
            sql_single += "WHERE grp_cd='"+grp_cd+"' AND syoten_nm_kanji like '%"+tempo+"%' AND heiten_ym=''))";
        }

        sql_single += "IS NULL THEN 0 ";
        sql_single += "ELSE ";

        sql_single += "(SELECT TOP 1 return_num FROM BLM_DB.dbo.sales_sum ";
        sql_single += "WHERE isbn_cd='"+isbn_cd+"' AND site_cd='01' ";
        sql_single += "AND kyoyu_sy_cd=";
        if(tempo.indexOf("合計")!=-1) {
            sql_single += "'000000') ";
        }
        else {
            sql_single += "(SELECT TOP 1 kyoyu_sy_cd FROM BLM_DB.master.kyoyu_sy ";
            sql_single += "WHERE grp_cd='"+grp_cd+"' AND syoten_nm_kanji like '%"+tempo+"%' AND heiten_ym=''))";
        }
        sql_single += "END), ";
        //--------------------------------------------------------------------------------------------------ここまで
        
        
        if(get_date.equals("")) {
            sql_single += "GETDATE(),'"+site_cd+"',";
        }
        else {
            sql_single += "'"+get_date+"','"+site_cd+"',";
        }
        
        if(tempo.indexOf("合計")!=-1) {
            sql_single += "'000000' ";
        }
        else {
            sql_single += "(SELECT TOP 1 kyoyu_sy_cd FROM BLM_DB.master.kyoyu_sy ";
            sql_single += "WHERE grp_cd='"+grp_cd+"' AND syoten_nm_kanji like '%"+tempo+"%' AND heiten_ym='' ";
            sql_single += "ORDER BY kyoyu_sy_cd) ";
        }
        sql_single += ");\n";
        //System.out.println(sql_single);
        return true;
    }

    public static boolean input_sum(String isbn_cd,String tempo,String siire_s,String nyuko_s,
            String syuko_s,String henpin_s,String syoka_r,String zaiko,String sale_t) {
        //前後の空白除去等
        tempo=tempo.trim();
        siire_s=siire_s.trim();if(siire_s.equals("")) siire_s="0";
        nyuko_s=nyuko_s.trim();if(nyuko_s.equals("")) nyuko_s="0";
        syuko_s=syuko_s.trim();if(syuko_s.equals("")) syuko_s="0";
        henpin_s=henpin_s.trim();if(henpin_s.equals("")) henpin_s="0";
        syoka_r=syoka_r.trim();syoka_r=syoka_r.replaceAll("%", "");if(syoka_r.equals("")) syoka_r="0";
        zaiko=zaiko.trim();if(zaiko.equals("")) zaiko="0";
        sale_t=sale_t.trim();if(sale_t.equals("")) sale_t="0";
        
        int purchase_n = Integer.valueOf(siire_s) + Integer.valueOf(nyuko_s) - Integer.valueOf(syuko_s);


        //全体のときは共有書店コードを「０」に

        //UPSERT用SQL
        sql_sum += "MERGE INTO BLM_DB.dbo.sales_sum T ";
        sql_sum += "USING (VALUES ('"+isbn_cd+"','"+purchase_n+"','"+henpin_s+"',";
        sql_sum += "'"+syoka_r+"','"+zaiko+"','"+sale_t+"','"+site_cd+"',";
        if(tempo.indexOf("合計")!=-1) sql_sum += "'000000')) ";
        else {
            sql_sum += "(SELECT TOP 1 kyoyu_sy_cd FROM BLM_DB.master.kyoyu_sy ";
            sql_sum += "WHERE grp_cd='"+grp_cd+"' AND syoten_nm_kanji like '%"+tempo+"%' AND heiten_ym=''";
            sql_sum += "ORDER BY kyoyu_sy_cd))) ";
        }
        sql_sum += "AS tmp(isbn,pn,hs,sr,za,st,sc,ksc) ";
        sql_sum += "ON (T.isbn_cd=tmp.isbn AND T.kyoyu_sy_cd=tmp.ksc AND T.site_cd=tmp.sc)" ;
        sql_sum += "WHEN MATCHED THEN ";
	sql_sum += "UPDATE SET T.isbn_cd=tmp.isbn,T.purchase_num=tmp.pn,T.return_num=tmp.hs,T.digestion_eff=tmp.sr,";
	sql_sum += "T.stock_num=tmp.za,T.sum_sales=tmp.st,T.site_cd=tmp.sc,T.kyoyu_sy_cd=tmp.ksc ";
        sql_sum += "WHEN NOT MATCHED THEN ";
	sql_sum += "INSERT (isbn_cd,purchase_num,return_num,digestion_eff,stock_num,sum_sales,site_cd,kyoyu_sy_cd)  ";
	sql_sum += "VALUES (tmp.isbn,tmp.pn,tmp.hs,tmp.sr,tmp.za,tmp.st,tmp.sc,tmp.ksc);\n";
        //ISBN,店舗,仕入累計,入庫累計,返品累計,消化率,在庫数量,売上累計
                                   
        return true;
    }
}
