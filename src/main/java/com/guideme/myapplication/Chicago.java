package com.guideme.myapplication;

public abstract class Chicago
{
    enum Area
    {
        DISTRICT(25), COMMUNITY(77), BEAT(2535), GEN(0); //GEN is for generated area definitions, ie. crimes within radius

        private int amount;

        Area(int amount)
        {
            this.amount = amount;
        }

        //public static final int DISTRCTS = 25;
        //public static final int COMMUNITIES = 77;
        //public static final int BEATS = 2535;

        public final int getAreasAmount()
        {
            return this.amount;
        }

        public static int getAreasAmount(Area area)
        {
            Area[] areas = values();
            for (Area are : areas)
            {
                if (area.equals(are))
                {
                    return are.hashCode();
                }
            }
            return 0;
        }

        public boolean isValidArea(int id)
        {
            return (id > 0 && id <= this.getAreasAmount());
        }

        /*
        public boolean isValidDistrict(int id)
        {
            return (id > 0 && id <= this.amount);
        }

        public boolean isValidBeat(int id)
        {
            return (id > 0 && id <= this.amount);
        }

        public boolean isValidCommunity(int id)
        {
            return (id > 0 && id <= this.amount);
        }
        */
    }
}
