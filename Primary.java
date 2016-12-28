package com.timerecord;

/**
 *功能库主类，库内其他类对象与业务均由本类生成与启动。
 * @author Hugh
 * @Time 2016/12/12
 */

import com.timerecord.TimeRecordAPI.*;


import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static com.timerecord.TimeRecordAPI.enMsgType.*;
import static com.timerecord.DatabasePro.Column.*;
import static java.text.DateFormat.getDateTimeInstance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Vector;

import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;


public class Primary {

    private static String CONFIGSYS = "config.txt";
    private static String CONFIGPRO = "Protocol.txt";
    private  byte[] m_byTypeNO = new byte[20];//终端型号  /TODO 定义终端型号

    /********************Primary Para 本类自用参数部分**********************/
    private Context m_context;              //application context
    private DatabasePro m_DB;               //数据库
    private TimeRecordAPI m_API;
    private SysInfo_S m_SysInfo;
    private SysStatus_S m_Status;
    private FaceRecognizeAccount_S m_Face;
    public long m_lAlarmSet;                //报警标志 TODO 需计算
    public long m_lStatusSet;               //状态为标志 TODO 需计算
    private int m_nRecordSN;                //学习编号中的4位序列号
    private String m_sDayRecordSN;          //记录学习编号的日期
    private int m_nGPSMile;                 //GPS里程  TODO 需计算
    private boolean m_bCoachLogin;          //教练登陆状态 1:已登陆 0:未登录
    private boolean m_bStudentLogin;        //学员登陆状态 1:已登陆 0:未登录
    private short m_TrainingForbid=1;       //1：可用，默认值；2：禁用




    /*student Para*/
    private long m_lClassID;                            //标识学员的一次培训过程， 计时终端 自行使用
    private byte[] m_byLearnLevel = new byte[2];
    private long m_lSpecifyTimeTotality;                //场地及道路总需培训学时  单位： min
    private long m_lSpecifyMileTotality = 300;          //场地及道路总需培训里程  单位： km
    private long m_lSpecifyTimePartTwo;                 //场地所需学时（含模拟及实操） 单位： min
    private long m_lSpecifyTimePartThree;               //道路所需学时（含模拟及实操）单位： min
    private long m_lSpecifyTimeForSimulate = 4;         //场地及道路模拟所需学时 单位： min
    private int m_nTrainObject;                        //当前培训课程 1-场地模拟，2-场地实操，3-道路模拟，4-道路实操
    private StudentLogin_S m_StudentLogin_S ;
    private long m_lStudyStartTime;                     //学员开始学习时间  单位  ms
    private long m_lStudyContinuousTime;                //持续学习时间      单位 分

    /* Protocol*/
    private byte[] m_byManufacturerID = {'Q','Y','X','N',0};



     /********************Protocol Para 网络协议参数部分**********************/
    public String m_SIMNum;                        //SIM卡号  //TODO 获取
    public byte[] m_byPlatformID = new byte[5];    //平台编号
    public byte[] m_byTrainDepartmentID = new byte[16];//培训机构编号
    public byte[] m_byTerminalID = new byte[16];   //计时终端编号
    public String m_sCertificate;                  //终端证书

    public String m_sMainServerAPN;                //主服务器APN
    public String m_sMainServerUser;               //主服务器账号
    public String m_sMainServerPassWord;           //主服务器密码
    public String m_sMainServerDomain;             //主服务器域名或IP
    public long   m_lMainServerPort;               //主服务器端口
    public String m_sSubServerAPN;                 //备份服务器APN
    public String m_sSubServerUser;                //备份务器账号
    public String m_sSubServerPassWord;            //备份务器密码
    public String m_sSubServerDomain;              //备份服务器域名或IP
    public long   m_lSubServerPort;                //备份服务器端口

    public long m_lGPSReportWay;                   //位置汇报策略，0：定时汇报； 1：定距汇报； 2：定时和定距汇报
    public long m_lGPSPositionWay;                 //位置汇报方案，0：根据ACC状态；1：根据登录状态和ACC状态，先判断登录状态，若登录再根据ACC状态
    public long m_lTimeIntervalDriverUnlogin;      //驾驶员未登录汇报时间间隔，单位为秒(s),>0
    public long m_lTimeIntervalDormancy;           //休眠时汇报时间间隔，单位为秒(s),>0
    public long m_lTimeIntervalAlarm;              //紧急报警时汇报时间间隔，单位为秒(s),>0
    public long m_lTimeIntervalDefault;            //缺省时间汇报间隔，单位为秒(s),>0
    public long m_lRangeIntervalDefault;           //缺省时间汇报间隔，单位为米(m),>0
    public long m_lRangeIntervalDriverUnlogin;     //驾驶员未登录汇报距离间隔，单位为米(m),>0
    public long m_lRangeIntervalDormancy;          //休眠时汇报距离间隔，单位为米(m),>0
    public long m_lRangeIntervalAlarm;             //紧急报警时汇报距离间隔，单位为米(m),
    public long m_lSwerveAngle;                    //拐点补传角度， <180°

    public String m_sPhoneNoOfPlatform;             //监控平台电话号码
    public String m_sPhoneNoOfResetTerminal;        //复位电话号码，可采用此电话号码拨打终端电话让终端复位
    public String m_sPhoneNoOfFactoryReset;         //恢复出厂设置电话号码，拨打此电话号码终端恢复出厂设置
    public String m_sPhoneNoOfPlatformSMS;          //监控平台 SMS 电话号码
    public String m_sPhoneNoForTerminalAlarm;       //接收终端 SMS 文本报警号码
    public long m_lWayPickUp;                       //终端电话接听策略，0:自动接听1:ACC ON 时自动接听，OFF 时手动接听
    public long m_lMaximunPhoneCallTime;            //每次最长通话时间，单位为秒(s),0 为不允许通话， 0xFFFFFFFF 为不限制
    public long m_lMaximunPhoneCallTimePerMonth;    //当月最长通话时间，单位为秒(s),0 为不允许通话， 0xFFFFFFFF 为不限制
    public String m_sPhoneNoOfListen;               //!!@@监听电话号码
    public String m_sSMSNoOfPlatformPrivilege;      //!!@@监管平台特权短信号码
    public long m_lAlarmMask;                       //报警屏蔽字。与位置信息报警标识相对应，相应位为1则相应报警被屏蔽
    public long m_lAlarmSwitchOfSMS;                //报警发送文本 SMS 开关，与位置信息报警标识相对应，相应位为 1 则相应报警时发送文本 SMS
    public long m_lAlarmTakePicture;                //报警拍摄开关，与位置信息报警标识相对应，相应位为1 则相应报警时摄像头拍摄
    public long m_lAlarmSavePhoto;                  //报警拍摄存储标识，与位置信息报警标识相对应，相应位为 1 则进行存储，否则实时长传
    public long m_lAlarmMark;                       //关键标识，与位置信息报警标识相对应，相应位为 1 则对相应报警为关键报警
    public long m_lMaximunSpeed;                    //最高速度，单位为公里每小时(km/h)
    public long m_lConsecutiveTimeOfOverSpeed;      //超速持续时间，单位为秒(s)
    public long m_lConsecutiveTimeOfDriving;        //连续驾驶时间门限，单位为秒(s)
    public long m_lConsecutiveTimeOfDrivePerday;    //当天累计驾驶时间门限，单位为秒(s)
    public long m_lMinimunTimeOfRest;               //最小休息时间，单位为秒(s)
    public long m_lMaximunTimeOfPark;               //最长停车时间，单位为秒(s)
    public long m_lPictureQuality;                  //图像/视频质量， 1-10,1 最好
    public long m_lPictureBriteness;                //亮度， 0-255
    public long m_lPictureContrast;                 //对比度， 0-127
    public long m_lPicSaturability;                 //饱和度， 0-127
    public long m_lPicColor;                        //色度， 0-255
    public long m_lVehicleMileage;                  //车辆里程表读数， 1/10km
    public long m_lVehicleProvinceID;               //车辆所在的省域 ID
    public long m_lVehicleCityID;                   //车辆所在的市域 ID
    public long m_lVehicleLicenceID;                //公安交通管理部门颁发的机动车号牌 !!@@
    public long m_lVehiclePlatColor;                //车牌颜色，按照 JT/T415-2006 的 5.4.12


