package com.timerecord;

import android.content.Context;

import java.util.Objects;

/**
 *计时终端功能库的主接口类，外部调用本功能库需要通过本类提供的接口完成
 * 为了达到库的内部消息能主动外送，外部调用者需实现本库中的特定消息接口MsgCallBack
 * 3个变量以上的结构体(类)支持clone()复制对象
 * @author Hugh
 * @Time 2016/12/12
 */



public class TimeRecordAPI
{
    private Primary Pri = null;
    public MsgCallBack CB; //回调接口



    /**MsgCallBack接口中的消息类型*/
    public enum enMsgType {
                                                        /*↓对应结构体↓*/
        MSG_TerminalRegister,   //终端注册平台回应   无对应结构体，参数nValue为应答结果 0：成功;1：车辆已被注册;2：数据库中无该车辆;3：终端已被注册;4：数据库中无该终端
        MSG_CoachLogin,         //教练登录平台回应,     CoachLogin_S
        MSG_CoachLogout,        //教练登出平台回应      CoachLogout_S
        MSG_StudentLogin,       //学员登录平台回应      StudentLogin_S
        MSG_StudentLogout,      //学员登出平台回应      StudentLogout_S
        MSG_TTSPlay,            //TTS播报   参数sStr为待TTS播报文本，无对应结构体
        MSG_SysStatus,          //系统状态              SysStatus_S
        //MSG_SysInfo,            //系统信息              SysInfo_S
        MSG_GPSInfo,            //GPS信息               GPSInfo_S
        MSG_CustomizedMsg,      //库自定义提示信息      CustomizedMsg_S
        MSG_PicBuffer,          //图像数据内存
        MSG_APPClose,           //程序关闭信息，收到后关闭APP，无对应结构体
    }

   // /**本文中所有消息结构体（实际为类）的父类*/
   // public class SuperStruct  {}

    /**MsgCallBack 回调接口
     * 库对外部调用者的信息回调接口，外部调用者需implements本接口
     * param Msg    消息类型，指示回调接口返回的消息类别，调用者根据类型枚举enMsgType的定义，将struct转换成实际的结构体或直接获取nValue与sStr数值
     * param struct 返回的是实际类的引用，如CoachLogin_S、StudentLogin_S等，调用者需进行类型转换
     * param nValue 整型数值，某些简单的数据可通过该值返回（无需定义结构体）
     * param sStr 字符型数据，某些简单的字符型数据可通过该值返回（无需定义结构体）
     * @return  TODO 返回类型用途说明*/
    public interface MsgCallBack
    {
        int TRCallBack (enMsgType Msg, Object struct, int nValue, String sStr);
    }

    /**教练员登录结构体*/
    public class CoachLogin_S
    {
        public short LoginResult;               // 1：登录成功;2：无效的教练员编号;3：准教车型不符；9：其他错误
        String sCoachID;                        //教练员编号  统一编号
    }

    /**教练员登出结构体*/
    public class CoachLogout_S
    {
        public short LoginResult;               // 1：登出成功；2：登出失败
        String sCoachID;                        //教练员编号  统一编号
    }

    /**学员登录结构体*/
    public class StudentLogin_S implements Cloneable
    {
        public short LoginResult;  // 1：登录成功；2：无效的学员编号；3：禁止登录的学员；4：区域外教学提醒；5：准教车型与培训车型不符；9：其他错/
                                    //注：若登陆失败后续内容为空
        public String sStudentID;                       //学员编号  统一编号
        public long lSpecifyTimeTotality;                //场地及道路总需培训学时  单位： min
        public long lSpecifyMileTotality;                //场地及道路总需培训里程  单位： km
        public long lSpecifyTimePartTwo;                 //场地所需学时（含模拟及实操） 单位： min
        public long lSpecifyTimePartThree;               //道路所需学时（含模拟及实操）单位： min
        public long lSpecifyTimeForSimulate;             //场地及道路模拟所需学时 单位： min

