package com.timerecord;

import java.lang.reflect.MalformedParameterizedTypeException;

/**
 * Created by Hugh on 2017/1/1.
 */

public class GPSAndOBD {
    private volatile boolean bExit;
    Primary mPri;
    public GPSAndOBD(Primary pri)
    {
        mPri = pri;
    }

    public int start()
    {
        StudyRecordPro();
        return 0;
    }
    public int End()
    {
        bExit = true;
        return 0;
    }
    public int StudyRecordPro()
    {
        //每分钟通过调用SendStudyRecord记录一次学习记录到数据库，并上传学习记录
        new Thread(new Runnable()
        {
            public void run()
            {
                try {
                    while (!bExit)
                    {
                        Primary.Net_GPS_S gps  =  mPri.new Net_GPS_S();
                        gps.lAlarmSet = 0;
                        gps.lStatusSet = 0;
                        gps.lLatitude = 110123456L;
                        gps.lLongitude = 22123456L;
                        gps.nOBDSpeed = 20;
                        gps.nGPSSpeed = 18;
                        gps.nDirection = 100;
                        mPri.SendStudyRecord(30,5,gps);
                    }
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return 0;
    }
}
