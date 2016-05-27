package com.chenyg.oftendb.data;

import java.lang.annotation.Annotation;

import com.chenyg.wporter.annotation.DBAnnotation.Key;

public interface ParamsGetter
{
    Params getParams();

    public static class Params
    {
        private DataAble dataAble;
        private Class<? extends Annotation> keyClass;


        /**
         * 使用Key.class
         *
         * @param dataClass
         */
        public Params(Class<? extends DataAble> dataClass)
        {
            this(Key.class, dataClass);
        }

        /**
         * @param key
         * @param dataClass
         */
        public Params(Class<? extends Annotation> key, Class<? extends DataAble> dataClass)
        {
            this.set(key, dataClass);
        }

        public Params(DataAble dataAble)
        {
            this.set(null, dataAble);
        }

        /**
         * 使用Key.class
         *
         * @param dataClass
         */
        public void set(Class<? extends DataAble> dataClass)
        {
            set(Key.class, dataClass);
        }

        /**
         * @param key
         * @param dataClass
         */
        public void set(Class<? extends Annotation> key, Class<? extends DataAble> dataClass)
        {
            try
            {
                DataAble dataAble = dataClass.newInstance();
                set(key, dataAble);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        public void set(Class<? extends Annotation> key, DataAble dataAble)
        {
            this.keyClass = key;
            this.dataAble = dataAble;
        }

        /**
         * 得到集合（或表）名
         *
         * @return
         */
        public String getCollName()
        {
            return dataAble.getCollectionName();
        }

//        public Class<? extends DataAble> getDataClass()
//        {
//
//            return dataAble.getClass();
//        }

        DataAble newData()
        {
            return dataAble.cloneData();
        }

        public DataAble getDataAble()
        {
            return dataAble;
        }

        public Class<? extends Annotation> getKeyClass()
        {
            return keyClass;
        }
    }
}
