package com.timerecord;

import com.timerecord.TimeRecordAPI.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

/**
 * 数据库类 sqlite3
 * @author Hugh
 * @Time 2016/12/12
 */
public class DatabasePro
{
    public enum Column
    {
        TTotal,
        MTotal,
        PTwoSimulate,
        PTwoPratical,
        PThreeSimulate,
        PThreePratical
    }
    final static String[] m_sColum = new String[]{"TTotal","MTotal","PTwoSimulate","PTwoPratical","PThreeSimulate","PThreePratical"};
    SQLiteDatabase m_DB;
    Context m_context;
    String m_sPathDB;
    Column m_Colunm;
    static String TABLE = "Train";
    public DatabasePro(Context context)
    {
        m_context = context;
        //m_sPathDB = context.getFilesDir().toString()+ File.separator+"TRDB.db";
        m_sPathDB = context.getExternalFilesDir(DIRECTORY_DOCUMENTS).toString()+ File.separator+"TRDB.db";
        System.out.println(m_sPathDB);
    }
    int InitDB()
    {
        m_DB = SQLiteDatabase.openOrCreateDatabase(m_sPathDB,null);
        String sCre="CREATE TABLE IF NOT EXISTS train(StudentID text primary key ,TTotal INTEGER,MTotal INTEGER,PTwoSimulate INTEGER," +
                "PTwoPratical INTEGER, PThreeSimulate INTEGER,PThreePratical INTEGER)";
        m_DB.execSQL(sCre);

        return 0;
    }

    int DBInsert(String sStudentID, long TTotal,  long MTotal,long PTwoSimulate, long PTwoPratical, long PThreeSimulate, long PThreePratical)
    {
        if(DBSelect(sStudentID) == 0) //sStudentID exist!
        {
            return -1;
        }
        StringBuffer str = new StringBuffer("INSERT INTO train");
        str.append("(StudentID,TTotal,MTotal,PTwoSimulate,PTwoPratical,PThreeSimulate,PThreePratical) VALUES('");
        str.append(sStudentID);
        str.append("','");
        str.append(String.valueOf(TTotal));
        str.append("','");
        str.append(String.valueOf(MTotal));
        str.append("','");
        str.append(String.valueOf(PTwoSimulate));
        str.append("','");
        str.append(String.valueOf(PTwoPratical));
        str.append("','");
        str.append(String.valueOf(PThreeSimulate));
        str.append("','");
        str.append(String.valueOf(PThreePratical));
        str.append("')");
        System.out.println(str.toString());
        m_DB.execSQL(str.toString());

//        //实例化常量值
//        ContentValues cValue = new ContentValues();
//        //添加用户名
//        cValue.put("name","1112");
//        //添加密码
//        cValue.put("DaBai","01005");
//        //调用insert()方法插入数据
//        m_DB.insert("train",null,cValue);
        return 0;
    }

    int DBSelect(String sStudentID)
    {
        int rt = -1;
        if(sStudentID == null || sStudentID.compareToIgnoreCase("ALL") == 0)
        {
            //查询获得游标
            Cursor cursor = m_DB.query ("train",null,null,null,null,null,null);

            //判断游标是否为空
            if(cursor.moveToFirst())
            {
                int nCount = cursor.getCount();
                System.out.println(cursor.getCount());
                for(int i=0;i<nCount;i++)//遍历游标
                {
                    StringBuffer StrBuf = new StringBuffer();
                    StrBuf.append(cursor.getString(0));
                    for(int j=1; j != 7; j++)
                    {
                        StrBuf.append("|");
                        StrBuf.append(cursor.getInt(j));
                    }
                   // StrBuf.
                    System.out.println(StrBuf.toString());
                    cursor.moveToNext();
                }

            }
            rt = 0;
        }
        else
        {
            String whereClause = "StudentID=";
            String[] whereArgs = {sStudentID};
           // Cursor cursor = m_DB.query ("train",null,whereClause,whereArgs,null,null,null);
            Cursor cursor = m_DB.rawQuery("SELECT * FROM train where StudentID = ?",new String[]{sStudentID});
            if(cursor.moveToNext())
            {
                StringBuffer sBuf = new StringBuffer();
                sBuf.append(cursor.getString(0));
                for(int j=1; j != 7; j++)
                {
                    sBuf.append("|");
                    sBuf.append(cursor.getInt(j));
                }
                // sBuf.
                System.out.println(sBuf.toString());
                rt = 0;
            }

        }
        return rt;
    }

    int DBGetStudentTrain(String sStudentID, StudentLogin_S Stu)
    {
        String whereClause = "StudentID=";
        String[] whereArgs = {sStudentID};
        // Cursor cursor = m_DB.query ("train",null,whereClause,whereArgs,null,null,null);
        Cursor cursor = m_DB.rawQuery("SELECT * FROM train where StudentID = ?",new String[]{sStudentID});
        if(cursor.moveToNext())
        {
            StringBuffer sBuf = new StringBuffer();
            sBuf.append(cursor.getString(0));
            for(int j=1; j != 7; j++)
            {
                sBuf.append("|");
                sBuf.append(cursor.getInt(j));
            }
            // sBuf.
            System.out.println(sBuf.toString());

            Stu.lTrainTimeTotality = cursor.getInt(1);
            Stu.lTrainMileTotality = cursor.getInt(2);
            Stu.lTrainTimePartTwoSimulate = cursor.getInt(3);
            Stu.lTrainTimePartTwoPratical = cursor.getInt(4);
            Stu.lTrainTimePartThreeSimulate = cursor.getInt(5);
            Stu.lTrainTimePartThreePratical = cursor.getInt(6);
        }
        return 0;
    }

    int DBAlter(String sStudentID, Column column,long sValue)
    {
        //实例化内容值
        ContentValues values = new ContentValues();
        //在values中添加内容
        values.put(m_sColum[column.ordinal()],sValue);
        //修改条件
        String whereClause = "StudentID=?";
        //修改添加参数
        String[] whereArgs={sStudentID};
        //修改
        m_DB.update("train",values,whereClause,whereArgs);

        return 0;
    }

    int DBDelete(String sStudentID)
    {

//删除条件
        String whereClause = "StudentID=?";
//删除条件参数
        String[] whereArgs = {sStudentID};
//执行删除
        m_DB.delete("train",whereClause,whereArgs);

        return 0;
    }

    int DBDropTable()
    {
        String str = "DROP TABLE train";
        m_DB.execSQL(str);
        return 0;
    }
    void DBClose()
    {
        m_DB.close();
    }
}