        public long lTrainTimeTotality;                  //总培训学时 单位： min
        public long lTrainMileTotality;                  //总培训里程 单位： 1/10km  (100米)
        public long lTrainTimePartTwoSimulate;           //场地模拟已培训学时 单位： min
        public long lTrainTimePartTwoPratical;           //场地实操已培训学时 单位： min
        public long lTrainTimePartThreeSimulate;         //道路模拟已培训学时 单位： min
        public long lTrainTimePartThreePratical;         //道路实操已培训学时 单位： min
        @Override
        public Object clone()
        {
            StudentLogin_S obj = null;
            try
            {
                obj = (StudentLogin_S)super.clone();
            }
            catch(CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
            return obj;
        }
    }

    /**学员登出结构体*/
    public class StudentLogout_S
    {
        public short LoginResult;           // 1：登出成功；2：登出失败
        public String byStudentID;          //学员编号  统一编号
    }

    /**服务平台网络结构体*/
    public class NetServerPara_S implements Cloneable
    {
        public String sMainServerAPN;                 //主服务器APN
        public String sMainServerUser;                //主服务器账号
        public String sMainServerPassWord;            //主服务器密码
        public String sMainServerDomain;              //主服务器域名或IP
        public long lMainServerPort;                  //主服务器端口
        public String sSubServerAPN;                  //备份服务器APN
        public String sSubServerUser;                 //备份务器账号
        public String sSubServerPassWord;             //备份务器密码
        public String sSubServerDomain;               //备份服务器域名或IP
        public long lSubServerPort;                   //备份服务器端口

        @Override
        public Object clone()
        {
            NetServerPara_S obj = null;
            try
            {
                obj = (NetServerPara_S)super.clone();
            }
            catch(CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
            return obj;
        }
    }

    /**系统状态结构体*/
    public class SysStatus_S implements Cloneable
    {
        public int nGPRSStatus;		                  //拨号状态    1:已拨号，0：未拨号
        public int nSignalIntensity;;                 //3G网络信号强度  0-100;
        public int nGPSAvailable;		              //GPS状态；  1：可用，0：不可用
        public String sGPRS_IP;			              //拨号IP
        public int nSateliteNum;		              //卫星数
        public int nPlatformConnection;               //计时平台链接状态 1：已连接，0：未连接
        public OBD_S OBD = new OBD_S();

        /**OBD数据结构体*/
        public class OBD_S implements Cloneable
        {
            public int nSpeed;
            public int nRotateSpeed;
            //档位
            //角加速度
            //水平误差
            //俯仰角
            //横滚角
            //行驶角
            //姿态航向
            //前距
            //后距
            @Override
            public Object clone()
            {
                OBD_S ob = null;
                try
                {
                    ob = (OBD_S)super.clone();
                }
                catch(CloneNotSupportedException e)
                {
                    e.printStackTrace();
                }
                return ob;
            }
        }
        @Override
        public Object clone()
        {
            SysStatus_S obj = null;
            try
            {
                obj = (SysStatus_S)super.clone();
                obj.OBD = (OBD_S) OBD.clone();
            }
            catch(CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
            return obj;
        }
    }

    /**系统信息结构体*/
    public class SysInfo_S implements Cloneable
    {
        public String sAPPVersion;                  //APP版本
        public String sOBDVersion;                  //OBD版本
        public String sSIMNo;                       //SIM卡卡号
        public String sPlatformID;                  //平台编号 统一编号
        public String sOrganizationID;              //培训机构编号 统一编号
        public String sTerminnalID;                 //计时终端编号 统一编号
        //待追加

        @Override
        public Object clone()
        {
            SysInfo_S obj = null;
            try
            {
                obj = (SysInfo_S)super.clone();
            }
            catch(CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
            return obj;
        }
    }


    /**GPS信息结构体*/
    public class GPSInfo_S implements Cloneable
    {
        public char cGPSAvailable;		            //GPS状态；  A:有效  V:无效
        public char cLatitudeNS;			        //南北纬度   N:北纬；S：南纬
        public char cLongitudeEW;		            //东西经度； E:东经；W：西经
        public double dLatitude;			        //纬度值 dd.dddddd
        public double dLongitude;		            //经度值 ddd.dddddd
        public double dSpeed;			            //速度;  xxx.xx(Km/h)
        public int nDirection;			            //GPS方向   0-359度
        @Override
        public Object clone()
        {
            GPSInfo_S obj = null;
            try
            {
                obj = (GPSInfo_S)super.clone();
            }
            catch(CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
            return obj;
        }
    }

