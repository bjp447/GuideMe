package com.guideme.myapplication;

public final class Crime
{
    public final int id;
    public final String date;
    public final String block;
    public final String iucr;
    public final String primaryType;
    public final String description;
    public final boolean arrest;
    public final String district;
    public final String beat;
    public final String community;
    public final String location;

//    public Crime(String district, String beat, String community, String date,
//                 String race, String fbi_code, String iucr, String primaryType, String description,
//                 String location)
//    {
//        this.district = district;
//        this.beat = beat;
//        this.community = community;
//        this.date = date;
//        this.iucr = iucr;
//        this.location = location;
//    }

    public Crime(String[] strings)
    {
        this.id = Integer.valueOf(strings[0]);
        this.date = strings[3];
        this.block = strings[4];
        this.iucr = strings[5];
        this.primaryType = strings[6];
        this.description = strings[7];
        this.arrest = Boolean.valueOf(strings[9]);
        this.beat = strings[11];
        this.district = strings[12];
        this.community = strings[14];
        this.location = strings[22];
    }

    public double getLat()
    {
        if (this.location.equals(""))
        {
            return 0;
        }

        String[] strings = this.location.split(",");
        String str = strings[0].replace('(', ' ');
        return Double.valueOf(str);
    }

    public double getLong()
    {
        if (this.location.equals(""))
        {
            return 0;
        }

        String[] strings = this.location.split(",");
        String str = strings[1].replace(')', ' ');
        return Double.valueOf(str);
    }

    public boolean hasLocation()
    {
        return (!this.location.equals(""));
    }

}