    /********************GPS Para GPS参数部分**********************/
//    public char m_cGPSAvailable;                    //GPS状态；  A:有效  V:无效
//    public char m_cLatitudeNS;                      //南北纬度   N:北纬；S：南纬
//    public char m_cLongitudeEW;                     //东西经度； E:东经；W：西经
//    public double m_dLatitude;                      //纬度值 dd.dddddd
//    public double m_dLongitude;                     //经度值 ddd.dddddd
//    public double m_dSpeed;                         //速度;  xxx.xx(Km/h)
//    public int m_nDirection;                        //GPS方向


    /************************Coach Para**************************/
    public byte[] m_byCoachID = new byte[16];          //教练员编号  统一编号
   // public byte[] m_byCoachIdentity = new byte[18];    //教练员身份证号ASCII 码，不足 18 位前补 0x00
    //public byte[] m_byCoachLevel = new byte[2];        //准教车型A1\A2\A3\B1\B2\C1\C2\C3\C4\D\E\F

    /************************student Para***************************/
    public byte[] m_byStudentID = new byte[16];        //学员编号  统一编号
//    public byte[] m_byStudentsCoach = new byte[16];    //当前教练编号  统一编号
//    public byte[] m_byTrainID = new byte[10];          //培训课程 课程编码见 平台技术规范 A4.2
//    public long m_lLessonID;                           // 课堂 ID 标识学员的一次培训过程， 计时终端自行使用
//
//    public long m_lTrainTimeTotality;                //总培训学时 单位： min
//    public long m_lTrainTimeCurrentPart;             //当前培训部分已完成学时 单位： min
//    public long m_lTrainMileTotality;                //总培训里程 单位： 1/10km
//    public long m_lTrainMileCurrentPart;             //当前培训部分已完成里程 单位： 1/10km


    /************************* System Practical Para*******************/

    public short m_TakePicInterval = 15;      //定时拍照时间间隔 单位： min，默认值:15。在学员登录后间隔固定时间拍摄照片
    public short m_UploadingPicWay;           //照片上传设置 0：不自动请求上传；1：自动请求上传
    public short m_TTSPlay = 1;               //是否报读附加消息  1：自动报读； 2：不报读 控制是否报读消息中的附加消息
    public short m_DeferTimeWhileShutdown;    //熄火后停止学时计时的延时时间  单位： min
    public long m_UploadingGPSInterval = 3600;//熄火后 GNSS 数据包上传间隔  单位： s，默认值 3600， 0 表示不上传
    public long m_DeferTimeForCoachLogOut = 150;//熄火后教练自动登出的延时时间  单位： min，默认值 150
    public long m_ReVerifyTime = 30;          //重新验证身份时间  单位： min，默认值 30
    public short m_OtherSchoolPermitOfCoach;  //教练跨校教学  1：允许 2：禁止
    public short m_OtherSchoolPermitOfstudent;//学员跨校学习 1：允许 2：禁止



    public Primary(TimeRecordAPI api,Context context) {
        this.m_API = api;
        m_context = context;
        m_SysInfo = m_API.new SysInfo_S();
        m_Status = m_API.new SysStatus_S();
        m_StudentLogin_S = m_API.new StudentLogin_S();





        /**模拟部分数据，正式板无下列初始化*/
        m_Status.nGPRSStatus = 1;
        m_Status.nGPSAvailable = 1;
        m_Status.nSignalIntensity = 1;
        m_Status.sGPRS_IP = "88.88.88.88";
        m_Status.nSateliteNum = 6;
        m_Status.nPlatformConnection = 1;
        SetSystemInfo();

        m_Face = m_API.new FaceRecognizeAccount_S();
        m_Face.sApiKey = "I don't have it yet";
        m_Face.sApiSecret = "8888";
    }

    public static void main(String[] args) {

    }


    public int StartBusiness() {
        System.out.println("Primary.StartBusiness Run");
        LoadConfig(null,null,CONFIGSYS);
        LoadConfig(null,null,CONFIGPRO);
        m_DB = new DatabasePro(m_context);
        m_DB.InitDB();
        ReadyToGetStudyRecordNO();

        /**testing code*/
//        byte[] bySrc = new byte[26];
//        GetStudyRecordNO(bySrc);
//        for (int i = 0 ; i != bySrc.length; i++)
//        {
//            System.out.printf("%02x ",bySrc[i]);
//        }

        /**testing code*/
        //System.out.println(m_context.getFilesDir());
        //System.out.println(m_context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
       // System.out.println(this.getApplicationContext());

        /**testing code*/
        //System.out.print(System.getProperty("file.encoding"));
//        CustomizedMsg CusMsg = m_API.new CustomizedMsg();
//        CusMsg.nMsgCategory = 1;
//        CusMsg.sMsg = "Customized Out put Message succeed!";
//        m_API.CB.TRCallBack(MSG_CustomizedMsg, CusMsg, 0, null);

        /**testing code*/
//        LoadConfig(null,null,CONFIGPRO);
//        SetConfig("m_lGPSReportWay","1",CONFIGPRO);
//        SetConfig("m_sPhoneNoOfPlatform","1319999999",CONFIGPRO);
//        SetConfig("m_lGPSPositionWay","1",CONFIGPRO);
//        LoadConfig(null,null,CONFIGPRO);

        /**testing code*/
       // m_DB.DBDropTable();
       // m_DB.DBInsert("001",100,300,1,10,1,8);
       // m_DB.DBDelete("003");
 //       m_DB.DBAlter("002", PTwoSimulate,1000);
 //        m_DB.DBSelect(null);
       // StudentLogin_S Stu = m_API.new StudentLogin_S();
       // m_DB.DBGetStudentTrain("004",Stu);

        /**testing code*/
      //  String PlatformID = "0001";
       // String TrainDepartmentID = "123456";
      //  String TerminalID = "888888";
      //  String sCertificate = "3nmdksla32";
      //  NET_RegisterRes((short)0,PlatformID.getBytes(),TrainDepartmentID.getBytes(), TerminalID.getBytes(),sCertificate);

        /**testing code*/
 //       StringBuffer sb = new StringBuffer();
  //      System.out.println(Net_GetPara("m_sSubServerUser",sb));
//        System.out.println(sb.toString());
//        StringBuffer sb1 = new StringBuffer();
//        System.out.println("@@@@@@@@"+Net_GetPara("m_nannnnn",sb1));
//        System.out.println("!!!!!!!!!!"+sb1.toString());
        return 0;
    }


    public int EndBusiness() {
        m_DB.DBClose();
        System.out.println("Primary EndBusiness");
        return 0;
    }


    /***********************************网络交互部分**********************************************/
    //GPS结构体
    class Net_GPS_S
    {
        long lAlarmSet;                     //报警标志
        long lStatusSet;                    //状态为标志
        long lLatitude;			            //纬度
        long lLongitude;		            //经度
        int nOBDSpeed;			            //行驶记录速度
        int nGPSSpeed;			            //GPS速度
        int nDirection;			            //GPS方向   0-359度
    }