    /**自定义提示信息结构体*/
    public class CustomizedMsg
    {
        public int nMsgCategory;            //信息类别  1：状态栏提示信息,2：串口弹出类型信息,3：其他类型信息
        public String sMsg;                 //具体信息内容
    }

    /**脸部识别账号结构体*/
    public class  FaceRecognizeAccount_S
    {
        public String sApiKey;              //API key
        public String sApiSecret;           //API Secret
    }


    /** TR_Init 初始化函数，在使用本库其他函数之前，务必调用该函数进行初始化
     * @param CB 公安交通管理部门颁发的机动车号牌  !!@@
     * @return  0：初始化成，1：已初始化，-1初始化失败，-2及以下：其他错误*/
    public  int TR_Init(MsgCallBack CB,Context context)
    {
        if(Pri != null)
        {
            return 1;
        }
        Pri = new Primary(this,context);
        this.CB = CB;
        int rt = Pri.StartBusiness();
        return rt;
    }

    /** TR_CleanUp 反初始化函数，在使用结束或程序终止前，请调用本函数进行资源回收
     * @return  void */
    public int TR_CleanUp()
    {
        Pri.EndBusiness();
        return 0;
    }


    /** TR_TerminalRegister 终端注册 平台终端注册结果在回调接口中的信息类型为MSG_TerminalRegister
     * @param sVehicleLicenceID [In]公安交通管理部门颁发的机动车号牌  !!@@
     * @param lPlatColor [In] 车牌颜色 0：未上牌1：蓝色 2：黄色 3：黑色 4：百色9：其他
     * @return  0：成功向平台发送注册信息（不代表注册成功），-1：注册出错，-2：传入参数有误，-3：库未初始化， -4：及以下：其他错误*/
    public int TR_TerminalRegister(String sVehicleLicenceID, long lPlatColor)
    {
        if(Pri == null)
        {
            return -3;
        }
        if(sVehicleLicenceID == null || lPlatColor < 0)
        {
            return -2;
        }
        int rt =Pri.UI_TerminalRegister(sVehicleLicenceID, lPlatColor);
        return rt;
    }

    /** TR_TerminalLogout 终端注销 无需平台应答
     * @return  0：注销成功（无需平台应答），-1：注销出错，-3：库未初始化， -4：及以下：其他错误*/
    public int TR_TerminalLogout()
    {
        if(Pri == null)
        {
            return -3;
        }
        int rt =Pri.UI_TerminalLogout();
        return rt;
    }

    /** TR_CoachLogin 教练员登录 ,平台教练登录结果在回调接口中的信息类型为MSG_CoachLogin
     * @param sCoachID 教练员编号  统一编号
     * @param byCoachIdentity 教练员身份证号ASCII 码，不足 18 位前补 0x00  GBK编码
     * @param byCoachLevel 准教车型A1\A2\A3\B1\B2\C1\C2\C3\C4\D\E\F   GBK 编码
     * @return  0：成功向平台发送登录信息（不代表登录成功），-1：登录出错，-2：传入参数有误,-3：库未初始化*/
    public int TR_CoachLogin(String sCoachID, byte[] byCoachIdentity, byte[] byCoachLevel)
    {
        if(Pri == null)
        {
            return -3;
        }
        if(sCoachID == null || byCoachIdentity == null || byCoachLevel == null)
        {
            return -2;
        }

        int rt = Pri.UI_CoachLogin(sCoachID,byCoachIdentity,byCoachLevel);
        return rt;
    }

