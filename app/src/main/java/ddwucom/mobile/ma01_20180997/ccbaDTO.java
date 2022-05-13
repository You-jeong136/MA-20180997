package ddwucom.mobile.ma01_20180997;

import java.io.Serializable;

class ccbaDTO implements Serializable {
    private int _id;
    private String name;
    private String ccma;
    private String cNum;
    private String ccsi;
    private String admin;
    private Double lat;
    private Double lng;
    private String memo;
    private String date;

    public ccbaDTO() {

    }

    public ccbaDTO(int _id, String name, String ccma, String cNum, String ccsi,
                   String admin, Double lat, Double lng, String memo, String date) {
        this._id = _id;
        this.name = name;
        this.ccma = ccma;
        this.cNum = cNum;
        this.ccsi = ccsi;
        this.admin = admin;
        this.lat = lat;
        this.lng = lng;
        this.memo = memo;
        this.date = date;
    }

    public String getcNum() { return cNum; }

    public void setcNum(String cNum) { this.cNum = cNum; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getCcma() {
        return ccma;
    }

    public void setCcma(String ccma) {
        this.ccma = ccma;
    }

    public String getCcmaNum() {
        return cNum;
    }

    public void setCcmaNum(String cNum) {
        this.cNum = cNum;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCcsi() {
        return ccsi;
    }

    public void setCcsi(String ccsi) {
        this.ccsi = ccsi;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String ccmaToString() {
        return ccma + " 제 " + cNum + "호";
    }

    public String ccsiToSting() {
        return "서울특별시 " + ccsi;
    }
}