    //学员登录结构体
    class Net_StudentLogin_S
    {
        public short LoginResult;                   // 1：登录成功；2：无效的学员编号；3：禁止登录的学员；4：区域外教学提醒；5：准教车型与培训车型不符；9：其他错误
        byte[] byStudentID = new byte[16];          //学员编号  统一编号
        public long lTrainTimeTotality;             //总培训学时 单位： min
        public long lCurrentPartTime;               //当前培训部分已完成学时  单位： min
        public long lTrainMileTotality;             //总培训里程 单位： 1/10km  (100米)
        public long lCurrentPartMile;               //当前培训部分已完成里程  单位： 1/10km //!!@@未使用
        public short TTSPlay;                       //0：不必报读；1：需要报读
        public String sPlayText;                    //语音播放内容
    }

    //计时终端应用参数结构体
    class Net_PracticalPara_S
    {
        public short TakePicInterval;          //定时拍照时间间隔 单位： min，默认值:15。在学员登录后间隔固定时间拍摄照片
        public short UploadingPicWay;          //照片上传设置 0：不自动请求上传；1：自动请求上传
        public short TTSPlay;                  //是否报读附加消息  1：自动报读； 2：不报读 控制是否报读消息中的附加消息
        public short DeferTimeWhileShutdown;   //熄火后停止学时计时的延时时间  单位： min
        public long UploadingGPSInterval;       //熄火后 GNSS 数据包上传间隔  单位： s，默认值 3600， 0 表示不上传
        public long DeferTimeForCoachLogOut;    //熄火后教练自动登出的延时时间  单位： min，默认值 150
        public long ReVerifyTime;               //重新验证身份时间  单位： min，默认值 30
        public short OtherSchoolPermitOfCoach;  //教练跨校教学  1：允许 2：禁止
        public short OtherSchoolPermitOfstudent;//学员跨校学习 1：允许 2：禁止
    }


    /**网络模块需实现函数,供主模块调用*/
    /** Net_TerminalRegister 终端注册
     * @param nVehicleProvinceID [In] 省ID
     * @param nVehicleCityID [In] 市ID
     * @param byManufacturerID  [In] 制造商ID
     * @param byTypeNO [In] 终端型号
     * @param byFactoryNumber [In] 计时终端出厂序列号
     * @param byIMEI [In] IMEI码
     * @param PlatColor [In] 车牌颜色 0：未上牌1：蓝色 2：黄色 3：黑色 4：百色9：其他
     * @param sVehicleLicenceID [In]公安交通管理部门颁发的机动车号牌
     * @return  0：成功 ,-1:失败，-2：传入空引用*/
    int Net_TerminalRegister(int nVehicleProvinceID, int nVehicleCityID, byte[] byManufacturerID, byte[] byTypeNO, byte[] byFactoryNumber, byte[] byIMEI, short PlatColor, String sVehicleLicenceID)
    { return 0;};

    /** Net_TerminalLogOff 终端注销
     * @return  0：成功 ,-1:失败*/
    int Net_TerminalLogOff()
    { return 0;};


    /** Net_SendGPS 发送GPS
     * @param gps [In]  PS结构体
     * @return  0：成功 ,-1:失败，-2：传入空引用*/
    public int Net_SendGPS(Net_GPS_S gps)
    {return 0;}


    /** Net_CoachLogin 教练员登录
     * @param byCoachID [In] 教练员编号  统一编号
     * @param byCoachIdentity [In] 教练员身份证号ASCII 码，不足 18 位前补 0x00
     * @param byCoachLevel [In] 准教车型A1\A2\A3\B1\B2\C1\C2\C3\C4\D\E\F
     * @param gps [In] PS结构体
     * @return  0：本机正常登录（不代表登录平台成功），-1：登录出错，-2：传入参数有误*/
    public int Net_CoachLogin(byte[] byCoachID, byte[] byCoachIdentity, byte[] byCoachLevel, Net_GPS_S gps)
    {return 0;}


    /** Net_CoachLogout 教练员登出
     * @param byCoachID [In] 教练员编号  统一编号
     * @param gps [In] PS结构体
     * @return  0：本机正常登出（不代表登出平台成功），-1：登出失败，-2：传入参数有误*/
    public int Net_CoachLogout(byte[] byCoachID, Net_GPS_S gps)
    {return 0;}

    /** Net_StudentLogin 学员登录
     * @param byStudentID [In] 学员编号  统一编号
     * @param byStudentsCoach [In] 当前教练编号  统一编号
     * @param sTrainingCourse [In] 培训课程
     * @param lClassID [In] 标识学员的一次培训过程， 计时终端 自行使用
     * @param gps [In] PS结构体
     * @return  0：本机正常登录（不代表登录平台成功），-1：登录失败，-2：传入参数有误*/
    public int Net_StudentLogin(byte[] byStudentID, byte[] byStudentsCoach, String sTrainingCourse, long lClassID,  Net_GPS_S gps)
    {return 0;}

    /** Net_StudentLogout 学员登出
     * @param byStudentID [In] 学员编号  统一编号
     * @param nStudyContinuousTime [In] 学员该次登录总时间
     * @param nStudyMile [In] 学员该次登录总里程
     * @param lClassID [In] 标识学员的一次培训过程， 计时终端 自行使用
     * @param gps [In] GPS结构体
     * @return  0：本机正常登出（不代表登出平台成功），-1 登出失败，-2 传入参数有误*/
    public int Net_StudentLogout(byte[] byStudentID,int nStudyContinuousTime, int nStudyMile, long lClassID, Net_GPS_S gps)
    { return 0;}


    /** Net_StudyRecord 上报学时记录
     * @param byStudyRecordNO [In] 学习记录编号
     * @param ReportType [In] 上报类型 1:自动上报  2:平台要求上报
     * @param byStudentID [In] 学员编号  统一编号
     * @param byCoachID [In] 教练
     * @param lClassID [In] 标识学员的一次培训过程， 计时终端 自行使用
     * @param sTrainingCourse [In] 培训课程
     * @param state [In] 培训状态 0:正常记录  1:异常记录
     * @param nMaxSpeed [In] 最大速度 1min 内车辆达到的最大卫星定位速度，1/10km/h
     * @param nMile [In] 里程 车辆 1min 内行驶的总里程，1/10km
     * @param gps [In] GPS结构体
     * @return  0：本机正常登出（不代表登出平台成功），-1 登出失败，-2 传入参数有误*/
    public int Net_StudyRecord(byte[] byStudyRecordNO, short ReportType, byte[] byStudentID, byte[] byCoachID, long lClassID,
                               String sTrainingCourse, short state, int nMaxSpeed, int nMile, Net_GPS_S gps)
    { return 0;}












    /**主模块向网络模块提供的接口函数*/