    /** TR_CoachLogin 教练员登出 平台教练登出结果在回调接口中的信息类型为MSG_CoachLogout
     * @param sCoachID 教练员编号  统一编号
     * @return  0：成功向平台发送登出信息（不代表登出成功），-1：登出出错，-2：传入参数有误，-3：库未初始化*/
    public int TR_CoachLogout(String sCoachID)
    {
        if(Pri == null)
        {
            return -3;
        }
        if(sCoachID == null)
        {
            return -2;
        }
        int rt = Pri.UI_CoachLogout(sCoachID);
        return rt;
    }

    /** TR_CoachLogin 学员登录 平台学员登出结果在回调接口中的信息类型为MSG_StudentLogin
    * @param sStudentID 学员编号  统一编号
    * @param sStudentsCoach 当前教练编号  统一编
    *@param byLearnLevel 学员报考驾照类型A1\A2\A3\B1\B2\C1\C2\C3\C4\D\E\F   GBK 编码      //TODO 增加了该参数
    * @param nTrainObject 培训项目 1-场地模拟，2-场地实操，3-道路模拟，4-道路实操
    * @return  0：成功向平台发送登录信息（不代表登录成功），-1：登录失败，-2：传入参数有误，-3：库未初始化*/
public int TR_StudentLogin(String sStudentID, String sStudentsCoach,byte[] byLearnLevel, int nTrainObject)
{
    if(Pri == null)
    {
        return -3;
    }
    if(sStudentID == null || sStudentsCoach == null || nTrainObject <= 0)
    {
        return -2;
    }

    int rt = Pri.UI_StudentLogin(sStudentID,sStudentsCoach,byLearnLevel,nTrainObject);
    return rt;
}


    /** TR_CoachLogin 学员登出 平台学员登出结果在回调接口中的信息类型为MSG_StudentLogin
     * @param sStudentID 学员编号  统一编号
     * @param nTrainObject 培训项目 1-场地模拟，2-场地实操，3-道路模拟，4-道路实操
     * @return  0：成功向平台发送登出信息（不代表登出成功），-1：登出出错，-2：传入参数有误，-3：库未初始化*/
    public int TR_StudentLogout(String sStudentID,int nTrainObject)
    {
        if(Pri == null)
        {
            return -3;
        }
        if(sStudentID == null || nTrainObject <= 0)
        {
            return -2;
        }

        int rt = Pri.UI_StudentLogout(sStudentID,nTrainObject);
        return rt;
    }

    /** TR_GetNetServerPara 获取服务器网络参数
     * @return  0：正常获取返回NetServerPara_S对象引用，null：获取失败*/
    public NetServerPara_S TR_GetNetServerPara()
    {
        if(Pri == null)
        {
            return null;
        }
        return Pri.UI_GetNetServerPara();
    }

    /** TR_SetNetServerPara 设置服务器网络参数
     * @param para 服务器网络参数结构体 NetServerPara_S
     * @return  0：设置成功，-1：获取失败，-2：传入参数有误，-3：库未初始化， -4：及以下：其他错误*/
    public int TR_SetNetServerPara(NetServerPara_S para)
    {
        if(Pri == null)
        {
            return -3;
        }
        if(para == null)
        {
            return -2;
        }
        int rt = Pri.UI_SetNetServerPara(para);
        return rt;
    }

    /** TR_GetSysStatus 获取系统状态参数
     * @return  0：正常获取返回SysStatus_S对象引用，null：获取失败*/
    public SysStatus_S TR_GetSysStatus()
    {
        if(Pri == null)
        {
            return null;
        }
        return  Pri.UI_GetSysStatus();
    }


    /** TR_GetInfo 获取系统信息
     * @return  0：正常获取返回SysInfo_S对象引用，null：获取失败*/
    public SysInfo_S TR_GetInfo()
    {
        if(Pri == null)
        {
            return null;
        }
        return Pri.UI_GetInfo();
    }


    /** TR_GetFaceRecognizeAccount获取脸部识别登录账户
     * @return  0：正常获取返回FaceRecognizeAccount_S对象引用，null：获取失败*/
    public FaceRecognizeAccount_S TR_GetFaceRecognizeAccount()
    {
        if(Pri == null)
        {
            return null;
        }
        return Pri.UI_GetFaceRecognizeAccount();
    }

}


