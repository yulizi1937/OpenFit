package com.jareddlc.openfit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.jareddlc.openfit.util.OpenFitData;
import com.jareddlc.openfit.protocol.OpenFitNotificationGeneralProtocol;
import com.jareddlc.openfit.protocol.OpenFitNotificationMessageProtocol;
import com.jareddlc.openfit.util.OpenFitDataType;
import com.jareddlc.openfit.util.OpenFitDataTypeAndString;
import com.jareddlc.openfit.util.OpenFitTimeZoneUtil;
import com.jareddlc.openfit.util.OpenFitVariableDataComposer;

import android.util.Log;

public class OpenFitApi {
    private static final String LOG_TAG = "OpenFit:OpenFitApi";

    public static byte[] getReady() {
        //000400000003000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)0);
        oVariableDataComposer.writeInt(OpenFitData.SIZE_OF_INT);
        oVariableDataComposer.writeInt(3);
        return oVariableDataComposer.toByteArray();
    }
    public static byte[] getUpdate() {
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte(OpenFitData.PORT_FOTA);
        oVariableDataComposer.writeInt(OpenFitData.SIZE_OF_INT);
        oVariableDataComposer.writeBytes("ODIN".getBytes());
        //oVariableDataComposer.writeByte((byte)79); // O
        //oVariableDataComposer.writeByte((byte)68); // D
        //oVariableDataComposer.writeByte((byte)73); // I
        //oVariableDataComposer.writeByte((byte)78); // N
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getUpdateFollowUp() {
        //640800000004020501
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte(OpenFitData.OPENFIT_DATA); // 100
        oVariableDataComposer.writeInt(OpenFitData.SIZE_OF_DOUBLE);
        oVariableDataComposer.writeByte((byte)4);
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeByte((byte)4);
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeByte((byte)5);
        oVariableDataComposer.writeByte((byte)1);
        return oVariableDataComposer.toByteArray();
    }
    
    public static byte[] getFotaCommand() {
        //4E020000000101
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte(OpenFitData.PORT_FOTA_COMMAND);
        oVariableDataComposer.writeInt(OpenFitData.SIZE_OF_SHORT);
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeByte((byte)1);
        return oVariableDataComposer.toByteArray();
    }
    
    public static byte[] getCurrentTimeInfo() {
        //011E0000000141CB3555F8FFFFFF000000000101010201A01DFC5490D43556100E0000
        //01
        //1e000000
        //01
        //41cb3555
        //f8ffffff
        //00000000
        //01
        //01
        //01
        //02
        //01
        //a01dfc54
        //90d43556
        //100e0000

        // build time data
        int millis = (int)(System.currentTimeMillis() / 1000L);
        Calendar oCalendar = Calendar.getInstance();
        TimeZone oTimeZone = oCalendar.getTimeZone();
        int i = oTimeZone.getRawOffset() / 60000;
        int j = i / 60;
        int k = i % 60;
        Date oDate = oCalendar.getTime();
        boolean inDaylightTime = oTimeZone.inDaylightTime(oDate);
        boolean useDaylightTime = oTimeZone.useDaylightTime();
        long l = oCalendar.getTimeInMillis();
        int m = (int)(OpenFitTimeZoneUtil.prevTransition(oTimeZone, l) / 1000L);
        int n = (int)(OpenFitTimeZoneUtil.nextTransition(oTimeZone, l) / 1000L);
        int dst = oTimeZone.getDSTSavings() / 1000;

        // write time data
        OpenFitVariableDataComposer oVDC = new OpenFitVariableDataComposer();
        oVDC.writeByte((byte)1);
        oVDC.writeInt(millis);
        oVDC.writeInt(j);
        oVDC.writeInt(k);
        oVDC.writeByte(OpenFitData.TEXT_DATE_FORMAT_TYPE);
        oVDC.writeBoolean(OpenFitData.IS_TIME_DISPLAY_24);
        oVDC.writeBoolean(inDaylightTime);
        oVDC.writeByte(OpenFitData.NUMBER_DATE_FORMAT_TYPE);
        oVDC.writeBoolean(useDaylightTime);
        oVDC.writeInt(m);
        oVDC.writeInt(n);
        oVDC.writeInt(dst);
        int length = oVDC.toByteArray().length;

        // write time byte array
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeInt(length);
        oVariableDataComposer.writeBytes(oVDC.toByteArray());
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getNotification() {
        //03
        //71000000 = size of msg
        //04 = DATA_TYPE_MESSAGE
        //0400000000000000 = size?
        //10 = sender name size + 2
        //FF
        //FE
        //4F00700065006E00460069007400 = OpenFit
        //16 = sender number size + 2
        //FF
        //FE
        //3500350035003100320033003400350036003700 = 5551234567
        //10 = msg title + 2
        //FF
        //FE
        //4E004F005400490054004C004500 = NOTITLE
        //28 = msg data + 2
        //00
        //FF
        //FE
        //570065006C0063006F006D006500200074006F0020004F00700065006E004600690074002100 = Welcome to OpenFit!
        //00
        //5E0E8955 = time stamp
        List mDataList = new ArrayList();
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, "OpenFit"));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, "5551234567"));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, "NOTITLE"));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.SHORT, "Welcome to OpenFit!"));

        long msgSize = mDataList.size();
        byte[] msg = OpenFitNotificationMessageProtocol.createNotificationProtocol(4, msgSize, mDataList, System.currentTimeMillis());
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)3);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenNotification(String sender, String number, String title, String message) {
        //03
        //71000000 = size of msg
        //04 = DATA_TYPE_MESSAGE
        //0400000000000000 = size?
        //10 = sender name size + 2
        //FF
        //FE
        //4F00700065006E00460069007400 = OpenFit
        //16 = sender number size + 2
        //FF
        //FE
        //3500350035003100320033003400350036003700 = 5551234567
        //10 = msg title + 2
        //FF
        //FE
        //4E004F005400490054004C004500 = NOTITLE
        //28 = msg data + 2
        //00
        //FF
        //FE
        //570065006C0063006F006D006500200074006F0020004F00700065006E004600690074002100 = Welcome to OpenFit!
        //00
        //5E0E8955 = time stamp
        /*List mDataList = new ArrayList();
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, sender));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, number));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, title));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.SHORT, message));

        long msgSize = mDataList.size();
        byte[] msg = OpenFitNotificationMessageProtocol.createNotificationProtocol(4, msgSize, mDataList, System.currentTimeMillis());
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)3);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();*/
        //return OpenFitApi.hexStringToByteArray("039d0000000c5f0200000000000024fffe63006f006d002e00660061006300650062006f006f006b002e006f0072006300610014fffe4d0065007300730065006e006700650072001cfffe5a0061006300680020004300680069006c00640065007200730000003400fffe79006f0075002000700061007400630068003f000a007000720065007400740079002000730069006d0070006c0065000a000167d03555");
        //OpenFitApi.hexStringToByteArray("037f0000000447160000000000001cfffe4a006f0068006e002000540068007500790020004d006100690016fffe3400300038003400370037003600380035003900003a00fffe540068006500200073006f0075006e00640074007200610063006b002000690073002000620061006400610073007300200074006f006f000113639c54");
        return OpenFitApi.hexStringToByteArray("038300000004b20400000000000028fffe4d0069006300680065006c006c0065002000570061007300680069006e00670074006f006e0016fffe350035003900320038003500340030003400380010fffe4e004f005400490054004c0045002200fffe4d00650072007200790020004300680072006900730074006d0061007300210000a8349c540309000000161b15000000000000");
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String hexStringToString(String hex){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for(int i=0; i<hex.length()-1; i+=2 ) {
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static int[] byteArrayToIntArray(byte[] bArray) {
      int[] iarray = new int[bArray.length];
      int i = 0;
      for(byte b : bArray) {
          iarray[i++] = b & 0xff;
      }
      return iarray;
    }
}