    /** Pri_RegisterRes 设置参数 支持参数值为String 、byte[]和 long类型的重载版本
     * @param Result [In] 应答结果
     * @param byPlatformID [In] 平台编号
     * @param byTrainDepartmentID [In] 培训机构编号
     * @param byTerminalID [In] 终端编号
     * @param sCertificate [In] 终端证书
     * @return  0：成功 ,-1:失败，-2：传入空引用*/
    public int Pri_RegisterRes(short Result,  byte[] byPlatformID, byte[] byTrainDepartmentID, byte[] byTerminalID , String sCertificate)
    {

        if (Result == 0)
        {
            if(byPlatformID == null || byTrainDepartmentID == null || byTerminalID == null || sCertificate == null)
            {
                return -2;
            }
            System.arraycopy(byPlatformID,0,m_byPlatformID,0,byPlatformID.length);
            System.arraycopy(byTrainDepartmentID,0,m_byTrainDepartmentID,0,byTrainDepartmentID.length);
            System.arraycopy(byTerminalID,0,m_byTerminalID,0,byTerminalID.length);
            m_sCertificate = sCertificate;
            SetConfig("m_byPlatformID",new String(byPlatformID),CONFIGPRO);
            SetConfig("m_byTrainDepartmentID",new String(byTrainDepartmentID),CONFIGPRO);
            SetConfig("m_byTerminalID",new String(byTerminalID),CONFIGPRO);
            SetConfig("m_sCertificate",m_sCertificate,CONFIGPRO);
        }
        m_API.CB.TRCallBack(MSG_TerminalRegister,null,Result,null);
        return 0;
    }


    /** Pri_SetPara 设置参数 支持参数值为String 、byte[]和 long类型的重载版本
     * @param sParaName 参数名称[In]，如设置主服务器APN 则为"m_sMainServerAPN"。注：所有参数都必须是Primary类的成员变量
     * @param sParaValue 参数值[In]，普通数据类型调用toString()转换后填入，byte[] 类型请使用new String("your byte data")
     * @return  0：成功 ,-1:参数名称错误，-2：传入空引用*/
    public int Pri_SetPara(String sParaName,String sParaValue)
    {
        if (sParaName == null || sParaValue == null)
        {
            return -2;
        }
        return SetConfig(sParaName,sParaValue,CONFIGPRO);
    }
    public int Pri_SetPara(String sParaName,long lParaValue)
    {
        if (sParaName == null)
        {
            return -2;
        }
        return SetConfig(sParaName,String.valueOf(lParaValue),CONFIGPRO);
    }
    public int Pri_SetPara(String sParaName,byte[] byParaValue)
    {
        if (sParaName == null)
        {
            return -2;
        }
        return SetConfig(sParaName, new String(byParaValue),CONFIGPRO);
    }

    /** Pri_SetPara 获取参数
     * @param sParaName 参数名称[In]，如设置主服务器APN 则为"m_sMainServerAPN"。注：所有参数都必须是Primary类的成员变量
     * @param sParaValue 参数值[Out]，注：参数为输出参数
     * @return  0：成功 ,1:参数名称错误，2：传入空引用*/
    public int Pri_GetPara(String sParaName,StringBuffer sParaValue)
    {
        if (sParaName == null)
        {
            return -1;
        }
        return LoadConfig(sParaName,sParaValue, CONFIGPRO);
    }


    /** Pri_TerminalControl 终端控制
     * @param command [In] //终端控制命令字说明见表 B.27
     * @param sPara [In] //命令参数格式具体见后描述，每个字段之间采用” ;”分隔
     * @return  0：成功 ,-1:失败*/
    public int Pri_TerminalControl(Short command,String sPara)
    {
        //TODO
       return 0;
    }

    /** Pri_GetGPS 发送GPS
     * @param gps [Out]  GPS结构体
     * @return  0：成功 ,-1:失败，-2：传入空引用*/
    public int Pri_GetGPS(Net_GPS_S gps)
    {
        GetGPS(gps);
        return 0;
    }


