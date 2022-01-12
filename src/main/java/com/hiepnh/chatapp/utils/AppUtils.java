package com.hiepnh.chatapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class AppUtils {

    private static final Logger logger = LoggerFactory.getLogger(AppUtils.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public static byte[] convertImgToByteArray(String pathName) {
        File file = new File(pathName);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
        WritableRaster raster = bufferedImage .getRaster();
        DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

        return data.getData();
    }

    public static Image convertByteArrayToImage(byte[] data){
        Image image;
        if(data == null){
            image = new Image(AppUtils.class.getClassLoader().getResource("ui/icons/default.png").toString(),50,50,true,true);
            return image;
        }
        InputStream is;
        try {
            is = new ByteArrayInputStream(data);
        } catch (Exception e) {
            logger.error("load avatar error : ", e);
            return null;
        }
        try {
            image = new Image(is);
            return image;
        }catch (Exception ex){
            logger.error("Load image : ", ex);
            return null;
        }
    }

    public static Image convertByteArrayToImage(byte[] data, int width, int height){
        Image image;
        if(data == null || data.length == 1){
             image = new Image(AppUtils.class.getClassLoader().getResource("ui/icons/default.png").toString(),50,50,true,true);
             return image;
        }
        InputStream is;
        try {
            is = new ByteArrayInputStream(data);
        } catch (Exception e) {
            logger.error("load avatar error : ", e);
            return null;
        }
        try {
            image = new Image(is,width,height,true,true);
            return image;
        }catch (Exception ex){
            logger.error("Load image : ", ex);
            return null;
        }
    }

    public static byte[] convertImageToByteArray(Image image){
        try {
             BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
             ByteArrayOutputStream s = new ByteArrayOutputStream();
             ImageIO.write(bImage, "png", s);
             return s.toByteArray();
        }catch (Exception ex){
            return  null;
        }
    }

    public static Image convertFileToImage(String path){
        File file = new File(path);
        try {
            Image image = new Image(file.toURI().toString());
            return image;
        }catch (Exception ex){
            return null;
        }
    }

    public static String convertByteArrayToString(byte[] data){
        return new String(data, StandardCharsets.UTF_8);
    }

    public static String convertTimeToString(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm ");
        return format.format(calendar.getTime());
    }

    public static String convertTimeToStringMessage(long time){
        long currTime = System.currentTimeMillis();
        String rs;
        Calendar currCalendar = Calendar.getInstance();
        currCalendar.setTimeInMillis(currTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        if(currCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
         && currCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
         && currCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)){
            SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");
            rs = format.format(calendar.getTime());
        }else if(currCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)){
            if(currCalendar.get(Calendar.WEEK_OF_YEAR) == currCalendar.get(Calendar.WEEK_OF_YEAR)){
                SimpleDateFormat format = new SimpleDateFormat("EEE hh:mm aa");
                rs = format.format(calendar.getTime());
            }else {
                SimpleDateFormat format = new SimpleDateFormat("dd MMM hh:mm aa");
                rs = format.format(calendar.getTime());
            }
        }else {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            rs = format.format(calendar.getTime());
        }
        return rs;
    }

    public static String convertTimeToStringMessage2(long time){
        long currTime = System.currentTimeMillis();
        String rs;
        Calendar currCalendar = Calendar.getInstance();
        currCalendar.setTimeInMillis(currTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        if(currCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && currCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && currCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)){
            SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");
            rs = format.format(calendar.getTime());
        }else if(currCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)){
            if(currCalendar.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR)){
                SimpleDateFormat format = new SimpleDateFormat("EEE");
                rs = format.format(calendar.getTime());
            }else {
                SimpleDateFormat format = new SimpleDateFormat("dd MMM");
                rs = format.format(calendar.getTime());
            }
        }else {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            rs = format.format(calendar.getTime());
        }
        return rs;
    }

    public static String convertTimeToString(long time, String pattern){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat(pattern);

        return format.format(calendar.getTime());
    }

    public static String convertToJson(Object input) {
        String rs;
        try {
            rs = mapper.writeValueAsString(input);
        } catch (Exception e) {
            rs = null;
            logger.error("Format obj : {}, err : {} ", input, e);
        }
        return rs;
    }

    public static <T> T convertJsonToObject(String json, Class<T> className) {
        T rs;
        try {
            rs = mapper.readValue(json, className);
        } catch (Exception e) {
            rs = null;
            logger.error("Format obj : {}, err : {} ", json, e);
        }
        return rs;
    }

    public static <T> T convertMapToObject(Map map,Class<T> className ){
        T rs;
        try {
            rs = mapper.convertValue(map, className);
        } catch (Exception e) {
            rs = null;
            logger.error("Format obj : {}, err : {} ", map, e);
        }
        return rs;
    }

    public static String parseString(Object obj) {
        if (obj == null) {
            return "";
        } else {
            try {
                return String.valueOf(obj);
            } catch (Exception var2) {
                return "";
            }
        }
    }

    public static int parseInt(Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof Double) {
            return ((Double) o).intValue();
        } else if (o instanceof Float) {
            return ((Float) o).intValue();
        } else {
            try {
                return Integer.parseInt(String.valueOf(o));
            } catch (Exception var2) {
                return 0;
            }
        }
    }
}
