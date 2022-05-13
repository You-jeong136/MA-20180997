package ddwucom.mobile.ma01_20180997;

import android.location.Location;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

class ccbaXMLParser {
    public static final String TAG = "ccbaXMLParser";

    public enum TagType { NONE, NAME, CCMA, CNUM, CCSI, ADMIN, LAT, LNG, CCBAASNO };

    final static String TAG_ITEM = "item";
    final static String TAG_NAME = "ccbaMnm1"; //문화재명
    final static String TAG_CCMA = "ccmaName"; //종목
    final static String TAG_CNUM = "crltsnoNm"; // 종목 지정 호수
    final static String TAG_CCSI = "ccsiName"; //시군구명
    final static String TAG_ADMIN = "ccbaAdmin"; //관리자 (소재지)
    final static String TAG_LAT = "latitude"; //위도
    final static String TAG_LNG = "longitude"; //경도
    final static String TAG_ASNO = "ccbaAsno"; //지정코드 _ id로 사용.

    Boolean FLAG = false; //근처에 있는지 여부 판단 플래그

    public ccbaXMLParser(){

    }

    public ArrayList<ccbaDTO> parse(String xml, Double MyLAT, Double MyLNG){
        ArrayList<ccbaDTO> resultList = new ArrayList<ccbaDTO>();
        ccbaDTO ccba = null;
        int lastIdx;

        TagType tagType = TagType.NONE;
        //test 용 _ 일단 반경 몇 미터~ 에 쓸 LAT, LNG 안쓰고 list에 값 잘 담기는지 먼저 확인.
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT :
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG :
                        if(parser.getName().equals(TAG_ITEM)){
                            ccba = new ccbaDTO();
                        } else if (parser.getName().equals(TAG_CCMA)){
                            if(ccba != null) tagType = TagType.CCMA;
                        } else if (parser.getName().equals(TAG_CNUM)){
                            if(ccba != null) tagType = TagType.CNUM;
                        } else if (parser.getName().equals(TAG_NAME)){
                            if(ccba != null) tagType = TagType.NAME;
                        } else if (parser.getName().equals(TAG_CCSI)){
                            if(ccba != null) tagType = TagType.CCSI;
                        } else if (parser.getName().equals(TAG_ADMIN)){
                            if(ccba != null) tagType = TagType.ADMIN;
                        } else if (parser.getName().equals(TAG_LAT)){
                            if(ccba != null) tagType = TagType.LAT;
                        } else if (parser.getName().equals(TAG_LNG)){
                            if(ccba != null) tagType = TagType.LNG;
                        } else if (parser.getName().equals(TAG_ASNO)){
                            if(ccba != null) tagType = TagType.CCBAASNO;
                        }
                        break;
                    case XmlPullParser.END_TAG :
                        if(parser.getName().equals(TAG_ITEM) && FLAG ){
                            resultList.add(ccba);
                            ccba = null;
                        }
                        break;
                    case XmlPullParser.TEXT :
                        //TagType { NONE, NAME, CCMA, CNUM, CCSI, ADMIN, LAT, LNG };
                        // 문자열 값에 경우 <![CDATA[ ~~ ]]> 의 느낌이기 때문에 substring 필요
                        switch(tagType) {
                            case CCMA :
                                ccba.setCcma(parser.getText());
                                break;
                            case CNUM :
                                ccba.setCcmaNum(parser.getText());
                                break;
                            case NAME:
                                ccba.setName( parser.getText());
                                break;
                            case CCSI:
                                ccba.setCcsi( parser.getText());
                                break;
                            case ADMIN:
                                ccba.setAdmin( parser.getText());
                                break;
                            case LNG:
                                ccba.setLng(Double.valueOf(parser.getText()));
                                break;
                            case LAT:
                                ccba.setLat(Double.valueOf(parser.getText()));
                                FLAG = IsCcbaNear(MyLAT, MyLNG, ccba.getLat(), ccba.getLng());
                                break;
                            case CCBAASNO:
                                ccba.set_id(Integer.parseInt(parser.getText()));
                                break;
                        }
                        tagType = TagType.NONE;
                        break;
                }
                eventType = parser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    Boolean IsCcbaNear(Double lat1, Double lng1, Double lat2, Double lng2) {
        Location l1 = new Location("location1");
        Location l2 = new Location("location2");

        l1.setLatitude(lat1);
        l1.setLongitude(lng1);

        l2.setLatitude(lat2);
        l2.setLongitude(lng2);

        float distance = l1.distanceTo(l2); //미터 단위
        Log.d(TAG, "distance : " + distance);
        if( distance <= 700){
            return true;
        }
        return false;
    }
}