    /** Pri_CoachLogin 教练员登录应答
     * @param LoginResult [In] 登陆结果 1：登录成功;2：无效的教练员编号;3：准教车型不符；9：其他错误
     * @param byCoachID [In]  教练员编号  统一编号
     * @param TTSPlay  [In] 语音播放设置 0：根据全局设置决定是否报读；1：需要报读；2：不必报读
     * @param sPlayText [In]  语音播放内容
     * @return  0：成功，-1：出错*/
    public int Pri_CoachLoginRes(short LoginResult,byte[] byCoachID, short TTSPlay, String sPlayText)
    {
        if(m_bCoachLogin)
        {
            return -1;
        }
        if(LoginResult == 1)
        {
            m_bCoachLogin = true;
            if(byCoachID == null)
            {
                return -2;
            }
            System.arraycopy(byCoachID,0,m_byCoachID,0,byCoachID.length);
        }
        CoachLogin_S Login = m_API.new CoachLogin_S();
        Login.LoginResult = LoginResult;
        try
        {
            Login.sCoachID = new String(byCoachID,"GBK");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        m_API.CB.TRCallBack(MSG_CoachLogin,Login,0,null);
        if(TTSPlay == 1 || m_TTSPlay == 1)
        {
            if(sPlayText != null)
            {
                m_API.CB.TRCallBack(MSG_TTSPlay,null,0,sPlayText);
            }
        }
        return 0;
    }


    /** Pri_CoachLogout 教练员登出应答
     * @param LoginResult [In] 登陆结果 1：登录成功;2：登出师表
     * @param byCoachID  [In] 教练员编号  统一编号
     * @return  0：成功，-1：出错*/
    public int Pri_CoachLogoutRes(short LoginResult,byte[] byCoachID)
    {
        if(!m_bCoachLogin)
        {
            return -1;
        }
        m_bCoachLogin = false;
        CoachLogout_S Logout = m_API.new CoachLogout_S();
        Logout.LoginResult = LoginResult;
        try
        {
            Logout.sCoachID = new String(byCoachID,"GBK");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        m_byCoachID = null;
        m_API.CB.TRCallBack(MSG_CoachLogin,Logout,0,null);
        return 0;
    }


    /** Pri_StudentLoginRes 学员登录应答
     * @param Stu [In]  学员登录结构体 Net_StudentLogin_S
     * @return  0：成功，-1：出错*/
    public int Pri_StudentLoginRes(Net_StudentLogin_S Stu)
    {
        if(m_bStudentLogin)
        {
            return -1;
        }
        m_bStudentLogin = true;
        m_StudentLogin_S.LoginResult = Stu.LoginResult;
        try {
            m_StudentLogin_S.sStudentID = new String(Stu.byStudentID,"GBK");
        }
        catch (UnsupportedEncodingException e)
        {e.printStackTrace();}
        if(Stu.LoginResult == 1)
        {
            m_lStudyStartTime = new Date().getTime();
            System.arraycopy(Stu.byStudentID,0,m_byStudentID,0,Stu.byStudentID.length);

            m_DB.DBAlter(m_StudentLogin_S.sStudentID,TTotal,Stu.lTrainTimeTotality);
            m_DB.DBAlter(m_StudentLogin_S.sStudentID,MTotal,Stu.lTrainMileTotality);
            if(m_nTrainObject == 1)
            {
                m_DB.DBAlter(m_StudentLogin_S.sStudentID,PTwoSimulate,Stu.lCurrentPartTime);
            }
            else if(m_nTrainObject == 2)
            {
                m_DB.DBAlter(m_StudentLogin_S.sStudentID,PTwoPratical,Stu.lCurrentPartTime);
            }
            else if(m_nTrainObject == 3)
            {
                m_DB.DBAlter(m_StudentLogin_S.sStudentID,PThreeSimulate,Stu.lCurrentPartTime);
            }
            else if(m_nTrainObject == 4)
            {
                m_DB.DBAlter(m_StudentLogin_S.sStudentID,PThreePratical,Stu.lCurrentPartTime);
            }

            m_DB.DBGetStudentTrain(m_StudentLogin_S.sStudentID,m_StudentLogin_S);
            m_StudentLogin_S.lSpecifyTimeTotality = m_lSpecifyTimeTotality;
            m_StudentLogin_S.lSpecifyMileTotality = m_lSpecifyMileTotality;                //场地及道路总需培训里程  单位： km
            m_StudentLogin_S.lSpecifyTimePartTwo = m_lSpecifyTimePartTwo;                 //场地所需学时（含模拟及实操） 单位： min
            m_StudentLogin_S.lSpecifyTimePartThree = m_lSpecifyTimePartThree;               //道路所需学时（含模拟及实操）单位： min
            m_StudentLogin_S.lSpecifyTimeForSimulate = m_lSpecifyTimeForSimulate;             //场地及道路模拟所需学时 单位： min
        }
        m_API.CB.TRCallBack(MSG_StudentLogin,m_StudentLogin_S,0,null);
        if(Stu.TTSPlay == 1 || m_TTSPlay == 1)
        {
            if(Stu.sPlayText != null)
            {
                m_API.CB.TRCallBack(MSG_TTSPlay,null,0,Stu.sPlayText);
            }
        }
        return 0;
    }


    /** Pri_StudentLogoutRes 学员登出应答
     * @param result [In]  学员登出
     * @param byStudentID [In] 学员编号
     * @return  0：成功，-1：出错*/
    public int Pri_StudentLogoutRes(short result, byte[] byStudentID)
    {
        if(!m_bStudentLogin)
        {
            return -1;
        }
        m_bStudentLogin = false;
        StudentLogout_S LogOut = m_API.new StudentLogout_S();
        LogOut.LoginResult = result;
        try {
            LogOut.byStudentID = new String(byStudentID,"GBK");
        }
        catch (UnsupportedEncodingException e)
        {e.printStackTrace();}
        if(result == 1)
        {
            if(m_nTrainObject == 1)
            {
                m_StudentLogin_S.lTrainTimePartTwoSimulate += m_lStudyContinuousTime;
                m_DB.DBAlter(m_StudentLogin_S.sStudentID,PTwoSimulate,m_StudentLogin_S.lTrainTimePartTwoSimulate);
            }
            else if(m_nTrainObject == 2)
            {
                m_StudentLogin_S.lTrainTimePartTwoPratical += m_lStudyContinuousTime;
                m_DB.DBAlter(m_StudentLogin_S.sStudentID,PTwoPratical, m_StudentLogin_S.lTrainTimePartTwoPratical);
            }
            else if(m_nTrainObject == 3)
            {
                m_StudentLogin_S.lTrainTimePartThreeSimulate += m_lStudyContinuousTime;
                m_DB.DBAlter(m_StudentLogin_S.sStudentID,PThreeSimulate,m_StudentLogin_S.lTrainTimePartThreeSimulate);
            }
            else if(m_nTrainObject == 4)
            {
                m_StudentLogin_S.lTrainTimePartThreePratical += m_lStudyContinuousTime;
                m_DB.DBAlter(m_StudentLogin_S.sStudentID,PThreePratical,m_StudentLogin_S.lTrainTimePartThreePratical);
            }
            m_DB.DBAlter(LogOut.byStudentID,TTotal, m_StudentLogin_S.lTrainTimeTotality + m_lStudyContinuousTime);
            m_DB.DBAlter(LogOut.byStudentID,MTotal, m_StudentLogin_S.lTrainMileTotality + m_nGPSMile);

            //记录数据清零
            m_lStudyStartTime = 0;
            m_lStudyContinuousTime = 0;
            m_nGPSMile = 0;
        }
        m_API.CB.TRCallBack(MSG_StudentLogout,LogOut,0,null);

        return 0;
    }


    /** Pri_SetPraticalPara 设置计时终端应用参数   注：根据函数返回值回复0x0501 设置禁训状态应答  返回值为0 回复成功，-1回复设置失败
     * @param ParaNO [In]  参数编号
     * @param para [In] 终端应用参数结构体
     * @return  0：成功，-1：出错, -2输入参数有误*/
    public int Pri_SetPraticalPara(short ParaNO, Net_PracticalPara_S para)
    {
        if(para == null)
        {
            return -2;
        }
        switch (ParaNO)
        {
            case 0:
            {
            }case 1:
        {
            m_TakePicInterval = para.TakePicInterval;
            SetConfig("m_TakePicInterval", String.valueOf(m_TakePicInterval),CONFIGPRO);
            if(ParaNO != 0)
            { break;}
        }case 2:
        {
            m_UploadingPicWay = para.UploadingPicWay;
            SetConfig("m_UploadingPicWay", String.valueOf(m_UploadingPicWay),CONFIGPRO);
            if(ParaNO != 0)
            { break;}
        }case 3:
        {
            m_TTSPlay = para.TTSPlay;
            SetConfig("m_TTSPlay", String.valueOf(m_TTSPlay),CONFIGPRO);
            if(ParaNO != 0)
            { break;}
        }case 4:
        {
            m_DeferTimeWhileShutdown = para.DeferTimeWhileShutdown;
            SetConfig("m_DeferTimeWhileShutdown", String.valueOf(m_DeferTimeWhileShutdown),CONFIGPRO);
            if(ParaNO != 0)
            { break;}
        }case 5:
        {
            m_UploadingGPSInterval = para.UploadingGPSInterval;
            SetConfig("m_UploadingGPSInterval", String.valueOf(m_UploadingGPSInterval),CONFIGPRO);
            if(ParaNO != 0)
            { break;}
        }case 6:
        {
            m_DeferTimeForCoachLogOut = para.DeferTimeForCoachLogOut;
            SetConfig("m_DeferTimeForCoachLogOut", String.valueOf(m_DeferTimeForCoachLogOut),CONFIGPRO);
            if(ParaNO != 0)
            { break;}
        }case 7:
        {
            m_ReVerifyTime = para.ReVerifyTime;
            SetConfig("m_ReVerifyTime", String.valueOf(m_ReVerifyTime),CONFIGPRO);
            if(ParaNO != 0)
            { break;}
        }
//           case 8:
//            {
//                m_OtherSchoolPermitOfCoach = para.OtherSchoolPermitOfCoach;
//                SetConfig("m_OtherSchoolPermitOfCoach", String.valueOf(m_OtherSchoolPermitOfCoach),CONFIGPRO);
//                break;
//            }case 9:
//            {
//                m_OtherSchoolPermitOfstudent = para.OtherSchoolPermitOfstudent;
//                SetConfig("m_OtherSchoolPermitOfstudent", String.valueOf(m_OtherSchoolPermitOfstudent),CONFIGPRO);
//                break;
//            }
            default:
            {
                break;
            }
        }
        return 0;
    }


    /** Pri_SetTrainingForbid 设置禁训状态   注：根据函数返回值回复0x0502设置计时终端应用参数应答 返回值为0 成功，-1回复设置失败  无默认应答，无提示消息
     * @param TrainingForbid [In]  1：可用，默认值；2：禁用
     * @param sText [In] 终端应用参数结构体
     * @return  0：成功，-1：出错*/
    public int Pri_SetTrainingForbid (short TrainingForbid, String sText)
    {
        m_TrainingForbid = TrainingForbid;
        SetConfig("m_TrainingForbid",String.valueOf(m_TrainingForbid),CONFIGPRO);
        //TODO 状态更新后的联动
        if(sText != null)
        {
            //TODO 提示信息有什么鬼用？
        }
        return 0;
    }

    /** Pri_SetPraticalPara 查询计时终端应用参数
     * @param para [Out] 终端应用参数结构体
     * @return  0：成功，-1：出错 -2 输入参数有误*/
    public int Pri_GetPraticalPara(Net_PracticalPara_S para)
    {
        if(para == null)
        {
            return -2;
        }
        para.TakePicInterval = m_TakePicInterval;
        para.UploadingPicWay = m_UploadingPicWay;
        para.TTSPlay = m_TTSPlay;
        para.DeferTimeWhileShutdown = m_DeferTimeWhileShutdown;
        para.UploadingGPSInterval = m_UploadingGPSInterval;
        para.DeferTimeForCoachLogOut = m_DeferTimeForCoachLogOut;
        para.ReVerifyTime = m_ReVerifyTime;
        para.OtherSchoolPermitOfCoach = m_OtherSchoolPermitOfCoach;
        para.OtherSchoolPermitOfstudent = m_OtherSchoolPermitOfstudent;
        return 0;
    }









    /***********************************类自用函数部分**********************************************/

    public synchronized int LoadConfig(String sName,StringBuffer sValue, String config)
    {
       // File filename=new File(m_context.getFilesDir(), CONFIGSYS);
        String readStr ="";
        BufferedReader bufread;
        int rt = 0;
        File filename=new File(m_context.getExternalFilesDir(DIRECTORY_DOCUMENTS),config);
        if(!filename.exists())
        {
            try {
                filename.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(sName != null)
        {
            try //检测参数sName是否在本类中存储，不存在返回-2
            {
                ClassLoader loader=Thread.currentThread().getContextClassLoader();
                Class clazz = loader.loadClass("com.timerecord.Primary");
                clazz.getDeclaredField(sName);
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (NoSuchFieldException e)
            {
                rt = -1;
               // System.out.println("rt1="+rt);
                e.printStackTrace();
                return rt;
            }
        }

       // System.out.println("rt2 ="+rt);
        String read;
        FileReader FR;
        try {
            FR = new FileReader(filename);
            bufread = new BufferedReader(FR);
            try {
                while ((read = bufread.readLine()) != null) {
                    readStr = readStr + read+ "\n";//TODO 换行符
                    int nOff = read.indexOf(";");
                    if(nOff != -1 && nOff < read.length())
                    {
                        String sParaName = read.substring(0,nOff);
                        String sParaValue = read.substring(nOff+1);
                     //   System.out.println("sParaName = " +sParaName+ "sParaValue = " + sParaValue);
                        if(sName == null) //加载所有参数到成员变量
                        {
                            configToPara(sParaName,sParaValue);
                        }
                        else if(sParaName.compareTo(sName) == 0) //目标获取的参数
                        {
                            sValue.append(sParaValue);
                            return 0;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(config + " content :"+System.getProperty("line.separator")+ readStr);
        System.out.println(config+" end");
        return rt;
    }

    public synchronized int SetConfig(String sParaName,String sParaValue,String config)
    {
        String temp;
        try //检测参数sParaName是否在本类中存储，不存在返回-2
        {
            ClassLoader loader=Thread.currentThread().getContextClassLoader();
            Class clazz = loader.loadClass("com.timerecord.Primary");
            clazz.getDeclaredField(sParaName);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
            return -1;
        }

        try
        {
           // File file = new File(m_context.getFilesDir(), CONFIGSYS);
            File file = new File(m_context.getExternalFilesDir(DIRECTORY_DOCUMENTS), config);
            if(!file.exists())
            {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            // 保存该行前面的内容
            for (int j = 1; (temp = br.readLine()) != null; j++)  //&& !temp.equals(sParaName)
            {
                int nOff = temp.indexOf(";");
                String sName = temp.substring(0,nOff);
                if(sName.equals(sParaName))
                {
                    break;
                }
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }

            // 将内容插入
            buf = buf.append(sParaName);
            buf = buf.append(";");
            buf = buf.append(sParaValue);

            // 保存该行后面的内容
            while ((temp = br.readLine()) != null) {
                buf = buf.append(System.getProperty("line.separator"));
                buf = buf.append(temp);
            }
            br.close();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return 0;
    }


    int configToPara(String ParaName, String ParaValue)
    {
        //用反射机制，由ParaName得到Para变量后设置ParaValue
        try
        {
            ClassLoader loader=Thread.currentThread().getContextClassLoader();
            Class clazz = loader.loadClass("com.timerecord.Primary");
            Field fl=clazz.getDeclaredField(ParaName);
            String sType = fl.getType().getSimpleName();
           // System.out.println(sType);
            fl.setAccessible(true);//取消java语言访问检查以访问private变量
            if(sType.compareTo("String") == 0)
            {
                fl.set(this, ParaValue);
            }
            else if(sType.compareTo("long") == 0 || sType.compareTo("int") == 0)
            {
                fl.set(this, Integer.parseInt(ParaValue));
            }
            else if(sType.compareTo("double") == 0)
            {
                fl.set(this, Double.parseDouble(ParaValue));
            }
            else if(sType.compareTo("char") == 0)
            {
                fl.set(this, ParaValue.charAt(0));
            }
            else if(sType.compareTo("byte[]") == 0)
            {
                fl.set(this,ParaValue.getBytes("UTF-8"));
            }
           else
            {
                System.out.println(sType+"was not a variable type");
            }
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            System.out.println("ParaName:"+ParaName);
            e.printStackTrace();
            return -2;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return 0;
    }


    /** GetTrainingCourse 获取学员训练课程编号
     * @param sStudentID 学员编号  统一编号
     * @param nTrainObject 培训项目 1-场地模拟，2-场地实操，3-道路模拟，4-道路实操
     * @return  0：成功*/
    private int GetTrainingCourse(String sStudentID, int nTrainObject, StringBuffer sbID)
    {
        try
        {
            if (nTrainObject == 1 || nTrainObject == 3)
            {
                sbID.append("3");
            }
            else if (nTrainObject == 2 || nTrainObject == 4)
            {
                sbID.append("1");
            }
            sbID.append(new String(m_byLearnLevel, "GBK"));

            StudentLogin_S Stu = m_API.new StudentLogin_S();
            m_DB.DBGetStudentTrain(sStudentID, Stu);
            long lTrainTimePartTwo = Stu.lTrainTimePartTwoPratical + Stu.lTrainTimePartTwoSimulate;
            long lTrainTimePartThree = Stu.lTrainTimePartThreePratical + Stu.lTrainTimePartThreeSimulate;
            long lSpecifyTimePartTwo = m_lSpecifyTimePartTwo;
            long lSpecifyTimePartThree = m_lSpecifyTimePartThree;
            String sCourseID = "11";
            if (nTrainObject <= 2)
            {
                sbID.append("2");
                if(lTrainTimePartTwo < lSpecifyTimePartTwo/3 )
                {
                    sCourseID = "11";
                }
                else if(lTrainTimePartTwo < lSpecifyTimePartTwo*2/3)
                {
                    sCourseID = "12";
                }
                else
                {
                    sCourseID = "13";
                }
            }
            else if (nTrainObject >= 3)
            {
                sbID.append("3");
                if(lTrainTimePartThree < lSpecifyTimePartThree/16)
                {
                    sCourseID = "21";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*2/16)
                {
                    sCourseID = "22";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*3/16)
                {
                    sCourseID = "23";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*4/16)
                {
                    sCourseID = "24";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*5/16)
                {
                    sCourseID = "25";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*6/16)
                {
                    sCourseID = "26";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*7/16)
                {
                    sCourseID = "27";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*8/16)
                {
                    sCourseID = "28";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*9/16)
                {
                    sCourseID = "29";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*10/16)
                {
                    sCourseID = "30";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*11/16)
                {
                    sCourseID = "31";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*12/16)
                {
                    sCourseID = "32";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*13/16)
                {
                    sCourseID = "33";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*14/16)
                {
                    sCourseID = "34";
                }
                else if(lTrainTimePartThree < lSpecifyTimePartThree*15/16)
                {
                    sCourseID = "35";
                }
                else
                {
                    sCourseID = "36";
                }

            }
            sbID.append(sCourseID);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        System.out.println(sbID.toString());

            return 0;
    }


    /** GetStudyRecordNO 获取学习记录编号
     * @return  0：本机正常登录（不代表登录平台成功），-1：登录失败，-2：传入参数有误*/
    private int GetStudyRecordNO( byte[] Src)
    {
        int nOffset = m_byTerminalID.length;
        System.arraycopy(m_byTerminalID,0,Src,0,nOffset);
        try
        {
            System.arraycopy(m_sDayRecordSN.getBytes("GBK"), 0, Src, nOffset, 6);
            nOffset += 6;
            String strTemp = String.format("%04d", m_nRecordSN);
           // System.out.println("Sn = : " + strTemp);
            System.arraycopy(strTemp.getBytes("GBK"), 0, Src, nOffset, 4);
            m_nRecordSN++;
            SetConfig("m_nRecordSN", strTemp, CONFIGSYS);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return  0;
    }

    private int ReadyToGetStudyRecordNO()
    {
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("yyMMdd");
        String now = ft.format(dNow).toString();
        if(m_sDayRecordSN.compareTo(now) != 0) //设备没有今天的学习发送记录(那应该是之前的，m_nRecordSN 清零)
        {
            m_nRecordSN = 0;
            SetConfig("m_sDayRecordSN",now,CONFIGSYS);
            SetConfig("m_nRecordSN","0",CONFIGSYS);
        }
        else //今天有发送记录，读取到m_nRecordSN
        {
            StringBuffer sb = new StringBuffer();
            LoadConfig("m_nRecordSN",sb,CONFIGSYS);
            m_nRecordSN = Integer.parseInt(sb.toString());
        }
        m_sDayRecordSN = now;
        return 0;
    }

    private int GetGPS(Net_GPS_S gps) //TODO 获取实际的GPS
    {
        gps.lAlarmSet = m_lAlarmSet;
        gps.lStatusSet = m_lStatusSet;
        gps.lLatitude = 110123456L;
        gps.lLongitude = 22123456L;
        gps.nOBDSpeed = 20;
        gps.nGPSSpeed = 18;
        gps.nDirection = 100;
        return 0;
    }










    /***********************************UI交互部分**********************************************/
    /** UI_TerminalRegister 终端注册
     * @param sVehicleLicenceID 公安交通管理部门颁发的机动车号牌  !!@@
     * @param lPlatColor    车牌颜色 0：未上牌1：蓝色 2：黄色 3：黑色 4：百色9：其他
     * @return  0：成功向平台发送注册信息（不代表注册成功），-1：注册出错，-2：传入参数有误， -4：及以下：其他错误*/
    public int UI_TerminalRegister(String sVehicleLicenceID, long lPlatColor)
    {
        int rt = 0;
        TelephonyManager tm = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);
        String DEVICE_ID = tm.getDeviceId();
       // System.out.println(DEVICE_ID);
        byte[] byDeviceID = new byte[15];
        try {
            byDeviceID = DEVICE_ID.getBytes("GBK");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        byte[]  byFactoryNumber = new byte[7]; //TODO 7 个字节，由大写字母和数字组成，此终端 ID 由制造商自行定义，位数不足时，后补“0X00”
        Net_TerminalRegister((int)m_lVehicleProvinceID,(int)m_lVehicleCityID ,m_byManufacturerID, m_byTerminalID, byFactoryNumber, byDeviceID, (short)lPlatColor, sVehicleLicenceID);
        return rt;
    }

    /** UI_TerminalLogout 终端注销 无需平台应答
     * @return  0：注销成功（无需平台应答），-1：注销出错， -4：及以下：其他错误*/
    public int UI_TerminalLogout()
    {
        Net_TerminalLogOff();
        return 0;
    }

    /** UI_CoachLogin 教练员登录
     * @param sCoachID 教练员编号  统一编号
     * @param byCoachIdentity 教练员身份证号ASCII 码，不足 18 位前补 0x00
     * @param byCoachLevel 准教车型A1\A2\A3\B1\B2\C1\C2\C3\C4\D\E\F
     * @return  0：本机正常登录（不代表登录平台成功），-1：登录出错，-2：传入参数有误*/
    public int UI_CoachLogin(String sCoachID, byte[] byCoachIdentity, byte[] byCoachLevel)
    {
         if(byCoachIdentity.length > 18 || byCoachLevel.length > 2)
        {
            return -2;
        }
        if(m_bCoachLogin)  //已登录
        {
            return 0;
        }
        //System.arraycopy(byCoachIdentity,0,m_byCoachIdentity,0,byCoachIdentity.length);
        //System.arraycopy(byCoachLevel,0,m_byCoachLevel,0,byCoachIdentity.length);
        byte[] byCoachID = new byte[16];          //教练员编号  统一编号
        try
        {
            byCoachID = sCoachID.getBytes("GBK");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        Net_GPS_S gps = new Net_GPS_S();
        GetGPS(gps);
        Net_CoachLogin(byCoachID,byCoachIdentity,byCoachLevel,gps);

        return 0;
    }


    /** UI_CoachLogout 教练员登出
     * @param sCoachID 教练员编号  统一编号
     * @return  0：本机正常登出（不代表登出平台成功），-1：登出失败，-2：传入参数有误*/
    public int UI_CoachLogout(String sCoachID)
    {
        if(!m_bCoachLogin)  //未登录
        {
            return 0;
        }
        byte[] byCoachID = new byte[16];          //教练员编号  统一编号
        try
        {
            byCoachID = sCoachID.getBytes("GBK");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        Net_GPS_S gps = new Net_GPS_S();
        GetGPS(gps);
        Net_CoachLogout(byCoachID,gps);
        return 0;
    }



    /** UI_StudentLogin 学员登录
     * @param sStudentID 学员编号  统一编号
     * @param sStudentsCoach 当前教练编号  统一编号
     *@param byLearnLevel 学员报考驾照类型A1\A2\A3\B1\B2\C1\C2\C3\C4\D\E\F   GBK 编码
     * @param nTrainObject 培训项目 1-场地模拟，2-场地实操，3-道路模拟，4-道路实操
     * @return  0：本机正常登录（不代表登录平台成功），-1：登录失败，-2：传入参数有误*/
    public int UI_StudentLogin(String sStudentID, String sStudentsCoach, byte[]byLearnLevel,int nTrainObject)
    {
        if(m_bStudentLogin)  //已登录
        {
            return 0;
        }
        m_nTrainObject = nTrainObject;
        int A1PTwo = 36;int A1PThree = 20;
        int A2PTwo = 40;int A2PThree= 22;
        int A3PTwo = 53;int A3PThree= 33;
        int B2PTwo = 54;int B2PThree= 32;
        int C1PTwo = 16;int C1PThree= 24;
        int C2PTwo = 14;int C2PThree= 24;
        int C3PTwo = 14;int C3PThree= 16;
        int C4PTwo = 10;int C4PThree= 10;
        int C5PTwo = 16;int C5PThree= 24;

        if(nTrainObject < 1 || nTrainObject >4)
        {return -2;}
        if(byLearnLevel[0] == 'A' && byLearnLevel[1] == '1' || byLearnLevel[0] == 'B' && byLearnLevel[1] == '1')
        {
            m_lSpecifyTimeTotality = A1PTwo + A1PThree;
            m_lSpecifyTimePartTwo = A1PTwo;
            m_lSpecifyTimePartThree = A1PThree;
        }
        else if(byLearnLevel[0] == 'A' && byLearnLevel[1] == '2')
        {
            m_lSpecifyTimeTotality = A2PTwo + A2PThree;
            m_lSpecifyTimePartTwo = A2PTwo;
            m_lSpecifyTimePartThree = A2PThree;
        }
        else if(byLearnLevel[0] == 'A' && byLearnLevel[1] == '3')
        {
            m_lSpecifyTimeTotality = A3PTwo + A3PThree;
            m_lSpecifyTimePartTwo = A3PTwo;
            m_lSpecifyTimePartThree = A3PThree;
        }
        else if(byLearnLevel[0] == 'B' && byLearnLevel[1] == '2')
        {
            m_lSpecifyTimeTotality = B2PTwo + B2PThree;
            m_lSpecifyTimePartTwo = B2PTwo;
            m_lSpecifyTimePartThree = B2PThree;
        }
        else if(byLearnLevel[0] == 'C' && byLearnLevel[1] == '1')
        {
            m_lSpecifyTimeTotality = C1PTwo + C1PThree;
            m_lSpecifyTimePartTwo = C1PTwo;
            m_lSpecifyTimePartThree = C1PThree;
        }
        else if(byLearnLevel[0] == 'C' && byLearnLevel[1] == '2')
        {
            m_lSpecifyTimeTotality = C2PTwo + C2PThree;
            m_lSpecifyTimePartTwo = C2PTwo;
            m_lSpecifyTimePartThree = C2PThree;
        }
        else if(byLearnLevel[0] == 'C' && byLearnLevel[1] == '3')
        {
            m_lSpecifyTimeTotality = C3PTwo + C3PThree;
            m_lSpecifyTimePartTwo = C3PTwo;
            m_lSpecifyTimePartThree = C3PThree;
        }
        else if(byLearnLevel[0] == 'C' && byLearnLevel[1] == '4')
        {
            m_lSpecifyTimeTotality = C4PTwo + C4PThree;
            m_lSpecifyTimePartTwo = C4PTwo;
            m_lSpecifyTimePartThree = C4PThree;
        }
        else if(byLearnLevel[0] == 'C' && byLearnLevel[1] == '5' || byLearnLevel[0] == 'D' || byLearnLevel[0] == 'E' || byLearnLevel[0] == 'F')
        {
            m_lSpecifyTimeTotality = C5PTwo + C5PThree;
            m_lSpecifyTimePartTwo = C5PTwo;
            m_lSpecifyTimePartThree = C5PThree;
        }

        System.arraycopy(byLearnLevel,0,m_byLearnLevel,0,2);
        StringBuffer sb = new StringBuffer();
        byte[] byStudentID = new byte[16];          //xue员编号  统一编号
        try
        {
            byStudentID = sStudentID.getBytes("GBK");
            GetTrainingCourse(sStudentID, nTrainObject, sb);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        Net_GPS_S gps = new Net_GPS_S();
        GetGPS(gps);
        Net_StudentLogin(byStudentID, m_byCoachID, sb.toString(), m_lClassID, gps);
        return 0;
    }

    /** UI_StudentLogout 学员登出
     * @param sStudentID 学员编号  统一编号
     * @param nTrainObject 培训项目 1-场地模拟，2-场地实操，3-道路模拟，4-道路实操
     * @return  0：本机正常登出（不代表登出平台成功），-1 登出失败，-2 传入参数有误*/
    public int UI_StudentLogout(String sStudentID,int nTrainObject)
    {
        if(nTrainObject < 1 || nTrainObject >4)
        {return -2;}
        if(!m_bStudentLogin)  //未登录
        {
            return 0;
        }
        byte[] byStudentID = new byte[16];          //学员编号  统一编号
        try
        {
            byStudentID = sStudentID.getBytes("GBK");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        if(m_lStudyStartTime != 0)
        {
            long StudyEnd = new Date().getTime();
            long diff = StudyEnd - m_lStudyStartTime;
            m_lStudyContinuousTime = diff/60000 + (diff/1000%60 > 30 ? 1 : 0);
            //System.out.println("StudyTime = "+ m_lStudyContinuousTime);
        }
        Net_GPS_S gps = new Net_GPS_S();
        GetGPS(gps);
        Net_StudentLogout(byStudentID, (int)m_lStudyContinuousTime, m_nGPSMile, m_lClassID, gps);
        return 0;
    }

    /** UI_GetNetServerPara 获取服务器网络参数
     * @return  0：正常获取返回NetServerPara_S对象引用，null：获取失败*/
    public NetServerPara_S  UI_GetNetServerPara()
    {
        NetServerPara_S Para = m_API.new NetServerPara_S();
        Para.sMainServerAPN = m_sMainServerAPN;
        Para.sMainServerUser = m_sMainServerUser;
        Para.sMainServerPassWord =  m_sMainServerPassWord;
        Para.sMainServerDomain = m_sMainServerDomain;
        Para.lMainServerPort = m_lMainServerPort;
        Para.sSubServerAPN =  m_sSubServerAPN;
        Para.sSubServerUser =  m_sSubServerUser;
        Para.sSubServerPassWord = m_sSubServerPassWord;
        Para.sSubServerDomain = m_sSubServerDomain;
        Para.lSubServerPort = m_lSubServerPort;
        return Para;
    }

    /** UI_SetNetServerPara 设置服务器网络参数
     * @return  0：设置成功，-1：获取失败，-2：传入参数有误，-4及以下：其他错误*/
    public int UI_SetNetServerPara(NetServerPara_S para)
    {
        m_sMainServerAPN = para.sMainServerAPN    ;
        m_sMainServerUser = para.sMainServerUser   ;
        m_sMainServerPassWord = para.sMainServerPassWord;
        m_sMainServerDomain = para.sMainServerDomain ;
        m_lMainServerPort = para.lMainServerPort   ;
        m_sSubServerAPN = para.sSubServerAPN     ;
        m_sSubServerUser = para.sSubServerUser    ;
        m_sSubServerPassWord = para.sSubServerPassWord;
        m_sSubServerDomain = para.sSubServerDomain  ;
        m_lSubServerPort = para.lSubServerPort    ;
        return 0;
    }

    /** UI_GetSysStatus 获取系统状态参数
     * @return  0：正常获取返回SysStatus_S对象引用，null：获取失败*/
    public SysStatus_S UI_GetSysStatus()
    {
        //m_API.CB.TRCallBack(MSG_SysStatus, m_Status, 0, null);
        SysStatus_S obj = (SysStatus_S)m_Status.clone();
        return obj;
    }


    public int SetSystemInfo()
    {
        m_SysInfo.sAPPVersion = "1.0.0.Beta";
        m_SysInfo.sOBDVersion = "1.0.0";
        m_SysInfo.sSIMNo = "13122223333";
        m_SysInfo.sPlatformID = "666666";
        m_SysInfo.sOrganizationID="12345";
        m_SysInfo.sTerminnalID = "88888888";
        return 0;
    }
    /** UIGetInfo 获取系统状态参数
     * @return  0：正常获取返回SysInfo_S对象引用，null：获取失败*/
    public SysInfo_S UI_GetInfo()
    {
        SysInfo_S Sys = (SysInfo_S)m_SysInfo.clone();
        return Sys;
    }

    /** UI_GetFaceRecognizeAccount获取脸部识别登录账户
     * @return  0：正常获取返回FaceRecognizeAccount_S对象引用，null：获取失败*/
    public FaceRecognizeAccount_S UI_GetFaceRecognizeAccount()
    {
        FaceRecognizeAccount_S Face = m_API.new FaceRecognizeAccount_S();
        Face.sApiSecret = m_Face.sApiSecret;
        Face.sApiKey = m_Face.sApiKey;
        return Face;
    }

}


